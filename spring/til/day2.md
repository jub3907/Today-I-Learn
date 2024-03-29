### 웹 개발 방법
* 정적 컨텐츠 : 파일을 그대로 웹 브라우저에 전달.
* MVC / Template Engine : 서버에서 HTML파일을 변형해 내려주는 방식
* API : 주로 JSON형태로 클라이언트/서버에 데이터를 전달하는 방식.


#### 정적 컨텐츠
* 스트링 부트는 정적 컨텐츠 기능을 제공
* [링크](https://docs.spring.io/spring-boot/docs/2.3.1.RELEASE/reference/html/spring-boot-features.html#boot-features-spring-mvc-static-content)
* 개략적인 구조는 아래와 같음

 ![image](https://user-images.githubusercontent.com/58246682/145194204-dcba19a0-a4b0-4f21-965e-7916fc645bc6.png)
현재는 컨트롤러가 존재하지 않으므로, 정적 페이지에 접근하게 됨.


#### MVC와 템플릿 엔진
* MVC : Model, View, Controller로 나눠 UI와 데이터, 논리구조를 구현하는 디자인 패턴. 주로 View는 화면을, Controller와 Model은 비즈니스 로직을 구현.
  * View : 화면을 그리는걸 담당.
  * Model : 비즈니스 로직을 담당
  * Controller : 사용자가 요청하고 이에 응답하는 이벤트를 처리.

![image](https://user-images.githubusercontent.com/58246682/145195792-3061c039-68f3-4218-a61a-4ca8a84faca8.png)

#### API
* ResponseBody 어노테이션 
  * HTTP의 header와 body에서, body부분에 문자 내용을 직접 반환하겠다.
  * `viewResolver` 대신에 `HttpMessageConverter` 가 동작
  * 기본 문자처리: `StringHttpMessageConverter`
  * 기본 객체처리: `MappingJackson2HttpMessageConverter`
  * byte 처리 등등 기타 여러 HttpMessageConverter가 기본으로 등록되어 있음

* 메소드에서 객체가 리턴되면 JSON형식으로 반환되는것이 기본 정책.
![image](https://user-images.githubusercontent.com/58246682/145197437-bb883f14-07fd-4a36-bc34-fdb97f74e5eb.png)

* 


### 회원 관리 예제 - 백엔드 개발
* Spring 생태계에서 어떤 방식으로 개발되는지 알아보기 위한 예제.


#### 비즈니스 요구사항 정리
* 데이터 : 회원의 ID / 이름
* 기능  : 회원 등록 / 조회
* DB가 존재하지 않는다는 가정.

#### 일반적인 웹 애플리케이션 계층 구조
![image](https://user-images.githubusercontent.com/58246682/145198125-4cee130b-a525-4521-b84f-67bc91086bba.png)
* **컨트롤러**: 웹 MVC의 컨트롤러 역할
* **서비스**: 핵심 비즈니스 로직 구현
* **도메인**: 비즈니스 도메인 객체, 예) 회원, 주문, 쿠폰 등등 주로 데이터베이스에 저장하고 관리됨
* **리포지토리**: 데이터베이스에 접근, 도메인 객체를 DB에 저장하고 관리

#### 클래스 의존관계!
[image](https://user-images.githubusercontent.com/58246682/145198285-6a98bbb6-7144-43a5-b3e7-94e9913e7cb3.png)
* 아직 데이터 저장소가 선정되지 않아서, 우선 인터페이스로 구현 클래스를 변경할 수 있도록 설계
* 데이터 저장소는 RDB, NoSQL 등등 다양한 저장소를 고민중인 상황으로 가정
* 개발을 진행하기 위해서 초기 개발 단계에서는 구현체로 가벼운 메모리 기반의 데이터 저장소 사용

### 회원 도메인 및 리포지토리 생성

* 회원 객체

```JAVA
package hello.hellospring.domain;

public class Member {

    // 유저 ID, 시스템이 지정
    private Long id;
    // 유저 이름
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


```
* 회원 repo 인터페이스
```JAVA
package hello.hellospring.repository;

import hello.hellospring.domain.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    Member save(Member member);
    // Optional : NULL일 경우를 대비해, Optional 사용
    Optional<Member> findById(Long Id);
    Optional<Member> findByName(String name);
    List<Member> findAll();

}

```
* 회원 repo 메모리 구현체

```JAVA
package hello.hellospring.repository;

public class MemoryMemberRepository implements MemberRepository{

    // 실무일 때, 동시성 문제가 있을 수 있을땐 concurrent hashmap 사용해야 함.
    private static Map<Long, Member> store = new HashMap<>();
    // 키 값, 실무에선 동시성 문제로 인해 다른 방식으로 사용해야함.
    private static long sequence = 0L;

    @Override
    public Member save(Member member) {
        member.setId(++sequence);
        store.put(member.getId(), member);
        return member;
    }

    @Override
    public Optional<Member> findById(Long id) {
        // null이 반환될 가능성이 있을 경우, Optional.ofnullable로 감쌈.
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Member> findByName(String name) {
        // 루프 실행
        return store.values().stream()
                .filter(member -> member.getName().equals(name))
                .findAny();

    }

    @Override
    public List<Member> findAll() {
        // 자바에서 실무할 땐, 리스트 자주 사용.
        return new ArrayList<>(store.values());
    }

    // 테스트가 종료될 때마다, 깔끔하게 메모리DB를 지워준다.
    public void clearStore() {
        store.clear();
    }

}

```

### 회원 repo 테스트 케이스
##### 현재는 개발 후, 테스트 케이스를 작성 했음. 
##### 하지만, 테스트 클래스를 먼저 작성한 뒤, 내가 만들어야 하는 것을 구현 클래스로 만드는 방식을 TDD라고 한다.

```JAVA
package hello.hellospring.repository;

public class MemoryMemberRepositoryTest {

    MemoryMemberRepository repository = new MemoryMemberRepository();


    // 테스트가 종료될 때마다, 깔끔하게 메모리DB를 지워준다. 콜백 메소드
    @AfterEach
    public void afterEach(){
        repository.clearStore();
    }

    // Test 어노테이션을 붙이면, 메소드를 실행해볼 수 있음.
    @Test
    public void save() {
        // given
        Member member = new Member();
        member.setName("spring");

        // when
        repository.save(member);
        // Optional에선 get을 사용해 데이터를 꺼내올 수 있음.
        Member result = repository.findById(member.getId()).get();

        // 테스트를 위해 Assertions 사용. 위는 junit의 방식, 아래는 assertj의 방식
        // Assertions.assertEquals(member, result);
        assertThat(member).isEqualTo(result);

    }

    @Test
    public void findByName() {
        Member member1 = new Member();
        member1.setName("spring1");
        repository.save(member1);

        Member member2 = new Member();
        member2.setName("spring1");
        repository.save(member2);

        Member result = repository.findByName("spring1").get();

        assertThat(result).isEqualTo(member1);

    }

    @Test
    public void findAll() {
        Member member1 = new Member();
        member1.setName("spring1");
        repository.save(member1);

        Member member2 = new Member();
        member2.setName("spring1");
        repository.save(member2);

        List<Member> result = repository.findAll();

        assertThat(result.size()).isEqualTo(2);
    }
}

```


### 회원 서비스
* 회원가입, 전체 회원 조회, 단일 회원 조회
```JAVA
package hello.hellospring.service;

public class MemberService {

    //
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * 회원 가입
     */
    public Long join(Member member) {
        validateDuplicateMember(member);

        memberRepository.save(member);
        return member.getId();
    }

    // 같은 이름이 있는 중복 회원이 있는지 검사해야 한다.
    // 옵셔널로 감쌌기 때문에, ifPresent 가능.
    private void validateDuplicateMember(Member member) {
        memberRepository.findByName(member.getName())
            .ifPresent(m -> {
                throw new IllegalStateException("이미 존재하는 회원입니다.");
            });
    }

    /**
     * 전체 회원 조회
     */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    public Optional<Member> findOne(Long memberId) {
        return memberRepository.findById(memberId);
    }
}

```

### 회원 서비스 테스트
```JAVA
class MemberServiceTest {

    MemberService memberService;
    MemoryMemberRepository memberRepository;

    // 의존관계 주입(DI)를 위해.
    @BeforeEach
    public void beforeEach() {
        memberRepository = new MemoryMemberRepository();
        memberService = new MemberService(memberRepository);

    }

    // 테스트가 종료될 때마다, 깔끔하게 메모리DB를 지워준다. 콜백 메소드
    @AfterEach
    public void afterEach(){
        memberRepository.clearStore();
    }


    @Test
    void 회원가입() {
        //given
        Member member = new Member();
        member.setName("hello");
        //when

        Long savedId = memberService.join(member);

        //then
        Member findMember = memberService.findOne(savedId).get();
        assertThat(member).isEqualTo(findMember);
    }

    @Test
    public void 중복_회원_예외() {
        //given
        Member member1 = new Member();
        member1.setName("spring1");

        Member member2 = new Member();
        member2.setName("spring1");
        //when
        memberService.join(member1);
        // member2를 join할건데, IllegalStateException이 터져야만 한다.
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> memberService.join(member2));
        assertThat(e.getMessage()).isEqualTo("이미 존재하는 회원입니다.");

        /*
        try {
            memberService.join(member2);
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("이미 존재하는 회원입니다.");
        }
        */

        //then

    }

    @Test
    void findMembers() {
    }

    @Test
    void findOne() {
    }
}
```

### Dependency Injection

* Member Service에 생성자 추가
```JAVA

    //...
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    //....

```

* Test에서 종속성 주입

```JAVA
    //...
    MemberService memberService;
    MemberRepository memberRepository;

    // 동작하기 전에 넣어줌
    @BeforeEach
    public void beforeEach() {
        memberRepository = new MemoryMemberRepository();
        memberService = new MemberService(memberRepository);
    }

    // 테스트 반복 시 마다 repo 초기화
    @AfterEach
    public void afterEach() {
        memberRepository.clearStore();
    }
    //...
```


### 스프링 빈과 의존관계
* 멤버 컨트롤러는 멤버 서비스를 통해서 회원가입하고, 서비스를 통해 데이터를 조회할 수 있어야 한다.
이를 **의존관계가 있다**고 표현한다.
*  스프링이 처음 생성될 때, `@Controller`가 있다면 해당 객체를 자동으로 생성하고, 스프링에서 관리해준다. 이를 **스프링 컨테이너**에서 **스프링 빈**이 관리된다고 이야기한다.

### 스프링 빈을 등록하는 방법
#### 자동 의존관계 등록
* `@Component` 애노테이션이 있으면 스프링 빈으로 자동 등록된다.
* `@Controller` 컨트롤러가 스프링 빈으로 자동 등록된 이유도 컴포넌트 스캔 때문이다
* 멤버 컨트롤러가 멤버 서비스를 통해 회원을 등록하고 회원을 조회하는 의존관계를 형성해야함.

  * Controller 애노테이션을 통해 자동으로 컨트롤러에서 스프링빈으로 자동 등록.
  * Autowired 애노테이션을 통해 서비스를 스프링이 스프링 컨테이너에 있는 서비스를 가져다가 연결. 이렇게 객체 의존관계를 외부에서 넣어주는걸 의존성 주입.
  * Service 애노테이션을 통해 서비스를 등록.
  * Repository 애노테이션을 통해 repo를 등록.
  ![image](https://user-images.githubusercontent.com/58246682/145392056-3321e086-5476-4393-ae0a-f0f0de106997.png)

* 스프링 빈으로 등록하게 되면 이점이 몇가지 존재.
> 참고: 스프링은 스프링 컨테이너에 스프링 빈을 등록할 때, 기본으로 싱글톤으로 등록한다(유일하게 하나만 등록해서 공유한다) 
> 따라서 같은 스프링 빈이면 모두 같은 인스턴스다. 설정으로 싱글톤이 아니게 설정할 수 있지만, 특별한 경우를 제외하면 대부분 싱글톤을 사용한다.

#### 자바 코드로 직접 스프링 빈 등록 - 컴포넌트 스캔

* Controller는 스프링이 관리하기 때문에 애노테이션 사용
* `@Bean`을 사용하게 되면 스프링이 켜질 때 `@Configuration`을 읽고, service와 repo를 컨테이너에 등록시켜 준다.

#### 참고
* DI에는 필드 주입, setter 주입, 생성자 주입 세가지 방법이 존재. (강의에서는 생성자 주입 )
  * 필드 주입 : 내부 코드 변경이 불가능해 비추천
  * setter 주입 : 서비스가 public으로 노출된다는 단점이 존재.
  * 생성자 주입을 권장, 의존 관계가 실행중에 동적으로 변경되는 경우가 없으므로 생성자 주입은 App이 
조립되는 시점에 Service만 한번 조회하면 되기 때문에 권장된다.

* 실무에서는 주로 정형화된 컨트롤러, 서비스, 리포지토리 같은 코드는 컴포넌트 스캔을 사용한다. 
그리고 정형화 되지 않거나, 상황에 따라 구현 클래스를 변경해야 하면 설정을 통해 스프링 빈으로
등록한다.

*  주의: `@Autowired` 를 통한 DI는 `helloConroller` , `memberService` 등과 같이 스프링이 관리하는
객체에서만 동작한다. 스프링 빈으로 등록하지 않고 내가 직접 생성한 객체에서는 동작하지 않는다.