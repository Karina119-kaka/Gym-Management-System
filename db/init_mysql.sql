-- =====================================================================
-- 健身房管理系统（SpringBoot + Vue）数据库初始化脚本
-- 数据库: gym_management_system  MySQL 8.0+
-- 说明: 脚本会【删除并重建】gym_management_system 库，仅用于开发环境
-- 使用方法: mysql -uroot -p < init_mysql.sql
-- 默认账号（密码均为 123456）:
--   管理员 admin / 教练 coach001 / 会员 member001
-- =====================================================================

DROP DATABASE IF EXISTS gym_management_system;
CREATE DATABASE gym_management_system
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE gym_management_system;

-- ---------------------------------------------------------------------
-- 1. 管理员表
-- ---------------------------------------------------------------------
CREATE TABLE admin
(
    Admin_id   INT AUTO_INCREMENT COMMENT 'ID' PRIMARY KEY,
    Admin_no   VARCHAR(10) NOT NULL COMMENT '管理员账号',
    Admin_name VARCHAR(8)  NULL COMMENT '姓名',
    Admin_sex  CHAR        NULL COMMENT '性别',
    Admin_tel  VARCHAR(11) NULL COMMENT '联系电话',
    Admin_pwd  VARCHAR(20) NOT NULL COMMENT '登录密码',
    CONSTRAINT uk_admin_no UNIQUE (Admin_no),
    CONSTRAINT uk_admin_tel UNIQUE (Admin_tel)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='管理员表';

-- ---------------------------------------------------------------------
-- 2. 教练表
-- ---------------------------------------------------------------------
CREATE TABLE coach
(
    Coach_id     INT AUTO_INCREMENT COMMENT 'ID' PRIMARY KEY,
    Coach_no     VARCHAR(10) NOT NULL COMMENT '教练工号',
    Coach_name   VARCHAR(8)  NULL COMMENT '姓名',
    Coach_sex    CHAR        NULL COMMENT '性别',
    Coach_age    INT         NULL COMMENT '年龄',
    Coach_tel    VARCHAR(11) NULL COMMENT '联系电话',
    Good_at      VARCHAR(50) NULL COMMENT '擅长课程',
    Coach_status VARCHAR(10) NULL COMMENT '账号状态(启用/禁用)',
    Create_time  DATETIME    NULL COMMENT '入职时间',
    Coach_pwd    VARCHAR(20) NOT NULL COMMENT '登录密码',
    CONSTRAINT uk_coach_no UNIQUE (Coach_no),
    CONSTRAINT uk_coach_tel UNIQUE (Coach_tel)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='教练表';

-- ---------------------------------------------------------------------
-- 3. 会员表
-- ---------------------------------------------------------------------
CREATE TABLE member
(
    Member_id   INT AUTO_INCREMENT COMMENT 'ID' PRIMARY KEY,
    Member_no   VARCHAR(10) NOT NULL COMMENT '会员账号',
    Member_name VARCHAR(8)  NULL COMMENT '姓名',
    Member_sex  CHAR        NULL COMMENT '性别',
    Member_age  INT         NULL COMMENT '年龄',
    Member_tel  VARCHAR(11) NULL COMMENT '联系电话',
    Card_status VARCHAR(10) NULL COMMENT '会员卡状态(正常/禁用)',
    Create_time DATETIME    NULL COMMENT '注册时间',
    Member_pwd  VARCHAR(20) NOT NULL COMMENT '登录密码',
    CONSTRAINT uk_member_no UNIQUE (Member_no),
    CONSTRAINT uk_member_tel UNIQUE (Member_tel)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='会员表';

-- ---------------------------------------------------------------------
-- 4. 课程表
-- ---------------------------------------------------------------------
CREATE TABLE course
(
    Course_id     INT AUTO_INCREMENT COMMENT '课程ID' PRIMARY KEY,
    Course_name   VARCHAR(30)    NOT NULL COMMENT '课程名称',
    Coach_id      INT            NULL COMMENT '教练ID',
    Course_price  DECIMAL(10, 2) NOT NULL COMMENT '课程价格',
    Course_intro  TEXT           NULL COMMENT '课程描述',
    sale_status   VARCHAR(10)    NULL COMMENT '销售状态(待审核/上架/下架)',
    Create_time   DATETIME       NULL COMMENT '发布时间',
    course_detail TEXT           NULL COMMENT '课程详细描述',
    CONSTRAINT fk_course_coach FOREIGN KEY (Coach_id) REFERENCES coach (Coach_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='课程表';

CREATE INDEX idx_course_coach ON course (Coach_id);

-- ---------------------------------------------------------------------
-- 5. 会员课程购买记录表
-- ---------------------------------------------------------------------
CREATE TABLE course_buy
(
    Buy_id     INT AUTO_INCREMENT COMMENT '购买ID' PRIMARY KEY,
    Member_id  INT            NULL COMMENT '会员ID',
    Coach_id   INT            NULL COMMENT '教练ID',
    Course_id  INT            NULL COMMENT '课程ID',
    Pay_price  DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    Pay_status VARCHAR(10)    NOT NULL COMMENT '支付状态(已购买)',
    Buy_time   DATETIME       NULL COMMENT '购买时间',
    CONSTRAINT fk_buy_member FOREIGN KEY (Member_id) REFERENCES member (Member_id),
    CONSTRAINT fk_buy_coach FOREIGN KEY (Coach_id) REFERENCES coach (Coach_id),
    CONSTRAINT fk_buy_course FOREIGN KEY (Course_id) REFERENCES course (Course_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='课程购买表';

CREATE INDEX idx_buy_member ON course_buy (Member_id);
CREATE INDEX idx_buy_coach ON course_buy (Coach_id);
CREATE INDEX idx_buy_course ON course_buy (Course_id);

-- ---------------------------------------------------------------------
-- 6. 课程预约表
-- ---------------------------------------------------------------------
CREATE TABLE course_order
(
    Order_id     INT AUTO_INCREMENT COMMENT '预约ID' PRIMARY KEY,
    Member_id    INT         NULL COMMENT '会员ID',
    Coach_id     INT         NULL COMMENT '教练ID',
    Course_id    INT         NULL COMMENT '课程ID',
    Buy_id       INT         NULL COMMENT '购买ID',
    Order_time   DATETIME    NOT NULL COMMENT '预约上课时间',
    Order_status VARCHAR(10) NOT NULL COMMENT '预约状态(待教练查看/预约成功/已拒绝/已到店/未到店/已失效)',
    CONSTRAINT fk_order_member FOREIGN KEY (Member_id) REFERENCES member (Member_id),
    CONSTRAINT fk_order_coach FOREIGN KEY (Coach_id) REFERENCES coach (Coach_id),
    CONSTRAINT fk_order_course FOREIGN KEY (Course_id) REFERENCES course (Course_id),
    CONSTRAINT fk_order_buy FOREIGN KEY (Buy_id) REFERENCES course_buy (Buy_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='课程预约表';

CREATE INDEX idx_order_member ON course_order (Member_id);
CREATE INDEX idx_order_coach ON course_order (Coach_id);
CREATE INDEX idx_order_course ON course_order (Course_id);
CREATE INDEX idx_order_buy ON course_order (Buy_id);

-- ---------------------------------------------------------------------
-- 7. 健身打卡表（含体重/体脂/BMI/卡路里等身体数据）
-- ---------------------------------------------------------------------
CREATE TABLE clock_in
(
    Clock_id     INT AUTO_INCREMENT COMMENT '打卡ID' PRIMARY KEY,
    Member_id    INT          NULL COMMENT '会员ID',
    Clock_time   DATETIME     NOT NULL COMMENT '打卡时间',
    weight_kg    DECIMAL(5, 2) NULL COMMENT '体重kg',
    body_fat_pct DECIMAL(5, 2) NULL COMMENT '体脂%',
    bmi          DECIMAL(5, 2) NULL COMMENT 'BMI',
    calories     INT          DEFAULT 0 NULL COMMENT '本次消耗卡路里',
    data_source  VARCHAR(20)  DEFAULT 'manual' NULL COMMENT 'manual/device',
    CONSTRAINT fk_clock_member FOREIGN KEY (Member_id) REFERENCES member (Member_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='健身打卡表';

CREATE INDEX idx_clock_member ON clock_in (Member_id);

-- ---------------------------------------------------------------------
-- 8. 教练预约消息已读记录表
-- ---------------------------------------------------------------------
CREATE TABLE coach_order_notice_read
(
    id        INT AUTO_INCREMENT PRIMARY KEY,
    coach_id  INT                                NOT NULL,
    order_id  INT                                NOT NULL,
    read_time DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_coach_order UNIQUE (coach_id, order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='教练预约消息已读记录';

CREATE INDEX idx_notice_coach ON coach_order_notice_read (coach_id);
CREATE INDEX idx_notice_order ON coach_order_notice_read (order_id);

-- =====================================================================
-- 演示数据（密码统一为 123456）
-- =====================================================================

-- 管理员
INSERT INTO admin (Admin_no, Admin_name, Admin_sex, Admin_tel, Admin_pwd) VALUES
('admin', '系统管理员', '男', '13800000001', '123456');

-- 教练
INSERT INTO coach (Coach_no, Coach_name, Coach_sex, Coach_age, Coach_tel, Good_at, Coach_status, Create_time, Coach_pwd) VALUES
('coach001', '张伟', '男', 32, '13800000002', '减脂塑形', '启用', NOW(), '123456'),
('coach002', '李娜', '女', 29, '13800000003', '瑜伽普拉提', '启用', NOW(), '123456'),
('coach003', '王强', '男', 35, '13800000004', '增肌力量', '启用', NOW(), '123456');

-- 会员
INSERT INTO member (Member_no, Member_name, Member_sex, Member_age, Member_tel, Card_status, Create_time, Member_pwd) VALUES
('member001', '陈晨', '男', 26, '13900000001', '正常', NOW(), '123456'),
('member002', '刘雨', '女', 24, '13900000002', '正常', NOW(), '123456'),
('member003', '赵磊', '男', 28, '13900000003', '正常', NOW(), '123456');

-- 课程（3 门已上架 + 1 门待审核，供管理端审核演示）
INSERT INTO course (Course_name, Coach_id, Course_price, Course_intro, sale_status, Create_time, course_detail)
SELECT '减脂塑形私教', c.Coach_id, 399.00, '一对一减脂塑形训练，含体测分析与饮食指导。',
       '上架', DATE_SUB(NOW(), INTERVAL 20 DAY),
       '针对个人体质制定减脂方案，包含力量训练、有氧训练与膳食建议，每节课 60 分钟。'
FROM coach c WHERE c.Coach_no = 'coach001';

INSERT INTO course (Course_name, Coach_id, Course_price, Course_intro, sale_status, Create_time, course_detail)
SELECT '哈他瑜伽课程', c.Coach_id, 199.00, '舒缓身心的哈他瑜伽，改善体态与柔韧性。',
       '上架', DATE_SUB(NOW(), INTERVAL 15 DAY),
       '从呼吸与基础体式入手，适合久坐人群，帮助放松肌肉、改善体态。'
FROM coach c WHERE c.Coach_no = 'coach002';

INSERT INTO course (Course_name, Coach_id, Course_price, Course_intro, sale_status, Create_time, course_detail)
SELECT '增肌力量训练', c.Coach_id, 499.00, '系统力量训练，帮助增肌与提升力量水平。',
       '上架', DATE_SUB(NOW(), INTERVAL 10 DAY),
       '围绕深蹲、硬拉、卧推等复合动作构建训练计划，每周三次系统进阶。'
FROM coach c WHERE c.Coach_no = 'coach003';

INSERT INTO course (Course_name, Coach_id, Course_price, Course_intro, sale_status, Create_time, course_detail)
SELECT '核心体能训练', c.Coach_id, 299.00, '核心力量与体能综合训练课程。',
       '待审核', DATE_SUB(NOW(), INTERVAL 1 DAY),
       '针对核心肌群与心肺耐力的综合训练，适合希望全面提升体能的人群。'
FROM coach c WHERE c.Coach_no = 'coach001';

-- 会员购买记录（用于购买->预约流程演示）
INSERT INTO course_buy (Member_id, Coach_id, Course_id, Pay_price, Pay_status, Buy_time)
SELECT m.Member_id, c.Coach_id, c.Course_id, c.Course_price, '已购买', DATE_SUB(NOW(), INTERVAL 3 DAY)
FROM member m, course c
WHERE m.Member_no = 'member001' AND c.Course_name = '减脂塑形私教';

INSERT INTO course_buy (Member_id, Coach_id, Course_id, Pay_price, Pay_status, Buy_time)
SELECT m.Member_id, c.Coach_id, c.Course_id, c.Course_price, '已购买', DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM member m, course c
WHERE m.Member_no = 'member001' AND c.Course_name = '减脂塑形私教';

INSERT INTO course_buy (Member_id, Coach_id, Course_id, Pay_price, Pay_status, Buy_time)
SELECT m.Member_id, c.Coach_id, c.Course_id, c.Course_price, '已购买', DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM member m, course c
WHERE m.Member_no = 'member002' AND c.Course_name = '哈他瑜伽课程';

INSERT INTO course_buy (Member_id, Coach_id, Course_id, Pay_price, Pay_status, Buy_time)
SELECT m.Member_id, c.Coach_id, c.Course_id, c.Course_price, '已购买', DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM member m, course c
WHERE m.Member_no = 'member001' AND c.Course_name = '哈他瑜伽课程';

INSERT INTO course_buy (Member_id, Coach_id, Course_id, Pay_price, Pay_status, Buy_time)
SELECT m.Member_id, c.Coach_id, c.Course_id, c.Course_price, '已购买', DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM member m, course c
WHERE m.Member_no = 'member003' AND c.Course_name = '增肌力量训练';

INSERT INTO course_buy (Member_id, Coach_id, Course_id, Pay_price, Pay_status, Buy_time)
SELECT m.Member_id, c.Coach_id, c.Course_id, c.Course_price, '已购买', DATE_SUB(NOW(), INTERVAL 4 HOUR)
FROM member m, course c
WHERE m.Member_no = 'member002' AND c.Course_name = '减脂塑形私教';

-- 预约记录（覆盖 已到店/预约成功/待教练查看 多种状态，便于三角色首页与考勤演示）
-- 会员1 今天稍后的一节已确认课程（教练首页“今日课表/下一节课”演示）
INSERT INTO course_order (Member_id, Coach_id, Course_id, Buy_id, Order_time, Order_status)
SELECT b.Member_id, b.Coach_id, b.Course_id, b.Buy_id, DATE_ADD(NOW(), INTERVAL 2 HOUR), '预约成功'
FROM course_buy b
WHERE b.Member_id = (SELECT Member_id FROM member WHERE Member_no = 'member001')
  AND b.Course_id = (SELECT Course_id FROM course WHERE Course_name = '减脂塑形私教')
  AND b.Buy_time = DATE_SUB(NOW(), INTERVAL 3 DAY);

-- 会员1 今天稍后的一条待教练查看预约（教练端未读消息演示）
INSERT INTO course_order (Member_id, Coach_id, Course_id, Buy_id, Order_time, Order_status)
SELECT b.Member_id, b.Coach_id, b.Course_id, b.Buy_id, DATE_ADD(NOW(), INTERVAL 3 HOUR), '待教练查看'
FROM course_buy b
WHERE b.Member_id = (SELECT Member_id FROM member WHERE Member_no = 'member001')
  AND b.Course_id = (SELECT Course_id FROM course WHERE Course_name = '减脂塑形私教')
  AND b.Buy_time = DATE_SUB(NOW(), INTERVAL 1 DAY);

-- 会员2 明天上午的已确认课程（明日课表演示）
INSERT INTO course_order (Member_id, Coach_id, Course_id, Buy_id, Order_time, Order_status)
SELECT b.Member_id, b.Coach_id, b.Course_id, b.Buy_id,
       DATE_ADD(DATE_ADD(CURDATE(), INTERVAL 1 DAY), INTERVAL 10 HOUR), '预约成功'
FROM course_buy b
WHERE b.Member_id = (SELECT Member_id FROM member WHERE Member_no = 'member002')
  AND b.Course_id = (SELECT Course_id FROM course WHERE Course_name = '哈他瑜伽课程')
  AND b.Buy_time = DATE_SUB(NOW(), INTERVAL 2 DAY);

-- 会员3 前天的一节已到店课程（考勤记录演示）
INSERT INTO course_order (Member_id, Coach_id, Course_id, Buy_id, Order_time, Order_status)
SELECT b.Member_id, b.Coach_id, b.Course_id, b.Buy_id, DATE_SUB(NOW(), INTERVAL 2 DAY), '已到店'
FROM course_buy b
WHERE b.Member_id = (SELECT Member_id FROM member WHERE Member_no = 'member003')
  AND b.Course_id = (SELECT Course_id FROM course WHERE Course_name = '增肌力量训练')
  AND b.Buy_time = DATE_SUB(NOW(), INTERVAL 1 DAY);

-- 健身打卡数据（会员1 历史 3 次打卡，供会员端数据看板演示；今日未打卡可体验打卡）
INSERT INTO clock_in (Member_id, Clock_time, weight_kg, body_fat_pct, bmi, calories, data_source)
SELECT Member_id, DATE_SUB(NOW(), INTERVAL 10 DAY), 76.20, 22.50, 24.90, 520, 'manual'
FROM member WHERE Member_no = 'member001';

INSERT INTO clock_in (Member_id, Clock_time, weight_kg, body_fat_pct, bmi, calories, data_source)
SELECT Member_id, DATE_SUB(NOW(), INTERVAL 5 DAY), 75.10, 21.80, 24.60, 560, 'device'
FROM member WHERE Member_no = 'member001';

INSERT INTO clock_in (Member_id, Clock_time, weight_kg, body_fat_pct, bmi, calories, data_source)
SELECT Member_id, DATE_SUB(NOW(), INTERVAL 1 DAY), 74.80, 21.40, 24.50, 530, 'manual'
FROM member WHERE Member_no = 'member001';
