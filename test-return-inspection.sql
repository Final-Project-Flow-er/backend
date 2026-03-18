-- ==============================================
-- 반품 검수 테스트 데이터 DML
-- ==============================================
drop table chaing.hqinventory, chaing.return_item, chaing.returns, chaing.franchise_order_item, chaing.franchise_order
-- 1. hq_inventory: 박스코드 동일 행 20개씩 2쌍 (총 40행)
--    - 쌍1: box_code = 'BOX-TEST-001', product_id = 1
--    - 쌍2: box_code = 'BOX-TEST-002', product_id = 2

INSERT INTO hqinventory (order_id, order_item_id, serial_code, product_id, manufacture_date, status, box_code, shipped_at, received_at, is_inspected, return_item_status, created_at, updated_at, version)
VALUES
-- 쌍1: BOX-TEST-001, product_id = 1
(1, 1, 'SER-001-01', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-02', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-03', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-04', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-05', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-06', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-07', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-08', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-09', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-10', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-11', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-12', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-13', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-14', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-15', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-16', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-17', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-18', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-19', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 1, 'SER-001-20', 1, '2026-01-15', 'RETURN_INBOUND', 'BOX-TEST-001', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
-- 쌍2: BOX-TEST-002, product_id = 2
(1, 2, 'SER-002-01', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-02', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-03', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-04', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-05', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-06', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-07', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-08', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-09', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-10', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-11', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-12', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-13', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-14', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-15', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-16', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-17', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-18', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-19', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0),
(1, 2, 'SER-002-20', 2, '2026-01-20', 'RETURN_INBOUND', 'BOX-TEST-002', '2026-03-10 09:00:00', '2026-03-11 10:00:00', false, NULL, NOW(), NOW(), 0);


-- 2. franchise_order: product_id 1, 2 (hq_inventory에 있는 것) + product_id 3 (없는 것)
--    총 수량 = 10 + 10 + 10 = 30, 단가 5000원 기준

INSERT INTO franchise_order (franchise_id, order_code, user_id, address, requirement, order_status, total_quantity, total_amount, delivery_date, delivery_time, cancelled_reason, created_at, updated_at, version)
VALUES (1, 'FO-TEST-001', 1, '서울시 강남구 테헤란로 123', '반품 검수 테스트용', 'COMPLETED', 30, 150000.00, '2026-03-15 09:00:00', '09:00', NULL, NOW(), NOW(), 0);

SET @fo_id = LAST_INSERT_ID();

-- 3. franchise_order_item: 3개 항목 (product_id 1, 2, 3), 각 10개씩

INSERT INTO franchise_order_item (franchise_order_id, product_id, quantity, unit_price, total_price, created_at, updated_at)
VALUES
(@fo_id, 1, 10, 5000.00, 50000.00, NOW(), NOW()),
(@fo_id, 2, 10, 5000.00, 50000.00, NOW(), NOW()),
(@fo_id, 3, 10, 5000.00, 50000.00, NOW(), NOW());

SET @foi_1 = LAST_INSERT_ID();       -- product_id = 1
SET @foi_2 = @foi_1 + 1;             -- product_id = 2
SET @foi_3 = @foi_1 + 2;             -- product_id = 3


-- 4. returns: franchise_order 참조, 반품 항목 2개 (product_id 1, 3)
--    총 반품 수량 = 10 + 10 = 20

INSERT INTO returns (franchise_id, franchise_order_id, return_code, user_id, return_type, description, total_return_quantity, total_return_amount, return_status, created_at, updated_at, version)
VALUES (1, @fo_id, 'RT-TEST-001', 1, 'PRODUCT_DEFECT', '상품 하자로 인한 반품', 20, 100000.00, 'INSPECTING', NOW(), NOW(), 0);

SET @ret_id = LAST_INSERT_ID();


-- 5. return_item: franchise_order_item 중 2개만 참조 (product_id 1, 3)
--    box_code는 각각 고유값

INSERT INTO return_item (return_id, franchise_order_item_id, box_code, return_item_status, created_at, updated_at)
VALUES
(@ret_id, @foi_1, 'RET-BOX-001', 'BEFORE_INSPECTION', NOW(), NOW()),
(@ret_id, @foi_3, 'RET-BOX-002', 'BEFORE_INSPECTION', NOW(), NOW());