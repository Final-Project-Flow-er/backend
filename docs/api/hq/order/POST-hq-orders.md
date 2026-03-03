### API 이름

> 본사 발주 생성

---

## 요청

- **메서드** `POST`
- **경로** `/api/v1/hq/orders`
- 헤더

  ```
  Content-Type: application/json
  Authorization: Bearer {accessToken}
  ```

- 요청 본문

  ```json
  {
    "username": "string",
    "phoneNumber": "string",
    "description": "string(nullable)",
    "isRegular": "boolean",
    "manufactureDate": "string(datetime)",
    "items": [
      {
        "productId": "number",
        "quantity": "number(min: 1)"
      }
    ]
  }
  ```

- curl 명령 예시

  ```
  curl -i -X POST 'http://localhost:8080/api/v1/hq/orders' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer {accessToken}' \
  -d '{
    "username": "홍길동",
    "phoneNumber": "010-1234-5678",
    "description": "정기 발주",
    "isRegular": true,
    "manufactureDate": "2024-02-01T00:00:00",
    "items": [
      { "productId": 1, "quantity": 100 }
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
        "status": "PENDING",
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
- username, phoneNumber는 필수이며 비어있으면 400을 반환한다
- isRegular, manufactureDate는 필수이며 null이면 400을 반환한다
- items는 비어있으면 400을 반환한다
- items의 각 productId는 필수이며 null이면 400을 반환한다
- items의 각 quantity는 1 이상이어야 한다 (미만이면 400)
- 존재하지 않는 productId면 404를 반환한다
- 생성된 발주의 초기 상태는 PENDING(대기)이다

---

## 테스트

- [ ] 올바르게 요청하면 200 OK 상태코드를 반환한다
- [ ] 액세스 토큰을 사용하지 않으면 401 Unauthorized 상태코드를 반환한다
- [ ] HQ 권한이 없으면 403 Forbidden 상태코드를 반환한다
- [ ] 필수 필드(username, phoneNumber, isRegular, manufactureDate)가 없으면 400 Bad Request를 반환한다
- [ ] items가 비어있으면 400 Bad Request를 반환한다
- [ ] quantity가 1 미만이면 400 Bad Request를 반환한다
- [ ] 존재하지 않는 productId면 404 Not Found를 반환한다
- [ ] 생성된 발주의 상태가 PENDING이다
