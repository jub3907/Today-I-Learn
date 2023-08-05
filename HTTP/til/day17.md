## HTTP 상태 코드
### 상태 코드
상태 코드란, 클라이언트가 보낸 요청의 처리 상태를 응답에서 알려주는 기능.
* 1xx (Informational): 요청이 수신되어 처리중
* 2xx (Successful): 요청 정상 처리
* 3xx (Redirection): 요청을 완료하려면 추가 행동이 필요
* 4xx (Client Error): 클라이언트 오류, 잘못된 문법등으로 서버가 요청을 수행할 수 없음
* 5xx (Server Error): 서버 오류, 서버가 정상 요청을 처리하지 못함

### 만약, 모르는 상태 코드가 나타난다면?
클라이언트가 인식할 수 없는 상태 코드를 서버가 반환한다면 어떻게 될까?\
클라이언트는 상위 상태코드로 해석해서 처리한다.\
만약 미래에 새로운 상태 코드가 추가되어도 클라이언트를 변경하지 않아도 된다.
* ex)
  * 299 ??? -> 2xx (Successful)
  * 451 ??? -> 4xx (Client Error)
  * 599 ??? -> 5xx (Server Error)
 
## 1xx (Informational)
요청이 수신되어, 처리중임을 나타낸다. 일반적으로 거의 사용하지 않으므로, 생략.

### 2xx (Successful)
클라리언트가 전송한 요청을 성공적으로 처리했음을 나타낸다.

#### 200 OK
보낸 요청이 성공적으로 처리됨.
#### 201 Created
클라이언트 요청에 성공해, 서버쪽에서 리소스를 생성됨.
#### 202 Accepted
요청이 접수 되었으나, 처리가 완료되지 않았음을 의미한다. \
주로 배치 처리같은 곳에서 사용한다.
#### 204 No Content
서버가 요청을 성공적으로 수행했지만, 응답 페이로드 본문에 보낼 데이터가 없다는걸 의미한다.\
예를 들어, 웹 문서 편집기에서 저장 버튼을 눌렀을 때, 해당 버튼의 결과로 아무 내용이 없어도 된다.\
즉, 결과 내용이 없더라도 204 메세지만으로 성공임을 인식할 수 있다.

## 3xx (Redirection)
요청을 완료하기 위해, 유저 에이전트(브라우저)의 추가 작업이 필요함을 나타내기 위해 전송하는 응답.
* 300 Multiple Choices
* 301 Moved Permanently
* 302 Found
* 303 See Other
* 304 Not Modified
* 307 Temporary Redirect
* 308 Permanent Redirect

### 리다이렉션
웹 브라우저는 3xx 응답의 결과에 Location 헤더가 있으면, Location 위치로 자동 이동한다. (Redirect)\
다음과 같은 예시를 살펴 보자.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/321fe450-6a8c-447d-aaa4-77fec66459d2)

이벤트 페이지의 주소가 /event에서 /new-event로 변경되었다고 가정해보자. \
유저가 /event 페이지로 들어오게 되면, 서버에선 `301 Moved Permanently`와 함께, 변경된 주소가 반환된다.\
이를 통해 유저는 /event 페이지로 들어오더라도, 정상적으로 변경된 이벤트 페이지로 이동하게 된다.


### 리다이렉션의 종류
* **영구 리다이렉션** - 특정 리소스의 URI가 영구적으로 이동
  * 예) /members -> /users
  * 예) /event -> /new-event
* **일시 리다이렉션** - 일시적인 변경
  * 주문 완료 후 주문 내역 화면으로 이동
  * PRG: Post/Redirect/Get
* **특수 리다이렉션**
  * 결과 대신 캐시를 사용

### 영구 리다이렉션 - 301, 308
* 리소스의 URI가 영구적으로 이동한다.
* 원래의 URL를 사용X, 검색 엔진 등에서도 변경 인지한다.
* **301 Moved Permanently**
  * ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/cbdcc531-741c-4f04-bbb0-d242e35684f3)
  * **리다이렉트시 요청 메서드가 GET으로 변하고, 본문이 제거될 수 있음(MAY)**

* **308 Permanent Redirect**
  * ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/bd93ec16-4c0e-4e13-a51b-808b7f313401)
  * 301과 기능은 같음
  * **리다이렉트시 요청 메서드와 본문 유지(처음 POST를 보내면 리다이렉트도 POST 유지)**


### 일시적인 리다이렉션
* 리소스의 URI가 일시적으로 변경
* 따라서 검색 엔진 등에서 URL을 변경하면 안됨
* **302 Found**
  * **리다이렉트시 요청 메서드가 GET으로 변하고, 본문이 제거될 수 있음(MAY)**
* **307 Temporary Redirect**
  * 302와 기능은 같음
  * **리다이렉트시 요청 메서드와 본문 유지(요청 메서드를 변경하면 안된다. MUST NOT)**
* **303 See Other**
  * 302와 기능은 같음
  * **리다이렉트시 요청 메서드가 GET으로 변경**


