### 옵션 처리
예를 들어, 스프링 빈을 Optional하게 설정해둔 뒤, 등록하지 않아도 기본 로직으로 동작하는 등 주입할 스프링 빈이 없어도 동작해야 할 때가 있다. 이 때, `@Autowired`만 사용하게 된다면 `required` 옵션의 기본 값이 `true`로 설정되어 있기 때문에, 자동 주입 대상이 없으면 오류가 발생한다.

자동 주입 대상을 옵션으로 처리하는 방법은 다음과 같다.
`@Autowired(required=false)` : 자동 주입할 대상이 없으면 수정자 메서드 자체가 호출 안됨
`org.springframework.lang.@Nullable` : 자동 주입할 대상이 없으면 null이 입력된다.
`Optional<>` : 자동 주입할 대상이 없으면 Optional.empty 가 입력된다.

```JAVA
//호출 안됨
@Autowired(required = false)
public void setNoBean1(Member member) {
    System.out.println("setNoBean1 = " + member);
}
//null 호출
@Autowired
public void setNoBean2(@Nullable Member member) {
    System.out.println("setNoBean2 = " + member);
}
//Optional.empty 호출
@Autowired(required = false)
public void setNoBean3(Optional<Member> member) {
    System.out.println("setNoBean3 = " + member);
}
```
* `Member`는 스프링 빈이 아니다.
* `setNoBean1`은 `required = false` 속성이기 때문에, 의존 관계가 없을 땐 아예 호출이 안된다.

### 생성자 주입을 선택하자.
과거에는 수정자 주입과 필드 주입을 많이 사용했지만, 최근엔 스프링을 포함한 DI 프레임워크 대부분 생정자 주입을 권장한다. 그 이유는 다음과 같다.

#### 불변
* 대부분의 의존관계 주입은 한번 일어나면 애플리케이션 종료시점까지 의존관계를 변경할 일이 없다. 오히려 대부분의 의존관계는 애플리케이션 종료 전까지 변하면 안된다.(불변해야 한다.)
* 수정자 주입을 사용하면, setXxx 메서드를 public으로 열어두어야 한다.
* 누군가 실수로 변경할 수 도 있고, 변경하면 안되는 메서드를 열어두는 것은 좋은 설계 방법이 아니다.
* 생성자 주입은 객체를 생성할 때 딱 1번만 호출되므로 이후에 호출되는 일이 없다. 따라서 불변하게 설계할 수 있다.

#### 누락
프레임워크 없이 순수한 자바 코드를 단위 테스트 하는 경우(이 방법이 좋은 단위 테스트), 다음과 같이 수정자 의존 관계인 경우를 생각해보자.
* 수정자 주입 코드
```JAVA
    public class OrderServiceImpl implements OrderService {
        private MemberRepository memberRepository;
        private DiscountPolicy discountPolicy;

        @Autowired
        public void setMemberRepository(MemberRepository memberRepository) {
            this.memberRepository = memberRepository;
        }

        @Autowired
        public void setDiscountPolicy(DiscountPolicy discountPolicy) {
            this.discountPolicy = discountPolicy;
        }

        //...
    }
```

* 테스트
```JAVA
class OrderServiceImplTest {
    @Test
    void createOrder() {
        OrderServiceImpl orderService = new OrderServiceImpl();
        orderService.createOrder(1L, "itemA", 10000);
    }
}
```

실행은 되나, `memberRepository`, `discountPolicy` 모두 의존관계 주입이 누락되었기 때문에 실행 결과에선 Null Point Exception이 발생한다. 

하지만, 생성자 주입을 사용한다면 다음처럼 주입 데이터를 누락했을 때 **컴파일 오류**가 발생한다. 즉, IDE상에서 어떤 값을 필수로 주입해야 하는지 바로 알아챌 수 있다.

```JAVA
@Test
void createOrder() {
    OrderServiceImpl orderService = new OrderServiceImpl();
    orderService.createOrder(1L, "itemA", 10000);
}
```

#### final 키워드
생성자 주입을 사용하면 필드에 final 키워드를 사용할 수 있다. 그래서 생성자에서 혹시라도 값이 설정되 지 않는 오류를 컴파일 시점에 막아준다.

