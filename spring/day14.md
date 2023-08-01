### 웹 스코프

지금까지 싱글톤과 프로토타입 스코프를 학습했다. 싱글톤은 스프링 컨테이너의 시작과 끝까지 함께하는 매우 긴 스코프이고, 프로토타입은 생성과 의존관계 주입, 그리고 초기화까지만 진행하는 특별한 스코프이다.

이번엔 웹 스코프에 대해 알아보자.

#### 웹 스코프의 특징
* 웹 스코프는 웹 환경에서만 동작한다.
* 웹 스코프는 프로토타입과 다르게 스프링이 해당 스코프의 종료시점까지 관리한다. 따라서 종료 메서드가 호출된다

#### 웹 스코프 종류
* **request**: **HTTP 요청 하나**가 들어오고 나갈 때 까지 유지되는 스코프, 각각의 HTTP 요청마다 별도의 빈 인스턴스가 생성되고, 관리된다.
    * 아래 그림과 같이 동시에 요청이 들어올 때, 두 개의 빈 인스턴스가 생성된다.
* **session**: HTTP Session과 동일한 생명주기를 가지는 스코프
* **application**: 서블릿 컨텍스트( ServletContext )와 동일한 생명주기를 가지는 스코프
* **websocket**: 웹 소켓과 동일한 생명주기를 가지는 스코프

