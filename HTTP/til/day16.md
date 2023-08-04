## HTTP 메소드 생성 예제

### 요구사항
회원 정보 관리 API를 만들자.
* 회원 목록 조회
* 회원 조회
* 회원 등록
* 회원 수정
* 회원 삭제

### API URI 설계
위와 같은 요구사항을 보고, 아래와 같이 URI를 설계를 했다.

* 회원 목록 조회 /read-member-list
* 회원 조회 /read-member-by-id
* 회원 등록 /create-member
* 회원 수정 /update-member
* 회원 삭제 /delete-member

과연, 이 URI가 좋은 URI 설계일까?

가장 중요한 것은, **리소스 식별**이다.

### API URL 설계 고민
* 리소스의 의미는 무엇일까?
  * 회원을 등록하고, 수정하고, 조회하는 것이 리소스가 아니다.
  * 즉, **회원이라는 개념 자체가 리소스**이다.
* 그렇다면, 리소스를 어떻게 식별하는게 좋을까?
  * 회원을 등록하고, 수정하고, 조회하는 것은 모두 배제한다.
  * **회원이라는 리소스만 식별하면 된다. -> 회원 리소스를 URI에 매핑한다.**

### API URL 설계, 리소스 식별, URI 계층 구조 활용
* 회원 목록 조회 /members
* 회원 조회 /members/{id}
* 회원 등록 /members/{id}
* 회원 수정 /members/{id}
* 회원 삭제 /members/{id}

> 참고: 계층 구조상 상위를 컬렉션으로 보고 복수단어 사용 권장(member -> members)

위처럼 설계 했는데, 조회와 등록, 수정, 삭제는 어떻게 구분해야 할까?

가장 중요한 것은, 리소스와 행위를 분리하는 것이다. 즉, 리소스를 식별하는 것이다.
* **URI는 리소스만 식별!**
* 리소스와 해당 리소스를 대상으로 하는 행위를 분리해라.
  * 리소스 : 회원
  * 행위 : 조회, 등록, 삭제, 변경

* 리소스는 명사, 행위는 동사.
* 그렇다면, 행위는 어떻게 구분하는가?
  * HTTP 메소드를 사용해 구분한다.

### HTTP 메소드 - GET, POST
HTTP 메소드란, 클라이언트가 서버에 요청을 할 때 **기대하는 행동**이다.
* GET: 리소스 조회
* POST: 요청 데이터 처리, 주로 등록에 사용
* PUT: 리소스를 대체, 해당 리소스가 없으면 생성
* PATCH: 리소스 부분 변경
* DELETE: 리소스 삭제

#### 기타 메소드
* HEAD: GET과 동일하지만 메시지 부분을 제외하고, 상태 줄과 헤더만 반환
* OPTIONS: 대상 리소스에 대한 통신 가능 옵션(메서드)을 설명(주로 CORS에서 사용)
* CONNECT: 대상 리소스로 식별되는 서버에 대한 터널을 설정
* TRACE: 대상 리소스에 대한 경로를 따라 메시지 루프백 테스트를 수행


### GET
```http
GET /search?q=hello&hl=ko HTTP/1.1
Host: www.google.com

```
리소스를 조회할 때 사용한다.

서버에 전달하고 싶은 데이터는 query를 통해서 전달한다. \
메세지 바디를 사용해서 데이터를 전달할 수 있지만, 권장하지는 않는다.

클라이언트가 서버에 다음과 같은 http 메세지를 전달한다고 가정하자.
```http
GET /members/100 HTTP/1.1
Host: localhost:8080
```
해당 데이터 조회에 성공하면, 서버는 다음과 같은 응답 데이터를 반환한다.
```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 34
{
 "username": "young",
 "age": 20
}
```

### POST
```http
POST /members HTTP/1.1
Content-Type: application/json

{
 "username": "hello",
 "age": 20
}
```
메세지 바디를 통해 서버로 요청 데이터를 전달해 처리해달라고 맡길 때 사용한다.

일반적으로, 메세지 바디를 통해 들어온 데이터를 처리하는 **모든 기능**을 수행한다.\
주로, 전달된 데이터로 신규 리소스 등록, 프로세스 처리 등에 사용된다.\
POST를 사용하기 위해선, 클라이언트와 서버 사이에 **미리 기능을 약속해두어야 한다.**

`POST /members`가 신규 회원을 등록한다는 약속이라고 가정해보자. \
클라이언트가 서버에 다음과 같은 http 메세지를 전달한다.
```http
POST /members HTTP/1.1
Content-Type: application/json

{
 "username": "young",
 "age": 20
}
```

