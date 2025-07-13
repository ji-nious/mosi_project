package com.kh.project.domain.seller.dao;

import com.kh.project.domain.entity.Seller;
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
 * 판매자 DAO 구현체 (SQL 별칭 방식으로 단순화)
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class SellerDAOImpl implements SellerDAO {

    private final NamedParameterJdbcTemplate template;

    // SQL 별칭으로 컬럼명 매핑
    private final String BASE_SELECT = """
        SELECT 
            seller_id as sellerId,
            EMAIL as email,
            PASSWORD as password,
            biz_reg_no as bizRegNo,
            shop_name as shopName,
            NAME as name,
            shop_address as shopAddress,
            TEL as tel,
            MEMBER_GUBUN as memberGubun,
            PIC as pic,
            STATUS as status,
            CDATE as cdate,
            UDATE as udate,
            withdrawn_at as withdrawnAt,
            withdrawn_reason as withdrawnReason
        FROM seller
        """;

    @Override
    public Seller save(Seller seller) {
        String sql = """
            INSERT INTO seller (EMAIL, PASSWORD, biz_reg_no, shop_name, NAME, shop_address, TEL, MEMBER_GUBUN, STATUS) 
            VALUES (:email, :password, :bizRegNo, :shopName, :name, :shopAddress, :tel, :memberGubun, :status)
            """;

        SqlParameterSource param = new BeanPropertySqlParameterSource(seller);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(sql, param, keyHolder, new String[]{"seller_id"});

        Long sellerId = keyHolder.getKey().longValue();
        seller.setSellerId(sellerId);

        log.info("판매자 저장 완료: sellerId={}", sellerId);
        return seller;
    }


    @Override
    public Optional<Seller> findById(Long sellerId) {
        String sql = BASE_SELECT + " WHERE seller_id = :sellerId AND STATUS != '탈퇴'";

        try {
            MapSqlParameterSource param = new MapSqlParameterSource("sellerId", sellerId);

            Seller seller = template.queryForObject(sql, param,
                BeanPropertyRowMapper.newInstance(Seller.class));
            return Optional.of(seller);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Seller> findByEmail(String email) {
        String sql = BASE_SELECT + " WHERE EMAIL = :email";

        try {
            MapSqlParameterSource param = new MapSqlParameterSource("email", email);

            Seller seller = template.queryForObject(sql, param,
                BeanPropertyRowMapper.newInstance(Seller.class));
            return Optional.of(seller);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Seller> findByBizRegNo(String bizRegNo) {
        String sql = BASE_SELECT + " WHERE biz_reg_no = :bizRegNo AND STATUS != '탈퇴'";

        try {
            MapSqlParameterSource param = new MapSqlParameterSource("bizRegNo", bizRegNo);

            Seller seller = template.queryForObject(sql, param,
                BeanPropertyRowMapper.newInstance(Seller.class));
            return Optional.of(seller);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM seller WHERE EMAIL = :email";

        MapSqlParameterSource param = new MapSqlParameterSource("email", email);

        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByBizRegNo(String bizRegNo) {
        String sql = "SELECT COUNT(*) FROM seller WHERE biz_reg_no = :bizRegNo AND STATUS IN ('활성화', '비활성화', '정지')";

        MapSqlParameterSource param = new MapSqlParameterSource("bizRegNo", bizRegNo);

        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByShopName(String shopName) {
        String sql = "SELECT COUNT(*) FROM seller WHERE shop_name = :shopName AND STATUS IN ('활성화', '비활성화', '정지')";

        MapSqlParameterSource param = new MapSqlParameterSource("shopName", shopName);

        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM seller WHERE NAME = :name AND STATUS IN ('활성화', '비활성화', '정지')";

        MapSqlParameterSource param = new MapSqlParameterSource("name", name);

        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByShopAddress(String shopAddress) {
        String sql = "SELECT COUNT(*) FROM seller WHERE shop_address = :shopAddress AND STATUS IN ('활성화', '비활성화', '정지')";

        MapSqlParameterSource param = new MapSqlParameterSource("shopAddress", shopAddress);

        Integer count = template.queryForObject(sql, param, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public int update(Long sellerId, Seller seller) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE seller SET ");

        MapSqlParameterSource param = new MapSqlParameterSource();
        StringBuilder setClauses = new StringBuilder();

        // 동적 쿼리 생성 (null이 아닌 필드만 업데이트)
        if (seller.getPassword() != null) {
            setClauses.append("PASSWORD = :password, ");
            param.addValue("password", seller.getPassword());
        }
        if (seller.getShopName() != null) {
            setClauses.append("shop_name = :shopName, ");
            param.addValue("shopName", seller.getShopName());
        }
        if (seller.getName() != null) {
            setClauses.append("NAME = :name, ");
            param.addValue("name", seller.getName());
        }
        if (seller.getShopAddress() != null) {
            setClauses.append("shop_address = :shopAddress, ");
            param.addValue("shopAddress", seller.getShopAddress());
        }
        if (seller.getTel() != null) {
            setClauses.append("TEL = :tel, ");
            param.addValue("tel", seller.getTel());
        }
        if (seller.getMemberGubun() != null) {
            setClauses.append("MEMBER_GUBUN = :memberGubun, ");
            param.addValue("memberGubun", seller.getMemberGubun());
        }

        // 항상 업데이트되는 필드
        setClauses.append("UDATE = SYSTIMESTAMP ");

        sql.append(setClauses.toString());
        sql.append("WHERE seller_id = :sellerId AND STATUS != '탈퇴'");

        param.addValue("sellerId", sellerId);

        return template.update(sql.toString(), param);
    }

    @Override
    public int withdrawWithReason(Long sellerId, String reason) {
        String sql = """
            UPDATE seller SET 
                STATUS = '탈퇴', 
                UDATE = SYSTIMESTAMP,
                withdrawn_at = SYSDATE,
                withdrawn_reason = :reason
            WHERE seller_id = :sellerId AND STATUS = '활성화'
            """;

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("sellerId", sellerId);
        param.addValue("reason", reason);

        return template.update(sql, param);
    }

    @Override
    public List<Seller> findWithdrawnMembers() {
        String sql = BASE_SELECT + " WHERE STATUS = '탈퇴' ORDER BY UDATE DESC";

        return template.query(sql, BeanPropertyRowMapper.newInstance(Seller.class));
    }

    @Override
    public List<Seller> findAll() {
        String sql = BASE_SELECT + " WHERE STATUS != '탈퇴' ORDER BY CDATE DESC";

        return template.query(sql, BeanPropertyRowMapper.newInstance(Seller.class));
    }

    @Override
    public int reactivate(String email, String password) {
        String sql = """
            UPDATE seller SET
                PASSWORD = :password,
                STATUS = '활성화',
                UDATE = SYSTIMESTAMP,
                withdrawn_at = null,
                withdrawn_reason = null
            WHERE email = :email AND STATUS = '탈퇴'
            """;
        
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("email", email);
        param.addValue("password", password);
        
        return template.update(sql, param);
    }

    @Override
    public int rejoin(Seller seller) {
        String sql = """
            UPDATE seller SET
                PASSWORD = :password,
                biz_reg_no = :bizRegNo,
                shop_name = :shopName,
                NAME = :name,
                shop_address = :shopAddress,
                TEL = :tel,
                STATUS = '활성화',
                UDATE = SYSTIMESTAMP,
                withdrawn_at = null,
                withdrawn_reason = null
            WHERE email = :email AND STATUS = '탈퇴'
            """;
        
        SqlParameterSource param = new BeanPropertySqlParameterSource(seller);
        return template.update(sql, param);
    }
}
