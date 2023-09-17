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

## 프로젝트 생성
### 프로젝트 설정
#### 라이브러리 추가 - pom.xml
```java
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
		 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

	<modelVersion>4.0.0</modelVersion>
	<groupId>jpa-basic</groupId>
	<artifactId>ex1-hello-jpa</artifactId>
	<version>1.0.0</version>

	<dependencies>
		<!-- JPA 하이버네이트 -->
		<dependency>
			<groupId>org.hibernate</groupId>
			<artifactId>hibernate-entitymanager</artifactId>
			<version>5.3.10.Final</version>
		</dependency>

		<!-- H2 데이터베이스 -->
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<version>1.4.200</version>
		</dependency>
	</dependencies>
</project>
```
<br/>

#### JPA 설정하기 - persistence.xml
* JPA 설정 파일
* /META-INF/persistence.xml 위치
* persistence-unit name으로 이름 지정
* javax.persistence로 시작: JPA 표준 속성
* hibernate로 시작: 하이버네이트 전용 속성

```java
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.2"
             xmlns="http://xmlns.jcp.org/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd">
    <persistence-unit name="hello">
        <properties>
            <!-- 필수 속성 -->
            <property name="javax.persistence.jdbc.driver" value="org.h2.Driver"/>
            <property name="javax.persistence.jdbc.user" value="sa"/>
            <property name="javax.persistence.jdbc.password" value=""/>
            <property name="javax.persistence.jdbc.url" value="jdbc:h2:tcp://localhost/~/test"/>
            <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>

            <!-- 옵션 -->
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
            <property name="hibernate.use_sql_comments" value="true"/>
            <!--<property name="hibernate.hbm2ddl.auto" value="create" />-->
        </properties>
    </persistence-unit>
</persistence>
```
<br/>

### 데이터베이스 방언
* JPA는 특정 데이터베이스에 종속 X
* 각각의 데이터베이스가 제공하는 SQL 문법과 함수는 조금씩 다름
    * 가변 문자: MySQL은 VARCHAR, Oracle은 VARCHAR2
    * 문자열을 자르는 함수: SQL 표준은 SUBSTRING(), Oracle은 SUBSTR()
    * 페이징: MySQL은 LIMIT , Oracle은 ROWNUM
* 방언: SQL 표준을 지키지 않는 특정 데이터베이스만의 고유한 기능

![image](https://github.com/jub3907/outSourcing/assets/58246682/dd44397f-f1b0-40e7-8baa-fa3cd4ea8e89)

* hibernate.dialect 속성에 지정
    * H2 : org.hibernate.dialect.H2Dialect
    * Oracle 10g : org.hibernate.dialect.Oracle10gDialect
    * MySQL : org.hibernate.dialect.MySQL5InnoDBDialect
* 하이버네이트는 40가지 이상의 데이터베이스 방언 지원
<br/>
<br/>
