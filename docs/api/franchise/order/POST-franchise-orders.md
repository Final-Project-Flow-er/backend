### API 이름

> 가맹점 발주 생성

---

## 요청

- **메서드** `POST`
- **경로** `/api/v1/franchise/orders`
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
    "deliveryDate": "string(datetime)",
    "deliveryTime": "string",
    "address": "string",
    "requirement": "string(nullable)",
    "items": [
      {
        "productCode": "string",
        "quantity": "number(min: 1)"
      }
    ]
  }
  ```

- curl 명령 예시

  ```
  curl -i -X POST 'http://localhost:8080/api/v1/franchise/orders' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer {accessToken}' \
  -d '{
    "username": "홍길동",
    "phoneNumber": "010-1234-5678",
    "deliveryDate": "2024-02-01T10:00:00",
    "deliveryTime": "오전",
    "address": "서울시 강남구 테헤란로 123",
    "requirement": "문 앞에 놓아주세요",
    "items": [
      { "productCode": "PRD-001", "quantity": 10 }
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
      "orderCode": "string",
      "orderStatus": "PENDING",
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
- username, phoneNumber, address, deliveryTime은 필수이며 비어있으면 400을 반환한다
- deliveryDate는 필수이며 null이면 400을 반환한다
- items의 각 productCode는 필수이며 비어있으면 400을 반환한다
- items의 각 quantity는 1 이상이어야 한다 (미만이면 400)
- 존재하지 않는 productCode면 404를 반환한다
- 생성된 발주의 초기 상태는 PENDING(대기)이다

---

## 테스트

- [ ] 올바르게 요청하면 200 OK 상태코드를 반환한다
- [ ] 액세스 토큰을 사용하지 않으면 401 Unauthorized 상태코드를 반환한다
- [ ] FRANCHISE 권한이 없으면 403 Forbidden 상태코드를 반환한다
- [ ] 필수 필드(username, phoneNumber, address, deliveryTime)가 없으면 400 Bad Request를 반환한다
- [ ] quantity가 1 미만이면 400 Bad Request를 반환한다
- [ ] 존재하지 않는 productCode면 404 Not Found를 반환한다
- [ ] 생성된 발주의 상태가 PENDING이다
