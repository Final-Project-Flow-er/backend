-- ==============================================
-- Sales / SalesItem 테스트 더미 데이터 DML
-- ==============================================
-- 전제: franchise_id = 1 인 가맹점 존재
--       product 테이블에 product_id 1~5 존재

-- ── 판매 1: 상품 2종, 총 5개 ──
INSERT INTO sales (franchise_id, sales_code, quantity, total_amount, is_canceled, created_at, updated_at)
VALUES (1, 'SL-20260301-001', 5, 75000.00, false, '2026-03-01 10:30:00', '2026-03-01 10:30:00');

SET @sales_1 = LAST_INSERT_ID();

INSERT INTO sales_item (sales_id, product_id, quantity, product_code, product_name, lot, unit_price, created_at, updated_at)
VALUES
(@sales_1, 1, 3, 'PRD-001', '아메리카노 원두 1kg', 'SE01-F01-L01-PRD001-BOX001', 15000.00, '2026-03-01 10:30:00', '2026-03-01 10:30:00'),
(@sales_1, 2, 2, 'PRD-002', '바닐라 시럽 500ml', 'SE01-F01-L02-PRD002-BOX001', 15000.00, '2026-03-01 10:30:00', '2026-03-01 10:30:00');


-- ── 판매 2: 상품 3종, 총 8개 ──
INSERT INTO sales (franchise_id, sales_code, quantity, total_amount, is_canceled, created_at, updated_at)
VALUES (1, 'SL-20260305-001', 8, 104000.00, false, '2026-03-05 14:15:00', '2026-03-05 14:15:00');

SET @sales_2 = LAST_INSERT_ID();

INSERT INTO sales_item (sales_id, product_id, quantity, product_code, product_name, lot, unit_price, created_at, updated_at)
VALUES
(@sales_2, 1, 2, 'PRD-001', '아메리카노 원두 1kg', 'SE01-F01-L01-PRD001-BOX002', 15000.00, '2026-03-05 14:15:00', '2026-03-05 14:15:00'),
(@sales_2, 3, 4, 'PRD-003', '카라멜 소스 1L', 'SE01-F01-L03-PRD003-BOX001', 12000.00, '2026-03-05 14:15:00', '2026-03-05 14:15:00'),
(@sales_2, 4, 2, 'PRD-004', '우유 1L', 'SE01-F01-L04-PRD004-BOX001', 7000.00, '2026-03-05 14:15:00', '2026-03-05 14:15:00');


-- ── 판매 3: 상품 1종, 총 10개 ──
INSERT INTO sales (franchise_id, sales_code, quantity, total_amount, is_canceled, created_at, updated_at)
VALUES (1, 'SL-20260310-001', 10, 50000.00, false, '2026-03-10 09:00:00', '2026-03-10 09:00:00');

SET @sales_3 = LAST_INSERT_ID();

INSERT INTO sales_item (sales_id, product_id, quantity, product_code, product_name, lot, unit_price, created_at, updated_at)
VALUES
(@sales_3, 5, 5, 'PRD-005', '녹차 파우더 500g', 'SE01-F01-L05-PRD005-BOX001', 5000.00, '2026-03-10 09:00:00', '2026-03-10 09:00:00'),
(@sales_3, 5, 5, 'PRD-005', '녹차 파우더 500g', 'SE01-F01-L05-PRD005-BOX002', 5000.00, '2026-03-10 09:00:00', '2026-03-10 09:00:00');


-- ── 판매 4: 상품 2종, 총 6개 ──
INSERT INTO sales (franchise_id, sales_code, quantity, total_amount, is_canceled, created_at, updated_at)
VALUES (1, 'SL-20260312-001', 6, 72000.00, false, '2026-03-12 11:45:00', '2026-03-12 11:45:00');

SET @sales_4 = LAST_INSERT_ID();

INSERT INTO sales_item (sales_id, product_id, quantity, product_code, product_name, lot, unit_price, created_at, updated_at)
VALUES
(@sales_4, 2, 3, 'PRD-002', '바닐라 시럽 500ml', 'SE01-F01-L02-PRD002-BOX002', 15000.00, '2026-03-12 11:45:00', '2026-03-12 11:45:00'),
(@sales_4, 3, 3, 'PRD-003', '카라멜 소스 1L', 'SE01-F01-L03-PRD003-BOX002', 12000.00, '2026-03-12 11:45:00', '2026-03-12 11:45:00');


-- ── 판매 5: 취소된 판매, 상품 1종, 총 2개 ──
INSERT INTO sales (franchise_id, sales_code, quantity, total_amount, is_canceled, created_at, updated_at)
VALUES (1, 'SL-20260315-001', 2, 30000.00, true, '2026-03-15 16:20:00', '2026-03-15 16:25:00');

SET @sales_5 = LAST_INSERT_ID();

INSERT INTO sales_item (sales_id, product_id, quantity, product_code, product_name, lot, unit_price, created_at, updated_at)
VALUES
(@sales_5, 1, 2, 'PRD-001', '아메리카노 원두 1kg', 'SE01-F01-L01-PRD001-BOX003', 15000.00, '2026-03-15 16:20:00', '2026-03-15 16:20:00');


-- ── 판매 6: 상품 4종, 총 12개 ──
INSERT INTO sales (franchise_id, sales_code, quantity, total_amount, is_canceled, created_at, updated_at)
VALUES (1, 'SL-20260318-001', 12, 145000.00, false, '2026-03-18 13:00:00', '2026-03-18 13:00:00');

SET @sales_6 = LAST_INSERT_ID();

INSERT INTO sales_item (sales_id, product_id, quantity, product_code, product_name, lot, unit_price, created_at, updated_at)
VALUES
(@sales_6, 1, 3, 'PRD-001', '아메리카노 원두 1kg', 'SE01-F01-L01-PRD001-BOX004', 15000.00, '2026-03-18 13:00:00', '2026-03-18 13:00:00'),
(@sales_6, 2, 3, 'PRD-002', '바닐라 시럽 500ml', 'SE01-F01-L02-PRD002-BOX003', 15000.00, '2026-03-18 13:00:00', '2026-03-18 13:00:00'),
(@sales_6, 3, 3, 'PRD-003', '카라멜 소스 1L', 'SE01-F01-L03-PRD003-BOX003', 12000.00, '2026-03-18 13:00:00', '2026-03-18 13:00:00'),
(@sales_6, 4, 3, 'PRD-004', '우유 1L', 'SE01-F01-L04-PRD004-BOX002', 7000.00, '2026-03-18 13:00:00', '2026-03-18 13:00:00');