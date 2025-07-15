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
 * SellerDAO ?µí•© ?ŒìŠ¤?? */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "logging.level.com.kh.project=DEBUG",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
@Transactional
@DisplayName("SellerDAO ?µí•© ?ŒìŠ¤??)
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
        seller.setShopName("?ŒìŠ¤?¸ìƒ??);
        seller.setName("?ë§¤?ì´ë¦?);
        seller.setShopAddress("ë¶€?°ì‹œ ?¨êµ¬ ?€?°ë¡œ");
        seller.setTel("010-1234-5678");
        seller.setGubun(MemberGubun.NEW.getCode());
        seller.setStatus("ACTIVE");
        return seller;
    }

    // ==================== ?€???ŒìŠ¤??====================

    @Test
    @DisplayName("?ë§¤???€??- ?±ê³µ")
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
    @DisplayName("?ë§¤???€??- ?„ìˆ˜ ?„ë“œ ?„ë½???¤íŒ¨")
    void save_fail_missing_required_fields() {
        // given: ?´ë©”?¼ì´ null???ë§¤??        testSeller.setEmail(null);

        // when & then: ?ˆì™¸ ë°œìƒ ?ˆìƒ
        assertThrows(Exception.class, () -> {
            sellerDAO.save(testSeller);
        });
    }

    // ==================== ì¡°íšŒ ?ŒìŠ¤??====================

    @Test
    @DisplayName("IDë¡??ë§¤??ì¡°íšŒ - ?±ê³µ")
    void findById_success() {
        // given: ?ë§¤???€??        Seller savedSeller = sellerDAO.save(testSeller);

        // when: IDë¡?ì¡°íšŒ
        Optional<Seller> foundSeller = sellerDAO.findById(savedSeller.getSellerId());

        // then: ì¡°íšŒ ?±ê³µ ?•ì¸
        assertTrue(foundSeller.isPresent());
        assertEquals(savedSeller.getSellerId(), foundSeller.get().getSellerId());
        assertEquals(savedSeller.getEmail(), foundSeller.get().getEmail());
        assertEquals(savedSeller.getShopName(), foundSeller.get().getShopName());
    }

    @Test
    @DisplayName("IDë¡??ë§¤??ì¡°íšŒ - ì¡´ì¬?˜ì? ?ŠëŠ” ID")
    void findById_not_found() {
        // when: ì¡´ì¬?˜ì? ?ŠëŠ” IDë¡?ì¡°íšŒ
        Optional<Seller> foundSeller = sellerDAO.findById(999999L);

        // then: ì¡°íšŒ ê²°ê³¼ ?†ìŒ
        assertFalse(foundSeller.isPresent());
    }

    @Test
    @DisplayName("?´ë©”?¼ë¡œ ?ë§¤??ì¡°íšŒ - ?±ê³µ")
    void findByEmail_success() {
        // given: ?ë§¤???€??        sellerDAO.save(testSeller);

        // when: ?´ë©”?¼ë¡œ ì¡°íšŒ
        Optional<Seller> foundSeller = sellerDAO.findByEmail(testSeller.getEmail());

        // then: ì¡°íšŒ ?±ê³µ ?•ì¸
        assertTrue(foundSeller.isPresent());
        assertEquals(testSeller.getEmail(), foundSeller.get().getEmail());
    }

    @Test
    @DisplayName("?´ë©”?¼ë¡œ ?ë§¤??ì¡°íšŒ - ì¡´ì¬?˜ì? ?ŠëŠ” ?´ë©”??)
    void findByEmail_not_found() {
        // when: ì¡´ì¬?˜ì? ?ŠëŠ” ?´ë©”?¼ë¡œ ì¡°íšŒ
        Optional<Seller> foundSeller = sellerDAO.findByEmail("notfound@seller.com");

        // then: ì¡°íšŒ ê²°ê³¼ ?†ìŒ
        assertFalse(foundSeller.isPresent());
    }

    @Test
    @DisplayName("?¬ì—…?ë“±ë¡ë²ˆ?¸ë¡œ ?ë§¤??ì¡°íšŒ - ?±ê³µ")
    void findByBizRegNo_success() {
        // given: ?ë§¤???€??        sellerDAO.save(testSeller);

        // when: ?¬ì—…?ë“±ë¡ë²ˆ?¸ë¡œ ì¡°íšŒ
        Optional<Seller> foundSeller = sellerDAO.findByBizRegNo(testSeller.getBizRegNo());

        // then: ì¡°íšŒ ?±ê³µ ?•ì¸
        assertTrue(foundSeller.isPresent());
        assertEquals(testSeller.getBizRegNo(), foundSeller.get().getBizRegNo());
    }

    @Test
    @DisplayName("?¬ì—…?ë“±ë¡ë²ˆ?¸ë¡œ ?ë§¤??ì¡°íšŒ - ì¡´ì¬?˜ì? ?ŠëŠ” ë²ˆí˜¸")
    void findByBizRegNo_not_found() {
        // when: ì¡´ì¬?˜ì? ?ŠëŠ” ?¬ì—…?ë“±ë¡ë²ˆ?¸ë¡œ ì¡°íšŒ
        Optional<Seller> foundSeller = sellerDAO.findByBizRegNo("999-88-77777");

        // then: ì¡°íšŒ ê²°ê³¼ ?†ìŒ
        assertFalse(foundSeller.isPresent());
    }

    // ==================== ?…ë°?´íŠ¸ ?ŒìŠ¤??====================

    @Test
    @DisplayName("?ë§¤???•ë³´ ?˜ì • - ?±ê³µ")
    void update_success() {
        // given: ?ë§¤???€??        Seller savedSeller = sellerDAO.save(testSeller);

        // when: ?•ë³´ ?˜ì •
        Seller updateSeller = new Seller();
        updateSeller.setShopName("?˜ì •?œìƒ?ëª…");
        updateSeller.setShopAddress("ë¶€?°ì‹œ ?´ìš´?€êµ?);
        updateSeller.setTel("010-9999-8888");

        int updatedRows = sellerDAO.update(savedSeller.getSellerId(), updateSeller);

        // then: ?˜ì • ?±ê³µ ?•ì¸
        assertEquals(1, updatedRows);

        // ?˜ì •???°ì´??ê²€ì¦?        Optional<Seller> updatedSellerOpt = sellerDAO.findById(savedSeller.getSellerId());
        assertTrue(updatedSellerOpt.isPresent());
        Seller updatedSellerData = updatedSellerOpt.get();
        assertEquals("?˜ì •?œìƒ?ëª…", updatedSellerData.getShopName());
        assertEquals("ë¶€?°ì‹œ ?´ìš´?€êµ?, updatedSellerData.getShopAddress());
        assertEquals("010-9999-8888", updatedSellerData.getTel());
    }

    @Test
    @DisplayName("?ë§¤???•ë³´ ?˜ì • - ì¡´ì¬?˜ì? ?ŠëŠ” ID")
    void update_not_found() {
        // given: ?˜ì •???°ì´??        Seller updateSeller = new Seller();
        updateSeller.setShopName("?˜ì •?œìƒ?ëª…");

        // when: ì¡´ì¬?˜ì? ?ŠëŠ” IDë¡??˜ì • ?œë„
        int updatedRows = sellerDAO.update(999999L, updateSeller);

        // then: ?˜ì •?˜ì? ?ŠìŒ
        assertEquals(0, updatedRows);
    }

    // ==================== ?ˆí‡´ ?ŒìŠ¤??====================

    @Test
    @DisplayName("?ë§¤???ˆí‡´ ì²˜ë¦¬ - ?±ê³µ")
    void withdrawWithReason_success() {
        // given: ?ë§¤???€??        Seller savedSeller = sellerDAO.save(testSeller);

        // when: ?ˆí‡´ ì²˜ë¦¬
        String reason = "?¬ì—… ì¢…ë£Œ";
        int withdrawnRows = sellerDAO.withdrawWithReason(savedSeller.getSellerId(), reason);

        // then: ?ˆí‡´ ?±ê³µ ?•ì¸
        assertEquals(1, withdrawnRows);

        // ?ˆí‡´ ?íƒœ ?•ì¸
        Optional<Seller> withdrawnSellerOpt = sellerDAO.findById(savedSeller.getSellerId());
        assertTrue(withdrawnSellerOpt.isPresent());
        Seller withdrawnSeller = withdrawnSellerOpt.get();
        assertEquals("WITHDRAWN", withdrawnSeller.getStatus());
        assertNotNull(withdrawnSeller.getWithdrawnAt());
        assertEquals(reason, withdrawnSeller.getWithdrawnReason());
    }

    @Test
    @DisplayName("?ë§¤???ˆí‡´ ì²˜ë¦¬ - ì¡´ì¬?˜ì? ?ŠëŠ” ID")
    void withdrawWithReason_not_found() {
        // when: ì¡´ì¬?˜ì? ?ŠëŠ” IDë¡??ˆí‡´ ?œë„
        int withdrawnRows = sellerDAO.withdrawWithReason(999999L, "?ˆí‡´ ?¬ìœ ");

        // then: ?ˆí‡´?˜ì? ?ŠìŒ
        assertEquals(0, withdrawnRows);
    }

    // ==================== ì¤‘ë³µ ì²´í¬ ?ŒìŠ¤??====================

    @Test
    @DisplayName("?´ë©”??ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ??)
    void existsByEmail_true() {
        // given: ?ë§¤???€??        sellerDAO.save(testSeller);

        // when: ?´ë©”??ì¤‘ë³µ ì²´í¬
        boolean exists = sellerDAO.existsByEmail(testSeller.getEmail());

        // then: ì¤‘ë³µ??        assertTrue(exists);
    }

    @Test
    @DisplayName("?´ë©”??ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ ?ˆë¨")
    void existsByEmail_false() {
        // when: ì¡´ì¬?˜ì? ?ŠëŠ” ?´ë©”??ì¤‘ë³µ ì²´í¬
        boolean exists = sellerDAO.existsByEmail("new@seller.com");

        // then: ì¤‘ë³µ ?ˆë¨
        assertFalse(exists);
    }

    @Test
    @DisplayName("?¬ì—…?ë“±ë¡ë²ˆ??ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ??)
    void existsByBizRegNo_true() {
        // given: ?ë§¤???€??        sellerDAO.save(testSeller);

        // when: ?¬ì—…?ë“±ë¡ë²ˆ??ì¤‘ë³µ ì²´í¬
        boolean exists = sellerDAO.existsByBizRegNo(testSeller.getBizRegNo());

        // then: ì¤‘ë³µ??        assertTrue(exists);
    }

    @Test
    @DisplayName("?¬ì—…?ë“±ë¡ë²ˆ??ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ ?ˆë¨")
    void existsByBizRegNo_false() {
        // when: ì¡´ì¬?˜ì? ?ŠëŠ” ?¬ì—…?ë“±ë¡ë²ˆ??ì¤‘ë³µ ì²´í¬
        boolean exists = sellerDAO.existsByBizRegNo("999-88-77777");

        // then: ì¤‘ë³µ ?ˆë¨
        assertFalse(exists);
    }

    @Test
    @DisplayName("?í˜¸ëª?ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ??)
    void existsByShopName_true() {
        // given: ?ë§¤???€??        sellerDAO.save(testSeller);

        // when: ?í˜¸ëª?ì¤‘ë³µ ì²´í¬
        boolean exists = sellerDAO.existsByShopName(testSeller.getShopName());

        // then: ì¤‘ë³µ??        assertTrue(exists);
    }

    @Test
    @DisplayName("?í˜¸ëª?ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ ?ˆë¨")
    void existsByShopName_false() {
        // when: ì¡´ì¬?˜ì? ?ŠëŠ” ?í˜¸ëª?ì¤‘ë³µ ì²´í¬
        boolean exists = sellerDAO.existsByShopName("?ˆìƒ?ëª…");

        // then: ì¤‘ë³µ ?ˆë¨
        assertFalse(exists);
    }

    // ==================== ëª©ë¡ ì¡°íšŒ ?ŒìŠ¤??====================

    @Test
    @DisplayName("?„ì²´ ?ë§¤??ëª©ë¡ ì¡°íšŒ")
    void findAll() {
        // given: ?¬ëŸ¬ ?ë§¤???€??        sellerDAO.save(testSeller);

        Seller seller2 = createSampleSeller();
        seller2.setEmail("seller2@shop.com");
        seller2.setBizRegNo("222-33-44444");
        seller2.setShopName("?ë²ˆì§¸ìƒ??);
        sellerDAO.save(seller2);

        // when: ?„ì²´ ëª©ë¡ ì¡°íšŒ
        List<Seller> sellers = sellerDAO.findAll();

        // then: ?€?¥ëœ ?ë§¤?ë“¤??ì¡°íšŒ??        assertNotNull(sellers);
        assertTrue(sellers.size() >= 2);
        assertTrue(sellers.stream().anyMatch(s -> s.getEmail().equals(testSeller.getEmail())));
        assertTrue(sellers.stream().anyMatch(s -> s.getEmail().equals(seller2.getEmail())));
    }

    @Test
    @DisplayName("?ˆí‡´???ë§¤??ëª©ë¡ ì¡°íšŒ")
    void findWithdrawnMembers() {
        // given: ?ë§¤???€?????ˆí‡´ ì²˜ë¦¬
        Seller savedSeller = sellerDAO.save(testSeller);
        sellerDAO.withdrawWithReason(savedSeller.getSellerId(), "?ŒìŠ¤???ˆí‡´");

        // when: ?ˆí‡´???ë§¤??ëª©ë¡ ì¡°íšŒ
        List<Seller> withdrawnSellers = sellerDAO.findWithdrawnMembers();

        // then: ?ˆí‡´???ë§¤?ê? ì¡°íšŒ??        assertNotNull(withdrawnSellers);
        assertTrue(withdrawnSellers.stream().anyMatch(s -> 
            s.getSellerId().equals(savedSeller.getSellerId()) && 
            "WITHDRAWN".equals(s.getStatus())
        ));
    }

    // ==================== Edge Case ?ŒìŠ¤??====================

    @Test
    @DisplayName("?€?Œë¬¸??êµ¬ë¶„ ?´ë©”??ì¤‘ë³µ ì²´í¬")
    void existsByEmail_case_sensitivity() {
        // given: ?Œë¬¸???´ë©”?¼ë¡œ ?€??        sellerDAO.save(testSeller);

        // when: ?€ë¬¸ìë¡?ì¤‘ë³µ ì²´í¬
        boolean existsUpper = sellerDAO.existsByEmail(testSeller.getEmail().toUpperCase());
        boolean existsLower = sellerDAO.existsByEmail(testSeller.getEmail().toLowerCase());

        // then: ?€?Œë¬¸??êµ¬ë¶„ ?•ì¸
        assertTrue(existsLower); // ?ë³¸ê³??™ì¼
        // existsUpper??DB ?¤ì •???°ë¼ ?¤ë¦„
    }

    @Test
    @DisplayName("?¹ìˆ˜ë¬¸ì ?¬í•¨ ?í˜¸ëª?ì²˜ë¦¬")
    void handle_special_characters_in_shop_name() {
        // given: ?¹ìˆ˜ë¬¸ì ?¬í•¨ ?í˜¸ëª?        testSeller.setShopName("?ŒìŠ¤?¸ìƒ??#$%");

        // when: ?€??ë°?ì¡°íšŒ
        Seller savedSeller = sellerDAO.save(testSeller);
        Optional<Seller> foundSeller = sellerDAO.findById(savedSeller.getSellerId());

        // then: ?¹ìˆ˜ë¬¸ì ? ì? ?•ì¸
        assertTrue(foundSeller.isPresent());
        assertEquals("?ŒìŠ¤?¸ìƒ??#$%", foundSeller.get().getShopName());
    }

    @Test
    @DisplayName("?¬ì—…?ë“±ë¡ë²ˆ???•ì‹ ?ŒìŠ¤??)
    void bizRegNo_format_test() {
        // given: ?¤ì–‘???•ì‹???¬ì—…?ë“±ë¡ë²ˆ??        testSeller.setBizRegNo("123-45-67890");

        // when: ?€??        Seller savedSeller = sellerDAO.save(testSeller);

        // then: ?•ì‹ ? ì? ?•ì¸
        assertEquals("123-45-67890", savedSeller.getBizRegNo());
    }

    @Test
    @DisplayName("null ê°?ì²˜ë¦¬ ?ŒìŠ¤??)
    void handle_null_values() {
        // when & then: null ?´ë©”?¼ë¡œ ì¤‘ë³µ ì²´í¬
        assertThrows(Exception.class, () -> {
            sellerDAO.existsByEmail(null);
        });

        // when & then: null IDë¡?ì¡°íšŒ
        assertThrows(Exception.class, () -> {
            sellerDAO.findById(null);
        });

        // when & then: null ?¬ì—…?ë“±ë¡ë²ˆ?¸ë¡œ ì¤‘ë³µ ì²´í¬
        assertThrows(Exception.class, () -> {
            sellerDAO.existsByBizRegNo(null);
        });
    }

    @Test
    @DisplayName("?™ì‹œ ?€???ŒìŠ¤??- ?´ë©”??ì¤‘ë³µ")
    void concurrent_save_test_email_duplicate() {
        // given: ?™ì¼???´ë©”?¼ì„ ê°€ì§????ë§¤??        Seller seller1 = createSampleSeller();
        Seller seller2 = createSampleSeller();
        seller2.setBizRegNo("222-33-44444"); // ?¬ì—…?ë²ˆ?¸ëŠ” ?¤ë¥´ê²?        seller2.setShopName("?¤ë¥¸?ì ëª?); // ?ì ëª…ë„ ?¤ë¥´ê²?
        // when: ì²?ë²ˆì§¸ ?€??        sellerDAO.save(seller1);

        // then: ??ë²ˆì§¸ ?€?¥ì‹œ ?´ë©”??ì¤‘ë³µ ?ëŸ¬
        assertThrows(Exception.class, () -> {
            sellerDAO.save(seller2);
        });
    }

    @Test
    @DisplayName("?™ì‹œ ?€???ŒìŠ¤??- ?¬ì—…?ë“±ë¡ë²ˆ??ì¤‘ë³µ")
    void concurrent_save_test_bizRegNo_duplicate() {
        // given: ?™ì¼???¬ì—…?ë“±ë¡ë²ˆ?¸ë? ê°€ì§????ë§¤??        Seller seller1 = createSampleSeller();
        Seller seller2 = createSampleSeller();
        seller2.setEmail("different@email.com"); // ?´ë©”?¼ì? ?¤ë¥´ê²?        seller2.setShopName("?¤ë¥¸?ì ëª?); // ?ì ëª…ë„ ?¤ë¥´ê²?
        // when: ì²?ë²ˆì§¸ ?€??        sellerDAO.save(seller1);

        // then: ??ë²ˆì§¸ ?€?¥ì‹œ ?¬ì—…?ë“±ë¡ë²ˆ??ì¤‘ë³µ ?ëŸ¬
        assertThrows(Exception.class, () -> {
            sellerDAO.save(seller2);
        });
    }

    @Test
    @DisplayName("ë§¤ìš° ê¸?ë¬¸ì??ì²˜ë¦¬ ?ŒìŠ¤??)
    void handle_long_strings() {
        // given: ë§¤ìš° ê¸??ì ëª?        String longShopName = "??.repeat(500); // ë§¤ìš° ê¸??ì ëª?        testSeller.setShopName(longShopName);

        // when & then: ê¸¸ì´ ?œí•œ ?•ì¸ (DB ì»¬ëŸ¼ ê¸¸ì´???°ë¼)
        if (longShopName.length() > 255) { // ?¼ë°˜?ì¸ ë¬¸ì??ê¸¸ì´ ?œí•œ
            assertThrows(Exception.class, () -> {
                sellerDAO.save(testSeller);
            });
        }
    }
} 
