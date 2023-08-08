## HttpServletResponse
### HttpServletResponse 역할
HttpServletResponse는 **HTTP 응답 메세지를 생성**하는 역할을 한다.\
대표적으로, HTTP 응답 코드를 지정하고, 헤더, 바디를 생성한다.

또한, Content-Type, 쿠키, Redirect를 편리하게 사용하는 기능을 제공한다.

### HttpServletResponse 기본 사용법
* `hello.servlet.basic.response.ResponseHeaderServlet`

```java
@WebServlet(name = "responseHeaderServlet", urlPatterns = "/response-header")
public class ResponseHeaderServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // [status-Line]
        response.setStatus(HttpServletResponse.SC_OK);

        // [response-header]
        response.setHeader("Content-Type", "tex t/plain;charset=utf-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("my-header","hello");

        //[Header 편의 메서드]
        content(response);
        cookie(response);
        //redirect(response);

        //[message body]
        PrintWriter writer = response.getWriter();
        writer.println("ok");

    }

    private void content(HttpServletResponse response) {
        //Content-Type: text/plain;charset=utf-8
        //Content-Length: 2
        //response.setHeader("Content-Type", "text/plain;charset=utf-8");
        response.setContentType("text/plain");
        response.setCharacterEncoding("utf-8");
        //response.setContentLength(2); //(생략시 자동 생성)
    }

    private void cookie(HttpServletResponse response) {
        //Set-Cookie: myCookie=good; Max-Age=600;
        //response.setHeader("Set-Cookie", "myCookie=good; Max-Age=600");
        Cookie cookie = new Cookie("myCookie", "good");
        cookie.setMaxAge(600); //600초
        response.addCookie(cookie);
    }

    private void redirect(HttpServletResponse response) throws IOException {
        //Status Code 302
        //Location: /basic/hello-form.html
        //response.setStatus(HttpServletResponse.SC_FOUND); //302
        //response.setHeader("Location", "/basic/hello-form.html");
        response.sendRedirect("/basic/hello-form.html");
    }
}
```
실행 결과, response header에 우리가 설정한 값들이 들어간걸 볼 수 있다.

