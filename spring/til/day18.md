## 스프링 웹 MVC
### 웹 서버, 웹 애플리케이션 서버
웹은 모두 HTTP 기반으로 통신을 한다.

예를 들어, 웹 브라우저에서 URL을 치면 서버에 접속하고, 서버에선 HTML을 클라이언트에 내려주어, 클라이언트에서 보여준다.\
이 모든 통신에서, HTTP를 사용한다.

### 모든 것이 HTTP
HTML, Text, Image, JSON등, 거의 모든 형태의 데이터를 전송 가능하다. \
심지어, 서버 간 데이터를 주고 받을때도 대부분 HTTP를 사용한다.

### 웹 서버 Web Server
웹 서버란, HTTP 기반으로 동작하는 서버를 의미한다. \
HTML, CSS, 이미지 등의 정적 리소스를 제공하고, 기타 부가기능을 제공한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/a3d078dd-42e7-42aa-b517-fb45da03ecb1)

Nginx, Apache등의 웹 서버가 주로 사용된다.

### 웹 애플리케이션 서버 WAS, Web Application Server
HTTP를 기반으로 동작하며, 웹 서버의 기능을 포함하지만\
프로그램 코드를 실행해 동적 HTML을 보여주거나, Rest API 등의 애플리케이션 로직을 수행할 수 있다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/1e6e8584-2742-49bb-87c2-d8092b329fa4)

톰캣(Tomcat), Undertow등의 WAS가 존재한다.

### 웹 서버와 웹 애플리케이션 서버의 차이
웹 서버는 정적 리소스(파일), WAS는 애플리케이션 로직까지 실행할 수 있다고 이해하면된다.

하지만, 둘의 용어 자체도 경계가 모호하다. \
웹 서버도 프로그램을 실행하는 기능을 포함하기도 하고,\
웹 애플리케이션 서버도 웹 서버의 기능을 제공한다. 

자바는 서블릿 컨테이너 기능을 제공하면 WAS라고 하며, \
WAS는 애플리케이션 코드를 실행하는데에 더 특화되어 있다.

### 웹 시스템 구성 - WAS, DB
WAS는 정적 리소스와 애플리케이션 로직 모두 제공할 수 있으므로, \
WAS와 dB만으로도 시스템을 구축할 수 있다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/6fc45e0b-a887-4b44-87ea-c356813afc8c)

하지만 WAS가 너무 많은 역할을 담당하기 떄문에 서버 과부하의 우려가 존재하고,\
가장 비싼(로직이 많은) 애플리케이션 로직이 정적 리소스때문에 수행이 어려울 수 있다.\
또한, WAS 장애시 오류 화면도 노출할 수 없다는 단점이 존재한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/14eccd47-e2a2-4b22-b946-c1e74d5bcd7c)


### 웹 시스템 구성 - WEB, WAS, DB
WEB, WAS, DB 세 가지를 사용해 웹 시스템을 구성한다면,\
WAS는 애플리케이션 로직을, 웹 서버는 정적 리소스를 담당한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/67389b61-f646-4b44-af96-af2d1f5426e5)

위와 같은 구조에서, 정적 리소스가 많이 사용 된다면 Web 서버만 증설하면 된다.\
또한, 애플리케이션 리소스가 많이 사용되면 Was를 증설한다.\
이를 통해 효율적인 리소스 관리가 가능하다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/6efafdde-96ba-4911-bdbf-e20ac68406ca)

정적 리소스만 제공하는 웹 서버는 잘 죽지 않는다. \
하지만, 애플리케이션 로직이 동작하는 WAS는 여러가지 이유로 잘 죽는다.\
이렇게 WAS, DB의 장애 상황에서 웹 서버가 오류 화면을 제공할 수 있다는 장점이 있다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/73a58f5c-b75a-4692-86d3-997b86f00cb2)


## 서블릿
다음과 같은 POST 전송 - 저장 기능을 하는 HTML 폼이 있다고 가정하자.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/355aefd6-8b26-4849-b287-6a9233df6378)

유저가 전송 버튼을 클릭했을 때, 서버는 다음과 같은 업무를 처리해야 한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/69581cd3-c0e5-4880-bd70-45bde29d8927)

