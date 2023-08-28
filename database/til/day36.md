## DB
### DB
전자적(Electronically)으로 저장되고 사용되는 \
관련있는(related) 데이터들의 조직화된 집합(organized collection)을 의미한다.

* 관련 있는 데이터
    * 같은 출처, 같은 목적, 같은 서비스 안에서 생성되는 데이터 들을 의미한다.
* 조직화 된 집합
    * 책이 중구 난방으로 쌓여있게 된다면, 책을 찾기가 어려울 것이다.
    * 이처럼, 조직화된 데이터들은 내가 찾으려는 데이터를 \
    좀 더 빠르게 찾을 수 있게 해주고, 데이터의 불일치, 데이터의 중복을 막을 수 있다.
<br/>

### DBMS
Database Management System의 약자로,\
사용자에게 DB를 정의하고, 만들고, 관리하는 기능을 제공하는 \
소프트웨어 시스템을 의미한다.

대표적으론 PostgreSQL, MySQL, Oracle 등이 존재한다.

사용자에게 DB를 정의하는 기능을 제공하다보면, 부가적인 데이터가 발생한다.\
이 때 부가 데이터를 **Metadata**라고 한다.
<br/>
<br/>

### MetaData
데이터베이스를 정의하거나, 기술(descriptive)하는 데이터를 의미한다.\
쉽게 말해, **데이터를 설명하는데 사용되는 데이터**를 의미한다. \
Metadata는 catalog라고도 부른다.

* ex) 데이터 유형, 구조, 제약 조건, 인덱스, 사용자 그룹 등

Metadata 또한 DBMS를 통해 저장되고, 관리 된다.
<br/>
<br/>

### Database System
Database와 DBMS, 이와 연관된 application을 합쳐, database system이라고 부른다.\
종종, database라고 줄여서 부르기도 한다.
<br/>
<br/>

## Data Model
### Data Model
DB의 구조(structure)를 기술하는데 사용될 수 있는 개념들이 모인 집합.\
DB 구조를 추상화해서 표현할 수 있는 수단을 제공한다.

데이터 모델은 여러 종류가 존재하며, \
추상화 수준과 DB 구조화 방식이 조금씩 다르다. \
또한, DB에서 읽고 쓰기 위한 기본적인 동작들도 포함된다.

> 쉽게, 모델링으로 이해하면 될 것 같음.
<br/>

### Data Model 분류
* Conceptual(high-level) data models
* Logical(representational) data models
* physical(low-level) data models
<br/>

### Conceptual data model
일반 사용자들이 쉽게 이해할 수 있는 개념들로 이뤄진 모델.\
따라서 추상화 수준이 가장 높고, \
비즈니스 요구 사항을 추상화하여 기술할 때 주로 사용된다.

