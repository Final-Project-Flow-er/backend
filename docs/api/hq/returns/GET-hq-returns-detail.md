### API 이름

> 본사 특정 반품 요청 조회

---

## 요청

- **메서드** `GET`
- **경로** `/api/v1/hq/returns/{return-code}`
- 헤더

  ```
  Content-Type: application/json
  Authorization: Bearer {accessToken}
  ```

- 경로 변수

  | 파라미터        | 타입 | 필수 | 설명 |
  |-------------| --- | --- | --- |
  | return-code | string | Y | 반품 코드 |

- curl 명령 예시

  ```
  curl -i -X GET 'http://localhost:8080/api/v1/hq/returns/RTN-20240101-001' \
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
      "franchiseCode": "string",
      "requestedDate": "string(datetime)",
      "returnCode": "string",
      "status": "PENDING | ACCEPTED | INSPECTING | COMPLETED | CANCELED | REJECTED",
      "productCode": "string",
      "type": "DEFECT | SURPLUS",
      "quantity": "number(min: 1)",
      "totalPrice": "number",
      "receiver": "string",
      "phoneNumber": "string",
      "boxCode": "string"
    }
  }
  ```

---

## 정책

- 요청자는 인증되어야 한다 (미인증 시 401)
- 요청자는 HQ 권한을 가져야 한다 (권한 없으면 403)
- 존재하지 않는 return-number면 404를 반환한다

---

## 테스트

- [ ] 올바르게 요청하면 200 OK 상태코드를 반환한다
- [ ] 액세스 토큰을 사용하지 않으면 401 Unauthorized 상태코드를 반환한다
- [ ] HQ 권한이 없으면 403 Forbidden 상태코드를 반환한다
- [ ] 존재하지 않는 return-number면 404 Not Found를 반환한다
- [ ] 반환된 반품의 returnCode가 요청한 return-number와 일치한다
