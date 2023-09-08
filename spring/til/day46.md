## MyBatis 소개
MyBatis는 앞서 설명한 JdbcTemplate보다 더 많은 기능을 제공하는 SQL Mapper 이다.\
기본적으로 JdbcTemplate이 제공하는 대부분의 기능을 제공한다.\
JdbcTemplate과 비교해서 MyBatis의 가장 매력적인 점은 \
SQL을 XML에 편리하게 작성할 수 있고 또 동적 쿼리를 \
매우 편리하게 작성할 수 있다는 점이다.

먼저 SQL이 여러줄에 걸쳐 있을 때 둘을 비교해보자.

### JdbcTemplate - SQL 여러줄
```java
String sql = "update item " +
        "set item_name=:itemName, price=:price, quantity=:quantity " +
        "where id=:id";
```
<br/>
<br/>

### MyBatis - SQL 여러줄
```java
<update id="update">
    update item
    set item_name=#{itemName},
        price=#{price},
        quantity=#{quantity}
    where id = #{id}
</update>
```
MyBatis는 XML에 작성하기 때문에 라인이 길어져도 문자 더하기에 대한 불편함이 없다.\
다음으로 상품을 검색하는 로직을 통해 동적 쿼리를 비교해보자.
<br/>
<br/>

### JdbcTemplate - 동적 쿼리
```java
String sql = "select id, item_name, price, quantity from item";

//동적 쿼리
if (StringUtils.hasText(itemName) || maxPrice != null) {
    sql += " where";
}

boolean andFlag = false;

if (StringUtils.hasText(itemName)) {
    sql += " item_name like concat('%',:itemName,'%')";
    andFlag = true;
}

if (maxPrice != null) {
    if (andFlag) {
        sql += " and";
    }
    sql += " price <= :maxPrice";
}

log.info("sql={}", sql);
return template.query(sql, param, itemRowMapper());
```
<br/>

### MyBatis - 동적 쿼리
```java
<select id="findAll" resultType="Item">
    select id, item_name, price, quantity
    from item
    <where>
        <if test="itemName != null and itemName != ''">
            and item_name like concat('%',#{itemName},'%')
        </if>
        <if test="maxPrice != null">
            and price &lt;= #{maxPrice}
        </if>
    </where>
</select>
```
JdbcTemplate은 자바 코드로 직접 동적 쿼리를 작성해야 한다. \
반면에 MyBatis는 동적 쿼리를 매우 편리하게 작성할 수 있는 다양한 기능들을 제공해준다.
<br/>
<br/>

### 설정의 장단점
JdbcTemplate은 스프링에 내장된 기능이고,\
별도의 설정없이 사용할 수 있다는 장점이 있다. \
반면에 MyBatis는 약간의 설정이 필요하다.
<br/>
<br/>

### 정리
프로젝트에서 동적 쿼리와 복잡한 쿼리가 많다면 MyBatis를 사용하고, \
단순한 쿼리들이 많으면 JdbcTemplate을 선택해서 사용하면 된다.\
물론 둘을 함께 사용해도 된다. 하지만 MyBatis를 선택했다면 그것으로 충분할 것이다.
<br/>
<br/>

### 참고
강의에서는 MyBatis의 기능을 하나하나를 자세하게 다루지는 않는다. \
MyBatis를 왜 사용하는지, 그리고 주로 사용하는 기능 위주로 다룰 것이다. \
그래도 이 강의를 듣고 나면 MyBatis로 개발을 할 수 있게 되고 추가로 \
필요한 내용을 공식 사이트에서 찾아서 사용할 수 있게 될 것이다.

MyBatis는 기능도 단순하고 또 공식 사이트가 한글로 잘 번역되어 있어서 원하는 기능을 편리하게 찾아볼
수 있다.

> 공식 사이트
> https://mybatis.org/mybatis-3/ko/index.html
<br/>

## MyBatis 설정
mybatis-spring-boot-starter 라이브러리를 사용하면 MyBatis를 스프링과 통합하고, \
설정도 아주 간단히 할 수 있다.

mybatis-spring-boot-starter 라이브러리를 사용해서 간단히 설정하는 방법을 알아보자.\
build.gradle 에 다음 의존 관계를 추가한다.
```java
//MyBatis 추가
implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:2.2.0'
```
참고로 뒤에 버전 정보가 붙는 이유는 스프링 부트가 버전을 \
관리해주는 공식 라이브러리가 아니기 때문이다. \
스프링 부트가 버전을 관리해주는 경우 버전 정보를 붙이지 않아도 \
최적의 버전을 자동으로 찾아준다.
<br/>
<br/>

### build.gradle - 의존관계 전체
```java
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	compileOnly 'org.projectlombok:lombok'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'

	//MyBatis 추가
	implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:2.2.0'

	//JdbcTemplate 추가
	implementation 'org.springframework.boot:spring-boot-starter-jdbc'

	//H2 데이터베이스 추가
	runtimeOnly 'com.h2database:h2'

	//테스트에서 lombok 사용
	testCompileOnly 'org.projectlombok:lombok'
	testAnnotationProcessor 'org.projectlombok:lombok'
}
```
다음과 같은 라이브러리가 추가된다.
* mybatis-spring-boot-starter : MyBatis를 스프링 부트에서 편리하게 \
  사용할 수 있게 시작하는 라이브러리
* mybatis-spring-boot-autoconfigure : MyBatis와 스프링 부트 설정 라이브러리
* mybatis-spring : MyBatis와 스프링을 연동하는 라이브러리
* mybatis : MyBatis 라이브러리

라이브러리 추가는 완료되었다 다음으로 설정을 해보자
<br/>
<br/>

### 설정
application.properties 에 다음 설정을 추가하자. \
#MyBatis 를 참고하면 된다.

주의! 웹 애플리케이션을 실행하는 main , 테스트를 실행하는\
test 각 위치의 application.properties 를 모두 수정해주어야 한다. \
설정을 변경해도 반영이 안된다면 이 부분을 꼭! 확인하자.

`main - application.properties`
```java
spring.profiles.active=local
spring.datasource.url=jdbc:h2:tcp://localhost/~/test
spring.datasource.username=sa

logging.level.org.springframework.jdbc=debug

#MyBatis
mybatis.type-aliases-package=hello.itemservice.domain
mybatis.configuration.map-underscore-to-camel-case=true
logging.level.hello.itemservice.repository.mybatis=trace
```

`test - application.properties`
```java
spring.profiles.active=test
#spring.datasource.url=jdbc:h2:tcp://localhost/~/testcase
#spring.datasource.username=sa

logging.level.org.springframework.jdbc=debug

#MyBatis
mybatis.type-aliases-package=hello.itemservice.domain
mybatis.configuration.map-underscore-to-camel-case=true
logging.level.hello.itemservice.repository.mybatis=trace
```
* mybatis.type-aliases-package
  * 마이바티스에서 타입 정보를 사용할 때는 패키지 이름을 적어주어야 하는데, \
    여기에 명시하면 패키지 이름을 생략할 수 있다.
  * 지정한 패키지와 그 하위 패키지가 자동으로 인식된다.
  * 여러 위치를 지정하려면 , , ; 로 구분하면 된다.
* mybatis.configuration.map-underscore-to-camel-case
  * JdbcTemplate의 BeanPropertyRowMapper 에서 처럼 \
    언더바를 카멜로 자동 변경해주는 기능을 활성화 한다. \
    바로 다음에 설명하는 관례의 불일치 내용을 참고하자.
* logging.level.hello.itemservice.repository.mybatis=trace
  * MyBatis에서 실행되는 쿼리 로그를 확인할 수 있다.
<br/>

### 관례의 불일치
자바 객체에는 주로 카멜( camelCase ) 표기법을 사용한다. \
itemName 처럼 중간에 낙타 봉이 올라와 있는 표기법이다.

반면에 관계형 데이터베이스에서는 주로 언더스코어를 사용하는 snake_case 표기법을 사용한다.\
item_name 처럼 중간에 언더스코어를 사용하는 표기법이다.

