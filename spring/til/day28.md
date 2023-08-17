# 검증
## 검증 요구사항
기존 상품 관리 예제에 새로운 요구사항이 추가되었다고 가정해보자.

### 요구사항: 검증 로직 추가
* 타입 검증
  * 가격, 수량에 문자가 들어가면 검증 오류 처리
* 필드 검증
  * 상품명: 필수, 공백X
  * 가격: 1000원 이상, 1백만원 이하
  * 수량: 최대 9999
* 특정 필드의 범위를 넘어서는 검증
  * 가격 * 수량의 합은 10,000원 이상

지금까지 만든 웹 애플리케이션은 폼 입력시 숫자를 문자로 작성하거나해서 \
검증 오류가 발생하면 오류 화면으로 바로 이동한다. \
이렇게 되면 사용자는 처음부터 해당 폼으로 다시 이동해서 입력을 해야 한다.\
아마도 이런 서비스라면 사용자는 금방 떠나버릴 것이다. \
웹 서비스는 폼 입력시 오류가 발생하면, 고객이 입력한 데이터를 유지한 상태로\
어떤 오류가 발생했는지 친절하게 알려주어야 한다.

컨트롤러의 중요한 역할중 하나는 HTTP 요청이 정상인지 검증하는 것이다. \
그리고 정상 로직보다 이런 검증 로직을 잘 개발하는 것이 어쩌면 더 어려울 수 있다.

> **참고: 클라이언트 검증, 서버 검증**\
> 클라이언트 검증은 조작할 수 있으므로 보안에 취약하다.\
> 서버만으로 검증하면, 즉각적인 고객 사용성이 부족해진다.\
> 둘을 적절히 섞어서 사용하되, 최종적으로 서버 검증은 필수이다.\
> API 방식을 사용하면 API 스펙을 잘 정의해서 검증 오류를 API 응답 결과에 잘 남겨주어야 한다.

먼저 검증을 직접 구현해보고, 뒤에서 스프링과 타임리프가 제공하는 검증 기능을 활용해보자.


## 검증 직접 처리 - 소개

### 상품 저장 성공
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/8be517a6-aab7-4ce3-88bf-debd1dc2d488)\
사용자가 상품 등록 폼에서 정상 범위의 데이터를 입력하면, \
서버에서는 검증 로직이 통과하고, \
상품을 저장하고, \
상품 상세 화면으로 redirect한다.

### 상품 저장 실패
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/fe93d324-8fcd-4b55-85e3-5de2d3eacdb8)\
고객이 상품 등록 폼에서 상품명을 입력하지 않거나, 가격, 수량 등이 너무 작거나 커서 \
검증 범위를 넘어서면, 서버 검증 로직이 실패해야 한다. \
이렇게 검증에 실패한 경우 고객에게 다시 상품 등록 폼을 보여주고, \
어떤 값을 잘못 입력했는지 친절하게 알려주어야 한다.

이제 요구사항에 맞추어 검증 로직을 직접 개발해보자.

## 검증 직접 처리 - 개발
### 상품 등록 검증
* ValidationItemControllerV1, addItem() 수정\
```java
@PostMapping("/add")
public String addItem(@ModelAttribute Item item, RedirectAttributes redirectAttributes, Model model) {

    // 검증 오류 결과 보관
    Map<String, String> errors = new HashMap<>();

    // 검증 로직
        // 아이템 이름이 없다면
    if (!StringUtils.hasText((item.getItemName()))) {
        // itemName 오류 발생.
        errors.put("itemName", "상품 이름은 필수입니다.");
    }

    if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
        errors.put("price", "상품 가격은 1,000 ~ 1,000,000까지 허용합니다.");
    }

    if (item.getQuantity() == null || item.getQuantity() >= 9999) {
        errors.put("quantity", "상품 수량은 최대 9999개까지 허용합니다.");
    }

    // 가격 * 수량의 합은 10000 이상
    if (item.getPrice() != null && item.getQuantity() != null) {
        int resultPrice = item.getQuantity() * item.getPrice();
        if (resultPrice < 10000) {
            errors.put("globalError", "가격 * 수량의 합은 10,000원 이상이여야 합니다. 현재 값 = " + resultPrice);
        }
    }


    // 검증 실패 시, 다시 입력 폼으로 이동.
    // 부정의 부정은 읽기 힘드므로, 리팩토링 필요.
    if (!errors.isEmpty()) {
        log.info("errors = {}", errors);
        model.addAttribute("errors", errors);
        return "validation/v1/addForm";
    }

    // 성공 로직

    Item savedItem = itemRepository.save(item);
    redirectAttributes.addAttribute("itemId", savedItem.getId());
    redirectAttributes.addAttribute("status", true);
    return "redirect:/validation/v1/items/{itemId}";
}
```

