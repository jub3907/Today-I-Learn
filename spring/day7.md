### Spring 강의 7일차

#### 객체 지향
* 객체 지향의 특징 : `추상화` `캡슐화` 상속` `다형성`
* 객체 지향 프로그래밍은 컴퓨터 프로그램을 명령어의 목록으로 보는 시각에서 벗어나, 여러개의 독립된 단위, 즉 **객체**들의 모임으로 파악하고자 하는 것.
* 각각의 객체는 매세지를 주고받고, 데이터를 처리할 수 있다.
* 객체 지향 프로그래밍은 프로그램을 유연하고 변경이 용이하게 만들기 때문에, 대규모 소프트웨어 개발에 많이 사용된다.

### 다형성
* 실세계와 객체 지향을 1:1로 매칭은 잘 안됨.
* 역할과 구현으로 구분하면 세상이 단순해지고, 유연해지며 변경도 편리해진다.
* 장점 
  * 클라이언트는 대상의 역할(인터페이스)만 알면 된다.
  * 클라이언트는 구현 대상의 내부 구조를 몰라도 된다.
  * 클라이언트는 구현 대상의 내부 구조가 변경되어도 영향을 받지 않는다.
  * 클라이언트는 구현 대상 자체를 변경해도 영향을 받지 않는다.

### 역할과 구현을 분리
* 자바에선 `역할 : 인터페이스`, `구현 : 인터페이스를 구현한 클래스, 구현 객체`.
* 객체를 설계할 때 역할과 구현을 명확히 분리해야 한다. 

### 좋은 객체 지향 설계의 5가지 법칙, SOLID
* 클린 코드로 유명한 로버트 마틴이 좋은 객체 지향 설계의 5가지 원칙을 정리.
* SRP: 단일 책임 원칙(single responsibility principle)
* OCP: 개방-폐쇄 원칙 (Open/closed principle)
* LSP: 리스코프 치환 원칙 (Liskov substitution principle)
* ISP: 인터페이스 분리 원칙 (Interface segregation principle)
* DIP: 의존관계 역전 원칙 (Dependency inversion principle)

### SRP 단일 책임 원칙
* 한 클래스는 하나의 책임만 가져야 한다.
* 하나의 책임이라는 것은 모호하다.
* 중요한 기준은 변경. 변경이 있을 때, 파급 효과가 적으면 단일 책임 원칙을 잘 따른 것.

### COP 개방-폐쇄 원칙
* 소프트웨어 요소는 **확장에는 열려**있으나, **변경에는 닫혀**있어야 한다.
* 다형성을 활용
* 인터페이스를 구현한 새로운 클래스를 하나 만들어서(**기존 코드 변경 x**) 새로운 기능을 구현 **(확장)**

### COP의 문제점
* MemberService 클라이언트가 구현 클래스를 직접 선택
  * MemberRepository m = new MemoryMemberRepository(); //기존 코드
  * MemberRepository m = new JdbcMemberRepository(); //변경 코드
* **구현 객체를 변경하려면 클라이언트 코드를 변경해야 한다.**
* **분명 다형성을 사용했지만, OCP 원칙을 지킬 수 없다.**
* 이 문제를 해결하기 위해 객체를 생성하고, 연관관계를 맺어주는 별도의 조립, 설정자가 필요하다.
* 추후 예제를 통해 학습

### LSP 리스코프 치환 원칙
* 프로그램의 객체를 프로그램의 정확성을 깨트리지 않으면서 하위 타입의 인스턴스로 바꿀 수 있어야 한다.
* 다형성에서 하위 클래스는 인터페이스 규약을 다 지켜야 한다는 것, 다형성을 지원하기 위한 원칙, 인터페이스를 구현한 구현체는 믿고 사용하려면, 이 원칙이 필요하다.
* 단순히 컴파일에 성공하는 것 과는 다른 이야기.
* ex)  자동차 인터페이스의 엑셀은 앞으로 가라는 기능, 뒤로 가게 구현하면 LSP 위반, 느리더라도 앞으로 가야함

### ISP 인터페이스 분리 원칙
* 특정 틀라이언트를 위한 인터페이스 여러 개가 범용 인터페이스 하나보다 낫다.
* 자동차 인터페이스 -> 운전 인터페이스, 정비 인터페이스로 분리
* 사용자 클라이언트 -> 운전자 클라이언트, 정비사 클라이언트로 분리
* 분리하면 정비 인터페이스 자체가 변해도 운전자 클라이언트에 영향을 주지 않는다.
* 인터페이스가 명확해지고, 대체 가능성이 높아진다.

### DIP 의존관계 역전 원칙
* 프로그래머는 **추상화에 의존해야지, 구체화에 의존하면 안된다.** 의존성 주입은 이 원칙을 따르는 방법 중 하나.
* 즉, **구현 클래스에 의존** 하지 말고, **인터페이스에** 의존하라는 뜻.
* 위 COP에서의 예제 `MemberService`는 인터페이스에 의존하지만, 구현 클래스도 동시에 의존한다. ( `MemberRepository m = new MemoryMemberRepository();` )
* 이는 DIP 위반.

### 정리
* 객체 지향의 핵심은 다형성이다.
* 다형성 만으로는 쉽게 부품을 갈아 끼우듯이 개발할 수 없다.
* 다형성 만으로는 구현 객체를 변경할 때 클아이언트 코드도 함께 변경된다.
* 다형성 만으로는 OCP, DIP를 지킬 수 없다.


