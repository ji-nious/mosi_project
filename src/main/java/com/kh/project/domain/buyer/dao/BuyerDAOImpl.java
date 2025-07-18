package com.kh.project.domain.buyer.dao;

import com.kh.project.domain.entity.Buyer;
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
@SuppressWarnings("unused") // Spring에서 의존성 주입으로 사용됨
public class BuyerDAOImpl implements BuyerDAO {

    private final NamedParameterJdbcTemplate template;

    // 상수 정의 - Enum 기반
    private static final String STATUS_ACTIVE = MemberStatus.ACTIVE.getCode();
    private static final String STATUS_WITHDRAWN = MemberStatus.WITHDRAWN.getCode();

    // 기본 SELECT 문 (Oracle 컬럼명으로 수정)
    private static final String BASE_SELECT = """
        SELECT BUYER_ID as buyerId, EMAIL as email, PASSWORD as password, NAME as name, 
               NICKNAME as nickname, TEL as tel, GENDER as gender, BIRTH as birth, 
               POST_NUMBER as postNumber, ADDRESS as address, MEMBER_GUBUN as memberGubun, 
               PIC as pic, STATUS as status, CDATE as cdate, UDATE as udate, 
               WITHDRAWN_AT as withdrawnAt, WITHDRAWN_REASON as withdrawnReason
        FROM BUYER
        """;

    /**
     * 구매자 저장 (Oracle 스키마에 맞게 수정)
     */
    @Override
    public Buyer save(Buyer buyer) {
        log.info("구매자 저장: email={}", buyer.getEmail());

        // 먼저 시퀀스에서 다음 값을 가져옴
        String seqSql = "SELECT buyer_buyer_id.NEXTVAL FROM DUAL";
        Long buyerId = template.getJdbcTemplate().queryForObject(seqSql, Long.class);

        String sql = """
            INSERT INTO BUYER (BUYER_ID, EMAIL, PASSWORD, NAME, NICKNAME, TEL, GENDER, BIRTH, 
                              POST_NUMBER, ADDRESS, MEMBER_GUBUN, PIC, STATUS, CDATE, UDATE)
            VALUES (:buyerId, :email, :password, :name, :nickname, :tel, :gender, :birth, 
                    :postNumber, :address, :memberGubun, :pic, :status, SYSTIMESTAMP, SYSTIMESTAMP)
            """;

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("buyerId", buyerId);
        param.addValue("email", buyer.getEmail());
        param.addValue("password", buyer.getPassword());
        param.addValue("name", buyer.getName());
        param.addValue("nickname", buyer.getNickname());
        param.addValue("tel", buyer.getTel());
        param.addValue("gender", buyer.getGender());
        param.addValue("birth", buyer.getBirth());
        param.addValue("postNumber", buyer.getPostNumber());
        param.addValue("address", buyer.getAddress());
        param.addValue("memberGubun", buyer.getMemberGubun() != null ? buyer.getMemberGubun().getCode() : "NEW");
        param.addValue("pic", buyer.getPic());
        param.addValue("status", buyer.getStatus());

        try {
            template.update(sql, param);
            buyer.setBuyerId(buyerId);
            log.info("구매자 저장 완료: buyerId={}", buyerId);
        } catch (Exception e) {
            log.error("구매자 저장 실패: email={}, error={}", buyer.getEmail(), e.getMessage());

            // 상세한 제약 조건 오류 정보 제공
            if (e.getMessage().contains("ORA-00001")) {
                log.error("중복 제약 조건 위배 상세정보 - buyerId: {}, email: {}, nickname: {}",
                    buyerId, buyer.getEmail(), buyer.getNickname());
                log.error("전체 오류 메시지: {}", e.getMessage());
            }

            throw e;
        }

        return buyer;
    }

    /**
     * ID로 구매자 조회
     */
    @Override
    public Optional<Buyer> findById(Long buyerId) {
        String sql = BASE_SELECT + " WHERE BUYER_ID = :buyerId";

        MapSqlParameterSource param = new MapSqlParameterSource("buyerId", buyerId);

        try {
            Buyer buyer = template.queryForObject(sql, param, BeanPropertyRowMapper.newInstance(Buyer.class));
            return Optional.ofNullable(buyer);
        } catch (EmptyResultDataAccessException e) {
            log.warn("구매자를 찾을 수 없습니다: buyerId={}", buyerId);
            return Optional.empty();
        }
    }

    /**
     * 이메일로 구매자 조회
     */
    @Override
    public Optional<Buyer> findByEmail(String email) {
        String sql = BASE_SELECT + " WHERE EMAIL = :email";

        MapSqlParameterSource param = new MapSqlParameterSource("email", email);

        try {
            Buyer buyer = template.queryForObject(sql, param, BeanPropertyRowMapper.newInstance(Buyer.class));
            return Optional.ofNullable(buyer);
        } catch (EmptyResultDataAccessException e) {
            log.warn("이메일로 구매자를 찾을 수 없습니다: email={}", email);
            return Optional.empty();
        }
    }

