### Modules
Module은 `@Module`이라는 데코레이터에게 어노테이트 된 클래스이다. \
`@Module` 데코레이터는 Nest가 앱의 구조를 조직할 수 있는 메타데이터를 제공해준다.

![image](https://user-images.githubusercontent.com/58246682/147656724-b4a28781-9837-4c20-ae63-613b669ada1c.png)


각 앱은 적어도 하나의 root module이 있다. \
이 모듈은 Nest가 `application graph`를 만들어낼 시작 포인트를 잡아준다. \
`application graph`는 Nest가 `Module`과 `Provider` 관계와 의존성을 결정할 때 사용하는 내부적인 데이터 구조이다. \
아주 작은 앱들은 이론적으로 `root modul`e만 필요할 수 있지만, 이런 경우가 보통 일반적인 것은 아니다.\
대부분의 앱에서는 여러가지의 모듈들을 사용하게 되고 각각은 연관된 `capabilities` 들의 모음으로 캡슐화 되게 된다.

`@Module()`이 인자에서 갖는 객체는 아래 모습과 같다.

* providers: Nest의 injector (typedi와 같은)에 의해 인스턴스화되고, 인스턴스들은 이 모듈 안에서 최소한으로 공유된다.
* controllers: 해당 모듈에서 정의된, 인스턴스화 되어야 하는 컨트롤러의 모음이다.
* imports: 임포트된 모듈들의 리스트이다. 이 리스트의 모듈들은 데코레이터에 사용 중인 모듈에서 필요한 providers를 export 하고 있어야 한다.
* exports: providers의 하위 집합으로, 데코레이터를 사용 중인 모듈이 제공받은 Provider의 일부를 내보낼 수 있다. 이는 다른 모듈에서 import 할 때 사용된다.
### 특징 단위의 모듈 (Feature module)
CatsController와 CatsService는 같은 application 영역이다. \
서로 연관이 깊기 때문에, feature module로 묶을 수 있다. \
feature module은 간단하게 특정한 특징들과 연관된 코드를 함께 조직화한다. \
이렇게 함으로써, 코드를 조직적이게 유지하고, 명확한 경계를 세울 수 있다. \
이는 SOLID 원칙과 함께 개발을 할 때 복잡성을 줄여준다.

```javascript
// cats/cats.module.ts

import {Module} from "@nestjs/common";
import {CatsController} from "./cats.controller";
import {CatsService} from "./cats.service";

@Module({
    controllers: [CatsController],
    providers: [CatsService]
})
export class CatsModule {}
```
CLI를 사용해서 만들려면 `nest g module cats`로 만들 수 있다.

위에서 정의한 cats.module.ts와 연관된 모든 모듈을 cats 디렉토리 아래에 둔다. \
마지막으로, 루트 모듈에 CatsModule을 임포트 시켜주면 된다.

```javascipt
// app.module.ts

import {Module} from "@nestjs/common";
import {CatsModule} from "./cats/cats.module";

@Module({
    imports: [CatsModule]
})
export class AppModule{}
```
결과적으로 디렉토리 구조는 아래와 같이 생겼다.


```javascipt
src/
    cats/
        dto/
            create-cat.dto.ts
        interfaces/
            cat.interface.ts
        cats.service.ts
        cats.controller.ts
        cats.module.ts
    app.module.ts
    main.ts
```
### 공유되는 모듈
Nest에서는 `Module`이 기본적으로는 `Singleton`이다. \
따라서 같은 어떠한 `Provider`의 인스턴스든 여러 모듈에서 공유할 수 있다.

![image](https://user-images.githubusercontent.com/58246682/147656833-4656a05f-af9b-4f6a-8ce5-2dd611ad724b.png)

모든 모듈은 자동적으로 shared module이 된다. \
한 번 만들어지면 어떤 모듈에서든 사용할 수 있다. \
예를 들어서 CatsService의 인스턴스를 몇 다른 모듈들에서 사용하고 싶다고 가정하자. \
이렇게 하려면, CatsService를 먼저 module의 export 배열에 담아서 내보내줘야 한다.
```javascipt
// cats.module.ts
import {Module} from "@nestjs/common";
import {CatsController} from "./cats.controller";
import {CatsService} from "./cats.service";

@Module({
    controllers: [CatsController],
    providers: [CatsService],
    exports: [CatsService]
})
export class CatsModule {}
```
이렇게 해두면, `CatsModule`을 임포트 하고 있는 어떤 모듈에서든 `CatsService` 인스턴스를 사용할 수 있다.

### 모듈 다시 내보내기
위에서 본 것 처럼, 모듈들은 모듈 내부적인 provider들을 내보낼 수 있다. \
게다가 모듈은 import 해온 것들을 export 할 수도 있다. \
아래의 `CommonModule`은 `CoreModule`을 임포트해와서 export 하고 있다.\
이렇게 함으로써, `CommonModule`을 임포트한 다른 모듈에서도 `CoreModule`을 사용할 수 있게 된다.

### 의존성 주입
모듈에서 providers를 넣어줄 때, providers를 주입하는 방식으로도 가능하다.

```javascript
// cats.module.ts
import {Module} from "@nestjs/common";
import {CatsController} from "./cats.controller";
import {CatsService} from "./cats.service";

@Module({
    controllers: [CatsController],
    providers: [CatsService]
})
export class CatsModule {
    constructor(private catsService: CatsService) {}
}
```
그렇지만 모듈 클래스 자체는 환 의존성 문제로 provider로서 주입할 수 없다.

### 글로벌 모듈들
만약 같은 모듈들 세트를 모든 곳에 임포트 하고 싶다면, 하나씩 하기는 귀찮은 일이다. \
Nest에서는 `Provider`들을 모듈 범위 안에서 캡슐화 한다. \
따라서 앞서서 캡슐화된 모듈을 임포트 하지 않는다면, 모듈의 `Provider`를 사용할 수 없다. \
여러 `Provider`들 집합을 어디서든 제공해주고 싶다면, 모듈을 `global`로 만들어야 한다. \
`@Global()` 데코레이터를 사용하게 되면 이를 가능하게 해준다.
```javascript
import {Module, Global} from "@nestjs/common";
import {CatsController} from "./cats.controller";
import {CatsService} from "./cats.service";

@Global()
@Module({
    controllers: [CatsController],
    providers: [CatsService],
    exports: [CatsService]
})
export class CatsModule {}
```
`@Global()` 데코레이터는 모듈을 글로벌 범위로 만들어준다. \
글로벌 모듈은 한 번만 등록될 수 있고, 일반적으로 루트나 코어 모듈에 의해 등록된다. \
위 예시에서, CatsService는 어디서나 쓸 수 있고, CatsService를 사용하고 싶은 모듈들은 CatsModule을 임포트 할 필요 없이 의존성 주입할 수 있다.

모든 것을 글로벌화 시키는 것은 좋은 디자인이 아니다. \
글로벌 모듈들은 불필요한 보일러플레이트를 줄여주는 역할을 한다. \
일반적으로 모듈을 소비하고 싶다면 import 배열에 담아주는 것이 좋다.

### 동적 모듈들
Nest의 모듈 시스템은 동적 모듈이라고 하는 강력한 기능을 포함하고 있다.\
이 기능은 커스텀 가능한 모듈들을 만들 수 있게 해주는데, 커스텀 가능한 모듈은 providers를 동적으로 설정하고 등록할 수 있게 해준다. \
동적 모듈은 [이 링크](https://docs.nestjs.com/fundamentals/dynamic-modules)에서 더 자세하게 확인할 수 있다. 

```javascript
import {Module, DynamicModule} from "@nestjs/common";
import {createDatabaseProviders} from "./database.providers";
import {Connection} from "./connection.provider";

@Module({
    providers: [Connection]
})
export class DatabaseModule {
    static forRoot(entities = [], options?): DynamicModule {
        const providers = createDatabaseProviders(options, entities);
        return {
            module: DatabaseModule,
            providers,
            exports: providers
        }
    }
}
```
이 모듈은 `Connection Provider`를 기본으로 `@Module()` 데코레이터 안에 메타데이터에서 정의하고 있다. \
하지만 forRoot에 넘어온 entities와 options에 따라서 추가적으로 provider들을 expose하고 있다\
(예를 들어서 createDatabaseProviders에서는 repositories가 생성된다고 하자). \
동적 모듈에 의해 반환된 프로퍼티들은 override되는 것이 아니라, `@Module()` 데코레이터에 정의된 기본 module 메타데이터에서 extend된다. \
이 방법으로 정적으로 선언된 Connection Provider와 동적으로 생성된 repository provider가 모듈에서 exported 된다.

만약 동적 모듈을 글로벌하게 등록하고 싶으면, 반환값에 global 프로퍼티를 true로 설정하면 된다.

```
{
    global: true,
    module: DatabaseModule,
    providers,
    exports: providers
}
```
DatabaseModule은 아래와 같은 방법으로 임포트되고 설정될 수 있다.
```javascript
import {Module} from "@nestjs/common"
import {DatabaseModule} from "./database/database.module";
import {User} from "./users/entities/user.entity";

@Module({
    imports: [DatabaseModule.forRoot([User])]
})
export class AppModule{}
```
만약 동적 모듈을 re-export(위에서 다시 내보내기와 같다) 하고 싶다면, \
forRoot 메서드를 호출하는 것을 빼고 다시 내보내주면 된다.
```javascript
import {Module} from "@nestjs/common";
import {DatabaseModule} from "./database/database.module";
import {User} from "./users/entities/user.entity";

@Module({
    imports: [DatabaseModule.forRoot([User])],
    exports: [DatabaseModule]
})
export class AppModule {}
```
