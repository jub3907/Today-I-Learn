
### 프로토타입 스코프
싱글톤 스코프의 빈을 조회하면 **스프링 컨테이너는 항상 같은 인스턴스의 스프링 빈을 반환**한다. 반면에 프로토타입 스코프를 스프링 컨테이너에 조회하면 스프링 컨테이너는 항상 새로운 인스턴스를 생성해서 반환한다.

* 싱글톤 요청

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/c1c81c74-1c2f-4008-bcc2-a01d32d2c36b)
1. 싱글톤 스코프의 빈을 스프링 컨테이너에 요청한다.
2. 스프링 컨테이너는 본인이 관리하는 스프링 빈을 반환한다.
3. 이후에 스프링 컨테이너에 같은 요청이 와도 같은 객체 인스턴스의 스프링 빈을 반환한다.

* 프로토타입 빈 요청

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/96ab0462-e04d-4084-a60b-69c38c251e5b)
1. 프로토타입 스코프의 빈을 스프링 컨테이너에 요청한다.
2. 스프링 컨테이너는 이 시점에 프로토타입 빈을 생성하고, 필요한 의존관계를 주입한다.


* 프로토타입 빈 요청 2

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/22f69063-681c-46e8-b6de-a57937d457e7)

3. 스프링 컨테이너는 생성한 프로토타입 빈을 클라이언트에 반환한다.
4. 이후에 스프링 컨테이너에 같은 요청이 오면 항상 새로운 프로토타입 빈을 생성해서 반환한다.

#### 정리
여기서 핵심은 **스프링 컨테이너는 프로토타입 빈을 생성하고, 의존관계 주입, 초기화까지만 처리한다는 것**이다. 클라이언트에 빈을 반환하고, 이후 스프링 컨테이너는 생성된 프로토타입 빈을 관리하지 않는다. 프로토타입 빈을 관리할 책임은 프로토타입 빈을 받은 클라이언트에 있다. 그래서 **`@PreDestroy` 같은 종료 메서드가 호출**되지 않는다.

#### 싱글톤 스코프 빈 테스트
```JAVA
public class SingletonTest {

    @Test
    void singletonBeanFind() {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(SingletonBean.class);

        SingletonBean singletonBean1 = ac.getBean(SingletonBean.class);
        SingletonBean singletonBean2 = ac.getBean(SingletonBean.class);
        System.out.println("singletonBean1 = " + singletonBean1);
        System.out.println("singletonBean2 = " + singletonBean2);

        Assertions.assertThat(singletonBean1).isSameAs(singletonBean2);

        ac.close();
    }

    @Scope("singleton")
    static class SingletonBean {
        @PostConstruct
        public void init() {
            System.out.println("SingletonBean.init");
        }

        @PreDestroy
        public void destroy() {
            System.out.println("SingletonBean.destroy");
        }

    }
}

```
* 실행 결과
```
SingletonBean.init
singletonBean1 = hello.core.scope.SingletonTest$SingletonBean@291a7e3c
singletonBean2 = hello.core.scope.SingletonTest$SingletonBean@291a7e3c
AnnotationConfigApplicationContext - Closing org.springframework.context.annotation.
SingletonBean.destroy
```
빈 초기화 메소드가 실행된 뒤 같은 인스턴스의 빈을 조회하고, 종료 메소드까지 정상적으로 호출된 것을 확인할 수 있다.

#### 프로토타입 스코프 빈 테스트

