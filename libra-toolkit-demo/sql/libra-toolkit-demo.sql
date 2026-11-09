/*
 Navicat Premium Dump SQL

 Source Server         : MySQL-8.0.20
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3307
 Source Schema         : libra-framework-demo

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 14/11/2025 15:10:27
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门编码',
  `parent_id` int DEFAULT NULL COMMENT '父部门ID',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '部门描述',
  `manager_id` bigint DEFAULT NULL COMMENT '部门经理ID',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '办公地点',
  `level` int DEFAULT NULL COMMENT '部门层级',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE=启用, INACTIVE=停用',
  `establish_date` date DEFAULT NULL COMMENT '成立日期',
  `extra_info` json DEFAULT NULL COMMENT '扩展信息(JSON)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_code` (`code`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='部门信息表';

-- ----------------------------
-- Records of department
-- ----------------------------
BEGIN;
INSERT INTO `department` (`id`, `name`, `code`, `parent_id`, `description`, `manager_id`, `location`, `level`, `status`, `establish_date`, `extra_info`, `created_time`, `updated_time`) VALUES (1, '总经办', 'CEO', NULL, '公司最高管理部门', NULL, '总部大楼10层', 1, 'ACTIVE', '2020-01-01', NULL, '2025-11-14 14:23:45', '2025-11-14 14:23:45');
INSERT INTO `department` (`id`, `name`, `code`, `parent_id`, `description`, `manager_id`, `location`, `level`, `status`, `establish_date`, `extra_info`, `created_time`, `updated_time`) VALUES (2, '技术部', 'TECH', 1, '技术研发中心', 1001, '总部大楼5层', 2, 'ACTIVE', '2020-01-01', NULL, '2025-11-14 14:23:45', '2025-11-14 14:23:45');
INSERT INTO `department` (`id`, `name`, `code`, `parent_id`, `description`, `manager_id`, `location`, `level`, `status`, `establish_date`, `extra_info`, `created_time`, `updated_time`) VALUES (3, '产品部', 'PRODUCT', 1, '产品设计中心', 1002, '总部大楼6层', 2, 'ACTIVE', '2020-01-01', NULL, '2025-11-14 14:23:45', '2025-11-14 14:23:45');
INSERT INTO `department` (`id`, `name`, `code`, `parent_id`, `description`, `manager_id`, `location`, `level`, `status`, `establish_date`, `extra_info`, `created_time`, `updated_time`) VALUES (4, '市场部', 'MARKETING', 1, '市场营销中心', 1003, '总部大楼7层', 2, 'ACTIVE', '2020-01-01', NULL, '2025-11-14 14:23:45', '2025-11-14 14:23:45');
INSERT INTO `department` (`id`, `name`, `code`, `parent_id`, `description`, `manager_id`, `location`, `level`, `status`, `establish_date`, `extra_info`, `created_time`, `updated_time`) VALUES (5, '人力资源部', 'HR', 1, '人力资源管理', 1004, '总部大楼8层', 2, 'ACTIVE', '2020-01-01', NULL, '2025-11-14 14:23:45', '2025-11-14 14:23:45');
COMMIT;

-- ----------------------------
-- Table structure for employee
-- ----------------------------
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '员工ID',
  `employee_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '员工编号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '电话',
  `department_id` int DEFAULT NULL COMMENT '部门ID',
  `position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '职位',
  `level` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '职级: P1-P10',
  `base_salary` decimal(10,2) DEFAULT NULL COMMENT '基础薪资',
  `hire_date` date DEFAULT NULL COMMENT '入职日期',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE=在职, INACTIVE=离职, RESIGNED=辞职',
  `skills` json DEFAULT NULL COMMENT '技能标签(JSON数组)',
  `address_province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '地址-省',
  `address_city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '地址-市',
  `address_street` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '地址-街道',
  `address_zipcode` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '地址-邮编',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_employee_no` (`employee_no`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_email` (`email`) USING BTREE,
  KEY `idx_department_id` (`department_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='员工信息表';

-- ----------------------------
-- Records of employee
-- ----------------------------
BEGIN;
INSERT INTO `employee` (`id`, `employee_no`, `name`, `email`, `phone`, `department_id`, `position`, `level`, `base_salary`, `hire_date`, `status`, `skills`, `address_province`, `address_city`, `address_street`, `address_zipcode`, `created_time`, `updated_time`) VALUES (1001, 'EMP001', '张技术', 'zhangjs@company.com', '13800138001', 2, '技术总监', 'P8', 25000.00, '2020-03-01', 'ACTIVE', '[\"Java\", \"Spring\", \"微服务\"]', '浙江省', '杭州市', '西湖区文三路100号', '310000', '2025-11-14 14:23:45', '2025-11-14 14:23:45');
INSERT INTO `employee` (`id`, `employee_no`, `name`, `email`, `phone`, `department_id`, `position`, `level`, `base_salary`, `hire_date`, `status`, `skills`, `address_province`, `address_city`, `address_street`, `address_zipcode`, `created_time`, `updated_time`) VALUES (1002, 'EMP002', '李产品', 'lichanpin@company.com', '13800138002', 3, '产品经理', 'P7', 22000.00, '2020-04-01', 'ACTIVE', '[\"产品设计\", \"需求分析\", \"原型设计\"]', '浙江省', '杭州市', '西湖区文三路100号', '310000', '2025-11-14 14:23:45', '2025-11-14 14:23:45');
INSERT INTO `employee` (`id`, `employee_no`, `name`, `email`, `phone`, `department_id`, `position`, `level`, `base_salary`, `hire_date`, `status`, `skills`, `address_province`, `address_city`, `address_street`, `address_zipcode`, `created_time`, `updated_time`) VALUES (1003, 'EMP003', '王市场', 'wangshichang@company.com', '13800138003', 4, '市场总监', 'P8', 23000.00, '2020-05-01', 'ACTIVE', '[\"市场营销\", \"品牌推广\", \"活动策划\"]', '浙江省', '杭州市', '西湖区文三路100号', '310000', '2025-11-14 14:23:45', '2025-11-14 14:23:45');
INSERT INTO `employee` (`id`, `employee_no`, `name`, `email`, `phone`, `department_id`, `position`, `level`, `base_salary`, `hire_date`, `status`, `skills`, `address_province`, `address_city`, `address_street`, `address_zipcode`, `created_time`, `updated_time`) VALUES (1004, 'EMP004', '赵人事', 'zhaoreshi@company.com', '13800138004', 5, 'HR主管', 'P6', 18000.00, '2020-06-01', 'ACTIVE', '[\"招聘\", \"培训\", \"薪酬管理\"]', '浙江省', '杭州市', '西湖区文三路100号', '310000', '2025-11-14 14:23:45', '2025-11-14 14:23:45');
INSERT INTO `employee` (`id`, `employee_no`, `name`, `email`, `phone`, `department_id`, `position`, `level`, `base_salary`, `hire_date`, `status`, `skills`, `address_province`, `address_city`, `address_street`, `address_zipcode`, `created_time`, `updated_time`) VALUES (1005, 'EMP005', '钱开发', 'qiankaifa@company.com', '13800138005', 2, 'Java开发工程师', 'P5', 15000.00, '2021-01-10', 'ACTIVE', '[\"Java\", \"MySQL\", \"Redis\"]', '浙江省', '杭州市', '西湖区文三路100号', '310000', '2025-11-14 14:23:45', '2025-11-14 14:23:45');
COMMIT;

-- ----------------------------
-- Table structure for employer
-- ----------------------------
DROP TABLE IF EXISTS `employer`;
CREATE TABLE `employer` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公司ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公司名称',
  `industry` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '所属行业',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '公司地址',
  `contact_info` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系方式',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_industry` (`industry`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='雇主信息表';

-- ----------------------------
-- Records of employer
-- ----------------------------
BEGIN;
INSERT INTO `employer` (`id`, `name`, `industry`, `address`, `contact_info`) VALUES ('emp001', '示例科技集团', 'IT互联网', '浙江省杭州市余杭区', '0571-0000001');
INSERT INTO `employer` (`id`, `name`, `industry`, `address`, `contact_info`) VALUES ('emp002', '示例信息技术公司', 'IT互联网', '广东省深圳市南山区', '0755-9876543');
INSERT INTO `employer` (`id`, `name`, `industry`, `address`, `contact_info`) VALUES ('emp003', '华为技术', 'IT通信', '广东省深圳市龙岗区', '0755-5555555');
COMMIT;

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `customer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户姓名',
  `customer_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '客户电话',
  `total_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单总额',
  `discount_amount` decimal(10,2) DEFAULT '0.00' COMMENT '折扣金额',
  `actual_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING=待确认, CONFIRMED=已确认, SHIPPED=已发货, DELIVERED=已送达, CANCELLED=已取消',
  `payment_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态: UNPAID=未支付, PAID=已支付, REFUNDED=已退款',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '支付方式',
  `order_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `pay_date` datetime DEFAULT NULL COMMENT '支付时间',
  `ship_date` datetime DEFAULT NULL COMMENT '发货时间',
  `delivery_date` datetime DEFAULT NULL COMMENT '收货时间',
  `shipping_address_province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '收货地址-省',
  `shipping_address_city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '收货地址-市',
  `shipping_address_street` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '收货地址-街道',
  `shipping_address_zipcode` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '收货地址-邮编',
  `remarks` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '订单备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_order_no` (`order_no`) USING BTREE,
  KEY `idx_customer_id` (`customer_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_payment_status` (`payment_status`) USING BTREE,
  KEY `idx_order_date` (`order_date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='订单主表';

-- ----------------------------
-- Records of order
-- ----------------------------
BEGIN;
INSERT INTO `order` (`id`, `order_no`, `customer_id`, `customer_name`, `customer_phone`, `total_amount`, `discount_amount`, `actual_amount`, `status`, `payment_status`, `payment_method`, `order_date`, `pay_date`, `ship_date`, `delivery_date`, `shipping_address_province`, `shipping_address_city`, `shipping_address_street`, `shipping_address_zipcode`, `remarks`, `created_time`, `updated_time`) VALUES (1, 'ORD20250101001', 10001, '张三', '13900139001', 1580.00, 80.00, 1500.00, 'DELIVERED', 'PAID', '支付宝', '2025-01-15 10:30:00', '2025-01-15 10:35:00', '2025-01-16 09:00:00', '2025-01-18 14:20:00', '浙江省', '杭州市', '西湖区文一路100号', '310000', NULL, '2025-11-14 14:23:45', '2025-11-14 14:23:45');
INSERT INTO `order` (`id`, `order_no`, `customer_id`, `customer_name`, `customer_phone`, `total_amount`, `discount_amount`, `actual_amount`, `status`, `payment_status`, `payment_method`, `order_date`, `pay_date`, `ship_date`, `delivery_date`, `shipping_address_province`, `shipping_address_city`, `shipping_address_street`, `shipping_address_zipcode`, `remarks`, `created_time`, `updated_time`) VALUES (2, 'ORD20250102001', 10002, '李四', '13900139002', 2680.00, 180.00, 2500.00, 'SHIPPED', 'PAID', '微信支付', '2025-01-18 15:20:00', '2025-01-18 15:22:00', '2025-01-19 10:00:00', NULL, '上海市', '浦东新区', '张江高科技园区', '201200', NULL, '2025-11-14 14:23:45', '2025-11-14 14:23:45');
INSERT INTO `order` (`id`, `order_no`, `customer_id`, `customer_name`, `customer_phone`, `total_amount`, `discount_amount`, `actual_amount`, `status`, `payment_status`, `payment_method`, `order_date`, `pay_date`, `ship_date`, `delivery_date`, `shipping_address_province`, `shipping_address_city`, `shipping_address_street`, `shipping_address_zipcode`, `remarks`, `created_time`, `updated_time`) VALUES (3, 'ORD20250103001', 10003, '王五', '13900139003', 980.00, 0.00, 980.00, 'PENDING', 'UNPAID', NULL, '2025-01-20 09:15:00', NULL, NULL, NULL, '广东省', '深圳市', '南山区科技园', '518000', NULL, '2025-11-14 14:23:45', '2025-11-14 14:23:45');
COMMIT;

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品编码',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `specification` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规格',
  `unit_price` decimal(10,2) NOT NULL COMMENT '单价',
  `quantity` int NOT NULL COMMENT '数量',
  `total_price` decimal(10,2) NOT NULL COMMENT '小计',
  `discount_amount` decimal(10,2) DEFAULT '0.00' COMMENT '明细折扣',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NORMAL' COMMENT '明细状态: NORMAL=正常, CANCELLED=已取消',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_order_id` (`order_id`) USING BTREE,
  KEY `idx_product_code` (`product_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='订单明细表';

-- ----------------------------
-- Records of order_item
-- ----------------------------
BEGIN;
INSERT INTO `order_item` (`id`, `order_id`, `product_code`, `product_name`, `specification`, `unit_price`, `quantity`, `total_price`, `discount_amount`, `status`, `created_time`) VALUES (1, 1, 'PROD001', 'Java编程思想(第4版)', '中文版', 120.00, 2, 240.00, 20.00, 'NORMAL', '2025-11-14 14:23:45');
INSERT INTO `order_item` (`id`, `order_id`, `product_code`, `product_name`, `specification`, `unit_price`, `quantity`, `total_price`, `discount_amount`, `status`, `created_time`) VALUES (2, 1, 'PROD002', 'Spring Boot实战', '第2版', 98.00, 3, 294.00, 14.00, 'NORMAL', '2025-11-14 14:23:45');
INSERT INTO `order_item` (`id`, `order_id`, `product_code`, `product_name`, `specification`, `unit_price`, `quantity`, `total_price`, `discount_amount`, `status`, `created_time`) VALUES (3, 1, 'PROD003', '深入理解JVM虚拟机', '第3版', 135.00, 2, 270.00, 10.00, 'NORMAL', '2025-11-14 14:23:45');
INSERT INTO `order_item` (`id`, `order_id`, `product_code`, `product_name`, `specification`, `unit_price`, `quantity`, `total_price`, `discount_amount`, `status`, `created_time`) VALUES (4, 1, 'PROD004', 'MySQL必知必会', '第5版', 88.00, 3, 264.00, 12.00, 'NORMAL', '2025-11-14 14:23:45');
INSERT INTO `order_item` (`id`, `order_id`, `product_code`, `product_name`, `specification`, `unit_price`, `quantity`, `total_price`, `discount_amount`, `status`, `created_time`) VALUES (5, 1, 'PROD005', 'Redis设计与实现', '最新版', 108.00, 2, 216.00, 16.00, 'NORMAL', '2025-11-14 14:23:45');
INSERT INTO `order_item` (`id`, `order_id`, `product_code`, `product_name`, `specification`, `unit_price`, `quantity`, `total_price`, `discount_amount`, `status`, `created_time`) VALUES (6, 2, 'PROD006', 'Kubernetes权威指南', '第5版', 168.00, 2, 336.00, 36.00, 'NORMAL', '2025-11-14 14:23:45');
INSERT INTO `order_item` (`id`, `order_id`, `product_code`, `product_name`, `specification`, `unit_price`, `quantity`, `total_price`, `discount_amount`, `status`, `created_time`) VALUES (7, 2, 'PROD007', 'Docker技术入门与实战', '第4版', 118.00, 3, 354.00, 24.00, 'NORMAL', '2025-11-14 14:23:45');
INSERT INTO `order_item` (`id`, `order_id`, `product_code`, `product_name`, `specification`, `unit_price`, `quantity`, `total_price`, `discount_amount`, `status`, `created_time`) VALUES (8, 2, 'PROD008', '微服务设计', '中文版', 128.00, 2, 256.00, 16.00, 'NORMAL', '2025-11-14 14:23:45');
INSERT INTO `order_item` (`id`, `order_id`, `product_code`, `product_name`, `specification`, `unit_price`, `quantity`, `total_price`, `discount_amount`, `status`, `created_time`) VALUES (9, 2, 'PROD009', 'Elasticsearch实战', '最新版', 148.00, 3, 444.00, 44.00, 'NORMAL', '2025-11-14 14:23:45');
INSERT INTO `order_item` (`id`, `order_id`, `product_code`, `product_name`, `specification`, `unit_price`, `quantity`, `total_price`, `discount_amount`, `status`, `created_time`) VALUES (10, 2, 'PROD010', 'RabbitMQ实战指南', '中文版', 98.00, 3, 294.00, 14.00, 'NORMAL', '2025-11-14 14:23:45');
INSERT INTO `order_item` (`id`, `order_id`, `product_code`, `product_name`, `specification`, `unit_price`, `quantity`, `total_price`, `discount_amount`, `status`, `created_time`) VALUES (11, 3, 'PROD011', 'Python编程快速上手', '第2版', 89.00, 2, 178.00, 0.00, 'NORMAL', '2025-11-14 14:23:45');
INSERT INTO `order_item` (`id`, `order_id`, `product_code`, `product_name`, `specification`, `unit_price`, `quantity`, `total_price`, `discount_amount`, `status`, `created_time`) VALUES (12, 3, 'PROD012', 'Python数据分析', '第3版', 118.00, 1, 118.00, 0.00, 'NORMAL', '2025-11-14 14:23:45');
INSERT INTO `order_item` (`id`, `order_id`, `product_code`, `product_name`, `specification`, `unit_price`, `quantity`, `total_price`, `discount_amount`, `status`, `created_time`) VALUES (13, 3, 'PROD013', '机器学习实战', '基于Scikit-Learn', 138.00, 1, 138.00, 0.00, 'NORMAL', '2025-11-14 14:23:45');
INSERT INTO `order_item` (`id`, `order_id`, `product_code`, `product_name`, `specification`, `unit_price`, `quantity`, `total_price`, `discount_amount`, `status`, `created_time`) VALUES (14, 3, 'PROD014', '深度学习', '中文版', 168.00, 1, 168.00, 0.00, 'NORMAL', '2025-11-14 14:23:45');
INSERT INTO `order_item` (`id`, `order_id`, `product_code`, `product_name`, `specification`, `unit_price`, `quantity`, `total_price`, `discount_amount`, `status`, `created_time`) VALUES (15, 3, 'PROD015', 'TensorFlow实战', '第2版', 128.00, 1, 128.00, 0.00, 'NORMAL', '2025-11-14 14:23:45');
COMMIT;

-- ----------------------------
-- Table structure for project
-- ----------------------------
DROP TABLE IF EXISTS `project`;
CREATE TABLE `project` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目编码',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '项目描述',
  `manager_id` bigint DEFAULT NULL COMMENT '项目经理ID',
  `department_id` int DEFAULT NULL COMMENT '所属部门',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PLANNING' COMMENT '状态: PLANNING=规划中, RUNNING=运行中, PAUSED=暂停, COMPLETED=已完成, CANCELLED=已取消',
  `priority` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'MEDIUM' COMMENT '优先级: HIGH=高, MEDIUM=中, LOW=低',
  `budget` decimal(12,2) DEFAULT NULL COMMENT '项目预算',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `actual_start_date` date DEFAULT NULL COMMENT '实际开始日期',
  `actual_end_date` date DEFAULT NULL COMMENT '实际结束日期',
  `progress` int DEFAULT '0' COMMENT '进度百分比(0-100)',
  `tags` json DEFAULT NULL COMMENT '项目标签(JSON数组)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_code` (`code`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_manager_id` (`manager_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_priority` (`priority`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='项目信息表';

-- ----------------------------
-- Records of project
-- ----------------------------
BEGIN;
INSERT INTO `project` (`id`, `name`, `code`, `description`, `manager_id`, `department_id`, `status`, `priority`, `budget`, `start_date`, `end_date`, `actual_start_date`, `actual_end_date`, `progress`, `tags`, `created_time`, `updated_time`) VALUES (1, '电商平台开发', 'PROJ001', '开发全新的电商交易平台', 1001, 2, 'RUNNING', 'HIGH', 1000000.00, '2025-01-01', '2025-12-31', '2025-01-01', NULL, 35, '[\"电商\", \"微服务\", \"云原生\"]', '2025-11-14 14:23:45', '2025-11-14 14:23:45');
INSERT INTO `project` (`id`, `name`, `code`, `description`, `manager_id`, `department_id`, `status`, `priority`, `budget`, `start_date`, `end_date`, `actual_start_date`, `actual_end_date`, `progress`, `tags`, `created_time`, `updated_time`) VALUES (2, 'CRM系统升级', 'PROJ002', '升级客户关系管理系统', 1001, 2, 'PLANNING', 'MEDIUM', 500000.00, '2025-03-01', '2025-09-30', NULL, NULL, 0, '[\"CRM\", \"系统升级\"]', '2025-11-14 14:23:45', '2025-11-14 14:23:45');
INSERT INTO `project` (`id`, `name`, `code`, `description`, `manager_id`, `department_id`, `status`, `priority`, `budget`, `start_date`, `end_date`, `actual_start_date`, `actual_end_date`, `progress`, `tags`, `created_time`, `updated_time`) VALUES (3, '品牌推广活动', 'PROJ003', '2025年度品牌推广活动策划与执行', 1003, 4, 'RUNNING', 'HIGH', 300000.00, '2025-01-15', '2025-06-30', '2025-01-15', NULL, 20, '[\"品牌\", \"营销\", \"活动\"]', '2025-11-14 14:23:45', '2025-11-14 14:23:45');
COMMIT;

-- ----------------------------
-- Table structure for salary
-- ----------------------------
DROP TABLE IF EXISTS `salary`;
CREATE TABLE `salary` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '薪资记录ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `period` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '薪资期间: 格式YYYY-MM',
  `base_salary` decimal(10,2) NOT NULL COMMENT '基础工资',
  `performance_bonus` decimal(10,2) DEFAULT '0.00' COMMENT '绩效奖金',
  `allowance` decimal(10,2) DEFAULT '0.00' COMMENT '补贴',
  `overtime` decimal(10,2) DEFAULT '0.00' COMMENT '加班费',
  `deduction` decimal(10,2) DEFAULT '0.00' COMMENT '扣款',
  `total_amount` decimal(10,2) NOT NULL COMMENT '总额',
  `tax_amount` decimal(10,2) DEFAULT '0.00' COMMENT '税额',
  `actual_amount` decimal(10,2) NOT NULL COMMENT '实发金额',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT=草稿, APPROVED=已审批, PAID=已发放',
  `pay_date` date DEFAULT NULL COMMENT '发放日期',
  `remarks` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `extra_amounts` json DEFAULT NULL COMMENT '其他金额项(JSON对象)',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_employee_period` (`employee_id`,`period`) USING BTREE,
  KEY `idx_period` (`period`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_pay_date` (`pay_date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='薪资详细表';

-- ----------------------------
-- Records of salary
-- ----------------------------
BEGIN;
INSERT INTO `salary` (`id`, `employee_id`, `period`, `base_salary`, `performance_bonus`, `allowance`, `overtime`, `deduction`, `total_amount`, `tax_amount`, `actual_amount`, `status`, `pay_date`, `remarks`, `extra_amounts`, `created_time`, `updated_time`) VALUES (1, 1001, '2025-01', 25000.00, 5000.00, 1000.00, 0.00, 0.00, 31000.00, 3100.00, 27900.00, 'PAID', '2025-01-25', NULL, NULL, '2025-11-14 14:23:45', '2025-11-14 14:23:45');
INSERT INTO `salary` (`id`, `employee_id`, `period`, `base_salary`, `performance_bonus`, `allowance`, `overtime`, `deduction`, `total_amount`, `tax_amount`, `actual_amount`, `status`, `pay_date`, `remarks`, `extra_amounts`, `created_time`, `updated_time`) VALUES (2, 1002, '2025-01', 22000.00, 4000.00, 1000.00, 0.00, 0.00, 27000.00, 2700.00, 24300.00, 'PAID', '2025-01-25', NULL, NULL, '2025-11-14 14:23:45', '2025-11-14 14:23:45');
INSERT INTO `salary` (`id`, `employee_id`, `period`, `base_salary`, `performance_bonus`, `allowance`, `overtime`, `deduction`, `total_amount`, `tax_amount`, `actual_amount`, `status`, `pay_date`, `remarks`, `extra_amounts`, `created_time`, `updated_time`) VALUES (3, 1003, '2025-01', 23000.00, 4500.00, 1000.00, 0.00, 0.00, 28500.00, 2850.00, 25650.00, 'PAID', '2025-01-25', NULL, NULL, '2025-11-14 14:23:45', '2025-11-14 14:23:45');
INSERT INTO `salary` (`id`, `employee_id`, `period`, `base_salary`, `performance_bonus`, `allowance`, `overtime`, `deduction`, `total_amount`, `tax_amount`, `actual_amount`, `status`, `pay_date`, `remarks`, `extra_amounts`, `created_time`, `updated_time`) VALUES (4, 1004, '2025-01', 18000.00, 3000.00, 1000.00, 0.00, 0.00, 22000.00, 2200.00, 19800.00, 'PAID', '2025-01-25', NULL, NULL, '2025-11-14 14:23:45', '2025-11-14 14:23:45');
INSERT INTO `salary` (`id`, `employee_id`, `period`, `base_salary`, `performance_bonus`, `allowance`, `overtime`, `deduction`, `total_amount`, `tax_amount`, `actual_amount`, `status`, `pay_date`, `remarks`, `extra_amounts`, `created_time`, `updated_time`) VALUES (5, 1005, '2025-01', 15000.00, 2000.00, 800.00, 500.00, 0.00, 18300.00, 1830.00, 16470.00, 'PAID', '2025-01-25', NULL, NULL, '2025-11-14 14:23:45', '2025-11-14 14:23:45');
COMMIT;

-- ----------------------------
-- Table structure for salaryer
-- ----------------------------
DROP TABLE IF EXISTS `salaryer`;
CREATE TABLE `salaryer` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '薪资记录ID',
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '薪资数额',
  `payment_date` date NOT NULL COMMENT '发薪日期',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING=待发, PAID=已发',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_payment_date` (`payment_date`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='薪资信息表';

-- ----------------------------
-- Records of salaryer
-- ----------------------------
BEGIN;
INSERT INTO `salaryer` (`id`, `user_id`, `amount`, `payment_date`, `status`) VALUES ('sal001', 'user001', 15000.00, '2025-01-25', 'PAID');
INSERT INTO `salaryer` (`id`, `user_id`, `amount`, `payment_date`, `status`) VALUES ('sal002', 'user002', 18000.00, '2025-01-25', 'PAID');
INSERT INTO `salaryer` (`id`, `user_id`, `amount`, `payment_date`, `status`) VALUES ('sal003', 'user003', 12000.00, '2025-01-25', 'PENDING');
COMMIT;

-- ----------------------------
-- Table structure for test_auto_fill
-- ----------------------------
DROP TABLE IF EXISTS `test_auto_fill`;
CREATE TABLE `test_auto_fill` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键ID',
  `business_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务名称',
  `business_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务编码',
  `business_status` tinyint NOT NULL DEFAULT '0' COMMENT '业务状态: 0=待处理, 1=处理中, 2=已完成',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_business_code` (`business_code`) USING BTREE,
  KEY `idx_business_name` (`business_name`) USING BTREE,
  KEY `idx_business_status` (`business_status`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AutoFill拦截器测试表';

-- ----------------------------
-- Records of test_auto_fill
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for test_meta_handler
-- ----------------------------
DROP TABLE IF EXISTS `test_meta_handler`;
CREATE TABLE `test_meta_handler` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `user_age` int DEFAULT NULL COMMENT '用户年龄',
  `user_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户邮箱',
  `user_status` tinyint NOT NULL DEFAULT '1' COMMENT '用户状态: 0=禁用, 1=启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_name` (`user_name`) USING BTREE,
  KEY `idx_user_email` (`user_email`) USING BTREE,
  KEY `idx_user_status` (`user_status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='MetaObjectHandler测试表';

-- ----------------------------
-- Records of test_meta_handler
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for test_pagination
-- ----------------------------
DROP TABLE IF EXISTS `test_pagination`;
CREATE TABLE `test_pagination` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `order_level` tinyint NOT NULL DEFAULT '1' COMMENT '订单级别: 1=普通, 2=重要, 3=VIP',
  `order_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单类型: NORMAL=普通, URGENT=紧急',
  `order_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '订单金额',
  `order_status` tinyint NOT NULL DEFAULT '1' COMMENT '订单状态: 1=待处理, 2=已完成',
  `customer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '客户名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_order_no` (`order_no`) USING BTREE,
  KEY `idx_order_level` (`order_level`) USING BTREE,
  KEY `idx_order_type` (`order_type`) USING BTREE,
  KEY `idx_order_status` (`order_status`) USING BTREE,
  KEY `idx_customer_name` (`customer_name`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='分页拦截器测试表';

-- ----------------------------
-- Records of test_pagination
-- ----------------------------
BEGIN;
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page001', 'ORDER20250001', 1, 'NORMAL', 100.00, 1, '张三', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page002', 'ORDER20250002', 2, 'URGENT', 200.00, 1, '李四', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page003', 'ORDER20250003', 1, 'NORMAL', 150.00, 2, '王五', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page004', 'ORDER20250004', 3, 'URGENT', 500.00, 1, '赵六', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page005', 'ORDER20250005', 2, 'NORMAL', 250.00, 2, '孙七', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page006', 'ORDER20250006', 1, 'NORMAL', 120.00, 1, '周八', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page007', 'ORDER20250007', 2, 'URGENT', 300.00, 1, '吴九', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page008', 'ORDER20250008', 3, 'NORMAL', 600.00, 2, '郑十', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page009', 'ORDER20250009', 1, 'URGENT', 180.00, 1, '钱十一', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page010', 'ORDER20250010', 2, 'NORMAL', 220.00, 2, '陈十二', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page011', 'ORDER20250011', 1, 'NORMAL', 90.00, 1, '张小明', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page012', 'ORDER20250012', 3, 'URGENT', 700.00, 1, '李小红', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page013', 'ORDER20250013', 2, 'NORMAL', 280.00, 2, '王小华', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page014', 'ORDER20250014', 1, 'URGENT', 160.00, 1, '赵小美', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page015', 'ORDER20250015', 2, 'NORMAL', 240.00, 1, '孙小刚', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page016', 'ORDER20250016', 3, 'URGENT', 800.00, 2, '周小丽', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page017', 'ORDER20250017', 1, 'NORMAL', 110.00, 1, '吴小强', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page018', 'ORDER20250018', 2, 'URGENT', 320.00, 1, '郑小芳', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page019', 'ORDER20250019', 1, 'NORMAL', 140.00, 2, '钱小东', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page020', 'ORDER20250020', 3, 'NORMAL', 650.00, 1, '陈小西', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page021', 'ORDER20250021', 2, 'URGENT', 290.00, 2, '张大明', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page022', 'ORDER20250022', 1, 'NORMAL', 105.00, 1, '李大红', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page023', 'ORDER20250023', 2, 'URGENT', 310.00, 1, '王大华', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page024', 'ORDER20250024', 3, 'NORMAL', 720.00, 2, '赵大美', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page025', 'ORDER20250025', 1, 'URGENT', 175.00, 1, '孙大刚', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page026', 'ORDER20250026', 2, 'NORMAL', 260.00, 1, '周大丽', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page027', 'ORDER20250027', 1, 'NORMAL', 130.00, 2, '吴大强', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page028', 'ORDER20250028', 3, 'URGENT', 850.00, 1, '郑大芳', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page029', 'ORDER20250029', 2, 'NORMAL', 270.00, 2, '钱大东', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page030', 'ORDER20250030', 1, 'URGENT', 190.00, 1, '陈大西', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page031', 'ORDER20250031', 2, 'NORMAL', 230.00, 1, '张二明', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page032', 'ORDER20250032', 3, 'URGENT', 900.00, 2, '李二红', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page033', 'ORDER20250033', 1, 'NORMAL', 115.00, 1, '王二华', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page034', 'ORDER20250034', 2, 'URGENT', 330.00, 1, '赵二美', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page035', 'ORDER20250035', 1, 'NORMAL', 95.00, 2, '孙二刚', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page036', 'ORDER20250036', 3, 'NORMAL', 750.00, 1, '周二丽', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page037', 'ORDER20250037', 2, 'URGENT', 305.00, 2, '吴二强', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page038', 'ORDER20250038', 1, 'NORMAL', 125.00, 1, '郑二芳', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page039', 'ORDER20250039', 2, 'URGENT', 340.00, 1, '钱二东', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page040', 'ORDER20250040', 3, 'NORMAL', 680.00, 2, '陈二西', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page041', 'ORDER20250041', 1, 'URGENT', 165.00, 1, '张三明', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page042', 'ORDER20250042', 2, 'NORMAL', 245.00, 1, '李三红', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page043', 'ORDER20250043', 1, 'NORMAL', 135.00, 2, '王三华', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page044', 'ORDER20250044', 3, 'URGENT', 920.00, 1, '赵三美', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page045', 'ORDER20250045', 2, 'NORMAL', 285.00, 2, '孙三刚', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page046', 'ORDER20250046', 1, 'URGENT', 170.00, 1, '周三丽', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page047', 'ORDER20250047', 2, 'NORMAL', 255.00, 1, '吴三强', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page048', 'ORDER20250048', 3, 'URGENT', 780.00, 2, '郑三芳', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page049', 'ORDER20250049', 1, 'NORMAL', 145.00, 1, '钱三东', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_pagination` (`id`, `order_no`, `order_level`, `order_type`, `order_amount`, `order_status`, `customer_name`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('page050', 'ORDER20250050', 2, 'URGENT', 350.00, 2, '陈三西', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
COMMIT;

-- ----------------------------
-- Table structure for test_user
-- ----------------------------
DROP TABLE IF EXISTS `test_user`;
CREATE TABLE `test_user` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（1：启用，0：禁用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`) USING BTREE,
  KEY `idx_email` (`email`) USING BTREE,
  KEY `idx_phone` (`phone`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='测试用户表';

-- ----------------------------
-- Records of test_user
-- ----------------------------
BEGIN;
INSERT INTO `test_user` (`id`, `username`, `password`, `email`, `phone`, `status`, `remark`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('test001', 'testuser1', 'demo_password', 'test1@example.com', '13900139001', 1, '测试用户1', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_user` (`id`, `username`, `password`, `email`, `phone`, `status`, `remark`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('test002', 'testuser2', 'demo_password', 'test2@example.com', '13900139002', 1, '测试用户2', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_user` (`id`, `username`, `password`, `email`, `phone`, `status`, `remark`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('test003', 'testuser3', 'demo_password', 'test3@example.com', '13900139003', 0, '测试用户3(禁用)', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_user` (`id`, `username`, `password`, `email`, `phone`, `status`, `remark`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('test004', 'testuser4', 'demo_password', 'test4@example.com', '13900139004', 1, '测试用户4', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
INSERT INTO `test_user` (`id`, `username`, `password`, `email`, `phone`, `status`, `remark`, `create_time`, `create_user`, `update_time`, `update_user`) VALUES ('test005', 'testuser5', 'demo_password', 'test5@example.com', '13900139005', 1, '测试用户5', '2025-11-12 14:18:42', 'system', '2025-11-12 14:18:42', 'system');
COMMIT;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户姓名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户邮箱',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_email` (`email`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户信息表';

-- ----------------------------
-- Records of user
-- ----------------------------
BEGIN;
INSERT INTO `user` (`id`, `name`, `email`) VALUES ('user001', '张三', 'zhangsan@example.com');
INSERT INTO `user` (`id`, `name`, `email`) VALUES ('user002', '李四', 'lisi@example.com');
INSERT INTO `user` (`id`, `name`, `email`) VALUES ('user003', '王五', 'wangwu@example.com');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
