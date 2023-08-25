## 뷰 템플릿에 컨버터 적용하기
이번에는 뷰 템플릿에 컨버터를 적용하는 방법을 알아보자.\
타임리프는 렌더링 시에 컨버터를 적용해서 렌더링 하는 방법을 편리하게 지원한다.\
이전까지는 문자를 객체로 변환했다면,\
이번에는 그 반대로 객체를 문자로 변환하는 작업을 확인할 수 있다.

### ConverterController
```java
@Slf4j
@Controller
public class ConverterController {

    @GetMapping("/converter-view")
    public String converterView(Model model) {

        model.addAttribute("number", 100000);
        model.addAttribute("ipPort", new IpPort("127.0.0.1", 8080));
        return "converter-view";
    }
}

```
`Model`에 숫자 10000와 ipPort객체를 담아서 뷰 템플릿에 전달한다.
<br/>
<br/>

`resources/templates/converter-view.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<ul>
    <li>${number}: <span th:text="${number}" ></span></li>
    <li>${{number}}: <span th:text="${{number}}" ></span></li>
    <li>${ipPort}: <span th:text="${ipPort}" ></span></li>
    <li>${{ipPort}}: <span th:text="${{ipPort}}" ></span></li>
</ul>
</body>
</html>

```
타임리프는 `${{...}}` 를 사용하면 자동으로 컨버전 서비스를 사용해서 \
변환된 결과를 출력해준다. 물론 스프링과 통합 되어서 \
스프링이 제공하는 컨버전 서비스를 사용하므로, 우리가 등록한 컨버터들을 사용할 수 있다.

* **변수 표현식 : `${...}`**
* **컨버전 서비스 적용 : `${{...}}`**
<br/>

### 실행 결과
```
• ${number}: 10000
• ${{number}}: 10000
• ${ipPort}: hello.typeconverter.type.IpPort@59cb0946
• ${{ipPort}}: 127.0.0.1:8080
```
<br/>

#### 실행 결과 로그
```
IntegerToStringConverter : convert source=10000
IpPortToStringConverter : convert
source=hello.typeconverter.type.IpPort@59cb0946
```
<br/>

#### ${{number}}
뷰 템플릿은 데이터를 문자로 출력한다. \
따라서 컨버터를 적용하게 되면 Integer타입인 10000을 \
String 타입으로 변환하는 컨버터인 `IntegerToStringConverter`를 실행하게 된다. \
이 부분은 컨버터를 실행하지 않아도 타임리프가 숫자를 문자로 \
자동으로 변환히기 때문에 컨버터를 적용할 때와 하지 않을 때가 같다.
<br/>
<br/>

#### ${{ipPort}} 
뷰 템플릿은 데이터를 문자로 출력한다. \
따라서 컨버터를 적용하게 되면 IpPort 타입을 String 타입으로 \
변환해야 하므로 IpPortToStringConverter 가 적용된다. \
그 결과 127.0.0.1:8080 가 출력된다.


### 폼에 적용하기
이번에는 컨버터를 폼에 적용해보자.

* **ConverterController - 코드 추가**
```java
@GetMapping("/converter/edit")
public String converterForm(Model model) {
    IpPort ipPort = new IpPort("127.0.0.1", 8080);
    Form form = new Form(ipPort);

    model.addAttribute("form", form);

    return "converter-form";
}

@PostMapping("/converter/edit")
public String converterEdit(@ModelAttribute Form form, Model model) {
    IpPort ipPort = form.getIpPort();
    model.addAttribute("ipPort", ipPort);
    return "converter-view";
}
```
Form 객체를 데이터를 전달하는 폼 객체로 사용한다.

* `GET /converter/edit` : IpPort 를 뷰 템플릿 폼에 출력한다.
* `POST /converter/edit` : 뷰 템플릿 폼의 IpPort 정보를 받아서 출력한다.
<br/>

`resources/templates/converter-form.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>

<body>
<form th:object="${form}" th:method="post">
    th:field <input type="text" th:field="*{ipPort}"><br/>
    th:value <input type="text" th:value="*{ipPort}">(보여주기 용도)<br/>
    <input type="submit"/>
</form>
</body>
</html>
```
타임리프의 th:field 는 앞서 설명했듯이 id, name를 출력하는 등 \
다양한 기능이 있는데, 여기에 컨버전 서비스도 함께 적용된다.

* `GET /converter/edit`
    * th:field 가 자동으로 컨버전 서비스를 적용해주어서 ${{ipPort}} 처럼 적용이 되었다. \
    따라서 IpPort String 으로 변환된다.
* `POST /converter/edit`
    * @ModelAttribute 를 사용해서 String IpPort 로 변환된다.


## 포맷터 - Formatter
Converter 는 입력과 출력 타입에 제한이 없는, 범용 타입 변환 기능을 제공한다.\
이번에는 일반적인 웹 애플리케이션 환경을 생각해보자. \
불린 타입을 숫자로 바꾸는 것 같은 범용 기능 보다는 \
개발자 입장에서는 문자를 다른 타입으로 변환하거나, \
다른 타입을 문자로 변환하는 상황이 대부분이다.

