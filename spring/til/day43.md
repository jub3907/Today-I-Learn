## 예외 계층
스프링이 제공하는 예외 추상화를 이해하기 위해서는 먼저 \
자바 기본 예외에 대한 이해가 필요하다.

예외는 자바 언어의 기본 문법에 들어가기 때문에 대부분 아는 내용일 것이다.\
예외의 기본 내용을 간단히 복습하고, \
실무에 필요한 체크 예외와 언체크 예외의 차이와 활용 방안에 대해서도 알아보자
<br/>
<br/>

### 예외 계층
* 예외 계층 그림\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/dcb3292c-3c47-4163-a3e7-1c9f7aff092c)

* `Object` : 예외도 객체이다. 모든 객체의 최상위 부모는 Object 이므로 예외의 최상위 부모도 Object 이다.
* `Throwable` : 최상위 예외이다. 하위에 Exception 과 Error 가 있다.
* `Error` : 메모리 부족이나 심각한 시스템 오류와 같이 \
  애플리케이션에서 복구 불가능한 시스템 예외이다. \
  애플리케이션 개발자는 이 예외를 잡으려고 해서는 안된다.
  * 상위 예외를 catch 로 잡으면 그 하위 예외까지 함께 잡는다. \
    따라서 애플리케이션 로직에서는 Throwable 예외도 잡으면 안되는데, \
    앞서 이야기한 Error 예외도 함께 잡을 수 있기 때문이다. \
    애플리케이션 로직은 이런 이유로 Exception 부터 필요한 예외로 생각하고 잡으면 된다.
  * 참고로 Error 도 언체크 예외이다.
* `Exception` : 체크 예외
  * 애플리케이션 로직에서 사용할 수 있는 실질적인 최상위 예외이다.
  * Exception 과 그 하위 예외는 모두 컴파일러가 체크하는 체크 예외이다. \
    단 RuntimeException 은 예외로 한다.
* `RuntimeException` : 언체크 예외, 런타임 예외
  * 컴파일러가 체크 하지 않는 언체크 예외이다.
  * RuntimeException 과 그 자식 예외는 모두 언체크 예외이다.
  * RuntimeException 의 이름을 따라서 RuntimeException 과 \
    그 하위 언체크 예외를 런타임 예외라고 많이 부른다. \
    여기서도 앞으로는 런타임 예외로 종종 부르겠다.
<br/>


## 예외 기본 규칙
예외는 폭탄 돌리기와 같다. 잡아서 처리하거나, 처리할 수 없으면 밖으로 던져야한다.


### 예외 처리
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/c69076ec-2229-48da-bee4-45af7122a626)

5번에서 예외를 처리하면 이후에는 애플리케이션 로직이 정상 흐름으로 동작한다.
<br/>
<br/>

### 예외 던짐
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/07c4d3e5-b62a-4758-95ae-3ccdcf4b8c4e)

예외를 처리하지 못하면 호출한 곳으로 예외를 계속 던지게 된다
<br/>
<br/>

### 예외에 대해서는 2가지 기본 규칙을 기억하자
* 1.예외는 잡아서 처리하거나 던져야 한다.
* 2.예외를 잡거나 던질 때 지정한 예외뿐만 아니라 그 예외의 자식들도 함께 처리된다.
  * 예를 들어서 Exception 을 catch 로 잡으면 그 하위 예외들도 모두 잡을 수 있다.
  * 예를 들어서 Exception 을 throws 로 던지면 그 하위 예외들도 모두 던질 수 있다.
<br/>
<br/>

### 참고: 예외를 처리하지 못하고 계속 던지면 어떻게 될까?
자바 main() 쓰레드의 경우 예외 로그를 출력하면서 시스템이 종료된다.\
웹 애플리케이션의 경우 여러 사용자의 요청을 처리하기 때문에 \
하나의 예외 때문에 시스템이 종료되면 안된다. \
WAS가 해당 예외를 받아서 처리하는데, 주로 사용자에게 개발자가 지정한 오류 페이지를 보여준다.\
다음으로 체크 예외와 언체크 예외의 차이에 대해서 코드로 알아보자.
<br/>
<br/>

## 체크 예외 기본 이해
Exception 과 그 하위 예외는 모두 컴파일러가 체크하는 체크 예외이다.\
단 RuntimeException 은 예외로 한다.

체크 예외는 잡아서 처리하거나, 또는 밖으로 던지도록 선언해야한다. \
그렇지 않으면 컴파일 오류가 발생한다.
<br/>
<br/>

### 체크 예외 전체 코드
```java

@Slf4j
public class CheckedTest {

    @Test
    void checked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void checked_throw() {
        Service service = new Service();
        Assertions.assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyCheckedException.class);

    }

    /**
     * Exception을 상속 받은 예외는 체크가 된다.
     */
    static class MyCheckedException extends Exception {
        public MyCheckedException(String message) {
            super(message);
        }
    }

    /**
     * Checked 예외는 예외를 잡아서 처리하거나,
     * 예외를 던져야만 한다.
     */
    static class Service {
        Repository repository = new Repository();

        /**
         * 체크 예외를 밖으로 던지는 코드
         * 체크 예외는 예외를 잡지 않고 밖으로 던진다
         * @throws MyCheckedException
         */
        public void callThrow() throws MyCheckedException {
            repository.call();
        }

        /**
         * 예외를 잡아 처리하는 코드
         */
        public void callCatch() {
            try {
                repository.call();
            } catch (MyCheckedException e) {
                // 예외 처리 로직
                log.info("예외 처리, message={}", e.getMessage(), e);
            }
        }
    }

    static class Repository {

        public void call() throws MyCheckedException {
            throw new MyCheckedException("ex");
        }
    }

}

```
<br/>
<br/>


### Exception을 상속받은 예외는 체크 예외가 된다
```java
static class MyCheckedException extends Exception {
    public MyCheckedException(String message) {
        super(message);
    }
}
```
* MyCheckedException 는 Exception 을 상속받았다. \
  Exception 을 상속받으면 체크 예외가 된다.
* 참고로 RuntimeException 을 상속받으면 언체크 예외가 된다. \
  이런 규칙은 자바 언어에서 문법으로 정한 것이다.
* 예외가 제공하는 여러가지 기본 기능이 있는데, \
  그 중에 오류 메시지를 보관하는 기능도 있다. 예제에서 보는 것 처럼 \
  생성자를 통해서 해당 기능을 그대로 사용하면 편리하다.\

먼저 예외를 잡아서 처리하는 코드를 실행해보자.
```java
@Test
void checked_catch() {
    Service service = new Service();
    service.callCatch();
}
```
* service.callCatch() 에서 예외를 처리했기 때문에\
  테스트 메서드까지 예외가 올라오지 않는다

### 실행 분석
실행 순서를 분석해보자.
1. `test` -> service.callCatch() -> repository.call() [예외 발생, 던짐]
2. `test` <- service.callCatch() [예외 처리] <- repository.call()
3. `test` [정상 흐름] <- service.callCatch() <- repository.call()

`Repository.call()` 에서 `MyUncheckedException` 예외가 발생하고, \
그 예외를 `Service.callCatch()` 에서 잡는 것을 확인할 수 있다.
```java
log.info("예외 처리, message={}", e.getMessage(), e);
```
<br/>
<br/>

### 실행 결과
```java
[Test worker] INFO hello.jdbc.exception.basic.CheckedTest - 예외 처리, message=ex
hello.jdbc.exception.basic.CheckedTest$MyCheckedException: ex
  at hello.jdbc.exception.basic.CheckedTest$Repository.call(CheckedTest.java:64)
  at hello.jdbc.exception.basic.CheckedTest$Service.callCatch(CheckedTest.java:45)
  at hello.jdbc.exception.basic.CheckedTest.checked_catch(CheckedTest.java:14)
```
실행 결과 로그를 보면 첫줄은 우리가 남긴 로그가 그대로 남는 것을 확인할 수 있다.\
그런데 두 번째 줄 부터 예외에 대한 스택 트레이스가 추가로 출력된다.\
이 부분은 로그를 남길 때 로그의 마지막 인수에 예외 객체를 전달해주면 \
로그가 해당 예외의 스택 트레이스를 추가로 출력해주는 것이다.

`log.info("예외 처리, message={}", e.getMessage(), e); `
<br/>
<br/>

### 체크 예외를 잡아서 처리하는 코드
```java
try {
    repository.call();
} catch (MyCheckedException e) {
    //예외 처리 로직
}
```
체크 예외를 잡아서 처리하려면 catch(..) 를 사용해서 예외를 잡으면 된다.\
여기서는 MyCheckedException 예외를 잡아서 처리한다.
<br/>
<br/>

### catch는 해당 타입과 그 하위 타입을 모두 잡을 수 있다
```java
public void callCatch() {
    try {
        repository.call();
    } catch (Exception e) {
        //예외 처리 로직
    }
}
```
catch 에 MyCheckedException 의 상위 타입인 Exception 을 적어주어도\
MyCheckedException 을 잡을 수 있다.

catch 에 예외를 지정하면 해당 예외와 그 하위 타입 예외를 모두 잡아준다.

물론 정확하게 MyCheckedException 만 잡고 싶다면 \
catch 에 MyCheckedException 을 적어주어야 한다.
<br/>
<br/>

