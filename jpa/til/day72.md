# 실무 활용 - 스프링 데이터 JPA와 Querydsl
## 스프링 데이터 JPA 리포지토리로 변경
### 스프링 데이터 JPA - MemberRepository 생성
```java
public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByUsername(String username);
}
```
<br/>

### 스프링 데이터 JPA 테스트
```java
@Transactional
public class MemberRepositoryTest {


    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void basicTest() {
        //given
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        //when
        Optional<Member> optional = memberRepository.findById(member.getId());
        Member findMember = optional.get();

        List<Member> all = memberRepository.findAll();
        List<Member> byUsername = memberRepository.findByUsername("member1");


        //then
        assertThat(findMember).isEqualTo(member);
        assertThat(all).containsExactly(member);
        assertThat(byUsername).containsExactly(member);
    }
}
```
Querydsl 전용 기능인 회원 search를 작성할 수 없다. \
즉, 사용자 정의 리포지토리 필요.
<br/>
<br/>

## 사용자 정의 리포지토리
### 사용자 정의 리포지토리 사용법
1. 사용자 정의 인터페이스 작성
2. 사용자 정의 인터페이스 구현
3. 스프링 데이터 리포지토리에 사용자 정의 인터페이스 상속
<br/>

### 사용자 정의 리포지토리 구성
![image](https://github.com/jub3907/Spring-study/assets/58246682/c140b9f9-0a82-44ab-9dd4-cda2d12301ef)
<br/>
<br/>

### 1. 사용자 정의 인터페이스 작성
```java
public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
}
```
<br/>

### 2. 사용자 정의 인터페이스 구현
```java
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}
```
<br/>

### 3. 스프링 데이터 리포지토리에 사용자 정의 인터페이스 상속
```java
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    List<Member> findByUsername(String username);
}
```
<br/>

### 커스텀 리포지토리 동작 테스트 추가
```java
@Test
public void searchTest() {
    //given
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

    MemberSearchCondition condition = new MemberSearchCondition();
    condition.setAgeGoe(35);
    condition.setAgeLoe(40);
    condition.setTeamName("teamB");

    //when
    List<MemberTeamDto> result = memberRepository.search(condition);

    //then
    assertThat(result).extracting("username").containsExactly("member4");

}
```
<br/>

## 스프링 데이터 페이징 활용1 - Querydsl 페이징 연동
* 스프링 데이터의 Page, Pageable을 활용해보자.
* 전체 카운트를 한번에 조회하는 단순한 방법
* 데이터 내용과 전체 카운트를 별도로 조회하는 방법
<br/>

### 사용자 정의 인터페이스에 페이징 추가
```java
// 페이징
Page<MemberTeamDto> searchPage(MemberSearchCondition condition, Pageable pageable);
```
<br/>

### searchPage()
데이터 내용과 전체 카운트를 별도로 조회하는 방법.
```java
@Override
public Page<MemberTeamDto> searchPage(MemberSearchCondition condition, Pageable pageable) {

    List<MemberTeamDto> content = queryFactory
            .select(new QMemberTeamDto(
                    member.id.as("memberId"),
                    member.username,
                    member.age,
                    team.id.as("teamId"),
                    team.name.as("teamName")
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                    usernameEq(condition.getUsername()),
                    teamNameEq(condition.getTeamName()),
                    ageGoe(condition.getAgeGoe()),
                    ageLoe(condition.getAgeLoe())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long total = count();

    return new PageImpl<>(content, pageable, total);
}

public Long count() {
    return queryFactory
            //.select(Wildcard.count) //select count(*)
            .select(member.count()) //select count(member.id)
            .from(member)
            .fetchOne();
}

```
전체 카운트를 조회 하는 방법을 최적화 할 수 있으면 이렇게 분리하면 된다. \
(예를 들어서 전체 카운트를 조회할 때 \
조인 쿼리를 줄일 수 있다면 상당한 효과가 있다.)

코드를 리펙토링해서 내용 쿼리과 전체 카운트 쿼리를 읽기 좋게 분리하면 좋다.
<br/>

### 테스트
```java
@Test
public void searchPageSimpleTest() {
    //given
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

    MemberSearchCondition condition = new MemberSearchCondition();

    PageRequest pageRequest = PageRequest.of(0, 3);

    //when
    Page<MemberTeamDto> result = memberRepository.searchPage(condition, pageRequest);

    //then
    assertThat(result.getSize()).isEqualTo(3);
    assertThat(result.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
}
```
<br/>


## 스프링 데이터 페이징 활용2 - CountQuery 최적화
### PageableExecutionUtils.getPage()로 최적화
```java
@Override
public Page<MemberTeamDto> searchPage(MemberSearchCondition condition, Pageable pageable) {

    List<MemberTeamDto> content = queryFactory
            .select(new QMemberTeamDto(
                    member.id.as("memberId"),
                    member.username,
                    member.age,
                    team.id.as("teamId"),
                    team.name.as("teamName")
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                    usernameEq(condition.getUsername()),
                    teamNameEq(condition.getTeamName()),
                    ageGoe(condition.getAgeGoe()),
                    ageLoe(condition.getAgeLoe())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    return PageableExecutionUtils.getPage(content, pageable, this::count);
}

public Long count() {
    return queryFactory
            //.select(Wildcard.count) //select count(*)
            .select(member.count()) //select count(member.id)
            .from(member)
            .fetchOne();
}
```
* 스프링 데이터 라이브러리가 제공한다.
* count 쿼리가 생략 가능한 경우, 생략해서 처리한다.
    * 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
    * 마지막 페이지 일 때 \
        (offset + 컨텐츠 사이즈를 더해서 전체 사이즈 구함, \
        더 정확히는 마지막 페이지이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때)
<br/>

## 스프링 데이터 페이징 활용3 - 컨트롤러 개발
### 실제 컨트롤러
```java
private final MemberRepository memberRepository;

// ...

@GetMapping("/v2/members")
public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
    return memberRepository.searchPage(condition, pageable);
}
```
* http://localhost:8080/v2/members?size=5&page=2
<br/>

## 스프링 데이터 정렬(Sort)
스프링 데이터 JPA는 자신의 정렬(Sort)을 \
Querydsl의 정렬(OrderSpecifier)로 편리하게 변경하는 기능을 제공한다. \
이 부분은 뒤에 스프링 데이터 JPA가 제공하는 Querydsl 기능에서 살펴보겠다.

스프링 데이터의 정렬을 Querydsl의 정렬로 직접 전환하는 방법은 다음 코드를 참고하자.
<br/>
<br/>

### 스프링 데이터 sort -> Querydsl의 OrderSpecifier로 변환
```java
JPAQuery<Member> query = queryFactory
        .selectFrom(member);
for (Sort.Order o : pageable.getSort()) {
    PathBuilder pathBuilder = new PathBuilder(member.getType(), member.getMetadata());
    query.orderBy(new OrderSpecifier(
        o.isAscending() ? Order.ASC : Order.DESC,
        pathBuilder.get(o.getProperty())));
}

List<Member> result = query.fetch();
```
정렬( Sort )은 조건이 조금만 복잡해져도 Pageable 의 Sort 기능을 사용하기 어렵다. \
루트 엔티티 범위를 넘어가는 동적 정렬 기능이 필요하면 \
스프링 데이터 페이징이 제공하는 Sort 를 사용하기 보다는 \
파라미터를 받아서 직접 처리하는 것을 권장한다.
<br/>


# 스프링 데이터 JPA가 제공하는 Querydsl 기능
여기서 소개하는 기능은 제약이 커서 복잡한 실무 환경에서 사용하기에는 많이 부족하다. \
그래도 스프링 데이터에서 제공하는 기능이므로 간단히 소개하고, \
왜 부족한지 설명하겠다.
<br/>
<br/>

## 인터페이스 지원 - QuerydslPredicateExecutor
* 공식 URL: https://docs.spring.io/spring-data/jpa/docs/2.2.3.RELEASE/reference/html/#core.extensions.querydsl
<br/>
<br/>

### QuerydslPredicateExecutor 인터페이스
```java
public interface QuerydslPredicateExecutor<T> {
    Optional<T> findById(Predicate predicate);
    Iterable<T> findAll(Predicate predicate);
    long count(Predicate predicate);
    boolean exists(Predicate predicate);
    // … more functionality omitted.
}
```
<br/>

### 리포지토리에 적용
```java
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom, QuerydslPredicateExecutor<Member> {
    List<Member> findByUsername(String username);
}
```

```java
@Test
public void querydslPredicateExecutorTest() {
    Member member1 = new Member("member1", 10);
    Member member2 = new Member("member2", 20);
    Member member3 = new Member("member3", 30);
    Member member4 = new Member("member4", 40);

    em.persist(member1);
    em.persist(member2);
    em.persist(member3);
    em.persist(member4);

    QMember member = QMember.member;
    Iterable<Member> result = memberRepository.findAll(member.age.between(10, 40).and(member.username.eq("member1")));

    for (Member member5 : result) {
        System.out.println("member5 = " + member5);
    }
}
```
<br/>

### 한계점
* 조인X (묵시적 조인은 가능하지만 left join이 불가능하다.)
* 클라이언트가 Querydsl에 의존해야 한다. \
    서비스 클래스가 Querydsl이라는 구현 기술에 의존해야 한다.
* 복잡한 실무환경에서 사용하기에는 한계가 명확하다.

> 참고: QuerydslPredicateExecutor 는 Pagable, Sort를 모두 지원하고 정상 동작한다.
<br/>

## Querydsl Web 지원
* 공식 URL: https://docs.spring.io/spring-data/jpa/docs/2.2.3.RELEASE/reference/html/#core.web.type-safe

### 동작 방식
```java
@Controller
class UserController {

  @Autowired UserRepository repository;

  @RequestMapping(value = "/", method = RequestMethod.GET)
  String index(Model model, @QuerydslPredicate(root = User.class) Predicate predicate,    
          Pageable pageable, @RequestParam MultiValueMap<String, String> parameters) {

    model.addAttribute("users", repository.findAll(predicate, pageable));

    return "index";
  }
}
```

```
?firstname=Dave&lastname=Matthews
```
->
```
QUser.user.firstname.eq("Dave").and(QUser.user.lastname.eq("Matthews"))
```
<br/>


### 한계점
* 단순한 조건만 가능
* 조건을 커스텀하는 기능이 복잡하고 명시적이지 않음
* 컨트롤러가 Querydsl에 의존
* 복잡한 실무환경에서 사용하기에는 한계가 명확
<br/>

## 리포지토리 지원 - QuerydslRepositorySupport
### 장점
* getQuerydsl().applyPagination() 스프링 데이터가 제공하는 페이징을 \
    Querydsl로 편리하게 변환가능(단! Sort는 오류발생)
* from() 으로 시작 가능\
    (최근에는 QueryFactory를 사용해서 select() 로 시작하는 것이 더 명시적)
* EntityManager 제공
<br/>

### 한계
* Querydsl 3.x 버전을 대상으로 만듬
* Querydsl 4.x에 나온 JPAQueryFactory로 시작할 수 없음
    * select로 시작할 수 없음 (from으로 시작해야함)
* QueryFactory 를 제공하지 않음
* 스프링 데이터 Sort 기능이 정상 동작하지 않음
<br/>

## Querydsl 지원 클래스 직접 만들기
스프링 데이터가 제공하는 QuerydslRepositorySupport가 \
지닌 한계를 극복하기 위해 직접 Querydsl 지원 클래스를 만들어보자.
<br/>
<br/>

### 장점
* 스프링 데이터가 제공하는 페이징을 편리하게 변환
* 페이징과 카운트 쿼리 분리 가능
* 스프링 데이터 Sort 지원
* select() , selectFrom() 으로 시작 가능
* EntityManager , QueryFactory 제공
<br/>

### Querydsl4RepositorySupport
```java
/**
 * Querydsl 4.x 버전에 맞춘 Querydsl 지원 라이브러리
 *
 * @author Younghan Kim
 * @see org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
 */
@Repository
public abstract class Querydsl4RepositorySupport {

    private final Class domainClass;
    private Querydsl querydsl;
    private EntityManager entityManager;
    private JPAQueryFactory queryFactory;

    public Querydsl4RepositorySupport(Class<?> domainClass) {
        Assert.notNull(domainClass, "Domain class must not be null!");
        this.domainClass = domainClass;
    }

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        Assert.notNull(entityManager, "EntityManager must not be null!");

        JpaEntityInformation entityInformation = JpaEntityInformationSupport.getEntityInformation(domainClass, entityManager);
        SimpleEntityPathResolver resolver = SimpleEntityPathResolver.INSTANCE;
        EntityPath path = resolver.createPath(entityInformation.getJavaType());

        this.entityManager = entityManager;
        this.querydsl = new Querydsl(entityManager, new PathBuilder<>(path.getType(), path.getMetadata()));
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @PostConstruct
    public void validate() {
        Assert.notNull(entityManager, "EntityManager must not be null!");
        Assert.notNull(querydsl, "Querydsl must not be null!");
        Assert.notNull(queryFactory, "QueryFactory must not be null!");
    }

    protected JPAQueryFactory getQueryFactory() {
        return queryFactory;
    }

    protected Querydsl getQuerydsl() {
        return querydsl;
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    protected <T> JPAQuery<T> select(Expression<T> expr) {
        return getQueryFactory().select(expr);
    }

    protected <T> JPAQuery<T> selectFrom(EntityPath<T> from) {
        return getQueryFactory().selectFrom(from);
    }

    protected <T> Page<T> applyPagination(Pageable pageable, Function<JPAQueryFactory, JPAQuery> contentQuery) {
        JPAQuery jpaQuery = contentQuery.apply(getQueryFactory());
        List<T> content = getQuerydsl().applyPagination(pageable, jpaQuery).fetch();

        return PageableExecutionUtils.getPage(content, pageable, jpaQuery::fetchCount);
    }

    protected <T> Page<T> applyPagination(Pageable pageable,
                                          Function<JPAQueryFactory, JPAQuery> contentQuery,
                                          Function<JPAQueryFactory, JPAQuery> countQuery) {

        JPAQuery jpaContentQuery = contentQuery.apply(getQueryFactory());
        List<T> content = getQuerydsl().applyPagination(pageable, jpaContentQuery).fetch();
        JPAQuery countResult = countQuery.apply(getQueryFactory());

        return PageableExecutionUtils.getPage(content, pageable, countResult::fetchCount);
    }
}
```
<br/>

### Querydsl4RepositorySupport 사용 코드
```java
@Repository
public class MemberTestRepository extends Querydsl4RepositorySupport {

    public MemberTestRepository() {
        super(Member.class);
    }

    public List<Member> basicSelect() {
        return select(member)
                .from(member)
                .fetch();
    }

    public List<Member> basicSelectFrom() {
        return selectFrom(member)
                .fetch();
    }

    public Page<Member> applyPagination1(MemberSearchCondition condition, Pageable pageable) {
        return applyPagination(pageable, query -> query
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                ));
    }


    public Page<Member> applyPagination2(MemberSearchCondition condition, Pageable pageable) {
        return applyPagination(pageable,
                contentQuery -> contentQuery
                        .selectFrom(member)
                        .leftJoin(member.team, team)
                        .where(
                                usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        ),
                countQuery -> countQuery
                        .select(member.id)
                        .from(member)
                        .leftJoin(member.team, team)
                        .where(

                                usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        ));
    }


    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}

```
<br/>