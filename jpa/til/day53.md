## 애플리케이션 구현 준비

### 구현 요구사항
![image](https://github.com/jub3907/Spring-study/assets/58246682/2020f306-6d31-4e1f-8cb1-00f71237f776)

* 회원 기능
  * 회원 등록
  * 회원 조회
* 상품 기능
  * 상품 등록
  * 상품 수정
  * 상품 조회
* 주문 기능
  * 상품 주문
  * 주문 내역 조회
  * 주문 취소

다만, **예제를 단순화 하기 위해 다음 기능은 구현X**
* 로그인과 권한 관리X
* 파라미터 검증과 예외 처리X
* 상품은 도서만 사용
* 카테고리는 사용X
* 배송 정보는 사용X
<br/>

### 애플리케이션 아키텍처
![image](https://github.com/jub3907/Spring-study/assets/58246682/7c76b5ce-566d-476c-a2a5-b0282f3e7222)
<br/>
<br/>

#### 계층형 구조 사용
* controller, web: 웹 계층
* service: 비즈니스 로직, 트랜잭션 처리
* repository: JPA를 직접 사용하는 계층, 엔티티 매니저 사용
* domain: 엔티티가 모여 있는 계층, 모든 계층에서 사용
<br/>

#### 패키지 구조
* jpabook.jpashop
  * domain
  * exception
  * repository
  * service
  * web
<br/>

#### 개발 순서
서비스, 리포지토리 계층을 개발하고,\
테스트 케이스를 작성해서 검증, \
마지막에 웹 계층 적용.
<br/>

## 회원 도메인 개발
* **구현 기능**
  * 회원 등록
  * 회원 목록 조회
* **순서**
  * 회원 엔티티 코드 다시 보기
  * 회원 리포지토리 개발
  * 회원 서비스 개발
  * 회원 기능 테스트
<br/>

### 회원 리포지토리 개발
#### 회원 리포지토리 코드
```java
@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

//    @PersistenceUnit
//    private EntityManagerFactory emf

    public void save(Member member) {
        em.persist(member);
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
```
<br/>

#### 기술 설명
* @Repository : 스프링 빈으로 등록, JPA 예외를 스프링 기반 예외로 예외 변환
* @PersistenceContext : 엔티티 메니저( EntityManager ) 주입
* @PersistenceUnit : 엔티티 메니터 팩토리( EntityManagerFactory ) 주입
<br/>

#### 기능 설명
* save()
* findOne()
* findAll()
* findByName()
<br/>

### 회원 서비스 개발
#### 회원 서비스 코드
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
```
<br/>

#### 기술 설명
* @Service
* @Transactional : 트랜잭션, 영속성 컨텍스트
  * readOnly=true : 데이터의 변경이 없는 읽기 전용 메서드에 사용, \
    영속성 컨텍스트를 플러시 하지 않으므로 약간의 성능 향상\
    (읽기 전용에는 다 적용)
  * 데이터베이스 드라이버가 지원하면 DB에서 성능 향상
* @Autowired
  * 생성자 Injection 많이 사용, 생성자가 하나면 생략 가능
<br/>

#### 기능 설명
* join()
* findMembers()
* findOne()
<br/>

> 실무에서는 검증 로직이 있어도 멀티 쓰레드 상황을 고려해서 \
> 회원 테이블의 회원명 컬럼에 유니크 제약 조건을 추가하는 것이 안전하다.

> 스프링 필드 주입 대신에 생성자 주입을 사용하자.
<br/>

#### 필드 주입
```java
public class MemberService {
    @Autowired
    MemberRepository memberRepository;
    ...
}
```
<br/>

#### 생성자 주입
```java
public class MemberService {
    private final MemberRepository memberRepository;
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    ...
}
```
* 생성자 주입 방식을 권장
* 변경 불가능한 안전한 객체 생성 가능
* 생성자가 하나면, @Autowired 를 생략할 수 있다.
* final 키워드를 추가하면 컴파일 시점에 memberRepository 를 \
  설정하지 않는 오류를 체크할 수 있다.\
  (보통 기본 생성자를 추가할 때 발견)
<br/>

#### lombok
```java
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    ...
}
```
스프링 데이터 JPA를 사용하면 EntityManager 도 주입 가능
```java
@Repository
@RequiredArgsConstructor
public class MemberRepository {
    private final EntityManager em;
    ...
}
```
<br/>


### 회원 기능 테스트
#### 테스트 요구사항
* 회원가입을 성공해야 한다.
* 회원가입 할 때 같은 이름이 있으면 예외가 발생해야 한다
<br/>

#### 회원가입 테스트 코드
```java
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired MemberService memberService;

    @Autowired MemberRepository memberRepository;

    @Test
    public void 회원가입() {
        // given
        Member member = new Member();
        member.setName("kim");

        // when
        memberRepository.save(member);

        // then
        assertThat(member).isEqualTo(memberRepository.findOne(member.getId()));
    }

    @Test
    public void 중복_회원_예외() {
        // given
        Member member1 = new Member();
        member1.setName("kim");

        Member member2 = new Member();
        member2.setName("kim");

        // when
        memberService.join(member1);

        // then
        assertThatThrownBy(() -> memberService.join(member2))
                .isInstanceOf(IllegalStateException.class);

    }
}
```
<br/>

#### 기술 설명
* @RunWith(SpringRunner.class) : 스프링과 테스트 통합
* @SpringBootTest : 스프링 부트 띄우고 테스트(이게 없으면 @Autowired 다 실패)
* @Transactional : 반복 가능한 테스트 지원, 각각의 테스트를 실행할 때마다 \
  트랜잭션을 시작하고 **테스트가 끝나면 트랜잭션을 강제로 롤백** \
  (이 어노테이션이 테스트 케이스에서 사용될 때만 롤백)
<br/>

#### 기능 설명
* 회원가입 테스트
* 중복 회원 예외처리 테스트
<br/>

> 테스트 케이스 작성 고수 되는 마법: Given, When, Then\
> (http://martinfowler.com/bliki/GivenWhenThen.html)\
> 이 방법이 필수는 아니지만 이 방법을 기본으로 해서 다양하게 응용하는 것을 권장한다.

### 테스트 케이스를 위한 설정
테스트는 케이스 격리된 환경에서 실행하고, 끝나면 데이터를 초기화하는 것이 좋다.\
그런 면에서 메모리 DB를 사용하는 것이 가장 이상적이다.

추가로 테스트 케이스를 위한 스프링 환경과, 일반적으로 \
애플리케이션을 실행하는 환경은 보통 다르므로 설정 파일을 다르게 사용하자.

다음과 같이 간단하게 테스트용 설정 파일을 추가하면 된다.

* `test/resources/application.yml`
  ```java
  spring:
  # datasource:
  # url: jdbc:h2:mem:testdb
  # username: sa
  # password:
  # driver-class-name: org.h2.Driver
  # jpa:
  # hibernate:
  # ddl-auto: create
  # properties:
  # hibernate:
      # show_sql: true
  # format_sql: true
  # open-in-view: false
  logging.level:
      org.hibernate.SQL: debug
  # org.hibernate.type: trace
  ```

이제 테스트에서 스프링을 실행하면 이 위치에 있는 설정 파일을 읽는다.\
(만약 이 위치에 없으면 src/resources/application.yml 을 읽는다.)

스프링 부트는 datasource 설정이 없으면, 기본적을 메모리 DB를 사용하고,\
driver-class도 현재 등록된 라이브러를 보고 찾아준다. \
추가로 ddl-auto 도 create-drop 모드로 동작한다. \
따라서 데이터소스나, JPA 관련된 별도의 추가 설정을 하지 않아도 된다.
<br/>
<br/>

## 상품 도메인 개발
#### 구현 기능
* 상품 등록
* 상품 목록 조회
* 상품 수정
<br/>

#### 순서
* 상품 엔티티 개발(비즈니스 로직 추가)
* 상품 리포지토리 개발
* 상품 서비스 개발
* 상품 기능 테스트
<br/>

### 상품 엔티티 개발(비즈니스 로직 추가)
#### 상품 엔티티 코드
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
<br/>

#### 예외 추가
```java
public class NotEnoughStockException extends RuntimeException {
    public NotEnoughStockException() { }

