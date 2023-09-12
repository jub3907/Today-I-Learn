## 스프링 트랜잭션 전파1 - 커밋, 롤백
트랜잭션이 둘 이상 있을 때 어떻게 동작하는지 자세히 알아보고, \
스프링이 제공하는 트랜잭션 전파 (propagation)라는 개념도 알아보자.

트랜잭션 전파를 이해하는 과정을 통해서 스프링 트랜잭션의 동작 원리도 \
더 깊이있게 이해할 수 있을 것이다.\
먼저 간단한 스프링 트랜잭션 코드를 통해 기본 원리를 학습하고, \
이후에 실제 예제를 통해 어떻게 활용하는지 알아보겠다.

간단한 예제 코드로 스프링 트랜잭션을 실행해보자
<br/>
<br/>

### BasicTxTest
```java
@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration  
    static class Config {

        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("트랜젝션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜젝션 커밋 시작");
        txManager.commit(status);
        log.info("트랜젝션 커밋 완료");
    }

    @Test
    void rollback() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 롤백 시작");
        txManager.rollback(status);
        log.info("트랜잭션 롤백 완료");
    }
}
```
* @TestConfiguration : 해당 테스트에서 필요한 스프링 설정을 추가로 할 수 있다.
* DataSourceTransactionManager 를 스프링 빈으로 등록했다. \
  이후 트랜잭션 매니저인 PlatformTransactionManager 를 주입 받으면 \
  방금 등록한 DataSourceTransactionManager 가 주입된다.

실행하기 전에 트랜잭션 관련 로그를 확인할 수 있도록 다음을 꼭! 추가하자.\
`application.properties` 추가
```java
logging.level.org.springframework.transaction.interceptor=TRACE
logging.level.org.springframework.jdbc.datasource.DataSourceTransactionManager=DEBUG
#JPA log
logging.level.org.springframework.orm.jpa.JpaTransactionManager=DEBUG
logging.level.org.hibernate.resource.transaction=DEBUG
#JPA SQL
logging.level.org.hibernate.SQL=DEBUG

```
<br/>
<br/>

#### commit()
`txManager.getTransaction(new DefaultTransactionAttribute())`\
트랜잭션 매니저를 통해 트랜잭션을 시작(획득)한다.

`txManager.commit(status)`\
트랜잭션을 커밋한다.
<br/>
<br/>

#### commit() - 실행 로그
```java
ringtx.propagation.BasicTxTest : 트랜잭션 시작
DataSourceTransactionManager : Creating new transaction with name [null]
DataSourceTransactionManager : Acquired Connection [conn0] for JDBC transaction
DataSourceTransactionManager : Switching JDBC Connection [conn0] to manual commit

ringtx.propagation.BasicTxTest : 트랜잭션 커밋 시작
DataSourceTransactionManager : Initiating transaction commit
DataSourceTransactionManager : Committing JDBC transaction on Connection [conn0]
DataSourceTransactionManager : Releasing JDBC Connection [conn0] after transaction
ringtx.propagation.BasicTxTest : 트랜잭션 커밋 완료
```
<br/>

#### rollback()
`txManager.getTransaction(new DefaultTransactionAttribute())`\
트랜잭션 매니저를 통해 트랜잭션을 시작(획득)한다.

`txManager.rollback(status)`\
트랜잭션을 롤백한다.
<br/>
<br/>

#### rollback() - 실행 로그
```java
ringtx.propagation.BasicTxTest : 트랜잭션 시작
DataSourceTransactionManager : Creating new transaction with name [null]
DataSourceTransactionManager : Acquired Connection [conn0] for JDBC transaction
DataSourceTransactionManager : Switching JDBC Connection [conn0] to manual commit
ringtx.propagation.BasicTxTest : 트랜잭션 롤백 시작
DataSourceTransactionManager : Initiating transaction rollback
DataSourceTransactionManager : Rolling back JDBC transaction on Connection [conn0]
DataSourceTransactionManager : Releasing JDBC Connection [conn0] after transaction
ringtx.propagation.BasicTxTest : 트랜잭션 롤백 완료
```
이미 앞서 학습한 내용들이어서 이해하기는 어렵지 않을 것이다. \
다음에는 트랜잭션을 하나 더 추가해보자.
<br/>
<br/>

## 스프링 트랜잭션 전파2 - 트랜잭션 두 번 사용
이번에는 트랜잭션이 각각 따로 사용되는 경우를 확인해보자.\
이 예제는 트랜잭션1이 완전히 끝나고나서 트랜잭션2를 수행한다.
<br/>
<br/>

#### double_commit() - BasicTxTest 추가
```java
@Test
void double_commit() {
   log.info("트랜젝션1 시작");
   TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
   log.info("트랜젝션1 커밋");
   txManager.commit(tx1);


   log.info("트랜젝션2 시작");
   TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
   log.info("트랜젝션2 커밋");
   txManager.commit(tx2);
}
```
<br/>
<br/>

#### double_commit() - 실행 로그
```java
트랜잭션1 시작
Creating new transaction with name [null]:
PROPAGATION_REQUIRED,ISOLATION_DEFAULT
Acquired Connection [HikariProxyConnection@1064414847 wrapping conn0] for JDBC transaction
Switching JDBC Connection [HikariProxyConnection@1064414847 wrapping conn0] to manual commit

트랜잭션1 커밋
Initiating transaction commit
Committing JDBC transaction on Connection [HikariProxyConnection@1064414847 wrapping conn0]
Releasing JDBC Connection [HikariProxyConnection@1064414847 wrapping conn0] after transaction

트랜잭션2 시작
Creating new transaction with name [null]:
PROPAGATION_REQUIRED,ISOLATION_DEFAULT
Acquired Connection [HikariProxyConnection@778350106 wrapping conn0] for JDBC transaction
Switching JDBC Connection [HikariProxyConnection@778350106 wrapping conn0] to manual commit

트랜잭션2 커밋
Initiating transaction commit
Committing JDBC transaction on Connection [HikariProxyConnection@778350106 wrapping conn0]
Releasing JDBC Connection [HikariProxyConnection@778350106 wrapping conn0] after transaction
```
<br/>
<br/>

#### 트랜잭션1
```java
Acquired Connection [HikariProxyConnection@1064414847 wrapping conn0] for JDBC transaction
```
트랜잭션1을 시작하고, 커넥션 풀에서 conn0 커넥션을 획득했다.

```java
Releasing JDBC Connection [HikariProxyConnection@1064414847 wrapping conn0] after transaction
```
트랜잭션1을 커밋하고, 커넥션 풀에 conn0 커넥션을 반납했다.
<br/>
<br/>

#### 트랜잭션2
```java
Acquired Connection [HikariProxyConnection@ 778350106 wrapping conn0] for JDBC transaction
```
트랜잭션2을 시작하고, 커넥션 풀에서 conn0 커넥션을 획득했다.
```java
Releasing JDBC Connection [HikariProxyConnection@ 778350106 wrapping conn0] after transaction
```
트랜잭션2을 커밋하고, 커넥션 풀에 conn0 커넥션을 반납했다.
<br/>
<br/>

#### 주의!
로그를 보면 트랜잭션1과 트랜잭션2가 같은 conn0 커넥션을 사용중이다. \
이것은 중간에 커넥션 풀 때문에 그런 것이다. \
트랜잭션1은 conn0 커넥션을 모두 사용하고 커넥션 풀에 반납까지 완료했다. \
이후에 트랜잭션2가 conn0 를 커넥션 풀에서 획득한 것이다. \
따라서 둘은 완전히 다른 커넥션으로 인지하는 것이 맞다.

그렇다면 둘을 구분할 수 있는 다른 방법은 없을까?

히카리 커넥션 풀에서 커넥션을 획득하면 실제 커넥션을 그대로 \
반환하는 것이 아니라 내부 관리를 위해 히카리 프록시 커넥션이라는 \
객체를 생성해서 반환한다. 물론 내부에는 실제 커넥션이 포함되어 있다.\
이 객체의 주소를 확인하면 커넥션 풀에서 획득한 커넥션을 구분할 수 있다.

* 트랜잭션1: Acquired Connection [HikariProxyConnection@1000000 wrapping conn0]
* 트랜잭션2: Acquired Connection [HikariProxyConnection@2000000 wrapping conn0]

히카리 커넥션풀이 반환해주는 커넥션을 다루는 프록시 객체의 주소가 \
트랜잭션1은 HikariProxyConnection@1000000 이고,\
트랜잭션2는 HikariProxyConnection@2000000 으로 서로 다른 것을 확인할 수 있다.

결과적으로 conn0 을 통해 커넥션이 재사용 된 것을 확인할 수 있고,\
HikariProxyConnection@1000000 , HikariProxyConnection@2000000 을 통해 \
각각 커넥션 풀에서 커넥션을 조회한 것을 확인할 수 있다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/a61983ac-8aee-4190-8965-82e080e7599a)

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/6d01cea8-01ca-4719-8ff2-a1e6bef03e00)

트랜잭션이 각각 수행되면서 사용되는 DB 커넥션도 각각 다르다.\
이 경우 트랜잭션을 각자 관리하기 때문에 전체 트랜잭션을 묶을 수 없다. \
예를 들어서 트랜잭션1이 커밋하고, 트랜잭션2가 롤백하는 경우 \
트랜잭션1에서 저장한 데이터는 커밋되고, \
트랜잭션2에서 저장한 데이터는 롤백된다. 다음 예제를 확인해보자.
<br/>
<br/>

### double_commit_rollback() - BasicTxTest 추가
```java
    @Test
    void double_commit_rollback() {
        log.info("트랜젝션1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜젝션1 커밋");
        txManager.commit(tx1);


        log.info("트랜젝션2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜젝션2 커밋");
        txManager.rollback(tx2);
    }
    
```
예제에서는 트랜잭션1은 커밋하고, 트랜잭션2는 롤백한다.\
전체 트랜잭션을 묶지 않고 각각 관리했기 때문에, \
트랜잭션1에서 저장한 데이터는 커밋되고, 트랜잭션2 에서 저장한 데이터는 롤백된다.
<br/>
<br/>

