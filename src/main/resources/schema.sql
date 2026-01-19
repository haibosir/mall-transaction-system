-- 用户账户表
CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'CNY',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- 商家账户表
CREATE TABLE IF NOT EXISTS merchant_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'CNY',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- 商品库存表
CREATE TABLE IF NOT EXISTS product_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    sku VARCHAR(100) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    currency VARCHAR(10) NOT NULL DEFAULT 'CNY',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_merchant_sku (merchant_id, sku)
);

-- 订单表
CREATE TABLE IF NOT EXISTS order_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    merchant_id BIGINT NOT NULL,
    sku VARCHAR(100) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    unit_price DECIMAL(19, 2) NOT NULL,
    quantity INT NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    currency VARCHAR(10) NOT NULL DEFAULT 'CNY',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- 创建索引
CREATE INDEX idx_user_account_user_id ON user_account(user_id);
CREATE INDEX idx_merchant_account_merchant_id ON merchant_account(merchant_id);
CREATE INDEX idx_product_inventory_merchant_id ON product_inventory(merchant_id);
CREATE INDEX idx_product_inventory_sku ON product_inventory(sku);
CREATE INDEX idx_order_info_order_no ON order_info(order_no);
CREATE INDEX idx_order_info_user_id ON order_info(user_id);
CREATE INDEX idx_order_info_merchant_id ON order_info(merchant_id);
CREATE INDEX idx_order_info_create_time ON order_info(create_time);
