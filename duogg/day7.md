원래는 지난 시간에 이어, 데이터 모델을 설계하는 작업을 진행해야 한다.

하지만 아직 백엔드의 실력이 미천한지라.. 설계된대로 진행된다는 보장이 없더라. 😢

모든걸 완벽하게 설계하고 넘어가고 싶지만, 그러면 기간을 맞추지 못할 것 같다는 생각이 든다.

따라서 **설계를 하긴 하되, 모든걸 검증하지는** 않고 넘어가려고 한다.

물론 모델 설계는 따로 진행하고, 글로 남기지는 않으려고 한다.

코드가 너무 많아서 정리하기가 힘들다...

## **DataDragon 채우기**

오늘 할 작업은 바로 DataDragon을 채우는 것이다.

DataDragon은 라이엇에서 제공하는 데이터 파일이라고 생각하면 편한데,

우리는 이 데이터파일을 정제하여 아이템이나 스펠마다 정해져있는

이미지 파일의 경로를 DB에 저장할 예정이다.

이렇게 저장된 이미지 경로는 클라이언트에서 이미지가 필요할 때 해당 품목의 타입 ( 아이템, 스펠 등 )과

그 품목의 unique key값을 통해 이미지의 경로를 리턴할 수 있게 만들 예정이다.

그럼, 가장 먼저 **API 로직**을 구성해 보자.

API는 여러 리졸버에서 사용될 예정이므로 **common**이라는 폴더를 생성하고,

api service를 추가해 그 안에 로직을 구현한다.

```
//common/api/api.service.ts
import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import axios from 'axios';

@Injectable()
export class SummonerService {
  constructor(private readonly config: ConfigService) {}

  async getDataDragon(version: string, type: string) {
    return await axios.get(
      `http://ddragon.leagueoflegends.com/cdn/${version}/data/ko_KR/${type}.json`,
    );
  }

  private getUri(url: string, parameter: string = null) {
    return (
      this.config.get('api.base') +
      url +
      encodeURI(parameter) +
      `?api_key=${this.config.get('api.key')}`
    );
  }
}

```

지금 당장은 데이터드래곤만 받아오면 되지만, 추후에 사용할 API 호출을 위해

API url과 추가 파라미터를 받아 API uri를 리턴하는 getUri 함수도 생성해 두었다.

~~( 이 getUri 함수는 이후에 네번정도 변경되었다... )~~

또한 이러한 공통 service에 대한 module도 구현하고, 이는 글로벌 모듈로 선언해주자.

```
import { Global, Module } from '@nestjs/common';
import { ApiService } from './api/api.service';

@Global()
@Module({
  providers: [ApiService],
  exports: [ApiService],
})
export class CommonModule {}

```

이제 dataDragon의 서비스와 리졸버를 구현해야 하는데, 여기가 조금 까다롭다.

**모든 데이터의 형태가 다 다르기 때문에..** 전부 각각 파싱해줘야 한다..

우선 아이템 정보부터 파싱해 보자.

```
// datadragon service
import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { DataDragon, DataDragonDocument } from './schema/data-dragon.schema';

@Injectable()
export class DataDragonService {

  parseItem(data: JSON) {
    return Object.keys(data).map((id) => ({ id, path: data[id].image.full }));
  }
}
// datadragon resolver
import { Mutation, Resolver } from '@nestjs/graphql';
import { ApiService } from 'src/common/api/api.service';
import { DataDragonService } from './data-dragon.service';

@Resolver((of) => Boolean)
export class DataDragonResolver {
  version: string;
  constructor(
    private readonly api: ApiService,
    private readonly ddService: DataDragonService,
  ) {
    this.version = '12.1.1';
  }

  @Mutation((returns) => Boolean)
  async updateItemData() {
    const data = await this.api.getDataDragon(this.version, 'item');

    const pathList = this.ddService.parseItem(data.data.data);
    console.log(pathList);
    return true;
  }
}

