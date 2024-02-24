## 1. 터미널을 이용해 AWS EC2 연결합니다.

## 2. apt-get update를 통해 업데이트합니다.

```
apt-get update
```

## 3. MySQL 설치하기

```
apt-get install mysql-server
```

위 명령어를 통해 mysql-server를 다운받습니다.

Version이 중요합니다.

5버전을 받았는지, 8버전을 받았는지 확인이 필요합니다. \
아래 명령어를 통해서 버전을 확인할 수 있습니다.

```
mysql --version
```

## 4. EC2 안에서 mysql 접근하기

평소 local 명령어와 같습니다.

설치 후 비밀번호가 없습니다. 엔터를 쳐서 mysql 접속하면 됩니다.

```
mysql -u root -p
```

### 4-0. 반드시 암호를 수정, 설정해야 합니다.

암호 설정 없이 EC2를 종료하면 찾지 못합니다. \
초기화하는 방법은 있지만 힘들고, EC2 Instance 생성 작업부터 \
다시 시작해야 하는 상황이 발생할 수 있습니다.

### 4-1. MySQL 비밀번호 바꾸기

```
use mysql
```

Database Changed가 출력됩니다.

```
// 5 버전 사용자
mysql> update user set password=password("암호") where user="root";
```

```
// 8 버전 사용자
mysql> alter user "root"@"localhost" identified with mysql_native_password by "암호";
```

### 4-2. 암호 적용하기

```
FLUSH PRIVILEGES;
```

## 5. 외부 접속 허용하기 (mysqld.cnf) 수정

### 5-1. EC2에서 최고 권한 부여받기

최고 권한 부여받는 방법은 쉽습니다.

```
sudo su
```

### 5-2. mysqld.cnf 디렉토리로 이동하기

아래 명령어를 통해 mysqld.cnf 파일이 있는 디렉토리로 이동이 가능하고, \
살펴보며 가실 분은 cd로 디렉토리를 이동하면서 \
ls를 한 번씩 쳐주는 방법이 있습니다.

```
cd/etc/mysql/mysql.conf.d
```

### 5-3. vi 에디터 명령어를 통해 mysqld.cnf 파일 실행

```
vi mysqld.cnf
```

### 5-4. bind-address 수정하기

bind-address를 찾아 수정해주면 됩니다.\
수정 후 :wq 를 통해 저장 후 종료 하면 됩니다.

```
bind-address = 0.0.0.0
```

## 6. MySQL 서버 재시작

`service mysql restart` 명령으로 MySQL 서버 재시작하기

## 7. 작동 확인

다양한 방법이 있지만 MySQL WorkBench를 사용하는 게 가장 좋다고 생각됩니다.

```
Hostname: EC2 IPv4 주소 입력
Username: root 또는 개인이 생성한 Username
Password: Store in Keychain 클릭 후 비밀번호 입력
```

Test Connection을 클릭해 원격 접속이 가능한지 확인 후 OK 클릭
(Test Connection을 권장해 드리는 이유는 OK 바로 클릭하면, \
사용자 또는 비밀번호 잘못됐을 때 대기 시간이 길어집니다.)

출처 : https://velog.io/@cptkuk91/AWS-EC2%EC%97%90-MySQL-%EC%84%A4%EC%B9%98-%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0