앞서 살펴본 예제들을 떠올려 보면 문자를 다른 객체로 변환하거나 \
객체를 문자로 변환하는 일이 대부분이다.
<br/>
<br/>

### 웹 애플리케이션에서 객체를 문자로, 문자를 객체로 변환하는 예
* 화면에 숫자를 출력해야 하는데, Integer String 출력 시점에 \
    숫자 1000 문자 "1,000" 이렇게 1000 단위에 쉼표를 넣어서 출력하거나, \
    또는 "1,000" 라는 문자를 1000 이라는 숫자로 변경해야 한다.
* 날짜 객체를 문자인 "2021-01-01 10:50:11" 와 같이 출력하거나 또는 그 반대의 상황
<br/>


### Locale
여기에 추가로 날짜 숫자의 표현 방법은 `Locale` 현지화 정보가 사용될 수 있다.

이렇게 객체를 특정한 포멧에 맞추어 문자로 출력하거나 \
또는 그 반대의 역할을 하는 것에 특화된 기능이 바로 포맷터(`Formatter`)이다. \
포맷터는 컨버터의 특별한 버전으로 이해하면 된다.
<br/>
<br/>


### Converter vs Formatter
* `Converter`는 범용(객체 객체)
* `Formatter`는 문자에 특화(객체 문자, 문자 객체) + 현지화(Locale)
    * `Converter` 의 특별한 버전

<br/>


### 포맷터 - Formatter 만들기
포맷터( Formatter )는 객체를 문자로 변경하고, \
문자를 객체로 변경하는 두 가지 기능을 모두 수행한다.

* `String print(T object, Locale locale)` : 객체를 문자로 변경한다.
* `T parse(String text, Locale locale)` : 문자를 객체로 변경한다

#### Formatter 인터페이스
```java
public interface Printer<T> {
    String print(T object, Locale locale);
}

public interface Parser<T> {
    T parse(String text, Locale locale) throws ParseException;
}

public interface Formatter<T> extends Printer<T>, Parser<T> {
}
```
숫자 1000 을 문자 "1,000" 으로 그러니까, \
1000 단위로 쉼표가 들어가는 포맷을 적용해보자. \
그리고 그 반대도 처리해주는 포맷터를 만들어보자.
<br/>
<br/>

### MyNumberFormatter
```java
@Slf4j
public class MyNumberFormatter implements Formatter<Number> {
    @Override
    public Number parse(String text, Locale locale) throws ParseException {
        log.info("text={}, locale={}", text, locale);

        return NumberFormat.getNumberInstance(locale).parse(text);

    }

    @Override
    public String print(Number object, Locale locale) {
        log.info("object={}, locale={}", object, locale);

        return NumberFormat.getInstance(locale).format(object);
    }
}
```
"1,000" 처럼 숫자 중간의 쉼표를 적용하려면 \
자바가 기본으로 제공하는 NumberFormat 객체를 사용하면 된다. \
이 객체는 Locale 정보를 활용해서 나라별로 다른 숫자 포맷을 만들어준다.

parse() 를 사용해서 문자를 숫자로 변환한다. \
참고로 Number 타입은 Integer , Long 과 같은 숫자 타입의 부모 클래스이다.\
print() 를 사용해서 객체를 문자로 변환한다.

잘 동작하는지 테스트 코드를 만들어보자.
<br/>
<br/>

### MyNumberFormatterTest
```java 
class MyNumberFormatterTest {

    MyNumberFormatter formatter = new MyNumberFormatter();

    @Test
    void parse() throws ParseException {

        Number result = formatter.parse("1,000", Locale.KOREA);
        assertThat(result).isEqualTo(1000L);
    }

    @Test
    void print() {
        String result = formatter.print(1000, Locale.KOREA);
        assertThat(result).isEqualTo("1,000");
    }
}
```
`parse()`의 결과가 Long 이기 때문에 `isEqualTo(1000L)`을 통해 \
비교할 때 마지막에 L 을 넣어주어야 한다.
<br/>
<br/>

#### 실행 결과 로그
```
MyNumberFormatter - text=1,000, locale=ko_KR
MyNumberFormatter - object=1000, locale=ko_KR
```

> 스프링은 용도에 따라 다양한 방식의 포맷터를 제공한다.\
> - Formatter 포맷터\
> - AnnotationFormatterFactory 필드의 타입이나 애노테이션 정보를 활용할 수 있는 포맷터\
> 자세한 내용은 공식 문서를 참고하자.\
> https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#format
<br/>

## 포맷터를 지원하는 컨버전 서비스
컨버전 서비스에는 컨버터만 등록할 수 있고, 포맷터를 등록할 수 는 없다. \
그런데 생각해보면 포맷터는 객체 -> 문자, 문자 -> 객체로 변환하는 특별한 컨버터일 뿐이다.

포맷터를 지원하는 컨버전 서비스를 사용하면 컨버전 서비스에 포맷터를 추가할 수 있다. \
내부에서 어댑터 패턴을 사용해서 Formatter 가 Converter 처럼 동작하도록 지원한다.

