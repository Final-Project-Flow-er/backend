### API 이름

> 가맹점 특정 발주 조회

---

## 요청

- **메서드** `GET`
- **경로** `/api/v1/franchise/orders/{order-code}`
- 헤더

  ```
  Content-Type: application/json
  Authorization: Bearer {accessToken}
  ```

- 경로 변수

  | 파라미터 | 타입 | 필수 | 설명 |
  | --- | --- | --- | --- |
  | order-code | string | Y | 발주 코드 |

- curl 명령 예시

  ```
  curl -i -X GET 'http://localhost:8080/api/v1/franchise/orders/ORD-20240101-001' \
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
    "data": {
      "orderCode": "string",
      "orderStatus": "PENDING | ACCEPTED | PARTIAL_ACCEPTED | DELIVERING | DELIVERED | CANCELED | REJECTED",
      "totalPrice": "number",
      "createdAt": "string(datetime)",
      "receiver": "string",
      "deliveryDate": "string(datetime)",
      "items": [
        {
          "serialCode": "string",
          "unitPrice": "number",
          "quantity": "number",
          "totalUnitPrice": "number"
        }
      ]
    }
  }
  ```

---

## 정책

- 요청자는 인증되어야 한다 (미인증 시 401)
- 요청자는 FRANCHISE 권한을 가져야 한다 (권한 없으면 403)
- 요청자가 속한 가맹점의 발주만 조회할 수 있다 (타 가맹점 발주 조회 시 403)
- order-code는 비어있지 않아야 한다
- 존재하지 않는 order-code면 404를 반환한다

---

## 테스트

- [ ] 올바르게 요청하면 200 OK 상태코드를 반환한다
- [ ] 액세스 토큰을 사용하지 않으면 401 Unauthorized 상태코드를 반환한다
- [ ] FRANCHISE 권한이 없으면 403 Forbidden 상태코드를 반환한다
- [ ] 타 가맹점의 발주를 조회하면 403 Forbidden 상태코드를 반환한다
- [ ] 존재하지 않는 order-code면 404 Not Found 상태코드를 반환한다
- [ ] 반환된 발주의 orderCode가 요청한 order-code와 일치한다
