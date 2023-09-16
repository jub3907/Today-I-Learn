## 주문 도메인 개발
#### 구현 기능
* 상품 주문
* 주문 내역 조회
* 주문 취소
<br/>

#### 순서
* 주문 엔티티, 주문상품 엔티티 개발
* 주문 리포지토리 개발
* 주문 서비스 개발
* 주문 검색 기능 개발
* 주문 기능 테스트
<br/>

### 주문, 주문상품 엔티티 개발
#### 주문 엔티티 코드
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

    @OneToOne(fetch = FetchType.LAZY)
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
<br/>
<br/>

#### 기능 설명
* **생성 메서드**( `createOrder()` ): 주문 엔티티를 생성할 때 사용한다. \
  주문 회원, 배송정보, 주문상품의 정보를 받아서 \
  실제 주문 엔티티를 생성한다.


* **주문 취소**( `cancel()` ): 주문 취소시 사용한다. \
  주문 상태를 취소로 변경하고 주문상품에 주문 취소를 알린다. \
  만약 이미 배송을 완료한 상품이면 주문을 취소하지 못하도록 예외를 발생시킨다.

* **전체 주문 가격 조회**: 주문 시 사용한 전체 주문 가격을 조회한다. \
  전체 주문 가격을 알려면 각각의 주문상품 가격을 알아야 한다. \
  로직을 보면 연관된 주문상품들의 가격을 조회해서 더한 값을 반환한다. \
  (실무에서는 주로 주문에 전체 주문 가격 필드를 두고 역정규화 한다.)
<br/>

### 주문상품 엔티티 개발
#### 주문상품 엔티티 코드
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
    @JoinColumn(name = "order_id") // 연관관계 주인을 order 테이블로 설정
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
<br/>

#### 기능 설명
* **생성 메서드**( `createOrderItem()` ): 주문 상품, 가격, 수량 정보를 사용해서 \
  주문상품 엔티티를 생성한다. 그리고 item.removeStock(count) 를 호출해서 \
  주문한 수량만큼 상품의 재고를 줄인다.
* **주문 취소**( `cancel()` ): getItem().addStock(count) 를 호출해서 \
  취소한 주문 수량만큼 상품의 재고를 증가시킨다.
* **주문 가격 조회**( `getTotalPrice()` ): 주문 가격에 수량을 곱한 값을 반환한다.
<br/>

### 주문 리포지토리 개발
#### 주문 리포지토리 코드
```java
@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

//    public List<Order> findAll(OrderSearch orderSearch) {
//        
//    }
}

```
주문 리포지토리에는 주문 엔티티를 저장하고 검색하는 기능이 있다. \
마지막의 findAll(OrderSearch orderSearch) 메서드는 \
조금 뒤에 있는 주문 검색 기능에서 자세히 알아보자.
<br/>
<br/>

### 주문 서비스 개발
#### 주문 서비스 코드
```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {

        // 엔티티 조회
        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findById(itemId);

        // 배송 정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());

        // 주문 정보 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        // 주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        // 주문 저장
        // cascade 옵션을 사용했기 때문에, order가 persist 될 때
        // 하위에 있는 orderItems, delivery도 persist 된다.
        // 이는 order가 orderItem, Delivery를 관리하기 떄문에 사용한다.
        // 즉 private owner일 때만 사용해야 한다. ( lifecycle을 동일하게 관리할 떄? )
        orderRepository.save(order);

        return order.getId();
    }

    // 취소
    @Transactional
    public void cancelOrder(Long orderId) {

        // 주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);

        // 주문 취소
        order.cancel();

        // JPA를 사용중이기 떄문에, 변경한 뒤 업데이트를 따로 해주지 않아도 된다.
    }


    // 검색
//    public List<Order> findOrders(OrderSearch orderSearch) {
//        return orderRepository.findAll(orderSearch);
//    }
}
```
주문 서비스는 주문 엔티티와 주문 상품 엔티티의 비즈니스 로직을 활용해서 \
주문, 주문 취소, 주문 내역 검색 기능을 제공한다.

> 예제를 단순화하려고 한 번에 하나의 상품만 주문할 수 있다

* **주문**( `order()` ): 주문하는 회원 식별자, 상품 식별자, \
  주문 수량 정보를 받아서 실제 주문 엔티티를 생성한 후 저장한다.
* **주문 취소**( ` ()` ): 주문 식별자를 받아서 \
  주문 엔티티를 조회한 후 주문 엔티티에 주문 취소를 요청한다.
* **주문 검색**( `findOrders()` ): OrderSearch 라는 \
  검색 조건을 가진 객체로 주문 엔티티를 검색한다.\
  자세한 내용은 다음에 나오는 주문 검색 기능에서 알아보자.
<br/>