이렇게 관례로 많이 사용하다 보니 map-underscore-to-camel-case 기능을 활성화 하면\
언더스코어 표기법을 카멜로 자동 변환해준다. \
따라서 DB에서 select item_name 으로 조회해도\
객체의 itemName ( setItemName() ) 속성에 값이 정상 입력된다.

정리하면 해당 옵션을 켜면 snake_case 는 자동으로 해결되니 그냥 두면 되고, \
컬럼 이름과 객체 이름이 완전히 다른 경우에는 조회 SQL에서 별칭을 사용하면 된다.

별칭을 통한 해결방안\
`select item_name as name`
<br/>
<br/>

## MyBatis 적용1 - 기본
이제부터 본격적으로 MyBatis를 사용해서 데이터베이스에 데이터를 저장해보자.\
XML에 작성한다는 점을 제외하고는 JDBC 반복을 줄여준다는 점에서 \
기존 JdbcTemplate과 거의 유사하다.

### ItemMapper
```java
@Mapper
public interface ItemMapper {

    void save(Item item);

    void update(@Param("id") Long id, @Param("updateParam") ItemUpdateDto updateParam);

    List<Item> findAll(ItemSearchCond itemSearch);

    Optional<Item> findById(Long id);
}
```
* 마이바티스 매핑 XML을 호출해주는 매퍼 인터페이스이다.
* 이 인터페이스에는 @Mapper 애노테이션을 붙여주어야 한다. 그래야 MyBatis에서 인식할 수 있다. 
* 이 인터페이스의 메서드를 호출하면 다음에 보이는 xml 의 해당 SQL을 실행하고 결과를 돌려준다.
* ItemMapper 인터페이스의 구현체에 대한 부분은 뒤에 별도로 설명한다.

이제 같은 위치에 실행할 SQL이 있는 XML 매핑 파일을 만들어주면 된다.\
참고로 자바 코드가 아니기 때문에 src/main/resources 하위에 만들되, \
패키지 위치는 맞추어 주어야 한다.

`src/main/resources/hello/itemservice/repository/mybatis/ItemMapper.xml`
```java
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="hello.itemservice.repository.mybatis.ItemMapper">

    <insert id="save" useGeneratedKeys="true" keyProperty="id">
        insert into item (item_name, price, quantity)
        values (#{itemName}, #{price}, #{quantity})
    </insert>
    
    <update id="update">
        update item
        set item_name=#{updateParam.itemName},
        price=#{updateParam.price},
        quantity=#{updateParam.quantity}
        where id = #{id}
    </update>

    <select id="findById" resultType="Item">
        select id, item_name, price, quantity
        from item
        where id = #{id}
    </select>

    <select id="findAll" resultType="Item">
        select id, item_name, price, quantity
        from item
        <where>
            <if test="itemName != null and itemName != ''">
                and item_name like concat('%',#{itemName},'%')
            </if>
            <if test="maxPrice != null">
                and price &lt;= #{maxPrice}
            </if>
        </where>
    </select>

</mapper>
```
* namespace : 앞서 만든 매퍼 인터페이스를 지정하면 된다.
* 주의! 경로와 파일 이름에 주의하자.

> 참고 - XML 파일 경로 수정하기\
> XML 파일을 원하는 위치에 두고 싶으면 application.properties 에 다음과 같이 설정하면 된다.\
> mybatis.mapper-locations=classpath:mapper/**/*.xml
>
> 이렇게 하면 resources/mapper 를 포함한 그 하위 폴더에 있는 XML을 XML 매핑 파일로 인식한다.\
> 이 경우 파일 이름은 자유롭게 설정해도 된다.
>
> 참고로 테스트의 application.properties 파일도 함께 수정해야 \
> 테스트를 실행할 때 인식할 수 있다
<br/>

### insert - save
```java
void save(Item item);

<insert id="save" useGeneratedKeys="true" keyProperty="id">
    insert into item (item_name, price, quantity)
    values (#{itemName}, #{price}, #{quantity})
</insert>
```
* Insert SQL은 <insert> 를 사용하면 된다.
* id 에는 매퍼 인터페이스에 설정한 메서드 이름을 지정하면 된다. \
  여기서는 메서드 이름이 save() 이므로 save 로 지정하면 된다.
* 파라미터는 #{} 문법을 사용하면 된다. 그리고 매퍼에서 넘긴 객체의 프로퍼티 이름을 적어주면 된다.
* #{} 문법을 사용하면 PreparedStatement 를 사용한다. JDBC의 ? 를 치환한다 생각하면 된다.
* useGeneratedKeys 는 데이터베이스가 키를 생성해 주는\
  IDENTITY 전략일 때 사용한다. keyProperty 는 생성되는 키의 속성 이름을 지정한다. \
  Insert가 끝나면 item 객체의 id 속성에 생성된 값이 입력된다.
<br/>

### update - update
```java
void update(@Param("id") Long id, @Param("updateParam") ItemUpdateDto updateParam);

<update id="update">
    update item
    set item_name=#{updateParam.itemName},
    price=#{updateParam.price},
    quantity=#{updateParam.quantity}
    where id = #{id}
</update>
```
* Update SQL은 <update> 를 사용하면 된다.
* 여기서는 파라미터가 Long id , ItemUpdateDto updateParam 으로 2개이다. \
  파라미터가 1개만 있으면 @Param 을 지정하지 않아도 되지만, \
  파라미터가 2개 이상이면 @Param 으로 이름을 지정해서 파라미터를 구분해야 한다.
<br/>

### select - findById
```java
Optional<Item> findById(Long id);

<select id="findById" resultType="Item">
    select id, item_name, price, quantity
    from item
    where id = #{id}
</select>
```
* Select SQL은 <select> 를 사용하면 된다.
* resultType 은 반환 타입을 명시하면 된다. 여기서는 결과를 Item 객체에 매핑한다.
* 앞서 application.properties 에 `mybatis.type-aliasespackage=hello.itemservice.domain`\
  속성을 지정한 덕분에 모든 패키지 명을 다 적지는 않아도 된다. \
  그렇지 않으면 모든 패키지 명을 다 적어야 한다.
* JdbcTemplate의 BeanPropertyRowMapper 처럼 SELECT SQL의 결과를 \
  편리하게 객체로 바로 변환해준다.
* mybatis.configuration.map-underscore-to-camel-case=true 속성을 지정한 덕분에\
  언더스코어를 카멜 표기법으로 자동으로 처리해준다. ( item_name -> itemName )
* 자바 코드에서 반환 객체가 하나이면 Item , Optional<Item> 과 같이 사용하면 되고, \
  반환 객체가 하나 이상이면 컬렉션을 사용하면 된다.\
  주로 List 를 사용한다. 다음을 참고하자.
<br/>

### select - findAll
```java
List<Item> findAll(ItemSearchCond itemSearch);

<select id="findAll" resultType="Item">
    select id, item_name, price, quantity
    from item
    <where>
        <if test="itemName != null and itemName != ''">
            and item_name like concat('%',#{itemName},'%')
        </if>
        <if test="maxPrice != null">
            and price &lt;= #{maxPrice}
        </if>
    </where>
</select>
```
* Mybatis는 <where> , <if> 같은 동적 쿼리 문법을 통해 편리한 동적 쿼리를 지원한다.
* <if> 는 해당 조건이 만족하면 구문을 추가한다.
* <where> 은 적절하게 where 문장을 만들어준다.
  * 예제에서 <if> 가 모두 실패하게 되면 SQL where 를 만들지 않는다.
  * 예제에서 <if> 가 하나라도 성공하면 처음 나타나는 and 를 where 로 변환해준다.
<br/>

