### 고급 매핑
### 상속관계 매핑
* 관계형 데이터베이스는 상속 관계가 존재하지 않는다.
* 슈퍼타입 서브타입 관계라는 모델링 기법이 객체 상속과 유사하다.
* 상속관계 매핑: 객체의 상속과 구조와 DB의 슈퍼타입 서브타입 관계를 매핑
![image](https://github.com/jub3907/Spring-study/assets/58246682/f097c922-3022-4dd8-81f2-e4786fe74e4b)

* 슈퍼타입 서브타입 논리 모델을 실제 물리 모델로 구현하는 방법
  * 각각 테이블로 변환 -> 조인 전략
  * 통합 테이블로 변환 -> 단일 테이블 전략
  * 서브타입 테이블로 변환 -> 구현 클래스마다 테이블 전략

### 주요 어노테이션
* `@Inheritance(strategy=InheritanceType.XXX)`
  * 상속 전략을 나타낸다.
  * **JOINED**: 조인 전략
  * **SINGLE_TABLE**: 단일 테이블 전략
  * **TABLE_PER_CLASS**: 구현 클래스마다 테이블 전략
* `@DiscriminatorColumn(name=“DTYPE”)`
  * 구분을 위한 컬럼을 생성한다.
* `@DiscriminatorValue(“XXX”)`
  * 구분을 위한 값을 설정한다.
<br/>

### 조인 전략
![image](https://github.com/jub3907/Spring-study/assets/58246682/cb3bc9b5-ec70-470b-9886-e9304d57c5d0)

데이터를 가져올 때, PK를 통한 **Join**을 사용해 함께 가져오는 방법.

다음과 같은 방식으로 구현 가능하다.
```java
@Entity
public class Item {

    @Id @GeneratedValue
    private Long id;

    private String name;
    private int price;
}

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Album extends Item{

    private String artist;
}

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Movie extends Item {

    private String director;
    private String actor;
}
```
* Movie 저장한 결과\
  ![image](https://github.com/jub3907/Spring-study/assets/58246682/e23ef7dc-a8b9-43cd-9c59-8631169a188d)

* 장점
  * 테이블을 정규화해서 모델링할 수 있다.
  * 외래 키 참조 무결성 제약조건 활용가능
  * 저장공간 효율화
* 단점
  * 조회시 조인을 많이 사용, 성능 저하
  * 조회 쿼리가 복잡함
  * 데이터 저장시 INSERT SQL 2번 호출

사실, 위에서 언급한 단점은 그렇게 큰 단점은 아닐 수 있다.\
하지만 테이블이 많아져서 복잡하고, 후술할 단일테이블 전략에 비해선 느리다.
<br/>

### 단일 테이블 전략
![image](https://github.com/jub3907/Spring-study/assets/58246682/6e785cb5-9857-49e2-8c30-a4498f512b27)

논리 모델을 하나의 테이블로 합치는 방법.  \
DTYPE과 같은 구분자를 사용한다.

위 조인전략과 코드 구성은 같지만, 상속시 단일 테이블 전략만 사용해주면 된다.
```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Album extends Item{

    private String artist;
}
```
* 테이블 형태
  ![image](https://github.com/jub3907/Spring-study/assets/58246682/abdb5ae4-488c-4fde-8280-b4c3d970fbd8)

* 장점
  * 조인이 필요 없으므로 일반적으로 조회 성능이 빠름
  * 조회 쿼리가 단순함
* 단점
  * 자식 엔티티가 매핑한 컬럼은 모두 null 허용
  * 단일 테이블에 모든 것을 저장하므로 테이블이 커질 수 있다. \
    상황에 따라서 조회 성능이 오히려 느려질 수 있다.
<br/>


### 구현 클래스마다 테이블 전략
![image](https://github.com/jub3907/Spring-study/assets/58246682/16b579f0-fe03-4c1c-aae9-a7184e19eea6)

**이 전략은 데이터베이스 설계자와 ORM 전문가 둘 다 추천하지 않는다.**

* 장점
  * 서브 타입을 명확하게 구분해서 처리할 때 효과적
  * not null 제약조건 사용 가능
* 단점
  * 여러 자식 테이블을 함께 조회할 때 성능이 느림(UNION SQL 필요)
  * 자식 테이블을 통합해서 쿼리하기 어려움
<br/>

### @MappedSuperclass
공통된 매핑 정보가 필요할 때 사용한다. (객체에서)

![image](https://github.com/jub3907/Spring-study/assets/58246682/bf708301-5d26-403e-886d-8dba3db2b24d)

* 상속관계 매핑X
* 엔티티X, 테이블과 매핑X
* 부모 클래스를 상속 받는 **자식 클래스에 매핑 정보만 제공**
* 조회, 검색 불가(**em.find(BaseEntity) 불가**)
* 직접 생성해서 사용할 일이 없으므로 **추상 클래스 권장**

MappedSuperClass는 테이블과 관계 없고, \
단순히 엔티티가 공통으로 사용하는 매핑 정보를 모으는 역할이다.

주로 등등록일, 수정일, 등록자, 수정자 같은 전체 엔티티에서 공통으로 적용하는 정보를 모을 때 사용한다.

>  @Entity 클래스는 엔티티나 @MappedSuperclass로 지정한 클래스만 상속 가능

```java
@MappedSuperclass
public abstract class BaseEntity {

    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;

    // getter setter
}


@Entity
public class Member extends BaseEntity {
    //...
}
```
즉, 그냥 **공통된 속성이 필요할 때,** 사용하는 superclass이다.
<br/>
<br/>

## 프록시와 연관관계 관리

### 프록시
#### 프록시를 왜 써야할까?
다음과 같은 상황이 있다고 가정해보자.

![image](https://github.com/jub3907/Spring-study/assets/58246682/42580d6b-bc92-4cd5-b830-c8371133eb3d)

Member와 Team을 같이 조회해야 하는 경우, \
다음과 같이 코드를 구성할 수 있다.
```java
public void printUserAndTeam(String memberId) {
    Member member = em.find(Member.class, memberId);
    Team team = member.getTeam();
    System.out.println("회원 이름: " + member.getUsername());
    System.out.println("소속팀: " + team.getName());
}
```
이 때, 우린 **TEAM** 정보가 필요하므로, Member를 조회할 때\
Team을 같이 조회하는게 그리 이상한 일이 아니라고 생각할 것이다.

하지만, 단순히 **Member** 정보만 필요한 경우는 어떨까?
```java
public void printUser(String memberId) {
    Member member = em.find(Member.class, memberId);
    Team team = member.getTeam();
    System.out.println("회원 이름: " + member.getUsername());
}
```
이런 경우, Team 정보를 DB에서 불러오는 것이 손해라고 생각될 수 있다.\
이는 비즈니스 상황마다 다른데, JPA 입장에 **무조건 둘 다 불러오는 것**은\
손해, 낭비라고 생각된다. 이러한 상황을 **지연 로딩**을 사용해 해결할 수 있다.

그리고, 이 지연로딩에서 사용되는 것이 바로 **프록시**이다.
<br/>
<br/>

#### 프록시 기초
* em.find() vs **em.getReference()**
* em.find(): 데이터베이스를 통해서 실제 엔티티 객체 조회
* em.getReference(): **데이터베이스 조회를 미루는 가짜(프록시) 엔티티 객체 조회**

즉, 프록시란 하이버네이트가 만들어낸 **가짜 클래스**라고 할 수 있다.\
프록시는 실제 클래스를 상속받아서 만들어지고, \
실제 클래스와 겉 모양이 같다.

즉, 사용하는 입장에서는 진짜 객체인지, 프록시 객체인지 구분하지 않고 사용하면 된다.

![image](https://github.com/jub3907/Spring-study/assets/58246682/77e9a8fc-7b88-4a0b-8824-a9076afc6698)

또한 프록시 객체는 실제 객체의 참조를 보관하기 떄문에,\
프록시 객체를 호출하면 프록시 객체는 실제 객체의 메소드를 호출한다.
<br/>
<br/>

#### 프록시 객체의 초기화
```java
Member member = em.getReference(Member.class, “id1”);
member.getName();
```
![image](https://github.com/jub3907/Spring-study/assets/58246682/b0da2676-1593-41fb-a7f3-013ca7eedcbb)
<br/>
<br/>

#### 프록시의 특징
* 프록시 객체는 처음 사용할 때 한 번만 초기화
* 프록시 객체를 초기화 할 때, 프록시 객체가 실제 엔티티로 바뀌는 것은 아님, \
  초기화되면 프록시 객체를 통해서 실제 엔티티에 접근 가능
* 프록시 객체는 원본 엔티티를 상속받음, 따라서 타입 체크시 주의해야함 \
  (== 비교 실패, 대신 instance of 사용)
* 영속성 컨텍스트에 찾는 엔티티가 이미 있으면 em.getReference()를 호출해도 실제 엔티티 반환
* 영속성 컨텍스트의 도움을 받을 수 없는 준영속 상태일 때, 프록시를 초기화하면 문제 발생\
  (하이버네이트는 org.hibernate.LazyInitializationException 예외를 터트림)
  ```
  Member member1 = em.getReference(Member.class, member.getId());
  em.close();
  System.out.println("member1.getName() = " + member1.getName());
  // 오류 발생
  ```
<br/>

#### 프록시 확인 방법
* 프록시 인스턴스의 초기화 여부 확인\
  PersistenceUnitUtil.isLoaded(Object entity)
* 프록시 클래스 확인 방법\
  entity.getClass().getName() 출력\
  (..javasist.. or HibernateProxy…)
* 프록시 강제 초기화\
  org.hibernate.Hibernate.initialize(entity);
* 참고: JPA 표준은 강제 초기화 없음\
  강제 호출: member.getName()
<br/>

### 지연로딩
앞서 봤던 예시와 동일하게, 다음과 같은 구조를 갖는 Member 엔티티가 존재한다.

```java
@Entity
public class Member {
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(name = "USERNAME")
    private String name;
    
    @JoinColumn(name = "TEAM_ID")
    private Team team;
    ..
}
```

Member를 조회할 때 Team도 함께 조회해야 할까?\
단순히 Member 정보만 사용하는 비즈니스 로직이라면, \
Member의 정보만 불러오는 것이 맞다.

우리는 이 때, 지연로딩 LAZY를 사용해 프록시로 조회할 수 있다.
```java
@Entity
public class Member {
    ..
    @ManyToOne(fetch = FetchType.LAZY) //**
    @JoinColumn(name = "TEAM_ID")
    private Team team;
    ..
}
```

다음 코드를 실행해보면, 우리가 지연 로딩으로 세팅한 Team이 프록시로 반환되는걸 알 수 있다.
```java
Member member1 = em.find(Member.class, member.getId());
System.out.println("member1.getTeam().getClass() = " + member1.getTeam().getClass());

// member1.getTeam().getClass() = class hellojpa.Team$HibernateProxy$buIHBO4O
```
<br/>

### 즉시 로딩
그럼, 위 예시와 반대로 생각해보자.\
만약, Member와 Team을 자주 함께 사용한다면 어떻게 될까?\
이 때 지연로딩을 사용한다면 Member, Team을 계속 따로 조회하기 때문에, \
성능상 손해를 보게 된다.

이 때 사용하는 것이 바로 **즉시 로딩**, EAGER 속성이다.
```java
@Entity
public class Member {
    ..
    @ManyToOne(fetch = FetchType.EAGER) //**
    @JoinColumn(name = "TEAM_ID")
    private Team team;
    ..
}
```
위와 같이 EAGER를 사용하게 되면 Member 조회시, 항상 Team도 조회된다.\
JPA 구현체는 가능한 조인을 사용해, SQL을 한번에 함께 조회한다.
<br/>
<br/>

#### 프록시와 즉시 로딩 주의점
**가급적, 지연 로딩만 사용해야 한다.**

특히나 실무에선 반드시 지연 로딩을 사용해야 하는데,\
즉시 로딩을 적용하면 **예상치 못한 SQL**, 심지어 **N + 1** 문제가 발생할 수 있다.\

@ManyToOne, @OneToOne은 기본이 즉시 로딩이기 떄문에, 일일히 LAZY로 설정해줘야 한다.\
다만, @OneToMany, @ManyToMany는 기본이 지연 로딩이다.

`fetch join`을 사용하면, 즉시 로딩처럼 동작할 수 있으므로, \
정 필요한 경우 `fetch join`을 사용하자.
<br/>
<br/>

#### 지연 로딩 활용
아래 내용은 전부 **이론적인 부분**이므로, 실무에선 전부 지연 로딩으로 설정하자.\
다음과 같은 구조가 있다고 가정해보자.

![image](https://github.com/jub3907/Spring-study/assets/58246682/ebb6fa1f-4324-4b8a-8a95-eede4e4c29cf)

* Member와 Team은 자주 함께 사용 -> 즉시 로딩
* Member와 Order는 가끔 사용 -> 지연 로딩
* Order와 Product는 자주 함께 사용 -> 즉시 로딩

![image](https://github.com/jub3907/Spring-study/assets/58246682/67a9f264-c38a-43a9-8c1c-3aee9457927c)

![image](https://github.com/jub3907/Spring-study/assets/58246682/8c30e08e-8176-4cb9-bb96-ca3f378989a8)

다시 한번 강조하지만, 위는 단순히 **이론적인 부분**이다.\
모든 연관관계엔 지연 로딩을 사용하고, \
특히 실무에선 즉시 로딩을 사용하지 말자.

정 필요한 경우 JPQL fetch 조인이나, 엔티티 그래프 기능을 사용해야 한다.
<br/>
<br/>

### 영속성 전이, CASCADE
특정 엔티티를 영속 상태로 만들 때,\
**연관된 엔티티도 함께 영속 상태로 만들고 싶을 때** 사용한다.\
ex) 부모 엔티티를 저장하면서, 자식 엔티티도 저장하고 싶을 때.

![image](https://github.com/jub3907/Spring-study/assets/58246682/6529c52b-5006-4ee8-8309-63811af7ba12)

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

@Entity
public class Child {

    @Id @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Parent parent;
}


Child child1 = new Child();
Child child2 = new Child();

child1.setName("child1");
child2.setName("child2");

Parent parent = new Parent();
parent.setName("parent");
parent.addChild(child1);
parent.addChild(child2);

em.persist(parent);
```
* 실행 결과
  ![image](https://github.com/jub3907/Spring-study/assets/58246682/c0f89931-ff4d-44b0-a80b-327c96e97da5)

![image](https://github.com/jub3907/Spring-study/assets/58246682/7da49148-0ca2-4386-a682-80f5103d86b9)

위처럼, **부모 클래스만 persist** 하더라도, 자식까지 저장되는걸 볼 수 있다.
<br/>
<br/>

#### CASCADE 주의점
* 영속성 전이는 연관관계를 매핑하는 것과 아무 관련이 없다.
* 엔티티를 영속화할 때 연관된 엔티티도 함께 영속화하는 편리함을 제공할 뿐이다.
* 주로, **하나의 부모만이 그 자식을 관리할 때** 사용해야 한다.\
  ex) 게시글 - 첨부파일과 같은 관계.
<br/>

### CASCADE의 종류
* **ALL: 모두 적용**
* **PERSIST: 영속**
* **REMOVE: 삭제**
* MERGE: 병합
* REFRESH: REFRESH
* DETACH: DETACH
<br/>

### 고아 객체
고아 객체란, **부모 엔티티와 연관관계가 끊어진 자식 엔티티**를 의미한다.\
`orphanRemoval`이란 옵션을 사용해, 고아 객체를 자동으로 삭제할 수 있다.
```java

@Entity
public class Parent {
    //...
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Child> childList = new ArrayList<>();
}


Child child1 = new Child();
Child child2 = new Child();

child1.setName("child1");
child2.setName("child2");

Parent parent = new Parent();
parent.setName("parent");
parent.addChild(child1);
parent.addChild(child2);

em.persist(parent);

Parent parent1 = em.find(Parent.class, parent.getId());
parent1.getChildList().remove(0);
```
* 실행 결과\
  ![image](https://github.com/jub3907/Spring-study/assets/58246682/15f71fbd-1ff6-4f9a-b4b0-e8a6f67a6a4b)
<br/>

#### 고아 객체 - 주의
* 참조가 제거된 엔티티는 다른 곳에서 참조하지 않는 고아 객체로 보고 삭제하는 기능
* 참조하는 곳이 하나일 때 사용해야함!
* **특정 엔티티가 개인 소유할 때 사용**
* @OneToOne, @OneToMany만 가능
* 참고: 개념적으로 부모를 제거하면 자식은 고아가 된다. \
  따라서 고아 객체 제거 기능을 활성화 하면, 부모를 제거할 때 \
  자식도 함께 제거된다. 이것은 CascadeType.REMOVE처럼 동작한다.
<br/>

### 영속성 전이 + 고아객체, 생명주기
* CascadeType.ALL + orphanRemoval=true
* 스스로 생명주기를 관리하는 엔티티는 em.persist()로 영속화, em.remove()로 제거
* 두 옵션을 모두 활성화 하면 부모 엔티티를 통해서 자식의 생명주기를 관리할 수 있음
* 도메인 주도 설계(DDD)의 Aggregate Root개념을 구현할 때 유용
<br/>