#### double_commit_rollback() - 실행 로그
```java
트랜잭션1 시작
Creating new transaction with name [null]:
PROPAGATION_REQUIRED,ISOLATION_DEFAULT
Acquired Connection [HikariProxyConnection@1943867171 wrapping conn0] for JDBC transaction
Switching JDBC Connection [HikariProxyConnection@1943867171 wrapping conn0] to manual commit
트랜잭션1 커밋
Initiating transaction commit
Committing JDBC transaction on Connection [HikariProxyConnection@1943867171 wrapping conn0]
Releasing JDBC Connection [HikariProxyConnection@1943867171 wrapping conn0] after transaction

트랜잭션2 시작
Creating new transaction with name [null]:
PROPAGATION_REQUIRED,ISOLATION_DEFAULT
Acquired Connection [HikariProxyConnection@239290560 wrapping conn0] for JDBC transaction
Switching JDBC Connection [HikariProxyConnection@239290560 wrapping conn0] to manual commit
트랜잭션2 롤백
Initiating transaction rollback
Rolling back JDBC transaction on Connection [HikariProxyConnection@239290560 wrapping conn0]
Releasing JDBC Connection [HikariProxyConnection@239290560 wrapping conn0] after transaction
```
로그를 보면 트랜잭션1은 커밋되지만, 트랜잭션2는 롤백되는 것을 확인할 수 있다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/d91c00f1-5263-4589-9a5d-16cd017a695c)

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/ea7a4b24-720e-406b-a46e-69f3db7028da)

여기까지는 이미 앞서 학습한 내용들이라 이해하기 어렵지 않을 것이다. \
이제 본격적으로 트랜잭션 전파에 대해서 알아보자.
<br/>
<br/>

## 스프링 트랜잭션 전파3 - 전파 기본
트랜잭션을 각각 사용하는 것이 아니라, 트랜잭션이 이미 진행중인데, \
여기에 추가로 트랜잭션을 수행하면 어떻게 될까?

기존 트랜잭션과 별도의 트랜잭션을 진행해야 할까? \
아니면 기존 트랜잭션을 그대로 이어 받아서 트랜잭션을 수행해야 할까?

이런 경우 어떻게 동작할지 결정하는 것을 트랜잭션 전파(propagation)라 한다.\
참고로 스프링은 다양한 트랜잭션 전파 옵션을 제공한다.\


> 지금부터 설명하는 내용은 트랜잭션 전파의 기본 옵션인 REQUIRED 를 기준으로 설명한다.\
> 옵션에 대한 내용은 마지막에 설명한다. 뒤에서 설명할 것이므로 참고만 해두자.

예제를 통해 본격적으로 스프링이 제공하는 트랜잭션 전파에 대해서 알아보자.

**외부 트랜잭션이 수행중인데, 내부 트랜잭션이 추가로 수행됨**

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/d6be3864-6ed5-4e31-9c04-34343119c11d)

외부 트랜잭션이 수행중이고, 아직 끝나지 않았는데, 내부 트랜잭션이 수행된다.\
외부 트랜잭션이라고 이름 붙인 것은 둘 중 상대적으로 밖에 있기 때문에 \
외부 트랜잭션이라 한다. 처음 시작된 트랜잭션으로 이해하면 된다.

내부 트랜잭션은 외부에 트랜잭션이 수행되고 있는 도중에 호출되기 때문에 \
마치 내부에 있는 것 처럼 보여서 내부 트랜잭션이라 한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/4f6a7ef7-aa7d-4600-8622-2116faa7cae7)

스프링 이 경우 외부 트랜잭션과 내부 트랜잭션을 묶어서 하나의 트랜잭션을 만들어준다. \
내부 트랜잭션이 외부 트랜잭션에 참여하는 것이다. 이것이 기본 동작이고, \
옵션을 통해 다른 동작방식도 선택할 수 있다.\
(다른 동작 방식은 뒤에 설명한다.)
<br/>
<br/>

### 물리 트랜잭션, 논리 트랜잭션
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/8b8b7a4f-74d7-435a-a4d2-22a477a84354)

스프링은 이해를 돕기 위해 논리 트랜잭션과 물리 트랜잭션이라는 개념을 나눈다.\
논리 트랜잭션들은 하나의 물리 트랜잭션으로 묶인다.\
물리 트랜잭션은 우리가 이해하는 실제 데이터베이스에 적용되는 트랜잭션을 뜻한다. \
실제 커넥션을 통해서 트랜잭션을 시작( setAutoCommit(false)) 하고, \
실제 커넥션을 통해서 커밋, 롤백하는 단위이다.

논리 트랜잭션은 트랜잭션 매니저를 통해 트랜잭션을 사용하는 단위이다.\
이러한 논리 트랜잭션 개념은 트랜잭션이 진행되는 중에 내부에 \
추가로 트랜잭션을 사용하는 경우에 나타난다. \
단순히 트랜잭션이 하나인 경우 둘을 구분하지는 않는다. \
(더 정확히는 REQUIRED 전파 옵션을 사용하는 경우에 나타나고, 이 옵션은 뒤에서 설명한다.)

그럼 왜 이렇게 논리 트랜잭션과 물리 트랜잭션을 나누어 설명하는 것일까?\
트랜잭션이 사용중일 때 또 다른 트랜잭션이 내부에 사용되면 여러가지 복잡한 상황이 발생한다.\
이때 논리 트랜잭션 개념을 도입하면 다음과 같은 단순한 원칙을 만들 수 있다.
<br/>
<br/>

### 원칙
* **모든 논리 트랜잭션이 커밋되어야 물리 트랜잭션이 커밋된다.**
* **하나의 논리 트랜잭션이라도 롤백되면 물리 트랜잭션은 롤백된다.**
풀어서 설명하면 이렇게 된다. 모든 트랜잭션 매니저를 커밋해야 물리 트랜잭션이 커밋된다.\
하나의 트랜잭션 매니저라도 롤백하면 물리 트랜잭션은 롤백된다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/9d9ebe40-4733-4487-80b8-258304dfb743)
* 모든 논리 트랜잭션이 커밋 되었으므로 물리 트랜잭션도 커밋된다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/b457a52b-1292-448a-b56c-adb748bfa09f)
* 외부 논리 트랜잭션이 롤백 되었으므로 물리 트랜잭션은 롤백된다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/bff0f055-75df-4b0a-acb0-9da688292e34)
* 내부 논리 트랜잭션이 롤백 되었으므로 물리 트랜잭션은 롤백된다
<br/>

## 스프링 트랜잭션 전파4 - 전파 예제
예제 코드를 통해서 스프링 트랜잭션 전파를 자세히 알아보자.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/ba9d011e-f89c-43d3-baef-802e144aecc7)
<br/>
<br/>

### inner_commit() - BasicTxTest 추가
```java
@Test
void inner_commit() {
   log.info("외부 트랜젝션 시작");
   TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
   log.info("outer.isNewTransaction()={}", outer.isNewTransaction());

   log.info("내부 트랜젝션 시작");
   TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
   log.info("inner.isNewTransaction()={}", inner.isNewTransaction());

   log.info("내부 트랜젝션 커밋");
   txManager.commit(inner);

   log.info("외부 트랜젝션 커밋");
   txManager.commit(outer);
}
```
* 외부 트랜잭션이 수행중인데, 내부 트랜잭션을 추가로 수행했다.\
* 외부 트랜잭션은 처음 수행된 트랜잭션이다. \
* 이 경우 신규 트랜잭션( isNewTransaction=true )이 된다.

* 내부 트랜잭션을 시작하는 시점에는 이미 외부 트랜잭션이 진행중인 상태이다. \
  이 경우 내부 트랜잭션은 외부 트랜잭션에 참여한다.
* 트랜잭션 참여
  * 내부 트랜잭션이 외부 트랜잭션에 참여한다는 뜻은 내부 트랜잭션이 \
    외부 트랜잭션을 그대로 이어 받아서 따른다는 뜻이다.
  * 다른 관점으로 보면 외부 트랜잭션의 범위가 내부 트랜잭션까지 넓어진다는 뜻이다.
  * 외부에서 시작된 물리적인 트랜잭션의 범위가 내부 트랜잭션까지 넓어진다는 뜻이다.
  * 정리하면 **외부 트랜잭션과 내부 트랜잭션이 하나의 물리 트랜잭션으로 묶이는** 것이다.
* 내부 트랜잭션은 이미 진행중인 외부 트랜잭션에 참여한다. \
  이 경우 신규 트랜잭션이 아니다. ( isNewTransaction=false ).
* 예제에서는 둘다 성공적으로 커밋했다.

이 예제에서는 외부 트랜잭션과 내부 트랜잭션이 하나의 물리 트랜잭션으로 묶인다고 설명했다.\
그런데 코드를 잘 보면 커밋을 두 번 호출했다. \
트랜잭션을 생각해보면 하나의 커넥션에 커밋은 한번만 호출할 수 있다.\
커밋이나 롤백을 하면 해당 트랜잭션은 끝나버린다.
```java
txManager.commit(inner);
txManager.commit(outer);
```
스프링은 어떻게 어떻게 외부 트랜잭션과 내부 트랜잭션을 묶어서 \
하나의 물리 트랜잭션으로 묶어서 동작하게 하는지 자세히 알아보자.
<br/>
<br/>

#### 실행 결과 - inner_commit()
```java
외부 트랜잭션 시작
Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
Acquired Connection [HikariProxyConnection@1943867171 wrapping conn0] for JDBC transaction
Switching JDBC Connection [HikariProxyConnection@1943867171 wrapping conn0] to manual commit
outer.isNewTransaction()=true

내부 트랜잭션 시작
Participating in existing transaction
inner.isNewTransaction()=false
내부 트랜잭션 커밋
외부 트랜잭션 커밋

Initiating transaction commit
Committing JDBC transaction on Connection [HikariProxyConnection@1943867171 wrapping conn0]
Releasing JDBC Connection [HikariProxyConnection@1943867171 wrapping conn0] after transaction
```
* 내부 트랜잭션을 시작할 때 Participating in existing transaction 이라는 \
  메시지를 확인할 수 있다. 이 메시지는 내부 트랜잭션이 \
  기존에 존재하는 외부 트랜잭션에 참여한다는 뜻이다.
* 실행 결과를 보면 외부 트랜잭션을 시작하거나 커밋할 때는 \
  DB 커넥션을 통한 물리 트랜잭션을 시작 ( manual commit )하고,\
  DB 커넥션을 통해 커밋 하는 것을 확인할 수 있다. \
  그런데 내부 트랜잭션을 시작하거나 커밋할 때는 DB 커넥션을 통해 \
  커밋하는 로그를 전혀 확인할 수 없다.
* 정리하면 외부 트랜잭션만 물리 트랜잭션을 시작하고, 커밋한다.
* 만약 내부 트랜잭션이 실제 물리 트랜잭션을 커밋하면 트랜잭션이 끝나버리기 때문에, \
  트랜잭션을 처음 시작한 외부 트랜잭션까지 이어갈 수 없다. \
  따라서 내부 트랜잭션은 DB 커넥션을 통한 물리 트랜잭션을 커밋하면 안된다.
* 스프링은 이렇게 여러 트랜잭션이 함께 사용되는 경우, \
  처음 트랜잭션을 시작한 외부 트랜잭션이 실제 물리 트랜잭션을 관리하도록 한다. \
  이를 통해 트랜잭션 중복 커밋 문제를 해결한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/a340376d-79c7-40b8-b976-9686c42488dd)