### PRG: Post/Redirect/Get, 일시적인 리다이렉션 예시
* POST로 주문 후, 웹 브라우저를 새로고침 하면?
* 새로고침은 **다시 요청**을 의미하므로, 중복 주문이 될 수 있다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/be2a80ae-754a-40cf-93fe-4e0fd39197b8)

이렇게 원치 않은 동작을 방지하기 위해, PRG를 사용한다.
* POST로 주문후에 새로 고침으로 인한 중복 주문 방지
* POST로 주문후에 주문 결과 화면을 GET 메서드로 리다이렉트
* 새로고침해도 결과 화면을 GET으로 조회
* 중복 주문 대신에 결과 화면만 GET으로 다시 요청

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/d2e654f7-64d4-42aa-8988-acd6ed0f1679)

RPG 이후, 리다이렉트해 URL이 이미 POST -> GET으로 리다이렉트 된다.\
따라서, 새로고침 하더라도 GET으로 결과 화면만 조회한다.



### 그래서, 일시적인 리다이렉션에선 뭘 써야 하는가?
* **정리**
  * 302 Found -> GET으로 변할 수 있음
  * 307 Temporary Redirect -> 메서드가 변하면 안됨
  * 303 See Other -> 메서드가 GET으로 변경
* **역사**
  * 처음 302 스펙의 의도는 HTTP 메서드를 유지하는 것
  * 그런데 웹 브라우저들이 대부분 GET으로 바꾸어버림(일부는 다르게 동작)
  * 그래서 모호한 302를 대신하는 명확한 307, 303이 등장함(301 대응으로 308도 등장)
* **현실**
  * 307, 303을 권장하지만 현실적으로 이미 많은 애플리케이션 라이브러리들이 302를 기본값으로 사용
  * 자동 리다이렉션시에 GET으로 변해도 되면 그냥 302를 사용해도 큰 문제 없음



### 기타 리다이렉션
* 300 Multiple Choices: 안쓴다.
* **304 Not Modified**
  * 캐시를 목적으로 사용
  * 클라이언트에게 리소스가 수정되지 않았음을 알려준다. 따라서 클라이언트는 로컬PC에 저장된 캐시를 재사용한다. (캐시로 리다이렉트 한다.)
  * 304 응답은 응답에 메시지 바디를 포함하면 안된다. (로컬 캐시를 사용해야 하므로)
  * 조건부 GET, HEAD 요청시 사용


## 4xx Client Error
**오류의 원인이 클라이언트에 있을 때** 발생하는 오류.\
클라이언트의 요청에 잘못된 문법 등으로 서버가 요청을 수행할 수 없다.

! 클라이언트가 이미 잘못된 요청, 데이터를 보내고 있기 때문에, 똑같은 재시도가 실패한다.

### 400 Bad Request
클라이언트가 잘못된 요청을 해서, 서버가 요청을 처리할 수 없다.
* 요청 구문, 메시지 등등 오류
* 클라이언트는 요청 내용을 다시 검토하고, 보내야함
* 예) 요청 파라미터가 잘못되거나, API 스펙이 맞지 않을 때

### 401 Unauthorized
클라이언트가 해당 리소스에 대한 인증이 필요하다.
* 인증(Authentication) 되지 않음
* 401 오류 발생시 응답에 WWW-Authenticate 헤더와 함께 인증 방법을 설명
* 참고
  * 인증(Authentication): 본인이 누구인지 확인, (로그인)
  * 인가(Authorization): 권한부여 (ADMIN 권한처럼 특정 리소스에 접근할 수 있는 권한, 인증이 있어야 인가가 있음)
  * 오류 메시지가 Unauthorized 이지만 인증 되지 않음 (이름이 아쉬움)


### 403 Forbidden
서버가 요청을 이해했지만, 승인을 거부했음.
* 주로 인증 자격 증명은 있지만, 접근 권한이 불충분한 경우.
* ex) 어드민 등급이 아닌 사용자가 로그인 했지만, 어드민 등급의 리소스에 접근하는 경우

### 404 Not Found
요청 리소스를 찾을 수 없다.
* 요청 리소스가 서버에 없음
* 또는, 클라이언트가 권한이 부족한 리소스에 접근할 때 해당 리소스를 숨기고 싶을 때


## 5xx (Server Error)
서버 문제로 오류가 발생할 때 발생하는 오류.\
서버에 문제가 있기 때문에, 재시도하면 요청에 성공할 수도 있다.

### 500 Internal Server Error
서버 문제로 오류가 발생했을 때, 애매하면 500 오류.

### 503 Service Unavaiiable
서버가 일시적인 과부하, 또는 예정된 작업으로 잠시 서비스 이용이 불가능할 때 발생한다.\
Retry-After 헤더 필드로 얼마 뒤에 복구되는지 보낼 수도 있다.


## HTTP 헤더
* header-field = field-name ":" OWS field-value OWS (OWS:띄어쓰기 허용)
* 이 때, field-name은 대소문자 구분이 없다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/5a500bc4-65ef-497d-85c0-51a0407a69cf)

