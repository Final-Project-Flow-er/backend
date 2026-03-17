import http from "k6/http";
import { sleep } from "k6";
import { Trend } from "k6/metrics";
import { BASE_URL, FRANCHISE_USER, DEFAULT_OPTIONS } from "./config.js";
import { login, authHeaders, checkOk } from "./helpers.js";

// 캐시 히트 응답만 측정
const cacheHitDuration = new Trend("cache_hit_duration", true);

export const options = {
  ...DEFAULT_OPTIONS,
  thresholds: {
    ...DEFAULT_OPTIONS.thresholds,
    cache_hit_duration: ["p(95)<300"],
  },
};

export function setup() {
  const token = login(FRANCHISE_USER.loginId, FRANCHISE_USER.password);
  const params = authHeaders(token);

  // 캐시 워밍업: 첫 요청으로 캐시 적재
  http.get(`${BASE_URL}/api/v1/franchise/orders`, params);

  return { token };
}

export default function (data) {
  const params = authHeaders(data.token);

  // 캐시 히트 요청만 측정
  const res = http.get(`${BASE_URL}/api/v1/franchise/orders`, params);
  checkOk(res, "cache_hit");
  cacheHitDuration.add(res.timings.duration);

  sleep(0.5);
}