## HTTP request 요청 당 각각 할당되는 request 스코프
![image](https://github.com/jub3907/outSourcing/assets/58246682/edc4cedb-1d3b-4bec-9a39-3cc29fb4f27d)

### request 스코프 예제
#### 웹 환경 추가
웹 스코프는 웹 환경에서만 동작하므로, 라이브러리를 추가하자.
```
/web 라이브러리 추가
implementation 'org.springframework.boot:spring-boot-starter-web'
```
이제 `hello.core.CoreApplication`의 main 메소드를 실행해보면, 다음과 같이 웹 애플리케이션이 실행되는 것을 확인할 수 있다.

```
Tomcat started on port(s): 8080 (http) with context path ''
Started CoreApplication in 1.737 seconds (JVM running for 2.106)
```

> 참고: spring-boot-starter-web 라이브러리를 추가하면 스프링 부트는 내장 톰켓 서버를 활용해서 웹 서버와 스프링을 함께 실행시킨다.

> 참고: 스프링 부트는 웹 라이브러리가 없으면 우리가 지금까지 학습한 `AnnotationConfigApplicationContext` 을 기반으로 애플리케이션을 구동한다. 웹 라이브러리가 추가되면 웹과 관련된 추가 설정과 환경들이 필요하므로 `AnnotationConfigServletWebServerApplicationContext` 를 기반으로 애플리케이션을 구동한다.


만약 기본 포트인 8080 포트를 다른곳에서 사용중이어서 오류가 발생하면 포트를 변경해야 한다. 9090 포트로 변경하려면 다음 설정을 추가하자.
```JAVA
// main/resources/application.properties
server.port=9090
```

#### request 스코프 개발 예제

동시에 여러 HTTP 요청이 오면 정확히 어떤 요청이 남긴 로그인지 구분하기 어렵다. 이럴때 사용하기 딱 좋은것이 바로 request 스코프이다.


다음과 같이 로그가 남도록 request 스코프를 활용해서 추가 기능을 개발해보자.
```
[d06b992f...] request scope bean create
[d06b992f...][http://localhost:8080/log-demo] controller test
[d06b992f...][http://localhost:8080/log-demo] service id = testId
[d06b992f...] request scope bean close

```

* 기대하는 공통 포멧: `[UUID][requestURL] {message}`
* UUID를 사용해서 HTTP 요청을 구분하자.
* requestURL 정보도 추가로 넣어서 어떤 URL을 요청해서 남은 로그인지 확인하자.


먼저 코드로 확인해보자

#### MyLogger

```JAVA

@Component
@Scope(value = "request")
public class MyLogger {

    private String uuid;
    private String requestURL;

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public void log(String message) {
        System.out.println("[" + uuid + "]" + "[" + requestURL + "] " + message);
    }

    @PostConstruct
    public void init() {
        // 고객 요청이 들어올 때, uuid 생성
        uuid = UUID.randomUUID().toString();
        System.out.println("[" + uuid + "] request scope bean create: " + this);

    }

    @PreDestroy
    public void close() {
        System.out.println("[" + uuid + "] request scope bean close: " + this);
    }
}

```

* 로그를 출력하기 위한 `MyLogger` 클래스이다.
* `@Scope(value = "request")` 를 사용해서 request 스코프로 지정했다. 이제 **이 빈은 HTTP 요청 당 하나씩 생성**되고, HTTP 요청이 끝나는 시점에 소멸된다.
* 이 빈이 생성되는 시점에 자동으로 `@PostConstruct` 초기화 메서드를 사용해서 `uuid`를 생성해서 저장해둔다. 이 빈은 HTTP 요청 당 하나씩 생성되므로, `uuid`를 저장해두면 **다른 HTTP 요청과 구분**할 수 있다.
* 이 빈이 소멸되는 시점에 `@PreDestroy` 를 사용해서 종료 메시지를 남긴다.
* requestURL 은 이 빈이 생성되는 시점에는 알 수 없으므로, 외부에서 setter로 입력 받는다.

#### LogDemoController

```JAVA
@Controller
@RequiredArgsConstructor // 의존관계 자동 주입
public class LogDemoController {
    private final LogDemoService logDemoService;
    private final MyLogger myLogger;

    @RequestMapping("log-demo")
    // view 화면 없이 바로 문자를 반환
    @ResponseBody
    public String logDemo(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        myLogger.setRequestURL(requestURL);

        myLogger.log("controller test");
        logDemoService.logic("testId");
        return "OK";
    }

}
```
* 로거가 잘 작동하는지 확인하는 테스트용 컨트롤러다.
* `HttpServletRequest`를 통해서 요청 URL을 받았다.
    *requestURL 값 http://localhost:8080/log-demo
* 이렇게 받은 `requestURL` 값을 `myLogger`에 저장해둔다. `myLogger`는 HTTP 요청 당 각각 구분되므로 다른 HTTP 요청 때문에 값이 섞이는 걱정은 하지 않아도 된다.
* 컨트롤러에서 `controller test`라는 로그를 남긴다.

> 참고: requestURL을 MyLogger에 저장하는 부분은 컨트롤러 보다는 공통 처리가 가능한 스프링 인터셉터나 서블릿 필터 같은 곳을 활용하는 것이 좋다. 여기서는 예제를 단순화하고, 아직 스프링 인터셉터를 학습하지 않은 분들을 위해서 컨트롤러를 사용했다. 스프링 웹에 익숙하다면 인터셉터를 사용해서 구현해보자.


#### LogDemoService

```JAVA
@Service
@RequiredArgsConstructor
public class LogDemoService {

    private final MyLogger myLogger;

    public void logic(String id) {
        myLogger.log("service id = " + id);
    }
}

```

* 비즈니스 로직이 있는 서비스 계층에서도 로그를 출력해보자.
* 여기서 중요한점이 있다. `request scope`를 사용하지 않고 파라미터로 이 모든 정보를 서비스 계층에 넘긴다면, 파라미터가 많아서 지저분해진다. 더 문제는 requestURL 같은 웹과 관련된 정보가 웹과 관련없는 서비스 계층까지 넘어가게 된다. 웹과 관련된 부분은 컨트롤러까지만 사용해야 한다. 서비스 계층은 웹 기술에종속되지 않고, 가급적 순수하게 유지하는 것이 유지보수 관점에서 좋다.
* `request scope`의 MyLogger 덕분에 이런 부분을 파라미터로 넘기지 않고, MyLogger의 멤버변수에 저
장해서 코드와 계층을 깔끔하게 유지할 수 있다.

#### 기대하는 출력
```
[d06b992f...] request scope bean create
[d06b992f...][http://localhost:8080/log-demo] controller test
[d06b992f...][http://localhost:8080/log-demo] service id = testId
[d06b992f...] request scope bean close
```

#### 오류 발생
```
Error creating bean with name 'myLogger': Scope 'request' is not active for the
current thread; consider defining a scoped proxy for this bean if you intend to
refer to it from a singleton;
```
스프링 컨테이너가 뜰 때, 컨트롤러를 스프링 빈에 등록해야 한다. 이를 위해 의존관게 주입을 해 주려고 하지만, MyLogger의 Scope는 request이다.

request scope의 생존 범위는 요청이 생성되고, 완료될 때 까지이다. 즉, 의존관계 주입 시점에는 Request가 존재하지 않아, 의존 관계를 주입해줄 수 없다.

이는 이전에 배운 `Provider`를 사용하면 해결할 수 있다. 

### 스코프와 Provider

첫 번째 해결 방안은 앞서 배운 Provider를 사용하는 것이다. 
`ObjectProvider`를 사용해보자.

* LogDemoController
```JAVA
@Controller
@RequiredArgsConstructor // 의존관계 자동 주입
public class LogDemoController {
    private final LogDemoService logDemoService;
    private final ObjectProvider<MyLogger> myLoggerProvider;

    @RequestMapping("log-demo")
    // view 화면 없이 바로 문자를 반환
    @ResponseBody
    public String logDemo(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        // 필요한 시점에 주입 받는다.
        MyLogger myLogger = myLoggerProvider.getObject();
        myLogger.setRequestURL(requestURL);

        myLogger.log("controller test");
        logDemoService.logic("testId");
        return "OK";
    }

}
```
* LogDemoService
```JAVA
@Service
@RequiredArgsConstructor
public class LogDemoService {

    private final ObjectProvider<MyLogger> myLoggerProvider;

    public void logic(String id) {
        MyLogger myLogger = myLoggerProvider.getObject();
        myLogger.log("service id = " + id);
    }
}
```

* `localhost:8080/log-demo` 접속 시, 다음과 같은 로그가 출력되는걸 볼 수 있다.
```
[6633aa2c-852d-..] request scope bean create: hello.core.common.MyLogger@6d6eb1e3
[6633aa2c-852d-..][http://localhost:8080/log-demo] controller test
[6633aa2c-852d-..][http://localhost:8080/log-demo] service id = testId
[6633aa2c-852d-..] request scope bean close: hello.core.common.MyLogger@6d6eb1e3

```

* `ObjectProvider` 덕분에 `ObjectProvider.getObject()` 를 호출하는 시점까지 request scope 빈의
생성을 지연할 수 있다.
* `ObjectProvider.getObject()` 를 호출하시는 시점에는 HTTP 요청이 진행중이므로 request scope
빈의 생성이 정상 처리된다.
* **`ObjectProvider.getObject()` 를 `LogDemoController` , `LogDemoService` 에서 각각 한번씩 따로 호출해도 같은 HTTP 요청이면 같은 스프링 빈이 반환된다**.

### 스코프와 프록시
이번엔, 프록시 방식을 사용해보자.
```JAVA

@Component
// proxy 가짜를 생성한다?
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MyLogger {
    //...
}
```
* `MyLogger`에 `proxyMode = ScopedProxyMode.TARGET_CLASS` 를 추가해주자.
    * 적용 대상이 인터페이스가 아닌 클래스면 `TARGET_CLASS` 를 선택
    * 적용 대상이 인터페이스면 `INTERFACES` 를 선택
* 이렇게 하면 `MyLogger`의 가짜 프록시 클래스를 만들어두고 `HTTP request`와 상관 없이 가짜 프록시 클래스를 다른 빈에 미리 주입해 둘 수 있다.

이제, Controller와 Service는 `Provider` 적용 전으로 돌려 둔다면, `Provider`를 적용한 것 처럼 잘 동작하는걸 볼 수 있다. 어떻게 가능한지, 동작 원리를 알아보자.

#### 웹 스코프와 프록시 동작 원리

먼저, 주입된 myLogger를 확인해보자.
```JAVA
System.out.println("myLogger = " + myLogger.getClass());
```
**출력 결과**
```
myLogger = class hello.core.common.MyLogger$$EnhancerBySpringCGLIB$$bffbffb4
```
출력 결과에서도 볼 수 있듯, **CGLIB라는 라이브러리로 내 클래스를 상속 받은 가짜 프록시 객체를 만들어서 주입한다.**


* `@Scope` 의 `proxyMode = ScopedProxyMode.TARGET_CLASS)` 를 설정하면 스프링 컨테이너는 CGLIB라는 바이트코드를 조작하는 라이브러리를 사용해서, MyLogger를 상속받은 가짜 프록시 객체를 생성한다.
* 결과를 확인해보면 우리가 등록한 순수한 MyLogger 클래스가 아니라 `MyLogger$$EnhancerBySpringCGLIB` 이라는 클래스로 만들어진 객체가 대신 등록된 것을 확인할 수 있다.
* 그리고 스프링 컨테이너에 "myLogger"라는 이름으로 진짜 대신에 이 가짜 프록시 객체를 등록한다.
* `ac.getBean("myLogger", MyLogger.class)` 로 조회해도 프록시 객체가 조회되는 것을 확인할 수 있
다.
* 그래서 의존관계 주입도 이 가짜 프록시 객체가 주입된다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/10591dbe-edd5-43da-9641-a65b27b8ec25)

