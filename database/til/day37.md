## SQL
### SQL이란?
Structured Query Language의 줄임말.\
현업에서 쓰이는 relational DBMS의 표준 언어이다.
<br/>
<br/>

### SQL 주요 용어
| Relational Data Model  | SQL |
| ------------- | ------------- |
| relation  | table  |
| attribute  | column  |
| tuple  | row  |
| domain  | domain  |
<br/>

### SQL에서의 relation
multiset of tuples. 중복된 tuple을 허용한다. 
<br/>
<br/>

### SQL and RDBMS
SQL은 RDBMS의 표준 언어이지만, 실제 구현에는 강제가 없기 떄문에\
RDBMS마다 제공하는 SQL의 스펙이 조금씩 다르다.
<br/>
<br/>

## MySQL 예제
### 데이터 베이스 확인
```sql
show databases;
+--------------------+
| Database           |
+--------------------+
| information_schema |
| mysql              |
| performance_schema |
| sys                |
+--------------------+
```
<br/>


### 데이터베이스 생성
```sql
create database company;
+--------------------+
| Database           |
+--------------------+
| company            |
| information_schema |
| mysql              |
| performance_schema |
| sys                |
+--------------------+
```
<br/>


### 현재 사용중인 데이터베이스 확인
```sql
select database();
+------------+
| database() |
+------------+
| NULL       |
+------------+
```
<br/>

### 사용하고자 하는 데이터베이스 선택
```sql
use company;
+------------+
| database() |
+------------+
| company    |
+------------+
```
<br/>

### 데이터베이스 삭제
```sql
DROP DATABASE company;
```
<br/>

### DATABASE vs Schema
MySQL에선 DATABASE와 SCHEMA가 같은 뜻을 의미한다.\
CREATE DATABASE company = CREATE SCHEMA company\
하지만, 다른 RDBMS에선 의미가 다르게 사용될 수 있으므로, 주의.
<br/>
<br/>

