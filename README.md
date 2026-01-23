# Back-end
## 공통 사항
- TDD(Test-Driven Development)
- 단위 테스트 작성(controller, service, repository 메소드 별로) : Junit 사용
  - service 메소드가 많아져 분리한다면 테스트코드도 그에 맞게 분리
- 지라 티켓을 생성하고 브랜치 생성 후 작업 시작
- 데이터 입출력에 대한 로그 잘 작성
- 평일 오전 09시 10분간 데일리 스크럼 실시
- 매주 화요일 09시 위클리 스크럼

<br>

## 개발규칙

### ⭐ Code Convention

---

<details>
<summary style = " font-size:1.3em;">Naming</summary>
<div markdown="1">

- 패키지 : 언더스코어(`_`)나 대문자를 섞지 않고 소문자를 사용하여 작성합니다.
- 클래스 : 클래스 이름은 명사나 명사절로 지으며, 대문자 카멜표기법(Upper camel case)을 사용합니다.
- 메서드 : 메서드 이름은 동사/전치사로 시작하며, 소문자 카멜표기법(Lower camel case)를 사용합니다. 의도가 전달되도록 최대한 간결하게 표현합니다.
- 변수 : 소문자 카멜표기법(Lower camel case)를 사용합니다.
- ENUM, 상수 : 상태를 가지지 않는 자료형이면서 `static final`로 선언되어 있는 필드일 때를 상수로 간주하며, 대문자와 언더스코어(Upper_snake_case)로 구성합니다.
- DB 테이블: 소문자와 언더스코어로(lower_snake_case) 구성합니다.
- 컬렉션(Collection): **복수형**을 사용하거나 **컬렉션을 명시합니다**. (Ex. userList, users, userMap)


</div>
</details>
<details>
<summary style = " font-size:1.3em;">Comment</summary>
<div markdown="1">

### 1. 한줄 주석은 // 를 사용한다.

```java
// 하이~
```

### 2. Bracket 사용 시 내부에 주석을 작성한다.

```java
/*
   하이~!
*/
```

</div>
</details>
<details>
<summary style = " font-size:1.3em;">Import</summary>
<div markdown="1">

### 1. 소스파일당 1개의 탑레벨 클래스를 담기

> 탑레벨 클래스(Top level class)는 소스 파일에 1개만 존재해야 한다.

### 2. static import에만 와일드 카드 허용

> 클래스를 import할때는 와일드카드(`*`) 없이 모든 클래스명을 다 쓴다. static import에서는 와일드카드를 허용한다.

### 3. 애너테이션 선언 후 새줄 사용

> 클래스, 인터페이스, 메서드, 생성자에 붙는 애너테이션은 선언 후 새줄을 사용한다. 이 위치에서도 파라미터가 없는 애너테이션 1개는 같은 줄에 선언할 수 있다.


### 4. 배열에서 대괄호는 타입 뒤에 선언

> 배열 선언에 오는 대괄호(`[]`)는 타입의 바로 뒤에 붙인다. 변수명 뒤에 붙이지 않는다.

### 5. `long`형 값의 마지막에 `L`붙이기

> long형의 숫자에는 마지막에 대문자 'L’을 붙인다. 소문자 'l’보다 숫자 '1’과의 차이가 커서 가독성이 높아진다.

</div>
</details>
<details>
<summary style = " font-size:1.3em;">URL</summary>
<div markdown="1">

### URL

URL은 RESTful API 설계 가이드에 따라 작성합니다.

- HTTP Method로 구분할 수 있는 get, put 등의 행위는 url에 표현하지 않습니다.
- 마지막에 `/` 를 포함하지 않습니다.
- `_` 대신 `-`를 사용합니다.
- 소문자를 사용합니다.
- 확장자는 포함하지 않습니다.


</div>
</details>

<br>

### ☀️ Commit Convention

---

<details>
<summary style = " font-size:1.3em;">Rules</summary>
<div markdown="1">

### 1. Git Flow

작업 시작 시 선행되어야 할 작업은 다음과 같습니다.


> 1. issue를 생성합니다.
> 2. 생성된 Jira의 티켓의 세부 내용(Story Point, Start Date, Due Date 등)을 관리합니다. 
> 3. Jira의 티켓 번호에 따라 feature branch를 생성합니다.
> 4. add → commit → push → pull request 를 진행합니다.
> 5. pull request를 dev branch로 merge 합니다.
> 6. 이전에 merge된 작업이 있을 경우 다른 branch에서 진행하던 작업에 merge된 작업을 pull 받아옵니다.


### 2. Etc

준수해야 할 규칙은 다음과 같습니다.

> 1. develop branch에서의 작업은 원칙적으로 금지합니다. 단, README 작성은 develop branch에서 수행합니다.
> 2. commit, push, merge, pull request 등 모든 작업은 오류 없이 정상적으로 실행되는 지 확인 후 수행합니다.

</div>
</details>

<details>
<summary style = " font-size:1.3em;">Branch</summary>
<div markdown="1">

### 1. Branch

branch는 작업 단위 & 기능 단위로 생성하며 이는 Jira Ticket를 기반으로 합니다.

### 2. Branch Naming Rule

