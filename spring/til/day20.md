## 프론트 컨트롤러

### 프론트 컨트롤러 패턴 소개
* 프론트 컨트롤러 도입 전\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/f2cf2ef7-e7fc-4eaa-a4f3-e92d4cbf52c1)

* 프론트 컨트롤러 도입 후\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/e7bcaef9-1dc4-4d7f-a235-459bf8bce4d0)

### 프론트 컨트롤러(FrontController) 패턴 특징
* 프론트 컨트롤러 서블릿 하나로 클라이언트의 요청을 받는다.
* 프론트 컨트롤러가 요청에 맞는 컨트롤러를 찾아서 호출해준다.
* 즉, 입구를 하나로 설정해, 공통 처리가 가능하다!
* 프론트 컨트롤러를 제외한 나머지 컨트롤러는 서블릿을 사용하지 않아도 된다.


### 스프링 웹 MVC와 프론트 컨트롤러
스프링 웹 MVC의 핵심도 바로 `FrontController`\이다.
스프링 웹 MVC의 `DispatcherServlet`이 `FrontController` 패턴으로 구현되어 있음 


## 프론트 컨트롤러 도입 - v1
프론트 컨트롤러를 단계적으로 도입해보도록 하자.\
이번 목표는기존 코드를 최대한 유지하면서, 컨트롤러를 도입하는 것이다.\
먼저, 구조를 맞추어두고 점진적으로 리펙터링 해보자.

### v1 구조
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/97f0f1c0-460d-4d62-a9d4-ca93c80d9b8b)

### ControllerV1
```java
 public interface ControllerV1 {

    void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
    
}
```
서블릿과 비슷한 모양의 컨트롤러 인터페이스를 도입한다. \
각 컨트롤러들은 이 인터페이스를 구현하면 된다. \
프론트 컨트롤러는 이 인터페이스를 호출해서 구현과 관계없이 로직의 일관성을 가져갈 수 있다.

이제 이 인터페이스를 구현한 컨트롤러를 만들어보자.\ 
지금 단계에서는 기존 로직을 최대한 유지하는게 핵심이다.

* MemberFormControllerV1, 회원 등록 컨트롤러
```java
public class MemberFormControllerV1 implements ControllerV1 {

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String viewPath = "/WEB-INF/views/new-form.jsp";
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);
    }
}
```
* MemberSaveControllerV1, 회원 저장 컨트롤러
```java
public class MemberSaveControllerV1 implements ControllerV1 {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));

        Member member = new Member(username, age);
        memberRepository.save(member);

        // 모델에 데이터를 보관한다.
        request.setAttribute("member", member);

        String viewPath = "/WEB-INF/views/save-result.jsp";
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);

    }
}
```
* MemberListControllerV1, 회원 목록 컨트롤러
```java
public class MemberListControllerV1 implements ControllerV1 {
    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Member> members = memberRepository.findAll();

        request.setAttribute("members", members);

        String viewPath = "/WEB-INF/views/members.jsp";
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);
    }
}
```
### FrontControllerServletV1, 프론트 컨트롤러
위에서 작성한 컨트롤러들은 기존의 서블릿과 거의 동일하다.\
그럼, 이제 프론트 컨트롤러를 만들어보자.

```java
// "/front-controller/v1/*" : v1/ 하위의 모든 경로가 들어오더라도, 이 servlet이 호출된다.
@WebServlet(name = "frontControllerServletV1", urlPatterns = "/front-controller/v1/*")
public class FrontControllerServletV1 extends HttpServlet {

    private Map<String, ControllerV1> controllerMap = new HashMap<>();

    public FrontControllerServletV1() {
        controllerMap.put("/front-controller/v1/members/new-form", new MemberFormControllerV1());
        controllerMap.put("/front-controller/v1/members/save", new MemberSaveControllerV1());
        controllerMap.put("/front-controller/v1/members", new MemberListControllerV1());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        ControllerV1 controller = controllerMap.get(requestURI);

        if (controller == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        controller.process(request, response);
    }
}
```
### 프론트 컨트롤러 분석
* `urlPatterns`
  * `urlPatterns = ".../v1/*"`을 사용해, `.../v1`을 포함한 모든 하위 요청은 이 서블릿에서 받아들인다.
* service()
  * 먼저, `requestURI`를 조회하여 실제 호출할 컨트롤러를 `controllerMap`에서 찾는다.\
    만약 컨트롤러가 존재하지 않는다면, 404 상태 코드를 반환한다.\
    컨트롤러를 찾았다면 `process` 메소드를 호출해, 해당 컨트롤러를 실행한다.



## View 분리 - v2
지금은 모든 컨트롤러에선 뷰로 이동하는 부분에 중복이 존재하고, 깔끔하지 않다.
```java
String viewPath = "/WEB-INF/views/new-form.jsp";
RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
dispatcher.forward(request, response);
```
이 부분을 해결하기 위해, 별도로 뷰를 처리하는 객체를 만들어보자.

### V2 구조
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/0bd859ed-bcde-4348-a739-2c145ac75425)