## table
### IT 회사의 RDB 만들기
부서, 사원, 프로젝트 관련 정보들을 저장할 수 있는 관계형 데이터베이스를 만들어보자.\
![image](https://github.com/jub3907/Spring-study/assets/58246682/813afe1b-118c-4adf-b77b-098c7a800c7e)

### DEPARTMENT Table 생성
```sql
create table DEPARTMENT (
  id int primary key,
  name varchar(20) not null unique,
  leader_id int
);
```
각각의 line은 attribute를 생성한다.
<br/>
<br/>


#### attribute data type, 숫자
![image](https://github.com/jub3907/Spring-study/assets/58246682/5da8c65d-37ad-450a-93ad-8d9f3957c2ea)
<br/>
<br/>

#### attribute data type, 문자열
![image](https://github.com/jub3907/Spring-study/assets/58246682/e623dacf-a3f3-4fa5-9724-01b687e7da8e)
<br/>
<br/>


#### attribute data type, 날짜와 시간
![image](https://github.com/jub3907/Spring-study/assets/58246682/6bed8ddb-8394-4ba8-bd7e-fa2bbb76ba3d)
<br/>
<br/>

#### attribute data type, 그 외
![image](https://github.com/jub3907/Spring-study/assets/58246682/61b78f1c-ca6a-40ef-9941-4ae5b483ba7e)
<br/>
<br/>

#### Key constraints: PRIMARY KEY
primary key는 테이블의 tuple을 식별하기 위해 사용하며, \
하나 이상의 attribute로 구성된다.

이러한 pk는 중복된 값을 가질 수 없으며, NULL도 값으로 가질 수 없다.\
다음은 **잘못된 PK의 예시**이다.\
![image](https://github.com/jub3907/Spring-study/assets/58246682/47581059-c2f4-4dc8-b028-603c0b120782)

또한, 테이블을 생성할 때 다음과 같이 두 가지 방식으로 pk를 선언할 수 있다.
```sql
create table PLAYER (
  id int PRIMARY KEY,
  ...
);

create table PLAYER (
  team_id varchar(12),
  back_number int,
  ...

  PRIMARY KEY (team_id, back_number)
);
```
<br/>

#### key constraints, UNIQUE
UNIQUE로 지정된 attribute는 중복된 값을 가질 수 없다.\
단, NULL은 중복을 허용할 수도 있다. (RDBMS마다 다르다.)\
![image](https://github.com/jub3907/Spring-study/assets/58246682/32126947-5cca-4cd3-8c99-0028df659f84)

PK와 동일하게, 2개 이상의 attribute를 UNIQUE로 설정할 경우, \
attribute 리스트 마지막에 UNIQUE 키워드를 넣어줄 수 있다.
```sql

create table Orders (
  ....
  ....
  UNIQUE (team_id, back_number)
)
```
<br/>

### EMPLOYEE Table 생성
```sql
create table EMPLOYEE (
    id          int         primary key,
    name        VARCHAR(30) NOT NULL,
    birth_date  DATE,
    sex         CHAR(1)     CHECK (sex in ('M', 'F')),
    position    VARCHAR(10),
    salary      INT         DEFAULT 50000000,
    dept_id     INT,
    FOREIGN KEY (dept_id) references DEPARTMENT(id)
        on delete set NULL on update CASCADE,
    CHECK (salary >= 50000000)
);
```
<br/>

#### attribute DEFAULT
attribute의 default 값을 정의할 때 사용한다.\
새로운 tuple을 저장할 때, 해당 attribute에 대한 값이 없다면 default 값으로 저장된다.
```sql
create table Orders (
  menu  varchar(15)  DEFAULT '짜장면'
)
```
<br/>

#### CHECK constraint
CHECK는 attribute의 값을 제한하고 싶을 때 사용한다. 
```sql
# attribute 하나로 구성될 때
create table EMPLOYEE (
  ...
  age  INT  CHECK (age >= 20)
)

# attribute 하나 이상으로 구성될 때
create table PROJECT (
  start_date   DATE,
  end_date     DATE,

  CHECK (start_date < end_date)
)
```
<br/>


#### Referential Integrity constraint: FOREIGN KEY
attribute가 다른 table의 primary key나 unique key를 참조할 때 사용한다.\
![image](https://github.com/jub3907/Spring-study/assets/58246682/0f8dfd59-96be-4727-a464-cfbf792e77b5)
이 때, foreign key로 사용한 값(departmemt_id)은 참조하는 테이블에 반드시 존재해야 한다.

FOREIGN KEY는 다음과 같이 선언할 수 있다.
```sql
create table Employee (
  ...
  dept_id  INT,
  FOREIGN KEY (dept_id) references DEPARTMENT(id)
        on delete reference_option
        on update reference_option
)
```
참조하고 있던 값이 삭제되거나, 업데이트 될 때 동작 방식은 \
**`reference_option`**으로 설정할 수 있다.

* **reference_option**
  * CASCADE: 참조 값의 삭제/변경을 그대로 반영한다.
  * SET NULL: 참조 값이 삭제/변경 시 NULL로 변경한다.
  * RESTRICT: 참조 값이 삭제/변경되는 것을 금지한다.
  * NO ACTION: RESTRICT와 유사
  * SET DEFAULT: 참조 값이 삭제/변경 시 default 값으로 변경된다.
<br/>
<br/>

#### constraint 이름 명시하기
constraint에 이름을 붙이면, 어떤 규약을 위반했는지 쉽게 파악할 수 있다.\
constraint를 삭제하고 싶을 때, 해당 이름으로 삭제할 수 있다.
```sql
create table TEST (
  age   INT   CONSTRAINT   age_over_20   CHECK (age > 20)
)
```
위처럼 규약에 이름을 붙인다면, 규약을 위반했을 때 다음과 같은 메세지를 볼 수 있다.
```sql
Check constraint 'age_over_20' is violated.
# 기존: Check constraint 'test_chk_1' is violated
```
<br/>

### PROJECT Table 생성
```sql
create table PROJECT (
    id          INT         PRIMARY KEY,
    name        VARCHAR(20) NOT NULL        UNIQUE,
    leader_id   INT,
    start_date  DATE,
    end_date    DATE,
    FOREIGN KEY (leader_id) references EMPLOYEE(id)
        on delete SET NULL
        on update CASCADE,
    CHECK (start_date < end_date)
);
```
<br/>

### WORKS_ON Table 생성
```sql
create table WORKS_ON (
    empl_id         INT,
    proj_id         INT,
    PRIMARY KEY (empl_id, proj_id),
    FOREIGN KEY (empl_id) references EMPLOYEE(id)
        on delete CASCADE on update CASCADE,
    FOREIGN KEY (proj_id) references PROJECT(id)
        on delete CASCADE on update CASCADE
);
```
<br/>

### DEPARTMENT Table 수정
이전, DEPARTMEMT Table을 생성할 때, leader_id에 FOREIGN KEY를 걸어주지 않았다.\
이를 수정해보자.
```sql
ALTER TABLE department ADD FOREIGN KEY (leader_id)
REFERENCES employee(id)
on update CASCADE
on delete SET NULL;
```
<br/>

#### ALTER TABLE
table의 schema를 변경하고 싶을 때 사용된다.\
![image](https://github.com/jub3907/Spring-study/assets/58246682/829475d8-5c60-44a4-a155-70e022dd2e1d)

ALTER TABLE을 사용할 때, 이미 서비스중인 table의 schema를 변경하는 것이라면\
변경 작업 때문에 서비스의 백엔드에 영향이 없을지 검토한 후에 변경하는 것이 중요하다.
<br/>
<br/>

### 데이터 베이스 구조를 정의할 때 중요한 점
만들고자 하는 서비스의 스펙과 데이터 일관성,\
편의성, 확장성 등을 종합적으로 고려하여 DB 스키마를 적절하게 정의하는 것이 중요하다.
<br/>
<br/>

```sql
insert into project values
(2001, '쿠폰 구매/선물 서비스 개발', 13, '2022-03-10', '2022-07-09'),
(2002, '확장성 있게 백엔드 리팩토링', 13, '2022-01-23', '2022-03-23'),
(2003, '홈페이지 UI 개선', 11, '2022-05-09', '2022-06-11');
```

## 데이터 추가
### Employ table에 데이터 추가
```sql
# INSERT INTO table VALUES (attribute_values);
INSERT INTO employee VALUES (1, 'messi', '1987-02-01', 'M', 'DEV_BACK', 100000000, null);
INSERT INTO employee VALUES (2, 'JANE', '1996-05-05', 'F', 'DSGN', 90000000, null);
```
만약, 동일한 PK를 가진 데이터를 삽입하려고 한다면, 다음과 같은 오류가 발생한다.\
```sql
INSERT INTO employee VALUES (1, 'JANE', '1996-05-05', 'F', 'DSGN', 90000000, null);
# ERROR 1062 (23000): Duplicate entry '1' for key 'employee.PRIMARY'
```
다른 방식으로도 데이터를 삽입해보자.

```sql
INSERT INTO employee (name, birth_date, sex, position, id)
  VALUES ('JENNY', '2000-10-12', 'F', 'DEV_BACK', 3);
```
이러한 방식을 사용하면 **원하는 순서로 데이터를** 넣을 수 있고, \
원하는 값만 넣어줄 수 있다. 추가로, 나머지 데이터들도 넣어주자.
```sql
insert into employee values
(4, 'BROWN', '1996-03-13', 'M', 'CEO', 120000000, null), 
(5, 'DINGYO', '1990-11-05', 'M', 'CTO', 120000000, null),
(6, 'JULIA', '1986-12-11', 'F', 'CFO', 120000000, null),
(7, 'MINA', '1993-06-17', 'F', 'DSGN', 80000000, null),
(8, 'JOHN', '1999-10-22', 'M', 'DEV_FRONT', 65000000, null),
(9, 'HENRY', '1982-05-20', 'M', 'HR', 82000000, null),
(10, 'NICOLE', '1991-03-26', 'F', 'DEV_FRONT', 90000000, null),
(11, 'SUZANNE', '1993-03-23', 'F', 'PO', 75000000, null),
(12, 'CURRY', '1998-01-15', 'M', 'PLN', 85000000, null),
(13, 'JISUNG', '1989-07-07', 'M', 'PO', 90000000, null),
(14, 'SAM', '1992-08-04', 'M', 'DEV_INFRA', 70000000, null);
```
<br/>
<br/>

### INSERT statement 정리
* INSERT INTO `table_name` VALUES (`comma-separated all values)`
* INSERT INTO `table_name` (`attributes list`) VALUES (`values`)
* INSERT INTO `table_name` VALUES (`...`), (`...`);
<br/>

### 데이터 조회
```sql
SELECT * FROM employee;
+----+-------+------------+------+----------+-----------+---------+
| id | name  | birth_date | sex  | position | salary    | dept_id |
+----+-------+------------+------+----------+-----------+---------+
|  1 | messi | 1987-02-01 | M    | DEV_BACK | 100000000 |    NULL |
|  2 | JANE  | 1996-05-05 | F    | DSGN     |  90000000 |    NULL |
|  3 | JENNY | 2000-10-12 | F    | DEV_BACK |  50000000 |    NULL |
+----+-------+------------+------+----------+-----------+---------+
```
<br/>


### DEPARTMENT Table 데이터 추가
```sql
insert into department values 
(1001, 'headquarter', 4), 
(1002, 'HR', 6),
(1003, 'development', 1), 
(1004, 'design', 3), 
(1005, 'product', 13);
```
<br/>
<br/>

### PROJECT Table 데이터 추가
```sql
insert into project values
(2001, '쿠폰 구매/선물 서비스 개발', 13, '2022-03-10', '2022-07-09'),
(2002, '확장성 있게 백엔드 리팩토링', 13, '2022-01-23', '2022-03-23'),
(2003, '홈페이지 UI 개선', 11, '2022-05-09', '2022-06-11');
```
<br/>

### WORKS_ON 데이터 추가
```sql
insert into works_on values
(5, 2001),
(13, 2001),
(1, 2001),
(8, 2001),
(5, 2002),
(11, 2003),
(7, 2003),
(2, 2003),
(12, 2003);
```
<br/>

### 데이터 수정하기
employee id가 1인 MESSI는 개발팀 소속이다.\
개발팀 ID는 1003이므로, dept_id를 수정해주자.
```sql
UPDATE employee SET dept_id = 1003 WHERE id = 1;

# select * from employee where id = 1;
+----+-------+------------+------+----------+-----------+---------+
| id | name  | birth_date | sex  | position | salary    | dept_id |
+----+-------+------------+------+----------+-----------+---------+
|  1 | messi | 1987-02-01 | M    | DEV_BACK | 100000000 |    1003 |
+----+-------+------------+------+----------+-----------+---------+
```
<br/>

### 데이터 수정하기 2
이번엔, 개발팀 연봉을 두 배로 인상하고 싶다고 가정해보자.\
개발팀 ID는 1003이다.
```sql
UPDATE employee
SET salary = salary * 2
WHERE dept_id = 1003;
```
<br/>

### 데이터 수정하기 3
프로젝트 ID 2003에 참여한 임직원의 연봉을 두 배로 인상해보자.
```sql
UPDATE employee, works_on
SET salary = salary * 2
WHERE employee.id = works_on.empl_id and works_on.proj_id = 2003;
# WHERE id = empl_id and proj_id = 2003;
```
<br/>

### 데이터 수정하기 4
회사의 모든 인원의 연봉을 두배로 증가시켜보자.
```sql
UPDATE employee
SET salary = salary * 2;
```
<br/>

### UPDATE statement
UPDATE `table_names`\
SET `attribute` = `value` [, `attribute` = `value`, .. ]\
[ WHERE `conditions` ];
<br/>


## 데이터 삭제
### DELETE statement
John이 퇴사하게 되면서, employee 테이블에서 John 정보를 삭제해야 한다.\
![image](https://github.com/jub3907/Spring-study/assets/58246682/901ffc97-db75-42bf-b916-de4e0ed06736)

* 존의 employee id는 8이다.
* 현재 존은 project 2001에 참여하고 있었다.

```sql
DELETE FROM employee WHERE id = 8;
```
위처럼 삭제 하게 된다면 employee 테이블에서 데이터를 손쉽게 삭제할 수 있다.\
하지만, WORKS_ON 테이블에 데이터가 남아있지 않은가?

이전에 생성했던 WORKS_ON 테이블을 다시 살펴보자.
```sql
create table WORKS_ON (
    empl_id         INT,
    proj_id         INT,
    PRIMARY KEY (empl_id, proj_id),
    FOREIGN KEY (empl_id) references EMPLOYEE(id)
        on delete CASCADE on update CASCADE,
    FOREIGN KEY (proj_id) references PROJECT(id)
        on delete CASCADE on update CASCADE
);
```
우린 empl_id를 FK로 지정해줄 때, employee row가 삭제될 때 해당 삭제가 CASCADE되어, \
empl_id가 employee table의 id와 동일한 튜플은 모두 삭제되게 된다.
<br/>
<br/>

### DELETE State 2
Jane이 휴가를 떠나게 되어, 현재 진행중인 프로젝트에서 하차하게 되었다고 가정하자.
* Jane의 ID는 2이다.

```sql
DELETE FROM works_on WHERE empl_id = 2;
```

### DELETE State 정리
DELETE FROM `table_name`\
[ WHERE `conditions` ];