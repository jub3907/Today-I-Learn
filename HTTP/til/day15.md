## 인터넷 통신
클라이언트는 인터넷 속 수 많은 노드들을 거쳐, 서버에 도달해야만 한다.

![image](https://github.com/jub3907/outSourcing/assets/58246682/418a0dd1-356e-422d-a8c5-af47bfc31b19)

이러한 복잡한 인터넷 망에서, 클라이언트는 어떻게 서버를 구별하여 "Hello, World"라는 메세지를 전달할 수 있을까?
이 때 사용하는 최소한의 규칙이 바로 **IP 주소**이다.

## IP(인터넷 프로토콜)
![image](https://github.com/jub3907/outSourcing/assets/58246682/032d3b3d-a960-49b8-a54e-f86cfe7546ca)

### IP의 역할
* 지정한 IP 주소(IP Address)에 데이터를 전달
* 패킷(Packet)이라는 통신 단위로 데이터 전달

### IP 패킷 정보
![image](https://github.com/jub3907/outSourcing/assets/58246682/12a53163-68dc-4ef6-8e8e-739493a96fff)

"Hello World"라는 메세지를 전송하기 위해, 현재 내 IP와 목적지의 IP 주소값을 이용해 패킷을 생성한다. 

클라이언트에선 생성된 패킷을 인터넷망에 보내, 각 노드끼리 목적지 IP를 받을 수 있도록 패킷을 전송하여 서버에 도달하게 된다.

![image](https://github.com/jub3907/outSourcing/assets/58246682/cbe3716c-94d7-4e9e-a231-52518c2f5a60)

서버 또한 패킷을 생성해, 클라이언트에 정보를 전송할 수 있다.

![image](https://github.com/jub3907/outSourcing/assets/58246682/db99ac16-5a0a-4934-b411-3f85d19de4fd)

그림에서 볼 수 있듯, 클라이언트에서 서버로 전송하는 경로와, 서버에서 클라이언트로 전송하는 경로는 서로 다를 수 있다.

### IP 프로토콜의 한계
* 비 연결성
  * 패킷이 받을 대상이 없거나, 서비스 불능 상태여도 패킷을 전송한다.
* 비 신뢰성
  * 중간에 패킷이 사라질 수 있음.
  * 패킷을 여러개 전송했을 때, 순서대로 도착하지 않을 수 있음.
* 프로그램 구분
  * 같은 IP를 사용하는 서버에서 통신하는 애플리케이션이 둘 이상이라면?
  * ex) 하나의 PC에서 게임을 하면서 유튜브를 본다면?
 
#### 전송 대상이 서비스 불능일 때, 패킷을 전송하는 경우
![image](https://github.com/jub3907/outSourcing/assets/58246682/805f5de3-870e-457c-9ae1-c4d6962c604e)

클라이언트는 대상 서버가 패킷을 받을 수 있는 상태인지, 아닌지 모르고, 패킷을 전송하게 된다. 이 경우, 인터넷 망을 타고 패킷이 전송되지만, 정작 서버에선 패킷을 받을 수 없다.

![image](https://github.com/jub3907/outSourcing/assets/58246682/a6e36d7b-7412-46df-a443-a26529866284)

#### 패킷 소실
![image](https://github.com/jub3907/outSourcing/assets/58246682/c9a8a093-ee89-4461-8d6c-b6b6eb3dc3b9)

패킷을 전송하던 도중, 인터넷 노드 하나에 문제가 발생해 패킷 전송이 소실된다 하더라도 클라이언트는 알 수 없다.

#### 패킷 전달 순서 문제 발생
![image](https://github.com/jub3907/outSourcing/assets/58246682/10455d4e-97e3-4058-a2d4-fecc8d7051fa)

메세지가 1500바이트가 넘게 된다면, 일반적으로 패킷을 분할하여 끊어서 보내게 된다. 여러 개의 패킷을 전송하게 되면 각각 인터넷 노드를 타면서 전송한 패킷의 순서가 뒤바뀔 수 있다.

* 위와같은 여러 가지 문제점을 해결하기 위해 나온 것이 바로 **TCP** 이다.


## TCP, Transmission Control Protocol
인터넷 프로토콜 스택의 4계층은 다음과 같다.

![image](https://github.com/jub3907/outSourcing/assets/58246682/0581eb37-4ab8-46a8-8ca7-e4a86adee20e)

![image](https://github.com/jub3907/outSourcing/assets/58246682/394c1a7e-2184-4bd7-b62b-b7146fafcfdd)

위와 같은 구조에서, Hello World라는 메세지를 전송하고 싶다면 다음과 같은 과정을 거친다.

![image](https://github.com/jub3907/outSourcing/assets/58246682/a8b6f3b3-fd4c-4c90-bc8b-cda1e44a0fc0)

1. 소켓 라이브러리를 통해 OS 계층에 Hello 메세지를 전달한다.
2. OS 계층에서 TCP가 Hello라는 메세지에 TCP 정보를 추가한다. ( 그림 상의 녹색 )
3. IP 계층에서 IP와 관련된 정보를 추가한다. ( 그림 상의 노란색 )
4. 네트워크 인터페이스(랜카드)를 통해 나갈 때, 이더넷 프레임(Ethernet frame)이 포함되어 나간다. ( 그림 상의 검은색 )


### TCP/IP 패킷 정보
![image](https://github.com/jub3907/outSourcing/assets/58246682/bcc6f812-c2cf-423a-89cd-ac4c3ba88428)

TCP에는 **출발지 PORT, 목적지 PORT, 전송 제어, 순서, 검증** 과 관련된 정보들이 들어간다. 이를 통해, 순서와 같은 여러가지 문제들이 해결된다.

### TCP의 특징
TCP(Transmission Control Protocol, 전송 제어 프로토콜)이란 이름 그대로, 전송을 어떤 방식으로 할 지 제어한다.

* 연결 지향 - TCP 3 way handshake(가상 연결)
  * 상대 서버가 정상적인 상태인지 확인한 뒤, 메세지를 전송한다.
* 데이터 전달 보증
  * 메세지를 전송한 뒤, 해당 메세지가 누락되었는 지 확인 가능.
* 순서 보장

위와 같은 특성으로 인해 TCP는 신뢰할 수 있는 프로토콜이 되었고, 현재는 대부분 TCP를 사용한다.

### 3 way handshake
![image](https://github.com/jub3907/outSourcing/assets/58246682/7041fc73-1d15-4650-8154-1b3d058bf468)

1. 클라이언트에서 서버로 SYN(syncronize, 접속 요청) 메세지를 전송한다.
2. 서버에서 클라이언트로 ACK(acknowledge, 접속 요청 수락)라는 메세지를 보내면서, SYN 메세지를 전송한다.
3. 클라이언트에서 서버로 ACK 메세지를 보낸다.
4. 클라이언트에서 서버로 데이터를 전송한다. (요즘엔, 3번 과정에서 데이터를 함께 전송한다.)

위와 같은 방식으로 클라이언트와 서버가 서로 연결 되었음을 확인할 수 있고, 이러한 연결 방식을 3 way handshake라고 한다.

다만 주의해야 할 점은, 클라이언트와 서버가 실제로 연결된 것이 아니다. 서로 멀쩡한 것을 확인하고 논리적으로만 연결되었음을 확인하는 과정이다.

### 데이터 전달 보증
![image](https://github.com/jub3907/outSourcing/assets/58246682/271c0ed5-f625-41ba-a927-69e5acbc07f7)

클라이언트에서 서버로 데이터를 전송한 뒤, 서버도 데이터를 잘 받았다고 확인시켜 주므로, 데이터의 전송 여부를 확인할 수 있다.

### 순서 보장
![image](https://github.com/jub3907/outSourcing/assets/58246682/627aba87-a88b-49c7-a3d9-1a03c4a5ba6d)

예를 들어, 패킷을 1-2-3 순서로 보냈다고 가정하자. 서버에 1-**3**-2 순서로 도착하게 되었다면 서버는 클라이언트에 패킷 재전송을 요청하게 된다.

이러한 모든 특성은 TCP 패킷에 들어가는 **전송 제어, 순서, 검증 정보**들이 존재하기 때문에 가능하다.

## UDP, User Datagram Protocol
UDP는 TCP와 같은 계층에 존재하는 프로토콜로, 따로 기능이 존재하지 않는다. \
즉, TCP에서 제공하는 연결 지향(3 way handshake)나 데이터 전달 보증, 순서 보장이 존재하지 않는다.

따라서 데이터 전달 및 순서가 보장되지는 않지만, 단순하고 빠르다. \
정리하자면, **출발지, 목적지 PORT**와 체크섬 정도만 추가된 IP라고 생각하자.

> 체크섬 : 이 메세지가 정확한지 검증.
>
* 하얀 도화지에 비유(기능이 거의 없음)
* 연결지향 X - TCP 3 way handshake X
* 데이터 전달 보증 X
* 순서 보장 X
* 데이터 전달 및 순서가 보장되지 않지만, 단순하고 빠름
* 정리
  * IP와 거의 같다. +PORT +체크섬 정도만 추가
  * 애플리케이션에서 추가 작업 필요

머나먼 과거에는 신뢰할 수 있는 정보는 TCP, 영상과 같은 정보는 UDP로 전송했다. 하지만 시간이 지나면서 TCP가 90% 이상을 점유했지만, 최근 http3 스펙이 나오면서 또 다시 UDP가 떠오르고 있다.

## PORT
하나의 IP에서 여러 Application이 실행되고 있을 때, 내게 전송되는 여러가지 패킷의 사용처를 구분할 때 사용한다.

이전에 학습했던 TCP/IP 패킷 정보를 다시 살펴보자.

![image](https://github.com/jub3907/outSourcing/assets/58246682/d00a53df-09c8-4599-9c12-9c70645be6d8)

위와 같은 패킷 정보에서 IP는 목적지 서버를 찾기 위해, **PORT는 서버 내에 실행되고 있는 애플리케이션을 구분하기 위해** 사용한다.\
IP가 아파트라면, PORT는 개별 집이라고 생각하면 이해가 쉽다.

### PORT - 같은 IP 내에서 프로세스를 구분

![image](https://github.com/jub3907/outSourcing/assets/58246682/5a1a0922-dfca-45e4-ac85-9b8a34f40bdb)
위처럼, 게임은 8090포트, 화상 통화는 21000포트, 웹 브라우저는 10010 포트를 사용한다고 가정하자.\
패킷을 전송할 때 **출발지 PORT**까지 전송하므로, 서버에서도 주어진 PORT에만 데이터를 전송해주게 된다.

PORT는 0 ~ 65535까지 할당 가능하다. 하지만 0 ~ 1023까진 잘 알려진 포트이기 때문에, 사용하지 않는 것이 좋다.
* FTP - 20, 21
* TELNET - 23
* HTTP - 80
* HTTPS - 443


## DNS
IP는 기억하기가 어렵고, IP는 변경될 수 있다.

![image](https://github.com/jub3907/outSourcing/assets/58246682/06c6403c-361e-4fa4-9eb1-9a185611457a)

이런 경우 서버에 접근이 불가능하고, 다시 IP를 알아야만 한다. 이를 해결하기 위해 나온 시스템이 바로 DNS(Domain Name System)이다.

### DNS (Domain Name System, 도메인 네임 시스템)
일종의 전화번호부 기능을 하는 서버를 제공하여, 도메인 명을 등록해 IP주소로 바꿀 수 있다.

![image](https://github.com/jub3907/outSourcing/assets/58246682/d4e86135-7687-471d-a4ae-06ace9b5bbbb)

0. DNS 서버에 각 도메인(google.com)에 맞는 IP(200.200.200.2)를 등록한다.
1. 클라이언트가 DNS 서버에 도메인에 맞는 IP를 달라고 요청한다.
2. DNS 서버가 IP주소를 반환한다.
3. 클라이언트가 받아온 IP를 통해 서버에 요청한다.

## 정리
인터넷망을 통해 메세지를 전송하기 위해선 IP라는 Internet Protocol이 있어야만 한다.\
IP만으로는 메세지가 도착했는지 신뢰하기도 어렵고, PORT라는 개념도 존재하지 않는다.\
이러한 문제를 TCP 프로토콜이 해결해준다.

포트는 같은 IP 내에서 통신하는 애플리케이션을 구분하기 위해 사용한다.\
예를 들어, IP가 아파트라면 포트는 각각의 집이라고 볼 수 있다.

IP는 변하기 쉽고, 외우기 어렵기 때문에 DNS를 사용한다.



## URI, Uniform Resource Identifier
사람을 식별할 때 주민번호를 사용하는 것 처럼, 자원(Resource)을 식별하는데엔 여러 가지 방법이 존재한다.

크게 URL(Resource Locator)과 URN(Resource Name)이 존재한다.

![image](https://github.com/jub3907/outSourcing/assets/58246682/bea83e9e-7e64-41ad-92ef-7fe11262fe05)

URL과 URN은 다음과 같은 차이가 존재한다.

![image](https://github.com/jub3907/outSourcing/assets/58246682/d6bdd935-1b1e-47b3-95a5-c10b6d8dec82)

우리가 웹 브라우저에서 작성하는 것이 URL이다. URN은 주소에 이름을 부여하는 것으로, 자원에 대한 매핑이 어렵기 때문에 거의 URL만 사용한다.

### URL란?
* Uniform: 리소스를 식별하는 통일된 방식
* Resource: 자원, URI로 식별할 수 있는 **모든 것**. 제한이 따로 존재하지 않는다.
  * 자원에는 웹 브라우저에 있는 파일만 자원을 뜻하는 것이 아닌, 실시간 교통정보 등 우리가 구분할 수 있는 모든 것을 의미한다.
* Identifier: 다른 항목과 구분하는데 필요한 정보

* URL: Uniform Resource Locator
• URN: Uniform Resource Name

### URL, URN이란?
* URL - Locator: 리소스가 있는 **위치**를 지정.
  * 즉, 웹 브라우저에 무언가를 치면 **리소스가 그 곳에 존재한다**. 라고 치는 것. 
* URN - Name: 리소스에 이름을 부여

* 위치는 변할 수 있지만, 이름은 변하지 않는다.
* urn : isbn:8960777331 ( 어떤 책의 isbn URN )
* URN 이름만으로 실제 리소스를 찾을 수 있는 방법이 보편화 되지 않았다.
* 따라서, **앞으로는 URI를 URL과 같은 의미로 이야기 하자**.


### URL 분석

* https://www.google.com/search?q=hello&hl=ko

#### 전체 문법
`scheme://[userinfo@]host[:port][/path][?query][#fragment]`\
`https://www.google.com:443/search?q=hello&hl=ko`

* 프로토콜(https)
* 호스트명(www.google.com)
* 포트 번호(443)
* 패스(/search)
* 쿼리 파라미터(q=hello&hl=ko)

#### URL scheme
scheme:`//[userinfo@]host[:port][/path][?query][#fragment]`\
https:`//www.google.com:443/search?q=hello&hl=ko`

* 주로 프로토콜을 사용한다.
  * 프로토콜 : 어떤 방식으로 자원에 접근할 것인가 하는 클라이언트 - 서버 간의 약속과 규칙. http, https, ftp 등.
* http는 80포트, https는 443 포트를 주로 사용하며, 포트는 생략 가능하다.
* https는 http에 보안을 추가한 프로토콜이다. (HTTP Secure)

#### URL userinfo
`scheme://` [userinfo@] `host[:port][/path][?query][#fragment]`\
`https://www.google.com:443/search?q=hello&hl=ko`

* URL에 사용자 정보를 포함해서 인증한다.
* 거의 사용하지 않는다.

#### URL host
`scheme://[userinfo@]`host`[:port][/path][?query][#fragment]`\
`https://`<k>ww<k>w.google.com`:443/search?q=hello&hl=ko`

* 호스트 명
* 도메인 명, 또는 IP 주소를 직접 사용 가능하다.

#### URL POST
`scheme://[userinfo@]host`[:port]`[/path][?query][#fragment]`\
`https://www.google.com`:443`/search?q=hello&hl=ko`

* 포트(PORT)
* 접속 포트를 의미
* 일반적으로 생략하고, 생략시 http는 80, https는 443을 의미한다.

#### URL path
`scheme://[userinfo@]host[:port]`[/path]`[?query][#fragment]`\
`https://www.google.com:443/`search`?q=hello&hl=ko`
* 리소스가 존재하는 경로(path), 보통 계층적 구조로 되어 있다.
* 예)
  * /home/file1.jpg
  * /members
  * /members/100
  * /items/iphone12
 
#### URL query
`scheme://[userinfo@]host[:port][/path]`[?query]`[#fragment]`\
`https://www.google.com:443/search`?q=hello&hl=ko

* key=value 형태로 작성하며, ?로 시작, &로 추가 가능.
  * ?keyA=valueA&keyB=valueB
* 웹 서버에 제공하는 파라미터이기 때문에 query parameter, 문자 형태이기 때문에 query string 등으로 불린다.

#### URL fragment
`scheme://[userinfo@]host[:port][/path][?query]`[#fragment]
`https://docs.spring.io/spring-boot/docs/current/reference/html/gettingstarted.html`#getting-started-introducing-spring-boot

* html 내부 북마크 등에 사용되며, 서버에 전송하는 정보는 아니다.
* 일반적으로 위키백과 등에서 사용된다.

## 웹 브라우저 요청 흐름
### HTTP 메세지 생성
이전 시간처럼, `https://www.google.com:443/search?q=hello&hl=ko` 주소에 접근한다고 가정해보자.

![image](https://github.com/jub3907/outSourcing/assets/58246682/c6675d78-c937-4440-bee5-52f8cc917bea)

먼저 웹 브라우저가 DNS 서버를 조회하여 `www.google.com`에 해당하는 200.200.200.2 IP를 받아온다.\
https의 PORT는 443이므로, HTTP 요청 메세지를 생성한다. 

```http
GET /search?q=hello&hl=ko HTTP/1.1
Host: www.google.com
```

### HTTP 메세지 전송
![image](https://github.com/jub3907/outSourcing/assets/58246682/3121d82f-2301-4210-bdb7-f23c1c9dfce7)

웹 브라우저가 메세지를 생성했다면, SOCKET 라이브러리를 통해 OS(TCP/IP계층)에 전달한다. \
위에서 구한 IP, PORT 정보를 통해 TCP/IP 패킷을 생성한 뒤 서버와 연결하고, 데이터를 전송한다.

#### 성성된 패킷
![image](https://github.com/jub3907/outSourcing/assets/58246682/50cb0333-5497-4a58-8994-69169ef5c949)

#### 패킷 전송
![image](https://github.com/jub3907/outSourcing/assets/58246682/572cae73-d8d9-4aed-9756-143598e65c27)

요청 패킷이 도착했다면, 구글 서버는 TCP/IP 정보를 제외한 HTTP 메세지를 가지고 해석해 다음과 같은 HTTP 응답 메세지를 생성한다.

![image](https://github.com/jub3907/outSourcing/assets/58246682/555b47fa-14ae-4c23-8ddf-d0536a55b98d)

* HTTP 버전, 정상 응답인지
* Content-Type: 응답하는 데이터가 text, html 형식이며, UTF-8 형식이다.
* Content-Length: 응답 데이터의 길이.

서버 또한, HTTP 응답 메세지에 TCP/IP 패킷을 씌워 클라이언트에 패킷을 전송한다.

![image](https://github.com/jub3907/outSourcing/assets/58246682/7ae3bd79-b5dc-4f9b-9c62-b07792769263)

클라이언트에선 HTTP 응답 메세지를 받아, 웹 브라우저에서 렌더링 한다.

![image](https://github.com/jub3907/outSourcing/assets/58246682/5483a5d8-be00-4d25-8da2-76fa66a69f3b)


## HTTP
### 모든 것은 HTTP로
HyperText Transfer Protocol. 문서간의 링크를 통해서 연결할 수 있는 html을 전송할 수 있는 프로토콜로서 시작되었다. 

하지만, 지금은 모든 데이터를 HTTP 메세지에 담아서 전송할 수 있다. 
* HTML, Text
* Image, 음성, 영상, 파일
* JSON, XML (API)

거의 모든 형태의 데이터를 전송할 수 있다. 심지어, 서버간에 데이터를 주고 받을 때도 대부분 HTTP를 사용한다.

### HTTP의 역사
* HTTP/0.9 1991년: GET 메서드만 지원, HTTP 헤더X
* HTTP/1.0 1996년: 메서드, 헤더 추가
* **HTTP/1.1 1997년: 가장 많이 사용, 우리에게 가장 중요한 버전**
  * RFC2068 (1997) -> RFC2616 (1999) -> RFC7230~7235 (2014)
* HTTP/2 2015년: 성능 개선
* HTTP/3 진행중: TCP 대신에 UDP 사용, 성능 개선

HTTP 1.1 버전에 거의 모든 기능이 들어있고, HTTP/2와 HTTP/3은 거의 성능 개선에 초점이 맞춰져 있다.

### 기반 프로토콜
HTTP/1.1과 2는 TCP 기반 위에서 동작하지만, HTTP/3은 UDP 기반으로 개발 되어 있다. 

TCP는 3-way handshake가 필요하고, 기본적으로 필요한 데이터가 너무 많기 떄문에 속도가 빠른 매커니즘이 아니다. 이를 UDP 프로토콜 위에 애플리케이션 레벨에서 성능을 최적화 하도록 성능을 최적화해서 나온 것이 HTTP 3이다.

* TCP : HTTP/1.1, HTTP/2
* UDP : HTTP/3
* 현재 HTTP/1.1 주로 사용
  * HTTP/2, HTTP/3 도 점점 증가

### HTTP의 특징
* 클라이언트 - 서버 구조
* 무상태 프로토콜(Stateless), 비 연결성
* 보내고, 받을 때 HTTP 메세지를 사용한다.
* 단순함, 확장 가능

### HTTP의 특징 1. 클라이언트 - 서버 구조

HTTP는 클라이언트가 HTTP 메세지를 통해 서버에 요청을 보내고, 서버에 응답이 올 때까지 대기한다.\
서버가 요청에대한 결과를 만들어서 응답이 오면, 해당 응답 결과를 열어 클라이언트가 동작한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/5d9c3a98-ff53-4eb8-b196-73ffe450bad1)

이는 표면적인 동작 방식일 뿐이고, 이렇게 서버와 클라이언트를 분리한 점 자체가 굉장히 중요하다. \
비즈니스 로직과 데이터 관련된 것은 서버에, 클라이언트는 UI를 그리고, 사용성을 증진시키는 데 집중한다.\
이를 통해 클라이언트와 서버는 각각 독립적으로 진화할 수 있다.

즉, 클라이언트는 복잡한 데이터를 다룰 필요도, 비즈니스 로직을 다룰 필요도 없고, \
그냥 단순하게 UI를 어떻게 그릴지, UX를 어떻게 증진시킬지만 고민하면 된다.

반대로, 서버도 요청이 폭발적으로 증가하게 되었을 때, \
어떤 방식으로 대용량 트래픽에 대응하기 위해 더 고도화하여 진화할 지만 고민하면 된다.

### HTTP의 특징 2. 무상태 프로토콜, Stateless
서버가 클라이언트의 상태를 보존하지 않는 걸 의미한다. \
이해를 돕기 위해 상태를 유지하는 Stateful, 상태를 유지하지 않는 Stateless의 예시를 한번 살펴 보자.

#### 상태 유지 - Stateful 예시
```
• 고객: 이 노트북 얼마인가요?
• 점원: 100만원 입니다.

• 고객: 2개 구매하겠습니다.
• 점원: 200만원 입니다. 신용카드, 현금중에 어떤 걸로 구매 하시겠어요?

• 고객: 신용카드로 구매하겠습니다.
• 점원: 200만원 결제 완료되었습니다.
```
굉장히 일상적인 예제이지만, 다음과 같이 **점원이 중간에 변경되는 경우**를 보자.

#### 상태 유지 - Stateful, 점원이 중간에 바뀌는 경우

```
• 고객: 이 노트북 얼마인가요?
• 점원A: 100만원 입니다.

• 고객: 2개 구매하겠습니다.
• 점원B: ? 무엇을 2개 구매하시겠어요?

• 고객: 신용카드로 구매하겠습니다.
• 점원C: ? 무슨 제품을 몇 개 신용카드로 구매하시겠어요?
```
위 예시에서, 점원 B와 점원 C는 요청의 맥락(Context)를 알 수 없다. 

이전에, Stateless는 서버가 클라이언트의 상태를 보존하지 않는다.\
Statful은, 서버가 클라이언트의 이전 상태(문맥)을 보존하는 것을 의미한다.

#### 상태 유지 - Stateful, 정리
```
• 고객: 이 노트북 얼마인가요?
• 점원: 100만원 입니다. (노트북 상태 유지)

• 고객: 2개 구매하겠습니다.
• 점원: 200만원 입니다. 신용카드, 현금중에 어떤 걸로 구매 하시겠어요?
(노트북, 2개 상태 유지)

• 고객: 신용카드로 구매하겠습니다.
• 점원: 200만원 결제 완료되었습니다. (노트북, 2개, 신용카드 상태 유지)
```
점원은 물건이 노트북, 2개, 신용카드라는 각각의 상태를 유지하고 있다. \
이렇게 Statful에선 서버가 클라이언트의 상태를 보존한다.

이번엔 무상태, Stateless 예시를 보자.

#### 무상태 - Stateless
```
• 고객: 이 노트북 얼마인가요?
• 점원: 100만원 입니다.

• 고객: 노트북 2개 구매하겠습니다.
• 점원: 노트북 2개는 200만원 입니다. 신용카드, 현금중에 어떤 걸로 구매 하시겠어요?

• 고객: 노트북 2개를 신용카드로 구매하겠습니다.
• 점원: 200만원 결제 완료되었습니다.
```
점원이 고객의 상태를 유지하지 않기 때문에 고객은 자신이 무엇을 살 것인지, \
무엇으로 살 것인지 일일히 알려주고 있다.

다음 예시를 보자.

#### 무상태 - Stateless, 점원이 중간에 바뀌는 경우
```
• 고객: 이 노트북 얼마인가요?
• 점원A: 100만원 입니다.

• 고객: 노트북 2개 구매하겠습니다.
• 점원B: 노트북 2개는 200만원 입니다. 신용카드, 현금중에 어떤 걸로 구매 하시겠어요?

• 고객: 노트북 2개를 신용카드로 구매하겠습니다.
• 점원C: 200만원 결제 완료되었습니다.
```
Stateful 상태에선 점원이 중간에 변경되는 경우 응대가 불가능했지만(장애 발생), \
Stateless 상태에선 점원이 변경되더라도 문제 없이 결제가 가능해졌다.

#### Stateful, Stateless 차이 정리
* **상태 유지** : 중간에 다른 점원으로 바뀌면 안된다.
  * (중간에 다른 점원으로 바뀔 때 상태 정보를 다른 점원에게 미리 알려줘야 한다.)
* **무상태** : 중간에 다른 점원으로 바뀌어도 된다.
  * 갑자기 고객이 증가해도 점원을 대거 투입할 수 있다.
  * -> 갑자기 클라이언트 요청이 증가해도 서버를 대거 투입할 수 있다.
* 무상태는 응답 서버를 쉽게 바꿀 수 있다. -> **무한한 서버 증설 가능**

#### Stateless의 실무에서의 한계점
모든 것을 무상태로 설계할 수 있는 경우도 있고, 없는 경우도 존재한다.
* 무상태
  * ex) 로그인이 필요 없는 단순한 서비스 소개 화면
* 상태 유지
  * ex) 로그인
    
로그인과 같은 상태를 유지해야 하는 경우, 로그인한 상태를 서버에 유지해줘야 한다.\
일반적으로, 브라우저의 쿠키와 서버의 세션을 사용해서 로그인 상태를 유지하게 된다.

이런 꼭 필요한 경우에만, 최소한으로 상태 유지를 사용해야 한다.

또한 너무 많은 데이터를 전송해야 한다는 단점도 존재한다.

### HTTP의 특징 3. 비 연결성
TCP/IP 연결 같은 경우, 클라이언트와 서버간의 연결을 유지해야 한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/86af77ae-febd-4f91-87fa-95b41b7d73ed)

다른 클라이언트가 서버에 연결 요청을 보내더라도, 이전의 연결은 계속 유지해야 한다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/703e6be6-959d-472a-8bbc-1a987f8a0178)

이 과정에서, 서버의 자원은 계속 소모된다. \
즉, 다른 클라이언트가 놀고 있더라도, 서버의 연결은 계속 유지되어야만 한다.

#### 연결을 유지하지 않는 모델
그럼, 연결을 유지하지 않는 모델을 살펴보자.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/07c76b02-1ae6-46fb-b12b-10278b9dc102)

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/48b90ca0-5740-4514-8fcf-a89912cef6a2)

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/0e7a845f-87c4-47ae-bdb7-cb3aec048a0a)

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/11726cb0-6ed8-4650-91f4-d089cd151ade)

이렇게, 서버는 클라이언트와의 연결을 유지하지 않는다면 서버가 유지하는 자원을 최소한으로 줄일 수 있다. 

HTTP는 이처럼, **기본적으로 연결을 유지하지 않는 모델**이다. \
일반적으로 초단위 이하의 빠른 속도로 응답하는데, 1시간동안 수천명이 서비스를 사용하더라도 \
실제 서버에서 동시에 처리하는 요청은 수십개 이하로 매우 작기 때문에, **서버 자원을 효율적으로 사용할 수 있다.** 

#### 비 연결성의 한계와 극복
기본적으로 연결을 유지하지 않기 때문에, 요청이 들어올 때마다 TCP/IP 연결을 새로 맺어야 한다.\
-> 즉, 3 way handshake 시간이 추가로 소요된다.

웹 브라우저로 사이트를 요청하면 HTML 뿐만 아니라, 자바스크립트나 CSS, 이미지 등의 자원이 다운로드 된다.\
이러한 자원을 받을 때마다 연결하고, 받고, 끊고를 반복하게 되면, 너무나 비 효율적이다.\
이러한 문제점은 HTTP 지속 연결(Persistent Connections)로 해결하게 되었다.\
또한, HTTP/2, HTTP/3에서 더 많은 최적화가 되었다.

* HTTP 초기, 연결과 종료의 낭비
  * ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/fc9fda1a-3c8d-47d6-ae6c-acc31e8880c8)

  
* HTTP 지속 연결(Persistent Connections)
  * ![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/d1d19f94-65e6-4b0d-9fa6-99379636514f)

#### Stateless를 기억하자!
* 같은 시간에 딱 맞추어 발생하는 대용량 트래픽을 서버 개발자들이 매우 어려워 한다.
  * ex) 선착순 이벤트, 학과 수업 등록 등 수만명 동시 요청

### HTTP의 특징 4. HTTP 메세지
#### HTTP 메세지 구조
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/f4d02517-3927-4603-9955-eeee9eac9fdb)

HTTP 메세지는 크게 **시작 라인, 헤더, 공백 라인, 바디**로 나뉜다. 이 때, **공백 라인은 반드시 존재해야 한다.**

#### HTTP 요청 메세지 예시
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/99bc6a71-0d06-4acc-a262-016ff594b3bd)

위 요청 메세지는 `start-line`, `header`, `empty line`으로 이루어져 있다.
> 요청 메시지도 body 본문을 가질 수 있음


#### HTTP 응답 메세지
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/3e66f28a-a1b1-428d-8c30-8e22dbc19245)

위 응답 메세지는 `start-line`, `header`, `empty line`, `message body`로 이루어져 있다.


### HTTP 메세지 - 시작 라인
#### 요청 메세지
`GET /search?q=hello&hl=ko HTTP/1.1 `

시작 라인은 크게 `request-line`과 `status-line`으로 나누어져 있다.
* **request-line** : `method` SP(공백) `request-target` SP `HTTP-version` CRLF(엔터) 
  * method: HTTP 메소드 (GET:조회 등)
  * request-target: 요청 대상 (/search?q=...)
  * HTTP-version : HTTP의 버전 (HTTP/1.1)

> HTTP 메소드의 종류 : GET, POST, PUT, DELETE 등..

> 서버가 수행해야 할 동작을 지정한다.\
> * GET: 리소스 조회
> * POST: 요청 내역 처리
> * 추후 추가 학습.

#### 응답 메세지
`HTTP/1.1 200 OK `

* status-line: : `HTTP-version` SP `status-code` SP `reason-phrase` CRLF
  * HTTP-version: HTTP의 버전
  * status-code : HTTP 상태 코드, 요청 성공과 실패를 나타낸다.
    * 200 : 성공
    * 400 : 클라이언트 요청 오류
    * 500 : 서버 내부 오류
  * reason-phrase : 사람이 이해할 수 있는 짧은 상태 코드 설명 글

### HTTP 메세지 - 헤더
![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/b1ff1c73-235d-4fe1-aa58-e2b2efa7cedd)

* header-field : `field-name`: `field-value`
* field-name은 대소문자를 구분하지 않지만, value는 대소문자를 구분해야한다.

#### 헤더의 용도
* HTTP 전송에 필요한 모든 부가 정보를 담고 있다.
  * ex) 메세지 바디의 내용, 바디의 크기, 압축, 인증, 캐시 관리 등..
* 표준 헤더가 너무나도 많다. [링크](https://en.wikipedia.org/wiki/List_of_HTTP_header_fields)
* 필요할 때, 임의의 헤더를 추가할 수 있다.
  * helloworld: hihi
 
### HTTP 메세지 - 바디
```
<html>
 <body>...</body>
</html>
```
바디는 실제 전송할 데이터.\
HTML 문서나 이미지, 영상, JSON 등 바이트로 표현할 수 있는 모든 데이터를 전송할 수 있다.

### HTTP 정리
* HTTP 메시지에 모든 것을 전송
* HTTP 역사 HTTP/1.1을 기준으로 학습
* 클라이언트 서버 구조
* 무상태 프로토콜(스테이스리스)
* HTTP 메시지
* 단순함, 확장 가능