이번에는 예외를 처리하지 않고, 밖으로 던지는 코드를 살펴보자.
```java
@Test
void checked_throw() {
    Service service = new Service();
    assertThatThrownBy(() -> service.callThrow())
            .isInstanceOf(MyCheckedException.class);
}
```
service.callThrow() 에서 예외를 처리하지 않고, \
밖으로 던졌기 때문에 예외가 테스트 메서드까지 올라온다.\
테스트에서는 기대한 것 처럼 MyCheckedException 예외가 던져지면 성공으로 처리한다.
<br/>
<br/>

### 실행 순서 분석
실행 순서를 분석해보자.
1. test -> service.callThrow() -> repository.call() [예외 발생, 던짐]
2. test <- service.callThrow() [예외 던짐] <- repository.call()
3. test <- [예외 도착] service.callThrow() <- repository.call()

### 체크 예외를 밖으로 던지는 코드
```java
public void callThrow() throws MyCheckedException {
    repository.call();
}
```
체크 예외를 처리할 수 없을 때는 method() throws 예외 을 사용해서 \
밖으로 던질 예외를 필수로 지정해주어야 한다. \
여기서는 MyCheckedException 을 밖으로 던지도록 지정해주었다.
<br/>
<br/>

### 체크 예외를 밖으로 던지지 않으면 컴파일 오류 발생
```java
public void callThrow() {
    repository.call();
}
```
throws 를 지정하지 않으면 컴파일 오류가 발생한다.\
* `Unhandled exception: hello.jdbc.exception.basic.CheckedTest.MyCheckedException`
체크 예외의 경우 예외를 잡아서 처리하거나 또는 throws 를 지정해서\
예외를 밖으로 던진다는 선언을 필수로 해주어야 한다.
<br/>
<br/>

**참고로 체크 예외를 밖으로 던지는 경우에도 해당 타입과 그 하위 타입을 모두 던질 수 있다**
```java
public void callThrow() throws Exception {
    repository.call();
}
```
throws 에 MyCheckedException 의 상위 타입인 Exception 을 적어주어도 \
MyCheckedException 을 던질 수 있다.\
throws 에 지정한 타입과 그 하위 타입 예외를 밖으로 던진다.\
물론 정확하게 MyCheckedException 만 밖으로 던지고 싶다면 throws 에\
MyCheckedException 을 적어주어야 한다.
<br/>
<br/>

### 체크 예외의 장단점
체크 예외는 예외를 잡아서 처리할 수 없을 때, \
예외를 밖으로 던지는 throws 예외 를 필수로 선언해야 한다. \
그렇지 않으면 컴파일 오류가 발생한다. 이것 때문에 장점과 단점이 동시에 존재한다.

* 장점: 개발자가 실수로 예외를 누락하지 않도록 컴파일러를 통해 문제를 잡아주는 훌륭한 안전 장치이다.
* 단점: 하지만 실제로는 개발자가 모든 체크 예외를 반드시 잡거나 \
  던지도록 처리해야 하기 때문에, 너무 번거로운 일이 된다. \
  크게 신경쓰고 싶지 않은 예외까지 모두 챙겨야 한다. \
  추가로 의존관계에 따른 단점도 있는데 이 부분은 뒤에서 설명하겠다.
<br/>

## 언체크 예외 기본 이해
RuntimeException 과 그 하위 예외는 언체크 예외로 분류된다.\
언체크 예외는 말 그대로 컴파일러가 예외를 체크하지 않는다는 뜻이다.\
언체크 예외는 체크 예외와 기본적으로 동일하다. \
차이가 있다면 예외를 던지는 throws 를 선언하지 않고, 생략할 수 있다. \
이 경우 자동으로 예외를 던진다.
<br/>
<br/>

### 체크 예외 VS 언체크 예외
체크 예외: 예외를 잡아서 처리하지 않으면 항상 throws 에 던지는 예외를 선언해야 한다.\
언체크 예외: 예외를 잡아서 처리하지 않아도 throws 를 생략할 수 있다.
<br/>
<br/>

### 언체크 예외 전체 코드
```java
@Slf4j
public class UncheckedTest {

    @Test
    void unchecked_catch() {
        Service service = new Service();
        service.callCatch();
    }
    
    
    @Test
    void unchecked_throw() {
        Service service = new Service();
        assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyUncheckedException.class);
    }
    
    
    /**
     * RuntimeException을 상속받은 예외는 언체크 예외가 된다.
     */
    static class MyUncheckedException extends RuntimeException {
        public MyUncheckedException(String message) {
            super(message);
        }
    }
    
    
    /**
     * UnChecked 예외는
     * 예외를 잡거나, 던지지 않아도 된다.
     * 예외를 잡지 않으면 자동으로 밖으로 던진다.
     */
    static class Service {
        Repository repository = new Repository();
        
        
        /**
         * 필요한 경우 예외를 잡아서 처리하면 된다.
         */
        public void callCatch() {
            try {
                repository.call();
            } catch (MyUncheckedException e) {
                //예외 처리 로직
                log.info("예외 처리, message={}", e.getMessage(), e);
            }
        }
        
        
        /**
         * 예외를 잡지 않아도 된다. 자연스럽게 상위로 넘어간다.
         * 체크 예외와 다르게 throws 예외 선언을 하지 않아도 된다.
         */
        public void callThrow() {
            repository.call();
        }
    }
    
    
    static class Repository {
        public void call() {
            throw new MyUncheckedException("ex");
        }
    }
}

```
<br/>
<br/>

### 언체크 예외를 잡아서 처리하는 코드
```java
try {
    repository.call();
} catch (MyUncheckedException e) {
    //예외 처리 로직
    log.info("error", e);
}
```
언체크 예외도 필요한 경우 이렇게 잡아서 처리할 수 있다
<br/>
<br/>

### 언체크 예외를 밖으로 던지는 코드 - 생략
```java
public void callThrow() {
    repository.call();
}
```
언체크 예외는 체크 예외와 다르게 throws 예외 를 선언하지 않아도 된다.\
말 그대로 컴파일러가 이런 부분을 체크하지 않기 때문에 언체크 예외이다.
<br/>
<br/>

### 언체크 예외를 밖으로 던지는 코드 - 선언
```java
public void callThrow() throws MyUncheckedException {
    repository.call();
}
```
참고로 언체크 예외도 throws 예외 를 선언해도 된다. 물론 생략할 수 있다.\
언체크 예외는 주로 생략하지만, 중요한 예외의 경우 이렇게 선언해두면\
해당 코드를 호출하는 개발자가 이 런 예외가 발생한다는 점을 IDE를 통해\
좀 더 편리하게 인지할 수 있다.\
(컴파일 시점에 막을 수 있는 것은 아니고, IDE를 통해서 인지할 수 있는 정도이다.)
<br/>
<br/>


### 언체크 예외의 장단점
언체크 예외는 예외를 잡아서 처리할 수 없을 때, \
예외를 밖으로 던지는 throws 예외 를 생략할 수 있다. \
이것 때문에 장점과 단점이 동시에 존재한다.

* 장점: 신경쓰고 싶지 않은 언체크 예외를 무시할 수 있다. \
  체크 예외의 경우 처리할 수 없는 예외를 밖으로 던지려면 \
  항상 throws 예외 를 선언해야 하지만, 언체크 예외는 \
  이 부분을 생략할 수 있다. 이후에 설명하겠지만, \
  신경쓰고 싶지 않은 예외의 의존관계를 참조하지 않아도 되는 장점이 있다.

* 단점: 언체크 예외는 개발자가 실수로 예외를 누락할 수 있다. \
  반면에 체크 예외는 컴파일러를 통해 예외 누락을 잡아준다.
<br/>

### 정리
체크 예외와 언체크 예외의 차이는 사실 \
예외를 처리할 수 없을 때 예외를 밖으로 던지는 부분에 있다. \
이 부분을 필수로 선언해야 하는가 생략할 수 있는가의 차이다
<br/>
<br/>

## 체크 예외 활용
그렇다면 언제 체크 예외를 사용하고 언제 언체크(런타임) 예외를 사용하면 좋을까?

**기본 원칙은 다음 2가지를 기억하자.**
* 기본적으로 언체크(런타임) 예외를 사용하자.
* 체크 예외는 비즈니스 로직상 의도적으로 던지는 예외에만 사용하자.
  * 이 경우 해당 예외를 잡아서 반드시 처리해야 하는 문제일 때만 체크 예외를 사용해야 한다. \
    예를 들어서 다음과 같은 경우가 있다.
  * 체크 예외 예)
    * 계좌 이체 실패 예외
    * 결제시 포인트 부족 예외
    * 로그인 ID, PW 불일치 예외
* 물론 이 경우에도 100% 체크 예외로 만들어야 하는 것은 아니다. \
  다만 계좌 이체 실패처럼 매우 심각한 문제는 개발자가 실수로 \
  예외를 놓치면 안된다고 판단할 수 있다. \
  이 경우 체크 예외로 만들어 두면 컴파일러를 통해 놓친 예외를 인지할 수 있다.
<br/>

### 체크 예외의 문제점
체크 예외는 컴파일러가 예외 누락을 체크해주기 때문에 개발자가 실수로 \
예외를 놓치는 것을 막아준다. 그래서 항상 명시적으로 예외를 잡아서 처리하거나, \
처리할 수 없을 때는 예외를 던지도록 method() throws 예외 로 선언해야 한다.