* 검증 오류 보관
  ```java
  Map<String, String> errors = new HashMap<>();
  ```
  만약 검증시 오류가 발생한다면, 어떤 검증에서 오류가 발생했는지 정보를 담아둔다.
  

* 검증 로직
  ```java
  if (!StringUtils.hasText(item.getItemName())) {
    errors.put("itemName", "상품 이름은 필수입니다.");
  }
  ```
  검증시 오류가 발생하면 errors 에 담아둔다. 이때 어떤 필드에서 오류가 발생했는지 \
  구분하기 위해 오류가 발생한 필드명을 key 로 사용한다. \
  이후 뷰에서 이 데이터를 사용해서 고객에게 친절한 오류 메시지를 출력할 수 있다


* 특정 필드의 범위를 넘어서는 검증 로직
  ```java
  // 가격 * 수량의 합은 10000 이상
  if (item.getPrice() != null && item.getQuantity() != null) {
      int resultPrice = item.getQuantity() * item.getPrice();
      if (resultPrice < 10000) {
          errors.put("globalError", "가격 * 수량의 합은 10,000원 이상이여야 합니다. 현재 값 = " + resultPrice);
      }
  }
  ```
  특정 필드를 넘어서는 오류를 처리해야 할 수도 있다. \
  이때는 필드 이름을 넣을 수 없으므로 `globalError`라는 key를 사용한다.


* 검증에 실패하면 다시 입력 폼으로
  ```java
    // 검증 실패 시, 다시 입력 폼으로 이동.
    // 부정의 부정은 읽기 힘드므로, 리팩토링 필요.
    if (!errors.isEmpty()) {
        log.info("errors = {}", errors);
        model.addAttribute("errors", errors);
        return "validation/v1/addForm";
    }
  ```
  만약 검증에서 오류 메시지가 하나라도 있으면 오류 메시지를 출력하기 위해\
  `model`에 `errors`를 담고, 입력 폼이 있는 뷰 템플릿으로 보낸다.

### 오류를 보여줄 수 있도록 HTML 수정
* addForm.html
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
    <link th:href="@{/css/bootstrap.min.css}"
          href="../css/bootstrap.min.css" rel="stylesheet">
    <style>
        .container {
            max-width: 560px;
        }
        .field-error {
            border-color: #dc3545;
            color: #dc3545;
        }

    </style>
</head>
<body>

<div class="container">

    <div class="py-5 text-center">
        <h2 th:text="#{page.addItem}">상품 등록</h2>
    </div>

    <form action="item.html" th:action th:object="${item}" method="post">

        <div th:if="${errors?.containsKey('globalError')}">
            <p class="field-error" th:text="${errors['globalError']}">전체 오류 메시지</p>
        </div>
        <div>
            <label for="itemName" th:text="#{label.item.itemName}">상품명</label>
            <input type="text" id="itemName" th:field="*{itemName}" class="form-control" placeholder="이름을 입력하세요"
                   th:classappend="${errors?.containsKey('itemName')} ? 'field-error' : '_'">
            <div class="field-error" th:if="${errors?.containsKey('itemName')}" th:text="${errors['itemName']}">
                상품명 오류
            </div>
        </div>
        <div>
            <label for="price" th:text="#{label.item.price}">가격</label>
            <input type="text" id="price" th:field="*{price}" class="form-control" placeholder="가격을 입력하세요"
                   th:class="${errors?.containsKey('price')} ? 'form-control field-error' : 'form-control'">
            <div class="field-error" th:if="${errors?.containsKey('price')}" th:text="${errors['price']}">
                가격 오류
            </div>

        </div>
        <div>
            <label for="quantity" th:text="#{label.item.quantity}">수량</label>
            <input type="text" id="quantity" th:field="*{quantity}" class="form-control" placeholder="수량을 입력하세요"
                   th:class="${errors?.containsKey('quantity')} ? 'form-control field-error' : 'form-control'">
            <div class="field-error" th:if="${errors?.containsKey('quantity')}" th:text="${errors['quantity']}">
                수량 오류
            </div>
        </div>

        <hr class="my-4">

        <div class="row">
            <div class="col">
                <button class="w-100 btn btn-primary btn-lg" type="submit" th:text="#{button.save}">상품 등록</button>
            </div>
            <div class="col">
                <button class="w-100 btn btn-secondary btn-lg"
                        onclick="location.href='items.html'"
                        th:onclick="|location.href='@{/validation/v1/items}'|"
                        type="button" th:text="#{button.cancel}">취소</button>
            </div>
        </div>

    </form>

