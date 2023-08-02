### BeanFactory와 ApplicationContext

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/bd5086db-1e1c-41b4-b773-0d6c256f34ac)

#### BeanFactory
* 스프링 컨테이너의 최상위 인터페이스.
* 스프링 빈을 관리하고 조회하는 역할을 담당한다.
* `getBean()`을 제공한다.

#### ApplicationContext
* BeanFactory 기능을 모두 상속받아서 제공한다.
* 빈을 관리하고 검색하는 기능을 BeanFactory가 제공해주는데, 그러면 둘의 차이가 뭘까?
* 애플리케이션을 개발할 때는 빈은 관리하고 조회하는 기능은 물론이고, 수 많은 부가기능이 필요하다.

#### ApplicationContext가 제공하는 부가기능
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/540dcbd7-b661-4039-9709-5dc489f90eb1)

* **메세지 소스를 활용한 국제화 기능**
  * ex) 접속 국가에 따른 언어 변경
* **환경 변수**
  * 로컬, 개발환경, 운영환경을 구분해서 처리
* **애플리케이션 이벤트**
  * 이벤트를 발행하고 구독하는 모델을 편리하게 지원
* **편리한 리소스 조회**
  * 파일, 클래스패스, 외부 등에서 리소스를 편리하게 조회


#### 정리
* ApplicationContext는 BeanFactory의 기능을 상속받는다.
* ApplicationContext는 빈 관리기능 + 편리한 부가 기능을 제공한다.
* **BeanFactory나 ApplicationContext를 스프링 컨테이너라 한다.**

#### 스프링 빈 설정 메타 정보 - BeanDefinition
* 스프링은 어떻게 이런 다양한 설정 형식을 지원하는 것일까?
  * -> 그 중심에는 `BeanDefinition`이라는 추상화가 존재한다.
  
* 쉽게 말해, **역할과 구현을 개념적으로 나눈 것**.
* `BeanDefinition`은 **빈 설정 메타 정보** 라고 하며, `@bean`당 각각 하나의 메타 정보가 생성된다.
* 스프링 컨테이너는 이 메타 정보를 기반으로 스프링 빈을 생성한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/a51d3e31-fb1d-48a8-9ba9-b98161f819c4)

* `BeanDefinition`에는 메타정보들이 들어있다.
* `BeanDefinition`을 직접 생성해서 스프링 컨테이너에 등록할 수 도 있다. 하지만 실무에서 `BeanDefinition`을 직접 정의하거나, 사용할 일은 거의 없다.
* `BeanDefinition`에 대해서는 너무 깊이있게 이해하기 보다는, 스프링이 다양한 형태 설정 정보를 `BeanDefinition`으로 추상화해서 사용하는 것 정도만 이해하면 된다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/887449b5-ee05-4830-8c00-4d5b43a3510f)
* 코드 레벨로 조금 더 깊이 있게 들어가면, 다음과 같다.
  * `AnnotationConfigApplicationContext` 는 `AnnotatedBeanDefinitionReader` 를 사용해서 `AppConfig.class` 를 읽고 `BeanDefinition` 을 생성한다.
  * `GenericXmlApplicationContext` 는 `XmlBeanDefinitionReader` 를 사용해서 `appConfig.xml` 설정 정보를 읽고 `BeanDefinition` 을 생성한다.
  * 새로운 형식의 설정 정보가 추가되면, `XxxBeanDefinitionReader를` 만들어서 `BeanDefinition` 을 생성 하면 된다.


### 웹 애플리케이션과 싱글톤
#### 웹 애플리케이션과 싱글톤
* 스프링 -> 태생이 기업용 온라인 서비스 기술을 지원하기 위해 탄생.
* 대부분의 스프링 애플리케이션은 웹 애플리케이션이다. 물론 웹이 아닌 애플리케이션 개발도 얼마든지 개발할 수 있다.
* 웹 애플리케이션은 보통 여러 고객이 동시에 요청을 한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/5335957b-6ce1-44b5-87f2-5489708e7408)

* 우리가 만들었던 스프링 없는 순수한 DI 컨테이너인 AppConfig는 요청을 할 때 마다 객체를 생성하게 된다. 
* 즉, 고객 트래픽이 초당 100이 나오면, 초당 100개의 객체가 생성되고 소멸되므로, 메모리 낭비가 심하다.
* 해결 방안은 해당 객체가 딱 1개만 생성되고, 공유하도록 설계하면 된다. -> **싱글톤**

