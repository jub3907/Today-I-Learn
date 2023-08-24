## 스프링 타입 컨버터
문자를 숫자로 변환하거나, 반대로 숫자를 문자로 변환해야 하는 것 처럼 애플리케이션을 개발하다 보면
타입을 변환해야 하는 경우가 상당히 많다.

다음 예를 보자.
<br/>
<br/>

### HelloController - 문자 타입을 숫자 타입으로 변경
```java
@Slf4j
@RestController
public class HelloController {

    @GetMapping("/hello-v1")
    public String helloV1(HttpServletRequest request) {
        String data = request.getParameter("data");
        Integer intValue = Integer.valueOf(data);
        System.out.println("intValue = " + intValue);
        return "ok";
    }
}
```
<br/>
#### 분석
`String data = request.getParameter("data")`\
HTTP 요청 파라미터는 모두 문자로 처리된다. \
따라서 요청 파라미터를 자바에서 다른 타입으로 변환해서 사용하고 싶으면 \
다음과 같이 숫자 타입으로 변환하는 과정을 거쳐야 한다.

`Integer intValue = Integer.valueOf(data)`

이번에는 스프링 MVC가 제공하는 @RequestParam 을 사용해보자.
<br/>
<br/>

### HelloController - 추가
```java
@GetMapping("/hello-v2")
public String helloV2(@RequestParam Integer data) {
    System.out.println("data = " + data);
    return "ok";
}
```
앞서 보았듯이 HTTP 쿼리 스트링으로 전달하는 data=10 부분에서 10은 \
숫자 10이 아니라 문자 10이다.

스프링이 제공하는 @RequestParam 을 사용하면 이 문자 10을 \
Integer 타입의 숫자 10으로 편리하게 받을 수 있다.\
이것은 스프링이 중간에서 타입을 변환해주었기 때문이다.

이러한 예는 @ModelAttribute , @PathVariable 에서도 확인할 수 있다.
```java
@ModelAttribute UserData data
class UserData {
 Integer data;
}
```
```java
/users/{userId}
@PathVariable("userId") Integer data
```
URL 경로는 문자다. /users/10 여기서 10도 숫자 10이 아니라 그냥 문자 "10"이다.\
data를 Integer 타입으로 받을 수 있는 것도 스프링이 타입 변환을 해주기 때문이다.
<br/>
<br/>


### 스프링 타입 변환 적용 예시
* 스프링 MVC 요청 파라미터
  * @RequestParam , @ModelAttribute , @PathVariable
* @Value 등으로 YML 정보 읽기
* XML에 넣은 스프링 빈 정보를 변환
* 뷰를 렌더링 할 때
<br/>

### 스프링과 타입 변환
이렇게 타입을 변환해야 하는 경우는 상당히 많다. \
개발자가 직접 하나하나 타입 변환을 해야 한다면, 생각만 해도 괴로울 것이다.

스프링이 중간에 타입 변환기를 사용해서 타입을 String Integer로 변환해주었기 때문에 \
개발자는 편리하게 해당 타입을 바로 받을 수 있다. \
앞에서는 문자를 숫자로 변경하는 예시를 들었지만, \
반대로 숫자를 문자로 변경하는 것도 가능하고, \
Boolean 타입을 숫자로 변경하는 것도 가능하다. 
만약 개발자가 새로운 타입을 만들어서 변환하고 싶으면 어떻게 하면 될까?
<br/>
<br/>

### 컨버터 인터페이스
```java
package org.springframework.core.convert.converter;
public interface Converter<S, T> {
  T convert(S source);
}
```
스프링은 확장 가능한 컨버터 인터페이스를 제공한다.\
개발자는 스프링에 추가적인 타입 변환이 필요하면 이 컨버터 인터페이스를 구현해서 등록하면 된다.\
이 컨버터 인터페이스는 모든 타입에 적용할 수 있다. \
필요하면 X Y 타입으로 변환하는 컨버터 인터페이스를 만들고, \
또 Y X 타입으로 변환하는 컨버터 인터페이스를 만들어서 등록하면 된다.

예를 들어서 문자로 "true" 가 오면 Boolean 타입으로 받고 싶으면 \
String Boolean 타입으로 변환되도록 컨버터 인터페이스를 만들어서 등록하고, \
반대로 적용하고 싶으면 Boolean String 타입으로 변환되도록 \
컨버터를 추가로 만들어서 등록하면 된다.
<br/>
<br/>