### HTTP 헤더의 용도
* HTTP 전송에 필요한 모든 부가 정보가 들어간다.
* ex) 메세지 바디의 내용, 크기, 압축, 인증 등..
* 필요한 경우, 임의의 헤더도 추가가 가능하다.


### HTTP 헤더 분류 - RFC2616(과거)
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/5ee8f080-2b70-4480-b607-b30b8f38be1f)

* **General 헤더**: 메시지 전체에 적용되는 정보, 예) Connection: close
* **Request 헤더**: 요청 정보, 예) User-Agent: Mozilla/5.0 (Macintosh; ..)
* **Response 헤더**: 응답 정보, 예) Server: Apache
* **Entity 헤더**: 엔티티 바디 정보, 예) Content-Type: text/html, Content-Length: 3423

### HTTP 메세지 본문 - RFC 2616(과거)
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/0c6353c4-929c-43b7-bd1d-f58dddeef2d2)

* 메시지 본문(message body)은 엔티티 본문(entity body)을 전달하는데 사용
* 엔티티 본문은 요청이나 응답에서 전달할 실제 데이터
* **엔티티 헤더**는 **엔티티 본문**의 데이터를 해석할 수 있는 정보 제공
  * 데이터 유형(html, json), 데이터 길이, 압축 정보 등등
 
위와 같은 이러한 RFC2616 표준이 폐기되고, 2014년 RFC7230 ~ 7235가 등장한다.

### RFC723x 변화
* 엔티티(Entity) -> 표현(Representation)
* Representation = representation Metadata + Representation Data
* 표현 = 표현 메타데이터 + 표현 데이터

### HTTP 메세지 본문 - RFC7230
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/42a9c614-5f17-4021-b7aa-8a18e0951536)

* 메시지 본문(message body)을 통해 표현 데이터 전달
* 메시지 본문 = 페이로드(payload)
* **표현**은 요청이나 응답에서 전달할 실제 데이터
* **표현 헤더**는 **표현 데이터를 해석할 수 있는 정보 제공**
  * 데이터 유형(html, json), 데이터 길이, 압축 정보 등등
* 참고: 표현 헤더는 표현 메타데이터와, 페이로드 메시지를 구분해야 하지만, 여기서는 생략

## 표현
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/8d606586-335a-4632-a098-81f785d1835f)

* Content-Type: 표현 데이터의 형식
* Content-Encoding: 표현 데이터의 압축 방식
* Content-Language: 표현 데이터의 자연 언어
* Content-Length: 표현 데이터의 길이

표현 헤더(페이로드 헤더)는 전송, 응답 둘 다 사용한다.
### Content-Type
```http
Content-Type: text/html;charset=UTF-8
```
표현 데이터의 형식을 설명할 때 사용한다. 미디어 타입이나, 문자 인코딩 등을 작성한다.
* ex)
  * text/html; charset=utf-8
  * application/json
  * image/png

### Content-Encoding
```http
Content-Encoding: gzip
```
표현 데이터를 압축할 때 사용한다. 데이터를 전달하는 곳에서 압축 후 인코딩 헤더를 추가하고,\
데이터를 읽는 쪽에서 인코딩 헤더의 정보로 압축을 해제한다.
* ex)
  * gzip
  * deflate
  * identify

### Content-Language
```http
Content-Language: ko
```
표현 데이터의 자연 언어를 표현한다.
* ex) 
  * ko
  * en
  * en-US

### Content-Length
```http
Content-Length: 5
```
표현 데이터의 길이를 나타내며, 바이트 단위로 작성한다.\
만약 Transfer-Encoding(전송 코딩)을 사용하면 Content-Length를 사용하면 안된다.

## 협상 (콘텐츠 네고시에이션)
**클라이언트가 선호하는** 표현을 달라고 서버에게 하는 요청을 의미한다. 
* Accept: **클라이언트가 선호하는** 미디어 타입 전달
* Accept-Charset: 클라이언트가 선호하는 문자 인코딩
* Accept-Encoding: 클라이언트가 선호하는 압축 인코딩
* Accept-Language: 클라이언트가 선호하는 자연 언어

협상 헤더는 요청시에만 사용한다.

### Accept-Language 적용 전
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/1ed6aa9f-d234-4be0-8006-ba942c3eb892)

만약, 우리가 한국어 브라우저를 사용한다고 가정하자. \
서버는 영어와 한국어를 지원하지만, 클라이언트의 요청에 따로 언어에 대한 설정이 없기 때문에\
기본 서버 설정인 영어를 반환한다.

### Accept-Language 적용 후
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/be0917b8-5ff0-41c1-a097-91be135e2b65)

위와 동일한 상황이지만, `Accept-Language` 헤더를 추가했다.\
이를 통해 서버는 클라이언트가 선호하는 언어가 한국어임을 알 수 있고,\
한국어를 반환하게 된다.

