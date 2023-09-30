# 스프링 데이터 JPA 분석
## 스프링 데이터 JPA 구현체 분석
* 스프링 데이터 JPA가 제공하는 공통 인터페이스의 구현체
* org.springframework.data.jpa.repository.support.SimpleJpaRepository
<br/>

### 리스트 12.31 SimpleJpaRepository
```java
@Repository
@Transactional(readOnly = true)
public class SimpleJpaRepository<T, ID> ...{
    @Transactional
    public <S extends T> S save(S entity) {
        if (entityInformation.isNew(entity)) {
            em.persist(entity);
            return entity;
        } else {
            return em.merge(entity);
        }
    }
    ...
}
```
* @Repository 적용: JPA 예외를 스프링이 추상화한 예외로 변환
* @Transactional 트랜잭션 적용
  * JPA의 모든 변경은 트랜잭션 안에서 동작한다.\
    스프링 데이터 JPA는 변경(등록, 수정, 삭제) 메서드를 트랜잭션 처리한다.\
    서비스 계층에서 트랜잭션을 시작하지 않으면 리파지토리에서 트랜잭션 시작한다.\
    서비스 계층에서 트랜잭션을 시작하면 리파지토리는 해당 트랜잭션을 전파 받아서 사용\
    그래서 스프링 데이터 JPA를 사용할 때 트랜잭션이 없어도 \
    데이터 등록, 변경이 가능했다.\
    (사실은 트랜잭션이 리포지토리 계층에 걸려있는 것임)

* @Transactional(readOnly = true)
  * 데이터를 단순히 조회만 하고 변경하지 않는 트랜잭션에서 \
  readOnly = true 옵션을 사용하면 플러시를 생략해서 \
  약간의 성능 향상을 얻을 수 있다.
  * 자세한 내용은 JPA 책 15.4.2 읽기 전용 쿼리의 성능 최적화 참고
<br/>
<br/>

### 매우 중요!!!
* save() 메서드
  * 새로운 엔티티면 저장( persist )
  * 새로운 엔티티가 아니면 병합( merge )
<br/>

## 새로운 엔티티를 구별하는 방법
### 매우 중요!!!
* save() 메서드
  * 새로운 엔티티면 저장( persist )
  * 새로운 엔티티가 아니면 병합( merge )
<br/>

### 새로운 엔티티를 판단하는 기본 전략
* 식별자가 객체일 때 null 로 판단한다.
* 식별자가 자바 기본 타입일 때 0 으로 판단
* Persistable 인터페이스를 구현해서 판단 로직 변경 가능
<br/>

### Persistable
```java
package org.springframework.data.domain;
public interface Persistable<ID> {
    ID getId();
    boolean isNew();
}
```
참고: JPA 식별자 생성 전략이 @GenerateValue 면 save() 호출 시점에 \
식별자가 없으므로 새로운 엔티티로 인식해서 정상 동작한다. \
그런데 JPA 식별자 생성 전략이 @Id 만 사용해서 직접 할당이면 \
이미 식별자 값이 있는 상태로 save() 를 호출한다.\
따라서 이 경우 merge() 가 호출된다. 
   
merge() 는 우선 DB를 호출해서 값을 확인하고, \
DB에 값이 없으면 새로운 엔티티로 인지하므로 매우 비효율 적이다. \
따라서 Persistable 를 사용해서 새로운 엔티티 확인 여부를 \
직접 구현하는게 효과적이다.

참고로 등록시간( @CreatedDate )을 조합해서 사용하면 \
이 필드로 새로운 엔티티 여부를 편리하게 확인할수 있다. \
(@CreatedDate에 값이 없으면 새로운 엔티티로 판단)
<br/>
<br/>

### Persistable 구현
```java
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item implements Persistable<String> {
    @Id
    private String id;

    @CreatedDate
    private LocalDateTime createdDate;


    public Item(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return createdDate == null;
    }
}
```
<br/>

