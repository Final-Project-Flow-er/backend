### API 이름

> 본사 발주 목록 조회

---

## 요청

- **메서드** `GET`
- **경로** `/api/v1/hq/orders`
- 헤더

  ```
  Content-Type: application/json
  Authorization: Bearer {accessToken}
  ```

- curl 명령 예시

  ```
  curl -i -X GET 'http://localhost:8080/api/v1/hq/orders' \
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
        "orderCode": "string",
        "status": "PENDING | ACCEPTED | DELIVERING | DELIVERED | CANCELED | REJECTED",
        "quantity": "number",
        "username": "string",
        "phoneNumber": "string",
        "requestedDate": "string(datetime)",
        "manufacturedDate": "string(datetime)",
        "storedDate": "string",
        "productCode": "string"
      }
    ]
  }
  ```

---

## 정책

- 요청자는 인증되어야 한다 (미인증 시 401)
- 요청자는 HQ 권한을 가져야 한다 (권한 없으면 403)
- 본사의 전체 발주 목록을 반환한다
- 발주가 없으면 빈 배열을 반환한다

---

## 테스트

- [ ] 올바르게 요청하면 200 OK 상태코드를 반환한다
- [ ] 액세스 토큰을 사용하지 않으면 401 Unauthorized 상태코드를 반환한다
- [ ] HQ 권한이 없으면 403 Forbidden 상태코드를 반환한다
- [ ] 발주가 없으면 빈 배열을 반환한다
