### Controller
`controller`는 흔히들 말하는 것과 같이, 들어오는 요청 `Request`를 처리하고 응답 `Response`를 처리하는 로직이다.\
컨트롤러의 목적은 애플리케이션에 대한 특정 요청을 수신하는 것이며, \
**라우팅** 매커니즘은 어떤 컨트롤러가 어떤 요청을 수신하는 지를 제어한다.
![image](https://user-images.githubusercontent.com/58246682/147237740-0a751d99-bd45-442c-9393-c1fba3f8a0cb.png)


### 라우팅
NestJS에서 기본 컨트롤러를 만들려면 `class`와 `@Controller` 데코레이터를 사용한다.\
또한, `Controller` 데코레이터를 통해서 `path` 접두사를 사용하면 관련 라우트 집합을 쉽게 그룹화하고, 반복 코드를 최소화 할 수 있다.
```typescript
cats.controller.tsJS;

import { Controller, Get } from "@nestjs/common";

@Controller("cats")
export class CatsController {
  @Get()
  findAll(): string {
    // 여기는 유저가 정의하는 부분이다.
    return "This action returns all cats";
  }
}
```

`@Get()`은 NestJS에게 HTTP 요청들의 엔드포인트에 대한 핸들러를 생성하도록 지시한다.\
`@Get()`을 사용해 위 컨트롤러에서 정해진 prefix(cat)와 합쳐, 엔드포인트를 지정할 수 있다.\
즉, 지금 예시는 `GET /cats` 요청에 대해 응답하는 컨트롤러라고 볼 수 있다.\
위의 메서드는 200 상태코드와 응답을 반환하고, NestJS는 아래와 같이 응답을 조작하기 위한 두 가지 옵션을 사용한다.
* 표준(Standard)
  * NestJS에서 추천하는 방식이며, 요청에 대한 값을 리턴하면 자동으로 `JSON`으로 직렬화(Serialization) 해 준다.
  * 하지만, 자바스크립트의 기본 타입을 반환하면 직렬화를 시행하지 않고, 값만 전송한다.
  * 또한, 응답의 상태 코드는 201을 사용하는 `POST` 요청을 제외하고서는 항상 200이다. 
* Library-specific
  * 메소드 핸들러 시그니처에서 `@Res()` 데코레이터를 사용해 삽입할 수 있는 라이브러리(ex. Express)별 응답 객체를 사용할 수 있다.

### Request Object
핸들러는 종종 클라이언트 **요청**의 세부 정보에 엑세스 해야 한다.\
Nest는 역시 요청 객체에 접근하는 방식을 데코레이터 `@Req`로 제공한다.
```typescript
import { Controller, Get, Req } from '@nestjs/common';
import { Request } from 'express';

@Controller('cats')
export class CatsController {
  @Get()
  findAll(@Req() request: Request): string {
    return 'This action returns all cats';
  }
}
```
요청 객체는 HTTP 요청을 나타내며, 요청 쿼리 문자열과 매개변수, HTTP 헤더 및 본문에 대한 속성을 갖는다. [참조](https://expressjs.com/en/api.html#req)\
하지만, 대부분 이러한 속성들을 수동으로 가져 올 필요는 없고, `@Body()`나 `@Query`와 같은 전용 데코레이터를 사용하면 된다.
```
@Request()	            -> req
@Response(), @Res()	    -> res    
@Next()	                -> next
@Session()	            -> req.session
@Param(key?: string)	  -> req.params / req.params[key]
@Body(key?: string)	    -> req.body / req.body[key]
@Query(key?: string)	  -> req.query / req.query[key]
@Headers(name?: string)	-> req.headers / req.headers[name]
```

### 리소스
위에서는 cat 리소스를 가져오는 `GET` 엔드포인트를 정의했다. \
이와 마찬가지로, Nest는 모든 표준 HTTP 메소드에 대한 데코레이터를 제공한다.\
* `@Post()`, `@Put()`, `@Delete()`, `@Patch()`, `@Options()`, `@Head()`, `@All()`
```typescript
import { Controller, Get, Post } from '@nestjs/common';

@Controller('cats')
export class CatsController {
  @Post()
  create(): string {
    return 'This action adds a new cat';
  }

  @Get()
  findAll(): string {
    return 'This action returns all cats';
  }
}
```

### Wildcard Routing
패턴 기반 라우트도 제공하고 있다. 예를 들어, 별표(asterisk)는 와일드카드로 사용되며, 모든 문자 조합과 일치된다.
```typescript
@Get('ab*cd')
findAll() {
  return 'This route uses a wildcard';
}
```
`?`, `+`, `*`, `()` 문자가 라우트 경로에 사용될 수 있다.

### 상태 코드
위에서도 언급했듯, 상태 코드가 201인 `POST` 요청을 제외하고 기본적으로 항상 200이다.\
하지만, 핸들러 레벨에서 `@HttpCode(...)` 데코레이터를 추가해 이 동작을 변경할 수 있다.

### 헤더
커스텀 응답 헤더를 지정하기 위해선 `@Header()` 데코레이터를 사용하면 된다.
```typescript
@Post()
@Header('Cache-Control', 'none')
create() {
  return 'This action adds a new cat';
}
```

### Redirection
응답을 특정 URL로 리디렉션 하기위해선 `@Redirect()` 데코레이터를 사용하거나, library-specific한 응답 객체를 사용해야 한다.\
`@Redirect`는 `url`과 `statusCode`라는 두 개의 인자를 취하며, 둘 다 선택사항.\
생략된 경우, `statusCode`의 기본값은 302이다.
```typescript
@Get()
@Redirect('https://nestjs.com', 301)
```
때로, HTTP 상태 코드, 혹은 리디렉션 URL을 동적으로 확인하고 싶다면, 라우트 핸들러 메소드에서 아래와 같은 객체를 반환 해 주면 된다.
```typescript
{
  "url": string,
  "statusCode": number
}
```
반환된 값은 @Redirect() 데코레이터에 전달된 모든 인수를 재정의합니다.
```typescript
@Get('docs')
@Redirect('https://docs.nestjs.com', 302)
getDocs(@Query('version') version) {
  if (version && version === '5') {
    return { url: 'https://docs.nestjs.com/v5/' };
  }
}
````