다만, 클라이언트가 원하는 언어를 서버에서 제공하지 않을 때를 생각해보자.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/96faddc2-7053-48f4-8445-e289a26966a3)

서버는 독일어와 영어를 지원하지만, 클라이언트가 원하는 언어인 한국어는 제공하지 않고 있다.\
따라서 서버는 기본 설정인 독일어를 반환한다.

하지만, 클라이언트가 **그나마 읽을 수 있는 언어**인 영어를 반환 받기 위해, 우선 순위를 넣어줄 수 있다.

### 협상과 우선 순위, Quality Values
```http
GET /event
Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7
```
* Quality Values(q) 값 사용
* 0~1, 클수록 높은 우선순위
* 생략하면 1
* `Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7`
  * 1. ko-KR;q=1 (q생략)
  * 2. ko;q=0.9
  * 3. en-US;q=0.8
  * 4. en;q=0.7

이를 통해, 위 독일어를 반환 받는 상황에서 다음과 같이 영어를 반환받을 수 있다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/29226ba3-df31-4575-a02e-ad3187dbbe7f)


### 협상과 우선순위 2, Quality Values
```http
GET /event
Accept: text/*, text/plain, text/plain;format=flowed, */*
```
구체적인 것이 우선한다. 즉, 디테일하게 적은 것이 우선된다.
* `Accept: text/*, text/plain, text/plain;format=flowed, */*`
  * 1. text/plain;format=flowed
  * 2. text/plain
  * 3. text/*
  * 4. */*

### 협상과 우선순위 3, Quality Values
* 구체적인 것을 기준으로 미디어 타입을 맞춘다.
* `Accept: text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5`

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/e02f3c93-1b95-4589-8b93-04fea659e7b6)



## 전송 방식
* 단순, 압축, 분할, 범위 전송으로 나뉜다.
### 단순 전송
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/22559e45-baa7-4026-b08a-9ccc936f309c)

Content에 대한 길이를 알 때, 해당 Content의 길이를 받는 것.

### 압축 전송
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/f0f69f42-5495-4cc8-8238-35675145fbb7)

Content를 압축하여 전송하는 방식. 이 때, 어떤 방식으로 압축되었는지 헤더에 넣어주어야 한다.

### 분할 전송
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/36d19d80-0282-4342-a114-e63f07fb5aab)

나누어, 분할하여 전송하는 방법. 이 땐 Content-Length를 넣으면 안된다.

### 범위 전송
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/39160a01-7a31-4e2e-aaef-0acce19cffbf)

받고자 하는 데이터의 범위를 나누어, 서버에게 요청하는 방식.

## 일반 정보
* From: 유저 에이전트의 이메일 정보
* Referer: 이전 웹 페이지 주소
* User-Agent: 유저 에이전트 애플리케이션 정보
* Server: 요청을 처리하는 오리진 서버의 소프트웨어 정보
* Date: 메시지가 생성된 날짜

### From
유저 에이전트의 이메일 정보를 의미한다. 일반적으로는 잘 사용하지 않고, 검색 엔진 같은곳에서 주로 사용한다.

### Referer
현재 요청된 페이지의 이전 웹 페이지 주소를 의미한다.\
A -> B로 이동하는 경우, B를 요청할 때 Referer: A를 포함해서 요청한다.

Referer를 사용하면 유입 경로를 분석할 수 있고, 요청에서 사용한다.

주) referer는 단어 referrer의 오타. 

### User-Agent
유저 에이전트 애플리케이션 정보를 나타낸다.
```
user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/
537.36 (KHTML, like Gecko) Chrome/86.0.4240.183 Safari/537.36
```

* 클라이언트의 애플리케이션 정보(웹 브라우저 정보, 등등)
* 통계 정보를 뽑을 수 있고, 어떤 종류의 브라우저에서 장애가 발생하는지 파악 가능
* 요청에서 사용

### Server
요청을 처리하는 ORIGIN 서버의 소프트웨어 정보를 의미한다.
```
Server: Apache/2.2.22 (Debian)
```
응답에서 사용한다.

### Date
메세지가 발생한 날짜와 시간을 의미한다.
```
Date: Tue, 15 Nov 1994 08:12:31 GMT
```
응답에서 사용한다.

## 특별한 정보
* Host: 요청한 호스트 정보(도메인)
* Location: 페이지 리다이렉션
* Allow: 허용 가능한 HTTP 메서드
* Retry-After: 유저 에이전트가 다음 요청을 하기까지 기다려야 하는 시간

### Host
```
GET /search?q=hello&hl=ko HTTP/1.1
Host: www.google.com
```
요청한 호스트 정보(도메인)을 의미한다.\
요청에서 사용하며, **필수**이다.

* 하나의 서버가 여러 도메인을 처리해야 할 때
* 하나의 IP 주소에 여러 도메인이 적용되어 있을 때

