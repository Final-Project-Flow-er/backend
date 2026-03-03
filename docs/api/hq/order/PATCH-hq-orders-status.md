### API 이름

> 가맹점 발주 상태 변경 (본사)

---

## 요청

- **메서드** `PATCH`
- **경로** `/api/v1/hq/orders`
- 헤더

  ```
  Content-Type: application/json
  Authorization: Bearer {accessToken}
  ```

- 요청 본문

  ```json
  {
    "orderCodes": ["string"],
    "isAccepted": "boolean"
  }
  ```

- curl 명령 예시

  ```
  curl -i -X PATCH 'http://localhost:8080/api/v1/hq/orders' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer {accessToken}' \
  -d '{
    "orderCodes": ["ORD-20240101-001", "ORD-20240101-002"],
    "isAccepted": true
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
    "data": [
      {
        "orderCode": "string",
        "status": "ACCEPTED | REJECTED"
      }
    ]
  }
  ```

---

## 정책

- 요청자는 인증되어야 한다 (미인증 시 401)
- 요청자는 HQ 권한을 가져야 한다 (권한 없으면 403)
- orderCodes는 필수이며 null이면 400을 반환한다
- isAccepted는 필수이며 null이면 400을 반환한다
- isAccepted가 true이면 상태를 ACCEPTED(접수)로, false이면 REJECTED(반려)로 변경한다
- 존재하지 않는 orderCode가 포함되어 있으면 404를 반환한다
- PENDING(대기) 상태인 발주만 상태를 변경할 수 있다

---

## 테스트

- [ ] 올바르게 요청하면 200 OK 상태코드를 반환한다
- [ ] 액세스 토큰을 사용하지 않으면 401 Unauthorized 상태코드를 반환한다
- [ ] HQ 권한이 없으면 403 Forbidden 상태코드를 반환한다
- [ ] orderCodes 또는 isAccepted가 null이면 400 Bad Request를 반환한다
- [ ] 존재하지 않는 orderCode가 포함되면 404 Not Found를 반환한다
- [ ] isAccepted가 true이면 상태가 ACCEPTED로 변경된다
- [ ] isAccepted가 false이면 상태가 REJECTED로 변경된다
- [ ] PENDING이 아닌 발주의 상태를 변경하면 예외를 반환한다
- [ ] 여러 발주를 한 번에 상태 변경할 수 있다
