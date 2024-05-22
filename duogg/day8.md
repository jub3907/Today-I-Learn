## **ranking 변경**

기존에 league-entry에 구현해 두었던 ranking 뮤테이션을 summoner-basic으로 옮기게 되었다.

이전 포스트에서도 언급했지만, ranking 뮤테이션은

소환사 레벨과 같은 기본정보들이 존재하지 않았다.😢

또한, 기존에 soleRank, freeRank의 경우

각 ResolveField마다 한 번의 API 호출을 하기때문에

랭킹 정보를 전부 받아오기 위해선 총 21번의 API 호출이 필요하므로, 이를 줄일 필요가 있었다.

이 경우엔 DB를 사용하기로 했다.

basicSummonerInfo, ranking 쿼리가 들어올 때 소환사 이름으로 소환사정보를 받아온 뒤

소환사의 id값으로 Entries, 즉 리그 정보를 받아와 DB에 업데이트 한다.

그 뒤, ResolveField에서는 API를 호출하는 것이 아니라 DB에서 데이터를 받아오는 구조이다!

이러한 방식을 선택해 21번의 API 호출을 11번으로 줄일 수 있었다.

물론.. 이것도 적은 API 호출은 아니지만, 기존 방식의 ranking 뮤테이션은 API 호출 횟수가 너무 많아

뮤테이션이 실행 불가능한 현상이 발생했었다. 정말 다행이다.. 😢

이외의 뮤테이션은 따로 글로 적지 않고, 위와 동일한 방식을 사용해 구현한다.

다만 Mutation을 구현하면서, 혹은 구현하고 난 뒤 했던 고민들과 문제점,

알아두면 좋을만한 점들을 정리하려고 한다.

## **Mastery 관련 구현하던 중..**

소환사의 챔피언 숙련도를 구현하던 중, 갑자기 이런 생각이 들었다.

현재 구상으로는 클라이언트에서 챔피언 숙련도를 보여줄 때,

해당 챔피언을 **언제** 플레이했는지 데이터가 필요하다.

DB에는 **마지막으로 플레이한 시간**이 Unix millisecond로 저장되어 있기 때문에,

이 데이터를 **몇시간 전, 혹은 몇일 전**으로 변환해서 보여줘야 하는데, 이걸 백엔드에서 할지,

프론트엔드에서 할지 고민이 되었다.

현재 생각으로는 **데이터 자체를 조작**하는것이 아닌 **데이터를 보여주는 방식**을 변환하는 행위이므로

프론트엔드에서 조작하는게 낫다고 생각한다.

물론 이것도 리서치를 하면서 변경될 여지는 존재하지만.. 우선은 이 방식을 택했다!

## **매번 헷갈리는 InputType과 ArgsType**

백엔드 코드를 짜다보면, **InputType과 ArgsType**이 진짜 매번 헷갈린다.

이 역시 내 배움이 아직 부족하다는 의미겠지만.. 다음엔 꼭 기억하자는 의미에서 기록을 남긴다!

InputType과 ArgsType 모두 Query, 혹은 Mutation에서 Arguments들을 받고자 할 때 사용한다.

두 Type은 **코드 작성 시 ,** 또 **GraphQL 요청시** **차이점을 보인다.**

### **코드를 작성할 때**

우선, 둘 다 **Args()** 데코레이터를 사용한다.

다만 InputType은 Args 데코레이터의 인자로 arguments의 이름을 넣어줘야 하고,

ArgsType은 인자로 이름을 넣어주지 않아도 된다.

### **GQL을 사용할 때**

InputType은 Args에 넘겨준 arguments의 이름으로 **하나의 객체**를 보낸다.

```
createAuthor(example: { firstName: "Brendan", lastName: "Eich" })

```

하지만 ArgsType은 **각각의 필드를 따로따로 전송한다**는 차이점이 존재한다!

```
  createAuthor(firstName: "Brendan", lastName: "Eich")

```

이렇게 정리하면 **대체 왜 맨날 헷갈리는지** 모르겠는 명백한 차이점이지만..

..더 열심히 기억하도록 하자!

## **비밀번호 해쉬**

위의 의문점까지 해결하고 나니 **전적 검색 - 전적 총합 페이지**의 뮤테이션들은 전부 구현했다.

이제 **소환사에게 한마디** 페이지를 구현해야 하는데, 이 페이지에선 **작성자의 닉네임과 비밀번호**를 필요로 한다.

근데, 아무리 개인정보가 존재하지 않는다고 해도

DB에 비밀번호를 그대로 저장하는 건 보안 측면에서 좋지 않고 생각된다.

그래서 이 비밀번호를 암호화 해야 하는데, 이 때 사용하는 것이 바로 **해싱**, 혹은 **암호화**이다.

암호화는 **양방향**, 즉 암호화 키를 알면 복호화도 가능하지만

해싱은 **단방향,** 즉 복호화가 불가능하다는 차이점이 존재하다!

nestjs에선 친절하게도 Hashing에 대한 기본적인 가이드를 제공하고 있다.

[Hashing 가이드](https://docs.nestjs.com/security/encryption-and-hashing#hashing)

이 가이드를 참고해 **CryptService**를 생성했다.

```
import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import axios from 'axios';
import { ApiObject, ApiType } from './type/api.type';
import * as bcrypt from 'bcrypt';

@Injectable()
export class CryptService {
  async hashPassword(password: string) {
    const salt = await bcrypt.genSalt();
    return await bcrypt.hash(password, salt);
  }

  async comparePassword(password: string, hash: string) {
    return await bcrypt.compare(password, hash);
  }
}

```

원래는 이러한 유틸리티 함수들을 모은 util service를 만들까 했지만,

지금 당장은 crypt만 필요하다보니 위처럼 cryptService만 만들었다.
