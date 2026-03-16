import http from "k6/http";
import { sleep } from "k6";
import { Trend } from "k6/metrics";
import { BASE_URL, HQ_USER, DEFAULT_OPTIONS } from "./config.js";
import { login, authHeaders, checkOk } from "./helpers.js";

const getAllOrdersDuration = new Trend("get_all_hq_orders_duration", true);
const getOrderDetailDuration = new Trend(
  "get_hq_order_detail_duration",
  true
);
const getRequestedPendingDuration = new Trend(
  "get_requested_pending_duration",
  true
);
const getRequestedAllDuration = new Trend(
  "get_requested_all_duration",
  true
);

export const options = {
  ...DEFAULT_OPTIONS,
  thresholds: {
    ...DEFAULT_OPTIONS.thresholds,
    get_all_hq_orders_duration: ["p(95)<300"],
    get_hq_order_detail_duration: ["p(95)<300"],
    get_requested_pending_duration: ["p(95)<300"],
    get_requested_all_duration: ["p(95)<300"],
  },
};

export function setup() {
  const token = login(HQ_USER.loginId, HQ_USER.password);

  const res = http.get(`${BASE_URL}/api/v1/hq/orders`, authHeaders(token));
  const orders = res.json().data;
  const orderCode = orders && orders.length > 0 ? orders[0].orderCode : null;

  return { token, orderCode };
}

export default function (data) {
  const params = authHeaders(data.token);

  // 1) 본사 발주 전체 조회
  const allRes = http.get(`${BASE_URL}/api/v1/hq/orders`, params);
  checkOk(allRes, "getAllHQOrders");
  getAllOrdersDuration.add(allRes.timings.duration);

  // 2) 본사 발주 상세 조회
  if (data.orderCode) {
    const detailRes = http.get(
      `${BASE_URL}/api/v1/hq/orders/${data.orderCode}`,
      params
    );
    checkOk(detailRes, "getHQOrderDetail");
    getOrderDetailDuration.add(detailRes.timings.duration);
  }

  // 3) 가맹점 발주 요청 조회 (대기만)
  const pendingRes = http.get(
    `${BASE_URL}/api/v1/hq/orders/requested?isPending=true`,
    params
  );
  checkOk(pendingRes, "getRequestedPending");
  getRequestedPendingDuration.add(pendingRes.timings.duration);

  // 4) 가맹점 발주 요청 조회 (전체)
  const allReqRes = http.get(
    `${BASE_URL}/api/v1/hq/orders/requested?isPending=false`,
    params
  );
  checkOk(allReqRes, "getRequestedAll");
  getRequestedAllDuration.add(allReqRes.timings.duration);

  sleep(0.5);
}
