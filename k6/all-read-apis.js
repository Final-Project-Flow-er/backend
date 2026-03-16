import http from "k6/http";
import { sleep, group } from "k6";
import { Trend } from "k6/metrics";
import { BASE_URL, FRANCHISE_USER, HQ_USER, DEFAULT_OPTIONS } from "./config.js";
import { login, authHeaders, checkOk } from "./helpers.js";

// 커스텀 메트릭 — Facade 단위
const frOrderDuration = new Trend("franchise_order_read", true);
const frReturnDuration = new Trend("franchise_return_read", true);
const frSalesDuration = new Trend("franchise_sales_read", true);
const hqOrderDuration = new Trend("hq_order_read", true);
const hqReturnDuration = new Trend("hq_return_read", true);

export const options = {
  ...DEFAULT_OPTIONS,
  thresholds: {
    ...DEFAULT_OPTIONS.thresholds,
    franchise_order_read: ["p(95)<300"],
    franchise_return_read: ["p(95)<300"],
    franchise_sales_read: ["p(95)<300"],
    hq_order_read: ["p(95)<300"],
    hq_return_read: ["p(95)<300"],
  },
};

export function setup() {
  const frToken = login(FRANCHISE_USER.loginId, FRANCHISE_USER.password);
  const hqToken = login(HQ_USER.loginId, HQ_USER.password);
  return { frToken, hqToken };
}

export default function (data) {
  const frParams = authHeaders(data.frToken);
  const hqParams = authHeaders(data.hqToken);

  group("Franchise Orders", () => {
    const res = http.get(`${BASE_URL}/api/v1/franchise/orders`, frParams);
    checkOk(res, "fr:orders");
    frOrderDuration.add(res.timings.duration);
  });

  group("Franchise Returns", () => {
    const res = http.get(`${BASE_URL}/api/v1/franchise/returns`, frParams);
    checkOk(res, "fr:returns");
    frReturnDuration.add(res.timings.duration);
  });

  group("Franchise Sales", () => {
    const res = http.get(`${BASE_URL}/api/v1/franchise/sales`, frParams);
    checkOk(res, "fr:sales");
    frSalesDuration.add(res.timings.duration);
  });

  group("HQ Orders", () => {
    const res = http.get(`${BASE_URL}/api/v1/hq/orders`, hqParams);
    checkOk(res, "hq:orders");
    hqOrderDuration.add(res.timings.duration);
  });

  group("HQ Returns", () => {
    const res = http.get(`${BASE_URL}/api/v1/hq/returns?isAll=true`, hqParams);
    checkOk(res, "hq:returns");
    hqReturnDuration.add(res.timings.duration);
  });

  sleep(0.5);
}