다음과 같이, 가상 호스트를 통해 여러 도메인을 한번에 처리할 수 있는 서버가 있다고 가정해보자.\
이 때, 실제 애플리케이션이 여러개 구동될 수 있다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/3cecc60c-5d2c-4e5f-b952-328ab9390737)

이 때 클라이언트가 서버에 요청을 전송한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/0ff24a98-ece5-4647-870f-83060698b825)

서버와 클라이언트는 IP로만 통신하기 때문에, aaa, bbb, ccc 셋 중 어떤 도메인에서 처리해야할 지 알 수 없다.\
이러한 문제를 해결하기 위해, `Host` 헤더를 반드시 추가해야 한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/f5c1ad04-e93e-45c7-a91a-22dff1dab085)


### Location
페이지 리다이렉션을 위해 사용한다.\
웹 브라우저는 3xx 응답 결과에 Location 헤더가 있으면, Location 위치로 리다이렉트 한다.
* 201 (Created): Location 값은 요청에 의해 생성된 리소스 URI
* 3xx (Redirection): Location 값은 요청을 자동으로 리디렉션하기 위한 대상 리소스를 가리킴

### Allow
허용 가능한 HTTP 메소드를 나타낸다. 405 (Method Not Allowed)에서 응답에 포함해야 한다.

### Retry-After
유저 에이전트가 다음 요청을 하기까지 기다려야 하는 시간을 나타낸다.\
503 (Service Unavailable): 서비스가 언제까지 불능인지 알려줄 수 있다.

## 인증
* Authorization: 클라이언트 인증 정보를 서버에 전달
* WWW-Authenticate: 리소스 접근시 필요한 인증 방법 정의

### Authorization
```
Authorization: Basic xxxxxxxxxxxxxxxx
Authorization: bearer xxxxxxxxxxxxxxxx
```
클라이언트의 인증 정보를 서버에 전달할 때 사용한다.

### WWW-Authenticate
```
WWW-Authenticate: Newauth realm="apps", type=1, title="Login to \"apps\"", Basic realm="simple"
```

리소스 접근시 필요한 인증 방법을 정의할 때 사용한다. 401 Unauthorized 응답과 함께사용한다.


## 쿠키
* Set-Cookie: 서버에서 클라이언트로 쿠키 전달(응답)
• Cookie: 클라이언트가 서버에서 받은 쿠키를 저장하고, HTTP 요청시 서버로 전달

### 쿠키 미사용
* 처음 welcome 페이지 접근 시\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/b5cb7064-bb57-4733-8bca-6853d13cff75)

* 로그인\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/3ebac7d4-6aa1-4f2c-9b80-a714d24d6324)

* 로그인 이후, welcome 페이지 접근\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/4b8ad47a-1fcb-448e-843e-f703a5d521d7)

이전에도 학습했듯, HTTP는 **무상태(Stateless)** 프로토콜이다. \
클라이언트와 서버가 요청과 응답을 주고 받게 되면, 연결이 끊어진다.\
즉, 클라이언트가 다시 요청하면 서버는 이전 요청을 기억하지 못한다.

이러한 문제점을 해결하기 위해선 요청에 사용자 정보를 포함해서 전송해야 한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/5688bdb3-284e-4c74-a351-31441d8596d0)

하지만, 이러한 방법을 사용한다면 **모든 요청**에 사용자 정보가 포함되도록 개발해야 한다.

그럼, 쿠키를 사용하면 어떻게 될까?

### 쿠키 사용
* 로그인\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/a2ba22d2-056d-491e-a1a0-66e5b3d83ae0)

* 로그인 이후, welcome 페이지 접근\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/4ceac7b9-031b-4809-b08d-8f2398f54e26)

* 모든 요청에 쿠키 정보를 자동으로 포함한다.\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/1d384992-245d-4370-8533-9858c420dbb6)


### 쿠키
```
set-cookie: sessionId=abcde1234; expires=Sat, 26-Dec-2020 00:00:00 GMT; path=/; domain=.google.com; Secure
```
쿠키는 주로 사용자 로그인 세션 관리, 혹은 광고 정보 트래킹을 위해 사용된다.

쿠키 정보는 항상 서버에 전송되기 떄문에, 네트워크 트래픽을 추가로 유발한다.\
따라서 **최소한의 정보**만 사용해야 한다.(세선 id, 인증 토큰 등.)\
만약 서버에 전송하지 않고, 웹 브라우저 내부에 데이터를 저장하고 싶다면 **웹 스토리지**를 사용해야 한다.

> 주의! 보안에 민감한 데이터는 저장해서는 안된다.


### 쿠키 - 생명 주기
```
Set-Cookie: expires=Sat, 26-Dec-2020 04:39:21 GMT
```
expires는 **만료일**을 의미하며, 만료일이 되면 쿠키가 삭제된다.

```
• Set-Cookie: max-age=3600 (3600초)
```
max-age를 사용하면 쿠키의 유효 기간을 지정한다.

* 세션 쿠키: 만료 날짜를 생략하면 브라우저 종료시 까지만 유지
* 영속 쿠키: 만료 날짜를 입력하면 해당 날짜까지 유지

