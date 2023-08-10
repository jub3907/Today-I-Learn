## 단순하고 실용적인 컨트롤러, V4
앞서 만든 v3 컨트롤러는 서블릿 종속성을 제거하고 뷰 경로의 중복을 제거하는 등, \
잘 설계된 컨트롤러이다. 그런데 실제 컨트톨러 인터페이스를 구현하는 개발자 입장에서 보면, \
항상 ModelView 객체를 생성하고 반환해야 하는 부분이 조금은 번거롭다.

좋은 프레임워크는 아키텍처도 중요하지만,\
그와 더불어 실제 개발하는 개발자가 단순하고 편리하게 사용할 수 있어야 한다. \
소위 실용성이 있어야 한다.

이번에는 v3를 조금 변경해서 실제 구현하는 개발자들이 매우 편리하게 개발할 수 있는 v4 버전을 개발해보자.

### V4 구조
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/6f951c5d-a3e4-4301-bd8b-68ec7c295b6f)

기본적인 구조는 V3와 같다. 대신에 컨트롤러가 `ModelView` 를 반환하지 않고, `ViewName` 만 반환한다.


### ControllerV4
```java
public interface ControllerV4 {
    /**
     *
     * @param paramMap
     * @param model
     * @return viewName
     */
    String process(Map<String, String> paramMap, Map<String, Object> model);
}
```

이번 버전은 인터페이스에 `ModelView`가 없다.\
model 객체는 파라미터로 전달되기 때문에 그냥 사용하면 되고, \
결과로 뷰의 이름만 반환해주면 된다.

### MemberFormControllerV4
```java
public class MemberFormControllerV4 implements ControllerV4 {
    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
        //view의 논리 이름 반환
        return "new-form";
    }
}
```
단순하게, `new-form`이라는 뷰의 논리 이름만 반환하면 된다.

### MemberSaveControllerV4
```java
public class MemberSaveControllerV4 implements ControllerV4 {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
        String username = paramMap.get("username");
        int age = Integer.parseInt(paramMap.get("age"));


        Member member = new Member(username, age);
        memberRepository.save(member);

        model.put("member", member);
        return "save-result";
    }
}
```
모델은 파라미터로 전달되기 때문에, 이전처럼 모델을 직접 생성하지 않는다.

### MemberListControllerV4
```java
public class MemberListControllerV4 implements ControllerV4 {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
        List<Member> members = memberRepository.findAll();
        ModelView mv = new ModelView("members");
        model.put("members", members);

        return "members";
    }
}
```

