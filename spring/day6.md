### Spring 강의 9일차


### 관심사의 분리
* 애플리케이션을 하나의 공연이라고 생각 해 보면, 각각의 인터페이스를 배역이라고 생각할 수 있다.
* 이 때, 실제 배역에 맞는 배우는 누가 선택하는가?
* 이는 공연을 기획하는 기획자나 감독이 선택하지, 배우 자체가 다른 배우를 선택하지는 않는다.
* 이처럼, 이전 코드는 배우가 공연도 하고, 다른 배우도 선택하는 **다양한 책임**을 가지고 있었다.

#### 관심사를 분리하자.
* 배우는 본인의 역할인 배역을 수행하는 것에만 집중해야 한다.
* 즉, 공연을 구성하고, 역할에 맞는 배우는 지정하는 책임을 지는 별도의 **공연 기획자**가 나올 시점이다.

#### AppConfig
* 애플리케이션의 전체 동작 방식을 구성하기 위해, **구현 객체를 생성**하고, **연결**하는 책임을 가지는 별도의 설정 클래스를 만든다.
* 생성자 주입 ( 생성자를 통해 객체가 들어간다. )
* AppConfig는 애플리케이션 실제 동작에 필요한 **구현 객체를 생성**한다.
* 생성한 객체 인스턴스의 참조를 **생성자를 통해서 주입**해준다.

* 클라이언트인 `memberServiceImpl`의 입장에서 보면 의존관계를 마치 외부에서 주입해주는 것 같다고 해서 DI, **의존관계 주입**, 혹은 **의존성 주입**이라고 한다.

```JAVA
// Application의 환경 구성을 Config에서 전담.
public class AppConfig {

    public MemberService memberService() {
        return new MemberServiceImpl(new MemoryMemberRepository());
    }

    public OrderService orderService() {
        return new OrderServiceImpl(new MemoryMemberRepository(), new FixDiscountPolicy());
    }

}
```
* AppConfig는 App의 실제 동작에 필요한 **구현 객체를 생성**한다.
  * `MemberServiceImpl`
  * `MemoryMemberRepository`
  * `OrderServiceImpl`
  * `FixDiscountPolicy`

* AppConfig는 생성한 객체 인스턴스의 참조를 **생성자를 통해 주입**해준다.
  * `MemberServiceImpl` -> `MemoryMemberRepository`
  * `OrderServiceImpl` -> `MemoryMemberRepository` , `FixDiscountPolicy`

* 이러한 설계 변경으로, `MemberServiceImpl`은 `MemoryMemberRepository`에 의존하지 않게 되고, 인터페이스에만 의존한다.
* 즉, `MemberServiceImpl`은 생성자를 통해 어떠한 구현 객체가 들어올 지 알 수 없고, 오직 외부의 `AppConfig`에서 결정되기 때문에 **의존 광계에 대한 고민은 외부**에 맡기고, **실행에만 집중** 하면 된다.

#### 변경된 클래스 다이어그램
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/7b6f313e-7fb6-4ac4-a84c-1f8b579d5332)
* 객체의 생성과 연결은 `AppConfig`가 담당한다.
* **DIP 완성 :** `MemberServiceImpl`은 `MemberRepository`인 추상에만 의존하면 된다. 즉, 구체 클레스를 몰라도 된다.
* **관심사의 분리:** 객체를 생성하고 연결하는 역할과, 실행하는 역할이 명확히 분리되었다.
* 클라이언트인 `memberServiceImpl` 입장에서 보면 의존 관계를 마치 외부에서 주입해주는 것 같다고 해서, DI(Dependancy Injection), 의존관계 주입이라 한다.

#### AppConfig 리팩토링
* 현재 `AppConfig`는 중복이 존재하고, **역할**에 따른 **구현**이 잘 보이지 않는다.
* 설정 정보는 **역할**과 **구현**을 분리해서 한눈에 보여야한다.
* 기대하는 그림

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/22b4f7ba-2ccc-454d-ab8a-04ef2ccb871b)

* 변경 전
```JAVA
public class AppConfig {
    public MemberService memberService() {
        return new MemberServiceImpl(new MemoryMemberRepository());
    }

    public OrderService orderService() {
        return new OrderServiceImpl(new MemoryMemberRepository(), new FixDiscountPolicy());
    }
}
```

* 변경 후
```JAVA
public class AppConfig {

    // Repository는 Memory로 할 것이다.
    private MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

    // Discount Policy는 Fix를 사용한다.
    public DiscountPolicy discountPolicy(){
        return new FixDiscountPolicy();
    }

    // 멤버 서비스에선, MemberServiceImpl을 사용하겠다.
    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }

    // 오더 서비스에선 OrderServiceImpl을 사용하겠다.
    public OrderService orderService() {
        return new OrderServiceImpl(memberRepository(), discountPolicy());
    }
}
```

* 중복이 제거되어, 다른 구현체로 변경하고 싶을 때 한 부분만 변경하면 된다.
* `AppConfig`를 보면, 역할과 구현 클래스가 한눈에 들어온다. 즉, **애플리케이션 전체 구성이 어떻게 되어 있는지 빠르게 파악할 수 있다.**