이전에 생성한 FrontController를 그대로 사용한다.\
다만 이전처럼 FrontController에서 직접 JSP를 호출하는것이 아닌,\
별도로 뷰를 처리하는 객체 `MyView`를 호출하는 구조이다.

### MyView
뷰 객체는 이후 다른 버전에서도 함께 사용하므로 패키지 위치를 `frontcontroller` 에 두었다.
```java
public class MyView {

    private String viewPath;

    public MyView(String viewPath) {
        this.viewPath = viewPath;
    }

    public void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);
    }
}
```
위 객체를 이용할 수 있도록, v1처럼 컨트롤러 인터페이스과 구현체를 만들어보자.

### ControllerV2
```java
public interface ControllerV2 {

    MyView process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
```

### MemberFormControllerV2 - 회원 등록 폼
```java

public class MemberFormControllerV2 implements ControllerV2 {

    @Override
    public MyView process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        return new MyView("/WEB-INF/views/new-form.jsp");
    }
}
```
위 코드에서도 볼 수 있듯, 이제 컨트롤러는 `dispatch` 관련 코드를 직접 생성해서 호출하지 않아도 된다.\
대신, 뷰 경로를 사용해 `MyView` 객체를 생성하고, 반환해주면 된다.

회원 저장, 회원 목록 컨트롤러는 생략하고, 프론트 컨트롤러를 만들어보자.

### FrontControllerV2
```java
@WebServlet(name = "frontControllerServletV2", urlPatterns = "/front-controller/v2/*")
public class FrontControllerServletV2 extends HttpServlet {

    private Map<String, ControllerV2> controllerMap = new HashMap<>();

    public FrontControllerServletV2() {
        controllerMap.put("/front-controller/v2/members/new-form", new MemberFormControllerV2());
        controllerMap.put("/front-controller/v2/members/save", new MemberSaveControllerV2());
        controllerMap.put("/front-controller/v2/members", new MemberListControllerV2());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        ControllerV2 controller = controllerMap.get(requestURI);

        if (controller == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        MyView view = controller.process(request, response);
        view.render(request, response);
    }
}
```
ControllerV2의 반환 타입은 `MyView`이다. \
즉, 프론트 컨트롤러는 컨트롤러 호출 결과로 `MyView`를 반환 받는다.\
반환 받은 `MyView` 객체에서 `render` 메소드를 호출하면 `forward` 로직이 수행되고, JSP가 실행된다.

프론트 컨트롤러의 도입으로 `MyView` 객체의 `render()`를 호출하는 부분을 모두 일관되게 처리할 수 있다.\
각각의 컨트롤러는 `MyView` 객체를 생성만 해서 반환하면 된다.

## Model 추가
### 서블릿 종속성 제거
컨트롤러 입장에서 생각해보자. `HttpServletRequest`, `HttpServletResponse`이 꼭 필요할까?\
**요청 파라미터 정보**는 자바의 Map으로 대신 넘기도록 하면, \
지금 구조에선 컨트롤러가 서블릿 기술을 몰라도 동작할 수 있다.\
그리고 `request` 객체를 `Model`로 사용하는 대신, 별도의 `Model`을 만들어서 반환하면 된다.

우리가 구현하는 컨트롤러가 서블릿 기술을 전혀 사용하지 않도록 변경해보자.\
이렇게 하면 구현 코드도 매우 단순해지고, 테스트코드 작성이 쉽다.

### 뷰 이름 중복 제거
컨트롤러에서 지정하는 뷰 이름에 중복이 있는 것을 확인할 수 있다.\
컨트롤러는 뷰의 논리 이름을 반환하고, \
실제 물리 위치의 이름은 프론트 컨트롤러에서 처리하도록 단순화하자.\
이렇게 해두면 향후 뷰의 폴더 위치가 함께 이동해도 프론트 컨트롤러만 고치면 된다. 

* `/WEB-INF/views/new-form.jsp` -> new-form
* `/WEB-INF/views/save-result.jsp` -> save-result
* `/WEB-INF/views/members.jsp` -> members

