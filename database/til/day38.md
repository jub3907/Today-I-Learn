## SELECT
### SELECT state 1
ID가 9인 임직원의 이름, 직군 탐색
```sql
SELECT name, position FROM employee WHERE id = 9;
```
이 때, `id = 0`를 **selection condition**, \
`name, position`을 **projection attributes**라 부른다.
<br/>

### Select 정리
```sql
SELECT attributes
FROM table
[ WHERE conditions ];
```
<br/>


### SELECT statement 2
project 2002를 리딩(leading)하고 있는 임직원의 ID, 이름, 직군
```sql
SELECT employee.id, employee.name, position
FROM project, employee
WHERE project.id = 2002 and project.leader_id = employee.id;

+----+--------+----------+
| id | name   | position |
+----+--------+----------+
| 13 | JISUNG | PO       |
+----+--------+----------+
```
* projection attributes : employee.id, employee.name, position
* selection condition : project.id = 2002
* join condition : project.leader_id = employee.id
<br/>

## AS
AS는 테이블이나 attribute에 별칭(alias)를 붙일 때 사용한다.\
이전, SELECT statement 2를 AS를 사용해 수정해보자.
```sql
SELECT E.id, E.name, position
FROM project AS P, employee AS E
WHERE P.id = 2002 and P.leader_id = E.id;

+----+--------+----------+
| id | name   | position |
+----+--------+----------+
| 13 | JISUNG | PO       |
+----+--------+----------+
```

또한, 위에서 설명한 것처럼 attribute에도 AS를 사용할 수 있다.
```sql
SELECT E.id AS leader_id, E.name AS leader_name, position
FROM project AS P, employee AS E
WHERE P.id = 2002 and P.leader_id = E.id;
```
또한, AS는 생략 가능하다.
```sql
SELECT E.id leader_id, E.name leader_name, position
FROM project P, employee E
WHERE P.id = 2002 and P.leader_id = E.id;
```
<br/>

## DISTINCT
DISTINCT는 select 결과에서 중복되는 tuples을 제외하고 싶을 때 사용한다.

### DISTINCT 사용하기
디자이너들이 참여하고 있는 프로젝트들의 ID와 이름을 알고 싶다고 가정해보자.\
우리가 사용할 조건은 EMPLOYEE.position이 'DSGN'인 경우이다.
```sql
SELECT P.id, P.name
FROM employee as E, works_on AS W, project AS P
WHERE E.position = 'DSGN' and
      E.id = W.empl_id and
      W.proj_id = P.id;
```
이번엔 DISTINCT를 사용해보자.
```sql
SELECT DISTINCT P.id, P.name
FROM employee as E, works_on AS W, project AS P
WHERE E.position = 'DSGN' and
      E.id = W.empl_id and
      W.proj_id = P.id
```
<br/>

## LIKE
LIKE는 문자열 pattern matching에 사용된다.
* reserved character
  * `%` : 0개 이상의 임의의 개수를 가지는 문자들을 의미한다.
  * `_` : 하나의 문자를 의미한다.
* escape character
  * `\` : 예약 문자를 escape시켜, 문자 본연의 문자로 사용하고 싶을 때 사용한다.
<br/>

### LIKE 사용하기
이름이 N으로 시작하거나 N으로 끝나는 임직원들의 이름 탐색
```sql
SELECT name
FROM employee
WHERE name LIKE 'N%' or name LIKE '%N';
+--------+
| name   |
+--------+
| BROWN  |
| NICOLE |
+--------+
```
<br/>
### LIKE 사용하기 2
이름에 'NG'가 들어가는 임직원의 이름 탐색
```sql
SELECT name
FROM employee
WHERE name LIKE '%NG%';

+--------+
| name   |
+--------+
| DINGYO |
| JISUNG |
+--------+
```
<br/>

### LIKE 사용하기 3
이름이 J로 시작하는, 총 네 글자의 이름을 가지는 임직원들의 이름 탐색
```sql
SELECT name
FROM employee
WHERE name LIKE 'J____';
```
<br/>

### escape 문자와 함께 LIKE 사용하기
%로 시작하거나, _로 끝나는 프로젝트 이름을 알고 싶다면, \
escape문자를 사용하면 된다.
```sql
SELECT name
FROM project
WHERE name LIKE '\%%' or
      name LIKE '%\_';