이러한 업무 중 **초록색으로 강조한** 업무가 바로 **의미있는 비즈니스 로직**을 의미한다. \
간단한 비스니스 로직을 위해 너무나도 많은 업무를 추가로 수행해야 하는건 너무나도 비효율적이기 때문에,\
바로 **서블릿**이 만들어지게 되었다.

서블릿을 사용하게 되면 비즈니스 로직을 제외만 모든 부분을 자동화 해준다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/44b8f92b-13b5-449e-b4a7-9b77353e2e32)

### 서블릿의 특징
```java
@WebServlet(name = "helloServlet", urlPatterns = "/hello")
public class HelloServlet extends HttpServlet {

   @Override
   protected void service(HttpServletRequest request, HttpServletResponse response){
     //애플리케이션 로직
   }
}
```
* urlPatterns(/hello)의 URL이 호출되면 서블릿 코드가 실행된다.
* HTTP **요청 정보**를 편리하게 사용할 수 있는 `HttpServletRequest`
* HTTP **응답 정보**를 편리하게 제공할 수 있는 `HttpServletResponse`
* 개발자는 HTTP 스펙을 매우 편리하게 사용할 수 있다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/91f0d0c1-043a-4c91-96d2-54ba94a0de3b)

### 서블릿의 HTTP 요청, 응답 흐름
1. HTTP 요청시, WAS는 Request, Response 객체를 새로 만들어 서블릿 객체를 호출한다.
2. 개발자는 Request 객체에서 HTTP 요청 정보를 편리하게 꺼내서 사용한다.
3. 개발자는 Response 객체에 HTTP 응답 정보를 편리하게 입력한다.
4. WAS는 Response 객체에 담겨있는 내용으로 HTTP 응답 정보를 생성한다.

### 서블릿 컨테이너
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/7dcad72c-c6e8-44c0-bb2b-42fa23a02df3)

톰캣과 같이, 서블릿을 지원하는 WAS를 서블릿 컨테이너라고 한다.

서블릿 컨테이너는 서블릿 객체를 생성, 초기화, 호출, 종료하는 생명 주기를 관리하며,\
이 때 서블릿 객체는 싱글톤으로 관리된다.

> 고객의 요청이 올 때마다 계속 객체를 생성하는 것은 비효율적이다.\
> 따라서 최초 로딩 시점에 서블릿 객체를 미리 만들어두고 재활용해,\
> 모든 고객 요청이 동일한 서블릿 객체 인스턴스에 접근한다.\
> 따라서 **공유 변수 사용에 주의**를 기울여야 하고,\
> 서블릿 컨테이너 종료시 서블릿 객체도 함께 종료된다.

JSP도 서블릿으로 변환되어서 사용되고, 동시 요청을 위한 멀티 쓰레드 처리도 지원하고 있다.

### 동시 요청, 멀티 쓰레드
애플리케이션 코드를 하나하나 순차적으로 실행하는 것을 쓰레드라고 한다. \
자바 메인 메소드를 처음 실행하면 main이라는 이름의 쓰레드가 실행된다.\
만약 이 쓰레드가 없다면 자바 애플리케이션 실행이 불가능하며,\
쓰레드는 한번에 하나의 코드 라인만 수행한다.

만약 **동시 처리가 필요하다면, 쓰레드를 추가로 생성해야 한다.**

### 단일 요청, 쓰레드를 하나 사용하는 예시
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/60255872-6c1b-45e2-abff-bb9f2ed21dde)\
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/d26c35be-a031-433f-a91c-d63ad389c571)\
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/0c1b202e-d5eb-4d55-9294-6c0d3fbbbe19)\

단일 요청이므로, 쓰레드를 하나 사용하더라도 큰 문제가 발생하지 않는 모습을 볼 수 있다.\
그럼, 요청의 개수가 늘어나게되면 어떤 문제가 발생할까?

### 다중 요청, 쓰레드를 하나 사용하는 예시
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/1dade022-bbd6-49ab-a28c-2938d542ee28)

일반적인 상황에는 큰 문제가 아닐 수 있지만, 요청1 처리가 지연된다고 가정해보자.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/0a06db81-049e-4e10-9ec3-66022b074af7)\
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/beb3acf1-662c-41b8-bd77-f5485608246f)

이후 요청2가 들어오게 되었을 때, 요청1과 요청2 둘 다 문제가 발생하게 된다.\
이를 어떠한 방식으로 해결하는지 확인해보자.