`FormattingConversionService`는 포맷터를 지원하는 컨버전 서비스이다.\
`DefaultFormattingConversionService`는 `FormattingConversionService`에 \
기본적인 통화, 숫자 관련 몇가지 기본 포맷터를 추가해서 제공한다.

```java
public class FormattingConversionServiceTest {
    
    @Test
    void formattingConversionService() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();

        //컨버터 등록
        conversionService.addConverter(new StringToIpPortConverter());
        conversionService.addConverter(new IpPortToStringConverter());

        //포맷터 등록
        conversionService.addFormatter(new MyNumberFormatter());

        //컨버터 사용
        IpPort ipPort = conversionService.convert("127.0.0.1:8080", IpPort.class);
        assertThat(ipPort).isEqualTo(new IpPort("127.0.0.1", 8080));

        //포맷터 사용
        assertThat(conversionService.convert(1000, String.class)).isEqualTo("1,000");
        assertThat(conversionService.convert("1,000", Long.class)).isEqualTo(1000L);
    }
}
```
<br/>

### DefaultFormattingConversionService 상속 관계
`FormattingConversionService` 는 `ConversionService`관련 기능을 상속받기 때문에 \
결과적으로 컨버터도 포맷터도 모두 등록할 수 있다. \
그리고 사용할 때는 `ConversionService`가 제공하는 convert 를 사용하면 된다.\
추가로 스프링 부트는 `DefaultFormattingConversionService`를 상속 받은\
`WebConversionService`를 내부에서 사용한다.
<br/>
<br/>

## 포맷터 적용하기
포맷터를 웹 애플리케이션에 적용해보자

### WebConfig - 수정
```java
@Configuration
public class WebConverter implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
//        registry.addConverter(new StringToIntegerConverter());
//        registry.addConverter(new IntegerToStringConverter());
        registry.addConverter(new StringToIpPortConverter());
        registry.addConverter(new IpPortToStringConverter());

        // 포맷터 등록
        registry.addFormatter(new MyNumberFormatter());
    }
}
```
`StringToIntegerConverter`, `IntegerToStringConverter`를 꼭 주석처리 하자.

`MyNumberFormatter`도 숫자 -> 문자, 문자 -> 숫자로 변경하기 때문에 \
둘의 기능이 겹친다. 우선순위는 컨버터가 우선하므로 포맷터가 적용되지 않고, \
컨버터가 적용된다.

* http://localhost:8080/converter-view
* http://localhost:8080/hello-v2?data=10,000
<br/>

## 스프링이 제공하는 기본 포맷터
스프링은 자바에서 기본으로 제공하는 타입들에 대해 수 많은 포맷터를 기본으로 제공한다.\
IDE에서 Formatter 인터페이스의 구현 클래스를 찾아보면 \
수 많은 날짜나 시간 관련 포맷터가 제공되는 것을 확인할 수 있다.

그런데 포맷터는 기본 형식이 지정되어 있기 때문에, \
객체의 각 필드마다 다른 형식으로 포맷을 지정하기는 어렵다.

스프링은 이런 문제를 해결하기 위해 애노테이션 기반으로 \
원하는 형식을 지정해서 사용할 수 있는 매우 유용한 포맷터 두 가지를 기본으로 제공한다.

* `@NumberFormat` : 숫자 관련 형식 지정 포맷터 사용, \
    `NumberFormatAnnotationFormatterFactory`
* `@DateTimeFormat` : 날짜 관련 형식 지정 포맷터 사용,\
    `Jsr310DateTimeFormatAnnotationFormatterFactory`
<br/>

### FormatterController
```java
@Slf4j
@Controller
public class FormatterController {

    @GetMapping("/formatter/edit")
    public String formatterForm(Model model) {
        Form form = new Form();
        form.setNumber(10000);
        form.setLocalDateTime(LocalDateTime.now());
        model.addAttribute("form", form);

        return "formatter-form";
    }

    @PostMapping("/formatter/edit")
    public String formatterEdit(@ModelAttribute Form form) {
        return "formatter-view";
    }

    @Data
    static class Form {

        @NumberFormat(pattern = "###,###")
        private Integer number;

        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime localDateTime;
    }
}
```
<br/>

* `templates/formatter-form.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>

<body>
<form th:object="${form}" th:method="post">
    number <input type="text" th:field="*{number}"><br/>
    localDateTime <input type="text" th:field="*{localDateTime}"><br/>
    <input type="submit"/>
</form>
</body>

</html>
```
<br/>

* `templates/formatter-view.html`
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>

<body>
<ul>
    <li>${form.number}: <span th:text="${form.number}" ></span></li>
    <li>${{form.number}}: <span th:text="${{form.number}}" ></span></li>
    <li>${form.localDateTime}: <span th:text="${form.localDateTime}" ></span></li>
    <li>${{form.localDateTime}}: <span th:text="${{form.localDateTime}}" ></span></li>
</ul>
</body>

