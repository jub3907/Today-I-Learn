## Authentication
인증은 대부분의 Application에서 필수 요소이다. 인증을 처리하는 전략과 방법은 아주 다양하며,\
프로젝트가 의존하는 Application 요구사항에 맞춰서 선택해야 한다.

이번 챕터에서는 다양한 요구사항에 맞춰 결합할 수 있는 인증 구현 방법에 대해 알아보고자 한다.


Passport.js는 Node.js에서 인기있는 인증 라이브러리이다. 높은 인지도를 지니면서, 동시에\
많은 실제 Application에서 사용되고 있다. Nestjs는 `@nestjs/passport` 모듈을 이용해 Passport를 사용할 수 있다.

Passport는 다음과 같이 사용한다.

* 요청에 포함된 자격 증명을 바탕으로, 사용자를 인증한다. 자격 증명은 이름이나 암호, JSON 웹 토큰, 인증 공급자가 발급한 인증 토큰 등이 될 수 있다.
* 인증 상태의 유지가 필요한 경우 Session을 유지하거나, JWT같은 토큰 인증을 이용한다.
* 인증된 사용자의 정보 객체는 라우팅 행들러에서 사용할 수 있도록 `Request` 객체에 프로퍼티로 추가한다.

PAssport는 다양한 인증 매커니즘을 구현하는 전략을 제공한다. 인증을 위한 다양한 과정을 표준 패턴으로 추상화하며,\
`nestjs/passport` 패키지는 그러한 패턴을 Nest에 맞게 감싸고, 표준화 한다.

이 중, JWT를 사용하는 방법에 대해서만 알아보자.

## JWT 인증 전략
이번에 사용할 JWT 인증 시스템의 요구사항은 아래와 같다.
* 최초 인증은 이름과 암호로 진행하며, 다음 요청부터는 JWT를 사용해 인증한다.
* 헤더를 통해 넘어오는 유효한 JWT를 Bearer 토큰으로서, 특정 API 라우팅을 보호한다.


### 패키지 설치
JWT 구현을 위해 다음 패키지가 필요하다.
* `@nestjs/jwt`
* `passport-jwt`
* `@types/passport-jwt`

`@nestjs/jwt` 모듈은 JWT 조작을 도와주는 유틸리티 패키지이다. 그리고 `passport-jwt`는 JWT 전략을 구현하는 Passport 패키지이다.

### JWT 발행
`Post /auth/login` 요청이 처리하는 라우팅 핸들러에는 `passport-local` 전략의 `AuthGuard`를 사용했었다. 이는 아래와 같은 의미를 가진다.
* 라우팅 행들러는 반드시 사용자가 검증되어야만 실행된다.
* `req` 매개변수는 `user` 프로퍼티를 가진다. 이는 Passport가 passport-local 인증 과정에서 삽입한 것이다.

이제 이 라우팅에서 JWT를 발행해 리턴하도록 할 것이다.

먼저, 다음과 같이 `AuthModule`의 `imports`에서 `JWTModule`을 동적 모듈로서 가져오고, `exports`로 내보낸다.
```tsx
// auth/auth.module.ts

import { JwtModule } from '@nestjs/jwt';
import { jwtConstants } from './constants';

@Module({
  imports: [
    UsersModule,
    PassportModule,
    JwtModule.register({
      secret: jwtConstants.secret,
      signOptions: { expiresIn: '60s' },
    }),
  ],
  providers: [AuthService, LocalStrategy],
  exports: [AuthService, JwtModule],
})
export class AuthModule {}
```

