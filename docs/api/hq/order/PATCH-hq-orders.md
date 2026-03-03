### API 이름

> 본사 발주 수정

---

## 요청

- **메서드** `PATCH`
- **경로** `/api/v1/hq/orders/{order-code}`
- 헤더

  ```
  Content-Type: application/json
  Authorization: Bearer {accessToken}
  ```

- 경로 변수

  | 파라미터 | 타입 | 필수 | 설명 |
  | --- | --- | --- | --- |
  | order-code | string | Y | 발주 코드 |

- 요청 본문

  ```json
  {
    "manufactureDate": "string(datetime)",
    "items": [
      {
        "productId": "number",
        "quantity": "number"
      }
    ]
  }
  ```

- curl 명령 예시

  ```
  curl -i -X PATCH 'http://localhost:8080/api/v1/hq/orders/ORD-20240101-001' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer {accessToken}' \
  -d '{
    "manufactureDate": "2024-02-15T00:00:00",
    "items": [
      { "productId": 1, "quantity": 150 }
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
      "orderInfo": {
        "orderId": "number",
        "orderCode": "string",
        "status": "PENDING | ACCEPTED | DELIVERING | DELIVERED | CANCELED | REJECTED",
        "username": "string",
        "phoneNumber": "string",
        "requestedDate": "string(datetime)",
        "manufacturedDate": "string(datetime)",
        "storedDate": "string(nullable)",
        "description": "string(nullable)"
      },
      "items": [
        {
          "productId": "number",
          "productCode": "string",
          "productName": "string",
          "quantity": "number",
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
- 요청자는 HQ 권한을 가져야 한다 (권한 없으면 403)
- manufactureDate는 필수이며 null이면 400을 반환한다
- items는 필수이며 null이면 400을 반환한다
- items의 각 productId는 필수이며 null이면 400을 반환한다
- 존재하지 않는 order-code면 404를 반환한다
- 존재하지 않는 productId면 404를 반환한다
- PENDING(대기) 상태인 발주만 수정할 수 있다

---

## 테스트

- [ ] 올바르게 요청하면 200 OK 상태코드를 반환한다
- [ ] 액세스 토큰을 사용하지 않으면 401 Unauthorized 상태코드를 반환한다
- [ ] HQ 권한이 없으면 403 Forbidden 상태코드를 반환한다
- [ ] 존재하지 않는 order-code면 404 Not Found를 반환한다
- [ ] 필수 필드(manufactureDate, items)가 없으면 400 Bad Request를 반환한다
- [ ] 존재하지 않는 productId면 404 Not Found를 반환한다
- [ ] PENDING이 아닌 발주를 수정하면 예외를 반환한다
