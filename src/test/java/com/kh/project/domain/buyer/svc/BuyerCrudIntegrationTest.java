package com.kh.project.domain.buyer.svc;

import com.kh.project.domain.buyer.dao.BuyerDAO;
import com.kh.project.domain.entity.Buyer;
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
 * êµ¬ë§¤??ì¡°íšŒ/?˜ì •/?ˆí‡´ ?µí•© ?ŒìŠ¤???œë‚˜ë¦¬ì˜¤
 * ?¤ì œ ?°ì´?°ë² ?´ìŠ¤?€ ëª¨ë“  ê³„ì¸µ???µí•©???˜ê²½?ì„œ ?ŒìŠ¤?? */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("êµ¬ë§¤??ì¡°íšŒ/?˜ì •/?ˆí‡´ ?µí•© ?ŒìŠ¤??)
class BuyerCrudIntegrationTest {

    @Autowired
    private BuyerSVC buyerSVC;

    @Autowired
    private BuyerDAO buyerDAO;

    private Buyer testBuyer;

    @BeforeEach
    void setUp() {
        // ?ŒìŠ¤???°ì´???•ë¦¬
        buyerDAO.deleteAll();
        
        // ?ŒìŠ¤?¸ìš© êµ¬ë§¤???ì„±
        testBuyer = Buyer.builder()
                .name("ê¹€êµ¬ë§¤??)
                .nickname("buyer123")
                .email("buyer@test.com")
                .password("password123")
                .tel("010-1234-5678")
                .gender("?¨ì„±")
                .birth(LocalDate.of(1990, 1, 1))
                .postcode("12345")
                .address("?œìš¸??ê°•ë‚¨êµ??ŒìŠ¤?¸ë¡œ 123")
                .detailAddress("101??)
                .memberGubun(MemberGubun.BRONZE.getCode())
                .memberStatus(MemberStatus.ACTIVE.getCode())
                .build();
        
        testBuyer = buyerSVC.join(testBuyer);
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??1: ?Œì› ?•ë³´ ì¡°íšŒ - IDë¡?ì¡°íšŒ")
    void integrationTest_findById() {
        // When - IDë¡??Œì› ì¡°íšŒ
        Optional<Buyer> foundBuyer = buyerSVC.findById(testBuyer.getId());

        // Then - ì¡°íšŒ ?±ê³µ ê²€ì¦?        assertTrue(foundBuyer.isPresent());
        assertEquals(testBuyer.getId(), foundBuyer.get().getId());
        assertEquals(testBuyer.getEmail(), foundBuyer.get().getEmail());
        assertEquals(testBuyer.getNickname(), foundBuyer.get().getNickname());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??2: ?Œì› ?•ë³´ ì¡°íšŒ - ?´ë©”?¼ë¡œ ì¡°íšŒ")
    void integrationTest_findByEmail() {
        // When - ?´ë©”?¼ë¡œ ?Œì› ì¡°íšŒ
        Optional<Buyer> foundBuyer = buyerSVC.findByEmail(testBuyer.getEmail());

        // Then - ì¡°íšŒ ?±ê³µ ê²€ì¦?        assertTrue(foundBuyer.isPresent());
        assertEquals(testBuyer.getEmail(), foundBuyer.get().getEmail());
        assertEquals(testBuyer.getId(), foundBuyer.get().getId());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??3: ?Œì› ?•ë³´ ?˜ì • - ê¸°ë³¸ ?•ë³´ ?˜ì •")
    void integrationTest_updateBasicInfo() {
        // Given - ?˜ì •???•ë³´
        Buyer updateBuyer = Buyer.builder()
                .name("ê¹€êµ¬ë§¤?ìˆ˜??)
                .nickname("modifiedBuyer")
                .tel("010-9999-8888")
                .gender("?¬ì„±")
                .birth(LocalDate.of(1995, 5, 15))
                .postcode("54321")
                .address("ë¶€?°ì‹œ ?´ìš´?€êµ??˜ì •ë¡?456")
                .detailAddress("202??)
                .build();

        // When - ?•ë³´ ?˜ì • ?¤í–‰
        int updateCount = buyerSVC.update(testBuyer.getId(), updateBuyer);

        // Then - ?˜ì • ?±ê³µ ê²€ì¦?        assertEquals(1, updateCount);

        // ?˜ì •???•ë³´ ì¡°íšŒ ê²€ì¦?        Optional<Buyer> updatedBuyer = buyerSVC.findById(testBuyer.getId());
        assertTrue(updatedBuyer.isPresent());
        assertEquals("ê¹€êµ¬ë§¤?ìˆ˜??, updatedBuyer.get().getName());
        assertEquals("modifiedBuyer", updatedBuyer.get().getNickname());
        assertEquals("010-9999-8888", updatedBuyer.get().getTel());
        assertEquals("?¬ì„±", updatedBuyer.get().getGender());
        assertEquals("ë¶€?°ì‹œ ?´ìš´?€êµ??˜ì •ë¡?456", updatedBuyer.get().getAddress());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??4: ?Œì› ?•ë³´ ?˜ì • - ?‰ë„¤??ì¤‘ë³µ ???¤íŒ¨")
    void integrationTest_updateNicknameDuplicateFailure() {
        // Given - ?¤ë¥¸ ?Œì› ?ì„±
        Buyer anotherBuyer = Buyer.builder()
                .name("?¤ë¥¸êµ¬ë§¤??)
                .nickname("anotherBuyer")
                .email("another@test.com")
                .password("password123")
                .tel("010-1111-2222")
                .memberGubun(MemberGubun.BRONZE.getCode())
                .memberStatus(MemberStatus.ACTIVE.getCode())
                .build();
        buyerSVC.join(anotherBuyer);

        // When & Then - ê¸°ì¡´ ?‰ë„¤?„ìœ¼ë¡??˜ì • ?œë„
        Buyer updateBuyer = Buyer.builder()
                .nickname("anotherBuyer")
                .build();

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            buyerSVC.update(testBuyer.getId(), updateBuyer);
        });

        assertEquals("?´ë? ?¬ìš© ì¤‘ì¸ ?‰ë„¤?„ì…?ˆë‹¤.", exception.getMessage());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??5: ë¹„ë?ë²ˆí˜¸ ?•ì¸ ê²€ì¦?)
    void integrationTest_passwordVerification() {
        // When & Then - ?¬ë°”ë¥?ë¹„ë?ë²ˆí˜¸ ?•ì¸
        assertTrue(buyerSVC.checkPassword(testBuyer.getId(), "password123"));

        // When & Then - ?˜ëª»??ë¹„ë?ë²ˆí˜¸ ?•ì¸
        assertFalse(buyerSVC.checkPassword(testBuyer.getId(), "wrongPassword"));
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??6: ?Œì› ?ˆí‡´ - ?•ìƒ ?ˆí‡´")
    void integrationTest_withdrawSuccess() {
        // Given - ?ˆí‡´ ê°€?¥í•œ ?íƒœ ?•ì¸
        assertTrue(buyerSVC.canWithdraw(testBuyer.getId()));

        // When - ?Œì› ?ˆí‡´ ?¤í–‰
        int withdrawResult = buyerSVC.withdraw(testBuyer.getId(), "ê°œì¸?¬ìœ ");

        // Then - ?ˆí‡´ ?±ê³µ ê²€ì¦?        assertEquals(1, withdrawResult);

        // ?ˆí‡´ ???íƒœ ê²€ì¦?        Optional<Buyer> withdrawnBuyer = buyerSVC.findById(testBuyer.getId());
        assertTrue(withdrawnBuyer.isPresent());
        assertEquals(MemberStatus.WITHDRAWN.getCode(), withdrawnBuyer.get().getMemberStatus());
        assertTrue(buyerSVC.isWithdrawn(withdrawnBuyer.get()));
        assertFalse(buyerSVC.canLogin(withdrawnBuyer.get()));
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??7: ?ˆí‡´ ??ë¡œê·¸??ì°¨ë‹¨")
    void integrationTest_loginBlockedAfterWithdraw() {
        // Given - ?Œì› ?ˆí‡´
        buyerSVC.withdraw(testBuyer.getId(), "ê°œì¸?¬ìœ ");

        // When & Then - ?ˆí‡´???Œì› ë¡œê·¸???œë„
        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            buyerSVC.login(testBuyer.getEmail(), testBuyer.getPassword());
        });

        assertEquals("?ˆí‡´???Œì›?…ë‹ˆ??", exception.getMessage());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??8: ?ˆí‡´ ???¬í™œ?±í™”")
    void integrationTest_reactivateAfterWithdraw() {
        // Given - ?Œì› ?ˆí‡´
        buyerSVC.withdraw(testBuyer.getId(), "ê°œì¸?¬ìœ ");

        // When - ?¬í™œ?±í™” ?œë„
        Optional<Buyer> reactivatedBuyer = buyerSVC.reactivate(testBuyer.getEmail(), testBuyer.getPassword());

        // Then - ?¬í™œ?±í™” ?±ê³µ ê²€ì¦?        assertTrue(reactivatedBuyer.isPresent());
        assertEquals(MemberStatus.ACTIVE.getCode(), reactivatedBuyer.get().getMemberStatus());
        assertTrue(buyerSVC.canLogin(reactivatedBuyer.get()));
        assertFalse(buyerSVC.isWithdrawn(reactivatedBuyer.get()));
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??9: ?œë¹„???´ìš© ?„í™© ì¡°íšŒ")
    void integrationTest_serviceUsageInfo() {
        // When - ?œë¹„???´ìš© ?„í™© ì¡°íšŒ
        Map<String, Object> serviceUsage = buyerSVC.getServiceUsage(testBuyer.getId());

        // Then - ?´ìš© ?„í™© ê²€ì¦?        assertNotNull(serviceUsage);
        assertTrue(serviceUsage.containsKey("canWithdraw"));
        assertTrue(serviceUsage.containsKey("orderCount"));
        assertTrue(serviceUsage.containsKey("pointBalance"));
        assertTrue(serviceUsage.containsKey("disputeCount"));
        assertTrue(serviceUsage.containsKey("refundCount"));

        // ì´ˆê¸° ?íƒœ ê²€ì¦?        assertTrue((Boolean) serviceUsage.get("canWithdraw"));
        assertEquals(0, serviceUsage.get("orderCount"));
        assertEquals(0, serviceUsage.get("pointBalance"));
        assertEquals(0, serviceUsage.get("disputeCount"));
        assertEquals(0, serviceUsage.get("refundCount"));
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??10: ?Œì› ?±ê¸‰ ?•ë³´ ì¡°íšŒ")
    void integrationTest_memberGradeInfo() {
        // When - ?Œì› ?±ê¸‰ ?•ë³´ ì¡°íšŒ
        var gubunInfo = buyerSVC.getGubunInfo(testBuyer);
        var statusInfo = buyerSVC.getStatusInfo(testBuyer);

        // Then - ?±ê¸‰ ?•ë³´ ê²€ì¦?        assertNotNull(gubunInfo);
        assertEquals(MemberGubun.BRONZE.getCode(), gubunInfo.getCode());
        assertEquals(MemberGubun.BRONZE.getDescription(), gubunInfo.getName());

        assertNotNull(statusInfo);
        assertEquals(MemberStatus.ACTIVE.getCode(), statusInfo.getCode());
        assertEquals(MemberStatus.ACTIVE.getDescription(), statusInfo.getName());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??11: ?„ì²´ CRUD ?œë‚˜ë¦¬ì˜¤ ì¢…í•© ê²€ì¦?)
    void integrationTest_completeCrudScenario() {
        // 1. ì¡°íšŒ (Read)
        Optional<Buyer> foundBuyer = buyerSVC.findById(testBuyer.getId());
        assertTrue(foundBuyer.isPresent());

        // 2. ?˜ì • (Update)
        Buyer updateBuyer = Buyer.builder()
                .name("?˜ì •?œì´ë¦?)
                .nickname("modifiedNickname")
                .tel("010-9999-9999")
                .build();
        
        int updateResult = buyerSVC.update(testBuyer.getId(), updateBuyer);
        assertEquals(1, updateResult);

        // 3. ?˜ì • ?•ì¸
        Optional<Buyer> updatedBuyer = buyerSVC.findById(testBuyer.getId());
        assertTrue(updatedBuyer.isPresent());
        assertEquals("?˜ì •?œì´ë¦?, updatedBuyer.get().getName());
        assertEquals("modifiedNickname", updatedBuyer.get().getNickname());

        // 4. ?ˆí‡´ (Delete - ?¼ë¦¬???? œ)
        int withdrawResult = buyerSVC.withdraw(testBuyer.getId(), "?ŒìŠ¤???„ë£Œ");
        assertEquals(1, withdrawResult);

        // 5. ?ˆí‡´ ?•ì¸
        Optional<Buyer> withdrawnBuyer = buyerSVC.findById(testBuyer.getId());
        assertTrue(withdrawnBuyer.isPresent());
        assertTrue(buyerSVC.isWithdrawn(withdrawnBuyer.get()));

        // 6. ?¬í™œ?±í™”
        Optional<Buyer> reactivatedBuyer = buyerSVC.reactivate(testBuyer.getEmail(), testBuyer.getPassword());
        assertTrue(reactivatedBuyer.isPresent());
        assertFalse(buyerSVC.isWithdrawn(reactivatedBuyer.get()));

        // 7. ìµœì¢… ?íƒœ ê²€ì¦?        assertTrue(buyerSVC.canLogin(reactivatedBuyer.get()));
        assertEquals(MemberStatus.ACTIVE.getCode(), reactivatedBuyer.get().getMemberStatus());
    }
} 