### 쿠키 - 도메인
```
domain=example.org
```
* 명시 : 명시한 문서 기준 도메인과, 해당 서 도메인에 포함되어 전송.
  * example.org는 물론이고
  * dev.example.org도 쿠키 접근 가능.
* 생략 : 현재 문서 기준 도메인에만 전송한다.
  * example.org 에서 쿠키를 생성하고 domain 지정을 생략
    * example.org 에서만 쿠키 접근
    * dev.example.org는 쿠키 미접근


### 쿠키 - 경로
```
path=home
```
이 경로를 포함한 **하위 경로 페이지**만 쿠키에 접근할 수 있다. 일반적으로, `path=/` 루트로 지정한다.
* ex) path=/home 지정
  * /home -> 가능
  * /home/level1 -> 가능
  * /home/level1/level2 -> 가능
  * /hello -> 불가능


### 쿠키 - 보안
Secure, HttpOnly, SameSite 세 가지가 존재한다.
* Secure
  * 쿠키는 http, https를 구분하지 않고 전송한다.
  * Secure를 적용하면 https인 경우에만 전송한다.
* HttpOnly
  * XSS 공격 방지
  * 자바스크립트에서 접근 불가(document.cookie)
  * HTTP 전송에만 사용
* SameSite
  * XSRF 공격 방지
  * 요청 도메인과 쿠키에 설정된 도메인이 같은 경우만 쿠키 전송
 

## 캐시
### 캐시가 없을 때
* 첫번째 요청\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/f8300a10-d884-4355-af11-387b792a0120)

* 두번째 요청\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/45b20e27-09a0-4ebd-b63f-a53ef922b9bd)


이렇게, 캐시가 없다면 데이터가 변경되지 않더라도 계속해서 네트워크를 통해 데이터를 받아야 한다.\
인터넷 네트워크는 클라이언트의 하드웨어에 비해 매우 느리고 비싸다!

따라서 브라우저 로딩 속도가 느려지므로, 느린 사용자 경험으로 이어진다.

### 캐시 적용
* 첫 번째 요청\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/43aaa396-a05f-4050-83c7-cdb07103f9c1)

위와 동일하게 별 이미지를 반환 받았지만, 이번엔 서버 응답 메세지에 다음과 같은 헤더가 추가되었다.
```http
...
cache-control: max-age=60
...
```
해당 헤더를 통해 브라우저는 받아온 별 이미지를 브라우저 캐시에 60초간 저장한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/5d44c48a-4e77-40dc-97da-39744b0d5e98)


* 두 번째 요청\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/b8b58bb4-8bdb-41ea-b725-cc33d9a5d32c)

두 번째 요청 시, 우선 브라우저 캐시를 살펴보아 유효한 캐시가 존재하는지 확인하고, \
캐시에서 바로 이미지를 반환한다.

이러한 캐시 적분에, 캐시 가능 시간동안 네트워크를 사용하지 않아도 된다.\
이를 통해, 비싼 네트워크 사용량을 줄이고, 브라우저 로딩 속도를 증가시킬 수 있다.

### 캐시 시간 초과
앞서 우리는 60초 유효 시간을 가진 캐시를 저장했었다.\
따라서, 만약 60초가 지나게 되면 해당 캐시는 유효기간이 지나, 우리는 다시 서버에서 이미지를 받아와야 한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/b2928956-5ccd-4508-8fd2-e37bc1a27bb4)

캐시 유효 기간이 초과했기 때문에 서버를 통해 데이터를 다시 조회하고, 캐시를 갱신한다.\
이 때 다시 네트워크 다운로드가 발생한다.

근데, 이미지는 동일한데 굳이 다시 다운로드를 받아와야 할 필요가 있을까?

## 검증 헤더와 조건부 요청 1
### 캐시 시간 초과
캐시 유효 기간을 초과해서 서버에 요청하면, 다음 두 가지 상황이 발생한다.
1. 서버에서 기존 데이터를 변경한다. `노란색 별` -> `녹색 별`
2. 서버에서 기존 데이터를 변경하지 않았다. `노란색 별 유지`

캐시 만료 후에도 서버에 데이터를 변경하지 않는 경우를 생각해보자.\
생각해보면, 데이터를 전송하는 대신, 저장해두었던 캐시를 재사용할 수 있다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/1b69b156-acbd-4d05-a83c-a51994c2df35)

하지만 이 때, 클라이언트의 데이터와 서버의 데이터가 같다는 사실을 확인할 수 있는 방법이 필요하다.


### 검증 헤더 추가
#### 첫 번째 요청
브라우저에서 첫 번째 요청 시, 서버에선 캐시 유효 기간과 **Last Modified, 최종 수정일**을 반환할 수 있다.
```http
...
cache-control: max-age=60
Last-Modified: 2020년 11월 10일 10:00:00
...
```
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/3863e7b5-fde6-4988-a88e-72c0267b1467)

