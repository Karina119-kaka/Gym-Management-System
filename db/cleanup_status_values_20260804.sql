USE gym_management_system;

START TRANSACTION;

-- course_order: normalize historical garbled and mixed status values
UPDATE course_order
SET order_status = '待教练查看'
WHERE order_status IN ('寰呮暀缁冩煡鐪?', '瀵板懏鏆€缂佸啯鐓￠惇?');

UPDATE course_order
SET order_status = '已预约'
WHERE order_status IN ('宸查绾?', '瀹告煡顣╃痪?');

UPDATE course_order
SET order_status = '预约成功'
WHERE order_status IN ('棰勭害鎴愬姛', '妫板嫮瀹抽幋鎰');

UPDATE course_order
SET order_status = '预约中'
WHERE order_status IN ('棰勭害涓?', '妫板嫮瀹虫稉?');

UPDATE course_order
SET order_status = '已拒绝'
WHERE order_status IN ('宸叉嫆缁?', '瀹稿弶瀚嗙紒?');

UPDATE course_order
SET order_status = '已失效'
WHERE order_status IN ('宸插け鏁?', '瀹告彃銇戦弫?');

UPDATE course_order
SET order_status = '已到店'
WHERE order_status IN ('宸插埌搴?', '瀹告彃鍩屾惔?');

UPDATE course_order
SET order_status = '未到店'
WHERE order_status IN ('鏈埌搴?', '閺堫亜鍩屾惔?');

-- course_buy: normalize payment status
UPDATE course_buy
SET pay_status = '已购买'
WHERE pay_status IN ('PAID', '宸茶喘涔?', '瀹歌尪鍠樻稊?');

-- course: normalize sale status
UPDATE course
SET sale_status = '上架'
WHERE sale_status IN ('ON_SALE', '涓婃灦');

UPDATE course
SET sale_status = '待审核'
WHERE sale_status IN ('PENDING', '寰呭鏍?', '待审批');

UPDATE course
SET sale_status = '下架'
WHERE sale_status IN ('OFF_SALE', '涓嬫灦');

UPDATE course
SET sale_status = '已拒绝'
WHERE sale_status IN ('宸叉嫆缁?', 'REJECTED');

-- member / coach account status
UPDATE member
SET card_status = '正常'
WHERE card_status IN ('NORMAL', 'ACTIVE');

UPDATE member
SET card_status = '禁用'
WHERE card_status IN ('DISABLED', 'INACTIVE');

UPDATE coach
SET coach_status = '启用'
WHERE coach_status IN ('ACTIVE', 'NORMAL');

UPDATE coach
SET coach_status = '禁用'
WHERE coach_status IN ('DISABLED', 'INACTIVE');

COMMIT;

-- verification queries
SELECT 'course_order' AS table_name, order_status AS status_value, COUNT(*) AS row_count
FROM course_order
GROUP BY order_status
ORDER BY row_count DESC, status_value;

SELECT 'course_buy' AS table_name, pay_status AS status_value, COUNT(*) AS row_count
FROM course_buy
GROUP BY pay_status
ORDER BY row_count DESC, status_value;

SELECT 'course' AS table_name, sale_status AS status_value, COUNT(*) AS row_count
FROM course
GROUP BY sale_status
ORDER BY row_count DESC, status_value;

SELECT 'member' AS table_name, card_status AS status_value, COUNT(*) AS row_count
FROM member
GROUP BY card_status
ORDER BY row_count DESC, status_value;

SELECT 'coach' AS table_name, coach_status AS status_value, COUNT(*) AS row_count
FROM coach
GROUP BY coach_status
ORDER BY row_count DESC, status_value;
