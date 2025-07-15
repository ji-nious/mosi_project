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
 * BuyerDAO ?µí•© ?ŒìŠ¤?? */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "logging.level.com.kh.project=DEBUG",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
@Transactional
@DisplayName("BuyerDAO ?µí•© ?ŒìŠ¤??)
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
        buyer.setName("?ŒìŠ¤?¸êµ¬ë§¤ì");
        buyer.setNickname("?ŒìŠ¤??);
        buyer.setTel("010-1234-5678");
        buyer.setGender("?¨ì„±");
        buyer.setBirth(new Date());
        buyer.setAddress("ë¶€?°ì‹œ ì¤‘êµ¬ ì¤‘ì•™?€ë¡?);
        buyer.setGubun(MemberGubun.NEW.getCode());
        buyer.setStatus("ACTIVE");
        return buyer;
    }

    // ==================== ?€???ŒìŠ¤??====================

    @Test
    @DisplayName("êµ¬ë§¤???€??- ?±ê³µ")
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
    @DisplayName("êµ¬ë§¤???€??- ?„ìˆ˜ ?„ë“œ ?„ë½???¤íŒ¨")
    void save_fail_missing_required_fields() {
        // given: ?´ë©”?¼ì´ null??êµ¬ë§¤??        testBuyer.setEmail(null);

        // when & then: ?ˆì™¸ ë°œìƒ ?ˆìƒ
        assertThrows(Exception.class, () -> {
            buyerDAO.save(testBuyer);
        });
    }

    // ==================== ì¡°íšŒ ?ŒìŠ¤??====================

    @Test
    @DisplayName("IDë¡?êµ¬ë§¤??ì¡°íšŒ - ?±ê³µ")
    void findById_success() {
        // given: êµ¬ë§¤???€??        Buyer savedBuyer = buyerDAO.save(testBuyer);

        // when: IDë¡?ì¡°íšŒ
        Optional<Buyer> foundBuyer = buyerDAO.findById(savedBuyer.getBuyerId());

        // then: ì¡°íšŒ ?±ê³µ ?•ì¸
        assertTrue(foundBuyer.isPresent());
        assertEquals(savedBuyer.getBuyerId(), foundBuyer.get().getBuyerId());
        assertEquals(savedBuyer.getEmail(), foundBuyer.get().getEmail());
    }

    @Test
    @DisplayName("IDë¡?êµ¬ë§¤??ì¡°íšŒ - ì¡´ì¬?˜ì? ?ŠëŠ” ID")
    void findById_not_found() {
        // when: ì¡´ì¬?˜ì? ?ŠëŠ” IDë¡?ì¡°íšŒ
        Optional<Buyer> foundBuyer = buyerDAO.findById(999999L);

        // then: ì¡°íšŒ ê²°ê³¼ ?†ìŒ
        assertFalse(foundBuyer.isPresent());
    }

    @Test
    @DisplayName("?´ë©”?¼ë¡œ êµ¬ë§¤??ì¡°íšŒ - ?±ê³µ")
    void findByEmail_success() {
        // given: êµ¬ë§¤???€??        buyerDAO.save(testBuyer);

        // when: ?´ë©”?¼ë¡œ ì¡°íšŒ
        Optional<Buyer> foundBuyer = buyerDAO.findByEmail(testBuyer.getEmail());

        // then: ì¡°íšŒ ?±ê³µ ?•ì¸
        assertTrue(foundBuyer.isPresent());
        assertEquals(testBuyer.getEmail(), foundBuyer.get().getEmail());
    }

    @Test
    @DisplayName("?´ë©”?¼ë¡œ êµ¬ë§¤??ì¡°íšŒ - ì¡´ì¬?˜ì? ?ŠëŠ” ?´ë©”??)
    void findByEmail_not_found() {
        // when: ì¡´ì¬?˜ì? ?ŠëŠ” ?´ë©”?¼ë¡œ ì¡°íšŒ
        Optional<Buyer> foundBuyer = buyerDAO.findByEmail("notfound@email.com");

        // then: ì¡°íšŒ ê²°ê³¼ ?†ìŒ
        assertFalse(foundBuyer.isPresent());
    }

    // ==================== ?…ë°?´íŠ¸ ?ŒìŠ¤??====================

    @Test
    @DisplayName("êµ¬ë§¤???•ë³´ ?˜ì • - ?±ê³µ")
    void update_success() {
        // given: êµ¬ë§¤???€??        Buyer savedBuyer = buyerDAO.save(testBuyer);

        // when: ?•ë³´ ?˜ì •
        Buyer updateBuyer = new Buyer();
        updateBuyer.setName("?˜ì •?œì´ë¦?);
        updateBuyer.setTel("010-9999-8888");
        updateBuyer.setAddress("ë¶€?°ì‹œ ?´ìš´?€êµ?);

        int updatedRows = buyerDAO.update(savedBuyer.getBuyerId(), updateBuyer);

        // then: ?˜ì • ?±ê³µ ?•ì¸
        assertEquals(1, updatedRows);

        // ?˜ì •???°ì´??ê²€ì¦?        Optional<Buyer> updatedBuyerOpt = buyerDAO.findById(savedBuyer.getBuyerId());
        assertTrue(updatedBuyerOpt.isPresent());
        Buyer updatedBuyerData = updatedBuyerOpt.get();
        assertEquals("?˜ì •?œì´ë¦?, updatedBuyerData.getName());
        assertEquals("010-9999-8888", updatedBuyerData.getTel());
        assertEquals("ë¶€?°ì‹œ ?´ìš´?€êµ?, updatedBuyerData.getAddress());
    }

    @Test
    @DisplayName("êµ¬ë§¤???•ë³´ ?˜ì • - ì¡´ì¬?˜ì? ?ŠëŠ” ID")
    void update_not_found() {
        // given: ?˜ì •???°ì´??        Buyer updateBuyer = new Buyer();
        updateBuyer.setName("?˜ì •?œì´ë¦?);

        // when: ì¡´ì¬?˜ì? ?ŠëŠ” IDë¡??˜ì • ?œë„
        int updatedRows = buyerDAO.update(999999L, updateBuyer);

        // then: ?˜ì •?˜ì? ?ŠìŒ
        assertEquals(0, updatedRows);
    }

    // ==================== ?ˆí‡´ ?ŒìŠ¤??====================

    @Test
    @DisplayName("êµ¬ë§¤???ˆí‡´ ì²˜ë¦¬ - ?±ê³µ")
    void withdrawWithReason_success() {
        // given: êµ¬ë§¤???€??        Buyer savedBuyer = buyerDAO.save(testBuyer);

        // when: ?ˆí‡´ ì²˜ë¦¬
        String reason = "?œë¹„??ë¶ˆë§Œì¡?;
        int withdrawnRows = buyerDAO.withdrawWithReason(savedBuyer.getBuyerId(), reason);

        // then: ?ˆí‡´ ?±ê³µ ?•ì¸
        assertEquals(1, withdrawnRows);

        // ?ˆí‡´ ?íƒœ ?•ì¸
        Optional<Buyer> withdrawnBuyerOpt = buyerDAO.findById(savedBuyer.getBuyerId());
        assertTrue(withdrawnBuyerOpt.isPresent());
        Buyer withdrawnBuyer = withdrawnBuyerOpt.get();
        assertEquals("WITHDRAWN", withdrawnBuyer.getStatus());
        assertNotNull(withdrawnBuyer.getWithdrawnAt());
        assertEquals(reason, withdrawnBuyer.getWithdrawnReason());
    }

    @Test
    @DisplayName("êµ¬ë§¤???ˆí‡´ ì²˜ë¦¬ - ì¡´ì¬?˜ì? ?ŠëŠ” ID")
    void withdrawWithReason_not_found() {
        // when: ì¡´ì¬?˜ì? ?ŠëŠ” IDë¡??ˆí‡´ ?œë„
        int withdrawnRows = buyerDAO.withdrawWithReason(999999L, "?ˆí‡´ ?¬ìœ ");

        // then: ?ˆí‡´?˜ì? ?ŠìŒ
        assertEquals(0, withdrawnRows);
    }

    // ==================== ì¤‘ë³µ ì²´í¬ ?ŒìŠ¤??====================

    @Test
    @DisplayName("?´ë©”??ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ??)
    void existsByEmail_true() {
        // given: êµ¬ë§¤???€??        buyerDAO.save(testBuyer);

        // when: ?´ë©”??ì¤‘ë³µ ì²´í¬
        boolean exists = buyerDAO.existsByEmail(testBuyer.getEmail());

        // then: ì¤‘ë³µ??        assertTrue(exists);
    }

    @Test
    @DisplayName("?´ë©”??ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ ?ˆë¨")
    void existsByEmail_false() {
        // when: ì¡´ì¬?˜ì? ?ŠëŠ” ?´ë©”??ì¤‘ë³µ ì²´í¬
        boolean exists = buyerDAO.existsByEmail("new@email.com");

        // then: ì¤‘ë³µ ?ˆë¨
        assertFalse(exists);
    }

    @Test
    @DisplayName("?‰ë„¤??ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ??)
    void existsByNickname_true() {
        // given: êµ¬ë§¤???€??        buyerDAO.save(testBuyer);

        // when: ?‰ë„¤??ì¤‘ë³µ ì²´í¬
        boolean exists = buyerDAO.existsByNickname(testBuyer.getNickname());

        // then: ì¤‘ë³µ??        assertTrue(exists);
    }

    @Test
    @DisplayName("?‰ë„¤??ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ ?ˆë¨")
    void existsByNickname_false() {
        // when: ì¡´ì¬?˜ì? ?ŠëŠ” ?‰ë„¤??ì¤‘ë³µ ì²´í¬
        boolean exists = buyerDAO.existsByNickname("?ˆë‹‰?¤ì„");

        // then: ì¤‘ë³µ ?ˆë¨
        assertFalse(exists);
    }

    // ==================== ëª©ë¡ ì¡°íšŒ ?ŒìŠ¤??====================

    @Test
    @DisplayName("?„ì²´ êµ¬ë§¤??ëª©ë¡ ì¡°íšŒ")
    void findAll() {
        // given: ?¬ëŸ¬ êµ¬ë§¤???€??        buyerDAO.save(testBuyer);

        Buyer buyer2 = createSampleBuyer();
        buyer2.setEmail("buyer2@email.com");
        buyer2.setNickname("êµ¬ë§¤??");
        buyerDAO.save(buyer2);

        // when: ?„ì²´ ëª©ë¡ ì¡°íšŒ
        List<Buyer> buyers = buyerDAO.findAll();

        // then: ?€?¥ëœ êµ¬ë§¤?ë“¤??ì¡°íšŒ??        assertNotNull(buyers);
        assertTrue(buyers.size() >= 2);
        assertTrue(buyers.stream().anyMatch(b -> b.getEmail().equals(testBuyer.getEmail())));
        assertTrue(buyers.stream().anyMatch(b -> b.getEmail().equals(buyer2.getEmail())));
    }

    @Test
    @DisplayName("?ˆí‡´??êµ¬ë§¤??ëª©ë¡ ì¡°íšŒ")
    void findWithdrawnMembers() {
        // given: êµ¬ë§¤???€?????ˆí‡´ ì²˜ë¦¬
        Buyer savedBuyer = buyerDAO.save(testBuyer);
        buyerDAO.withdrawWithReason(savedBuyer.getBuyerId(), "?ŒìŠ¤???ˆí‡´");

        // when: ?ˆí‡´??êµ¬ë§¤??ëª©ë¡ ì¡°íšŒ
        List<Buyer> withdrawnBuyers = buyerDAO.findWithdrawnMembers();

        // then: ?ˆí‡´??êµ¬ë§¤?ê? ì¡°íšŒ??        assertNotNull(withdrawnBuyers);
        assertTrue(withdrawnBuyers.stream().anyMatch(b -> 
            b.getBuyerId().equals(savedBuyer.getBuyerId()) && 
            "WITHDRAWN".equals(b.getStatus())
        ));
    }

    // ==================== Edge Case ?ŒìŠ¤??====================

    @Test
    @DisplayName("?€?Œë¬¸??êµ¬ë¶„ ?´ë©”??ì¤‘ë³µ ì²´í¬")
    void existsByEmail_case_sensitivity() {
        // given: ?Œë¬¸???´ë©”?¼ë¡œ ?€??        buyerDAO.save(testBuyer);

        // when: ?€ë¬¸ìë¡?ì¤‘ë³µ ì²´í¬
        boolean existsUpper = buyerDAO.existsByEmail(testBuyer.getEmail().toUpperCase());
        boolean existsLower = buyerDAO.existsByEmail(testBuyer.getEmail().toLowerCase());

        // then: ?€?Œë¬¸??êµ¬ë¶„ ?•ì¸ (êµ¬í˜„???°ë¼ ê²°ê³¼ê°€ ?¤ë? ???ˆìŒ)
        assertTrue(existsLower); // ?ë³¸ê³??™ì¼
        // existsUpper??DB ?¤ì •???°ë¼ ?¤ë¦„
    }

    @Test
    @DisplayName("null ê°?ì²˜ë¦¬ ?ŒìŠ¤??)
    void handle_null_values() {
        // when & then: null ?´ë©”?¼ë¡œ ì¤‘ë³µ ì²´í¬
        assertThrows(Exception.class, () -> {
            buyerDAO.existsByEmail(null);
        });

        // when & then: null IDë¡?ì¡°íšŒ
        assertThrows(Exception.class, () -> {
            buyerDAO.findById(null);
        });
    }

    @Test
    @DisplayName("ë§¤ìš° ê¸?ë¬¸ì??ì²˜ë¦¬ ?ŒìŠ¤??)
    void handle_long_strings() {
        // given: ë§¤ìš° ê¸?ë¬¸ì??        String longEmail = "a".repeat(255) + "@test.com"; // ?´ë©”??ê¸¸ì´ ?œí•œ ?ŒìŠ¤??        testBuyer.setEmail(longEmail);

        // when & then: ê¸¸ì´ ?œí•œ ?•ì¸
        if (longEmail.length() > 255) { // ?¼ë°˜?ì¸ ?´ë©”??ê¸¸ì´ ?œí•œ
            assertThrows(Exception.class, () -> {
                buyerDAO.save(testBuyer);
            });
        }
    }

    @Test
    @DisplayName("?™ì‹œ ?€???ŒìŠ¤??)
    void concurrent_save_test() {
        // given: ?™ì¼???´ë©”?¼ì„ ê°€ì§???êµ¬ë§¤??        Buyer buyer1 = createSampleBuyer();
        Buyer buyer2 = createSampleBuyer();

        // when: ì²?ë²ˆì§¸ ?€??        buyerDAO.save(buyer1);

        // then: ??ë²ˆì§¸ ?€?¥ì‹œ ì¤‘ë³µ ?ëŸ¬ (DB ?œì•½ì¡°ê±´???°ë¼)
        assertThrows(Exception.class, () -> {
            buyerDAO.save(buyer2);
        });
    }
} 
