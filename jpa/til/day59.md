## 기본 값 타입
### 엔티티 타입
* @Entity로 정의하는 객체
* 데이터가 변해도 식별자로 지속해서 추적 가능
* 예) 회원 엔티티의 키나 나이 값을 변경해도 식별자로 인식 가능
<br/>

### 값 타입
* int, Integer, String처럼 단순히 값으로 사용하는 자바 기본 타입이나 객체
* 식별자가 없고 값만 있으므로 변경시 추적 불가
* 예) 숫자 100을 200으로 변경하면 완전히 다른 값으로 대체
<br/>

### 값 타입 분류
* 기본값 타입
  * 자바 기본 타입(int, double)
  * 래퍼 클래스(Integer, Long)
  * String
* 임베디드 타입(embedded type, 복합 값 타입)
* 컬렉션 값 타입(collection value type)
<br/>

### 기본값 타입
* 예): String name, int age
* 생명주기를 엔티티의 의존
  * 예) 회원을 삭제하면 이름, 나이 필드도 함께 삭제
* 값 타입은 공유하면X
  * 예) 회원 이름 변경시 다른 회원의 이름도 함께 변경되면 안됨

> 자바의 기본 타입은 절대 공유되지 않는다.\
> 즉, int, double같은 기본 타입은 절대 공유되지 않고, 항상 값을 복사한다.\
<br/>

## 임베디드 타입(복합 값 타입)
### 임베디드 타입
* 새로운 값 타입을 직접 정의할 수 있음
* JPA는 임베디드 타입(embedded type)이라 함
* 주로 기본 값 타입을 모아서 만들어서 복합 값 타입이라고도 함
* int, String과 같은 값 타입

예를 들어보자.\
회원 엔티티는 이름, 근무 시작일, 근무 종료일, 주소 도시, 주소 번지, 주소 우편번호를 가진다.

