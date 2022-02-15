## 웹훅
웹훅이란, 어떤 작업이 수행되면 수행된 쪽에서 해당 작업이 수행되었음을 알려주는 기능이다.

디스코드 채널에 웹훅을 설정해, Github organization에서 수행된 작업을 디스코드로 알림을 받아보자.

## 디스코드 채널에 웹훅 만들기

특정 채널에 웹훅을 만들기 위해선, 우선 채널 편집으로 들어가자.
<image src = "https://user-images.githubusercontent.com/58246682/154058766-cc9dde50-7fb3-40bf-89b6-66180df367f9.png" width = "50%" />


연동 메뉴로 들어가면 **웹후크** 항목이 보인다. 해당 항목을 클릭하자.

<image src = "https://user-images.githubusercontent.com/58246682/154058924-ed16f084-a489-46aa-8282-6bd000327824.png" width = "50%" />

웹훅을 처음 만들 경우, 아래와 같이 설정 창을 볼 수 있다.

<image src = "https://user-images.githubusercontent.com/58246682/154058887-18e19d96-3c3f-451d-afa1-b5fd79d4e0b2.png" width = "50%" />

여기서 웹후크 URL 복사를 눌러 주소를 얻으면, 아래와 같은 형태인걸 확인할 수 있다.\
https://discord.com/api/webhooks/{웹훅 아이디}/{웹훅 토큰}\
이제 Github와 연결을 해보자.


## github에 웹훅 연결

지금 진행중인 프로젝트를 위해 Organization이 존재한다.

<image src = "https://user-images.githubusercontent.com/58246682/154059205-8ba28cc9-f68b-49b0-b425-9208b67e57db.png" width = "50%" />

이 Organization의 설정 중, Webhook에 들어가자.

<image src = "https://user-images.githubusercontent.com/58246682/154059260-a76cc18b-e9f7-431d-964a-2723609d573c.png" width = "50%" />

새로운 웹훅을 추가하여 URL에 디스코드에서 받아온 주소값의 뒤에 **/github**를 붙이고, \
Content type을 application/json으로 설정한다.\
또한 webhook으로 **모든 정보**를 받아올 수 있도록, Send me everything을 체크하고 웹훅을 생성한다.

<image src = "https://user-images.githubusercontent.com/58246682/154059813-dabe1761-a5b6-434f-9919-a1e4c14932c8.png" width = "50%" />


완성!
![image](https://user-images.githubusercontent.com/58246682/154060343-5ee231f7-928e-4c87-8bb6-e7cb9a953500.png)