지금까지 이야기를 들어보면 체크 예외가 런타임 예외보다 더 안전하고 좋아보이는데, \
왜 체크 예외를 기본으로 사용하는 것이 문제가 될까?

그림과 코드로 문제점을 이해해보자.
<br/>
<br/>


### 체크 예외 문제점 - 그림
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/c15df81f-65b9-46a0-a6cc-e2a6ed99b242)
* 리포지토리는 DB에 접근해서 데이터를 저장하고 관리한다. 여기서는 SQLException 체크 예외를 던진다.
* NetworkClient 는 외부 네트워크에 접속해서 어떤 기능을 처리하는 객체이다. 여기서는 ConnectException 체크 예외를 던진다.
* 서비스는 리포지토리와 NetworkClient 를 둘다 호출한다.
  * 따라서 두 곳에서 올라오는 체크 예외인 SQLException 과 ConnectException 을 처리해야 한다.
  * 그런데 서비스는 이 둘을 처리할 방법을 모른다. ConnectException 처럼 연결이 실패하거나, SQLException 처럼 데이터베이스에서 발생하는 문제처럼 심각한 문제들은 대부분 애플리케이션 로직에서 처리할 방법이 없다.
* 서비스는 SQLException 과 ConnectException 를 처리할 수 없으므로 둘다 밖으로 던진다.
  * 체크 예외이기 때문에 던질 경우 다음과 같이 선언해야 한다.
  * method() throws SQLException, ConnectException
* 컨트롤러도 두 예외를 처리할 방법이 없다.
  * 다음을 선언해서 예외를 밖으로 던진다.
  * method() throws SQLException, ConnectException
* 웹 애플리케이션이라면 서블릿의 오류 페이지나, 또는 \
  스프링 MVC가 제공하는 ControllerAdvice 에서 이런 예외를 공통으로 처리한다.
  * 이런 문제들은 보통 사용자에게 어떤 문제가 발생했는지 자세히 설명하기가 어렵다. \
    그래서 사용자에게는 "서비스에 문제가 있습니다." 라는 일반적인 메시지를 보여준다. \
    ("데이터베이스에 어떤 오류가발생했어요" 라고 알려주어도 일반 사용자가 이해할 수 없다. \
    그리고 보안에도 문제가 될 수 있다.)
  * API라면 보통 HTTP 상태코드 500(내부 서버 오류)을 사용해서 응답을 내려준다.
  * 이렇게 해결이 불가능한 공통 예외는 별도의 오류 로그를 남기고, \
    개발자가 오류를 빨리 인지할 수 있도록 메일, 알림(문자, 슬랙)등을 통해서 \
    전달 받아야 한다. 예를 들어서 SQLException 이 잘못된 SQL을 작성해서 발생했다면, \
    개발자가 해당 SQL을 수정해서 배포하기 전까지 사용자는 같은 문제를 겪게 된다.
<br/>

### 체크 예외 문제점 - 코드 - CheckedAppTest
```java

public class CheckedAppTest {

    @Test
    void checked() {
        Controller controller = new Controller();
        assertThatThrownBy(() -> controller.request())
                .isInstanceOf(Exception.class);
    }

    static class Controller {
        Service service = new Service();

        public void request() throws SQLException, ConnectException {
            service.logic();
        }
    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() throws ConnectException, SQLException {
            repository.call();
            networkClient.call();
        }

    }

    static class NetworkClient {
        public void call() throws ConnectException {
            throw new ConnectException();
        }
    }

    static class Repository {
        public void call() throws SQLException {
            throw new SQLException();
        }
    }

}
```
* 서비스
  * 체크 예외를 처리하지 못해서 밖으로 던지기 위해 \
    `logic() throws SQLException, ConnectException` 를 선언했다.
* 컨트롤러
  * 체크 예외를 처리하지 못해서 밖으로 던지기 위해 \
    `request() throws SQLException, ConnectException` 를 선언했다.
<br/>

### 2가지 문제
지금까지 설명한 예시와 코드를 보면 크게 2가지 문제를 알 수 있다.
1. 복구 불가능한 예외
2. 의존 관계에 대한 문제
<br/>

### 1. 복구 불가능한 예외
대부분의 예외는 복구가 불가능하다. 일부 복구가 가능한 예외도 있지만 아주 적다.\
SQLException 을 예를 들면 데이터베이스에 무언가 문제가 있어서 발생하는 예외이다. \
SQL 문법에 문제가 있을 수도 있고, 데이터베이스 자체에 뭔가 문제가 발생했을 수도 있다. \
데이터베이스 서버가 중간에 다운되었을 수도 있다. \
이런 문제들은 대부분 복구가 불가능하다. 

특히나 대부분의 서비스나 컨트롤러는 이런 문제를 해결할 수는 없다. \
따라서 이런 문제들은 일관성 있게 공통으로 처리해야 한다. \
오류 로그를 남기고 개발자가 해당 오류를 빠르게 인지하는 것이 필요하다. \
서블릿 필터, 스프링 인터셉터, 스프링의 ControllerAdvice 를 사용하면 \
이런 부분을 깔끔하게 공통으로 해결할 수 있다.
<br/>
<br/>

### 2. 의존 관계에 대한 문제
체크 예외의 또 다른 심각한 문제는 예외에 대한 의존 관계 문제이다.\
앞서 대부분의 예외는 복구 불가능한 예외라고 했다. \
그런데 체크 예외이기 때문에 컨트롤러나 서비스 입장에서는 \
본인이 처리할 수 없어도 어쩔 수 없이 throws 를 통해 던지는 예외를 선언해야 한다.
<br/>
<br/>

### 체크 예외 throws 선언
```java
class Controller {
    public void request() throws SQLException, ConnectException {
        service.logic();
    }
}

class Service {
    public void logic() throws SQLException, ConnectException {
        repository.call();
        networkClient.call();
    }
}
```
throws SQLException, ConnectException 처럼 예외를 던지는 부분을 \
코드에 선언하는 것이 왜 문제가 될까?

**바로 서비스, 컨트롤러에서 java.sql.SQLException 을 의존하기 때문에** 문제가 된다.

향후 리포지토리를 JDBC 기술이 아닌 다른 기술로 변경한다면, \
그래서 SQLException 이 아니라 예를 들어서 \
JPAException 으로 예외가 변경된다면 어떻게 될까?

SQLException 에 의존하던 모든 서비스, 컨트롤러의 코드를\
JPAException 에 의존하도록 고쳐야 한다.\
서비스나 컨트롤러 입장에서는 어차피 본인이 처리할 수 도 없는 예외를 \
의존해야 하는 큰 단점이 발생하게된다.

결과적으로 OCP, DI를 통해 클라이언트 코드의 변경 없이 대상 \
구현체를 변경할 수 있다는 장점이 체크 예외 때문에 발목을 잡게 된다.
<br/>
<br/>


### 체크 예외 구현 기술 변경시 파급 효과
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/9efc3699-7c31-4da2-962f-722b17510222)
JDBC JPA 같은 기술로 변경하면 예외도 함께 변경해야한다. \
그리고 해당 예외를 던지는 모든 다음 부분도 함께 변경해야 한다.

`logic() throws SQLException logic() throws JPAException`\
(참고로 JPA 예외는 실제 이렇지는 않고, 이해하기 쉽게 예를 든 것이다.)
<br/>
<br/>

### 정리
처리할 수 있는 체크 예외라면 서비스나 컨트롤러에서 처리하겠지만, \
지금처럼 데이터베이스나 네트워크 통신처럼 시스템 레벨에서 올라온 예외들은 \
대부분 복구가 불가능하다. \
그리고 실무에서 발생하는 대부분의 예외들은 이런 시스템 예외들이다.

문제는 이런 경우에 체크 예외를 사용하면 아래에서 올라온 \
복구 불가능한 예외를 서비스, 컨트롤러 같은 각각의 클래스가 모두 알고 있어야 한다. \
그래서 불필요한 의존관계 문제가 발생하게 된다.
<br/>
<br/>

### throws Exception
SQLException , ConnectException 같은 시스템 예외는 \
컨트롤러나 서비스에서는 대부분 복구가 불가능하고 처리할 수 없는 체크 예외이다. \
따라서 다음과 같이 처리해주어야 한다.
```java
void method() throws SQLException, ConnectException {..}
```
그런데 다음과 같이 최상위 예외인 Exception 을 던져도 문제를 해결할 수 있다.
```java
void method() throws Exception {..}
```
이렇게 하면 Exception 은 물론이고 그 하위 타입인 SQLException,\
ConnectException 도 함께 던지게 된다. 코드가 깔끔해지는 것 같지만, \
Exception 은 최상위 타입이므로 모든 체크 예외를 다 밖으로 던지는 문제가 발생한다.

결과적으로 체크 예외의 최상위 타입인 Exception 을 던지게 되면 \
다른 체크 예외를 체크할 수 있는 기능이 무효화 되고, \
중요한 체크 예외를 다 놓치게 된다. 중간에 중요한 체크 예외가 발생해도 \
컴파일러는 Exception 을 던지기 때문에 문법에 맞다고 판단해서 \
컴파일 오류가 발생하지 않는다.