</html>
```
<br/>

#### 결과
* http://localhost:8080/formatter/edit
```
• ${form.number}: 10000
• ${{form.number}}: 10,000
• ${form.localDateTime}: 2021-01-01T00:00:00
• ${{form.localDateTime}}: 2021-01-01 00:00:00
```
> @NumberFormat , @DateTimeFormat 의 자세한 사용법은 \
> 다음 링크를 참고하거나 관련 애노테이션을 검색해보자.\
> https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#formatCustomFormatAnnotations


## 정리
컨버터를 사용하든, 포맷터를 사용하든 등록 방법은 다르지만, \
사용할 때는 컨버전 서비스를 통해서 일관성 있게 사용할 수 있다.

주의할 점으로는 다음과 같다.\
메시지 컨버터(`HttpMessageConverter`)에는 컨버전 서비스가 적용되지 않는다.\
특히 객체를 JSON으로 변환할 때 메시지 컨버터를 사용하면서 \
이 부분을 많이 오해하는데, HttpMessageConverter 의 역할은 \
HTTP 메시지 바디의 내용을 객체로 변환하거나 \
객체를 HTTP 메시지 바디에 입력하는 것이다. 

예를 들어서 JSON을 객체로 변환하는 메시지 컨버터는 내부에서 \
Jackson 같은 라이브러리를 사용한다. 객체를 JSON으로 변환한다면 \
그 결과는 이 라이브러리에 달린 것이다. 따라서 JSON 결과로 \
만들어지는 숫자나 날짜 포맷을 변경하고 싶으면 해당 라이브러리가 제공하는 \
설정을 통해서 포맷을 지정해야 한다. 결과적으로 이것은 컨버전 서비스와 전혀 관계가 없다.

컨버전 서비스는 `@RequestParam`, `@ModelAttribute`, `@PathVariable`, \
뷰 템플릿 등에서 사용할 수 있다.
<br/>
<br/>



## 파일 업로드 소개
일반적으로 사용하는 HTML Form을 통한 파일 업로드를 이해하려면 \
먼저 폼을 전송하는 다음 두 가지 방식의 차이를 이해해야 한다.

### HTML 폼 전송 방식
* **application/x-www-form-urlencoded**
* **multipart/form-data**
<br/>

### application/x-www-form-urlencoded 방식
![image](https://github.com/jub3907/Spring-study/assets/58246682/f9b6b02e-651f-48b6-8865-1768a4247fbe)

application/x-www-form-urlencoded 방식은 HTML 폼 데이터를 서버로 전송하는 \
가장 기본적인 방법이다. Form 태그에 별도의 enctype 옵션이 없으면\
웹 브라우저는 요청 HTTP 메시지의 헤더에 다음 내용을 추가한다.

```json
Content-Type: application/x-www-form-urlencoded
```

그리고 폼에 입력한 전송할 항목을 HTTP Body에 문자로 \
username=kim&age=20 와 같이 & 로 구분해서 전송한다.

파일을 업로드 하려면 파일은 문자가 아니라 바이너리 데이터를 전송해야 한다. \
문자를 전송하는 이 방식으로 파일을 전송하기는 어렵다. \
그리고 또 한가지 문제가 더 있는데, 보통 폼을 전송할 때\
파일만 전송하는 것이 아니라는 점이다. 다음 예를 보자.
```
- 이름
- 나이
- 첨부파일
```
여기에서 이름과 나이도 전송해야 하고, 첨부파일도 함께 전송해야 한다. \
문제는 이름과 나이는 문자로 전송하고, \
첨부파일은 바이너리로 전송해야 한다는 점이다. 

여기에서 문제가 발생한다. **문자와 바이너리를 동시에 전송**해야 하는 상황이다.\
이 문제를 해결하기 위해 HTTP는 `multipart/form-data` 라는 전송 방식을 제공한다.
<br/>
<br/>

### multipart/form-data 방식
![image](https://github.com/jub3907/Spring-study/assets/58246682/fbcea64d-b390-4f6e-8f81-1214100dc5a7)

이 방식을 사용하려면 Form 태그에 별도의 enctype="multipart/form-data" 를 지정해야 한다.\
`multipart/form-data`방식은 다른 종류의 여러 파일과 \
폼의 내용 함께 전송할 수 있다. (그래서 이름이 `multipart` 이다.)

폼의 입력 결과로 생성된 HTTP 메시지를 보면 각각의 전송 항목이 구분이 되어있다. \
`ContentDisposition`이라는 항목별 헤더가 추가되어 있고 여기에 부가 정보가 있다. \
예제에서는 `username`, `age`, `file1`이 각각 분리되어 있고, \
폼의 일반 데이터는 각 항목별로 문자가 전송되고, \
파일의 경우 파일 이름과 Content-Type이 추가되고 바이너리 데이터가 전송된다.

`multipart/form-data`는 이렇게 각각의 항목을 구분해서, 한번에 전송하는 것이다.
<br/>
<br/>

### Part
multipart/form-data 는 application/x-www-form-urlencoded 와 비교해서 \
매우 복잡하고 각각의 부분( Part )로 나누어져 있다. \
그렇다면 이렇게 복잡한 HTTP 메시지를 서버에서 어떻게 사용할 수 있을까?


## 서블릿과 파일 업로드1
먼저 서블릿을 통한 파일 업로드를 코드와 함께 알아보자.

### ServletUploadControllerV1
```java
@Slf4j
@Controller
@RequestMapping("/servlet/v1")
public class ServletUploadControllerV1 {

