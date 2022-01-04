## Pipes
### Pipes
파이프는 `Injectable()` 데코레이터로, 주석이 달린 클래스이다.\
파이프는 `PipeTransform` 인터페이스를 구현해야 한다.


일반적으로, 파이프는 아래와 같은 두가지 일반적인 사용사례가 존재한다.
* **변환**(transformation) : 입력 데이터를 원하는 방식으로 변환. ( ex) 문자열에서 정수 )
* **유효성 검사**(validation) : 입력 데이터를 평가하고, 유효하지 않은 데이터는 에러를 던진다.

두 경우 모두 파이프가 [컨트롤러 라우트 핸들러](https://docs.nestjs.kr/controllers#route-parameters)가 처리하는 `arguments`에서 작동한다.\
Nest는 메소드가 호출되기 **직전에** 파이프를 삽입하고, 파이프는 메소드로 향하는 인자를 수신해 작동한다.\
모든 변환, 혹은 유효성 검사 작업은 이 때 발생하며 그 후 라우트 핸들러가 변환된 인자와 함께 호출된다.


Nest에는 기본적으로 사용할 수 있는 여러 내장된 파이프가 함께 제공된다.\
물론 고유한 커스텀 파이프도 제작할 수 있다.\


### Built-in Pipes
Nest에는 기본적으로 아래와 같은 6개의 파이프가 제공된다.
* `ValidationPipe`
* `ParseIntPipe`
* `ParseBoolPipe`
* `ParseArrayPipe`
* `ParseUUIDPipe`
* `DefaultValuePipe`

이는 `@nestjs/common` 패키지에서 내보내진다.\
`ParseIntPipe`는 파이프가 메소드 핸들러 매개변수가 자바스크립트 정수로 변환되도록 하는 **변환** 사용 사례중 하나이다.\

### Binding Pipes
파이프를 사용하려면 파이프 클래스의 인스턴스를 적절한 컨텍스트에 바인딩해야 한다..\
`ParseIntPipe` 예제에서 파이프를 특정 라우트 핸들러 메소드와 연관시키고 \
메소드가 호출되기 전에 실행되는지 확인하려고 합니다. \
```javascript
@Get(':id')
async findOne(@Param('id', ParseIntPipe) id: number) {
  return this.catsService.findOne(id);
}
```
위의 예시는 `findOne()` 메소드에서 수신하는 매개변수가 숫자가 아니면 예외가 발생한다.\
``` GET localhost:3000/abc ```
```javascript
{
  "statusCode": 400,
  "message": "Validation failed (numeric string is expected)",
  "error": "Bad Request"
}
```

위 예시에서는 인스턴스가 아닌 클래스(`ParseIntPipe`)를 전달하여 인스턴스화를 프레임워크에 맡기고, 종속성 주입을 활성화 했다.\
파이프 및 가드와 마찬가지로, 대신 내부 인스턴스를 전달할 수 있다.\
내부 인스턴스를 전달하는 것은 옵션을 전달하여 내장 파이프의 동작을 커스텀하려는 경우 유용하게 사용할 수 있다.\

```javascript
@Get(':id')
async findOne(
  @Param('id', new ParseIntPipe({ errorHttpStatusCode: HttpStatus.NOT_ACCEPTABLE }))
  id: number,
) {
  return this.catsService.findOne(id);
}
```

아래는 `ParseIntUUIDPipe`를 사용해 문자열 매개변수를 구문분석하고, UUID인지 확인하는 예제이다.\

```javascript
@Get(':uuid')
async findOne(@Param('uuid', new ParseUUIDPipe()) uuid: string) {
  return this.catsService.findOne(uuid);
}
```


### Custom Pipes
Nest는 위와같은 강력한 내장 파이프들을 제공하지만, 커스텀 파이프도 물론 사용할 수 있다.\
아래는 단순히 입력값을 취하고, 즉시 동일한 값을 반환하여 식별함수처럼 작동하는 `ValidationPipe`이다.\
```javascript
import { PipeTransform, Injectable, ArgumentMetadata } from '@nestjs/common';

@Injectable()
export class ValidationPipe implements PipeTransform {
  transform(value: any, metadata: ArgumentMetadata) {
    return value;
  }
}
```

> `PipeTransform<T, R>`은 파이프로 구현해야하는 일반 인터페이스입니다. \
> 일반 인터페이스는 `T`를 사용하여 입력 `value`의 유형을 나타내고 \
> `R`을 사용하여 `transform()`메서드의 반환유형을 나타냅니다.



모든 파이프는 `PipeTransform` 인터페이스를 구현하기 위해, `transform()` 메소드를 구현해야 한다.\
이 메소드에는 아래와 같은 두 매개변수가 존재한다.
* value
* metadata

`value` 매개변수는 현재 처리된 메소드의 인자이고, `metadata`는 현재 처리된 메소드 인자의 메타데이터이다.\
메타데이터 객체에는 다음과 같은 속성이 존재한다.

```javascript
export interface ArgumentMetadata {
  type: 'body' | 'query' | 'param' | 'custom';
  metatype?: Type<unknown>;
  data?: string;
}
```

* `type` : 인수가 본문 `@Body()`, 쿼리 `@Query()`, param `@Param()` 또는 커스텀 매개변수인지 여부를 나타냅니다
* `metatype : 인수의 메타타입을 제공합니다(예: String).
* `data` : 데코레이터에 전달된 문자열


### Schema based validation
위에서 작성한 `ValidationPipe`를 좀 더 유용하게 만들어보자.\
`CatsController`의 `create()` 메소드를 살펴보면, 서비스를 실행하기 전에 Post 본문 객체가 유효한지 확인하는 것이 좋습니다.

```javascript
@Post()
async create(@Body() createCatDto: CreateCatDto) {
  this.catsService.create(createCatDto);
}
```

`createCatDto` 본문 매개변수에 초점을 맞춰보자. 타입은 `CreateCatDto` 이다.



```javascript
export class CreateCatDto {
  name: string;
  age: number;
  breed: string;
}
```
create 메서드로 들어오는 모든 요청에 유효한 본문이 포함되어 있는지 확인해야 하므로, createCatDto 객체의 세 멤버를 검증해야 한다. \
라우트 핸들러 메소드내에서 이를 수행할 수 있지만 **단일 책임 규칙(SRP single responsibility rule)을** 위반하므로 이상적이지 않다.



또 다른 접근방식은 유효성 검사기 클래스를 만들고 여기에 작업을 위임하는 것이다.\
이것은 우리가 각 메서드의 시작부분에서 이 유효성 검사기를 호출해야 한다는 것을 기억해야 한다는 단점이 있다.



유효성 검사 미들웨어를 만드는 것은 어떨까?\
이것은 작동할 수 있지만 불행히도 전체 애플리케이션의 모든 컨텍스트에서 사용할 수 있는 일반 미들웨어를 만드는 것은 불가능하다.\
이는 미들웨어가 호출될 핸들러 및 매개변수를 포함하여 실행 컨텍스트를 인식하지 못하기 때문이다.



이것이 파이프가 설계된 사용사례이다. \
이제 계속해서 검증 파이프를 개선해 보자.


### Object schema validatio
깔끔한 방법으로 객체 유효성 검사를 수행하는데 사용할 수 있는 몇가지 방법이 있다.\
한가지 접근 방식은 **스키바 기반**의 유효성 검사를 사용하는 것이다.



**Joi** 라이브러리를 사용하면 읽기 쉬운 API를 사용해 간단한 방식으로 스키마를 만들 수 있다.\
Joi 기반 스키마를 사용하는 유효성 검사 파이프를 구축해보자.\
아래 패키지를 설치하고 시작.

```
$ npm install --save joi
$ npm install --save-dev @types/joi
```


아래 코드 샘플에선 스키마를 `constructor` 인수로 사용하는 간단한 클래스를 작성한다.\
그런 다음 제공된 스키마에 대해 들어오는 인수의 유효성을 검사하는 `schema.validate()` 메소드를 적용한다.



앞서 언급했듯, **유효성 검사 파이프**는 값을 변경하지 않고 반환하거나, 예외를 던진다.



다음 섹션에서는 @UsePipes() 데코레이터를 사용하여 주어진 컨트롤러 메소드에 적절한 스키마를 제공하는 방법을 알려준다. \
이렇게 하면 검증 파이프를 컨텍스트 전체에서 다시 사용할 수 있다.

```javascript
import { PipeTransform, Injectable, ArgumentMetadata, BadRequestException } from '@nestjs/common';
import { ObjectSchema } from 'joi';

@Injectable()
export class JoiValidationPipe implements PipeTransform {
  constructor(private schema: ObjectSchema) {}

  transform(value: any, metadata: ArgumentMetadata) {
    const { error } = this.schema.validate(value);
    if (error) {
      throw new BadRequestException('Validation failed');
    }
    return value;
  }
}
```
### Binding validation Pipes
앞서 ParseIntPipe 및 나머지 Parse* 파이프와 같은 변환 파이프를 바인딩하는 방법을 살펴 보았다.
바인딩 유효성 검사 파이프도 매우 간단합니다.
이 경우 메서드 호출 수준에서 파이프를 바인딩하려고 합니다. \
현재 예제에서 JoiValidationPipe를 사용하려면 다음을 수행해야 한다.

1. JoiValidationPipe의 인스턴스를 만듭니다.
2. 파이프의 클래스 생성자에 컨텍스트별 Joi 스키마를 전달합니다.
3. 파이프를 메서드에 바인딩

아래와 같이 @UsePipes() 데코레이터를 사용합니다.

```javascript
@Post()
@UsePipes(new JoiValidationPipe(createCatSchema))
async create(@Body() createCatDto: CreateCatDto) {
  this.catsService.create(createCatDto);
}
```

### Class validator
유효성 검사 기술의 대체 구현을 살펴 보자.



Nest는 class-validator 라이브러리와 잘 맞는다. \
이 강력한 라이브러리를 사용하면 데코레이터 기반 유효성 검사를 사용할 수 있다. \
데코레이터 기반 유효성 검사는 특히, 처리된 속성의 metatype에 액세스할 수 있으므로 Nest의 파이프 기능과 결합할 때 매우 강력하다. \
시작하기 전에 필요한 패키지를 설치해야 한다.

```
$ npm i --save class-validator class-transformer
```
이것들이 설치되면 `CreateCatDto` 클래스에 데코레이터 몇개를 추가할 수 있다. \
여기에서 이 기법의 중요한 이점을 볼 수 있습니다.\
CreateCatDto 클래스는 Post 본문 객체에 대한 단일 소스로 남아 있습니다. \
(별도의 유효성 검사 클래스를 만들 필요가 없음)
```javascript
import { IsString, IsInt } from 'class-validator';

export class CreateCatDto {
  @IsString()
  name: string;

  @IsInt()
  age: number;

  @IsString()
  breed: string;
}
```
이제 이러한 주석을 사용하는 `ValidationPipe` 클래스를 만들 수 있다.

```javascript
import { PipeTransform, Injectable, ArgumentMetadata, BadRequestException } from '@nestjs/common';
import { validate } from 'class-validator';
import { plainToClass } from 'class-transformer';

@Injectable()
export class ValidationPipe implements PipeTransform<any> {
  async transform(value: any, { metatype }: ArgumentMetadata) {
    if (!metatype || !this.toValidate(metatype)) {
      return value;
    }
    const object = plainToClass(metatype, value);
    const errors = await validate(object);
    if (errors.length > 0) {
      throw new BadRequestException('Validation failed');
    }
    return value;
  }

  private toValidate(metatype: Function): boolean {
    const types: Function[] = [String, Boolean, Number, Array, Object];
    return !types.includes(metatype);
  }
}
```
이 코드를 살펴보겠습니다. \
먼저 `transform()` 메서드가 `async`로 표시되어 있다. \
Nest가 동기 및 비동기 파이프를 모두 지원하기 때문에 가능하다. \
일부 `class-validator` 유효성 검사가 비동기화될 수 있기(`Promise` 활용)때문에 이 메서드를 async로 만든다.



다음으로, 우리는 메타타입 필드 (`ArgumentMetadata`에서 이 멤버만 추출)를 `metatype` 매개변수로 추출하기 위해 디스트럭처링을 사용하고 있다. \
이것은 전체 `ArgumentMetadata`를 가져온 다음 메타타입 변수를 할당하는 추가 명령문을 갖는 것에 대한 속기일 뿐이다.



다음으로 헬퍼 함수 `toValidate()`를 확인한다. \
처리중인 현재 인수가 네이티브 자바스크립트 타입인 경우 유효성 검사 단계를 건너 뛰는 역할을 한다.\
(이러한 인수는 유효성 검사 데코레이터를 연결할 수 없으므로 유효성 검사 단계를 통해 실행할 이유가 없습니다).



다음으로 클래스 변환기 함수 plainToClass()를 사용하여 \
일반 자바스크립트 인수 객체를 타입이 지정된 객체로 변환하여 유효성 검사를 적용할 수 있다. \
이 작업을 수행해야 하는 이유는 네트워크 요청에서 역직렬화될 때 들어오는 \
포스트(post) 본문 객체가 아무 타입 정보도 가지고 있지 않기 때문이다.\
클래스 유효성 검사기는 이전에 DTO에 대해 정의한 유효성 검사 데코레이터를 사용해야 하므로 \
들어오는 본문을 단순한 바닐라 객체가 아닌 적절하게 장식된 객체로 처리하기 위해 이 변환을 수행해야 한다.



또한 앞서 언급했듯이 이것은 유효성 검사 파이프이므로 변경되지 않은 값을 반환하거나 예외를 던집니다(throw).



마지막 단계는 ValidationPipe를 바인딩하는 것이다. \
파이프는 매개변수 범위, 메서드 범위, 컨트롤러 범위 또는 전역 범위일 수 있다. \
앞서 Joi 기반 유효성 검사 파이프를 사용하여 메서드 수준에서 파이프를 바인딩하는 예를 보았습니다. \
아래 예제에서는 파이프 인스턴스를 라우트 핸들러 @Body() 데코레이터에 바인딩하여 파이프가 포스트(post) 본문의 유효성을 검사하도록 호출한다.
```
@Post()
async create(
  @Body(new ValidationPipe()) createCatDto: CreateCatDto,
) {
  this.catsService.create(createCatDto);
}
```
