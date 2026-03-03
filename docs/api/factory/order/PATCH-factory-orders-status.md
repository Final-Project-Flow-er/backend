### API 이름

> 공장 발주 상태 변경

---

## 요청

- **메서드** `PATCH`
- **경로** `/api/v1/factory/orders/{order-number}`
- 헤더

  ```
  Content-Type: application/json
  Authorization: Bearer {accessToken}
  ```

- 경로 변수

  | 파라미터 | 타입 | 필수 | 설명 |
  | --- | --- | --- | --- |
  | order-number | string | Y | 발주 코드 |

- 요청 본문

  ```json
  {}
  ```


- curl 명령 예시

  ```
  curl -i -X PATCH 'http://localhost:8080/api/v1/factory/orders/ORD-20240101-001' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer {accessToken}' \
  -d '{}'
  ```

---

## 성공 응답

- 상태코드: 200 OK

  | 항목 | 값 |
  | --- | --- |
  | **상태코드** | `200 OK` |

- 본문

    ```json
  {
    "success": true,
    "message": "SUCCESS",
    "data": {
      "orderCode": "string",
      "status": "PENDING | ACCEPTED"
    }
  }
  ```

  > 현재 응답 본문 구조는 미정입니다.

---

## 정책

- 요청자는 인증되어야 한다 (미인증 시 401)
- 요청자는 FACTORY 권한을 가져야 한다 (권한 없으면 403)
- 인증된 사용자가 속한 공장에 배정된 발주만 상태를 변경할 수 있다 (타 공장 발주 변경 시 403)
- 존재하지 않는 order-number면 404를 반환한다
- 변경 가능한 상태: PENDING → ACCEPTED 또는 REJECTED, ACCEPTED → DELIVERING, DELIVERING → DELIVERED

---

## 테스트

- [ ] 올바르게 요청하면 200 OK 상태코드를 반환한다
- [ ] 액세스 토큰을 사용하지 않으면 401 Unauthorized 상태코드를 반환한다
- [ ] FACTORY 권한이 없으면 403 Forbidden 상태코드를 반환한다
- [ ] 타 공장의 발주 상태를 변경하면 403 Forbidden 상태코드를 반환한다
- [ ] 존재하지 않는 order-number면 404 Not Found를 반환한다
- [ ] PENDING 상태에서 ACCEPTED로 변경할 수 있다
- [ ] PENDING 상태에서 REJECTED로 변경할 수 있다
- [ ] ACCEPTED 상태에서 DELIVERING으로 변경할 수 있다
- [ ] DELIVERED 상태의 발주 상태를 변경하면 예외를 반환한다
