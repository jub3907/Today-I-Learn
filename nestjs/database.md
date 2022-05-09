## DataBase

Nest에서는 데이터베이스 종류에도 구애받지 않습니다. 따라서 필요에 따라 어떤 종류의 SQL 또는 NoSQL 데이터베이스든 사용할 수 있다. Nest에 데이터베이스를 연결하는 것은 Express나 Fastify에서와 마찬가지로 Node.js 드라이버만 있으면 된다.

NodeJS 범용 데이터베이스 통합 라이브러리나 ORM을 직접 사용할 수 있다. 이들은 데이터베이스를 고도로 추상화 해 데이터베이스 운용에 도움을 준다.\
Nest는 편의상 vusdmltkd TypeORM과 Sequelize를 각각 `@nestjs/typeorm`과 `@nestjs/sequelize` 패키지로 통합할 수 있고, Mongoose는 `@nestjs/mongoose`로 사용할 수 있다.\
이러한 통합은 모델 - 저장소 주입, 테스트 가능성과 비동기 구성같은 NestJS관련 추가 기능들을 제공해 선택한 DB에 더욱 쉽게 엑세스 할 수 있도록 한다.

## TypeORM 통합

SQL이나 NoSQL 데이터베이스를 통합하기 위해 사용할 수 있다. TypeORM은 가장 발달된 TypeScript ORM으로, Typescript로 작성되어 있기 때문에 Nest 프레임워크와 잘 통합된다.

이를 사용하려면 몇 가지 패키지 의존성이 필요하다. TypeORM은 Postres, Oracle, MS SQL, SQLite 등 다양한 RDB와 MongoDB 등의 NoSQL DB를 지원한다. 본 챕터에선 관계영 DBMS인 MySQL을 사용한다.

우선, 아래 패키지를 설치하자.

- `@nestjs/typeorm`
- `typeorm`
- `mysql2`

설치 후, 루트모듈인 `AppModule`에 어댑터 동적 모듈인 `TypeOrmModule`을 추가한다.

```tsx
// app.module.ts

import { Module } from "@nestjs/common";
import { TypeOrmModule } from "@nestjs/typeorm";

@Module({
  imports: [
    TypeOrmModule.forRoot({
      type: "mysql",
      host: "localhost",
      port: 3306,
      username: "root",
      password: "root",
      database: "test",
      entities: [],
      synchronize: true,
    }),
  ],
})
export class AppModule {}
```

> **Warning**
> 실제 제품 환경에서는 `syncronize: true` 옵션을 사용하면 데이터가 손실될 수 있으므로, 사용하면 안된다. 이 옵션은 스키마를 변경시킨다.

모듈이 등록된 뒤 TypeORM의 `Connection`과 `EntityManager`는 전체 프로젝트의 어디서든 추가설정 없이 주입될 수 있다.

## 구성 설정