### V3 구조
![image](https://github.com/jub3907/pubg-server-nest/assets/58246682/69149e87-7789-4bae-9e9e-d8b71ba30251)

### ModelView
지금까지 컨트롤러에서 서블릿에 종속적인 `HttpServletRequest`를 사용했다. \
그리고 Model도 `request.setAttribute()` 를 통해 데이터를 저장하고 뷰에 전달했다.

서블릿의 종속성을 제거하기 위해 Model을 직접 만들고, \
추가로 View 이름까지 전달하는 객체를 만들어보자.

> 이번 버전에서는 컨트롤러에서 `HttpServletRequest`를 사용할 수 없다. \
> 따라서 직접 `request.setAttribute()` 를 호출할 수 도 없다. \
> 따라서 Model이 별도로 필요하다.

참고로 `ModelView` 객체는 다른 버전에서도 사용하므로 패키지를 `frontcontroller` 에 둔다.

```java
@Getter @Setter
public class ModelView {
    private String viewName;

    private Map<String, Object> model = new HashMap<>();

    public ModelView(String viewName) {
        this.viewName = viewName;
    }
}
```
뷰의 이름과 뷰를 렌더링할 때 필요한 `model` 객체를 갖는다. \
model은 단순히 map으로 되어 있으므로, 컨트롤러에서 뷰에 필요한 데이터를 key, value로 넣어준다.

### ControllerV3

```java
public interface ControllerV3 {
    ModelView process(Map<String, String> paramMap);
}
```
V3 컨트롤러는 서블릿 기술을 전혀 사용하지 않는다. \
따라서 구현이 매우 단순해지고, 테스트 코드 작성시 테스트 하기 쉽다.

`HttpServletRequest`가 제공하는 파라미터는 프론트 컨트롤러가 paramMap에 담아서 호출해주면 된다.\응답 결과로 뷰 이름과 뷰에 전달할 Model 데이터를 포함하는 ModelView 객체를 반환하면 된다.

### MemberFormControllerV3 - 회원 등록 폼
```java
public class MemberFormControllerV3 implements ControllerV3 {

    @Override
    public ModelView process(Map<String, String> paramMap) {
        return new ModelView("new-form");
    }
}
```
`ModelView`를 생성할 때 `new-form` 이라는 view의 논리적인 이름을 지정한다. \
실제 물리적인 이름은 프론트 컨트롤러에서 처리한다.

### MemberSaveControllerV3 - 회원 저장

```java
public class MemberSaveControllerV3 implements ControllerV3 {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public ModelView process(Map<String, String> paramMap) {
        String username = paramMap.get("username");
        int age = Integer.parseInt(paramMap.get("age"));


        Member member = new Member(username, age);
        memberRepository.save(member);

        ModelView mv = new ModelView("save-result");
        mv.getModel().put("member", member);

        return mv;
    }
}

```

* `paramMap.get("username");`
  * 파라미터 정보는 map에 담겨있다. map에서 필요한 요청 파라미터를 조회하면 된다.
* `mv.getModel().put("member", member);`
  * 모델은 단순한 map이므로 모델에 뷰에서 필요한 member 객체를 담고 반환한다

### MemberListControllerV3 - 회원 목록
```java
public class MemberListControllerV3 implements ControllerV3 {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public ModelView process(Map<String, String> paramMap) {
        List<Member> members = memberRepository.findAll();
        ModelView mv = new ModelView("members");
        mv.getModel().put("members", members);

        return mv;
    }
}

```

### FrontControllerServletV3
```java
@WebServlet(name = "frontControllerServletV3", urlPatterns = "/front-controller/v3/*")
public class FrontControllerServletV3 extends HttpServlet {

    private Map<String, ControllerV3> controllerMap = new HashMap<>();

    public FrontControllerServletV3() {
        controllerMap.put("/front-controller/v3/members/new-form", new MemberFormControllerV3());
        controllerMap.put("/front-controller/v3/members/save", new MemberSaveControllerV3());
        controllerMap.put("/front-controller/v3/members", new MemberListControllerV3());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        String requestURI = request.getRequestURI();
        ControllerV3 controller = controllerMap.get(requestURI);

        if (controller == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // paramMap을 넘겨줘야 함
        Map<String, String> paramMap = createParamMap(request);

        // 여기까지, view의 논리 이름(new-form)만 얻을 수 있음.
        ModelView mv = controller.process(paramMap);

        String viewName = mv.getViewName();
        MyView view = viewResolver(viewName);

        view.render(mv.getModel(), request, response);
    }

    private MyView viewResolver(String viewName) {
        return new MyView("/WEB-INF/views/" + viewName + ".jsp");

    }

    private static Map<String, String> createParamMap(HttpServletRequest request) {
        Map<String, String> paramMap = new HashMap<>();
        request.getParameterNames().asIterator()
                .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
        return paramMap;
    }
}
```
* 뷰 리졸버 `MyView view = viewResolver(viewName)`
  * 컨트롤러가 반환한 논리 뷰 이름을 실제 물리 뷰 경로로 변경한다.\
  그리고 실제 물리 경로가 있는 MyView 객체를 반환한다.

* `view.render(mv.getModel(), request, response)`
  * 뷰 객체를 통해 HTMl 화면을 렌더링한다.
  * 뷰 객체의 `render()`는 모델 정보도 함께 받는다.
  * JSP는 `request.getAttribute()`로 데이터를 조회하기 때문에,\
  모델의 데이터를 꺼내어 `request.setAttribute`로 담아둬야 한다.



### MyView
Model의 데이터를 request 객체에 추가하기 위해, \
MyView 객체에 다음과 같은 메소드를 하나 추가해야 한다.

```java
    public void render(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException  {
        modelToRequestAttribute(model, request);
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);
    }

    private static void modelToRequestAttribute(Map<String, Object> model, HttpServletRequest request) {
        model.forEach((key, value) -> request.setAttribute(key, value));
    }
```