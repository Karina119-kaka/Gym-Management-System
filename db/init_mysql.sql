CREATE DATABASE IF NOT EXISTS gym_management_system
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE gym_management_system;

CREATE TABLE IF NOT EXISTS admin (
  admin_id INT PRIMARY KEY AUTO_INCREMENT,
  admin_no VARCHAR(50) NOT NULL UNIQUE,
  admin_name VARCHAR(100) NOT NULL,
  admin_pwd VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS coach (
  coach_id INT PRIMARY KEY AUTO_INCREMENT,
  coach_no VARCHAR(50) NOT NULL UNIQUE,
  coach_name VARCHAR(100) NOT NULL,
  coach_sex VARCHAR(10),
  coach_age INT,
  coach_tel VARCHAR(30),
  good_at VARCHAR(255),
  coach_status VARCHAR(30),
  coach_pwd VARCHAR(100) NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS member (
  member_id INT PRIMARY KEY AUTO_INCREMENT,
  member_no VARCHAR(50) NOT NULL UNIQUE,
  member_name VARCHAR(100) NOT NULL,
  member_sex VARCHAR(10),
  member_age INT,
  member_tel VARCHAR(30),
  card_status VARCHAR(30),
  member_pwd VARCHAR(100) NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS course (
  course_id INT PRIMARY KEY AUTO_INCREMENT,
  course_name VARCHAR(100) NOT NULL,
  coach_id INT,
  course_price DECIMAL(10,2),
  course_intro VARCHAR(500),
  course_detail TEXT NULL,
  sale_status VARCHAR(30),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_course_coach FOREIGN KEY (coach_id) REFERENCES coach(coach_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS course_buy (
  buy_id INT PRIMARY KEY AUTO_INCREMENT,
  member_id INT,
  coach_id INT,
  course_id INT,
  pay_price DECIMAL(10,2),
  pay_status VARCHAR(30),
  buy_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS course_order (
  order_id INT PRIMARY KEY AUTO_INCREMENT,
  member_id INT,
  coach_id INT,
  course_id INT,
  buy_id INT,
  order_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  order_status VARCHAR(30)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS clock_in (
  clock_id INT PRIMARY KEY AUTO_INCREMENT,
  member_id INT,
  clock_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  end_time DATETIME NULL,
  duration_min INT DEFAULT 0,
  clock_status VARCHAR(30) DEFAULT 'COMPLETED'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO admin (admin_no, admin_name, admin_pwd)
SELECT 'admin', 'admin_user', '123456'
WHERE NOT EXISTS (SELECT 1 FROM admin WHERE admin_no = 'admin');

INSERT INTO coach (coach_no, coach_name, coach_sex, coach_age, coach_tel, good_at, coach_status, coach_pwd)
SELECT 'coach001', 'Coach Zhang', 'M', 31, '13800000001', 'Strength', '启用', '123456'
WHERE NOT EXISTS (SELECT 1 FROM coach WHERE coach_no = 'coach001');

INSERT INTO coach (coach_no, coach_name, coach_sex, coach_age, coach_tel, good_at, coach_status, coach_pwd)
SELECT 'coach002', 'Coach Li', 'F', 28, '13800000002', 'Yoga', '启用', '123456'
WHERE NOT EXISTS (SELECT 1 FROM coach WHERE coach_no = 'coach002');

INSERT INTO member (member_no, member_name, member_sex, member_age, member_tel, card_status, member_pwd)
SELECT 'member001', 'Member Wang', 'M', 25, '13900000001', '正常', '123456'
WHERE NOT EXISTS (SELECT 1 FROM member WHERE member_no = 'member001');

INSERT INTO member (member_no, member_name, member_sex, member_age, member_tel, card_status, member_pwd)
SELECT 'member002', 'Member Zhao', 'F', 23, '13900000002', '正常', '123456'
WHERE NOT EXISTS (SELECT 1 FROM member WHERE member_no = 'member002');

INSERT INTO course (course_name, coach_id, course_price, course_intro, sale_status)
SELECT 'Basic PT', c.coach_id, 299.00, 'Beginner personal training', '上架'
FROM coach c
WHERE c.coach_no = 'coach001'
  AND NOT EXISTS (SELECT 1 FROM course WHERE course_name = 'Basic PT');

INSERT INTO course (course_name, coach_id, course_price, course_intro, sale_status)
SELECT 'Advanced Yoga', c.coach_id, 199.00, 'Yoga stretch and shape', '待审核'
FROM coach c
WHERE c.coach_no = 'coach002'
  AND NOT EXISTS (SELECT 1 FROM course WHERE course_name = 'Advanced Yoga');
