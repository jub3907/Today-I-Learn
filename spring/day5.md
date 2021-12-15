### day5. Spring 강의 5일차

#### 직접 메소드 실행 시간을 측정하는 경우
* 회원가입, 회원 조회 등은 핵심 기능이 아니다.
* 시간을 측정하는 로직은 공통 관심 사항이다.
* 시간을 측정하는 로직과 핵심 비즈니스 로직이 섞여서 유지보수가 어렵다.


#### AOP
* AOP가 필요한 상황
  * 모든 메소드의 호출 시간을 측정하고 싶다면?
  * 공통 관심사항(cross-cutting concern) vs 핵심 관심사항(core concern) 분리

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

* 
