
## **백엔드 프로젝트 생성**

우선, 5일차에서 정립한 DB구조를 구현하기 위해 백엔드 프로젝트를 생성했다.

백엔드는 nestjs로 구현하고 프론트에서 요청은 GraphQL로,

데이터베이스는 MongoDB를 사용할 예정이다.

우선 nestjs 프로젝트를 생성하고,

```
@nestjs/cli new duo_gg_server

```

아래 패키지들을 설치한다.

```
npm install --save @nestjs/mongoose mongoose
npm i --save @nestjs/config

```

MongoDB를 사용하기 위한 모듈, Configuration을 위한 모듈이다.

✅ **Configuration**

우선 Configuration을 AppModule에 추가해 보자.

Configuration은 우리가 프로젝트를 **다른 환경**에서 실행해야 하는 경우,

이 환경 변수들의 값을 다르게 실행할 수 있도록 해 준다.

또한 보안을 유지해야 하는 Token key나 api 호출 키 등을

따로 분리할 수 있기 때문에, 가장 먼저 세팅해줘야 한다.

```
@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      envFilePath: '.env.development',
    }),
  ],
  controllers: [AppController],
  providers: [AppService, ConfigService],
})
export class AppModule {}

```

isGlobal은 ConfigModule을 다른 모듈에서 전역적으로 사용할 수 있게 해주고,

envFilePath는 이름 그대로 환경 변수 파일의 이름을 의미한다.

세팅을 마쳤으면 환경 변수 파일을 추가하고,

분리한 환경 변수 값으로 DB Connection을 진행한다.

```
// .env.development
NODE_ENV='development'
PORT=3333

# DB
DB_HOST=""
DB_USERNAME=""
DB_PASSWORD=""
DB_NAME=""

```

```
// src/config/configuration.ts
export default () => ({
  port: process.env.PORT || 3333,
  db: `mongodb+srv://${process.env.DB_USERNAME}:${process.env.DB_PASSWORD}@${process.env.DB_HOST}/${process.env.DB_NAME}`,
});
import configuration from './config/configuration';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      envFilePath: '.env.development',
      load: [configuration],
    }),
  ],
  controllers: [AppController],
  providers: [AppService, ConfigService],
})
export class AppModule {}

```

✅ **Mongoose**

다음은 MongoDB를 연결해 보자.

Mongoose 모듈을 사용하며 useFactory를 사용해 위에서 정의한

configuration을 동적으로 불러오고, db를 연결한다.

( useFactory 대신, useClass를 사용할 수도 있다. )

```
import configuration from './config/configuration';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      envFilePath: '.env.development',
      load: [configuration],
    }),
    MongooseModule.forRootAsync({
      useFactory: async (config: ConfigService) => {
        return {
          uri: config.get('db'),
        };
      },
      inject: [ConfigService],
    }),
  ],
  controllers: [AppController],
  providers: [AppService, ConfigService],
})

```

Mongoose는 스키마를 기반으로 동작하고,

스키마는 MongoDB의 컬렉션에 매핑되어 각 컬렉션의 형태를 정의한다.

## **폴더 구조**

schema를 정의하기에 앞서, 폴더 구조를 먼저 정해야만 한다.

기본적으로 nestjs에선 cli를 사용해 컨트롤러나 모듈, 서비스를 생성할 수 있다.

이 때, src 아래에 각각의 네이밍에 맞는 폴더를 생성하고,

그 안에 컨트롤러, 모듈, 서비스 등을 모아둔다.

이는 [nestjs의 graphql 공식 예제](https://github.com/nestjs/nest/tree/master/sample/23-graphql-code-first/src/recipes)에서도 동일하게 사용하고 있으므로, 이 폴더 구조를 따라가려 한다.

다만, **summoner**와 **comment**처럼 일방적인 종속성을 지니는 schema를 분리할지,

혹은 한 폴더로 묶어 관리할 지 조금 고민이 되는데..

우선은 묶어서 관리하고, 조금 더 리서치 후 변경할지 고민해 보자.

## **schema 생성**

다음으로, 이제 데이터 구조를 schema 파일로 구현한다.

nestjs는 @Schema라는 데코레이터를 통해 이 스키마를 구현할 수 있다.

아래는 comment schema의 예시이고, 이와 유사한 형태로 모든 스키마를 구현했다.

👉 **예시, comment**

```
import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import * as mongoose from 'mongoose';