* 코드 예시
```JAVA
@Component
public class OrderServiceImpl implements OrderService {
    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;

    @Autowired
    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
    }
    //...
}
```
* 필수 필드인 `discountPolicy`에 값을 설정해야 하는데, 이 부분이 누락되어 있다. 자바는 컴파일 시점에 다음 오류를 발생시킨다.
    * `java: variable discountPolicy might not have been initialized`
* 가장 좋은 오류는 **컴파일 오류**이다. 컴파일 오류는 세상에서 가장 빠르고, 좋은 오류다.

> 참고: 수정자 주입을 포함한 나머지 주입 방식은 모두 생성자 이후에 호출되므로, 필드에 `final` 키워드를 사용할 수 없다. 오직 생성자 주입 방식만 `final` 키워드를 사용할 수 있다.

#### 정리
* 생성자 주입 방식을 선택하는 이유는 여러가지가 있지만, 프레임워크에 의존하지 않고, 순수한 자바 언어의 특징을 잘 살리는 방법이기도 하다.
* 기본으로 생성자 주입을 사용하고, 필수 값이 아닌 경우에는 수정자 주입 방식을 옵션으로 부여하면 된다. 생 성자 주입과 수정자 주입을 동시에 사용할 수 있다.
* 항상 생성자 주입을 선택해라! 그리고 가끔 옵션이 필요하면 수정자 주입을 선택해라. 필드 주입은 사용하지 않는게 좋다.

### 롬복과 최신 트렌드
다음 기본 코드를 최적화 해보자.
```JAVA

@Component
public class OrderServiceImpl implements OrderService{
    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;

    @Autowired
    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
    //...
```
위처럼 생성자가 한 개만 존재한다면, `@Autowired`를 생략할 수 있다. 

이제, 롬복을 적용해보자. 
* `build.gradle`
```
plugins {
	id 'java'
	id 'org.springframework.boot' version '2.7.14'
	id 'io.spring.dependency-management' version '1.0.15.RELEASE'
}

group = 'hello'
version = '0.0.1-SNAPSHOT'

//lombok 설정 추가 시작
configurations {
	compileOnly {
		extendsFrom annotationProcessor
	}
}
//lombok 설정 추가 끝

java {
	sourceCompatibility = '11'
}

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'

	//lombok 라이브러리 추가 시작
	compileOnly 'org.projectlombok:lombok'
	annotationProcessor 'org.projectlombok:lombok'
	
	testCompileOnly 'org.projectlombok:lombok'
	testAnnotationProcessor 'org.projectlombok:lombok'
	//lombok 라이브러리 추가 끝
}

tasks.named('test') {
	useJUnitPlatform()
}
```

* 롬복 라이브러리를 사용하면 `@Getter`, `@Setter` 어노테이션을 사용할 수 있다.
```JAVA
@Getter
@Setter
public class HelloLombok {

    private String name;
    private int age;

    public static void main(String[] args) {
        HelloLombok helloLombok = new HelloLombok();
        helloLombok.setName("adfasdf");

        String name = helloLombok.getName();
        System.out.println("name = " + name);
    }
}
```
위처럼 편하게 Getter, Setter를 세팅할 수도 있고, 이외에도 `@ToString` 등이 존재한다. 실무에서도 많이 사용되므로, 사용법을 알아 두면 좋다.

다시 돌아가, 기존의 `@Autowired` 코드를 최적화 해보자.
* 기존 코드
```JAVA
@Component
public class OrderServiceImpl implements OrderService{
    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;

    @Autowired
    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
    //...
```
* Lombok 적용 코드
```JAVA

@Component
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;

}

```

`@RequiredArgsConstructor`를 사용하게 되면, 필수를 의미하는 `final`이 붙은 변수 값을 사용해 생성자를 생성해준다. 따라서 Lombok 라이브러리의 @RequiredArgsConstructor 함께 사용하면 기능은 다 제공하면서, 코드는 깔끔하게 사용할 수 있다.

### 조회 빈이 2개 이상일 때 발생하는 문제

`@Autowired`는 타입으로 조회하게 된다.
```JAVA
@Autowired
private DiscountPolicy discountPolicy
```
즉, 타입으로 조회하기 때문에 다음 코드와 유사하게 동작한다. 실제로는 더 많은 기능을 제공한다.
`ac.getBean(DiscountPolicy.class)`

