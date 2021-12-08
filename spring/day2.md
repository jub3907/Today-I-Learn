## Spring 강의 2일차 [링크](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%EC%9E%85%EB%AC%B8-%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8/lecture/49576?tab=curriculum&speed=1.5)


### 웹 개발 방법
* 정적 컨텐츠 : 파일을 그대로 웹 브라우저에 전달.
* MVC / Template Engine : 서버에서 HTML파일을 변형해 내려주는 방식
* API : 주로 JSON형태로 클라이언트/서버에 데이터를 전달하는 방식.


#### 정적 컨텐츠
* 스트링 부트는 정적 컨텐츠 기능을 제공
* [링크](https://docs.spring.io/spring-boot/docs/2.3.1.RELEASE/reference/html/spring-boot-features.html#boot-features-spring-mvc-static-content)
* 개략적인 구조는 아래와 같음

 ![image](https://user-images.githubusercontent.com/58246682/145194204-dcba19a0-a4b0-4f21-965e-7916fc645bc6.png)

#### MVC와 템플릿 엔진
* MVC : Model, View, Controller로 나눠 UI와 데이터, 논리구조를 구현하는 디자인 패턴. 주로 View는 화면을, Controller와 Model은 비즈니스 로직을 구현.
  * View : 화면을 그리는걸 담당.
  * Model : 비즈니스 로직을 담당
  * Controller : 사용자가 요청하고 이에 응답하는 이벤트를 처리.

![image](https://user-images.githubusercontent.com/58246682/145195792-3061c039-68f3-4218-a61a-4ca8a84faca8.png)

#### API
* ResponseBody 
  * HTTP의 header와 body에서, body부분에 문자 내용을 직접 반환
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
    private Long id;
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
    Optional<Member> findById(Long id);
    Optional<Member> findByName(String name);
    List<Member> findAll();
}
```
* 회원 repo 메모리 구현체

```JAVA
package hello.hellospring.repository;

import hello.hellospring.domain.Member;

import java.util.*;

public class MemoryMemberRepository implements MemberRepository{

    private static Map<Long, Member> store = new HashMap<>();
    private static long sequence = 0L;

    @Override
    public Member save(Member member) {
        member.setId(++sequence);
        store.put(member.getId(), member);
        return member;

    }

    @Override
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Member> findByName(String name) {
        return store.values().stream()
                .filter(member -> member.getName().equals(name))
                .findAny();
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }
}

```

### 회원 repo 테스트 케이스
* save
```JAVA
@Test
public void save() {
    //given
    Member member = new Member();
    member.setName("spring");

    //when
    repository.save(member);

    //then
    Member result = repository.findById(member.getId()).get();
    assertThat(result).isEqualTo(member);
}
```
* findByName
```JAVA
@Test
public void findByName() {
    //given
    Member member1 = new Member();
    member1.setName("spring1");
    repository.save(member1);

    Member member2 = new Member();
    member2.setName("spring2");
    repository.save(member2);

    //when
    Member result = repository.findByName("spring1").get();

    //then
    assertThat(result).isEqualTo(member1);
}
```
* findAll
```JAVA
@Test
public void findAll() {
    //given
    Member member1 = new Member();
    member1.setName("spring1");
    repository.save(member1);

    Member member2 = new Member();
    member2.setName("spring2");
    repository.save(member2);

    //when
    List<Member> result = repository.findAll();

    //then
    assertThat(result.size()).isEqualTo(2);
}
```

### 회원 서비스
* 회원가입
```JAVA
public Long join(Member member){
    // 중복 회원 x
    validateDuplicateMember(member);
    memberRepository.save(member);
    return member.getId();
}

private void validateDuplicateMember(Member member) {
    memberRepository.findByName(member.getName())
            .ifPresent(m -> {
                throw new IllegalStateException("이미 존재하는 회원입니다.");
            });
}
```
* 전체회원조회
```JAVA
public List<Member> findMembers(){
    return memberRepository.findAll();
}
```
* 단일회원조회
```JAVA
public Optional<Member> findOne(Long memberId) {
    return memberRepository.findById(memberId);
}
```

### 회원 서비스 테스트
```JAVA

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