#### 새로운 구조와, 예제의 할인 정책 변경
* `AppConfig`의 등장으로, 애플리케이션은 크게 **사용 영역**과 객체를 생성하고, 구성하는 **Configuration** 영역으로 분리되었다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/9f04bb74-3f97-4ea0-8429-d772804470e2)

* 따라서, 이제 Fix discount policy를 Rate discount policy로 변경하기 위해, 클라이언트측 코드를 변경할 필요가 없어졌다.


#### 좋은 객체 지향 설계 5가지 원칙의 적용 부분
* SRP : 한 클래스는 하나의 책임만 가져야 한다.
  * 클라이언트 객체(ex.`OrderService`)는 직접 구현 객체를 연결하고, 실행하는 다양한 책임을 가지고 있었다. 
  * 이를 **SRP 단일 책임 원칙**을 따르면서 관심사를 분리하여, AppConfig가 구현 객체를 생성하고 연결하는 책임을 가지게 되었다.
  * 이를 통해 클라이언트 객체는 실행하는 책임만 담당.

* DIP: 프로그래머는 **"추상화에 의존해야지, 구체화에 의존하면 안된다."** 의존성 주입은 이 원칙을 따르는 방법 중 하나.
  * AppConfig가 `FixDiscountPolicy` 객체 인스턴스를 클라이언트 코드 대신 생성해서 클라이언트 코드에 의존관계를 주입했다. 따라서, **DIP 원칙을 따르게 됨.**

* OCP : 소프트웨어 요소는 확장에는 열려있으나 변경에는 닫혀 있어야 한다.
  * 다형성을 사용하고, 클라이언트가 DIP를 지킨다.
  * 애플리케이션을 사용 영역과 구성 영역으로 나누었다.
  * AppConfig가 의존관계를 `Fix -> Rate`로 변경해서 클라이언트에 주입하므로, 클라이언트 코드는 변경하지 않아도 된다.
  * 즉, **소프트 웨어 요소를 새롭게 확장해도 사용 영역의 변경은 닫혀있다.**

### IOC, DI, 그리고 컨테이너
#### 제어의 역전 IoC (Inversion of Control)
* 기존 프로그램은 클라이언트 구현 객체가 스스로 필요한 서버 구현 객체를 생성하고, 연결, 실행함.
* 한마디로, 구현 객체가 프로그램의 제어 흐름을 스스로 조종했다.
* 이는 개발자 입장에서는 자연스러운 흐름.

* 반면에 AppConfig가 등장한 이후, 구현 객체는 자신의 로직을 실행하는 역할만 담당한다.
* 프로그램의 제어 흐름은 AppConfig가 가져간다.

* 프로그램에 대한 제어 흐름은 모두 AppConfig가 가져가고, 심지어 `OrderServiceImpl`도 Appconfig가 생성.
* 이렇게 프로그램의 제어 흐름을 **직접** 제어하는 것이 아니라 외부에서 관리하는 것을 **제어의 역전(IoC)**이라 한다.

#### 프레임워크 vs 라이브러리
* 프레임워크가 내가 작성한 코드를 제어하고, 대신 실행하면 프레임워크 이다. (JUnit)
* 반면에 내가 작성한 코드가 직접 제어의 흐름을 담당한다면, 그것은 프레임워크가 아니라 라이브러리 이다.

#### 의존관계 주입 DI
* `OrderServiceImpl`은 `DiscountPolicy` 인터페이스에만 의존한다. 즉, 실제 어떤 구현 객체가 사용될지는 모른다.
* 의존관계를 **정적인 클래스 의존 관계**와, 실행 시점에 결정되는 **동적인 객체 의존 관계**, 둘을 분리해서 생각해야 한다.

* **정적인 의존 관계**
  * 정적인 의존 관계는 애플리케이션을 실행하지 않아도 분석할 수 있다. ( 인터페이스 )
  * 하지만, 이러한 클래스 의존관계 만으로는 실제로 어떤 객체가 `OrderServiceImpl`에 주입될지 알 수 없다.
* 클래스 다이어그램

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/204288e9-b45a-47a7-b487-03056ecd5e64)


* **동적인 객체 인스턴스 의존 관계**
  * 애플리케이션 실행 시점에 실제 생성된 객체 인스턴스의 참조가 연결된 의존 관계.
* 객체 다이어그램

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/20d608f5-ebad-462b-be23-cf7634356e25)


* 애플리케이션이 **실행시점(런타임)에** 외부에서 실제 구현 객체를 생성하고 클라이언트에 전달해서 클라이언트와 서버의 실제 의존관계가 연결되는 것을 **의존관계 주입**이라고 한다.
* 의존관계 주입을 사용하면 정적인 클래스 의존관계를 변경하지 않고, 동적인 객체 인스턴스를 쉽게 변경할 수 있다.