### FrontControllerServletV4
```java
@WebServlet(name = "frontControllerServletV4", urlPatterns = "/front-controller/v4/*")
public class FrontControllerServletV4 extends HttpServlet {

    private Map<String, ControllerV4> controllerMap = new HashMap<>();

    public FrontControllerServletV4() {
        controllerMap.put("/front-controller/v4/members/new-form", new MemberFormControllerV4());
        controllerMap.put("/front-controller/v4/members/save", new MemberSaveControllerV4());
        controllerMap.put("/front-controller/v4/members", new MemberListControllerV4());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        String requestURI = request.getRequestURI();
        ControllerV4 controller = controllerMap.get(requestURI);

        if (controller == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Map<String, String> paramMap = createParamMap(request);
        Map<String, Object> model = new HashMap<>();

        String viewName = controller.process(paramMap, model);
        MyView view = viewResolver(viewName);
        view.render(model, request, response);
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
* 모델 객체 전달\
  `Map<String, Object> model = new HashMap<>();` 코드를 사용해 모델 객체를 생성한다.\
  생성한 모델 객체는 컨트롤러에 넘겨주고, 컨트롤러엔 모델 객체에 값을 담는다.


* 뷰의 논리 이름 직접 반환
  `String viewName = controller.process(paramMap, model);`, \
  컨트롤러가 직접 뷰의 논리 이름을 반환하므로 이 값을 사용해\
  실제 물리 뷰를 찾을 수 있다.


### 정리
이번 버전의 컨트롤러는 매우 단순하고 실용적이다. \
기존 구조에서 모델을 파라미터로 넘기고, \
뷰의 논리 이름을 반환한다는 작은 아이디어를 적용했을 뿐인데, \
컨트롤러를 구현하는 개발자 입장에서 보면 이제 군더더기 없는 코드를 작성할 수 있다.

또한 중요한 사실은 여기까지 한번에 온 것이 아니라는 점이다. \
프레임워크가 점진적으로 발전하는 과정속에서 이런 방법도 찾을 수 있었다.


## 유연한 컨트롤러 1, V5
지금까지 개발한 컨트롤러는 구현 형태가 고정되어 있다는 단점이 하나 존재한다.\
만약 어떤 개발자는 `ControllerV3` 방식으로 개발하고 싶고,\
어떤 개발자는 `ControllerV4` 방식으로 개발하고 싶다면 어떻게 해아할까?

```java
public interface ControllerV3 {
   ModelView process(Map<String, String> paramMap);
}
```
```java
public interface ControllerV4 {
   String process(Map<String, String> paramMap, Map<String, Object> model);
}
```
이는 인터페이스의 장점이자 단점인데, 이를 해결하는 방법을 알아보자.

### 어댑터 패턴
지금까지 우리가 개발한 프론트 컨트롤러는 한가지 방식의 컨트롤러 인터페이스만 사용할 수 있다.\
`ControllerV3`, `ControllerV4` 는 완전히 다른 인터페이스이다. 따라서 호환이 불가능하다. \
마치 v3는 110v이고, v4는 220v 전기 콘센트 같은 것이다. \
이럴 때 사용하는 것이 바로 어댑터이다.

어댑터 패턴을 사용해서 프론트 컨트롤러가 다양한 방식의 컨트롤러를 처리할 수 있도록 변경해보자.

### V5 구조
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/ca5c06c2-9692-4f6f-ae56-514d058c73ae)

* **핸들러 어댑터**
  * 중간에 어댑터 역할을 하는 어댑터가 추가되었는데 이름이 핸들러 어댑터이다. \
  여기서 어댑터 역할을 해주는 덕분에 다양한 종류의 컨트롤러를 호출할 수 있다.

* **핸들러**
  * 컨트롤러의 이름을 더 넓은 범위인 핸들러로 변경했다. \
  그 이유는 이제 어댑터가 있기 때문에 꼭 컨트롤러의 개념 뿐만 아니라 \
  어떠한 것이든 해당하는 종류의 어댑터만 있으면 다 처리할 수 있기 때문이다


### MyHandlerAdapter
```java
public interface MyHandlerAdapter {
    boolean supports(Object handler);
    ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException, IOException;
}
```
* `boolean supports(Object handler);`
  * handler는 컨트롤러를 의미한다.
  * 어댑터가 해당 컨트롤러를 처리할 수 있는지 판단하는 메소드. ( V3 어댑터, V4 어댑터)

* `ModelView handel(...)`
  * 어댑터가 실제 컨트롤러를 호출하고, 그 결과로 ModelView를 반환해야 한다.
  * 만약 실제 컨트롤러가 ModelView를 반환하지 못한다면 어댑터가 직접 생성해 반환해야 한다.
  * 이전에는 프론트 컨트롤러가 실제 컨트롤러를 호출했지만,\
    이제는 이 어댑터를 통해 실제 컨트롤러가 호출된다.


### ControllerV3HandlerAdapter
```java

public class ControllerV3HandlerAdapter implements MyHandlerAdapter {
    @Override
    public boolean supports(Object handler) {
        return (handler instanceof ControllerV3);
    }

    @Override
    public ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException, IOException {
        // frontcontroller에서 걸렀기 때문에 가능
        ControllerV3 controller = (ControllerV3) handler;

        Map<String, String> paramMap = createParamMap(request);
        ModelView mv = controller.process(paramMap);

        return mv;
    }