트랜잭션 전파가 실제 어떻게 동작하는지 그림으로 알아보자.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/a9033400-bfda-4841-a2b3-97ad9b872c0c)

#### 요청 흐름 - 외부 트랜잭션
1. txManager.getTransaction() 를 호출해서 외부 트랜잭션을 시작한다.
2. 트랜잭션 매니저는 데이터소스를 통해 커넥션을 생성한다.
3. 생성한 커넥션을 수동 커밋 모드( setAutoCommit(false) )로 설정한다. - **물리 트랜잭션 시작**
4. 트랜잭션 매니저는 트랜잭션 동기화 매니저에 커넥션을 보관한다.
5. 트랜잭션 매니저는 트랜잭션을 생성한 결과를 TransactionStatus 에 담아서 반환하는데, \
   여기에 신규 트랜잭션의 여부가 담겨 있다. isNewTransaction 를 통해\
   신규 트랜잭션 여부를 확인할 수 있다. \
   트랜잭션을 처음 시작했으므로 신규 트랜잭션이다.( true )
7. 로직1이 사용되고, 커넥션이 필요한 경우 트랜잭션 동기화 매니저를 통해\
   트랜잭션이 적용된 커넥션을 획득해서 사용한다.
<br/>

#### 요청 흐름 - 내부 트랜잭션
7. txManager.getTransaction() 를 호출해서 내부 트랜잭션을 시작한다.
8. 트랜잭션 매니저는 트랜잭션 동기화 매니저를 통해서 기존 트랜잭션이 존재하는지 확인한다.
9. 기존 트랜잭션이 존재하므로 기존 트랜잭션에 참여한다. \
   기존 트랜잭션에 참여한다는 뜻은 사실 아무것도 하지 않는다는 뜻이다.
   이미 기존 트랜잭션인 외부 트랜잭션에서 물리 트랜잭션을 시작했다.\
   그리고 물리 트랜잭션이 시작된 커넥션을 트랜잭션 동기화 매니저에 담아두었다.\
   따라서 이미 물리 트랜잭션이 진행중이므로 그냥 두면 이후 로직이 \
   기존에 시작된 트랜잭션을 자연스럽게 사용하게 되는 것이다.\
   이후 로직은 자연스럽게 트랜잭션 동기화 매니저에 보관된 기존 커넥션을 사용하게 된다.
11. 트랜잭션 매니저는 트랜잭션을 생성한 결과를 TransactionStatus 에 담아서 반환하는데, \
    여기에서 isNewTransaction 를 통해 신규 트랜잭션 여부를 확인할 수 있다. \
    여기서는 기존 트랜잭션에 참여했기 때문에 신규 트랜잭션이 아니다. ( false )
13. 로직2가 사용되고, 커넥션이 필요한 경우 트랜잭션 동기화 매니저를 통해 \
    외부 트랜잭션이 보관한 커넥션을 획득해서 사용한다.
<br/>

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/16e17b2c-2487-48c9-9d22-88ae8dd7dde5)

#### 응답 흐름 - 내부 트랜잭션
12. 로직2가 끝나고 트랜잭션 매니저를 통해 내부 트랜잭션을 커밋한다.
13. 트랜잭션 매니저는 커밋 시점에 신규 트랜잭션 여부에 따라 다르게 동작한다. \
    이 경우 신규 트랜잭션이 아니기 때문에 실제 커밋을 호출하지 않는다. 이 부분이 중요한데, \
    실제 커넥션에 커밋이나 롤백을 호출하면 물리 트랜잭션이 끝나버린다. \
    아직 트랜잭션이 끝난 것이 아니기 때문에 실제 커밋을 호출하면 안된다. \
    물리 트랜잭션은 외부 트랜잭션을 종료할 때 까지 이어져야한다.
<br/>

#### 응답 흐름 - 외부 트랜잭션
14. 로직1이 끝나고 트랜잭션 매니저를 통해 외부 트랜잭션을 커밋한다.
15. 트랜잭션 매니저는 커밋 시점에 신규 트랜잭션 여부에 따라 다르게 동작한다. \
    외부 트랜잭션은 신규 트랜잭션이다. 따라서 DB 커넥션에 실제 커밋을 호출한다.
17. 트랜잭션 매니저에 커밋하는 것이 논리적인 커밋이라면, 실제 커넥션에 커밋하는 것을 \
    물리 커밋이라 할 수 있다. 실제 데이터베이스에 커밋이 반영되고, 물리 트랜잭션도 끝난다.
<br/>

### 핵심 정리
여기서 핵심은 트랜잭션 매니저에 커밋을 호출한다고해서 항상 \
실제 커넥션에 물리 커밋이 발생하지는 않는다는 점이다.
신규 트랜잭션인 경우에만 실제 커넥션을 사용해서 물리 커밋과 롤백을 수행한다. \
신규 트랜잭션이 아니면 실제 물리 커넥션을 사용하지 않는다.

이렇게 트랜잭션이 내부에서 추가로 사용되면 트랜잭션 매니저에 커밋하는 것이\
항상 물리 커밋으로 이어지지 않는다. 그래서 이 경우 논리 트랜잭션과 \
물리 트랜잭션을 나누게 된다. 또는 외부 트랜잭션과 내부 트랜잭션으로 나누어 설명하기도 한다.
트랜잭션이 내부에서 추가로 사용되면, 트랜잭션 매니저를 통해 논리 트랜잭션을 관리하고, \
모든 논리 트랜잭션이 커밋되면 물리 트랜잭션이 커밋된다고 이해하면 된다.
<br/>
<br/>


## 스프링 트랜잭션 전파5 - 외부 롤백
이번에는 내부 트랜잭션은 커밋되는데, 외부 트랜잭션이 롤백되는 상황을 알아보자.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/4bfbcba3-dfa6-4f50-b4df-cbe7072f5405)

논리 트랜잭션이 하나라도 롤백되면 전체 물리 트랜잭션은 롤백된다.\
따라서 이 경우 내부 트랜잭션이 커밋했어도, \
내부 트랜잭션 안에서 저장한 데이터도 모두 함께 롤백된다.
<br/>
<br/>

### outer_rollback() - BasicTxTest 추가
```java
@Test
void outer_rollback() {
   log.info("외부 트랜젝션 시작");
   TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());

   log.info("내부 트랜젝션 시작");
   TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());

   log.info("내부 트랜젝션 커밋");
   txManager.commit(inner);

   log.info("외부 트랜젝션 롤백");
   txManager.rollback(outer);
}
```
<br/>
<br/>

#### 실행 결과 - outer_rollback()
```java
외부 트랜잭션 시작
Creating new transaction with name [null]:
PROPAGATION_REQUIRED,ISOLATION_DEFAULT
Acquired Connection [HikariProxyConnection@461376017 wrapping conn0] for JDBC transaction
Switching JDBC Connection [HikariProxyConnection@461376017 wrapping conn0] to manual commit

내부 트랜잭션 시작
Participating in existing transaction
내부 트랜잭션 커밋

외부 트랜잭션 롤백
Initiating transaction rollback
Rolling back JDBC transaction on Connection [HikariProxyConnection@461376017 wrapping conn0]
Releasing JDBC Connection [HikariProxyConnection@461376017 wrapping conn0] after transaction
```
* 외부 트랜잭션이 물리 트랜잭션을 시작하고 롤백하는 것을 확인할 수 있다.
* 내부 트랜잭션은 앞서 배운대로 직접 물리 트랜잭션에 관여하지 않는다.
* 결과적으로 외부 트랜잭션에서 시작한 물리 트랜잭션의 범위가 내부 트랜잭션까지 사용된다.\
  이후 외부 트랜잭션이 롤백되면서 전체 내용은 모두 롤백된다.
<br/>
<br/>

#### 응답 흐름
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/801714b4-e28d-4536-be87-cd918cee4155)

요청 흐름은 앞서 본 것과 같으므로 생략했다. 이번에는 응답 흐름에 집중해보자
<br/>
<br/>

#### 응답 흐름 - 내부 트랜잭션
1. 로직2가 끝나고 트랜잭션 매니저를 통해 내부 트랜잭션을 커밋한다.
2. 트랜잭션 매니저는 커밋 시점에 신규 트랜잭션 여부에 따라 다르게 동작한다. \
   이 경우 신규 트랜잭션이 아니기 때문에 실제 커밋을 호출하지 않는다. 이 부분이 중요한데,\
   실제 커넥션에 커밋이나 롤백을 호출하면 물리 트랜잭션이 끝나버린다. \
   아직 트랜잭션이 끝난 것이 아니기 때문에 실제 커밋을 호출하면 안된다. \
   물리 트랜잭션은 외부 트랜잭션을 종료할 때 까지 이어져야한다.
<br/>
<br/>

#### 응답 흐름 - 외부 트랜잭션
3. 로직1이 끝나고 트랜잭션 매니저를 통해 외부 트랜잭션을 롤백한다.
4. 트랜잭션 매니저는 롤백 시점에 신규 트랜잭션 여부에 따라 다르게 동작한다. \
   외부 트랜잭션은 신규 트랜잭션이다. 따라서 DB 커넥션에 실제 롤백을 호출한다.
6. 트랜잭션 매니저에 롤백하는 것이 논리적인 롤백이라면, 실제 커넥션에 롤백하는 것을 \
   물리 롤백이라 할 수 있다. 실제 데이터베이스에 롤백이 반영되고, 물리 트랜잭션도 끝난다.

앞서 학습한 내용과 거의 같고, 커밋을 롤백으로 바꾸었을 뿐이기 때문에 이해하기 어렵지는 않을 것이다.
<br/>
<br/>

## 스프링 트랜잭션 전파6 - 내부 롤백
이번에는 내부 트랜잭션은 롤백되는데, 외부 트랜잭션이 커밋되는 상황을 알아보자.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/51b1f943-755a-4189-953d-a88e331bea15)

이 상황은 겉으로 보기에는 단순하지만, 실제로는 단순하지 않다. \
내부 트랜잭션이 롤백을 했지만, 내부 트랜잭션은 물리 트랜잭션에 영향을 주지 않는다. \
그런데 외부 트랜잭션은 커밋을 해버린다. 지금까지 학습한 내용을 돌아보면 \
외부 트랜잭션만 물리 트랜잭션에 영향을 주기 때문에 물리 트랜잭션이 커밋될 것 같다.

전체를 롤백해야 하는데, 스프링은 이 문제를 어떻게 해결할까? 지금부터 함께 살펴보자.
<br/>
<br/>

### inner_rollback() - BasicTxTest 추가
```java
@Test
void inner_rollback() {
   log.info("외부 트랜젝션 시작");
   TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());

   log.info("내부 트랜젝션 시작");
   TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());

   log.info("내부 트랜젝션 롤백");
   txManager.rollback(inner);

   log.info("외부 트랜젝션 커밋");
   txManager.commit(outer);
}
```
실행 결과를 보면 마지막에 외부 트랜잭션을 커밋할 때 \
UnexpectedRollbackException.class 이 발생하는 것을 확인할 수 있다. \
이 부분은 바로 뒤에 설명한다.
<br/>
<br/>

