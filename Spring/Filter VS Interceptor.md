# Filter VS Interceptor

### Filter란

- Filter는 Spring의 기능이 아닌 Java 서블릿에서 제공하는 기능이다.
- 클라이언트와 서블릿 사이 request와 response의 중간에서 특정한 작업을 한다.

  <br>
  
 ![image](https://github.com/yuriyeom/TIL/assets/43941336/2baeea00-7fd0-4fb5-8bda-6da79efcff22)

  <br>
    
- 클라이언트가 어플리케이션으로 요청을 보내면 컨테이너는 Servlet과 여러 Filter로 구성된 FilterChain을 만들어 요청 URI path 기반으로 HttpServletRequest를 처리한다.
- 스프링 MVC 어플리케이션에서의 Servlet은 DispatcherServlet이다.
- 하나의 서블릿은 하나의 HttpServletRequest, HttpServletResponse를 처리한다.
- 하지만 Filter는 다음과 같은 방식으로 여러 개 사용할 수 있다.
    - 하향 필터 혹은 서블릿이 호출되는 것을 막는다. 이 경우 Filter에서 HttpServletResponse를 작성한다.
    - 하향 필터 혹은 서블릿이 사용하는 HttpServletRequest, HttpServletResponse를 수정한다.
- Filter는 FilterChain 안에 있을 때 효력을 발휘한다.
    - FilterChain 사용 예시
        
        ```java
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        	// do something before the rest of the application
            chain.doFilter(request, response); // invoke the rest of the application
            // do something after the rest of the application
        }
        ```
        
    - Filter는 하향에 있는 Filter와 서블릿에서만 영향을 주기 때문에 Filter의 순서는 매우 중요하다.
- Filter를 추가하려면 Filter 인터페이스를 구현해야 하며 다음과 같은 메소드를 가진다.
    
    ```java
    public interface Filter {
    
        public default void init(FilterConfig filterConfig) throws ServletException {}
    
    		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    				// 이 곳에 원하는 작업 추가
    		    chain.doFilter(request, response); 
    				// 이 곳에 원하는 작업 추가
    		}
    		
        public default void destroy() {}
    }
    ```
    
    1. init() : Filter 객체를 초기화하고 서비스에 추가하기 위한 메소드
    2. doFilter() :  URI patatern에 맞는 모든 HTTP 요청이 DispatcherServlet으로 전달되기 전에 실행되는 메소드. FilterChain 파라미터로 다음 대상으로 요청을 전달한다. chain.doFilter 전후로 원하는 작업을 추가할 수 있다.
    3. destroy() : Filter 객체를 종료하고 자원을 반환하는 메소드

<br>
<br>

### Interceptor란

- 요청과 응답을 가로채서 특정한 작업을 한다.
- Spring MVC가 제공하는 기능이며 Spring Context 내 서블릿과 컨트롤러 사이에서 동작한다.
- DispatcherServlet이 핸들러 매핑에게 요청을 처리하는 핸들러를 찾도록 요청하면, 핸들러 실행체인이 동작해서 핸들러 인터셉터를 거쳐서 컨트롤러를 실행한다. (인터셉터 없으면 곧바로 컨트롤러 실행)
- 인터셉터를 추가하려면 HandlerInterceptor 인터페이스를 구현해야 하며 다음과 같은 메소드를 가진다.
    
    ```java
    public interface HandlerInterceptor 
    
        default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
    	throws Exception {
            return true;
        }
        
        default void postHandle(HttpServletRequest request, HttpServletResponse response,
    	Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        }
            
        default void afterCompletion(HttpServletRequest request, HttpServletResponse response,
    	Object handler, @Nullable Exception ex) throws Exception {
        }
    }
    ```
    
    1. preHandle() : 컨트롤러 호출 전에 실행된다. 리턴이 true이면 계속 진행, false이면 진행을 멈춘다.
    2. postHandle() : 컨트롤러 호출 후에 실행된다.
    3. afterCompletion() : 모든 작업이 완료 된 후 view가 렌더링된 후에 실행된다.

### Filter VS Interceptor
![image](https://github.com/yuriyeom/TIL/assets/43941336/6a726871-be8e-41d4-9ffa-5c2d7f9f2e5d)

<br>

|  | Filter | Interceptor |
| ---- | ---- | ---- |
| 관리하는 컨테이너 | Servlet Container | Spring Container |
| 실행 위치 | Web Context | Spring Context |
| Request/Response 조작 | 가능 | 불가능 |
| 용도 | 웹 애플리케이션 전역적으로 처리하는 작업 | 요청과 관련되어 처리해야 하는 작업 |
| |- 공통된 보안 및 인증/인가 작업 | - 세부적인 보안 및 인증/인가 작업|
| |- 모든 요청에 대한 로깅 |- API 호출에 대한 로깅 |
| |- 이미지/데이터 압축 및 문자열 인코딩 |- 컨트롤러로 넘겨주는 데이터의 가공 |
| |- Spring과 분리하려는 기능 | |