그럼 서버에선 해당 데이터로 신규 리소스 식별자 `/members/100`을 생성하고, \
클라이언트로부터 받은 데이터를 DB에 저장한다.
```json
{
 "username": "young",
 "age": 20
}
```

신규 리소스 생성에 성공 했으므로, 다음과 같은 응답 데이터를 전송한다.
```http
HTTP/1.1 201 Created
Content-Type: application/json
Content-Length: 34
Location: /members/100

{
 "username": "young",
 "age": 20
}
```

#### POST는 요청 데이터를 어떻게 처리한다는 뜻일까?
* 스펙: POST 메서드는 대상 리소스가 리소스의 고유 한 의미 체계에 따라 요청에 포함 된 표현을 처리하도록 요청합니다. (구글 번역)
* 예를 들어 POST는 다음과 같은 기능에 사용됩니다.
  * HTML 양식에 입력 된 필드와 같은 데이터 블록을 데이터 처리 프로세스에 제공
    * 예) HTML FORM에 입력한 정보로 회원 가입, 주문 등에서 사용
* 게시판, 뉴스 그룹, 메일링 리스트, 블로그 또는 유사한 기사 그룹에 메시지 게시
  * 예) 게시판 글쓰기, 댓글 달기
* 서버가 아직 식별하지 않은 새 리소스 생성
  * 예) 신규 주문 생성
* 기존 자원에 데이터 추가
  * 예) 한 문서 끝에 내용 추가하기

**이 리소스 URI에 POST 요청이 오면 요청 데이터를 어떻게 처리할지 리소스마다 따로 정해야 함 -> 정해진 것이 없음**

### POST 정리
1. 새 리소스 생성(등록)
  * 서버가 아직 식별하지 않은 새 리소스 생성
2. 요청 데이터 처리
  * 단순히 데이터를 생성하거나, 변경하는 것을 넘어서 프로세스를 처리해야 하는 경우
  * 예) 주문에서 결제완료 -> 배달시작 -> 배달완료 처럼 단순히 값 변경을 넘어 프로세스의 상태가 변경되는 경우
  * POST의 결과로 새로운 리소스가 생성되지 않을 수도 있음
  * 예) POST /orders/{orderId}/start-delivery (컨트롤 URI)
3. 다른 메서드로 처리하기 애매한 경우
  * 예) JSON으로 조회 데이터를 넘겨야 하는데, GET 메서드를 사용하기 어려운 경우
  * 애매하면 POST를 사용하자.


### PUT
```http
PUT /members/100 HTTP/1.1
Content-Type: application/json

{
 "username": "hello",
 "age": 20
}
```

리소스를 대체할 때 사용한다.\
리소스가 있으면 대체하지만, 리소스가 없으면 새로 생성한다.\
즉, 쉽게 이야기해서 덮어씌우기를 한다고 생각하면 편하다.

중요한 점은, **클라이언트가 리소스를 식별**한다는 점이다.\
클라이언트가 리소스의 위치를 알고, URI를 지정한다. 이 점이 POST와의 차이점이다.
> http 메세지를 보면, 회원 정보를 전송하고 있다.

#### PUT - 리소스가 이미 있는 경우
클라이언트는 서버에, 다음과 같은 http 요청을 보냈다고 가정하자.
```http
PUT /members/100 HTTP/1.1
Content-Type: application/json

{
 "username": "old",
 "age": 50
}
```
이 때, 서버에는 다음과 같은 데이터가 **이미** 존재하고 있다.
`/members/100`
```json
{
 "username": "young",
 "age": 20
}
```
클라이언트는 **PUT 메소드를** 사용했기 때문에, \
서버에 저장되어 있던 리소스는 전송 받은 데이터로 **완전히 대체**된다.
`/members/100`
```json
{
 "username": "old",
 "age": 50
}
```

#### PUT - 리소스가 없는 경우
이전과 동일하게, 클라이언트가 http 요청을 전달했다.
```http
PUT /members/100 HTTP/1.1
Content-Type: application/json

{
 "username": "old",
 "age": 50
}
```
이번엔 서버에 `/members/100`에 해당하는 리소스가 없기 때문에, 신규 리소스를 생성한다.
```json
{
 "username": "old",
 "age": 50
}
```

