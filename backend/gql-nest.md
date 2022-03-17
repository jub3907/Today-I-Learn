이번엔 GraphQL Schema를 설계할 때, 피해야할 antipatterns에 대해 적어보자.

### ✅ **Nullable Fields**

첫 번째는 Nullable 필드이다.

클라이언트에서는 특정한 필드 값을 서버에 넘겨주지 않는다면 오류가 발생할 것으로 예상하고, 그에 맞는 코드를 구성한다.

하지만 Nullable Field가 존재하는 경우, 필드 값이 존재하지 않더라도 오류가 발생하지 않기 때문에, 프론트에서 예기치 않은 오류가 발생할 수 있다.

물론 NestJS에선 기본적으로 필드에 `nullable: True`를 설정해 주지 않는다면 오류가 발생하지 않는다.

Nullable을 넣는 경우, 이 Nullable이 꼭 필요한 세팅인지, 다른 대체할 수 있는 방향은 없는지 고려해봐야 한다.

### ✅ **Lengthy arguments to mutation**

다음은 **Mutation의 인자**에 관련된 내용이다.

Mutation은 CUD 작업을 수행함에 있어, 여러가지 인자가 필요하다.

예를 들어, 아래와 같은 인자가 필요하다고 가정해 보자.

```jsx
type MutationResponse {
  status: String!
}

type Mutation {
  createPassenger(name: String!, age: String!, address: String!): MutationResponse
}

# Query in Frontend looks like:
mutation PassengerMutation($name: String!, $age: String!, $address: String! ) {
  createPassenger(name: $name, age: $age, address: $address ) {
    status
  }
}
```

위처럼 모든 인자를 다 따로, 모두 `ArgsType`으로 관리하면 프론트에서 알아보기가 너무 어렵고, 유지보수도 어려워진다.

따라서 여러 개의 인자를 사용하게 되면 **Input Object**를 사용하는 편이 좋다.

Input Object는 Mutation에 들어가는 모든 인자를 하나의 객체로 사용하는 방식 ( `InputType` )이며, 이를 통해 데이터의 가독성과 유지보수성을 증진시킬 수 있다.

```jsx
type MutationResponse {
  status: String!
}

type PassengerData {
  name: String!
  age: String!
  address: String!
}

type Mutation {
  createPassenger(passenger: PassengerData!): MutationResponse
}

# Query in Frontend looks like:
mutation PassengerMutation($passenger: PassengerData! ) {
  createPassenger(passenger: $passenger) {
    status
  }
}
```

TODO: `ArgsType`과 `InputType`을 사용하는 **명확한** 기준이 존재하는가?

### ✅ **Insufficient Mutation Response**

GQL에서 Mutation은 데이터를 변경하는 모든 작업에서 필요하다.

실제 응용 프로그램에서 Mutation이 실행된다면 백엔드 측의 데이터가 변경되고, 이에 맞춰 프론트의 데이터를 업데이트 시켜야만 한다.

이러한 업데이트는 Mutation이 종료된 뒤 Query를 다시 요청하는 것으로 수행할 수도 있지만, ( `refetchQuery` ) 추가적인 요청에 대한 리소스가 들어간다는 단점이 존재한다.

Mutation의 리턴 값은 자유롭게 작성이 가능하므로, 만일 프론트에서 변경된 데이터가 필요하지만 Mutation 결과만 리턴해 준다면, 이는 **Antipattern**이라고 지칭할 수 있다.

### **✅ Allowing invalid inputs**

GQL은 `Int` `Float` `String` `Boolean` `ID`, 총 5개의 scalar type만 지원한다.

하지만 실제 응용 프로그램에서, 위 5개의 타입만으로 데이터를 관리하기엔 유효하지 않은 입력 값이 인자로 넘어갈 가능성이 크다.

→ `class-validator`로 관리.

→ 해당 문서에서 설명하는 것은 [Enum](https://docs.nestjs.com/graphql/unions-and-enums#enums)과 [Custom Scalar](https://docs.nestjs.com/graphql/scalars#scalars)이며, 공식문서에서 잘 설명되어 있음.

### ✅ Massive data in response

실제 응용 프로그램에서 메인으로 사용하는 데이터 ( ex) `post` )에 대한 모든 데이터를 한번에 불러온다면 클라이언트 측에서 감당할 수 없다.

이러한 사태를 방지하기 위해, GraphQL은 `limit`과 `offset`을 사용한 페이지네이션을 지원하고 있다.

하지만 실수로 limit을 설정하지 않는다면 쿼리가 제한없이 실행되어 서버에서 대량의 데이터를 불러올 수 있고, 이는 스키마 설계시 `default value`를 설정해 문제를 해결할 수 있다.