`JwtModule`은 서명된 JWT를 발행해주는 JwtService를 내보낸다.\`Module.register()`에는 아래 설정을 사용할 수 있다.
- `secret: string | Buffer` — HMAC 알고리즘에 사용할 비밀키입니다.
- `publicKey: string | Buffer` — 비대칭 알고리즘에 사용할 PEM 인코딩된 공개키입니다.
- `privateKey: jwt.Secret` —  비대칭 알고리즘에 사용할 PEM 인코딩된 비밀키입니다. passphrase도 포함합니다. (`{ key, passphrase }`)
- `secretOrKeyProvider: Function` — 비밀키나 비대칭키를 포함하는 객체를 리턴하는 함수입니다. 동적으로 키를 생성하려는 경우에 사용합니다. (`(reqType, tokenOrPayload, options?) => jwt.Secret`)
- `verifyOptions: jwt.VerifyOptions` — 만료 옵션 등의 전역 JWT 옵션을 지정합니다. 여기서 지정한 옵션은 `jwtService`의 메서드를 사용할 때 지정하는 옵션에 의해 무시될 수 있습니다. 

또한, 위 예제에서 secret은 다음과 같이 별도 파일에서 보관해야 한다.

```tsx
// auth/constants.ts

export const jwtConstants = {
  secret: 'my-secret-key',
};
```

이제 다음과 같이 `AuthService`에 `jwtService`를 주입하고, 주어진 사용자 정보로 서명된 JWT를 발급해주는 `login()`를 구현한다.
```tsx
// auth/auth.service.ts

import { JwtService } from '@nestjs/jwt';

@Injectable()
export class AuthService {
  constructor(
    private usersService: UsersService,
    private jwtService: JwtService,
  ) {}

  async validateUser(username: string, pass: string): Promise<any> {
    const user = await this.usersService.findOne(username);
    if (user && user.password === pass) {
      const { password, ...result } = user;
      return result;
    }
    return null;
  }

  async login(user: any) {
    const payload = { username: user.username, sub: user.userId };
    return {
      access_token: this.jwtService.sign(payload),
    };
  }
}
```

`login()` 메소드는 주어진 `user` 정보를 payload로 삼아, `jwtService`의 `sign()` 메소드를 사용하여 서명한 JWT를 생성하고 있다.\
여기서 JWT 표준을 준수하기 위해 `sub` 프로퍼티에는 `userId` 값을 지정했다.

이제 컨트롤러를 다음과 같이 세팅하고, JWT를 리턴하는 코드를 생성한다.
```tsx
import { Controller, Request, Post, UseGuards } from '@nestjs/common';
import { LocalAuthGuard } from './auth/local-auth.guard';
import { AuthService } from './auth/auth.service';

@Controller()
export class AppController {
  constructor(private authService: AuthService) {}

  @UseGuards(LocalAuthGuard)
  @Post('auth/login')
  async login(@Request() req) {
    return this.authService.login(req.user);
  }
}

```

## Passport 전략 구현
특정 라우팅에서 JWT 인증을 구현하기 위해선 `Passport` 전략을 구현해야 한다.\
Passport의 passport-jwt 전략을 사용해 보자.

`auth` 폴더의 아래 내용의 `jwt.strategy.ts` 파일을 생성하자.
```tsx
// auth/jwt.strategy.ts

import { ExtractJwt, Strategy } from 'passport-jwt';
import { PassportStrategy } from '@nestjs/passport';
import { Injectable } from '@nestjs/common';
import { jwtConstants } from './constants';

@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy) {
  constructor() {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      ignoreExpiration: false,
      secretOrKey: jwtConstants.secret,
    });
  }

  async validate(payload: any) {
    return { userId: payload.sub, username: payload.username };
  }
}
```

