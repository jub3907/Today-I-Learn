## 영속성 관리
### JPA에서 가장 중요한 2가지
* 객체와 관계형 데이터베이스 매핑하기 (Object Relational Mapping)
* 영속성 컨텍스트

### 매니저 팩토리와 엔티티 매니저
![image](https://github.com/jub3907/Spring-study/assets/58246682/31c92318-5ab9-465c-b296-86e2b7d34187)
<br/>
<br/>

### 영속성 컨텍스트
JPA를 이해하는데 가장 중요한 용어로, **엔티티를 영구 저장하는 환경**이라는 뜻이다.\
```
EntityManager.persist(entity)
```
persist -> DB에 저장이 아닌, 엔티티를 영속성 컨텍스트에 저장한다.\
영속성 컨텍스트는 논리적인 개념으로, 눈에 보이지 않는다.\
엔티티 매니저를 통해 영속성 컨텍스트에 접근한다.
<br/>
<br/>

### 엔티티의 생명주기
* 비영속 (new/transient)
  * 영속성 컨텍스트와 전혀 관계가 없는 새로운 상태
* 영속 (managed)
  * 영속성 컨텍스트에 관리되는 상태
* 준영속 (detached)
  * 영속성 컨텍스트에 저장되었다가 분리된 상태
* 삭제 (removed)
  * 삭제된 상태
<br/>

#### 비영속
멤버 객체를 생성하고, 값을 넣기만 한 상태.
```java
//객체를 생성한 상태(비영속)
Member member = new Member();
member.setId("member1");
member.setUsername("회원1");
```
<br/>
<br/>

#### 영속
멤버 객체를 생성한 뒤, 영속성 컨텍스트에 멤버 객체를 넣어주어 \
영속성 컨텍스트에 의해 관리되는 상태.
```java
Member member = new Member();
member.setId(1L);
member.setName("kim");
em.persist(member);
```
<br/>

#### 준영속, 삭제
```java
//회원 엔티티를 영속성 컨텍스트에서 분리, 준영속 상태
em.detach(member);

//객체를 삭제한 상태(삭제)
em.remove(member);
```
<br/>

### 영속성 컨텍스트의 이점
* 1차 캐시
* 동일성(identity) 보장
* 트랜잭션을 지원하는 쓰기 지연(transactional write-behind)
* 변경 감지(Dirty Checking)
* 지연 로딩(Lazy Loading)
<br/>

#### 엔티티 조회, 1차 캐시

![image](https://github.com/jub3907/Spring-study/assets/58246682/77da8333-fb33-4fb3-9878-23e76dc8fc74)

```java
Member member = new Member();
member.setId("member1");
member.setUsername("회원1");

//1차 캐시에 저장됨
em.persist(member);

//1차 캐시에서 조회
Member findMember = em.find(Member.class, "member1");
```

#### 데이터베이스에서 조회하는 경우
```java
Member findMember2 = em.find(Member.class, "member2");
```

![image](https://github.com/jub3907/Spring-study/assets/58246682/4ff23856-1223-48db-861c-eb5493d6bf57)
<br/>
<br/>

#### 영속 엔티티의 동일성 보장
```java
Member a = em.find(Member.class, "member1");
Member b = em.find(Member.class, "member1");

System.out.println(a == b); //동일성 비교 true
```
1차 캐시로 반복 가능한 읽기(REPEATABLE READ) 등급의 트랜잭션 격리 수준을 \
데이터베이스가 아닌 애플리케이션 차원에서 제공
<br/>

#### 엔티티 등록
트랜잭션을 지원하는 쓰기 지연
```java
EntityManager em = emf.createEntityManager();
EntityTransaction transaction = em.getTransaction();
//엔티티 매니저는 데이터 변경시 트랜잭션을 시작해야 한다.
transaction.begin(); // [트랜잭션] 시작

em.persist(memberA);
em.persist(memberB);
//여기까지 INSERT SQL을 데이터베이스에 보내지 않는다.

//커밋하는 순간 데이터베이스에 INSERT SQL을 보낸다.
transaction.commit(); // [트랜잭션] 커밋
```

* em.persist(memberA);\
  ![image](https://github.com/jub3907/Spring-study/assets/58246682/63dc908e-e59c-4560-b261-208ef466572e)

* em.persist(memberB);
  ![image](https://github.com/jub3907/Spring-study/assets/58246682/1824cb70-c10e-422a-8d84-a5f3dbb46d85)

* transaction.commit();
  ![image](https://github.com/jub3907/Spring-study/assets/58246682/69728ba2-9920-4951-a2f3-293105e470ed)


persist를 사용했을 때, 영속성 컨텍스트 안에 존재하는 SQL 저장소에 \
Insert Query를 생성해 쌓아둔다.\
이렇게 생성된 Query가 commit시점에 DB에 flush되어, 변경사항이 반영된다.

이 때, 우리는 **버퍼링**이라는 기능을 사용할 수 있다.\
Query문을 쌓아서 한번에 커밋하기 때문에, 네트워크 통신 횟수를 줄여\
응답 속도를 증진시킬 수 있다.
<br/>
<br/>

#### 엔티티 수정 - 변경 감지
```java
EntityManager em = emf.createEntityManager();
EntityTransaction transaction = em.getTransaction();
transaction.begin(); // [트랜잭션] 시작

// 영속 엔티티 조회
Member memberA = em.find(Member.class, "memberA");

// 영속 엔티티 데이터 수정
memberA.setUsername("hi");
memberA.setAge(10);

//em.update(member) 이런 코드가 있어야 하지 않을까?

transaction.commit(); // [트랜잭션] 커밋
```

영속 상태의 객체는 자동으로 변경 내역을 탐지해(Dirty Check) Update Query가 생성된다.
<br/>
<br/>

#### 엔티티 삭제
```java
//삭제 대상 엔티티 조회
Member memberA = em.find(Member.class, “memberA");

em.remove(memberA); //엔티티 삭제
```
<br/>

### 플러시
영속성 컨텍스트의 변경내용을 데이터베이스에 반영한다.\
플러시가 발생하면 변경 감지, 수정된 엔티티를 쓰기 지언 저장소에 등록하고,\
쓰기 지연 SQL 저장소의 쿼리를 데이터베이스에 전송한다.

영속성 컨텍스트는 다음과 같은 방법으로 플러시할 수 있다.
* em.flush() - 직접 호출
* 트랜잭션 커밋 - 플러시 자동 호출
* JPQL 쿼리 실행 - 플러시 자동 호출
<br/>

#### JPQL 쿼리 실행시 플러시가 자동으로 호출되는 이유
```java
em.persist(memberA);
em.persist(memberB);
em.persist(memberC);

//중간에 JPQL 실행
query = em.createQuery("select m from Member m", Member.class);
List<Member> members= query.getResultList();
```
위와 같은 상황을 방지하기 위해 자동으로 플러시된다.
<br/>
<br/>

플러시는 영속성 컨텍스트를 비우지 않는다.\
또한, 영속성 컨텍스트의 변경 내용을 데이터 베이스에 동기화 한다.\
트랜잭션이라는 작업 단위가 중요하다. \
커밋 직전에만 동기화하면 되기 때문에, 이러한 매커니즘들이 가능해진다.

### 준영속 상태
영속 상태의 엔티티가 영속성 컨텍스트에서 분리된 상태.\
이 상태에선 영속성 컨텍스트가 제공하는 기능을 사용할 수 없다.
* em.detach(entity)
  * 특정 엔티티만 준영속 상태로 전환
* em.clear()
  * 영속성 컨텍스트를 완전히 초기화
* em.close()
  * 영속성 컨텍스트를 종료
<br/>

## 엔티티 매핑
### @Entity, 객체-테이블 매핑
* 객체와 테이블을 매핑할 때 사용하는 애노테이션.
* @Entity가 붙은 클래스는 JPA가 관리하며, 엔티티라 한다.
* JPA를 사용해서 테이블과 매핑할 클래스는 @Entity 필수
* 주의
  * 기본 생성자 필수(파라미터가 없는 public 또는 protected 생성자)
  * final 클래스, enum, interface, inner 클래스 사용X
  * 저장할 필드에 final 사용 X
<br/>

#### @Entity 속성 정리
* 속성: name
  * JPA에서 사용할 엔티티 이름을 지정한다.
  * 기본값: 클래스 이름을 그대로 사용(예: Member)
  * 같은 클래스 이름이 없으면 가급적 기본값을 사용한다.
<br/>
<br/>

### @Table
* @Table은 엔티티와 매핑할 테이블 지정한다.
* name
  * 매핑할 데이터 이름.
  * 엔티티 이름을 기본값으로 사용한다.
* catalog
  * 데이터베이스 catalog 매핑
* schema
  * 데이터베이스 schema 매핑
* uniqueConstraints
  * DDL 생성 시에 유니크 제약 조건 생성
<br/>

### 데이터베이스 스키마 자동 생성
* DDL을 애플리케이션 실행 시점에 자동 생성
* 테이블 중심 -> 객체 중심
* 데이터베이스 방언을 활용해서 데이터베이스에 맞는 적절한DDL 생성
* 이렇게 **생성된 DDL은 개발 장비에서만 사용**
* 생성된 DDL은 운영서버에서는 사용하지 않거나, 적절히 다듬은 후 사용
<br/>
<br/>

#### 데이터베이스 스키마 자동 생성 - 속성
**`hibernate.hbm2ddl.auto`**\
* create
  * 기존테이블 삭제 후 다시 생성 (DROP + CREATE)
* create-drop 
  * create와 같으나 종료시점에 테이블 DROP
* update 
  * 변경분만 반영(운영DB에는 사용하면 안됨)
* validate 
  * 엔티티와 테이블이 정상 매핑되었는지만 확인
* none 
  * 사용하지 않음
<br/>
<br/>

#### 데이터베이스 스키마 자동 생성 - 주의
* **운영 장비에는 절대 create, create-drop, update 사용하면 안된다.**
* 개발 초기 단계는 create 또는 update
* 테스트 서버는 update 또는 validate
* 스테이징과 운영 서버는 validate 또는 none
<br/>

#### DDL 생성 기능
* 제약조건 추가: 회원 이름은 필수, 10자 초과X
  * @Column(nullable = false, length = 10)
* 유니크 제약조건 추가
  * `@Table(uniqueConstraints = {@UniqueConstraint( name = "NAME_AGE_UNIQUE", columnNames = {"NAME", "AGE"} )})`
* DDL 생성 기능은 DDL을 자동 생성할 때만 사용되고 JPA의 실행 로직에는 영향을 주지 않는다.
<br/>

### 필드와 컬럼 매핑
다음과 같은 요구사항을 구현한다고 가정해보자.
1. 회원은 일반 회원과 관리자로 구분해야 한다.
2. 회원 가입일과 수정일이 있어야 한다.
3. 회원을 설명할 수 있는 필드가 있어야 한다. 이 필드는 길이 제한이 없다.

이를 엔티티로 구현하면, 다음과 같다.
```java
@Entity
public class Member {

    @Id
    private Long id;

    @Column(name = "name")
    private String username;
    private Integer age;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Lob
    private String description;
}
```
<br/>
<br/>

### 매핑 어노테이션 정리
* @Column
  * 컬럼 매핑
* @Temporal 
  * 날짜 타입 매핑
* @Enumerated
  * enum 타입을 매핑할 때 사용한다.
* @Lob BLOB,
  * CLOB 매핑
* @Transient 
  * 특정 필드를 컬럼에 매핑하지 않음(매핑 무시)
<br/>

#### @Column
|속성|설명|기본값|
|------|---|---|
|name |필드와 매핑할 테이블의 컬럼 이름|객체의 필드 이름|
|insertable,updatable|등록, 변경 가능 여부|TRUE|
|nullable(DDL)|null 값의 허용 여부를 설정한다. false로 설정하면 DDL 생성 시에 not null 제약조건이 붙는다.|테스트3|
|unique(DDL)|@Table의 uniqueConstraints와 같지만 한 컬럼에 간단히 유니크 제약조건을 걸 때 사용한다.|필드의 자바 타입과 방언 정보를 사용|
|columnDefinition(DDL) |데이터베이스 컬럼 정보를 직접 줄 수 있다.ex) varchar(100) default ‘EMPTY|테스트3|
|length(DDL) |문자 길이 제약조건, String 타입에만 사용한다. |255|
|precision, scale(DDL)|BigDecimal 타입에서 사용한다(BigInteger도 사용할 수 있다).precision은 소수점을 포함한 전체 자릿수를, scale은 소수의 자릿수다. 참고로 double, float 타입에는 적용되지 않는다. 아주 큰 숫자나정 밀한 소수를 다루어야 할 때만 사용한다|precision=19,scale=2 |
<br/>

#### @Enumerated
자바 enum 타입을 매핑할 때 사용한다. **반드시 STRING을 사용하자.**
|속성|설명|기본값|
|------|---|---|
|value|EnumType.ORDINAL: enum 순서를 데이터베이스에 저장<br/>EnumType.STRING: enum 이름을 데이터베이스에 저장|EnumType.ORDINAL|
<br/>

#### @Temporal
날짜 타입(java.util.Date, java.util.Calendar)을 매핑할 때 사용

> LocalDate, LocalDateTime을 사용할 때는 생략 가능(최신 하이버네이트 지원)

|속성|설명|기본값|
|------|---|---|
|value|TemporalType.DATE: 날짜, 데이터베이스 date 타입과 매핑<br/>(예: 2013–10–11)<br/>TemporalType.TIME: 시간, 데이터베이스 time 타입과 매핑<br/>(예: 11:11:11)<br/>TemporalType.TIMESTAMP: <br/>날짜와 시간, 데이터베이스 timestamp 타입과 매핑<br/>(예: 2013–10–11 11:11:11)|테스트3|
<br/>

#### @Lob
데이터베이스 BLOB, CLOB 타입과 매핑
* @Lob에는 지정할 수 있는 속성이 없다.
* 매핑하는 필드 타입이 문자면 CLOB 매핑, 나머지는 BLOB 매핑
  * CLOB: String, char[], java.sql.CLOB
  * BLOB: byte[], java.sql. BLOB 
<br/>

#### @Transient
* 필드 매핑X
* 데이터베이스에 저장X, 조회X
* 주로 메모리상에서만 임시로 어떤 값을 보관하고 싶을 때 사용

```java
@Transient
private Integer temp; 
```
<br/>
<br/>

### 기본 키 매핑
* @Id
* @GeneratedValue
```java
@Id @GeneratedValue(strategy = GenerationType.AUTO)
private Long id;
```
<br/>
<br/>

#### 기본 키 매핑 방법
* 직접 할당: @Id만 사용
* 자동 생성(@GeneratedValue)
  * **IDENTITY**: 데이터베이스에 위임, MYSQL
  * **SEQUENCE**: 데이터베이스 시퀀스 오브젝트 사용, ORACLE
    * @SequenceGenerator 필요
  * **TABLE**: 키 생성용 테이블 사용, 모든 DB에서 사용
    * @TableGenerator 필요
  * **AUTO**: 방언에 따라 자동 지정, 기본값
<br/>

#### IDENTITY 전략 - 특징
* 기본 키 생성을 데이터베이스에 위임
* 주로 MySQL, PostgreSQL, SQL Server, DB2에서 사용\
  (예: MySQL의 AUTO_ INCREMENT)
* JPA는 보통 트랜잭션 커밋 시점에 INSERT SQL 실행
* AUTO_ INCREMENT는 데이터베이스에 INSERT SQL을 실행한 이후에 ID 값을 알 수 있음
* IDENTITY 전략은 em.persist() 시점에 즉시 INSERT SQL 실행하고 DB에서 식별자를 조회
```java
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 
```
<br/>

#### SEQUENCE 전략 - 특징
* 데이터베이스 시퀀스는 유일한 값을 순서대로 생성하는 \
  특별한 데이터베이스 오브젝트(예: 오라클 시퀀스)
* 오라클, PostgreSQL, DB2, H2 데이터베이스에서 사용

```java
@Entity
@SequenceGenerator(
    name = “MEMBER_SEQ_GENERATOR",
    sequenceName = “MEMBER_SEQ", //매핑할 데이터베이스 시퀀스 이름
    initialValue = 1,
    allocationSize = 1)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MEMBER_SEQ_GENERATOR")
    private Long id; 
```
단, `@SequenceGenerator`에서 사용하는 allocationSize 속성은\
시퀀스 한 번 호출에 증가하는 수를 의미한다.\
데이터베이스 시퀀스 값이 하나씩 증가하도록 설정되어 있다면,\
이 값을 반드시 1로 설정해야만 한다.
<br/>
<br/>

#### TABLE 전략
* 키 생성 전용 테이블을 하나 만들어서 데이터베이스 시퀀스를 흉내내는 전략
* 장점: 모든 데이터베이스에 적용 가능
* 단점: 성능

```sql
create table MY_SEQUENCES (
    sequence_name varchar(255) not null,
    next_val bigint,
    primary key ( sequence_name )
)
```

```java
@Entity
@TableGenerator(
    name = "MEMBER_SEQ_GENERATOR",
    table = "MY_SEQUENCES",
    pkColumnValue = “MEMBER_SEQ",
    allocationSize = 1)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MEMBER_SEQ_GENERATOR")
    private Long id; 
```
<br/>
<br/>

### 권장하는 식별자 전략
* **기본 키 제약 조건**: null 아님, 유일, 변하면 안된다.
* 미래까지 이 조건을 만족하는 자연키는 찾기 어렵다. 대리키(대체키)를 사용하자.
* 예를 들어 주민등록번호도 기본 키로 적절하기 않다.
* 권장: Long형 + 대체키 + 키 생성전략 사용
<br/>
<br/>

## 연관관계 매핑 기초
* 방향(Direction): 단방향, 양방향
* 다중성(Multiplicity): 다대일(N:1), 일대다(1:N), 일대일(1:1), 다대다(N:M) 이해
* 연관관계의 주인(Owner): 객체 양방향 연관관계는 관리 주인 이 필요
<br/>

### 연관관계가 필요한 이유
객체지향 설계의 목표는 자율적인 객체들의 협력 공동체를 만드는 것이다.
<br/>
<br/>

### 예제 시나리오
* 회원과 팀이 있다.
* 회원은 하나의 팀에만 소속될 수 있다.
* 회원과 팀은 다대일 관계다.

객체를 테이블에 맞추어 모델링 하면, 다음과 같다.\
![image](https://github.com/jub3907/Spring-study/assets/58246682/c6f9898e-f418-4579-9fc2-a3595611e540)
<br/>
<br/>

```java
@Entity
public class Member {
    @Id @GeneratedValue
    private Long id;

    @Column(name = "USERNAME")
    private String name;

    @Column(name = "TEAM_ID")
    private Long teamId;
    …
}

@Entity
public class Team {
    @Id @GeneratedValue
    private Long id;

    private String name;
    …
}

//팀 저장
Team team = new Team();
team.setName("TeamA");
em.persist(team);

//회원 저장
Member member = new Member();
member.setName("member1");
member.setTeamId(team.getId());
em.persist(member);

//조회
Member findMember = em.find(Member.class, member.getId());

//연관관계가 없음
Team findTeam = em.find(Team.class, team.getId());
```
이처럼, 객체를 테이블에 맞추어 데이터 중심으로 모델링하면 협력 관계를 만들 수 없다.\
테이블은 외래 키로 조인을 사용해서 연관된 테이블을 찾는다.\
객체는 참조를 사용해서 연관된 객체를 찾는다.\
테이블과 객체 사이에는 이런 큰 간격이 있다.
<br/>
<br/>

### 단방향 연관관계
![image](https://github.com/jub3907/Spring-study/assets/58246682/9ac07257-eb05-4155-ade0-47fb6fa826b8)

그럼, 이번엔 객체의 참조와 테이블의 외래 키를 매핑해보자.
```java
@Entity
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @Column(name = "USERNAME")
    private String name;

//    @Column(name = "TEAM_ID")
//    private Long teamId;
    @ManyToOne // 멤버 입장에선 members -> team이므로, many to one.
    @JoinColumn(name = "TEAM_ID")
    private Team team;
}

// 팀 저장
Team team = new Team();
team.setName("TeamA");
em.persist(team);

// 회원 저장
Member member = new Member();
member.setName("member1");
member.setTeam(team); //단방향 연관관계 설정, 참조 저장
em.persist(member);

// 회원 조회
Member findMember = em.find(Member.class, member.getId());

// 팀 조회
Team findTeam = findMember.getTeam();

System.out.println("findTeam = " + findTeam);
System.out.println("findMember = " + findMember);
```
<br/>

### 양방향 연관관계
이전, 단방향 연관관계에선 Member -> team은 가능했지만, \
team -> member는 불가능했다.

이를 양방향으로, team <-> member가 가능하도록 수정해보자.\
이번에도 역시, **테이블에는 전혀 변화가 없다.**

![image](https://github.com/jub3907/Spring-study/assets/58246682/05639f51-e687-4ddd-99f5-a11c529b92f5)

```java
@Entity
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @Column(name = "USERNAME")
    private String name;

    @ManyToOne // 멤버 입장에선 members -> team이므로, many to one.
    @JoinColumn(name = "TEAM_ID")
    private Team team;

}

@Entity
public class Team {
    @Id @GeneratedValue
    @Column(name = "team_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "team") // mappedBy -> "무엇과 연결되어 있는가?"
    private List<Member> members = new ArrayList<>();

}

// 팀 저장
Team team = new Team();
team.setName("TeamA");
em.persist(team);

// 회원 저장
Member member = new Member();
member.setName("member1");
member.setTeam(team); //단방향 연관관계 설정, 참조 저장
em.persist(member);

em.flush();
em.clear();

// 회원 조회
Member findMember = em.find(Member.class, member.getId());
List<Member> members = findMember.getTeam().getMembers();

for (Member m : members) {
    System.out.println("m = " + m);
}

// 팀 조회
Team findTeam = findMember.getTeam();

System.out.println("findTeam = " + findTeam);
System.out.println("findMember = " + findMember);
```
<br/>

### mappedBy
mappedBy는 **객체와 테이블간 연관관계를 맺는 차이를 이해해야 한다.**

#### 객체와 테이블이 관계를 맺는 차이
* 객체 연관관계 = 2개
  * 회원 -> 팀 연관관계 1개(단방향)
  * 팀 -> 회원 연관관계 1개(단방향)
* 테이블 연관관계 = 1개
  * 회원 <-> 팀의 연관관계 1개(양방향)
![image](https://github.com/jub3907/Spring-study/assets/58246682/a8298135-e116-4c0d-bd6b-493fc5c1c94b)

객체의 양방향 관계는 사실 양방향 관계가 아니라 서로 다른 단뱡향 관계 2개다.\
즉, 객체를 양방향으로 참조하려면 단방향 연관관계를 2개 만들어야 한다.
```java
class A {
    B b;
}

class B {
    A a;
}
```
<br/>

하지만, 테이블은 **외래 키 하나**로 두 테이블의 연관관계를 관리한다.\
`MEMBER.TEAM_ID`라는 외래 키 하나로 양방향의 연관 관계를 갖는다.\
이를 통해 **양쪽으로** 조인할 수 있다.
```sql
SELECT *
FROM MEMBER M
JOIN TEAM T ON M.TEAM_ID = T.TEAM_ID

SELECT *
FROM TEAM T
JOIN MEMBER M ON T.TEAM_ID = M.TEAM_ID
```
<br/>

여기서 딜레마가 발생한다.\
Member, 혹은 Team 둘 중 하나에서 **외래 키를 관리**해야만 한다.

즉, Member의 Team을 수정했을 떄 외래키를 업데이트하거나,\
Team의 members를 수정했을 때 외래키가 업데이트되거나, 둘 중 하나여야 한다.

여기서 우리가 사용해야 하는 것은 바로 **연관관계의 주인**이다.
<br/>
<br/>

### 연관관계의 주인
양방향 매핑시, 객체의 두 관계 중 하나를 연관관계의 주인으로 지정한다.\
**연관관계의 주인만이 외래 키를 관리**하고, \
**주인이 아닌 쪽은 읽기만** 가능하다.

주인은 mappedBy 속성을 사용하지 않고, \
주인이 아니면 mappedBy 속성을 사용해 주인을 지정해야 한다.\
그럼, 누굴 주인으로 사용해야 할까?

#### 누굴 주인으로?
바로, **외래 키가 있는 곳을 주인으로 설정해야 한다.**\
여기서는 **`Member.team`**이 연관관계의 주인이다.

![image](https://github.com/jub3907/Spring-study/assets/58246682/d474c7c7-1e90-4ad5-a705-0b7c58b34989)
<br/>
<br/>


### 양방향 매핑시 가장 많이 하는 실수
#### 연관관계의 주인에 값을 입력하지 않은 경우
```java
Team team = new Team();
team.setName("TeamA");
em.persist(team);

Member member = new Member();
member.setName("member1");

//역방향(주인이 아닌 방향)만 연관관계 설정
team.getMembers().add(member);

em.persist(member);
```
양방향 매핑 시, 연관관계의 주인에 값을 입력해야 한다.\
다만, 순수한 객체 관계를 고려하면, 항상 양쪽 다 값을 입력해야 한다.
```java
//...

team.getMembers().add(member);
//연관관계의 주인에 값 설정
member.setTeam(team); 

em.persist(member);
```
<br/>

#### 추가로 주의해야 하는 사항
* 순수 객체 상태를 고려해서 항상 양쪽에 값을 설정하자
* 연관관계 편의 메소드를 생성하자
* 양방향 매핑시에 무한 루프를 조심하자
  * 예: toString(), lombok, JSON 생성 라이브러리

* 컨트롤러에선 엔티티를 반환하지 말자.
<br/>

### 양방향 매핑 정리
* 단방향 매핑만으로도 이미 연관관계 매핑은 완료
* 양방향 매핑은 반대 방향으로 조회(객체 그래프 탐색) 기능이 추가된 것 뿐
* JPQL에서 역방향으로 탐색할 일이 많음
* 단방향 매핑을 잘 하고 양방향은 필요할 때 추가해도 됨\
  (테이블에 영향을 주지 않음)
<br/>

### 연관관계의 주인을 정하는 기준
* 비즈니스 로직을 기준으로 연관관계의 주인을 선택하면 안됨
* 연관관계의 주인은 외래 키의 위치를 기준으로 정해야함
<br/>
