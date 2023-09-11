## 기존 방식의 문제점
### Query의 문제점
QUERY는 문자로, Type Check가 불가능하다.\
또한, 실행하기 전까지 작동 여부를 확인할 수 없다.

우리가 개발하며 발생하는 에러는 크게 두 가지가 존재한다.
* 컴파일 에러 (좋은 에러)
* 런타임 에러 (나쁜 에러)
QUERY는 바로 **런타임 에러**에 속한다.

또한 우리는 DB에 저장된 모든 컬럼명을 외울 수 없다.\
이 때, SQL이 클래스처럼 타입이 존재하고,\
자바 코드로 작성할 수 있다면 어떨까?

이런 방식을 Type-Safe라고 한다.\
Type-Safe 방식을 사용하면 컴파일시 에러 체크가 가능해지고, \
code assistant가 가능해진다는 크나큰 장점이 존재한다.
<br/>
<br/>

### QueryDSL
QueryDSL이 바로 **쿼리를 Java로 Type-Safe하게 개발할 수 있게 지원**하는 프레임워크이다.\
주로, JPA 쿼리(JPQL)에 사용한다.
<br/>
<br/>

### JPA Query 예시
다음과 같은 사람을 찾는다고 가정해보자.
* 20 ~ 40살
* 김씨
* 나이가 많은 순서
* 3명 출력

이 때, JPA에서 Query 방법은 크게 3가지가 존재한다.
1. JPQL(HQL)
2. CriteriaAPI
3. MetaModel Criteria API(type-safe)
<br/>

#### 1. JPQL(HQL)
```java
@Test
public voidjpql(){

    Stringquery=
    "select m from Member m"+
    " where m.age between 20 and 40"+
    " and m.name like '김%'"+
    " order by m.age desc";

    List<Member> resultList = entityManager
                  .createQuery(query, Member.class)
                  .setMaxResults(3)
                  .getResultList();

}
```
JPQL은 SQL QUERY와 비슷해서 금방 익숙해진다는 장점이 있지만,\
Type-Safe가 아니며, 동적 쿼리 생성이 어렵다는 단점이 존재한다.

