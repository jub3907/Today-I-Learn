# 모델-뷰-컨트롤러 패턴

Nest에서는 기본적으로 Express 라이브러리를 사용하기 때문에, Express에서 사용하는 MVC 구현 패턴을 Nest에서도 사용할 수 있습니다.

# Express 플랫폼

Express 플랫폼 환경에서는 템플릿 렌더링을 위해 다음 패키지가 필요합니다.

- `hbs` ([handlebars.js 패키지](https://github.com/pillarjs/hbs#readme))

꼭 Handlebars 엔진이 아니더라도 필요에 따라 적절한 패키지를 사용해도 됩니다.

## 구성

Handlebars 엔진을 구성하기 위해 `main.ts`에서 다음과 같이 Express 설정을 해야 합니다.

```tsx
import { NestFactory } from '@nestjs/core';
import { NestExpressApplication } from '@nestjs/platform-express';
import { join } from 'path';
import { AppModule } from './app.module';

async function bootstrap() {
  const app = await NestFactory.create<NestExpressApplication>(
    AppModule,
  );

  app.useStaticAssets(join(__dirname, '..', 'public'));
  app.setBaseViewsDir(join(__dirname, '..', 'views'));
  app.setViewEngine('hbs');

  await app.listen(3000);
}
bootstrap();
```

먼저 정적 애셋 파일 처리를 위해 `useStaticAssets()` 메서드로 `public` 폴더 경로를  지정하였습니다. 그리고 템플릿 파일이 들어갈 폴더를 `setBaseViewDir()` 메서드로 지정하였고, `setViewEngine()` 메서드를  통해 뷰 엔진을 `hbs`로 설정하였습니다.

## 템플릿 렌더링

이제 `views` 폴더에 다음과 같은 `index.hbs` 템플릿 파일을 만듭니다. 이 템플릿은 컨트롤러에서 전달받은 `message` 문자열을 출력합니다.

```html
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8" />
    <title>App</title>
  </head>
  <body>
    {{ message }}
  </body>
</html>
```

이제 `app.controller.ts`에서 다음과 같이 `root()` 라우팅 핸들러 메서드를 작성합니다.

```tsx
import { Get, Controller, Render } from '@nestjs/common';

@Controller()
export class AppController {
  @Get()
  @Render('index')
  root() {
    return { message: 'Hello world!' };
  }
}
```

`@Render()` 데코레이터로 템플릿을 지정하였음에 주목하세요. 그리고 템플릿에 전달될 객체를 리턴하고 있습니다.

## 동적 템플릿 렌더링

애플리케이션이 렌더링할 템플릿을 동적으로 결정해야한다면, 다음과 같이 응답 객체로 직접 렌더링해야합니다.

```tsx
import { Get, Controller, Res, Render } from '@nestjs/common';
import { Response } from 'express';
import { AppService } from './app.service';

@Controller()
export class AppController {
  constructor(private appService: AppService) {}

  @Get()
  root(@Res() res: Response) {
    return res.render(
      this.appService.getViewName(),
      { message: 'Hello world!' },
    );
  }
}
```

<aside>
💡 `@Res()` 데코레이터를 사용하면 라이브러리에 종속되는 `response` 객체를 주입받습니다. Express의 Response API에 대한 내용은 [Express 공식 문서](https://expressjs.com/en/api.html)를 참조하세요.

</aside>

## 예제

실제 작동하는 Express 플랫폼 기반 MVC 애플리케이션의 전체 예제는 [Nest GitHub 저장소](https://github.com/nestjs/nest/tree/master/sample/15-mvc)에 있습니다.

# Fastify 플랫폼

MVC 애플리케이션을 Fastify에서 구현하려면 다음 패키지를 설치해야합니다.

- `fastify-static` (정적 파일 서브)
- `point-of-view`
- `handlebars`

## 구성

Express와 마찬가지로 다음과 같이 `main.ts`에서 Fastify에 대해 구성 설정을 해주어야합니다.

```tsx
import { NestFactory } from '@nestjs/core';
import { NestFastifyApplication, FastifyAdapter } from '@nestjs/platform-fastify';
import { AppModule } from './app.module';
import { join } from 'path';

async function bootstrap() {
  const app = await NestFactory.create<NestFastifyApplication>(
    AppModule,
    new FastifyAdapter(),
  );
  app.useStaticAssets({
    root: join(__dirname, '..', 'public'),
    prefix: '/public/',
  });
  app.setViewEngine({
    engine: {
      handlebars: require('handlebars'),
    },
    templates: join(__dirname, '..', 'views'),
  });
  await app.listen(3000);
}
bootstrap();
```

Fastify은 구성 방법은 Express와 많은 차이를 보입니다.

## 템플릿 렌더링

구성 방법은 Express와 많이 다르지만, 라우팅 핸들러 메서드에서 사용하는 방법은 거의 동일합니다. 단, `@Render()` 메서드의 인자에 확장자까지 지정해줘야한다는 차이는 있습니다.

```tsx
import { Get, Controller, Render } from '@nestjs/common';

@Controller()
export class AppController {
  @Get()
  @Render('index.hbs')
  root() {
    return { message: 'Hello world!' };
  }
}
```

## 예제

실제 작동하는 Fastify 플랫폼 기반 MVC 애플리케이션의 전체 예제는 [Nest GitHub 저장소](https://github.com/nestjs/nest/tree/master/sample/17-mvc-fastify)에 있습니다.
