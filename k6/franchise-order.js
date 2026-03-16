import http from "k6/http";
import { sleep } from "k6";
import { Trend } from "k6/metrics";
import { BASE_URL, FRANCHISE_USER, DEFAULT_OPTIONS } from "./config.js";
import { login, authHeaders, checkOk } from "./helpers.js";

// 커스텀 메트릭
const getAllOrdersDuration = new Trend("get_all_orders_duration", true);
const getOrderDetailDuration = new Trend("get_order_detail_duration", true);

export const options = {
  ...DEFAULT_OPTIONS,
  thresholds: {
    ...DEFAULT_OPTIONS.thresholds,
    get_all_orders_duration: ["p(95)<300"],
    get_order_detail_duration: ["p(95)<300"],
  },
};

let token;

export function setup() {
  token = login(FRANCHISE_USER.loginId, FRANCHISE_USER.password);

  // 첫 조회로 orderCode 하나 확보
  const res = http.get(
    `${BASE_URL}/api/v1/franchise/orders`,
    authHeaders(token)
  );
  const orders = res.json().data;
  const orderCode = orders && orders.length > 0 ? orders[0].orderCode : null;

  return { token, orderCode };
}

export default function (data) {
  const params = authHeaders(data.token);

  // 1) 전체 발주 조회
  const allRes = http.get(`${BASE_URL}/api/v1/franchise/orders`, params);
  checkOk(allRes, "getAllOrders");
  getAllOrdersDuration.add(allRes.timings.duration);

  // 2) 상세 발주 조회
  if (data.orderCode) {
    const detailRes = http.get(
      `${BASE_URL}/api/v1/franchise/orders/${data.orderCode}`,
      params
    );
    checkOk(detailRes, "getOrderDetail");
    getOrderDetailDuration.add(detailRes.timings.duration);
  }

  sleep(0.5);
}
