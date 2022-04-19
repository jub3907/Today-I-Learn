### Jest test for nestjs graphql

- https://circleci.com/blog/testing-nestjs-graphql/ 의 글을 보고 재정리한 내용입니다.
- https://github.com/GodwinEkuma/invoice-app-without-test 프로젝트를 클론하고 보시면 좋습니다.
- TypeOrm(MySQL) + graphql + nestjs에 대한 테스트입니다.

### Unit testing the customer service

현재, customer service는 `create`, `findAll`, `findOne` 3개의 메소드를 갖고 있다.\
각 메소드에 대한 테스트를 추가할 예정이고, `cusomer.service.spec.ts`는 이미 customer service를 테스트 하기 위한 boilerplate를 가지고 있다.\
테스트를 추가하기 전에, 필요한 의존성을 주입해줌으로서 테스트 모듈을 구성할 필요가 있다.\
테스트 config는 `beforeEach` 블록에 더해진다.\
아래는 customer service에 대한 config이다.

```tsx
// src/customer/customer.service.spec.ts

import { Test, TestingModule } from "@nestjs/testing";
import { getRepositoryToken } from "@nestjs/typeorm";
import { Repository } from "typeorm";
import { CustomerModel } from "./customer.model";
import { CustomerService } from "./customer.service";
type MockType<T> = {
  [P in keyof T]?: jest.Mock<{}>;
};
describe("CustomerService", () => {
  let service: CustomerService;
  const customerRepositoryMock: MockType<Repository<CustomerModel>> = {
    save: jest.fn(),
    findOne: jest.fn(),
    find: jest.fn(),
  };
  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        CustomerService,
        {
          provide: getRepositoryToken(CustomerModel),
          useValue: customerRepositoryMock,
        },
      ],
    }).compile();
    service = module.get<CustomerService>(CustomerService);
  });
});
```

위 코드에서, 우리는 `CutomerService`, 그리고 `CustomerRepository`라는 두 가지 프로바이더를 더해주었다.\
이건 유닛 테스트이기 때문에, `CustomerRepository`를 위한 mock value를 사용한다.\
아래는 각 메소드에 대한 테스트 코드까지 구현한 코드이다.

```tsx
// src/customer/customer.service.spec.ts

import { Test, TestingModule } from "@nestjs/testing";
import { getRepositoryToken } from "@nestjs/typeorm";
import { Repository } from "typeorm";
import { CustomerModel } from "./customer.model";
import { CustomerService } from "./customer.service";
type MockType<T> = {
  [P in keyof T]?: jest.Mock<{}>;
};
describe("CustomerService", () => {
  let service: CustomerService;
  const customerRepositoryMock: MockType<Repository<CustomerModel>> = {
    save: jest.fn(),
    findOne: jest.fn(),
    find: jest.fn(),
  };
  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        CustomerService,
        {
          provide: getRepositoryToken(CustomerModel),
          useValue: customerRepositoryMock,
        },
      ],
    }).compile();
    service = module.get<CustomerService>(CustomerService);
  });
  it("should be defined", () => {
    expect(service).toBeDefined();
  });
  describe("create", () => {
    it("should create a new customer", async () => {
      const customerDTO = {
        name: "John Doe",
        email: "john.doe@email.com",
        phone: "3134045867",
        address: "123 Road, Springfied, MO",
      };
      customerRepositoryMock.save.mockReturnValue(customerDTO);
      const newCustomer = await service.create(customerDTO);
      expect(newCustomer).toMatchObject(customerDTO);
      expect(customerRepositoryMock.save).toHaveBeenCalledWith(customerDTO);
    });
  });
  describe("findAll", () => {
    it("should find all customers", async () => {
      const customers = [
        {
          id: "1234",
          name: "John Doe",
          email: "john.doe@email.com",
          phone: "3134045867",
          address: "123 Road, Springfied, MO",
        },

        {
          id: "5678",
          name: "John Ford",
          email: "john.ford@email.com",
          phone: "3134045867",
          address: "456 Road, Springfied, MO",
        },
      ];
      customerRepositoryMock.find.mockReturnValue(customers);
      const foundCustomers = await service.findAll();
      expect(foundCustomers).toContainEqual({
        id: "1234",
        name: "John Doe",
        email: "john.doe@email.com",
        phone: "3134045867",
        address: "123 Road, Springfied, MO",
      });
      expect(customerRepositoryMock.find).toHaveBeenCalled();
    });
  });
  describe("findOne", () => {
    it("should find a customer", async () => {
      const customer = {
        id: "1234",
        name: "John Doe",
        email: "john.doe@email.com",
        phone: "3134045867",
        address: "123 Road, Springfied, MO",
      };
      customerRepositoryMock.findOne.mockReturnValue(customer);
      const foundCustomer = await service.findOne(customer.id);
      expect(foundCustomer).toMatchObject(customer);
      expect(customerRepositoryMock.findOne).toHaveBeenCalledWith(customer.id);
    });
  });
});
```