    @GetMapping("/upload")
    public String newFile() {
        return "upload-form";
    }

    @PostMapping("/upload")
    public String saveFileV1(HttpServletRequest request) throws ServletException, IOException {
        log.info("request={}", request);

        String itemName = request.getParameter("itemName");
        log.info("itemName={}", itemName);

        Collection<Part> parts = request.getParts();
        log.info("parts={}", parts);

        return "upload-form";
    }
}
```
`request.getParts()` : multipart/form-data 전송 방식에서 각각 나누어진 부분을 받아서 확인할 수 있다.
<br/>
<br/>

`resources/templates/upload-form.html`
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">

<head>
    <meta charset="utf-8">
</head>

<body>
<div class="container">
    <div class="py-5 text-center">
        <h2>상품 등록 폼</h2>
    </div>
    <h4 class="mb-3">상품 입력</h4>
    <form th:action method="post" enctype="multipart/form-data">
        <ul>
            <li>상품명 <input type="text" name="itemName"></li>
            <li>파일<input type="file" name="file" ></li>
        </ul>
        <input type="submit"/>
    </form>
</div> <!-- /container -->
</body>

</html>
```
<br/>

테스트를 진행하기 전에 먼저 다음 옵션들을 추가하자.
* `application.properties`
    ```
    logging.level.org.apache.coyote.http11=debug
    ```
이 옵션을 사용하면 HTTP 요청 메시지를 확인할 수 있다.
<br/>
<br/>

### 실행 및 결과
* http://localhost:8080/servlet/v1/upload

실행해보면 `logging.level.org.apache.coyote.http11`옵션을 통한 \
로그에서 `multipart/formdata`방식으로 전송된 것을 확인할 수 있다.

```HTTP
Content-Type: multipart/form-data; boundary=----xxxx

------xxxx
Content-Disposition: form-data; name="itemName"

Spring
------xxxx
Content-Disposition: form-data; name="file"; filename="test.data"
Content-Type: application/octet-stream

sdklajkljdf...
```
<br/>

## 멀티파트 사용 옵션
### 업로드 사이즈 제한
```
spring.servlet.multipart.max-file-size=1MB
spring.servlet.multipart.max-request-size=10MB
```
큰 파일을 무제한 업로드하게 둘 수는 없으므로 업로드 사이즈를 제한할 수 있다.\
사이즈를 넘으면 예외( SizeLimitExceededException )가 발생한다.\
* max-file-size : 파일 하나의 최대 사이즈, 기본 1MB
* max-request-size : 멀티파트 요청 하나에 여러 파일을 업로드 할 수 있는데, 그 전체 합이다. 기본 10MB
<br/>

### spring.servlet.multipart.enabled 끄기
`spring.servlet.multipart.enabled=false`

* 결과 로그
    ```
    request=org.apache.catalina.connector.RequestFacade@xxx
    itemName=null
    parts=[]
    ```
멀티파트는 일반적인 폼 요청인 `application/x-www-form-urlencoded`보다 훨씬 복잡하다.\
`spring.servlet.multipart.enabled`옵션을 끄면 \
서블릿 컨테이너는 멀티파트와 관련된 처리를 하지 않는다.
그래서 결과 로그를 보면 `request.getParameter("itemName")`, \
`request.getParts()`의 결과가 비어있다. 
<br/>
<br/>

### spring.servlet.multipart.enabled 켜기
`spring.servlet.multipart.enabled=true `

이 옵션을 켜면 스프링 부트는 서블릿 컨테이너에게 \
멀티파트 데이터를 처리하라고 설정한다. 

```
request=org.springframework.web.multipart.support.StandardMultipartHttpServletRequest
itemName=Spring
parts=[ApplicationPart1, ApplicationPart2]
```

request.getParameter("itemName") 의 결과도 잘 출력되고, \
request.getParts() 에도 요청한 두 가지 멀티파트의 \
부분 데이터가 포함된 것을 확인할 수 있다. 이 옵션을 켜면 \
복잡한 멀티파트 요청을 처리해서 사용할 수 있게 제공한다.

로그를 보면 `HttpServletRequest` 객체가 `RequestFacade` -> \
`StandardMultipartHttpServletRequest` 로 변한 것을 확인할 수 있다.
<br/>
<br/>

### 참고.
spring.servlet.multipart.enabled 옵션을 켜면 \
스프링의 DispatcherServlet 에서 멀티파트 리졸버( `MultipartResolver` )를 실행한다.

멀티파트 리졸버는 멀티파트 요청인 경우 서블릿 컨테이너가 전달하는 \
일반적인 `HttpServletRequest` 를 `MultipartHttpServletRequest` 로 변환해서 반환한다.

`MultipartHttpServletRequest` 는 `HttpServletRequest` 의 자식 인터페이스이고, \
멀티파트와 관련된 추가 기능을 제공한다.

