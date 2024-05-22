## **Match Base Resolver**

기존에 recentMatch, matchDetail이 선언되어있던 match-basic과 match-detail은

matchType이라는 ResolveField가 필요하다는 공통점이 존재한다.

다른점이 더 많기 때문에 굳이 상속구조를 만들 필요는 없지만,

기존에 resolver를 구현할 때, base resolver를 구현해본 적은 없기 때문에

match-basic, match-detail의 부모인 match-base resolver를 구현해 봤다.

base-resolver의 경우, 역시 nestjs의 공식문서에 가이드라인이 존재한다.

https://docs.nestjs.com/graphql/resolvers#class-inheritance

위 링크를 따라가면서 차근차근 진행해, 아래와 같은 base resolver를 생성했다.

```
import { Type } from '@nestjs/common';
import { Parent, ResolveField, Resolver } from '@nestjs/graphql';
import { MatchDocument } from './schema/match.schema';

export function MatchBaseResolver<T extends Type<unknown>>(model: T): any {
  @Resolver((of) => model, { isAbstract: true })
  abstract class MatchBase {
    @ResolveField((returns) => String)
    matchType(@Parent() match: MatchDocument) {
      return match.queueId === 420
        ? '솔로 랭크'
        : match.queueId === 430
        ? '일반 게임'
        : match.queueId === 440
        ? '자유 5:5 랭크'
        : match.queueId === 450
        ? '무작위 총력전'
        : match.queueId === 1400
        ? '궁극기 주문서'
        : '기타';
    }
  }

  return MatchBase;
}

```

## **기존 Args**

지금, mutation의 대부분은 inputType이 아닌 일반 파라미터로 설정되어있다.

```
async posts(
    @Args('createdAt') createdAt: number,
    @Args('limit') limit: number,
  )

```

각 parameter에는 validation이 필요한데,

위와 같은 형태로 작성하다 보니 validation의 적용이 어려워졌다.

이러한 parameter들을 필요한 경우 inputType,

이외엔 전부 ArgsType으로 변경하려고 한다.

그리고 class-validator 라이브러리를 사용해 validation을 추가해주자!

```
import { ArgsType, Field } from '@nestjs/graphql';
import { IsString } from 'class-validator';

@ArgsType()
export class NameArgs {
  @Field((type) => String)
  @IsString()
  name: string;
}

```

### 

### 

## **Api 호출 및 파싱함수**

기존 글을 읽어봤다면 알겠지만,

나는 **각 폴더의 서비스**에 API 호출 함수와, 파싱 함수를 작성해 두었었다.

👉 **Timeline Service**

```
async getTimeline(matchId: string) {
    return await this.api.getApiResult('timelineBymatchId', matchId);
  }

  parseTimeline(data: JSON): TimelineDto {
    const events = data['info']['frames']
      .map(({ events }) =>
        events.reduce((acc, event) => {
          if (TimelineEventType.includes(event.type)) {
            acc.push({
              ...event,
              timestamp: Math.floor(
                event.timestamp / data['info']['frameInterval'],
              ),
            });
          }
          return acc;
        }, []),
      )
      .flat();

    return { matchId: data['metadata']['matchId'], events: events };
  }

```

이 때는 **각 서비스는 그 서비스의 데이터 구조를 다룬다**는 생각으로 이처럼 작성했었지만,

API를 호출하고 나면 데이터는 JSON타입이므로 파싱 함수에 들어가기 전까지 내부 구조를 알 수 없다.

즉, 구조도 모르는 모호한 데이터가 resolver에서 돌아다니는 것을 계속 보니..

너무나 마음에 안 들었다. 😑

그래서 API 호출 코드와 파싱 함수를 private로 작성하고,

API 호출 및 파싱 후 리턴 하는 함수를 만드려고 했지만..

DB에서 데이터를 꺼내오는 함수와 결과가 같아 네이밍도 헷갈리고, 내가 만약 이 코드를 처음 보면

**findByName** 함수와 **getSummoner** 함수의 차이를 절대 모를 것 같다는 생각이 들었다.

👉 **findByName** : 이름을 파라미터로 받아, DB에서 데이터를 꺼내는 함수

👉 **getSummoner** : 이름을 파라미터로 받아, API 호출 결과를 리턴하는 함수

따라서 API 호출 및 파싱 함수를 전부 API 서비스에 몰아넣었고,

결과적으로 각 폴더의 서비스들을 좀 더 간결하게 유지할 수 있었다. 😉

## **백엔드 1차 구현을 마치고..**

일단 백엔드 1차 구현을 마쳤다!

Mutation은 총 12개로, 아래와 같다.

- basicSummonerInfo
- createComment
- deleteComment
- matchBuild
- ranking
- recentMatches
- updateChampionData
- updateIconData
- updateItemData
- updateRuneData
- updateSummonerData
- createPost

Query는 4개이다.

- mastery
- matchDetail
- posts
- comments

뮤테이션 이름만 보면 당연히 쿼리일 것 같은 ranking, matchBuild 뮤테이션도

DB에서 조회만 하는 게 아니라, API를 호출하고 DB를 업데이트하는 작업까지 하다 보니

쿼리가 많이 줄어들었다.

이러한 점이 조금 마음에 걸려 **API 호출 및 DB 저장** 뮤테이션과 **DB 조회 쿼리**로 분리해서

클라이언트측에게 각각 요청하도록 만들까 고민도 했었지만,

이건 서버 요청을 한번 더 할뿐만 아니라, **DB 저장 후 바로 리턴**하던 데이터를

DB 저장 -> 쿼리 요청 -> DB조회 -> 데이터 리턴 순서의, 중간 단계를 추가하기 때문에

그다지 좋은 방식은 아니라고 생각한다.

물론 지금 방식도 좋은 방식은 아니라고 생각이 드는지라,

어떻게 이 난관을 헤쳐나갈지.. 생각해봐야겠다.

지금까지 구성한 코드에서 부족한 부분을 찾자면..

- **API 호출을 실패했을 때 ( 주로 Limit 이슈 ) 대기 후 재 호출 할 것인지, 클라이언트에 호출이 실패했음을 알릴 것인지?**
- 의미가 모호한 네이밍 ( match-basic, match-detail 등.. )
- MatchBasicModel에 **summonerInGameData**를 추가하기 위해 recentMatch 뮤테이션 리턴 값에 Object.assign을 사용해 소환사의 puuid값을 강제로 넣어주어, summonerInGameData ResolveField의 parent 타입이 꼬인 것. 스파게티 코드로 가는 지름길이라고 생각한다.
- DataDragon의 DB 구조
- ResolveField로 이루어진 아이템, 스펠 등의 이미지 경로를 받아오기 위해 매번 DB에 접근하는 방식

이외에도 함수 관심사의 분리에 실패하는 등 여러가지 문제가 존재하지만,

이제 프론트엔드 작업을 시작하지 않으면 기간 내에 끝내지 못하겠다는 생각이 든다.

우선 1차 구현을 이 정도로 마치고, 프론트엔드 작업을 끝낸 뒤 다시 손을 대 보자!
