# JPA 내용 정리

### 지연 로딩과 즉시 로딩
* 지연 로딩 : 객체가 실제로 사용될 때 로딩한다.
* 즉시 로딩 : Join SQL로, 한번에 연관된 객체까지 미리 조회한다. N+1문제가 발생할 수 있다.
<br/>

### 영속성 컨텍스트
엔티티를 영구 저장하는 환경.

* 저장 : `jpa.persist(Entitiy Data)`
* 조회 : `Object data = jpa.find(Object.class, PK)`
* 삭제 : `jpa.remove(Entity Data)`

통일한 트랜젝션에서 조회한 엔티티는 같다는 것이 보장된다.
<br/>
<br/>

### 엔티티(데이터 집합)의 생명 주기
* 비영속 : 멤버 객체를 생성하고, 값을 넣기만 한 상태
* 영속 : 영속성 컨텍스트에 객체를 넣어주어, 영속성 컨텍스트에 의해 관리되는 상태.
* 준영속 : 엔티티를 영속성 컨텍스트에서 분리한 상태
* 삭제 : 객체를 삭제한 상태.
<br/>

### 영속성 컨텍스트의 이점
* 1차 캐시 가능
* 동일성 보장
* 트랜젝션을 지원하는 쓰기 지연
* Dirty Check(변경 감지)
* 지연 로딩(Lazy Loading)
<br/>

### 플러시
영속성 컨텍스트의 변경 내용을 데이터베이스에 반영한다. \
다음과 같은 방법으로 플러시할 수 있다.

* `em.flush()` 호출
* 트랜젝션 커밋
* JPQL 쿼리 실행
<br/>


### @Entity
객체와 테이블을 매핑할 때 사용하는 애노테이션.\
@Entity가 붙은 클래스는 JPA가 관리하며, 엔티티라고 한다.

* name : JPA에서 사용할 엔티티 이름
<br/>

### Table
엔티티와 매핑할 테이블을 지정한다. 

* name : 매핑할 데이터 이름
* catalog : 데이터베이스 catalog 매핑
* schema : 데이터베이스 schema 매핑
* uniqueConstraints : DDL 생성시, 유니크 제약 조건 생성
<br/>

### 데이터베이스 스키마 자동생성
DDL을 애플리케이션 실행 시점에 자동으로 생성한다. \
이렇게 생성된 DDL은 개발 장비에서만 사용해야 한다.\
운영 장비에는 절대 create, create-drop, update를 사용하지 말자.\
자세한건 검색.
<br/>


### 필드, 컬럼 매핑
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
|nullable(DDL)|null 값의 허용 여부를 설정한다. <br/>false로 설정하면 DDL 생성 시에 not null 제약조건이 붙는다.|테스트3|
|unique(DDL)|@Table의 uniqueConstraints와 같지만 한 컬럼에 간단히 유니크 제약조건을 걸 때 사용한다.|필드의 자바 타입과 방언 정보를 사용|
|columnDefinition(DDL) |데이터베이스 컬럼 정보를 직접 줄 수 있다. ex) varchar(100) default ‘EMPTY|테스트3|
|length(DDL) |문자 길이 제약조건, String 타입에만 사용한다. |255|
|precision, scale(DDL)|BigDecimal 타입에서 사용한다<br/>(BigInteger도 사용할 수 있다)<br/>.precision은 소수점을 포함한 전체 자릿수를, <br/>scale은 소수의 자릿수다. <br/>참고로 double, float 타입에는 적용되지 않는다. <br/>아주 큰 숫자나정 밀한 소수를 다루어야 할 때만 사용한다|precision=19,scale=2 |
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


