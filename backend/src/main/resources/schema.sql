-- 创建数据库
CREATE DATABASE IF NOT EXISTS secondhand_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE secondhand_db;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    avatar VARCHAR(255),
    nickname VARCHAR(50),
    real_name VARCHAR(50),
    address VARCHAR(255),
    credit_score INT DEFAULT 100,
    balance DECIMAL(10,2) DEFAULT 0.00,
    status INT DEFAULT 1 COMMENT '0-禁用 1-正常',
    role VARCHAR(20) DEFAULT 'USER',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 商品分类表
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    icon VARCHAR(255),
    parent_id INT DEFAULT 0,
    sort_order INT DEFAULT 0,
    status INT DEFAULT 1 COMMENT '0-禁用 1-启用'
);

-- 商品表
CREATE TABLE IF NOT EXISTS goods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    category_id BIGINT,
    seller_id BIGINT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    original_price DECIMAL(10,2),
    condition_level INT DEFAULT 9 COMMENT '成色 1-10',
    images TEXT,
    location VARCHAR(100),
    status INT DEFAULT 0 COMMENT '0-待审核 1-在售 2-已售 3-下架 4-拍卖中',
    view_count INT DEFAULT 0,
    favorite_count INT DEFAULT 0,
    is_auction INT DEFAULT 0 COMMENT '0-普通商品 1-拍卖商品',
    ai_estimated_price DECIMAL(10,2),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    goods_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    original_price DECIMAL(10,2) NOT NULL COMMENT '商品原价',
    amount DECIMAL(10,2) NOT NULL COMMENT '实际交易价格',
    status INT DEFAULT 0 COMMENT '0-待付款 1-待发货 2-待收货 3-已完成 4-已取消 5-待确认',
    address_id BIGINT,
    remark VARCHAR(255),
    session_id BIGINT COMMENT '关联的聊天会话ID',
    price_changed INT DEFAULT 0 COMMENT '0-未改价 1-已改价',
    pay_time DATETIME,
    ship_time DATETIME,
    confirm_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 拍卖表
CREATE TABLE IF NOT EXISTS auctions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    goods_id BIGINT NOT NULL,
    start_price DECIMAL(10,2) NOT NULL,
    current_price DECIMAL(10,2),
    min_increment DECIMAL(10,2) DEFAULT 1.00 COMMENT '最低加价幅度',
    buy_now_price DECIMAL(10,2) COMMENT '一口价',
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    winner_id BIGINT,
    bid_count INT DEFAULT 0,
    status INT DEFAULT 0 COMMENT '0-未开始 1-进行中 2-已结束',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 拍卖出价记录
CREATE TABLE IF NOT EXISTS auction_bids (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    auction_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    bid_price DECIMAL(10,2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 收藏表
CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    goods_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_goods (user_id, goods_id)
);

-- 收货地址表
CREATE TABLE IF NOT EXISTS addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    detail VARCHAR(255) NOT NULL,
    is_default INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 聊天会话表
CREATE TABLE IF NOT EXISTS chat_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    goods_id BIGINT,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    last_message TEXT,
    last_message_time DATETIME,
    unread_count_buyer INT DEFAULT 0,
    unread_count_seller INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 聊天消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    msg_type INT DEFAULT 0 COMMENT '0-文本 1-图片 2-系统消息',
    is_read INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 物流表
CREATE TABLE IF NOT EXISTS logistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    company VARCHAR(50),
    tracking_no VARCHAR(100),
    status VARCHAR(50) DEFAULT '待发货' COMMENT '待发货/已发货/运输中/已签收',
    details TEXT COMMENT '物流轨迹JSON',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入默认分类
INSERT INTO categories (name, parent_id, sort_order) VALUES
('数码产品', 0, 1),
('服装鞋帽', 0, 2),
('图书文具', 0, 3),
('家居用品', 0, 4),
('运动户外', 0, 5),
('美妆护肤', 0, 6),
('母婴用品', 0, 7),
('其他', 0, 99);