</div> <!-- /container -->
</body>
</html>
```
* **css 추가**
  ```java
    .field-error {
      border-color: #dc3545;
      color: #dc3545;
  }
  ```
  오류 메시지를 빨간색으로 강조하기 위해 추가.

* **글로벌 오류 메시지**
  ```java
  <div th:if="${errors?.containsKey('globalError')}">
    <p class="field-error" th:text="${errors['globalError']}">전체 오류 메시지</p>
  </div>
  ```
  오류 메시지는 errors 에 내용이 있을 때만 출력하면 된다. \
  타임리프의 th:if 를 사용하면 조건에 만족할 때만 해당 HTML 태그를 출력할 수 있다.


* 참고: **Safe Navigation Operator\
  만약 여기에서 `errors`가 `null`이라면 어떻게 될까?\
  생각해보면 등록폼에 진입한 시점에는(`GET addForm`) errors 가 없다.\
  따라서 errors.containsKey() 를 호출하는 순간 NullPointerException 이 발생한다.\
  errors?. 은 errors 가 null 일때 NullPointerException이 발생하는 대신, null 을 반환하는 문법이다.\
  th:if 에서 null 은 실패로 처리되므로 오류 메시지가 출력되지 않는다.\
  [스프링의 SpringEL이 제공하는 문법](http://docs.spring.io/spring-framework/docs/current/reference/html/core.html#expressions-operator-safe-navigation)이다


* **필드 오류 처리**
  ```java
  <input type="text" id="itemName" th:field="*{itemName}" class="form-control" placeholder="이름을 입력하세요"
         th:classappend="${errors?.containsKey('itemName')} ? 'field-error' : '_'">
  ```
  `classappend`를 사용해서 해당 필드에 오류가 있으면 \
  `field-error`라는 클래스 정보를 더해서 폼의 색깔을 빨간색으로 강조한다. \
  만약 값이 없으면 _ (No-Operation)을 사용해서 아무것도 하지 않는다\

* **필드 오류 처리 - 메시지**
  ```java
  <div class="field-error" th:if="${errors?.containsKey('itemName')}" th:text="${errors['itemName']}">
    상품명 오류
  </div>
  ```
  글로벌 오류 메시지의 경우와 동일

### 정리
1. 검증 오류가 발생했을 땐, 입력 폼을 다시 보여준다.
2. 검증 오류를 고객에게 친절하게 안내하고, 다시 입력할 수 있게 한다.
3. 검증 오류가 발생해도 고객이 입력한 데이터가 유지된다.

### 남은 문제점
1. 뷰 템플릿에서 중복 처리가 많다.

2. 타입 오류 처리가 안된다. \
  Item 의 price , quantity 같은 숫자 필드는 타입이 Integer 이므로 \
  문자 타입으로 설정하는 것이 불가능하다. 숫자 타입에 문자가 들어오면 오류가 발생한다. \
  그런데 이러한 오류는 스프링MVC에서 컨트롤러에 진입하기도 전에 예외가 발생하기 때문에, \
  컨트롤러가 호출되지도 않고, 400 예외가 발생하면서 오류 페이지를 띄워준다.

4. Item 의 price 에 문자를 입력하는 것 처럼 타입 오류가 발생해도 고객이 입력한 문자를 화면에 남겨야 한다. \
  만약 컨트롤러가 호출된다고 가정해도 `Item`의 `price`는 `Integer`이므로 문자를 보관할 수가 없다.\
  결국 문자는 바인딩이 불가능하므로 고객이 입력한 문자가 사라지게 되고, \
  고객은 본인이 어떤 내용을 입력해서 오류가 발생했는지 이해하기 어렵다.

5. 결국 고객이 입력한 값도 어딘가에 별도로 관리가 되어야 한다.


## 검증 프로젝트 V2, BindResult
이제, 스프링이 제공하는 검증 오류 처리 방법을 알아보도록 하자.\
핵심은 **BindingResult**이다.

### ValidationItemControllerV2, addItemV1
```java
// BindingResult는 MoodelAttribute 뒤에 와야한다.
@PostMapping("/add")
public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

    // 검증 로직
    if (!StringUtils.hasText((item.getItemName()))) {
        bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
    }

    if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
        bindingResult.addError(new FieldError("item", "price", "상품 가격은 1,000 ~ 1,000,000까지 허용합니다."));
    }

    if (item.getQuantity() == null || item.getQuantity() >= 9999) {
        bindingResult.addError(new FieldError("item", "quantity", "상품 수량은 최대 9999개까지 허용합니다."));
    }

    // 가격 * 수량의 합은 10000 이상
    if (item.getPrice() != null && item.getQuantity() != null) {
        int resultPrice = item.getQuantity() * item.getPrice();
        if (resultPrice < 10000) {
            bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이여야 합니다. 현재 값 = " + resultPrice));
        }
    }

    // 검증 실패 로직
    if (bindingResult.hasErrors()) {
        log.info("errors = {}", bindingResult);
        // 모델에 추가해주지 않아도 된다.
        return "validation/v2/addForm";
    }
    
    // 성공 로직
    Item savedItem = itemRepository.save(item);
    redirectAttributes.addAttribute("itemId", savedItem.getId());
    redirectAttributes.addAttribute("status", true);
    return "redirect:/validation/v2/items/{itemId}";
}
```
`BindingResult bindingResult` 파라미터의 위치는 반드시\
`@ModelAttribute Item item` 뒤에 와야만 한다.

* 필드 오류 - FieldError
  ```java
    // 검증 로직
  if (!StringUtils.hasText((item.getItemName()))) {
      bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
  }
  ```
  필드에 오류가 발생하게 되면, `FieldError` 객체를 생성해서 `bindingResult`에 넣어주면 된다.\
  `public FieldError(String objectName, String field, String defaultMessage) {}`\
  * `objectName` : `@ModelAttribute` 이름
  * `field` : 오류가 발생한 필드 이름
  * `defaultMessage` : 오류 기본 메시지


* 글로벌 오류 - ObjectError
  ```java
  bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이여야 합니다. 현재 값 = " + resultPrice));
  ```
  특정 필드를 넘어선 글로벌 오류가 발생한다면 `ObjectError` 객체를 생성해, 담아준다.\
  `public ObjectError(String objectName, String defaultMessage) {}`\
  * `objectName` : `@ModelAttribute` 의 이름
  * `defaultMessage` : 오류 기본 메시지

### addForm 수정
```html
<div th:if="${#fields.hasGlobalErrors()}">
    <p class="field-error" th:each="err : ${#fields.globalErrors()}" th:text="${err}">전체 오류 메시지</p>