## 나머지 기능들
### Specifications (명세)
책 도메인 주도 설계(Domain Driven Design)는 SPECIFICATION(명세)라는 개념을 소개한다.\
스프링 데이터 JPA는 JPA Criteria를 활용해서 이 개념을 사용할 수 있도록 지원
<br/>
<br/>

### 술어(predicate)
* 참 또는 거짓으로 평가
* AND OR 같은 연산자로 조합해서 다양한 검색조건을 쉽게 생성(컴포지트 패턴)
  * 예) 검색 조건 하나하나
* 스프링 데이터 JPA는 org.springframework.data.jpa.domain.Specification 클래스로 정의
<br/>

### 명세 기능 사용 방법
#### JpaSpecificationExecutor 인터페이스 상속
```java
public interface MemberRepository extends JpaRepository<Member, Long>,
    JpaSpecificationExecutor<Member> {
}
```
<br/>

#### JpaSpecificationExecutor 인터페이스
```java
public interface JpaSpecificationExecutor<T> {
    Optional<T> findOne(@Nullable Specification<T> spec);
    List<T> findAll(Specification<T> spec);
    Page<T> findAll(Specification<T> spec, Pageable pageable);
    List<T> findAll(Specification<T> spec, Sort sort);
    long count(Specification<T> spec);
}
```
Specification 을 파라미터로 받아서 검색 조건으로 사용한다.
<br/>
<br/>

### 명세 사용 코드
```java
@Test
public void specBasic() throws Exception {
    //given
    Team teamA = new Team("teamA");
    em.persist(teamA);

    Member m1 = new Member("m1", 0, teamA);
    Member m2 = new Member("m2", 0, teamA);
    em.persist(m1);
    em.persist(m2);

    em.flush();
    em.clear();

    //when
    Specification<Member> spec = MemberSpec.username("m1").and(MemberSpec.teamName("teamA"));
    List<Member> result = memberRepository.findAll(spec);

    //then
    Assertions.assertThat(result.size()).isEqualTo(1);
}
```
* Specification 을 구현하면 명세들을 조립할 수 있음. \
  where() , and() , or() , not() 제공
* findAll 을 보면 회원 이름 명세( username )와 \
  팀 이름 명세( teamName )를 and 로 조합해서 검색 조건으로 사용
<br/>

###  MemberSpec 명세 정의 코드
```java
public class MemberSpec {
    public static Specification<Member> teamName(final String teamName) {
        return (Specification<Member>) (root, query, builder) -> {
            if (StringUtils.isEmpty(teamName)) {
                return null;
            }
            
            Join<Member, Team> t = root.join("team", JoinType.INNER); //회원과 조            인
            return builder.equal(t.get("name"), teamName);
        };
    }
    
    public static Specification<Member> username(final String username) {
        return (Specification<Member>) (root, query, builder) ->
                builder.equal(root.get("username"), username);
    }
}
```
* 명세를 정의하려면 Specification 인터페이스를 구현
* 명세를 정의할 때는 toPredicate(...) 메서드만 구현하면 되는데 \
  JPA Criteria의 `Root`, `CriteriaQuery`, \
  `CriteriaBuilder` 클래스를 파라미터 제공
* 예제에서는 편의상 람다를 사용
> 참고: 실무에서는 JPA Criteria를 거의 안쓴다! 대신에 QueryDSL을 사용하자.
<br/>

## Query By Example
https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#query-by-example

```java
@Test
public void queryByExample() {
    Team team = new Team("teamA");
    teamRepository.save(team);

    Member member1 = new Member("member1", 10, team);
    memberRepository.save(member1);

    Member member2 = new Member("member2", 20, team);
    memberRepository.save(member2);

    em.flush();
    em.clear();

      // when
    // Probe
    Member member = new Member("member1");
    member.setTeam(new Team("teamA"));
    // Exclude 설정
    ExampleMatcher matcher = ExampleMatcher.matching()
            .withIgnorePaths("age");

    Example<Member> example = Example.of(member, matcher);

    // Example을 사용해서 탐색
    List<Member> members = memberRepository.findAll(example);

    assertThat(members.get(0).getUsername()).isEqualTo("member1");
}
```
* Probe: 필드에 데이터가 있는 실제 도메인 객체
* ExampleMatcher: 특정 필드를 일치시키는 상세한 정보 제공, 재사용 가능
* Example: Probe와 ExampleMatcher로 구성, 쿼리를 생성하는데 사용
<br/>