### XML 특수문자
그런데 가격을 비교하는 조건을 보자\
`and price &lt;= #{maxPrice}`\
여기에 보면 <= 를 사용하지 않고 &lt;= 를 사용한 것을 확인할 수 있다. \
그 이유는 XML에서는 데이터 영역에 < , > 같은 특수 문자를 사용할 수 없기 때문이다. \
이유는 간단한데, XML에서 TAG가 시작하거나 종료할 때 < , > 와 같은 \
특수문자를 사용하기 때문이다.
```java
< : &lt;
> : &gt;
& : &amp;
```
다른 해결 방안으로는 XML에서 지원하는 CDATA 구문 문법을 사용하는 것이다.\
이 구문 안에서는 특수문자를 사용할 수 있다. \
대신 이 구문 안에서는 XML TAG가 단순 문자로 인식되기 때문에\
<if> , <where> 등이 적용되지 않는다.
<br/>
<br/>

### XML CDATA 사용
```xml
<select id="findAll" resultType="Item">
    select id, item_name, price, quantity
    from item
    <where>
        <if test="itemName != null and itemName != ''">
            and item_name like concat('%',#{itemName},'%')
        </if>
        <if test="maxPrice != null">
            <![CDATA[
            and price <= #{maxPrice}
            ]]>
        </if>
    </where>
</select>
```
특수문자와 CDATA 각각 상황에 따른 장단점이 있으므로 \
원하는 방법을 그때그때 선택하면 된다.
<br/>
<br/>

## MyBatis 적용2 - 설정과 실행
### MyBatisItemRepository
```java

@Repository
@RequiredArgsConstructor
public class MyBatisItemRepository implements ItemRepository {

    private final ItemMapper itemMapper;

    @Override
    public Item save(Item item) {
        itemMapper.save(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        itemMapper.update(itemId, updateParam);

    }

    @Override
    public Optional<Item> findById(Long id) {
        return itemMapper.findById(id);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        return itemMapper.findAll(cond);
    }
}

```
ItemRepository 를 구현해서 MyBatisItemRepository 를 만들자.\
MyBatisItemRepository 는 단순히 ItemMapper 에 기능을 위임한다.
<br/>
<br/>

### MyBatisConfig
```java
@Configuration
@RequiredArgsConstructor
public class MyBatisConfig {

    private final DataSource dataSource;
    private final ItemMapper itemMapper;

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository() {
        return new MyBatisItemRepository(itemMapper);
    }
}
```
MyBatisConfig 는 ItemMapper 를 주입받고, 필요한 의존관계를 만든다.
<br/>
<br/>

### ItemServiceApplication - 변경
```java
@Slf4j
//@Import(MemoryConfig.class)
//@Import(JdbcTemplateV1Config.class)
//@Import(JdbcTemplateV2Config.class)
//@Import(JdbcTemplateV3Config.class)
@Import(MyBatisConfig.class)
@SpringBootApplication(scanBasePackages = "hello.itemservice.web")
public class ItemServiceApplication {}
```
* @Import(MyBatisConfig.class) : 앞서 설정한 MyBatisConfig.class 를 사용하도록 설정했다.

먼저 ItemRepositoryTest 를 통해서 리포지토리가 정상 동작하는지 확인해보자. \
테스트가 모두 성공해야 한다.
<br/>
<br/>

## MyBatis 적용3 - 분석
생각해보면 지금까지 진행한 내용중에 약간 이상한 부분이 있다.\
ItemMapper 매퍼 인터페이스의 구현체가 없는데 어떻게 동작한 것일까?

### ItemMapper 인터페이스
```java
@Mapper
public interface ItemMapper {

    void save(Item item);

    void update(@Param("id") Long id, @Param("updateParam") ItemUpdateDto updateParam);

    List<Item> findAll(ItemSearchCond itemSearch);

    Optional<Item> findById(Long id);
}

```
이 부분은 MyBatis 스프링 연동 모듈에서 자동으로 처리해주는데 다음과 같다.
<br/>
<br/>

