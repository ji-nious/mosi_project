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
 * êµ¬ë§¤???Œì›ê°€???µí•© ?ŒìŠ¤???œë‚˜ë¦¬ì˜¤
 * ?¤ì œ ?°ì´?°ë² ?´ìŠ¤?€ ëª¨ë“  ê³„ì¸µ???µí•©???˜ê²½?ì„œ ?ŒìŠ¤?? */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("êµ¬ë§¤???Œì›ê°€???µí•© ?ŒìŠ¤??)
class BuyerIntegrationTest {

    @Autowired
    private BuyerSVC buyerSVC;

    @Autowired
    private BuyerDAO buyerDAO;

    private Buyer createValidBuyer() {
        return Buyer.builder()
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
    }

    @BeforeEach
    void setUp() {
        // ?ŒìŠ¤???°ì´???•ë¦¬
        buyerDAO.deleteAll();
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??1: ?•ìƒ ?Œì›ê°€???Œë¡œ??)
    void integrationTest_normalSignupFlow() {
        // Given - ? íš¨??êµ¬ë§¤???°ì´??        Buyer buyer = createValidBuyer();

        // When - ?Œì›ê°€???¤í–‰
        Buyer savedBuyer = buyerSVC.join(buyer);

        // Then - ?Œì›ê°€???±ê³µ ê²€ì¦?        assertNotNull(savedBuyer);
        assertNotNull(savedBuyer.getId());
        assertEquals(buyer.getEmail(), savedBuyer.getEmail());
        assertEquals(buyer.getNickname(), savedBuyer.getNickname());
        assertEquals(MemberStatus.ACTIVE.getCode(), savedBuyer.getMemberStatus());

        // ?°ì´?°ë² ?´ìŠ¤ ?€??ê²€ì¦?        Optional<Buyer> foundBuyer = buyerDAO.findByEmail(buyer.getEmail());
        assertTrue(foundBuyer.isPresent());
        assertEquals(buyer.getEmail(), foundBuyer.get().getEmail());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??2: ?´ë©”??ì¤‘ë³µ ???Œì›ê°€???¤íŒ¨")
    void integrationTest_emailDuplicateFailure() {
        // Given - ì²?ë²ˆì§¸ ?Œì›ê°€??        Buyer firstBuyer = createValidBuyer();
        buyerSVC.join(firstBuyer);

        // When & Then - ê°™ì? ?´ë©”?¼ë¡œ ??ë²ˆì§¸ ?Œì›ê°€???œë„
        Buyer secondBuyer = createValidBuyer();
        secondBuyer.setNickname("differentNickname");

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            buyerSVC.join(secondBuyer);
        });

        assertEquals("?´ë? ê°€?…ëœ ?´ë©”?¼ì…?ˆë‹¤.", exception.getMessage());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??3: ?‰ë„¤??ì¤‘ë³µ ???Œì›ê°€???¤íŒ¨")
    void integrationTest_nicknameDuplicateFailure() {
        // Given - ì²?ë²ˆì§¸ ?Œì›ê°€??        Buyer firstBuyer = createValidBuyer();
        buyerSVC.join(firstBuyer);

        // When & Then - ê°™ì? ?‰ë„¤?„ìœ¼ë¡???ë²ˆì§¸ ?Œì›ê°€???œë„
        Buyer secondBuyer = createValidBuyer();
        secondBuyer.setEmail("different@test.com");

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            buyerSVC.join(secondBuyer);
        });

        assertEquals("?´ë? ?¬ìš© ì¤‘ì¸ ?‰ë„¤?„ì…?ˆë‹¤.", exception.getMessage());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??4: ?ˆí‡´???Œì› ?¬ê????±ê³µ")
    void integrationTest_withdrawnMemberRejoinSuccess() {
        // Given - ?Œì›ê°€?????ˆí‡´
        Buyer buyer = createValidBuyer();
        Buyer savedBuyer = buyerSVC.join(buyer);
        buyerSVC.withdraw(savedBuyer.getId(), "ê°œì¸?¬ìœ ");

        // When - ?™ì¼???´ë©”?¼ë¡œ ?¬ê????œë„
        Buyer rejoinBuyer = createValidBuyer();
        rejoinBuyer.setNickname("newNickname"); // ?‰ë„¤??ë³€ê²?
        // Then - ?¬ê????±ê³µ
        Buyer rejoinedBuyer = buyerSVC.join(rejoinBuyer);
        assertNotNull(rejoinedBuyer);
        assertEquals(buyer.getEmail(), rejoinedBuyer.getEmail());
        assertEquals("newNickname", rejoinedBuyer.getNickname());
        assertEquals(MemberStatus.ACTIVE.getCode(), rejoinedBuyer.getMemberStatus());
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??5: ?Œì›ê°€????ì¦‰ì‹œ ë¡œê·¸???±ê³µ")
    void integrationTest_signupThenLoginSuccess() {
        // Given - ?Œì›ê°€??        Buyer buyer = createValidBuyer();
        buyerSVC.join(buyer);

        // When - ë¡œê·¸???œë„
        Buyer loginBuyer = buyerSVC.login(buyer.getEmail(), buyer.getPassword());

        // Then - ë¡œê·¸???±ê³µ
        assertNotNull(loginBuyer);
        assertEquals(buyer.getEmail(), loginBuyer.getEmail());
        assertTrue(buyerSVC.canLogin(loginBuyer));
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??6: ?Œì›ê°€????ì¤‘ë³µ ì²´í¬ ê¸°ëŠ¥ ê²€ì¦?)
    void integrationTest_duplicateCheckAfterSignup() {
        // Given - ?Œì›ê°€??        Buyer buyer = createValidBuyer();
        buyerSVC.join(buyer);

        // When & Then - ì¤‘ë³µ ì²´í¬ ê²€ì¦?        assertTrue(buyerSVC.existsByEmail(buyer.getEmail()));
        assertTrue(buyerSVC.existsByNickname(buyer.getNickname()));
        assertFalse(buyerSVC.existsByEmail("nonexistent@test.com"));
        assertFalse(buyerSVC.existsByNickname("nonexistentNickname"));
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??7: ?Œì›ê°€????ì´ˆê¸° ?íƒœ ê²€ì¦?)
    void integrationTest_initialStateAfterSignup() {
        // Given - ?Œì›ê°€??        Buyer buyer = createValidBuyer();
        Buyer savedBuyer = buyerSVC.join(buyer);

        // When - ì´ˆê¸° ?íƒœ ì¡°íšŒ
        Map<String, Object> serviceUsage = buyerSVC.getServiceUsage(savedBuyer.getId());

        // Then - ì´ˆê¸° ?íƒœ ê²€ì¦?        assertTrue((Boolean) serviceUsage.get("canWithdraw"));
        assertEquals(0, serviceUsage.get("orderCount"));
        assertEquals(0, serviceUsage.get("pointBalance"));
        assertEquals(0, serviceUsage.get("disputeCount"));
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??8: ?˜ëª»???°ì´?°ë¡œ ?Œì›ê°€?????ˆì™¸ ë°œìƒ")
    void integrationTest_invalidDataSignupFailure() {
        // Given - ?˜ëª»???´ë©”???•ì‹
        Buyer invalidBuyer = createValidBuyer();
        invalidBuyer.setEmail("invalid-email");

        // When & Then - ?°ì´??? íš¨??ê²€ì¦??¤íŒ¨
        assertThrows(Exception.class, () -> {
            buyerSVC.join(invalidBuyer);
        });
    }

    @Test
    @DisplayName("?µí•© ?ŒìŠ¤??9: ?„ì²´ ?Œì›ê°€???„ë¡œ?¸ìŠ¤ ì¢…í•© ê²€ì¦?)
    void integrationTest_completeSignupProcessValidation() {
        // Given - ? íš¨??êµ¬ë§¤???°ì´??        Buyer buyer = createValidBuyer();

        // When - ?Œì›ê°€???¤í–‰
        Buyer savedBuyer = buyerSVC.join(buyer);

        // Then - ì¢…í•© ê²€ì¦?        // 1. ê¸°ë³¸ ?•ë³´ ê²€ì¦?        assertEquals(buyer.getName(), savedBuyer.getName());
        assertEquals(buyer.getEmail(), savedBuyer.getEmail());
        assertEquals(buyer.getTel(), savedBuyer.getTel());

        // 2. ?œìŠ¤???¤ì • ê²€ì¦?        assertEquals(MemberGubun.BRONZE.getCode(), savedBuyer.getMemberGubun());
        assertEquals(MemberStatus.ACTIVE.getCode(), savedBuyer.getMemberStatus());

        // 3. ë¡œê·¸??ê°€???íƒœ ê²€ì¦?        assertTrue(buyerSVC.canLogin(savedBuyer));
        assertFalse(buyerSVC.isWithdrawn(savedBuyer));

        // 4. ?œë¹„???´ìš© ê°€???íƒœ ê²€ì¦?        assertTrue(buyerSVC.canWithdraw(savedBuyer.getId()));

        // 5. ?°ì´?°ë² ?´ìŠ¤ ?¼ê???ê²€ì¦?        Optional<Buyer> dbBuyer = buyerDAO.findByEmail(buyer.getEmail());
        assertTrue(dbBuyer.isPresent());
        assertEquals(savedBuyer.getId(), dbBuyer.get().getId());
    }
} 