#### 실행 결과 - inner_rollback
```java
외부 트랜잭션 시작
Creating new transaction with name [null]:
PROPAGATION_REQUIRED,ISOLATION_DEFAULT
Acquired Connection [HikariProxyConnection@220038608 wrapping conn0] for JDBC transaction
Switching JDBC Connection [HikariProxyConnection@220038608 wrapping conn0] to manual commit

내부 트랜잭션 시작
Participating in existing transaction
내부 트랜잭션 롤백
Participating transaction failed - marking existing transaction as rollbackonly
Setting JDBC transaction [HikariProxyConnection@220038608 wrapping conn0] rollback-only

외부 트랜잭션 커밋
Global transaction is marked as rollback-only but transactional code requested commit
Initiating transaction rollback
Rolling back JDBC transaction on Connection [HikariProxyConnection@220038608 wrapping conn0]
Releasing JDBC Connection [HikariProxyConnection@220038608 wrapping conn0] after transaction
```
* 외부 트랜잭션 시작
  * 물리 트랜잭션을 시작한다.
* 내부 트랜잭션 시작
  * Participating in existing transaction
  * 기존 트랜잭션에 참여한다.
* 내부 트랜잭션 롤백
  * Participating transaction failed - marking existing transaction as rollbackonly
  * 내부 트랜잭션을 롤백하면 실제 물리 트랜잭션은 롤백하지 않는다. \
    대신에 기존 트랜잭션을 롤백 전용으로 표시한다.
* 외부 트랜잭션 커밋
  * 외부 트랜잭션을 커밋한다.
  * Global transaction is marked as rollback-only
  * 커밋을 호출했지만, 전체 트랜잭션이 롤백 전용으로 표시되어 있다. 따라서 물리 트랜잭션을 롤백한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/96b36863-a7ca-475d-8a9d-262bea91e6d0)
<br/>
<br/>

### 응답 흐름
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/6ed8671e-bf6a-4834-8b97-900b8b599f03)
<br/>
<br/>

#### 응답 흐름 - 내부 트랜잭션
1. 로직2가 끝나고 트랜잭션 매니저를 통해 내부 트랜잭션을 롤백한다. \
   (로직2에 문제가 있어서 롤백한다고 가정한다.)
3. 트랜잭션 매니저는 롤백 시점에 신규 트랜잭션 여부에 따라 다르게 동작한다. \
   이 경우 신규 트랜잭션이 아니기 때문에 실제 롤백을 호출하지 않는다. \
   이 부분이 중요한데, 실제 커넥션에 커밋이나 롤백을 호출하면 물리 트랜잭션이 끝나버린다.\
   아직 트랜잭션이 끝난 것이 아니기 때문에 실제 롤백을 호출하면 안된다. \
   물리 트랜잭션은 외부 트랜잭션을 종료할 때 까지 이어져야한다.
5. 내부 트랜잭션은 물리 트랜잭션을 롤백하지 않는 대신에 \
   트랜잭션 동기화 매니저에 rollbackOnly=true 라는 표시를 해둔다.
<br/>

#### 응답 흐름 - 외부 트랜잭션
4. 로직1이 끝나고 트랜잭션 매니저를 통해 외부 트랜잭션을 커밋한다.
5. 트랜잭션 매니저는 커밋 시점에 신규 트랜잭션 여부에 따라 다르게 동작한다. \
   외부 트랜잭션은 신규 트랜잭션이다. 따라서 DB 커넥션에 실제 커밋을 호출해야 한다.\
   이때 먼저 트랜잭션 동기화 매니저에 롤백 전용( rollbackOnly=true ) 표시가\
   있는지 확인한다. 롤백 전용 표시가 있으면 물리 트랜잭션을 커밋하는 것이 아니라 롤백한다.
7. 실제 데이터베이스에 롤백이 반영되고, 물리 트랜잭션도 끝난다.
8. 트랜잭션 매니저에 커밋을 호출한 개발자 입장에서는 분명 커밋을 기대했는데 \
   롤백 전용 표시로 인해 실제로는 롤백이 되어버렸다.\
   .\
   이것은 조용히 넘어갈 수 있는 문제가 아니다. 시스템 입장에서는 \
   커밋을 호출했지만 롤백이 되었다는 것은 분명하게 알려주어야 한다.\
   예를 들어서 고객은 주문이 성공했다고 생각했는데, \
   실제로는 롤백이 되어서 주문이 생성되지 않은 것이다.
   .\
   스프링은 이 경우 UnexpectedRollbackException 런타임 예외를 던진다. \
   그래서 커밋을 시도했지만, 기대하지 않은 롤백이 발생했다는 것을 명확하게 알려준다
<br/>

### 정리
논리 트랜잭션이 하나라도 롤백되면 물리 트랜잭션은 롤백된다.\
내부 논리 트랜잭션이 롤백되면 롤백 전용 마크를 표시한다.\
외부 트랜잭션을 커밋할 때 롤백 전용 마크를 확인한다. \
롤백 전용 마크가 표시되어 있으면 물리 트랜잭션을 롤백하고,\
UnexpectedRollbackException 예외를 던진다.

> 애플리케이션 개발에서 중요한 기본 원칙은 모호함을 제거하는 것이다. \
> 개발은 명확해야 한다. 이렇게 커밋을 호출했는데, 내부에서 롤백이 발생한 경우 \
> 모호하게 두면 아주 심각한 문제가 발생한다. \
> 이렇게 기대한 결과가 다른 경우 예외를 발생시켜서 명확하게 문제를 알려주는 것이 좋은 설계이다.
<br/>

## 스프링 트랜잭션 전파7 - REQUIRES_NEW
이번에는 외부 트랜잭션과 내부 트랜잭션을 완전히 분리해서 사용하는 방법에 대해서 알아보자.\
외부 트랜잭션과 내부 트랜잭션을 완전히 분리해서 \
각각 별도의 물리 트랜잭션을 사용하는 방법이다.\
그래서 커밋과 롤백도 각각 별도로 이루어지게 된다.\
이 방법은 내부 트랜잭션에 문제가 발생해서 롤백해도, \
외부 트랜잭션에는 영향을 주지 않는다. 반대로 외부 트랜잭션에 문제가 발생해도 \
내부 트랜잭션에 영향을 주지 않는다. 

이 방법을 사용하는 구체적인 예는 이후에 알아보고 지금은 작동 원리를 이해해보자.
<br/>
<br/>

### REQUIRES_NEW
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/bbab7ab2-e83f-434a-8598-0eeb601cc673)

이렇게 물리 트랜잭션을 분리하려면 내부 트랜잭션을 시작할 때 \
REQUIRES_NEW 옵션을 사용하면 된다.

외부 트랜잭션과 내부 트랜잭션이 각각 별도의 물리 트랜잭션을 가진다.\
별도의 물리 트랜잭션을 가진다는 뜻은 DB 커넥션을 따로 사용한다는 뜻이다.\
이 경우 내부 트랜잭션이 롤백되면서 로직 2가 롤백되어도 로직 1에서 \
저장한 데이터에는 영향을 주지 않는다.

최종적으로 로직2는 롤백되고, 로직1은 커밋된다.
<br/>
<br/>

### inner_rollback_requires_new() - BasicTxTest 추가
```java
@Test
void inner_rollback_requires_new() {

   log.info("외부 트랜젝션 시작");
   TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
   log.info("outer.isNewTransaction()={}", outer.isNewTransaction());

   log.info("내부 트랜젝션 시작");
   DefaultTransactionAttribute definition = new DefaultTransactionAttribute();
   definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
   TransactionStatus inner = txManager.getTransaction(definition);
   log.info("inner.isNewTransaction()={}", inner.isNewTransaction());

   log.info("내부 트랜젝션 롤백");
   txManager.rollback(inner);

   log.info("외부 트랜젝션 커밋");
   txManager.commit(outer);
}
```
내부 트랜잭션을 시작할 때 전파 옵션인 propagationBehavior 에 \
PROPAGATION_REQUIRES_NEW 옵션을 주었다.

이 전파 옵션을 사용하면 내부 트랜잭션을 시작할 때 \
기존 트랜잭션에 참여하는 것이 아니라 새로운 물리 트랜잭션을 만들어서 시작하게 된다.
<br/>
<br/>

#### 실행 결과 - inner_rollback_requires_new()
```java
외부 트랜잭션 시작
Creating new transaction with name [null]:
PROPAGATION_REQUIRED,ISOLATION_DEFAULT
Acquired Connection [HikariProxyConnection@1064414847 wrapping conn0] for JDBC transaction
Switching JDBC Connection [HikariProxyConnection@1064414847 wrapping conn0] to manual commit
outer.isNewTransaction()=true

내부 트랜잭션 시작
Suspending current transaction, creating new transaction with name [null]
Acquired Connection [HikariProxyConnection@778350106 wrapping conn1] for JDBC transaction
Switching JDBC Connection [HikariProxyConnection@778350106 wrapping conn1] to manual commit
inner.isNewTransaction()=true
내부 트랜잭션 롤백

Initiating transaction rollback
Rolling back JDBC transaction on Connection [HikariProxyConnection@778350106 wrapping conn1]
Releasing JDBC Connection [HikariProxyConnection@778350106 wrapping conn1] after transaction
Resuming suspended transaction after completion of inner transaction

외부 트랜잭션 커밋
Initiating transaction commit
Committing JDBC transaction on Connection [HikariProxyConnection@1064414847 wrapping conn0]
Releasing JDBC Connection [HikariProxyConnection@1064414847 wrapping conn0] after transaction
```
<br/>
<br/>

#### 외부 트랜잭션 시작
외부 트랜잭션을 시작하면서 conn0 를 획득하고 manual commit 으로 변경해서 \
물리 트랜잭션을 시작한다.

외부 트랜잭션은 신규 트랜잭션이다.( outer.isNewTransaction()=true )
<br/>
<br/>

#### 내부 트랜잭션 시작
내부 트랜잭션을 시작하면서 conn1 를 획득하고 manual commit 으로 \
변경해서 물리 트랜잭션을 시작한다.

내부 트랜잭션은 외부 트랜잭션에 참여하는 것이 아니라, \
PROPAGATION_REQUIRES_NEW 옵션을 사용했기 때문에 \
완전히 새로운 신규 트랜잭션으로 생성된다.( inner.isNewTransaction()=true )
<br/>
<br/>

