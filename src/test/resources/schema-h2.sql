-- H2 테스트 데이터베이스 스키마 (실제 Oracle과 일치)

-- 구매자 테이블
CREATE TABLE IF NOT EXISTS buyer (
    buyer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(45) NOT NULL,
    nickname VARCHAR(24) NOT NULL UNIQUE,
    tel VARCHAR(13) NOT NULL,
    gender VARCHAR(6) CHECK (gender IN ('남성', '여성') OR gender IS NULL),
    birth DATE,
    address VARCHAR(200) NOT NULL,
    pic BLOB DEFAULT NULL,
    gubun VARCHAR(20) DEFAULT 'NEW',
    status VARCHAR(30) DEFAULT '활성화' CHECK (status IN ('활성화', '비활성화', '정지', '탈퇴')),
    withdrawn_at TIMESTAMP NULL,
    withdraw_reason VARCHAR(255) NULL,
    cdate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    udate TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 판매자 테이블
CREATE TABLE IF NOT EXISTS seller (
    seller_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    biz_reg_no VARCHAR(30) NOT NULL UNIQUE,
    shop_name VARCHAR(100) NOT NULL,
    name VARCHAR(30) NOT NULL,
    shop_address VARCHAR(200) NOT NULL,
    tel VARCHAR(13) NOT NULL,
    pic BLOB DEFAULT NULL,
    gubun VARCHAR(20) DEFAULT 'NEW',
    status VARCHAR(30) DEFAULT '활성화' CHECK (status IN ('활성화', '비활성화', '정지', '탈퇴')),
    withdrawn_at TIMESTAMP NULL,
    withdraw_reason VARCHAR(255) NULL,
    cdate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    udate TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_buyer_email ON buyer(email);
CREATE INDEX IF NOT EXISTS idx_buyer_nickname ON buyer(nickname);
CREATE INDEX IF NOT EXISTS idx_buyer_status ON buyer(status);
CREATE INDEX IF NOT EXISTS idx_buyer_gubun ON buyer(gubun);
CREATE INDEX IF NOT EXISTS idx_buyer_status_gubun ON buyer(status, gubun);

CREATE INDEX IF NOT EXISTS idx_seller_email ON seller(email);
CREATE INDEX IF NOT EXISTS idx_seller_biz_reg_no ON seller(biz_reg_no);
CREATE INDEX IF NOT EXISTS idx_seller_shop_name ON seller(shop_name);
CREATE INDEX IF NOT EXISTS idx_seller_status ON seller(status);
CREATE INDEX IF NOT EXISTS idx_seller_gubun ON seller(gubun);
CREATE INDEX IF NOT EXISTS idx_seller_status_gubun ON seller(status, gubun);
CREATE INDEX IF NOT EXISTS idx_seller_withdrawn_at ON seller(withdrawn_at); 