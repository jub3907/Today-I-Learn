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