### API 이름

> 본사 반품 요청 목록 조회

---

## 요청

- **메서드** `GET`
- **경로** `/api/v1/hq/returns`
- 헤더

  ```
  Content-Type: application/json
  Authorization: Bearer {accessToken}
  ```

- 쿼리 매개변수

  | 파라미터 | 타입 | 필수 | 설명 |
  | --- | --- | --- | --- |
  | isAccepted | boolean | Y | false: 대기(PENDING) 목록 조회, true: 접수 이후 목록 조회 |

- curl 명령 예시

  ```
  curl -i -X GET 'http://localhost:8080/api/v1/hq/returns?isAccepted=false' \
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
    ]
  }
  ```

---

## 정책

- 요청자는 인증되어야 한다 (미인증 시 401)
- 요청자는 HQ 권한을 가져야 한다 (권한 없으면 403)
- isAccepted 쿼리 파라미터는 필수이며 없으면 400을 반환한다
- isAccepted = false: PENDING(대기) 상태의 반품 목록을 반환한다
- isAccepted = true: PENDING 이후 상태(접수, 검수중, 완료, 취소, 반려)의 반품 목록을 반환한다
- 반품이 없으면 빈 배열을 반환한다

---

## 테스트

- [ ] 올바르게 요청하면 200 OK 상태코드를 반환한다
- [ ] 액세스 토큰을 사용하지 않으면 401 Unauthorized 상태코드를 반환한다
- [ ] HQ 권한이 없으면 403 Forbidden 상태코드를 반환한다
- [ ] isAccepted 파라미터가 없으면 400 Bad Request를 반환한다
- [ ] isAccepted=false이면 PENDING 상태의 반품만 반환된다
- [ ] isAccepted=true이면 PENDING 이후 상태의 반품만 반환된다
- [ ] 반품이 없으면 빈 배열을 반환한다
