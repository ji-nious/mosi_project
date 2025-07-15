package com.kh.project.domain.seller.dao;

import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.entity.MemberGubun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SellerDAO ?�합 ?�스?? */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "logging.level.com.kh.project=DEBUG",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
@Transactional
@DisplayName("SellerDAO ?�합 ?�스??)
class SellerDAOImplTest {

    @Autowired
    private SellerDAO sellerDAO;

    private Seller testSeller;

    @BeforeEach
    void setUp() {
        testSeller = createSampleSeller();
    }

    private Seller createSampleSeller() {
        Seller seller = new Seller();
        seller.setEmail("seller@shop.com");
        seller.setPassword("ShopPass123!");
        seller.setBizRegNo("111-22-33333");
        seller.setShopName("?�스?�상??);
        seller.setName("?�매?�이�?);
        seller.setShopAddress("부?�시 ?�구 ?�?�로");
        seller.setTel("010-1234-5678");
        seller.setGubun(MemberGubun.NEW.getCode());
        seller.setStatus("ACTIVE");
        return seller;
    }

    // ==================== ?�???�스??====================

    @Test
    @DisplayName("?�매???�??- ?�공")
    void save_success() {
        // when
        Seller savedSeller = sellerDAO.save(testSeller);

        // then
        assertNotNull(savedSeller);
        assertNotNull(savedSeller.getSellerId());
        assertEquals(testSeller.getEmail(), savedSeller.getEmail());
        assertEquals(testSeller.getBizRegNo(), savedSeller.getBizRegNo());
        assertEquals(testSeller.getShopName(), savedSeller.getShopName());
        assertNotNull(savedSeller.getCdate());
    }

    @Test
    @DisplayName("?�매???�??- ?�수 ?�드 ?�락???�패")
    void save_fail_missing_required_fields() {
        // given: ?�메?�이 null???�매??        testSeller.setEmail(null);

        // when & then: ?�외 발생 ?�상
        assertThrows(Exception.class, () -> {
            sellerDAO.save(testSeller);
        });
    }

    // ==================== 조회 ?�스??====================

    @Test
    @DisplayName("ID�??�매??조회 - ?�공")
    void findById_success() {
        // given: ?�매???�??        Seller savedSeller = sellerDAO.save(testSeller);

        // when: ID�?조회
        Optional<Seller> foundSeller = sellerDAO.findById(savedSeller.getSellerId());

        // then: 조회 ?�공 ?�인
        assertTrue(foundSeller.isPresent());
        assertEquals(savedSeller.getSellerId(), foundSeller.get().getSellerId());
        assertEquals(savedSeller.getEmail(), foundSeller.get().getEmail());
        assertEquals(savedSeller.getShopName(), foundSeller.get().getShopName());
    }

    @Test
    @DisplayName("ID�??�매??조회 - 존재?��? ?�는 ID")
    void findById_not_found() {
        // when: 존재?��? ?�는 ID�?조회
        Optional<Seller> foundSeller = sellerDAO.findById(999999L);

        // then: 조회 결과 ?�음
        assertFalse(foundSeller.isPresent());
    }

    @Test
    @DisplayName("?�메?�로 ?�매??조회 - ?�공")
    void findByEmail_success() {
        // given: ?�매???�??        sellerDAO.save(testSeller);

        // when: ?�메?�로 조회
        Optional<Seller> foundSeller = sellerDAO.findByEmail(testSeller.getEmail());

        // then: 조회 ?�공 ?�인
        assertTrue(foundSeller.isPresent());
        assertEquals(testSeller.getEmail(), foundSeller.get().getEmail());
    }

    @Test
    @DisplayName("?�메?�로 ?�매??조회 - 존재?��? ?�는 ?�메??)
    void findByEmail_not_found() {
        // when: 존재?��? ?�는 ?�메?�로 조회
        Optional<Seller> foundSeller = sellerDAO.findByEmail("notfound@seller.com");

        // then: 조회 결과 ?�음
        assertFalse(foundSeller.isPresent());
    }

    @Test
    @DisplayName("?�업?�등록번?�로 ?�매??조회 - ?�공")
    void findByBizRegNo_success() {
        // given: ?�매???�??        sellerDAO.save(testSeller);

        // when: ?�업?�등록번?�로 조회
        Optional<Seller> foundSeller = sellerDAO.findByBizRegNo(testSeller.getBizRegNo());

        // then: 조회 ?�공 ?�인
        assertTrue(foundSeller.isPresent());
        assertEquals(testSeller.getBizRegNo(), foundSeller.get().getBizRegNo());
    }

    @Test
    @DisplayName("?�업?�등록번?�로 ?�매??조회 - 존재?��? ?�는 번호")
    void findByBizRegNo_not_found() {
        // when: 존재?��? ?�는 ?�업?�등록번?�로 조회
        Optional<Seller> foundSeller = sellerDAO.findByBizRegNo("999-88-77777");

        // then: 조회 결과 ?�음
        assertFalse(foundSeller.isPresent());
    }

    // ==================== ?�데?�트 ?�스??====================

    @Test
    @DisplayName("?�매???�보 ?�정 - ?�공")
    void update_success() {
        // given: ?�매???�??        Seller savedSeller = sellerDAO.save(testSeller);

        // when: ?�보 ?�정
        Seller updateSeller = new Seller();
        updateSeller.setShopName("?�정?�상?�명");
        updateSeller.setShopAddress("부?�시 ?�운?��?);
        updateSeller.setTel("010-9999-8888");

        int updatedRows = sellerDAO.update(savedSeller.getSellerId(), updateSeller);

        // then: ?�정 ?�공 ?�인
        assertEquals(1, updatedRows);

        // ?�정???�이??검�?        Optional<Seller> updatedSellerOpt = sellerDAO.findById(savedSeller.getSellerId());
        assertTrue(updatedSellerOpt.isPresent());
        Seller updatedSellerData = updatedSellerOpt.get();
        assertEquals("?�정?�상?�명", updatedSellerData.getShopName());
        assertEquals("부?�시 ?�운?��?, updatedSellerData.getShopAddress());
        assertEquals("010-9999-8888", updatedSellerData.getTel());
    }

    @Test
    @DisplayName("?�매???�보 ?�정 - 존재?��? ?�는 ID")
    void update_not_found() {
        // given: ?�정???�이??        Seller updateSeller = new Seller();
        updateSeller.setShopName("?�정?�상?�명");

        // when: 존재?��? ?�는 ID�??�정 ?�도
        int updatedRows = sellerDAO.update(999999L, updateSeller);

        // then: ?�정?��? ?�음
        assertEquals(0, updatedRows);
    }

    // ==================== ?�퇴 ?�스??====================

    @Test
    @DisplayName("?�매???�퇴 처리 - ?�공")
    void withdrawWithReason_success() {
        // given: ?�매???�??        Seller savedSeller = sellerDAO.save(testSeller);

        // when: ?�퇴 처리
        String reason = "?�업 종료";
        int withdrawnRows = sellerDAO.withdrawWithReason(savedSeller.getSellerId(), reason);

        // then: ?�퇴 ?�공 ?�인
        assertEquals(1, withdrawnRows);

        // ?�퇴 ?�태 ?�인
        Optional<Seller> withdrawnSellerOpt = sellerDAO.findById(savedSeller.getSellerId());
        assertTrue(withdrawnSellerOpt.isPresent());
        Seller withdrawnSeller = withdrawnSellerOpt.get();
        assertEquals("WITHDRAWN", withdrawnSeller.getStatus());
        assertNotNull(withdrawnSeller.getWithdrawnAt());
        assertEquals(reason, withdrawnSeller.getWithdrawnReason());
    }

    @Test
    @DisplayName("?�매???�퇴 처리 - 존재?��? ?�는 ID")
    void withdrawWithReason_not_found() {
        // when: 존재?��? ?�는 ID�??�퇴 ?�도
        int withdrawnRows = sellerDAO.withdrawWithReason(999999L, "?�퇴 ?�유");

        // then: ?�퇴?��? ?�음
        assertEquals(0, withdrawnRows);
    }

    // ==================== 중복 체크 ?�스??====================

    @Test
    @DisplayName("?�메??중복 체크 - 중복??)
    void existsByEmail_true() {
        // given: ?�매???�??        sellerDAO.save(testSeller);

        // when: ?�메??중복 체크
        boolean exists = sellerDAO.existsByEmail(testSeller.getEmail());

        // then: 중복??        assertTrue(exists);
    }

    @Test
    @DisplayName("?�메??중복 체크 - 중복 ?�됨")
    void existsByEmail_false() {
        // when: 존재?��? ?�는 ?�메??중복 체크
        boolean exists = sellerDAO.existsByEmail("new@seller.com");

        // then: 중복 ?�됨
        assertFalse(exists);
    }

    @Test
    @DisplayName("?�업?�등록번??중복 체크 - 중복??)
    void existsByBizRegNo_true() {
        // given: ?�매???�??        sellerDAO.save(testSeller);

        // when: ?�업?�등록번??중복 체크
        boolean exists = sellerDAO.existsByBizRegNo(testSeller.getBizRegNo());

        // then: 중복??        assertTrue(exists);
    }

    @Test
    @DisplayName("?�업?�등록번??중복 체크 - 중복 ?�됨")
    void existsByBizRegNo_false() {
        // when: 존재?��? ?�는 ?�업?�등록번??중복 체크
        boolean exists = sellerDAO.existsByBizRegNo("999-88-77777");

        // then: 중복 ?�됨
        assertFalse(exists);
    }

    @Test
    @DisplayName("?�호�?중복 체크 - 중복??)
    void existsByShopName_true() {
        // given: ?�매???�??        sellerDAO.save(testSeller);

        // when: ?�호�?중복 체크
        boolean exists = sellerDAO.existsByShopName(testSeller.getShopName());

        // then: 중복??        assertTrue(exists);
    }

    @Test
    @DisplayName("?�호�?중복 체크 - 중복 ?�됨")
    void existsByShopName_false() {
        // when: 존재?��? ?�는 ?�호�?중복 체크
        boolean exists = sellerDAO.existsByShopName("?�상?�명");

        // then: 중복 ?�됨
        assertFalse(exists);
    }

    // ==================== 목록 조회 ?�스??====================

    @Test
    @DisplayName("?�체 ?�매??목록 조회")
    void findAll() {
        // given: ?�러 ?�매???�??        sellerDAO.save(testSeller);

        Seller seller2 = createSampleSeller();
        seller2.setEmail("seller2@shop.com");
        seller2.setBizRegNo("222-33-44444");
        seller2.setShopName("?�번째상??);
        sellerDAO.save(seller2);

        // when: ?�체 목록 조회
        List<Seller> sellers = sellerDAO.findAll();

        // then: ?�?�된 ?�매?�들??조회??        assertNotNull(sellers);
        assertTrue(sellers.size() >= 2);
        assertTrue(sellers.stream().anyMatch(s -> s.getEmail().equals(testSeller.getEmail())));
        assertTrue(sellers.stream().anyMatch(s -> s.getEmail().equals(seller2.getEmail())));
    }

    @Test
    @DisplayName("?�퇴???�매??목록 조회")
    void findWithdrawnMembers() {
        // given: ?�매???�?????�퇴 처리
        Seller savedSeller = sellerDAO.save(testSeller);
        sellerDAO.withdrawWithReason(savedSeller.getSellerId(), "?�스???�퇴");

        // when: ?�퇴???�매??목록 조회
        List<Seller> withdrawnSellers = sellerDAO.findWithdrawnMembers();

        // then: ?�퇴???�매?��? 조회??        assertNotNull(withdrawnSellers);
        assertTrue(withdrawnSellers.stream().anyMatch(s -> 
            s.getSellerId().equals(savedSeller.getSellerId()) && 
            "WITHDRAWN".equals(s.getStatus())
        ));
    }

    // ==================== Edge Case ?�스??====================

    @Test
    @DisplayName("?�?�문??구분 ?�메??중복 체크")
    void existsByEmail_case_sensitivity() {
        // given: ?�문???�메?�로 ?�??        sellerDAO.save(testSeller);

        // when: ?�문자�?중복 체크
        boolean existsUpper = sellerDAO.existsByEmail(testSeller.getEmail().toUpperCase());
        boolean existsLower = sellerDAO.existsByEmail(testSeller.getEmail().toLowerCase());

        // then: ?�?�문??구분 ?�인
        assertTrue(existsLower); // ?�본�??�일
        // existsUpper??DB ?�정???�라 ?�름
    }

    @Test
    @DisplayName("?�수문자 ?�함 ?�호�?처리")
    void handle_special_characters_in_shop_name() {
        // given: ?�수문자 ?�함 ?�호�?        testSeller.setShopName("?�스?�상??#$%");

        // when: ?�??�?조회
        Seller savedSeller = sellerDAO.save(testSeller);
        Optional<Seller> foundSeller = sellerDAO.findById(savedSeller.getSellerId());

        // then: ?�수문자 ?��? ?�인
        assertTrue(foundSeller.isPresent());
        assertEquals("?�스?�상??#$%", foundSeller.get().getShopName());
    }

    @Test
    @DisplayName("?�업?�등록번???�식 ?�스??)
    void bizRegNo_format_test() {
        // given: ?�양???�식???�업?�등록번??        testSeller.setBizRegNo("123-45-67890");

        // when: ?�??        Seller savedSeller = sellerDAO.save(testSeller);

        // then: ?�식 ?��? ?�인
        assertEquals("123-45-67890", savedSeller.getBizRegNo());
    }

    @Test
    @DisplayName("null �?처리 ?�스??)
    void handle_null_values() {
        // when & then: null ?�메?�로 중복 체크
        assertThrows(Exception.class, () -> {
            sellerDAO.existsByEmail(null);
        });

        // when & then: null ID�?조회
        assertThrows(Exception.class, () -> {
            sellerDAO.findById(null);
        });

        // when & then: null ?�업?�등록번?�로 중복 체크
        assertThrows(Exception.class, () -> {
            sellerDAO.existsByBizRegNo(null);
        });
    }

    @Test
    @DisplayName("?�시 ?�???�스??- ?�메??중복")
    void concurrent_save_test_email_duplicate() {
        // given: ?�일???�메?�을 가�????�매??        Seller seller1 = createSampleSeller();
        Seller seller2 = createSampleSeller();
        seller2.setBizRegNo("222-33-44444"); // ?�업?�번?�는 ?�르�?        seller2.setShopName("?�른?�점�?); // ?�점명도 ?�르�?
        // when: �?번째 ?�??        sellerDAO.save(seller1);

        // then: ??번째 ?�?�시 ?�메??중복 ?�러
        assertThrows(Exception.class, () -> {
            sellerDAO.save(seller2);
        });
    }

    @Test
    @DisplayName("?�시 ?�???�스??- ?�업?�등록번??중복")
    void concurrent_save_test_bizRegNo_duplicate() {
        // given: ?�일???�업?�등록번?��? 가�????�매??        Seller seller1 = createSampleSeller();
        Seller seller2 = createSampleSeller();
        seller2.setEmail("different@email.com"); // ?�메?��? ?�르�?        seller2.setShopName("?�른?�점�?); // ?�점명도 ?�르�?
        // when: �?번째 ?�??        sellerDAO.save(seller1);

        // then: ??번째 ?�?�시 ?�업?�등록번??중복 ?�러
        assertThrows(Exception.class, () -> {
            sellerDAO.save(seller2);
        });
    }

    @Test
    @DisplayName("매우 �?문자??처리 ?�스??)
    void handle_long_strings() {
        // given: 매우 �??�점�?        String longShopName = "??.repeat(500); // 매우 �??�점�?        testSeller.setShopName(longShopName);

        // when & then: 길이 ?�한 ?�인 (DB 컬럼 길이???�라)
        if (longShopName.length() > 255) { // ?�반?�인 문자??길이 ?�한
            assertThrows(Exception.class, () -> {
                sellerDAO.save(testSeller);
            });
        }
    }
} 