![image](https://github.com/jub3907/Spring-study/assets/58246682/28570b31-9222-4abc-96a2-52c827da26a5)
<br/>
<br/>

### Logical Data Model
이해하기 어렵지 않으면서도, **디테일하게 DB를 구조화**할 수 있는 개념을 제공한다.\
데이터가 컴퓨터에 저장될 때의 구조와 크게 다르지 않게, DB를 구조화할 수 있다.

특정 DBMS나 storage에 종속되지 않는 수준에서 DB를 구조화할 수 있는 모델이다.\
이 중, 백엔드 개발자가 가장 많이 사용하게 되는 건 relational data model이다.

![image](https://github.com/jub3907/Spring-study/assets/58246682/25ea97e0-cd03-4625-a8a4-76b9a70eac4e)

이 때 **relation**은, 위의 테이블을 의미한다고 보면 된다.\
이외에도 Object Data Model, Object-relational Data Model 등이 존재한다.
<br/>
<br/>

### Physical Data Model
컴퓨터가 어떻게 파일 형태로 저장되는지를 기술할 수 있는 수단을 제공한다.\
data format, data ordering, access path 등이 대표적이다.
<br/>
<br/>

## Schema and State
### Database Schema
Data Model을 바탕으로 database의 구조를 기술(description)한 것.\
schema는 database를 설계할 때 정해지며, 한번 정해진 후에는 자주 바뀌지 않는다.
<br/>
<br/>

### Database State
database에 있는 **실제 데이터**는 꽤 자주 바뀔 수 있다.\
이 때, 특정 시점에 database에 저장된 데이터를 **database state**, 혹은 **snapshot**이라 한다.\
혹은 database에 있는 현재 instances의 집합이라고도 부른다.
<br/>
<br/>

### three-schema architecture
database system을 구축하는 architecture 중의 하나.\
user application으로부터 물리적인 database를 분리시키는 목적으로 사용된다.

세 가지 level이 존재하며, 각각의 level마다 schema가 정의되어 있다.
* external schemas
* conceptual schemas
* internal schemas

![image](https://github.com/jub3907/Spring-study/assets/58246682/6f8bf585-c4ef-4533-b66b-7a9638cfb052)
<br/>
<br/>

### internal schema
사진에서 보이는 것처럼, 실제 물리 저장장치에 가장 가까이 있는 schema.\
물리적으로, 실제로 데이터가 어떻게 저장되는지 physical data model을 통해 표현된다.

data storage, structure, access path(index) 등 실체가 있는 내용을 기술한다.
<br/>
<br/>

### external schema
실제 사용자가 바라보는 schema로, external view, user view라고도 불린다.\
각각 유저들이 필요로 하는 데이터만 표현하고,\
그 외 알려줄 필요가 없는 데이터는 숨기게 된다.

logical data model을 통해 표현된다.
<br/>
<br/>

### conceptual schema
전체 database에 대한 구조를 기술한다. \
internal schema를 한번 추상화 시켜서 표햔한 schema라고 이해하자.

물리적인 저장 구조에 관한 내용은 숨기지만, 논리적으로 conceptual하게 데이터를 표현한다.

entities, data types, relationship, constraints에 집중한다.
<br/>
<br/>

### three-schema architecture 정리
안정적으로 데이터 베이스 시스템을 운영하기 위해 사용하는 architecture.\
각 레벨을 독립시켜서 어느 레벨에서의 변화가 상위 레벨에 영향을 주지 않기 위한 목적으로 사용된다.

대부분의 DBMS가 three level을 완벽하게, 혹은 명시적으로 나누지는 않는다.\
단, 데이터가 존재하는 곳은 internal level이다.
<br/>
<br/>

## Database Language
### data definition language(DDL)
conceptual schema를 정의하기 위해 사용되는 언어.\
간혹, internal schema까지 정의할 수 있는 경우도 존재한다.
<br/>
<br/>

### storage definition language(SDL)
internal schema를 정의하는 용도로 사용되는 언어.
<br/>
<br/>

### view definition language(VDL)
external schema를 정의하기 위해 사용되는 언어.\
대부분의 DBMS에선 DDL이 VDL 역할까지 수행한다.
<br/>
<br/>

### data manipulation language(DML)
database에 있는 데이터를 활용하기 위한 언어.\
data 추가, 삭제, 수정, 검색 등의 기능을 제공하는 언어이다.
<br/>
<br/>

### 통합된 언어
오늘날의 DBMS는 DMS, VDL, DDL이 따로 존재하기 보단, 통합된 언어로 존재한다.\
대표적인 예가 바로 **Relational Database Language(SQL)**이다.
<br/>
<br/>

![image](https://github.com/jub3907/Spring-study/assets/58246682/f40c8ca2-d70d-44c1-a52a-2313b59ac8d0)## Relational Data Model 
### set
서로 다른 elements를 가지지 않는 collection. 중복 값을 가지지 않는다.\
하나의 set에서, elements의 순서는 중요하지 않다.


### relation in mathmatics
set A : { 1, 2 }
set B : { p, 1, r }

* Cartesian product A X B : A와 B에서 원소를 뽑아, 가능한 모든 경우의 쌍.\
* 모든 Binary Relation은 Cartesian Product의 부분 집합이다.

즉, 수학에서의 **relation**은 Cartesian Product의 부분 집합, \
혹은 **튜플(tuples)**의 집합을 의미한다.
<br/>
<br/>


### relational data model
data model에서 relation을 이해하기 위해, 다음과 같은 도메인을 정의해보자.
* student_ids : 학번의 집합, 7자리 integer 정수
* human_names : 사람 이름 집합, 문자열
* university_grades : 대학교 학년 집합, {1, 2, 3, 4}
* major_names : 대학교에서 배우는 전공 이름 집합
* phone_numbers : 핸드폰 번호 집합

### domain and attribute of student relation
![image](https://github.com/jub3907/Spring-study/assets/58246682/1e5b471c-1dfa-4948-928b-799b6eb0c1f4)

설계를 하던 중, 학생이 연락되지 않을 때를 대비해 **비상 연락망**이 필요하다는 생각이 든다.\
하지만 동일한 도메인(phone_number)이 같은 도메인에서 두 번 사용되므로, \
이 때 역할을 표시해주기 위해, relational data model에선 **attribute**라는 개념이 등장한다.

![image](https://github.com/jub3907/Spring-study/assets/58246682/b4f8c55b-5398-4e69-9353-ec203161521a)

attribute는 각각의 도메인에서 어떤 역할을 수행하는지를 나타낸다.\
<br/>
<br/>

### student relation in relational data model
![image](https://github.com/jub3907/Spring-study/assets/58246682/f75cca22-a2c3-4c32-8b2f-c034e4b37b33)

### relational data model 주요 개념 정리
* domain
  * set of atomic(더 이상 나눠질 수 없는) values

* domain name
  * domain 이름

* attribute
  * domain이 relation(table)에서 맡은 역할 이름

* tuple
  * 각 attribute의 값으로 이루어진 리스트. 일부 값은 NULL일 수 있다.

* relation
  * set of tuple

* relation name
  * relation의 이
<br/>

### relation schema
relation의 구조를 나타내며, relation 이름과 attributes 리스트로 표현된다.
```sql
STUDENT(id, name, grade, major, phone_num, emer_phone_num)
```
attributes와 관련된 constraints도 포함된다.
<br/>
<br/>

### degree(차수) of a relation
relation schema에서 attribtes의 수를 의미한다.
<br/>
<br/>

### Relational DataBase
relational data model에 기반하여 구조화된 database를 의미한다.\
이러한 relational database는 여러 개의 relations로 구성된다.
<br/>
<br/>

### relation의 특징들
* relation은 중복된 tuple을 가질 수 없다.\
  ![image](https://github.com/jub3907/Spring-study/assets/58246682/1cddd349-b615-456d-802d-90bd9fd5f07f)

* relation의 tuple을 식별하기 위해, attribute의 부분 집합을 key로 설정한다.\
  ![image](https://github.com/jub3907/Spring-study/assets/58246682/7f0a0145-b92d-4953-a190-842f702ed6ff)
  
* relation에서 tuple의 순서는 중요하지 않다.
* 하나의 relation에서 attribute의 이름은 중복되선 안된다.\
  ![image](https://github.com/jub3907/Spring-study/assets/58246682/a1e2ac89-eebd-4626-8a86-4f08dda54a8e)\
  또한, 하나의 tuple에서 attribute의 순서는 중요하지 않다.
* attribute는 atomic 해야 한다. (composite, multivalued attribe 허응 x)\
  ![image](https://github.com/jub3907/Spring-study/assets/58246682/53f3c275-9ba6-4e0b-8fdc-8c2dc0c740a6)\
  이 때, Atomic이란 **원자적인, 더이상 나뉠 수 없는**을 의미한다.\
  사진에서 address를 볼 때, **서울 특별시**와 **강남구**, **청담동**은 서로 나뉠 수 있으므로\
  쪼개서 저장해야만 한다. 전공도 마찬가지이다.
<br/>
  
### NULL
![image](https://github.com/jub3907/Spring-study/assets/58246682/452c49f3-8999-450f-b91f-fc7e2e3b984c)

NULL은 총 세 가지 의미를 갖는다.
* **값이 존재하지 않는다**
* 값이 존재하나, 아직 그 값이 무엇인지 알지 못한다.
* 해당 사항과 관련이 없다.
<br/>

## key
### super key
relation에서 tuples를 unique하게 식별할 수 있는 attributes set을 의미한다.\
예를 들어, `PLAYER(id, name, team_id, back_number, birth_day)`의 superkey는\
`{id, name, team_id, back_number, birth_date}`, `{id, name}` 등이 될 수 있다.
<br/>
<br/>

### candidate key
어느 한 attribute라도 제거하면 unique하게 tuples를 식별할 수 없는 superkey를 의미한다.\
key of minimal superkey라고 이해할 수 있다.

`PLAYER(id, name, team_id, back_number, birth_day)`의 candidate key는\
`{id}`, `{team_id, back_number}`가 된다.
<br/>
<br/>

### primary key
relation에서 tuples를 unique하게 식별하기 위해 선택된 candidate key를 의미한다.

`PLAYER(id, name, team_id, back_number, birth_day)`의 primary key는\
`{id}`, `{team_id, back_number}`가 된다. 하지만, 일반적으로 primary key는\
attribute 숫자가 적은것으로 선택한다.
<br/>
<br/>

### unique key
primary key가 아닌 candidate keys를 의미한다. alternate key라고도 불린다.\
앞서 예시에서 id가 primary key로 선택되었다고 가정한다면, \
`{team_id, back_number}`가 unique key이다.
<br/>
<br/>

### foreign key
다른 relation의 Primary Key(PK)를 참조하는 attributes set을 의미한다.

예를 들어, `PLAYER(id, name, team_id, back_number, birth_day)`와 \
`TEAM(id, name, manager)`가 있을 때, foreign key는 PLAYER의 `{team_id}`이다.\
(PLAYER의 team_id는 TEAM의 id를 참조할 수 있으므로.)
<br/>
<br/>

## Constraints
### Constraints란?
relational database의 relations들이 언제나 항상 지켜줘야 하는 제약 사항을 의미한다.
<br/>
<br/>

### Implicit constraints
relational data model 자체가 가지는 constraints.
* relation은 중복되는 tuple을 가질 수 없다.
* relation 내에서는 같은 이름의 attribute를 가질 수 없다.
<br/>

### Schema-based constraints
주로 DDL을 통해 schema에 직접 명시할 수 있는 constraints를 의미한다.\
explicit constraints라고도 불린다.

* **domain constraints**
  * attribute의 value는 해당 attribute의 domain에 속한 value여야 한다.\
    ![image](https://github.com/jub3907/Spring-study/assets/58246682/411225ba-42df-41c2-b24c-779ba57684d6)
* **key constraints**
  * 서로 다른 tuples는 같은 value의 key를 가질 수 없다.\
    ![image](https://github.com/jub3907/Spring-study/assets/58246682/9730a4d9-37b8-463c-bb87-a1cb9b6f2ef2)
* **NULL value constraints**
  * attribute가 NOT NULL로 명시되었다면, NULL을 값으로 가질 수 없다.\
    ![image](https://github.com/jub3907/Spring-study/assets/58246682/0433d7fe-adfc-43fc-9381-8452d7257fb8)
* entity integrity constraint
  * primary key는 value에 NULL을 가질 수 없다.\
* referential integrity constraint
  * Foreign key와 Primary key는 도메인이 같아야하고,\
    PK에 없는 value를 FK가 값으로 가질 수 없다.\
    ![image](https://github.com/jub3907/Spring-study/assets/58246682/4cb06fc9-e106-4cb4-886c-3cbefdc08532)