```JAVA
public class PrototypeTest {

    @Test
    void prototypeBeanFind() {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(PrototypeBean.class);

        System.out.println("find prototypeBean1");
        PrototypeBean prototypeBean1 = ac.getBean(PrototypeBean.class);

        System.out.println("find prototypeBean2");
        PrototypeBean prototypeBean2 = ac.getBean(PrototypeBean.class);

        System.out.println("prototypeBean1 = " + prototypeBean1);
        System.out.println("prototypeBean2 = " + prototypeBean2);

        Assertions.assertThat(prototypeBean1).isNotSameAs(prototypeBean2);
        ac.close();

    }


    @Scope("prototype")
    static class PrototypeBean {
        @PostConstruct
        public void init() {
            System.out.println("SingletonBean.init");
        }

        @PreDestroy
        public void destroy() {
            System.out.println("SingletonBean.destroy");
        }

    }
}
```
* 실행 결과
```
find prototypeBean1
SingletonBean.init
find prototypeBean2
SingletonBean.init
prototypeBean1 = hello.core.scope.PrototypeTest$PrototypeBean@291a7e3c
prototypeBean2 = hello.core.scope.PrototypeTest$PrototypeBean@ca30bc1
AnnotationConfigApplicationContext - Closing org.springframework.context.annotation.
```

* 싱글톤 빈은 스프링 컨테이너 생성 시점에 초기화 메서드가 실행 되지만, 프로토타입 스코프의 빈은 스프링 컨테이너에서 빈을 조회할 때 생성되고, 초기화 메서드도 실행된다.

* 프로토타입 빈을 2번 조회했으므로 완전히 다른 스프링 빈이 생성되고, 초기화도 2번 실행된 것을 확인할 수 있다.

* 싱글톤 빈은 스프링 컨테이너가 관리하기 때문에 스프링 컨테이너가 종료될 때 빈의 종료 메서드가 실행되지만, 프로토타입 빈은 스프링 컨테이너가 생성과 의존관계 주입 그리고 초기화 까지만 관여하고, 더는 관리하지 않는다. 따라서 프로토타입 빈은 스프링 컨테이너가 종료될 때 @PreDestroy 같은 종료 메서드가 전혀실행되지 않는다.

#### 프로토타입 빈의 특징 정리
* 스프링 컨테이너에 요청할 때 마다 새로 생성된다.
* 스프링 컨테이너는 프로토타입 빈의 생성과 의존관계 주입 그리고 초기화까지만 관여한다.
* 종료 메서드가 호출되지 않는다.
* 그래서 프로토타입 빈은 프로토타입 빈을 조회한 클라이언트가 관리해야 한다. 종료 메서드에 대한 호출도 클라이언트가 직접 해야한다.

### 프로토타입 스코프 - 싱글톤 빈과 함께 사용시 문제점
스프링 컨테이너에 프로토타입 스코프의 빈을 요청하면 항상 새로운 객체 인스턴스를 생성해서 반환한다. 하지만 **싱글톤 빈과 함께 사용할 때는 의도한 대로 잘 동작하지 않으므로** 주의해야 한다. 그림과 코드로 설명하겠다.

먼저 스프링 컨테이너에 프로토타입 빈을 직접 요청하는 예제를 보자.


#### 프로토타입 빈 직접 요청
* **스프링 컨테이너에 프로토타입 빈 직접 요청1**

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/ff72fc14-1677-4b41-b96c-799607ab57fc)


1. 클라이언트A는 스프링 컨테이너에 프로토타입 빈을 요청한다.
2. 스프링 컨테이너는 프로토타입 빈을 새로 생성해서 반환(x01)한다. 해당 빈의 count 필드 값은 0이다.
3. 클라이언트는 조회한 프로토타입 빈에 addCount() 를 호출하면서 count 필드를 +1 한다. 결과적으로 프로토타입 빈(x01)의 count는 1이 된다.

* **스프링 컨테이너에 프로토타입 빈 직접 요청2**

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/ce9c4467-bf6b-477b-900c-bbbb745faf7b)

1. 클라이언트B는 스프링 컨테이너에 프로토타입 빈을 요청한다.
2. 스프링 컨테이너는 프로토타입 빈을 새로 생성해서 반환(x02)한다. 해당 빈의 count 필드 값은 0이다.
3. 클라이언트는 조회한 프로토타입 빈에 addCount() 를 호출하면서 count 필드를 +1 한다.
결과적으로 프로토타입 빈(x02)의 count는 1이 된다.

#### 싱글톤 빈에서 프로토타입 빈 사용
이번에는 `clientBean` 이라는 싱글톤 빈이 의존관계 주입을 통해서 프로토타입 빈을 주입받아서 사용하는 예를 보자.

