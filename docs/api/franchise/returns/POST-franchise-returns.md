### API 이름

> 가맹점 반품 생성

---

## 요청

- **메서드** `POST`
- **경로** `/api/v1/franchise/returns`
- 헤더

  ```
  Content-Type: application/json
  Authorization: Bearer {accessToken}
  ```

- 요청 본문

  ```json
  {
    "orderCode": "string",
    "returnType": "DEFECT | SURPLUS",
    "description": "string(nullable)",
    "totalPrice": "number",
    "items": [
      {
        "boxCode": "string",
        "productCode": "string",
        "productName": "string",
        "unitPrice": "number"
      }
    ]
  }
  ```

- curl 명령 예시

  ```
  curl -i -X POST 'http://localhost:8080/api/v1/franchise/returns' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer {accessToken}' \
  -d '{
    "orderCode": "ORD-20240101-001",
    "returnType": "DEFECT",
    "description": "제품 불량 발생",
    "totalPrice": 30000,
    "items": [
      {
        "boxCode": "BOX-001",
        "productCode": "PRD-001",
        "productName": "국물떡볶이 밀키트",
        "unitPrice": 15000
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
      "returnInfo": {
        "returnCode": "string",
        "status": "PENDING",
        "franchiseOrderId": "number",
        "type": "DEFECT | SURPLUS",
        "requestedDate": "string(datetime)"
      },
      "returnAndOrderInfos": [
        {
          "orderItemId": "number",
          "returnItemId": "number"
        }
      ]
    }
  }
  ```

---

## 정책

- 요청자는 인증되어야 한다 (미인증 시 401)
- 요청자는 FRANCHISE 권한을 가져야 한다 (권한 없으면 403)
- orderCode는 필수이며 비어있으면 400을 반환한다
- returnType은 필수이며 null이면 400을 반환한다
- items의 각 boxCode, productCode, productName은 필수이며 비어있으면 400을 반환한다
- 존재하지 않는 orderCode면 404를 반환한다
- 요청자가 속한 가맹점의 발주에 대해서만 반품을 생성할 수 있다 (타 가맹점 발주 반품 시 403)
- DELIVERED(배송 완료) 상태인 발주에 대해서만 반품을 생성할 수 있다
- 생성된 반품의 초기 상태는 PENDING(대기)이다

---

## 테스트

- [ ] 올바르게 요청하면 200 OK 상태코드를 반환한다
- [ ] 액세스 토큰을 사용하지 않으면 401 Unauthorized 상태코드를 반환한다
- [ ] FRANCHISE 권한이 없으면 403 Forbidden 상태코드를 반환한다
- [ ] 타 가맹점의 발주에 대해 반품을 생성하면 403 Forbidden 상태코드를 반환한다
- [ ] orderCode가 비어있으면 400 Bad Request를 반환한다
- [ ] returnType이 null이면 400 Bad Request를 반환한다
- [ ] 존재하지 않는 orderCode면 404 Not Found를 반환한다
- [ ] DELIVERED가 아닌 발주에 반품을 생성하면 예외를 반환한다
- [ ] 생성된 반품의 상태가 PENDING이다