const Types = mongoose.Schema.Types;

@Schema({
  timestamps: { currentTime: () => new Date().getTime() },
})
export class Comment {
  @Prop({ type: Types.Number })
  createdAt: number;

  @Prop({ type: Types.String })
  nickname: string;

  @Prop({ type: Types.String })
  password: string;

  @Prop({ type: Types.String })
  text: string;
}

export type CommentDocument = Comment & mongoose.Document;
export const CommentSchema = SchemaFactory.createForClass(Comment);

```

혹시 다른 코드가 궁금하신분들은 아래 github에서 develop-v0.1.0을 참고해보세요!

https://github.com/jub3907/duo_gg_server

## **페이지 별 필요 데이터**

그럼, 이제 페이지를 보면서 각 페이지에서 어떤 데이터가 필요할지 생각해 보자!

이 작업은 클라이언트에서 요청했을 때 어떤 데이터를 리턴해야 할 지 설계하기 위해 필요하다.

- 홈
- 전적 결과 - 전적 종합
- 전적 결과 - 통계
- 전적 결과 - 소환사에게 한마디
- 멀티서치
- 듀오 신청

한번에 다 하긴 양이 조금 많으니, 우선 **홈** 페이지와 **전적 결과 - 전적 종합** 페이지만 살펴보자.

✅ **홈**

홈 페이지에선 단순히 **소환사 랭킹 상위 10명의 정보**만 필요로 한다.

- 소환사 아이콘 경로
- 소환사명
- 랭킹
- 솔로랭크 티어
- 솔로랭크 점수
- 레벨
- 솔로랭크 승리횟수
- 솔로랭크 패배횟수

DB구조를 보면서 랭킹 정보를 DB에 저장할지,

그냥 요청 시마다 받아올까 고민했는데

굳이 해당 데이터를 DB에 저장할 이유가 없다고 생각된다.

아마 프론트에선 revalidate를 추가해 static generation 방식을 사용할 듯!

✅ **전적 결과 - 전적 종합**

전적 종합 페이지는 꽤 많은 데이터들을 필요로 한다.

✔ **기본 정보**

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/337989f4-a354-4271-b668-063a27edee77)

첫 번째는 소환사의 기본 정보이다.

- 소환사 아이콘 경로
- 소환사명
- 레벨
- 랭킹
- 솔로랭크 티어
- 티어 이미지 경로
- 솔로랭크 점수
- 솔로랭크 승리횟수
- 솔로랭크 패배횟수
- 자유랭크 승리횟수
- 자유랭크 패배횟수
- 자유랭크 티어
- 자유랭크 티어 이미지 경로
- 자유랭크 점수

딱 보더라도 **홈 페이지**의 데이터 구조와 유사한 걸 볼 수 있다.

이후 모델 설계 시 참고하도록 하자!

**✔ 최근 남겨진 한마디**

두 번째는 소환사에게 남겨진 한마디이다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/f2042715-5c3e-405e-9973-635a68299097)

이 친구는 추후에 소환사에게 한마디 페이지를 다룰 때 이야기하자!

✔ **챔피언별 숙련도**

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/1f011425-2aa8-4757-988c-07102b593205)

세 번째는 높은 숙련도 순서의 챔피언 3개 이다.

- 챔피언 이미지 경로
- 챔피언 이름
- 숙련도
- 레벨
- 최근 플레이한 시간
- 

✔ **최근 게임 분석**

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/b0b4eb09-2c6e-4e12-add5-1eb2015af46b)

네 번째는 전적 결과 페이지에서 다루는 최근 20 게임의 종합 분석이다.

- 총 승리 횟수
- 총 패배 횟수
- 평균 킬수
- 평균 데스수
- 평균 어시스트수
- 평균 킬관여율
- 포지션별 선택횟수
- 포지션별 승률

✔ **한 게임의 요약 정보**

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/2ed73998-6bed-4c9c-8904-d2305bdb2470)

다음은 한 게임의 정보인데, 데이터의 종류를 두 가지로 나누려고 한다.

크게 **게임 자체의 정보**와, **소환사의 인게임 데이터**이다.

👉 **게임 자체의 정보**

- 게임 타입
- 게임 길이
- 게임을 플레이한 시간
- 승리 팀
- 각 소환사의 챔피언 아이콘 경로
- 각 소환사의 소환사명

👉 **인게임 데이터**

- 챔피언 이미지 경로
- 스펠0 이미지 경로
- 스펠1 이미지 경로
- 레벨
- 킬 수
- 데스 수
- 어시스트 수
- 아이템0~6 이미지 경로
- 킬관여율
- 골드획득량
- CS량
- 분당CS량

두 정보를 한번에 다루게 되면 **한 게임의 요약 정보**에 **소환사 인게임 데이터**를 추가해 줘야 하는데,

추후에 나올 게임 세부 정보를 생각해 보면 두 데이터를 독립적으로 받아오는 게 맞다고 생각된다.

✔ **한 게임의 세부정보 - 전적 총합**

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/56ba8a80-8b57-4c39-ae08-4c0db32ff707)

다음은 한 게임의 세부 정보이다.

여기서도 한 게임의 정보와 각 소환사의 데이터를 분리해야 하는가, 라고 생각했지만,

한 게임의 정보에서 필요한 데이터가 전부 플레이어에게 종속되어있다.

대신 **팀 별**로 데이터를 따로 받아오는 것이 프론트에서 다루기도 더 쉬울 것 같다.

👉 **한 팀의 데이터**

- 총 타워 부신 수
- 총 바론 킬수
- 총 드래곤 킬수
- 승리 여부
- 총 킬수
- 총 골드 획득량
- 팀원들의 인게임 데이터

이 때 **팀원들의 인게임 데이터** 는 위에서 다룬 인게임 데이터에서 와드 설치, 와드 제거, 제어와드 만 추가되면 될 것 같다!

**✔ 한 게임의 세부정보 - 팀 분석**

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/490e4269-0d76-4825-9663-df528de6ae15)

다음은 팀 분석이다.

👉 **각 플레이어의 데이터**

- 플레이한 챔피언 이미지 경로
- 챔피언 킬 수
- 골드 획득량
- 챔피언에게 가한 피해량
- 와드 설치
- 받은 피해량 CS

이 데이터 또한 모델 구현 시 **인게임 데이터**로 한번에 관리할 수 있을 것 같다!

✔ **한 게임의 세부정보 - 빌드**

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/ae24fa10-3557-4076-8c03-b2321639c7c9)

마지막, **빌드**이다.

빌드에서 필요한 데이터는 세 가지로 나뉘는데, 바로

**시간 별 아이템 구매**, **스킬을 찍은 순서**, **룬 특성 정보**이다.

스킬을 찍은 순서와 시간대 별 아이템 구매는 **timeline**이라는 데이터로 한번에 저장하고 있지만,

두 데이터의 타입이 다른 만큼 별개의 모델로 저장하는 게 맞다고 생각된다.

👉 **아이템 빌드**

- 아이템 구매 시간
- 아이템 구매 리스트

👉 **스킬 빌드**

- 스킬을 찍은 레벨
- 찍은 스킬

👉 **룬**

- 주요 룬 이미지 경로
- 주요 룬 이름
- 주요 룬의 서브룬 이미지 경로 리스트
- 서브룬 이미지 경로
- 서브룬 이름
- 서브룬의 서브룬 이미지 경로 리스트
- 특성 이미지 경로 리스트

이렇게 한 페이지에서 받아와야 하는 데이터 리스트를 알아보았다.

백엔드에서 raw data를 보내준 뒤, 프론트에서 데이터를 가공할 수도 있고,

백엔드에서 모든 데이터를 가공한 뒤 프론트에서는 단순한 출력만 진행할 수도 있지만,

데이터에 관련된 모든 조작은 백엔드에서 진행하는 것이 맞다고 생각한다.

지금 당장은 눈에 보이는 성과가 없어보이지만, 백엔드를 잘 설계하면 그만큼 프론트에서 편할거라는 생각으로 임하자~!