```

위처럼 API 호출을 통해 데이터를 받아오고, 파싱 함수에 넣어 **id, path**를 키로 갖는 객체 리스트를 생성했다.

이제 이를 기존에 선언해둔 스키마에 mapping하고, DB에 저장해주면 된다.

다음으로 해줘야 할 작업은 **스키마를** 등록해주는 작업이다.

아래와 같이 MongooseModule의 forFeature 메소드를 통해 스키마를 등록할 수 있다.

```
import { Module } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import { DataDragonResolver } from './data-dragon.resolver';
import { DataDragonService } from './data-dragon.service';
import { DataDragon, DataDragonSchema } from './schema/data-dragon.schema';

@Module({
  imports: [
    MongooseModule.forFeature([
      { name: DataDragon.name, schema: DataDragonSchema },
    ]),
  ],
  providers: [DataDragonResolver, DataDragonService],
  exports: [DataDragonService],
})
export class DataDragonModule {}

```

스키마를 모듈에 등록했다면 **@InjectModel()** 데코레이터를 사용하여

DataDragon 모델을 service에 삽입할 수 있다.

```
import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { ApiService } from 'src/common/api/api.service';
import { DataDragon, DataDragonDocument } from './schema/data-dragon.schema';

@Injectable()
export class DataDragonService {
  constructor(
    @InjectModel(DataDragon.name)
    private readonly ddModel: Model<DataDragonDocument>,
  ) {}

  parseItem(data: JSON) {
    return Object.keys(data).map((id) => ({ id, path: data[id].image.full }));
  }
}

```

다음은 DataDragon DTO를 정의한다.

```
// datadragon.dto.ts
export class DataDragonDto {
  type: string;
  base: string;
  pathes: {
    id: string;
    path: string;
  }[];
}

```

이는 service에서 함수의 parameter의 타입을 정의할 때 사용하며, 입력 받은 DTO를 그대로 저장만 해주면 된다.

```
// datadragon service
import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { ApiService } from 'src/common/api/api.service';
import { DataDragonDto } from './dto/data-dragon.dto';
import { DataDragon, DataDragonDocument } from './schema/data-dragon.schema';

@Injectable()
export class DataDragonService {
  constructor(
    @InjectModel(DataDragon.name)
    private readonly ddModel: Model<DataDragonDocument>,
  ) {}

  parseItem(data: JSON) {
    return Object.keys(data).map((id) => ({ id, path: data[id].image.full }));
  }

  async create(dataDragonDto: DataDragonDto) {
    return await this.ddModel.create(dataDragonDto);
  }
}
// datadragon resolver
import { Mutation, Resolver } from '@nestjs/graphql';
import { create } from 'domain';
import { ApiService } from 'src/common/api/api.service';
import { DataDragonService } from './data-dragon.service';

@Resolver((of) => Boolean)
export class DataDragonResolver {
  version: string;
  constructor(
    private readonly api: ApiService,
    private readonly ddService: DataDragonService,
  ) {
    this.version = '12.1.1';
  }

  @Mutation((returns) => Boolean)
  async updateItemData() {
    const data = await this.api.getDataDragon(this.version, 'item');

    const pathList = this.ddService.parseItem(data.data.data);

    const result = await this.ddService.create({
      type: 'item',
      base: '<http://ddragon.leagueoflegends.com/cdn/12.2.1/img/item/>',
      pathes: pathList,
    });

    if (result) {
      return true;
    } else {
      return false;
    }
  }
}

```

👉 **결과**

!https://s3.ap-northeast-2.amazonaws.com/images.codemate.kr/images/BearBear/post/1642854292623/Untitled-8.png

이렇게 아이템 하나에 대한 dataDragon 등록이 끝났다.

다른 데이터 (소환사아이콘과 티어, 챔피언 이미지, 스펠, 룬, 스텟 )에 대해 동일한 작업을 수행하자.

원래는 하나의 mutation에 파라미터를 받아서 API를 호출하겠지만,

이 경우엔 데이터 파싱 함수가 다 다르다보니, 적용이 조금 어렵다고 생각했다.

따라서 각 데이터당 하나의 뮤테이션으로 구현했다.

```
import { Mutation, Resolver } from '@nestjs/graphql';
import { create } from 'domain';
import { ApiService } from 'src/common/api/api.service';
import { DataDragonService } from './data-dragon.service';

@Resolver((of) => Boolean)
export class DataDragonResolver {
  version: string;
  constructor(
    private readonly api: ApiService,
    private readonly ddService: DataDragonService,
  ) {
    this.version = '12.1.1';
  }

