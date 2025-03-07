CREATE DATABASE IF NOT EXISTS eureka_project_db;
USE eureka_project_db;

-- 제품 테이블
CREATE TABLE products (
                          product_id INT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,        -- 제품 이름
                          manufacturer VARCHAR(50) NOT NULL, -- 제조사
                          price INT NOT NULL,                -- 가격
                          store_quantity INT DEFAULT 0,      -- 매장 재고 수량
                          warehouse_quantity INT DEFAULT 0   -- 창고 재고 수량
);

-- 판매 테이블
CREATE TABLE sales (
                       sale_id INT AUTO_INCREMENT PRIMARY KEY,
                       sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 판매 일시
                       total_amount INT NOT NULL                       -- 총 금액
);

-- 판매 상세 내역 테이블
CREATE TABLE sale_items (
                            sale_item_id INT AUTO_INCREMENT PRIMARY KEY,
                            sale_id INT NOT NULL,
                            product_id INT NOT NULL,
                            quantity INT NOT NULL DEFAULT 1,  -- 수량
                            unit_price INT NOT NULL,          -- 판매 당시 단가
                            total_price INT NOT NULL,         -- 총 가격(단가 * 수량)
                            FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE,
                            FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- 대기 주문 테이블
CREATE TABLE waiting_orders (
                                order_id INT AUTO_INCREMENT PRIMARY KEY,
                                product_id INT NOT NULL,
                                quantity INT NOT NULL,                           -- 주문 수량
                                request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 요청 일시
                                status VARCHAR(20) NOT NULL DEFAULT 'waiting',    -- 상태(waiting, processed, cancelled)
                                FOREIGN KEY (product_id) REFERENCES products(product_id)
);