    /**
     * 이메일 중복 확인 (탈퇴하지 않은 회원만)
     */
    @Override
    public boolean existsByEmail(String email) {
        try {
            String sql = "SELECT COUNT(*) FROM BUYER WHERE UPPER(EMAIL) = UPPER(:email)";

            MapSqlParameterSource param = new MapSqlParameterSource();
            param.addValue("email", email);

            Integer count = template.queryForObject(sql, param, Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            return false; // 테이블이 없으면 중복되지 않은 것으로 처리
        }
    }

    /**
     * 닉네임 중복 확인 (탈퇴하지 않은 회원만)
     */
    @Override
    public boolean existsByNickname(String nickname) {
        try {
            String sql = "SELECT COUNT(*) FROM BUYER WHERE NICKNAME = :nickname AND STATUS != :withdrawnStatus";

            MapSqlParameterSource param = new MapSqlParameterSource();
            param.addValue("nickname", nickname);
            param.addValue("withdrawnStatus", STATUS_WITHDRAWN);

            Integer count = template.queryForObject(sql, param, Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            return false; // 테이블이 없으면 중복되지 않은 것으로 처리
        }
    }

    /**
     * 구매자 정보 수정
     */
    @Override
    public int update(Long buyerId, Buyer buyer) {
        String sql = """
            UPDATE BUYER 
            SET PASSWORD = :password, NAME = :name, NICKNAME = :nickname, TEL = :tel, 
                GENDER = :gender, BIRTH = :birth, POST_NUMBER = :postNumber, ADDRESS = :address, 
                MEMBER_GUBUN = :memberGubun, PIC = :pic, STATUS = :status, UDATE = SYSTIMESTAMP
            WHERE BUYER_ID = :buyerId
            """;

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("buyerId", buyerId);
        param.addValue("password", buyer.getPassword());
        param.addValue("name", buyer.getName());
        param.addValue("nickname", buyer.getNickname());
        param.addValue("tel", buyer.getTel());
        param.addValue("gender", buyer.getGender());
        param.addValue("birth", buyer.getBirth());
        param.addValue("postNumber", buyer.getPostNumber());
        param.addValue("address", buyer.getAddress());
        param.addValue("memberGubun", buyer.getMemberGubun() != null ? buyer.getMemberGubun().getCode() : "NEW");
        param.addValue("pic", buyer.getPic());
        param.addValue("status", buyer.getStatus());

        return template.update(sql, param);
    }

    /**
     * 구매자 탈퇴 처리
     */
    @Override
    public int withdrawWithReason(Long buyerId, String reason) {
        String sql = """
            UPDATE BUYER 
            SET STATUS = :withdrawnStatus, 
                WITHDRAWN_AT = SYSTIMESTAMP, 
                WITHDRAWN_REASON = :reason, 
                UDATE = SYSTIMESTAMP
            WHERE BUYER_ID = :buyerId
            """;

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("buyerId", buyerId);
        param.addValue("reason", reason);
        param.addValue("withdrawnStatus", STATUS_WITHDRAWN);

        return template.update(sql, param);
    }

    /**
     * 탈퇴 회원 목록 조회
     */
    @Override
    public List<Buyer> findWithdrawnMembers() {
        String sql = BASE_SELECT + " WHERE WITHDRAWN_AT IS NOT NULL ORDER BY WITHDRAWN_AT DESC";

        return template.query(sql, BeanPropertyRowMapper.newInstance(Buyer.class));
    }

    /**
     * 전체 구매자 목록 조회
     */
    @Override
    public List<Buyer> findAll() {
        String sql = BASE_SELECT + " ORDER BY CDATE DESC";

        return template.query(sql, BeanPropertyRowMapper.newInstance(Buyer.class));
    }

    /**
     * 탈퇴 회원 재활성화
     */
    @Override
    public int reactivate(String email, String password) {
        String sql = """
            UPDATE BUYER 
            SET STATUS = :activeStatus, WITHDRAWN_AT = NULL, WITHDRAWN_REASON = NULL, UDATE = SYSTIMESTAMP
            WHERE EMAIL = :email AND PASSWORD = :password AND WITHDRAWN_AT IS NOT NULL
            """;

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("email", email);
        param.addValue("password", password);
        param.addValue("activeStatus", STATUS_ACTIVE);

        return template.update(sql, param);
    }

    /**
     * 탈퇴 회원 재가입 처리
     */
    @Override
    public int rejoin(Buyer buyer) {
        String sql = """
            UPDATE BUYER 
            SET PASSWORD = :password, NAME = :name, NICKNAME = :nickname, TEL = :tel, 
                GENDER = :gender, BIRTH = :birth, POST_NUMBER = :postNumber, ADDRESS = :address, 
                MEMBER_GUBUN = :memberGubun, PIC = :pic, STATUS = :activeStatus, 
                WITHDRAWN_AT = NULL, WITHDRAWN_REASON = NULL, UDATE = SYSTIMESTAMP
            WHERE EMAIL = :email AND WITHDRAWN_AT IS NOT NULL
            """;

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("email", buyer.getEmail());
        param.addValue("password", buyer.getPassword());
        param.addValue("name", buyer.getName());
        param.addValue("nickname", buyer.getNickname());
        param.addValue("tel", buyer.getTel());
        param.addValue("gender", buyer.getGender());
        param.addValue("birth", buyer.getBirth());
        param.addValue("postNumber", buyer.getPostNumber());
        param.addValue("address", buyer.getAddress());
        param.addValue("memberGubun", buyer.getMemberGubun() != null ? buyer.getMemberGubun().getCode() : "NEW");
        param.addValue("pic", buyer.getPic());
        param.addValue("activeStatus", STATUS_ACTIVE);

        return template.update(sql, param);
    }
}