### 장점
* 동적 쿼리를 편리하게 처리
* 도메인 객체를 그대로 사용
* 데이터 저장소를 RDB에서 NOSQL로 변경해도 코드 변경이 없게 추상화 되어 있음
* 스프링 데이터 JPA JpaRepository 인터페이스에 이미 포함
<br/>

### 단점
* 조인은 가능하지만 내부 조인(INNER JOIN)만 가능함 외부 조인(LEFT JOIN) 안됨
* 다음과 같은 중첩 제약조건 안됨
  * firstname = ?0 or (firstname = ?1 and lastname = ?2)
* 매칭 조건이 매우 단순함
  * 문자는 starts/contains/ends/regex
  * 다른 속성은 정확한 매칭( = )만 지원
<br/>

### 정리
실무에서 사용하기에는 매칭 조건이 너무 단순하고, LEFT 조인이 안된다.\
실무에서는 QueryDSL을 사용하자
<br/>

## Projections
https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#projections

엔티티 대신에 DTO를 편리하게 조회할 때 사용한다.\
전체 엔티티가 아니라 만약 회원 이름만 딱 조회하고 싶으면?
```java
public interface UsernameOnly {
    String getUsername();
}
```
* 조회할 엔티티의 필드를 getter 형식으로 지정하면 \
  해당 필드만 선택해서 조회(Projection)
  
```java
public interface MemberRepository ... {
    List<UsernameOnly> findProjectionsByUsername(String username);
}
```
* 메서드 이름은 자유, 반환 타입으로 인지

```java
@Test
public void projections() {
    Team team = new Team("teamA");
    teamRepository.save(team);

    Member member1 = new Member("member1", 10, team);
    memberRepository.save(member1);

    Member member2 = new Member("member2", 20, team);
    memberRepository.save(member2);

    em.flush();
    em.clear();

    // when
    List<UsernameOnly> result = memberRepository.findProjectionsByUsername("member1");
    for (UsernameOnly usernameOnly : result) {
        System.out.println("usernameOnly = " + usernameOnly);
    }
}
```

```sql
select
    member0_.username as col_0_0_ 
from
    member member0_ 
where
    member0_.username=?
```
SQL에서도 select절에서 username만 조회(Projection)하는 것을 확인
<br/>
<br/>

### 인터페이스 기반 Closed Projections
프로퍼티 형식(getter)의 인터페이스를 제공하면, \
구현체는 스프링 데이터 JPA가 제공
```java
public interface UsernameOnly {
    String getUsername();
}
```
<br/>

### 인터페이스 기반 Open Proejctions
다음과 같이 스프링의 SpEL 문법도 지원
```java
public interface UsernameOnly {
    @Value("#{target.username + ' ' + target.age + ' ' + target.team.name}")
    String getUsername();
}
```
단! 이렇게 SpEL문법을 사용하면, \
DB에서 엔티티 필드를 다 조회해온 다음에 계산한다! \
따라서 JPQL SELECT 절 최적화가 안된다.
<br/>

### 클래스 기반 Projection
다음과 같이 인터페이스가 아닌 구체적인 DTO 형식도 가능하다.

생성자의 파라미터 이름으로 매칭
```java
public class UsernameOnlyDto {

    private final String username;

    public UsernameOnlyDto(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}

```
<br/>

### 동적 Projections
다음과 같이 Generic type을 주면, 동적으로 프로젝션 데이터 번경 가능하다.
```java
<T> List<T> findProjectionsByUsername(String username, Class<T> type);
```
<br/>