</div>
<div>
    <label for="itemName" th:text="#{label.item.itemName}">상품명</label>
    <input type="text" id="itemName"  placeholder="이름을 입력하세요" class="form-control"
           th:field="*{itemName}"
           th:errorclass="field-error">
    <div class="field-error" th:errors="*{itemName}">
        상품명 오류
    </div>
</div>
<div>
    <label for="price" th:text="#{label.item.price}">가격</label>
    <input type="text" id="price" class="form-control" placeholder="가격을 입력하세요"
           th:field="*{price}"
           th:errorclass="field-error">
    <div class="field-error" th:errors="*{price}">
        가격 오류
    </div>

</div>
<div>
    <label for="quantity" th:text="#{label.item.quantity}">수량</label>
    <input type="text" id="quantity" class="form-control" placeholder="수량을 입력하세요"
           th:field="*{quantity}"
           th:errorclass="field-error">
    <div class="field-error" th:errors="*{quantity}">
        수량 오류
    </div>
</div>
```
타임 리프는 스프링의 `BindingResult`를 활용해 편리하게 검증 오류를 표현하는 기능을 제공한다.
* `#fields` : `#fields` 로 `BindingResult` 가 제공하는 검증 오류에 접근할 수 있다.
* `th:errors` : 해당 필드에 오류가 있는 경우에 태그를 출력한다. `th:if` 의 편의 버전이다.
* `th:errorclass` : `th:field` 에서 지정한 필드에 오류가 있으면 `class` 정보를 추가한다.


* 글로벌 오류 처리
  ```java
  <div th:if="${#fields.hasGlobalErrors()}">
    <p class="field-error" th:each="err : ${#fields.globalErrors()}" th:text="${err}">전체 오류 메시지</p>
  </div>
  ```

* 필드 오류 처리
  ```java
  <input type="text" id="itemName"  placeholder="이름을 입력하세요" class="form-control"
         th:field="*{itemName}"
         th:errorclass="field-error">
  <div class="field-error" th:errors="*{itemName}">
      상품명 오류
  </div>
  ```

## BindResult2
BindingResult는 스프링이 제공하는 검증 오류를 보관하는 객체이다. \
검증 오류가 발생하면 여기에 보관하면 된다.

`BindingResult`가 있으면 `@ModelAttribute`에 데이터 바인딩 시 오류가 발생해도 컨트롤러가 호출된다!\
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/7748fc58-8796-4ec2-b3fe-13d4b28f26ef)

