## Spring 강의 2일차 [링크](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%EC%9E%85%EB%AC%B8-%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8/lecture/49576?tab=curriculum&speed=1.5)


### 스프링 빈과 의존관계


### 스프링 빈을 등록하는 방법
#### 자동 의존관계 등록
* `@Component` 애노테이션이 있으면 스프링 빈으로 자동 등록된다.
* `@Controller` 컨트롤러가 스프링 빈으로 자동 등록된 이유도 컴포넌트 스캔 때문이다
* 멤버 컨트롤러가 멤버 서비스를 통해 회원을 등록하고 회원을 조회하는 의존관계를 형성해야함.

  * Controller 애노테이션을 통해 자동으로 컨트롤러에서 스프링빈으로 자동 등록.
  * Autowired 애노테이션을 통해 서비스를 스프링이 스프링 컨테이너에 있는 서비스를 가져다가 연결. 이렇게 객체 의존관계를 외부에서 넣어주는걸 의존성 주입.
  * Service 애노테이션을 통해 서비스를 등록.
  * Repository 애노테이션을 통해 repo를 등록.
  ![image](https://user-images.githubusercontent.com/58246682/145392056-3321e086-5476-4393-ae0a-f0f0de106997.png)

* 스프링 빈으로 등록하게 되면 이점이 몇가지 존재.
> 참고: 스프링은 스프링 컨테이너에 스프링 빈을 등록할 때, 기본으로 싱글톤으로 등록한다(유일하게 하나만 등록해서 공유한다) 
> 따라서 같은 스프링 빈이면 모두 같은 인스턴스다. 설정으로 싱글톤이 아니게 설정할 수 있지만, 특별한 경우를 제외하면 대부분 싱글톤을 사용한다.

#### 자바 코드로 직접 스프링 빈 등록 - 컴포넌트 스캔

* `@Bean`과 `@Configuration`을 사용해 Repo와 Service를 스프링 빈에 등록
* Controller는 스프링이 관리하기 때문에 애노테이션 사용

#### 참고
* DI에는 필드 주입, setter 주입, 생성자 주입 세가지 방법이 존재. (강의에서는 생성자 주입 )
  * 필드 주입 : 내부 코드 변경이 불가능해 비추천
  * setter 주입 : 서비스가 public으로 노출된다는 단점이 존재.
  * 생성자 주입을 권장, 의존 관계가 실행중에 동적으로 변경되는 경우가 없으므로 생성자 주입은 App이 
조립되는 시점에 Service만 한번 조회하면 되기 때문에 권장된다.

* 실무에서는 주로 정형화된 컨트롤러, 서비스, 리포지토리 같은 코드는 컴포넌트 스캔을 사용한다. 
그리고 정형화 되지 않거나, 상황에 따라 구현 클래스를 변경해야 하면 설정을 통해 스프링 빈으로
등록한다.

*  주의: `@Autowired` 를 통한 DI는 `helloConroller` , `memberService` 등과 같이 스프링이 관리하는
객체에서만 동작한다. 스프링 빈으로 등록하지 않고 내가 직접 생성한 객체에서는 동작하지 않는다.

### 회원 관리 예제 - 웹 MVC 개발
#### 홈화면 추가
* 우선순위에 따라 요청이 오면 컨트롤러에 관련 응답이 존재하는지 찾기 때문에, 
기존에 존재하는 static 파일이 아닌 template가 리턴.
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
