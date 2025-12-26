-- 咖啡店数据库初始化脚本 (第一版本 - 角色管理版)
CREATE DATABASE IF NOT EXISTS coffee_shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE coffee_shop;

-- 删除旧表（如果存在）
DROP TABLE IF EXISTS `order_item`;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS `coffee`;
DROP TABLE IF EXISTS `user`;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(100) NOT NULL,
    `nickname` VARCHAR(50),
    `role` VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    `avatar` VARCHAR(200),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 咖啡表
CREATE TABLE IF NOT EXISTS `coffee` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `description` VARCHAR(200),
    `price` DECIMAL(10,2) NOT NULL,
    `image` VARCHAR(200),
    `category` VARCHAR(30),
    `stock` INT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_no` VARCHAR(32) NOT NULL,
    `customer_id` BIGINT NOT NULL,
    `barista_id` BIGINT,
    `total_amount` DECIMAL(10,2) NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    `remark` VARCHAR(200),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_customer` (`customer_id`),
    KEY `idx_barista` (`barista_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单项表
CREATE TABLE IF NOT EXISTS `order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `order_id` BIGINT NOT NULL,
    `coffee_id` BIGINT NOT NULL,
    `coffee_name` VARCHAR(50) NOT NULL,
    `price` DECIMAL(10,2) NOT NULL,
    `quantity` INT NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============ 初始数据 ============

-- 插入默认用户
INSERT INTO `user` (`username`, `password`, `nickname`, `role`) VALUES
('admin', '123456', '管理员', 'MANAGER'),
('barista', '123456', '咖啡师小王', 'BARISTA'),
('customer', '123456', '顾客小明', 'CUSTOMER');

-- 插入咖啡数据
INSERT INTO `coffee` (`name`, `description`, `price`, `category`, `stock`, `status`) VALUES
('美式咖啡', '经典美式，浓郁醇厚', 18.00, '经典', 50, 1),
('拿铁', '香浓奶泡，丝滑口感', 22.00, '经典', 40, 1),
('卡布奇诺', '浓缩咖啡与绵密奶泡的完美结合', 24.00, '经典', 35, 1),
('摩卡', '巧克力与咖啡的甜蜜邂逅', 26.00, '特调', 30, 1),
('焦糖玛奇朵', '香甜焦糖与浓郁咖啡', 28.00, '特调', 25, 1),
('冰美式', '清爽冰镇美式咖啡', 20.00, '冰饮', 45, 1),
('抹茶拿铁', '日式抹茶与牛奶的融合', 28.00, '特调', 20, 1),
('香草拿铁', '香草风味拿铁', 24.00, '特调', 30, 1);