### 내부 트랜잭션 롤백
내부 트랜잭션을 롤백한다.\
내부 트랜잭션은 신규 트랜잭션이기 때문에 실제 물리 트랜잭션을 롤백한다.\
내부 트랜잭션은 conn1 을 사용하므로 conn1 에 물리 롤백을 수행한다.
<br/>
<br/>

### 외부 트랜잭션 커밋
외부 트랜잭션을 커밋한다.\
외부 트랜잭션은 신규 트랜잭션이기 때문에 실제 물리 트랜잭션을 커밋한다.\
외부 트랜잭션은 conn0 를 사용하므로 conn0 에 물리 커밋을 수행한다.
<br/>
<br/>

### 현재 상황
현재 상황을 그림으로 확인해보자.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/0a3f67e8-c340-4e75-b0dc-635a26a339f1)
<br/>
<br/>

#### 요청 흐름 - REQUIRES_NEW
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/a5243ec3-3a84-4bdb-aeae-2cedc727a9be)
<br/>
<br/>

#### 요청 흐름 - 외부 트랜잭션
1. txManager.getTransaction() 를 호출해서 외부 트랜잭션을 시작한다.
2. 트랜잭션 매니저는 데이터소스를 통해 커넥션을 생성한다.
3. 생성한 커넥션을 수동 커밋 모드( setAutoCommit(false) )로 설정한다. - 물리 트랜잭션 시작
4. 트랜잭션 매니저는 트랜잭션 동기화 매니저에 커넥션을 보관한다.
5. 트랜잭션 매니저는 트랜잭션을 생성한 결과를 TransactionStatus 에 담아서 반환하는데, \
   여기에 신규 트랜잭션의 여부가 담겨 있다. isNewTransaction 를 통해\
   신규 트랜잭션 여부를 확인할 수 있다.\
   트랜잭션을 처음 시작했으므로 신규 트랜잭션이다.( true )
7. 로직1이 사용되고, 커넥션이 필요한 경우 트랜잭션 동기화 매니저를 통해 \
   트랜잭션이 적용된 커넥션을 획득해서 사용한다.
<br/>

#### 요청 흐름 - 내부 트랜잭션
7. REQUIRES_NEW 옵션과 함께 txManager.getTransaction() 를 호출해서 \
   내부 트랜잭션을 시작한다.\
   트랜잭션 매니저는 REQUIRES_NEW 옵션을 확인하고, \
   기존 트랜잭션에 참여하는 것이 아니라 새로운 트랜잭션을 시작한다.
9. 트랜잭션 매니저는 데이터소스를 통해 커넥션을 생성한다.
10. 생성한 커넥션을 수동 커밋 모드( setAutoCommit(false) )로 설정한다. - **물리 트랜잭션 시작**
11. 트랜잭션 매니저는 트랜잭션 동기화 매니저에 커넥션을 보관한다.\
    이때 con1 은 잠시 보류되고, 지금부터는 con2 가 사용된다. \
    (내부 트랜잭션을 완료할 때 까지 con2 가 사용된다.)
13. 트랜잭션 매니저는 신규 트랜잭션의 생성한 결과를 반환한다. isNewTransaction == true
14. 로직2가 사용되고, 커넥션이 필요한 경우 트랜잭션 동기화 매니저에 있는 \
    con2 커넥션을 획득해서 사용한다.
<br/>

### 응답 흐름 - REQUIRES_NEW
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/a7c010c2-7d41-4120-9479-165bed8916b7)
<br/>
<br/>

#### 응답 흐름 - 내부 트랜잭션
1. 로직2가 끝나고 트랜잭션 매니저를 통해 내부 트랜잭션을 롤백한다. \
   (로직2에 문제가 있어서 롤백한다고 가정한다.)
3. 트랜잭션 매니저는 롤백 시점에 신규 트랜잭션 여부에 따라 다르게 동작한다.\
   현재 내부 트랜잭션은 신규 트랜잭션이다. 따라서 실제 롤백을 호출한다.
5. 내부 트랜잭션이 con2 물리 트랜잭션을 롤백한다.\
   트랜잭션이 종료되고, con2 는 종료되거나, 커넥션 풀에 반납된다.\
   이후에 con1 의 보류가 끝나고, 다시 con1 을 사용한다.
<br/>

#### 응답 흐름 - 외부 트랜잭션
4. 외부 트랜잭션에 커밋을 요청한다.
5. 외부 트랜잭션은 신규 트랜잭션이기 때문에 물리 트랜잭션을 커밋한다.
6. 이때 rollbackOnly 설정을 체크한다. rollbackOnly 설정이 없으므로 커밋한다.
7. 본인이 만든 con1 커넥션을 통해 물리 트랜잭션을 커밋한다.\
   트랜잭션이 종료되고, con1 은 종료되거나, 커넥션 풀에 반납된다
<br/>

#### 정리
REQUIRES_NEW 옵션을 사용하면 물리 트랜잭션이 명확하게 분리된다.\
REQUIRES_NEW 를 사용하면 데이터베이스 커넥션이 동시에 2개 사용된다는 점을 주의해야 한다
<br/>
<br/>

## 스프링 트랜잭션 전파8 - 다양한 전파 옵션
스프링은 다양한 트랜잭션 전파 옵션을 제공한다. \
전파 옵션에 별도의 설정을 하지 않으면 REQUIRED 가 기본으로 사용된다.

참고로 실무에서는 대부분 REQUIRED 옵션을 사용한다. \
그리고 아주 가끔 REQUIRES_NEW 을 사용하고,나머지는 거의 사용하지 않는다. \
그래서 나머지 옵션은 이런 것이 있다는 정도로만 알아두고 필요할 때 찾아보자.
<br/>
<br/>

#### REQUIRED
가장 많이 사용하는 기본 설정이다. 기존 트랜잭션이 없으면 생성하고, 있으면 참여한다.\
트랜잭션이 필수라는 의미로 이해하면 된다. \
(필수이기 때문에 없으면 만들고, 있으면 참여한다.)\
기존 트랜잭션 없음: 새로운 트랜잭션을 생성한다.\
기존 트랜잭션 있음: 기존 트랜잭션에 참여한다.
<br/>
<br/>

#### REQUIRES_NEW
항상 새로운 트랜잭션을 생성한다.\
기존 트랜잭션 없음: 새로운 트랜잭션을 생성한다.\
기존 트랜잭션 있음: 새로운 트랜잭션을 생성한다.
<br/>
<br/>

#### SUPPORT
트랜잭션을 지원한다는 뜻이다. 기존 트랜잭션이 없으면, 없는대로 진행하고, 있으면 참여한다.\
기존 트랜잭션 없음: 트랜잭션 없이 진행한다.\
기존 트랜잭션 있음: 기존 트랜잭션에 참여한다.
<br/>
<br/>

#### NOT_SUPPORT
트랜잭션을 지원하지 않는다는 의미이다.\
기존 트랜잭션 없음: 트랜잭션 없이 진행한다.\
기존 트랜잭션 있음: 트랜잭션 없이 진행한다. (기존 트랜잭션은 보류한다)
<br/>
<br/>

#### MANDATORY
의무사항이다. 트랜잭션이 반드시 있어야 한다. \
기존 트랜잭션이 없으면 예외가 발생한다.\
기존 트랜잭션 없음: IllegalTransactionStateException 예외 발생\
기존 트랜잭션 있음: 기존 트랜잭션에 참여한다.
<br/>
<br/>

#### NEVER
트랜잭션을 사용하지 않는다는 의미이다. 기존 트랜잭션이 있으면 예외가 발생한다. \
기존 트랜잭션도 허용하지 않는 강한 부정의 의미로 이해하면 된다.\
기존 트랜잭션 없음: 트랜잭션 없이 진행한다.\
기존 트랜잭션 있음: IllegalTransactionStateException 예외 발생
<br/>
<br/>

#### NESTED
기존 트랜잭션 없음: 새로운 트랜잭션을 생성한다.\
기존 트랜잭션 있음: 중첩 트랜잭션을 만든다.\
중첩 트랜잭션은 외부 트랜잭션의 영향을 받지만, \
중첩 트랜잭션은 외부에 영향을 주지 않는다.
중첩 트랜잭션이 롤백 되어도 외부 트랜잭션은 커밋할 수 있다.\
외부 트랜잭션이 롤백 되면 중첩 트랜잭션도 함께 롤백된다.

참고로, JDBC savepoint 기능을 사용한다.\
DB 드라이버에서 해당 기능을 지원하는지 확인이 필요하다.\
중첩 트랜잭션은 JPA에서는 사용할 수 없다
<br/>
<br/>

#### 트랜잭션 전파와 옵션
isolation , timeout , readOnly 는 트랜잭션이 처음 시작될 때만 적용된다. \
트랜잭션에 참여하는 경우에는 적용되지 않는다.\
예를 들어서 REQUIRED 를 통한 트랜잭션 시작, \
REQUIRES_NEW 를 통한 트랜잭션 시작 시점에만 적용된다
<br/>
<br/>

##  예제 프로젝트 시작
지금까지 배운 트랜잭션 전파에 대한 내용을 실제 예제를 통해서 이해해보자.
<br/>
<br/>

### 비즈니스 요구사항
* 회원을 등록하고 조회한다.
* 회원에 대한 변경 이력을 추적할 수 있도록 회원 데이터가 변경될 때 \
  변경 이력을 DB LOG 테이블에 남겨야 한다.
  * 여기서는 예제를 단순화 하기 위해 회원 등록시에만 DB LOG 테이블에 남긴다.
<br/>

### Member
```java
@Entity
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue
    private Long id;
    private String username;

    public Member(String username) {
        this.username = username;
    }

    public Member() {
    }
}
```
JPA를 통해 관리하는 회원 엔티티이다.
<br/>
<br/>

### MemberRepository
```java
@Slf4j
@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    @Transactional
    public void save(Member member) {
        log.info("member 저장");
        em.persist(member);
    }

    public Optional<Member> find(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList().stream().findAny();
    }
}

```
JPA를 사용하는 회원 리포지토리이다. 저장과 조회 기능을 제공한다.
<br/>
<br/>

### Log
```java
@Entity
@Getter
@Setter
public class Log {

    @Id
    @GeneratedValue
    private Long id;
    private String message;

    public Log() {
    }

    public Log(String message) {
        this.message = message;
    }
}

```
JPA를 통해 관리하는 로그 엔티티이다.
<br/>
<br/>

### LogRepository
```java
@Repository
@Slf4j
@RequiredArgsConstructor
public class LogRepository {

    private final EntityManager em;

    @Transactional
    public void save(Log logMessage) {
        log.info("로그 저장");
        em.persist(logMessage);

        if (logMessage.getMessage().contains("로그예외")) {
            log.info("log 저장시 예외 발생");
            throw new RuntimeException("예외 발생");
        }
    }


    public Optional<Log> find(String message) {
        return em.createQuery("select l from Log l where l.message = :message", Log.class)
                .setParameter("message", message)
                .getResultList().stream().findAny();
    }
}
```
JPA를 사용하는 로그 리포지토리이다. 저장과 조회 기능을 제공한다.\
중간에 예외 상황을 재현하기 위해 `로그예외` 라고 입력하는 경우 예외를 발생시킨다.
<br/>
<br/>