  @Mutation((returns) => Boolean)
  async updateItemData() {
    const data = await this.api.getDataDragon(this.version, 'item');
    const pathList = this.ddService.parseItemAndIcon(data.data.data);

    await this.ddService.delete('item');

    const result = await this.ddService.create({
      type: 'item',
      base: '<http://ddragon.leagueoflegends.com/cdn/12.2.1/img/item/>',
      pathes: pathList,
    });

    if (result) {
      return true;
    } else {
      return false;
    }
  }

  @Mutation((returns) => Boolean)
  async updateIconData() {
    const data = await this.api.getDataDragon(this.version, 'profileicon');
    const pathList = this.ddService.parseItemAndIcon(data.data.data);

    await this.ddService.delete('profileicon');

    const result = await this.ddService.create({
      type: 'profileicon',
      base: '<http://ddragon.leagueoflegends.com/cdn/12.2.1/img/profileicon/>',
      pathes: pathList,
    });

    if (result) {
      return true;
    } else {
      return false;
    }
  }

  @Mutation((returns) => Boolean)
  async updateSummonerData() {
    const data = await this.api.getDataDragon(this.version, 'summoner');
    const pathList = this.ddService.parseSpellAndChampion(data.data.data);

    await this.ddService.delete('summoner');

    const result = await this.ddService.create({
      type: 'summoner',
      base: '<http://ddragon.leagueoflegends.com/cdn/12.2.1/img/spell/>',
      pathes: pathList,
    });

    if (result) {
      return true;
    } else {
      return false;
    }
  }

  @Mutation((returns) => Boolean)
  async updateChampionData() {
    const data = await this.api.getDataDragon(this.version, 'champion');

    const pathList = this.ddService.parseSpellAndChampion(data.data.data);

    await this.ddService.delete('champion');

    const result = await this.ddService.create({
      type: 'champion',
      base: '<http://ddragon.leagueoflegends.com/cdn/12.2.1/img/champion/>',
      pathes: pathList,
    });

    if (result) {
      return true;
    } else {
      return false;
    }
  }

  @Mutation((returns) => Boolean)
  async updateRuneData() {
    const data = await this.api.getDataDragon(this.version, 'runesReforged');

    const pathList = this.ddService.parseRune(data.data);

    await this.ddService.delete('runes');

    const result = await this.ddService.create({
      type: 'runes',
      base: '<http://ddragon.leagueoflegends.com/cdn/img/>',
      pathes: pathList,
    });

    if (result) {
      return true;
    } else {
      return false;
    }
  }
}
import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { ApiService } from 'src/common/api/api.service';
import { DataDragonDto } from './dto/data-dragon.dto';
import { DataDragon, DataDragonDocument } from './schema/data-dragon.schema';

@Injectable()
export class DataDragonService {
  constructor(
    @InjectModel(DataDragon.name)
    private readonly ddModel: Model<DataDragonDocument>,
  ) {}

  parseItemAndIcon(data: JSON) {
    return Object.keys(data).map((id) => ({ id, path: data[id].image.full }));
  }

  parseSpellAndChampion(data: JSON) {
    return Object.keys(data).map((id) => ({
      id: data[id].key,
      path: data[id].image.full,
    }));
  }

  parseRune(data: Array<any>) {
    return data
      .map(({ id, icon, slots }) =>
        [{ id, path: icon }].concat(
          slots
            .map(({ runes }) =>
              runes.map(({ id, icon }) => ({ id, path: icon })).flat(),
            )
            .flat(),
        ),
      )
      .flat();
  }

  async create(dataDragonDto: DataDragonDto) {
    return await this.ddModel.create(dataDragonDto);
  }

  async delete(type: string) {
    return await this.ddModel.deleteOne({ type });
  }
}