    private static Map<String, String> createParamMap(HttpServletRequest request) {
        Map<String, String> paramMap = new HashMap<>();
        request.getParameterNames().asIterator()
                .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
        return paramMap;
    }
}
```
하나씩 분석해보자.
```java
    @Override
    public boolean supports(Object handler) {
        return (handler instanceof ControllerV3);
    }
```
`ControllerV3`를 처리할 수 있는 어댑터를 의미한다.

```java
        ControllerV3 controller = (ControllerV3) handler;

        Map<String, String> paramMap = createParamMap(request);
        ModelView mv = controller.process(paramMap);

        return mv;
```
handler를 컨트롤러 V3로 스케일릴 후, V3 형식에 맞도록 호출한다.\
`supports()`를 통해 `ControllerV3`만 지원하기 때문에, 타입 변환은 걱정 없이 실행해도 된다.\
ControllerV3는 ModelView를 반환하기 때문에, 그대로 ModelView를 반환한다.

### FrontControllerServletV5

```java

@WebServlet(name = "frontControllerServletV5", urlPatterns = "/front-controller/v5/*")
public class FrontControllerServletV5 extends HttpServlet {
    // v3, v4 아무거나 들어가야 하기 때문에 Object
    private final Map<String, Object> handlerMappingMap = new HashMap<>();
    private final List<MyHandlerAdapter> handlerAdapters = new ArrayList<>();

    public FrontControllerServletV5() {
        initHandlerMappingMap();

        initHandlerAdapters();
    }

    private void initHandlerAdapters() {
        handlerAdapters.add(new ControllerV3HandlerAdapter());
    }

    private void initHandlerMappingMap() {
        handlerMappingMap.put("/front-controller/v5/v3/members/new-form", new MemberFormControllerV3());
        handlerMappingMap.put("/front-controller/v5/v3/members/save", new MemberSaveControllerV3());
        handlerMappingMap.put("/front-controller/v5/v3/members", new MemberListControllerV3());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 핸들러(컨트롤러) 받아오기
        Object handler = getHandler(request);

        if (handler == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // 어댑터 받아오기
        MyHandlerAdapter adapter = getHandlerAdapter(handler);

        ModelView mv = adapter.handle(request, response, handler);

        String viewName = mv.getViewName();
        MyView view = viewResolver(viewName);

        view.render(mv.getModel(), request, response);
    }

    private MyView viewResolver(String viewName) {
        return new MyView("/WEB-INF/views/" + viewName + ".jsp");

    }

    private MyHandlerAdapter getHandlerAdapter(Object handler) {
        for (MyHandlerAdapter adapter : handlerAdapters) {
            if (adapter.supports(handler)) {
                return adapter;
            }
        }

        throw new IllegalArgumentException("handler adapter is not found");
    }

    private Object getHandler(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return handlerMappingMap.get(requestURI);
    }
}
```
* 컨트롤러(Controller) -> 핸들러(Handler)
  * 이전에는 컨트롤러를 직접 매핑해서 사용했다.\
    그런데 이제는 어댑터를 사용하기 때문에, 컨트롤러 뿐만 아니라\
    어댑터가 지원하기만 하면 그 어떤것이라도 URL에 매핑해서 사용할 수 있다.\
    그래서 이름을 컨트롤러에서, 더 넓은 의미의 핸들러로 변경했다.

#### 생성자
```java
    public FrontControllerServletV5() {
        initHandlerMappingMap();

        initHandlerAdapters();
    }

    private void initHandlerAdapters() {
        handlerAdapters.add(new ControllerV3HandlerAdapter());
    }

