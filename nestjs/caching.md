# 캐싱

캐싱은 앱 퍼포먼스를 향상시키는 간단하고도 강력한 기법이다.\
캐싱은 높은 성능의 데이터 접근을 위한 임시 스토어처럼 동작한다..

Nests는 다양한 캐시 스토리지 라이브러리를 위한 통합된 API를 제공하며, **인 메모리 데이터 스토어**를 내장하고있다.

이는 Redis 등의 다른 라이브러리로 쉽게 전환 가능하다.

# 패키지 설치

다음 패키지가 필요.

- `cache-manager`
- `@types/cache-manager` (개발 의존성)

# 모듈 등록

캐싱을 활성화하려면 `CacheModule.register()`로 동적 모듈을 등록한다.

```tsx
import { CacheModule, Module } from '@nestjs/common';
import { AppController } from './app.controller';

@Module({
  imports: [CacheModule.register()],
  controllers: [AppController],
})
export class AppModule {}
```

## 옵션

- `ttl: number | null` — 초 단위의 캐시 만료 시간. `null`은 무제한을 의미한다. (기본값 `5`)
- `max: number` — 캐싱할 응답(항목)의 최대 개수. (기본값 `100`)
- `isCacheableValue: (value: any) => boolean` — 값이 캐싱 가능한지 판별하는 함수.
- `store: string | CacheStoreFactory` — 캐시를 저장할 스토어. (기본값 `memory`)

## 비동기 등록

`registerAsync()` 메서드를 이용하면 모듈을 비동기적으로 등록할 수 있다.

### 팩토리 함수 방법

설정 객체를 반환하는 팩토리 함수를 제공한다.

```tsx
CacheModule.registerAsync({
  useFactory: () => ({
    ttl: 5,
  }),
});
```

이 때, 팩토리 프로바이더와 같이 `async` 비동기 함수 사용과 `inject`를 통해 의존성 주입이 가능하다.

```tsx
CacheModule.registerAsync({
  imports: [ConfigModule],
  useFactory: async (configService: ConfigService) => ({
    ttl: configService.get('CACHE_TTL'),
  }),
  inject: [ConfigService],
});
```

### useClass 방법

`CacheOptionsFactory` 인터페이스를 구현하는 클래스를 사용한다.

```tsx
@Injectable()
class CacheConfigService implements CacheOptionsFactory {
  createCacheOptions(): CacheModuleOptions {
    return {
      ttl: 5,
    };
  }
}

CacheModule.registerAsync({
  useClass: CacheConfigService,
});
```

이 예제와 같은 경우, `CacheConfigService`가 `CacheModule`에서 인스턴스화되고, \
`createCacheOptions()` 메서드를 통해 설정 객체를 얻는다.

다음과 같이 `useExisting`을 이용하여 다른 모듈이 내보낸 클래스를 사용할 수도 있다.

```tsx
CacheModule.registerAsync({
  imports: [ConfigModule],
  useExisting: ConfigService,
});
```

그러면 한 가지의 중요한 차이점만 빼고 `useClass`와 동일하게 작동한다.\
`CacheModule`은 가져온 모듈(`ConfigModule`)이 내보낸 `CacheConfigService`를 재사용합니다.

# 캐시 스토어 사용

캐시 매니저 인스턴스와 상호작용하려면 다음과 같이 클래스에 `CACHE_MANAGER` 토큰으로 `Cache` 인스턴스를 주입한다.

```tsx
import { CACHE_MANAGER } from '@nestjs/common';
import { Cache } from 'cache-manager';

constructor(@Inject(CACHE_MANAGER) private cacheManager: Cache) {}
```

- `get()` — 주어진 키로 캐시에서 항목을 획득. 항목이 없으면 예외 발생.
    
    ```tsx
    const value = this.cacheManager.get('key');
    ```
    
- `set()` — 주어진 키로 캐시에 항목을 설정.
    
    ```tsx
    await this.cacheManager.set('key', 'value');
    ```
    
    기본 캐시 만료 시간은 5초이며, 초 단위 TTL(만료 시간)은 옵션으로 지정할 수 있다. \
    또한 기본 만료 시간은 모듈 옵션으로 바꿀 수 있습니다.
    
    ```tsx
    // 1000초
    await this.cacheManager.set('key', 'value', { ttl: 1000 });
    
    // 무제한
    await this.cacheManager.set('key', 'value', { ttl: null });
    ```
    
- `del()` — 캐시에서 주어진 키의 항목을 제거한다.
    
    ```tsx
    await this.cacheManager.del('key');
    ```
    
- `reset()` — 모든 캐시를 비웁니다.
    
    ```tsx
    await this.cacheManager.reset()
    ```
    

# 자동으로 응답 캐싱

>⛔ **경고**
>GraphQL 애플리케이션에서 인터셉터는 각 필드마다 실행됩니다. \
>따라서 응답 캐싱을 위해 인터셉터를 사용하는 `CacheModule`은 제대로 동작하지 않습니다.