* **싱글톤에서 프로토타입 빈 사용1**

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/37ac2d03-33ac-47aa-9cbb-dbaa0021ca91)

* `clientBean` 은 싱글톤이므로, 보통 스프링 컨테이너 생성 시점에 함께 생성되고, 의존관계 주입도 발생한다.
* 1. `clientBean` 은 의존관계 자동 주입을 사용한다. 주입 시점에 스프링 컨테이너에 프로토타입 빈을 요청한다.
* 2. 스프링 컨테이너는 프로토타입 빈을 생성해서 `clientBean` 에 반환한다. 프로토타입 빈의 `count` 필드값은 0이다.
* 이제 `clientBean` 은 프로토타입 빈을 내부 필드에 보관한다. (정확히는 참조값을 보관한다.)

* **싱글톤에서 프로토타입 빈 사용2**

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/a093afb5-117f-4ae0-8832-d1caa68259bb)

* 클라이언트 A는 `clientBean` 을 스프링 컨테이너에 요청해서 받는다.싱글톤이므로 항상 같은 `clientBean` 이 반환된다.
* 3. 클라이언트 A는 `clientBean.logic()` 을 호출한다.
* 4. `clientBean` 은 `prototypeBean의` `addCount()` 를 호출해서 프로토타입 빈의 `count`를 증가한다. `count`값이 1이 된다.


* **싱글톤에서 프로토타입 빈 사용3**

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/39f9fa96-064e-4aa1-91a6-553c6fe20c19)

* 클라이언트 B는 `clientBean` 을 스프링 컨테이너에 요청해서 받는다.싱글톤이므로 항상 같은 `clientBean` 이 반환된다.
* **여기서 중요한 점이 있는데, `clientBean`이 내부에 가지고 있는 프로토타입 빈은 이미 과거에 주입이 끝난 빈이다. 주입 시점에 스프링 컨테이너에 요청해서 프로토타입 빈이 새로 생성이 된 것이지, 사용 할 때마다 새로 생성되는 것이 아니다!**
* 5. 클라이언트 B는 `clientBean.logic()` 을 호출한다.
* 6. `clientBean` 은 `prototypeBean의` `addCount()` 를 호출해서 프로토타입 빈의 `count`를 증가한다. 원래 `count` 값이 1이었으므로 2가 된다.

* 테스트 코드
```JAVA
public class SingletonWithPrototypeTest1 {
        @Test
        void prototypeFind() {
            AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(PrototypeBean.class);

            PrototypeBean prototypeBean1 = ac.getBean(PrototypeBean.class);
            prototypeBean1.addCount();
            assertThat(prototypeBean1.getCount()).isEqualTo(1);

            PrototypeBean prototypeBean2 = ac.getBean(PrototypeBean.class);
            prototypeBean2.addCount();
            assertThat(prototypeBean2.getCount()).isEqualTo(1);
        }

        @Test
        void singletonClientUsePrototype() {
            AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(ClientBean.class, PrototypeBean.class);

            ClientBean clientBean1 = ac.getBean(ClientBean.class);
            int count1 = clientBean1.logic();
            assertThat(count1).isEqualTo(1);

            ClientBean clientBean2 = ac.getBean(ClientBean.class);
            int count2 = clientBean2.logic();
            assertThat(count2).isEqualTo(2);

        }

        @Scope
        static class ClientBean {
            private final PrototypeBean prototypeBean;

            @Autowired
            public ClientBean(PrototypeBean prototypeBean) {
                this.prototypeBean = prototypeBean;
            }

            public int logic() {
                prototypeBean.addCount();
                int count = prototypeBean.getCount();
                return count;
            }
        }

        @Scope("prototype")
        static class PrototypeBean {
            private int count = 0;

            public void addCount() {
                count++;
            }

            public int getCount() {
                return count;
            }

            @PostConstruct
            public void init() {
                System.out.println("PrototypeBean.init " + this);
            }

            @PreDestroy
            public void destroy() {
                System.out.println("PrototypeBean.destroy");
            }
        }
}
```

