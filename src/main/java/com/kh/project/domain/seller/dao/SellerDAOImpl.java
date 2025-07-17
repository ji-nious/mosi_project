package com.kh.project.domain.seller.dao;

import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.entity.MemberStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class SellerDAOImpl implements SellerDAO {

    private final NamedParameterJdbcTemplate template;

    // 상수 정의 - Enum 기반 (String으로 변경)
    private static final String STATUS_ACTIVE = MemberStatus.ACTIVE.getCode();
    private static final String STATUS_WITHDRAWN = MemberStatus.WITHDRAWN.getCode();

    // 로그인용 SELECT 문 (PIC 컬럼 제거하여 BLOB 에러 방지)
    private static final String LOGIN_SELECT = """
        SELECT SELLER_ID as sellerId, EMAIL as email, PASSWORD as password, BIZ_REG_NO as bizRegNo,
               SHOP_NAME as shopName, NAME as name, SHOP_ADDRESS as shopAddress, TEL as tel,
               POST_NUMBER as postNumber, STATUS as status,
               CDATE as cdate, UDATE as udate, WITHDRAWN_AT as withdrawnAt, WITHDRAWN_REASON as withdrawnReason
        FROM SELLER
        """;

    // 상세 정보용 SELECT 문 (PIC 포함)
    private static final String DETAIL_SELECT = """
        SELECT SELLER_ID as sellerId, EMAIL as email, PASSWORD as password, BIZ_REG_NO as bizRegNo,
               SHOP_NAME as shopName, NAME as name, SHOP_ADDRESS as shopAddress, TEL as tel,
               PIC as pic, POST_NUMBER as postNumber, STATUS as status,
               CDATE as cdate, UDATE as udate, WITHDRAWN_AT as withdrawnAt, WITHDRAWN_REASON as withdrawnReason
        FROM SELLER
        """;

    /**
     * 판매자 저장
     */
    @Override
    public Seller save(Seller seller) {
        log.info("판매자 저장: email={}", seller.getEmail());

        // 시퀀스에서 다음 값을 가져옴
        String seqSql = "SELECT seller_seller_id.NEXTVAL FROM DUAL";
        Long sellerId = template.getJdbcTemplate().queryForObject(seqSql, Long.class);

        String sql = """
            INSERT INTO SELLER (SELLER_ID, EMAIL, PASSWORD, BIZ_REG_NO, SHOP_NAME, NAME, SHOP_ADDRESS, TEL, PIC, POST_NUMBER, STATUS, CDATE, UDATE)
            VALUES (:sellerId, :email, :password, :bizRegNo, :shopName, :name, :shopAddress, :tel, :pic, :postNumber, :status, SYSTIMESTAMP, SYSTIMESTAMP)
            """;

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("sellerId", sellerId);
        param.addValue("email", seller.getEmail());
        param.addValue("password", seller.getPassword());
        param.addValue("bizRegNo", seller.getBizRegNo());
        param.addValue("shopName", seller.getShopName());
        param.addValue("name", seller.getName());
        param.addValue("shopAddress", seller.getShopAddress());
        param.addValue("tel", seller.getTel());
        param.addValue("pic", seller.getPic());
        param.addValue("postNumber", seller.getPostNumber());
        param.addValue("status", seller.getStatus());

        try {
            template.update(sql, param);
            seller.setSellerId(sellerId);
            log.info("판매자 저장 완료: sellerId={}", sellerId);
        } catch (Exception e) {
            log.error("판매자 저장 실패: email={}, error={}", seller.getEmail(), e.getMessage());
            throw e;
        }

        return seller;
    }

    /**
     * ID로 판매자 조회
     */
    @Override
    public Optional<Seller> findById(Long sellerId) {
        String sql = DETAIL_SELECT + " WHERE SELLER_ID = :sellerId";

        MapSqlParameterSource param = new MapSqlParameterSource("sellerId", sellerId);

        try {
            Seller seller = template.queryForObject(sql, param, BeanPropertyRowMapper.newInstance(Seller.class));
            return Optional.ofNullable(seller);
        } catch (EmptyResultDataAccessException e) {
            log.warn("판매자를 찾을 수 없습니다: sellerId={}", sellerId);
            return Optional.empty();
        }
    }

    /**
     * 이메일로 판매자 조회
     */
    @Override
    public Optional<Seller> findByEmail(String email) {
        String sql = LOGIN_SELECT + " WHERE EMAIL = :email";

        MapSqlParameterSource param = new MapSqlParameterSource("email", email);

        try {
            Seller seller = template.queryForObject(sql, param, BeanPropertyRowMapper.newInstance(Seller.class));
            return Optional.ofNullable(seller);
        } catch (EmptyResultDataAccessException e) {
            log.warn("이메일로 판매자를 찾을 수 없습니다: email={}", email);
            return Optional.empty();
        }
    }

    /**
     * 이메일 중복 체크 (모든 상태)
     */
    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM SELLER WHERE EMAIL = :email";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("email", email);

        Integer count = template.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    /**
     * 이메일 중복 체크 (특정 상태 제외)
     */
    @Override
    public boolean existsByEmailAndStatusNot(String email, String excludeStatus) {
        String sql = "SELECT COUNT(*) FROM SELLER WHERE EMAIL = :email AND STATUS != :excludeStatus";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("email", email);
        params.addValue("excludeStatus", excludeStatus);

        Integer count = template.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    /**
     * 사업자번호 중복 체크 (모든 상태)
     */
    @Override
    public boolean existsByBizRegNo(String bizRegNo) {
        String sql = "SELECT COUNT(*) FROM SELLER WHERE BIZ_REG_NO = :bizRegNo";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("bizRegNo", bizRegNo);

        Integer count = template.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    /**
     * 상호명 중복 체크 (활성 회원만)
     */
    @Override
    public boolean existsByShopName(String shopName) {
        String sql = "SELECT COUNT(*) FROM SELLER WHERE SHOP_NAME = :shopName AND STATUS != :withdrawnStatus";

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("shopName", shopName);
        param.addValue("withdrawnStatus", STATUS_WITHDRAWN);

        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    /**
     * 대표자명 중복 체크 (활성 회원만)
     */
    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM SELLER WHERE NAME = :name AND STATUS != :withdrawnStatus";

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("name", name);
        param.addValue("withdrawnStatus", STATUS_WITHDRAWN);

        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    /**
     * 사업자등록번호로 판매자 조회
     */
    @Override
    public Optional<Seller> findByBizRegNo(String bizRegNo) {
        String sql = DETAIL_SELECT + " WHERE BIZ_REG_NO = :bizRegNo";

        MapSqlParameterSource param = new MapSqlParameterSource("bizRegNo", bizRegNo);

        try {
            Seller seller = template.queryForObject(sql, param, BeanPropertyRowMapper.newInstance(Seller.class));
            return Optional.ofNullable(seller);
        } catch (EmptyResultDataAccessException e) {
            log.warn("사업자등록번호로 판매자를 찾을 수 없습니다: bizRegNo={}", bizRegNo);
            return Optional.empty();
        }
    }

    /**
     * 판매자 정보 수정
     */
    @Override
    public int update(Long sellerId, Seller seller) {
        String sql = """
            UPDATE SELLER 
            SET PASSWORD = :password, BIZ_REG_NO = :bizRegNo, SHOP_NAME = :shopName, NAME = :name,
                SHOP_ADDRESS = :shopAddress, TEL = :tel, PIC = :pic, POST_NUMBER = :postNumber,
                STATUS = :status, UDATE = SYSTIMESTAMP
            WHERE SELLER_ID = :sellerId
            """;

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("sellerId", sellerId);
        param.addValue("password", seller.getPassword());
        param.addValue("bizRegNo", seller.getBizRegNo());
        param.addValue("shopName", seller.getShopName());
        param.addValue("name", seller.getName());
        param.addValue("shopAddress", seller.getShopAddress());
        param.addValue("tel", seller.getTel());
        param.addValue("pic", seller.getPic());
        param.addValue("postNumber", seller.getPostNumber());
        param.addValue("status", seller.getStatus());

        return template.update(sql, param);
    }

    /**
     * 판매자 탈퇴 처리
     */
    @Override
    public int withdrawWithReason(Long sellerId, String reason) {
        String sql = """
            UPDATE SELLER 
            SET STATUS = :withdrawnStatus, 
                WITHDRAWN_AT = SYSTIMESTAMP, 
                WITHDRAWN_REASON = :reason, 
                UDATE = SYSTIMESTAMP
            WHERE SELLER_ID = :sellerId AND STATUS = :activeStatus
            """;

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("sellerId", sellerId);
        param.addValue("reason", reason);
        param.addValue("withdrawnStatus", STATUS_WITHDRAWN);
        param.addValue("activeStatus", STATUS_ACTIVE);

        return template.update(sql, param);
    }

    /**
     * 탈퇴 회원 목록 조회
     */
    @Override
    public List<Seller> findWithdrawnMembers() {
        String sql = DETAIL_SELECT + " WHERE WITHDRAWN_AT IS NOT NULL ORDER BY WITHDRAWN_AT DESC";

        return template.query(sql, BeanPropertyRowMapper.newInstance(Seller.class));
    }

    /**
     * 전체 판매자 목록 조회
     */
    @Override
    public List<Seller> findAll() {
        String sql = DETAIL_SELECT + " ORDER BY CDATE DESC";

        return template.query(sql, BeanPropertyRowMapper.newInstance(Seller.class));
    }

    /**
     * 탈퇴 판매자 재활성화
     */
    @Override
    public int reactivate(String email, String password) {
        String sql = """
            UPDATE SELLER 
            SET STATUS = :activeStatus, WITHDRAWN_AT = NULL, WITHDRAWN_REASON = NULL, UDATE = SYSTIMESTAMP
            WHERE EMAIL = :email AND PASSWORD = :password AND STATUS = :withdrawnStatus
            """;

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("email", email);
        param.addValue("password", password);
        param.addValue("activeStatus", STATUS_ACTIVE);
        param.addValue("withdrawnStatus", STATUS_WITHDRAWN);

        return template.update(sql, param);
    }

    /**
     * 탈퇴 판매자 재가입 처리
     */
    @Override
    public int rejoin(Seller seller) {
        String sql = """
            UPDATE SELLER 
            SET PASSWORD = :password, BIZ_REG_NO = :bizRegNo, SHOP_NAME = :shopName, NAME = :name,
                SHOP_ADDRESS = :shopAddress, TEL = :tel, PIC = :pic, POST_NUMBER = :postNumber,
                STATUS = :activeStatus, WITHDRAWN_AT = NULL, WITHDRAWN_REASON = NULL, UDATE = SYSTIMESTAMP
            WHERE EMAIL = :email AND STATUS = :withdrawnStatus
            """;

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("email", seller.getEmail());
        param.addValue("password", seller.getPassword());
        param.addValue("bizRegNo", seller.getBizRegNo());
        param.addValue("shopName", seller.getShopName());
        param.addValue("name", seller.getName());
        param.addValue("shopAddress", seller.getShopAddress());
        param.addValue("tel", seller.getTel());
        param.addValue("pic", seller.getPic());
        param.addValue("postNumber", seller.getPostNumber());
        param.addValue("activeStatus", STATUS_ACTIVE);
        param.addValue("withdrawnStatus", STATUS_WITHDRAWN);

        return template.update(sql, param);
    }


    @Override
    public boolean existsByEmailAndStatus(String email, String status) {
        String sql = "SELECT COUNT(*) FROM SELLER WHERE EMAIL = :email AND STATUS = :status";

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("email", email);
        param.addValue("status", status);

        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByBizRegNoAndStatus(String bizRegNo, String status) {
        String sql = "SELECT COUNT(*) FROM SELLER WHERE BIZ_REG_NO = :bizRegNo AND STATUS = :status";

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("bizRegNo", bizRegNo);
        param.addValue("status", status);

        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByTelAndStatus(String tel, String status) {
        String sql = "SELECT COUNT(*) FROM SELLER WHERE TEL = :tel AND STATUS = :status";

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("tel", tel);
        param.addValue("status", status);

        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    // ========================================
    // 복합 키 전용 메서드들 (String 파라미터로 수정)
    // ========================================

    @Override
    public Optional<Seller> findByBizRegNoAndStatus(String bizRegNo, String status) {
        String sql = DETAIL_SELECT + " WHERE BIZ_REG_NO = :bizRegNo AND STATUS = :status";

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("bizRegNo", bizRegNo);
        param.addValue("status", status);

        try {
            Seller seller = template.queryForObject(sql, param, BeanPropertyRowMapper.newInstance(Seller.class));
            return Optional.ofNullable(seller);
        } catch (EmptyResultDataAccessException e) {
            log.warn("사업자번호와 상태로 판매자를 찾을 수 없습니다: bizRegNo={}, status={}", bizRegNo, status);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Seller> findActiveSeller(String bizRegNo) {
        return findByBizRegNoAndStatus(bizRegNo, STATUS_ACTIVE);
    }

    @Override
    public Optional<Seller> findWithdrawnSeller(String bizRegNo) {
        return findByBizRegNoAndStatus(bizRegNo, STATUS_WITHDRAWN);
    }

    @Override
    public List<Seller> findAllByBizRegNo(String bizRegNo) {
        String sql = DETAIL_SELECT + " WHERE BIZ_REG_NO = :bizRegNo ORDER BY CDATE DESC";

        MapSqlParameterSource param = new MapSqlParameterSource("bizRegNo", bizRegNo);

        return template.query(sql, param, BeanPropertyRowMapper.newInstance(Seller.class));
    }
}