### MemberService
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final LogRepository logRepository;

    // 트랜젝션을 각각 사용하는 예제
    public void joinV1(String username) {
        Member member = new Member(username);
        Log logMessage = new Log(username);

        log.info("== memberRepository 호출 시작 ==");
        memberRepository.save(member);
        log.info("== memberRepository 호출 종료 ==");

        log.info("== logRepository 호출 시작 ==");
        logRepository.save(logMessage);
        log.info("== logRepository 호출 종료 ==");
    }


    public void joinV2(String username) {
        Member member = new Member(username);
        Log logMessage = new Log(username);

        log.info("== memberRepository 호출 시작 ==");
        memberRepository.save(member);
        log.info("== memberRepository 호출 종료 ==");

        log.info("== logRepository 호출 시작 ==");
        try {
            logRepository.save(logMessage);
        } catch (RuntimeException e) {
            log.info("log 저장에 실패했습니다. logMessage={}", logMessage);
            log.info("정상 흐름 반환");
        }
        log.info("== logRepository 호출 종료 ==");
    }
}
```
회원을 등록하면서 동시에 회원 등록에 대한 DB 로그도 함께 남긴다.
* `joinV1()`
  * 회원과 DB로그를 함께 남기는 비즈니스 로직이다.
  * 현재 별도의 트랜잭션은 설정하지 않는다.
* `joinV2()`
  * joinV1() 과 같은 기능을 수행한다.
  * DB로그 저장시 예외가 발생하면 예외를 복구한다.
  * 현재 별도의 트랜잭션은 설정하지 않는다.
<br/>

### MemberServiceTest
```java
@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;

    /**
     * MemberService @Transactional:OFF
     * MemberRepository @Transactional:ON
     * LogRepository @Transactional:ON
     */
    @Test
    void outerTxOff_success() {
        // given
        String username = "outerTxOff_success";

        // when
        memberService.joinV1(username);

        // then
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }
}
```
정상 동작하는지 테스트 코드를 만들어서 수행해보자.\
우선 테스트가 정상 실행되는 것만 확인하자.
<br/>
<br/>

### 참고
JPA의 구현체인 하이버네이트가 테이블을 자동으로 생성해준다.\
메모리 DB이기 때문에 모든 테스트가 완료된 이후에 DB는 사라진다.\
여기서는 각각의 테스트가 완료된 시점에 데이터를 삭제하지 않는다. \
따라서 username 은 테스트별로 각각 다르게 설정해야 한다. \
그렇지 않으면 다음 테스트에 영향을 준다. \
(모든 테스트가 완료되어야 DB가 사라진다.)
<br/>
<br/>

### JPA와 데이터 변경
JPA를 통한 모든 데이터 변경(등록, 수정, 삭제)에는 트랜잭션이 필요하다.\
(조회는 트랜잭션 없이 가능하다.)\
현재 코드에서 서비스 계층에 트랜잭션이 없기 때문에 리포지토리에 트랜잭션이 있다.
<br/>
<br/>

## 트랜잭션 전파 활용2 - 커밋, 롤백
### 서비스 계층에 트랜잭션이 없을 때 - 커밋
예제를 통해 서비스 계층에 트랜잭션이 없을 때 \
트랜잭션이 각각 어떻게 작동하는지 확인해보자.

* 상황
  * 서비스 계층에 트랜잭션이 없다.
  * 회원, 로그 리포지토리가 각각 트랜잭션을 가지고 있다.
  * 회원, 로그 리포지토리 둘다 커밋에 성공한다.
<br/>

### outerTxOff_success
```java
/**
 * MemberService @Transactional:OFF
 * MemberRepository @Transactional:ON
 * LogRepository @Transactional:ON
 */
@Test
void outerTxOff_success() {
    // given
    String username = "outerTxOff_success";

    // when
    memberService.joinV1(username);

    // then
    assertTrue(memberRepository.find(username).isPresent());
    assertTrue(logRepository.find(username).isPresent());
}
```
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/2b92e37b-0fc4-4583-8a76-fee890311ef7)

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/6b1b6635-664f-496d-b6f7-80caf2998295)

#### 1. MemberService 에서 MemberRepository 를 호출한다. 
MemberRepository 에는 @Transactional 애노테이션이 있으므로 트랜잭션 AOP가 작동한다. \
여기서 트랜잭션 매니저를 통해 트랜잭션을 시작한다. \
이렇게 시작한 트랜잭션을 트랜잭션B라 하자.

그림에서는 생략했지만, 트랜잭션 매니저에 트랜잭션을 요청하면 데이터소스를 통해 \
커넥션 con1 을 획득하고, 해당 커넥션을 수동 커밋 모드로 변경해서 트랜잭션을 시작한다.

그리고 트랜잭션 동기화 매니저를 통해 트랜잭션을 시작한 커넥션을 보관한다.\
트랜잭션 매니저의 호출 결과로 status 를 반환한다. \
여기서는 신규 트랜잭션 여부가 참이 된다.

#### 2. MemberRepository 는 JPA를 통해 회원을 저장하는데, 
이때 JPA는 트랜잭션이 시작된 con1 을 사용해서 회원을 저장한다.

#### 3. MemberRepository 가 정상 응답을 반환했기 때문에 
트랜잭션 AOP는 트랜잭션 매니저에 커밋을 요청한다.

#### 4. 트랜잭션 매니저는 con1 을 통해 물리 트랜잭션을 커밋한다.
물론 이 시점에 앞서 설명한 신규 트랜잭션 여부, \
rollbackOnly 여부를 모두 체크한다. \
이렇게 해서 MemberRepository 와 관련된 모든 데이터는 정상 커밋되고, \
트랜잭션B는 완전히 종료된다.

이후에 LogRepository 를 통해 트랜잭션C를 시작하고, 정상 커밋한다.\
결과적으로 둘다 커밋되었으므로 Member , Log 모두 안전하게 저장된다.
<br/>
<br/>

#### @Transactional과 REQUIRED
* 트랜잭션 전파의 기본 값은 REQUIRED 이다. 따라서 다음 둘은 같다.
  * @Transactional(propagation = Propagation.REQUIRED)
  * @Transactional
* REQUIRED 는 기존 트랜잭션이 없으면 새로운 트랜잭션을 만들고, 기존 트랜잭션이 있으면 참여한다.
<br/>
<br/>

### 서비스 계층에 트랜잭션이 없을 때 - 롤백
* 상황
  * 서비스 계층에 트랜잭션이 없다.
  * 회원, 로그 리포지토리가 각각 트랜잭션을 가지고 있다.
  * 회원 리포지토리는 정상 동작하지만 로그 리포지토리에서 예외가 발생한다.
<br/>

### outerTxOff_fail
```java
/**
 * MemberService @Transactional:OFF
 * MemberRepository @Transactional:ON
 * LogRepository @Transactional:ON, Exception
 */
@Test
void outerTxOff_fail() {
    // given
    String username = "로그예외_outerTxOff_fail";

    // when
    Assertions.assertThatThrownBy(() -> memberService.joinV1(username))
            .isInstanceOf(RuntimeException.class);

    // then
    assertTrue(memberRepository.find(username).isPresent());
    assertTrue(logRepository.find(username).isEmpty());
}
```
사용자 이름에 로그예외 라는 단어가 포함되어 있으면 \
LogRepository 에서 런타임 예외가 발생한다.\
트랜잭션 AOP는 해당 런타임 예외를 확인하고 롤백 처리한다.
<br/>
<br/>

### 로그예외 로직
```java
if (logMessage.getMessage().contains("로그예외")) {
    log.info("log 저장시 예외 발생");
    throw new RuntimeException("예외 발생");
}
```
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/ff4887b5-0651-46c7-80a6-d6785ad58cca)

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/670de288-acae-4634-aec6-da12720415d6)

MemberService 에서 MemberRepository 를 호출하는 부분은 앞서 설명한 내용과 같다. \
트랜잭션이 정상 커밋되고, 회원 데이터도 DB에 정상 반영된다.

MemberService 에서 LogRepository 를 호출하는데, 로그예외 라는 이름을 전달한다. \
이 과정에서 새로운 트랜잭션 C가 만들어진다.
<br/>
<br/>

#### LogRepository 응답 로직
1. LogRepository 는 트랜잭션C와 관련된 con2 를 사용한다.
2. 로그예외 라는 이름을 전달해서 LogRepository 에 런타임 예외가 발생한다.
3. LogRepository 는 해당 예외를 밖으로 던진다. 이 경우 트랜잭션 AOP가 예외를 받게된다.
4. 런타임 예외가 발생해서 트랜잭션 AOP는 트랜잭션 매니저에 롤백을 호출한다.
5. 트랜잭션 매니저는 신규 트랜잭션이므로 물리 롤백을 호출한다.
<br/>

### 참고
트랜잭션 AOP도 결국 내부에서는 트랜잭션 매니저를 사용하게 된다.\
이 경우 회원은 저장되지만, 회원 이력 로그는 롤백된다. \
따라서 데이터 정합성에 문제가 발생할 수 있다.\
둘을 하나의 트랜잭션으로 묶어서 처리해보자.
<br/>
<br/>

## 트랜잭션 전파 활용3 - 단일 트랜잭션
### 트랜잭션 하나만 사용하기
회원 리포지토리와 로그 리포지토리를 하나의 트랜잭션으로 묶는 가장 간단한 방법은\
이 둘을 호출하는 회원 서비스에만 트랜잭션을 사용하는 것이다.
<br/>
<br/>

### singleTx
```java
/**
 * MemberService @Transactional:ON
 * MemberRepository @Transactional:OFF
 * LogRepository @Transactional:OFF
 */