스프링은 일반적으로 싱글톤 빈을 사용하므로, 싱글톤 빈이 프로토타입 빈을 사용하게 된다. 그런데 싱글톤 빈은 생성 시점에만 의존관계 주입을 받기 때문에, 프로토타입 빈이 새로 생성되기는 하지만, 싱글톤 빈과 함께 계속 유지되는 것이 문제다.

아마 원하는 것이 이런 것은 아닐 것이다. **프로토타입 빈을 주입 시점에만 새로 생성하는게 아니라, 사용할때 마다 새로 생성해서 사용하는 것을 원할 것이다.**

> 참고: 여러 빈에서 같은 프로토타입 빈을 주입 받으면, 주입 받는 시점에 각각 새로운 프로토타입 빈이 생성된다. 예를 들어서 clientA, clientB가 각각 의존관계 주입을 받으면 각각 다른 인스턴스의 프로토타입 빈을 주입 받는다.

> `clientA prototypeBean@x01`
> `clientB prototypeBean@x02`

> 물론 사용할 때 마다 새로 생성되는 것은 아니다.


### 프로토타입 스코프 - 싱글톤 빈과 함께 사용시 Provider로 문제 해결

싱글톤 빈과 프로토타입 빈을 함께 사용할 때, 어떻게 하면 사용할 때 마다 항상 새로운 프로토타입 빈을 생성할 수 있을까?

#### 스프링 컨테이너에 요청

가장 간단한 방법은 싱글톤 빈이 프로토타입을 사용할 때 마다 스프링 컨테이너에 새로 요청하는 것이다.

```JAVA
static class ClientBean {
    @Autowired
    private ApplicationContext ac;

    public int logic() {
        PrototypeBean prototypeBean = ac.getBean(PrototypeBean.class);
        prototypeBean.addCount();
        int count = prototypeBean.getCount();
        return count;
    }
}
```

* 실행해보면 `ac.getBean()` 을 통해서 항상 새로운 프로토타입 빈이 생성되는 것을 확인할 수 있다.
* 의존관계를 외부에서 주입(DI) 받는게 아니라 이렇게 직접 필요한 의존관계를 찾는 것을 **Dependency Lookup**, 의존관계 조회(탐색) 이라한다.
* 그런데 이렇게 스프링의 애플리케이션 컨텍스트 전체를 주입받게 되면, 스프링 컨테이너에 종속적인 코드가 되고, 단위 테스트도 어려워진다.
* 지금 필요한 기능은 지정한 프로토타입 빈을 컨테이너에서 대신 찾아주는 딱! **DL** 정도의 기능만 제공하는 무언가가 있으면 된다.

#### ObjectFactory, ObjectProvider

지정한 빈을 컨테이너에서 대신 찾아주는 DL 서비스를 제공하는 것이 바로 ObjectProvider 이다. 참고로 과거에는 ObjectFactory 가 있었는데, 여기에 편의 기능을 추가해서 ObjectProvider 가 만들어졌다.

```JAVA
        static class ClientBean {
            @Autowired
            private ObjectProvider<PrototypeBean> prototypebeanProvider;

            public int logic() {
                PrototypeBean prototypeBean = prototypebeanProvider.getObject();
                prototypeBean.addCount();
                int count = prototypeBean.getCount();
                return count;
            }
        }
```
* 실행해보면 `prototypeBeanProvider.getObject()` 을 통해서 항상 새로운 프로토타입 빈이 생성되는 것을 확인할 수 있다.
* `ObjectProvider` 의 `getObject()` 를 호출하면 내부에서는 스프링 컨테이너를 통해 해당 빈을 찾아서 반환한다. (DL)
* 스프링이 제공하는 기능을 사용하지만, 기능이 단순하므로 단위테스트를 만들거나 mock 코드를 만들기는 훨씬 쉬워진다.
* `ObjectProvider` 는 지금 딱 필요한 DL 정도의 기능만 제공한다.