```
<br/>

## 조회시 주의사항
SELECT로 조회할 때, 조건을 포함해서 조회한다고 가정해보자.\
이 때, 이 조건들과 관련된 attributes에 index가 걸려있어야 한다.\
그렇지 않으면, 데이터가 많아질수록 조회 속도가 느려진다.\
ex) `SELECT * FROM employee WHERE position = 'dev_back';`


## subquery
subquery(nested query, or inner query)는 SELECT, \
INSERT, UPDATE, DELETE에 포함된 쿼리를 의미한다.

이 때, subquery를 포함하는 쿼리를 **outer query, main query**라고 한다.\
subquery는 `( )` 안에 기술된다.
<br/>
<br/>

### SELECT with subquery 1
ID가 14인 임직원보다 생일이 빠른 임직원의 ID, 이름, 생일 탐색을 한다 가정하자.\
우선, id가 14인 임직원의 생일을 가져온다.
```sql
SELECT birth_date FROM employee WHERE id = 14;

+------------+
| birth_date |
+------------+
| 1992-08-04 |
+------------+
```
우리는 이 가져온 birth_date를 사용해, 원래 가져오고자 하는 데이터를 불러온다.
```sql
SELECT id, name, birth_date
FROM employee
WHERE birth_date < '1992-08-04';
```
이 두 개의 쿼리 하나의 쿼리로 합치기 위해, 첫 번쨰 쿼리를 subquery로 만들 수 있다.
```sql
SELECT id, name, birth_date
FROM employee
WHERE birth_date < (
            SELECT birth_date FROM employee WHERE id = 14
      );

+----+--------+------------+
| id | name   | birth_date |
+----+--------+------------+
|  1 | messi  | 1987-02-01 |
|  5 | DINGYO | 1990-11-05 |
|  6 | JULIA  | 1986-12-11 |
|  9 | HENRY  | 1982-05-20 |
| 10 | NICOLE | 1991-03-26 |
| 13 | JISUNG | 1989-07-07 |
+----+--------+------------+
```
<br/>

### SELECT with subquery 2
ID가 1인 임직원과 같은 부서, 같은 성별인 임직원들의 ID, 이름, 직
```sql
SELECT id, name, position
FROM employee
WHERE (dept_id, sex) = (
    SELECT dept_id, sex
    FROM employee
    WHERE id = 1
);
```
<br/>

### SELECT with subquery 3
ID가 5인 임직원과 같은 프로젝트에 참여한 임직원들의 ID

우선, ID가 5인 임직원이 참여한 프로젝트 ID를 찾아오자.
```sql
SELECT proj_id FROM works_on WHERE empl_id = 5;

+---------+
| proj_id |
+---------+
|    2001 |
|    2002 |
+---------+
```
얻어온 프로젝트 ID를 사용해, 기존 목표를 달성하자.
```sql
SELECT DISTINCT empl_id 
FROM works_on
WHERE empl_id != 5 AND (proj_id = 2001 OR proj_id = 2002);
```
<br/>

## IN
`v IN (v1, v2, v3, ..)\
v가 (v1, v2, v3, ...) 중 하나와 값이 같다면, TRUE를 반환한다.\
이 때, (v1, v2, v3...)은 명시적인 값들의 집합일 수도 있고,\
subquery의 결과(set or multiset)일 수도 있다.

`v NOT IN (v1, v2, v3, ...)\
v가 (v1, v2, v3, ...)의 모든 값과 값이 다르다면 TRUE를 반환한다.
<br/>
<br/>

### IN 사용 예제
앞선 Select with subquery 3 예제는 IN 키워드를 사용해 손쉽게 구성할 수 있다.
```sql
SELECT DISTINCT empl_id 
FROM works_on
WHERE empl_id != 5 AND proj_id in (2001, 2002);
```
쿼리를 보면 알 수 있지만, 여기엔 subquery를 적용시킬 수 있다.
```sql
SELECT DISTINCT empl_id 
FROM works_on
WHERE empl_id != 5 AND proj_id in (
    SELECT proj_id FROM works_on WHERE empl_id = 5
);
```

이 때, unqualified attribute가 참조하는 테이블은\
해당 attribute가 사용된 query를 포함하여, 그 query의 바깥쪽으로 존재하는 \
모든 queries중, 해당 attribute 이름을 가지는 가장 가까이에 있는 table을 참조한다.

### SELECT with subquery 4
앞선 예제를 발전시켜, ID가 5인 임직원과 같은 프로젝트에 참여한\
임직원들의 ID와 이름을 찾아보자.
```sql
SELECT id, name
FROM employee
WHERE id IN (
    SELECT DISTINCT empl_id 
    FROM works_on
    WHERE empl_id != 5 AND proj_id in (
        SELECT proj_id 
        FROM works_on 
        WHERE empl_id = 5
    )
);
```
또한 subquery에 AS 키워드를 사용할 수 있다.
```sql
SELECT id, name
FROM employee, 
    ( 
        SELECT DISTINCT empl_id 
        FROM works_on
        WHERE empl_id != 5 AND proj_id in (
            SELECT proj_id 
            FROM works_on 
            WHERE empl_id = 5
        )
    ) AS DSTNCT_E
