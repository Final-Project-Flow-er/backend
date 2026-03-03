### API 이름

> 반품 제품 검수

---

## 요청

- **메서드** `PATCH`
- **경로** `/api/v1/hq/returns/{return-code}/inspection`
- 헤더

  ```
  Content-Type: application/json
  Authorization: Bearer {accessToken}
  ```

- 경로 변수

  | 파라미터        | 타입 | 필수 | 설명 |
  |-------------| --- | --- | --- |
  | return-code | string | Y | 반품 코드 |

- 요청 본문

    ```json
  {
    "returnCode": "string",
    "items": [
      {
        "serialCode": "string",
        "status": "BEFORE | NORMAL | DEFECTIVE"
      }
    ]
  }
  ```

  > 현재 요청 본문 구조는 미정입니다.

- curl 명령 예시

  ```
  curl -i -X PATCH 'http://localhost:8080/api/v1/hq/returns/RTN-20240101-001/inspection' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer {accessToken}' \
  -d '{}'
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
      {"result": "ACCEPTED | REJECTED"}
    ]
  }
  ```


---

## 정책

- 요청자는 인증되어야 한다 (미인증 시 401)
- 요청자는 HQ 권한을 가져야 한다 (권한 없으면 403)
- 존재하지 않는 return-number면 404를 반환한다
- ACCEPTED(접수) 상태인 반품에 대해서만 검수를 진행할 수 있다

---

## 테스트

- [ ] 올바르게 요청하면 200 OK 상태코드를 반환한다
- [ ] 액세스 토큰을 사용하지 않으면 401 Unauthorized 상태코드를 반환한다
- [ ] HQ 권한이 없으면 403 Forbidden 상태코드를 반환한다
- [ ] 존재하지 않는 return-number면 404 Not Found를 반환한다
- [ ] ACCEPTED 상태가 아닌 반품을 검수하면 예외를 반환한다