#### 참고사항
주문 서비스의 주문과 주문 취소 메서드를 보면 비즈니스 로직 대부분이 엔티티에 있다. \
서비스 계층 은 단순히 엔티티에 필요한 요청을 위임하는 역할을 한다. \
이처럼 엔티티가 비즈니스 로직을 가지고 객체 지향의 특성을 적극 활용하는 것을 \
도메인 모델 패턴(http://martinfowler.com/eaaCatalog/domainModel.html)이라 한다. \
반대로 엔티티에는 비즈니스 로직이 거의 없고 서비스 계층에서 \
대부분 의 비즈니스 로직을 처리하는 것을 \
트랜잭션 스크립트 패턴(http://martinfowler.com/eaaCatalog/transactionScript.html)이라 한다.
<br/>
<br/>


### 주문 기능 테스트
#### 테스트 요구사항
* 상품 주문이 성공해야 한다.
* 상품을 주문할 때 재고 수량을 초과하면 안 된다.
* 주문 취소가 성공해야 한다.
<br/>

#### 상품 주문 테스트 코드
```java
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;


    @Test
    public void 상품주문() {
        // given
        Member member = createMember("member1");

        Book book = createBook("시공 JPA 5", 10000, 10);

        int orderCount = 2;

        // when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // then
        Order findOrder = orderRepository.findOne(orderId);
        assertThat(findOrder.getStatus()).isEqualTo(OrderStatus.ORDER);
        assertThat(findOrder.getOrderItems().size()).isEqualTo(1);
        assertThat(findOrder.getTotalPrice()).isEqualTo(10000 * orderCount);
        assertThat(book.getStockQuantity()).isEqualTo(8);
    }

    @Test
    public void 상품주문_재고수량초과() {
        // ...
    }

    @Test
    public void 주문취소() {
        // ...
    }


    private Book createBook(String name, int price, int quantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(quantity);
        em.persist(book);
        return book;
    }

    private Member createMember(String name) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(new Address("서울", "강가", "123-123"));
        em.persist(member);
        return member;
    }
}
```
상품주문이 정상 동작하는지 확인하는 테스트다. \
Given 절에서 테스트를 위한 회원과 상품을 만들고 When 절에서 \
실제 상품을 주문하고 Then 절에서 주문 가격이 올바른지, \
주문 후 재고 수량이 정확히 줄었는지 검증한다
<br/>

#### 재고 수량 초과 테스트
재고 수량을 초과해서 상품을 주문해보자. \
이때는 NotEnoughStockException 예외가 발생해야 한다
<br/>
<br/>

#### 재고 수량 초과 테스트 코드
```java
@Test
public void 상품주문_재고수량초과() {
    // given
    Member member = createMember("member1");

    Book book = createBook("시공 JPA 5", 10000, 10);

    int orderCount = 11;

    // when

    // then
    assertThatThrownBy(() -> orderService.order(member.getId(), book.getId(), orderCount))
            .isInstanceOf(NotEnoughStockException.class);
}
```
코드를 보면 재고는 10권인데 orderCount = 11 로 재고보다 1권 더 많은 수량을 주문했다. \
주문 초과로 다음 로직에서 예외가 발생한다.
```java
public abstract class Item {
    //...

    public void removeStock(int orderQuantity) {
    int restStock = this.stockQuantity - orderQuantity;
    if (restStock < 0) {
        throw new NotEnoughStockException("need more stock");
    }
    this.stockQuantity = restStock;
    }
}
```
<br/>

#### 주문 취소 테스트
주문 취소 테스트 코드를 작성하자. \
주문을 취소하면 그만큼 재고가 증가해야 한다
<br/>
<br/>

#### 주문 취소 테스트 코드
```java
@Test
public void 주문취소() {
    // given
    Member member = createMember("member1");
    Book book = createBook("시공 JPA 5", 10000, 10);

    int orderCount = 2;
    Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

    // when
    orderService.cancelOrder(orderId);

    // then
    Order order = orderRepository.findOne(orderId);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCEL);
    assertThat(book.getStockQuantity()).isEqualTo(10);
}
```
주문을 취소하려면 먼저 주문을 해야 한다. \
Given 절에서 주문하고 When 절에서 해당 주문을 취소했다.
Then 절에서 주문상태가 주문 취소 상태인지( CANCEL ), \
취소한 만큼 재고가 증가했는지 검증한다.
<br/>

### 주문 검색 기능 개발
JPA에서 동적 쿼리를 어떻게 해결해야 하는가?

![image](https://github.com/jub3907/outSourcing/assets/58246682/4f0d7481-a64f-4279-8487-55a37a21a804)

#### 검색 조건 파라미터 OrderSearch
```java
@Getter
@Setter
public class OrderSearch {

    private String memberName;
    private OrderStatus orderStatus; // 주문 상태[ORDER, CANCEL]
}
```
<br/>

#### 검색을 추가한 주문 리포지토리 코드
```java
@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAll(OrderSearch orderSearch) {
      //...
    }
}
```
findAll(OrderSearch orderSearch) 메서드는 검색 조건에 \
동적으로 쿼리를 생성해서 주문 엔티티를 조회한다.
<br/>
<br/>

#### JPQL로 처리
```java
public List<Order> findAllByString(OrderSearch orderSearch) {
    String jpql = "select o From Order o join o.member m";
    boolean isFirstCondition = true;

    //주문 상태 검색
    if (orderSearch.getOrderStatus() != null) {
        if (isFirstCondition) {
            jpql += " where";
            isFirstCondition = false;
        } else {
            jpql += " and";
        }
        jpql += " o.status = :status";
    }

    //회원 이름 검색
    if (StringUtils.hasText(orderSearch.getMemberName())) {
        if (isFirstCondition) {
            jpql += " where";
            isFirstCondition = false;
        } else {
            jpql += " and";
        }
        jpql += " m.name like :name";
    }

    TypedQuery<Order> query = em.createQuery(jpql, Order.class)
            .setMaxResults(1000); //최대 1000건

    if (orderSearch.getOrderStatus() != null) {
        query = query.setParameter("status", orderSearch.getOrderStatus());
    }

    if (StringUtils.hasText(orderSearch.getMemberName())) {
        query = query.setParameter("name", orderSearch.getMemberName());
    }

    return query.getResultList();
}
```
JPQL 쿼리를 문자로 생성하기는 번거롭고, \
실수로 인한 버그가 충분히 발생할 수 있다.
<br/>
<br/>

#### JPA Criteria로 처리
```java
/**
 * JPA Criteria
 */
public List<Order> findAllByCriteria(OrderSearch orderSearch) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Order> cq = cb.createQuery(Order.class);
    Root<Order> o = cq.from(Order.class);
    Join<Object, Object> m = o.join("member", JoinType.INNER);

    List<Predicate> criteria = new ArrayList<>();

    //주문 상태 검색
    if (orderSearch.getOrderStatus() != null) {
        Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
        criteria.add(status);
    }

    //회원 이름 검색
    if (StringUtils.hasText(orderSearch.getMemberName())) {
        Predicate name = cb.like(m.<String>get("name"),
                "%" + orderSearch.getMemberName() + "%");

        criteria.add(name);
    }

    cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
    TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건

    return query.getResultList();
}
```
JPA Criteria는 JPA 표준 스펙이지만 실무에서 사용하기에 너무 복잡하다. \
결국 다른 대안이 필요하다. 많은 개발자가 비슷한 고민을 했지만, \
가장 멋진 해결책은 Querydsl이 제시했다. 

Querydsl 소개장에서 간단히 언급하겠다. 지금은 이대로 진행하자.
<br/>
<br/>

## 변경 감지와 병합

### 준영속 엔티티?
영속성 컨텍스트가 더는 관리하지 않는 엔티티를 말한다.
(여기서는 itemService.saveItem(book) 에서 수정을 시도하는 Book 객체다. \
Book 객체는 이미 DB 에 한번 저장되어서 식별자가 존재한다. \
이렇게 임의로 만들어낸 엔티티도 기존 식별자를 가지고 있으면 \
준영속 엔티티로 볼 수 있다.)
<br/>
<br/>

### 준영속 엔티티를 수정하는 2가지 방법
* 변경 감지 기능 사용
* 병합( merge ) 사용
<br/>


### 변경 감지 기능 사용
```java
@Transactional
void update(Item itemParam) { //itemParam: 파리미터로 넘어온 준영속 상태의 엔티티
    Item findItem = em.find(Item.class, itemParam.getId()); //같은 엔티티를 조회한다.
    findItem.setPrice(itemParam.getPrice()); //데이터를 수정한다.
}
```
* 영속성 컨텍스트에서 엔티티를 다시 조회한 후에 데이터를 수정하는 방법
    * 트랜잭션 안에서 엔티티를 다시 조회, 변경할 값 선택\
      -> 트랜잭션 커밋 시점에 변경 감지(Dirty Checking)
이 동작해서 데이터베이스에 UPDATE SQL 실행
<br/>

### 병합 사용
병합은 준영속 상태의 엔티티를 영속 상태로 변경할 때 사용하는 기능이다.

```java
@Transactional
void update(Item itemParam) { //itemParam: 파리미터로 넘어온 준영속 상태의 엔티티
    Item mergeItem = em.merge(itemParam);
}
```
<br/>

### 병합 동작 방식
1. merge() 를 실행한다.
2. 파라미터로 넘어온 준영속 엔티티의 식별자 값으로 1차 캐시에서 엔티티를 조회한다.
3. 만약 1차 캐시에 엔티티가 없으면 데이터베이스에서 엔티티를 조회하고, 1차 캐시에 저장한다.
4. 조회한 영속 엔티티( mergeMember )에 member 엔티티의 값을 채워 넣는다. \
    (member 엔티티의 모든 값을 mergeMember에 밀어 넣는다. \
    이때 mergeMember의 “회원1”이라는 이름이 “회원명변경”으로 바뀐다.)
5. 영속 상태인 mergeMember를 반환한다.
<br/>

### 병합시 동작 방식을 간단히 정리
1. 준영속 엔티티의 식별자 값으로 영속 엔티티를 조회한다.
2. 영속 엔티티의 값을 준영속 엔티티의 값으로 모두 교체한다.(병합한다.)
3. 트랜잭션 커밋 시점에 변경 감지 기능이 동작해서 데이터베이스에 UPDATE SQL이 실행
<br/>
<br/>

> 주의: 변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만, \
> 병합을 사용하면 모든 속성이 변경된다. \
> 병합시 값이 없으면 null 로 업데이트 할 위험도 있다. \
> (병합은 모든 필드를 교체한다.)
<br/>

기존에 작성한 itemRepository의 코드를 살펴보자.
```java
public void save(Item item) {
    if (item.getId() == null) {
        em.persist(item);
    } else {
        em.merge(item);
    }
}
```
* save() 메서드는 식별자 값이 없으면( null ) 새로운 엔티티로 판단해서 \
    영속화(persist)하고 식별자가 있으면 병합(merge)
* 지금처럼 준영속 상태인 상품 엔티티를 수정할 때는 id 값이 있으므로 병합 수행
<br/>

#### 새로운 엔티티 저장과 준영속 엔티티 병합을 편리하게 한번에 처리
상품 리포지토리에선 save() 메서드를 유심히 봐야 하는데, \
이 메서드 하나로 저장과 수정(병합)을 다 처리한다. \
코드를 보면 식별자 값이 없으면 새로운 엔티티로 판단해서 persist() 로 영속화하고 \
만약 식별자값이 있으면 이미 한번 영속화 되었던 엔티티로 판단해서 \
merge() 로 수정(병합)한다. 결국 여기서의 저장(save)이라는 의미는 \
신규 데이터를 저장하는 것뿐만 아니라 변경된 데이터의 저장이라는 의미도 포함한다.

이렇게 함으로써 이 메서드를 사용하는 클라이언트는 저장과 수정을 \
구분하지 않아도 되므로 클라이언트의 로직이 단순해진다.

여기서 사용하는 수정(병합)은 준영속 상태의 엔티티를 수정할 때 사용한다.\
영속 상태의 엔티티는 변경 감지(dirty checking)기능이 동작해서 트랜잭션을 커밋할 때 \
자동으로 수정되므로 별도의 수정 메서드를 호출할 필요가 없고 그런 메서드도 없다.
<br/>
<br/>

#### 참고
save() 메서드는 식별자를 자동 생성해야 정상 동작한다. \
여기서 사용한 Item 엔티티의 식별자는 자동으로 생성되도록\
@GeneratedValue 를 선언했다. 따라서 식별자 없이 save() 메서드를 호출하면\
persist() 가 호출되면서 식별자 값이 자동으로 할당된다. 

반면에 식별자를 직접 할당하도록 @Id 만 선언했다고 가정하자. \
이 경우 식별자를 직접 할당하지 않고, save() 메서드를 호출하면 \
식별자가 없는 상태로 persist() 를 호출한다. \
그러면 식별자가 없다는 예외가 발생한다.

실무에서는 보통 업데이트 기능이 매우 제한적이다. \
그런데 병합은 모든 필드를 변경해버리고, 데이터가 없으면 null 로 업데이트 해버린다. \
병합을 사용하면서 이 문제를 해결하려면, 변경 폼 화면에서 모든 데이터를 항상 유지해야 한다. 실무에서는 보통 변경가능한 데이터만 노출하기 때문에, \
병합을 사용하는 것이 오히려 번거롭다.
<br/>
<br/>


### 가장 좋은 해결 방법
**엔티티를 변경할 때는 항상 변경 감지를 사용하자.**

* 컨트롤러에서 어설프게 엔티티를 생성하지 말자.
* 트랜잭션이 있는 서비스 계층에 식별자( id )와 \
    변경할 데이터를 명확하게 전달하자.(파라미터 or dto)
* 트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회하고, \
    엔티티의 데이터를 직접 변경하자.
* 트랜잭션 커밋 시점에 변경 감지가 실행된다.