WHERE id = DSTNCT_E.empl_id;
```

<br/>


### EXIST 사용 예제
ID가 7, 혹은 12인 임직원이 참여한 프로젝트의 ID와 이름
```sql
SELECT P.id, P.name
FROM project P
WHERE EXISTS (
    SELECT *
    FROM works_on W
    WHERE W.proj_id = P.id AND W.empl_id IN (7, 12)
);
```
* Correlated query
  * subquery가 바깥쪽 query의 attribute를 참조할 때, \
    correlated subquery라고 부른다.
* EXISTS
  * subquery의 결과가 **최소** 하나의 row라도 있다면 TRUE를 반환한다.

<br/>


### SELECT with subquery 5
2000년대생이 없는 부서의 ID와 이름
```sql
SELECT D.id, D.name
FROM department as D
WHERE NOT EXISTS (
    SELECT *
    FROM employee E
    WHERE E.dept_id = D.id AND E.birth_date >= '2000-01-01'
);
```
이는 NOT IN으로도 변경할 수 있다.
```sql
SELECT D.id, D.name
FROM department as D
WHERE D.id NOT IN (
    SELECT E.dept_id
    FROM employee E
    WHERE E.birth_date >= '2000-01-01'
);
```
<br/>


### SELECT with subquery 6, ANY
리더보다 높은 연봉을 받는 부서원을 가진 리더의 ID, 이름, 연봉
```sql
SELECT E.id, E.name, E.salary
FROM department D, employee E
WHERE D.leader_id = E.id AND E.salary < ANY (
        SELECT salary
        FROM employee
        WHERE id <> D.leader_id AND dept_id = E.dept_id
    );
```
* `<>` = `!=`
* `v comparision_operator ANY (subquery)`
  * subquery가 반환한 결과들 중에 단 하나라도 v와 비교 연산이 TRUE라면, TRUE 반환.
  * SOME도 ANY와 같은 역할을 한다.

추가로, 해당 부서의 최고 연봉도 알아보도록 하자.
```sql
SELECT E.id, E.name, E.salary, 
    ( 
        SELECT max(salary)
        FROM employee
        WHERE dept_id = E.dept_id
    ) AS dept_max_salary
FROM department D, employee E
WHERE D.leader_id = E.id AND E.salary < ANY (
        SELECT salary
        FROM employee
        WHERE id <> D.leader_id AND dept_id = E.dept_id
    );
