### API 이름

> 가맹점 판매 세부 조회

---

## 요청

- **메서드** `GET`
- **경로** `/api/v1/franchise/sales/{sales-code}`
- 헤더

  ```
  Content-Type: application/json
  Authorization: Bearer {accessToken}
  ```

- 경로 변수

  | 파라미터 | 타입 | 필수 | 설명 |
  | --- | --- | --- | --- |
  | sales-code | string | Y | 판매 코드 |

- curl 명령 예시

  ```
  curl -i -X GET 'http://localhost:8080/api/v1/franchise/sales/SLS-20240101-001' \
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
      "salesCode": "string",
      "salesDate": "string(datetime)",
      "products": [
        {
          "productCode": "string",
          "productName": "string",
          "quantity": "number(min: 1)",
          "unitPrice": "number(min: 1)",
          "totalPrice": "number(min: 1)",
          "lot": "string"
        }
      ]
    }
  }
  ```

---

## 정책

- 요청자는 인증되어야 한다 (미인증 시 401)
- 요청자는 FRANCHISE 권한을 가져야 한다 (권한 없으면 403)
- 요청자가 속한 가맹점의 판매만 조회할 수 있다 (타 가맹점 판매 조회 시 403)
- 존재하지 않는 sales-code면 404를 반환한다

---

## 테스트

- [ ] 올바르게 요청하면 200 OK 상태코드를 반환한다
- [ ] 액세스 토큰을 사용하지 않으면 401 Unauthorized 상태코드를 반환한다
- [ ] FRANCHISE 권한이 없으면 403 Forbidden 상태코드를 반환한다
- [ ] 타 가맹점의 판매를 조회하면 403 Forbidden 상태코드를 반환한다
- [ ] 존재하지 않는 sales-code면 404 Not Found를 반환한다
- [ ] 반환된 판매의 salesCode가 요청한 sales-code와 일치한다
