## Spring 강의 3일차

### 회원 관리 예제 - 웹 MVC 개발
#### 홈화면 추가
* 우선순위에 따라 요청이 오면 컨트롤러에 관련 응답이 존재하는지 찾기 때문에, 
기존에 존재하는 static 파일이 아닌 template가 리턴.
* 즉, 정적 페이지보다 컨트롤러가 우선 순위가 높다.
#### 회원 등록
1. URL로 접근
2. URL로 접근 할 경우 `@GetMapping`에 맞는 메소드에 접근하여 Template에 진입
3. 해당 template에서 `등록`을 누를 경우 form의 method가 post이기 때문에 `@PostMapping`에 접근
4. `@PostMapping`에 맞는 메소드에 접근해 회원 등록

```JAVA
@GetMapping("/members/new")
public String createForm(){
    return "members/createMemberForm";
}

@PostMapping("/members/new")
public String create(MemberForm form) {
    Member member = new Member();
    member.setName(form.getName());

    memberService.join(member);

    return "redirect:/";
}
```


#### 회원 조회

```JAVA
@GetMapping("/members")
public String list(Model model) {
    List<Member> members = memberService.findMembers();
    model.addAttribute("members", members);
    return "members/memberList";
}
```

### 스프링 DB 접근 기술 - Jdbc
#### H2 데이터베이스 설치
> https://www.h2database.com/html/download.html
```
drop table if exists member CASCADE;
create table member
(
   id bigint generated by default as identity,
   name varchar(255),
   primary key (id)
);
```

#### 순수 JDBC
* bundle.gradle 파일에 jdbc, h2 데이터베이스 관련 라이브러리 추가
```JAVA
implementation 'org.springframework.boot:spring-boot-starter-jdbc'
runtimeOnly 'com.h2database:h2'
```

* 스프링 부트 데이터베이스 연결 설정 추가
> resources/application.properties
```JAVA
spring.datasource.url=jdbc:h2:tcp://localhost/~/test
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
```

* 구현된 클래스 이미지

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/82611ddf-96a5-4d70-98fc-746513492118)


* 개방-폐쇄 원칙(OCP, Open-closed Principle)
   * 확장에는 열려있고, 수정,변경에는 닫혀 있다.
   * Memory에서 DB로 기능을 완전히 변경하더라도, Application 전체를 변경할 필요가 없었다. 이는 OCP를 잘 지켰다고 할 수 있다.
* 스프링의 DI(Dependancy Injection)를 사용하면 **기존 코드를 전혀 손대지 않고, 설정만으로 구현 클래스를 변경** 할 수 있다.



#### 스프링 통합 테스트
* `@SpringBootTest`
   * 스프링 컨테이너와 테스트를 함께 실행한다.
* `@Transactional`
   * 데이터베이스는 Trasaction이라는 개념이 있음.
   * DB에 Insert query까지 해준 뒤, 데이터를 commit해줘야 반영되는데 스프링에서는 Transactional이라는 Annotation을 사용해 DB에 Query를 삽입한 뒤, 테스트 후 롤백시켜 준다.
   * 즉, 테스트 케이스에 이 애노테이션이 있으면, 테스트 시작 전에 트랜잭션을 시작하고, 테스트 완료 후에 항상 롤백한다. 이렇게 하면 DB에 데이터가 남지 않으므로 다음 테스트에 영향을 주지 않는다.

* 그럼, 순수 자바 코드로 사용되는 테스트는 필요없지 않은가?
   * 스프링 컨테이너와 DB까지 연동되는 경우, 속도가 매우 느리고, 테스트 설계가 잘못되었을 확률이 높다.
   * 즉, 순수한 단위 테스트가 훨씬 좋은 테스트이다.


#### 스프링 jdbc Template
* 순수 Jdbc와 동일한 환경설정을 하면 된다.

* 스프링 JdbcTemplate과 MyBatis 같은 라이브러리는 JDBC API에서 본 반복 코드를 대부분
제거해준다. 하지만 SQL은 직접 작성해야 한다.

#### JPA
* JPA는 Interface만 제공되며, 해당 구현체로 hibernate등 여러 구현체를 사용한다.
* JPA는 객체와 ORM(Object Relational Mapping)이라는 기술.
   * JPA는 기존의 반복 코드는 물론이고, 기본적인 SQL도 JPA가 직접 만들어서 실행해준다.
   * JPA를 사용하면, SQL과 데이터 중심의 설계에서 객체 중심의 설계로 패러다임을 전환을 할 수 있다.
   * JPA를 사용하면 개발 생산성을 크게 높일 수 있다

* Entity 애노테이션을 통해 Mapping
   * JPA를 쓰려면 Entity Manager를 주입 받아야 한다.
   * JPA를 쓰려면 데이터를 변경하기 위해 Transactional 애노테이션 안에서 실행되어야 한다. ( Service 계층 )