![image](https://github.com/jub3907/Spring-study/assets/58246682/5a87b807-c861-47e5-b7f8-e773a4da4698)

### HTTP 응답 데이터 - 단순 텍스트, HTML
HTTP 응답 데이터는 주로 다음 내용을 담아 전달한다.

* 단순 텍스트 응답
  * 앞에서 살펴본 `writer.println("ok");`
* HTML 응답
* HTTP API - MessageBody JSON 응답

이번엔 HTML 응답에 대해 알아보도록 하자.
* `hello.servlet.web.response.ResponseHtmlServlet`

```java
@WebServlet(name = "responseHtmlServlet", urlPatterns = "/response-html")
public class ResponseHtmlServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Content-Type : text/html;charset=utf-8
        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");

        PrintWriter writer = response.getWriter();

        writer.println("<html>");
        writer.println("<body>");
        writer.println(" <div>안녕?</div>");
        writer.println("</body>");
        writer.println("</html>");
    }
}
```
HTTP 응답으로 HTML을 반환하기 때문에, Content-Type을 `text/html`로 설정해야 한다.\
위에서 설정한 url에 접속하면, HTML이 반환된 것을 확인할 수 있다.

![image](https://github.com/jub3907/Spring-study/assets/58246682/2062706f-4d75-4057-a087-3505c4305615)

### HTTP 응답 데이터 - API JSON
마지막으로, API JSON을 반환해보자.

* `hello.servlet.web.response. ResponseJsonServlet`
```java
@WebServlet(name = "responseJsonServlet", urlPatterns = "/response-json")
public class ResponseJsonServlet extends HttpServlet {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Content-Type: application/json
        response.setHeader("content-type", "application/json");
        response.setCharacterEncoding("utf-8");

        HelloData data = new HelloData();
        data.setUsername("kim");
        data.setAge(20);
        // {username: "kim", "age" : 20}

        String result = objectMapper.writeValueAsString(data);

        response.getWriter().write(result);

    }
}
```
HTTP 응답으로 JSON을 반환할 때는 content-type을 application/json 로 지정해야 한다.\
Jackson 라이브러리가 제공하는 `objectMapper.writeValueAsString()` 를 사용하면 \
객체를 JSON 문자로 변경할 수 있다

> application/json 은 스펙상 utf-8 형식을 사용하도록 정의되어 있다. 그래서 스펙에서 \
> charset=utf-8과 같은 추가 파라미터를 지원하지 않는다. 따라서 `application/json` 이라고만 사용해야지\
> `application/json;charset=utf-8` 이라고 전달하는 것은 의미 없는 파라미터를 추가한 것이 된다.

> `response.getWriter()`를 사용하면 추가 파라미터를 자동으로 추가해버린다.\
> 이때는 `response.getOutputStream()`으로 출력하면 그런 문제가 없다.


## 회원 관리 웹 애플리케이션 예제
### 회원 관리 웹 애플리케이션 요구사항
* 회원 정보
  * 이름 : `username`
  * 나이 : `age`
* 기능 요구사항
  * 회원 저장
  * 회원 목록 조회
 

 #### 회원 도메인 모델
 ```java
@Getter @Setter
public class Member {

    private Long id;
    private String username;
    private int age;

    // 기본 생성자
    public Member() {
    }

    public Member(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
```
#### 회원 저장소
```java
/**
 * 동시성 문제가 고려되어 있지 않기 때문에, 실무에선 ConcurrentHashMap, AtomicLong 사용을 고려해야 한다.
 */
public class MemberRepository {

    private Map<Long, Member> store = new HashMap<>();
    // static이기 때문에, 전역으로 한개만 존재.
    private static long sequence = 0L;

    private static final MemberRepository instance = new MemberRepository();

    public static MemberRepository getInstance() {
        return instance;
    }

    public Member save(Member member) {
        member.setId(++sequence);
        store.put(member.getId(), member);
        return member;
    }

    public Member findById(Long id) {
        return store.get(id);
    }

    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    public void clearStore() {
        store.clear();
    }

}
```
회원 저장소는 싱글톤 패턴을 사용한다. 스프링을 사용하면 스프링 빈으로 등록하여\
자동으로 싱글톤으로 관리할 수 있지만, 지금은 최대한 순수 서블릿 만으로 구현하는 것이 목적이다.\
싱글톤 패턴은 객체를 단 하나만 생성해서 공유해야 하므로, 생성자를 `private`로 설정한다.

#### 회원 저장소 테스트 코드
```java
public class MemberRepositoryTest {

    MemberRepository memberRepository = MemberRepository.getInstance();

    // 테스트 끝날때마다 초기화
    @AfterEach
    void afterEach() {
        memberRepository.clearStore();
    }

    @Test
    void save() {
        // given
        Member member = new Member("hello", 20);

        // when
        Member savedMember = memberRepository.save(member);

        // then
        Member findMember = memberRepository.findById(savedMember.getId());
        assertThat(savedMember).isEqualTo(findMember);

    }

    @Test
    void findAll() {
        //given
        Member member1 = new Member("member1", 20);
        Member member2 = new Member("member2", 30);

        memberRepository.save(member1);
        memberRepository.save(member2);

        //when

        List<Member> members = memberRepository.findAll();

        //then
        assertThat(members.size()).isEqualTo(2);
        assertThat(members).contains(member1, member2);
    }
}
```
### 서블릿으로 회원 관리 웹 애플리케이션 만들기
이제, 본격적으로 서블릿으로 회원 관리 웹 애플리케이션을 만들어보자.

가장 먼저, 서블릿으로 회원 등록 HTML 폼을 제공해보자.

#### MemberFormServlet - 회원 등록 폼
```java

@WebServlet(name = "memberFormServlet", urlPatterns = "/servlet/members/new-form")
public class MemberFormServlet extends HttpServlet {

    // 싱글톤이기 때문에, getInstance로 가져옴.
    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //html 반환해야 함
        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");

        // 자바 코드로 작성해야 하기 떄문에, 굉장히 불편.
        // form action이 servlet/members/save에 post로 전송됨.
        PrintWriter w = response.getWriter();
        w.write("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                " <meta charset=\"UTF-8\">\n" +
                " <title>Title</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<form action=\"/servlet/members/save\" method=\"post\">\n" +
                " username: <input type=\"text\" name=\"username\" />\n" +
                " age: <input type=\"text\" name=\"age\" />\n" +
                " <button type=\"submit\">전송</button>\n" +
                "</form>\n" +
                "</body>\n" +
                "</html>\n");
    }
}
```
`MemberFormServlet`은 단순한 회원 정보를 입력할 수 있는 HTML Form을 만들고, 응답한다.\
위 폼은 HTML Form 데이터를 `/servlet/members/save`에 POST로 전달한다. \
아직 우리는 전달 받는 서블릿을 만들지 않았기 때문에, 오류가 발생한다.

이제, 폼에서 데이터를 입력 받아, 실제로 회원 데이터가 저장되도록 해보자.\
전송 방식은 POST HTML Form에서 학습한 내용과 동일하다.

#### MemberSaveServlet, 회원 저장
```java
@WebServlet(name = "memberSaveServlet", urlPatterns = "/servlet/members/save")
public class MemberSaveServlet extends HttpServlet {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // URL 쿼리 스트링이건 Post 데이터건, getParameter 사용.
        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));

        Member member = new Member(username, age);
        memberRepository.save(member);

        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");

        PrintWriter w = response.getWriter();
        w.write("<html>\n" +
                "<head>\n" +
                " <meta charset=\"UTF-8\">\n" +
                "</head>\n" +
                "<body>\n" +
                "성공\n" +
                "<ul>\n" +
                " <li>id="+member.getId()+"</li>\n" +
                " <li>username="+member.getUsername()+"</li>\n" +
                " <li>age="+member.getAge()+"</li>\n" +
                "</ul>\n" +
                "<a href=\"/index.html\">메인</a>\n" +
                "</body>\n" +
                "</html>");
    }
}
```
`servlet/members/save`에 post로 들어온 파라미터를 조회해 Member 객체를 만들고,\
Member 객체를 MemberRepository를 통해 저장한다.\
이후. Member 객체를 사용해 결과 화면용 HTML을 동적으로 만들고 있다.

이번엔 저장된 모든 회원 목록을 조회하는 기능을 만들어 보자.
#### MemberListServlet, 회원 목록
```
@WebServlet(name = "memberListServlet", urlPatterns = "/servlet/members")
public class MemberListServlet extends HttpServlet {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Member> members = memberRepository.findAll();

        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");

        PrintWriter w = response.getWriter();
        PrintWriter w = response.getWriter();
        w.write("<html>");
        w.write("<head>");
        w.write(" <meta charset=\"UTF-8\">");
        w.write(" <title>Title</title>");
        w.write("</head>");
        w.write("<body>");
        w.write("<a href=\"/index.html\">메인</a>");
        w.write("<table>");
        w.write(" <thead>");
        w.write(" <th>id</th>");
        w.write(" <th>username</th>");
        w.write(" <th>age</th>");
        w.write(" </thead>");
        w.write(" <tbody>");
/*
 w.write(" <tr>");
 w.write(" <td>1</td>");
 w.write(" <td>userA</td>");
 w.write(" <td>10</td>");
 w.write(" </tr>");
*/
        for (Member member : members) {
            w.write(" <tr>");
            w.write(" <td>" + member.getId() + "</td>");
            w.write(" <td>" + member.getUsername() + "</td>");
            w.write(" <td>" + member.getAge() + "</td>");
            w.write(" </tr>");
        }
        w.write(" </tbody>");
        w.write("</table>");
        w.write("</body>");
        w.write("</html>");

    }
}
```
`memberRepository.findAll()`을 통해 모든 회원을 조회하고, \
for 루프를 사용해 회원 목록 HTML을 동적으로 생성하고, 응답한다.

![image](https://github.com/jub3907/Spring-study/assets/58246682/42558d10-e39c-4ec0-8d82-8a40fe72212c)

### 템플릿 엔진으로
지금까지 서블릿과 자바 코드만으로 HTML을 만들어보았다. \
서블릿 덕분에 동적으로 원하는 HTML을 마음껏 만들 수 있다. \
정적인 HTML 문서라면 화면이 계속 달라지는 회원의 저장 결과라던가, \
회원 목록같은 동적인 HTML을 만드는 일은 불가능 할 것이다.

그런데, 코드에서 보듯이 이것은 매우 복잡하고 비효율 적이다. \
자바 코드로 HTML을 만들어 내는 것 보다 차라리 HTML 문서에 \
동적으로 변경해야 하는 부분만 자바 코드를 넣을 수 있다면 더 편리할 것이다.

이것이 바로 템플릿 엔진이 나온 이유이다. \
템플릿 엔진을 사용하면 HTML 문서에서 필요한 곳만 코드를 적용해서 동적으로 변경할 수 있다.\
템플릿 엔진에는 JSP, Thymeleaf, Freemarker, Velocity등이 있다.\

다음 시간에는 JSP로 동일한 작업을 진행해보자.

> JSP는 성능과 기능면에서 다른 템플릿 엔진과의 경쟁에서 밀리면서, 점점 사장되어 가는 추세이다. 템플릿엔진들은 각각 장단점이 있는데, 강의에서는 JSP는 앞부분에서 잠깐 다루고, 스프링과 잘 통합되는 Thymeleaf를 사용한다.

### Welcome 페이지 변경
지금부터 서블릿에서 JSP, MVC, 직접 만드는 MVC 프레임워크 등 긴 여정을 해야 한다.\
편리하게 참고할 수 있도록 welcome 페이지를 변경하자.
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
    <li>서블릿
        <ul>
            <li><a href="/servlet/members/new-form">회원가입</a></li>
            <li><a href="/servlet/members">회원목록</a></li>
        </ul>
    </li>
    <li>JSP
        <ul>
            <li><a href="/jsp/members/new-form.jsp">회원가입</a></li>
            <li><a href="/jsp/members.jsp">회원목록</a></li>
        </ul>
    </li>
    <li>서블릿 MVC
        <ul>
            <li><a href="/servlet-mvc/members/new-form">회원가입</a></li>
            <li><a href="/servlet-mvc/members">회원목록</a></li>
        </ul>
    </li>
    <li>FrontController - v1
        <ul>
            <li><a href="/front-controller/v1/members/new-form">회원가입</a></li>
            <li><a href="/front-controller/v1/members">회원목록</a></li>
        </ul>
    </li>
    <li>FrontController - v2
        <ul>
            <li><a href="/front-controller/v2/members/new-form">회원가입</a></li>
            <li><a href="/front-controller/v2/members">회원목록</a></li>
        </ul>
    </li>
    <li>FrontController - v3
        <ul>
            <li><a href="/front-controller/v3/members/new-form">회원가입</a></li>
            <li><a href="/front-controller/v3/members">회원목록</a></li>
        </ul>
    </li>
    <li>FrontController - v4
        <ul>
            <li><a href="/front-controller/v4/members/new-form">회원가입</a></li>
            <li><a href="/front-controller/v4/members">회원목록</a></li>
        </ul>
    </li>
    <li>FrontController - v5 - v3
        <ul>
            <li><a href="/front-controller/v5/v3/members/new-form">회원가입</a></
            li>
            <li><a href="/front-controller/v5/v3/members">회원목록</a></li>
        </ul>
    </li>
    <li>FrontController - v5 - v4
        <ul>
            <li><a href="/front-controller/v5/v4/members/new-form">회원가입</a></
            li>
            <li><a href="/front-controller/v5/v4/members">회원목록</a></li>
        </ul>
    </li>
    <li>SpringMVC - v1
        <ul>
            <li><a href="/springmvc/v1/members/new-form">회원가입</a></li>
            <li><a href="/springmvc/v1/members">회원목록</a></li>
        </ul>
    </li>
    <li>SpringMVC - v2
        <ul>
            <li><a href="/springmvc/v2/members/new-form">회원가입</a></li>
            <li><a href="/springmvc/v2/members">회원목록</a></li>
        </ul>
    </li>
    <li>SpringMVC - v3
        <ul>
            <li><a href="/springmvc/v3/members/new-form">회원가입</a></li>
            <li><a href="/springmvc/v3/members">회원목록</a></li>
        </ul>
    </li>
</ul>
</body>
</html>
```

## JSP로 회원 관리 웹 애플리케이션 만들기
JSP를 사용하려면 먼저 다음 라이브러리를 추가해야 한다.
* 스프링 부트 3.0 미만
```
//JSP 추가 시작
implementation 'org.apache.tomcat.embed:tomcat-embed-jasper'
implementation 'javax.servlet:jstl'
//JSP 추가 끝
```
* 스프링 부트 3.0 이상
```
//JSP 추가 시작
implementation 'org.apache.tomcat.embed:tomcat-embed-jasper'
implementation 'jakarta.servlet:jakarta.servlet-api' //스프링부트 3.0 이상
implementation 'jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api' //
스프링부트 3.0 이상
implementation 'org.glassfish.web:jakarta.servlet.jsp.jstl' //스프링부트 3.0 이상
//JSP 추가 끝
```
스프링 부트 3.0 이상이라면 `javax.servlet:jstl`을 제거하고, 위 코드 추가.

### 회원 등록 폼 JSP
`main/webapp/jsp/members/new-form.jsp`
```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
    <form action="/jsp/members/save.jsp" method="post">
         username: <input type="text" name="username" />
         age: <input type="text" name="age" />
         <button type="submit">전송</button>
    </form>
</body>
</html>

```
* `<%@ page contentType="text/html;charset=UTF-8" language="java" %>`
  * 첫 줄은 해당 문서가 JSP 문서라는 뜻. JSP 문서는 이와 같이 시작해야 한다.
 
회원 등록 폼 JSP를 보면 첫 줄을 제외하고는 완전히 HTML와 똑같다. JSP는 서버 내부에서 서블릿으로\
변환되는데, 우리가 만들었던 MemberFormServlet과 거의 비슷한 모습으로 변환된다.

### 회원 저장 JSP
`main/webapp/jsp/members/save.jsp`
```jsp
<%@ page import="hello.servlet.domain.member.MemberRepository" %>
<%@ page import="hello.servlet.domain.member.Member" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
        // request, response 사용 가능
     MemberRepository memberRepository = MemberRepository.getInstance();

     System.out.println("save.jsp");

     String username = request.getParameter("username");
     int age = Integer.parseInt(request.getParameter("age"));
     Member member = new Member(username, age);

     System.out.println("member = " + member);

     memberRepository.save(member);
%>
<html>
<head>
    <meta charset="UTF-8">
</head>
<body>
성공
    <ul>
         <li>id=<%=member.getId()%></li>
         <li>username=<%=member.getUsername()%></li>
         <li>age=<%=member.getAge()%></li>
    </ul>
    <a href="/index.html">메인</a>
</body>
</html>
```
JSP는 자바 코드를 그대로 다 사용할 수 있다.
* `<%@ page import="hello.servlet.domain.member.MemberRepository" %>`
  * 자바의 import 문과 같다.
* `<% ~~ %>`
  * 이 부분에는 자바 코드를 입력할 수 있다.
* `<%= ~~ %>`
  * 이 부분에는 자바 코드를 출력할 수 있다.

회원 저장 JSP를 보면, 회원 저장 서블릿 코드와 같다. 다른 점이 있다면, HTML을 중심으로 하고, 자바\
코드를 부분부분 입력해주었다. `<% ~ %>` 를 사용해서 HTML 중간에 자바 코드를 출력하고 있다.

### 회원 목록 JSP
`main/webapp/jsp/members.jsp`
```jsp
<%@ page import="java.util.List" %>
<%@ page import="hello.servlet.domain.member.MemberRepository" %>
<%@ page import="hello.servlet.domain.member.Member" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
     MemberRepository memberRepository = MemberRepository.getInstance();
     List<Member> members = memberRepository.findAll();
%>
<html>
<head>
     <meta charset="UTF-8">
     <title>Title</title>
</head>
<body>
    <a href="/index.html">메인</a>
    <table>
         <thead>
             <th>id</th>
             <th>username</th>
             <th>age</th>
         </thead>
         <tbody>
            <%
             for (Member member : members) {
                 out.write(" <tr>");
                 out.write(" <td>" + member.getId() + "</td>");
                 out.write(" <td>" + member.getUsername() + "</td>");
                 out.write(" <td>" + member.getAge() + "</td>");
                 out.write(" </tr>");
             }
            %>
         </tbody>
    </table>
</body>
</html>
```

회원 리포지토리를 먼저 조회하고, 결과 List를 사용해서 중간에 <tr><td> HTML 태그를 반복해서
출력하고 있다.

### 서블릿과 JSP의 한계
서블릿으로 개발할땐 View 화면을 위해 HTML을 만드는 작업이 자바 코드에 섞여서 지저분하고 복잡했다.\
JSP를 사용한 덕분에 뷰를 생성하는 HTML을 작업을 깔끔하게 가져가고,\
중간중간 동적으로 변경이 필요한 부분에만 자바 코드를 적용했다.

하지만, 이렇게 해도 해결되지 않는 몇 가지 고민이 남는다.

회원 저장 JSP를 보자. 코드의 상위 절반은 회원을 저장하기 위한 비즈니스 로직이다.\
하지만 나머지 절반은 결과를 HTML으로 보여주기 위한 뷰 영역이다.

코드를 잘 보면 JAVA코드, 데이터 조회를 위한 Repository 등, 다양한 코드가 모두 JSP에 노출되어 있다.\
JSP가 너무 많은 역할을 한다. 이렇게 작은 프로젝트도 벌써 머리가 아파오는데,\
수백 수천줄이 넘어가는 JSP를 떠올려보면 정말 지옥같을 것이다.

### MVC 패턴의 등장
위와같은 문제를 해결하기 위해, MVC라는 디자인 패턴이 등장하게 되었다.

비즈니스 로직은 서블릿처럼 다른곳에서 처리하고, JSP는 목적에 맞게 HTML로 화면을 그리는 일에 집중하도록 하자.