### @ModelAttribute에 바인딩 시 타입 오류가 발생하면?
* `BindingResult`가 없으면 400 오류가 발생하면서 컨트롤러가 호출되지 않고, 오류 페이지로 이동한다.
* `BindingResult`가 있으면 오류 정보( `FieldError` )를 `BindingResult`에 담아서 컨트롤러를 정상 호출한다.

### BindingResult에 검증 오류를 적용하는 3가지 방법
1. `@ModelAttribute`의 객체에 타입 오류 등으로 바인딩이 실패하는 경우 \
   스프링이 `FieldError`를 생성해서 `BindingResult` 에 넣어준다.
3. 개발자가 직접 넣어준다.
4. Validator 사용

### BindingResult 사용시 주의사항
BindingResult 는 검증할 대상 바로 다음에 와야한다. 순서가 중요하다. \
예를 들어서 `@ModelAttribute Item item,` 바로 다음에 `BindingResult`가 와야 한다.

BindingResult 는 Model에 자동으로 포함된다.


### BindingResult와 Errors
* `org.springframework.validation.Errors`
* `org.springframework.validation.BindingResult`

BindingResult 는 인터페이스이고, Errors 인터페이스를 상속받고 있다.
 
실제 넘어오는 구현체는 `BeanPropertyBindingResult`라는 것인데, \
둘다 구현하고 있으므로 `BindingResult` 대신에 `Errors`를 사용해도 된다.\
`Errors` 인터페이스는 단순한 오류 저장과 조회 기능을 제공한다. \
`BindingResult`는 여기에 더해서 추가적인 기능들을 제공한다. \
`addError()`도 `BindingResult`가 제공하므로 여기서는 `BindingResult`를 사용하자. \
주로 관례상 `BindingResult`를 많이 사용한다.


## FieldError, ObjectError
사용자 입력 오류 메시지가 화면에 남도록 해보자.
### ValidationItemControllerV2, addItemV2
```java
@PostMapping("/add")
public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

    // 검증 로직
    if (!StringUtils.hasText((item.getItemName()))) {
        bindingResult.addError(new FieldError("item", "itemName", item.getItemName(),
                false, null, null,  "상품 이름은 필수입니다."));
    }

    if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
        bindingResult.addError(new FieldError("item", "price", item.getPrice(),
                false, null, null,  "상품 가격은 1,000 ~ 1,000,000까지 허용합니다."));
    }

    if (item.getQuantity() == null || item.getQuantity() >= 9999) {
        bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(),
                false, null, null,  "상품 수량은 최대 9999개까지 허용합니다."));
    }

    // 가격 * 수량의 합은 10000 이상
    if (item.getPrice() != null && item.getQuantity() != null) {
        int resultPrice = item.getQuantity() * item.getPrice();
        if (resultPrice < 10000) {
            bindingResult.addError(new ObjectError("item", null, null, "가격 * 수량의 합은 10,000원 이상이여야 합니다. 현재 값 = " + resultPrice));
        }
    }

    // 검증 실패 로직
    if (bindingResult.hasErrors()) {
        log.info("errors = {}", bindingResult);
        // 모델에 추가해주지 않아도 된다.
        return "validation/v2/addForm";
    }

    // 성공 로직
    Item savedItem = itemRepository.save(item);
    redirectAttributes.addAttribute("itemId", savedItem.getId());
    redirectAttributes.addAttribute("status", true);
    return "redirect:/validation/v2/items/{itemId}";
}
```

### FieldError 생성자
앞서 사용한 `FieldError`는 두 가지 생성자를 제공한다.
```java
public FieldError(String objectName, String field, String defaultMessage);
public FieldError(String objectName, String field, @Nullable Object rejectedValue,
                  boolean bindingFailure, @Nullable String[] codes, @Nullable Object[] arguments,
                  @Nullable String defaultMessage)
```
* `objectName` : 오류가 발생한 객체 이름
* `field` : 오류 필드
* `rejectedValue` : 사용자가 입력한 값(거절된 값)
* `bindingFailure` : 타입 오류 같은 바인딩 실패인지, 검증 실패인지 구분 값
* `codes` : 메시지 코드
* `arguments` : 메시지에서 사용하는 인자
* `defaultMessage` : 기본 오류 메시지

