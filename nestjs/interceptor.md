### Interceptors
인터셉터는 `@Injectable()` 데코레이터로 주석이 달린 클래스 이다.\
또한, 인터셉터는 `NestInterceptor` 인터페이스를 구현해야 한다.
![image](https://user-images.githubusercontent.com/58246682/148935551-f2c079b6-04c1-4ea2-8c6f-01b9fd2693d0.png)


인터셉터는 [AOP, Aspect Oriented Programminmg](https://en.wikipedia.org/wiki/Aspect-oriented_programming) 기술에서 영감을 받은 유용한 기능set이 존재한다. 이를 통해 아래와 같은 기능들을 수행할 수 있다.\
* 메소드 실행전/후에 추가적인 로직을 바인딩한다.
* 함수에서 반환된 결과를 변환한다.
* 함수에서 던져진 예외를 반환한다.
* 기본 기능 동작을 확장한다.
* 특정 조건에 따라 기능을 완전히 재정의한다.


### Basics
각 인터셉터는 두개의 인자를 취하는 `intercept()` 메소드를 구현한다.\
첫 번째는 `ExecutionContext` 인스턴스인데, 이는 **가드**와 정확히 동일한 객체이다.\
`ExecutionContext`는 `ArgumentsHost`에서 상속된다. 앞서 예외 필터에서 보았듯이, `ArgumentsHost`에서는 \
원래 핸들러에 전달된 인자를 둘러싼 wrapper이며, 애플리케이션 유형에 따라 다른 인자 배열을 포함하고 있음을 알 수 있었다.\

### Execution context
`ArgumentHost`를 확장함으로써 `ExecutionContext`는 현재 실행 프로세스에 대한 \
추가 세부 정보를 제공하는 몇 가지 Helper 메소드도 추가할 수 있다.\
이러한 세부 정보는 광범위한 컨트롤러, 메소드, 실행 컨텍스트에서 작동할 수 있는 \
보다 일반적인 인터셉터를 빌드하는데에 도움이 될 수 있다.\


### Call handler
두 번째 인자는 `CallHandler` 이다. \`
`CallHandler` 인터페이스는 인터셉터의 특정 지점에서 라우트 핸들러 메소드를 호출하는데에 사용하는 `handle()` 메소드를 구현한다.\
`intercept()` 메소드 구현에서 `handle()` 메소드를 호출하지 않는다면, 라우트 핸들러 메소드가 실행되지 않는다.


이 접근방식은 `intercept()` 메소드가 요청/응답 스트림을 효과적으로 포장한다.\
결과적으로, 최종 라우트 핸들러 실행 전/후에 커스텀 로직을 구현하는게 가능해지는 것이다!\
`handle()`을 호출하기 **전에** 실행되는 `intercept()` 메소드에 코드를 작성할 수 있다는 것은 분명하지만,\
이후에 일어나는 일에는 어떠한 영향을 미칠 수 있을까?\
`handle()` 메소드는 `Observable`을 반환하기 때문에, 강력한 `RxJS` 연산자를 사용해 응답을 추가로 조작할 수 있다.\
AOP 용어를 사용해 라우트 핸들러의 호출( 즉, `handle()` 호출 )을 `PointCut`이라고 하며, 추가적인 로직이 삽입된다.


예를 들어, 들어오는 `Post /cats` 요청을 생각해 보자.\
이 요청은 `CatsController` 내에 정의된 `create()` 핸들러를 대상으로 한다.\
`handle()` 메소드를 호출하지 않는 인터셉터가 도중에 호출된다면, `create()` 메소드가 실행되지 않는다.\
`handle()` 메소드가 호출되면(즉, `Observable`이 반환되면) `create()` 핸들러가 트리거 된다.\
그리고, 응답 스트림이 `Observable`을 통해 수신되면 스트림에서 추가작업을 수행할 수 있으며, 최종 결과가 호출자에게 반환된다.

### Aspect interception
이제 살펴볼 첫 번째 사용사례는 인터셉터를 사용해 사용자 상호작용을 기록하는 것이다.\
아래의 간단한 `LoggingInterceptor`를 살펴보자.
```javascript
import { Injectable, NestInterceptor, ExecutionContext, CallHandler } from '@nestjs/common';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable()
export class LoggingInterceptor implements NestInterceptor {
  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    console.log('Before...');

    const now = Date.now();
    return next
      .handle()
      .pipe(
        tap(() => console.log(`After... ${Date.now() - now}ms`)),
      );
  }
}
``` 
> 컨트롤러, 프로바이더, 가드 등과 같은 인터셉터는 `constructor`를 통해 종속성을 주입할 수 있다.

`handle()`은 RxJS의 `Observable`을 반환하므로, 스트림을 조작하는데 사용할 수 있는 다양한 연산자를 선택할 수 있다.\


### Binding Interceptors
인터셉터를 설정하기 위해 `@nestjs/common` 패키지에서 가져온 `@UseInterceptors()` 데코레이터를 사용한다.\
파이프 및 가드와 마찬가지로, 인터셉터는 컨트롤러 범위, 메소드 범위, 혹은 전역 범위에 설정할 수 있다.
```javascript
@UseInterceptors(LoggingInterceptor)
export class CatsController {}
```
위의 구성을 사용하여 CatsController에 정의된 각 라우트 핸들러는 LoggingInterceptor를 사용해 보자. \
누군가 GET /cats 엔드포인트를 호출하면 표준출력에 다음과 같은 출력이 표시된다.

```
Before...
After... 1ms
```
인스턴스 대신 LoggingInterceptor 타입을 전달하여 인스턴스화를 프레임워크에 맡기고 종속성 주입을 활성화했다.\
파이프, 가드, 예외필터와 마찬가지로 내부 인스턴스도 전달할 수 있다.


```javascript
@UseInterceptors(new LoggingInterceptor())
export class CatsController {}
```
언급했듯이 위의 구성은 이 컨트롤러가 선언한 모든 핸들러에 인터셉터를 연결한다. \
인터셉터의 범위를 단일 메서드로 제한하려면 메서드 수준에서 데코레이터를 적용하면 된다.



전역 인터셉터를 설정하기 위해 Nest 애플리케이션 인스턴스의 useGlobalInterceptors() 메서드를 사용한다.
```javascript
const app = await NestFactory.create(AppModule);
app.useGlobalInterceptors(new LoggingInterceptor());
```
글로벌 인터셉터는 모든 컨트롤러와 모든 라우트 핸들러에 대해 전체 애플리케이션에서 사용된다. \
의존성 주입과 관련하여 모듈 외부에서 등록된 전역 인터셉터(위의 예에서와 같이 useGlobalInterceptors() 사용)는 \
모듈의 컨텍스트 외부에서 수행되므로 종속성을 주입할 수 없다. \
이 문제를 해결하기 위해 다음 구성을 사용하여 모든 모듈에서 직접 인터셉터를 설정할 수 있다.

```javascript
import { Module } from '@nestjs/common';
import { APP_INTERCEPTOR } from '@nestjs/core';

@Module({
  providers: [
    {
      provide: APP_INTERCEPTOR,
      useClass: LoggingInterceptor,
    },
  ],
})
export class AppModule {}
```
>이 접근방식을 사용하여 인터셉터에 대한 종속성 주입을 수행할 때 이 구성이 사용되는 모듈에 관계없이 인터셉터는 실제로 전역적이다.\
>인터셉터(위의 예에서는 LoggingInterceptor)가 정의된 모듈을 선택한다.\
>또한 useClass가 커스텀 프로바이더 등록을 처리하는 유일한 방법은 아니다. [링크](https://docs.nestjs.kr/fundamentals/custom-providers)를 참조해 보자.


### Response mapping
우리는 이미 `handle()`이 `Observable`을 반환한다는 것을 알고있다. \
스트림에는 라우트 핸들러에서 반환된 값이 포함되어 있으므로, RxJS의 `map()` 연산자를 사용해 쉽게 변경할 수 있다.



프로세스를 보여주는 간단한 방법으로, 각 응답을 수정하는 `TransformInterceptor`를 만들어보자.\
RxJS의 `map()` 연산자를 사용해 새로 생성된 객체의 `data` 속성에 응답 객체를 할당하고,\
새 객체를 클라이언트에 반환한다.

```javascript
import { Injectable, NestInterceptor, ExecutionContext, CallHandler } from '@nestjs/common';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface Response<T> {
  data: T;
}

@Injectable()
export class TransformInterceptor<T> implements NestInterceptor<T, Response<T>> {
  intercept(context: ExecutionContext, next: CallHandler): Observable<Response<T>> {
    return next.handle().pipe(map(data => ({ data })));
  }
}
```
> Nest 인터셉터는 동기 및 비동기 `intercept()` 메서드 모두에서 작동합니다. \
> 필요한 경우 메서드를 async로 간단히 전환할 수 있습니다.

위의 구성에서 누군가가 `GET / cats` 엔드포인트를 호출하면, 응답은 아래와 같다.
```
{
  "data": []
}
```
인터셉터는 전체 애플리케이션에서 발생하는 요구사항에 대한 재사용 가능한 솔루션을 만드는데 큰 가치를 둔다. \
예를 들어, `null`값의 각 항목을 빈 문자열 `''`로 변환해야 한다고 가정해보자. \
한줄의 코드를 사용하여 이를 수행하고 인터셉터를 전역적으로 바인딩하여 등록된 각 핸들러가 자동으로 사용하도록 할 수 있다.
```javascript
import { Injectable, NestInterceptor, ExecutionContext, CallHandler } from '@nestjs/common';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable()
export class ExcludeNullInterceptor implements NestInterceptor {
  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    return next
      .handle()
      .pipe(map(value => value === null ? '' : value ));
  }
}
```
