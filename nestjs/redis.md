## Redis

[Radis](https://redis.io/) Transporter는 구독/발행 메세지 체계를 구현하고, Redis의 Pub/Sub 기능을 활용한다.

발행된 메세지는 이는 어떤 구독자가 메세지를 받는 지 알지 못하는 상태로 채널에 분류된다. \
각 마이크로서비스는 여러개의 채널을 구독할 수 있고, 한 번에 둘 이상의 채널을 구독할 수 있다.

채널을 통해 교환된 메세지는 **fire-and-forget**되는데, 이는 메세지가 발행된 뒤 메세지에 대한 구독자가 없을 때\
메세지가 삭제되고 복구할 수 없다는 걸 의미한다.

즉, 메세지와 이벤트 둘 다 최소한 한 개 이상의 서비스에서 다뤄지고 있다는걸 의미한다.\
하나의 메세지는 여러개의 구독자를 둘 수 있다.

![image](https://user-images.githubusercontent.com/58246682/165510455-093fb0c6-5bd3-4ae8-8649-af0178142720.png)

### Installation

Redis 기반의 마이크로서비스를 구성하기 위해, 아래 패키지를 설치해야 한다.

> 현재 지원하는 Redis는 v3이며, 최신 버전인 v4가 아니라는 점을 주의하자.

```tsx
$ npm i --save redis@^3
```

### Overview

Redis Transporter를 사용하기 위해, `createMicroservice()` 메소드에 아래 옵션을 전달해야 한다.

```tsx
// main.ts
const app = await NestFactory.createMicroservice(AppModule, {
  transport: Transport.REDIS,
  options: {
    url: "redis://localhost:6379",
  },
});
```

> `Transport` enum은 `nestjs/microservices` 패키지에서 불러온다.

### Options

`options` 특성은 선택된 transporter에 한정된다.

- `url` : 연결 할 URL
- `retryAttempts` : 재시도 할 횟수, default는 0.
- `retryDelay` : 재시도 할 시간 간격, default는 0.

공식 [redis] 클라이언트에서 지원하는 모든 특성은 이 transporter에서도 지원한다.

### Client

다른 마이크로서비스 transporter처럼, Redis의 `ClientProxy` 객체를 생성하기 위한 [옵션](https://docs.nestjs.com/microservices/basics#client)이 여러가지 존재한다.

인스턴스를 생성하는 한 가지 방법은 `ClientsModule`을 사용하는 것이다.\
`ClientsModule`로 인스턴스를 생성하기 위해, 위의 `createMicroservice()` 메소드에서 사용한 것과 동일한 옵션을 지닌 객체를\
`register()` 메소드에 전달하고, `name` 옵션을 토큰을 주입하기 위해서 사용해야 한다.

```tsx
@Module({
  imports: [
    ClientsModule.register([
      {
        name: 'MATH_SERVICE',
        transport: Transport.REDIS,
        options: {
          url: 'redis://localhost:6379',
        }
      },
    ]),
  ]
  ...
})
```

클라이언트를 생성하기 위한 다른 옵션들 또한 존재하며, 이는 [링크](https://docs.nestjs.com/microservices/basics#client)에서 확인할 수 있다.

### Context

좀 더 정교한 시나리오로 들어가 보자면, 들어오는 요청에 대해 좀 더 자세한 정보가 필요할 수가 있다.\
Redis transporter를 사용한다면 `RedisContext` 객체에 접근할 수 있다.

```tsx
@MessagePattern('notifications')
getNotifications(@Payload() data: number[], @Ctx() context: RedisContext) {
  console.log(`Channel: ${context.getChannel()}`);
}
```