사용자의 입력 데이터가 컨트롤러의 `@ModelAttribute`에 바인딩되는 시점에 오류가 발생하면 \
모델 객체에 사용자 입력 값을 유지하기 어렵다. 예를 들어서 가격에 숫자가 아닌 문자가 입력된다면 \
가격은 `Integer`타입이므로 문자를 보관할 수 있는 방법이 없다. \
그래서 오류가 발생한 경우 사용자 입력 값을 보관하는 별도의 방법이 필요하다. \
그리고 이렇게 보관한 사용자 입력 값을 검증 오류 발생시 화면에 다시 출력하면 된다.\
`FieldError`는 오류 발생시 사용자 입력 값을 저장하는 기능을 제공한다.

여기서 `rejectedValue`가 바로 오류 발생시 사용자 입력 값을 저장하는 필드다.\
`bindingFailure`는 타입 오류 같은 바인딩이 실패했는지 여부를 적어주면 된다. \
여기서는 바인딩이 실패한 것은 아니기 때문에 `false`를 사용한다.

* 타임리프의 사용자 입력 값 유지\
  `th:field="*{price}"`\
  타임리프의 `th:field`는 매우 똑똑하게 동작하는데, \
  정상 상황에는 모델 객체의 값을 사용하지만, 오류가 발생하면 \
  `FieldError` 에서 보관한 값을 사용해서 값을 출력한다.

* 스프링의 바인딩 오류 처리\
  타입 오류로 바인딩에 실패하면 스프링은 `FieldError`를 생성하면서 \
  사용자가 입력한 값을 넣어둔다. 그리고 해당 오류를 \
  `BindingResult`에 담아서 컨트롤러를 호출한다. \
  따라서 타입 오류 같은 바인딩 실패시에도 사용자의 오류 메시지를 정상 출력할 수 있다.



## 오류 코드와 메세지 처리
앞서 살펴본 `FieldError`와 `ObjectError`의 생성자는 `codes`, `arguments`를 제공한다.\
이는 오류 발생시 오류 코드로 메세지를 찾기 위해 사용된다.

### errors 메시지 파일 생성
messages.properties 를 사용해도 되지만, \
오류 메시지를 구분하기 쉽게 `errors.properties`라는 별도의 파일로 관리해보자.

먼저 스프링 부트가 해당 메시지 파일을 인식할 수 있게 다음 설정을 추가한다. \
이렇게하면 `messages.properties`, `errors.properties`두 파일을 모두 인식한다. \
(생략하면 `messages.properties`를 기본으로 인식한다.)

### 스프링 부트 메시지 설정 추가
* `application.properties`
```java
spring.messages.basename=messages,errors
```

### errors.properties 추가
* `src/main/resources/errors.properties`
```java
required.item.itemName=상품 이름은 필수입니다.
range.item.price=가격은 {0} ~ {1} 까지 허용합니다.
max.item.quantity=수량은 최대 {0} 까지 허용합니다.
totalPriceMin=가격 * 수량의 합은 {0}원 이상이어야 합니다. 현재 값 = {1}
```

### ValidationItemControllerV2 - addItemV3() 추가
```java
@PostMapping("/add")
public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

    // 검증 로직
    if (!StringUtils.hasText((item.getItemName()))) {
        bindingResult.addError(new FieldError("item", "itemName", item.getItemName(),
                false, new String[]{"required.item.itemName"}, null, null));
    }

    if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
        bindingResult.addError(new FieldError("item", "price", item.getPrice(),
                false, new String[]{"range.item.price"}, new Object[]{1000, 1000000}, null));
    }

    if (item.getQuantity() == null || item.getQuantity() >= 9999) {
        bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(),
                false, new String[]{"max.item.quantity"}, new Object[]{9999},  null));
    }

    // 가격 * 수량의 합은 10000 이상
    if (item.getPrice() != null && item.getQuantity() != null) {
        int resultPrice = item.getQuantity() * item.getPrice();
        if (resultPrice < 10000) {
            bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice}, null));
        }
    }

    // 검증 실패 로직
    if (bindingResult.hasErrors()) {
        log.info("errors = {}", bindingResult);
        // 모델에 추가해주지 않아도 된다.
        return "validation/v2/addForm";
    }

    // 성공 로직
    Item savedItem = itemRepository.save(item);
    redirectAttributes.addAttribute("itemId", savedItem.getId());
    redirectAttributes.addAttribute("status", true);
    return "redirect:/validation/v2/items/{itemId}";
}
```

```java
new FieldError("item", "price", item.getPrice(), false, new String[]{"range.item.price"}, new Object[]{1000, 1000000}
```
* codes : required.item.itemName 를 사용해서 메시지 코드를 지정한다. \
  메시지 코드는 하나가 아니라 배열로 여러 값을 전달할 수 있는데, \
  순서대로 매칭해서 처음 매칭되는 메시지가 사용된다.