![image](https://github.com/jub3907/Spring-study/assets/58246682/e1aad20f-72f3-4c57-96ea-e50bef9fe00c)

이 회원 엔티티는 다음과 같이, 묶어낼 수 있다.

![image](https://github.com/jub3907/Spring-study/assets/58246682/48f45516-7d07-4490-992d-745382710b7a)\
![image](https://github.com/jub3907/Spring-study/assets/58246682/c7cd7b1e-c4ea-44cb-91bd-aa056d7e9f9e)

### 임베디드 타입 사용법
JPA에선 이러한 임베디드 타입을 애노테이션을 사용해 선언할 수 있다.
* @Embeddable: 값 타입을 정의하는 곳에 표시
* @Embedded: 값 타입을 사용하는 곳에 표시
* 기본 생성자 필수

```java
@Embeddable
public class Address {
    private String city;
    private String street;
    private String zipcode;

    public Address() {
    }
}

@Embeddable
public class Period {

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public Period() {
    }
}

@Entity
public class Member extends BaseEntity {
    //...
    @Embedded
    private Period workPeriod;

    @Embedded
    private Address homeAddress;
    //...
}
```
<br/>

### 임베디드 타입의 장점
* 재사용
* 높은 응집도
* Period.isWork()처럼 해당 값 타입만 사용하는 의미 있는 메소드를 만들 수 있음
* 임베디드 타입을 포함한 모든 값 타입은, 값 타입을 소유한 엔티티에 생명주기를 의존함
<br/>

### 임베디드 타입과 테이블 매핑
![image](https://github.com/jub3907/Spring-study/assets/58246682/49967737-f5b6-4d74-ad53-230789b84cbe)

* 임베디드 타입은 엔티티의 값일 뿐이다.
* 임베디드 타입을 사용하기 전과 후에 **매핑하는 테이블은 같다**.
* 객체와 테이블을 아주 세밀하게(find-grained) 매핑하는 것이 가능
* 잘 설계한 ORM 애플리케이션은 매핑한 테이블의 수보다 클래스의 수가 더 많음
<br/>

### @AttributeOverride: 속성 재정의
* 한 엔티티에서 같은 값 타입을 사용하면?
* 컬럼 명이 중복된다.
* @AttributeOverrides, @AttributeOverride를 사용해서 컬럼 명 속성을 재정의할 수 있다.

```java

@Entity
public class Member extends BaseEntity {
    @Embedded
    private Address homeAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "city", column = @Column(name = "WORK_CITY")),
            @AttributeOverride(name = "street", column = @Column(name = "WORK_STREET")),
            @AttributeOverride(name = "zipcode", column = @Column(name = "WORK_ZIPCODE"))
    })
    private Address workAddress;
}
```
<br/>

### 임베디드 타입과 null
* 임베디드 타입의 값이 null이면 매핑한 컬럼 값은 모두 null
<br/>


## 값 타입과 불변 객체
값 타입은 복잡한 객체 세상을 조금이라도 단순화하려고 만든 개념이다. \
따라서 값 타입은 단순하고 안전하게 다룰 수 있어야 한다.
<br/>
<br/>

### 값 타입 공유 참조
우리는 int, String과 같은 값을 변경하는 데에 있어서 부담을 느끼지 않는다.\
이는 값 타입이 단순하고, 안전하게 설계되어있기 때문에 그렇다.

하지만, 임베디드 타입 같은 값 타입을 여러 엔티티에서 공유하면 위험하다.\
다음과 같이, `OldCity`값을 두 회원이 참조하고 있다고 가정해보자.

![image](https://github.com/jub3907/Spring-study/assets/58246682/fb6e6c1e-93c2-4124-be6f-11ae0882b771)

이 때, 회원 1의 주소를 참조해서, city 값을 NewCity로 변경하면 어떻게 될까?

```java
Address address = new Address("city", "OldCity", "zipCode);

member1.setAddress(address);
member2.setAddress(address);

member1.getAddress().setCity("NewCity");
```

바로 부작용이 발생하게 된다.\
값 타입의 실제 인스턴스인 값을 공유하는 것은 위험하다. \
따라서, 대신 값(인스턴스)를 복사해서 사용해야 한다.

```java

Address addr = new Address("city", "OldCity", "zipCode");

member1.setAddress(addr);

Address copyAddress = new Address(addr.getCity(), addr.getCity(), addr.getZipCode());

member2.setAddress(copyAddress);
```
<br/>

### 객체 타입의 한계
* 항상 값을 복사해서 사용하면 공유 참조로 인해 발생하는 부작용을 피할 수 있다.
* 문제는 임베디드 타입처럼 **직접 정의한 값 타입은 자바의 기본타입이 아니라 객체 타입**이다.
* 자바 기본 타입에 값을 대입하면 값을 복사한다.
* **객체 타입은 참조 값을 직접 대입하는 것을 막을 방법이 없다.**
* **객체의 공유 참조는 피할 수 없다.**

* 기본 타입(primitive type)
  ```java
  int a = 10;
  int b = a;//기본 타입은 값을 복사
  b = 4;
  ```
* 객체 타입
  ```java
  Address a = new Address(“Old”);
  Address b = a; //객체 타입은 참조를 전달
  b. setCity(“New”)
  ```
<br/>

### 불변 객체
* 객체 타입을 수정할 수 없게 만들면 **부작용을 원천 차단한다.**
* **값 타입은 불변 객체(immutable object)로 설계해야한다.**
* **불변 객체: 생성 시점 이후 절대 값을 변경할 수 없는 객체**
* 생성자로만 값을 설정하고 수정자(Setter)를 만들지 않으면 된다.

> Integer, String은 자바가 제공하는 대표적인 불변 객체
<br/>
<br/>

## 값 타입의 비교
### 값 타입의 비교
인스턴스가 달라도 그 안에 값이 같으면 같은 것으로 봐야 한다.

```java
int a = 10;
int b = 10;
// a == b -> true

Address a = new Address(“서울시”)
Address b = new Address(“서울시”)
// a == b -> false
```
* 동일성(identity) 비교: 인스턴스의 참조 값을 비교, == 사용
* 동등성(equivalence) 비교: 인스턴스의 값을 비교, equals()사용

값 타입은 a.equals(b)를 사용해서 동등성 비교를 해야 한다.\
따라서, 값 타입의 equals() 메소드를 적절하게 재정의해야 한다.

```java
@Embeddable
public class Address {
    //...

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(city, address.city) && Objects.equals(street, address.street) && Objects.equals(zipcode, address.zipcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, street, zipcode);
    }
}
```
<br/>
<br/>

## 값 타입 컬렉션

값 타입 컬렉션이란, 값 타입을 컬렉션에 담아서 사용하는 것을 의미한다.\
앞서, 우리는 엔티티를 컬렉션으로 사용한 적이 있다.
```java
@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Child> childList = new ArrayList<>();
```
이처럼, 이번엔 값 타입을 컬렉션으로 사용해보자.

![image](https://github.com/jub3907/Spring-study/assets/58246682/4d09f993-0477-4218-84eb-0b9990064575)

관계형 DB는 기본적으로, 값 타입을 컬렉션으로 테이블 안에 담을 수 없다.\
따라서, 위와 같은 구조에서 `favoriteFood`를 위한 별도의 테이블로 저장해야 한다.\
이 때, 테이블의 모든 컬럼을 PK로 지정해줘야 한다.

```java
@Entity
public class Member extends BaseEntity {
    // 값 타입 컬렉션
    @ElementCollection
    @CollectionTable(name = "FAVORITE_FOOD")
    @Column(name = "FOOD_NAME") // 예외적으로 허용된다.
    private Set<String> favoriteFoolds = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "ADDRESS",
            joinColumns = @JoinColumn(name = "MEMBER_ID"))
    private List<Address> addressHistory = new ArrayList<>();

}
```
<br/>

### 값 타입 컬렉션
* 값 타입을 하나 이상 저장할 때 사용
* @ElementCollection, @CollectionTable 사용
* 데이터베이스는 컬렉션을 같은 테이블에 저장할 수 없다.
* 컬렉션을 저장하기 위한 별도의 테이블이 필요함
<br/>

### 값 타입 컬렉션 사용
#### 값 타입 저장 예제
```java
Member member = new Member();
member.setName("member1");
member.setHomeAddress(new Address("city1", "street", "zipcode"));

member.getFavoriteFoolds().add("치킨");
member.getFavoriteFoolds().add("피자");
member.getFavoriteFoolds().add("족발");

member.getAddressHistory().add(new Address("old1", "street", "zipcode"));
member.getAddressHistory().add(new Address("old2", "street", "zipcode"));

em.persist(member);
```

![image](https://github.com/jub3907/Spring-study/assets/58246682/064a1e58-21e7-4e59-8dcf-046ece3e4f47)
<br/>
<br/>

#### 값 타입 조회 예제
```java
// 앞서 사용한 저장 코드 사용

Set<String> favoriteFoods = member1.getFavoriteFoods();
List<Address> addressHistory = member1.getAddressHistory();

for (String favoriteFood : favoriteFoods) {
    System.out.println("favoriteFood = " + favoriteFood);
}

for (Address address : addressHistory) {
    System.out.println("address = " + address);
}
```
* 실행 결과
  ```java
  favoriteFood = 족발
  favoriteFood = 치킨
  favoriteFood = 피자
  address = Address{city='old1', street='street', zipcode='zipcode'}
  address = Address{city='old2', street='street', zipcode='zipcode'}
  ```
값 타입 컬렉션은 지연 로딩 전략을 사용한다.
<br/>
<br/>

#### 값 타입 수정 예제
```java
// 값 타입 수정
// 치킨 -> 한식
member1.getFavoriteFoods().remove("치킨");
member1.getFavoriteFoods().add("한식");

member1.getAddressHistory().remove(new Address("old1", "street", "zipcode"));
member1.getAddressHistory().add(new Address("new_old_City", "street", "zipcode"));
```
![image](https://github.com/jub3907/Spring-study/assets/58246682/46d98c4b-9b98-4301-a14c-3eb7f828ae1b)

수정은 잘 됐지만, 쿼리문을 보면 다음과 같다.
```sql
delete 
from
    ADDRESS 
where
    MEMBER_ID=?

insert 
into
    ADDRESS
    (MEMBER_ID, city, street, zipcode) 
values
    (?, ?, ?, ?)

insert 
into
    ADDRESS
    (MEMBER_ID, city, street, zipcode) 
values
    (?, ?, ?, ?)
```
우리가 기대한건 "old1" 주소를 갖는 address를 제거하고,\
"new_old_city" 주소값을 갖는 address를 넣어주는 것이였다.\
하지만, 실제론 **두 번의 insert 쿼리**가 실행되었다.

즉, **값 타입 컬렉션에 변경 사항이 발생하면,**\
**주인 텐티티와 연관된 모든 데이터를 삭제하고,**\
**값 타입 컬렉션에 있는 현재 값을 모두 다시 저장한다.**

> 값 타입 컬렉션은 영속성 전에(Cascade) + 고아 객체 제거 기능을 필수로 가진다고 볼 수 있다.
<br/>
<br/>

### 값 타입 컬렉션의 제약사항
* 값 타입은 엔티티와 다르게 식별자 개념이 없다.
* 값은 변경하면 추적이 어렵다.
* 값 타입 컬렉션에 변경 사항이 발생하면, \
  주인 엔티티와 연관된 모든 데이터를 삭제하고, \
  값 타입 컬렉션에 있는 현재 값을 모두 다시 저장한다.
* 값 타입 컬렉션을 매핑하는 테이블은 모든 컬럼을 묶어서 기본키를 구성해야 함\
  : null 입력X, 중복 저장X
<br/>

### 값 타입 컬렉션 대안
* 실무에서는 상황에 따라 값 타입 컬렉션 대신에 일대다 관계를 고려하자.
* 일대다 관계를 위한 엔티티를 만들고, 여기에서 값 타입을 사용
* 영속성 전이(Cascade) + 고아 객체 제거를 사용해서 값 타입 컬렉션 처럼 사용
* EX) AddressEntity
  ```java
  @Entity
  public class AddressEntity {
  
      @Id @GeneratedValue
      private String id;
  
      @Embedded
      private Address address;
  }
  
  @Entity
  public class Member extends BaseEntity {
  //    @ElementCollection
  //    @CollectionTable(
  //            name = "ADDRESS",
  //            joinColumns = @JoinColumn(name = "MEMBER_ID"))
  //    private List<Address> addressHistory = new ArrayList<>();
  
      @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
      @JoinColumn(name = "MEMBER_ID")
      private List<AddressEntity> addressHistory = new ArrayList<>();
  }
  ```
<br/>

### 정리
* **엔티티 타입의 특징**
  * 식별자O
  * 생명 주기 관리
  * 공유
* **값 타입의 특징**
  * 식별자X
  * 생명 주기를 엔티티에 의존
  * 공유하지 않는 것이 안전(복사해서 사용)
  * 불변 객체로 만드는 것이 안전

**값 타입은 정말 값 타입이라 판단될 때만 사용하자.**

**엔티티와 값 타입을 혼동해서 엔티티를 값 타입으로 만들면 안된다.**

**식별자가 필요하고, 지속해서 값을 추적, 변경해야 한다면 그것 은 값 타입이 아닌 엔티티이다.**
