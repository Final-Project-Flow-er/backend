// k6 공통 설정
export const BASE_URL = __ENV.BASE_URL || "http://beyond21.iptime.org:7001";

// 테스트 계정 — 환경변수 또는 기본값
export const FRANCHISE_USER = {
  loginId: __ENV.FR_LOGIN_ID || "fr202603001",
  password: __ENV.FR_PASSWORD || "Qwer1234!",
};

export const HQ_USER = {
  loginId: __ENV.HQ_LOGIN_ID || "hq202603001",
  password: __ENV.HQ_PASSWORD || "Qwer1234!",
};

// 공통 부하 옵션
export const DEFAULT_OPTIONS = {
  stages: [
    { duration: "5s", target: 10 },
    { duration: "20s", target: 10 },
    { duration: "5s", target: 0 },
  ],
  thresholds: {
    http_req_duration: ["p(95)<500"],
  },
};
