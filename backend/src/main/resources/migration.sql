-- ============================================
-- 数据库增量迁移脚本
-- 执行前请备份数据库！
-- ============================================

-- 1. 重命名表（如果存在旧表名）
-- 注意：如果表名已经是新名称，跳过对应语句

-- 1.1 分类表
ALTER TABLE IF EXISTS category RENAME TO categories;

-- 1.2 拍卖表
ALTER TABLE IF EXISTS auction RENAME TO auctions;

-- 1.3 拍卖出价记录表
ALTER TABLE IF EXISTS auction_bid RENAME TO auction_bids;

-- 1.4 收藏表
ALTER TABLE IF EXISTS favorite RENAME TO favorites;

-- 1.5 收货地址表
ALTER TABLE IF EXISTS address RENAME TO addresses;

-- 1.6 聊天会话表
ALTER TABLE IF EXISTS chat_session RENAME TO chat_sessions;

-- 1.7 聊天消息表
ALTER TABLE IF EXISTS chat_message RENAME TO chat_messages;


-- ============================================
-- 2. 修改表结构
-- ============================================

-- 2.1 goods 商品表新增字段
ALTER TABLE goods
    ADD COLUMN IF NOT EXISTS location VARCHAR(100),
    ADD COLUMN IF NOT EXISTS view_count INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS favorite_count INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS is_auction INT DEFAULT 0 COMMENT '0-普通商品 1-拍卖商品',
    ADD COLUMN IF NOT EXISTS ai_estimated_price DECIMAL(10,2);

-- 修改 status 字段注释
ALTER TABLE goods MODIFY COLUMN status INT DEFAULT 0 COMMENT '0-待审核 1-在售 2-已售 3-下架 4-拍卖中';


-- 2.2 orders 订单表新增字段
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS original_price DECIMAL(10,2) COMMENT '商品原价',
    ADD COLUMN IF NOT EXISTS amount DECIMAL(10,2) COMMENT '实际交易价格',
    ADD COLUMN IF NOT EXISTS remark VARCHAR(255),
    ADD COLUMN IF NOT EXISTS session_id BIGINT COMMENT '关联的聊天会话ID',
    ADD COLUMN IF NOT EXISTS price_changed INT DEFAULT 0 COMMENT '0-未改价 1-已改价',
    ADD COLUMN IF NOT EXISTS pay_time DATETIME,
    ADD COLUMN IF NOT EXISTS ship_time DATETIME,
    ADD COLUMN IF NOT EXISTS confirm_time DATETIME;

-- 如果存在旧的 price 字段，迁移数据后删除
-- UPDATE orders SET original_price = price, amount = price WHERE original_price IS NULL;
-- ALTER TABLE orders DROP COLUMN IF EXISTS price;


-- 2.3 logistics 物流表修改
ALTER TABLE logistics
    ADD COLUMN IF NOT EXISTS details TEXT COMMENT '物流轨迹JSON';

-- 修改 status 字段类型（INT -> VARCHAR）
-- 需要先创建临时列，迁移数据，删除旧列，重命名
ALTER TABLE logistics ADD COLUMN IF NOT EXISTS status_new VARCHAR(50) DEFAULT '待发货';
UPDATE logistics SET status_new = CASE status
    WHEN 0 THEN '待发货'
    WHEN 1 THEN '已发货'
    WHEN 2 THEN '运输中'
    WHEN 3 THEN '已签收'
    ELSE '待发货'
END WHERE status_new IS NULL OR status_new = '待发货';
ALTER TABLE logistics DROP COLUMN IF EXISTS status;
ALTER TABLE logistics CHANGE COLUMN status_new status VARCHAR(50) DEFAULT '待发货' COMMENT '待发货/已发货/运输中/已签收';


-- 2.4 addresses 收货地址表修改字段名
ALTER TABLE addresses CHANGE COLUMN IF EXISTS receiver_name receiver VARCHAR(50);
ALTER TABLE addresses CHANGE COLUMN IF EXISTS detail_address detail VARCHAR(255);


-- 2.5 chat_sessions 聊天会话表重构
-- 先新增字段
ALTER TABLE chat_sessions
    ADD COLUMN IF NOT EXISTS buyer_id BIGINT,
    ADD COLUMN IF NOT EXISTS seller_id BIGINT,
    ADD COLUMN IF NOT EXISTS unread_count_buyer INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS unread_count_seller INT DEFAULT 0;

-- 迁移数据（根据业务逻辑确定 buyer 和 seller）
-- 假设 user1_id 是买家，user2_id 是卖家，根据实际情况调整
UPDATE chat_sessions SET buyer_id = user1_id, seller_id = user2_id WHERE buyer_id IS NULL;

-- 删除旧字段
ALTER TABLE chat_sessions
    DROP COLUMN IF EXISTS user1_id,
    DROP COLUMN IF EXISTS user2_id,
    DROP COLUMN IF EXISTS last_time;

-- 新增 last_message_time
ALTER TABLE chat_sessions ADD COLUMN IF NOT EXISTS last_message_time DATETIME;
UPDATE chat_sessions SET last_message_time = last_time WHERE last_message_time IS NULL;


-- 2.6 chat_messages 聊天消息表修改
ALTER TABLE chat_messages CHANGE COLUMN IF EXISTS type msg_type INT DEFAULT 0 COMMENT '0-文本 1-图片 2-系统消息';


-- 2.7 categories 分类表新增字段
ALTER TABLE categories
    ADD COLUMN IF NOT EXISTS icon VARCHAR(255),
    ADD COLUMN IF NOT EXISTS status INT DEFAULT 1 COMMENT '0-禁用 1-启用';

-- 修改 id 类型为 INT
ALTER TABLE categories MODIFY COLUMN id INT AUTO_INCREMENT;
ALTER TABLE categories MODIFY COLUMN parent_id INT DEFAULT 0;


-- 2.8 auctions 拍卖表新增字段
ALTER TABLE auctions
    ADD COLUMN IF NOT EXISTS min_increment DECIMAL(10,2) DEFAULT 1.00 COMMENT '最低加价幅度',
    ADD COLUMN IF NOT EXISTS buy_now_price DECIMAL(10,2) COMMENT '一口价',
    ADD COLUMN IF NOT EXISTS bid_count INT DEFAULT 0;

-- 删除不再需要的字段
ALTER TABLE auctions DROP COLUMN IF EXISTS reserve_price;


-- ============================================
-- 3. 索引优化
-- ============================================

-- 订单表索引
CREATE INDEX IF NOT EXISTS idx_orders_buyer_id ON orders(buyer_id);
CREATE INDEX IF NOT EXISTS idx_orders_seller_id ON orders(seller_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

-- 商品表索引
CREATE INDEX IF NOT EXISTS idx_goods_seller_id ON goods(seller_id);
CREATE INDEX IF NOT EXISTS idx_goods_status ON goods(status);
CREATE INDEX IF NOT EXISTS idx_goods_category_id ON goods(category_id);

-- 聊天会话索引
CREATE INDEX IF NOT EXISTS idx_chat_sessions_buyer ON chat_sessions(buyer_id);
CREATE INDEX IF NOT EXISTS idx_chat_sessions_seller ON chat_sessions(seller_id);


-- ============================================
-- 4. 数据迁移检查
-- ============================================

-- 检查 orders 表数据一致性
-- UPDATE orders SET original_price = amount WHERE original_price IS NULL;
-- UPDATE orders SET amount = original_price WHERE amount IS NULL;

-- 完成
SELECT 'Migration completed successfully!' AS message;