스프링이 제공하는 기본 멀티파트 리졸버는 `MultipartHttpServletRequest` 인터페이스를 \
구현한 `StandardMultipartHttpServletRequest` 를 반환한다.\
이제 컨트롤러에서 `HttpServletRequest` 대신에 `MultipartHttpServletRequest` 를 \
주입받을 수 있는데, 이것을 사용하면 멀티파트와 관련된 \
여러가지 처리를 편리하게 할 수 있다. 

그런데 `MultipartFile` 이라는 것을 사용하는 것이 더 편하기 때문에\
`MultipartHttpServletRequest` 를 잘 사용하지는 않는다. \
더 자세한 내용은 `MultipartResolver` 를 검색해보자.
<br/>
<br/>


## 서블릿과 파일 업로드2
서블릿이 제공하는 `Part` 에 대해 알아보고 실제 파일도 서버에 업로드 해보자.

먼저 파일을 업로드를 하려면 실제 파일이 저장되는 경로가 필요하다.

해당 경로에 실제 폴더를 만들어두자.\
그리고 다음에 만들어진 경로를 입력해두자.


* **application.properties**
    ```
    file.dir=파일 업로드 경로 설정(예): /Users/kimyounghan/study/file/
    ```
    * 꼭 해당 경로에 실제 폴더를 미리 만들어두자.
    * application.properties 에서 설정할 때 마지막에 / (슬래시)가 포함된 것에 주의하자.

<br/>


### ServletUploadControllerV2
```java
@Slf4j
@Controller
@RequestMapping("/servlet/v2")
public class ServletUploadControllerV2 {

    @Value("${file.dir}")
    private String fileDir;

    @GetMapping("/upload")
    public String newFile() {
        return "upload-form";
    }

    @PostMapping("/upload")
    public String saveFileV1(HttpServletRequest request) throws ServletException, IOException {
        log.info("request={}", request);

        String itemName = request.getParameter("itemName");
        log.info("itemName={}", itemName);

        Collection<Part> parts = request.getParts();
        log.info("parts={}", parts);

        for (Part part : parts) {
            log.info("==== PART ====");
            log.info("name={}", part.getName());

            Collection<String> headerNames = part.getHeaderNames();
            for (String headerName : headerNames) {
                log.info("header {} : {}", headerName, part.getHeader(headerName));
            }

            // 편의 메소드
            //content-disposition; filename
            log.info("submittedFilename={}", part.getSubmittedFileName());
            log.info("size={}", part.getSize());

            // 데이터 읽기
            InputStream inputStream = part.getInputStream();
            String body = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            log.info("body={}", body);

            // 파일에 저장하기
            if (StringUtils.hasText(part.getSubmittedFileName())) {
                String fullPath = fileDir + part.getSubmittedFileName();
                log.info("파일 저장 fullpath={}", fullPath);

                part.write(fullPath);
            }

            inputStream.close();
        }

        return "upload-form";
    }
}
```
<br/>

```java
@Value("${file.dir}")
private String fileDir;
```
`application.properties`에서 설정한 `file.dir` 의 값을 주입한다

멀티파트 형식은 전송 데이터를 하나하나 각각 부분( Part )으로 나누어 전송한다. \
parts 에는 이렇게 나누어진 데이터가 각각 담긴다.

서블릿이 제공하는 Part 는 멀티파트 형식을 편리하게 읽을 수 있는 다양한 메서드를 제공한다.
<br/>
<br/>

### Part 주요 메서드
`part.getSubmittedFileName()` : 클라이언트가 전달한 파일명\
`part.getInputStream()`: Part의 전송 데이터를 읽을 수 있다.\
`part.write(...)`: Part를 통해 전송된 데이터를 저장할 수 있다
<br/>
<br/>


### 실행 및 결과
* http://localhost:8080/servlet/v2/upload

다음 내용을 전송했다.
* `itemName` : 상품A
* `file` : 스크릿샷.png

```
==== PART ====
name=itemName
header content-disposition : form-data; name="itemName"
submittedFilename=null
size=6
body=123123
==== PART ====
name=file
header content-disposition : form-data; name="file"; filename="달.png"
header content-type : image/png
submittedFilename=달.png
size=113164
body=�PNG
파일 저장 fullpath=C:/Users/s_jub3907/Documents/dev/Spring-study/Spring-study/upload/file/달.png
```
파일 저장 경로에 가보면 실제 파일이 저장된 것을 확인할 수 있다. \
만약 저장이 되지 않았다면 파일 저장 경로를 다시 확인하자.

서블릿이 제공하는 Part 는 편하기는 하지만, `HttpServletRequest`를 사용해야 하고, \
추가로 파일 부분만 구분하려면 여러가지 코드를 넣어야 한다. \
이번에는 스프링이 이 부분을 얼마나 편리하게 제공하는지 확인해보자.
<br/>
<br/>


## 스프링과 파일 업로드
스프링은 `MultipartFile`이라는 인터페이스로 멀티파트 파일을 매우 편리하게 지원한다

