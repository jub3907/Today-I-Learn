## 파일 업로드

Nest에서는 파일 업로드를 처리하기 위해, [Multer 미들웨어](https://github.com/expressjs/multer)를 내장하고 있다.\
Express는 Multer를 이용해 `multipart/form-data` 포맷으로 `POST` 요청되는 데이터를 처리한다.

> 경고\
> Multer는 Multipart 포맷(`multipart/form-data`)이 아닌 데이터는 처리하지 못합니다.\
> 그리고 Multer는 `FastiftyAdapter`가 아니므로, Fastify 플랫폼에서 사용할 수 없습니다.

Muilter에 대한 타입 지원이 필요한 경우, 아래 패키지를 설치합니다.

- `@types/multer`

이 패키지를 설치하면 `express` 패키지로부터 `Express.Multer.File` 타입을 가져와서 사용할 수 있다.

## 파일 업로드 인터셉터

단일 파일 업로드를 처리하려면 `POST` 라우팅 핸들러에 `FileInterceptors` 인터셉터를 사용한다.\
그리고 요청 객체에서 `file`을 추출해주는 `@UploadedFile()` 데코레이터를 사용한다.

```tsx
@Post('upload')
@UseInterceptors(FileInterceptor('file'))
uploadFile(@UploadedFile() file: Express.Multer.File) {
  console.log(file);
}
```

## 인터셉터 옵션

`FileInterceptor()`의 첫번째 인자는 `fieldName: string`이고, 파일을 쥐고있는 HTML form의 필드 이름을 의미한다.\
두 번째 인자인 `options: MulterOptions`는 내부적으로 Multer의 생성자에 사용될 설정 객체이다.

## 전역 옵션 등록

인터셉터마다 일일이 옵션을 설정하는 대신, `MulterModule`을 등록해 기본 설정을 전역으로 지정해 사용할 수 있다.

```tsx
MulterModule.register({
  dest: "./upload",
});
```

## 전역 옵션 비동기 등록

`MulterModule.registerAsync()` 메소드를 이용하면 기본 설정을 비동기적으로 지정할 수 있다.

### factory 함수 사용 방법

설정 객체를 반환하는 팩토리 함수를 제공한다.

```tsx
MulterModule.registerAsync({
  useFactory: () => ({
    dest: "./upload",
  }),
});
```

이 때, 팩토리 프로바이더와 같이 `async` 비동기 함수 사용과 `inject`를 통해 의존성 주입이 가능하다.

```tsx
MulterModule.registerAsync({
  imports: [ConfigModule],
  useFactory: async (configService: ConfigService) => ({
    dest: configService.getString("MULTER_DEST"),
  }),
  inject: [ConfigService],
});
```

### useClass 방법

`MulterOptionsFactory` 인터페이스를 구현하는 클래스를 사용한다.

```tsx
@Injectable()
class MulterConfigService implements MulterOptionsFactory {
  createMulterOptions(): MulterModuleOptions {
    return {
      dest: "./upload",
    };
  }
}

MulterModule.registerAsync({
  useClass: MulterConfigService,
});
```

위 예제의 경우, `MulterConfigService`가 `MulterModule`에서 인스턴스화 되고,\
`createMulterOptions()` 메소드를 통해 설정 객체를 획득한다.

또는, 아래와 같이 `useExisting`을 이용해 다른 모듈이 내보낸 클래스를 사용할 수도 있다.

```tsx
MulterModule.registerAsync({
  imports: [ConfigModule],
  useExisting: ConfigService,
});
```

## 예제

실제로 작동하는 전체 예제는 [링크](https://github.com/nestjs/nest/tree/master/sample/29-file-upload)에 있다.