이렇게 하면 모든 예외를 다 던지기 때문에 체크 예외를 의도한 대로 \
사용하는 것이 아니다. 따라서 꼭 필요한 경우가 아니면 이렇게\
Exception 자체를 밖으로 던지는 것은 좋지 않은 방법이다.
<br/>
<br/>

## 언체크 예외 활용
이번에는 런타임 예외를 사용해보자.

### 런타임 예외 사용 - 그림
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/08182b42-d277-4215-926e-b66da5dd3e97)

SQLException 을 런타임 예외인 RuntimeSQLException 으로 변환했다.\
ConnectException 대신에 RuntimeConnectException 을 사용하도록 바꾸었다.\
런타임 예외이기 때문에 서비스, 컨트롤러는 해당 예외들을 처리할 수 없다면 \
별도의 선언 없이 그냥 두면 된다.
<br/>
<br/>

### 런타임 예외 사용 변환 - 코드 - UncheckedAppTest
```java

public class UncheckedAppTest {

    @Test
    void unchecked() {
        Controller controller = new Controller();
        assertThatThrownBy(() -> controller.request())
                .isInstanceOf(Exception.class);
    }

    static class Controller {
        Service service = new Service();

        public void request() {
            service.logic();
        }
    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() {
            repository.call();
            networkClient.call();
        }

    }

    static class NetworkClient {
        public void call() {
            throw new RuntimeConnectException("연결 실패");
        }
    }

    static class Repository {
        public void call()  {
            try {
                runSQL();
            } catch (SQLException e) {
                throw new RuntimeSQLException(e);
            }
        }

        public void runSQL () throws SQLException {
            throw new SQLException();
        }
    }

    static class RuntimeConnectException extends RuntimeException {
        public RuntimeConnectException(String message) {
            super(message);
        }
    }

    static class RuntimeSQLException extends RuntimeException {
        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }

        public RuntimeSQLException(String message) {
            super(message);
        }
    }

}
```
#### 예외 전환
* 리포지토리에서 체크 예외인 SQLException 이 발생하면 \
  런타임 예외인 RuntimeSQLException 으로 전환해서 예외를 던진다.
  * 참고로 이때 기존 예외를 포함해주어야 예외 출력시 스택 트레이스에서 \
    기존 예외도 함께 확인할 수 있다. 예외 포함에 대한 부분은 \
    조금 뒤에 더 자세히 설명한다.
* NetworkClient 는 단순히 기존 체크 예외를 \
  RuntimeConnectException 이라는 런타임 예외가 발생하도록 코드를 바꾸었다.
<br/>

#### 런타임 예외 - 대부분 복구 불가능한 예외
시스템에서 발생한 예외는 대부분 복구 불가능 예외이다. \
런타임 예외를 사용하면 서비스나 컨트롤러가 이런 복구 불가능한 예외를 \
신경쓰지 않아도 된다. 물론 이렇게 복구 불가능한 예외는 일관성 있게 공통으로 처리해야 한다.
<br/>
<br/>

#### 런타임 예외 - 의존 관계에 대한 문제
런타임 예외는 해당 객체가 처리할 수 없는 예외는 무시하면 된다. \
따라서 체크 예외 처럼 예외를 강제로 의존하지 않아도 된다.
<br/>
<br/>

### 런타임 예외 throws 생략
```java
static class Controller {
    Service service = new Service();

    public void request() {
        service.logic();
    }
}

static class Service {
    Repository repository = new Repository();
    NetworkClient networkClient = new NetworkClient();

    public void logic() {
        repository.call();
        networkClient.call();
    }
}
```
런타임 예외이기 때문에 컨트롤러나 서비스가 \
예외를 처리할 수 없다면 다음 부분을 생략할 수 있다.
* method() throws RuntimeSQLException, RuntimeConnectException
따라서 컨트롤러와 서비스에서 해당 예외에 대한 의존 관계가 발생하지 않는다.
<br/>
<br/>

### 런타임 예외 구현 기술 변경시 파급 효과
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/8df54923-3c5f-4300-b4f8-068a1000055a)
런타임 예외를 사용하면 중간에 기술이 변경되어도 \
해당 예외를 사용하지 않는 컨트롤러, 서비스에서는 코드를 변경하지 않아도 된다.

구현 기술이 변경되는 경우, 예외를 공통으로 처리하는 곳에서는 \
예외에 따른 다른 처리가 필요할 수 있다.\
하지만 공통 처리하는 한곳만 변경하면 되기 때문에 변경의 영향 범위는 최소화 된다.
<br/>
<br/>


### 정리
처음 자바를 설계할 당시에는 체크 예외가 더 나은 선택이라 생각했다. \
그래서 자바가 기본으로 제공하는 기능들에는 체크 예외가 많다. 

그런데 시간이 흐르면서 복구 할 수 없는 예외가 너무 많아졌다. \
특히 라이브러리를 점점 더 많이 사용하면서 처리해야 하는 예외도 더 늘어났다. \
체크 예외는 해당 라이브러리들이 제공하는 모든 예외를 처리할 수 없을 때마다 \
throws 에 예외를 덕지덕지 붙어야 했다. 

그래서 개발자들은 throws Exception 이라는 극단적?인 방법도 자주 사용하게 되었다. \
물론 이 방법은 사용하면 안된다. \
모든 예외를 던진다고 선언하는 것인데, \
결과적으로 어떤 예외를 잡고 어떤 예외를 던지는지 알 수 없기 때문이다. \
체크 예외를 사용한다면 잡을 건 잡고 던질 예외는 명확하게 던지도록 선언해야 한다.\
체크 예외의 이런 문제점 때문에 최근 라이브러리들은 대부분 런타임 예외를 기본으로 제공한다. 

사실 위에서 예시로 설명한 JPA 기술도 런타임 예외를 사용한다. \
스프링도 대부분 런타임 예외를 제공한다.\
런타임 예외도 필요하면 잡을 수 있기 때문에 필요한 경우에는 잡아서 처리하고, \
그렇지 않으면 자연스럽게던지도록 둔다. \
그리고 예외를 공통으로 처리하는 부분을 앞에 만들어서 처리하면 된다.\
추가로 런타임 예외는 놓칠 수 있기 때문에 문서화가 중요하다.
<br/>
<br/>

### 런타임 예외는 문서화
런타임 예외는 문서화를 잘해야 한다.\
또는 코드에 throws 런타임예외 을 남겨서 중요한 예외를 인지할 수 있게 해준다.
<br/>
<br/>


### JPA EntityManager
```java
/**
 * Make an instance managed and persistent.
 * @param entity entity instance
 * @throws EntityExistsException if the entity already exists.
 * @throws IllegalArgumentException if the instance is not an
 * entity
 * @throws TransactionRequiredException if there is no transaction when
 * invoked on a container-managed entity manager of that is of type
 * <code>PersistenceContextType.TRANSACTION</code>
 */
public void persist(Object entity);
```
예) 문서에 예외 명시
<br/>
<br/>


### 스프링 JdbcTemplate
```java
/**
 * Issue a single SQL execute, typically a DDL statement.
 * @param sql static SQL to execute
 * @throws DataAccessException if there is any problem
 */
void execute(String sql) throws DataAccessException;
```
* 예) method() throws DataAccessException 와 같이 문서화 + 코드에도 명시
* 런타임 예외도 throws 에 선언할 수 있다. 물론 생략해도 된다.
* 던지는 예외가 명확하고 중요하다면, \
  코드에 어떤 예외를 던지는지 명시되어 있기 때문에 \
  개발자가 IDE를 통해서 예외를 확인하가 편리하다.
* 물론 컨트롤러나 서비스에서 DataAccessException 을 사용하지 않는다면 \
  런타임 예외이기 때문에 무시해도 된다.
<br/>

## 예외 포함과 스택 트레이스
예외를 전환할 때는 꼭! 기존 예외를 포함해야 한다. \
그렇지 않으면 스택 트레이스를 확인할 때 심각한 문제가 발생한다.
```java
@Test
void printEx() {
    Controller controller = new Controller();
    try {
        controller.request();
    } catch (Exception e) {
//      e.printStackTrace();
        log.info("ex", e);
    }
}
```
* 로그를 출력할 때 마지막 파라미터에 예외를 넣어주면 로그에 스택 트레이스를 출력할 수 있다.
  * 예) log.info("message={}", "message", ex) , 여기에서 \
    마지막에 ex 를 전달하는 것을 확인할수 있다. \
    이렇게 하면 스택 트레이스에 로그를 출력할 수 있다.
  * 예) log.info("ex", ex) 지금 예에서는 파라미터가 없기 때문에, \
    예외만 파라미터에 전달하면 스택 트레이스를 로그에 출력할 수 있다.
* System.out 에 스택 트레이스를 출력하려면 e.printStackTrace() 를 사용하면 된다.
  * 실무에서는 항상 로그를 사용해야 한다는 점을 기억하자.
<br/>

### 기존 예외를 포함하는 경우
```java
public void call()  {
    try {
        runSQL();
    } catch (SQLException e) {
        throw new RuntimeSQLException(e);
    }
}
```