```

참고로, GraphQL 모듈도 App모듈에 등록해줘야한다. 😊

### 

### 

## **API 구현**

데이터드래곤 작업도 얼추 마무리 되었으니, 이제 실제 API를 호출하고,

이를 DB에 저장하는 작업을 해보자.

이후에 실제 코드를 구현하게 되면 인터셉터를 사용하는 방식도 생각하고 있지만

지금 당장은 아무것도 없기 때문에.. 아직은 적용이 조금 어려울 것 같다.

우선은 각 서비스에 API호출 코드와 리턴받은 데이터 파싱, 그리고 DB 저장 코드까지 구현해 주자.

( 물론, DB에 저장하지 않는 경우, 혹은 파싱이 필요 없는 경우는 제외한다. )

맨 처음 작업은 **API 서비스에 API 호출 코드 구현**이다.

지금은 데이터드래곤을 위한 **getDataDragon** 함수만 구현되어있지만,

이제 API 호출을 위한 **getApiResult**함수도 추가해 준다.

이 때, 함수는 간결하게 api 호출 타입과 추가 파라미터만 받을 수 있도록 하기위해

api 타입을 먼저 선언해 준다.

```
export const ApiObject = {
  summonerByName: {
    path: '<https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-name/>',
    behind: '',
  },
  entriesById: {
    path: '<https://kr.api.riotgames.com/lol/league/v4/entries/by-summoner/lol/league/v4/entries/by-summoner/>',
    behind: '',
  },
  challengersByQueue: {
    path: '<https://kr.api.riotgames.com/lol/league/v4/challengerleagues/by-queue/>',
    behind: '',
  },
  masteryById: {
    path: '<https://kr.api.riotgames.com/lol/champion-mastery/v4/scores/by-summoner/lol/champion-mastery/v4/champion-masteries/by-summoner/>',
    behind: '',
  },
  matchBymatchId: {
    path: '<https://asia.api.riotgames.com/lol/match/v5/matches/lol/match/v5/matches/>',
    behind: '',
  },
  matchesByPuuid: {
    path: '<https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/>',
    behind: '/ids',
  },
  timelineBymatchId: {
    path: '<https://asia.api.riotgames.com/lol/match/v5/matches/>',
    behind: '/timeline',
  },
} as const;

export type ApiType = keyof typeof ApiObject;

```

타입 선언 이후엔 바로 Api 호출 코드를 생성한다.

```
import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import axios from 'axios';
import { ApiObject, ApiType } from './api.type';

@Injectable()
export class ApiService {
  constructor(private readonly config: ConfigService) {}

  async getDataDragon(version: string, type: string) {
    return await axios.get(
      `http://ddragon.leagueoflegends.com/cdn/${version}/data/ko_KR/${type}.json`,
    );
  }

  async getApiResult(type: ApiType, variable: string, params: any = {}) {
    return await axios.get(this.getUri(type, variable), {
      params: {
        ...params,
        api_key: this.config.get('api.key'),
      },
    });
  }

  private getUri(type: ApiType, variable: string) {
    return ApiObject[type].path + encodeURI(variable) + ApiObject[type].behind;
  }
}

```

api 타입을 좀 더 간결하게 만들고 싶었지만, api 호출을 위한 베이스 주소나

Api의 종단점이 파라미터가 아닌 경우가 존재해 위와같이 코드를 구현할 수 밖에 없었다..

조금 더 나은 방법이 있는지 고민해 보자.

summoner의 API 호출 및 DB저장은 아래와 같이 구현되었다.

- summoner DTO 선언

```
import { CommentDto } from './comment.dto';
import { MasteryDto } from './mastery.dto';

export class SummonerDto {
  accountId: string;
  profileIconId: number;
  id: string;
  name: string;
  puuid: string;
  summonerLevel: number;
  comment?: CommentDto[];
  masteries: MasteryDto[];
}

// mastery.dto.ts
export class MasteryDto {
  championId: string;
  championLevel: number;
  championPoints: number;
  lastPlayTime: number;
}

// comment.dto.ts
export class CommentDto {
  createdAt: number;
  nickname: string;
  password: string;
  text: string;
}

```

- 서비스에 API 호출 함수와 DB저장 함수 선언

```
import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { ApiService } from 'src/common/api/api.service';
import { SummonerDto } from './dto/summoner.dto';
import { Summoner, SummonerDocument } from './schema/summoner.schema';

@Injectable()
export class SummonerService {
  constructor(
    @InjectModel(Summoner.name)
    private readonly SummonerModel: Model<SummonerDocument>,
    private readonly api: ApiService,
  ) {}