스프링 빈 조회에서도 학습했듯, 타입으로 조회하면 선택된 빈이 2개 이상일 때, 문제가 발생한다. `DiscountPolicy`의 하위 타입인 `FixDiscountPolicy`, `RateDiscountPolicy` 둘 다 스프링 빈으로 선언해보자.

```JAVA
@Component
public class FixDiscountPolicy implements DiscountPolicy {}
```

```JAVA
@Component
public class RateDiscountPolicy implements DiscountPolicy {}
```
이후에 의존관계 자동 주입을 실행하면, `NoUniqueBeanDefinitionException` 오류가 발생하게 된다.
```JAVA
@Autowired
private DiscountPolicy discountPolicy

// NoUniqueBeanDefinitionException: No qualifying bean of type
// ...
```
이 때, 하위 타입으로 지정할 수도 있지만, 하위 타입으로 지정하는 것은 DIP를 위배하고 유연성이 떨어진다. 그리고 이름만 다르고, 완전히 똑같은 타입의 스프링 빈이 2개 있을 때 해결이 안된다.

스프링 빈을 수동 등록해서 문제를 해결해도 되지만, 의존 관계 자동 주입에서 해결하는 여러 방법이 있다.

### @Autowired 필드 명, @Qualifier, @Primary
조회 대상 빈이 2개 이상일 때, 다음과 같은 해결 방법이 존재한다.
* `@Autowired` 필드 명 매칭
* `@Qualifier` -> `@Qualifier`끼리 매칭 -> 빈 이름 매칭
* `@Primary` 사용

#### @Autowired 필드 명 매칭
`@Autowired`는 처음 타입 매칭을 시도하고, 여러 빈이 있다면 필드 이름이나 파라미터 이름으로 빈 이름을 추가 매칭한다.

* 기존 코드
```JAVA
@Autowired
private DiscountPolicy discountPolicy
```
* 필드 명을 빈 이름으로 변경
```JAVA
@Autowired
private DiscountPolicy rateDiscountPolicy
```
필드 명이 `rateDiscountPolicy`이므로, 오류가 발생하지 않고 정상 주입 된다.
즉, **필드 명 매칭은 먼저 타입 매칭을 시도하고, 그 결과에 여러개의 빈이 존재할 때 추가로 동작한다.**

#### @Qualifier 사용
`@Qualifier`는 추가 구분자를 붙여주는 방법이다. 주입시 추가적인 방법을 제공하는 것으로, 빈 이름을 변경하는 것은 아니다. **빈 등록 시, @Qualifier를 붙여 준다.**

```JAVA
@Component
@Qualifier("mainDiscountPolicy")
public class RateDiscountPolicy implements DiscountPolicy {}
```

이후, 주입 시에 `@Qualifier`를 붙여주고 등록한 이름을 적어준다.

```JAVA
    public OrderServiceImpl(MemberRepository memberRepository, @Qualifier("mainDiscountPolicy") DiscountPolicy DiscountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = DiscountPolicy;
    }
```

> 만약 `@Qualifier`로 주입할 때, `@Qualifier("mainDiscountPolicy")`를 못찾으면 `mainDiscountPolicy`라는 이름의 스프링 빈을 추가로 찾는다. 하지만, `@Qualifier`는 `@Qualifier`를 찾는 용도로만 사용하는게 명확하고 좋다.

다음과 같이, 직접 빈을 등록할 때도 `@Qualifier`를 동일하게 사용할 수 있다.

```JAVA
@Bean
@Qualifier("mainDiscountPolicy")
public DiscountPolicy discountPolicy() {
    return new ...
}
```

#### @Primary 사용

`@Primary`는 우선순위를 정하는 방법으로, `@Autowired`시 여러 개의 빈이 매칭된다면, @Primary가 우선권을 가진다.

```JAVA
@Component
@Primary
public class RateDiscountPolicy implements DiscountPolicy {}
```
코드를 실행해본다면, `@Primary`가 잘 동작하는걸 확인할 수 있다. 

#### @Primary, @Qualifer 활용


