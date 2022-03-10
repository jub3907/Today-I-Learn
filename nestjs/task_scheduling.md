## Task Scheduling
Task 스케쥴링은 내가 짠 임의의 코드를 특정 시간에 실행시키거나, 특정 시간마다 실행시키는 방법이다.

리눅스에서, 이러한 반복 실행은 OS레벨에서 **cron**같은 패키지를 사용해 다룰 수 있다.

Node 앱에서, cron처럼 에뮬레이팅하는 패키지들이 존재하고, Nest는 `@nestjjs/schedule` 패키지를 제공한다.

이 패키지는 널리 사용되는 Node.js의 `node-cron` 패키지를 통합한다.



## Installation
패키지를 사용하기 위해, 아래 패키지들을 설치해야 한다.

```
$ npm install --save @nestjs/schedule
$ npm install --save-dev @types/cron
```

작업 스케쥴링을 활성화 하기 위해, `ScheduleModule`을 `AppModule`에 불러오고, 아래와 같이 `forRoot()` 정적 메소드를 실행한다.

```tsx
import { Module } from '@nestjs/common';
import { ScheduleModule } from '@nestjs/schedule';

@Module({
  imports: [
    ScheduleModule.forRoot()
  ],
})
export class AppModule {}
```

`.forRoot()`은 스케쥴러를 초기화 하고, 앱에 존재하는 선언적 크론 작업과 timeout, intervals를 등록한다.\
모든 모듈이 예약된 작업을 로드하고 선언했는 지 확인하는 `onApplicationBootstrap` 라이프사이클 훅이 발생할 때, 등록된다.


## Declarative cron jobs
크론 작업은 임의의 함수가 자동으로 실행되도록 예약한다.\
실행될 수 있는 크론 작업은 아래와 같다.
* 특정 시간에 한번
* 지정된 간격으로 반복

실행할 코드를 포함하는 메소드 정의 앞에 `@Cron()` 데코레이터를 사용해, 크론 작업을 선언한다.

```tsx
import { Injectable, Logger } from '@nestjs/common';
import { Cron } from '@nestjs/schedule';

@Injectable()
export class TasksService {
  private readonly logger = new Logger(TasksService.name);

  @Cron('45 * * * * *')
  handleCron() {
    this.logger.debug('Called when the current second is 45');
  }
}
```

위 예시에서, `handleCron()` 메소드는 현재 시간이 45초 일 때 마다 실행된다. \
다시말해, 위 메소드는 1분 마다, 45초에 실행된다.

`@Cron()` 데코레이터를 모든 기준 크론 패턴을 지원한다.

* Asterisk ( `*` )
* Ranges (`1-3, 5`)
* Steps (`*/2`)

위 예시에서, 우리는 `45 * * * * *`를 데코레이터에 전달 했다.\
아래 값은 크론 패턴의 각 위치가 어떻게 해석되는 지를 알려준다.

```
* * * * * *
| | | | | |
| | | | | day of week
| | | | months
| | | day of month
| | hours
| minutes
seconds (optional)

```

`@nestjs/schedule` 패키지는 흔히 사용되는 크론 패턴들을 `ENUM`으로 제공한다.

```tsx
import { Injectable, Logger } from '@nestjs/common';
import { Cron, CronExpression } from '@nestjs/schedule';

@Injectable()
export class TasksService {
  private readonly logger = new Logger(TasksService.name);

  @Cron(CronExpression.EVERY_30_SECONDS)
  handleCron() {
    this.logger.debug('Called every 30 seconds');
  }
}
```

위 예시에서, `handleCron()` 메소드는 매 30초마다 실행된다.

위와 다르게, Javascript의 `Date` 객체를 `@Cron()` 데코레이터에 넣어주게 되면,\
메소드가 지정한 날짜에 딱 한번 실행 된다.

또한, `@Cron()` 데코레이터의 두 번째 파라미터로 추가적인 옵션을 제공할 수도 있다.
* `name` : 크론 작업이 선언된 후에 액세스 하거나, 제어하는 데 유용하게 사용할 수 있다.
* `timezone`: 실행 시간대를 지정한다. timezone은 시간대를 기준으로 실제 시간이 수정되고, 만약 timezone이 유효하지 않은 경우 오류가 발생한다,
* `utcOffset` 실행 시간대를 지정하는 것 대신, 시간대에 맞는 offset을 제공할 수도 있다.

```tsx
import { Injectable } from '@nestjs/common';
import { Cron, CronExpression } from '@nestjs/schedule';

@Injectable()
export class NotificationService {
  @Cron('* * 0 * * *', {
    name: 'notifications',
    timeZone: 'Europe/Paris',
  })
  triggerNotifications() {}
}
```


## Declarative Intervals
특정한 시간 간격으로 실행되어야만 하는 메소드를 선언하기 위해서, 메소드 정의 앞에 `@Interval()` 데코레이터를 사용한다.\
아래와 같이 시간 간격을 밀리초 단위로 데코레이터에 전달한다.

```tsx
@Interval(10000)
handleInterval() {
  this.logger.debug('Called every 10 seconds');
}
```

## Declarative timeouts
메소드가 지정된 시간 초과에 대해 한 번 실행되어야 한다고 선언하기 위해서, 메소드 정의 앞에 `@Timeout()` 데코레이터를 붙인다.\
아래와 같이 application 시작 시 데코레이터로 상대 시간 오프셋을 전달한다.

```tsx
@Timeout(5000)
handleTimeout() {
  this.logger.debug('Called once after 5 seconds');
}
```

만약 cron 선언문들을 Dynamic API를 사용해 클래스 외부에서 선언하고 싶다면, 아래와 같이 Timeout을 이름과 연결해라.

```tsx
@Timeout('notifications', 2500)
handleTimeout() {}
```