이 응답 결과 역시, 캐시에 저장된다.\
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/21325662-0f7a-434e-94d8-32c39a625806)


#### 두 번째 요청, 캐시 시간 초과
두 번째 요청 시 캐시에 데이터 최종 수정일이 저장되어 있다면, 요청 시 `if-modified-since` 헤더가 추가된다.\
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/5330b4c7-eb71-4d0c-9b6d-e6151ac247c4)

이 헤더를 통해 캐시의 이미지와 서버의 이미지를 비교한다.\
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/9155a23b-1b96-4b3b-bfe5-13e714e32700)

이미지가 수정되지 않아 이미지를 전송해줄 필요가 없다면, 서버는 `304 Not Modified` 응답 결과를 반환한다.\
단, 이 때 **HTTP Body**가 없는걸 볼 수 있다.\
```http
HTTP/1.1 304 Not Modified
Content-Type: image/jpeg
cache-control: max-age=60
Last-Modified: 2020년 11월 10일 10:00:00
Content-Length: 34012

```
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/6ae54c36-03a5-40a2-ad36-8a35e2c23524)



### 검증 헤더와 조건부 요청 정리
캐시 유효 시간이 초과해도, 서버의 데이터가 갱신되지 않으면 304 Not Modified + 헤더 메타 정보만 응답한다.(바디X)\
클라이언트는 서버가 보낸 응답 헤더 정보로 캐시의 메타 정보를 갱신하고, \
클라이언트는 캐시에 저장되어 있는 데이터를 재활용한다.

결과적으로 네트워크 다운로드가 발생하지만 용량이 적은 헤더 정보만 다운로드한다.\
매우 실용적인 해결책!


## 검증 헤더와 조건부 요청 2
### 검증 헤더와 조건부 요청
* **검증 헤더**
  * 캐시 데이터와 서버 데이터가 같은지 검증하는 데이터
  * Last-Modified , ETag
* **조건부 요청 헤더**
  * 검증 헤더로 조건에 따른 분기
  * If-Modified-Since: Last-Modified 사용
  * If-None-Match: ETag 사용
  * 조건이 만족하면 200 OK
  * 조건이 만족하지 않으면 304 Not Modified


### 예시
* If-Modified-Since: 이후에 데이터가 수정되었나요? 
  * **데이터 미변경 예시**
    * 캐시: 2020년 11월 10일 10:00:00 vs 서버: 2020년 11월 10일 10:00:00
    * **요청 자체는 실패이므로, 200 OK 가 아니다!**
    * **304 Not Modified**, 헤더 데이터만 전송(BODY 미포함)
    * 전송 용량 0.1M (헤더 0.1M)
  * **데이터 변경 예시**
    * 캐시: 2020년 11월 10일 10:00:00 vs 서버: 2020년 11월 10일 11:00:00
    * 200 OK, 모든 데이터 전송(BODY 포함)
    * 전송 용량 1.1M (헤더 0.1M, 바디 1.0M)

### Last-Modified, If-Modified-Since 단점
* 1초 미만(0.x초) 단위로 캐시 조정이 불가능
* 날짜 기반의 로직 사용
* 데이터를 수정해서 날짜가 다르지만, 같은 데이터를 수정해서 데이터 결과가 똑같은 경우
  * A 데이터 -> B 데이터 -> A 데이터로 변경.
* 서버에서 별도의 캐시 로직을 관리하고 싶은 경우
  * 예) 스페이스나 주석처럼 크게 영향이 없는 변경에서 캐시를 유지하고 싶은 경우

### ETag(Entity Tag), If-None-Match
* 캐시용 데이터에 임의의 고유한 버전 이름을 달아둔다.
  * 예) ETag: "v1.0", ETag: "a2jiodwjekjl3"
* 데이터가 변경되면 이 이름을 바꾸어서 변경함(Hash를 다시 생성)
  * 예) ETag: "aaaaa" -> ETag: "bbbbb"
* 진짜 단순하게 ETag만 보내서 같으면 유지, 다르면 다시 받기!

#### 예시
* 첫번째 요청\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/f6473312-f8c6-4574-a2ed-a79fcb61bfa8)\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/44e2d7ae-18a0-4c72-9f14-aa7ff0ac5aff)
  

* 두번째 요청 - 캐시 시간 초과\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/190d6a10-24db-4004-afce-da41ccaed328)\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/7676687f-fa69-41fa-a57a-a7887d7fc65d)\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/a22a96cd-5b3f-4ba9-98e1-d6fffbbb97ec)\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/f6cfbaaa-8f66-4a5a-9bc7-f51fd4b701f4)\
  ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/6abcb725-05f1-4b8b-a540-d92fca0db9a0)


### ETag, If-None-Match 정리
* 진짜 단순하게 ETag만 서버에 보내서 같으면 유지, 다르면 다시 받기!
* **캐시 제어 로직을 서버에서 완전히 관리**
* 클라이언트는 단순히 이 값을 서버에 제공(클라이언트는 캐시 메커니즘을 모름)
* 예)
  * 서버는 배타 오픈 기간인 3일 동안 파일이 변경되어도 ETag를 동일하게 유지
  * 애플리케이션 배포 주기에 맞추어 ETag 모두 갱신

