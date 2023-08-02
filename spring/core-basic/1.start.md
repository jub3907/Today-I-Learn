## Spring 강의 1일차 [링크](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%EC%9E%85%EB%AC%B8-%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8/lecture/48553?tab=curriculum&speed=1.5)
> JAVA 설치/ 프로젝트 생성/ 프로젝트 실행 및 빛드 
### JAVA 설치
* Version : 11
* IDE : IntelliJ

### 프로젝트 생성
#### start.spring.io
* Project
  * Maven / Gradle, 초보자는 Gradle 추천. 
* 지금은, gradle에 대해선 자세하게 알기보단, 버전 설정 및 라이브러리 설치를 위한 툴이라는 정도만 이해하고 넘어감.
* Dependencies : 프로젝트 의존성 설정
  * Spring Web, Thymeleaf 라이브러리 추가
* 프로젝트 이름은 hellospring으로 설정


### 프로젝트 실행
* HelloSpringApplication (Main Method) 실행시, Spring Boot Application 실행됨.
* Tomcat 웹 서버를 자체적으로 내장하고 있기 때문에, 8080포트에서 웹서버 실행.


### 라이브러리
#### 스프링 부트 라이브러리
* spring-boot-starter-web
  * spring-boot-starter-tomcat: 톰캣 (웹서버)
  * spring-webmvc: 스프링 웹 MVC
* spring-boot-starter-thymeleaf: 타임리프 템플릿 엔진(View)
* spring-boot-starter(공통): 스프링 부트 + 스프링 코어 + 로깅
  * spring-boot
    * spring-core
  * spring-boot-starter-logging
    * logback, slf4j

### View 환경설정
#### Welcome Page 만들기

``` html
# resources/static/index.html
<!DOCTYPE HTML>
<html>
    <head>
        <title>Hello</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    </head>
    <body>
        Hello
        <a href="/hello">hello</a>
    </body>
</html>
```

* static/index.html 파일을 생성해주어 Home (/ ) 경로 접근 시 출력되는 화면 생성.
인덱스 페이지(웰컴 페이지)는 정적이며, 템플릿화 되어 있음.

#### thymeleaf 템플릿 엔진
* thymeleaf 공식 사이트: https://www.thymeleaf.org/
* 스프링 공식 튜토리얼: https://spring.io/guides/gs/serving-web-content/
* 스프링부트 메뉴얼: https://docs.spring.io/spring-boot/docs/2.3.1.RELEASE/reference/html/spring-boot-features.html#boot-features-spring-mvc-template-engines

#### Controller 생성
* Controller : 요청에 대한 진입점.
* @GetMapping : 웹 Application의 요청에 맞게 호출 할 메소드를 지정하는 데코레이터. 쉽게 말해, getMapping으로 사용한 파라미터로 페이지네이션이 가능하다?

### 동작 환경 그림
![image](https://user-images.githubusercontent.com/58246682/145021250-742fbd62-275b-4703-a653-9823e6853f8f.png)
* 컨트롤러에서 리턴 값으로 **문자**를 반환하면 **view resolver가 화면**을 찾아 처리한다.
  * spring boot 템플릿 엔진 기본 viewName 매핑.
  * `resources:templates/` + `{View Name}` + `.html`
  * 1일차의 내용에서, /hello라고 진입할 경우 HelloController의 모델 메소드(`addAttribute`)로 더해준 data : hello!! 값을 해당 페이지 템플릿에서 사용할 수 있게 된다.
```
public class HelloController {
    // ./hello 페이지로 진입했을 때, 실행된다.
    @GetMapping("hello")
    public String hello(Model model) {
        // model에 data: hello!! 값을 넣어준다.
        model.addAttribute("data", "hello!!");
        // resources의 hello를 찾아, 해당 페이지를 렌더링 해라.
        // 이를 View Name이라 칭하는 걸로 보임.
        return "hello";
    }
}
```

* 페이지의 th는 타임리프 문법을 사용하기 위해 넣어줌.

> 참고: spring-boot-devtools 라이브러리를 추가하면, html 파일을 컴파일만 해주면 서버 재시작 없이 View 파일 변경이 가능하다.

#### 프로젝트 빌드 및 실행
* `./gradlew build`
* `cd build`
* `cd libs`
* `java -jar hello-spring-0.0.1-SNAPSHOT.jar`

