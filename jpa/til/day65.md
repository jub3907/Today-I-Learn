## JPA 및 DB 설정
### application.yml
```java
spring:
  output:
    ansi:
      enabled: always

  datasource:
    url: jdbc:h2:tcp://localhost/~/datajpa
    username: sa
    password:
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create
    properties:
      hibernate:
#        show_sql: true
        format_sql: true
#        default_batch_fetch_size: 100

logging.level:
  org.hibernate.SQL: debug
#  org.hibernate.type: trace #스프링 부트 2.x, hibernate5
#  org.hibernate.orm.jdbc.bind: trace #스프링 부트 3.x, hibernate6
```
* `spring.jpa.hibernate.ddl-auto: create`
  * 이 옵션은 애플리케이션 실행 시점에 테이블을 drop 하고, 다시 생성한다.

> 모든 로그 출력은 가급적 로거를 통해 남겨야 한다.\
> `show_sql` : 옵션은 System.out 에 하이버네이트 실행 SQL을 남긴다.\
> `org.hibernate.SQL` : 옵션은 logger를 통해 하이버네이트 실행 SQL을 남긴다.
<br/>

### 실제 동작 확인
#### 회원 엔티티
```java
@Entity
@Getter
@Setter
public class Member {

    @Id @GeneratedValue
    private Long id;
    private String username;

    protected Member() {
    }

    public Member(String username) {
        this.username = username;
    }
}
```
<br/>

#### 회원 JPA 리포지토리
```java
@Repository
public class MemberJpaRepository {

    @PersistenceContext
    private EntityManager em;

    public Member save(Member member) {
        em.persist(member);
        return member;
    }

    public Member find(Long id) {
        return em.find(Member.class, id);
    }
}
```
<br/>

#### JPA 기반 테스트
```java
@SpringBootTest
@Transactional
//@Rollback(value = false)
class MemberJpaRepositoryTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    void testMember() {
        Member member = new Member("username");
        Member savedMember = memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.find(savedMember.getId());

        assertThat(findMember).isEqualTo(member);
    }
}
```
<br/>

#### 스프링 데이터 JPA 리포지토리
```java
public interface MemberRepository extends JpaRepository<Member, Long> {
}
```
<br/>

#### 스프링 데이터 JPA 기반 테스트
```java
@SpringBootTest
@Transactional
//@Rollback(value = false)
class MemberJpaRepositoryTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    void testMember() {
        Member member = new Member("username");
        Member savedMember = memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.find(savedMember.getId());

        assertThat(findMember).isEqualTo(member);
    }
}
```
<br/>

