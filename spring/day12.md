### Spring 강의 12일차

#### @Configuration과 싱글톤
* memberSErvice 빈을 만드는 코드를 보면, memberRepository를 호출한다.
  * 이 메서드를 호출하면 `new MemoryMemberRepository`를 호출한다.

* 또한, orderService 빈을 만드는 코드에서도 memoryMemberRepository를 호출한다.
  * 이는 싱글톤 패턴이 깨지는 것이 아닌가?
  * -> 확인해보면, MemoryMemberRepository 인스턴스는 하나만 생성된다.


#### @Configuration과 바이트코드 조작의 마법
* 순수한 클래스라면 `class hello.core.AppConfig`가 출력되어야 한다.
* 하지만, 예상과는 다르게 클래스 명에 xxxCGLIB가 붙어있다.
* 이는 내가 만든 클래스가 아니라, 스프링이 CGLIB라는 바이트코드 조작 라이브러리를 사용해서 AppConfig 클래스를 상속받은 임의의 다른 클래스를 만들고, 그 다른 클래스를 스프링 빈으로 등록한 것이다.
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
  
#### 정리
* @Bean만 사용해도 스프링으로 등록되지만, 싱글톤을 보장하지는 않는다.
  * `memberRepository()`처럼 의존관계 주입이 필요해서 메서드를 직접 호출할 때 싱글톤을 보장하지 않는다.
* 크게 고민할 것 없이, 스프링 설정 정보는 항상 `@Configuration`을 사용하자.
