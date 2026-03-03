### API 이름

> 본사 발주 취소

---

## 요청

- **메서드** `PATCH`
- **경로** `/api/v1/hq/orders/{order-code}/cancellation`
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
  curl -i -X PATCH 'http://localhost:8080/api/v1/hq/orders/ORD-20240101-001/cancellation' \
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
      "status": "CANCELED"
    }
  }
  ```

---

## 정책

- 요청자는 인증되어야 한다 (미인증 시 401)
- 요청자는 HQ 권한을 가져야 한다 (권한 없으면 403)
- 존재하지 않는 order-code면 404를 반환한다
- PENDING(대기) 상태인 발주만 취소할 수 있다
- 취소 후 발주 상태는 CANCELED가 된다

---

## 테스트

- [ ] 올바르게 요청하면 200 OK 상태코드를 반환한다
- [ ] 액세스 토큰을 사용하지 않으면 401 Unauthorized 상태코드를 반환한다
- [ ] HQ 권한이 없으면 403 Forbidden 상태코드를 반환한다
- [ ] 존재하지 않는 order-code면 404 Not Found를 반환한다
- [ ] 취소 후 응답의 status가 CANCELED이다
- [ ] PENDING이 아닌 발주를 취소하면 예외를 반환한다