```java
13:10:45.626 [Test worker] INFO hello.jdbc.exception.basic.UncheckedAppTest - ex
hello.jdbc.exception.basic.UncheckedAppTest$RuntimeSQLException:java.sql.SQLException: ex
  at hello.jdbc.exception.basic.UncheckedAppTest$Repository.call(UncheckedAppTest.java:61)
  at hello.jdbc.exception.basic.UncheckedAppTest$Service.logic(UncheckedAppTest.java:45)
  at hello.jdbc.exception.basic.UncheckedAppTest$Controller.request(UncheckedAppTest.java:35)
  at hello.jdbc.exception.basic.UncheckedAppTest.printEx(UncheckedAppTest.java:24)
Caused by: java.sql.SQLException: ex
  at hello.jdbc.exception.basic.UncheckedAppTest$Repository.runSQL(UncheckedAppTest.java:66)
  at hello.jdbc.exception.basic.UncheckedAppTest$Repository.call(UncheckedAppTest.java:59)
```
예외를 포함해서 기존에 발생한 java.sql.SQLException 과 스택 트레이스를 확인할 수 있다.
<br/>
<br/>


### 기존 예외를 포함하지 않는 경우
```java
public void call() {
    try {
        runSQL();
    } catch (SQLException e) {
        throw new RuntimeSQLException(); //기존 예외(e) 제외
    }
}
```
```java
[Test worker] INFO hello.jdbc.exception.basic.UncheckedAppTest - ex
hello.jdbc.exception.basic.UncheckedAppTest$RuntimeSQLException: null
  at hello.jdbc.exception.basic.UncheckedAppTest$Repository.call(UncheckedAppTest.java:61)
  at hello.jdbc.exception.basic.UncheckedAppTest$Service.logic(UncheckedAppTest.java:45)
```
예외를 포함하지 않아서 기존에 발생한 java.sql.SQLException 과 스택 트레이스를 확인할 수 없다. \
변환한 RuntimeSQLException 부터 예외를 확인할 수 있다. 

만약 실제 DB에 연동했다면 DB에서 발생한 예외를 확인할 수 없는 심각한 문제가 발생한다.\
예외를 전환할 때는 꼭! 기존 예외를 포함하자
<br/>
<br/>

## 체크 예외와 인터페이스
서비스 계층은 가급적 특정 구현 기술에 의존하지 않고, 순수하게 유지하는 것이 좋다. \
이렇게 하려면 예외에 대한 의존도 함께 해결해야한다.

예를 들어서 서비스가 처리할 수 없는 SQLException 에 대한 의존을 제거하려면 어떻게 해야할까?\
서비스가 처리할 수 없으므로 리포지토리가 던지는 SQLException 체크 예외를 \
런타임 예외로 전환해서 서비스 계층에 던지자. \
이렇게 하면 서비스 계층이 해당 예외를 무시할 수 있기 때문에, \
특정 구현 기술에 의존하는 부분을 제거하고 서비스 계층을 순수하게 유지할 수 있다.

지금부터 코드로 이 방법을 적용해보자

### 인터페이스 도입
먼저 MemberRepository 인터페이스도 도입해서 구현 기술을 쉽게 변경할 수 있게 해보자.