### 요청마다 쓰레드 생성
이를 해결하기 위한 가장 단순한 방법은 바로 **요청마다 쓰레드를 생성**하는 방법이다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/70e4480c-91c4-4333-bbe8-55eb8d2df7ce)

이를 통해 동시 요청을 처리할 수 있다. \
이를 통해 하나의 쓰레드가 지연되어도, 나머지 쓰레드가 정상 작동한다.\
이러한 해결 방법은 리소스(CPU, 메모리)가 허용할 때까지 처리 가능하다는 장점이 있다.

하지만 쓰레드는 생성 비용이 매우 비싸, \
고객의 요청이 올 때마다 쓰레드를 생성하면 응답 속도가 늦어지게 된다.

또한 컨텍스트 스위칭 비용이 발생하고, 쓰레드 생성에 제한이 없다면\
고객 요청이 폭발적으로 증가했을 때, 리소스 임계점을 넘어 서버가 죽을 수 있다는 단점이 존재한다.

### 쓰레드 풀
또 다른 해결 방법은 바로 **쓰레드 풀**을 사용하는 방법이다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/a93630a0-0c2c-4b1c-a665-9878fe9a015a)

이름처럼 쓰레드가 모인 수영장을 만들어, 요청이 들어왔을 때 쓰레드를 쓰레드 풀에서 꺼내어 사용한다.\
사용을 종료했을 땐 사용한 쓰레드를 쓰레드 풀에 반납한다.\
만약 쓰레드 풀에 있는 모든 쓰레드를 전부 사용중이더라도, 요청을 대기시키거나 거절시킬 수도 있다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/49387d60-c95f-4b31-a82f-bc5a75ec1eda)

쓰레드 풀을 사용하게 되면 쓰레드가 미리 생성되어 있으므로,\
쓰레드를 생성하고 종료하는 비용이 절약되고, 응답시간이 빠르다는 장점이 있다.

또한 생성 가능한 쓰레드의 최대치가 정해져 있으므로, 너무 많은 요청이 들어오더라도\
기존 요청은 안정하게 처리할 수 있다.


### 쓰레드 풀을 사용하는 실무 팁
WAS의 주요한 튜닝 포인트는 최대 쓰레드 수(Max Thread)이다.

이 값을 너무 낮게 설정하게 되면 동시 요청이 많을 때\
서버 리소스는 여유롭지만, 클라이언트는 금방 응답 지연이 발생하게 된다.

반대로, 이 값을 너무 높게 설정하면 동시 요청이 많을 때\
CPU, 메모리 임계점 초과로 서버가 다운된다.

만약 장애가 발생했다면, 클라우드를 사용중이라면 우선 서버부터 늘린 뒤, 튜닝해야 한다.\
만약 클라우드가 아니라면 열심히 튜닝해야한다. :)

### 쓰레드 풀의 적정 숫자 찾기?
쓰레드 풀의 적정 숫자는 애플리케이션 로직의 복잡도나 \
CPU, 메모리, IO 리소스 상황에 따라 모두 다르다.

이를 위해 아파치 ab, 제이미터, nGrinder 등과 같은 도구를 사용해,\
최대한 실제 서비스와 유사하게 성능 테스트를 시도해 적정 숫자를 찾아야 한다.

### WAS의 멀티 쓰레드 지원 핵심
* 멀티 쓰레드에 대한 부분은 WAS가 처리한다.
* 즉, **개발자가 멀티 쓰레드 관련 코드를 신경쓰지 않아도 된다.**
* 개발자는 마치 **싱글 쓰레드 프로그래밍을 하듯, 편하게 소스코드를 개발하면 된다.**
* 멀티 쓰레드 환경이므로, 싱글톤 객체(서블릿, 스프링 빈)은 주의해서 사용해야 한다.


## HTML, HTTP API, CSR, SSR
### 정적 리소스
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/70ba97b2-b2cc-4f38-8d37-54aee477529b)

고정된 HTML 파일, CSS, JS, 이미지, 영상 등을 제공한다. 

### HTML 페이지
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/42cb6d81-a3fe-467e-8a4e-865c0a6f9d83)

동적으로 필요한 HTML 파일을 생성해 전달한다. 이 때 웹 브라우저는 HTML을 해석한다.

### HTTP API
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/4baf70ed-04f8-420b-9f6a-fea67fc1f8a1)