## 타입 컨버터 - Converter
타입 컨버터를 어떻게 사용하는지 코드로 알아보자.

타입 컨버터를 사용하려면 `org.springframework.core.convert.converter.Converter`\
인터페이스를 구현하면 된다. 

### 컨버터 인터페이스
```java
package org.springframework.core.convert.converter;
public interface Converter<S, T> {
  T convert(S source);
}
```
먼저 가장 단순한 형태인 문자를 숫자로 바꾸는 타입 컨버터를 만들어보자.
<br/>
<br/>



### StringToIntegerConverter - 문자를 숫자로 변환하는 타입 컨버터
```java
@Slf4j
public class StringToIntegerConverter implements Converter<String, Integer> {

    @Override
    public Integer convert(String source) {
        log.info("convert source={}", source);
        return Integer.valueOf(source);
    }
}
```
String을 Integer 로 변환하기 때문에 소스가 String이 된다. \
이 문자를 Integer.valueOf(source) 를 사용해서 \
숫자로 변경한 다음에 변경된 숫자를 반환하면 된다.
<br/>
<br/>

### IntegerToStringConverter - 숫자를 문자로 변환하는 타입 컨버터
```java
@Slf4j
public class IntegerToStringConverter implements Converter<Integer, String> {

    @Override
    public String convert(Integer source) {
        log.info("convert source={}", source);
        return String.valueOf(source);
    }
}
```
이번에는 숫자를 문자로 변환하는 타입 컨버터이다. 앞의 컨버터와 반대의 일을 한다. \
이번에는 숫자가 입력되기 때문에 소스가 Integer 가 된다.\
String.valueOf(source) 를 사용해서 문자로 변경한 다음 변경된 문자를 반환하면 된다.
<br/>
<br/>

### ConverterTest - 타입 컨버터 테스트 코드

### 
```java
public class ConverterTest {

    @Test
    void stringToInteger() {
        StringToIntegerConverter converter = new StringToIntegerConverter();
        Integer result = converter.convert("10");
        assertThat(result).isEqualTo(10);
    }

    @Test
    void integerToString() {
        IntegerToStringConverter converter = new IntegerToStringConverter();
        String result = converter.convert(10);
        assertThat(result).isEqualTo("10");
    }
}
```
<br/>

### 사용자 정의 타입 컨버터
타입 컨버터 이해를 돕기 위해 조금 다른 컨버터를 준비해보았다.\
127.0.0.1:8080 과 같은 IP, PORT를 입력하면 IpPort 객체로 변환하는 컨버터를 만들어보자.

### IpPort
```java
// "127.0.0.1:8080" -> ip
@Getter
@EqualsAndHashCode
public class IpPort {

    private String ip;
    private int port;

    public IpPort(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}
```
롬복의 @EqualsAndHashCode 를 넣으면 모든 필드를 사용해서 equals() , hashcode() 를 생성한다. \
따라서 모든 필드의 값이 같다면 a.equals(b) 의 결과가 참이 된다.
<br/>
<br/>

### StringToIpPortConverter - 컨버터
```java
@Slf4j
public class StringToIpPortConverter implements Converter<String, IpPort> {

    @Override
    public IpPort convert(String source) {
        log.info("convert source={}", source);
        // "127.0.0.1:8080"

        String[] split = source.split(":");
        String ip = split[0];
        int port = Integer.parseInt(split[1]);
        return new IpPort(ip, port);
    }
}
```
`127.0.0.1:8080` 같은 문자를 입력하면 `IpPort` 객체를 만들어 반환한다
<br/>
<br/>


### IpPortToStringConverter
```java
@Slf4j
public class IpPortToStringConverter implements Converter<IpPort, String> {
    @Override
    public String convert(IpPort source) {
        log.info("convert source={}", source);

        return source.getIp() + ":" + source.getPort();
    }
}

```
`IpPort` 객체를 입력하면 `127.0.0.1:8080` 같은 문자를 반환한다.
<br/>
<br/>