여기까지 보면 `@Primary`와 `@Qualifier`중 어떤 것을 사용하면 좋을 지 고민이 될 것이다.

이 때, `@Qualifier`의 단점은 주입받을 때, 다음과 같이 모든 코드에 `@Qualifier`를 붙여줘야 한다는 점이다.
```JAVA
    public OrderServiceImpl(MemberRepository memberRepository, @Qualifier("mainDiscountPolicy") DiscountPolicy DiscountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = DiscountPolicy;
    }
```
반면, `@Primary`를 사용하면 이렇게 `@Qualifier`를 붙일 필요가 없다.

코드에서 자주 사용하는 **메인 데이터베이스**의 커넥션을 획득하는 스프링 빈이 있고, 코드에서 특별한 기능으로 가끔 사용하는 **서브 데이터베이스**의 커넥션을 획득하는 스프링 빈이 있다고 생각해보자. 

메인 데이터 베이스의 커넥션을 획득하는 스프링 빈은 `@Primary` 를 적용해서 조회하는 곳에서 `@Qualifier` 지정 없이 편리하게 조회하고, 서브 데이터베이스 커넥션 빈을 획득할 때는 `@Qualifier` 를 지정해서 명시적으로 획득 하는 방식으로 사용하면 코드를 깔끔하게 유지할 수 있다. 

물론 이때 메인 데이터베이스의 스프링 빈을 등록할 때 `@Qualifier` 를 지정해주는 것은 상관없다.

#### @Primary와  @Qualifier의 우선 순위
`@Primary` 는 기본값 처럼 동작하는 것이고, `@Qualifier` 는 매우 상세하게 동작한다. 이런 경우 어떤 것이 우선권을 가져갈까? 

스프링은 자동보다는 수동이, 넒은 범위의 선택권 보다는 좁은 범위의 선택권이 우선 순위가 높다. 따라서 여기서도 `@Qualifier` 가 우선권이 높다.


### 애노테이션 직접 만들기
`@Qualifier("mainDiscountPolicy")` 이렇게 문자를 적으면 컴파일시 타입 체크가 안된다. 다음과 같은 애노테이션을 만들어서 문제를 해결할 수 있다.

* 애노테이션 생성
```JAVA
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Qualifier("mainDiscountPolicy")
public @interface MainDiscountPolicy {
}
```

* 애노테이션 사용
```JAVA
@Component
@MainDiscountPolicy
public class RateDiscountPolicy implements DiscountPolicy{
    //...
}
```
```JAVA

@Component
public class OrderServiceImpl implements OrderService{
    //...
    public OrderServiceImpl(MemberRepository memberRepository, @MainDiscountPolicy DiscountPolicy DiscountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = DiscountPolicy;
    }
    //...
}
```

**애노테이션에는 상속이라는 개념이 없다.** 

이렇게 여러 애노테이션을 모아서 사용하는 기능은 스프링이 지원 해주는 기능이다. `@Qualifier` 뿐만 아니라 다른 애노테이션들도 함께 조합해서 사용할 수 있다. 단적으로 @Autowired 도 재정의 할 수 있다. 

물론 스프링이 제공하는 기능을 뚜렷한 목적 없이 무분별하게 재정의 하는 것은 유지보수에 더 혼란만 가중할 수 있다.


### 조회한 빈이 모두 필요할 때, List, Map

의도적으로, 정말 해당 타입의 스프링 빈이 다 필요한 경우도 있다.

예를 들어, 할인 서비스를 제공한다고 가정해보자. 클라이언트가 할인의 종류(rate, fix)를 선택할 수 있게 만들어야 한다면, 스프링을 사용할 땐 이러한 전략 패턴을 아주 쉽게 구현할 수 있다.
```JAVA

public class AllBeanTest {

    @Test
    void findAllBean() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AutoAppConfig.class, DiscountService.class);

        DiscountService discountService = ac.getBean(DiscountService.class);
        Member member = new Member(1L, "userA", Grade.VIP);
        int discountPrice = discountService.discount(member, 10000, "fixDiscountPolicy");

        assertThat(discountService).isInstanceOf(DiscountService.class);
        assertThat(discountPrice).isEqualTo(1000);

        int rateDiscountPrice = discountService.discount(member, 20000, "rateDiscountPolicy");
        assertThat(rateDiscountPrice).isEqualTo(2000);
    }

    static class DiscountService {
        private final Map<String, DiscountPolicy> policyMap;
        private final List<DiscountPolicy> policies;

        @Autowired
        public DiscountService(Map<String, DiscountPolicy> policyMap, List<DiscountPolicy> policies) {
            this.policyMap = policyMap;
            this.policies = policies;

            System.out.println("policyMap = " + policyMap);
            System.out.println("policies = " + policies);
        }

        public int discount(Member member, int price, String discountCode) {
            DiscountPolicy discountPolicy = policyMap.get(discountCode);
            return discountPolicy.discount(member, price);
        }
    }
}
```

