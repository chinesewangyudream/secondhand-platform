-- ============================================
-- 增量迁移脚本 - 适用于已有数据的数据库
-- 在 Navicat 中逐条执行
-- ============================================

USE secondhand_db;

-- =====================
-- 1. 重命名表
-- =====================

ALTER TABLE category RENAME TO categories;
ALTER TABLE auction RENAME TO auctions;
ALTER TABLE auction_bid RENAME TO auction_bids;
ALTER TABLE favorite RENAME TO favorites;
ALTER TABLE address RENAME TO addresses;
ALTER TABLE chat_session RENAME TO chat_sessions;
ALTER TABLE chat_message RENAME TO chat_messages;

-- =====================
-- 2. goods 表
-- =====================

ALTER TABLE goods
    ADD COLUMN location VARCHAR(100),
    ADD COLUMN view_count INT DEFAULT 0,
    ADD COLUMN favorite_count INT DEFAULT 0,
    ADD COLUMN is_auction INT DEFAULT 0 COMMENT '0-普通商品 1-拍卖商品',
    ADD COLUMN ai_estimated_price DECIMAL(10,2);

ALTER TABLE goods MODIFY COLUMN status INT DEFAULT 0 COMMENT '0-待审核 1-在售 2-已售 3-下架 4-拍卖中';

-- =====================
-- 3. orders 表
-- =====================

-- 3.1 先迁移旧数据（如果存在 price 字段）
UPDATE orders SET original_price = price, amount = price WHERE original_price IS NULL;

-- 3.2 新增字段
ALTER TABLE orders
    ADD COLUMN original_price DECIMAL(10,2) COMMENT '商品原价',
    ADD COLUMN amount DECIMAL(10,2) COMMENT '实际交易价格',
    ADD COLUMN remark VARCHAR(255),
    ADD COLUMN session_id BIGINT COMMENT '关联的聊天会话ID',
    ADD COLUMN price_changed INT DEFAULT 0 COMMENT '0-未改价 1-已改价',
    ADD COLUMN pay_time DATETIME,
    ADD COLUMN ship_time DATETIME,
    ADD COLUMN confirm_time DATETIME;

-- 3.3 从旧 price 字段迁移数据
UPDATE orders SET original_price = price, amount = price WHERE price IS NOT NULL;

-- 3.4 删除旧的 price 字段（确认数据迁移完成后再执行）
ALTER TABLE orders DROP COLUMN price;

-- =====================
-- 4. logistics 表
-- =====================

-- 4.1 新增 details 字段
ALTER TABLE logistics ADD COLUMN details TEXT COMMENT '物流轨迹JSON';

-- 4.2 修改 status 字段类型
ALTER TABLE logistics ADD COLUMN status_new VARCHAR(50) DEFAULT '待发货';

UPDATE logistics SET status_new = CASE status
    WHEN 0 THEN '待发货'
    WHEN 1 THEN '已发货'
    WHEN 2 THEN '运输中'
    WHEN 3 THEN '已签收'
    ELSE '待发货'
END;

ALTER TABLE logistics DROP COLUMN status;
ALTER TABLE logistics CHANGE COLUMN status_new status VARCHAR(50) DEFAULT '待发货' COMMENT '待发货/已发货/运输中/已签收';

-- =====================
-- 5. addresses 表
-- =====================

ALTER TABLE addresses CHANGE COLUMN receiver_name receiver VARCHAR(50);
ALTER TABLE addresses CHANGE COLUMN detail_address detail VARCHAR(255);

-- =====================
-- 6. chat_sessions 表
-- =====================

-- 6.1 新增字段
ALTER TABLE chat_sessions
    ADD COLUMN buyer_id BIGINT,
    ADD COLUMN seller_id BIGINT,
    ADD COLUMN unread_count_buyer INT DEFAULT 0,
    ADD COLUMN unread_count_seller INT DEFAULT 0,
    ADD COLUMN last_message_time DATETIME;

-- 6.2 迁移数据（假设 user1_id 是买家，请根据实际情况调整）
UPDATE chat_sessions SET
    buyer_id = user1_id,
    seller_id = user2_id,
    last_message_time = last_time;

-- 6.3 删除旧字段
ALTER TABLE chat_sessions
    DROP COLUMN user1_id,
    DROP COLUMN user2_id,
    DROP COLUMN last_time;

-- =====================
-- 7. chat_messages 表
-- =====================

ALTER TABLE chat_messages CHANGE COLUMN type msg_type INT DEFAULT 0 COMMENT '0-文本 1-图片 2-系统消息';

-- =====================
-- 8. categories 表
-- =====================

ALTER TABLE categories
    ADD COLUMN icon VARCHAR(255),
    ADD COLUMN status INT DEFAULT 1 COMMENT '0-禁用 1-启用';

ALTER TABLE categories MODIFY COLUMN id INT AUTO_INCREMENT;
ALTER TABLE categories MODIFY COLUMN parent_id INT DEFAULT 0;

-- =====================
-- 9. auctions 表
-- =====================

ALTER TABLE auctions
    ADD COLUMN min_increment DECIMAL(10,2) DEFAULT 1.00 COMMENT '最低加价幅度',
    ADD COLUMN buy_now_price DECIMAL(10,2) COMMENT '一口价',
    ADD COLUMN bid_count INT DEFAULT 0;

ALTER TABLE auctions DROP COLUMN reserve_price;

-- =====================
-- 10. 添加索引
-- =====================

CREATE INDEX idx_orders_buyer_id ON orders(buyer_id);
CREATE INDEX idx_orders_seller_id ON orders(seller_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_goods_seller_id ON goods(seller_id);
CREATE INDEX idx_goods_status ON goods(status);
CREATE INDEX idx_chat_sessions_buyer ON chat_sessions(buyer_id);
CREATE INDEX idx_chat_sessions_seller ON chat_sessions(seller_id);

-- =====================
-- 完成
-- =====================

SELECT 'Migration completed!' AS message;