### 설정 원리
![image](https://github.com/jub3907/Spring-study/assets/58246682/2f82cdb2-c4b3-477c-93bf-6427f8fd57d3)

1. 애플리케이션 로딩 시점에 MyBatis 스프링 연동 모듈은 @Mapper 가 붙어있는 인터페이스를 조사한다.
2. 해당 인터페이스가 발견되면 동적 프록시 기술을 사용해서 ItemMapper 인터페이스의 구현체를 만든다.
3. 생성된 구현체를 스프링 빈으로 등록한다.

실제 동적 프록시 기술이 사용되었는지 간단히 확인해보자.
<br/>
<br/>

### MyBatisItemRepository - 로그 추가
```java
@Slf4j
@Repository
@RequiredArgsConstructor
public class MyBatisItemRepository implements ItemRepository {

    private final ItemMapper itemMapper;

    @Override
    public Item save(Item item) {
        log.info("itemMapper class={}", itemMapper.getClass());
        itemMapper.save(item);
        return item;
    }
}
```
실행해서 주입 받은 ItemMapper 의 클래스를 출력해보자.
<br/>
<br/>

#### 실행 결과
```java
itemMapper class=class com.sun.proxy.$Proxy66
```
출력해보면 JDK 동적 프록시가 적용된 것을 확인할 수 있다.
<br/>
<br/>

### 매퍼 구현체
마이바티스 스프링 연동 모듈이 만들어주는 ItemMapper 의 구현체 덕분에 \
인터페이스 만으로 편리하게 XML의 데이터를 찾아서 호출할 수 있다.

원래 마이바티스를 사용하려면 더 번잡한 코드를 거쳐야 하는데, \
이런 부분을 인터페이스 하나로 매우 깔끔하고 편리하게 사용할 수 있다.

매퍼 구현체는 예외 변환까지 처리해준다. \
MyBatis에서 발생한 예외를 스프링 예외 추상화인 DataAccessException 에 맞게 \
변환해서 반환해준다. JdbcTemplate이 제공하는 예외 변환 기능을 \
여기서도 제공한다고 이해하면 된다.
<br/>
<br/>

### 정리
매퍼 구현체 덕분에 마이바티스를 스프링에 편리하게 통합해서 사용할 수 있다.\
매퍼 구현체를 사용하면 스프링 예외 추상화도 함께 적용된다.\
마이바티스 스프링 연동 모듈이 많은 부분을 자동으로 설정해주는데, \
데이터베이스 커넥션, 트랜잭션과 관련된 기능도 마이바티스와 함께 연동하고, 동기화해준다.

> 마이바티스 스프링 연동 모듈이 자동으로 등록해주는 부분은 MybatisAutoConfiguration 클래스를 참고하자.
<br/>

## MyBatis 기능 정리1 - 동적 쿼리
MyBatis에서 자주 사용하는 주요 기능을 공식 메뉴얼이 제공하는 예제를 통해 간단히 정리해보자

* MyBatis 공식 메뉴얼: https://mybatis.org/mybatis-3/ko/index.html
* MyBatis 스프링 공식 메뉴얼: https://mybatis.org/spring/ko/index.html
<br/>

### 동적 SQL
마이바티스가 제공하는 최고의 기능이자 마이바티스를 사용하는 이유는 바로 동적 SQL 기능 때문이다.
동적 쿼리를 위해 제공되는 기능은 다음과 같다.

* if
* choose (when, otherwise)
* trim (where, set)
* foreach

공식 메뉴얼에서 제공하는 예제를 통해 동적 SQL을 알아보자.
<br/>
<br/>

### if
```java
<select id="findActiveBlogWithTitleLike" resultType="Blog">
    SELECT * FROM BLOG
    WHERE state = ‘ACTIVE’
    <if test="title != null">
        AND title like #{title}
    </if>
</select>
```
해당 조건에 따라 값을 추가할지 말지 판단한다.\
내부의 문법은 OGNL을 사용한다. 자세한 내용은 OGNL을 검색해보자.
<br/>
<br/>

### choose, when, otherwise
```java
<select id="findActiveBlogLike" resultType="Blog">
    SELECT * FROM BLOG WHERE state = ‘ACTIVE’
    <choose>
        <when test="title != null">
            AND title like #{title}
        </when>
        <when test="author != null and author.name != null">
            AND author_name like #{author.name}
        </when>
        <otherwise>
            AND featured = 1
        </otherwise>
    </choose>
</select>

```
자바의 switch 구문과 유사한 구문도 사용할 수 있다.
<br/>
<br/>

### trim, where, set
```java
<select id="findActiveBlogLike" resultType="Blog">
    SELECT * FROM BLOG
    WHERE
    <if test="state != null">
        state = #{state}
    </if>
    <if test="title != null">
        AND title like #{title}
    </if>
    <if test="author != null and author.name != null">
        AND author_name like #{author.name}
    </if>
</select>
```
<br/>

이 예제의 문제점은 문장을 모두 만족하지 않을 때 발생한다.
```sql
SELECT * FROM BLOG
WHERE
```

title 만 만족할 때도 문제가 발생한다
```java
SELECT * FROM BLOG
WHERE
AND title like ‘someTitle’
```
결국 WHERE 문을 언제 넣어야 할지 상황에 따라서 동적으로 달라지는 문제가 있다.\
<where> 를 사용하면 이런 문제를 해결할 수 있다.
<br/>
<br/>

### <where> 사용
```java
<select id="findActiveBlogLike" resultType="Blog">
    SELECT * FROM BLOG
    <where>
        <if test="state != null">
            state = #{state}
        </if>
        <if test="title != null">
            AND title like #{title}
        </if>
        <if test="author != null and author.name != null">
            AND author_name like #{author.name}
        </if>
    </where>
</select>
```
<where> 는 문장이 없으면 where 를 추가하지 않는다. \
문장이 있으면 where 를 추가한다. 만약 and 가 먼저 시작된다면 and 를 지운다.\
참고로 다음과 같이 trim 이라는 기능으로 사용해도 된다. \
이렇게 정의하면 <where> 와 같은 기능을 수행한다.
```html
<trim prefix="WHERE" prefixOverrides="AND |OR ">
 ...
</trim>
```
<br/>

### foreach
```java
<select id="selectPostIn" resultType="domain.blog.Post">
    SELECT *
    FROM POST P
    <where>
        <foreach item="item" index="index" collection="list"
                 open="ID in (" separator="," close=")" nullable="true">
            #{item}
        </foreach>
    </where>
</select>
```
컬렉션을 반복 처리할 때 사용한다.\
where in (1,2,3,4,5,6) 와 같은 문장을 쉽게 완성할 수 있다.\
파라미터로 List 를 전달하면 된다.

> 동적 쿼리에 대한 자세한 내용은 다음을 참고하자.\
> https://mybatis.org/mybatis-3/ko/dynamic-sql.html
<br/>
<br/>

## MyBatis 기능 정리2 - 기타 기능
### 애노테이션으로 SQL 작성
다음과 같이 XML 대신에 애노테이션에 SQL을 작성할 수 있다.
```java
@Select("select id, item_name, price, quantity from item where id=#{id}")
Optional<Item> findById(Long id);
```
@Insert , @Update , @Delete , @Select 기능이 제공된다.\
이 경우 XML에는 <select id="findById"> ~ </select> 는 제거해야 한다.\
동적 SQL이 해결되지 않으므로 간단한 경우에만 사용한다.

> 애노테이션으로 SQL 작성에 대한 더 자세한 내용은 다음을 참고하자.
> https://mybatis.org/mybatis-3/ko/java-api.html
<br/>
<br/>

### 문자열 대체(String Substitution)
#{} 문법은 ?를 넣고 파라미터를 바인딩하는 PreparedStatement 를 사용한다.\
때로는 파라미터 바인딩이 아니라 문자 그대로를 처리하고 싶은 경우도 있다.\
이때는 ${} 를 사용하면 된다. 

다음 예제를 보자
`ORDER BY ${columnName}`
```java
@Select("select * from user where ${column} = #{value}")
User findByColumn(@Param("column") String column, @Param("value") String value);
```

* 주의할 점
  * ${} 를 사용하면 SQL 인젝션 공격을 당할 수 있다. \
    따라서 가급적 사용하면 안된다. \
    사용하더라도 매우 주의깊게 사용해야 한다.
<br/>

### 재사용 가능한 SQL 조각
<sql> 을 사용하면 SQL 코드를 재사용 할 수 있다
```java
<sql id="userColumns"> ${alias}.id,${alias}.username,${alias}.password </sql>
```

```java
<select id="selectUsers" resultType="map">
    select
        <include refid="userColumns"><property name="alias" value="t1"/></include>,
        <include refid="userColumns"><property name="alias" value="t2"/></include>
    from some_table t1
    cross join some_table t2
</select>
```
* <include> 를 통해서 <sql> 조각을 찾아서 사용할 수 있다.

```java
<sql id="sometable">
    ${prefix}Table
</sql>

<sql id="someinclude">
    from
    <include refid="${include_target}"/>
</sql>

<select id="select" resultType="map">
    select
    field1, field2, field3
    <include refid="someinclude">
        <property name="prefix" value="Some"/>
        <property name="include_target" value="sometable"/>
    </include>
</select>

```
프로퍼티 값을 전달할 수 있고, 해당 값은 내부에서 사용할 수 있다.
<br/>
<br/>

### Result Maps
결과를 매핑할 때 테이블은 user_id 이지만 객체는 id 이다.\
이 경우 컬럼명과 객체의 프로퍼티 명이 다르다. \
그러면 다음과 같이 별칭( as )을 사용하면 된다.
```java
<select id="selectUsers" resultType="User">
    select
        user_id as "id",
        user_name as "userName",
        hashed_password as "hashedPassword"
    from some_table
    where id = #{id}
</select>
```
별칭을 사용하지 않고도 문제를 해결할 수 있는데,\
다음과 같이 resultMap 을 선언해서 사용하면 된다.
```java
<resultMap id="userResultMap" type="User">
    <id property="id" column="user_id" />
    <result property="username" column="user_name"/>
    <result property="password" column="hashed_password"/>
</resultMap>

<select id="selectUsers" resultMap="userResultMap">
    select user_id, user_name, hashed_password
    from some_table
    where id = #{id}
</select>
```
<br/>
<br/>

### 복잡한 결과매핑
MyBatis도 매우 복잡한 결과에 객체 연관관계를 고려해서 데이터를 조회하는 것이 가능하다.\
이때는 <association> , <collection> 등을 사용한다.\
이 부분은 성능과 실효성에서 측면에서 많은 고민이 필요하다.\
JPA는 객체와 관계형 데이터베이스를 ORM 개념으로 매핑하기 때문에 \
이런 부분이 자연스럽지만, MyBatis에서는 들어가는 공수도 많고, \
성능을 최적화하기도 어렵다. 따라서 해당기능을 사용할 때는 신중하게 사용해야 한다.

해당 기능에 대한 자세한 내용은 공식 메뉴얼을 참고하자.

> 결과 매핑에 대한 자세한 내용은 다음을 참고하자.\
> https://mybatis.org/mybatis-3/ko/sqlmap-xml.html#Result_Maps

## JPA
스프링과 JPA는 자바 엔터프라이즈(기업) 시장의 주력 기술이다.\
스프링이 DI 컨테이너를 포함한 애플리케이션 전반의 다양한 기능을 제공한다면, \
JPA는 ORM 데이터 접근 기술을 제공한다.

스프링 + 데이터 접근기술의 조합을 구글 트랜드로 비교했을 때
* 글로벌에서는 스프링+JPA 조합을 80%이상 사용한다.
* 국내에서도 스프링 + JPA 조합을 50%정도 사용하고, \
  2015년 부터 점점 그 추세가 증가하고 있다.

JPA는 스프링 만큼이나 방대하고, 학습해야할 분량도 많다. \
하지만 한번 배워두면 데이터 접근 기술에서 매우 큰 생산성 향상을 얻을 수 있다. \
대표적으로 JdbcTemplate이나 MyBatis 같은 SQL 매퍼 기술은 \
SQL을 개발자가 직접 작성해야 하지만, JPA를 사용하면 \
SQL도 JPA가 대신 작성하고 처리해준다.

실무에서는 JPA를 더욱 편리하게 사용하기 위해 스프링 데이터 JPA와\
Querydsl이라는 기술을 함께 사용한다.

중요한 것은 JPA이다. 

스프링 데이터 JPA, Querydsl은 JPA를 편리하게 사용하도록 도와주는 도구라 생각하면 된다.
이 강의에서는 모든 내용을 다루지 않고, \
JPA와 스프링 데이터 JPA, 그리고 Querydsl로 이어지는 전체 그림을 볼 것이다. \
그리고 이 기술들을 우리 애플리케이션에 적용하면서 자연스럽게 왜 사용해야 하는지,\
그리고 어떤 장점이 있는지 이해할 수 있게 된다.

이렇게 전체 그림을 보고 나면 앞으로 어떻게 공부해야 할지 쉽게 접근할 수 있을 것이다.
<br/>
<br/>

#### 참고

각각의 기술들은 별도의 강의로 다룰 정도로 내용이 방대하다.\
여기서는 해당 기술들의 기본 기능과, 왜 사용해야 하는지 각각의 장단점을 알아본다. \
각 기술들의 자세한 내용은 다음 강의를 참고하자.

* JPA - 자바 ORM 표준 JPA 프로그래밍 - 기본편
* 스프링 데이터 JPA - 실전! 스프링 데이터 JPA
* Querydsl - 실전! Querydsl
<br/>
<br/>


## ORM 개념1 - SQL 중심적인 개발의 문제점
지금은 객체를 관계형 DB에 저장하는 시대이다.

하지만, 이 때문에 SQL 중심적인 개발에 문제점이 발생한다.\
바로, **SQL에 의존적인 개발**을 피해기 어렵다는 점이다.

또 다른 문제점으로, **패러다임의 불일치**가 발생한다.\
객체 지향 프로그래밍은 추상화, 캡슐화, 정보은닉, 상속, 다형성 등 \
시스템의 복잡성을 제어할 수 있는 다양한 장치들을 제공한다.

현실적으로, 객체를 저장할 때 우리는 관계형 데이터베이스에 저장한다.\
이를 위해 객체를 SQL로 변환하고, DB에 조회하거나 삽입해야 한다.\
바로 이 **SQL 변환**을 개발자가 수행하는 작업이다. \
Application 하나를 작성할 때, 개발자가 SQL 매퍼의 역할을 해야만 한다.
<br/>
<br/>

### 객체와 관계형 데이터베이스의 차이
객체와 관계형 데이터 베이스엔 다음과 같은 차이점이 존재한다.
* 상속
* 연관관계
* 데이터 타입
* 데이터 식별 방법
<br/>

### 상속
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/4bd2a468-ecf1-4ed6-b184-f4d32a74a7bd)