### SpringUploadController
```java
@Slf4j
@Controller
@RequestMapping("/spring")
public class SpringUploadController {

    @Value("${file.dir}")
    private String fileDir;

    @GetMapping("/upload")
    public String newFile() {
        return "upload-form";
    }

    @PostMapping("/upload")
    public String saveFile(@RequestParam String itemName,
                           @RequestParam MultipartFile file,
                           HttpServletRequest request) throws IOException {
        log.info("request={}", request);
        log.info("itemName={}", itemName);
        log.info("file={}", file);

        if (!file.isEmpty()) {
            String fullPath = fileDir + file.getOriginalFilename();
            log.info("파일 저장 fullPath={}", fullPath);
            file.transferTo(new File(fullPath));
        }

        return "upload-form";
    }
}
```
코드를 보면 스프링 답게 딱 필요한 부분의 코드만 작성하면 된다.

`@RequestParam MultipartFile file`
업로드하는 HTML Form의 name에 맞추어 @RequestParam 을 적용하면 된다. \
추가로 @ModelAttribute 에서도 MultipartFile 을 동일하게 사용할 수 있다.

#### MultipartFile 주요 메서드
* `file.getOriginalFilename()` : 업로드 파일 명
* `file.transferTo(...)` : 파일 저장
<br/>


### 실행 및 결과
* http://localhost:8080/spring/upload
```
request=org.springframework.web.multipart.support.StandardMultipartHttpServletRequest@628275caitemName=123123123
file=org.springframework.web.multipart.support.tandardMultipartHttpServletRequest$StandardMultipartFile@46d15352
파일 저장 fullPath=C:/Users/s_jub3907/Documents/dev/Spring-study/Spring-study/upload/file/달.png
```
<br/>


## 예제로 구현하는 파일 업로드, 다운로드
실제 파일이나 이미지를 업로드, 다운로드 할 때는 몇가지 고려할 점이 있는데, \
구체적인 예제로 알아보자.

### 요구사항
* 상품을 관리
    * 상품 이름
    * 첨부파일 하나
    * 이미지 파일 여러개
* 첨부파일을 업로드 다운로드 할 수 있다.
* 업로드한 이미지를 웹 브라우저에서 확인할 수 있다.
<br/>

### Item - 상품 도메인
```java
@Data
public class Item {

    private Long id;
    private String itemName;
    private UploadFile attachFile;
    private List<UploadFile> imageFiles;

}
```
<br/>


### ItemRepository - 상품 리포지토리
```java
@Repository
public class itemRepository {

    private final Map<Long, Item> store = new HashMap<>();
    private long sequence = 0L;

    public Item save(Item item) {
        item.setId(++sequence);
        store.put(item.getId(), item);
        return item;
    }

    public Item findById(Long id) {
        return store.get(id);
    }
}
```
<br/>


### UploadFile - 업로드 파일 정보 보관
```java
@Data
public class UploadFile {

    private String uploadFileName;
    private String storeFileName;

    public UploadFile(String uploadFileName, String storeFileName) {
        this.uploadFileName = uploadFileName;
        this.storeFileName = storeFileName;
    }
}

```
* `uploadFileName` : 고객이 업로드한 파일명
* `storeFileName` : 서버 내부에서 관리하는 파일명

고객이 업로드한 파일명으로 서버 내부에 파일을 저장하면 안된다. \
왜냐하면 서로 다른 고객이 같은 파일이름을 업로드 하는 경우 \
기존 파일 이름과 충돌이 날 수 있다. 

서버에서는 저장할 파일명이 겹치지 않도록 내부에서 관리하는 별도의 파일명이 필요하다.
<br/>
<br/>


### FileStore - 파일 저장과 관련된 업무 처리
```java
@Component
public class FileStore {

    @Value("${file.dir}")
    private String fileDir;

    public String getFullPath(String filename) {
        return fileDir + filename;
    }

    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
        List<UploadFile> storeFileResult = new ArrayList<>();

        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFile.isEmpty()) {
                storeFileResult.add(storeFile(multipartFile));
            }
        }

        return storeFileResult;
    }

    public UploadFile storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFilename);

        multipartFile.transferTo(new File(getFullPath(storeFileName)));

        return new UploadFile(originalFilename, storeFileName);
    }

    private static String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename);
        String uuid = UUID.randomUUID().toString();

        return uuid + "." + ext;
    }

    // 확장자 추출
    private static String extractExt(String originalFilename) {
        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
    }
}
```
멀티파트 파일을 서버에 저장하는 역할을 담당한다.
* `createStoreFileName()`
    * 서버 내부에서 관리하는 파일명은 유일한 이름을 생성하는\
    UUID 를 사용해서 충돌하지 않도록 한다.
* `extractExt()`
    * 확장자를 별도로 추출해서 서버 내부에서 관리하는 파일명에도 붙여준다. \
    예를 들어서 고객이 a.png 라는 이름으로 업로드 하면\
    51041c62-86e4-4274-801d-614a7d994edb.png 와 같이 저장한다.

<br/>