HTML이 아닌 데이터를 전달하는 방식으로, 주로 JSON 형식을 사용한다.\
웹 브라우저 뿐만 아닌 다양한 시스템에서 호출한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/0d19ed0d-802d-4c05-8b03-d99fef551ea9)

HTTP API는 데이터만 주고 받기 때문에, UI가 필요하다면 클라이언트가 별도로 처리해야 한다.\


### SSR - 서버 사이드 렌더링
HTML 최종 결과를 서버에서 만들어, 웹 브라우저에 전달하는 방식이다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/09a8b884-2506-49a8-b19a-e70d54e77fd5)

주로 정적인 화면에 사용하며, JSP, 타임리프 등 백엔드 측의 기술이 필요하다.

### CSR - 클라이언트 사이드 렌더링
HTML 결과를 자바스크립트를 사용해 웹 브라우저에서 동적으로 생성해서 적용하는 방식이다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/4639750a-eb2c-408b-b1cf-ab73e875935f)

주로 동적인 화면에 사용하며, 웹 환경을 마치 앱처럼 필요한 부분부분 변경할 수 있다.\
구글 지도나 Gmail등에 사용할 수 있으며, React나 Vue.js등이 대표적인 기술이다.

### 백엔드 개발자 입장에서 UI 기술
* **백엔드 - 서버 사이드 렌더링 기술**
  * JSP, 타임리프
  * 화면이 정적이고, 복잡하지 않을 때 사용
  * 백엔드 개발자는 서버 사이드 렌더링 기술 학습 **필수**
* **웹 프론트엔드 - 클라이언트 사이드 렌더링 기술**
  * React, Vue.js
  * 복잡하고 동적인 UI 사용
  * 웹 프론트엔드 개발자의 전문 분야
* **선택과 집중**
  * 백엔드 개발자의 웹 프론트엔드 기술 학습은 **옵션**
  * 백엔드 개발자는 서버, DB, 인프라 등등 수 많은 백엔드 기술을 공부해야 한다.
  * 웹 프론트엔드도 깊이있게 잘 하려면 숙련에 오랜 시간이 필요하다.


## 서블릿 프로젝트
### 서블릿 환경 구성
```java
package hello.servlet;

@ServletComponentScan // 서블릿 자동 등록
@SpringBootApplication
public class ServletApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServletApplication.class, args);
	}

}
```
* `ServletComponentScan` : 스프링이 자동으로 서블릿을 찾아, 자동으로 서블릿을 등록해준다.


### 서블릿 등록
```java
// urlPattern으로 오면, HelloServlet이 실행된다.
@WebServlet(name = "helloServlet", urlPatterns = "/hello")
public class HelloServlet extends HttpServlet {

    // Ctrl + O를 사용해 오버라이딩
    // 서블릿이 실행되면(hello로 들어오면) service가 실행됨.
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("HelloServlet.service");
        System.out.println("request = " + request);
        System.out.println("response = " + response);

        // 파라미터 받기
        String username = request.getParameter("username");
        System.out.println("username = " + username);

        response.setContentType("text/plain");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write("hello " + username);

    }
}
```
* `@WebServlet` : 서블릿 애노테이션
  * name : 서블릿 이름
  * urlPatterns : URL 매핑
 
HTTP 요청을 통해 매핑된 URL이 호출되면, 서블릿 컨테이너는 다음 메소드를 실행하게 된다.
```java
protected void service(...) { ... }
```

### HTTP 요청 메세지 로그로 확인하기
`application.properties`에 다음 설정을 추가하자.
```
logging.level.org.apache.coyote.http11=debug
```

서버를 재시작하면 다음과 같이 HTTP 요청 메세지를 출력하는걸 볼 수 있다.

```
Received [GET /hello?username=kim HTTP/1.1
Host: localhost:8080
Connection: keep-alive
Cache-Control: max-age=0
sec-ch-ua: "Whale";v="3", "Not-A.Brand";v="8", "Chromium";v="114"
sec-ch-ua-mobile: ?0
sec-ch-ua-platform: "Windows"
Upgrade-Insecure-Requests: 1
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Whale/3.21.192.18 Safari/537.36
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7
Sec-Fetch-Site: none
Sec-Fetch-Mode: navigate
Sec-Fetch-User: ?1
Sec-Fetch-Dest: document
Accept-Encoding: gzip, deflate, br
Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7,vi;q=0.6
]

HelloServlet.service
request = org.apache.catalina.connector.RequestFacade@1081ff8b
response = org.apache.catalina.connector.ResponseFacade@500c1921
username = kim
```

