### API 이름

> 가맹점 판매 생성

---

## 요청

- **메서드** `POST`
- **경로** `/api/v1/franchise/sales`
- 헤더

  ```
  Content-Type: application/json
  Authorization: Bearer {accessToken}
  ```

- 요청 본문

  ```json
  {
    "totalQuantity": "number",
    "totalAmount": "number(min: 1)",
    "requestList": [
      {
        "productId": "number",
        "productCode": "string",
        "productName": "string",
        "quantity": "number(min: 1)",
        "unitPrice": "number",
        "lot": "string"
      }
    ]
  }
  ```

- curl 명령 예시

  ```
  curl -i -X POST 'http://localhost:8080/api/v1/franchise/sales' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer {accessToken}' \
  -d '{
    "totalQuantity": 3,
    "totalAmount": 45000,
    "requestList": [
      {
        "productId": 1,
        "productCode": "PRD-001",
        "productName": "국물떡볶이 밀키트",
        "quantity": 3,
        "unitPrice": 15000,
        "lot": "2024010101"
      }
    ]
  }'
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
      "salesCode": "string",
      "totalQuantity": "number",
      "totalPrice": "number",
      "items": [
        {
          "productCode": "string",
          "productName": "string",
          "quantity": "number(min: 1)",
          "unitPrice": "number",
          "totalPrice": "number"
        }
      ]
    }
  }
  ```

---

## 정책

- 요청자는 인증되어야 한다 (미인증 시 401)
- 요청자는 FRANCHISE 권한을 가져야 한다 (권한 없으면 403)
- totalQuantity는 필수이며 null이면 400을 반환한다
- totalAmount는 필수이며 1 미만이면 400을 반환한다
- requestList의 각 productId, productCode, productName, lot은 필수이며 비어있으면 400을 반환한다
- requestList의 각 quantity는 1 이상이어야 한다 (미만이면 400)
- 존재하지 않는 productId면 404를 반환한다

---

## 테스트

- [ ] 올바르게 요청하면 200 OK 상태코드를 반환한다
- [ ] 액세스 토큰을 사용하지 않으면 401 Unauthorized 상태코드를 반환한다
- [ ] FRANCHISE 권한이 없으면 403 Forbidden 상태코드를 반환한다
- [ ] 필수 필드가 없으면 400 Bad Request를 반환한다
- [ ] totalAmount가 1 미만이면 400 Bad Request를 반환한다
- [ ] quantity가 1 미만이면 400 Bad Request를 반환한다
- [ ] 존재하지 않는 productId면 404 Not Found를 반환한다