@Test
void singleTx() {
    // given
    String username = "outerTxOff_success";

    // when
    memberService.joinV1(username);

    // then
    assertTrue(memberRepository.find(username).isPresent());
    assertTrue(logRepository.find(username).isPresent());
}
```
클래스 상위의 주석을 참고하자. 어디에 트랜잭션을 걸고 빼야 하는지 나와있다
<br/>
<br/>

#### MemberService - joinV1()
```java
@Transactional //추가
public void joinV1(String username)
```
<br/>
<br/>

#### MemberRepository - save
```java
//@Transactional //제거
public void save(Member member)
```
<br/>
<br/>

#### LogRepository - save()
```java
//@Transactional //제거
public void save(Log logMessage)
```
MemberRepository , LogRepository 의 @Transactional 코드를 제거하자.
그리고 MemberService 에만 @Transactional 코드를 추가하자.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/23538d42-5eda-4112-9bb9-974fd5f2e1db)

* 이렇게 하면 MemberService 를 시작할 때 부터 종료할 때 까지의 모든 로직을 \
  하나의 트랜잭션으로 묶을수 있다.
  * 물론 MemberService 가 MemberRepository , LogRepository 를 호출하므로\
    이 로직들은 같은 트랜잭션을 사용한다.
* MemberService 만 트랜잭션을 처리하기 때문에 앞서 배운 논리 트랜잭션, \
  물리 트랜잭션, 외부 트랜잭션, 내부 트랜잭션, rollbackOnly , \
  신규 트랜잭션, 트랜잭션 전파와 같은 복잡한 것을 고민할 필요가 없다.\
  아주 단순하고 깔끔하게 트랜잭션을 묶을 수 있다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/cb1f03af-7b2d-4201-b944-30e17f65f26f)

* @Transactional 이 MemberService 에만 붙어있기 때문에 여기에만 트랜잭션 AOP가 적용된다.
  * MemberRepository , LogRepository 는 트랜잭션 AOP가 적용되지 않는다.
* MemberService 의 시작부터 끝까지, 관련 로직은 해당 트랜잭션이 생성한 커넥션을 사용하게 된다.
  * MemberService 가 호출하는 MemberRepository , LogRepository 도 \
    같은 커넥션을 사용하면서 자연스럽게 트랜잭션 범위에 포함된다.

> 같은 쓰레드를 사용하면 트랜잭션 동기화 매니저는 같은 커넥션을 반환한다.
<br/>
<br/>

### 각각 트랜잭션이 필요한 상황
하지만 다음과 같이 각각 트랜잭션이 필요하면 어떻게 해야할까?

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/d6e1f3f6-65b8-4828-86f4-f5352e60f189)
<br/>
<br/>

#### 트랜잭션 적용 범위
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/cf00ec35-370b-415f-9fbe-1e98fd0869ef)
* 클라이언트 A는 MemberService 부터 MemberRepository , LogRepository 를 \
  모두 하나의 트랜잭션으로 묶고 싶다.
* 클라이언트 B는 MemberRepository 만 호출하고 여기에만 트랜잭션을 사용하고 싶다.
* 클라이언트 C는 LogRepository 만 호출하고 여기에만 트랜잭션을 사용하고 싶다.


* 클라이언트 A만 생각하면 MemberService 에 트랜잭션 코드를 남기고, \
  MemberRepository , LogRepository 의 트랜잭션 코드를 제거하면 \
  앞서 배운 것 처럼 깔끔하게 하나의 트랜잭션을 적용할 수 있다.
* 하지만 이렇게 되면 클라이언트 B, C가 호출하는 MemberRepository , \
  LogRepository 에는 트랜잭션을 적용할 수 없다.


트랜잭션 전파 없이 이런 문제를 해결하려면 아마도 트랜잭션이 있는 메서드와 \
트랜잭션이 없는 메서드를 각각 만들어야 할 것이다.

더 복잡하게 다음과 같은 상황이 발생할 수도 있다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/915a8565-fdd4-429f-9d34-1992fc735cce)

클라이언트 Z가 호출하는 OrderService 에서도 트랜잭션을 시작할 수 있어야 하고, \
클라이언트A가 호출하는 MemberService 에서도 트랜잭션을 시작할 수 있어야 한다.

**이런 문제를 해결하기 위해 트랜잭션 전파가 필요한 것이다.**
<br/>
<br/>

## 트랜잭션 전파 활용4 - 전파 커밋
스프링은 @Transactional 이 적용되어 있으면 기본으로 REQUIRED 라는 전파 옵션을 사용한다.\
이 옵션은 기존 트랜잭션이 없으면 트랜잭션을 생성하고, \
기존 트랜잭션이 있으면 기존 트랜잭션에 참여한다. \
참여한다는 뜻은 해당 트랜잭션을 그대로 따른다는 뜻이고, \
동시에 같은 동기화 커넥션을 사용한다는 뜻이다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/7a5f15ca-5960-4f9d-9707-3700f111a1cc)

이렇게 둘 이상의 트랜잭션이 하나의 물리 트랜잭션에 묶이게 되면 \
둘을 구분하기 위해 논리 트랜잭션과 물리 트랜잭션으로 구분한다. 
<br/>
<br/>

#### 신규 트랜잭션
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/29ed9b7e-b69f-4b20-97aa-9be0eb13da15)

이 경우 외부에 있는 신규 트랜잭션만 실제 물리 트랜잭션을 시작하고 커밋한다.\
내부에 있는 트랜잭션은 물리 트랜잭션 시작하거나 커밋하지 않는다
<br/>
<br/>

#### 모든 논리 트랜잭션 커밋
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/7dd3eca8-de02-436a-9758-c8267a622e33)

모든 논리 트랜잭션을 커밋해야 물리 트랜잭션도 커밋된다. \
하나라도 롤백되면 물리 트랜잭션은 롤백된다.\
먼저 모든 논리 트랜잭션이 정상 커밋되는 경우를 코드로 확인해보자.
<br/>
<br/>

### outerTxOn_success
```java
/**
 * MemberService @Transactional:ON
 * MemberRepository @Transactional:ON
 * LogRepository @Transactional:ON
 */
@Test
void outerTxOn_success() {
    // given
    String username = "outerTxOn_success";

    // when
    memberService.joinV1(username);

    // then
    assertTrue(memberRepository.find(username).isPresent());
    assertTrue(logRepository.find(username).isPresent());
}
```
클래스 위의 주석을 확인해서 모든 곳에 트랜잭션을 적용하자.
* `MemberService @Transactional:ON`
* `MemberRepository @Transactional:ON`
* `LogRepository @Transactional:ON`

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/f3e72f1a-6c45-425d-ae1b-6e1451f2213c)

* 클라이언트A(여기서는 테스트 코드)가 MemberService 를 호출하면서 트랜잭션 AOP가 호출된다.
  * 여기서 신규 트랜잭션이 생성되고, 물리 트랜잭션도 시작한다.
* MemberRepository 를 호출하면서 트랜잭션 AOP가 호출된다.
  * 이미 트랜잭션이 있으므로 기존 트랜잭션에 참여한다.
* MemberRepository 의 로직 호출이 끝나고 정상 응답하면 트랜잭션 AOP가 호출된다.
  * 트랜잭션 AOP는 정상 응답이므로 트랜잭션 매니저에 커밋을 요청한다. \
    이 경우 신규 트랜잭션이 아니므로 실제 커밋을 호출하지 않는다.
* LogRepository 를 호출하면서 트랜잭션 AOP가 호출된다.
  * 이미 트랜잭션이 있으므로 기존 트랜잭션에 참여한다.
* LogRepository 의 로직 호출이 끝나고 정상 응답하면 트랜잭션 AOP가 호출된다.
  * 트랜잭션 AOP는 정상 응답이므로 트랜잭션 매니저에 커밋을 요청한다. \
    이 경우 신규 트랜잭션이 아니므로 실제 커밋(물리 커밋)을 호출하지 않는다.
* MemberService 의 로직 호출이 끝나고 정상 응답하면 트랜잭션 AOP가 호출된다.
  * 트랜잭션 AOP는 정상 응답이므로 트랜잭션 매니저에 커밋을 요청한다.\
    이 경우 신규 트랜잭션이므로 물리 커밋을 호출한다.
<br/>

## 트랜잭션 전파 활용5 - 전파 롤백
이번에는 로그 리포지토리에서 예외가 발생해서 전체 트랜잭션이 롤백되는 경우를 알아보자.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/6ebb8fc8-8814-4a95-9607-54ce18d3cdd0)
<br/>
<br/>

### outerTxOn_fail
```java
/**
 * MemberService @Transactional:ON
 * MemberRepository @Transactional:ON
 * LogRepository @Transactional:ON
 */
@Test
void outerTxOn_fail() {
    // given
    String username = "로그예외_outerTxOn_fail";

    // when
    Assertions.assertThatThrownBy(() -> memberService.joinV1(username))
            .isInstanceOf(RuntimeException.class);

    // then: 모든 데이터가 롤백된다
    assertTrue(memberRepository.find(username).isEmpty());
    assertTrue(logRepository.find(username).isEmpty());
}
```
클래스 위의 주석을 확인해서 모든 곳에 트랜잭션을 적용하자.
* `MemberService @Transactional:ON`
* `MemberRepository @Transactional:ON`
* `LogRepository @Transactional:ON`
여기서는 로그예외 라고 넘겼기 때문에 LogRepository 에서 런타임 예외가 발생한다.
<br/>
<br/>

### 흐름
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/2da27230-fc34-4add-929f-db0edf1fad99)

* 클라이언트A가 MemberService 를 호출하면서 트랜잭션 AOP가 호출된다.
  * 여기서 신규 트랜잭션이 생성되고, 물리 트랜잭션도 시작한다.
* MemberRepository 를 호출하면서 트랜잭션 AOP가 호출된다.
  * 이미 트랜잭션이 있으므로 기존 트랜잭션에 참여한다.
* MemberRepository 의 로직 호출이 끝나고 정상 응답하면 트랜잭션 AOP가 호출된다.
  * 트랜잭션 AOP는 정상 응답이므로 트랜잭션 매니저에 커밋을 요청한다.\
    이 경우 신규 트랜잭션이 아니므로 실제 커밋을 호출하지 않는다.
* LogRepository 를 호출하면서 트랜잭션 AOP가 호출된다.
  * 이미 트랜잭션이 있으므로 기존 트랜잭션에 참여한다.
* LogRepository 로직에서 런타임 예외가 발생한다. \
  예외를 던지면 트랜잭션 AOP가 해당 예외를 받게 된다.
  * 트랜잭션 AOP는 런타임 예외가 발생했으므로 트랜잭션 매니저에 롤백을 요청한다.\
    이 경우 신규 트랜잭션이 아니므로 물리 롤백을 호출하지는 않는다. \
    대신에 rollbackOnly 를 설정한다.
  * LogRepository 가 예외를 던졌기 때문에 트랜잭션 AOP도 해당 예외를 그대로 밖으로 던진다.
* MemberService 에서도 런타임 예외를 받게 되는데, 여기 로직에서는 \
  해당 런타임 예외를 처리하지 않고 밖으로 던진다.
  * 트랜잭션 AOP는 런타임 예외가 발생했으므로 트랜잭션 매니저에 롤백을 요청한다. \
    이 경우 신규 트랜잭션이므로 물리 롤백을 호출한다.
  * 참고로 이 경우 어차피 롤백이 되었기 때문에, rollbackOnly 설정은 참고하지 않는다.
  * MemberService 가 예외를 던졌기 때문에 트랜잭션 AOP도 해당 예외를 그대로 밖으로 던진다.
* 클라이언트A는 LogRepository 부터 넘어온 런타임 예외를 받게 된다.
<br/>

### 정리
회원과 회원 이력 로그를 처리하는 부분을 하나의 트랜잭션으로 묶은 덕분에 \
문제가 발생했을 때 회원과 회원 이력 로그가 모두 함께 롤백된다. \
따라서 데이터 정합성에 문제가 발생하지 않는다.
<br/>
<br/>

## 트랜잭션 전파 활용6 - 복구 REQUIRED
앞서 회원과 로그를 하나의 트랜잭션으로 묶어서 데이터 정합성 문제를 깔끔하게 해결했다. \
그런데 회원 이력 로그를 DB에 남기는 작업에 가끔 문제가 발생해서 \
회원 가입 자체가 안되는 경우가 가끔 발생하게 되었다. \
그래서 사용자들이 회원 가입에 실패해서 이탈하는 문제가 발생하기 시작했다.

회원 이력 로그의 경우 여러가지 방법으로 추후에 복구가 가능할 것으로 보인다.\
그래서 비즈니스 요구사항이 변경되었다.\

**회원 가입을 시도한 로그를 남기는데 실패하더라도 회원 가입은 유지되어야 한다.**

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/fc98c0e8-e506-4f77-b581-306f099ba25f)

단순하게 생각해보면 LogRepository 에서 예외가 발생하면 \
그것을 MemberService 에서 예외를 잡아서 처리하면 될 것 같다.

이렇게 하면 MemberService 에서 정상 흐름으로 바꿀 수 있기 때문에 \
MemberService 의 트랜잭션 AOP 에서 커밋을 수행할 수 있다.

맞다. 뭔가 이상하게 느껴질 것이다. 이 방법이 실패할 것으로 생각했다면, \
지금까지 제대로 학습한 것이다. 이 방법이 왜 실패하는지 예제를 통해서 알아보자. \
참고로 실무에서 많은 개발자가 이 방법을 사용해서 실패한다.
<br/>
<br/>

### recoverException_fail
```java
/**
 * MemberService @Transactional:ON
 * MemberRepository @Transactional:ON
 * LogRepository @Transactional:ON Exception
 */
