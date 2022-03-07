## Server-Send Events
SSE는 HTTP 연결을 통해, 클라이언트에서 자동 업데이트를 가능하게 하는 서버 push 기술이다.

각각의 notification은 한 쌍의 줄바꿈으로 종료되는 텍스트뭉치로 전송된다.

### 사용법
한 라우트에서 SSE가 가능하게 하기 위해서, `Sse()` 데코레이터와 함께 메소드를 작성해야 한다.

```tsx
@Sse('sse')
sse(): Observable<MessageEvent> {
  return interval(1000).pipe(map((_) => ({ data: { hello: 'world' } })));
}
```

> HINT\
> `@Sse()` 데코레이터와 `MessageEvent` 인터페이스는 `@nestjs/common`에서 import되고, `Observable`, `interval`, `map`은 `rxjs` 라이브러리에서 import된다.

> WARNING\
> SSE 루트는 반드시 `Observable` 스트림을 리턴해야 한다.

위 예시에서, 우리는 `sse`라는 네이밍의 라우트를 정의했고, 이 라우트는 실시간 업데이트를 가능하게 한다.\
이러한 이벤트들은 `EventSource API`를 사용해 구독? 할 수 있다.

`sse` 메소드는 여러개의 `MessageEvent`를 발생시키는 `Observable`을 리턴한다.\
`MessageEvent` 객체는 규격과 일치하도록 아래 인터페이스를 준수해야만 한다.

```tsx
export interface MessageEvent {
  data: string | object;
  id?: string;
  type?: string;
  retry?: number;
}
```

이를 통해, 우리는 클라이언트 측 응용프로그램에서 `EventSource`클래스의 인스턴스를 생성할 수 있고,\
생성자 인수로 `/sse` 라우트를 전달할 수 있다.

`EventSource` 인스턴스는 HTTP 서버와 지속석인 연결을 생성하며,\
HTTP 서버는 `text/event-stream` 형태로 이벤트를 전송한다.\
이 연결은 `EventSource.close()`가 호출될 때 까지 지속된다.

일단 연결된 후 서버로 들어오는 메세지는 코드에 이벤트의 형태로 전송된다.\
만약 수신된 메세지에 이벤트 필드가 존재한다면, 트리거된 이벤트는 이벤트 필드 값과 동일하다.\
만약 이벤트 필드가 없으면, 일반적인 메세지 이벤트가 발생한다.

```tsx
const eventSource = new EventSource('/sse');
eventSource.onmessage = ({ data }) => {
  console.log('New message', JSON.parse(data));
};
```

### example
예시는 아래 링크에서 찾아볼수 있다.\
https://github.com/nestjs/nest/tree/master/sample/28-sse