```
<br/>

### SELECT with subquery, ALL
ID가 13인 임직원과 한번도 같은 프로젝트에 참여하지 못한 임직원의 ID, 이름, 직
```sql
SELECT DISTINCT E.id, E.name, E.position
FROM employee E, works_on W
WHERE E.id = W.empl_id AND W.proj_id <> ALL (
    SELECT proj_id
    FROM works_on
    WHERE empl_id = 13
);
```
* `v comparision_operator ALL (subquery)`
  * subquery가 반환된 결과들과 v와의 비교연산이 모두 TRUE라면, TRUE 반
<br/>

### IN vs EXISTS
RDBMS의 종류와 버전에 따라 성능이 다르지만,\
최근엔 많은 개선이 이루어져, IN과 EXISTS의 성능 차이가 거의 없다.


## NULL
### SQL에서 NULL의 의미
* unknown
* unavailable or withheld
* not applicable

EMPLOYEE 테이블에, 다음과 같은 데이터가 있다고 가정해보자.\
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/7ac9549e-a9f0-43fe-9046-f2ca652b3c6b)\
birth_date가 NULL인 데이터를 조회해보자.
```sql
SELECT id FROM emplyee WHERE birth_date = NULL;
```

```sql
SELECT id FROM employee WHERE birth_date IS NULL;
```
<br/>

### NULL과 Three-Valued Logic
```sql
SELECT * FROM employee WHERE birth_date = '1990-03-09';
```
위 쿼리를 실행했을 때, birth_date가 null인 row가 존재한다.\
SQL에서 NULL과 비교연산을 하게 되면, 그 결과는 **UNKNOWN**이다.\
UNKNOWN은 **TRUE일 수도 있고, FALSE일 수도 있다**라는 의미.

* three-valued logic : 비교/논리 연산의 결과로 TRUE, FALSE, UNKNOWN을 가진다.
<br/>

### NULL의 비교 연산 결과
|제목|내용|
|:---:|:---:|
|1 = 1|TRUE|
|1 != 1|FALSE|
|1 = NULL|UNKNOWN|
|1 != NULL|UNKNOWN|
|1 > NULL|UNKNOWN|
|1 <= NULL|UNKNOWN|
|NULL = NULL|UNKNOWN|
<br/>

### UNKNOWN의 논리 연산 결과
|AND|TRUE|FALSE|UNKNOWN|
|:---:|:---:|:---:|:---:|
|TRUE|TRUE|FALSE|UNKNOWN|
|FALSE|FALSE|FALSE|FALSE|
|UNKNOWN|UNKNOWN|FALSE|UNKNOWN|

|OR|TRUE|FALSE|UNKNOWN|
|:---:|:---:|:---:|:---:|
|TRUE|TRUE|TRUE|TRUE|
|FALSE|TRUE|FALSE|UNKNOWN|
|UNKNOWN|TRUE|UNKNOWN|UNKNOWN|

|NOT|TRUE|FALSE|UNKNOWN|
|:---:|:---:|:---:|:---:|
||FALSE|TRUE|UNKNOWN|
<br/>

### WHERE절의 conditions
where절에 있는 condition의 결과가 TRUE인 tuple만 선택된다.\
즉, 결과가 FALSE이거나 **UNKNOWN**이면 tuple은 선택되지 않는다.
<br/>
<br/>

### NOT IN 사용시 주의사항
`v NOT IN (v1, v2, v3)`은 다음과 같은 의미이다.\
`v != v1 AND v != v2 AND v != v3`\
이 때, 만약 v1, v2, v3중 하나가 NULL이라면, UNKNOWN이 반환될 수 있다.

|NOT IN 예제|결과|
|:---:|:---:|
|3 not in (1, 2, 4)|TRUE|
|3 not in (1, 2, 3|FALSE|
|3 not in (1, 3, NULL)|FALSE|
|3 not in (1, 2, NULL)|UNKNOWN|
<br/>


## JOIN
두 개 이상의 table들에 있는 데이터를 한 번에 조회하는 것.\
여러 종류의 JOIN이 존재한다
<br/>
<br/>

## Implicit Join vs Explicit Join
### Implicit Join 
ID가 1인 임직원이 속한 부서 이름을 가져온다고 가정해보자.
```sql
SELECT D.name
FROM employee AS E, department AS D
WHERE E.id = 1 and E.dept_id = D.id;
```
위와 같은 방식을 Implicit join이라 한다.

Implicit join은 FROM절에 table들만 나열하고,\
WHERE절에 join condition을 명시하는 방식이다.

WHERE절에 selection condition과 join condition이 같이 있기 떄문에, 가독성이 떨어진다.\
또한, 복잡한 join 쿼리를 작성하다보면 실수로 잘못된 쿼리를 작성할 가능성이 크다.
<br/>
<br/>

### explicit join
동일하게, ID가 1인 임직원이 속한 부서 이름을 가져온다고 가정해보자.
```sql
SELECT D.name
FROM employee AS E JOIN department AS D ON E.dept_id = D.id
WHERE E.id = 1;
```
위와 같은 방식을 Explicit Join이라 한다.

explicit join은 from절에 JOIN 키워드와 함께 joined table들을 명시하는 방식이다.\
from절에서 ON 뒤에 join condition이 명시된다.

가독성이 좋고, 복잡한 join 쿼리 작성 중에도 실수할 가능성이 적다.
<br/>
<br/>

## Inner Join vs Outer Join
### Inner Join
기존 데이터가 다음과 같이 존재한다고 가정하자.\
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/923ce6fe-7ee5-4dc4-bf95-d92537461d6b)

이 때, **Inner Join**을 사용하면, 다음과 같은 결과물을 얻을 수 있다.

```sql
SELECT D.name
FROM employee AS E INNER JOIN department AS D ON E.dept_id = D.id
```
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/8a210d97-3948-4a1c-818f-1e403f425221)

inner join은 두 table에서 join condition을 만족하는 tuple들로\
result table을 만드는 join이다.

* `FROM table1 [INNER] JOIN table2 ON join_condition`

join condition에는 >, <, >, != 등등 여러 비교 연산자를 사용할 수 있다.\
join condition에서 null 값을 가지는 tuple은 result table에 포함되지 못한다
<br/>
<br/>


### Outer Join
outer join은 두 table에서 join condition을 만족하지 않는 tuple들도 \
result table에 포함하는 join이다.
* `FROM table1 LEFT [OUTER] JOIN table2 ON join_condition`
* `FROM table1 RIGHT (OUTER] JOIN table2 ON join_condition`
* `FROM table1 FULL [OUTER] JOIN table2 ON join_condition`

join condition에는 >, <, >, != 등등 여러 비교 연산자를 사용할 수 있다.

이전과 동일하게, 다음과 같은 데이터가 존재한다고 가정하자.\
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/003712ec-be72-42b8-8af5-3ccb50074c24)\
위 데이터에 OUTER join을 사용해보자.
* LEFT JOIN
  ```sql
  SELECT *
  FROM employee E LEFT OUTER JOIN department D ON E.dept_id = D.id;
  ```
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/10055099-2de6-4c6d-ad7d-490ebb6cff5a)

* RIGHT JOIN
  ```sql
  SELECT *
  FROM employee E RIGHT OUTER JOIN department D ON E.dept_id = D.id;
  ```
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/c32bc6a6-3b3a-4709-9e79-49b137690dc0)

* FULL OUTER JOIN
  ```sql
  SELECT *
  FROM employee E FULL OUTER JOIN department D ON E.dept_id = D.id;
  ```
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/18366ca8-5ae7-4268-b196-8f5ca3f9f02f)

<br/>

### equi join
join condition에서 =(equality comparator)를 사용하는 join.

equi join에는 두 가지 시각이 존재한다.
* inner join outer join 상관없이 = 를 사용한 join이라면 equi join으로 보는 경우
* inner join으로 한정해서 = 를 사용한 경우에 equi join으로 보는 경우
<br/>

### using
두 tableol equi join 할 때 join하는 attribute의 이름이 같다면, \
**USING** 으로 간단하게 작성할 수 있다.\
이 때 같은 이름의 attribute는 result table에서 한번만 표시된다.

* `FROM table1 [INNER] JOIN table2 USING (attribute(s))`
* `FROM table1 LEFT [OUTER] JOIN table2 USING (attribute(s))`
* `FROM table1 RIGHT [OUTER] JOIN table2 USING (attribute(s))`
* `FROM table1 FULL [OUTER] JOIN table2 USING (attribute(s))`

```sql
SELECT *
FROM employee E INNER JOIN department D USING (dept_id);
```
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/aca1cfc5-84dd-45ba-8933-008149608d13)
<br/>
<br/>

### natural join
두 table에서 같은 이름을 가지는 모든 attribute pair에 대해서 \
equi join을 수행한다. join condition을 따로 명시하지 않는다.
```sql
FROM table1 NATURAL [INNER] JOIN table2
FROM table1 NATURAL LEFT [OUTER] JOIN table2
FROM table1 NATURAL RIGHT [OUTER] JOIN table2
FROM table1 NATURAL FULL [OUTER] JOIN table2
```

```sql
SELECT *
FROM employee E NATURAL INNER JOIN department D;
```
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/27bf3cb9-f828-4876-b9d7-e4817e0ee847)

<br/>
<br/>

### cross join
두 table의 tuple pair로 만들 수 있는 모든 조합(= Cartesian product)을 \
result table로 반환한다. join condition이 없다
* implicit cross join: `FROM table1, table2`
* explicit cross join: `FROM table1 CROSS JOIN table2`

```sql
SELECT *
FROM employee CROSS JOIN department;
```
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/e3902155-69ca-4fe6-bc41-24d66283940a)

다만, MySQL에선 cross join = inner join = join이다.\
CROSS JOIN에 ON을 같이 쓰면 INNER JOIN으로 동작한다.\
INNER JOIN이 ON 없이 사용되면 CROSS JOIN으로 동작한다.
<br/>
<br/>

### join example 1
ID가 1003인 부서에 속하는 임직원 중, \
리더를 제외한 부서원의 ID, 이름, 연봉
```sql
SELECT E.id, E.name, E.salary
FROM employee E JOIN department D ON E.dept_id = D.id
WHERE E.dept_id = 1003 and E.id != D.leader_id;
```
<br/>

### join example 2
ID가 2001인 프로젝트에 참여한 임직원들의 이름과 직군과 소속 부서 이름
```sql
SELECT  E.name AS empl_name,
        E.position AS empl_position,
        D.name AS dept_name