`JwtStrategy`에선 `super()`로 몇 가지 설정이 가능하다.\
대표적으로 아래와 같은 설정들이 가능하며, 더 많은 설정은 [문서](https://github.com/mikenicholson/passport-jwt#configure-strategy)를 참고하자.

- `jwtFromRequest` — `Request`에서 JWT 토큰을 어떻게 추출할지 결정하는 메서드를 지정합니다. 여기서는 API 요청의 `Authorization:` 헤더 값을 Bearer 토큰으로 삼는 표준 접근법을 택했습니다. 다른 옵션은 [passport-jwt 공식 문서](https://github.com/mikenicholson/passport-jwt#extracting-the-jwt-from-the-request)를 참조하세요.
- `ignoreExpiration` — `false`를 지정하면 JWT의 만료를 확인하는 책임을 Passport에 맡깁니다. 만료된 JWT로 요청하면 401 Unauthorized 응답 코드와 함께 요청이 거부처리됩니다.
- `secretOrKey` — 토큰을 서명할 키를 지정합니다. HMAC 대칭키 또는 PEM 인코딩된 비대칭키를 제공할 수 있습니다. 키는 절대로 외부에 공개되어선 안 됩니다.

Passport는 PassportStrategy를 통해 JWT 서명을 검증하고, Payload의 JSON을 파싱한다.\
그리고 파싱한 JSON을 인자로 `validate()`를 호출한다.

전달받은 JSON은 서명 검증이 끝난 것이며, 남은 할 일은 `validate()` 메소드에서 JSON 데이터 내용의 유효성을 검증하는 것이다.

하지만, 위 예제에선 별도 검증을 거치지 않고, 그냥 사용자 객체로 변환해 바로 리턴하고 있다.\
이전에 배운 것 처럼, `validate()`에서 리턴한 객체는 `Request`에 `user` 프로퍼티로 설정된다.


이러한 접근법은 JWT 인증 과정에 추가적인 비즈니스 로직을 주입할 수 있다는 장점이 존재한다.\
예를 들어, validate() 메소드에서 데이터베이스를 조회해 더 많은 사용자 정보를 `Request` 객체에 주입할 수 있고,\
파기된 토큰 목록을 이용해 JWT 토큰에 추가적인 검증을 할 수도 있다.


이번 예제는 `Stateless JWT` 모델을 따른다. 인증은 JWT의 서명만을 검증하여 이뤄지고,\
요청 사용자의 정보도 적으면서 속도는 매우 빠르다.

아래와 같이 `JwtStrategy`를 `AuthModule`에 주입해 보자.
```tsx
// auth/auth.module.ts

@Module({
  imports: [
    UsersModule,
    PassportModule,
    JwtModule.register({
      secret: jwtConstants.secret,
      signOptions: { expiresIn: '60s' },
    }),
  ],
  providers: [AuthService, LocalStrategy, JwtStrategy],
  exports: [AuthService],
})
export class AuthModule {} 
```

이제 `LocalAuthGuard`처럼, 다음과 같이 `JwtAuthGuard`를 생성한다.

```tsx
// auth/jwt-auth.guard.ts

@Injectable()
export class JwtAuthGuard extends AuthGuard('jwt') {}
```

## JWT 전략으로 라우팅 보호

이제, `JwtAuthGuard`를 사용해 라우팅을 보호할 수 있다. `app.controller.ts`를 열어 다음과 같이 `profile` 라우팅을 추가하고, 가드를 추가하자.
```tsx
import { Controller, Get, Request, Post, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from './auth/jwt-auth.guard';
import { LocalAuthGuard } from './auth/local-auth.guard';
import { AuthService } from './auth/auth.service';

@Controller()
export class AppController {
  constructor(private authService: AuthService) {}

  @UseGuards(LocalAuthGuard)
  @Post('auth/login')
  async login(@Request() req) {
    return this.authService.login(req.user);
  }

  @UseGuards(JwtAuthGuard)
  @Get('profile')
  getProfile(@Request() req) {
    return req.user;
  }
}
```
`JwtAuthGuard`를 적용한 덕에, `AuthGuard`는 jwt 이름의 전략으로 자동 프로비저닝 된다.\
이제 `GET /profile` 라우팅이 들어오게 되면, 가드가 커스텀 구성한 passport-jwt 로직을 실행해 JWT를 검증하고,\
`Request` 객체에 `user` 프로퍼티를 추가해 줄 것이다.
