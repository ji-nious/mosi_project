package com.kh.project.domain.buyer.dao;

import com.kh.project.domain.entity.Buyer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 구매자 DAO 구현체 (SQL 별칭 방식으로 단순화)
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class BuyerDAOImpl implements BuyerDAO {

    private final NamedParameterJdbcTemplate template;

    // SQL 별칭으로 컬럼명 매핑
    private final String BASE_SELECT = """
        SELECT 
            Buyer_id as buyerId,
            EMAIL as email,
            PASSWORD as password,
            NAME as name,
            NICKNAME as nickname,
            TEL as tel,
            GENDER as gender,
            BIRTH as birth,
            ADDRESS as address,
            MEMBER_GUBUN as memberGubun,
            PIC as pic,
            STATUS as status,
            CDATE as cdate,
            UDATE as udate,
            withdrawn_at as withdrawnAt,
            withdrawn_reason as withdrawnReason
        FROM buyer
        """;

    @Override
    public Buyer save(Buyer buyer) {
        String sql = """
            INSERT INTO buyer (EMAIL, PASSWORD, NAME, NICKNAME, TEL, GENDER, BIRTH, ADDRESS, MEMBER_GUBUN, STATUS) 
            VALUES (:email, :password, :name, :nickname, :tel, :gender, :birth, :address, :memberGubun, :status)
            """;

        SqlParameterSource param = new BeanPropertySqlParameterSource(buyer);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(sql, param, keyHolder, new String[]{"Buyer_id"});

        Long buyerId = keyHolder.getKey().longValue();
        buyer.setBuyerId(buyerId);

        log.info("구매자 저장 완료: buyerId={}", buyerId);
        return buyer;
    }

    @Override
    public Optional<Buyer> findById(Long buyerId) {
        String sql = BASE_SELECT + " WHERE Buyer_id = :buyerId AND STATUS != '탈퇴'";

        try {
            MapSqlParameterSource param = new MapSqlParameterSource("buyerId", buyerId);

            // BeanPropertyRowMapper 자동 매핑 (별칭 떄문에 가능)
            Buyer buyer = template.queryForObject(sql, param,
                BeanPropertyRowMapper.newInstance(Buyer.class));
            return Optional.of(buyer);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Buyer> findByEmail(String email) {
        String sql = BASE_SELECT + " WHERE EMAIL = :email AND STATUS != '탈퇴'";

        try {
            MapSqlParameterSource param = new MapSqlParameterSource("email", email);

            Buyer buyer = template.queryForObject(sql, param,
                BeanPropertyRowMapper.newInstance(Buyer.class));
            return Optional.of(buyer);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM buyer WHERE EMAIL = :email AND STATUS IN ('활성화', '비활성화', '정지')";

        MapSqlParameterSource param = new MapSqlParameterSource("email", email);

        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByNickname(String nickname) {
        String sql = "SELECT COUNT(*) FROM buyer WHERE NICKNAME = :nickname AND STATUS IN ('활성화', '비활성화', '정지')";

        MapSqlParameterSource param = new MapSqlParameterSource("nickname", nickname);

        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }


    @Override
    public int update(Long buyerId, Buyer buyer) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE buyer SET ");

        MapSqlParameterSource param = new MapSqlParameterSource();
        StringBuilder setClauses = new StringBuilder();

        // 동적 쿼리 생성 (null이 아닌 필드만 업데이트)
        if (buyer.getPassword() != null) {
            setClauses.append("PASSWORD = :password, ");
            param.addValue("password", buyer.getPassword());
        }
        if (buyer.getName() != null) {
            setClauses.append("NAME = :name, ");
            param.addValue("name", buyer.getName());
        }
        if (buyer.getNickname() != null) {
            setClauses.append("NICKNAME = :nickname, ");
            param.addValue("nickname", buyer.getNickname());
        }
        if (buyer.getTel() != null) {
            setClauses.append("TEL = :tel, ");
            param.addValue("tel", buyer.getTel());
        }
        if (buyer.getGender() != null) {
            setClauses.append("GENDER = :gender, ");
            param.addValue("gender", buyer.getGender());
        }
        if (buyer.getBirth() != null) {
            setClauses.append("BIRTH = :birth, ");
            param.addValue("birth", buyer.getBirth());
        }
        if (buyer.getAddress() != null) {
            setClauses.append("ADDRESS = :address, ");
            param.addValue("address", buyer.getAddress());
        }
        if (buyer.getMemberGubun() != null) {
            setClauses.append("MEMBER_GUBUN = :memberGubun, ");
            param.addValue("memberGubun", buyer.getMemberGubun());
        }

        // 항상 업데이트되는 필드
        setClauses.append("UDATE = SYSTIMESTAMP ");

        sql.append(setClauses.toString());
        sql.append("WHERE Buyer_id = :buyerId AND STATUS != '탈퇴'");

        param.addValue("buyerId", buyerId);

        return template.update(sql.toString(), param);
    }

    @Override
    public int withdrawWithReason(Long buyerId, String reason) {
        String sql = """
            UPDATE buyer SET 
                STATUS = '탈퇴', 
                UDATE = SYSTIMESTAMP,
                withdrawn_at = SYSDATE,
                withdrawn_reason = :reason
            WHERE Buyer_id = :buyerId AND STATUS = '활성화'
            """;

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("buyerId", buyerId);
        param.addValue("reason", reason);

        return template.update(sql, param);
    }

    @Override
    public List<Buyer> findWithdrawnMembers() {
        String sql = BASE_SELECT + " WHERE STATUS = '탈퇴' ORDER BY UDATE DESC";

        return template.query(sql, BeanPropertyRowMapper.newInstance(Buyer.class));
    }

    @Override
    public List<Buyer> findAll() {
        String sql = BASE_SELECT + " WHERE STATUS != '탈퇴' ORDER BY CDATE DESC";

        return template.query(sql, BeanPropertyRowMapper.newInstance(Buyer.class));
    }
}
