## Exception Filters

### Exception filters
Nest에는 애플리케이션 전체에서 처리되지 않은 모든 예외를 처리하는 Exception Layer가 내장되어 있다.\
애플리케이션 코드에서 예외를 처리하지 않으면 이 레이어에서 예외를 포착하고,\
적절하게 사용자 친화적인 응답을 자동으로 전송한다.\
기본적으로 이 작업은 `HttpException` 유형의 예외를 처리하는 **내장 전역 예외필터**에 의해 수행된다.

### Throwing Standard Exceptions
Nest는 `@nestjs/common` 패키지에서 노출된 내장 `HttpException` 클래스를 제공한다.\
일반적인 Http REST/GraphQL API 기반 애플리케이션의 경우, 특정 오류 조건이 발생할 때 표준 HTTP 응답객체를 보내는 것이 가장 좋다.\


예를 들어 `CatsController`에는 `findAll()` 메소드가 존재한다.\
이 라우트 핸들러가 어떠한 이유로 예외를 던진다고 가정해 보자.\
```javascript
@Get()
async findAll() {
  throw new HttpException('Forbidden', HttpStatus.FORBIDDEN);
}
```
`HttpException` 생성자는 응답을 결정하는 두개의 필수인자를 사용한다.
* `response` 인수는 JSON 응답 본문을 정의한다. 아래에 설명된대로 `string`, 또는 `object`일 수 있다.
* `status` 인수는 [HTTP 상태코드](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status)를 정의한다.

기본적으로 JSON 응답 본문에는 두가지 속성이 포함된다.
* `statusCode` : `status` 인수에 제공된 HTTP 상태 코드
* `message`: `status`에 따른 HTTP 오류에 대한 간단한 설명


### Custom exception
일반적으로는 커스컴 exception을 작성할 필요는 존재하지 않고, 다음 섹션에서 설명할 기본 제공 Nest HTTP 예외를 사용한다.\
커스텀 exception을 만들어야 하는 경우, 커스텀 exception이 기본 `HttpException` 클래스에서 상속되는 고유한 exception layer를 만드는 것이 좋다.\
이 접근방식을 통해, Nest가 예외를 인식하고 오류에 대한 응답을 자동으로 처리한다.
```javascript
export class ForbiddenException extends HttpException {
  constructor() {
    super('Forbidden', HttpStatus.FORBIDDEN);
  }
}
```
`ForbiddenException`은 기본 `HttpException`을 상속하기 때문에, 내장된 exception handler와 원활하게 작동한다.
```javascript
@Get()
async findAll() {
  throw new ForbiddenException();
}

```