#### 스프링 데이터 JPA
> 스프링 부트와 JPA만 사용해도 개발 생산성이 정말 많이 증가하고, 개발해야할 코드도 확연히 줄어듭니다. 여기에 스프링 데이터 JPA를 사용하면, 기존의 한계를 넘어 마치 마법처럼, 리포지토리에 구현 클래스 없이 인터페이스 만으로 개발을 완료할 수 있습니다. 그리고 반복 개발해온 기본 CRUD 기능도 스프링 데이터 JPA가 모두 제공합니다. 스프링 부트와 JPA라는 기반 위에, 스프링 데이터 JPA라는 환상적인 프레임워크를 더하면 개발이 정말 즐거워집니다. 지금까지 조금이라도 단순하고 반복이라 생각했던 개발 코드들이 확연하게 줄어듭니다. 따라서 개발자는 핵심 비즈니스 로직을 개발하는데, 집중할 수 있습니다. 실무에서 관계형 데이터베이스를 사용한다면 스프링 데이터 JPA는 이제 선택이 아니라 필수 입니다.

* 스프링 데이터 JPA는 기본적인 CRUD 기능을 모두 제공.
* 페이징 기능 자동 제공
* 단순한 쿼리는 인터페이스의 메소드 이름(`findByxxx`)만으로 전부 해결할 수 있다.

![image](https://github.com/jub3907/Today-I-Learn/assets/58246682/d85bf01f-defd-4199-8241-fec6f688a1fe)

* 단, JPA를 모르고 사용한다면 발생할 수 있는 여러가지 이슈를 해결할 수 없으므로, JPA를 공부하는 것이 선결 조건.

> 참고: 실무에서는 JPA와 스프링 데이터 JPA를 기본으로 사용하고, 복잡한 동적 쿼리는 Querydsl이라는 라이브러리를 사용하면 된다. Querydsl을 사용하면 쿼리도 자바 코드로 안전하게 작성할 수 있고, 동적 쿼리도 편리하게 작성할 수 있다. 이 조합으로 해결하기 어려운 쿼리는 JPA가 제공하는 네이티브 쿼리를 사용하거나, 앞서 학습한 스프링 JdbcTemplate를 사용하면 된다.
### day5. Spring 강의 5일차

#### 직접 메소드 실행 시간을 측정하는 경우
* 회원가입, 회원 조회 등은 핵심 기능이 아니다.
* 시간을 측정하는 로직은 공통 관심 사항이다.
* 시간을 측정하는 로직과 핵심 비즈니스 로직이 섞여서 유지보수가 어렵다.

```JAVA
    public List<Member> findMembers() {
        long start = System.currentTimeMillis();

        try {
            return memberRepository.findAll();
        } finally {
            long finish = System.currentTimeMillis();
            long timeMs = finish - start;
            System.out.println("findMembers " + timeMs + "ms");
        }
    }
```
* 위처럼, 모든 메소드에 시간 측정 로직을 추가하는 것은 매우 비효율적인 작업이 된다.
* 이러한 문제를 해결하는 방법을 AOP라고 한다.

#### AOP
* AOP란, 공통 관심사항(cross-cutting concern) vs 핵심 관심사항(core concern) 분리하고, 내가 원하는 곳에 공통 관심 상황을 적용하는 것을 의미한다.

* AOP가 필요한 상황
  * 모든 메소드의 호출 시간을 측정하고 싶다면?

#### AOP 적용, Aspect Oriented Programming
* AOP : 공통 관심사항과 핵심 관심사항을 분리하는 것.
![image](https://user-images.githubusercontent.com/58246682/146176074-0b0ca699-39a5-4190-b5ea-48ead05543f8.png)
* 위 이미지처럼, 시간측정 로직을 구현한 뒤 원하는 메소드에 적용시키면 된다.

```JAVA
package hello.hellospring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class TimeTraceAOP {
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        System.out.println("Start: " + joinPoint.toString());
        try{
            return joinPoint.proceed();
        } finally {
            long finish = System.currentTimeMillis();
            long timeMs = finish - start;
        }
    }
}

```
* AOP 클래스 작성 후, 스프링 빈에 등록해줘야함.
* Component Scan or Config에 직접 등록, 어떤 방법이던 상관 없음.
* `@Around("execution(* hello.hellospring..*(..))")` : hellospring 패키지 하위에 AOP를 모두 적용.
* 변경사항이 필요 한 경우, AOP만 수정하면 된다는 장점이 존재하다.

#### AOP 동작 방식
* AOP 적용 전 의존 관계

![image](https://user-images.githubusercontent.com/58246682/146178338-f5ac441b-58d2-4d16-82e4-a4594c7fe31b.png)


* AOP 적용 후 의존관계

![image](https://user-images.githubusercontent.com/58246682/146178389-ac92118f-69dd-4df4-87c3-e689134789fb.png)

프록시라고 하는 **가짜 스프링 빈**의 의존관계를 형성.

* AOP 적용 전 전체 그림

![image](https://user-images.githubusercontent.com/58246682/146178721-dc43fd2b-bbae-4295-b5ef-e73fc6413731.png)

* AOP 적용 후 전체 그림

![image](https://user-images.githubusercontent.com/58246682/146178742-3a2f56ac-6075-4ce0-9d9c-1ac33de2bab8.png)
