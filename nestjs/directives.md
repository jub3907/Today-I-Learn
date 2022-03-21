## Directives

directive는 Field나 fragment에 부착되어, 서버가 원하는대로 쿼리가 동작하도록 영향을 줄 수 있다. GQL의 명세서에는 기본적으로 아래와 같은 지침이 적혀있다.

- `@include(if: Boolean)` : 주어진 파라미터가 참값인 경우에만, 해당 필드를 포함한다.
- `@skip(if: Boolean)` : 주어진 파라미터가 참값인 경우, 해당 필드를 스킵한다.
- `@deprecated(reason: String)` : 메세지와 함께, 해당 필드를 deprecated 처리한다.

directive는 `@` 문자 앞에 이어지는 식별자이고, 그 뒤에는 명명한 파라미터들이 표시된다.\
이 파라미터들은 GQL 쿼리와 스키마에 정의된 거의 모든 요소 뒤에 표시될 수 있다.

### Custom directives

Apollo/Mercurius가 directive를 만났을 때, 어떤 일이 발생하는지 알아보기 위해 예시를 하나 만들어 보자.\
이 함수 는 `mapSchema` 함수를 사용해 필드 내의 위치를 반복하고(?), 이에 맞는 변환을 수행한다.

```tsx
import { getDirective, MapperKind, mapSchema } from "@graphql-tools/utils";
import { defaultFieldResolver, GraphQLSchema } from "graphql";

export function upperDirectiveTransformer(
  schema: GraphQLSchema,
  directiveName: string
) {
  return mapSchema(schema, {
    [MapperKind.OBJECT_FIELD]: (fieldConfig) => {
      const upperDirective = getDirective(
        schema,
        fieldConfig,
        directiveName
      )?.[0];

      if (upperDirective) {
        const { resolve = defaultFieldResolver } = fieldConfig;

        // Replace the original resolver with a function that *first* calls
        // the original resolver, then converts its result to upper case
        fieldConfig.resolve = async function (source, args, context, info) {
          const result = await resolve(source, args, context, info);
          if (typeof result === "string") {
            return result.toUpperCase();
          }
          return result;
        };
        return fieldConfig;
      }
    },
  });
}
```

이제 위에서 만든 `upperDirectiveTransformer` 함수를 `transformSchema` 함수를 사용해 `GraphQLModule`의 `forRoot`에 적용시키자.

```tsx
GraphQLModule.forRoot({
  // ...
  transformSchema: (schema) => upperDirectiveTransformer(schema, "upper"),
});
```

일단 한번 등록된 뒤엔, `@upper` directive는 스키마 내부에서 사용될 수 있다.\
하지만, directive를 적용하는 방법은 `code first` 방식과 `schema fist` 방식으로 나뉜다.

### Code First

`code first` 접근 방법에선, `Directive()` 데코레이터를 사용해 directive를 적용할 수 있다.

```tsx
@Directive('@upper')
@Field()
title: string;
```

Directive는 필드나 FieldResolver, input, Object Type 뿐만 아니라 Query와 Mutation까지도 적용될 수 있다.

```tsx
@Directive('@deprecated(reason: "This query will be removed in the next version")')
@Query(returns => Author, { name: 'author' })
async getAuthor(@Args({ name: 'id', type: () => Int }) id: number) {
  return this.authorsService.findOneById(id);
}
```

> `@Directive()` 데코레이터를 사용해 적용된 Directive는 생성된 스키마 정의 파일에는 반영되지 않습니다.

마지막으로, `GraphQLModule`에서 해당 directive를 선언해 주자.

```tsx
GraphQLModule.forRoot({
  // ...,
  transformSchema: schema => upperDirectiveTransformer(schema, 'upper'),
  buildSchemaOptions: {
    directives: [
      new GraphQLDirective({
        name: 'upper',
        locations: [DirectiveLocation.FIELD_DEFINITION],
      }),
    ],
  },
}),
```