### ItemForm
```java
@Data
public class ItemForm {
    private Long itemId;
    private String ItemName;
    private List<MultipartFile> imageFiles;
    private MultipartFile attachFile;
}

```
상품 저장용 폼이다.
* `List<MultipartFile> imageFiles`
    * 이미지를 다중 업로드 하기 위해 `MultipartFile` 를 사용했다.
* `MultipartFile attachFile `
    * 멀티파트는 @ModelAttribute 에서 사용할 수 있다.
<br/>


### ItemController
```java
@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;
    private final FileStore fileStore;

    @GetMapping("/items/new")
    public String newItem(@ModelAttribute ItemForm form) {
        return "item-form";
    }

    @PostMapping("/items/new")
    public String saveItem(@ModelAttribute ItemForm form, RedirectAttributes redirectAttributes) throws IOException {
        // 파일 저장
        UploadFile attachFile = fileStore.storeFile(form.getAttachFile());
        List<UploadFile> storeImageFiles = fileStore.storeFiles(form.getImageFiles());

        // 데이터베이스에 저장
        Item item = new Item();
        item.setItemName(form.getItemName());
        item.setAttachFile(attachFile);
        item.setImageFiles(storeImageFiles);
        itemRepository.save(item);
        // 이렇게, 일반적으로 이미지 자체를 저장하지는 않는다.
        // 따로 경로만 DB에 저장하고, 이미지는 S3등에 저장.

        redirectAttributes.addAttribute("itemId", item.getId());

        return "redirect:/items/{itemId}";
    }

    @GetMapping("/items/{id}")
    public String items(@PathVariable Long id, Model model) {
        Item item = itemRepository.findById(id);
        model.addAttribute("item", item);
        return "item-view";
    }

    @ResponseBody
    @GetMapping("/images/{filename}")
    public Resource downloadImage(@PathVariable String filename) throws MalformedURLException {
        return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

    @GetMapping("/attach/{itemId}")
    public ResponseEntity<Resource> downloadAttach(@PathVariable Long itemId) throws MalformedURLException {
        Item item = itemRepository.findById(itemId);
        String storeFileName = item.getAttachFile().getStoreFileName();
        String uploadFileName = item.getAttachFile().getUploadFileName();

        UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(storeFileName));

        log.info("uploadFileName={}", uploadFileName);

        String encodedUploadFileName = UriUtils.encode(uploadFileName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}
```
* @GetMapping("/items/new")
    등록 폼을 보여준다.
* @PostMapping("/items/new")
    폼의 데이터를 저장하고 보여주는 화면으로 리다이렉트 한다.
* @GetMapping("/items/{id}")
    상품을 보여준다.
* @GetMapping("/images/{filename}")
    <img> 태그로 이미지를 조회할 때 사용한다. \
    UrlResource로 이미지 파일을 읽어서 @ResponseBody 로 \
    이미지 바이너리를 반환한다.
* @GetMapping("/attach/{itemId}") 
    파일을 다운로드 할 때 실행한다. \
    예제를 더 단순화 할 수 있지만, 파일 다운로드 시 권한 체크같은 \
    복잡한 상황까지 가정한다 생각하고 이미지 id 를 요청하도록 했다. \
    파일 다운로드시에는 고객이 업로드한 파일 이름으로 다운로드 하는게 좋다. \
    이때는 Content-Disposition 해더에 `attachment; filename="업로드 파일명" `값을 주면 된다.
<br/>


### 등록 폼 뷰
`resources/templates/item-form.html`
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="utf-8">
</head>

<body>
<div class="container">
  
    <div class="py-5 text-center">
        <h2>상품 등록</h2>
    </div>
  
    <form th:action method="post" enctype="multipart/form-data">
        <ul>
            <li>상품명 <input type="text" name="itemName"></li>
            <li>첨부파일<input type="file" name="attachFile" ></li>
            <li>이미지 파일들<input type="file" multiple="multiple" name="imageFiles" ></li>
        </ul>
        <input type="submit"/>
    </form>
  
</div> <!-- /container -->
</body>
</html>
```
다중 파일 업로드를 하려면 `multiple="multiple"` 옵션을 주면 된다.\
`ItemForm` 의 다음 코드에서 여러 이미지 파일을 받을 수 있다.\
`private List<MultipartFile> imageFiles;`
<br/>
<br/>


### 조회 뷰
`resources/templates/item-view.html`
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">

<head>
  <meta charset="utf-8">
</head>

<body>
<div class="container">
    <div class="py-5 text-center">
        <h2>상품 조회</h2>
    </div>
        상품명: <span th:text="${item.itemName}">상품명</span><br/>
        첨부파일: <a th:if="${item.attachFile}" th:href="|/attach/${item.id}|" th:text="${item.getAttachFile().getUploadFileName()}" /><br/>
    <img th:each="imageFile : ${item.imageFiles}" th:src="|/images/${imageFile.getStoreFileName()}|" width="300" height="300"/>
</div> <!-- /container -->
</body>

</html>
```
첨부 파일은 링크로 걸어두고, 이미지는 <img> 태그를 반복해서 출력한다.\
실행해보면 하나의 첨부파일을 다운로드 업로드 하고, \
여러 이미지 파일을 한번에 업로드 할 수 있다.