### ConverterTest - IpPort 컨버터 테스트 추가
```java
@Test
void stringToIpPort() {
    IpPortToStringConverter converter = new IpPortToStringConverter();
    IpPort source = new IpPort("127.0.0.1", 8080);
    String result = converter.convert(source);
    assertThat(result).isEqualTo("127.0.0.1:8080");
}

@Test
void ipPortToString() {
    StringToIpPortConverter converter = new StringToIpPortConverter();
    String source = "127.0.0.1:8080";
    IpPort result = converter.convert(source);

    // equals 애노테이션
    assertThat(result).isEqualTo(new IpPort("127.0.0.1", 8080));
}
```
타입 컨버터 인터페이스가 단순해서 이해하기 어렵지 않을 것이다.\
그런데 이렇게 타입 컨버터를 하나하나 직접 사용하면, \
개발자가 직접 컨버팅 하는 것과 큰 차이가 없다.

타입 컨버터를 등록하고 관리하면서 편리하게 변환 기능을 제공하는 역할을 하는 무언가가 필요하다.
<br/>
<br/>

### 참고 사항
스프링은 용도에 따라 다양한 방식의 타입 컨버터를 제공한다.

* Converter 기본 타입 컨버터
* ConverterFactory 전체 클래스 계층 구조가 필요할 때
* GenericConverter 정교한 구현, 대상 필드의 애노테이션 정보 사용 가능
* ConditionalGenericConverter 특정 조건이 참인 경우에만 실행

자세한 내용은 공식 문서를 참고하자.
https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#coreconvert


> 스프링은 문자, 숫자, 불린, Enum등 일반적인 타입에 대한 대부분의 컨버터를 기본으로 제공한다.\
> IDE에서 `Converter`, `ConverterFactory`, `GenericConverter`의 구현체를 찾아보면 \
> 수 많은 컨버터를 확인할 수 있다.
<br/>


## 컨버전 서비스 - ConversionService
이렇게 타입 컨버터를 하나하나 직접 찾아서 타입 변환에 사용하는 것은 매우 불편하다. \
그래서 스프링은 개별 컨버터를 모아두고 그것들을 묶어서 \
편리하게 사용할 수 있는 기능을 제공하는데,\
이것이 바로 컨버전 서비스(`ConversionService`)이다.

### ConversionService 인터페이스
```java
package org.springframework.core.convert;
import org.springframework.lang.Nullable;

public interface ConversionService {
    boolean canConvert(@Nullable Class<?> sourceType, Class<?> targetType);
    boolean canConvert(@Nullable TypeDescriptor sourceType, TypeDescriptor targetType);
    
    <T> T convert(@Nullable Object source, Class<T> targetType);
    Object convert(@Nullable Object source, @Nullable TypeDescriptor sourceType, TypeDescriptor targetType);
}
```
컨버전 서비스 인터페이스는 단순히 컨버팅이 가능한가? 확인하는 기능과, 컨버팅 기능을 제공한다.
사용 예를 확인해보자.
<br/>
<br/>

### ConversionServiceTest - 컨버전 서비스 테스트 코드
```java
public class ConversionServiceTest {

    @Test
    void conversionService() {
        // 등록
        DefaultConversionService conversionService = new DefaultConversionService();
        conversionService.addConverter(new StringToIpPortConverter());
        conversionService.addConverter(new StringToIntegerConverter());
        conversionService.addConverter(new IntegerToStringConverter());
        conversionService.addConverter(new IpPortToStringConverter());

        // 사용
        assertThat(conversionService.convert("10", Integer.class)).isEqualTo(10);
        assertThat(conversionService.convert(10, String.class)).isEqualTo("10");

        IpPort ipPort = new IpPort("127.0.0.1", 8080);
        String ipPortString = "127.0.0.1:8080";
        assertThat(conversionService.convert(ipPort, String.class)).isEqualTo(ipPortString);
        assertThat(conversionService.convert(ipPortString, IpPort.class)).isEqualTo(ipPort);
    }
}
```
DefaultConversionService 는 ConversionService 인터페이스를 구현했는데, \
추가로 컨버터를 등록하는 기능도 제공한다.
<br/>
<br/>

### 등록과 사용 분리
컨버터를 등록할 때는 StringToIntegerConverter 같은 타입 컨버터를 명확하게 알아야 한다. \
반면에 컨버터를 사용하는 입장에서는 타입 컨버터를 전혀 몰라도 된다. 

타입 컨버터들은 모두 컨버전 서비스 내부에 숨어서 제공된다. \
따라서 타입을 변환을 원하는 사용자는 컨버전 서비스 인터페이스에만 의존하면 된다. \
물론 컨버전 서비스를 등록하는 부분과 사용하는 부분을 분리하고 의존관계 주입을 사용해야 한다.

#### 컨버전 서비스 사용
`Integer value = conversionService.convert("10", Integer.class)`
<br/>
<br/>