### 기본 키 매핑
* 직접 할당 : `@Id`만 사용
* 자동 생성 : `@GeneratedValue` 추가 사용
* 자세한 내용은 [링크](https://github.com/jub3907/Today-I-Learn/blob/main/jpa/basic/3.entity-mapping.md#%EA%B8%B0%EB%B3%B8-%ED%82%A4-%EB%A7%A4%ED%95%91-%EB%B0%A9%EB%B2%95) 참조
<br/>


### 연관관계 매핑
* [연관관계 매핑 기초](https://github.com/jub3907/Today-I-Learn/blob/main/jpa/basic/4.relation-mapping-basic.md#%EC%97%B0%EA%B4%80%EA%B4%80%EA%B3%84-%EB%A7%A4%ED%95%91-%EA%B8%B0%EC%B4%88)
* [연관관계 매핑 정리](https://github.com/jub3907/Today-I-Learn/blob/main/jpa/basic/5.several-relation-mapping.md)

팀 멤버 - 팀의 관계를 생각하자.

Member Entity를 구성할 때, Member의 입장에선 \
Member 여러개가 하나의 팀에 속하므로, Many To One.\
또한 지연 로딩으로 설정해야 하므로, LAZY 설정.
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "TEAM_ID")
private Team team;
```
Team Entity를 구성할 땐, Team의 입장에선\
Team 하나에 여러 Member가 속할 수 있으므로, One To Many.
```java
@OneToMany(mappedBy = "team") // mappedBy -> "무엇과 연결되어 있는가?"
private List<Member> members = new ArrayList<>();
```
<br/>

### MappedBy, 연관관계 주인
테이블은 외래 키 하나로 두 테이블의 연관관계를 관리하므로,\
두 객체 중 하나에서 이 외래 키를 관리해야 한다.\
이 때, 주인이 아닌 Entity에선 MappedBy를 사용해 주인을 지정해준다.

연관관계의 주인은 **외래 키가 존재하는 엔티티**여야 한다.\
따라서 Team Entity에서 mappedBy 사용.
<br/>

### 상속관계 매핑
조인, 혹 단일 테이블 전략을 사용하자.
* [링크](https://github.com/jub3907/Today-I-Learn/blob/main/jpa/basic/6.inheritance-mapping-and-mapped-superclass.md)
```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Album extends Item{

    private String artist;
}

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Album extends Item{
    private String artist;
}
```
<br/>

### 프록시
프록시는 지연 로딩을 위해 사용되는 가짜 객체.
<br/>
<br/>

### 영속성 전이, CASCADE
특정 엔티티를 영속 상태로 만들고 싶을 때,\
연관된 엔티티도 함께 영속상태로 만들고 싶으면 사용한다.
```java
@Entity
public class Parent {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Child> childList = new ArrayList<>();
}
```
<br/>

### 고아 객체
부모 엔티티와 연관관계가 끊어진 자식 엔티티(고아 객체)를 자동으로 삭제하는 옵션.\
특정 엔티티가 개인 소유일 때만 사용 가능하다.

CascadeType.All, orphanRemoval = true 옵션을 사용하면 \
부모 엔티티를 통해 자식의 생명 주기를 관리할 수 있다.
<br/>
<br/>

### 임베디드 타입
JPA에선 단순히 값으로만 사용되는 자바 기본 타입, 혹은 객체를 생성할 수 있다.
* @Embeddable: 값 타입을 정의하는 곳에 표시
* @Embedded: 값 타입을 사용하는 곳에 표시

다만, [값 타입 공유 참조](https://github.com/jub3907/Today-I-Learn/blob/main/jpa/basic/8.value-type.md#%EA%B0%92-%ED%83%80%EC%9E%85-%EA%B3%B5%EC%9C%A0-%EC%B0%B8%EC%A1%B0)를 조심하자.\
이는 **불변 객체**를 사용해 부작용을 원천 차단할 수 있다.\
생성자로만 값을 설정하고, 수정자는 만들지 말자.
<br/>
<br/>

### 값 타입 컬렉션
내용이 기므로, [링크](https://github.com/jub3907/Today-I-Learn/blob/main/jpa/basic/8.value-type.md#%EA%B0%92-%ED%83%80%EC%9E%85-%EC%BB%AC%EB%A0%89%EC%85%98)를 참조.\
실무에선 값 타입 컬렉션 대신, 일대다 관게를 고려하는게 나아보인다. [링크](https://github.com/jub3907/Today-I-Learn/blob/main/jpa/basic/8.value-type.md#%EA%B0%92-%ED%83%80%EC%9E%85-%EC%BB%AC%EB%A0%89%EC%85%98-%EB%8C%80%EC%95%88) 참조.

값 타입은 정말 값 타입이라 판단될 때만 사용하자.

엔티티와 값 타입을 혼동해서 엔티티를 값 타입으로 만들면 안된다.

식별자가 필요하고, 지속해서 값을 추적, 변경해야 한다면 그것 은 값 타입이 아닌 엔티티이다.
<br/>
<br/>

### JPQL
내용이 기므로, [링크1](https://github.com/jub3907/Today-I-Learn/blob/main/jpa/basic/9.jpql-basic-grammer.md), [링크2](https://github.com/jub3907/Today-I-Learn/blob/main/jpa/basic/10.jpql-middle-grammer.md) 참조.
<br/>
<br/>
