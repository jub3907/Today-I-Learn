## **Github 생성**

실제 프로젝트에 들어가기 앞서, 우선 프로젝트를 생성해 주자!

이 프로젝트는 그냥 public으로 오픈해 둘 예정이다. 보고싶은 분이 계실지는 잘 모르겠지만..

👉 https://github.com/jub3907/duo_gg_client

👉 https://github.com/jub3907/duo_gg_server

프로젝트도 생성했으니, 이제 결정해야 하는게 한 가지 존재한다.

바로 **API를 어디서 호출할 지이다.**

전적 검색 사이트를 구현하기 위해선 Riot에서 제공하는 API를 사용해야 한다.

즉, 내 DB에서 데이터를 받아오는 것이 아닌 외부 API를 사용해야 하는데,

이 작업은 백엔드에서도 가능하고, 프론트엔드에서도 가능하다.

하지만, API를 호출하는 과정에서 **key**가 필요하고,

이는 외부에 절대 노출되어서는 안되는 값이며, 일반적으로

프론트엔드에선 이 값을 몰라야 하기 때문에 백엔드에서 api를 호출한다.

또한 gql의 InMemoryCache를 사용해, api 호출 회수를 줄일 수도 있다고 생각된다!