@Test
void recoverException_fail() {
    // given
    String username = "로그예외_recoverException_fail";

    // when
    Assertions.assertThatThrownBy(() -> memberService.joinV2(username))
            .isInstanceOf(UnexpectedRollbackException.class);

    // then: 모든 데이터가 롤백된다
    assertTrue(memberRepository.find(username).isEmpty());
    assertTrue(logRepository.find(username).isEmpty());
}
```
**모든 트랜잭션을 켜자**
* `MemberService @Transactional:ON`
* `MemberRepository @Transactional:ON`
* `LogRepository @Transactional:ON`

**여기서 memberService.joinV2()를 호출하는 부분을 주의해야 한다.** \
**joinV2()에는 예외를 잡아서 정상 흐름으로 변환하는 로직이 추가되어 있다.**

```java
try {
    logRepository.save(logMessage);
} catch (RuntimeException e) {
    log.info("log 저장에 실패했습니다. logMessage={}", logMessage);
    log.info("정상 흐름 변환");
}
```

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/084271d3-95bf-4d3b-ac56-d25a7fc191e7)

* 내부 트랜잭션에서 rollbackOnly 를 설정하기 때문에 결과적으로 \
  정상 흐름 처리를 해서 외부 트랜잭션에서 커밋을 호출해도 물리 트랜잭션은 롤백된다.
* 그리고 UnexpectedRollbackException 이 던져진다.

전체 흐름을 좀 더 자세히 알아보자

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/52613da6-93f4-48c2-a9e9-50cff55e682e)

* LogRepository 에서 예외가 발생한다. \
  예외를 던지면 LogRepository 의 트랜잭션 AOP가 해당 예외를 받는다.
* 신규 트랜잭션이 아니므로 물리 트랜잭션을 롤백하지는 않고,\
  트랜잭션 동기화 매니저에 rollbackOnly 를 표시한다.
* 이후 트랜잭션 AOP는 전달 받은 예외를 밖으로 던진다.
* 예외가 MemberService 에 던져지고, MemberService 는 해당 예외를 복구한다. \
  그리고 정상적으로 리턴한다.
* 정상 흐름이 되었으므로 MemberService 의 트랜잭션 AOP는 커밋을 호출한다.
* 커밋을 호출할 때 신규 트랜잭션이므로 실제 물리 트랜잭션을 커밋해야 한다. \
  이때 rollbackOnly 를 체크한다.
* rollbackOnly 가 체크 되어 있으므로 물리 트랜잭션을 롤백한다.
* 트랜잭션 매니저는 UnexpectedRollbackException 예외를 던진다.
* 트랜잭션 AOP도 전달받은 UnexpectedRollbackException 을 클라이언트에 던진다.
<br/>

### 정리
논리 트랜잭션 중 하나라도 롤백되면 전체 트랜잭션은 롤백된다.

내부 트랜잭션이 롤백 되었는데, 외부 트랜잭션이 커밋되면 \
UnexpectedRollbackException 예외가 발생한다.

rollbackOnly 상황에서 커밋이 발생하면 UnexpectedRollbackException 예외가 발생한다.

그렇다면 어떻게 해야 다음 요구사항을 만족할 수 있을까?

**회원 가입을 시도한 로그를 남기는데 실패하더라도 회원 가입은 유지되어야 한다.**
<br/>
<br/>


## 트랜잭션 전파 활용7 - 복구 REQUIRES_NEW
회원 가입을 시도한 로그를 남기는데 실패하더라도 회원 가입은 유지되어야 한다.\
이 요구사항을 만족하기 위해서 로그와 관련된 물리 트랜잭션을 별도로 분리해보자. \
바로 REQUIRES_NEW 를 사용하는 것이다
<br/>
<br/>

### recoverException_success
```java
/**
 * MemberService @Transactional:ON
 * MemberRepository @Transactional:ON
 * LogRepository @Transactional:ON(REQURES_NEW Exception
 */
@Test
void recoverException_success() {
    // given
    String username = "로그예외_recoverException_success";

    // when
    memberService.joinV2(username);

    // then: member 저장, log 롤백
    assertTrue(memberRepository.find(username).isPresent());
    assertTrue(logRepository.find(username).isEmpty());
}
```
* `MemberService @Transactional:ON`
* `MemberRepository @Transactional:ON`
* `LogRepository @Transactional(REQUIRES_NEW)`
<br/>
<br/>

### LogRepository - save()
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void save(Log logMessage)
```
이렇게 해서 기존 트랜잭션에 참여하는 REQUIRED 대신에, \
항상 신규 트랜잭션을 생성하는 REQUIRES_NEW 를 적용하자.

**예외를 복구하는 memberService.joinV2()를 사용한다는 점도 주의하자**
<br/>
<br/>

### REQUIRES_NEW - 물리 트랜잭션 분리
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/f91a6fed-ba1f-49c0-aba2-50f9c6ab6091)

* MemberRepository 는 REQUIRED 옵션을 사용한다. 따라서 기존 트랜잭션에 참여한다.
* LogRepository 의 트랜잭션 옵션에 REQUIRES_NEW 를 사용했다.
* REQUIRES_NEW 는 항상 새로운 트랜잭션을 만든다. \
  따라서 해당 트랜잭션 안에서는 DB 커넥션도 별도로 사용하게 된다.
<br/>

### REQUIRES_NEW - 복구
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/07dc2b34-6158-49a6-80b3-c9923e665488)

* REQUIRES_NEW 를 사용하게 되면 물리 트랜잭션 자체가 완전히 분리되어 버린다.
* 그리고 REQUIRES_NEW 는 신규 트랜잭션이므로 rollbackOnly 표시가 되지 않는다. \
  그냥 해당 트랜잭션이 물리 롤백되고 끝난다.
<br/>

### REQUIRES_NEW - 자세히
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/91a25e23-313e-483c-88a7-f91d178fbaf3)

* LogRepository 에서 예외가 발생한다. \
  예외를 던지면 LogRepository 의 트랜잭션 AOP가 해당 예외를 받는다.
* REQUIRES_NEW 를 사용한 신규 트랜잭션이므로 물리 트랜잭션을 롤백한다. \
  물리 트랜잭션을 롤백했으므로 rollbackOnly 를 표시하지 않는다. \
  여기서 REQUIRES_NEW 를 사용한 물리 트랜잭션은 롤백되고 완전히 끝이 나버린다.
* 이후 트랜잭션 AOP는 전달 받은 예외를 밖으로 던진다.
* 예외가 MemberService 에 던져지고, MemberService 는 해당 예외를 복구한다. \
  그리고 정상적으로 리턴한다.
* 정상 흐름이 되었으므로 MemberService 의 트랜잭션 AOP는 커밋을 호출한다.
* 커밋을 호출할 때 신규 트랜잭션이므로 실제 물리 트랜잭션을 커밋해야 한다. \
  이때 rollbackOnly 를 체크한다.
* rollbackOnly 가 없으므로 물리 트랜잭션을 커밋한다.
* 이후 정상 흐름이 반환된다.

**결과적으로 회원 데이터는 저장되고, 로그 데이터만 롤백 되는 것을 확인할 수 있다.**
<br/>
<br/>

### 정리
논리 트랜잭션은 하나라도 롤백되면 관련된 물리 트랜잭션은 롤백되어 버린다.\
이 문제를 해결하려면 REQUIRES_NEW 를 사용해서 트랜잭션을 분리해야 한다.

참고로 예제를 단순화 하기 위해 MemberService 가 MemberRepository , \
LogRepository 만 호출하지만 실제로는 더 많은 리포지토리들을 호출하고 \
그 중에 LogRepository 만 트랜잭션을 분리한다고 생각해보면 \
이해하는데 도움이 될 것이다.
<br/>
<br/>

### 주의
REQUIRES_NEW 를 사용하면 하나의 HTTP 요청에 동시에 \
2개의 데이터베이스 커넥션을 사용하게 된다.\
따라서 성능이 중요한 곳에서는 이런 부분을 주의해서 사용해야 한다.\
REQUIRES_NEW 를 사용하지 않고 문제를 해결할 수 있는 단순한 방법이 있다면, \
그 방법을 선택하는 것이 더 좋다.

예를 들면 다음과 같이 REQUIRES_NEW 를 사용하지 않고 구조를 변경하는 것이다.\
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/28ec997c-9e26-439f-9047-209eed5dbb62)

이렇게 하면 HTTP 요청에 동시에 2개의 커넥션을 사용하지는 않는다. \
순차적으로 사용하고 반환하게 된다.

물론 구조상 REQUIRES_NEW 를 사용하는 것이 더 깔끔한 경우도 있으므로 \
각각의 장단점을 이해하고 적절하게 선택해서 사용하면 된다.