FROM works_on W JOIN employee E ON W.empl_id = E.id
                LEFT JOIN department D ON E.dept_id = D.id
WHERE W.proj_id = 2001;
```
<br/>


SELECT proj_id, COUNT(*), ROUND(AVG(salary), 0)
FROM works_on W JOIN employee E ON w.empl_id = E.id WHERE E.birth_date BETWEEN '1990-01-01' AND '1999-12-31'
AND W.proj_id IN (SELECT proj_id FROM works_on
GROUP BY W.proj_id ORDER BY W.proj_id;
GROUP BY proj_id HAVING COUNT(*) >= 7)

## ORDER BY
조회 결과를 특정 attribute(s) 기준으로 정렬하여 가져오고 싶을 때 사용한다.\
default 정렬 방식은 오름차순이다.

* 오름차순 정렬은 ASC로 표기한다
* 내림차순 정렬은 DESC로 표기한다

### ORDER BY 예제
임직원들의 정보를 연봉 순서대로 정렬
```sql
SELECT * FROM employee ORDER BY salary;

+----+---------+------------+------+-----------+-----------+---------+
| id | name    | birth_date | sex  | position  | salary    | dept_id |
+----+---------+------------+------+-----------+-----------+---------+
| 11 | SUZANNE | 1993-03-23 | F    | PO        | 150000000 |    1005 |
|  9 | HENRY   | 1982-05-20 | M    | HR        | 164000000 |    1002 |
| 13 | JISUNG  | 1989-07-07 | M    | PO        | 180000000 |    1005 |
|  3 | JENNY   | 2000-10-12 | F    | DEV_BACK  | 200000000 |    1003 |
|  4 | BROWN   | 1996-03-13 | M    | CEO       | 240000000 |    1001 |
|  5 | DINGYO  | 1990-11-05 | M    | CTO       | 240000000 |    1001 |
|  6 | JULIA   | 1986-12-11 | F    | CFO       | 240000000 |    1001 |
| 14 | SAM     | 1992-08-04 | M    | DEV_INFRA | 280000000 |    1003 |
|  7 | MINA    | 1993-06-17 | F    | DSGN      | 320000000 |    1004 |
| 12 | CURRY   | 1998-01-15 | M    | PLN       | 340000000 |    1005 |
|  2 | JANE    | 1996-05-05 | F    | DSGN      | 360000000 |    1004 |
| 10 | NICOLE  | 1991-03-26 | F    | DEV_FRONT | 360000000 |    1003 |
|  1 | messi   | 1987-02-01 | M    | DEV_BACK  | 400000000 |    1003 |
+----+---------+------------+------+-----------+-----------+---------+
...
```
기본적으로 오름 차순 방식으로 데이터가 출력된다.\
이는 DESC 키워드를 사용해, 내림 차순으로 변경할 수 있다.

```sql
SELECT * FROM employee ORDER BY salary DESC;