## 캐시와 조건부 요청 헤더
### 캐시 제어 헤더
* Cache-Control: 캐시 제어
* Pragma: 캐시 제어(하위 호환)
* Expires: 캐시 유효 기간(하위 호환)

### Cache-Control, 캐시 지시어(directives)
* Cache-Control: max-age
  * 캐시 유효 시간, 초 단위
* Cache-Control: no-cache
  * 데이터는 캐시해도 되지만, 데이터를 사용하기 전 **항상 원(origin) 서버에 검증하고 사용**
* Cache-Control: no-store
  * 데이터에 민감한 정보가 있으므로 저장하면 안됨\
    (메모리에서 사용하고 최대한 빨리 삭제)

### Pragma - 캐시 제어 (하위 호환)
* Pragma: no-cache
* HTTP 1.0 하위 호환

### Expires - 캐시 만료일 지정 (하위호환)
```http
expires: Mon, 01 Jan 1990 00:00:00 GMT
```
* 캐시 만료일을 정확한 날짜로 지정한다.
* HTTP 1.0 부터 사용한다.
* 지금은 더 유연한 Cache-Control: max-age 권장
* Cache-Control: max-age와 함께 사용하면 Expires는 무시된다.

### 검증 헤더와 조건부 요청 헤더
* **검증 헤더 (Validator)**
  * `ETag: "v1.0", ETag: "asid93jkrh2l"`
  * `Last-Modified: Thu, 04 Jun 2020 07:19:24 GMT`
* **조건부 요청 헤더**
  * If-Match, If-None-Match: ETag 값 사용
  * If-Modified-Since, If-Unmodified-Since: Last-Modified 값 사용

## 프록시 캐시
한국에 있는 여러 클라이언트가 미국에 있는 원 서버에 접근한다고 가정해보자.\
이 때 접속 시간은 0.5초로, 이미지를 받기 위해 0.5초씩 대기해야만 한다.\
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/aa30d746-19ac-4a69-8ab3-fa17bde9e1fe)

이를 해결하기 위해, **한국 어딘가에 중간 다리, 프록시 서버**를 추가한다.\
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/b586c54a-9a92-4e5c-918f-45841e8b28bb)

이를 통해 서버 응답 속도를 증진시킬 수 있다.\
이 때, 웹 브라우저마다 저장되는 캐시를 private 캐시, 프록시 캐시 서버의 캐시를 public 캐시라고 한다.\
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/036842d6-7ad0-4897-a17e-0e429b6b8bc2)


### Cache-Control
* **Cache-Control: public**
  * 응답이 public 캐시에 저장되어도 됨
* **Cache-Control: private**
  * 응답이 해당 사용자만을 위한 것임, private 캐시에 저장해야 함(기본값)
* **Cache-Control: s-maxage**
  * 프록시 캐시에만 적용되는 max-age
* **Age: 60** (HTTP 헤더)
  * 오리진 서버에서 응답 후 프록시 캐시 내에 머문 시간(초)
   
## 캐시 무효화
웹 브라우저가 임의로 캐시를 하는 경우도 존재하기 때문에, 이를 막기 위해 사용한다.
### Cache-Control
```http
Cache-Control: no-cache, no-store, must-revalidate
```
* **Cache-Control: no-cache**
  * 데이터는 캐시해도 되지만, **항상 원 서버에 검증하고 사용**(이름에 주의!)
* Cache-Control: no-store
  * 데이터에 민감한 정보가 있으므로 저장하면 안됨\
    (메모리에서 사용하고 최대한 빨리 삭제)
* **Cache-Control: must-revalidate**
  * 캐시 만료후 최초 조회시 **원 서버에 검증**해야함
  * 원 서버 접근 실패시 반드시 오류가 발생해야함 - 504(Gateway Timeout)
  * must-revalidate는 캐시 유효 시간이라면 캐시를 사용함

```http
Pragma: no-cache
```
• Pragma는 HTTP 1.0 하위 호환

### no-cache vs must-revalidate
#### no-cache 기본 동작
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/541acb03-a2b7-44da-9698-344803df83c1)

no-cache이기 떄문에, 원서버로 요청을 넘겨, 원서버에서 응답을 받아오는걸 볼 수 있다.\
하지만, 만약 **원서버에 접속할 수 없는 경우**는 어떻게 될까?

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/099e26f6-a694-453f-8206-0ec5a7f3de81)

이러한 경우, 오류가 나는 것보단 이전의 데이터를 보내주는 것이 나으므로, 프록시 캐시에서 응답할 수 있다.

#### must-revalidate
하지만 must-revalidate에선 위와 같은 네트워크 단절 상황에서, 반드시 504 오류가 발생해야 한다.\
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/02e8a526-e8f2-4c78-974e-9be3dbd10236)



