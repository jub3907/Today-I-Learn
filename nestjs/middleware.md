### 미들웨어
미들웨어는 **라우드 핸들러 이전에** 호출되는 함수.\
미들웨어 기능은 `요청` 및 `응답` 객체, 애플리케이션의 요청-응답주기에서 `next()` 미들웨어 함수.\
**next**미들웨어 함수는 일반적으로 `next`라는 변수로 표시된다.



Nest의 미들웨어는 기본적으로 express의 미들웨어와 동일하다.\
동식 익스프레스 문서에서는 아래와 같이 설명하고 있다.
```
미들웨어 기능은 다음 작업을 수행할 수 있습니다.
- 모든 코드를 실행하십시오.
- 요청 및 응답 객체를 변경합니다.
- 요청-응답주기를 종료합니다.
- 스택의 next 미들웨어 함수를 호출합니다.
- 현재 미들웨어 함수가 요청-응답주기를 종료하지 않으면 next()를 호출하여 next 미들웨어 기능에 제어를 전달합니다. 그렇지 않으면 요청이 중단됩니다.
```



함수, 또는 `Injectable` 데코레이터가 있는 클래스에서 커스텀 Nest 미들웨어를 구현할 수 있다.\
클래스는 `NestMiddleware` 인터페이스를 구현해야 하지만, 이외의 특별한 요구사항은 존재하지 않는다.
```javascript
import { Injectable, NestMiddleware } from '@nestjs/common';
import { Request, Response, NextFunction } from 'express';

@Injectable()
export class LoggerMiddleware implements NestMiddleware {
  use(req: Request, res: Response, next: NextFunction) {
    console.log('Request...');
    next();
  }
}
```

### Dependency injection
Nest 미들웨어는 종속성 주입을 완벽하게 지원한다.\
프로바이더 및 컨트롤러와 마 찬가지로 동일한 모듈내에서 사용할 수 있는 종속성을 삽입할 수 있다.\
늘 그렇듯 생성자를 통해 이루어진다.


### Applying middleware
`@Module` 데코레이터는 미들웨어를 위한 위치가 존재하지 않는다.\
대신, 모듈 클래스의 `configure()` 메소드를 사용해 설정하고, \
미들웨어를 포함하는 모듈은 `NestModule` 인터페이스를 구현해야 한다.\

```javascript
import { Module, NestModule, MiddlewareConsumer } from '@nestjs/common';
import { LoggerMiddleware } from './common/middleware/logger.middleware';
import { CatsModule } from './cats/cats.module';

@Module({
  imports: [CatsModule],
})
export class AppModule implements NestModule {
  configure(consumer: MiddlewareConsumer) {
    consumer
      .apply(LoggerMiddleware)
      .forRoutes('cats');
  }
}
```
위의 예시에서, 이전에 `CatsCOntroller`에 정의된 `/cats` 라우트 핸들러에 대해 `LoggerMiddleware`를 설정했다.\
또한 미들웨어를 구성할 때 `path` 라우트가 포함된 객체를 전달하고, `method`를 `forRoutes()` 메소드에 요청하여\
미들웨어를 특정 요청 메소드로 제한할 수도 있다.\
아래 예시에서 원하는 요청 메소드 타입을 참조하기 위해, `RequestMethod` Enum을 가져온다.

```javascript
import { Module, NestModule, RequestMethod, MiddlewareConsumer } from '@nestjs/common';
import { LoggerMiddleware } from './common/middleware/logger.middleware';
import { CatsModule } from './cats/cats.module';

@Module({
  imports: [CatsModule],
})
export class AppModule implements NestModule {
  configure(consumer: MiddlewareConsumer) {
    consumer
      .apply(LoggerMiddleware)
      .forRoutes({ path: 'cats', method: RequestMethod.GET });
  }
}
```
> `configure()` 메소드는 `async await`을 사용해 비동기식으로 만들 수 있습니다.

### Route wildcards
패턴 기반 리스트도 지원이 된다.\
예를 들어, 별표는 와일드카드로 사용되며, 모든 문자 조합과 일치한다.
```javascript
forRoutes({ path: 'ab*cd', method: RequestMethod.ALL });
```

### Middleware consumer
`MiddlewareConsumer`는 헬퍼 클래스이다.\
미들웨어를 관리하기 위한 몇가지 내장된 방법을 제공한다.
모두 `Fluent interface`로 간단하게 연결될 수 있다.\
`forRoutes()` 메소드는 단일 문자열, 여러 문자열, `RouteInfo` 객체, 컨트롤러 클래스 및 여러 컨트롤러 클래스를 사용할 수 있다.
```javascript
import { Module, NestModule, MiddlewareConsumer } from '@nestjs/common';
import { LoggerMiddleware } from './common/middleware/logger.middleware';
import { CatsModule } from './cats/cats.module';
import { CatsController } from './cats/cats.controller.ts';

@Module({
  imports: [CatsModule],
})
export class AppModule implements NestModule {
  configure(consumer: MiddlewareConsumer) {
    consumer
      .apply(LoggerMiddleware)
      .forRoutes(CatsController);
  }
}
```

### Excluding routes
떄로, 우리는 미들웨어 적용시 특정 라우트가 필요하지 않은 경우가 존재한다.\
`exclude()` 메소드로 특정 라우트를 쉽게 제외할 수 있다.\
이 메소드는 아래와 같이 제외할 라우트를 식별하는 단일 문자열, 여러 문자열 또는 `RouteInfo` 객체를 사용할 수 있다.
```javascript
consumer
  .apply(LoggerMiddleware)
  .exclude(
    { path: 'cats', method: RequestMethod.GET },
    { path: 'cats', method: RequestMethod.POST },
    'cats/(.*)',
  )
  .forRoutes(CatsController);
```

### Functional middleware
우리가 사용해온 `LoggerMiddleware` 클래스는 멤버, 추가메소드, 종속성이 존재하지 않아 간단하다고 할 수 있다.\
이런 경우, 클래스를 간단한 함수로 대체할 수 있다.\
이렇게 함수로 정의된 미들웨어를 `Functional Middleware`, **기능적 미들웨어**라고 한다.\
로거 미들웨어는 클래스 기반에서 기능적 미들웨어로 변환해보자.
```javascript
import { Request, Response, NextFunction } from 'express';

export function logger(req: Request, res: Response, next: NextFunction) {
  console.log(`Request...`);
  next();
};
```
위와 같이 선언한 뒤, AppModule에서 사용하면 된다.
```javascript
consumer
  .apply(logger)
  .forRoutes(CatsController);
```
> 미들웨어에 종속성이 필요하지 않을 때마다 더 간단한 기능적 미들웨어 대안을 사용하는 것이 좋다.

