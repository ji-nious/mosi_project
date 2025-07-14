package com.kh.project.domain.seller.svc;

import com.kh.project.domain.seller.dao.SellerDAO;
import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.entity.MemberGubun;
import com.kh.project.domain.entity.MemberStatus;
import com.kh.project.web.exception.BusinessValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ?ë§¤???Œì›ê°€???µí•© ?ŒìŠ¤???œë‚˜ë¦¬ì˜¤
 * ?¤ì œ ?°ì´?°ë² ?´ìŠ¤?€ ëª¨ë“  ê³„ì¸µ???µí•©???˜ê²½?ì„œ ?ŒìŠ¤?? */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("?ë§¤???Œì›ê°€???µí•© ?ŒìŠ¤??)
class SellerIntegrationTest {

    @Autowired
    private SellerSVC sellerSVC;

    @Autowired
    private SellerDAO sellerDAO;

    private Seller createValidSeller() {
        return Seller.builder()
                .email("seller@test.com")
                .password("password123")
                .bizRegNo("123-45-67890")
                .shopName("?ŒìŠ¤?¸ìƒ??)
                .name("ê¹€?ë§¤??)
                .postcode("12345")
                .address("?œìš¸??ê°•ë‚¨êµ??ŒìŠ¤?¸ë¡œ 123")
                .detailAddress("101??)
                .tel("02-1234-5678")
                .birth(LocalDate.of(1980, 3, 15))
                .memberGubun(MemberGubun.BRONZE.getCode())
                .memberStatus(MemberStatus.ACTIVE.getCode())
                .build();
    }

    @BeforeEach
    void setUp() {
        // ?ŒìŠ¤???°ì´???•ë¦¬
        sellerDAO.deleteAll();
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??1: ?•ìƒ ?ë§¤???Œì›ê°€???Œë¡œ??)
    void integrationTest_normalSellerSignupFlow() {
        // Given - ? íš¨???ë§¤???°ì´??        Seller seller = createValidSeller();

        // When - ?Œì›ê°€???¤í–‰
        Seller savedSeller = sellerSVC.join(seller);

        // Then - ?Œì›ê°€???±ê³µ ê²€ì¦?        assertNotNull(savedSeller);
        assertNotNull(savedSeller.getId());
        assertEquals(seller.getEmail(), savedSeller.getEmail());
        assertEquals(seller.getBizRegNo(), savedSeller.getBizRegNo());
        assertEquals(seller.getShopName(), savedSeller.getShopName());
        assertEquals(MemberStatus.ACTIVE.getCode(), savedSeller.getMemberStatus());

        // ?°ì´?°ë² ?´ìŠ¤ ?€??ê²€ì¦?        Optional<Seller> foundSeller = sellerDAO.findByEmail(seller.getEmail());
        assertTrue(foundSeller.isPresent());
        assertEquals(seller.getEmail(), foundSeller.get().getEmail());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??2: ?´ë©”??ì¤‘ë³µ ???Œì›ê°€???¤íŒ¨")
    void integrationTest_emailDuplicateFailure() {
        // Given - ì²?ë²ˆì§¸ ?Œì›ê°€??        Seller firstSeller = createValidSeller();
        sellerSVC.join(firstSeller);

        // When & Then - ê°™ì? ?´ë©”?¼ë¡œ ??ë²ˆì§¸ ?Œì›ê°€???œë„
        Seller secondSeller = createValidSeller();
        secondSeller.setBizRegNo("987-65-43210");
        secondSeller.setShopName("?¤ë¥¸?ì ");

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            sellerSVC.join(secondSeller);
        });

        assertEquals("?´ë? ê°€?…ëœ ?´ë©”?¼ì…?ˆë‹¤.", exception.getMessage());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??3: ?¬ì—…?ë“±ë¡ë²ˆ??ì¤‘ë³µ ???Œì›ê°€???¤íŒ¨")
    void integrationTest_bizRegNoDuplicateFailure() {
        // Given - ì²?ë²ˆì§¸ ?Œì›ê°€??        Seller firstSeller = createValidSeller();
        sellerSVC.join(firstSeller);

        // When & Then - ê°™ì? ?¬ì—…?ë“±ë¡ë²ˆ?¸ë¡œ ??ë²ˆì§¸ ?Œì›ê°€???œë„
        Seller secondSeller = createValidSeller();
        secondSeller.setEmail("different@test.com");
        secondSeller.setShopName("?¤ë¥¸?ì ");

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            sellerSVC.join(secondSeller);
        });

        assertEquals("?´ë? ?±ë¡???¬ì—…?ë“±ë¡ë²ˆ?¸ì…?ˆë‹¤.", exception.getMessage());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??4: ?í˜¸ëª?ì¤‘ë³µ ???Œì›ê°€???¤íŒ¨")
    void integrationTest_shopNameDuplicateFailure() {
        // Given - ì²?ë²ˆì§¸ ?Œì›ê°€??        Seller firstSeller = createValidSeller();
        sellerSVC.join(firstSeller);

        // When & Then - ê°™ì? ?í˜¸ëª…ìœ¼ë¡???ë²ˆì§¸ ?Œì›ê°€???œë„
        Seller secondSeller = createValidSeller();
        secondSeller.setEmail("different@test.com");
        secondSeller.setBizRegNo("987-65-43210");

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            sellerSVC.join(secondSeller);
        });

        assertEquals("?´ë? ?¬ìš© ì¤‘ì¸ ?í˜¸ëª…ì…?ˆë‹¤.", exception.getMessage());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??5: ?€?œìëª?ì¤‘ë³µ ???Œì›ê°€???¤íŒ¨")
    void integrationTest_nameDuplicateFailure() {
        // Given - ì²?ë²ˆì§¸ ?Œì›ê°€??        Seller firstSeller = createValidSeller();
        sellerSVC.join(firstSeller);

        // When & Then - ê°™ì? ?€?œìëª…ìœ¼ë¡???ë²ˆì§¸ ?Œì›ê°€???œë„
        Seller secondSeller = createValidSeller();
        secondSeller.setEmail("different@test.com");
        secondSeller.setBizRegNo("987-65-43210");
        secondSeller.setShopName("?¤ë¥¸?ì ");

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            sellerSVC.join(secondSeller);
        });

        assertEquals("?´ë? ?±ë¡???€?œìëª…ì…?ˆë‹¤.", exception.getMessage());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??6: ?ì  ì£¼ì†Œ ì¤‘ë³µ ???Œì›ê°€???¤íŒ¨")
    void integrationTest_shopAddressDuplicateFailure() {
        // Given - ì²?ë²ˆì§¸ ?Œì›ê°€??        Seller firstSeller = createValidSeller();
        sellerSVC.join(firstSeller);

        // When & Then - ê°™ì? ?ì  ì£¼ì†Œë¡???ë²ˆì§¸ ?Œì›ê°€???œë„
        Seller secondSeller = createValidSeller();
        secondSeller.setEmail("different@test.com");
        secondSeller.setBizRegNo("987-65-43210");
        secondSeller.setShopName("?¤ë¥¸?ì ");
        secondSeller.setName("?¤ë¥¸?ë§¤??);

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            sellerSVC.join(secondSeller);
        });

        assertEquals("?´ë? ?±ë¡???ì  ì£¼ì†Œ?…ë‹ˆ??", exception.getMessage());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??7: ?ˆí‡´???ë§¤???¬ê????±ê³µ")
    void integrationTest_withdrawnSellerRejoinSuccess() {
        // Given - ?Œì›ê°€?????ˆí‡´
        Seller seller = createValidSeller();
        Seller savedSeller = sellerSVC.join(seller);
        sellerSVC.withdraw(savedSeller.getId(), "?¬ì—… ì¢…ë£Œ");

        // When - ?™ì¼???´ë©”?¼ë¡œ ?¬ê????œë„ (?¤ë¥¸ ?•ë³´ë¡?ë³€ê²?
        Seller rejoinSeller = createValidSeller();
        rejoinSeller.setBizRegNo("999-88-77777");
        rejoinSeller.setShopName("?ˆë¡œ?´ìƒ??);
        rejoinSeller.setName("?ˆë¡œ?´ë??œì");
        rejoinSeller.setAddress("ë¶€?°ì‹œ ?´ìš´?€êµ??ˆë¡œ?´ë¡œ 456");

        // Then - ?¬ê????±ê³µ
        Seller rejoinedSeller = sellerSVC.join(rejoinSeller);
        assertNotNull(rejoinedSeller);
        assertEquals(seller.getEmail(), rejoinedSeller.getEmail());
        assertEquals("?ˆë¡œ?´ìƒ??, rejoinedSeller.getShopName());
        assertEquals(MemberStatus.ACTIVE.getCode(), rejoinedSeller.getMemberStatus());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??8: ?Œì›ê°€????ì¦‰ì‹œ ë¡œê·¸???±ê³µ")
    void integrationTest_signupThenLoginSuccess() {
        // Given - ?Œì›ê°€??        Seller seller = createValidSeller();
        sellerSVC.join(seller);

        // When - ë¡œê·¸???œë„
        Seller loginSeller = sellerSVC.login(seller.getEmail(), seller.getPassword());

        // Then - ë¡œê·¸???±ê³µ
        assertNotNull(loginSeller);
        assertEquals(seller.getEmail(), loginSeller.getEmail());
        assertTrue(sellerSVC.canLogin(loginSeller));
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??9: ?¬ì—…?ë“±ë¡ë²ˆ??? íš¨??ê²€ì¦?)
    void integrationTest_bizRegNoValidation() {
        // Given - ?˜ëª»???¬ì—…?ë“±ë¡ë²ˆ???•ì‹
        Seller invalidSeller = createValidSeller();
        invalidSeller.setBizRegNo("123456789"); // ?¬ë°”ë¥??•ì‹???„ë‹˜

        // When & Then - ? íš¨??ê²€ì¦??¤íŒ¨
        assertFalse(sellerSVC.validateBizRegNo(invalidSeller.getBizRegNo()));
        
        // ?¬ë°”ë¥??•ì‹ ê²€ì¦?        assertTrue(sellerSVC.validateBizRegNo("123-45-67890"));
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??10: ?Œì›ê°€????ì¤‘ë³µ ì²´í¬ ê¸°ëŠ¥ ê²€ì¦?)
    void integrationTest_duplicateCheckAfterSignup() {
        // Given - ?Œì›ê°€??        Seller seller = createValidSeller();
        sellerSVC.join(seller);

        // When & Then - ì¤‘ë³µ ì²´í¬ ê²€ì¦?        assertTrue(sellerSVC.existsByEmail(seller.getEmail()));
        assertTrue(sellerSVC.existsByBizRegNo(seller.getBizRegNo()));
        assertTrue(sellerSVC.existsByShopName(seller.getShopName()));
        assertTrue(sellerSVC.existsByName(seller.getName()));
        assertTrue(sellerSVC.existsByShopAddress(seller.getAddress()));

        // ì¡´ì¬?˜ì? ?ŠëŠ” ?•ë³´ ê²€ì¦?        assertFalse(sellerSVC.existsByEmail("nonexistent@test.com"));
        assertFalse(sellerSVC.existsByBizRegNo("999-99-99999"));
        assertFalse(sellerSVC.existsByShopName("ì¡´ì¬?˜ì??ŠëŠ”?ì "));
        assertFalse(sellerSVC.existsByName("ì¡´ì¬?˜ì??ŠëŠ”?€?œì"));
        assertFalse(sellerSVC.existsByShopAddress("ì¡´ì¬?˜ì??ŠëŠ”ì£¼ì†Œ"));
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??11: ?Œì›ê°€????ì´ˆê¸° ?íƒœ ê²€ì¦?)
    void integrationTest_initialStateAfterSignup() {
        // Given - ?Œì›ê°€??        Seller seller = createValidSeller();
        Seller savedSeller = sellerSVC.join(seller);

        // When - ì´ˆê¸° ?íƒœ ì¡°íšŒ
        Map<String, Object> serviceUsage = sellerSVC.getServiceUsage(savedSeller.getId());

        // Then - ì´ˆê¸° ?íƒœ ê²€ì¦?        assertTrue((Boolean) serviceUsage.get("canWithdraw"));
        assertEquals(0, serviceUsage.get("orderCount"));
        assertEquals(0, serviceUsage.get("productCount"));
        assertEquals(0, serviceUsage.get("disputeCount"));
        assertEquals(0, serviceUsage.get("pointBalance"));
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??12: ?„ì²´ ?Œì›ê°€???„ë¡œ?¸ìŠ¤ ì¢…í•© ê²€ì¦?)
    void integrationTest_completeSignupProcessValidation() {
        // Given - ? íš¨???ë§¤???°ì´??        Seller seller = createValidSeller();

        // When - ?Œì›ê°€???¤í–‰
        Seller savedSeller = sellerSVC.join(seller);

        // Then - ì¢…í•© ê²€ì¦?        // 1. ê¸°ë³¸ ?•ë³´ ê²€ì¦?        assertEquals(seller.getEmail(), savedSeller.getEmail());
        assertEquals(seller.getBizRegNo(), savedSeller.getBizRegNo());
        assertEquals(seller.getShopName(), savedSeller.getShopName());
        assertEquals(seller.getName(), savedSeller.getName());
        assertEquals(seller.getTel(), savedSeller.getTel());

        // 2. ?œìŠ¤???¤ì • ê²€ì¦?        assertEquals(MemberGubun.BRONZE.getCode(), savedSeller.getMemberGubun());
        assertEquals(MemberStatus.ACTIVE.getCode(), savedSeller.getMemberStatus());

        // 3. ë¡œê·¸??ê°€???íƒœ ê²€ì¦?        assertTrue(sellerSVC.canLogin(savedSeller));
        assertFalse(sellerSVC.isWithdrawn(savedSeller));

        // 4. ?œë¹„???´ìš© ê°€???íƒœ ê²€ì¦?        assertTrue(sellerSVC.canWithdraw(savedSeller.getId()));

        // 5. ?°ì´?°ë² ?´ìŠ¤ ?¼ê???ê²€ì¦?        Optional<Seller> dbSeller = sellerDAO.findByEmail(seller.getEmail());
        assertTrue(dbSeller.isPresent());
        assertEquals(savedSeller.getId(), dbSeller.get().getId());

        // 6. ?ì  ?•ë³´ ê²€ì¦?        var shopInfo = sellerSVC.getShopInfo(savedSeller);
        assertNotNull(shopInfo);
        assertEquals(seller.getShopName(), shopInfo.getCode());
        assertEquals(seller.getShopName(), shopInfo.getName());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??13: ?ˆí‡´???ë§¤???¬ê?????ì¤‘ë³µ ì²´í¬ ë¡œì§")
    void integrationTest_withdrawnSellerRejoinDuplicateCheck() {
        // Given - ì²?ë²ˆì§¸ ?ë§¤???Œì›ê°€?????ˆí‡´
        Seller firstSeller = createValidSeller();
        Seller savedFirst = sellerSVC.join(firstSeller);
        sellerSVC.withdraw(savedFirst.getId(), "?¬ì—… ì¢…ë£Œ");

        // When - ?™ì¼???•ë³´ë¡??¬ê????œë„
        Seller rejoinSeller = createValidSeller();

        // Then - ?¬ê????±ê³µ (?ˆí‡´ ?Œì›?€ ì¤‘ë³µ ì²´í¬?ì„œ ?œì™¸)
        assertDoesNotThrow(() -> {
            sellerSVC.join(rejoinSeller);
        });

        // ?ˆë¡œ???ë§¤?ê? ê°™ì? ?•ë³´ë¡?ê°€???œë„ ???¤íŒ¨
        Seller anotherSeller = createValidSeller();
        anotherSeller.setEmail("another@test.com");

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            sellerSVC.join(anotherSeller);
        });

        assertEquals("?´ë? ?±ë¡???¬ì—…?ë“±ë¡ë²ˆ?¸ì…?ˆë‹¤.", exception.getMessage());
    }
} 