### Testing the customer resolver

위에선 cutomer service에 대한 테스트 코드를 추가했지만, customer resolver에 대한 테스트 코드도 추가해주어야 한다.\
물론, 위와 마찬가지로 `customer.resolver.spec.ts`라는 resolver 테스트를 위한 boilerplate도 존재한다.\
또한, `CustomerService`와 `InvoiceService` 의존성에 대한 mock value도 제공하고 있다.

```tsx
// src/customer/customer.resolver.spec.ts

import { Test, TestingModule } from "@nestjs/testing";
import { InvoiceService } from "../invoice/invoice.service";
import { CustomerDTO } from "./customer.dto";
import { CustomerResolver } from "./customer.resolver";
import { CustomerService } from "./customer.service";
const invoice = {
  id: "1234",
  invoiceNo: "INV-01",
  description: "GSVBS Website Project",
  customer: {},
  paymentStatus: "Paid",
  currency: "NGN",
  taxRate: 5,
  taxAmount: 8000,
  subTotal: 160000,
  total: 168000,
  amountPaid: "0",
  outstandingBalance: 168000,
  issueDate: "2017-06-06",
  dueDate: "2017-06-20",
  note: "Thank you for your patronage.",
  createdAt: "2017-06-06 11:11:07",
  updatedAt: "2017-06-06 11:11:07",
};
describe("CustomerResolver", () => {
  let resolver: CustomerResolver;
  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        CustomerResolver,
        {
          provide: CustomerService,
          useFactory: () => ({
            create: jest.fn((customer: CustomerDTO) => ({
              id: "1234",
              ...customer,
            })),
            findAll: jest.fn(() => [
              {
                id: "1234",
                name: "John Doe",
                email: "john.doe@email.com",
                phone: "3134045867",
                address: "123 Road, Springfied, MO",
              },

              {
                id: "5678",
                name: "John Ford",
                email: "john.ford@email.com",
                phone: "3134045867",
                address: "456 Road, Springfied, MO",
              },
            ]),
            findOne: jest.fn((id: string) => ({
              id: id,
              name: "John Doe",
              email: "john.doe@email.com",
              phone: "3134045867",
              address: "123 Road, Springfied, MO",
            })),
          }),
        },
        {
          provide: InvoiceService,
          useFactory: () => ({
            findByCustomer: jest.fn((id: string) => invoice),
          }),
        },
      ],
    }).compile();
    resolver = module.get<CustomerResolver>(CustomerResolver);
  });
  it("should be defined", () => {
    expect(resolver).toBeDefined();
  });
  describe("customer", () => {
    it("should find and return a customer", async () => {
      const customer = await resolver.customer("1234");
      expect(customer).toEqual({
        id: "1234",
        name: "John Doe",
        email: "john.doe@email.com",
        phone: "3134045867",
        address: "123 Road, Springfied, MO",
      });
    });
  });
  describe("customers", () => {
    it("should find and return a list of customers", async () => {
      const customers = await resolver.customers();
      expect(customers).toContainEqual({
        id: "1234",
        name: "John Doe",
        email: "john.doe@email.com",
        phone: "3134045867",
        address: "123 Road, Springfied, MO",
      });
    });
  });
  describe("invoices", () => {
    it("should find and return a customer invoice", async () => {
      const customer = await resolver.invoices({ id: "1234" });
      expect(customer).toEqual(invoice);
    });
  });
  describe("createCustomer", () => {
    it("should find and return a customer invoice", async () => {
      const customer = await resolver.createCustomer(
        "John Doe",
        "john.doe@email.com",
        "3134045867",
        "123 Road, Springfied, MO"
      );
      expect(customer).toEqual({
        id: "1234",
        name: "John Doe",
        email: "john.doe@email.com",
        phone: "3134045867",
        address: "123 Road, Springfied, MO",
      });
    });
  });
});
```

### Automating end-to-end testing

e2e(End-to-end) 테스트는 각 컴포넌트(모델, 리졸버, 서비스)를 테스트하고,\
서로 잘 어우러져서 동작하도록 만드는 것이 목표이기 때문에 mock value를 사용하지는 않는다.

테스트용 DB를 위한 TypeOrm cinfig는 메인 DB를 위한 config와는 약간 다르다.\
아래 코드에선 `config`를 생성하고, 환경 변수에 기반해 TypeOrm config를 export 하기 위해 `config.database.ts`를 더해주었다.