|  1 | messi   | 1987-02-01 | M    | DEV_BACK  | 400000000 |    1003 |
|  2 | JANE    | 1996-05-05 | F    | DSGN      | 360000000 |    1004 |
| 10 | NICOLE  | 1991-03-26 | F    | DEV_FRONT | 360000000 |    1003 |
| 12 | CURRY   | 1998-01-15 | M    | PLN       | 340000000 |    1005 |
...
```

이번엔 **부서 별로 묶어**, 같은 부서 내에서 연봉 순서로 나열해보자.
```sql
SELECT * FROM employee ORDER BY dept_id ASC, salary DESC;

+----+---------+------------+------+-----------+-----------+---------+
| id | name    | birth_date | sex  | position  | salary    | dept_id |
+----+---------+------------+------+-----------+-----------+---------+
|  4 | BROWN   | 1996-03-13 | M    | CEO       | 240000000 |    1001 |
|  5 | DINGYO  | 1990-11-05 | M    | CTO       | 240000000 |    1001 |
|  6 | JULIA   | 1986-12-11 | F    | CFO       | 240000000 |    1001 |
|  9 | HENRY   | 1982-05-20 | M    | HR        | 164000000 |    1002 |
|  1 | messi   | 1987-02-01 | M    | DEV_BACK  | 400000000 |    1003 |
| 10 | NICOLE  | 1991-03-26 | F    | DEV_FRONT | 360000000 |    1003 |
| 14 | SAM     | 1992-08-04 | M    | DEV_INFRA | 280000000 |    1003 |
|  3 | JENNY   | 2000-10-12 | F    | DEV_BACK  | 200000000 |    1003 |
|  2 | JANE    | 1996-05-05 | F    | DSGN      | 360000000 |    1004 |
|  7 | MINA    | 1993-06-17 | F    | DSGN      | 320000000 |    1004 |
| 12 | CURRY   | 1998-01-15 | M    | PLN       | 340000000 |    1005 |
| 13 | JISUNG  | 1989-07-07 | M    | PO        | 180000000 |    1005 |
| 11 | SUZANNE | 1993-03-23 | F    | PO        | 150000000 |    1005 |
+----+---------+------------+------+-----------+-----------+---------+
```
결과를 보면 알 수 있듯, **우선** dept_id를 기준으로 정렬한다.\
그 후, 연봉을 내림차순으로 정렬하는걸 확인할 수 있다.
<br/>
<br/>

## aggregate function
여러 tuple들의 정보를 요약해서 하나의 값으로 추출하는 함수.\
대표적으로 COUNT, SUM, MAX, MIN, AVG 함수가 있다.

주로 관심있는 attribute에 사용된다 e.g.) AVG(salary), MAX(birth_date)\
NULL값들은 제외하고 요약 값을 추출한다.

### aggregate function 예제 1
임직원 수를 알고 싶다.
```sql
SELECT COUNT(*) FROM employee;