#### PUT 메소드, 주의해야 할 점
이번엔 `username` 필드가 없는 데이터를 전송했다고 가정하자.
```http
PUT /members/100 HTTP/1.1
Content-Type: application/json

{
 "age": 50
}
```
서버에는 이미 다음과 같은 데이터가 저장되어 있다.
```json
{
 "username": "young",
 "age": 20
}
```
얼핏 느끼기엔 `age` 필드만 업데이트 될 것 같지만, \
실제로는 **전체 리소스를 대체하기 때문에** 다음과 같이 age 데이터만 남게 된다.
`/members/100`
```json
{
 "age": 50,
}
```

### PATCH
```http
PATCH /members/100 HTTP/1.1
Content-Type: application/json

{
 "age": 50
}
```
PATCH는 리소스를 부분적으로 변경할 때 사용한다.

* 현재 서버 상태

`/members/100`
```json
{
 "username": "young",
 "age": 20
}
```

* 클라이언트 -> 서버, 요청 전송
```http
PATCH /members/100 HTTP/1.1
Content-Type: application/json

{
 "age": 50
}
```

* 요청 성공, age만 50으로 변경된 서버 상태

`/members/100`
```json
{
 "username": "young",
 "age": 50
}
```

### DELETE
```http
DELETE /members/100 HTTP/1.1
Host: localhost:8080

```
리소스를 삭제할 때 사용한다.

### HTTP 메소드의 속성
* 안전(Safe Methods)
* 멱등(Idempotent Methods)
* 캐시 가능(Cachealble Methods)

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/52505a41-79ee-4c88-b8ff-37d7df8fabf6)

#### 안전, Safe Method
호출해도, 리소스가 변경되지 않는 메소드를 의미한다.\
POST나 PATCH처럼 데이터를 변경하는 메소드들은 안전하지 않다고 이야기 한다.

안전 메소드를 계속 호출해서 로그가 쌓이고, 장애가 발생할 수 있지만,\
안전은 해당 리소스가 변하는가, 변하지 않는가만 고려한다. 

#### 멱등, Itempotent Methods
* f(f(x)) = f(x)

한 번 호출하건 두 번 호출하건 100번 호출하건, 결과가 똑같은 메소드를 의미한다.
* GET: 한 번 조회하든, 두 번 조회하든 같은 결과가 조회된다.
* PUT: 결과를 대체한다. 따라서 같은 요청을 여러번 해도 최종 결과는 같다.
* DELETE: 결과를 삭제한다. 같은 요청을 여러번 해도 삭제된 결과는 똑같다.
* **POST: 멱등이 아니다!** 두 번 호출하면 같은 결제가 중복해서 발생할 수 있다.

이러한 멱등 메소드들은 자동 복구 메커니즘에 주로 사용된다. 서버가 TIMEOUT 등으로 \
정상 응답을 못주었을 때, 클라이언트가 같은 요청을 다시 해도 되는가? 판단의 근거가 된다.

하지만, 멱등 메소드를 보며 이러한 질문이 생길 수 있다.
* Q. 재 요청 중간에, 다른 곳에서 리소스를 변경해버리면 결과가 다르지 않나?
  * 사용자1: GET -> username:A, age:20
  * 사용자2: PUT -> username:A, age:30
  * 사용자1: GET -> username:A, age:30 -> 사용자2의 영향으로 바뀐 데이터 조회
* A. **멱등은 외부 요인으로 중간에 리소스가 변경되는 것 까지는 고려하지 않는다.**


#### 캐시 가능, Cacheable
간단하게, 우리가 웹 브라우저에 이미지를 요청했다고 가정하자. \
해당 이미지가 한번 더 필요하다면, 이전에 받은 이미지를 사용하면 된다. 이러한 행위가 간단한 캐싱이다.

GET, HEAD, POST, PATCH 메소드만 캐시가 가능하지만, \
POST와 PATCH는 본문 내용까지 캐시 키로 고려해야 하는데, 구현이 쉽지 않으므로\
실제로는 GET, HEAD 정도만 캐시로 사용한다.

## HTTP 메소드 활용
### 클라이언트에서 서버로 데이터 전송
데이터 전달 방식은 크게 두 가지로 나뉜다.
* **쿼리 파라미터를 통한 데이터 전송**
  * GET
  * 주로, 정렬 필터(검색어)에 사용
 
* **메시지 바디를 통한 데이터 전송**
  * POST, PUT, PATCH
  * 회원 가입, 상품 주문, 리소스 등록, 리소스 변경
 

### 클라이언트에서 서버로 데이터 전송 4가지 상황
* 정적 데이터 조회
  * 이미지, 정적 텍스트 문서
* 동적 데이터 조회
  * 주로 검색, 게시판 목록에서 정렬 필터(검색어)