### 사용코드
```java
List<UsernameOnly> result = memberRepository.findProjectionsByUsername("m1", UsernameOnly.class);
```
<br/>

### 중첩 구조 처리
```java
public interface NestedClosedProjection {
    String getUsername();
    TeamInfo getTeam();

    interface TeamInfo {
        String getName();
    }
}
```
<br/>

```sql
select
    m.username as col_0_0_,
    t.teamid as col_1_0_,
    t.teamid as teamid1_2_,
    t.name as name2_2_
from
    member m
left outer join
    team t
    on m.teamid=t.teamid
where
    m.username=?

```
<br/>

### 주의
* 프로젝션 대상이 root 엔티티면, JPQL SELECT 절 최적화 가능하다.
* 프로젝션 대상이 ROOT가 아니면 
  * LEFT OUTER JOIN 처리
  * 모든 필드를 SELECT해서 엔티티로 조회한 다음에 계산
<br/>


### 정리
* 프로젝션 대상이 root 엔티티면 유용하다.
* 프로젝션 대상이 root 엔티티를 넘어가면 JPQL SELECT 최적화가 안된다!
* 실무의 복잡한 쿼리를 해결하기에는 한계가 있다.
* 실무에서는 단순할 때만 사용하고, 조금만 복잡해지면 QueryDSL을 사용하자
<br/>

## 네이티브 쿼리
가급적 네이티브 쿼리는 사용하지 않는게 좋다. 정말 어쩔 수 없을 때 사용하자.\
최근에 나온 궁극의 방법 스프링 데이터 Projections를 활용하는 것이다.
<br/>

### 스프링 데이터 JPA 기반 네이티브 쿼리
* 페이징 지원
* 반환 타입
  * Object[]
  * Tuple
  * DTO(스프링 데이터 인터페이스 Projections 지원)
* 제약
  * Sort 파라미터를 통한 정렬이 정상 동작하지 않을 수 있음(믿지 말고 직접 처리)
  * JPQL처럼 애플리케이션 로딩 시점에 문법 확인 불가
  * 동적 쿼리 불가
<br/>

### JPA 네이티브 SQL 지원
```java
public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query(value = "select * from member where username = ?", nativeQuery = true)
    Member findByNativeQuery(String username);
}
```
* JPQL은 위치 기반 파리미터를 1부터 시작하지만 네이티브 SQL은 0부터 시작
* 네이티브 SQL을 엔티티가 아닌 DTO로 변환은 하려면
  * DTO 대신 JPA TUPLE 조회
  * DTO 대신 MAP 조회
  * @SqlResultSetMapping 복잡
  * Hibernate ResultTransformer를 사용해야함 복잡
  * https://vladmihalcea.com/the-best-way-to-map-a-projection-query-to-a-dto-with-jpa-and-hibernate/
  * 네이티브 SQL을 DTO로 조회할 때는 JdbcTemplate or myBatis 권장
<br/>

### Projections 활용
예) 스프링 데이터 JPA 네이티브 쿼리 + 인터페이스 기반 Projections 활용
```java
@Query(value = "SELECT m.member_id as id, m.username, t.name as teamName " +
            "FROM member m left join team t ON m.team_id = t.team_id",
            countQuery = "SELECT count(*) from member",
            nativeQuery = true)
Page<MemberProjection> findByNativeProjection(Pageable pageable);
```
<br/>

### 동적 네이티브 쿼리
* 하이버네이트를 직접 활용
* 스프링 JdbcTemplate, myBatis, jooq같은 외부 라이브러리 사용

예) 하이버네이트 기능 사용
```java
//given
String sql = "select m.username as username from member m";
List<MemberDto> result = em.createNativeQuery(sql)
          .setFirstResult(0)
          .setMaxResults(10)
          .unwrap(NativeQuery.class)
          .addScalar("username")
          .setResultTransformer(Transformers.aliasToBean(MemberDto.class))
          .getResultList();
}
```
<br/>
<br/>
