## Store
### Stored Function
사용자가 정의하여, DBMS에 저장되고 사용되는 함수.\
SQL의 select, insert, update, delete statement에서 사용할 수 있다.

### Stored function 예제 1
임직원의 ID를 열 자리 정수로 랜덤하게 발급하고 싶다고 가정해보자.\
단, ID의 맨 앞자리는 1로 고정이다.

```sql

delimiter $$
CREATE FUNCTION id_generator() 
RETURNS int
NO SQL 
BEGIN
    RETURN (1000000000 + floor (rand() * 1000000000));
END
$$
delimiter ;
```
이렇게 생성한 function을 사용해, 데이터를 삽입해보자.
```sql
INSERT INTO employee
VALUES (id_generator(), 'JEHN', '1991-08-04', 'F', 'PO', 100000000, 1005);

select * from employee where name = 'JEHN';

+------------+------+------------+------+----------+-----------+---------+
| id         | name | birth_date | sex  | position | salary    | dept_id |
+------------+------+------------+------+----------+-----------+---------+
| 1823677362 | JEHN | 1991-08-04 | F    | PO       | 100000000 |    1005 |
+------------+------+------------+------+----------+-----------+---------+
```
### Stored function 예제 2
부서의 ID를 파라미터로 받으면, 해당 부서의 평균 연봉을 알려주는 함수.
```sql
delimiter $$
CREATE FUNCTION dept_avg_salary(d_id int)
RETURNS int
READS SQL DATA
BEGIN
    DECLARE avg_sal int;
    select avg(salary) into avg_sal
                       from employee
                       where dept_id = d_id;
    RETURN avg_sal;
END
$$
delimiter ;

SELECT *, dept_avg_salary(id)
FROM department;

+------+-------------+-----------+---------------------+
| id   | name        | leader_id | dept_avg_salary(id) |
+------+-------------+-----------+---------------------+
| 1001 | headquarter |         4 |           240000000 |
| 1002 | HR          |         6 |           164000000 |
| 1003 | development |         1 |           310000000 |
| 1004 | design      |         3 |           340000000 |
| 1005 | product     |        13 |           192500000 |
+------+-------------+-----------+---------------------+
```

### stored function이 추가로 할 수 있는 영역
loop를 돌면서 반복적인 작업을 수행하거나,\
case 키워드를 사용해서 값에 따라 분기 처리하거나,\
에러를 핸들링하거나, 에러를 일으키는 등 다양한 동작을 정의할 수 있다.

### 등록된 stored function 확인
```sql
SHOW FUNCTION STATUS where DB = 'company';

+---------+-----------------+--
| Db      | Name            | ...
+---------+-----------------+-
| company | dept_avg_salary | ...
| company | id_generator    | ...
+---------+-----------------+---
```

## Trigger
### SQL에서 Trigger
데이터베이스에서 어떠한 이벤트가 발생했을 때, 자동적으로 실행되는 procedure.\
즉, 데이터에 변경이 생겼을 때(insert, update, delete)\
이것이 계기가 되어 자동적으로 실행되는 프로시저를 의미한다.

### Trigger 예시 1
사용자의 닉네임 변경 이력을 저장하는 트리거를 만든다고 가정하자.
다음과 같이, USERS 테이블의 닉네임이 변경되면,\
USERS_LOG 테이블에 기존 닉네임을 저장한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/0d44a6d2-a270-46bb-833b-a1d49aea39c6)

```sql
delimiter $$
CREATE TRIGGER log_user_nickname_trigger
BEFORE UPDATE
ON users FOR EACH ROW
BEGIN
    insert into users_log values (OLD.id, OLD.nickname, now());
END
$$
delimiter ;
```
이 때, OLD는 update되기 전의 tuple을 가리킨다.

### TRIGGER 예시 2
사용자가 마트에서 상품을 구매할 때마다,\
지금까지 누적된 구매 비용을 구하는 트리거를 작성해보자.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/eb0d4ec2-e370-4825-8a6f-01ca8162223c)

위와 같은 테이블 상황에서, 구매 이벤트(BUY 테이블에 데이터 삽입)이 발생하면\
USER_BUY_STATS 테이블을 업데이트 시켜줘야 한다.
```sql

delimiter $$
CREATE TRIGGER sum_buy_prices_trigger
AFTER INSERT
ON buy FOR EACH ROW
BEGIN
    DECLARE total INT;
    DECLARE user_id INT DEFAULT NEW.user_id;
    
    select sum(price) into total from buy where user_id = user_id; 
    update user_buy_stats set price_sum = total where user_id = user_id;
END
$$
delimiter;
```
이 때, NEW 키워드는 테이블에 insert된 tuple을 가리킨다.\
혹은, update 된 후의 tuple을 의미한다.

### Trigger 사용시 주의 사항
소스코드로는 발견할 수 없는 로직이기 때문에,\
어떤 동작이 일어나는지 파악하기 어렵고,\
문제가 생겼을 때 대응하기 어렵다.

또한, 과도한 트리거 사용은 DB에 부담을 주고, 응답을 느리게 만든다.