#### IoC 컨테이너, DI 컨테이너
* AppConfig처럼 객체를 생성하고 관리하면서 **의존관계를 연결** 해 주는것을 **IoC 컨테이너, DI 컨테이너**라고 한다.
* 의존관계 주입에 초점을 맞춰, 최근에는 주로 DI 컨테이너라고 한다.
* 또는 어셈블러, 오브젝트 팩토리 등으로 불리기도 한다.

#### 스프링으로 전환
* `ApplicationContext`를 스프링 컨테이너라 한다. 
* 기존에는 개발자가 `AppConfig`를 사용해서 직접 객체를 생성하고 DI를 주입 해 줬지만, 이제부턴 스프링 컨테이너를 통해서 사용한다.
* 스프링 컨테이너는 `@Configuration`이 붙은 `AppConfig`를 설정 정보로 사용한다. 여기서 `@Bean`이 적힌 메서드를 모두 호출해서 반환된 객체를 스프링 컨테이너에 등록한다.
* 이렇게 스프링 컨테이너레 등록된 객체를 **스프링 빈**이라고 한다.
* 이전에는 개발자가 필요한 객체를 `AppConfig`를 사용해서 직접 조회했지만, 이제부터는 스프링 컨테이너를 통해서 필요한 스프링 빈을 찾아야 한다.
* 스프링 빈은 `applicationContext.getBean()` 메소드를 사용해서 찾을 수 있다.


### 스프링 컨테이너와 스프링 빈
#### 스프링 컨테이너 생성
`ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);`
* `ApplicationContext`를 **스프링 컨테이너**라고 하며, 인터페이스이다.
* 스프링 컨테이너는 XML을 기반으로 만들 수 있고, 애노테이션 기반의 자바 설정 클래스로 만들 수 있다.
* 직전에 `AppConfig`를 사용했던 방식이, 애노테이션 기반의 자바 설정 클래스로 스프링 컨테이너를 만든 것.

> 참고: 더 정확히는 스프링 컨테이너를 부를 때 `BeanFactory` , `ApplicationContext` 로 구분해서 이야기 한다. 이 부분은 뒤에서 설명하겠다. `BeanFactory` 를 직접 사용하는 경우는 거의 없으므로 일반적으로 `ApplicationContext`` 를 스프링 컨테이너라 한다.

#### 스프링 컨테이너 생성 과정

1. 스프링 컨테이너 생성
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/69f41c83-f54d-4fa3-9180-759930db679f)
* `new AnnotationConfigApplicationContext(AppConfig.class)`
* 스프링 컨테이너를 생성할 때는 구성 정보를 지정해줘야 하며, 여기선 `AppConfig.class` 사용.

2. 스프링 빈 등록
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/f429ddc4-b2df-4b9b-b699-74b13994b9d3)
* 스프링 컨테이너는 파라미터로 넘어온 설정 클래스 정보를 사용해 스프링 빈을 등록한다.
* 기본적으로, 빈 이름은 메소드 이름을 사용하며 직접 부여할 수도 있다.


3. 스프링 빈 의존관계 설정
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/c2f83a0f-093a-4b57-b14d-67c6a58adb2d)
* 스프링 컨테이너는 설정 정보를 참고해서 의존관계를 주입 ( DI ) 한다.
* 단순히 자바 코드를 호출하는 것 같지만, 차이가 있다. 이 차이는, 추후 싱글톤 컨테이너에서 언급.

* **참고**
  * 스프링은 빈을 생성하고, 의존관계를 주입하는 단계가 나뉘어 있다. 
  * 하지만, 이렇게 자바 코드로 스프링 빈을 등록하면 생성자를 호출하면서 의존관계 주입도 한번에 처리된다.

#### 컨테이너에 등록된 모든 빈 조회
* 모든 빈 출력하기
  * `ac.getBeanDefinitionNames()` : 스프링에 등록된 모든 빈 이름을 조회한다.
  * `ac.getBean()` : 빈 이름으로 빈 객체를 조회한다.

* 애플리케이션 빈 출력하기
  * 스프링이 내부에서 사용하는 빈은 제외하고, 내가 등록한 빈만 출력한다.
  * 스프링이 내부에서 사용하는 빈은 `getRole()`로 구분할 수 있다.
  * `ROLE_APPLICATION` : 직접 등록한 애플리케이션 빈
  * `ROLE_INFRASTRUCTURE` : 스프링이 내부에서 사용하는 빈

* 실제로는 위처럼, 직접 출력할 일은 없다.

#### 스프링 빈 조회 - 기본
* 스프링 컨테이너에서 스프링 빈을 찾는 가장 기본적인 조회방법
  * `ac.getBean(빈이름, 타입)`
  * `ac.getBean(타입)`
* 조회 대상 스프링 빈이 없으면 예외 발생.
  * `NoSuchBeanDefinitionException: No bean named 'xxxxx' available`

#### 스프링 빈 조회 - 동일한 타입이 둘 이상.
* 타입 조회시 같은 타입의 스프링 빈이 둘 이상이면 오류가 발생한다. 이때는 빈 이름을 지정해야 한다.
* `ac.getBeansOfType()`을 사용하면 해당 타입의 모든 빈을 조회할 수 있다.
* `NoUniqueBeanDefinitionException` 발생