* 로직 분석
    * `DiscountService`는 Map으로 주입된 모든 `DiscountPolicy`를 주입 받는다. 이 때, `fix, rate`가 주입된다.
    * `discount` 메소드는 `discountCode`를 사용해 `"fixDiscountPolicy"`가 넘어오면 map에서 `fixDiscountPolicy` 스프링 빈을 찾아서 실행한다. rate의 경우도 마찬가지.

* 주입 분석
    * `Map<String, DiscountPolicy>` : map의 키에 스프링 빈의 이름을 넣어주고, 그 값으로 `DiscountPolicy` 타입으로 조회한 모든 스프링 빈을 담아준다.
    * `List<DiscountPolicy>` : `DiscountPolicy` 타입으로 조회한 모든 스프링 빈을 담아준다.
    * 만약 해당하는 타입의 스프링 빈이 없으면, 빈 컬렉션이나 Map을 주입한다.

> 참고 : 스프링 컨테이너는 생성자에 클래스 정보를 받는다. 여기에 클래스 정보를 넘기면 해당 클래스가 스프링 빈 으로 자동 등록된다. `new AnnotationConfigApplicationContext(AutoAppConfig.class,DiscountService.class);` 이 코드는 2가지로 나누어 이해할 수 있다.

> 1. `new AnnotationConfigApplicationContext()` 를 통해 스프링 컨테이너를 생성한다.

> 2. `AutoAppConfig.class` , `DiscountService.class` 를 파라미터로 넘기면서 해당 클래스를 자동으로 스프링 빈으로 등록한다.

> 정리하면 스프링 컨테이너를 생성하면서, 해당 컨테이너에 동시에 AutoAppConfig , DiscountService 를 스프링 빈으로 자동 등록한다.


### 자동, 수동의 올바른 실무 운영 기준

#### 편리한 자동 기능을 기본으로 사용하자.
그러면 어떤 경우에 컴포넌트 스캔과 자동 주입을 사용하고, 어떤 경우에 설정 정보를 통해서 수동으로 빈을
등록하고, 의존관계도 수동으로 주입해야 할까?

결론부터 이야기하자면, 스프링이 나오고 시간이 갈 수록 점점 자동을 선호하는 추세이다. 스프링은 `@Component` 뿐만 아니라 `@Controller` , `@Service`, `@Repository`처럼 계층에 맞추어 일반적인 애플리케이션 로직을 자동으로 스캔할 수 있도록 지원한다. 거기에 더해, 최근 스프링 부트는 컴포넌트 스캔을 기본으로 사용하고, 스프링 부트의 다양한 스프링 빈들도 조건에 맞으면 자동으로 등록하도록 설계했다.

설정 정보를 기반으로 애플리케이션을 구성하는 부분과 실제 동작하는 부분을 명확하게 나누는 것이 이상적이지만, 개발자 입장에서 스프링 빈을 하나 등록할 때 `@Component` 만 넣어주면 끝나는 일을 `@Configuration` 설정 정보에 가서 `@Bean` 을 적고, 객체를 생성하고, 주입할 대상을 일일이 적어주는 과정은 상당히 번거롭다.

또 관리할 빈이 많아서 설정 정보가 커지면 설정 정보를 관리하는 것 자체가 부담이 된다. 그리고 결정적으로 자동 빈 등록을 사용해도 OCP, DIP를 지킬 수 있다.

#### 그렇다면, 수동 빈 등록은 언제 사용하면 좋을까?
애플리케이션은 크게 업무 로직과, 기술 지원 로직으로 나눌 수 있다.