```tsx
import dotenv from "dotenv";
dotenv.config();
const database = {
  development: {
    type: "postgres",
    host: "localhost",
    port: 5432,
    username: "godwinekuma",
    password: "",
    database: "invoiceapp",
    entities: ["dist/**/*.model.js"],
    synchronize: false,
    uuidExtension: "pgcrypto",
  },
  test: {
    type: "postgres",
    host: "localhost",
    port: 5432,
    username: process.env.POSTGRES_USER,
    password: "",
    database: process.env.POSTGRES_DB,
    entities: ["src/**/*.model.ts"],
    synchronize: true,
    dropSchema: true,
    migrationsRun: false,
    migrations: ["src/database/migrations/*.ts"],
    cli: {
      migrationsDir: "src/database/migrations",
    },
    keepConnectionAlive: true,
    uuidExtension: "pgcrypto",
  },
};
const DatabaseConfig = () => ({
  ...database[process.env.NODE_ENV],
});
export = DatabaseConfig;
```

위 코드에서 눈여겨 볼 점은, `dropSchema`가 `true`로 설정되어 있다는 점이다.\
이전의 테스트 실행으로부터 독립적으로 테스트 이후에 데이터를 삭제할 수 있게 해 준다.\
각 테스트 실행 후, connection에 등록된 모든 entity의 데이터를 지우는 것이 좋다.\
`connection.ts` 파일을 테스트 볼더에 넣고, 아래 코드를 넣어주자.

```tsx
// /test/connection.ts
import { createConnection, getConnection } from "typeorm";

const connection = {
  async close() {
    await getConnection().close();
  },

  async clear() {
    const connection = getConnection();
    const entities = connection.entityMetadatas;

    entities.forEach(async (entity) => {
      const repository = connection.getRepository(entity.name);
      await repository.query(`DELETE FROM ${entity.tableName}`);
    });
  },
};
export default connection;
```

`connection`은 `close`와 `clear`, 두 가지 메소드를 내보내고 있다.\
모든 테스트가 실행된 뒤 DB 연결을 종료하기 위해서 `close` 메소드가 실행되고, \
또 다른 테스트가 실행되기 이전에 모든 데이터를 삭제하기 위해 `clear` 메소드가 실행된다.

이제, 테스트를 추가해 주자.

```tsx
// /test/customer.e2e-spec.ts

import { Test, TestingModule } from "@nestjs/testing";
import { INestApplication } from "@nestjs/common";
import request = require("supertest");
import { AppModule } from "../src/app.module";
import connection from "./connection";
import { getConnection } from "typeorm";
import { CustomerModel } from "../src/customer/customer.model";

describe("CustomerResolver (e2e)", () => {
  let app: INestApplication;

  beforeEach(async () => {
    const moduleFixture: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    }).compile();

    app = moduleFixture.createNestApplication();
    await connection.clear();
    await app.init();
  });

  afterAll(async () => {
    await connection.close();
    await app.close();
  });

  const gql = "/graphql";

  describe("createCustomer", () => {
    it("should create a new customer", () => {
      return request(app.getHttpServer())
        .post(gql)
        .send({
          query:
            'mutation {createCustomer(name: "John Doe", email: "john.doe@example.com", phone: "145677312965", address: "123 Road, Springfied, MO") {address name phone email}}',
        })
        .expect(200)
        .expect((res) => {
          expect(res.body.data.createCustomer).toEqual({
            name: "John Doe",
            email: "john.doe@example.com",
            phone: "145677312965",
            address: "123 Road, Springfied, MO",
          });
        });
    });

    it("should get a single customer by id", () => {
      let customer;
      return request(app.getHttpServer())
        .post(gql)
        .send({
          query:
            'mutation {createCustomer(name: "John Doe", email: "john.doe@example.com", phone: "145677312965", address: "123 Road, Springfied, MO") {address name id phone email}}',
        })
        .expect(200)
        .expect((res) => {
          customer = res.body.data.createCustomer;
        })
        .then(() =>
          request(app.getHttpServer())
            .post(gql)
            .send({
              query: `{customer(id: "${customer.id}") {address name id phone email}}`,
            })
            .expect(200)
            .expect((res) => {
              expect(res.body.data.customer).toEqual({
                id: customer.id,
                address: customer.address,
                name: customer.name,
                phone: customer.phone,
                email: customer.email,
              });
            })
        );
    });

    it("should retrieve all customer data", async () => {
      const data = [
        {
          name: "John Doe",
          email: "john.doe@example.com",
          phone: "145677312965",
          address: "123 Road, Springfied, MO",
        },
        {
          name: "Jane Doe",
          email: "jane.doe@example.com",
          phone: "145677312900",
          address: "456 Road, Springfied, MO",
        },
      ];
      const connection = await getConnection();
      data.map(async (item) => {
        await connection
          .createQueryBuilder()
          .insert()
          .into(CustomerModel)
          .values(item)
          .execute();
      });

      request(app.getHttpServer())
        .post(gql)
        .send({
          query: `{customers() {address name phone email}}`,
        })
        .expect(200)
        .expect((res) => {
          expect(res.body.data.customers.length).toEqual(data.length);
          expect(res.body.data.customers[0]).toEqual(data[0]);
        });
    });
  });
});
```

### Adding CircleCI configuration to the project

생략