    private void initHandlerMappingMap() {
        handlerMappingMap.put("/front-controller/v5/v3/members/new-form", new MemberFormControllerV3());
        handlerMappingMap.put("/front-controller/v5/v3/members/save", new MemberSaveControllerV3());
        handlerMappingMap.put("/front-controller/v5/v3/members", new MemberListControllerV3());
    }
```
생성자는 핸들러 매핑 정보와 핸들러어댑터를 초기화한다.

#### 핸들러 매핑
```java
Object handler = getHandler(request)
```
핸들러 매핑 정보인 `handlerMappingMap`에서 URL에 매핑된 핸들러 객체를 받아온다.

#### 어댑터 조회
```java
MyHandlerAdapter adapter = getHandlerAdapter(handler);
```
`handler`를 처리할 수 있는 어댑터를 `adapter.supports(handler)`를 통해 찾는다.\
handler가 구현된 인터페이스의 핸들러어댑터 객체가 반환된다.

#### 어댑터 호출
```java
ModelView mv = adapter.handle(request, response, handler);
```
어댑터의 `handle(request, response, handler)` 메서드를 통해 실제 어댑터가 호출된다.\
어댑터는 handler(컨트롤러)를 호출하고 그 결과를 어댑터에 맞추어 반환한다.\
`ControllerV3HandlerAdapter` 의 경우 어댑터의 모양과 컨트롤러의 모양이 유사해서 변환 로직이 단순하다.

### 유연한 컨트롤러2, V5
이제 `FrontControllerServletV5`에 `ControllerV4` 기능도 추가해보자.
```java
private void initHandlerAdapters() {
    handlerAdapters.add(new ControllerV3HandlerAdapter());
    handlerAdapters.add(new ControllerV4HandlerAdapter()); //V4 추가
}

private void initHandlerMappingMap() {
    handlerMappingMap.put("/front-controller/v5/v3/members/new-form", new MemberFormControllerV3());
    handlerMappingMap.put("/front-controller/v5/v3/members/save", new MemberSaveControllerV3());
    handlerMappingMap.put("/front-controller/v5/v3/members", new MemberListControllerV3());
    //V4 추가
    handlerMappingMap.put("/front-controller/v5/v4/members/new-form", new MemberFormControllerV4());
    handlerMappingMap.put("/front-controller/v5/v4/members/save", new MemberSaveControllerV4());
    handlerMappingMap.put("/front-controller/v5/v4/members", new MemberListControllerV4());
}
```
핸들러 매핑에 `ControllerV4`를 사용하는 컨트롤러를 추가하고,\
해당 컨트롤러를 처리할 수 있는 어댑터인 `ControllerV4HandlerAdapter`도 추가하자.

### ControllerV4HandlerAdapter
```java
public class ControllerV4HandlerAdapter implements MyHandlerAdapter {
    @Override
    public boolean supports(Object handler) {
        return (handler instanceof ControllerV4);
    }

    @Override
    public ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException, IOException {
        ControllerV4 controller = (ControllerV4) handler;

        Map<String, String> paramMap = createParamMap(request);
        HashMap<String, Object> model = new HashMap<>();

        String viewName = controller.process(paramMap, model);

        ModelView mv = new ModelView(viewName);
        mv.setModel(model);

        return mv;
    }


    private static Map<String, String> createParamMap(HttpServletRequest request) {
        Map<String, String> paramMap = new HashMap<>();
        request.getParameterNames().asIterator()
                .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
        return paramMap;
    }
}
```
하나씩 분석해보자.
```java
public boolean supports(Object handler) {
   return (handler instanceof ControllerV4);
}
```
이전과 동일하게, `handler`가 `ControllerV4`인 경우에만 처리하는 어댑터.

```java
ControllerV4 controller = (ControllerV4) handler;

Map<String, String> paramMap = createParamMap(request);
HashMap<String, Object> model = new HashMap<>();

String viewName = controller.process(paramMap, model);
```
이전과 동일하게 `handler`를 `ControllerV4`로 캐스팅하고, \
`paramMap`과 `model`을 만들어 해당 컨트롤러를 호출해 `viewName`을 반환 받는다.

```java
ModelView mv = new ModelView(viewName);
mv.setModel(model);

return mv;
```
어댑터가 호출하는 `ControllerV4`의 뷰의 이름을 반환한다.\
그런데 어댑터는 뷰의 이름이 아닌, `ModelView`를 반환해야 한다. 이것이 어댑터가 꼭 필요한 이유이다.

`ControllerV4`는 뷰의 이름을 반환했지만, \
어댑터는 이것을 `ModelView`로 만들어서 형식을 맞춰 반환해준다.