* **업무 로직 빈**: 웹을 지원하는 **컨트롤러**, 핵심 비즈니스 로직이 있는 **서비스**, 데이터 계층의 로직을 처리하는 **리포지토리**등이 모두 업무 로직이다. 보통 비즈니스 요구사항을 개발할 때 추가되거나 변경된다.

* **기술 지원 빈**: 기술적인 문제나 공통 관심사(AOP)를 처리할 때 주로 사용된다. 데이터베이스 연결이나, 공통 로그 처리 처럼 업무 로직을 지원하기 위한 하부 기술이나 공통 기술들이다.

업무 로직은 숫자도 매우 많고, 한번 개발하게 되면 컨트롤러, 서비스, 리포지토리처럼 어느정도 유사한 패턴이 존재한다. 이런 경우, 자동 기능을 사용하는 것을 추천한다. 보통, 문제가 발생하더라도 어떤 곳에서 문제가 발생했는지 명확히 파악하기가 쉽다.

하지만 기술 지원 로직은 업무 로직에 비해 그 수가 매우 적고, 보통 애플리케이션 전반에 걸쳐서 광범위하게 영향을 미친다. 또한, 기술 지원 로직은 적용이 잘 되고 있는지, 아닌지 조차 파악하기 어려운 경우가 많다. 따라서, 이러한 기술 지원 로직은 가급적 수동 빈 등록을 사용하여 명확하게 드려내는 것이 좋다.

즉, **애플리케이션에 광범위하게 영향을 미치는 기술 지원 객체는 수동 빈으로 등록해서 설정 정보에 바로 나타나게 하는 것이 유지보수 하기 좋다.**

#### 비즈니스 로직 중에서, 다형성을 적극 활용할 때.
의존관계 자동 주입 - 조회한 빈이 모두 필요할 때, List, Map을 다시 확인해보자.

`DiscountService`가 의존관계 자동 주입으로, `Map<String, DiscountPolicy>`에 주입을 받을 때, 여기에 어떤 빈이 주입될 지, 각 빈들의 이름은 무엇일지 코드만 보고 한번에 쉽게 파악할 수 있을까?

지금은 내가 혼자 개발하는 예제이므로 상관 없겠지만, 이 코드를 다른 개발자가 내게 준 것이라면, 자동 등록을 사용하고 있기 때문에 전체를 파악하기 위해 여러 코드를 찾아봐야만 한다.

이런 경우, 수동 빈으로 등록하거나, 혹은 자동으로 하게 되면 **특정 패키지에 같이 묶어두는 것**이 좋다. 핵심은, 한 눈에 내용을 파악할 수 있어야만 한다.

이러한 부분을 별도의 설정 정보로 만들고, 수동으로 등록하면 다음과 같다.
```JAVA
@Configuration
public class DiscountPolicyConfig {

    @Bean
    public DiscountPolicy rateDiscountPolicy() {
        return new RateDiscountPolicy();
    }
    @Bean
    public DiscountPolicy fixDiscountPolicy() {
        return new FixDiscountPolicy();
    }
}
```
이 설정 정보만 봐도 한눈에 빈의 이름은 물론이고, 어떤 빈들이 주입될지 파악할 수 있다. 그래도 빈 자동 등 록을 사용하고 싶으면 파악하기 좋게 `DiscountPolicy` 의 구현 빈들만 따로 모아서 특정 패키지에 모아두는 것이 좋다.

> 참고. **스프링과 스프링 부트가 자동으로 등록하는 수 많은 빈들은 예외**이다. 이런 부분들은 스프링 자체를 잘 이해하고 스프링의 의도대로 잘 사용하는게 중요하다. 스프링 부트의 경우 `DataSource` 같은 데이터베이스 연결에 사용하는 기술 지원 로직까지 내부에서 자동으로 등록하는데, 이런 부분은 메뉴얼을 잘 참고해서 스프링 부트가 의도한 대로 편리하게 사용하면 된다. 반면에 스프링 부트가 아니라 내가 직접 기술 지원 객체를 스프링 빈으로 등록한다면 수동으로 등록해서 명확하게 드러내는 것이 좋다.