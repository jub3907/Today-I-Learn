### Provider
Provider를 확인해보자. \
문서에서 말하기로는 Provider가 가장 핵심적인 역할을 하는 곳이라고 한다. \
많은 Nest의 기본 클래스들은 Provider로서 취급된다. (services, repositories, factories, helpers 등) \
Provider의 기본적인 아이디어는 **의존성을 주입하는 것**이다. \
즉, 객체들이 다른 것들과 다양한 관계를 만들 수 있다는 것이다. \
하나의 Provider는 @Injectable() 데코레이터로 간단하게 클래스에 annotated된 형태이다.

![image](https://user-images.githubusercontent.com/58246682/147655538-3707093f-9382-476b-a10f-0f5f92c874c7.png)

### Services
CatsService는 데이터 저장과 검색에 대한 일을 한다. \
그리고 이것은 CatController에서 동작하도록 설계되었다. \
그래서 이러한 경우는 provider로서 정의되기 좋다. \
따라서, 우리는 @Injectable 데코레이터로 이 클래스를 데코레이트 하면 된다.\
service를 CLI를 통해서 만들 수 있는데, 아래와 같은 서비스는 nest g service cats 명령어로 실행 가능하다.

```javascript
// cats.service.ts

import { Injectable } from "@nestjs/common";
import { Cat } from "./interfaces/cat.interface";

@Injectable()
export class CatService {
  private readonly cats: Cat[] = [];

  create(cat: Cat) {
    this.cats.push(cat);
  }

  findAll(): Cat[] {
    return this.cats;
  }
}
```


``` javascript
// interfaces/cats.interface.ts
export interface Cat {
  name: string;
  age: number;
  bread: string;
}
```

위와 같이 CatsService는 두 메서드와 하나의 프로퍼티를 가지고 있는 클래스이다. \
새로운 특정은 `@Injectable()` 데코레이터를 사용했다는 것인데, 이 데코레이터는 메타데이터를 붙여준다. \
그 메타데이터는 Nest에게 이 클래스가 Provider라는 점을 알려준다.\
이 서비스를 CatsController에 담아보면 아래와 같다.

```javascript
import { Controller, Get, Post, Body } from "@nestjs/common";
import { CreateCatDto } from "./dto/create-cat.dto";
import { CatsService } from "./cats.service";
import { Cat } from "./interfaces/cat.interface";

@Controller("cats")
export class CatsController {
  constructor(private catsService: CatService) {}

  @Post()
  async create(@Body() createCatDto: CreateCatDto) {
    this.catsService.create(createCatDto);
  }

  @Get()
  async findAll(): Promise<Cat[]> {
    return this.catsService.findAll();
  }
}
```

CatsService는 class의 생성자를 통해서 주입된다. \
`@Injectable()`를 사용하면, 뱔도의 작업 없이(constructor에서 담아주는 것을 빼고) 클래스 내부에서 this로 접근해 사용할 수 있게 된다. 

### Scopes
Provider는 일반적으로 애플리케이션의 라이프타임과 동일한 라이프타임을 갖는다. \
애플리케이션이 시작되면, 모든 의존성들이 주입되고, 그리고 모든 Provider가 인스턴스화된다. \
마찬가지로, 애플리케이션이 종료되면, 각 Provider들은 파괴된다. \
하지만, Provider의 라이프타임을 request-scope로 만들 수도 있다. \
해당 내용은 [이 글](https://docs.nestjs.com/fundamentals/injection-scopes)에서 읽어볼 수 있다.

### Custom providers
Nest는 provider 사이의 관계를 정리해주는 내장된 제어 역전 (IoC) 컨테이너가 있다. \
제어의 역전과 의존성 주입과 관련된 OOP와 연관된 개념은 [이 링크](https://develogs.tistory.com/19)에서 확인해보자. \
이러한 특징들은 위에서 언급했던 의존성 주입과 관련된 특징들에 기반해 있다. \
`@Injectable()` 데코레이터는 사실 Provider를 정의하는 유일한 방법은 아니고, \
일반적인 값, 클래스들, 비동기 또는 동기 factories를 사용할 수도 있다. 
[링크](https://docs.nestjs.com/fundamentals/custom-providers)

### 선택적(Optional) Providers
의존성 중에서는 반드시 필요 없는 의존성이 존재할 수도 있다. \
즉 옵셔널하게 사용되는 경우가 있다는 뜻인데 이러한 경우에서는 `@Optional()`데코레이터를 생성자에 사용해줄 수 있다.

```javascript
import { Injectable, Optional, Inject } from "@nestjs/common";

@Injectable()
export class HttpService<T> {
  constructor(@Optional() @Inject("HTTP_OPTIONS") private httpClient: T) {}
}
```

### Property-based Injection
위에서 계속 사용된 기술들은 생성자 기반의 주입이라고 (class-based injection) 볼 수 있다. \
특별한 경우에는 프로퍼티 기반의 주입 (Property-based injection)을 사용할 수도 있는데, \
예를 들어서 최상위 클래스가 하나 또는 다수의 providers에게 의존성을 가지고 있다면, \
이러한 경우에 하위 클래스에서 `super`를 통해 넘겨주는 것은 그렇게 좋은 방법은 아니다. \
이러한 경우를 피하기 위해서 `@Inject()` 데코레이터를 프로퍼티에게도 사용할 수 있다.

```javascript
import { Injectable, Inject } from "@nestjs/common";

@Injectable()
export class HttpService<T> {
  @Inject("HTTP_OPTIONS")
  private readonly httpClient: T;
}
```

### Provider 등록
위 과정을 통해서 CatsService라는 Provider를 만들어낸 것으로 볼 수 있다. \
그리고 이는 CatsController에서 소비될 것이다. \
따라서 이 서비스를 Nest에 등록하고 주입되도록 해야하는데, \
이는 `app.module.ts`에서 `providers` 배열에 서비스를 더해주면 된다.

```javascript
import { Module } from "@nestjs/common";
import { CatsController } from "./cats/cats.controller";
import { CatsService } from "./cats/cats.service";

@Module({
  controllers: [CatsController],
  providers: [CatsService]
})
export class AppModule {}
```

지금까지 예시에서 프로젝트의 디렉토리 구조는 다음과 같다.
```
src/
    cats/
        dto/
            create-cat.dto.ts
        interfaces/
            cat.interface.ts
        cats.service.ts
        cats.controller.ts
app.module.ts
main.ts
```