#### ObjectFactory, ObjectProvider의 특징
* `ObjectFactory`: 기능이 단순하고, 별도의 라이브러리가 필요하지 않다. 또한, 스프링에 의존적.
* `ObjectProvider`: ObjectFactory 상속, 옵션, 스트림 처리등 편의 기능이 많고, 별도의 라이브러리 필요없음, 스프링에 의존

### JSR-330 Provider
마지막 방법은 `javax.inject.Provider` 라는 JSR-330 자바 표준을 사용하는 방법이다.
스프링 부트 3.0은 `jakarta.inject.Provider` 사용한다.

이 방법을 사용하려면 다음 라이브러리를 gradle에 추가해야 한다.

#### 스프링부트 3.0 미만
`javax.inject:javax.inject:1` 라이브러리를 gradle에 추가해야 한다.
#### 스프링부트 3.0 이상
`jakarta.inject:jakarta.inject-api:2.0.1` 라이브러리를 gradle에 추가해야 한다.


#### javax.inject.Provider 참고용 코드 - 스프링부트 3.0 미만
```
package javax.inject;
public interface Provider<T> {
 T get();
}
```
* 스프링 부트 3.0은 jakarta.inject.Provider 사용

```JAVA
@Autowired
private Provider<PrototypeBean> provider;

public int logic() {
    PrototypeBean prototypeBean = provider.get();
    prototypeBean.addCount();
    int count = prototypeBean.getCount();
    return count;
}
```

* 실행해보면 `provider.get()` 을 통해서 항상 새로운 프로토타입 빈이 생성되는 것을 확인할 수 있다.
* `provider` 의 `get()` 을 호출하면 내부에서는 스프링 컨테이너를 통해 해당 빈을 찾아서 반환한다. (DL)
* 자바 표준이고, 기능이 단순하므로 단위테스트를 만들거나 mock 코드를 만들기는 훨씬 쉬워진다.
* `Provider` 는 지금 딱 필요한 DL 정도의 기능만 제공한다.

#### Provider의 특징
* `get()` 메서드 하나로 기능이 매우 단순하다.
* 별도의 라이브러리가 필요하다.
* 자바 표준이므로 스프링이 아닌 다른 컨테이너에서도 사용할 수 있다.

#### 정리
* 그러면 프로토타입 빈을 언제 사용할까? 매번 사용할 때 마다 의존관계 주입이 완료된 새로운 객체가 필요하면 사용하면 된다. 그런데 실무에서 웹 애플리케이션을 개발해보면, 싱글톤 빈으로 대부분의 문제를 해결할 수 있기 때문에 프로토타입 빈을 직접적으로 사용하는 일은 매우 드물다.
* `ObjectProvider`, `JSR330 Provider` 등은 프로토타입 뿐만 아니라 DL이 필요한 경우는 언제든지 사용
할 수 있다.
    * Provider는 Circular Dependency(순환 참조), 지연 등이 필요할 때 사용하면 좋다.
    * 하지만, 거의 사용하지 않는다고 봐도 된다.

> 참고: 실무에서 자바 표준인 JSR-330 Provider를 사용할 것인지, 아니면 스프링이 제공하는 ObjectProvider를 사용할 것인지 고민이 될 것이다. ObjectProvider는 DL을 위한 편의 기능을 많이 제공해주고 스프링 외에 별도의 의존관계 추가가 필요 없기 때문에 편리하다. 만약(정말 그럴일은 거의 없겠지만) 코드를 스프링이 아닌 다른 컨테이너에서도 사용할 수 있어야 한다면 JSR-330 Provider를 사용해야한다.


> 스프링을 사용하다 보면 이 기능 뿐만 아니라 다른 기능들도 자바 표준과 스프링이 제공하는 기능이 겹칠때가 많이 있다. 대부분 스프링이 더 다양하고 편리한 기능을 제공해주기 때문에, 특별히 다른 컨테이너를 사용할 일이 없다면, 스프링이 제공하는 기능을 사용하면 된다.