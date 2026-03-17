import http from "k6/http";
import { check, fail } from "k6";
import { BASE_URL } from "./config.js";

// 로그인 후 accessToken 반환
export function login(loginId, password) {
  const res = http.post(
    `${BASE_URL}/api/v1/auth/login`,
    JSON.stringify({ loginId, password }),
    { headers: { "Content-Type": "application/json" } }
  );

  const ok = check(res, {
    "login status 201": (r) => r.status === 201,
  });

  if (!ok) {
    fail(`Login failed for ${loginId}: ${res.status} ${res.body}`);
  }

  return res.json().data.accessToken;
}

// Authorization 헤더 생성
export function authHeaders(token) {
  return {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  };
}

// 응답 검증 헬퍼
export function checkOk(res, name) {
  check(res, {
    [`${name} status 200`]: (r) => r.status === 200,
  });
}