* HTML Form을 통한 데이터 전송
  * 회원 가입, 상품 주문, 데이터 변경
* HTTP API를 통한 데이터 전송
  * 회원 가입, 상품 주문, 데이터 변경
  * 서버 to 서버, 앱 클라이언트, 웹 클라이언트(Ajax)
 
### 정적 데이터 조회 - 쿼리 파라미터 미사용
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/b025917b-66fa-4747-938b-586438931d6d)

클라이언트에서 서버로 이미지를 요청했을 때, 서버는 이미지 리소스를 내려준다. \
간단한 데이터 조회이므로, URI를 제외한 데이터는 전송하지 않는다.

즉, 이미지나 정적 텍스트 문서같은 경우 조회이기 때문에 GET을 사용하고,\
정적 데이터는 위처럼 일반적으로 쿼리 파라미터 없이 리소스 경로로 단순하게 조회할 수 있다.

### 동적 데이터 조회 - 쿼리 파라미터 사용
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/7987f831-4b47-489a-917c-132320985de2)

동적 데이터를 조회하게 된다면, 위처럼 쿼리 파라미터를 사용해 데이터를 요청한다.\
서버에선 이러한 쿼리를 사용해 데이터를 찾고, 응답해준다.

주로 조회 조건을 줄여주는 필터나, 조회 결과를 정렬하는 정렬 조건에 사용한다.\
이 역시 조회이기 때문에 GET을 사용한다.

### HTML Form을 통한 데이터 전송
#### POST 전송, 저장
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/8b386520-8630-4d85-84fa-413307d32aac)

Form에선 `method`와 `name`을 사용해 http 요청 메세지를 생성해, 서버로 전송한다.\
서버에선 해당 데이터를 받아, 약속된 동작(이 경우, 회원 가입)을 수행한다.

위의 form에서 http 메소드를 GET으로 변경할 수도 있다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/0d5e27ee-ff75-4d2b-a69e-5bba9577b8b4)

GET을 사용하면 바디 부분이 아닌 쿼리 파라미터로 데이터가 전송 되지만, \
GET은 리소스 변경이 아닌 조회에만 사용해야 한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/8dc19072-9c07-4219-8885-33236fd01dc7)

#### multipart/form-data
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/9abb7ecd-79aa-4b4b-b6e3-8eafbae54a08)

파일 업로드같은 바이너리 데이터 전송시 사용한다.\
다른 종류의 여러 파일, 폼의 내용과 함께 전송할 수 있다. 

#### 정리
* HTML Form submit시 POST 전송
  * 예) 회원 가입, 상품 주문, 데이터 변경
* Content-Type: application/x-www-form-urlencoded 사용
  * form의 내용을 메시지 바디를 통해서 전송(key=value, 쿼리 파라미터 형식)
  * 전송 데이터를 url encoding 처리
    * 예) abc김 -> abc%EA%B9%80
* HTML Form은 GET 전송도 가능
* Content-Type: multipart/form-data
  * 파일 업로드 같은 바이너리 데이터 전송시 사용
  * 다른 종류의 여러 파일과 폼의 내용 함께 전송 가능(그래서 이름이 multipart)
* 참고: HTML Form 전송은 **GET, POST**만 지원

### HTTP API 데이터 전송
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/c094d910-2314-4bcf-8127-b1b7811bc18e)

* 서버 to 서버
  * 백엔드 시스템 통신
* 앱 클라이언트
  * 아이폰, 안드로이드
* 웹 클라이언트
  * HTML에서 Form 전송 대신 자바 스크립트를 통한 통신에 사용(AJAX)
  * 예) React, VueJs 같은 웹 클라이언트와 API 통신
* POST, PUT, PATCH: 메시지 바디를 통해 데이터 전송
* GET: 조회, 쿼리 파라미터로 데이터 전달
* Content-Type: application/json을 주로 사용 (사실상 표준)
  * TEXT, XML, JSON 등등


## HTTP API 설계 예제

* **HTTP API - 컬렉션**
  * **POST 기반 등록**
  * 예) 회원 관리 API 제공
* **HTTP API - 스토어**
  * **PUT 기반 등록**
  * 예) 정적 컨텐츠 관리, 원격 파일 관리
* **HTML FORM 사용**
  * 웹 페이지 회원 관리
  * GET, POST만 지원

