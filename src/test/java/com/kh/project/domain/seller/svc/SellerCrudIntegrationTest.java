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
 * ?ë§¤??ì¡°íšŒ/?˜ì •/?ˆí‡´ ?µí•© ?ŒìŠ¤???œë‚˜ë¦¬ì˜¤
 * ?¤ì œ ?°ì´?°ë² ?´ìŠ¤?€ ëª¨ë“  ê³„ì¸µ???µí•©???˜ê²½?ì„œ ?ŒìŠ¤?? */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("?ë§¤??ì¡°íšŒ/?˜ì •/?ˆí‡´ ?µí•© ?ŒìŠ¤??)
class SellerCrudIntegrationTest {

    @Autowired
    private SellerSVC sellerSVC;

    @Autowired
    private SellerDAO sellerDAO;

    private Seller testSeller;

    @BeforeEach
    void setUp() {
        // ?ŒìŠ¤???°ì´???•ë¦¬
        sellerDAO.deleteAll();
        
        // ?ŒìŠ¤?¸ìš© ?ë§¤???ì„±
        testSeller = Seller.builder()
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
        
        testSeller = sellerSVC.join(testSeller);
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??1: ?ë§¤???•ë³´ ì¡°íšŒ - IDë¡?ì¡°íšŒ")
    void integrationTest_findById() {
        // When - IDë¡??ë§¤??ì¡°íšŒ
        Optional<Seller> foundSeller = sellerSVC.findById(testSeller.getId());

        // Then - ì¡°íšŒ ?±ê³µ ê²€ì¦?        assertTrue(foundSeller.isPresent());
        assertEquals(testSeller.getId(), foundSeller.get().getId());
        assertEquals(testSeller.getEmail(), foundSeller.get().getEmail());
        assertEquals(testSeller.getShopName(), foundSeller.get().getShopName());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??2: ?ë§¤???•ë³´ ì¡°íšŒ - ?´ë©”?¼ë¡œ ì¡°íšŒ")
    void integrationTest_findByEmail() {
        // When - ?´ë©”?¼ë¡œ ?ë§¤??ì¡°íšŒ
        Optional<Seller> foundSeller = sellerSVC.findByEmail(testSeller.getEmail());

        // Then - ì¡°íšŒ ?±ê³µ ê²€ì¦?        assertTrue(foundSeller.isPresent());
        assertEquals(testSeller.getEmail(), foundSeller.get().getEmail());
        assertEquals(testSeller.getId(), foundSeller.get().getId());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??3: ?ë§¤???•ë³´ ?˜ì • - ê¸°ë³¸ ?•ë³´ ?˜ì •")
    void integrationTest_updateBasicInfo() {
        // Given - ?˜ì •???•ë³´
        Seller updateSeller = Seller.builder()
                .shopName("?˜ì •?œìƒ??)
                .name("?˜ì •?œë??œì")
                .tel("02-9999-8888")
                .postcode("54321")
                .address("ë¶€?°ì‹œ ?´ìš´?€êµ??˜ì •ë¡?456")
                .detailAddress("202??)
                .birth(LocalDate.of(1985, 6, 20))
                .build();

        // When - ?•ë³´ ?˜ì • ?¤í–‰
        int updateCount = sellerSVC.update(testSeller.getId(), updateSeller);

        // Then - ?˜ì • ?±ê³µ ê²€ì¦?        assertEquals(1, updateCount);

        // ?˜ì •???•ë³´ ì¡°íšŒ ê²€ì¦?        Optional<Seller> updatedSeller = sellerSVC.findById(testSeller.getId());
        assertTrue(updatedSeller.isPresent());
        assertEquals("?˜ì •?œìƒ??, updatedSeller.get().getShopName());
        assertEquals("?˜ì •?œë??œì", updatedSeller.get().getName());
        assertEquals("02-9999-8888", updatedSeller.get().getTel());
        assertEquals("ë¶€?°ì‹œ ?´ìš´?€êµ??˜ì •ë¡?456", updatedSeller.get().getAddress());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??4: ?ë§¤???•ë³´ ?˜ì • - ?í˜¸ëª?ì¤‘ë³µ ???¤íŒ¨")
    void integrationTest_updateShopNameDuplicateFailure() {
        // Given - ?¤ë¥¸ ?ë§¤???ì„±
        Seller anotherSeller = Seller.builder()
                .email("another@test.com")
                .password("password123")
                .bizRegNo("987-65-43210")
                .shopName("?¤ë¥¸?ì ")
                .name("?¤ë¥¸?ë§¤??)
                .tel("02-1111-2222")
                .memberGubun(MemberGubun.BRONZE.getCode())
                .memberStatus(MemberStatus.ACTIVE.getCode())
                .build();
        sellerSVC.join(anotherSeller);

        // When & Then - ê¸°ì¡´ ?í˜¸ëª…ìœ¼ë¡??˜ì • ?œë„
        Seller updateSeller = Seller.builder()
                .shopName("?¤ë¥¸?ì ")
                .build();

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            sellerSVC.update(testSeller.getId(), updateSeller);
        });

        assertEquals("?´ë? ?¬ìš© ì¤‘ì¸ ?í˜¸ëª…ì…?ˆë‹¤.", exception.getMessage());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??5: ?ë§¤???•ë³´ ?˜ì • - ?€?œìëª?ì¤‘ë³µ ???¤íŒ¨")
    void integrationTest_updateNameDuplicateFailure() {
        // Given - ?¤ë¥¸ ?ë§¤???ì„±
        Seller anotherSeller = Seller.builder()
                .email("another@test.com")
                .password("password123")
                .bizRegNo("987-65-43210")
                .shopName("?¤ë¥¸?ì ")
                .name("?¤ë¥¸?ë§¤??)
                .tel("02-1111-2222")
                .memberGubun(MemberGubun.BRONZE.getCode())
                .memberStatus(MemberStatus.ACTIVE.getCode())
                .build();
        sellerSVC.join(anotherSeller);

        // When & Then - ê¸°ì¡´ ?€?œìëª…ìœ¼ë¡??˜ì • ?œë„
        Seller updateSeller = Seller.builder()
                .name("?¤ë¥¸?ë§¤??)
                .build();

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            sellerSVC.update(testSeller.getId(), updateSeller);
        });

        assertEquals("?´ë? ?±ë¡???€?œìëª…ì…?ˆë‹¤.", exception.getMessage());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??6: ë¹„ë?ë²ˆí˜¸ ?•ì¸ ê²€ì¦?)
    void integrationTest_passwordVerification() {
        // When & Then - ?¬ë°”ë¥?ë¹„ë?ë²ˆí˜¸ ?•ì¸
        assertTrue(sellerSVC.checkPassword(testSeller.getId(), "password123"));

        // When & Then - ?˜ëª»??ë¹„ë?ë²ˆí˜¸ ?•ì¸
        assertFalse(sellerSVC.checkPassword(testSeller.getId(), "wrongPassword"));
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??7: ?ë§¤???ˆí‡´ - ?•ìƒ ?ˆí‡´")
    void integrationTest_withdrawSuccess() {
        // Given - ?ˆí‡´ ê°€?¥í•œ ?íƒœ ?•ì¸
        assertTrue(sellerSVC.canWithdraw(testSeller.getId()));

        // When - ?ë§¤???ˆí‡´ ?¤í–‰
        int withdrawResult = sellerSVC.withdraw(testSeller.getId(), "?¬ì—… ì¢…ë£Œ");

        // Then - ?ˆí‡´ ?±ê³µ ê²€ì¦?        assertEquals(1, withdrawResult);

        // ?ˆí‡´ ???íƒœ ê²€ì¦?        Optional<Seller> withdrawnSeller = sellerSVC.findById(testSeller.getId());
        assertTrue(withdrawnSeller.isPresent());
        assertEquals(MemberStatus.WITHDRAWN.getCode(), withdrawnSeller.get().getMemberStatus());
        assertTrue(sellerSVC.isWithdrawn(withdrawnSeller.get()));
        assertFalse(sellerSVC.canLogin(withdrawnSeller.get()));
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??8: ?ˆí‡´ ??ë¡œê·¸??ì°¨ë‹¨")
    void integrationTest_loginBlockedAfterWithdraw() {
        // Given - ?ë§¤???ˆí‡´
        sellerSVC.withdraw(testSeller.getId(), "?¬ì—… ì¢…ë£Œ");

        // When & Then - ?ˆí‡´???ë§¤??ë¡œê·¸???œë„
        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            sellerSVC.login(testSeller.getEmail(), testSeller.getPassword());
        });

        assertEquals("?ˆí‡´???Œì›?…ë‹ˆ??", exception.getMessage());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??9: ?ˆí‡´ ???¬í™œ?±í™”")
    void integrationTest_reactivateAfterWithdraw() {
        // Given - ?ë§¤???ˆí‡´
        sellerSVC.withdraw(testSeller.getId(), "?¬ì—… ì¢…ë£Œ");

        // When - ?¬í™œ?±í™” ?œë„
        Optional<Seller> reactivatedSeller = sellerSVC.reactivate(testSeller.getEmail(), testSeller.getPassword());

        // Then - ?¬í™œ?±í™” ?±ê³µ ê²€ì¦?        assertTrue(reactivatedSeller.isPresent());
        assertEquals(MemberStatus.ACTIVE.getCode(), reactivatedSeller.get().getMemberStatus());
        assertTrue(sellerSVC.canLogin(reactivatedSeller.get()));
        assertFalse(sellerSVC.isWithdrawn(reactivatedSeller.get()));
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??10: ?œë¹„???´ìš© ?„í™© ì¡°íšŒ")
    void integrationTest_serviceUsageInfo() {
        // When - ?œë¹„???´ìš© ?„í™© ì¡°íšŒ
        Map<String, Object> serviceUsage = sellerSVC.getServiceUsage(testSeller.getId());

        // Then - ?´ìš© ?„í™© ê²€ì¦?        assertNotNull(serviceUsage);
        assertTrue(serviceUsage.containsKey("canWithdraw"));
        assertTrue(serviceUsage.containsKey("orderCount"));
        assertTrue(serviceUsage.containsKey("productCount"));
        assertTrue(serviceUsage.containsKey("disputeCount"));
        assertTrue(serviceUsage.containsKey("pointBalance"));
        assertTrue(serviceUsage.containsKey("refundCount"));

        // ì´ˆê¸° ?íƒœ ê²€ì¦?        assertTrue((Boolean) serviceUsage.get("canWithdraw"));
        assertEquals(0, serviceUsage.get("orderCount"));
        assertEquals(0, serviceUsage.get("productCount"));
        assertEquals(0, serviceUsage.get("disputeCount"));
        assertEquals(0, serviceUsage.get("pointBalance"));
        assertEquals(0, serviceUsage.get("refundCount"));
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??11: ?ë§¤???±ê¸‰ ë°??ì  ?•ë³´ ì¡°íšŒ")
    void integrationTest_sellerGradeAndShopInfo() {
        // When - ?ë§¤???±ê¸‰ ë°??ì  ?•ë³´ ì¡°íšŒ
        var gubunInfo = sellerSVC.getGubunInfo(testSeller);
        var statusInfo = sellerSVC.getStatusInfo(testSeller);
        var shopInfo = sellerSVC.getShopInfo(testSeller);

        // Then - ?±ê¸‰ ?•ë³´ ê²€ì¦?        assertNotNull(gubunInfo);
        assertEquals(MemberGubun.BRONZE.getCode(), gubunInfo.getCode());
        assertEquals(MemberGubun.BRONZE.getDescription(), gubunInfo.getName());

        assertNotNull(statusInfo);
        assertEquals(MemberStatus.ACTIVE.getCode(), statusInfo.getCode());
        assertEquals(MemberStatus.ACTIVE.getDescription(), statusInfo.getName());

        assertNotNull(shopInfo);
        assertEquals(testSeller.getShopName(), shopInfo.getCode());
        assertEquals(testSeller.getShopName(), shopInfo.getName());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??12: ?¬ì—…?ë“±ë¡ë²ˆ??? íš¨??ê²€ì¦?)
    void integrationTest_bizRegNoValidation() {
        // When & Then - ? íš¨???¬ì—…?ë“±ë¡ë²ˆ??ê²€ì¦?        assertTrue(sellerSVC.validateBizRegNo("123-45-67890"));
        assertTrue(sellerSVC.validateBizRegNo("999-99-99999"));

        // When & Then - ? íš¨?˜ì? ?Šì? ?¬ì—…?ë“±ë¡ë²ˆ??ê²€ì¦?        assertFalse(sellerSVC.validateBizRegNo("123456789"));
        assertFalse(sellerSVC.validateBizRegNo("123-456-789"));
        assertFalse(sellerSVC.validateBizRegNo("12-34-56789"));
        assertFalse(sellerSVC.validateBizRegNo(""));
        assertFalse(sellerSVC.validateBizRegNo(null));
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??13: ?ë§¤???ˆí‡´ ???œë¹„???´ìš© ?„í™© ê²€??)
    void integrationTest_withdrawServiceUsageCheck() {
        // Given - ì´ˆê¸° ?íƒœ?ì„œ ?ˆí‡´ ê°€???•ì¸
        assertTrue(sellerSVC.canWithdraw(testSeller.getId()));

        // When - ?œë¹„???´ìš© ?„í™© ì¡°íšŒ
        Map<String, Object> serviceUsage = sellerSVC.getServiceUsage(testSeller.getId());

        // Then - ?ˆí‡´ ê°€??ì¡°ê±´ ê²€ì¦?        assertTrue((Boolean) serviceUsage.get("canWithdraw"));
        assertEquals(0, serviceUsage.get("orderCount"));
        assertEquals(0, serviceUsage.get("productCount"));
        assertEquals(0, serviceUsage.get("disputeCount"));
        assertEquals(0, serviceUsage.get("pointBalance"));
        assertEquals(0, serviceUsage.get("refundCount"));
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??14: ?„ì²´ CRUD ?œë‚˜ë¦¬ì˜¤ ì¢…í•© ê²€ì¦?)
    void integrationTest_completeCrudScenario() {
        // 1. ì¡°íšŒ (Read)
        Optional<Seller> foundSeller = sellerSVC.findById(testSeller.getId());
        assertTrue(foundSeller.isPresent());

        // 2. ?˜ì • (Update)
        Seller updateSeller = Seller.builder()
                .shopName("?˜ì •?œìƒ?ëª…")
                .name("?˜ì •?œë??œìëª?)
                .tel("02-9999-9999")
                .address("?˜ì •?œì£¼??)
                .build();
        
        int updateResult = sellerSVC.update(testSeller.getId(), updateSeller);
        assertEquals(1, updateResult);

        // 3. ?˜ì • ?•ì¸
        Optional<Seller> updatedSeller = sellerSVC.findById(testSeller.getId());
        assertTrue(updatedSeller.isPresent());
        assertEquals("?˜ì •?œìƒ?ëª…", updatedSeller.get().getShopName());
        assertEquals("?˜ì •?œë??œìëª?, updatedSeller.get().getName());

        // 4. ?ˆí‡´ (Delete - ?¼ë¦¬???? œ)
        int withdrawResult = sellerSVC.withdraw(testSeller.getId(), "?ŒìŠ¤???„ë£Œ");
        assertEquals(1, withdrawResult);

        // 5. ?ˆí‡´ ?•ì¸
        Optional<Seller> withdrawnSeller = sellerSVC.findById(testSeller.getId());
        assertTrue(withdrawnSeller.isPresent());
        assertTrue(sellerSVC.isWithdrawn(withdrawnSeller.get()));

        // 6. ?¬í™œ?±í™”
        Optional<Seller> reactivatedSeller = sellerSVC.reactivate(testSeller.getEmail(), testSeller.getPassword());
        assertTrue(reactivatedSeller.isPresent());
        assertFalse(sellerSVC.isWithdrawn(reactivatedSeller.get()));

        // 7. ìµœì¢… ?íƒœ ê²€ì¦?        assertTrue(sellerSVC.canLogin(reactivatedSeller.get()));
        assertEquals(MemberStatus.ACTIVE.getCode(), reactivatedSeller.get().getMemberStatus());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??15: ?ë§¤??ê³ ìœ  ?œì•½ ì¡°ê±´ ê²€ì¦?)
    void integrationTest_sellerUniqueConstraints() {
        // Given - ?„ì¬ ?ë§¤???•ë³´ ì¡°íšŒ
        Optional<Seller> currentSeller = sellerSVC.findById(testSeller.getId());
        assertTrue(currentSeller.isPresent());

        // When & Then - ê³ ìœ  ?œì•½ ì¡°ê±´ ê²€ì¦?        assertTrue(sellerSVC.existsByEmail(currentSeller.get().getEmail()));
        assertTrue(sellerSVC.existsByBizRegNo(currentSeller.get().getBizRegNo()));
        assertTrue(sellerSVC.existsByShopName(currentSeller.get().getShopName()));
        assertTrue(sellerSVC.existsByName(currentSeller.get().getName()));
        assertTrue(sellerSVC.existsByShopAddress(currentSeller.get().getAddress()));

        // ì¡´ì¬?˜ì? ?ŠëŠ” ê°’ë“¤ ê²€ì¦?        assertFalse(sellerSVC.existsByEmail("nonexistent@test.com"));
        assertFalse(sellerSVC.existsByBizRegNo("999-99-99999"));
        assertFalse(sellerSVC.existsByShopName("ì¡´ì¬?˜ì??ŠëŠ”?ì "));
        assertFalse(sellerSVC.existsByName("ì¡´ì¬?˜ì??ŠëŠ”?€?œì"));
        assertFalse(sellerSVC.existsByShopAddress("ì¡´ì¬?˜ì??ŠëŠ”ì£¼ì†Œ"));
    }
} 