  async getSummoner(name: string) {
    return await this.api.getApiResult('summonerByName', name);
  }

	async create(data: SummonerDto) {
    await this.SummonerModel.create(data);
  }

  async findByName(name: string) {
    return await this.SummonerModel.findOne({ name }, 'name puuid');
  }
}

```

- 테스트용 Mutation, query 코드 생성

```
import { ConfigService } from '@nestjs/config';
import { Args, Mutation, Resolver, Query } from '@nestjs/graphql';
import axios from 'axios';
import { SummonerService } from './summoner.service';

@Resolver()
export class SummonerResolver {
  constructor(private readonly summonerService: SummonerService) {}

  /**
   *   test용 코드
   * */
  @Mutation((returns) => String)
  async testUpdateSummoner(@Args('name') name: string) {
    const apiResult = await this.summonerService.getSummoner(name);

    await this.summonerService.create(apiResult.data);

    return 'test';
  }

  @Query((returns) => String)
  async getSummoner(@Args('name') name: string) {
    const result = await this.summonerService.findByName(name);
    return result.name;
  }
}

```

위와 같은 방식으로, API 호출을 모두 구현해 준다.

### **❗ 변경사항**

구현하던 중, 굳이 매치 정보에 timeline 데이터를 추가할 필요가 없다고 생각되었다.

timeline 데이터는 **전적 결과** - 전적 종합 페이지에서 **빌드**에서만 필요한데,

사용처가 한정적인 데이터를 위해 매치 정보 하나 저장할 때 마다

30만줄이 넘는 데이터를 받아오고, 파싱할 필요는 없다고 생각된다.

따라서 match 스키마에서 timeline 스키마 삭제했다!

또한, 이 경우 timeline이 match에 종속된 데이터가 아니기 때문에,

timeline 폴더를 새로 생성하는게 맞다고 생각했다.

### **❗ 구현을 완료하고..**

API를 전부 구현한 뒤, 약간의 의문점이 생겼다.

API를 호출하는 코드가 전부 개별 Service에 나뉘어져 있는데, 이게 과연 유지보수의 용이성을 증진시킨다고 할 수 있을까?

이 부분에서 개발 커뮤니티에 리서치를 해 봤는데, 얻은 답변은

**별도의 service를 만들고, controller를 가져오는 것이 좋습니다.**이였다.

이해한 바로는 **API를 위한 service,** 즉 지금 단순히 API 호출을 위한 함수만 구현되어있는 서비스에

종류 별 API 호출 코드를 작성하라는 말인 것 같은데,

이게 맞는지, 혹은 지금 작성한 방식이 맞는지 조금 더 고민해봐야 할 것 같다.

## **Mutation 구현**

이제, Mutation을 하나씩 구현해보자.

첫 번째로 구현할 mutaiton은 소환사의 기본 정보를 받아오는 basicSummonerInfo 이다.

**model 정의 → 필요한 비즈니스 로직을 Service에 정의 → Mutation 구현** 순서로 진행한다.

### **model 정의**

👉 **League-Entry Model**

```
import { Field, ObjectType } from '@nestjs/graphql';

@ObjectType()
export class LeagueEntryModel {
  @Field((type) => String)
  summonerId: string;

  @Field((type) => String)
  summonerName: string;

  @Field((type) => String)
  queueType: string;

  @Field((type) => String)
  tier: string;

  @Field((type) => String)
  rank: string;

  @Field((type) => Number)
  leaguePoints: number;

  @Field((type) => Number)
  wins: number;

  @Field((type) => Number)
  losses: number;
}

```

👉 **Summoner-Entry Model**

```
import { ObjectType, OmitType, PickType } from '@nestjs/graphql';
import { LeagueEntryModel } from 'src/league-entry/model/league-entry.model';

@ObjectType()
export class SummonerEntryModel extends PickType(LeagueEntryModel, [
  'tier',
  'rank',
  'leaguePoints',
  'wins',
  'losses',
] as const) {}

```

👉 **Summoner-Basic Model**

```
import { Field, ObjectType } from '@nestjs/graphql';
import { SummonerEntryModel } from './summoner-entry.model';

