### API 이름

> 반품 생성 화면 데이터 조회

---

## 요청

- **메서드** `GET`
- **경로** `/api/v1/franchise/returns/{order-code}/{username}`
- 헤더

  ```
  Content-Type: application/json
  Authorization: Bearer {accessToken}
  ```

- 경로 변수

  | 파라미터 | 타입 | 필수 | 설명 |
  | --- | --- | --- | --- |
  | order-code | string | Y | 반품 대상 발주 코드 |
  | username | string | Y | 발주 담당자 username |

- curl 명령 예시

  ```
  curl -i -X GET 'http://localhost:8080/api/v1/franchise/returns/ORD-20240101-001/홍길동' \
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
      "orderInfo": {
        "orderId": "number",
        "username": "string",
        "phoneNumber": "string",
        "franchiseCode": "string"
      },
      "items": [
        {
          "boxCode": "string",
          "productCode": "string",
          "productName": "string",
          "unitPrice": "number"
        }
      ]
    }
  }
  ```

---

## 정책

- 요청자는 인증되어야 한다 (미인증 시 401)
- 요청자는 FRANCHISE 권한을 가져야 한다 (권한 없으면 403)
- 요청자가 속한 가맹점의 발주만 조회할 수 있다 (타 가맹점 발주 조회 시 403)
- 존재하지 않는 order-code면 404를 반환한다
- 반품 가능한(DELIVERED) 상태의 발주만 조회할 수 있다

---

## 테스트

- [ ] 올바르게 요청하면 200 OK 상태코드를 반환한다
- [ ] 액세스 토큰을 사용하지 않으면 401 Unauthorized 상태코드를 반환한다
- [ ] FRANCHISE 권한이 없으면 403 Forbidden 상태코드를 반환한다
- [ ] 타 가맹점의 발주를 조회하면 403 Forbidden 상태코드를 반환한다
- [ ] 존재하지 않는 order-code면 404 Not Found를 반환한다
- [ ] 반환된 데이터에 발주 정보(orderInfo)와 상품 목록(items)이 모두 포함된다