### 서블릿 컨테이너의 동작 방식

#### 1. 내장 톰캣 서버 생성
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/84bf7e5e-9237-403f-8317-41014ff9ddfc)

스프링 부트가 실행되면 내장 톰켓 서버를 띄워준다. \
톰켓 서버를 통해 서블릿을 전부 생성해 서블릿 컨테이너에 `helloServlet`이 생성된다.

#### 2. HTTP 요청, HTTP 응답 메세지
* HTTP 요청
```http
GET /hello?username=world HTTP/1.1
Host: localhost:8080

```

* HTTP 응답
```http
HTTP/1.1 200 OK
Content-Type: text/plain;charset=utf-8
Content-Length: 11

hello world
```

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/8ba43e6d-adb5-4aa4-aea2-d4e084b2323d)

요청 메세지를 기반으로 request, response 객체를 생성해 `helloServlet` 객체를 호출한다.\
해당 객체의 service 메소드를 호출해 필요한 작업을 한 뒤, \
WAS 서버가 response 정보를 통해 HTTP 응답 메세지를 만들어 반환해준다.

> HTTP 응답에서 Content-length는 WAS가 자동으로 생성해준다.



### welcome 페이지 추가
이후에 개발할 내용을 편리하게 참고할 수 있도록 welcome 페이지를 추가하자.

`webapp` 경로에 `index.html`을 두면 http://localhost:8080 호출 시 `index.html` 페이지가 열린다.

* `main/webapp/index.html`
```html
<!DOCTYPE html>
<html>
  <head>
      <meta charset="UTF-8">
      <title>Title</title>
  </head>
  <body>
    <ul>
        <li><a href="basic.html">서블릿 basic</a></li>
    </ul>
  </body>
</html>
```

추가로, 직후 학습할 내용인 `basic.html` 파일도 추가해주자.

* `main/webapp/basic.html`
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<ul>
    <li>hello 서블릿
        <ul>
            <li><a href="/hello?username=servlet">hello 서블릿 호출</a></li>
        </ul>
    </li>
    <li>HttpServletRequest
        <ul>
            <li><a href="/request-header">기본 사용법, Header 조회</a></li>
            <li>HTTP 요청 메시지 바디 조회
                <ul>
                    <li><a href="/request-param?username=hello&age=20">GET -
                        쿼리 파라미터</a></li>
                    <li><a href="/basic/hello-form.html">POST - HTML Form</a></
                    li>
                    <li>HTTP API - MessageBody -> Postman 테스트</li>
                </ul>
            </li>
        </ul>
    </li>
    <li>HttpServletResponse
        <ul>
            <li><a href="/response-header">기본 사용법, Header 조회</a></li>
            <li>HTTP 응답 메시지 바디 조회
                <ul>
                    <li><a href="/response-html">HTML 응답</a></li>
                    <li><a href="/response-json">HTTP API JSON 응답</a></li>
                </ul>
            </li>
        </ul>
    </li>
</ul>
</body>
</html>
```

## HttpServletRequest
HTTP 요청 메세지를 개발자가 직접 파싱해서 사용해도 되지만, 매우 불편할 것이다.
```http
POST /save HTTP/1.1
Host: localhost:8080
Content-Type: application/x-www-form-urlencoded

