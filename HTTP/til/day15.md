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