객체엔 상속 관계가 존재하지만,\
Database엔 따로 상속 관계가 존재하지 않는다.\
다만, 상속과 유사한 슈퍼 타입과 서브 타입의 관계는 존재하지만\
엄밀하게 따지자면 상속 관계는 아니라고 할 수 있다.

위의 Album 객체를 저장한다고 가정해보자.\
이를 위해 객체를 분해해 일부는 ITEM 테이블에,\
일부는 ALBUM 테이블에 저장해야만 한다.\
각각의 테이블에 따른 Join SQL을 작성하고, 각각의 객체를 생성하고..

이러한 작업이 너무나 복잡하기 때문에, \
DB에 저장할 객체엔 상속 관계를 쓰지 않는다.\
만약, 자바 컬렉션에 저장한다면 어떨까?

* list.add(album);

조회 또한 마찬가지로, 굉장히 간단히 수행할 수 있다.

* Album album = list.get(albumId);

또한 부모 타입으로 조회한 뒤, 다형성을 활용할 수도 있다.

* Item item = list.get(albumId);
<br/>

### 연관관꼐
객체는 **참조**를 사용한다. 
* member.getTeam()

하지만, 테이블은 **외래 키**를 사용한다.
* JOIN ON M.TEAM_ID = T.TEAM_ID

따라서 객체를 테이블에 맞춰 모델링하는 방법을 주로 사용한다.
```java
class Member {
    String id; //MEMBER_ID 컬럼 사용
    Long teamId; //TEAM_ID FK 컬럼 사용 //**
    String username;//USERNAME 컬럼 사용
}
class Team {
    Long id; //TEAM_ID PK 사용
    String name; //NAME 컬럼 사용
}

INSERT INTO MEMBER(MEMBER_ID, TEAM_ID, USERNAME) VALUES …
```

하지만, 객체 다운 모델링은 외래 키가 들어가는 것이 아닌, \
참조를 통해 연관 관계를 맺는 것이다.

```java
class Member {
    String id; //MEMBER_ID 컬럼 사용
    Team team; //참조로 연관관계를 맺는다. //**
    String username;//USERNAME 컬럼 사용

    Team getTeam() {
        return team;
    }
}

class Team {
    Long id; //TEAM_ID PK 사용
    String name; //NAME 컬럼 사용
}
```
<br/>

하지만 객체 모델링을 조회하는 코드를 생각해보자.\
이 방법은 굉장히 번거롭다는걸 알 수 있다.
```java
SELECT M.*, T.*
FROM MEMBER M
JOIN TEAM T ON M.TEAM_ID = T.TEAM_ID 

public Member find(String memberId) {
    //SQL 실행 ...
    Member member = new Member();
    //데이터베이스에서 조회한 회원 관련 정보를 모두 입력
    Team team = new Team();
    //데이터베이스에서 조회한 팀 관련 정보를 모두 입력
    //회원과 팀 관계 설정
    member.setTeam(team); //**
    return member;
}
```

그렇다면, 객체 모델링을 자바 컬렉션에 관리해보자.

```java
list.add(member);

Member member = list.get(memberId);
Team team = member.getTeam();
```
굉장히 간단하게 삽입과 조회가 이루어진다.

이외에도 물리적으론 계층이 분할되어 있지만\
논리적으로는 계층이 분할되지 않아, \
**진정한 의미의 계층 분할이 어렵다**는 단점이 존재한다.
<br/>

### 비교
Repository에서 비교한다고 가정해보자.
```java
String memberId = "100";
Member member1 = memberDAO.getMember(memberId);
Member member2 = memberDAO.getMember(memberId);

member1 == member2; //다르다.

class MemberDAO {

    public Member getMember(String memberId) {
    String sql = "SELECT * FROM MEMBER WHERE MEMBER_ID = ?";
        ...
        //JDBC API, SQL 실행
        return new Member(...);
    }
}
```

그리고, 이번엔 자바 컬렉션에서 비교한다고 가정해보자.

```java
String memberId = "100";
Member member1 = list.get(memberId);
Member member2 = list.get(memberId);

member1 == member2; //같다.
```
<br/>

### 정리
우리가 SQL로 작업하는 것과, 자바 객체 안에서 \
순수하게 비교하는 것은 결과가 다르다. 우리가 객체답게 모델링 할 수록, \
연관관계를 만들수록 연관관계만 늘어난다.

이렇게, DB 저장은 굉장히 번거롭지만, \
데이터를 자바 컬렉션에 저장하는 작업은 매우 간편하고, \
신뢰성이 더 높은걸 볼 수 있었다.
객체를 자바 컬렉션에 저장하듯 DB에 저장할 순 없을까?

이러한 요구 사항에 맞춰 나온 것이 바로 \
**JPA, Java Persistence API**이다.
<br/>
<br/>

## ORM 개념2 - JPA 소개
### JPA?
Java Persistence API.\
자바 진영의 ORM 기술 표준을 의미한다.
<br/>
<br/>

### ORM?
Object-relational mapping(객체 관계 매핑)
객체는 객체대로 설계하고, 관계형 데이터베이스는 관계형 데이터베이스대로 설계한다.\
이 둘 사이를 ORM 프레임워크가 중간에서 매핑해주는 기술이다.\
대중적인 언어에는 대부분 ORM 기술이 존재한다.
<br/>
<br/>

### JPA의 동작
JPA는 애플리케이션과 JDBC 사이에서 동작한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/ac04d6f0-5664-4276-9dcf-fdeb1705b2ed)

* JPA 동작 - 저장
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/e7587b9a-b89b-471d-9a42-5ed729a875a0)

