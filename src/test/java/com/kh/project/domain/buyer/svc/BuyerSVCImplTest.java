package com.kh.project.domain.buyer.svc;

import com.kh.project.domain.buyer.dao.BuyerDAO;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.domain.entity.MemberGubun;
import com.kh.project.domain.entity.MemberStatus;
import com.kh.project.web.exception.BusinessValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * BuyerSVC ?¬ê´„???¨ìœ„ ?ŒìŠ¤?? */
@ExtendWith(MockitoExtension.class)
@DisplayName("BuyerSVC ?¬ê´„???ŒìŠ¤??)
class BuyerSVCImplTest {

    @Mock
    private BuyerDAO buyerDAO;

    @InjectMocks
    private BuyerSVCImpl buyerSVC;

    private Buyer testBuyer;

    @BeforeEach
    void setUp() {
        testBuyer = createSampleBuyer();
    }

    private Buyer createSampleBuyer() {
        Buyer buyer = new Buyer();
        buyer.setBuyerId(1L);
        buyer.setEmail("test@buyer.com");
        buyer.setPassword("TestPass123!");
        buyer.setName("?ŒìŠ¤?¸êµ¬ë§¤ì");
        buyer.setNickname("?ŒìŠ¤??);
        buyer.setTel("010-1234-5678");
        buyer.setGender("?¨ì„±");
        buyer.setBirth(new Date());
        buyer.setAddress("ë¶€?°ì‹œ ?´ìš´?€êµ?);
        buyer.setGubun(MemberGubun.NEW.getCode());
        buyer.setStatus(MemberStatus.ACTIVE);
        buyer.setCdate(new Date());
        return buyer;
    }

    // ==================== ?Œì›ê°€???ŒìŠ¤??====================

    @Test
    @DisplayName("?Œì›ê°€??- ?±ê³µ")
    void join_success() {
        // given
        when(buyerDAO.existsByEmail(testBuyer.getEmail())).thenReturn(false);
        when(buyerDAO.existsByNickname(testBuyer.getNickname())).thenReturn(false);
        when(buyerDAO.save(any(Buyer.class))).thenReturn(testBuyer);

        // when
        Buyer savedBuyer = buyerSVC.join(testBuyer);

        // then
        assertNotNull(savedBuyer);
        assertEquals(testBuyer.getEmail(), savedBuyer.getEmail());
        assertEquals(MemberGubun.NEW.getCode(), savedBuyer.getGubun());
        verify(buyerDAO).existsByEmail(testBuyer.getEmail());
        verify(buyerDAO).existsByNickname(testBuyer.getNickname());
        verify(buyerDAO).save(any(Buyer.class));
    }

    @Test
    @DisplayName("?Œì›ê°€??- ?¤íŒ¨ (?´ë©”??ì¤‘ë³µ)")
    void join_fail_email_exists() {
        // given
        when(buyerDAO.existsByEmail(testBuyer.getEmail())).thenReturn(true);

        // when & then
        assertThrows(BusinessValidationException.class, () -> {
            buyerSVC.join(testBuyer);
        });

        verify(buyerDAO).existsByEmail(testBuyer.getEmail());
        verify(buyerDAO, never()).save(any(Buyer.class));
    }

    @Test
    @DisplayName("?Œì›ê°€??- ?¤íŒ¨ (?‰ë„¤??ì¤‘ë³µ)")
    void join_fail_nickname_exists() {
        // given
        when(buyerDAO.existsByEmail(testBuyer.getEmail())).thenReturn(false);
        when(buyerDAO.existsByNickname(testBuyer.getNickname())).thenReturn(true);

        // when & then
        assertThrows(BusinessValidationException.class, () -> {
            buyerSVC.join(testBuyer);
        });

        verify(buyerDAO).existsByEmail(testBuyer.getEmail());
        verify(buyerDAO).existsByNickname(testBuyer.getNickname());
        verify(buyerDAO, never()).save(any(Buyer.class));
    }

    // ==================== ë¡œê·¸???ŒìŠ¤??====================

    @Test
    @DisplayName("ë¡œê·¸??- ?±ê³µ")
    void login_success() {
        // given
        when(buyerDAO.findByEmail(testBuyer.getEmail())).thenReturn(Optional.of(testBuyer));

        // when
        Buyer loginBuyer = buyerSVC.login(testBuyer.getEmail(), testBuyer.getPassword());

        // then
        assertNotNull(loginBuyer);
        assertEquals(testBuyer.getEmail(), loginBuyer.getEmail());
        verify(buyerDAO).findByEmail(testBuyer.getEmail());
    }

    @Test
    @DisplayName("ë¡œê·¸??- ?¤íŒ¨ (ì¡´ì¬?˜ì? ?ŠëŠ” ?´ë©”??")
    void login_fail_user_not_found() {
        // given
        when(buyerDAO.findByEmail("notfound@email.com")).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessValidationException.class, () -> {
            buyerSVC.login("notfound@email.com", "password");
        });

        verify(buyerDAO).findByEmail("notfound@email.com");
    }

    @Test
    @DisplayName("ë¡œê·¸??- ?¤íŒ¨ (?˜ëª»??ë¹„ë?ë²ˆí˜¸)")
    void login_fail_wrong_password() {
        // given
        when(buyerDAO.findByEmail(testBuyer.getEmail())).thenReturn(Optional.of(testBuyer));

        // when & then
        assertThrows(BusinessValidationException.class, () -> {
            buyerSVC.login(testBuyer.getEmail(), "wrongpassword");
        });

        verify(buyerDAO).findByEmail(testBuyer.getEmail());
    }

    @Test
    @DisplayName("ë¡œê·¸??- ?¤íŒ¨ (?ˆí‡´???Œì›)")
    void login_fail_withdrawn_user() {
        // given
        testBuyer.setStatus(MemberStatus.WITHDRAWN);
        testBuyer.setWithdrawnAt(new Date());
        when(buyerDAO.findByEmail(testBuyer.getEmail())).thenReturn(Optional.of(testBuyer));

        // when & then
        assertThrows(BusinessValidationException.class, () -> {
            buyerSVC.login(testBuyer.getEmail(), testBuyer.getPassword());
        });

        verify(buyerDAO).findByEmail(testBuyer.getEmail());
    }

    // ==================== ?•ë³´ ì¡°íšŒ ?ŒìŠ¤??====================

    @Test
    @DisplayName("IDë¡??Œì› ì¡°íšŒ - ?±ê³µ")
    void findById_success() {
        // given
        when(buyerDAO.findById(1L)).thenReturn(Optional.of(testBuyer));

        // when
        Optional<Buyer> foundBuyer = buyerSVC.findById(1L);

        // then
        assertTrue(foundBuyer.isPresent());
        assertEquals(testBuyer.getBuyerId(), foundBuyer.get().getBuyerId());
        verify(buyerDAO).findById(1L);
    }

    @Test
    @DisplayName("IDë¡??Œì› ì¡°íšŒ - ?¤íŒ¨ (ì¡´ì¬?˜ì? ?ŠìŒ)")
    void findById_not_found() {
        // given
        when(buyerDAO.findById(999L)).thenReturn(Optional.empty());

        // when
        Optional<Buyer> foundBuyer = buyerSVC.findById(999L);

        // then
        assertFalse(foundBuyer.isPresent());
        verify(buyerDAO).findById(999L);
    }

    // ==================== ?•ë³´ ?˜ì • ?ŒìŠ¤??====================

    @Test
    @DisplayName("?Œì› ?•ë³´ ?˜ì • - ?±ê³µ")
    void update_success() {
        // given
        Buyer updateBuyer = new Buyer();
        updateBuyer.setName("?˜ì •?œì´ë¦?);
        updateBuyer.setTel("010-9999-8888");
        
        when(buyerDAO.update(eq(1L), any(Buyer.class))).thenReturn(1);

        // when
        int updatedRows = buyerSVC.update(1L, updateBuyer);

        // then
        assertEquals(1, updatedRows);
        verify(buyerDAO).update(eq(1L), any(Buyer.class));
    }

    @Test
    @DisplayName("?Œì› ?•ë³´ ?˜ì • - ?¤íŒ¨ (ì¡´ì¬?˜ì? ?ŠëŠ” ?Œì›)")
    void update_fail_user_not_found() {
        // given
        Buyer updateBuyer = new Buyer();
        updateBuyer.setName("?˜ì •?œì´ë¦?);
        
        when(buyerDAO.update(eq(999L), any(Buyer.class))).thenReturn(0);

        // when
        int updatedRows = buyerSVC.update(999L, updateBuyer);

        // then
        assertEquals(0, updatedRows);
        verify(buyerDAO).update(eq(999L), any(Buyer.class));
    }

    // ==================== ?ˆí‡´ ?ŒìŠ¤??====================

    @Test
    @DisplayName("?Œì› ?ˆí‡´ - ?±ê³µ")
    void withdraw_success() {
        // given
        String reason = "?œë¹„??ë¶ˆë§Œì¡?;
        when(buyerDAO.withdrawWithReason(1L, reason)).thenReturn(1);

        // when
        int withdrawnRows = buyerSVC.withdraw(1L, reason);

        // then
        assertEquals(1, withdrawnRows);
        verify(buyerDAO).withdrawWithReason(1L, reason);
    }

    @Test
    @DisplayName("?Œì› ?ˆí‡´ - ?¤íŒ¨ (ì¡´ì¬?˜ì? ?ŠëŠ” ?Œì›)")
    void withdraw_fail_user_not_found() {
        // given
        String reason = "?œë¹„??ë¶ˆë§Œì¡?;
        when(buyerDAO.withdrawWithReason(999L, reason)).thenReturn(0);

        // when
        int withdrawnRows = buyerSVC.withdraw(999L, reason);

        // then
        assertEquals(0, withdrawnRows);
        verify(buyerDAO).withdrawWithReason(999L, reason);
    }

    // ==================== ì¤‘ë³µ ì²´í¬ ?ŒìŠ¤??====================

    @Test
    @DisplayName("?´ë©”??ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ??)
    void existsByEmail_true() {
        // given
        when(buyerDAO.existsByEmail("test@buyer.com")).thenReturn(true);

        // when
        boolean exists = buyerSVC.existsByEmail("test@buyer.com");

        // then
        assertTrue(exists);
        verify(buyerDAO).existsByEmail("test@buyer.com");
    }

    @Test
    @DisplayName("?´ë©”??ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ ?ˆë¨")
    void existsByEmail_false() {
        // given
        when(buyerDAO.existsByEmail("new@buyer.com")).thenReturn(false);

        // when
        boolean exists = buyerSVC.existsByEmail("new@buyer.com");

        // then
        assertFalse(exists);
        verify(buyerDAO).existsByEmail("new@buyer.com");
    }

    @Test
    @DisplayName("?‰ë„¤??ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ??)
    void existsByNickname_true() {
        // given
        when(buyerDAO.existsByNickname("?ŒìŠ¤??)).thenReturn(true);

        // when
        boolean exists = buyerSVC.existsByNickname("?ŒìŠ¤??);

        // then
        assertTrue(exists);
        verify(buyerDAO).existsByNickname("?ŒìŠ¤??);
    }

    @Test
    @DisplayName("?‰ë„¤??ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ ?ˆë¨")
    void existsByNickname_false() {
        // given
        when(buyerDAO.existsByNickname("?ˆë‹‰?¤ì„")).thenReturn(false);

        // when
        boolean exists = buyerSVC.existsByNickname("?ˆë‹‰?¤ì„");

        // then
        assertFalse(exists);
        verify(buyerDAO).existsByNickname("?ˆë‹‰?¤ì„");
    }

    // ==================== ë¹„ì¦ˆ?ˆìŠ¤ ë¡œì§ ?ŒìŠ¤??====================

    @Test
    @DisplayName("ë¡œê·¸??ê°€???¬ë? ì²´í¬ - ê°€??)
    void canLogin_true() {
        // given
        testBuyer.setStatus(MemberStatus.ACTIVE);
        testBuyer.setWithdrawnAt(null);

        // when
        boolean canLogin = buyerSVC.canLogin(testBuyer);

        // then
        assertTrue(canLogin);
    }

    @Test
    @DisplayName("ë¡œê·¸??ê°€???¬ë? ì²´í¬ - ë¶ˆê???(?ˆí‡´)")
    void canLogin_false_withdrawn() {
        // given
        testBuyer.setStatus(MemberStatus.WITHDRAWN);
        testBuyer.setWithdrawnAt(new Date());

        // when
        boolean canLogin = buyerSVC.canLogin(testBuyer);

        // then
        assertFalse(canLogin);
    }

    @Test
    @DisplayName("?ˆí‡´ ?¬ë? ì²´í¬ - ?ˆí‡´??)
    void isWithdrawn_true() {
        // given
        testBuyer.setStatus(MemberStatus.WITHDRAWN);
        testBuyer.setWithdrawnAt(new Date());

        // when
        boolean isWithdrawn = buyerSVC.isWithdrawn(testBuyer);

        // then
        assertTrue(isWithdrawn);
    }

    @Test
    @DisplayName("?ˆí‡´ ?¬ë? ì²´í¬ - ?ˆí‡´ ?ˆí•¨")
    void isWithdrawn_false() {
        // given
        testBuyer.setStatus(MemberStatus.ACTIVE);
        testBuyer.setWithdrawnAt(null);

        // when
        boolean isWithdrawn = buyerSVC.isWithdrawn(testBuyer);

        // then
        assertFalse(isWithdrawn);
    }

    @Test
    @DisplayName("?Œì› ?±ê¸‰ ?•ë³´ ì¡°íšŒ - ?•ìƒ")
    void getGubunInfo_Success() {
        // given
        testBuyer.setGubun(MemberGubun.GOLD);

        // when
        Map<String, String> gubunInfo = buyerSVC.getGubunInfo(testBuyer);

        // then
        assertThat(gubunInfo).isNotNull();
        assertThat(gubunInfo.get("code")).isEqualTo("GOLD");
        assertThat(gubunInfo.get("name")).isEqualTo("ê³¨ë“œ");
    }

    @Test
    @DisplayName("?Œì› ?íƒœ ?•ë³´ ì¡°íšŒ - ?•ìƒ")
    void getStatusInfo_Success() {
        // given
        testBuyer.setStatus(MemberStatus.ACTIVE);

        // when
        Map<String, String> statusInfo = buyerSVC.getStatusInfo(testBuyer);

        // then
        assertThat(statusInfo).isNotNull();
        assertThat(statusInfo.get("code")).isEqualTo("?œì„±??);
        assertThat(statusInfo.get("name")).isEqualTo("?œì„±??);
    }

    @Test
    @DisplayName("?Œì› ?•ë³´ ì¡°íšŒ - null??ê²½ìš°")
    void getInfo_Null() {
        // when
        Map<String, String> gubunInfo = buyerSVC.getGubunInfo(null);
        Map<String, String> statusInfo = buyerSVC.getStatusInfo(null);

        // then
        assertThat(gubunInfo.get("code")).isEqualTo("UNKNOWN");
        assertThat(statusInfo.get("code")).isEqualTo("UNKNOWN");
    }

    @Test
    @DisplayName("?Œì› ?±ê¸‰ ?•ë³´ ì¡°íšŒ - ?±ê¸‰??null??ê²½ìš°")
    void getGubunInfo_NullGubun() {
        // given
        testBuyer.setGubun(null);

        // when
        Map<String, String> gubunInfo = buyerSVC.getGubunInfo(testBuyer);

        // then
        assertThat(gubunInfo.get("code")).isEqualTo("UNKNOWN");
        assertThat(gubunInfo.get("name")).isEqualTo("?????†ìŒ");
    }

    // ==================== ?±ê¸‰ ?¹ê¸‰ ?ŒìŠ¤??====================

    @Test
    @DisplayName("?±ê¸‰ ?¹ê¸‰ - ?±ê³µ")
    void upgradeGubun_success() {
        // given
        when(buyerDAO.update(eq(1L), any(Buyer.class))).thenReturn(1);

        // when & then
        assertDoesNotThrow(() -> {
            buyerSVC.upgradeGubun(1L, MemberGubun.BRONZE.getCode());
        });

        verify(buyerDAO).update(eq(1L), any(Buyer.class));
    }

    // ==================== ê´€ë¦?ê¸°ëŠ¥ ?ŒìŠ¤??====================

    @Test
    @DisplayName("?ˆí‡´ ?Œì› ëª©ë¡ ì¡°íšŒ")
    void getWithdrawnMembers() {
        // given
        Buyer withdrawnBuyer1 = createSampleBuyer();
        withdrawnBuyer1.setBuyerId(2L);
        withdrawnBuyer1.setStatus(MemberStatus.WITHDRAWN);
        
        Buyer withdrawnBuyer2 = createSampleBuyer();
        withdrawnBuyer2.setBuyerId(3L);
        withdrawnBuyer2.setStatus(MemberStatus.WITHDRAWN);

        List<Buyer> withdrawnList = Arrays.asList(withdrawnBuyer1, withdrawnBuyer2);
        when(buyerDAO.findWithdrawnMembers()).thenReturn(withdrawnList);

        // when
        List<Buyer> result = buyerSVC.getWithdrawnMembers();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(buyer -> MemberStatus.WITHDRAWN.equals(buyer.getStatus())));
        verify(buyerDAO).findWithdrawnMembers();
    }

    // ==================== Edge Case ?ŒìŠ¤??====================

    @Test
    @DisplayName("null ?Œì› ë¹„ì¦ˆ?ˆìŠ¤ ë¡œì§ ?ŒìŠ¤??)
    void businessLogic_with_null_buyer() {
        // when & then
        assertFalse(buyerSVC.canLogin(null));
        assertFalse(buyerSVC.isWithdrawn(null));
        
        Map<String, String> gubunInfo = buyerSVC.getGubunInfo(null);
        assertThat(gubunInfo.get("code")).isEqualTo("UNKNOWN");
        assertThat(gubunInfo.get("name")).isEqualTo("?????†ìŒ");
        
        Map<String, String> statusInfo = buyerSVC.getStatusInfo(null);
        assertThat(statusInfo.get("code")).isEqualTo("UNKNOWN");
        assertThat(statusInfo.get("name")).isEqualTo("?????†ìŒ");
    }

    @Test
    @DisplayName("?˜ëª»???±ê¸‰ ì½”ë“œ ì²˜ë¦¬")
    void getGubunInfo_invalid_code() {
        // given
        testBuyer.setGubun("INVALID_CODE");

        // when
        Map<String, String> gubunInfo = buyerSVC.getGubunInfo(testBuyer);

        // then
        assertThat(gubunInfo.get("code")).isEqualTo(MemberGubun.NEW.getCode());
        assertThat(gubunInfo.get("name")).isEqualTo(MemberGubun.NEW.getDescription());
    }
}