branch를 생성하기 전 issue를 먼저 작성합니다. issue 작성 후 생성되는 티켓의 번호와 domain 명을 조합하여 branch의 이름을 결정합니다. `<Prefix>/<Ticket_Number>-<Domain>-<Description>` 의 양식을 준수합니다.

### 3. Prefix

- `main` : 개발이 완료된 산출물이 저장될 공간입니다.
- `dev`: feature branch에서 구현된 기능들이 merge될 default branch 입니다.
- `feat`: 기능을 개발하는 branch 입니다. 이슈 별 & 작업 별로 branch를 생성 후 기능을 개발하며 naming은 소문자를 사용합니다.

### 4. Domain

- `user`, `inventory`, `settlement`, `order`, `purchase` 


### 5. Etc

- `feat/7-order-create-order`, `feat/5-settlement-monthly`


</div>
</details>

<details>
<summary style = " font-size:1.3em;">Issue</summary>
<div markdown="1">

### 1. Issue

작업 시작 전 issue 생성이 선행되어야 합니다. issue 는 작업 단위 & 기능 단위로 생성하며 연동되어 생성되는 Jira의 ticket number 를 참조하여 branch 이름과 commit message를 작성합니다.

issue 제목에는 기능의 대표적인 설명을 적고 내용에는 세부적인 내용 및 작업 진행 상황을 작성합니다.

### 2. Issue Naming Rule

`[<Prefix>] <Description>` 의 양식을 준수하되, prefix는 commit message convention을 따릅니다.

### 3. Etc

<aside>
[feat] 약속 잡기 API 구현
<br/>[chore] spring data JPA 의존성 추가

</aside>

---

</div>
</details>

<details>
<summary style = " font-size:1.3em;">Commit</summary>
<div markdown="1">

### 1. Commit Message Convention

`[<Prefix>] #<Issue_Number> <Description>` 의 양식을 준수합니다.

- **feat** : 새로운 기능 구현 `[feat] #11 구글 로그인 API 기능 구현`
- **fix** : 코드 오류 수정 `[fix] #10 회원가입 비즈니스 로직 오류 수정`
- **del** : 쓸모없는 코드 삭제 `[del] #12 불필요한 import 제거`
- **docs** : README나 wiki 등의 문서 개정 `[docs] #14 리드미 수정`
- **refactor** : 내부 로직은 변경 하지 않고 기존의 코드를 개선하는 리팩터링 `[refactor] #15 코드 로직 개선`
- **chore** : 의존성 추가, yml 추가와 수정, 패키지 구조 변경, 파일 이동 `[chore] #21 yml 수정`, `[chore] #22 lombok 의존성 추가`
- **test**: 테스트 코드 작성, 수정 `[test] #20 로그인 API 테스트 코드 작성`
- **style** : 코드에 관련 없는 주석 달기, 줄바꿈

커밋에 대해 설명해야 하는 부분이 많다면 git commit -m "[<Prefix>] #<Issue_Number> <Description>" -m "<Description>"으로 작성합니다.

</div>
</details>

<details>
<summary style = " font-size:1.3em;">Pull Request</summary>
<div markdown="1">

### 1. Pull Request

develop & main branch로 merge할 때에는 pull request가 필요합니다. pull request의 내용에는 변경된 사항에 대한 설명을 명시합니다.

### 2. Pull Request Naming Rule

`[<Prefix>] #<Issue_Number> <Description>` 의 양식을 준수하되, prefix는 commit message convention을 따릅니다.

### 3. Etc

[feat] #3 약속 잡기 API 구현
<br/>
[chore] #5 spring data JPA 의존성 추가


</div>
</details>

<br/>


### ☀️ Test Code Convention

---

### 1. 테스트 네이밍 컨벤션
- 메서드명 패턴: MethodName_Given상황_Should기대결과
```
@Test
void createOrder_GivenValidProduct_ShouldReturnOrderId() {
    // given
    Product product = new Product("떡볶이", 5000);
    
    // when
    Long orderId = orderService.createOrder(product);
    
    // then
    assertThat(orderId).isNotNull();
}
```

### 2. 테스트 구조
1. Arrange (given): 테스트 데이터와 Mock 설정
2. Act (when): 테스트 대상 메소드 실행
3. Assert (then): 결과 검증
- 기존 사용하던 방식과 동일
- 모든 테스트 메소드는 해당 구조를 따르도록 작성

### 3. 패키지 구조
테스트 구조는 원본 클래스와 동일한 패키지 구조를 유지
```
src/main/java/com/example/order/OrderService.java
src/test/java/com/example/order/OrderServiceTest.java
```
- 서비스 코드가 분리되면 테스트 코드도 분리되어야 함


### 4. 테스트 코드 작성 대상
- Controller
  - @WebMvcTest 사용
  - Mock 대상: Service
  - 테스트 목적: HTTP 통신, 유효성 검증
- Service
  - @SpringBootTest 사용
  - Mock 대상: 없음. 실제 DB 사용
  - 테스트 목적: 비즈니스 로직, 트랜잭션 검증
- Repository
  - @DataJpaTest 사용
  - Mock 대상: 없음
  - 테스트 목적: 쿼리 메소드, 데이터 접근 검증
 
<br/>
<br/>
<br/>
