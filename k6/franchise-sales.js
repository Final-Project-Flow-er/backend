import http from "k6/http";
import { sleep } from "k6";
import { Trend } from "k6/metrics";
import { BASE_URL, FRANCHISE_USER, DEFAULT_OPTIONS } from "./config.js";
import { login, authHeaders, checkOk } from "./helpers.js";

const getAllSalesDuration = new Trend("get_all_sales_duration", true);
const getCanceledSalesDuration = new Trend("get_canceled_sales_duration", true);
const getSalesDetailDuration = new Trend("get_sales_detail_duration", true);

export const options = {
  ...DEFAULT_OPTIONS,
  thresholds: {
    ...DEFAULT_OPTIONS.thresholds,
    get_all_sales_duration: ["p(95)<300"],
    get_canceled_sales_duration: ["p(95)<300"],
    get_sales_detail_duration: ["p(95)<300"],
  },
};

export function setup() {
  const token = login(FRANCHISE_USER.loginId, FRANCHISE_USER.password);

  const res = http.get(
    `${BASE_URL}/api/v1/franchise/sales`,
    authHeaders(token)
  );
  const sales = res.json().data;
  const salesCode = sales && sales.length > 0 ? sales[0].salesCode : null;

  return { token, salesCode };
}

export default function (data) {
  const params = authHeaders(data.token);

  // 1) 미취소 판매 조회
  const allRes = http.get(`${BASE_URL}/api/v1/franchise/sales`, params);
  checkOk(allRes, "getAllSales");
  getAllSalesDuration.add(allRes.timings.duration);

  // 2) 취소 판매 조회
  const canceledRes = http.get(
    `${BASE_URL}/api/v1/franchise/sales/canceled`,
    params
  );
  checkOk(canceledRes, "getCanceledSales");
  getCanceledSalesDuration.add(canceledRes.timings.duration);

  // 3) 판매 상세 조회
  if (data.salesCode) {
    const detailRes = http.get(
      `${BASE_URL}/api/v1/franchise/sales/${data.salesCode}`,
      params
    );
    checkOk(detailRes, "getSalesDetail");
    getSalesDetailDuration.add(detailRes.timings.duration);
  }

  sleep(0.5);
}
