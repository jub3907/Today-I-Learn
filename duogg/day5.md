## **데이터 구조 설계**

다음 과정은 데이터 구조 설계였다.

데이터 구조 설계 시, 중점으로 생각한 것은 아래와 같았다.

✅ **각 게임의 정보와, 소환사의 정보를 분리한다.**

소환사의 정보와, 각 게임의 정보를 분리하지 않으면, 전적검색 사이트의 특성상

한 게임의 모든 정보가 소환사에게 종속될 수 밖에 없다고 생각한다.

예를 들어, 두 사람이 같은 게임을 했다고 가정해 보자.

이 때 게임의 정보가 소환사에게 종속되어 있다면,

같은 게임의 데이터가 두 사람에게 각각 부여되어 있어야 한다.

이렇게 불필요한 중복 데이터가 존재할 필요가 없으므로,

게임의 데이터는 게임의 데이터대로 저장하고,

소환사는 해당 게임의 unique key 값만 저장하는 방식을 선택했다.

또한 api를 사용했을 때 리턴되는 데이터의 이름을 최대한 따라가는 방향으로 진행하되,

추후 사용하기 편한 형태로 파싱해 저장하려고 한다.

( 근데.. riot에서 리턴해주는 데이터들이 네이밍이 다 다르다.. 어디는 summoner라고 되어있고, 어디는 spell이라고 되어있고.... ㅠㅠ )

## **summoner**

```
accountId: 해당 계정의 고유 id
profileIconId: 소환사가 설정해둔 프로필 아이콘의 unique key
name: 소환사명
id: 소환사의 id. LeagueEntry를 업데이트 하는데에 사용된다.
puuid: 소환사의 puuid
summonerLevel: 소환사의 레벨

comments: 해당 소환사에게 남겨진 commnet 리스트
masteries: 챔피언에 대한 mastery 리스트

```

comment

```
createdAt: 생성시간
nickname: 댓글 작성 닉네임
password: 암호화된 댓글 작성 비밀번호
text: 댓글

```

mastery

```
champId: 챔피언의 unique id
championLevel: 챔피언에 대한 숙련도 레벨
championPoints: 챔피언에 대한 숙련도 점수

```

## **leagueEntry**

```
queueType: 랭크의 종류. 파싱후 솔로랭크, 자유랭크만 저장
summonerId: 소환사의 id
summonerName: 소환사명
tier: 소환사의 티어. ex) Challenger
rank: 소환사의 랭크. ex) I
leaguePoints: 소환사의 점수. ex) 1000
wins: 승리 수
losses: 패배 수
createdAt: 해당 데이터 생성 시간
updatedAt: 해당 데이터가 업데이트된 시간

```

## **matches**

```
matchId: 해당 매치에 대한 unique key
gameCreation: 게임 생성 시간, Unix Milliseconds
gameDuration: 게임 진행 시간, sec
winner : 승리한 팀의 값. 100 / 200
queueId: 게임 타입
participants: 해당 매치에 대한 각 participant의 인게임 정보 리스트

timelines: 시간대 별 이벤트. 아이템구매/ 아이템철회/ 스킬정보 리스트

```

## **participant**

```
puuid: 해당 플레이어의 unique key
participantsId: 해당 매치에서 참여자의 식별번호. 1~10
teamId: 블루, 레드팀 식별값. 100 / 200
win : 승리 여부
individualPosition: 포지션
champLevel: 인게임 종료 시 레벨
champId: 선택한 챔피언의 unique id
championName: 선택한 챔피언 이름
dragonKills: 드래곤 킬수
baronKills: 바론 킬수
turretKills: 타워 부신수
goldEarned: 골드 획득량
kills: 킬 수
deaths: 데스 수
assists: 어시스트 수
totalMinionsKilled: 미니언 처치 횟수
wardsKilled: 와드 부순 횟수
wardsPlaces: 와드 설치 횟수
visionWardsBoughtInGame: 제어와드 구매 횟수
totalDamageDealtToChampions: 가한 피해량
totalDamageTaken: 받은 피해량
items: 아이템 정보를 저장하는 ??? 리스트
summoners: 스펠 정보를 저장하는 ??? 리스트
perks: 스탯과 룬에 대한 정보가 저장된 객체

```

??? - 아이템, 스펠의 위치와 unique key 저장 -**임시로 image라는 네이밍 사용..**

```
index: 품목의 위치.
id: 품목의 unique key

```

perks

```
flex: 선택한 스탯의 unique key
defense: 선택한 스탯의 unique key
offense: 선택한 스탯의 unique key
primaryStyle: 메인 룬의 종류에 대한 unique key
primarySelections: 선택한 세부 룬의 unique key 리스트
subStyle: 서브 룬의 종류에 대한 unique key
subSelections: 선택한 세부 룬의 unique key 리스트

```

perks는 한 데이터에서 불러오지만, 해당 데이터를 사용하는 위치가 달라

편의성을 위해 따로 처리.

### **timeline**

```
type: 이벤트 정보
timestamp: 이벤트가 발생한 시간, 60000이 1분
participantId: 이벤트 발생자의 인게임 식별번호
itemId: 아이템의 unique id, nullable
skillSlot: 스킬의 id.

```

## **DataDragon - 각 아이템이나 스텟 등의 unique key와 그 경로를 저장**

```
type: items 데이터의 타입.
base: 이미지 경로의 base path
pathes: 각 품목에 대한 이미지 정보 리스트

```

path

```
id: 품목에 대한 unique key
path: 품목에 대한 이미지 경로

```

## **posts**

```
queueType: 랭크 타입
role: 포지션
name: 소환사명
tier: 티어
text: 게시글 본문
createdAt: 작성시간
```