username=kim&age=20
```
서블릿은 개발자가 HTTP 요청 메세지를 편리하게 사용할 수 있도록 HTTP 요청 메세지를 파싱한다.\
그리고 그 결과를 `HttpServletRequest` 객체에 담아서 제공한다.

* START LINE
  * HTTP 메소드
  * URL
  * 쿼리 스트링
  * 스키마
* 헤더
  * 헤더 조회
* 바디
  * form 파라미터 형식 조회
  * message body 데이터 직접 조회

이러한 기능 이외에도 `HttpServletRequest` 객체는 추가로 여러가지 부가 기능도 제공한다.

### 임시 저장소 기능
`HttpServletRequest` 객체는 HTTP 요청이 시작했을 때부터 끝날 때까지 유지되는 임시 저장소 기능을 한다.
* 저장 : `request.setAttribute(name, value)`
* 조회 : `request.getAttribute(name)`

### 세션 관리 기능
로그인 등에서 사용되는 세션을 관리하는 기능을 제공한다.
* `request.getSesstion(create: true)`

> HttpServletRequest, HttpServletResponse를 사용할 때 가장 중요한 점은 이 객체들이 HTTP 요청메시지, HTTP 응답 메시지를 편리하게 사용하도록 도와주는 객체라는 점이다. 따라서 이 기능에 대해서 깊이있는 이해를 하려면 **HTTP 스펙이 제공하는 요청, 응답 메시지 자체를 이해**해야 한다.

### HttpServletRequest 기본 사용법
#### hello.servlet.basic.request.RequestHeaderServlet

```java
@WebServlet(name = "requestHeaderServlet", urlPatterns = "/request-header")
public class RequestHeaderServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        printStartLine(request);
    }

    private static void printStartLine(HttpServletRequest request) {
        System.out.println("--- REQUEST-LINE - start ---");
        System.out.println("request.getMethod() = " + request.getMethod()); //GET
        System.out.println("request.getProtocol() = " + request.getProtocol()); //HTTP/1.1
        System.out.println("request.getScheme() = " + request.getScheme()); //http
        // http://localhost:8080/request-header
        System.out.println("request.getRequestURL() = " + request.getRequestURL());
        // /request-header
        System.out.println("request.getRequestURI() = " + request.getRequestURI());
        //username=hi
        System.out.println("request.getQueryString() = " + request.getQueryString());
        System.out.println("request.isSecure() = " + request.isSecure()); //https 사용 유무
        System.out.println("--- REQUEST-LINE - end ---");
        System.out.println();
    }
}
```
* 출력 결과
```
--- REQUEST-LINE - start ---
request.getMethod() = GET
request.getProtocol() = HTTP/1.1
request.getScheme() = http
request.getRequestURL() = http://localhost:8080/request-header
request.getRequestURI() = /request-header
request.getQueryString() = null
request.isSecure() = false
```

#### 헤더 정보 조회
```java
    private void printHeaders(HttpServletRequest request) {
        System.out.println("--- Headers - start ---");

//        Enumeration<String> headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//         String headerName = headerNames.nextElement();
//         System.out.println(headerName + ": " + request.getHeader(headerName));
//        }

        request.getHeaderNames().asIterator()
                .forEachRemaining(headerName ->  System.out.println(headerName + ":" + request.getHeader(headerName)));
        System.out.println("--- Headers - end ---");
        System.out.println();
    }
```
* 출력 결과
```
--- Headers - start ---
host: localhost:8080
connection: keep-alive
cache-control: max-age=0
sec-ch-ua: "Chromium";v="88", "Google Chrome";v="88", ";Not A Brand";v="99"
sec-ch-ua-mobile: ?0
upgrade-insecure-requests: 1
user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 11_2_0) AppleWebKit/537.36
(KHTML, like Gecko) Chrome/88.0.4324.150 Safari/537.36
accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/
webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
sec-fetch-site: none
sec-fetch-mode: navigate
sec-fetch-user: ?1
sec-fetch-dest: document
accept-encoding: gzip, deflate, br
accept-language: ko,en-US;q=0.9,en;q=0.8,ko-KR;q=0.7
--- Headers - end ---
```
* Header 편리한 조회, 기타 정보는 생략.  

## HTTP 요청 데이터 
### HTTP 요청 데이터 개요
HTTP 요청 메세지를 통해 클라이언트에서 서버로 데이터를 전달할 수 있다.
* **GET - 쿼리 파라미터**
  * `/url?username=hello&age=20`
  * 메시지 바디 없이, **URL의 쿼리 파라미터에 데이터를 포함해서** 전달하는데에 사용한다.\
  ex) 검색, 필터, 페이징등에서 많이 사용하는 방식

* **POST - HTML Form**
  * `content-type: application/x-www-form-urlencoded`
  * 메시지 바디에 쿼리 파리미터 형식으로 전달한다. `username=hello&age=20`\
    ex) 회원 가입, 상품 주문, HTML Form 사용

* **HTTP message body**에 데이터를 직접 담아서 요청
  * HTTP API에서 주로 사용, **JSON**, XML, TEXT
  * 데이터 형식은 주로 JSON 사용
  * POST, PUT, PATCH

### HTTP 요청 데이터 - GET 쿼리 파라미터
다음 데이터를 클라이언트에서 서버로 전송한다고 가정해보자.
* username=hello
* age=20

쿼리 파라미터는 URL에 다음과 같이 `?`를 사용해 표시할 수 있고, 추가 파라미터는 `&`로 구분한다.

* http://localhost:8080/request-param?username=hello&age=20

서버에선 `HttpServletRequest`가 제공하는 다음 메소드들을 통해 쿼리 파라미터를 편리하게 조회할 수 있다.
```java
String username = request.getParameter("username"); //단일 파라미터 조회
Enumeration<String> parameterNames = request.getParameterNames(); //파라미터 이름들모두 조회
Map<String, String[]> parameterMap = request.getParameterMap(); //파라미터를 Map으로 조회
String[] usernames = request.getParameterValues("username"); //복수 파라미터 조회
```
실제로 확인해보자.

#### RequestParamServlet
```java
package hello.servlet.basic.request;