**가짜 프록시 객체는 요청이 오면 그때 내부에서 진짜 빈을 요청하는 위임 로직이 들어있다.**
* 가짜 프록시 객체는 내부에 진짜 myLogger를 찾는 방법을 알고 있다.
* 클라이언트가 `myLogger.logic()` 을 호출하면 사실은 가짜 프록시 객체의 메서드를 호출한 것이다.
* 가짜 프록시 객체는 request 스코프의 진짜 `myLogger.logic()` 를 호출한다.
* 가짜 프록시 객체는 원본 클래스를 상속 받아서 만들어졌기 때문에 이 객체를 사용하는 클라이언트 입장에서는 사실 원본인지 아닌지도 모르게, 동일하게 사용할 수 있다(다형성)

#### 동작 정리
* CGLIB라는 라이브러리로 내 클래스를 상속 받은 가짜 프록시 객체를 만들어서 주입한다.
* 이 가짜 프록시 객체는 실제 요청이 오면 그때 내부에서 실제 빈을 요청하는 위임 로직이 들어있다.
* 가짜 프록시 객체는 실제 request scope와는 관계가 없다. 그냥 가짜이고, 내부에 단순한 위임 로직만 있고, 싱글톤 처럼 동작한다

#### 특징 정리
* 프록시 객체 덕분에 클라이언트는 마치 싱글톤 빈을 사용하듯이 편리하게 request scope를 사용할 수 있다.
* 사실 Provider를 사용하든, 프록시를 사용하든 핵심 아이디어는 진짜 객체 조회를 꼭 필요한 시점까지 지연처리 한다는 점이다.
* 단지 애노테이션 설정 변경만으로 원본 객체를 프록시 객체로 대체할 수 있다. 이것이 바로 다형성과 DI 컨테이너가 가진 큰 강점이다.
* 꼭 웹 스코프가 아니어도 프록시는 사용할 수 있다.

#### 주의점
* 마치 싱글톤을 사용하는 것 같지만 다르게 동작하기 때문에 결국 주의해서 사용해야 한다.
* 이런 특별한 scope는 꼭 필요한 곳에만 최소화해서 사용하자, 무분별하게 사용하면 유지보수하기 어려워진다.