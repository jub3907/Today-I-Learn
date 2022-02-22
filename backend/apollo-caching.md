## Apollo Client의 Server-side caching

아폴로 클라이언트는 각 field를 기반으로 캐싱을 구성할 수 있다.

이는 `@cacheControl`이라는 데코레이터를 통해 구현되며, 각 `maxAge`와 `scope` 파라미터를 갖는다.

```tsx
type Post {
  id: ID!
  title: String
  author: Author

  votes: Int @cacheControl(maxAge: 30)
  comments: [Comment]

  readByCurrentUser: Boolean! @cacheControl(maxAge: 10, scope: PRIVATE)
}

```

아폴로 서버는 요청에 응답할 때 응답field의 가장 **제한적인** 설정을 기준으로, 적절한 캐시 동작 방식을 계산한다.

### Setting cache hints
위에서 언급한 field-level caching은 두 가지 방법이 존재한다.\
하나는 **schema 정의에서 정적으로** 선언하는 방법과, **resolver에서 동적으로** 선언하는 방법이다.

캐시를 설정할 때, 아래 세 가지를 유념하자.
* 스키마에서, 어떤 Field가 안전하게 cache 될 수 있는지.
* 캐시의 유효 기간은 얼마가 적절할지
* 캐시가 global하게 세팅될지, 각 유저마다 캐시를 진행할 지


### In your Schema, Static
아폴로 서버는 스키마에서 단일 필드, 혹은 특정한 유형을 반환하는 **모든 필드**에 대해 캐싱 동작을 정의하는데 사용할 수 있는\
`@cacheControl` 지시문을 지해할 수 있다.

**`@cacheControl` 지시문을 사용하기 위해서, 아래 정의를 스키마에 등록해야만 한다.**
```tsx
enum CacheControlScope {
  PUBLIC
  PRIVATE
}

directive @cacheControl(
  maxAge: Int
  scope: CacheControlScope
  inheritMaxAge: Boolean
) on FIELD_DEFINITION | OBJECT | INTERFACE | UNION

```

위의 정의를 등록하지 않는다면 아폴로 서버에선 `@cacheControl` 지시문을 이해할 수 없고, 오류를 발생시킨다.

`@cacheControl` 지시문은 아래의 파라미터들을 받는다.
* `maxAge` : 캐시의 유효 기간을 설정한다. 초 단위로 입력받고, 기본 값은 0이다. [링크](https://www.apollographql.com/docs/apollo-server/performance/caching/#setting-a-different-default-maxage)를 참고해 기본값을 변경할 수 있다.
* `scope` : `PRIVATE`로 설정하는 경우, 각 유저마다 캐시가 진행된다. 기본값은 `PUBLIC`이며, PRIVATE으로 설정할 시의 동작 방식은 [링크](https://www.apollographql.com/docs/apollo-server/performance/caching/#identifying-users-for-private-responses)를 참고하자.
* 'inheritMaxAge` : `true`로 설정된 경우, 해당 필드는 default로 설정된 `maxAge`값 대신 부모 필드의 `maxAge`를 사용한다.

만약 캐시의 설정이 런타임마다 달라지는 경우, [dynamic method](https://www.apollographql.com/docs/apollo-server/performance/caching/#in-your-resolvers-dynamic)를 참고하자.

>**Important** : 아폴로 서버는 각 GQL 응답의 캐시에서, 해당 응답에 포함된 모든 필드의 `maxAge`중 최소값을 사용한다.\
>즉, 만약 특정한 필드의 `maxAge`가 0으로 설정되어 있다면, 응답은 캐시가 진행되지 않는다.

> 이와 유사하게, `scope` 속성 역시 한 field라도 `PRIVATE`로 설정되어 있다면, `PRIVATE` 속성으로 진행된다.


#### Field-level Definition
아래 예시는 쿼리가 아닌 각 필드에 캐시를 진행한 예시이다.
```tsx
type Post {
  id: ID!
  title: String
  author: Author

  votes: Int @cacheControl(maxAge: 30)
  comments: [Comment]

  readByCurrentUser: Boolean! @cacheControl(maxAge: 10, scope: PRIVATE)
}

```
위 예시에서, `votes`의 값은 최대 30초간 캐시된다.

또한 `readByCurrentUser`의 값은 최대 10초간 캐시되며, 각 유저마다 캐시된다.

#### type-level Definition
아래 예시는 `Post` 객체를 리턴하는 모든 스키마 Field에 대한 캐시 설정이다.

```tsx
type Post @cacheControl(maxAge: 240) {
  id: Int!
  title: String
  author: Author
  votes: Int
  comments: [Comment]
  readByCurrentUser: Boolean!
}

```

만약, 스키마에 `Post`를 필드로 갖는 객체 타입이 존재한다면, 이 객체의 값은 최대 240초간 캐시가 진행된다.

```tsx
type Comment {
  post: Post! # Cached for up to 240 seconds
  body: String!
}
```

위 두 방식이 존재하지만, **field-level setting**이 `type-level setting`을 뒤덮는다.(오버라이드 한다.)

아래 예시에서, **Comment.post**는 240초가 아닌, 120초간 캐시가 된다.



### In your resolver, dynamic.
위에서는 정적으로 캐시를 지정해 줬지만, 이와는 반대로

resolving중에 특정 필드의 캐시 방법을 지정할 수도 있다.

이는 **모든 resolver**에 `cacheControl` 객체를 파라미터로 집어 넣어 수행할 수 있다.

> 만약 필드 레벨의 캐싱을 주입했었다면, 스키마에 제공된 캐시를 오버라이딩 한다.

#### `cacheControl.setCacheHint`
`cacheControl` 객체는 `setCacheHint` 방식을 포함하고 있고, 아래와 같이 사용한다.
```tsx
const resolvers = {
  Query: {
    post: (_, { id }, _, info) => {

      info.cacheControl.setCacheHint({ maxAge: 60, scope: 'PRIVATE' });
      return find(posts, { id });
    }
  }
}

```

`setCacheHint` 메소드는 역시 `maxAge`, `scope` 필드 객체를 받는다.

#### `cacheControl.cacheHint`
이 객체는 현재 필드의 캐시 방식을 나타내며, 객체는 아래 내용들을 필드 값으로 갖는다.
* 필드의 현재 `maxAge`와 `scope`
* `setCacheHint` 메소드와 유사하지만, *relax 하는?* `restrict` 메소드

```tsx
// If we call this first...
info.cacheControl.setCacheHint({ maxAge: 60, scope: 'PRIVATE' });

// ...then this changes maxAge (more restrictive) but NOT scope (less restrictive)
info.cacheControl.cacheHint.restrict({ maxAge: 30, scope: 'PUBLIC'});

```

#### `cacheControl.cacheHintFromType`
이 메소드는 특정한 객체 타입을 위한 default 캐시 방식을 리턴한다.

이는 union이나 interface field와 같이 여러 객체들 중 하나를 리턴받는 요청 시 유용하다.


### Calculation cache behavior
보안? 안전?을 위해, 각각의 응답 캐시 방식은 *가장 규제가 강한* 캐시 방식으로 결정되어야 한다.
* 응답의 `maxAge`는 모든 필드 중 가장 `maxAge` 값이 낮은 필드의 값과 같다.
* 모든 필드에서 하나라도 `scope`가 `Private`로 설정되어 있다면, 해당 요청의 `scope`는 `PRIVATE` 이다.