/**
 * 1. 파라미터 전송 기능
 * http://localhost:8080/request-param?username=hello&age=20
 */

@WebServlet(name = "requestParamServlet", urlPatterns = "/request-param")
public class RequestParamServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        System.out.println("[전체 파라미터 조회] - start");
        // 모든 파라미터 꺼내기
        req.getParameterNames().asIterator().
                forEachRemaining(paramName -> System.out.println("paramName = " + req.getParameter(paramName)));
        System.out.println("[전체 파라미터 조회] - end");
        System.out.println();


        System.out.println("[단일 파라미터 조회] - start");
        String username = req.getParameter("username");
        String age = req.getParameter("age");

        System.out.println("username = " + username);
        System.out.println("age = " + age);

        System.out.println("[단일 파라미터 조회] - end");
        System.out.println();

        // 파라미터가 중복될 때
        System.out.println("[이름이 같은 복수 파라미터 조회] - start");
        String[] usernames = req.getParameterValues("username");
        for (String name : usernames) {
            System.out.println("name = " + name);
        }

        System.out.println("[이름이 같은 복수 파라미터 조회] - end");
        System.out.println();
        
        resp.getWriter().write("ok");
    }
}
```
* 실행 결과
```
[전체 파라미터 조회] - start
paramName = hello
paramName = 20
[전체 파라미터 조회] - end

[단일 파라미터 조회] - start
username = hello
age = 20
[단일 파라미터 조회] - end

[이름이 같은 복수 파라미터 조회] - start
name = hello
name = hello2
[이름이 같은 복수 파라미터 조회] - end
```
#### 복수 파라미터에서 단일 파라미터 조회
`username=hello&username=kim` 과 같이 파라미터 이름은 하나인데, 값이 중복이면 어떻게 될까?

`request.getParameter()` 는 하나의 파라미터 이름에 대해서 단 하나의 값만 있을 때 사용해야 한다.\
지금처럼 중복일 때는 `request.getParameterValues()` 를 사용해야 한다.

참고로 이렇게 중복일 때 `request.getParameter()` 를 사용하면 `request.getParameterValues()`의 첫 번째 값을 반환한다.

> 사실, 중복으로 보낼 일은 많지 않다.


### HTTP 요청 데이터 - POST HTML Form
이번엔 HTML의 Form을 사용해 클라이언트에서 서버로 데이터를 전송해보자.\
Form은 주로 회원 가입, 상품 주문 등에서 주로 사용하는 방식이다.

* 특징
  * content-type: `application/x-www-form-urlencoded`
  * 메시지 바디에 쿼리 파리미터 형식으로 데이터를 전달한다. `username=hello&age=20`

Form을 사용하기 위해, `src/main/webapp/basic/hello-form.html`을 생성하자.
```html
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Title</title>
    </head>
    <body>
    <form action="/request-param" method="post">
        username: <input type="text" name="username" />
        age: <input type="text" name="age" />
        <button type="submit">전송</button>
    </form>
    </body>
</html>
```
위 HTML은 `http://localhost:8080/basic/hello-form.html`으로 접속하면 확인할 수 있다.

POST의 HTML Form을 전송하면 웹 브라우저는 다음 형식으로 HTTP 메세지를 만든다.
* **요청 URL**: http://localhost:8080/request-param
* **content-type**: `application/x-www-form-urlencoded`
* **message body**: `username=hello&age=20`

