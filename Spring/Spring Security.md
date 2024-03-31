##  Spring Security
  - Spring 기반 어플리케이션의 보안(인증과 권한, 인가 등)을 담당하는 스프링 하위 프레임워크
  - Filter 기반으로 동작해서 Spring MVC와 분리되어 있다.
    
<br>
    
### Filter의 위치
        
![image](https://github.com/yuriyeom/TIL/assets/43941336/79163709-ba73-496e-9f59-579f37e65b8f)

<br>

### Spring Security에서 Filter 역할   
        
![image](https://github.com/yuriyeom/TIL/assets/43941336/1270f2a3-54b2-4754-aa60-0378515f1df0)

        
- Filter는 스프링에서 정의된 빈을 주입받아 사용할 수없다. 하지만 보안 정책을 관리하기 위해 빈 주입이 반드시 필요하다.
- Spring Security는 `DelegatingFilterProxy`를 통해 서블릿 컨테이너에서 Filter로서 요청을 취득하고, 스프링 컨테이너에 존재하는 특정 빈을 찾아 요청을 위임한다.
- `DelegatingFilterProxy` 는 서블릿 컨테이너와 스프링 컨테이너의 다리 역할
- Filter Chain에 등록되는 Filter들은 Spring Security 설정에 따라 구성된다.
- Filter 종류
    - **HeaderWriterFilter** : Request의 Http 해더를 검사하여 header를 추가하거나 빼주는 역할을 한다.
    - **CorsFilter** : 허가된 사이트나 클라이언트의 요청인지 검사하는 역할을 한다.
    - **CsrfFilter** : Post나 Put과 같이 리소스를 변경하는 요청의 경우 내가 내보냈던 리소스에서 올라온 요청인지 확인한다.
    - **LogoutFilter** : Request가 로그아웃하겠다고 하는것인지 체크한다.
    - **UsernamePasswordAuthenticationFilter** : username / password 로 로그인을 하려고 하는지 체크하여 승인이 되면 Authentication을 부여하고 이동 할 페이지로 이동한다.
    - **ConcurrentSessionFilter** : 동시 접속을 허용할지 체크한다.
    - **BearerTokenAuthenticationFilter** : Authorization 해더에 Bearer 토큰을 인증해주는 역할을 한다.
    - **BasicAuthenticationFilter** : Authorization 해더에 Basic 토큰을 인증해주는 역할을 한다.
    - **RequestCacheAwareFilter** : request한 내용을 다음에 필요할 수 있어서 Cache에 담아주는 역할을 한다. 다음 Request가 오면 이전의 Cache값을 줄 수 있다.
    - **SecurityContextHolderAwareRequestFilter** : 보안 관련 Servlet 3 스펙을 지원하기 위한 필터라고 한다.
    - **RememberMeAuthenticationFilter** : 아직 Authentication 인증이 안된 경우라면 RememberMe 쿠키를 검사해서 인증 처리해준다.
    - **AnonymousAuthenticationFilter** : 앞선 필터를 통해 인증이 아직도 안되었으면 해당 유저는 익명 사용자라고 Authentication을 정해주는 역할을 한다. (Authentication이 Null인 것을 방지!!)
    - **SessionManagementFilter** : 서버에서 지정한 세션정책에 맞게 사용자가 사용하고 있는지 검사하는 역할을 한다.
    - **ExcpetionTranslationFilter** : 해당 필터 이후에 인증이나 권한 예외가 발생하면 해당 필터가 처리를 해준다.
    - **FilterSecurityInterceptor** : 사용자가 요청한 request에 들어가고 결과를 리턴해도 되는 권한(Authorization)이 있는지를 체크한다. 해당 필터에서 권한이 없다는 결과가 나온다면 위의 ExcpetionTranslationFilter필터에서 Exception을 처리해준다.
        
<br>

###  설정 파일을 만들어서 Filter 관리
        
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

<br>
  
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

 ### Spring Security 아키텍쳐

 ![image](https://github.com/yuriyeom/TIL/assets/43941336/304275f3-1748-42ef-a49f-6e6a0db71f38)

1. 사용자가 로그인 정보와 함께 인증 요청을 한다.(Http Request)

2. AuthenticationFilter가 요청을 가로채고, 가로챈 정보를 통해 UsernamePasswordAuthenticationToken의 인증용 객체를 생성한다.

3. AuthenticationManager의 구현체인 ProviderManager에게 생성한 UsernamePasswordToken 객체를 전달한다.

4. AuthenticationManager는 등록된 AuthenticationProvider(들)을 조회하여 인증을 요구한다.

5. 실제 DB에서 사용자 인증정보를 가져오는 UserDetailsService에 사용자 정보를 넘겨준다.

6. 넘겨받은 사용자 정보를 통해 DB에서 찾은 사용자 정보인 UserDetails 객체를 만든다.

7. AuthenticationProvider(들)은 UserDetails를 넘겨받고 사용자 정보를 비교한다.

8. 인증이 완료되면 권한 등의 사용자 정보를 담은 Authentication 객체를 반환한다.

9. 다시 최초의 AuthenticationFilter에 Authentication 객체가 반환된다.

10. Authenticaton 객체를 SecurityContext에 저장한다.

<br>

최종적으로 SecurityContextHolder는 세션 영역에 있는 SecurityContext에 Authentication 객체를 저장한다.

사용자 정보를 저장한다는 것은 Spring Security가 전통적인 세션-쿠키 기반의 인증 방식을 사용한다는 것을 의미한다.