* arguments : Object[]{1000, 1000000}를 사용해서 코드의 {0} , {1} 로 치환할 값을 전달한다.


## 오류 코드와 메시지 처리 2
사용하기 복잡하고, 긴 `FieldError`와 `ObjectError`를 좀 더 간소화 시켜보자.

컨트롤러에서 `BindingResult`는 검증해야 할 객체인 `target`바로 다음에 온다. \
따라서 `BindingResult`는 이미 본인이 검증해야 할 객체인 `target`을 알고 있다.

다음 코드를 컨트롤러에서 실행시켜 보자.
```java
log.info("objectName={}", bindingResult.getObjectName());
log.info("target={}", bindingResult.getTarget());
```
* 실행 결과
  ```java
  objectName=item //@ModelAttribute name
  target=Item(id=null, itemName=상품, price=100, quantity=1234)
  ```

### rejectValue() , reject()
`BindingResult`가 제공하는 `rejectValue()`, `reject()`를 사용하면 \
`FieldError`, `ObjectError`를 직접 생성하지 않고, 깔끔하게 검증 오류를 다룰 수 있다.

`rejectValue()`, `reject()`를 사용해서 기존 코드를 단순화해보자.

### ValidationItemControllerV2 - addItemV4() 추가
```java
@PostMapping("/add")
public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

    log.info("objectName={}", bindingResult.getObjectName());
    log.info("target={}", bindingResult.getTarget());

    // 검증 로직
    if (!StringUtils.hasText((item.getItemName()))) {
        bindingResult.rejectValue("itemName", "required");
    }

    if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
        bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
    }

    if (item.getQuantity() == null || item.getQuantity() >= 9999) {
        bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);
    }

    // 가격 * 수량의 합은 10000 이상
    if (item.getPrice() != null && item.getQuantity() != null) {
        int resultPrice = item.getQuantity() * item.getPrice();
        if (resultPrice < 10000) {
            bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
        }
    }

    // 검증 실패 로직
    if (bindingResult.hasErrors()) {
        log.info("errors = {}", bindingResult);
        // 모델에 추가해주지 않아도 된다.
        return "validation/v2/addForm";
    }

    // 성공 로직
    Item savedItem = itemRepository.save(item);
    redirectAttributes.addAttribute("itemId", savedItem.getId());
    redirectAttributes.addAttribute("status", true);
    return "redirect:/validation/v2/items/{itemId}";
}
```
오류 메시지가 정상 출력된다. \
그런데 errors.properties 에 있는 코드를 직접 입력하지 않았는데 어떻게 된 것일까?
* rejectValue()
  ```java
  void rejectValue(@Nullable String field, String errorCode, @Nullable Object[] errorArgs, @Nullable String defaultMessage);
  ```
  * field : 오류 필드명
  * errorCode : 오류 코드(이 오류 코드는 메시지에 등록된 코드가 아니다. \
  뒤에서 설명할 messageResolver를 위한 오류 코드이다.)
  * errorArgs : 오류 메시지에서 {0} 을 치환하기 위한 값
  * defaultMessage : 오류 메시지를 찾을 수 없을 때 사용하는 기본 메시지

앞서, BindingResult는 어떤 객체를 대상으로 검증하는지 target을 이미 알고 있다고 했다. \
따라서 target(item)에 대한 정보는 없어도 된다. 오류 필드명은 동일하게 price를 사용했다.

### 축약된 오류 코드

`FieldError()`를 직접 다룰 때는 오류 코드를 모두 입력했다.(`range.item.price`) \
그런데 `rejectValue()`를 사용하고 부터는 오류 코드를 `range`로 간단하게 입력했다. \
그래도 오류 메시지를 잘 찾아서 출력한다. 무언가 규칙이 있는 것 처럼 보인다. \
이 부분을 이해하려면 `MessageCodesResolver`를 이해해야 한다. \
왜 이런식으로 오류 코드를 구성하는지 바로 다음에 자세히 알아보자.


## 오류 코드와 메시지 처리 3
오류 코드를 만들 때 다음과 같이 자세히 만들 수 있다.
* `required.item.itemName` : 상품 이름은 필수 입니다.
* `range.item.price` : 상품의 가격 범위 오류 입니다.

혹은, 다음과 같이 단순하게 만들 수도 있다.
* `required` : 필수 값 입니다.
* `range` : 범위 오류 입니다.

단순하게 만들면 범용성이 좋아서 여러곳에서 사용할 수 있지만, 메시지를 세밀하게 작성하기 어렵다.\
반대로 너무 자세하게 만들면 범용성이 떨어진다. 

