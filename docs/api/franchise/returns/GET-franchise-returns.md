### API 이름

> 가맹점 반품 목록 조회

---

## 요청

- **메서드** `GET`
- **경로** `/api/v1/franchise/returns`
- 헤더

  ```
  Content-Type: application/json
  Authorization: Bearer {accessToken}
  ```

- curl 명령 예시

  ```
  curl -i -X GET 'http://localhost:8080/api/v1/franchise/returns' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer {accessToken}'
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
    "data": [
      {
        "returnCode": "string",
        "status": "PENDING | ACCEPTED | INSPECTING | COMPLETED | CANCELED | REJECTED",
        "unitPrice": "number",
        "franchiseOrderId": "number",
        "quantity": "number",
        "type": "DEFECT | SURPLUS",
        "requestedDate": "string(datetime)",
        "boxCode": "string",
        "serialCode": "string",
        "orderCode": "string",
        "productCode": "string",
        "productName": "string"
      }
    ]
  }
  ```

---

## 정책

- 요청자는 인증되어야 한다 (미인증 시 401)
- 요청자는 FRANCHISE 권한을 가져야 한다 (권한 없으면 403)
- 인증된 사용자가 속한 가맹점의 반품 목록만 조회된다
- 반품이 없으면 빈 배열을 반환한다

---

## 테스트

- [ ] 올바르게 요청하면 200 OK 상태코드를 반환한다
- [ ] 액세스 토큰을 사용하지 않으면 401 Unauthorized 상태코드를 반환한다
- [ ] FRANCHISE 권한이 없으면 403 Forbidden 상태코드를 반환한다
- [ ] 반환된 반품 목록이 인증된 사용자의 가맹점 반품만 포함된다
- [ ] 반품이 없으면 빈 배열을 반환한다
