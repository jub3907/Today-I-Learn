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


# [JPA 예제](https://github.com/jub3907/Spring-study/tree/main/jpashop) 코드 분석
## 비즈니스 상황
### 엔티티
![image](https://github.com/jub3907/outSourcing/assets/58246682/e5d48668-b1c2-4b5d-8aea-a3df4c26a00c)
<br/>
<br/>

### 테이블
![image](https://github.com/jub3907/outSourcing/assets/58246682/6ad70554-cace-438c-baac-43e623ec7540)
<br/>
<br/>


## 도메인
### item
```java
@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // 싱글 테이블 전략 사용
@DiscriminatorColumn(name = "dtype")
public abstract class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    // == 비즈니스 로직 추가 ==

    /**
     * stock 증가
     */
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * stock 감소
     */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }

        this.stockQuantity -= quantity;
    }
}
```
* Album, Book, Movie 엔티티의 베이스가 되는 엔티티.
* `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)`를 사용해 싱글 테이블 전략 사용.
* 부모에 `@DiscriminatorColumn(name = "dtype")`를 사용해 하위 클래스를 구분하는 용도의 컬럼을 만듦.
* 실무에선 사용해선 안되나, `ManyToMany`를 사용해 다대다 연관관계를 매핑. `mappedBy`를 사용해, 연관관계의 주인을 `Category.items`로 설정.
* stock의 개수를 증가, 감소시키는 비즈니스 로직을 엔티티에 구현
<br/>

### Album
```java
@Entity
@Getter
@Setter
@DiscriminatorValue("A")
public class Album extends Item {

    private String artist;
    private String etc;
}
```
`@DiscriminatorValue("A")`를 사용해 슈퍼타입의 구분 컬럼에 저장할 값을 지정함.
<br/>

### Category
```java
@Entity
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(name = "category_item",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> child = new ArrayList<>();

    // ==연관관계 메소드==
    public void addChildCategory(Category child) {
        this.child.add(child);
        child.setParent(this);
    }
}
```
* `@ManyToOne`에는 `FetchType.LAZY`를 사용해 지연 로딩 설정
* `@JoinColumn`을 사용해 연관관계에서 외래키를 매핑한다.
<br/>

### OrderItem
```java
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") 
    private Order order;

    private int orderPrice;
    private int count;

    // ==생성 메소드==
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        item.removeStock(count);
        return orderItem;
    }

    // ==비즈니스 로직==
    public void cancel() {
        getItem().addStock(count);
    }

    //==조회 로직==

    /**
     * 주문 상품 가격 조회
     */
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
}
```
* `@NoArgsConstructor`를 사용해 파라미터가 없는 생성자를 만들어준다.
* `Item`, `Order` 엔티티에 지연 로딩을 적용한 `ManyToOne`과 `JoinColumn`을 사용해 연관관계를 설정하고, 외래 키를 매핑한다.
* 생성 메소드, 취소 비즈니스 로직, 주문상품 가격조회 로직을 엔티티에 구현한다.
<br/>

### Order
```java

@Entity
@Getter
@Setter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Many-to-one 설정
    @JoinColumn(name = "member_id") // FK 설정
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate; // 주문 시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // 주문 상태 [ORDER, CANCEL]

    // ==연관관계 메소드==
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    //==생성메소드==
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }

        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    // ==비즈니스 로직==

    /**
     * 주문 취소
     */
    public void cancel() {
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송 완료된 상품은 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    // ==조회 로직==

    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {
        return orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }
}
```
* `@Table(name = "orders")`를 사용해 테이블 명을 orders로 지정
* `Member` 엔티티와 Many-To-One 관계를 설정한다. order 여러개는 하나의 멤버와 연관될 수 있다. 또한, `@JoinColumn`을 사용해 FK를 설정한다.
* `OrderItem` 엔티티와 One-To-Many 관계를 설정한다. 하나의 Order는 여러개의 OrderItem을 가질 수 있고, Order의 변경사항이 OrderItem에 Cascade된다.
* `Delivery` 엔티티와는 One-To-One 관계를 설정한다. 주문은 하나의 배송 정보를 생성하므로, 일대일 관계이다.
* `@Enumerated(EnumType.STRING)` 애노테이션을 사용해, 주문 상태를 ENUM으로 관리한다.
<br/>

### Member, Delivery 엔티티는 생략.

## Repository
### ItemRepository
```java
@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {
        if (item.getId() == null) {
            em.persist(item);
        } else {
            em.merge(item); // update와 유사?
        }
    }

    public Item findById(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }
}
```
* `private final EntityManager em;` : JPA를 위해, EntityManager를 주입 받는다.
* `save` : item의 id가 null일 때, 해당 데이터는 새로운 엔티티이므로 영속화한다. 식별자가 있다면 병합한다. 병합은 모든 데이터를 변경하게 되므로, 사용에 주의해야 한다.
<br/>

### 나머지 생략

## Controller
### OrderController
```java
@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;

    @GetMapping(value = "/order")
    public String createForm(Model model) {
        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();
        model.addAttribute("members", members);
        model.addAttribute("items", items);
        return "order/orderForm";
    }


    @PostMapping(value = "/order")
    public String order(@RequestParam("memberId") Long memberId,
                        @RequestParam("itemId") Long itemId,
                        @RequestParam("count") int count) {
        orderService.order(memberId, itemId, count);
        return "redirect:/orders";
    }

    @GetMapping(value = "/orders")
    public String orderList(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
        List<Order> orders = orderService.findOrders(orderSearch);
        model.addAttribute("orders", orders);
        return "order/orderList";
    }

    @PostMapping(value = "/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return "redirect:/orders";
    }
}
```
* `@ModelAttribute` : controller에서 endpoint로 받을 파라미터에 사용한다. 적절한 생성자를 찾아, setter 메소드를 찾아서 실행시킨다.
* `@RequestParam` : URI를 통해 전송된 쿼리스트링을 받는다. `.../user?id=a&count=3`
* `@PathVariable` : URI 변수를 통해 전송된 값을 받는다. `.../user/[id]`
<br/>

### 나머지는 생략

## Service
### MemberService
```java

@Service
@Transactional(readOnly = true)  // 데이터 변경 -> transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원 가입
     */
    @Transactional // readonly false 설정.
    public Long join(Member member) {
        // 이름이 중복인 멤버가 존재하면 오류 발생
        validateDuplicateMember(member);
        memberRepository.save(member);

        return member.getId();
    }

    /**
     * 회원 전체 조회
     */
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    /**
     * 회원 단건 조회
     */
    public Member findOne(Long id) {
        return memberRepository.findOne(id);
    }


    private void validateDuplicateMember(Member member) {
        // Exception
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }
}
* `@Transactional` : 트랜젝션을 적용한다. readonly 옵션을 활성화할 경우, 읽기 전용 트랜젝션이 되므로 데이터 업데이트가 불가능해진다. 하지만 조회만 할 땐 여러 면에서 장점이 존재하므로, 메소드 레벨의 트랜젝션 수준을 파악한 뒤, 잘 설정해주자.
```
<br/>


### 도메인 주도 설계 vs 서비스 위주 개발
어느쪽이 정답인가.. 아직 잘 모르겠음.\
https://www.inflearn.com/questions/117315/%EB%B9%84%EC%A7%80%EB%8B%88%EC%8A%A4-%EB%A1%9C%EC%A7%81%EA%B5%AC%ED%98%84-entity-vs-service