#### 2. Criteria API
![image](https://github.com/jub3907/Spring-study/assets/58246682/4e3ae53d-7b64-4f20-b98a-c1065e0257cc)

Criteria API는 동적 쿼리 생성이 나름 쉽지만,\
이 역시 Type-Safe가 아니고, 너무나도 복잡하고, 알아야할 것이 너무나도 많다.
<br/>
<br/>

#### 3. MetaModel Criteria API(type-safe)
Criteria API에서 더 나아가서, MetaModel이라는 것을 제공한다.\
root.get("age") -> root.get(Member_.age)

하지만, 이 또한 복잡하긴 마찬가지이다.
<br/>
<br/>

## Querydsl의 사용
### QueryDSL
DSL은 Domain(도메인) + Specific(특화) + Language(언어)의 약자로,\
특정한 도메인에 초점을 맞춘, 제한적인 표현력을 가진 컴퓨터 프로그래밍 언어를 뜻한다.\
단순하고, 간결하고, 유창하다는 장점이 존재한다.

따라서 QueryDSL이란 **쿼리**에 특화된 프로그래밍 언어를 의미한다.\
다양한 저장소 쿼리 기능을 통합하여 제공한다.

즉, 모든 저장소의 조회 기능을 추상화 해보기 위해 나오게 되었다.\
![image](https://github.com/jub3907/Spring-study/assets/58246682/3e0824df-c731-4a24-9c08-82bb3716d701)\

한 마디로 축약 하자면, QueryDSL은\
JPA, MongoDB, SQL같은 기술들을 위해 Type-Safe SQL을 만드는 프레임워크 이다.
<br/>
<br/>

### 예제
앞서 했던 예제와 동일하게, 다음과 같은 사람을 찾는다고 가정해보자.
* 20 ~ 40살
* 김씨
* 나이가 많은 순서
* 3명 출력
<br/>

QueryDSL-JPA를 사용해 다음과 같은 코드가 자동적으로 생성된다.\
![image](https://github.com/jub3907/Spring-study/assets/58246682/d85fb642-02b6-441e-9a99-577561d4148b)

```
@Generated("...")
public class QMember extends EntityPathBase<Member>{
    public finalNumberPath<Long> id = createNumber("id", Long.class);
    public finalNumberPath<Integer> age = createNumber("age", Integer.class);
    public final String Pathname = createString("name");

    public static final QMember member = new QMember("member");
    ...
}
```

이후, 다음과 같이 QueryDSL을 사용하면 된다.

```java
JPAQueryFactoryquery=newJPAQueryFactory(entityManager);
QMemberm=QMember.member;

List<Member>list=query
    .select(m)
    .from(m)
    .where(
        m.age.between(20,40).and(m.name.like("김%"))
    )
    .orderBy(m.age.desc())
    .limit(3)
    .fetch(m);
```
<br/>

#### 작동 방식
![image](https://github.com/jub3907/Spring-study/assets/58246682/a69eb821-af2c-4536-bbf1-2ed5d964b68a)
<br/>
<br/>

#### 장단점
Type-Safe하고, 단순하고, 쉽다는 장점이 있다.\
다만, Q코드 생성을 위한 APT(Annotation Process Tool)를 \
설정해야 한다는 단점이 존재한다.
<br/>
<br/>

### QueryDSL-JPA의 기능

#### Query
* from
* innerJoin, join, leftJoin, fullJoin, on
* where(and, or, allOf, anyOf)
* groupBy
* having
* orderBy(desc, asc)
* limit, offset, restrict(limit + offset) (Paging)
* list
* iterate
* count
* fetch():목록조회
* fetchOne():단건조회
<br/>

#### 단순 조회
```java
QMemberm=QMember.member;

List<Member>results = query
    .select(m)
    .from(m)
    .where(m.name.startsWith("김").and(m.age.between(20,40)))
    .fetch(m);
```
<br/>

#### 동적 쿼리
```java
String firstName="김"; int min=20 , max = 40;

BooleanBuilder builder = new BooleanBuilder();
if(StringUtils.hasText(str))
    builder.and(m.name.startsWith(firstName));

if(min != 0 && max != 0)
    builder.and(m.age.between(min,max));

List<Member>results=query
        .select(m)
        .from(m)
        .where(builder)
        .fetch(m);
```
<br/>

#### 조인 쿼리
```java
QMember m=QMember.member;
QMemberCard mc=QMemberCard.memberCard;

List<Member> list = query
        .select(m)
        .from(m).join(m.memberCards,mc)
        .fetch(m);
```
<br/>

#### 페이징, 정렬
```java
List<Member> result = queryFactory
        .selectFrom(member)
        .orderBy(member.username.desc())
        .offset(1)//0부터시작(zeroindex)
        .limit(2)//최대2건조회
        .fetch();
```
<br/>

### SpringDataJPA + Querydsl
SpringData 프로젝트의 약점은 조회이다.\
QueryDSL로 복잡한 조회 기능을 보완한다.

단순한 경우는 SpringDataJPA를 사용하면 되고,\
복잡한 경우 QueryDSL을 직접 사용하면 된다.
<br/>
<br/>


## Querydsl 설정
### build.gradle
```java
plugins {
	id 'org.springframework.boot' version '2.6.5'
	id 'io.spring.dependency-management' version '1.0.11.RELEASE'
	id 'java'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

ext["hibernate.version"] = "5.6.5.Final"

configurations {
	compileOnly {
		extendsFrom annotationProcessor
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	compileOnly 'org.projectlombok:lombok'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'

	//JdbcTemplate 추가
//	implementation 'org.springframework.boot:spring-boot-starter-jdbc'

	//MyBatis 추가
	implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:2.2.0'

	//JPA, 스프링 데이터 JPA 추가
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

	//Querydsl 추가
	implementation 'com.querydsl:querydsl-jpa'
	annotationProcessor "com.querydsl:querydsl-apt:${dependencyManagement.importedProperties['querydsl.version']}:jpa"
	annotationProcessor "jakarta.annotation:jakarta.annotation-api"
	annotationProcessor "jakarta.persistence:jakarta.persistence-api"


	//H2 데이터베이스 추가
	runtimeOnly 'com.h2database:h2'

	//테스트에서 lombok 사용
	testCompileOnly 'org.projectlombok:lombok'
	testAnnotationProcessor 'org.projectlombok:lombok'
}

tasks.named('test') {
	useJUnitPlatform()
}

//Querydsl 추가, 자동 생성된 Q클래스 gradle clean으로 제거
clean {
	delete file('src/main/generated')
}

```

Querydsl로 추가된 부분은 다음 두 부분이다

```java
dependencies {
    //Querydsl 추가
    implementation 'com.querydsl:querydsl-jpa'
    annotationProcessor "com.querydsl:querydsl-apt:${dependencyManagement.importedProperties['querydsl.version']}:jpa"
    annotationProcessor "jakarta.annotation:jakarta.annotation-api"
    annotationProcessor "jakarta.persistence:jakarta.persistence-api"
}

//Querydsl 추가, 자동 생성된 Q클래스 gradle clean으로 제거
clean {
    delete file('src/main/generated')
}
```

### 검증 - Q 타입 생성 확인 방법
Preferences Build, Execution, Deployment Build Tools Gradle

![image](https://github.com/jub3907/Spring-study/assets/58246682/75772094-c9b9-4f42-b931-607780a9cccb)

#### 옵션 선택1 - Gradle - Q타입 생성 확인 방법
* **Gradle IntelliJ 사용법**
  * Gradle -> Tasks -> build -> clean
  * Gradle -> Tasks -> other -> compileJava

* **Gradle 콘솔 사용법**
  * ./gradlew clean compileJava
 
* Q 타입 생성 확인
  * build -> generated -> sources -> annotationProcessor -> \
    java/main 하위에 hello.itemservice.domain.QItem 이 생성되어 있어야 한다

> 참고: Q타입은 컴파일 시점에 자동 생성되므로 버전관리(GIT)에 포함하지 않는 것이 좋다.\
> gradle 옵션을 선택하면 Q타입은 gradle build 폴더 아래에 생성되기 때문에 \
> 여기를 포함하지 않아야 한다. 대부분 gradle build 폴더를 \
> git에 포함하지 않기 때문에 이 부분은 자연스럽게 해결된다. 
<br/>

#### Q타입 삭제
gradle clean 을 수행하면 build 폴더 자체가 삭제된다. \
따라서 별도의 설정은 없어도 된다
<br/>
<br/>

### 옵션 선택2 - IntelliJ IDEA - Q타입 생성 확인 방법
Build -> Build Project 또는 Build -> Rebuild 또는\
main() , 또는 테스트를 실행하면 된다.

src/main/generated 하위에 hello.itemservice.domain.QItem 이 생성되어 있어야 한다.

> 참고: Q타입은 컴파일 시점에 자동 생성되므로 버전관리(GIT)에 포함하지 않는 것이 좋다.
> IntelliJ IDEA 옵션을 선택하면 Q타입은 src/main/generated \
> 폴더 아래에 생성되기 때문에 여기를 포함하지 않는 것이 좋다.
<br/>

#### Q타입 삭제
```java
//Querydsl 추가, 자동 생성된 Q클래스 gradle clean으로 제거
clean {
    delete file('src/main/generated')
}
```
IntelliJ IDEA 옵션을 선택하면 src/main/generated 에 파일이 생성되고,\
필요한 경우 Q파일을 직접 삭제해야 한다.

gradle 에 해당 스크립트를 추가하면 gradle clean 명령어를 실행할 때\
src/main/generated 의 파일도 함께 삭제해준다.
<br/>
<br/>

#### 참고
Querydsl은 이렇게 설정하는 부분이 사용하면서 조금 귀찮은 부분인데, \
IntelliJ가 버전업 하거나 Querydsl의 Gradle 설정이 버전업 하면서 \
적용 방법이 조금씩 달라지기도 한다. 

그리고 본인의 환경에 따라서 잘 동작하지 않기도 한다. \
공식 메뉴얼에 소개 되어 있는 부분이 아니기 때문에, \
설정에 수고로움이 있지만 querydsl gradle 로 검색하면 \
본인 환경에 맞는 대안을 금방 찾을 수 있을 것이다
<br/>
<br/>

## Querydsl 적용
### JpaItemRepositoryV3
```java
@Repository
@Transactional
public class JpaItemRepositoryV3 implements ItemRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    public JpaItemRepositoryV3(EntityManager em) {
        this.em = em;
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public Item save(Item item) {
        em.persist(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = em.find(Item.class, itemId);
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id);
        return Optional.ofNullable(item);
    }

    public List<Item> findAllOld(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

//        QItem item = new QItem("i");
        QItem item = QItem.item;
        BooleanBuilder builder = new BooleanBuilder();

        if (maxPrice != null) {
            builder.and(item.price.loe(maxPrice));
        }

        List<Item> result = query
                .select(item)
                .from(item)
                .where(builder)
                .fetch();

        return result;
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        return query.select(item)
                .from(item)
                .where(likeItemName(itemName), maxPrice(maxPrice))
                .fetch();
    }

    private BooleanExpression maxPrice(Integer maxPrice) {
        if (maxPrice != null) {
            return item.price.loe(maxPrice);
        }

        return null;
    }

    private BooleanExpression likeItemName(String itemName) {
        if (StringUtils.hasText(itemName)) {
            return item.itemName.like("%" + itemName + "%");
        }

        return null;
    }
}
```
<br/>

#### 공통
Querydsl을 사용하려면 JPAQueryFactory 가 필요하다.\
JPAQueryFactory 는 JPA 쿼리인 JPQL을 만들기 때문에 EntityManager 가 필요하다.

설정 방식은 JdbcTemplate 을 설정하는 것과 유사하다.\
참고로 JPAQueryFactory 를 스프링 빈으로 등록해서 사용해도 된다.
<br/>

#### save(), update(), findById()
기본 기능들은 JPA가 제공하는 기본 기능을 사용한다
<br/>
<br/>

#### findAllOld
Querydsl을 사용해서 동적 쿼리 문제를 해결한다.\
BooleanBuilder 를 사용해서 원하는 where 조건들을 넣어주면 된다.\
이 모든 것을 자바 코드로 작성하기 때문에 동적 쿼리를 매우 편리하게 작성할 수 있다.
<br/>
<br/>

#### findAll
앞서 findAllOld 에서 작성한 코드를 깔끔하게 리팩토링 했다. \
다음 코드는 누가 봐도 쉽게 이해할 수 있을 것이다.
```java
List<Item> result = query
        .select(item)
        .from(item)
        .where(likeItemName(itemName), maxPrice(maxPrice))
        .fetch();
```
Querydsl에서 where(A,B) 에 다양한 조건들을 직접 넣을 수 있는데, \
이렇게 넣으면 AND 조건으로 처리된다. \
참고로 where() 에 null 을 입력하면 해당 조건은 무시한다.

이 코드의 또 다른 장점은 likeItemName() , maxPrice() 를 다른 쿼리를 \
작성할 때 재사용 할 수 있다는 점이다. \
쉽게 이야기해서 쿼리 조건을 부분적으로 모듈화 할 수 있다. \
자바 코드로 개발하기 때문에 얻을 수 있는 큰 장점이다.

이제 설정하고 실행해보자.
<br/>
<br/>

### QuerydslConfig
```java
@Configuration
@RequiredArgsConstructor
public class QuerydslConfig {

    private final EntityManager em;

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository() {
        return new JpaItemRepositoryV3(em);
    }

}

```
<br/>
<br/>

### ItemServiceApplication - 변경
```java
//@Import(SpringDataJpaConfig.class)
@Import(QuerydslConfig.class)
@SpringBootApplication(scanBasePackages = "hello.itemservice.web")
public class ItemServiceApplication {}
```
QuerydslConfig 를 사용하도록 변경했다.
<br/>
<br/>

#### 예외 변환
Querydsl 은 별도의 스프링 예외 추상화를 지원하지 않는다. \
대신에 JPA에서 학습한 것 처럼 @Repository 에서 스프링 예외 추상화를 처리해준다.
<br/>
<br/>

## 정리

### Querydsl 장점
Querydsl 덕분에 동적 쿼리를 매우 깔끔하게 사용할 수 있다.
```java
List<Item> result = query
        .select(item)
        .from(item)
        .where(likeItemName(itemName), maxPrice(maxPrice))
        .fetch();
```
쿼리 문장에 오타가 있어도 컴파일 시점에 오류를 막을 수 있다.\
메서드 추출을 통해서 코드를 재사용할 수 있다. \
예를 들어서 여기서 만든 likeItemName(itemName) , \
maxPrice(maxPrice) 메서드를 다른 쿼리에서도 함께 사용할 수 있다.

Querydsl을 사용해서 자바 코드로 쿼리를 작성하는 장점을 느껴보았을 것이다.\
그리고 동적 쿼리 문제도 깔끔하게 해결해보았다.\
Querydsl은 이 외에도 수 많은 편리한 기능을 제공한다. \
예를 들어서 최적의 쿼리 결과를 만들기 위해서 DTO로 편리하게 조회하는 기능은 \
실무에서 자주 사용하는 기능이다. JPA를 사용한다면 스프링 데이터 JPA와 \
Querydsl은 실무의 다양한 문제를 편리하게 해결하기 위해 선택하는 기본 기술이라 생각한다.
<br/>
<br/>