* JPA 동작 - 조회
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/9270a214-83ab-49ea-8316-4a2ace8228fd)

JPA를 통해, 우리는 자바 컬렉션에 조회하는 것처럼 조회할 수 있다.
<br/>
<br/>

### JPA 사용 이유
그럼, JPA는 **왜** 사용해야 하는가?
앞서 우리는 SQL 중심적인 개발과, \
자바 컬렉션을 사용했을 때의 차이점을 볼 수 있었다.

JPA를 사용하면 SQL 중심적인 개발에서 객체 중심적인 개발로 넘어갈 수 있고,\
추가로 다음과 같은 장점들을 취할 수 있다.
* 생산성
* 유지보수
* 패러다임의 불일치 해결
* 성능
* 데이터 접근 추상화와 벤더 독립성
* 표준
<br/>

#### 생산성
* 저장: **jpa.persist**(member)
* 조회: Member member = **jpa.find**(memberId)
* 수정: **member.setName**(“변경할 이름”)
* 삭제: **jpa.remove**(member)
<br/>

#### 유지보수
기존 SQL을 사용할 떄, 필드를 변경하려면 모든 SQL을 수정해야만 했다.\
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/ab0e79c6-4e5c-4407-9b40-ffcbeaa309a6)

JPA를 사용하면 필드만 추가하면 되고, SQL은 JPA가 알아서 처리해준다.\
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/74a3f05c-fdd7-4409-ab8b-98aef72de330)
<br/>
<br/>

### JPA와 패러다임의 불일치 해결
1.JPA와 상속
2.JPA와 연관관계
3.JPA와 객체 그래프 탐색
4.JPA와 비교하기
<br/>

#### JPA와 상속
다음과 같은 자바 클래스 상속관계와, \
DB 슈퍼타입-서브타입 관계가 있다고 가정해보자.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/9b3877c5-6ab3-494b-8be5-83699eae03ff)

Album의 데이터를 SQL로 저장하기 위해선 \
각 테이블에 나누어 데이터를 저장해야만 하지만,\
JPA를 사용하면 JPA가 나머지도 알아서 처리해준다.

* 개발자가 할 일\
  `jpa.persist(album);`
* 나머지 JPA가 처리할 일
  ```sql
  INSERT INTO ITEM ...
  INSERT INTO ALBUM ...;
  ```

조회 또한 마찬가지이다. 알아서 Join하고, 데이터를 반환해준다.

* 개발자가 할 일\
  `Album album = jpa.find(Album.class, albumId);`
* 나머지 JPA가 처리할 일
  ```sql
  SELECT I.*, A.*
  FROM ITEM I
  JOIN ALBUM A ON I.ITEM_ID = A.ITEM_ID
  ```
<br/>

#### JPA와 연관관계, 객체 그래프 탐색
연관관계와, 객체 그래프 또한 손쉽게 탐색할 수 있다.\
알아서 외래 키를 고민하고, 저장하고, 조회해준다.

* 연관관계 저장
  ```java
  member.setTeam(team);
  jpa.persist(member);
  ```
* 객체 그래프 탐색
  ```java
  Member member = jpa.find(Member.class, memberId);
  Team team = member.getTeam();
  ```

이를 통해, 신뢰할 수 있는 엔티티, 계층이 성립된다.

```java
class MemberService {
    ...
    public void process() {
        Member member = memberDAO.find(memberId);
        member.getTeam(); //자유로운 객체 그래프 탐색
        member.getOrder().getDelivery();
    }
}
```
<br/>

#### JPA와 비교
```java
String memberId = "100";
Member member1 = jpa.find(Member.class, memberId);
Member member2 = jpa.find(Member.class, memberId);

member1 == member2; //같다
```
동일한 트랜잭션에서 조회한 엔티티는 같다는 것이 보장된다.
<br/>
<br/>


### JPA의 성능 최적화 기능
1. 1차 캐시와 동일성(identity) 보장
2. 트랜잭션을 지원하는 쓰기 지연(transactional write-behind)
3. 지연 로딩(Lazy Loading)
<br/>

#### 1차 캐시와 동일성 보장
1. 같은 트랜잭션 안에서는 같은 엔티티를 반환해 약간의 조회 성능 향상
```java
String memberId = "100";
Member m1 = jpa.find(Member.class, memberId); //SQL
Member m2 = jpa.find(Member.class, memberId); //캐시

println(m1 == m2) //true
```

2. DB Isolation Level이 Read Commit이어도 애플리케이션에서 Repeatable Read 보장
<br/>
<br/>

#### 트랜잭션을 지원하는 쓰기 지연 - INSERT
1. 트랜잭션을 커밋할 때까지 INSERT SQL을 모음
2. JDBC BATCH SQL 기능을 사용해서 한번에 SQL 전송

```java
transaction.begin(); // [트랜잭션] 시작

em.persist(memberA);
em.persist(memberB);
em.persist(memberC);
//여기까지 INSERT SQL을 데이터베이스에 보내지 않는다.

//커밋하는 순간 데이터베이스에 INSERT SQL을 모아서 보낸다.
transaction.commit(); // [트랜잭션] 커밋
```
<br/>

#### 지연 로딩과 즉시 로딩
* 지연 로딩: 객체가 실제 사용될 때 로딩
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/3a1aea27-3450-4d0c-8b1a-b2b6e613aecf)\
  이렇게, 실제로 팀을 사용할 떄까지 대기했다가, \
  실제로 필요할 때 조회한다.

* 즉시 로딩: JOIN SQL로 한번에 연관된 객체까지 미리 조회
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/30d2135a-41a3-413d-8108-601d46230ecc)\
  몇 가지 설정으로, 분리되어있는 쿼리를 하나의 join 쿼리로 조회할 수 있다.
<br/>

## JPA 설정
spring-boot-starter-data-jpa 라이브러리를 사용하면 JPA와 스프링 데이터 JPA를 \
스프링 부트와 통합하고, 설정도 아주 간단히 할 수 있다.

spring-boot-starter-data-jpa 라이브러리를 사용해서 간단히 설정하는 방법을 알아보자.\
build.gradle 에 다음 의존 관계를 추가한다.
```java
//JPA, 스프링 데이터 JPA 추가
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
```
build.gradle 에 다음 의존 관계를 제거한다.
```java
//JdbcTemplate 추가
//implementation 'org.springframework.boot:spring-boot-starter-jdbc'
```
spring-boot-starter-data-jpa 는 spring-boot-starter-jdbc 도 \
함께 포함(의존)한다. 따라서 해당 라이브러리 의존관계를 제거해도 된다.\
참고로 mybatis-spring-boot-starter 도 spring-bootstarter-jdbc 를 포함하기 때문에 제거해도 된다.
<br/>
<br/>


### build.gradle - 의존관계 전체
```java
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-web'

    //JdbcTemplate 추가
    //implementation 'org.springframework.boot:spring-boot-starter-jdbc'

    //MyBatis 추가
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:2.2.0'

    //JPA, 스프링 데이터 JPA 추가
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    //H2 데이터베이스 추가
    runtimeOnly 'com.h2database:h2'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'

    //테스트에서 lombok 사용
    testCompileOnly 'org.projectlombok:lombok'
    testAnnotationProcessor 'org.projectlombok:lombok'
}
```
다음과 같은 라이브러리가 추가된다.
* hibernate-core : JPA 구현체인 하이버네이트 라이브러리
* jakarta.persistence-api : JPA 인터페이스
* spring-data-jpa : 스프링 데이터 JPA 라이브러리
<br/>

`application.properties` 에 다음 설정을 추가하자.\
`main - application.properties`
```java
#JPA log
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```
`test - application.properties`
```java
#JPA log
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```
* org.hibernate.SQL=DEBUG : 하이버네이트가 생성하고 실행하는 SQL을 확인할 수 있다.
* org.hibernate.type.descriptor.sql.BasicBinder=TRACE : SQL에 바인딩 되는 \
  파라미터를 확인할 수 있다.