가장 좋은 방법은 범용성으로 사용하다가, 세밀하게 작성해야 하는 경우에는 \
세밀한 내용이 적용되도록 메시지에 단계를 두는 방법이다.

예를 들어서 required 라고 오류 코드를 사용한다고 가정해보자.\
다음과 같이 required 라는 메시지만 있으면 이 메시지를 선택해서 사용하는 것이다.
```java
required: 필수 값 입니다.
```

하지만 이 때, 오류 메세지에 `required.item.itemName`과 같이\
객체명과 필드명을 조합한 세밀한 메세지 코드가 있으면\
이 메세지를 높은 우선순위로 사용하는 것이다.

```java
#Level1
required.item.itemName: 상품 이름은 필수 입니다.

#Level2
required: 필수 값 입니다.
```
물론 이렇게 객체명과 필드명을 조합한 메시지가 있는지 우선 확인하고, \
없으면 좀 더 범용적인 메시지를 선택하도록 추가 개발을 해야겠지만, \
범용성 있게 잘 개발해두면 메시지의 추가 만으로 매우 편리하게 오류 메시지를 관리할 수 있을 것이다.

스프링은 `MessageCodesResolver`라는 것으로 이러한 기능을 지원한다.

## 오류 코드와 메세지 처리 4
우선, 테스트 코드를 사용해 **MessageCodesResolver**를 알아보자.

### MessageCodesREsolverTest
```java
public class MessageCodesResolverTest {

    MessageCodesResolver codesResolver = new DefaultMessageCodesResolver();

    @Test
    void messageCodesResolverObject() {
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item");

        for (String messageCode : messageCodes) {
            System.out.println("messageCode = " + messageCode);
//            messageCode = required.item
//            messageCode = required
        }

        assertThat(messageCodes).containsExactly("required.item", "required");
    }

    @Test
    void messageCodesResolverField() {
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item", "itemName", String.class);

        for (String messageCode : messageCodes) {
            System.out.println("messageCode = " + messageCode);
        }

        assertThat(messageCodes).containsExactly(
                "required.item.itemName",
                "required.itemName",
                "required.java.lang.String",
                "required"
        );
    }
}
```

#### MessageCodesResolver
* 검증 오류 코드로 메시지 코드들을 생성한다.
* `MessageCodesResolver`인터페이스이고 `DefaultMessageCodesResolver`는 기본 구현체이다.
* 주로 `ObjectError`, `FieldError`와 함께 사용 

### DefaultMessageCodesResolver의 기본 메시지 생성 규칙
* **객체 오류**
  ```
  객체 오류의 경우 다음 순서로 2가지 생성
  1.: code + "." + object name
  2.: code
  예) 오류 코드: required, object name: item
  1.: required.item
  2.: required
  ```

* **필드 오류**
  ```
  필드 오류의 경우 다음 순서로 4가지 메시지 코드 생성
  1.: code + "." + object name + "." + field
  2.: code + "." + field
  3.: code + "." + field type
  4.: code
  
  예) 오류 코드: typeMismatch, object name "user", field "age", field type: int
  1. "typeMismatch.user.age"
  2. "typeMismatch.age"
  3. "typeMismatch.int"
  4. "typeMismatch"
   ```

* **동작 방식**
  * `rejectValue()`, `reject()`는 내부에서 `MessageCodesResolver`를 사용한다. \
  여기에서 메시지 코드들을 생성한다.
  * `FieldError`, `ObjectError`의 생성자를 보면, 오류 코드를 하나가 아니라 여러 오류 코드를 가질 수 있다. `MessageCodesResolver`를 통해서 생성된 순서대로 오류 코드를 보관한다.
  * 이 부분을 `BindingResult`의 로그를 통해서 확인해보자.
    * `codes [range.item.price, range.price, range.java.lang.Integer, range]`

* **FieldError** `rejectValue("itemName", "required")`
  다음 4가지 오류 코드를 자동으로 생성
  * `required.item.itemName`
  * `required.itemName`
  * `required.java.lang.String`
  * `required`

* **ObjectError** `reject("totalPriceMin")`
  다음 2가지 오류 코드를 자동으로 생성
  * `totalPriceMin.item`
  * `totalPriceMin`

* 오류메세지 출력
  타임리프 화면을 렌더링 할 때 `th:errors`가 실행된다. 만약 이때 오류가 있다면 \
  생성된 오류 메시지 코드를 순서대로 돌아가면서 메시지를 찾는다. \
  그리고 없으면 디폴트 메시지를 출력한다.
