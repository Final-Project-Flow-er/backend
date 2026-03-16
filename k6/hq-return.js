import http from "k6/http";
import { sleep } from "k6";
import { Trend } from "k6/metrics";
import { BASE_URL, HQ_USER, DEFAULT_OPTIONS } from "./config.js";
import { login, authHeaders, checkOk } from "./helpers.js";

const getAllReturnsDuration = new Trend("get_all_hq_returns_duration", true);
const getReturnDetailDuration = new Trend(
  "get_hq_return_detail_duration",
  true
);

export const options = {
  ...DEFAULT_OPTIONS,
  thresholds: {
    ...DEFAULT_OPTIONS.thresholds,
    get_all_hq_returns_duration: ["p(95)<300"],
    get_hq_return_detail_duration: ["p(95)<300"],
  },
};

export function setup() {
  const token = login(HQ_USER.loginId, HQ_USER.password);

  const res = http.get(
    `${BASE_URL}/api/v1/hq/returns?isAll=true`,
    authHeaders(token)
  );
  const returns = res.json().data;
  const returnCode =
    returns && returns.length > 0 ? returns[0].returnCode : null;

  return { token, returnCode };
}

export default function (data) {
  const params = authHeaders(data.token);

  // 1) 반품 전체 조회
  const allRes = http.get(
    `${BASE_URL}/api/v1/hq/returns?isAll=true`,
    params
  );
  checkOk(allRes, "getAllHQReturns");
  getAllReturnsDuration.add(allRes.timings.duration);

  // 2) 대기 반품만 조회
  const pendingRes = http.get(
    `${BASE_URL}/api/v1/hq/returns?isAll=false`,
    params
  );
  checkOk(pendingRes, "getPendingHQReturns");
  getAllReturnsDuration.add(pendingRes.timings.duration);

  // 3) 반품 상세 조회
  if (data.returnCode) {
    const detailRes = http.get(
      `${BASE_URL}/api/v1/hq/returns/${data.returnCode}`,
      params
    );
    checkOk(detailRes, "getHQReturnDetail");
    getReturnDetailDuration.add(detailRes.timings.duration);
  }

  sleep(0.5);
}
