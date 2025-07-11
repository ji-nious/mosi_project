-- ========================================
-- 📊 회원 구분 기능 포함 - 핵심 스키마만
-- ========================================

-- 시퀀스 생성
CREATE SEQUENCE buyer_buyer_id START WITH 1 INCREMENT BY 1 MAXVALUE 99999;
CREATE SEQUENCE seller_seller_id START WITH 1 INCREMENT BY 1 MAXVALUE 99999;

-- 구매자 테이블
CREATE TABLE BUYER (
   Buyer_id NUMBER(5) DEFAULT buyer_buyer_id.nextval PRIMARY KEY,
   EMAIL varchar2(50) NOT NULL UNIQUE,
   password varchar2(100) NOT NULL,
   name varchar2(45) NOT NULL,
   nickname varchar2(24) UNIQUE,
   tel varchar2(13) UNIQUE,                                       
   gender varchar2(6),
   birth DATE,
   address varchar2(200) NOT NULL,
   MEMBER_GUBUN VARCHAR2(10) DEFAULT 'NEW' NOT NULL,
   pic BLOB DEFAULT NULL,
   status varchar2(30) DEFAULT '활성화' CHECK (status IN ('활성화','비활성화','정지','탈퇴')),
   cdate TIMESTAMP DEFAULT SYSTIMESTAMP,
   udate TIMESTAMP DEFAULT SYSTIMESTAMP,
   withdrawn_at DATE,
   withdrawn_reason VARCHAR2(500),
   CONSTRAINT ck_buyer_gender CHECK (gender IN ('남성', '여성')),
   CONSTRAINT chk_buyer_member_gubun CHECK (MEMBER_GUBUN IN ('NEW', 'BRONZE', 'SILVER', 'GOLD'))
);

-- 판매자 테이블
CREATE TABLE seller (
   Seller_id NUMBER(5) DEFAULT seller_seller_id.nextval PRIMARY KEY,
   EMAIL varchar2(50) NOT NULL UNIQUE,
   password varchar2(100) NOT NULL,
   tel varchar2(13) NOT NULL,
   shop_name varchar2(100) NOT NULL UNIQUE,
   biz_reg_no varchar2(30) NOT NULL UNIQUE,
   name varchar2(30) NOT NULL UNIQUE,
   shop_address varchar2(200) NOT NULL UNIQUE,
   MEMBER_GUBUN VARCHAR2(10) DEFAULT 'NEW' NOT NULL,                                 
   pic BLOB DEFAULT NULL,
   status varchar2(30) DEFAULT '활성화' CHECK (status IN ('활성화','비활성화','정지','탈퇴')),
   cdate TIMESTAMP DEFAULT SYSTIMESTAMP,
   udate TIMESTAMP DEFAULT SYSTIMESTAMP,
   withdrawn_at DATE,
   withdrawn_reason VARCHAR2(500),
   CONSTRAINT chk_seller_member_gubun CHECK (MEMBER_GUBUN IN ('NEW', 'BRONZE', 'SILVER', 'GOLD'))
);

-- 필수 인덱스
CREATE INDEX idx_buyer_email ON buyer(email);
CREATE INDEX idx_buyer_status ON buyer(status);
CREATE INDEX idx_seller_email ON seller(email);
CREATE INDEX idx_seller_biz_reg_no ON seller(biz_reg_no);
CREATE INDEX idx_seller_status ON seller(status); 