* spring.jpa.show-sql=true : 참고로 이런 설정도 있다. \
  이전 설정은 logger 를 통해서 SQL이 출력된다. \
  이 설정은 System.out 콘솔을 통해서 SQL이 출력된다.\
  따라서 이 설정은 권장하지는 않는다.\
  (둘다 켜면 logger , System.out 둘다 로그가 출력되어서 같은 로그가 중복해서 출력된다.)
<br/>

## JPA 적용1 - 개발
JPA에서 가장 중요한 부분은 객체와 테이블을 매핑하는 것이다. \
JPA가 제공하는 애노테이션을 사용해서 Item 객체와 테이블을 매핑해보자.
<br/>
<br/>

### Item - ORM 매핑
```java
@Data
@Entity // JPA에서 관리한다.
public class Item {

    @Id // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB에서 값을 증가
    private Long id;

    @Column(name = "item_name", length = 10)
    private String itemName;
    private Integer price;
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
```
* @Entity : JPA가 사용하는 객체라는 뜻이다. 이 에노테이션이 있어야 JPA가 인식할 수 있다.\
  이렇게 @Entity 가 붙은 객체를 JPA에서는 엔티티라 한다.
* @Id : 테이블의 PK와 해당 필드를 매핑한다.
* @GeneratedValue(strategy = GenerationType.IDENTITY) : PK 생성 값을 \
  데이터베이스에서 생성하는 IDENTITY 방식을 사용한다. \
  예) MySQL auto increment
* @Column : 객체의 필드를 테이블의 컬럼과 매핑한다.
  * name = "item_name" : 객체는 itemName 이지만 테이블의 컬럼은 \
    item_name 이므로 이렇게 매핑했다.
  * length = 10 : JPA의 매핑 정보로 DDL( create table )도 생성할 수 있는데, \
    그때 컬럼의 길이 값으로 활용된다. ( varchar 10 )
  * @Column 을 생략할 경우 필드의 이름을 테이블 컬럼 이름으로 사용한다. \
    참고로 지금처럼 스프링 부트와 통합해서 사용하면 필드 이름을 \
    테이블 컬럼 명으로 변경할 때 객체 필드의 카멜 케이스를 \
    테이블 컬럼의 언더스코어로 자동으로 변환해준다.
    * itemName item_name , 따라서 위 예제의 \
      @Column(name = "item_name") 를 생략해도 된다.

JPA는 public 또는 protected 의 기본 생성자가 필수이다. 기본 생성자를 꼭 넣어주자.
```java
public Item() {}
```
이렇게 하면 기본 매핑은 모두 끝난다. \
이제 JPA를 실제 사용하는 코드를 작성해보자.\
우선 코드를 작성하고 실행하면서 하나씩 알아보자.
<br/>
<br/>

### JpaItemRepositoryV1
```java

@Slf4j
@Repository
@Transactional
public class JpaItemRepositoryV1 implements ItemRepository {

    private final EntityManager em;

    public JpaItemRepositoryV1(EntityManager em) {
        this.em = em;
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
        // 따로 업데이트는 안해줘도 됨. 트랜젝션이 커밋되는 시점에 알아서 업데이트 해줌.
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id);
        return Optional.ofNullable(item);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String jpql = "select i from Item i";
        Integer maxPrice = cond.getMaxPrice();
        String itemName = cond.getItemName();

        if (StringUtils.hasText(itemName) || maxPrice != null) {
            jpql += " where";
        }

        boolean andFlag = false;
        if (StringUtils.hasText(itemName)) {
            jpql += " i.itemName like concat('%',:itemName,'%')";
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                jpql += " and";
            }
            jpql += " i.price <= :maxPrice";
        }

        log.info("jpql={}", jpql);
        TypedQuery<Item> query = em.createQuery(jpql, Item.class);

        if (StringUtils.hasText(itemName)) {
            query.setParameter("itemName", itemName);
        }

        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }

        return query.getResultList();
    }
}

```
* private final EntityManager em 
  * 생성자를 보면 스프링을 통해 엔티티 매니저( EntityManager )\
    라는 것을 주입받은 것을 확인할 수 있다. \
    JPA의 모든 동작은 엔티티 매니저를 통해서 이루어진다. 엔티티 매니저는 내부에\
    데이터소스를 가지고 있고, 데이터베이스에 접근할 수 있다.
* @Transactional
  * JPA의 모든 데이터 변경(등록, 수정, 삭제)은 트랜잭션 안에서 이루어져야 한다. \
    조회는 트랜잭션이 없어도 가능하다. 변경의 경우 일반적으로 \
    서비스 계층에서 트랜잭션을 시작하기 때문에 문제가 없다. \
    하지만 이번 예제에서는 복잡한 비즈니스 로직이 없어서 서비스 계층에서 \
    트랜잭션을 걸지 않았다. JPA에서는 데이터 변경시 트랜잭션이 필수다. \
    따라서 리포지토리에 트랜잭션을 걸어주었다. \
    다시한번 강조하지만 일반적으로는 비즈니스 로직을 \
    시작하는 서비스 계층에 트랜잭션을 걸어주는 것이 맞다.

참고: JPA를 설정하려면 EntityManagerFactory , \
JPA 트랜잭션 매니저( JpaTransactionManager ), 데이터소스 등등 \
다양한 설정을 해야 한다. 스프링 부트는 이 과정을 모두 자동화 해준다.\
main() 메서드 부터 시작해서 JPA를 처음부터 어떻게 설정하는지는 JPA 기본편을 참고하자. \
그리고 스프링 부트의 자동 설정은 JpaBaseConfiguration 를 참고하자.

먼저 설정을 완료하고 실행한 다음에, 코드를 분석해보자.
<br/>
<br/>

### JpaConfig
```java
@Configuration
public class JpaConfig {

    private final EntityManager em;

    public JpaConfig(EntityManager em) {
        this.em = em;
    }

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository() {
        return new JpaItemRepositoryV1(em);
    }
}
```
설정은 이해하는데 크게 어렵지 않을 것이다
<br/>
<br/>

### ItemServiceApplication - 변경
```java
//@Import(MyBatisConfig.class)
@Import(JpaConfig.class)
@SpringBootApplication(scanBasePackages = "hello.itemservice.web")
public class ItemServiceApplication {}
```
JpaConfig 를 사용하도록 변경했다.
<br/>
<br/>


## JPA 적용2 - 리포지토리 분석
**JpaItemRepositoryV1** 코드를 분석해보자.
<br/>
<br/>

### save() - 저장
```java
@Override
public Item save(Item item) {
    em.persist(item);
    return item;
}
```
em.persist(item) : JPA에서 객체를 테이블에 저장할 때는 \
엔티티 매니저가 제공하는 persist() 메서드를 사용하면 된다.
<br/>

#### JPA가 만들어서 실행한 SQL
```sql
insert into item (id, item_name, price, quantity) values (null, ?, ?, ?)
또는
insert into item (id, item_name, price, quantity) values (default, ?, ?, ?)
또는
insert into item (item_name, price, quantity) values (?, ?, ?)
```
JPA가 만들어서 실행한 SQL을 보면 id 에 값이 빠져있는 것을 확인할 수 있다. \
PK 키 생성 전략을 IDENTITY 로 사용했기 때문에 JPA가 \
이런 쿼리를 만들어서 실행한 것이다. 물론 쿼리 실행 이후에 \
Item 객체의 id 필드에 데이터베이스가 생성한 PK값이 들어가게 된다. \
(JPA가 INSERT SQL 실행 이후에 생성된 ID 결과를 받아서 넣어준다)
<br/>
<br/>

#### PK 매핑 참고
```java
@Id // PK
@GeneratedValue(strategy = GenerationType.IDENTITY) // DB에서 값을 증가
private Long id;
```
<br/>

### update() - 수정
```java
@Override
public void update(Long itemId, ItemUpdateDto updateParam) {
    Item findItem = em.find(Item.class, itemId);
    findItem.setItemName(updateParam.getItemName());
    findItem.setPrice(updateParam.getPrice());
    findItem.setQuantity(updateParam.getQuantity());
    // 따로 업데이트는 안해줘도 됨. 트랜젝션이 커밋되는 시점에 알아서 업데이트 해줌.
}
```
<br/>