+----------+
| COUNT(*) |
+----------+
|       13 |
+----------+
```
<br/>

### aggregate function 예제 2
프로젝트 2002에 참여한 임직원 수와, 최대 연봉, 최소 연봉, 평균 연봉
```sql
SELECT COUNT(*), MAX(salary), MIN(salary), AVG(salary) 
FROM works_on W JOIN employee E ON W.empl_id = E.id 
WHERE W.proj_id = 2002;
```

<br/>

## GROUP BY

관심있는 attribute(s) 기준으로 그룹을 나눠서 \
**그룹별로 aggregate function을 적용하고 싶을 때 사용**한다.

* grouping attribute(s): 그룹을 나누는 기준이 되는 attribute(s)

grouping attribute(s)에 NULL 값이 있을 때는 NULL 값을 가지는 tuple끼리 묶인다.

### GROUP BY 예제
바로 앞서 예제를 이어서 확인해보자.\
각 프로젝트에 참여한 임직원 수와 최대 연봉과 최소 연봉, 평균 연봉

```sql
SELECT W.proj_id, COUNT(*), MAX(salary), MIN(salary), AVG(salary) 
FROM works_on W JOIN employee E ON W.empl_id = E.id 
GROUP BY W.proj_id;

+---------+----------+-------------+-------------+----------------+
| proj_id | COUNT(*) | MAX(salary) | MIN(salary) | AVG(salary)    |
+---------+----------+-------------+-------------+----------------+
|    2001 |        3 |   400000000 |   180000000 | 273333333.3333 |
|    2003 |        2 |   340000000 |   320000000 | 330000000.0000 |
+---------+----------+-------------+-------------+----------------+
```
각각의 통계를 어떤 프로젝트에 대한 통계인지 알기 위해, proj_id를 넣어줘야 한다.
<br/>
<br/>

## having
GROUP BY와 함께 사용한다.\
aggregate function의 결과값을 바탕으로 그룹을 필터링하고 싶을 때 사용한다.\
HAVING절에 명시된 조건을 만족하는 그룹만 결과에 포함된다.

### having 예제
프로젝트 참여 인원이 7명 이상인 프로젝트들에 대해,\
각 프로젝트에 참여한 임직원의 수와 최대, 최소, 평균 연봉 탐색.
```sql
SELECT W.proj_id, COUNT(*), MAX(salary), MIN(salary), AVG(salary) 
FROM works_on W JOIN employee E ON W.empl_id = E.id 
GROUP BY W.proj_id;
HAVING COUNT(*) >= 3;