`application/x-www-form-urlencoded` 형식은 앞서 GET에서 살펴본 쿼리 파라미터 형식과 같다.\
따라서 쿼리 파라미터 조회 메서드를 그대로 사용하면 된다.

클라이언트(웹 브라우저) 입장에서는 두 방식에 차이가 있지만, 서버 입장에서는 둘의 형식이 동일하므로,\
request.getParameter() 로 편리하게 구분없이 조회할 수 있다.

정리하면, `request.getParameter()`는 GET URL 쿼리 파라미터 형식도 지원하고, POST HTML Form 형식도 지원한다.

> content-type은 HTTP 메세지 바디의 데이터 형식을 지정한다. \
> **GET URL 쿼리 파라미터 형식**으로 클라이언트에서 서버로 데이터를 전달할 때는 \
> HTTP 메시지 바디를 사용하지 않기 때문에 content-type이 없다.

> **POST HTML Form 형식**으로 데이터를 전달하면 HTTP 메시지 바디에 해당 데이터를 포함해서 \
> 보내기 때문에 바디에 포함된 데이터가 어떤 형식인지 content-type을 꼭 지정해야 한다.\
> 이렇게 폼으로 데이터를 전송하는 형식을 `application/x-www-form-urlencoded` 라 한다.

* Postman 테스트
  * 사실 이러한 간단한 테스트엔 HTML Form을 만들기는 귀찮고, Postman을 사용하면 간단하다.\
    ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/b0e11ebd-4863-4807-8436-1ae635333064)


### HTTP 요청 데이터 - API 바디 메세지 - 단순 텍스
* `HTTP message body`에 데이터를 직접 담아서 요청하는 방식이다.
  * HTTP API에서 주로 사용한다. 데이터 형식은 JSON

먼저, 가장 단순한 텍스트 메세지를 HTTP 메세지 바디에 담아서 전송하고, 읽어보자. \
HTTP 메세지 바디의 데이터는 `InputStream`을 사용해 직접 읽을 수 있다.

* RequestBodyStringServlet
```java
package hello.servlet.basic.request;

@WebServlet(name = "requestBodyStringServlet", urlPatterns = "/request-body-string")
public class RequestBodyStringServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // body의 내용을 byte code로 얻을 수 있다.
        ServletInputStream inputStream = request.getInputStream();
        // 바이트를 문자로 변환한다.
        String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

        System.out.println("messageBody = " + messageBody);

        response.getWriter().write("ok");
    }
}
```
Postman을 사용해 테스트 해보면, 우리가 전송한 body가 출력됨을 볼 수 있다.
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/10099d16-ad88-46e3-a1a4-72b076964cc4)
* 실행 결과
```
messageBody = hello!
```

### HTTP 요청 데이터 - API 메시지 바디 - JSON
이번엔 Raw Text가 아닌 HTTP API에서 주로 사용하 JSON 형식으로 데이터를 전달해보자.

### JSON 형식 파싱 추가
우선, JSON 형식으로 파싱할 수 있도록 객체를 하나 생성하자.

`hello.servlet.basic.HelloData`

```java
package hello.servlet.basic;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class HelloData {

    private String username;
    private int age;

    public String getUsername() {
        return username;
    }

    /*
    Lombok을 사용한 코드는 아래 코드와 동일하다.
    public void setUsername(String username) {
        this.username = username;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
    */
}
```

* `RequestBodyJsonServlet`

```java

@WebServlet(name = "requestBodyJsonServlet", urlPatterns = "/request-body-json")
public class RequestBodyJsonServlet extends HttpServlet {

    // jackson 라이브러리
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletInputStream inputStream = request.getInputStream();
        String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

        System.out.println("messageBody = " + messageBody);

        HelloData helloData = objectMapper.readValue(messageBody, HelloData.class);

        System.out.println("helloData.username = " + helloData.getUsername());
        System.out.println("helloData.age = " + helloData.getAge());

        response.getWriter().write("ok");
    }
}
```

Postman으로 테스트 해보자!

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/e6656fb2-de6e-4875-a4e7-5c66d63b47d7)

* 출력 결과
```
messageBody = {
    "username": "kim",
    "age": 20
}
helloData.username = kim
helloData.age = 20
```