### 회원 관리 시스템 - API 설계, POST 기반 등록
* 회원 목록 /members -> **GET**
* 회원 등록 /members -> **POST**
* 회원 조회 /members/{id} -> **GET**
* 회원 수정 /members/{id} -> **PATCH, PUT, POST**
  * PUT : 회원의 모든 데이터를 전송해야 하므로, 좋은 사용법이 아니다. PUT은 게시글 수정과 같이, 전체를 변경해야 하는 경우 사용하자.
  * PATCH : 부분 수정이 가능하므로, PATCH를 사용하는 것이 좋다.
* 회원 삭제 /members/{id} -> **DELETE**

#### POST 기반 등록, POST의 특징
* **클라이언트는 등록될 리소스의 URI를 모른다.**
  * 클라이언트는 `POST /members` 요청을 보내고, 서버에서 생성한 리소스의 URI 생성 및 반환.
  * 회원 등록 /members -> POST
* 서버가 새로 등록된 리소스 URI를 생성해준다.
  * HTTP/1.1 201 Created, Location: `/members/100`
* 컬렉션(Collection)
  * 서버가 관리하는 리소스 디렉토리
  * 서버가 리소스의 URI를 생성하고 관리
  * 여기서 컬렉션은 /members

### 파일 관리 시스템 - API 설계, PUT 기반 등록
* 파일 목록 /files -> GET
* 파일 조회 /files/{filename} -> GET
* 파일 등록 /files/{filename} -> PUT
  * 없으면 생성, 있으면 덮어쓰기. 
* 파일 삭제 /files/{filename} -> DELETE
* 파일 대량 등록 /files -> POST
  * 임의로.

#### 파일 관리 시스템, PUT의 특징
* POST와 반대로, 클라이언트가 리소스 URI를 알고 있어야 한다.
  * 파일 등록 /files/**{filename}** -> PUT
  * `PUT /files/star.jpg`
* 클라이언트가 직접 리소스의 URI를 지정한다.
* 이렇게, 클라이언트가 관리하는 리소스 저장소를 **스토어(Store)**라고 한다.
  * 클라이언트가 리소스의 URI를 알고 관리
  * 여기서 스토어는 /files


### HTML Form 사용
HTML FORM은 기본적으로, **GET, POST만 지원**한다. \
AJAX같은 기술을 사용해서 이러한 한계를 해결할 수 있지만, \
우리는 지금 순수한 HTML, HTML FORM을 이야기 하고 있으므로, 제약이 존재한다.

* 회원 목록 /members -> GET
* 회원 등록 폼 /members/new -> GET
* 회원 등록 /members/new, /members -> POST
  * 두 가지 URI를 사용할 수 있지만, `/members/new`를 추천.
* 회원 조회 /members/{id} -> GET
* 회원 수정 폼 /members/{id}/edit -> GET
* 회원 수정 /members/{id}/edit, /members/{id} -> POST
  * 이 또한 두 가지 URI를 사용할 수 있지만..
* 회원 삭제 /members/{id}/delete -> POST


앞서 말했듯, HTML FORM은 GET, POST만 지원한다. 이러한 제약을 해결하기 위해, 컨트롤 URI를 사용한다.
* 컨트롤 URI
  * GET, POST만 지원하므로 제약이 있음
  * 이런 제약을 해결하기 위해 동사로 된 리소스 경로 사용
  * POST의 /new, /edit, /delete가 컨트롤 URI
  * HTTP 메서드로 해결하기 애매한 경우 사용(HTTP API 포함)


## 정리
* **HTTP API** - 컬렉션
  * **POST 기반 등록**
  * 서버가 리소스 URI 결정
* **HTTP API**- 스토어
  * **PUT** 기반 등록
  * 클라이언트가 리소스 URI 결정
* **HTML FORM 사용**
  * 순수 HTML + HTML form 사용
  * GET, POST만 지원

### 참고하면 좋은 URI 설계 개념
#### 문서(document)
  * 단일 개념(파일 하나, 객체 인스턴스, 데이터베이스 row)
  * 예) /members/100, /files/star.jpg
#### 컬렉션(collection)
  * 서버가 관리하는 리소스 디렉터리
  * 서버가 리소스의 URI를 생성하고 관리
  * 예) /members
#### 스토어(store)
  * 클라이언트가 관리하는 자원 저장소
  * 클라이언트가 리소스의 URI를 알고 관리
  * 예) /files
#### 컨트롤러(controller), 컨트롤 URI
  * 문서, 컬렉션, 스토어로 해결하기 어려운 추가 프로세스 실행
  * 동사를 직접 사용
  * 예) /members/{id}/delete
> https://restfulapi.net/resource-naming/
