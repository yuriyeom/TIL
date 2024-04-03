##  Spring Security
  - Spring 기반 어플리케이션의 보안(인증과 권한, 인가 등)을 담당하는 스프링 하위 프레임워크
  - 개발자가 보안 관련 기능을 효율적이고 신속하게 구현할 수 있도록 돕는다.
  - Filter 기반으로 동작해서 Spring MVC와 분리되어 있다.
    
<br>

### Filter란

- Filter는 Spring의 기능이 아닌 Java 서블릿에서 제공하는 기능이다.
- 클라이언트와 서블릿 사이 request와 response의 중간에서 특정한 작업을 한다.
<br>

![image](https://github.com/yuriyeom/TIL/assets/43941336/7c61697a-c016-4550-9a1d-4f6a62c62fea)

<br>

### DelegatingFilterProxy

Filter는 서블릿이 제공하는 기술으로 스프링의 빈으로 등록하거나 정의된 빈을 주입받아 사용할 수 없다.

하지만 보안 정책을 관리하기 위해 빈 주입이 반드시 필요하다.


<br>

그래서 `DelegatingFilterProxy` 가 등장했다.


<br>

Spring Security는 `DelegatingFilterProxy`를 통해 서블릿 컨테이너에서 Filter로서 요청을 취득하고, 스프링 컨테이너에 존재하는 특정 빈을 찾아 요청을 위임한다. `DelegatingFilterProxy` 는 서블릿 컨테이너와 스프링 컨테이너의 다리 역할을 한다.

`DelegatingFilterProxy` 는 다음과 같이 FilterChain에 엮일 수 있다.   
<br>

![image](https://github.com/yuriyeom/TIL/assets/43941336/55deb2ec-4e71-4027-b1bc-b633423e16ac)

`DelegatingFilterProxy` 는 ApplicationContext에서 Bean Filter0를 찾아 실행한다.

<br>

아래 코드는 `DelegatingFilterProxy` 의 슈도코드이다.

```java
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
	Filter delegate = getFilterBean(someBeanName); // 1
	delegate.doFilter(request, response); // 2
}
```

1 : Filter 빈 인스턴스 참조를 지연시킬 수 있다. 이는 컨테이너를 시작하기 전에 Filter 인스턴스를 등록해야하기 때문에 중요하다. `DelegatingFilterProxy` 

2 :  스프링 빈에 작업을 위임한다.
<br>
<br>

![image](https://github.com/yuriyeom/TIL/assets/43941336/af10a841-bb7b-4858-b397-d2e31558da3e)

<br>

### FilterChainProxy

Spring Security는 `FilterChainProxy` 로 서블릿을 지원한다. `FilterChainProxy` 는 Spring Security가 제공하는 특별한 Filter로 SecurityFilterChain을 통해 여러 Filter 인스턴스로 위임할 수 있다. `FilterChainProxy` 는 빈이기 때문에 보통 DelegatingFilterProxy로 감싸져 있다.
<br>

![image](https://github.com/yuriyeom/TIL/assets/43941336/b6b9065a-063d-4cdb-a886-8f7e1750f789)

<br>

### SecurityFilterChain

FilterChainProxy가 요청에 사용할 Spring Security의 Filter들을 선택할 때 `SecurityFilterChain`을 사용한다.   
<br>

![image](https://github.com/yuriyeom/TIL/assets/43941336/6a4ccf11-d7f1-427a-8d87-3b16a2b25c0a)

<br>

사용할 SecurityFilterChain을 결정할 때도 FilterChainProxy를 사용한다. 
<br>

![image](https://github.com/yuriyeom/TIL/assets/43941336/f3798df8-0928-4942-850a-eb08b9784e83)


<br>

`FilterChainProxy` 은 가장 먼저 매칭된 SecurityFilterChain을 실행한다.

- /api/messages/ 요청 시 SecurityFilterChain(0)
    - /api/** 패턴과 가장 먼저 매칭되므로
- /messages/ 요청 시 SecurityFilterChain(n)
    - FilterChainProxy가 계속해서 SecurityFilterChain 찾는다.
    - 매칭되는 SecurityFilterChain이 없으므로 SecurityFilterChain(n) 실행

<br>

### Security Filters

Security Filter는 SecurityFilterChain API를 사용해서 FilterChainProxy에 추가한다. 이때 Filter 순서가 중요하다.

Filter Chain에 등록되는 Filter들은 Spring Security 설정에 따라 구성된다.

Filter 종류

- ChannelProcessingFilter : 웹 요청이 어떤 프로토콜(HTTP 또는 HTTPS)로 전달되어야 하는지 처리
- ConcurrentSessionFilter : 동시 접속을 허용할지 체크한다.
- WebAsyncManagerIntegrationFilter : 비동기 처리 시 파생되는 Thread들도 SecurityContext에 접근할 수 있도록 한다.
- SecurityContextPersistenceFilter : SecurityContextHodler에 SecurityContext를 제공한다.
- **HeaderWriterFilter** : Request의 Http 해더를 검사하여 header를 추가하거나 빼주는 역할을 한다.
- **CorsFilter** : 허가된 사이트나 클라이언트의 요청인지 검사하는 역할을 한다.
- **CsrfFilter** : Post나 Put과 같이 리소스를 변경하는 요청의 경우 내가 내보냈던 리소스에서 올라온 요청인지 확인한다.
- **LogoutFilter** : Request가 로그아웃하겠다고 하는것인지 체크한다.
- OAuth2AuthorizationRequestRedirectFilter
- Saml2WebSsoAuthenticationRequestFilter
- X509AuthenticationFilter
- AbstractPreAuthenticatedProcessingFilter
- CasAuthenticationFilter
- OAuth2LoginAuthenticationFilter
- Saml2WebSsoAuthenticationFilter
- **UsernamePasswordAuthenticationFilter** : username / password 로 로그인을 하려고 하는지 체크하여 승인이 되면 Authentication을 부여하고 이동 할 페이지로 이동한다.
- OpenIDAuthenticationFilter
- DefaultLoginPageGeneratingFilter : 로그인을 수행하는데 필요한 HTML을 생성함
- DefaultLogoutPageGeneratingFilter : 로그아웃을 수행하는데 필요한 HTML을 생성함
- DigestAuthenticationFilter
- **BearerTokenAuthenticationFilter** : Authorization 해더에 Bearer 토큰을 인증해주는 역할을 한다.
- **BasicAuthenticationFilter** : Authorization 해더에 Basic 토큰을 인증해주는 역할을 한다.
- **RequestCacheAwareFilter** : request한 내용을 다음에 필요할 수 있어서 Cache에 담아주는 역할을 한다. 다음 Request가 오면 이전의 Cache값을 줄 수 있다.
- **SecurityContextHolderAwareRequestFilter** : 보안 관련 Servlet 3 스펙을 지원하기 위한 필터
- JaasApiIntegrationFilter
- **RememberMeAuthenticationFilter** : 아직 Authentication 인증이 안된 경우라면 RememberMe 쿠키를 검사해서 인증 처리해준다.
- **AnonymousAuthenticationFilter** : 앞선 필터를 통해 인증이 아직도 안되었으면 해당 유저는 익명 사용자라고 Authentication을 정해주는 역할을 한다. (Authentication이 Null인 것을 방지!!)
- OAuth2AuthorizationCodeGrantFilter
- **SessionManagementFilter** : 서버에서 지정한 세션정책에 맞게 사용자가 사용하고 있는지 검사하는 역할을 한다.
- **ExcpetionTranslationFilter** : 해당 필터 이후에 인증이나 권한 예외가 발생하면 해당 필터가 처리를 해준다.
- **FilterSecurityInterceptor** : 사용자가 요청한 request에 들어가고 결과를 리턴해도 되는 권한(Authorization)이 있는지를 체크한다. 해당 필터에서 권한이 없다는 결과가 나온다면 위의 ExcpetionTranslationFilter필터에서 Exception을 처리해준다.
- SwitchUserFilter

<br>

### 설정 파일을 만들어서 Filter 관리

많은 예제에서 WebSecurityConfigurerAdapter을 상속해서 설정 파일을 만들도록 알려주는데 Deprecated 되었다.
```java
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  // ...
}

⇒ 사용 X

```

SecurityFilterChain을 Bean으로 등록해서 사용해야 한다.

```java
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 인증, 인가 관련 API 설정
        // Filter 세팅
        return http.build();
    }

}

```

<실제 사용한 설정 파일>
```java
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    private final JwtFilter jwtFilter;
    private final JwtExceptionFilter jwtExceptionFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 허용 설정
                .cors()

                .and()
                // CSRF : 정상적인 사용자가 의도치 않은 위조 요청을 보내는 것
                // REST API는 stateless하여 서버에 인증 정보를 저장하지 않고 요청에 인증정보를 포함시키 때문에 불필요
                .csrf().disable()
                .httpBasic().disable()
                .formLogin().disable() // FormLogin 사용하지않음

                // URL 권한 설정
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/v1/auth/users").authenticated()
                .antMatchers(HttpMethod.PUT, "/v1/auth/stores").authenticated()
                .antMatchers(HttpMethod.POST, "/v1/reviews").authenticated()
                .anyRequest().permitAll()

                // JWT 설정
                .and()
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, JwtFilter.class)

                // 예외 설정
                .exceptionHandling()
                // 인증이 되지 않은 유저가 요청 시 401
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                // 필요한 권한없이 요청 시 403
                .accessDeniedHandler(jwtAccessDeniedHandler)

                // 세션을 사용하지 않으므로 STATELESS로 설정
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                // 소셜 로그인 설정
                .and()
                .oauth2Login()
                .successHandler(oAuth2LoginSuccessHandler)// 동의하고 계속하기를 눌렀을때,
                .userInfoEndpoint()
                .userService(customOAuth2UserService);// userService 설정

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*"); // 허용할 URL
        config.addAllowedHeader("*"); // 허용할 Header
        config.addAllowedMethod("*"); // 허용할 Http Method
        config.addExposedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}

```
<br>

- CORS
    - 한 출처에서 실행중인 웹 애플리케이션이 다른 출처의 리소스에 접근할 수 있는 권한을 부여하는 정책
    - 출처
        - 프로토콜(http), 호스트(naver.com), 포트번호(80)
    - 필요한 이유
        - 출처가 다른 어플리케이션이 소통을 하게 되면 한 사이트를 모방하여 사용자의 정보를 탈취할 수 있다.
    - CORS 설정은 API 서버에서 HTTP 응답 헤더에 "Access-Control-Allow-Origin" 항목을 추가하여 허용할 도메인을 지정하는 것으로 가능합니다.
        - Access-Control-Allow-Origin: [https://example.com](https://example.com/)
- CSRF
    - 정상적인 사용자가 의도치 않은 위조 요청을 보내는 것
    - 쿠키 기반 서버의 세션 정보를 획득할 수 있을 때
    - REST API는 stateless해서 서버에 인증정보 보관하지 않음
        - 세션을 이용하지 않고 JWT 이용하므로 불필요
- HTTP METHOD
    - GET : 리소스 조회
    - POST : 요청 데이터 처리. 주로 등록에 사용
    - PUT : 리소스 대체. 없으면 생성
    - PATCH : 리소스 부분 수정
    - DELETE : 리소스 삭제
    
    - GET VS POST
        - GET
            - 쿼리스트링 노출
            - 멱등성 O → 여러번 조회시 같은 결과값 보장 O
            - 캐싱해서 조회 속도 우수
        - POST
            - 메세지 BODY에 데이터. 길이 제한 없음
            - 멱등성 X → 여러번 조회시 같은 결과값 보장 X
    - 멱등성 : 여러번 조회했을때 같은 결과값
        - GET, PUT, DELETE 보장
        - POST, PATCH 보장X

- Exception
    - authenticationEntryPoint
        - 인증 실패
        - 인증 처리 과정에서 예외가 발생한 경우 예외를 핸들링하는 인터페이스
        - 인증 예외가 생겼을 때 어떻게 할 것인가
        - 유효한 자격증명을 제공하지 않고 접근하려 할 때, 401
    - accessDeniedHandler
        - 인가 실패
        - 필요한 권한없이 접근하려 할 때, 403
        
- OAuth2
	- successHandler
	    authentication 객체에서 OAuth2User를 꺼낸다.	    
	    accessToken, refreshToken 발급	    
	    사용자 저장 또는 업데이트	    
	    role에 따라 처음 사용자는 추가 가입정보 입력페이지로 기존 사용자는 홈화면으로    
	- userService
	    1. access token을 이용해 서드파티 서버로부터 사용자 정보를 받아온다.
	    2. 해당 사용자가 이미 회원가입 되어있는 사용자인지 확인한다.만약 회원가입이 되어있지 않다면, 회원가입 처리한다.
	    3. 세션 방식에서는 여기서 return한 객체가 시큐리티 세션에 저장된다.
	        하지만 JWT 방식에서는 저장하지 않는다.

<br>

### Spring Security 아키텍쳐
![image](https://github.com/yuriyeom/TIL/assets/43941336/6c65a4b3-f632-4194-ac19-6d2081e00e2c)
<br>
1. Http Request 수신
    
    사용자가 로그인 정보와 함께 인증 요청을 한다.
    
2. 유저 자격을 기반으로 인증토큰 생성
    
    AuthenticationFilter가 요청을 가로채고, 가로챈 정보를 통해 UsernamePasswordAuthenticationToken의 인증용 객체를 생성한다.
    
3. Filter를 통해 AuthenticationToken을 AuthenticationManager로 위임
    
    AuthenticationManager의 구현체인 ProviderManager에게 생성한 UsernamePasswordToken 객체를 전달한다.
    
4. AuthenticationProvider의 목록으로 인증 시도
    
    AuthenticationManager는 등록된 AuthenticationProvider(들)을 조회하여 인증을 요구한다.
    
5. UserDetailsService의 요구
    
    실제 DB에서 사용자 인증정보를 가져오는 UserDetailsService에 사용자 정보를 넘겨준다.
    
6. UserDetails를 이용해 User 객체에 대한 정보 탐색
    
    넘겨받은 사용자 정보를 통해 DB에서 찾은 사용자 정보인 UserDetails 객체를 만든다.
    
7. User 객체의 정보들을 UserDetails가 UserDetailsService로 전달
    
    AuthenticationProvider(들)은 UserDetails를 넘겨받고 사용자 정보를 비교한다.
    
8. 인증 객체 또는 AuthenticationException
    
    인증이 완료되면 권한 등의 사용자 정보를 담은 Authentication 객체를 반환한다.
    
9. 인증 끝
    
    다시 최초의 AuthenticationFilter에 Authentication 객체가 반환된다.
    
10. SecurityContext에 인증 객체 설정
    
    Authenticaton 객체를 SecurityContext에 저장한다.

<br>

최종적으로 SecurityContextHolder는 세션 영역에 있는 SecurityContext에 Authentication 객체를 저장한다.

사용자 정보를 저장한다는 것은 Spring Security가 전통적인 세션-쿠키 기반의 인증 방식을 사용한다는 것을 의미한다.