## 예제 도메인 모델
### 엔티티 클래스
![image](https://github.com/charon-aesther/aesther-discord-bot/assets/58246682/7ea4521c-96bc-4c73-807a-254d06ea52bb)
<br/>
<br/>

### ERD
![image](https://github.com/charon-aesther/aesther-discord-bot/assets/58246682/8ddbcd0e-a418-42e1-9808-0adc4ed77b25)
<br/>
<br/>

### Team 엔티티
```java
package study.datajpa.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"})
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public Member(String username) {
        this.username = username;
    }

    public Member(String username, int age) {
        this.username = username;
        this.age = age;
    }

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if (team != null) {
            this.team = team;
        }

    }

    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
```
* 롬복 설명
  * @Setter: 실무에서 가급적 Setter는 사용하지 않기
  * @NoArgsConstructor AccessLevel.PROTECTED: 기본 생성자 막고 싶은데, \
    JPA 스팩상 PROTECTED로 열어두어야 함
  * @ToString은 가급적 내부 필드만(연관관계 없는 필드만)
* `changeTeam()` 으로 양방향 연관관계 한번에 처리(연관관계 편의 메소드)
<br/>

### 데이터 확인 테스트
```java
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "name"})
public class Team {

    @Id
    @GeneratedValue
    @Column(name = "team_id")
    private Long id;
    private String name;

    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();

    public Team(String name) {
        this.name = name;
    }
}
```
Member와 Team은 양방향 연관관계이고, Member.team 이 연관관계의 주인이다. \
Team.members 는 연관관계의 주인이 아니므로, \
Member.team 이 데이터베이스 외래키 값을 변경한다.\
반대편은 읽기만 가능하다.
<br/>
<br/>

### Member 엔티티
```java
@SpringBootTest
public class MemberTest {
    @PersistenceContext
    EntityManager em;

    @Test
    @Transactional
    @Rollback(false)
    public void testEntity() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        //초기화
        em.flush();
        em.clear();

        //확인
        List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        for (Member member : members) {
            System.out.println("member=" + member);
            System.out.println("-> member.team=" + member.getTeam());
        }
    }
}
```
가급적, 순수 JPA로 동작을 확인하자.
DB 테이블 결과와, 지연 로딩 동작을 확인할 수 있다.
<br/>
<br/>


## 공통 인터페이스 기능
이제, 공통 인터페이스 기능을 만들어보자.\
그 전에, 우선 순수 JPA 기반 리포지토리를 만들고,\
그 뒤 스프링 데이터 JPA를 사용하자.
<br/>

## 순수 JPA 기반 리포지토리
기본적인 CRUD 기능을 하는 순수한 JPA 기반 리포지토리를 만들어보자.

> JPA에서 수정은 변경감지 기능을 사용하면 된다.
> 트랜잭션 안에서 엔티티를 조회한 다음에 데이터를 변경하면, \
> 트랜잭션 종료 시점에 변경감지 기능이 작동해서 \
> 변경된 엔티티를 감지하고 UPDATE SQL을 실행한다
<br/>

### 순수 JPA 기반 리포지토리 - 회원
```java
@Repository
public class MemberJpaRepository {

    @PersistenceContext
    private EntityManager em;

    // Create
    public Member save(Member member) {
        em.persist(member);
        return member;
    }

    // Read
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public Member find(Long id) {
        return em.find(Member.class, id);
    }

    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(em.find(Member.class, id));
    }

    public long count() {
        return em.createQuery("select count(m) from Member m", Long.class)
                .getSingleResult();
    }

    // Delete
    public void delete(Member member) {
        em.remove(member);
    }
}
```
<br/>

### 순수 JPA 기반 리포지토리 - 팀
```java

@Repository
@RequiredArgsConstructor
public class TeamJpaRepository {

    private final EntityManager em;

    public Team save(Team team) {
        em.persist(team);
        return team;
    }

    public void delete(Team team) {
        em.remove(team);
    }

    public List<Team> findAll() {
        return em.createQuery("select t from Team t", Team.class)
                .getResultList();
    }


    public Team find(Long id) {
        return em.find(Team.class, id);
    }

    public Optional<Team> findById(Long id) {
        return Optional.ofNullable(em.find(Team.class, id));
    }

    public long count() {
        return em.createQuery("select count(t) from Team t", Long.class)
                .getSingleResult();
    }
}
```
회원 리포지토리와 거의 동일한걸 볼 수 있다.
<br/>
<br/>

### 순수 JPA 기반 리포지토리 테스트
```java
@SpringBootTest
@Transactional
//@Rollback(value = false)
class MemberJpaRepositoryTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
        Member findMember2 = memberJpaRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> members = memberJpaRepository.findAll();
        assertThat(members.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberJpaRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);

        long deletedCount = memberJpaRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }
}
```
기본적인 CRUD를 검증한다.
<br/>
<br/>

## 공통 인터페이스 설정
### JavaConfig 설정
```
@Configuration
@EnableJpaRepositories(basePackages = "jpabook.jpashop.repository")
public class AppConfig {}
```
스프링 부트 사용시 생략 가능하다.\
스프링 부트 사용시 @SpringBootApplication 위치를 지정한다.\
(해당 패키지와 하위 패키지 인식)

만약 위치가 달라지면 @EnableJpaRepositories가 필요하다.
<br/>
<br/>

### 스프링 데이터 JPA가 구현 클래스 대신 생성
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/dda2d4d9-6bbc-479f-9d06-9af7d967afae)

* `org.springframework.data.repository.Repository`를 구현한 클래스는 스캔의 대상이 된다.
  * MemberRepository 인터페이스가 동작한 이유
  * 실제 출력해보기(Proxy)
  * memberRepository.getClass() class com.sun.proxy.$ProxyXXX
* @Repository 애노테이션 생략 가능
  * 컴포넌트 스캔을 스프링 데이터 JPA가 자동으로 처리
  * JPA 예외를 스프링 예외로 변환하는 과정도 자동으로 처리
<br/>

## 공통 인터페이스 적용
이제, 순수 JPA로 구현한 `MemberJpaRepository` 대신,\
스프링 데이터 JPA가 제공하는 공통 인터페이스를 사용해보자.
<br/>
<br/>

### 스프링 데이터 JPA 기반 MemberRepository
```java
public interface MemberRepository extends JpaRepository<Member, Long> {
}
```
<br/>

### MemberRepository 테스트
```java
@SpringBootTest
@Transactional
//@Rollback(value = false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    void testMember() {
        Member member = new Member("username");
        Member savedMember = memberRepository.save(member);

        // 원랜, Optional이 NULL이면 NoSuchElementException이 터지므로, orElseThrow를 쓰던가 해야한다.
        Member findMember = memberRepository.findById(member.getId()).get();

        assertThat(findMember).isEqualTo(member);
    }


    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> members = memberRepository.findAll();
        assertThat(members.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }
}
```
기존 순수 JPA 기반 테스트에서 사용했던 코드를 그대로 \
스프링 데이터 JPA 리포지토리 기반 테스트로 변경해도 동일한 방식으로 동작한다.
<br/>
<br/>


### TeamRepository 생성
```java
public interface TeamRepository extends JpaRepository<Team, Long> {
}
```
<br/>

## 공통 인터페이스 분석
JpaRepository 인터페이스는 기본적으로, 공통 CRUD를 제공한다.\
제네릭은 <엔티티 타입, 식별자 타입>을 설정해준다.
<br/>
<br/>

### JpaRepository 공통 기능 인터페이스
```java
public interface JpaRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID>
{
    ...
}
```
<br/>

### JpaRepository를 사용하는 인터페이스
```java
public interface MemberRepository extends JpaRepository<Member, Long> {
}
```
<br/>

### 공통 인터페이스 구현
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/3ee60818-31c3-4e28-a90b-67e4adbb1da3)
<br/>
<br/>

#### 주의
최신 버전에선 다음과 같이 변경되었다.
* `T findOne(ID)` -> `Optional<T> findById(ID)` 변경
* `boolean exists(ID)` -> `boolean existsById(ID)` 변경
<br/>

#### 주요 메서드
* `save(S)` : 새로운 엔티티는 저장하고 이미 있는 엔티티는 병합한다.
* `delete(T)` : 엔티티 하나를 삭제한다. \
  내부에서 EntityManager.remove() 호출
* `findById(ID)` : 엔티티 하나를 조회한다. \
  내부에서 EntityManager.find() 호출
* `getOne(ID)` : 엔티티를 프록시로 조회한다. \
  내부에서 EntityManager.getReference() 호출
* `findAll(…)` : 모든 엔티티를 조회한다. \
  정렬( Sort )이나 페이징( Pageable ) 조건을 파라미터로 제공할 수 있다.
<br/>