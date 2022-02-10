# 순환 의존

두 클래스가 서로 의존하게되면 순환 의존 문제가 발생한다. Nest에서는 모듈 또는 프로바이더 간에 이런 문제가 생길 수 있다.

순환 의존은 피해야만 하지만, 그럴 수 없을 때도 존재한다.\
그런 경우 Nest는 프로바이더 간의 순환 의존을 두 가지 방법으로 해결할 수 있다.\
이 챕터에선 **전방 참조** 기법과 **ModuleRef 클래스**를 이용해 DI 컨테이너에서 프로바이더 인스턴스를 얻는 방법을 설명한다.\
또한 모듈 간의 순환 의존을 해결하는 방법도 설명합니다.

> ⛔ **경고**\
> 순환 의존은 각종 의존성 클래스가 그룹화된 [배럴 파일](https://aerocode.net/334)(`배럴폴더/index.ts`)을 이용하여 `import` 코드를 작성하는 경우에도 일어날 수 있다.\
> 모듈이나 프로바이더 클래스에 대해는 배럴 파일을 생략해야 한다.\
> 예를 들면 배럴 폴더 내에서 배럴 파일을 통해 같은 디렉터리의 파일을 가져오면 안 된다.\
> 즉, `src/cats/cats.controller.ts` 파일에서 `import { CatsService } from '.'`를 이용해 \
> `/src/cats/cats.service.ts` 파일의 클래스을 불러오지 말아야 한다. \
> 이 문제를 자세히 알아 보려면 [Nest GitHub 저장소의 이슈](https://github.com/nestjs/nest/issues/1181#issuecomment-430197191)를 읽어보자.


# 전방 참조 (forwardRef)

**전방 참조**는 유틸리티 함수인 `forwardRef()`를 이용하여 아직 정의하지 않은 클래스를 Nest가 참조할 수 있게 해준다. \
예를 들면 `CatsService`와 `CommonService`가 서로 의존하는 경우, \
양쪽 생성자에 `@Inject()`와 `forwardRef()` 함수를 사용해서 순환 의존을 해결할 수 있다. \
그렇지 않으면 필수적인 메타메이터를 사용할 수가 없기에 Nest에서 의존성을 인스턴스화할 수 없다.

```tsx
// cats.service.ts
@Injectable()
export class CatsService {
  constructor(
    @Inject(forwardRef(() => CommonService))
    private commonService: CommonService,
  ) {}
}

// common.service.ts
@Injectable()
export class CommonService {
  constructor(
    @Inject(forwardRef(() => CatsService))
    private catsService: CatsService,
  ) {}
}
```
> ⛔ **경고**\
> 이런 경우 인스턴스화되는 순서는 불확실하므로, 생성자의 호출 순서에 의존하는 코드는 작성하지 말라.


## 대체 유틸리티 클래스 ModuleRef

`forwardRef()` 대신에 유틸리티 클래스인 `ModuleRef`를 사용하면, \
순환 관계의 한쪽에서만 공급자를 검색하게 할 수 있다. \
유틸리티 클래스 `ModuleRef`에 대해서는 [모듈 참조](https://www.notion.so/51bf4852da124e88bec97be5ba6e9a66) 챕터에서 설명합니다.

## 모듈 간 전방 참조

모듈 간의 순환 의존 문제도 양쪽 모듈에 `forwardRef()` 함수를 써서 해결할 수 있습니다.

```tsx
// common.module.ts

@Module({
  imports: [forwardRef(() => CatsModule)],
})
export class CommonModule {}
```
