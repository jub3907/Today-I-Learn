#### 싱글톤 방식의 주의점
* 싱글톤 패턴이든, 스프링과 같은 싱글톤 컨테이너를 사용하든, 객체 인스턴스를 하나만 생성해서 공유하는 싱글톤 방식은 여러 클라이언트가 하나의 객체 인스턴스를 공유하기 때문에, 싱글톤 객체는 **상태를 유지하게 설계하면 안된다.**
* **무상태(stateless)**로 설계해야 한다!
  * 특정 클라이언트에 의존적인 필드가 있으면 안된다.
  * 특정 클라이언트가 값을 변경할 수 있는 필드가 있으면 안된다.
  * 가급적 **읽기만** 가능해야 한다.
  * 필드 대신에 자바에서 공유되지않는, 지역변수, 파라 미터, ThreadLocal 등을 사용해야 한다.

* 상태 유지 문제가 발생한 예시
```JAVA
//...
        // Thread A: A 사용자가 10000 주문
        statefulService1.order("userA", 10000);
        // Thread B: B 사용사자 20000 주문
        statefulService2.order("userB", 20000);

        // ThreadA : 사용자 A 주문금액 조회
        int price = statefulService1.getPrice();
        System.out.println("price = " + price);
//...
```
* 실행 결과
```
name = userA price = 10000
name = userB price = 20000
price = 20000
```
* `StatefulService`의 `price` 필드는 공유 되는 필드인데, 특정 클라이언트에서 값을 변경하고 있다.
* 따라서 사용자 A의 주문 금액은 사용자 B에 의해 `price`값이 수정되므로, 20000이 출력된다.
* 항상 스프링 빈은 항상 **무상태(stateless)**로 설계하자.


#### @Configuration과 싱글톤
* memberSErvice 빈을 만드는 코드를 보면, memberRepository를 호출한다.
  * 이 메서드를 호출하면 `new MemoryMemberRepository`를 호출한다.
```JAVA
    @Bean
    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }
```
* 또한, orderService 빈을 만드는 코드에서도 memoryMemberRepository를 호출한다.
  * 이는 싱글톤 패턴이 깨지는 것이 아닌가?
```JAVA
    @Bean
    public OrderService orderService() {
        return new OrderServiceImpl(memberRepository(), discountPolicy());
    }
```

* 테스트
```JAVA
    @Test
    void configurationTest() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

        MemberServiceImpl memberService = ac.getBean("memberService", MemberServiceImpl.class);
        OrderServiceImpl orderService = ac.getBean("orderService", OrderServiceImpl.class);
        MemberRepository memberRepository = ac.getBean("memberRepository", MemberRepository.class);

        MemberRepository memberRepository1 = memberService.getMemberRepository();
        MemberRepository memberRepository2 = orderService.getMemberRepository();

        System.out.println("memberRepository = " + memberRepository);
        System.out.println("memberRepository1 = " + memberRepository1);
        System.out.println("memberRepository2 = " + memberRepository2);
    }

    // memberRepository = hello.core.member.MemoryMemberRepository@37efd131
    // memberRepository1 = hello.core.member.MemoryMemberRepository@37efd131
    // memberRepository2 = hello.core.member.MemoryMemberRepository@37efd131
```
* 확인해보면, MemoryMemberRepository 인스턴스는 하나만 생성되어 공유되는걸 확인할 수 있다.
* 하지만, AppConfig에선 `new` 키워드를 사용해 여러번 객체를 생성하고 있다.
* 이제, 호출 횟수를 직접 확인 해보자.
```JAVA
    @Bean
    public MemberRepository memberRepository() {
        System.out.println("call AppConfig.memberRepository");
        return new MemoryMemberRepository();
    }

    // Discount Policy는 Fix를 사용한다.
    @Bean
    public DiscountPolicy discountPolicy(){
        return new RateDiscountPolicy();
    }

    // 멤버 서비스에선, MemberServiceImpl을 사용하겠다.
    @Bean
    public MemberService memberService() {
        System.out.println("call AppConfig.memberService");
        return new MemberServiceImpl(memberRepository());
    }

// call AppConfig.memberRepository
// call AppConfig.memberService
// call AppConfig.orderService
```
* 우리의 예상 대로면, 테스트 코드를 실행했을 때 `call AppConfig.memberRepository`가 세번 호출되어야 한다.
* 하지만, 결과적으론 한 번만 실행되는걸 볼 수 있다.
* 이는 아래에서 설명.


#### @Configuration과 바이트코드 조작의 마법
스프링 컨테이너는 싱글톤 레지스트리다. 따라서 스프링 빈이 싱글톤이 되도록 보장해주어야 한다. 그런데 스프링이 자바 코드까지 어떻게 하기는 어렵다. 저 자바 코드를 보면 분명 3번 호출되어야 하는 것이 맞다. 그래서 스프링은 클래스의 바이트코드를 조작하는 라이브러리를 사용한다.
* 모든 비밀은 `@Configuration` 을 적용한 `AppConfig` 에 있다

```JAVA
    @Test
    void configurationDeep() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
        AppConfig bean = ac.getBean(AppConfig.class);

        System.out.println("bean = " + bean.getClass());

    }
// bean = class hello.core.AppConfig$$EnhancerBySpringCGLIB$$aa31659a
```

* 순수한 클래스라면 `class hello.core.AppConfig`가 출력되어야 한다.
* 하지만, 예상과는 다르게 클래스 명에 `xxxCGLIB`가 붙어있다.
* 이는 내가 만든 클래스가 아니라, 스프링이 `CGLIB`라는 바이트코드 조작 라이브러리를 사용해서 `AppConfig` 클래스를 상속받은 임의의 다른 클래스를 만들고, 그 다른 클래스를 스프링 빈으로 등록한 것이다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/ceb26ae8-b6d3-44d1-a01f-02c9c1066279)

* 그 임의의 다른 클래스가 바로 싱글톤이 보장되도록 해준다.
* 예시 코드
```java
  @Bean
  public MemberRepository memberRepository() {

    if (memoryMemberRepository가 이미 스프링 컨테이너에 등록되어 있으면) {
      return ( 스프링 컨테이너에서 찾아서 반환);
    } else {
      기존 로직을 호출해서 MemoryMemberRepository를 생성하고 스프링 컨테이너에 등록
      return 반환
    }
  }
```
* `@Bean`이 붙은 메서드마다 이미 스프링 빈이 존재하면 존재하는 빈을 반환하고, 스프링 빈이 없으면 생성해서 스프링 빈으로 등록하고 반환하는 코드가 동적으로 만들어진다
* 이를 통해, 싱글톤이 보장되는 것이다.
  
#### 정리
* @Bean만 사용해도 스프링으로 등록되지만, 싱글톤을 보장하지는 않는다.
  * `memberRepository()`처럼 의존관계 주입이 필요해서 메서드를 직접 호출할 때 싱글톤을 보장하지 않는다.
* 크게 고민할 것 없이, 스프링 설정 정보는 항상 `@Configuration`을 사용하자.