#### JPA가 만들어서 실행한 SQL
```sql
update item set item_name=?, price=?, quantity=? where id=?
```
em.update() 같은 메서드를 전혀 호출하지 않았다. \
그런데 어떻게 UPDATE SQL이 실행되는 것일까?

JPA는 트랜잭션이 커밋되는 시점에, 변경된 엔티티 객체가 있는지 확인한다. \
특정 엔티티 객체가 변경된 경우에는 UPDATE SQL을 실행한다.

JPA가 어떻게 변경된 엔티티 객체를 찾는지 명확하게 이해하려면 \
영속성 컨텍스트라는 JPA 내부 원리를 이해해야 한다.\
이 부분은 JPA 기본편에서 자세히 다룬다. 

지금은 트랜잭션 커밋 시점에 JPA가 변경된 엔티티 객체를 찾아서 \
UPDATE SQL을 수행한다고 이해하면 된다. 테스트의 경우 마지막에\
트랜잭션이 롤백되기 때문에 JPA는 UPDATE SQL을 실행하지 않는다.\
테스트에서 UPDATE SQL을 확인하려면 @Commit 을 붙이면 확인할 수 있다.
<br/>
<br/>

### findById() - 단건 조회
```java
@Override
public Optional<Item> findById(Long id) {
    Item item = em.find(Item.class, id);
    return Optional.ofNullable(item);
}
```
JPA에서 엔티티 객체를 PK를 기준으로 조회할 때는 find() 를 사용하고 \
조회 타입과, PK 값을 주면 된다. 그러면 JPA가 다음과 같은 \
조회 SQL을 만들어서 실행하고, 결과를 객체로 바로 변환해준다.
<br/>
<br/>

#### JPA가 만들어서 실행한 SQL
```java
select
    item0_.id as id1_0_0_,
    item0_.item_name as item_nam2_0_0_,
    item0_.price as price3_0_0_,
    item0_.quantity as quantity4_0_0_
from item item0_
where item0_.id=?
```
JPA(하이버네이트)가 만들어서 실행한 SQL은 별칭이 조금 복잡하다. \
조인이 발생하거나 복잡한 조건에서도 문제 없도록 \
기계적으로 만들다 보니 이런 결과가 나온 듯 하다.

JPA에서 단순히 PK를 기준으로 조회하는 것이 아닌, \
여러 데이터를 복잡한 조건으로 데이터를 조회하려면 어떻게 하면 될까?
<br/>

### findAll - 목록 조회
```java
@Override
public List<Item> findAll(ItemSearchCond cond) {
    String jpql = "select i from Item i";
    Integer maxPrice = cond.getMaxPrice();
    String itemName = cond.getItemName();

    if (StringUtils.hasText(itemName) || maxPrice != null) {
        jpql += " where";
    }

    boolean andFlag = false;
    if (StringUtils.hasText(itemName)) {
        jpql += " i.itemName like concat('%',:itemName,'%')";
        andFlag = true;
    }

    if (maxPrice != null) {
        if (andFlag) {
            jpql += " and";
        }
        jpql += " i.price <= :maxPrice";
    }

    log.info("jpql={}", jpql);
    TypedQuery<Item> query = em.createQuery(jpql, Item.class);

    if (StringUtils.hasText(itemName)) {
        query.setParameter("itemName", itemName);
    }

    if (maxPrice != null) {
        query.setParameter("maxPrice", maxPrice);
    }

    return query.getResultList();
}
```
<br/>

#### JPQL
JPA는 JPQL(Java Persistence Query Language)이라는 객체지향 쿼리 언어를 제공한다.\
주로 여러 데이터를 복잡한 조건으로 조회할 때 사용한다.

SQL이 테이블을 대상으로 한다면, \
JPQL은 엔티티 객체를 대상으로 SQL을 실행한다 생각하면 된다.
엔티티 객체를 대상으로 하기 때문에 from 다음에 Item 엔티티 객체 이름이 들어간다. \
엔티티 객체와 속성의 대소문자는 구분해야 한다.\
JPQL은 SQL과 문법이 거의 비슷하기 때문에 개발자들이 쉽게 적응할 수 있다.\
결과적으로 JPQL을 실행하면 그 안에 포함된 엔티티 객체의 매핑 정보를 \
활용해서 SQL을 만들게 된다.
<br/>
<br/>

#### 실행된 JPQL
```sql
select i from Item i
where i.itemName like concat('%',:itemName,'%')
    and i.price <= :maxPrice
```
<br/>

#### JPQL을 통해 실행된 SQL
```java
select
    item0_.id as id1_0_,
    item0_.item_name as item_nam2_0_,
    item0_.price as price3_0_,
    item0_.quantity as quantity4_0_
from item item0_
where (item0_.item_name like ('%'||?||'%'))
    and item0_.price<=?
```
<br/>

#### 파라미터
JPQL에서 파라미터는 다음과 같이 입력한다.
* where price <= :maxPrice
파라미터 바인딩은 다음과 같이 사용한다.
* query.setParameter("maxPrice", maxPrice)
<br/>

### 동적 쿼리 문제
JPA를 사용해도 동적 쿼리 문제가 남아있다. 동적 쿼리는 뒤에서 설명하는\
Querydsl이라는 기술을 활용하면 매우 깔끔하게 사용할 수 있다. \
실무에서는 동적 쿼리 문제 때문에, JPA 사용할 때 Querydsl도 함께 선택하게 된다.

> JPQL에 대한 자세한 내용은 JPA 기본편 강의를 참고하자
<br/>

## JPA 적용3 - 예외 변환
JPA의 경우 예외가 발생하면 JPA 예외가 발생하게 된다.

```java
@Slf4j
@Repository
@Transactional
public class JpaItemRepositoryV1 implements ItemRepository {

    private final EntityManager em;

    public JpaItemRepositoryV1(EntityManager em) {
        this.em = em;
    }

    @Override
    public Item save(Item item) {
        em.persist(item);
        return item;
    }
}
```

EntityManager 는 순수한 JPA 기술이고, 스프링과는 관계가 없다.\
따라서 엔티티 매니저는 예외가 발생하면 JPA 관련 예외를 발생시킨다.

JPA는 PersistenceException 과 그 하위 예외를 발생시킨다.\
추가로 JPA는 IllegalStateException , IllegalArgumentException 을 발생시킬 수 있다.

그렇다면 JPA 예외를 스프링 예외 추상화( DataAccessException )로 어떻게 변환할 수 있을까?\
비밀은 바로 @Repository 에 있다.
<br/>
<br/>

### 예외 변환 전
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/124ba9e8-8ea8-4d4c-978b-79939b826a83)
<br/>
<br/>

### @Repository의 기능
@Repository 가 붙은 클래스는 컴포넌트 스캔의 대상이 된다.\
@Repository 가 붙은 클래스는 예외 변환 AOP의 적용 대상이 된다.

스프링과 JPA를 함께 사용하는 경우 스프링은 JPA 예외 변환기\
( PersistenceExceptionTranslator )를 등록한다.

예외 변환 AOP 프록시는 JPA 관련 예외가 발생하면 JPA 예외 변환기를 통해 \
발생한 예외를 스프링 데이터 접근 예외로 변환한다.
<br/>
<br/>

### 예외 변환 후
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/79beebbf-46ac-4674-b9e0-6842d1649ce9)

결과적으로 리포지토리에 @Repository 애노테이션만 있으면 \
스프링이 예외 변환을 처리하는 AOP를 만들어준다.

> 스프링 부트는 PersistenceExceptionTranslationPostProcessor 를 자동으로 등록하는데, \
> 여기에서 @Repository 를 AOP 프록시로 만드는 어드바이저가 등록된다.


> 복잡한 과정을 거쳐서 실제 예외를 변환하는데, 실제 JPA 예외를 변환하는 코드는\
> EntityManagerFactoryUtils.convertJpaAccessExceptionIfPossible() 이다
<br/>
<br/>