+---------+----------+-------------+-------------+----------------+
| proj_id | COUNT(*) | MAX(salary) | MIN(salary) | AVG(salary)    |
+---------+----------+-------------+-------------+----------------+
|    2001 |        3 |   400000000 |   180000000 | 273333333.3333 |
+---------+----------+-------------+-------------+----------------+
```
<br/>

## 예제 탐색
### 예제 1
각 부서별 인원수를 인원수가 많은 순서대로 정렬
```sql
SELECT dept_id, COUNT(*) AS empl_count FROM employee
GROUP BY dept_id
ORDER BY empl_count DESC;

+---------+------------+
| dept_id | empl_count |
+---------+------------+
|    1003 |          4 |
|    1001 |          3 |
|    1005 |          3 |
|    1004 |          2 |
|    1002 |          1 |
+---------+------------+
```
<br/>


### 예제 2
회사 전체의 평균 연봉보다 평균 연봉이 적은 부서들의 평균 연봉을 알고 싶다.
```sql
SELECT dept_id, AVG(salary) 
FROM employee 
GROUP BY dept_id
HAVING AVG(salary) < (
            SELECT AVG(salary) FROM employee
      );

+---------+----------------+
| dept_id | AVG(salary)    |
+---------+----------------+
|    1001 | 240000000.0000 |
|    1002 | 164000000.0000 |
|    1005 | 223333333.3333 |
+---------+----------------+
```
<br/>


### 예제 3
각 프로젝트별 프로젝트에 참여한 90년대생 수와 평균 연봉
```sql
SELECT proj_id, COUNT(*), ROUND(AVG(salary), 0)
FROM works_on W JOIN employee E ON w.empl_id = E.id
WHERE E.birth_date BETWEEN '1990-01-01' AND '1999-12-31' 
GROUP BY W.proj_id
ORDER BY W.proj_id;

+---------+----------+-----------------------+
| proj_id | COUNT(*) | ROUND(AVG(salary), 0) |
+---------+----------+-----------------------+
|    2001 |        1 |             240000000 |
|    2003 |        2 |             330000000 |
+---------+----------+-----------------------+
```
추가로, 프로젝트 참여 인원이 7명 이상인 프로젝트로 한정해보자.
```sql
SELECT proj_id, COUNT(*), ROUND(AVG(salary), 0)
FROM works_on W JOIN employee E ON w.empl_id = E.id
WHERE E.birth_date BETWEEN '1990-01-01' AND '1999-12-31' 
GROUP BY W.proj_id
HAVING COUNT(*) >= 7
ORDER BY W.proj_id;
```
쉽게 생각하면 위처럼 having을 사용하겠지만, 아니다.\
여기서 having에 사용된 count는 그룹핑을 한 뒤, 각 그룹에 대해 카운트를 한 것이다.\
따라서, 이 때의 count는 **프로젝트 참여 인원 수**가 아닌 \
**프로젝트에 참여한 90년대생의 수**이므로, where절을 사용해야만 한다.

```sql
SELECT proj_id, COUNT(*), ROUND(AVG(salary), 0)
FROM works_on W JOIN employee E ON w.empl_id = E.id
WHERE E.birth_date BETWEEN '1990-01-01' AND '1999-12-31' AND
      W.proj_id IN (  SELECT proj_id 
                      FROM works_ON
                      GROUP BY proj_id
                      HAVING COUNT(*) >= 7
                )
GROUP BY W.proj_id
ORDER BY W.proj_id;
```
having 키워드를 사용해선 안된다. 
<br/>


## SELECT 총 정리
SELECT `attributes or aggregate functions`
FROM `tables`
[ WHERE `conditions`]
[ GROUP BY `group attributes`]
[ HAVING `group conditions`]
[ ORDER BY `attributes`]