#### 싱글톤 패턴
* **클래스의 인스턴스가 딱 1개만 생성되는 것을 보장하는** 디자인 패턴이다.
* 그래서 객체 인스턴스를 2개 이상 생성하지 못하도록 막아야 한다.
  * private 생성자를 사용해서 외부에서 임의로 new 키워드를 사용하지 못하도록 막아야 한다.

##### 싱글톤 패턴 생성 과정
1. static 영역에 객체 instance를 미리 하나 생성해서 올려둔다.
2. 이 객체 인스턴스가 필요하면 오직 `getInstance()` 메서드를 통해서만 조회할 수 있다. 이 메서드를 호출하면 항상 같은 인스턴스를 반환한다.
3. 딱 1개의 객체 인스턴스만 존재해야 하므로, 생성자를 private로 막아서 혹시라도 외부에서 new 키워드로 객체 인스턴스가 생성되는 것을 막는다.

```JAVA
public class SingletonService {

    // 클래스 레벨에 올라가기 때문에, 하나만 존재.
    // static 영역에 객체를 1개만 생성해준다.
    private static final SingletonService instance = new SingletonService();

    // public으로 열어, 객체 인스턴스가 필요하다면 getInstance 메소드를 통해서만 조회하도록 허용한다.
    public static SingletonService getInstance() {
        return instance;
    }

    // 생성자를 private로 선언해, 외부에서 new 키워드를 사용한 객체 생성을 막는다.
    private SingletonService() {}

    public void logic() {
        System.out.println("싱글톤 객체 로직 호출");
    }
}
```
* private으로 new 키워드를 막아둔다.
> 위 과정은 객체를 미리 생성해두는 가장 단순하고 안전한 방법.

싱글톤 패턴을 적용하면 고객의 요청이 올 때 마다 객체를 생성하는 것이 아니라, 이미 만들어진 객체를 공유 해서 효율적으로 사용할 수 있다. 하지만 싱글톤 패턴은 다음과 같은 수 많은 문제점들을 가지고 있다.

* **싱글톤 패턴의 문제점**
  * 싱글톤 패턴을 구현하는 코드 자체가 많이 들어간다.
  * 의존관계상 클라이언트가 구체 클래스에 의존한다.(`~~Impl.getInstance()`) => DIP를 위반한다.
  * 클라이언트가 구체 클래스에 의존해서 OCP 원칙을 위반할 수 있다.
  * 테스트 하기가 어렵다.
  * 내부 속성을 변경하거나 초기화 하기 어렵다.
  * private 생성자로 자식 클래스를 만들기 어렵다.
  * 유연성이 떨어진다.
  * **안티패턴**으로 불리기도 한다.

#### 싱글톤 컨테이너 ( 스프링 컨테이너 )
* 스프링 컨테이너는 싱글톤 패턴의 문제점을 해결하면서, 객체 인스턴스를 싱글톤으로 관리한다.
  * 지금까지 학습한 스프링 빈이 바로 **싱글톤으로 관리되는 빈**이다.
* 스프링 컨테이너는 싱글톤 패턴을 적용하지 않아도, 객체 인스턴스를 싱글톤으로 관리한다.
* 스프링 컨테이너는 싱글톤 컨테이너의 역할을 한다. 이렇게 싱글톤 객체를 생성하고 관리하는 기능을 **싱글톤 레지스트리**라 한다.
* 스프링 컨테이너의 이런 기능 덕분에 싱글톤 패턴의 모든 단점을 해결하면서 객체를 싱글톤으로 유지할 수 있다. 
  * 싱글톤 패턴을 위한 지저분한 코드가 들어가지 안하도 된다.
  * DIP, OCP, 테스트, private 생성자로 부터 자유롭게 싱글톤을 사용할 수 있다.

```JAVA
    @Test
    @DisplayName("스프링 컨테이너와 싱글톤")
    void springContainer() {

        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

        MemberService memberService1 = ac.getBean("memberService", MemberService.class);
        MemberService memberService2 = ac.getBean("memberService", MemberService.class);

        System.out.println("memberService1 = " + memberService1);
        System.out.println("memberService2 = " + memberService2);

        assertThat(memberService1).isSameAs(memberService2);
    }
```

* 스프링 컨테이너 덕분에 고객의 요청이 올 때 마다 객체를 생성하는 것이 아니라, 이미 만들어진 객체를 공유해서 효율적으로 재사용할 수 있다.

> 참고: 스프링의 기본 빈 등록 방식은 싱글톤이지만, 싱글톤 방식만 지원하는 것은 아니다. 요청할 때 마다 새로운 객체를 생성해서 반환하는 기능도 제공한다
