# 📦 CHAIN-G

<img width="300" alt="Gemini_Generated_Image_8j8ddp8j8ddp8j8d-Photoroom" src="https://github.com/user-attachments/assets/6f623ede-7a0d-4fcd-a74f-3bec56de2c63" />

> CONNECT GOOD, VALUE CHAIN
>
**CHAIN-G**는 떡볶이 밀키트의 생산부터 유통, 가맹점 판매 및 정산까지의 전 과정을 효율적으로 관리하는 **공급망 관리(SCM) 시스템**입니다.  
본사, 가맹점, 공장 세 주체 간의 유기적인 발주, 재고 관리, 물류 배차 및 정산 프로세스를 자동화합니다.

<br>

## 👥 팀원 소개 (Team Flow-er)

| <img src="https://github.com/chaewoo-kim.png" width="120"> | <img src="https://github.com/rlatjddms.png" width="120"> | <img src="https://github.com/kyk5095.png" width="120"> | <img src="https://github.com/Yoocy0.png" width="120"> | <img src="https://github.com/cho-yunho01.png" width="120"> |
| :---: | :---: | :---: | :---: | :---: |
| **김채우** | **김성은** | **김윤경** | **유찬연** | **조윤호** |
| Order / Sales / <br>Return | User / BusinessUnit / <br>Notice / Notification | Settlement | In&Outbound / <br>Transport | Inventory / Product |
| [@chaewoo-kim](https://github.com/chaewoo-kim) | [@rlatjddms](https://github.com/rlatjddms) | [@kyk5095](https://github.com/kyk5095) | [@Yoocy0](https://github.com/Yoocy0) | [@cho-yunho01](https://github.com/cho-yunho01) |

<br>

<br>

## 🏢 프로젝트 아키텍처

본 프로젝트는 유지보수성과 확장성을 위해 **멀티 모듈 아키텍처**를 채택하고 있으며, 도메인 중심 설계(Domain-Driven Design)를 지향합니다.

### 모듈 구조
- **`module-core`**: 시스템 전반에서 공통으로 사용되는 유틸리티, 예외 처리, 공통 응답 규격을 포함합니다.
- **`module-domain`**: 순수 비즈니스 로직과 데이터 모델(Entity)을 담당하는 핵심 모듈입니다.
  - `domain-orders`: 가맹점 및 본사의 발주(Order) 프로세스
  - `domain-inventories`: 공장 및 가맹점의 재고(Inventory) 관리
  - `domain-settlements`: 가맹점 수익 및 본사 정산(Settlement) 로직
  - `domain-transports`: 물류 및 차량 배차(Logistics) 정보
  - `domain-users`: 회원 정보 및 권한 관리
  - *기타 도메인*: `products`, `notices`, `notifications`, `sales`, `returns` 등
- **`module-app:app-api`**: 외부 요청을 처리하는 API 엔드포인트를 제공하며 도메인 모듈을 조합하여 실제 서비스를 제공합니다.
- **`module-external-transport`**: 외부 시스템(물류 시스템 등)과의 연동을 담당합니다.

<br>

## 🚀 주요 비즈니스 프로세스

### 1. 발주 및 생산 (Ordering & Production)
- **가맹점 -> 본사 발주**: 가맹점에서 부족한 밀키트 품목을 본사에 요청합니다. (화/금 도착 기준)
- **본사 -> 공장 생산 요청**: 본사에서 전체 가맹점의 수요를 취합하여 공장에 생산 지시를 내립니다.
- **제품 코드 체계**: 제품명 + 매운맛(01~04) + 사이즈(01, 03)의 조합으로 관리됩니다.

- ### 2. 물류 및 재고 (Logistics & Inventory)
- **박스 및 제품 식별 코드**: 생산된 제품은 지역/공장/생산라인 정보가 포함된 고유 코드로 관리됩니다.
- **피킹 및 차량 배차**: 출고 전 패키징 확정(피킹) 후 적재 중량을 고려하여 물류 차량에 자동/반자동 배차를 진행합니다.

### 3. 정산 (Settlement)
- **매출 및 대금 정산**: 가맹점의 판매 데이터를 기반으로 본사 대금 차감 및 수수료 정산을 수행합니다.
- **반품 관리**: 하자 상품에 대한 반품 요청 및 검수 후 대금 차감을 지원합니다.

<br>

## 🛠 기술 스택

| Category | Stack |
| :--- | :--- |
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.2.2 |
| **Persistence** | Spring Data JPA, MySQL 8.x |
| **Build Tool** | Gradle |
| **Testing** | JUnit 5, AssertJ, JaCoCo (Line Coverage 80% 이상 준수) |
| **CI/CD & Tools** | SonarQube, Lombok, GitHub Actions |

<br>

## 📖 개발 가이드 및 컨벤션

## 👥 팀 협업 규칙

- **데일리 스크럼**: 평일 오전 09:00 (10분 내외)
- **지라(Jira) 연동**: 작업 시작 전 이슈 생성 및 브랜치(`feat/이슈번호-도메인`) 생성 필수
- **품질 관리**: 모든 PR은 SonarQube 분석 및 테스트 통과를 기본으로 합니다.

### ⭐ Code Convention

<details>
<summary style="font-size:1.2em;">1. Naming (명명 규칙)</summary>
<div markdown="1">

- **패키지**: 언더스코어(`_`) 없이 소문자만 사용
- **클래스**: 대문자 카멜 케이스 (`UpperCamelCase`)
- **메서드**: 동사/전치사 시작, 소문자 카멜 케이스 (`lowerCamelCase`)
- **변수**: 소문자 카멜 케이스 (`lowerCamelCase`)
- **ENUM/상수**: 대문자 및 언더스코어 (`UPPER_SNAKE_CASE`)
- **DB 테이블**: 소문자 및 언더스코어 (`lower_snake_case`)
- **컬렉션**: 복수형 사용 혹은 명시 (`users`, `userList`, `userMap`)

</div>
</details>

<details>
<summary style="font-size:1.2em;">2. Comment & Import (주석 및 임포트)</summary>
<div markdown="1">

- **주석**: 한 줄 주석은 `//`, 여러 줄은 `/* ... */` 사용
- **파일 구조**: 소스파일당 1개의 탑레벨 클래스만 포함
- **Import**: 와일드카드(`*`) 사용 금지 (단, static import는 허용)
- **Annotation**: 선언 후 새 줄 사용 (파라미터 없는 1개는 같은 줄 허용)
- **기타**: 배열 대괄호는 타입 뒤에 (`String[]`), `long`형 값 끝에는 대문자 `L` 사용

</div>
</details>

<details>
<summary style="font-size:1.2em;">3. URL (RESTful API)</summary>
<div markdown="1">

- **행위 배제**: URL에 get, put 등 행위 표현 금지 (HTTP Method로 구분)
- **구분자**: `_` 대신 `-`(Kebab-case) 사용
- **형식**: 소문자 사용, 마지막 `/` 및 확장자 포함 금지

</div>
</details>

### ⭐ Commit Convention

<details>
<summary style="font-size:1.2em;">1. Git Flow & Rules</summary>
<div markdown="1">

- **프로세스**: Issue 생성 → Jira 티켓 관리 → feature 브랜치 생성 → add/commit/push → PR 생성 → dev 머지
- **기본 규칙**: `dev` 브랜치 직접 작업 금지 (README 제외), 모든 작업은 정상 실행 확인 후 수행
- **브랜치 네이밍**: `<Prefix>/<Ticket_Number>-<Domain>-<Description>`
  - 예시: `feat/7-order-create-order`, `feat/5-settlement-monthly`

</div>
</details>

<details>
<summary style="font-size:1.2em;">2. Commit Message & PR</summary>
<div markdown="1">

- **양식**: `[<Prefix>] #<Issue_Number> <Description>`
- **Prefix**:
  - `feat`: 새로운 기능 구현
  - `fix`: 버그 수정
  - `del`: 코드 삭제
  - `docs`: 문서 개정
  - `refactor`: 리팩터링
  - `chore`: 빌드 업무, 패키지 구조 변경, 의존성 추가
  - `test`: 테스트 코드 작성 및 수정

</div>
</details>

<br>

## 📝 프로젝트 회고 (Team Retrospective)

프로젝트를 마치며 팀원 각자가 느낀 기술적 도전과 성장을 기록합니다.

<details>
<summary style="font-size:1.2em;">🦫 김채우</summary>
<div markdown="1">

(작성 예정)

</div>
</details>

<details>
<summary style="font-size:1.2em;">☠️ 김성은</summary>
<div markdown="1">

(작성 예정)

</div>
</details>

<details>
<summary style="font-size:1.2em;">🐇 김윤경</summary>
<div markdown="1">

(작성 예정)

</div>
</details>

<details>
<summary style="font-size:1.2em;">🦘 유찬연</summary>
<div markdown="1">

(작성 예정)

</div>
</details>

<details>
<summary style="font-size:1.2em;">🐜 조윤호</summary>
<div markdown="1">

(작성 예정)

</div>
</details>

<br>