    public NotEnoughStockException(String message) {
        super(message);
    }

    public NotEnoughStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnoughStockException(Throwable cause) {
        super(cause);
    }

    public NotEnoughStockException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

```
<br/>

#### 비즈니스 로직 분석
addStock() 메서드는 파라미터로 넘어온 수만큼 재고를 늘린다. \
이 메서드는 재고가 증가하거나 상품 주문을 취소해서 \
재고를 다시 늘려야 할 때 사용한다.

removeStock() 메서드는 파라미터로 넘어온 수만큼 재고를 줄인다. \
만약 재고가 부족하면 예외가 발생한다. \
주로 상품을 주문할 때 사용한다.
<br/>
<br/>

### 상품 리포지토리 개발
#### 상품 리포지토리 코드
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
<br/>

#### 기능 설명
* save()
  * id 가 없으면 신규로 보고 persist() 실행
  * id 가 있으면 이미 데이터베이스에 저장된 엔티티를 수정한다고 보고, \
    merge() 를 실행, 자세한 내용은 뒤에 웹에서 설명\
    (그냥 지금은 저장한다 정도로 생각하자)
<br/>

### 상품 서비스 개발
#### 상품 서비스 코드
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findById(itemId);
    }
}
```
상품 서비스는 상품 리포지토리에 단순히 위임만 하는 클래스.
<br/>
<br/>