### 인터페이스 분리 원칙 - ISP(Interface Segregation Principle)
인터페이스 분리 원칙은 클라이언트가 자신이 이용하지 않는 메서드에 의존하지 않아야 한다.

DefaultConversionService 는 다음 두 인터페이스를 구현했다.

* ConversionService : 컨버터 사용에 초점
* ConverterRegistry : 컨버터 등록에 초점

이렇게 인터페이스를 분리하면 컨버터를 사용하는 클라이언트와 \
컨버터를 등록하고 관리하는 클라이언트의 관심사를 명확하게 분리할 수 있다. \
특히 컨버터를 사용하는 클라이언트는 ConversionService 만 의존하면 되므로, \
컨버터를 어떻게 등록하고 관리하는지는 전혀 몰라도 된다. 

결과적으로 컨버터를 사용하는 클라이언트는 꼭 필요한 메서드만 알게된다. \
이렇게 인터페이스를 분리하는 것을 ISP 라 한다.

[ISP 참고 ](https://ko.wikipedia.org/wiki/%EC%9D%B8%ED%84%B0%ED%8E%98%EC%9D%B4%EC%8A%A4_%EB%B6%84%EB%A6%AC_%EC%9B%90%EC%B9%99)

스프링은 내부에서 ConversionService 를 사용해서 타입을 변환한다. \
예를 들어서 앞서 살펴본 @RequestParam 같은 곳에서 이 기능을 사용해서 타입을 변환한다.\

이제 컨버전 서비스를 스프링에 적용해보자.

## 스프링에 Converter 적용하기
웹 애플리케이션에 Converter 를 적용해보자.

### WebConfig - 컨버터 등록
```java
@Configuration
public class WebConverter implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToIntegerConverter());
        registry.addConverter(new StringToIpPortConverter());
        registry.addConverter(new IntegerToStringConverter());
        registry.addConverter(new IpPortToStringConverter());
    }
}
```
스프링은 내부에서 ConversionService 를 제공한다. \
우리는 WebMvcConfigurer 가 제공하는 addFormatters() 를 사용해서 \
추가하고 싶은 컨버터를 등록하면 된다. 

이렇게 하면 스프링은 내부에서 사용하는 ConversionService 에 컨버터를 추가해준다.\
등록한 컨버터가 잘 동작하는지 확인해보자.
<br/>
<br/>


### 실행
http://localhost:8080/hello-v2?data=10\
```
StringToIntegerConverter : convert source=10
data = 10
```
`?data=10` 의 쿼리 파라미터는 문자이고 이것을 Integer data 로 변환하는 과정이 필요하다.\
실행해보면 직접 등록한 StringToIntegerConverter가 작동하는 로그를 확인할 수 있다.

그런데 생각해보면 StringToIntegerConverter를 등록하기 전에도 이 코드는 잘 수행되었다.\
그것은 스프링이 내부에서 수 많은 기본 컨버터들을 제공하기 때문이다. \
컨버터를 추가하면 추가한 컨버터가 기본 컨버터 보다 높은 우선순위를 가진다.

이번에는 직접 정의한 타입인 IpPort 를 사용해보자.

### HelloController - 추가
```java
    @GetMapping("/ip-port")
    public String ipPort(@RequestParam IpPort ipPort) {
        System.out.println("ipPort IP = " + ipPort.getIp());
        System.out.println("ipPort PORT = " + ipPort.getPort());
        return "ok";
    }
```
#### 실행 결과
```
StringToIpPortConverter : convert source=127.0.0.1:8080
ipPort IP = 127.0.0.1
ipPort PORT = 8080
```
?ipPort=127.0.0.1:8080 쿼리 스트링이 @RequestParam IpPort ipPort 에서 \
객체 타입으로 잘 변환 된 것을 확인할 수 있다.
<br/>
<br/>

### 처리 과정
`@RequestParam`은 `@RequestParam`을 처리하는 `ArgumentResolver`인\
`RequestParamMethodArgumentResolver`에서 `ConversionService`를 사용해서 타입을 변환한다. \
부모 클래스와 다양한 외부 클래스를 호출하는 등 복잡한 내부 과정을 거치기 때문에\
대략 이렇게 처리되는것으로 이해해도 충분하다. 

만약 더 깊이있게 확인하고 싶으면 `IpPortConverter`에 디버그 브레이크 포인트를 걸어서 확인해보자.