@ObjectType()
export class SummonerBasicModel {
  @Field((type) => String)
  iconPath: string;

  @Field((type) => String)
  name: string;

  @Field((type) => Number)
  summonerLevel: number;

  @Field((type) => Number)
  profileIconId: number;

  @Field((type) => String)
  id: string;

  @Field((type) => SummonerEntryModel, { nullable: true })
  soleRank: SummonerEntryModel;

  @Field((type) => SummonerEntryModel, { nullable: true })
  freeRank: SummonerEntryModel;
}

```

### **Service에 함수 구현**

기본정보를 받아오는데에 필요한건 **소환사 정보를 받아오는** 함수와,

**소환사 정보를 통해 리그 정보를 받아오는 함수**와,

**소환사 아이콘의 id값으로 이미지 경로를 받아오는 함수**

세 가지가 필요하다.

소환사의 정보를 받아오는 함수는 이미 API로 구현되어 있으니 패스하고,

리그 정보는 **LeagueEntry Service**에,

**이미지 경로**는 **DataDragon Service**에서 담당하기로 했으니 각 서비스에 구현하자.

👉 **LeagueEntry Service, getEntryByType**

```
async getEntryByType(
    summonerId: string,
    queueType: 'RANKED_SOLO_5x5' | 'RANKED_FLEX_SR',
  ) {
    const entries = (await this.api.getApiResult('entriesById', summonerId))
      .data;

    return entries.find(
      ({ queueType: type }: LeagueEntryDto) => type == queueType,
    );
  }

```

깔끔하게 메모리를 사용하지 않고 구현하고 싶었지만..

우리의 API는 정보 하나만 보내주지 않기 때문에, 받아온 정보에서 원하는 정보만 필터링해 리턴해준다.

👉 **DataDragon Service, getImagePath**

```
async getImagePath(type: string, key: number | string) {
    const dataDragon = await this.ddModel.findOne({
      type,
    });

    return (
      dataDragon.base +
      dataDragon.pathes.find(({ id }) => id === key.toString()).path
    );
  }

```

이 함수를 구현하면서, 내가 DB구조를 조금 잘못짠건가..라는 생각이 들었다.

지금 DB 구조에선 pathes라는 nested array에 각각의 데이터가 저장되어 있다 보니,

type을 사용해 데이터를 받아오면, item 타입의 모든 데이터가 받아와진다.

받아온 데이터에서 filtering을 거치는 걸 보면

type과 id를 key값으로 저장할걸 그랬나..라는 생각이 들었다.

이건 추후에 구현하고 난 뒤 생각하도록 하겠다. 시간이 많이 없다..

- Mutation 구현

```
import { ConfigService } from '@nestjs/config';
import {
  Args,
  Mutation,
  Resolver,
  Query,
  ResolveField,
  Parent,
} from '@nestjs/graphql';
import axios from 'axios';
import { DataDragonService } from 'src/data-dragon/data-dragon.service';
import { LeagueEntryService } from 'src/league-entry/league-entry.service';
import { SummonerBasicModel } from './model/summoner-basic.model';
import { SummonerEntryModel } from './model/summoner-entry.model';
import { SummonerService } from './summoner.service';

@Resolver((of) => SummonerBasicModel)
export class SummonerBasicResolver {
  constructor(
    private readonly summonerService: SummonerService,
    private readonly leagueEntryService: LeagueEntryService,
    private readonly dataDragonService: DataDragonService,
  ) {}

  @ResolveField((returns) => String)
  iconPath(@Parent() summoner: SummonerBasicModel) {
    return this.dataDragonService.getImagePath(
      'profileicon',
      summoner.profileIconId,
    );
  }

  @ResolveField((returns) => SummonerEntryModel)
  async soleRank(@Parent() summoner: SummonerBasicModel) {
    return await this.leagueEntryService.getEntryByType(
      summoner.id,
      'RANKED_SOLO_5x5',
    );
  }

  @ResolveField((returns) => SummonerEntryModel)
  async freeRank(@Parent() summoner: SummonerBasicModel) {
    return await this.leagueEntryService.getEntryByType(
      summoner.id,
      'RANKED_FLEX_SR',
    );
  }

