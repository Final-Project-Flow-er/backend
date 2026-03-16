import http from "k6/http";
import { sleep } from "k6";
import { Trend } from "k6/metrics";
import { BASE_URL, FRANCHISE_USER, DEFAULT_OPTIONS } from "./config.js";
import { login, authHeaders, checkOk } from "./helpers.js";

const getAllReturnsDuration = new Trend("get_all_returns_duration", true);
const getReturnDetailDuration = new Trend("get_return_detail_duration", true);
const getTargetsDuration = new Trend("get_targets_duration", true);

export const options = {
  ...DEFAULT_OPTIONS,
  thresholds: {
    ...DEFAULT_OPTIONS.thresholds,
    get_all_returns_duration: ["p(95)<300"],
    get_return_detail_duration: ["p(95)<300"],
    get_targets_duration: ["p(95)<300"],
  },
};

export function setup() {
  const token = login(FRANCHISE_USER.loginId, FRANCHISE_USER.password);

  const res = http.get(
    `${BASE_URL}/api/v1/franchise/returns`,
    authHeaders(token)
  );
  const returns = res.json().data;
  const returnCode =
    returns && returns.length > 0 ? returns[0].returnCode : null;

  return { token, returnCode };
}

export default function (data) {
  const params = authHeaders(data.token);

  // 1) 전체 반품 조회
  const allRes = http.get(`${BASE_URL}/api/v1/franchise/returns`, params);
  checkOk(allRes, "getAllReturns");
  getAllReturnsDuration.add(allRes.timings.duration);

  // 2) 반품 상세 조회
  if (data.returnCode) {
    const detailRes = http.get(
      `${BASE_URL}/api/v1/franchise/returns/${data.returnCode}`,
      params
    );
    checkOk(detailRes, "getReturnDetail");
    getReturnDetailDuration.add(detailRes.timings.duration);
  }

  // 3) 반품 대상 조회
  const targetRes = http.get(
    `${BASE_URL}/api/v1/franchise/returns/target`,
    params
  );
  checkOk(targetRes, "getTargets");
  getTargetsDuration.add(targetRes.timings.duration);

  sleep(0.5);
}