응답을 자동으로 캐싱하려면 캐싱하고 싶은 곳에 `CacheInterceptor`를 설정한다.

```tsx
@Controller()
@UseInterceptors(CacheInterceptor)
export class AppController {
  @Get()
  findAll(): string[] {
    return [];
  }
}
```

>⛔ **경고**
>오직 `GET` 엔드포인트만 캐시됩니다. \
>네이티브 응답 객체(`@Res()`)를 주입하여 사용하는 메서드는 캐시 인터셉터를 사용할 수 없습니다. \
>이는 캐시 인터셉터가가 응답 매핑을 통해 구현되었기 때문입니다.


`CacheInterceptor`를 전역으로 등록할 수도 있다.

```tsx
import { CacheModule, Module, CacheInterceptor } from '@nestjs/common';
import { AppController } from './app.controller';
import { APP_INTERCEPTOR } from '@nestjs/core';

@Module({
  imports: [CacheModule.register()],
  controllers: [AppController],
  providers: [
    {
      provide: APP_INTERCEPTOR,
      useClass: CacheInterceptor,
    },
  ],
})
export class AppModule {}
```

# 전역 캐시 설정 대체

캐시를 전역으로 사용하는 경우, **라우팅 경로에 기반한 키**로 캐시 엔트리가 저장된다. \
메서드별 키는 `@CacheKey()`로 바꿀 수 있고, `@CacheTTL()`로는 만료 시간을 바꿀 수 있다. \
이를 통해 컨트롤러 메서드 별로 다른 캐싱 전략을 세울 수 있고, 다른 종류의 캐시 저장소를 사용할 때 요긴할 수 있다.

```tsx
@Controller()
export class AppController {
  @CacheKey('custom_key')
  @CacheTTL(20)
  findAll(): string[] {
    return [];
  }
}
```

# 트래킹 키 동적 변경

Nest는 요청 URL(HTTP 앱)이나 `@CacheKey()`를 통해 설정된 키를 이용하여 캐시 레코드와 엔드포인트를 연결짓는다. \
그러나 때로는 이를 다른 수단으로 연결하게끔 바꾸고 싶을 수 있다. 

예를 들면 HTTP 헤더 `Authorization`에 기반하여, \
`/profile` 엔드포인트에 대해서는 신원별로 다른 캐시 레코드를 이용해야하는 경우가 있다.

그러면 다음과 같이 `CacheInterceptor` 클래스를 확장하여 `trackBy()` 메서드를 오버라이드 한다.\
메서드 내에서 실행 컨텍스트 인스턴스를 이용하는 방법으로 자유롭게 구현하면 된다.

```tsx
@Injectable()
class HttpCacheInterceptor extends CacheInterceptor {
  trackBy(context: ExecutionContext): string | undefined {
    // 로직 생략
    return 'key';
  }
}
```

# 다른 종류의 스토어 사용

내부적으로 cache-manager를 이용하므로, Redis 스토어 등 다양한 종류의 스토어를 활용할 수 있다. \
사용 가능한 스토어 목록은 [cache-manager 공식 문서](https://github.com/BryanDonovan/node-cache-manager#store-engines)에 있다.

예를 들어 Redis를 사용하려면, 다음과 같이 필요한 옵션과 함께 register 메서드에 전달한다.

```tsx
import * as redisStore from 'cache-manager-redis-store';
import { CacheModule, Module } from '@nestjs/common';
import { AppController } from './app.controller';

@Module({
  imports: [
    CacheModule.register({
      store: redisStore,
      host: 'localhost',
      port: 6379,
    }),
  ],
  controllers: [AppController],
})
export class AppModule {}
```

# WebSocket과 마이크로서비스

`CacheInterceptor`는 사용하는 통신 방법에 관계없이, WebSocket 구독자 및 마이크로서비스 패턴에도 적용할 수 있다.

```tsx
@CacheKey('events')
@UseInterceptors(CacheInterceptor)
@SubscribeMessage('events')
handleEvent(client: Client, data: string[]): Observable<string[]> {
  return [];
}
```

여기서 캐싱을 위한 키를 지정하기 위해 `@CacheKey()` 데코레이터는 필수로 사용해야하며,\
모든 것을 캐싱하려고 하면 안된다.

예를 들어 단순하게 데이터를 쿼리하는 것이 아니라, 약간이라도 비즈니스 오퍼레이션이 동작하는 액션이라면 캐싱해선 안된다.

# 예제

실제 작동하는 전체 예제는 [Nest GitHub 저장소](https://github.com/nestjs/nest/tree/master/sample/20-cache)에 있습니다.

# 아폴로 클라이언트에서의 Caching
https://www.apollographql.com/docs/apollo-server/performance/caching/

### 출처
https://www.notion.so/NestJS-98db1add4f0d48709c95b87fcb568000