  @Mutation((returns) => SummonerBasicModel)
  async basicSummonerInfo(@Args('name') name: string) {
    const apiResult = await this.summonerService.getSummoner(name);

    await this.summonerService.updateSummoner(
      apiResult.data['accountId'],
      apiResult.data,
    );

    return apiResult.data;
  }
}

```

마지막으로, Mutation 구현이다.

**basicSummonerInfo**라는 뮤테이션 내부에서 리그 정보나 이미지 경로를 받아온 뒤

각각의 데이터들을 추가해 리턴해줄까 생각했지만,

그냥 ResolveField를 사용해 데이터를 넣어주기로 했다.

중간의 updateSummoner는 소환사를 닉네임으로 검색하기 때문에

닉네임을 변경했을 경우 comment 데이터가 초기화되는 현상이 발생할 수 있다.

따라서 소환사 정보에서 변하지 않는 값인 accountId를 사용해

데이터를 검색하고, 소환사 정보를 최신화 하는 코드이다.

!https://s3.ap-northeast-2.amazonaws.com/images.codemate.kr/images/BearBear/post/1642854552579/Untitled-9.png

잘 나온다..ㅎㅎ

리그 정보는 플레이하지 않으면 null값이 나오게 된다!

다음 구현은 ranking이다.

ranking은 생각보다 간단하게 구현하였다.

소환사의 **리그 정보**를 담당하는 league-entry에서 Challenger 리그의 소환사 정보를 받아오고,

해당 데이터를 파싱해서 리턴해주면 되었다.

받아온 뒤 간단한 파싱 과정만 거치면 우리가 원하는 정보가

그대로 나오다보니 따로 ResolveField를 추가해주지 않아도 되었다!

👉 **LeagueEntryModel**

```
import { Field, ObjectType } from '@nestjs/graphql';

@ObjectType()
export class LeagueEntryModel {
  @Field((type) => String)
  summonerId: string;

  @Field((type) => String)
  summonerName: string;

  @Field((type) => String)
  queueType: string;

  @Field((type) => String)
  tier: string;

  @Field((type) => String)
  rank: string;

  @Field((type) => Number)
  leaguePoints: number;

  @Field((type) => Number)
  wins: number;

  @Field((type) => Number)
  losses: number;
}

```

👉 **LeagueEntryservice**

```
async getChallengerEntries() {
    return await this.api.getApiResult('challengersByQueue', 'RANKED_SOLO_5x5');
  }

getRanking(entries: LeagueEntryDto[]) {
    return entries
      .sort(({ leaguePoints: a }, { leaguePoints: b }) => {
        if (a > b) {
          return -1;
        } else if (a < b) {
          return 1;
        } else {
          return 0;
        }
      })
      .slice(0, 10);
  }

```

👉 **LeagueEntryResolver**

```
@Query((returns) => [LeagueEntryModel])
  async Ranking() {
    const apiResult = await this.leagueEntryService.getChallengerEntries();
    const parsed = this.leagueEntryService.parseChallengerEntries(
      apiResult.data,
    );

    return this.leagueEntryService.getRanking(parsed);
  }

```

근데, 구현을 하고 리턴된 데이터를 보면서 곰곰히 생각해 보았다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/7d4feafd-d480-4d98-9cbd-390106302bec)

뭔가..없는 것 같은데..? 라는 생각이 불현듯 들기 시작했다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/4beced09-c585-4fc3-ac53-b433661e51fb)

..와이어프레임에서 간단한 데이터로 처리하다보니,

이미지로 생각을 안했던 **소환사 아이콘**이 없다는 사실을 이제야 깨달았다.

문제는 **Challenger 리그 정보를 받아오는 API에선 소환사 아이콘에 대한 정보를 제공해주지 않는다는 점**이다.

따라서 위에서 필터링한 상위 10명에 대해 각각 **소환사 정보를 받아오는 API**를 실행하고,

그 API의 데이터에서 소환사 아이콘 정보를 받아온 뒤,

아이콘 정보에 맞는 아이콘 경로를 DB에서 받아오는 작업이 추가로 필요하게 되었다.

..말만 들어도 작업량이나 사용될 리소스의 량이 상당한데,

다른 접근 방법이 없을지 고안해봐야겠다.