![image](https://github.com/jub3907/outSourcing/assets/58246682/5c4ff34c-4327-49f4-9ee9-4d6091a36583)

이렇게 인터페이스를 도입하면 MemberService 는 MemberRepository 인터페이스에만 의존하면 된다.\
이제 구현 기술을 변경하고 싶으면 DI를 사용해서 MemberService 코드의 변경 없이 \
구현 기술을 변경할 수 있다.

### MemberRepository 인터페이스
```java
package hello.jdbc.repository;
import hello.jdbc.domain.Member;

public interface MemberRepository {
    Member save(Member member);
    Member findById(String memberId);
    void update(String memberId, int money);
    void delete(String memberId);
}
```
특정 기술에 종속되지 않는 순수한 인터페이스이다. \
이 인터페이스를 기반으로 특정 기술을 사용하는 구현체를 만들면 된다.

### 체크 예외와 인터페이스
잠깐? 기존에는 왜 이런 인터페이스를 만들지 않았을까? \
사실 다음과 같은 문제가 있기 때문에 만들지 않았다.

왜냐하면 SQLException 이 체크 예외이기 때문이다. \
여기서 체크 예외가 또 발목을 잡는다. \
체크 예외를 사용하려면 인터페이스에도 해당 체크 예외가 선언 되어 있어야 한다.\
예를 들면 다음과 같은 코드가 된다

### 체크 예외 코드에 인터페이스 도입시 문제점 - 인터페이스
```java
package hello.jdbc.repository;
import hello.jdbc.domain.Member;
import java.sql.SQLException;
public interface MemberRepositoryEx {
    Member save(Member member) throws SQLException;
    Member findById(String memberId) throws SQLException;
    void update(String memberId, int money) throws SQLException;
    void delete(String memberId) throws SQLException;
}
```
인터페이스의 메서드에 throws SQLException 이 있는 것을 확인할 수 있다.

### 체크 예외 코드에 인터페이스 도입시 문제점 - 구현 클래스
```java
@Slf4j
public class MemberRepositoryV3 implements MemberRepositoryEx {
    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values(?, ?)";
    }
}
```
인터페이스의 구현체가 체크 예외를 던지려면, \
인터페이스 메서드에 먼저 체크 예외를 던지는 부분이 선언되어 있어야 한다. \
그래야 구현 클래스의 메서드도 체크 예외를 던질 수 있다.\
쉽게 이야기 해서 MemberRepositoryV3 가 throws SQLException 를 하려면
MemberRepositoryEx 인터페이스에도 throws SQLException 이 필요하다.

참고로 구현 클래스의 메서드에 선언할 수 있는 예외는 부모 타입에서 던진 예외와 같거나 \
하위 타입이어야 한다.\
예를 들어서 인터페이스 메서드에 throws Exception 를 선언하면, \
구현 클래스 메서드에 throws SQLException 는 가능하다. \
SQLException 은 Exception 의 하위 타입이기 때문이다.

### 특정 기술에 종속되는 인터페이스
구현 기술을 쉽게 변경하기 위해서 인터페이스를 도입하더라도 \
SQLException 과 같은 특정 구현 기술에 종속적인 체크 예외를 \
사용하게 되면 인터페이스에도 해당 예외를 포함해야 한다. 

하지만 이것은 우리가 원 하던 순수한 인터페이스가 아니다. \
JDBC 기술에 종속적인 인터페이스일 뿐이다. \
인터페이스를 만드는 목 적은 구현체를 쉽게 변경하기 위함인데,\
이미 인터페이스가 특정 구현 기술에 오염이 되어 버렸다. \
향후 JDBC가 아닌 다른 기술로 변경한다면 인터페이스 자체를 변경해야 한다.

### 런타임 예외와 인터페이스
런타임 예외는 이런 부분에서 자유롭다. \
인터페이스에 런타임 예외를 따로 선언하지 않아도 된다. \
따라서 인터페이스가 특정 기술에 종속적일 필요가 없다.

## 런타임 예외 적용
실제 코드에 런타임 예외를 사용하도록 적용해보자.

### MemberRepository 인터페이스
```java
package hello.jdbc.repository;
import hello.jdbc.domain.Member;

public interface MemberRepository {
    Member save(Member member);
    Member findById(String memberId);
    void update(String memberId, int money);
    void delete(String memberId);
}
```

### MyDbException 런타임 예외
```java
public class MyDbException extends RuntimeException{
    public MyDbException() {
    }

    public MyDbException(String message) {
        super(message);
    }

    public MyDbException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDbException(Throwable cause) {
        super(cause);
    }
}
```
RuntimeException 을 상속받았다. \
따라서 MyDbException 은 런타임(언체크) 예외가 된다

### MemberRepositoryV4_1
```java
/**
 * 예외 누수 문제 해결
 * 체크 예외를 런타임 예외로 변경
 * MemberRepository 인터페이스 사용
 * throws SQLException 제거
 */
@Slf4j
public class MemberRepositoryV4_1 implements MemberRepository{

    private final DataSource dataSource;

    public MemberRepositoryV4_1(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            throw new MyDbException(e);
        } finally {
            close(conn, pstmt, null);
        }
    }

    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));

                return member;
            } else {
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }

        } catch (SQLException e) {
            throw new MyDbException(e);
        } finally {
            close(con, pstmt, rs);
        }

    }

    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            throw new MyDbException(e);
        } finally {
            close(con, pstmt, null);
        }
    }

    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new MyDbException(e);
        } finally {
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다
        DataSourceUtils.releaseConnection(con, dataSource);
    }

    private Connection getConnection() throws SQLException {
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
        Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
    }
}
```
MemberRepository 인터페이스를 구현한다.\
이 코드에서 핵심은 SQLException 이라는 체크 예외를 \
MyDbException 이라는 런타임 예외로 변환해서 던지는 부분이다.

### 예외 변환
```java
catch (SQLException e) {
    throw new MyDbException(e);
}
```
보면 기존 예외를 생성자를 통해서 포함하고 있는 것을 확인할 수 있다. \
예외는 원인이 되는 예외를 내부 에 포함할 수 있는데, 꼭 이렇게 작성해야 한다. \
그래야 예외를 출력했을 때 원인이 되는 기존 예외도 함께 확인할 수 있다.

MyDbException 이 내부에 SQLException 을 포함하고 있다고 이해하면 된다.\
예외를 출력했을 때 스택 트레이스를 통해 둘다 확인할 수 있다

**다음과 같이 기존 예외를 무시하고 작성하면 절대 안된다!**

### 예외 변환 - 기존 예외 무시
```java
catch (SQLException e) {
    throw new MyDbException();
}
```
잘 보면 new MyDbException() 으로 해당 예외만 생성하고 \
기존에 있는 SQLException 은 포함하지 않고 무시한다.

따라서 MyDbException 은 내부에 원인이 되는 다른 예외를 포함하지 않는다.\
이렇게 원인이 되는 예외를 내부에 포함하지 않으면, \
예외를 스택 트레이스를 통해 출력했을 때 기존에 원인 이 되는 부분을 확인할 수 없다.

만약 SQLException 에서 문법 오류가 발생했다면 그 부분을 확인할 방법이 없게 된다.

> 예외를 변환할 때는 기존 예외를 꼭! 포함하자. \
> 장애가 발생하고 로그에서 진짜 원인이 남지 않는 심각한 문제가 발생할 수 있다. \
> 중요한 내용이어서 한번 더 설명했다. 

이번에는 서비스가 인터페이스를 사용하도록 하자. 기존 코드를 유지하기 위해 \
MemberServiceV3_3 를 복사해서 수정하자.

### MemberServiceV4
```java
/**
 * 예외 누수 문제 해결
 * SQLException 제ㅐ거
 *
 * MemberRepository 인터페이스 의존
 */
@Slf4j
public class MemberServiceV4 {

    private final MemberRepository memberRepository;

    public MemberServiceV4(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void accountTransfer(String fromId, String toId, int money) {
        bizLogic(fromId, toId, money);
    }

    private void bizLogic(String fromId, String toId, int money) {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }

    private static void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
```
MemberRepository 인터페이스에 의존하도록 코드를 변경했다.\
MemberServiceV3_3 와 비교해서 보면 드디어 메서드에서 throws SQLException 부분이 \
제거된 것을 확인할 수 있다.\
드디어 순수한 서비스를 완성했다.

### MemberServiceV4Test
```java
/**
 * 예외 누수 문제 해결
 * SQLException 제거
 *
 * MemberRepository 인터페이스 의존
 */
@Slf4j
@SpringBootTest
class MemberServiceV4Test {

    public static final String Member_A = "memberA";
    public static final String Member_B = "memberB";
    public static final String Member_EX = "ex";

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberServiceV4 memberService;

    @TestConfiguration

    static class TestConfig {

        private final DataSource dataSource;

        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        MemberRepository memberRepository() {
            return new MemberRepositoryV4_1(dataSource);
        }

        @Bean
        MemberServiceV4 memberServiceV4() {
            return new MemberServiceV4(memberRepository());
        }
    }

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(Member_A);
        memberRepository.delete(Member_B);
        memberRepository.delete(Member_EX);
    }

    @Test
    void AopCheck() {
        log.info("memberService class={}", memberService.getClass());
        log.info("memberRepository class={}", memberRepository.getClass());
        Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue();
        Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {

        //given
        Member memberA = new Member(Member_A, 10000);
        Member memberB = new Member(Member_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        //when
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);


        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx() throws SQLException {

        //given
        Member memberA = new Member(Member_A, 10000);
        Member memberEx = new Member(Member_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        //when
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        //then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberEx = memberRepository.findById(memberEx.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberEx.getMoney()).isEqualTo(10000);
    }

}
```
MemberRepository 인터페이스를 사용하도록 했다.
테스트가 모두 정상 동작하는 것을 확인할 수 있다.

### 정리
체크 예외를 런타임 예외로 변환하면서 인터페이스와 \
서비스 계층의 순수성을 유지할 수 있게 되었다.\
덕분에 향후 JDBC에서 다른 구현 기술로 변경하더라도 \
서비스 계층의 코드를 변경하지 않고 유지할 수 있다.

### 남은 문제
리포지토리에서 넘어오는 특정한 예외의 경우 복구를 시도할 수도 있다. \
그런데 지금 방식은 항상 MyDbException 이라는 예외만 넘어오기 때문에 \
예외를 구분할 수 없는 단점이 있다. 

만약 특정 상황에는 예외를 잡아서 복구하고 싶으면 예외를 어떻게 구분해서 처리할 수 있을까?


## 데이터 접근 예외 직접 만들기
데이터베이스 오류에 따라서 특정 예외는 복구하고 싶을 수 있다.\
예를 들어서 회원 가입시 DB에 이미 같은 ID가 있으면 ID 뒤에 \
숫자를 붙여서 새로운 ID를 만들어야 한다고 가정해보자.

ID를 hello 라고 가입 시도 했는데, 이미 같은 아이디가 있으면 \
hello12345 와 같이 뒤에 임의의 숫자를 붙여서 가입하는 것이다.

데이터를 DB에 저장할 때 같은 ID가 이미 데이터베이스에 저장되어 있다면,\
데이터베이스는 오류 코드를 반환하고, 이 오류 코드를 받은 JDBC 드라이버는\
`SQLException` 을 던진다. 그리고 `SQLException` 에는 \
데이터베이스가 제공하는 `errorCode` 라는 것이 들어있다.

* 데이터베이스 오류 코드 그림
  ![image](https://github.com/jub3907/outSourcing/assets/58246682/35b7daea-916e-44f8-932c-58906dab3c86)

* H2 데이터베이스의 키 중복 오류 코드
  ```
  e.getErrorCode() == 23505
  ```
  SQLException 내부에 들어있는 errorCode 를 활용하면 데이터베이스에서 \
  어떤 문제가 발생했는지 확인할 수 있다.

#### H2 데이터베이스 예
`23505` : 키 중복 오류
`42000` : SQL 문법 오류
참고로 같은 오류여도 각각의 데이터베이스마다 정의된 오류 코드가 다르다. \
따라서 오류 코드를 사용할 때는 데이터베이스 메뉴얼을 확인해야 한다.

> [참고: H2 데이터베이스 오류 코드](https://www.h2database.com/javadoc/org/h2/api/ErrorCode.html)

서비스 계층에서는 예외 복구를 위해 키 중복 오류를 확인할 수 있어야 한다. \
그래야 새로운 ID를 만들어서 다시 저장을 시도할 수 있기 때문이다.\
이러한 과정이 바로 예외를 확인해서 복구하는 과정이다. 

리포지토리 는 SQLException 을 서비스 계층에 던지고 \
서비스 계층은 이 예외의 오류 코드를 확인해서 키 중복 오류( 23505 )인 경우\
새로운 ID를 만들어서 다시 저장하면 된다.

그런데 SQLException 에 들어있는 오류 코드를 활용하기 위해\
SQLException 을 서비스 계층으로 던지게 되면,\
서비스 계층이 SQLException 이라는 JDBC 기술에 의존하게 되면서, \
지금까지 우리가 고민했던 서비스 계층의 순수성이 무너진다.

이 문제를 해결하려면 앞서 배운 것 처럼 리포지토리에서 예외를 변환해서 던지면 된다.\
`SQLException` -> `MyDuplicateKeyException`\
먼저 필요한 예외를 만들어보자.

### MyDuplicateKeyException
```java
public class MyDuplicateKeyException extends MyDbException{
    public MyDuplicateKeyException() {
    }

    public MyDuplicateKeyException(String message) {
        super(message);
    }

    public MyDuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDuplicateKeyException(Throwable cause) {
        super(cause);
    }
}
```
기존에 사용했던 MyDbException 을 상속받아서 의미있는 계층을 형성한다. \
이렇게하면 데이터베이스 관련 예외라는 계층을 만들 수 있다.

그리고 이름도 MyDuplicateKeyException 이라는 이름을 지었다. \
이 예외는 데이터 중복의 경우에만 던져야 한다.

이 예외는 우리가 직접 만든 것이기 때문에, JDBC나 JPA 같은 특정 기술에 종속적이지 않다. \
따라서 이 예외를 사용하더라도 서비스 계층의 순수성을 유지할 수 있다. \
(향후 JDBC에서 다른 기술로 바꾸어도 이 예외는 그대로 유지할 수 있다.)

실제 예제 코드를 만들어서 확인해보자.

### ExTranslatorV1Test
```java

@Slf4j
public class ExTranslatorV1Test {

    Repository repository;
    Service service;

    @BeforeEach
    void init() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        repository = new Repository(dataSource);
        service = new Service(repository);
    }

    @Test
    void duplicateKeySave() {
        service.create("myId");
        service.create("myId");
    }

    @Slf4j
    @RequiredArgsConstructor
    static class Service {
        private final Repository repository;

        public void create(String memberId) {
            try {
                repository.save(new Member(memberId, 0));
                log.info("saveId={}", memberId);
            } catch (MyDuplicateKeyException e) {
                log.info("키 중복, 복구 시도");
                String retryId = generateNewId(memberId);
                log.info("retryId={}", retryId);
                repository.save(new Member(retryId, 0));
            } catch (MyDbException e) {
                log.info("데이터 접근 계층 예외", e);
                throw e;
            }
        }

        private String generateNewId(String memberId) {
            return memberId + new Random().nextInt(10000);
        }
    }


    @RequiredArgsConstructor
    static class Repository {
        private final DataSource dataSource;

        public Member save(Member member) {

            String sql = "insert into member(member_id, money) values(?, ?)";
            Connection con = null;
            PreparedStatement pstmt = null;

            try {
                con = dataSource.getConnection();
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, member.getMemberId());
                pstmt.setInt(2, member.getMoney());
                pstmt.executeUpdate();
                return member;
            } catch (SQLException e) {
                //h2 db
                if (e.getErrorCode() == 23505) {
                    throw new MyDuplicateKeyException(e);
                }
                throw new MyDbException(e);
            } finally {
                closeStatement(pstmt);
                closeConnection(con);
            }
        }
    }

}
```
실행해보면 다음과 같은 로그를 확인할 수 있다
```
Service - saveId=myId
Service - 키 중복, 복구 시도
Service - retryId=myId492
```
같은 ID를 저장했지만, 중간에 예외를 잡아서 복구한 것을 확인할 수 있다.

#### 리포지토리
리포지토리 부터 중요한 부분을 살펴보자.
```java
} catch (SQLException e) {
    //h2 db
    if (e.getErrorCode() == 23505) {
        throw new MyDuplicateKeyException(e);
    }
    throw new MyDbException(e);
}
```
* `e.getErrorCode() == 23505` : 오류 코드가 키 중복 오류( 23505 )인 경우 \
  MyDuplicateKeyException 을 새로 만들어서 서비스 계층에 던진다.
* 나머지 경우 기존에 만들었던 MyDbException 을 던진다

### 서비스
서비스의 중요한 부분을 살펴보자
```java
try {
    repository.save(new Member(memberId, 0));
    log.info("saveId={}", memberId);
} catch (MyDuplicateKeyException e) {
    log.info("키 중복, 복구 시도");
    String retryId = generateNewId(memberId);
    log.info("retryId={}", retryId);
    repository.save(new Member(retryId, 0));
} catch (MyDbException e) {
    log.info("데이터 접근 계층 예외", e);
    throw e;
}
```
처음에 저장을 시도한다. \
만약 리포지토리에서 MyDuplicateKeyException 예외가 올라오면 이 예외를 잡는다.

예외를 잡아서 generateNewId(memberId) 로 새로운 ID 생성을 시도한다. \
그리고 다시 저장한다. 여기가 예외를 복구하는 부분이다.

만약 복구할 수 없는 예외( MyDbException )면 로그만 남기고 다시 예외를 던진다.\
참고로 이 경우 여기서 예외 로그를 남기지 않아도 된다. \
어차피 복구할 수 없는 예외는 예외를 공통으로 처리하는 부분까지 전달되기 때문이다. \
따라서 이렇게 복구 할 수 없는 예외는 공통으로 예외를 처리하는 곳에서 \
예외 로그를 남기는 것이 좋다. 

여기서는 다양하게 예외를 잡아서 처리할 수 있는 점을 보여주기 위해 이곳에 코드를 만들어두었다.


### 정리
SQL ErrorCode로 데이터베이스에 어떤 오류가 있는지 확인할 수 있었다.\
예외 변환을 통해 SQLException 을 특정 기술에 의존하지 않는 \
직접 만든 예외인 MyDuplicateKeyException 로 변환 할 수 있었다.

리포지토리 계층이 예외를 변환해준 덕분에 서비스 계층은 특정 기술에 의존하지 않는 \
MyDuplicateKeyException 을 사용해서 문제를 복구하고, \
서비스 계층의 순수성도 유지할 수 있었다.

### 남은 문제
SQL ErrorCode는 각각의 데이터베이스 마다 다르다. \
결과적으로 데이터베이스가 변경될 때 마다 ErrorCode도 모두 변경해야 한다.
* 예) 키 중복 오류 코드
  * H2: 23505
  * MySQL: 1062
데이터베이스가 전달하는 오류는 키 중복 뿐만 아니라 락이 걸린 경우, \
SQL 문법에 오류 있는 경우 등등 수 십 수백가지 오류 코드가 있다. \
이 모든 상황에 맞는 예외를 지금처럼 다 만들어야 할까? \
추가로 앞서 이야기한 것 처럼 데이터베이스마다 이 오류 코드는 모두 다르다.

## 스프링 예외 추상화 이해
스프링은 앞서 설명한 문제들을 해결하기 위해 데이터 접근과 관련된 예외를 추상화해서 제공한다.

### 스프링 데이터 접근 예외 계층
![image](https://github.com/jub3907/outSourcing/assets/58246682/2d642c91-19fc-40b4-93f3-0cc1a1615187)

스프링은 데이터 접근 계층에 대한 수십 가지 예외를 정리해서 일관된 예외 계층을 제공한다.

각각의 예외는 특정 기술에 종속적이지 않게 설계되어 있다. \
따라서 서비스 계층에서도 스프링이 제공하는예외를 사용하면 된다. \
예를 들어서 JDBC 기술을 사용하든, JPA 기술을 사용하든\
스프링이 제공하는 예외를 사용하면 된다.

JDBC나 JPA를 사용할 때 발생하는 예외를 스프링이 제공하는 예외로 변환해주는 역할도 \
스프링이 제공한다.

참고로 그림을 단순화 하기 위해 일부 계층을 생략했다.

예외의 최고 상위는 org.springframework.dao.DataAccessException 이다. \
그림에서 보는 것 처럼 런타임 예외를 상속 받았기 때문에 \
스프링이 제공하는 데이터 접근 계층의 모든 예외는 런타임 예외이다.

DataAccessException 은 크게 2가지로 구분하는데 NonTransient 예외와 Transient 예외이다.\
Transient 는 일시적이라는 뜻이다.\
Transient 하위 예외는 동일한 SQL을 다시 시도했을 때 성공할 가능성이 있다.
예를 들어서 쿼리 타임아웃, 락과 관련된 오류들이다. \
이런 오류들은 데이터베이스 상태가 좋아지거나, 락이 풀렸을 때 다시 시도하면 성공할 수 도 있다.

NonTransient 는 일시적이지 않다는 뜻이다. \
같은 SQL을 그대로 반복해서 실행하면 실패한다.\
SQL 문법 오류, 데이터베이스 제약조건 위배 등이 있다.

> 참고: 스프링 메뉴얼에 모든 예외가 정리되어 있지는 않기 때문에 \
> 코드를 직접 열어서 확인해보는 것이 필요하다.


### 스프링이 제공하는 예외 변환기
스프링은 데이터베이스에서 발생하는 오류 코드를 스프링이 정의한 예외로\
자동으로 변환해주는 변환기를 제공한다.
코드를 통해 스프링이 제공하는 예외 변환기를 알아보자. \
먼저 에러 코드를 확인하는 부분을 간단히 복습해보자.

### SpringExceptionTranslatorTest
```java
@Slf4j
public class SpringExceptionTranslatorTest {

    DataSource dataSource;

    @BeforeEach
    void init() {
        dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    }

    @Test
    void sqlExceptionErrorCode() {
        String sql = "select bad grammar";

        try {
            Connection con = dataSource.getConnection();
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.executeQuery();
        } catch (SQLException e) {
            assertThat(e.getErrorCode()).isEqualTo(42122);
            int errorCode = e.getErrorCode();
            log.info("errorCode={}", errorCode);
            //org.h2.jdbc.JdbcSQLSyntaxErrorException
            log.info("error", e);
        }
    }
}
```
이전에 살펴봤던 SQL ErrorCode를 직접 확인하는 방법이다. \
이렇게 직접 예외를 확인하고 하나하나 스프링이 만들어준 예외로\
변환하는 것은 현실성이 없다. 이렇게 하려면 해당 오류 코드를 확인하고 \
스프링의 예외 체계에 맞추어 예외를 직접 변환해야 할 것이다. \
그리고 데이터베이스마다 오류 코드가 다르다는 점도 해결해야 한다.

그래서 스프링은 예외 변환기를 제공한다.

### SpringExceptionTranslatorTest - 추가 exceptionTranslator
```java
@Test
void exceptionTranslator() {
    String sql = "select bad grammar";
    try {
        Connection con = dataSource.getConnection();
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.executeQuery();
    } catch (SQLException e) {
        assertThat(e.getErrorCode()).isEqualTo(42122);
        //org.springframework.jdbc.support.sql-error-codes.xml
        SQLExceptionTranslator exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
        //org.springframework.jdbc.BadSqlGrammarException
        DataAccessException resultEx = exTranslator.translate("select", sql, e);
        log.info("resultEx", resultEx);

        assertThat(resultEx.getClass()).isEqualTo(BadSqlGrammarException.class);
    }
}
```

스프링이 제공하는 SQL 예외 변환기는 다음과 같이 사용하면 된다.
```java
SQLExceptionTranslator exTranslator = new
SQLErrorCodeSQLExceptionTranslator(dataSource);
DataAccessException resultEx = exTranslator.translate("select", sql, e);
```
* translate() 메서드의 첫번째 파라미터는 읽을 수 있는 설명이고, \
  두번째는 실행한 sql, 마지막은 발생된 SQLException 을 전달하면 된다. \
  이렇게 하면 적절한 스프링 데이터 접근 계층의 예외로 변환해서 반환해준다.

* 예제에서는 SQL 문법이 잘못되었으므로 \
  BadSqlGrammarException 을 반환하는 것을 확인할 수 있다.
  * 눈에 보이는 반환 타입은 최상위 타입인 DataAccessException 이지만 실제로는\
    BadSqlGrammarException 예외가 반환된다. 마지막에 assertThat() 부분을 확인하자. 
  * 참고로 BadSqlGrammarException 은 \
    최상위 타입인 DataAccessException 를 상속 받아서 만들어진다.

각각의 DB마다 SQL ErrorCode는 다르다. 그런데 스프링은 어떻게 \
각각의 DB가 제공하는 SQL ErrorCode까지 고려해서 예외를 변환할 수 있을까?

비밀은 바로 다음 파일에 있다.

### sql-error-codes.xml
```java
<bean id="H2" class="org.springframework.jdbc.support.SQLErrorCodes">
    <property name="badSqlGrammarCodes">
        <value>42000,42001,42101,42102,42111,42112,42121,42122,42132</value>
    </property>
    <property name="duplicateKeyCodes">
        <value>23001,23505</value>
    </property>
</bean>
<bean id="MySQL" class="org.springframework.jdbc.support.SQLErrorCodes">
    <property name="badSqlGrammarCodes">
        <value>1054,1064,1146</value>
    </property>
    <property name="duplicateKeyCodes">
        <value>1062</value>
    </property>
</bean>
```
* `org.springframework.jdbc.support.sql-error-codes.xml`
* 스프링 SQL 예외 변환기는 SQL ErrorCode를 이 파일에 대입해서\
  어떤 스프링 데이터 접근 예외로 전환해야 할지 찾아낸다. \
  예를 들어서 H2 데이터베이스에서 42000 이 발생하면 \
  badSqlGrammarCodes 이기 때문에 BadSqlGrammarException 을 반환한다.

해당 파일을 확인해보면 10개 이상의 우리가 사용하는 \
대부분의 관계형 데이터베이스를 지원하는 것을 확인할 수 있다.

### 정리
스프링은 데이터 접근 계층에 대한 일관된 예외 추상화를 제공한다.\
스프링은 예외 변환기를 통해서 SQLException 의 ErrorCode 에 맞는 \
적절한 스프링 데이터 접근 예외로 변환해준다.

만약 서비스, 컨트롤러 계층에서 예외 처리가 필요하면 \
특정 기술에 종속적인 SQLException 같은 예외를 직접 사용하는 것이 아니라,\
스프링이 제공하는 데이터 접근 예외를 사용하면 된다.

스프링 예외 추상화 덕분에 특정 기술에 종속적이지 않게 되었다. \
이제 JDBC에서 JPA같은 기술로 변경되어도 예외로 인한 변경을 최소화 할 수 있다. \
향후 JDBC에서 JPA로 구현 기술을 변경하더라도, \
스프링은 JPA 예외를 적절한 스프링 데이터 접근 예외로 변환해준다.

물론 스프링이 제공하는 예외를 사용하기 때문에 스프링에 대한 기술 종속성은 발생한다.\
스프링에 대한 기술 종속성까지 완전히 제거하려면\
예외를 모두 직접 정의하고 예외 변환도 직접 하면 되지만, 실용적인 방법은 아니다

## 스프링 예외 추상화 적용
이제 우리가 만든 애플리케이션에 스프링이 제공하는 데이터 접근 예외 추상화와 \
SQL 예외 변환기를 적용 해보자

### MemberRepositoryV4_2
```java

/**
 * SQLExceptionTranslator 추가
 */
@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository{

    private final DataSource dataSource;
    private final SQLExceptionTranslator exTranslator;

    public MemberRepositoryV4_2(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    }

    @Override
    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            throw exTranslator.translate("save", sql, e);
        } finally {
            close(conn, pstmt, null);
        }
    }

    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));

                return member;
            } else {
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }

        } catch (SQLException e) {
            throw exTranslator.translate("findById", sql, e);
        } finally {
            close(con, pstmt, rs);
        }

    }

    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            throw exTranslator.translate("update", sql, e);
        } finally {
            close(con, pstmt, null);
        }
    }

    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw exTranslator.translate("delete", sql, e);
        } finally {
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다
        DataSourceUtils.releaseConnection(con, dataSource);
    }

    private Connection getConnection() throws SQLException {
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
        Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
    }
}
```

기존 코드에서 스프링 예외 변환기를 사용하도록 변경되었다.

```java
} catch (SQLException e) {
    throw exTranslator.translate("save", sql, e);
}
```

### MemberServiceV4Test - 수정
```java
@Bean
MemberRepository memberRepository() {
    //return new MemberRepositoryV4_1(dataSource); //단순 예외 변환
    return new MemberRepositoryV4_2(dataSource); //스프링 예외 변환
}
```
MemberRepository 인터페이스가 제공되므로 스프링 빈에 등록할 빈만 \
MemberRepositoryV4_1 에서 MemberRepositoryV4_2 로 교체하면 \
리포지토리를 변경해서 테스트를 확인할 수 있다.


### 정리
드디어 예외에 대한 부분을 깔끔하게 정리했다.\
스프링이 예외를 추상화해준 덕분에, 서비스 계층은 \
특정 리포지토리의 구현 기술과 예외에 종속적이지 않게 되었다. \
따라서 서비스 계층은 특정 구현 기술이 변경되어도 그대로 유지할 수 있게 되었다. \
다시 DI를 제 대로 활용할 수 있게 된 것이다.

추가로 서비스 계층에서 예외를 잡아서 복구해야 하는 경우, \
예외가 스프링이 제공하는 데이터 접근 예외로 변경되어서 \
서비스 계층에 넘어오기 때문에 필요한 경우 예외를 잡아서 복구하면 된다.


## JDBC 반복 문제 해결 - JdbcTemplate
지금까지 서비스 계층의 순수함을 유지하기 위해 수 많은 노력을 했고,\
덕분에 서비스 계층의 순수함을 유지하게 되었다. \
이번에는 리포지토리에서 JDBC를 사용하기 때문에 발생하는 반복 문제를 해결해보자

### JDBC 반복 문제
* 커넥션 조회, 커넥션 동기화
* `PreparedStatement` 생성 및 파라미터 바인딩
* 쿼리 실행
* 결과 바인딩
* 예외 발생시 스프링 예외 변환기 실행
* 리소스 종료
리포지토리의 각각의 메서드를 살펴보면 상당히 많은 부분이 반복된다. \
이런 반복을 효과적으로 처리하는 방법이 바로 템플릿 콜백 패턴이다.

스프링은 JDBC의 반복 문제를 해결하기 위해 JdbcTemplate 이라는 템플릿을 제공한다.\
JdbcTemplate 에 대한 자세한 사용법은 뒤에서 설명하겠다. \
지금은 전체 구조와, 이 기능을 사용해서 \
반복코드를 제거할 수 있다는 것에 초점을 맞추자

### MemberRepositoryV5
```java
/**
 * JdbcTemplate 사용
 */
@Slf4j
public class MemberRepositoryV5 implements MemberRepository{

    private final JdbcTemplate template;


    public MemberRepositoryV5(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values (?, ?)";
        template.update(sql, member.getMemberId(), member.getMoney());

        return member;
    }

    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";
        return template.queryForObject(sql, memberRowMapper(), memberId);
    }

    private RowMapper<Member> memberRowMapper() {
        return (rs, rowNum) -> {
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        };
    }

    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?";
        template.update(sql, money, memberId);
    }

    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id=?";
        template.update(sql, memberId);
    }
}

```

### MemberServiceV4Test - 수정
```java
@Bean
MemberRepository memberRepository() {
    //return new MemberRepositoryV4_1(dataSource); //단순 예외 변환
    //return new MemberRepositoryV4_2(dataSource); //스프링 예외 변환
    return new MemberRepositoryV5(dataSource); //JdbcTemplate
}
```
`MemberRepository` 인터페이스가 제공되므로 등록하는 빈만\
`MemberRepositoryV5` 로 변경해서 등록하면 된다.

JdbcTemplate 은 JDBC로 개발할 때 발생하는 반복을 대부분 해결해준다. \
그 뿐만 아니라 지금까지 학습했던, **트랜잭션을 위한 커넥션 동기화**는 물론이고, \
예외 발생시 **스프링 예외 변환기**도 자동으로 실행해준다.

> 템플릿 콜백 패턴에 대해서 지금은 자세히 이해하지 못해도 괜찮다. \
> 스프링이 JdbcTemplate 이라는 편리한 기능을 제공하는 구나 정도로 이해해도 된다.


### 정리
완성된 코드를 확인해보자.

* 서비스 계층의 순수성
  * 트랜잭션 추상화 + 트랜잭션 AOP 덕분에 서비스 계층의 순수성을 최대한 유지하면서 \
    서비스 계층에서 트랜잭션을 사용할 수 있다.
  * 스프링이 제공하는 예외 추상화와 예외 변환기 덕분에, \
    데이터 접근 기술이 변경되어도 서비스 계층의 순수성을 유지하면서 \
    예외도 사용할 수 있다.
  * 서비스 계층이 리포지토리 인터페이스에 의존한 덕분에 \
    향후 리포지토리가 다른 구현 기술로 변경되어도 서비스 계층을 순수하게 유지할 수 있다.
* 리포지토리에서 JDBC를 사용하는 반복 코드가 JdbcTemplate 으로 대부분 제거되었다

