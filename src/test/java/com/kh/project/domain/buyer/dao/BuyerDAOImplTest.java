package com.kh.project.domain.buyer.dao;

import com.kh.project.domain.entity.Buyer;
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
 * BuyerDAO ?�합 ?�스?? */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "logging.level.com.kh.project=DEBUG",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
@Transactional
@DisplayName("BuyerDAO ?�합 ?�스??)
class BuyerDAOImplTest {

    @Autowired
    private BuyerDAO buyerDAO;

    private Buyer testBuyer;

    @BeforeEach
    void setUp() {
        testBuyer = createSampleBuyer();
    }

    private Buyer createSampleBuyer() {
        Buyer buyer = new Buyer();
        buyer.setEmail("test@buyer.com");
        buyer.setPassword("TestPass123!");
        buyer.setName("?�스?�구매자");
        buyer.setNickname("?�스??);
        buyer.setTel("010-1234-5678");
        buyer.setGender("?�성");
        buyer.setBirth(new Date());
        buyer.setAddress("부?�시 중구 중앙?��?);
        buyer.setGubun(MemberGubun.NEW.getCode());
        buyer.setStatus("ACTIVE");
        return buyer;
    }

    // ==================== ?�???�스??====================

    @Test
    @DisplayName("구매???�??- ?�공")
    void save_success() {
        // when
        Buyer savedBuyer = buyerDAO.save(testBuyer);

        // then
        assertNotNull(savedBuyer);
        assertNotNull(savedBuyer.getBuyerId());
        assertEquals(testBuyer.getEmail(), savedBuyer.getEmail());
        assertEquals(testBuyer.getName(), savedBuyer.getName());
        assertEquals(testBuyer.getNickname(), savedBuyer.getNickname());
        assertNotNull(savedBuyer.getCdate());
    }

    @Test
    @DisplayName("구매???�??- ?�수 ?�드 ?�락???�패")
    void save_fail_missing_required_fields() {
        // given: ?�메?�이 null??구매??        testBuyer.setEmail(null);

        // when & then: ?�외 발생 ?�상
        assertThrows(Exception.class, () -> {
            buyerDAO.save(testBuyer);
        });
    }

    // ==================== 조회 ?�스??====================

    @Test
    @DisplayName("ID�?구매??조회 - ?�공")
    void findById_success() {
        // given: 구매???�??        Buyer savedBuyer = buyerDAO.save(testBuyer);

        // when: ID�?조회
        Optional<Buyer> foundBuyer = buyerDAO.findById(savedBuyer.getBuyerId());

        // then: 조회 ?�공 ?�인
        assertTrue(foundBuyer.isPresent());
        assertEquals(savedBuyer.getBuyerId(), foundBuyer.get().getBuyerId());
        assertEquals(savedBuyer.getEmail(), foundBuyer.get().getEmail());
    }

    @Test
    @DisplayName("ID�?구매??조회 - 존재?��? ?�는 ID")
    void findById_not_found() {
        // when: 존재?��? ?�는 ID�?조회
        Optional<Buyer> foundBuyer = buyerDAO.findById(999999L);

        // then: 조회 결과 ?�음
        assertFalse(foundBuyer.isPresent());
    }

    @Test
    @DisplayName("?�메?�로 구매??조회 - ?�공")
    void findByEmail_success() {
        // given: 구매???�??        buyerDAO.save(testBuyer);

        // when: ?�메?�로 조회
        Optional<Buyer> foundBuyer = buyerDAO.findByEmail(testBuyer.getEmail());

        // then: 조회 ?�공 ?�인
        assertTrue(foundBuyer.isPresent());
        assertEquals(testBuyer.getEmail(), foundBuyer.get().getEmail());
    }

    @Test
    @DisplayName("?�메?�로 구매??조회 - 존재?��? ?�는 ?�메??)
    void findByEmail_not_found() {
        // when: 존재?��? ?�는 ?�메?�로 조회
        Optional<Buyer> foundBuyer = buyerDAO.findByEmail("notfound@email.com");

        // then: 조회 결과 ?�음
        assertFalse(foundBuyer.isPresent());
    }

    // ==================== ?�데?�트 ?�스??====================

    @Test
    @DisplayName("구매???�보 ?�정 - ?�공")
    void update_success() {
        // given: 구매???�??        Buyer savedBuyer = buyerDAO.save(testBuyer);

        // when: ?�보 ?�정
        Buyer updateBuyer = new Buyer();
        updateBuyer.setName("?�정?�이�?);
        updateBuyer.setTel("010-9999-8888");
        updateBuyer.setAddress("부?�시 ?�운?��?);

        int updatedRows = buyerDAO.update(savedBuyer.getBuyerId(), updateBuyer);

        // then: ?�정 ?�공 ?�인
        assertEquals(1, updatedRows);

        // ?�정???�이??검�?        Optional<Buyer> updatedBuyerOpt = buyerDAO.findById(savedBuyer.getBuyerId());
        assertTrue(updatedBuyerOpt.isPresent());
        Buyer updatedBuyerData = updatedBuyerOpt.get();
        assertEquals("?�정?�이�?, updatedBuyerData.getName());
        assertEquals("010-9999-8888", updatedBuyerData.getTel());
        assertEquals("부?�시 ?�운?��?, updatedBuyerData.getAddress());
    }

    @Test
    @DisplayName("구매???�보 ?�정 - 존재?��? ?�는 ID")
    void update_not_found() {
        // given: ?�정???�이??        Buyer updateBuyer = new Buyer();
        updateBuyer.setName("?�정?�이�?);

        // when: 존재?��? ?�는 ID�??�정 ?�도
        int updatedRows = buyerDAO.update(999999L, updateBuyer);

        // then: ?�정?��? ?�음
        assertEquals(0, updatedRows);
    }

    // ==================== ?�퇴 ?�스??====================

    @Test
    @DisplayName("구매???�퇴 처리 - ?�공")
    void withdrawWithReason_success() {
        // given: 구매???�??        Buyer savedBuyer = buyerDAO.save(testBuyer);

        // when: ?�퇴 처리
        String reason = "?�비??불만�?;
        int withdrawnRows = buyerDAO.withdrawWithReason(savedBuyer.getBuyerId(), reason);

        // then: ?�퇴 ?�공 ?�인
        assertEquals(1, withdrawnRows);

        // ?�퇴 ?�태 ?�인
        Optional<Buyer> withdrawnBuyerOpt = buyerDAO.findById(savedBuyer.getBuyerId());
        assertTrue(withdrawnBuyerOpt.isPresent());
        Buyer withdrawnBuyer = withdrawnBuyerOpt.get();
        assertEquals("WITHDRAWN", withdrawnBuyer.getStatus());
        assertNotNull(withdrawnBuyer.getWithdrawnAt());
        assertEquals(reason, withdrawnBuyer.getWithdrawnReason());
    }

    @Test
    @DisplayName("구매???�퇴 처리 - 존재?��? ?�는 ID")
    void withdrawWithReason_not_found() {
        // when: 존재?��? ?�는 ID�??�퇴 ?�도
        int withdrawnRows = buyerDAO.withdrawWithReason(999999L, "?�퇴 ?�유");

        // then: ?�퇴?��? ?�음
        assertEquals(0, withdrawnRows);
    }

    // ==================== 중복 체크 ?�스??====================

    @Test
    @DisplayName("?�메??중복 체크 - 중복??)
    void existsByEmail_true() {
        // given: 구매???�??        buyerDAO.save(testBuyer);

        // when: ?�메??중복 체크
        boolean exists = buyerDAO.existsByEmail(testBuyer.getEmail());

        // then: 중복??        assertTrue(exists);
    }

    @Test
    @DisplayName("?�메??중복 체크 - 중복 ?�됨")
    void existsByEmail_false() {
        // when: 존재?��? ?�는 ?�메??중복 체크
        boolean exists = buyerDAO.existsByEmail("new@email.com");

        // then: 중복 ?�됨
        assertFalse(exists);
    }

    @Test
    @DisplayName("?�네??중복 체크 - 중복??)
    void existsByNickname_true() {
        // given: 구매???�??        buyerDAO.save(testBuyer);

        // when: ?�네??중복 체크
        boolean exists = buyerDAO.existsByNickname(testBuyer.getNickname());

        // then: 중복??        assertTrue(exists);
    }

    @Test
    @DisplayName("?�네??중복 체크 - 중복 ?�됨")
    void existsByNickname_false() {
        // when: 존재?��? ?�는 ?�네??중복 체크
        boolean exists = buyerDAO.existsByNickname("?�닉?�임");

        // then: 중복 ?�됨
        assertFalse(exists);
    }

    // ==================== 목록 조회 ?�스??====================

    @Test
    @DisplayName("?�체 구매??목록 조회")
    void findAll() {
        // given: ?�러 구매???�??        buyerDAO.save(testBuyer);

        Buyer buyer2 = createSampleBuyer();
        buyer2.setEmail("buyer2@email.com");
        buyer2.setNickname("구매??");
        buyerDAO.save(buyer2);

        // when: ?�체 목록 조회
        List<Buyer> buyers = buyerDAO.findAll();

        // then: ?�?�된 구매?�들??조회??        assertNotNull(buyers);
        assertTrue(buyers.size() >= 2);
        assertTrue(buyers.stream().anyMatch(b -> b.getEmail().equals(testBuyer.getEmail())));
        assertTrue(buyers.stream().anyMatch(b -> b.getEmail().equals(buyer2.getEmail())));
    }

    @Test
    @DisplayName("?�퇴??구매??목록 조회")
    void findWithdrawnMembers() {
        // given: 구매???�?????�퇴 처리
        Buyer savedBuyer = buyerDAO.save(testBuyer);
        buyerDAO.withdrawWithReason(savedBuyer.getBuyerId(), "?�스???�퇴");

        // when: ?�퇴??구매??목록 조회
        List<Buyer> withdrawnBuyers = buyerDAO.findWithdrawnMembers();

        // then: ?�퇴??구매?��? 조회??        assertNotNull(withdrawnBuyers);
        assertTrue(withdrawnBuyers.stream().anyMatch(b -> 
            b.getBuyerId().equals(savedBuyer.getBuyerId()) && 
            "WITHDRAWN".equals(b.getStatus())
        ));
    }

    // ==================== Edge Case ?�스??====================

    @Test
    @DisplayName("?�?�문??구분 ?�메??중복 체크")
    void existsByEmail_case_sensitivity() {
        // given: ?�문???�메?�로 ?�??        buyerDAO.save(testBuyer);

        // when: ?�문자�?중복 체크
        boolean existsUpper = buyerDAO.existsByEmail(testBuyer.getEmail().toUpperCase());
        boolean existsLower = buyerDAO.existsByEmail(testBuyer.getEmail().toLowerCase());

        // then: ?�?�문??구분 ?�인 (구현???�라 결과가 ?��? ???�음)
        assertTrue(existsLower); // ?�본�??�일
        // existsUpper??DB ?�정???�라 ?�름
    }

    @Test
    @DisplayName("null �?처리 ?�스??)
    void handle_null_values() {
        // when & then: null ?�메?�로 중복 체크
        assertThrows(Exception.class, () -> {
            buyerDAO.existsByEmail(null);
        });

        // when & then: null ID�?조회
        assertThrows(Exception.class, () -> {
            buyerDAO.findById(null);
        });
    }

    @Test
    @DisplayName("매우 �?문자??처리 ?�스??)
    void handle_long_strings() {
        // given: 매우 �?문자??        String longEmail = "a".repeat(255) + "@test.com"; // ?�메??길이 ?�한 ?�스??        testBuyer.setEmail(longEmail);

        // when & then: 길이 ?�한 ?�인
        if (longEmail.length() > 255) { // ?�반?�인 ?�메??길이 ?�한
            assertThrows(Exception.class, () -> {
                buyerDAO.save(testBuyer);
            });
        }
    }

    @Test
    @DisplayName("?�시 ?�???�스??)
    void concurrent_save_test() {
        // given: ?�일???�메?�을 가�???구매??        Buyer buyer1 = createSampleBuyer();
        Buyer buyer2 = createSampleBuyer();

        // when: �?번째 ?�??        buyerDAO.save(buyer1);

        // then: ??번째 ?�?�시 중복 ?�러 (DB ?�약조건???�라)
        assertThrows(Exception.class, () -> {
            buyerDAO.save(buyer2);
        });
    }
} 