`forRoot()` 메소드는 모듈을 동적으로 등록하며, TypeORM 패키지의 `createConnection()` 함수에서 [사용 가능한 모든 옵션](https://typeorm.io/migrations#connection-option)을 지원한다.\
또한, 아래 추가 옵션 또한 지원된다.

- `retryAttenpts` -- 데이터베이스 연결을 재시도하는 횟수 제한, default: 10
- `retryDelay` -- 연결 시도 간에 대기하는 ms단위 시간. default: 3000
- `autoLoadEntities` -- 엔티티가 자동으로 로드되는 여부. default: false
- `keepConnectionAlive` -- 어플리케이션이 종료되어도 연결이 닫히지 않도록 설정 여부. default : false

옵션 객체로 연결하는 대신, 프로젝트 루트에 `ormconfig.json`파일을 추가해 설정을 제공할 수 있다.

```json
{
  "type": "mysql",
  "host": "localhost",
  "port": 3306,
  "username": "root",
  "password": "root",
  "database": "test",
  "entities": ["dist/**/*.entity{.ts,.js}"],
  "synchronize": true
}
```

그리고 `forRoot()` 함수엔 아무 인자도 넘기지 않으면 된다.

```tsx
import { Module } from "@nestjs/common";
import { TypeOrmModule } from "@nestjs/typeorm";

@Module({
  imports: [TypeOrmModule.forRoot()],
})
export class AppModule {}
```

그런데, `ormconfig.json`파일은 `typeorm` 라이브러리에서 로드하므로, 위에서 설명한 `forRoot()`에서 사용할 수 있는 추가 프로퍼티는 사용하지 못한다.\
다행히 TypeORM은 `getConnectionOptions()` 함수를 제공하기에, `ormconfig.json` 파일이나 환경 변수를 수동으로 읽어들일 수 있다.

```tsx
TypeOrmModule.forRootAsync({
  useFactory: async () =>
    Object.assign(await getConnectionOptions(), {
      autoLoadEntities: true,
    }),
});
```

## 저장소 패턴

TypeORM은 디장니 패턴 중 하나인 저장소 패턴을 지원한다.\
각 엔티티는 각자의 저장소를 가지며, 데이터베이스 연결을 통해 접근할 수 있다.

### 정의

아래 예제는 `User` 엔티티를 정의한다.

```tsx
// src/users/user.entity.ts

import { Entity, Column, PrimaryGeneratedColumn } from "typeorm";

@Entity()
export class User {
  @PrimaryGeneratedColumn()
  id: number;

  @Column()
  firstName: string;

  @Column()
  lastName: string;

  @Column({ default: true })
  isActive: boolean;
}
```

> Entity에 대한 자세한 내용은 [링크](https://typeorm.io/entities)를 참고하세요.

`Uset` 엔티티 파일은 `uesr` 디렉토리에 위치시킵니다. 이 디렉토리에는 `UsersModule`과 관련된 파일을 모아놓는다. 엔티티 파일은 어디에 두더라도 문제는 없지만, NEst는 해당하는 도메인인 모듈 디렉토리 내에 위치시키는 것을 권장한다.

`User` 엔티티를 사용하려면 TypeORM에 인식시켜줘야한다. TypeORM 구성 설정의 `entities` 배열에 넣어주자.

```tsx
TypeOrmModule.forRoot({
  ...
  entities: [User],
}),
```

아니면 `entities`에 정적 글롭 경로를 지정해, 컴파일된 애플리케이션 디렉토리인 `dist` 디렉토리 내의 모든 `*.entities.js`를 인식키실 수 있다.

```tsx
TypeOrmModule.forRoot({
  ...
  entities: ['dist/**/*.entity{.ts,.js}'],
}),
```

### 주입

이제, `UsersModule`에 `TypeOrmModule.forFeature()` 메소드를 사용해 현재 스코프에 저장소를 등록한다.

```tsx
import { TypeOrmModule } from "@nestjs/typeorm";

@Module({
  imports: [TypeOrmModule.forFeature([User])],
  providers: [UsersService],
  controllers: [UsersController],
})
export class UsersModule {}
```

이제 `UsersService`에 `InjectRepository` 데코레이터를 통해, `UserRepository` 주입이 가능하다.

```tsx
import { Injectable } from "@nestjs/common";
import { InjectRepository } from "@nestjs/typeorm";
import { Repository } from "typeorm";
import { User } from "./user.entity";

@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(User)
    private usersRepository: Repository<User>
  ) {}

  findAll(): Promise<User[]> {
    return this.usersRepository.find();
  }

  findOne(id: string): Promise<User> {
    return this.usersRepository.findOne(id);
  }

  async remove(id: string): Promise<void> {
    await this.usersRepository.delete(id);
  }
}
```

`TypeORMModule.forFeature()` 메소드를 통해 가져온 저장소를 모듈 밖에서 사용하려면, 생성된 프로바이더를 다시 내보내야 한다.

```tsx
import { TypeOrmModule } from "@nestjs/typeorm";

@Module({
  imports: [TypeOrmModule.forFeature([User])],
  exports: [TypeOrmModule],
})
export class UsersModule {}
```

이제 다른 모듈에서 `UsersModule`을 가져오면 모듈의 프로바이더에서 `@InjectRepository(User)` 데코레이터를 사용해 저장소를 주입할 수 있다.

```tsx
@Module({
  imports: [UsersModule],
  providers: [UsersService],
  controllers: [UsersController],
})
export class UserHttpModule {}
```
