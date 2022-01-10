### Guard
가드는 `@Injectable()` 데코레이터로, 주석이 달린 클래스 이며\
`CanActivate` 인터페이스를 구현해야 한다.

![image](https://user-images.githubusercontent.com/58246682/148762356-13670357-0ef9-4a17-9eee-ad6ad8ce7ffb.png)



가드는 `Single Responsibility`를 가지고 있다.\
런타임에 존재한느 특정 조건에 따라 지정된 요청을 라우터 핸들러에 의해\
처리할지 여부를 결정하며, 이를 **`Authorization`**이라 한다.\




인증을 하기 위해 미들웨어를 선택하는것은 아주 좋은 선택이고, \
토큰 유효성 검사와 속성을 `request` 객체에 연결하는 것은 특정 라우트 컨텍스트와 강하게 연결되어 있지않기 때문이다.



그러나, 미들웨어는 본질적으로 멍청하다.\
`next` 함수를 호출한 후, 어떤 핸들러가 실행될지 알 수 없다.\
반면, 가드는 `ExecutionContext` 인스턴스에 엑세스할 수 있으므로,\
다음에 실행될 작업을 명확히 알고 있다.



예외 필터, 파이프 및 인터셉터와 매우 유사하게 요청/응답주기의 정확한 지점에서 처리 로직을 삽입하고, 선언적으로 수행할 수 있도록 설계되었다.\
이는 코드를 건조하고 선언적으로 유지하는데 도움이 된다.

> 가드는 각 미들웨어 **이후**에 실행되지만, 인터셉터나 파이프는 **앞에** 실행된다.


### Authorization guard
언급했듯이 승인은 호출자(일반적으로 특정 인증된 사용자)에게 충분한 권한이 있는 경우에만\
특정 라우트를 사용할 수 있어야하므로 가드의 훌륭한 사용사례이다.



이제 우리가 빌드할 AuthGuard는 인증된 사용자를 가정한다. (즉, 토큰이 요청 헤더에 첨부됨). \
토큰을 추출하고 유효성을 검사하고 추출된 정보를 사용하여 요청을 진행할 수 있는지 여부를 결정해 보자.
```javascript
import { Injectable, CanActivate, ExecutionContext } from '@nestjs/common';
import { Observable } from 'rxjs';

@Injectable()
export class AuthGuard implements CanActivate {
  canActivate(
    context: ExecutionContext,
  ): boolean | Promise<boolean> | Observable<boolean> {
    const request = context.switchToHttp().getRequest();
    return validateRequest(request);
  }
}
```

`validateRequest` 함수 내부의 로직은 필요에 따라 간단하거나, 정교할 수 있다.\
이 예시의 요점은 가드가 요청/응답 주기에 어떻게 부합하는지 보여주는 것이다.


모든 가드는 `canActivate()` 함수를 구현해야 한다. \
이 함수는 현재 요청이 허용되는지 여부를 나타내는 값을 반환해야 한다.\
응답을 비동기식 또는 동기식(`Observable`, 혹은 `Promise`)으로 반환할 수 있으며,\
Nest는 반환값을 사용해 다음 작업을 제어한다.\

* true를 반환하면 요청이 처리됩니다.
* false를 반환하면 Nest는 요청을 거부합니다.

### Execution Context
`canActivate()` 함수는 `ExecutionContext` 인스턴스라는 단일 인자를 받고,`ExecutionContext`는 `ArgumentsHost`에서 상속된다. \
이전에 예외필터 장에서 `ArgumentsHost`를 보았습니다. \
위의 샘플에서는 이전에 사용했던 `ArgumentsHost`에 정의된 동일한 헬퍼 메서드를 사용하여 `Request` 객체에 대한 참조를 얻는다.


ArgumentsHost를 확장함으로써 ExecutionContext는 현재 실행 프로세스에 대한 \
추가 세부정보를 제공하는 몇가지 새로운 헬퍼 메서드도 추가한다. \
이러한 세부정보는 광범위한 컨트롤러, 메서드 및 실행 컨텍스트에서 작동할 수 있는 \
보다 일반적인 가드를 구축하는 데 도움이될 수 있다.


### Role-based authentication

특정 역할을 가진 사용자에게만 액세스를 허용하는 보다 기능적인 가드를 구축해 보자.\
기본 가드 템플릿으로 시작하여 다음 섹션에서 빌드한다. \
지금은 모든 요청을 진행할 수 있다.

```javascript
import { Injectable, CanActivate, ExecutionContext } from '@nestjs/common';
import { Observable } from 'rxjs';

@Injectable()
export class RolesGuard implements CanActivate {
  canActivate(
    context: ExecutionContext,
  ): boolean | Promise<boolean> | Observable<boolean> {
    return true;
  }
}

```

### Binding guard
파이프 및 예외필터와 마찬가지로, 가드는 **컨트롤러 범위**(Controller-scoped), 메소드 범위, 전역 범위로 선언될 수 있다.\
아래는 `UseGuards()` 데코레이터를 사용해 컨트롤러 범위 가드를 설정한다.\
이 데코레이터는단일 인자, 혹은 쉼포로 구분된 인자 목록을 사용할 수 있다.\
이처럼 하나의 선언으로 적절한 가드 세트를 쉽게 적용할 수 있다.

```javascript
@Controller('cats')
@UseGuards(RolesGuard)
export class CatsController {}
```

위에서, 우리는 인스턴스 대신 `RolesGuard` 타입을 전달해 인스턴스화를 프레임워크에 맡기고, 종속성 주입을 활성화 했다.\
파이프 및 예외필터와 마찬가지로, 내부 인스턴스를 전달할 수도 있다.\

```javascript
@Controller('cats')
@UseGuards(new RolesGuard())
export class CatsController {}
```

위의 구성은 이 컨트롤러가 선언한 모든핸들러에 가드를 연결한다.\
가드가 단일 메소드에만 적용되도록 하려면, **메소드 수준**에서 `UseGuard()` 데코레이터를 적용한다.


전역 가드를 설정하려면 NEst 애플리케이션 인스턴스의 `useGlobalGuard()` 메소드를 사용하면 된다.

```javascript
const app = await NestFactory.create(AppModule);
app.useGlobalGuards(new RolesGuard());
```

> 하이브리드 앱의 경우 `useGlobalGuard()` 메소드는 기본적으로 게이트웨이 및 \
> 마이크로 서비스에 대한 보호를 설정하지 않는다.\
> **표준** 마이크로서비스 앱의 경우, `useGlobalGuards()`는 가드를 전역적으로 마운트한다.


```javascript
import { Module } from '@nestjs/common';
import { APP_GUARD } from '@nestjs/core';

@Module({
  providers: [
    {
      provide: APP_GUARD,
      useClass: RolesGuard,
    },
  ],
})
export class AppModule {}
```


### Setting roles per handler

우리의 RolesGuard는 작동하지만 아직 똑똑하지는 않다.. \
우리는 아직 가장 중요한 가드 기능인 실행 컨텍스트(execution context)를 활용하고 있지 않기 때문이다. \
즉, 역할이나 각 핸들러에 대해 허용되는 역할에 대해서는 아직 알지 못한다. \
예를 들어 CatsController는 라우트마다 다른 권한체계를 가질 수 있다. \
일부는 관리자만 사용할 수 있고 다른 일부는 모든 사용자가 사용할 수 있다. \
유연하고 재사용 가능한 방식으로 역할을 라우트에 어떻게 일치시킬 수 있을까?



여기에서 맞춤 메타데이터가 작동한다..\
Nest는 `@SetMetadata()` 데코레이터를 통해 라우트 핸들러에 커스텀 메타데이터를 첨부하는 기능을 제공한다. \
이 메타데이터는 스마트 가드가 결정을 내리는데 필요한 누락된 role 데이터를 제공한다. \
`@SetMetadata()` 사용을 살펴 보자.

```javascript
@Post()
@SetMetadata('roles', ['admin'])
async create(@Body() createCatDto: CreateCatDto) {
  this.catsService.create(createCatDto);
}
```

위의 구성을 통해 `create()` 메소드에 `roles` 메타데이터를 첨부했다.\
이것이 작동하지만, 라우트에서 직접  `@setMetadata()`를 사용하는 것은 좋지 않다.\
대신, 아래와 같이 자신만의 데코레이터를 작성해야 한다.

```javascript
import { SetMetadata } from '@nestjs/common';

export const Roles = (...roles: string[]) => SetMetadata('roles', roles);
```
이 접근방식은 기존보다 훨씬 깔끔하고 읽기 쉬우며, 강력하게 입력된다.\
이제 사용자 정의 `Roles()` 데코레이터가 있으므로, 이를 사용하여 `create()` 메소드를 데코레이션 할 수 있다.\

```javascript
@Post()
@Roles('admin')
async create(@Body() createCatDto: CreateCatDto) {
  this.catsService.create(createCatDto);
}

```


