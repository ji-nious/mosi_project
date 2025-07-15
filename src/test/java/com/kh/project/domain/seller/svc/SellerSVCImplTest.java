package com.kh.project.domain.seller.svc;

import com.kh.project.domain.seller.dao.SellerDAO;
import com.kh.project.domain.entity.Seller;
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
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("SellerSVC ?¬ê´„???ŒìŠ¤??)
class SellerSVCImplTest {

    @InjectMocks
    private SellerSVCImpl sellerSVC;
    
    @Mock
    private SellerDAO sellerDAO;

    private Seller testSeller;

    @BeforeEach
    void setUp() {
        testSeller = createSampleSeller();
    }

    private Seller createSampleSeller() {
        Seller seller = new Seller();
        seller.setSellerId(1L);
        seller.setEmail("seller@shop.com");
        seller.setPassword("ShopPass123!");
        seller.setBizRegNo("111-22-33333");
        seller.setShopName("My Awesome Shop");
        seller.setName("John Doe");
        seller.setShopAddress("ë¶€?°ì‹œ ë¶€?°ì§„êµ??œë©´ë¡?);
        seller.setTel("010-1234-5678");
        seller.setGubun(MemberGubun.NEW.getCode());
        seller.setStatus(MemberStatus.ACTIVE);
        seller.setCdate(new Date());
        return seller;
    }

    // ==================== ?Œì›ê°€???ŒìŠ¤??====================

    @Test
    @DisplayName("?ë§¤???Œì›ê°€??- ?±ê³µ")
    void join_success() {
        // given: ?´ë©”?¼ê³¼ ?¬ì—…??ë²ˆí˜¸ê°€ ì¤‘ë³µ?˜ì? ?ŠëŠ”?¤ê³  ê°€??        when(sellerDAO.existsByEmail(testSeller.getEmail())).thenReturn(false);
        when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(false);
        when(sellerDAO.existsByShopName(testSeller.getShopName())).thenReturn(false);
        when(sellerDAO.save(any(Seller.class))).thenReturn(testSeller);

        // when: ?Œì›ê°€???¤í–‰
        Seller joinedSeller = sellerSVC.join(testSeller);

        // then: ê²°ê³¼ ê²€ì¦?        assertNotNull(joinedSeller);
        assertEquals("111-22-33333", joinedSeller.getBizRegNo());
        assertEquals("My Awesome Shop", joinedSeller.getShopName());
        assertEquals(MemberGubun.NEW.getCode(), joinedSeller.getGubun());
        
        verify(sellerDAO).existsByEmail(testSeller.getEmail());
        verify(sellerDAO).existsByBizRegNo(testSeller.getBizRegNo());
        verify(sellerDAO).existsByShopName(testSeller.getShopName());
        verify(sellerDAO).save(any(Seller.class));
    }

    @Test
    @DisplayName("?ë§¤???Œì›ê°€??- ?¤íŒ¨ (?´ë©”??ì¤‘ë³µ)")
    void join_fail_email_exists() {
        // given
        when(sellerDAO.existsByEmail(testSeller.getEmail())).thenReturn(true);

        // when & then
        assertThrows(BusinessValidationException.class, () -> {
            sellerSVC.join(testSeller);
        });

        verify(sellerDAO).existsByEmail(testSeller.getEmail());
        verify(sellerDAO, never()).save(any(Seller.class));
    }

    @Test
    @DisplayName("?ë§¤???Œì›ê°€??- ?¤íŒ¨ (?¬ì—…??ë²ˆí˜¸ ì¤‘ë³µ)")
    void join_fail_biz_reg_no_exists() {
        // given: ?´ë©”?¼ì? ì¤‘ë³µ???„ë‹ˆì§€ë§? ?¬ì—…??ë²ˆí˜¸??ì¤‘ë³µ?´ë¼ê³?ê°€??        when(sellerDAO.existsByEmail(testSeller.getEmail())).thenReturn(false);
        when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(true);

        // when & then: BusinessException ?ˆì™¸ê°€ ë°œìƒ?˜ëŠ”ì§€ ê²€ì¦?        assertThrows(BusinessValidationException.class, () -> {
            sellerSVC.join(testSeller);
        });

        verify(sellerDAO).existsByEmail(testSeller.getEmail());
        verify(sellerDAO).existsByBizRegNo(testSeller.getBizRegNo());
        verify(sellerDAO, never()).save(any(Seller.class));
    }

    @Test
    @DisplayName("?ë§¤???Œì›ê°€??- ?¤íŒ¨ (?í˜¸ëª?ì¤‘ë³µ)")
    void join_fail_shop_name_exists() {
        // given
        when(sellerDAO.existsByEmail(testSeller.getEmail())).thenReturn(false);
        when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(false);
        when(sellerDAO.existsByShopName(testSeller.getShopName())).thenReturn(true);

        // when & then
        assertThrows(BusinessValidationException.class, () -> {
            sellerSVC.join(testSeller);
        });

        verify(sellerDAO).existsByEmail(testSeller.getEmail());
        verify(sellerDAO).existsByBizRegNo(testSeller.getBizRegNo());
        verify(sellerDAO).existsByShopName(testSeller.getShopName());
        verify(sellerDAO, never()).save(any(Seller.class));
    }

    // ==================== ë¡œê·¸???ŒìŠ¤??====================

    @Test
    @DisplayName("?ë§¤??ë¡œê·¸??- ?±ê³µ")
    void login_success() {
        // given
        when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.of(testSeller));

        // when
        Seller loginSeller = sellerSVC.login(testSeller.getEmail(), testSeller.getPassword());

        // then
        assertNotNull(loginSeller);
        assertEquals(testSeller.getEmail(), loginSeller.getEmail());
        assertEquals(testSeller.getShopName(), loginSeller.getShopName());
        verify(sellerDAO).findByEmail(testSeller.getEmail());
    }

    @Test
    @DisplayName("?ë§¤??ë¡œê·¸??- ?¤íŒ¨ (?¬ìš©???†ìŒ)")
    void login_fail_user_not_found() {
        // given: DAOê°€ ?´ë©”?¼ë¡œ ì¡°íšŒ?˜ë©´, ê²°ê³¼ê°€ ?†ë‹¤ê³?empty) ê°€??        when(sellerDAO.findByEmail("seller@shop.com")).thenReturn(Optional.empty());

        // when & then: BusinessException ?ˆì™¸ê°€ ë°œìƒ?˜ëŠ”ì§€ ê²€ì¦?        assertThrows(BusinessValidationException.class, () -> {
            sellerSVC.login("seller@shop.com", "ShopPass123!");
        });

        verify(sellerDAO).findByEmail("seller@shop.com");
    }

    @Test
    @DisplayName("?ë§¤??ë¡œê·¸??- ?¤íŒ¨ (?˜ëª»??ë¹„ë?ë²ˆí˜¸)")
    void login_fail_wrong_password() {
        // given
        when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.of(testSeller));

        // when & then
        assertThrows(BusinessValidationException.class, () -> {
            sellerSVC.login(testSeller.getEmail(), "wrongpassword");
        });

        verify(sellerDAO).findByEmail(testSeller.getEmail());
    }

    @Test
    @DisplayName("?ë§¤??ë¡œê·¸??- ?¤íŒ¨ (?ˆí‡´???Œì›)")
    void login_fail_withdrawn_user() {
        // given
        testSeller.setStatus(MemberStatus.WITHDRAWN);
        testSeller.setWithdrawnAt(new Date());
        when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.of(testSeller));

        // when & then
        assertThrows(BusinessValidationException.class, () -> {
            sellerSVC.login(testSeller.getEmail(), testSeller.getPassword());
        });

        verify(sellerDAO).findByEmail(testSeller.getEmail());
    }

    // ==================== ?•ë³´ ì¡°íšŒ ?ŒìŠ¤??====================

    @Test
    @DisplayName("IDë¡??ë§¤??ì¡°íšŒ - ?±ê³µ")
    void findById_success() {
        // given
        when(sellerDAO.findById(1L)).thenReturn(Optional.of(testSeller));

        // when
        Optional<Seller> foundSeller = sellerSVC.findById(1L);

        // then
        assertTrue(foundSeller.isPresent());
        assertEquals(testSeller.getSellerId(), foundSeller.get().getSellerId());
        assertEquals(testSeller.getShopName(), foundSeller.get().getShopName());
        verify(sellerDAO).findById(1L);
    }

    @Test
    @DisplayName("IDë¡??ë§¤??ì¡°íšŒ - ?¤íŒ¨ (ì¡´ì¬?˜ì? ?ŠìŒ)")
    void findById_not_found() {
        // given
        when(sellerDAO.findById(999L)).thenReturn(Optional.empty());

        // when
        Optional<Seller> foundSeller = sellerSVC.findById(999L);

        // then
        assertFalse(foundSeller.isPresent());
        verify(sellerDAO).findById(999L);
    }

    // ==================== ?•ë³´ ?˜ì • ?ŒìŠ¤??====================

    @Test
    @DisplayName("?ë§¤???•ë³´ ?˜ì • - ?±ê³µ")
    void update_success() {
        // given
        Seller updateSeller = new Seller();
        updateSeller.setShopName("Updated Shop Name");
        updateSeller.setShopAddress("ë¶€?°ì‹œ ?™ë˜êµ??¨ì²œì²œë¡œ");
        
        when(sellerDAO.update(eq(1L), any(Seller.class))).thenReturn(1);

        // when
        int updatedRows = sellerSVC.update(1L, updateSeller);

        // then
        assertEquals(1, updatedRows);
        verify(sellerDAO).update(eq(1L), any(Seller.class));
    }

    @Test
    @DisplayName("?ë§¤???•ë³´ ?˜ì • - ?¤íŒ¨ (ì¡´ì¬?˜ì? ?ŠëŠ” ?ë§¤??")
    void update_fail_seller_not_found() {
        // given
        Seller updateSeller = new Seller();
        updateSeller.setShopName("Updated Shop Name");
        
        when(sellerDAO.update(eq(999L), any(Seller.class))).thenReturn(0);

        // when
        int updatedRows = sellerSVC.update(999L, updateSeller);

        // then
        assertEquals(0, updatedRows);
        verify(sellerDAO).update(eq(999L), any(Seller.class));
    }

    // ==================== ?ˆí‡´ ?ŒìŠ¤??====================

    @Test
    @DisplayName("?ë§¤???ˆí‡´ - ?±ê³µ")
    void withdraw_success() {
        // given
        String reason = "?¬ì—… ì¢…ë£Œ";
        when(sellerDAO.withdrawWithReason(1L, reason)).thenReturn(1);

        // when
        int withdrawnRows = sellerSVC.withdraw(1L, reason);

        // then
        assertEquals(1, withdrawnRows);
        verify(sellerDAO).withdrawWithReason(1L, reason);
    }

    @Test
    @DisplayName("?ë§¤???ˆí‡´ - ?¤íŒ¨ (ì¡´ì¬?˜ì? ?ŠëŠ” ?ë§¤??")
    void withdraw_fail_seller_not_found() {
        // given
        String reason = "?¬ì—… ì¢…ë£Œ";
        when(sellerDAO.withdrawWithReason(999L, reason)).thenReturn(0);

        // when
        int withdrawnRows = sellerSVC.withdraw(999L, reason);

        // then
        assertEquals(0, withdrawnRows);
        verify(sellerDAO).withdrawWithReason(999L, reason);
    }

    // ==================== ì¤‘ë³µ ì²´í¬ ?ŒìŠ¤??====================

    @Test
    @DisplayName("?´ë©”??ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ??)
    void existsByEmail_true() {
        // given
        when(sellerDAO.existsByEmail("seller@shop.com")).thenReturn(true);

        // when
        boolean exists = sellerSVC.existsByEmail("seller@shop.com");

        // then
        assertTrue(exists);
        verify(sellerDAO).existsByEmail("seller@shop.com");
    }

    @Test
    @DisplayName("?´ë©”??ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ ?ˆë¨")
    void existsByEmail_false() {
        // given
        when(sellerDAO.existsByEmail("new@seller.com")).thenReturn(false);

        // when
        boolean exists = sellerSVC.existsByEmail("new@seller.com");

        // then
        assertFalse(exists);
        verify(sellerDAO).existsByEmail("new@seller.com");
    }

    @Test
    @DisplayName("?¬ì—…?ë“±ë¡ë²ˆ??ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ??)
    void existsByBizRegNo_true() {
        // given
        when(sellerDAO.existsByBizRegNo("111-22-33333")).thenReturn(true);

        // when
        boolean exists = sellerSVC.existsByBizRegNo("111-22-33333");

        // then
        assertTrue(exists);
        verify(sellerDAO).existsByBizRegNo("111-22-33333");
    }

    @Test
    @DisplayName("?¬ì—…?ë“±ë¡ë²ˆ??ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ ?ˆë¨")
    void existsByBizRegNo_false() {
        // given
        when(sellerDAO.existsByBizRegNo("999-88-77777")).thenReturn(false);

        // when
        boolean exists = sellerSVC.existsByBizRegNo("999-88-77777");

        // then
        assertFalse(exists);
        verify(sellerDAO).existsByBizRegNo("999-88-77777");
    }

    @Test
    @DisplayName("?í˜¸ëª?ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ??)
    void existsByShopName_true() {
        // given
        when(sellerDAO.existsByShopName("My Awesome Shop")).thenReturn(true);

        // when
        boolean exists = sellerSVC.existsByShopName("My Awesome Shop");

        // then
        assertTrue(exists);
        verify(sellerDAO).existsByShopName("My Awesome Shop");
    }

    @Test
    @DisplayName("?í˜¸ëª?ì¤‘ë³µ ì²´í¬ - ì¤‘ë³µ ?ˆë¨")
    void existsByShopName_false() {
        // given
        when(sellerDAO.existsByShopName("New Shop")).thenReturn(false);

        // when
        boolean exists = sellerSVC.existsByShopName("New Shop");

        // then
        assertFalse(exists);
        verify(sellerDAO).existsByShopName("New Shop");
    }

    // ==================== ë¹„ì¦ˆ?ˆìŠ¤ ë¡œì§ ?ŒìŠ¤??====================

    @Test
    @DisplayName("ë¡œê·¸??ê°€???¬ë? ì²´í¬ - ê°€??)
    void canLogin_true() {
        // given
        testSeller.setStatus(MemberStatus.ACTIVE);
        testSeller.setWithdrawnAt(null);

        // when
        boolean canLogin = sellerSVC.canLogin(testSeller);

        // then
        assertTrue(canLogin);
    }

    @Test
    @DisplayName("ë¡œê·¸??ê°€???¬ë? ì²´í¬ - ë¶ˆê???(?ˆí‡´)")
    void canLogin_false_withdrawn() {
        // given
        testSeller.setStatus(MemberStatus.WITHDRAWN);
        testSeller.setWithdrawnAt(new Date());

        // when
        boolean canLogin = sellerSVC.canLogin(testSeller);

        // then
        assertFalse(canLogin);
    }

    @Test
    @DisplayName("?ˆí‡´ ?¬ë? ì²´í¬ - ?ˆí‡´??)
    void isWithdrawn_true() {
        // given
        testSeller.setStatus(MemberStatus.WITHDRAWN);
        testSeller.setWithdrawnAt(new Date());

        // when
        boolean isWithdrawn = sellerSVC.isWithdrawn(testSeller);

        // then
        assertTrue(isWithdrawn);
    }

    @Test
    @DisplayName("?ˆí‡´ ?¬ë? ì²´í¬ - ?ˆí‡´ ?ˆí•¨")
    void isWithdrawn_false() {
        // given
        testSeller.setStatus(MemberStatus.ACTIVE);
        testSeller.setWithdrawnAt(null);

        // when
        boolean isWithdrawn = sellerSVC.isWithdrawn(testSeller);

        // then
        assertFalse(isWithdrawn);
    }

    @Test
    @DisplayName("?Œì› ?±ê¸‰ ?•ë³´ ì¡°íšŒ - ?•ìƒ")
    void getGubunInfo_Success() {
        // given
        testSeller.setGubun(MemberGubun.GOLD);

        // when
        Map<String, String> gubunInfo = sellerSVC.getGubunInfo(testSeller);

        // then
        assertThat(gubunInfo).isNotNull();
        assertThat(gubunInfo.get("code")).isEqualTo("GOLD");
        assertThat(gubunInfo.get("name")).isEqualTo("ê³¨ë“œ");
    }

    @Test
    @DisplayName("?Œì› ?íƒœ ?•ë³´ ì¡°íšŒ - ?•ìƒ")
    void getStatusInfo_Success() {
        // given
        testSeller.setStatus(MemberStatus.ACTIVE);

        // when
        Map<String, String> statusInfo = sellerSVC.getStatusInfo(testSeller);

        // then
        assertThat(statusInfo).isNotNull();
        assertThat(statusInfo.get("code")).isEqualTo("?œì„±??);
        assertThat(statusInfo.get("name")).isEqualTo("?œì„±??);
    }

    @Test
    @DisplayName("?ì  ?•ë³´ ì¡°íšŒ - ?•ìƒ")
    void getShopInfo_Success() {
        // when
        Map<String, String> shopInfo = sellerSVC.getShopInfo(testSeller);

        // then
        assertThat(shopInfo).isNotNull();
        assertThat(shopInfo.get("code")).isEqualTo(testSeller.getSellerId().toString());
        assertThat(shopInfo.get("name")).isEqualTo(testSeller.getShopName());
    }

    @Test
    @DisplayName("?Œì› ?•ë³´ ì¡°íšŒ - null??ê²½ìš°")
    void getInfo_Null() {
        // when
        Map<String, String> gubunInfo = sellerSVC.getGubunInfo(null);
        Map<String, String> statusInfo = sellerSVC.getStatusInfo(null);
        Map<String, String> shopInfo = sellerSVC.getShopInfo(null);

        // then
        assertThat(gubunInfo.get("code")).isEqualTo("UNKNOWN");
        assertThat(statusInfo.get("code")).isEqualTo("UNKNOWN");
        assertThat(shopInfo.get("code")).isEqualTo("UNKNOWN");
    }

    @Test
    @DisplayName("?Œì› ?±ê¸‰ ?•ë³´ ì¡°íšŒ - ?±ê¸‰??null??ê²½ìš°")
    void getGubunInfo_NullGubun() {
        // given
        testSeller.setGubun(null);

        // when
        Map<String, String> gubunInfo = sellerSVC.getGubunInfo(testSeller);

        // then
        assertThat(gubunInfo.get("code")).isEqualTo("UNKNOWN");
        assertThat(gubunInfo.get("name")).isEqualTo("?????†ìŒ");
    }

    @Test
    @DisplayName("?ì  ?•ë³´ ì¡°íšŒ - ?ì ëª…ì´ null??ê²½ìš°")
    void getShopInfo_NullShopName() {
        // given
        testSeller.setShopName(null);

        // when
        Map<String, String> shopInfo = sellerSVC.getShopInfo(testSeller);

        // then
        assertThat(shopInfo.get("code")).isEqualTo(testSeller.getSellerId().toString());
        assertThat(shopInfo.get("name")).isNull();
    }

    // ==================== ?±ê¸‰ ?¹ê¸‰ ?ŒìŠ¤??====================

    @Test
    @DisplayName("?±ê¸‰ ?¹ê¸‰ - ?±ê³µ")
    void upgradeGubun_success() {
        // given
        when(sellerDAO.update(eq(1L), any(Seller.class))).thenReturn(1);

        // when & then
        assertDoesNotThrow(() -> {
            sellerSVC.upgradeGubun(1L, MemberGubun.BRONZE.getCode());
        });

        verify(sellerDAO).update(eq(1L), any(Seller.class));
    }

    // ==================== ?¬ì—…?ë“±ë¡ë²ˆ??ê²€ì¦??ŒìŠ¤??====================

    @Test
    @DisplayName("?¬ì—…?ë“±ë¡ë²ˆ??ê²€ì¦?- ? íš¨??)
    void validateBizRegNo_valid() {
        // given
        String validBizRegNo = "123-45-67890";

        // when
        boolean isValid = sellerSVC.validateBizRegNo(validBizRegNo);

        // then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("?¬ì—…?ë“±ë¡ë²ˆ??ê²€ì¦?- ë¬´íš¨??(null)")
    void validateBizRegNo_invalid_null() {
        // when
        boolean isValid = sellerSVC.validateBizRegNo(null);

        // then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("?¬ì—…?ë“±ë¡ë²ˆ??ê²€ì¦?- ë¬´íš¨??(?•ì‹ ?¤ë¥˜)")
    void validateBizRegNo_invalid_format() {
        // given
        String invalidBizRegNo = "12345";

        // when
        boolean isValid = sellerSVC.validateBizRegNo(invalidBizRegNo);

        // then
        assertFalse(isValid);
    }

    // ==================== ê´€ë¦?ê¸°ëŠ¥ ?ŒìŠ¤??====================

    @Test
    @DisplayName("?ˆí‡´ ?ë§¤??ëª©ë¡ ì¡°íšŒ")
    void getWithdrawnMembers() {
        // given
        Seller withdrawnSeller1 = createSampleSeller();
        withdrawnSeller1.setSellerId(2L);
        withdrawnSeller1.setStatus(MemberStatus.WITHDRAWN);
        withdrawnSeller1.setShopName("Withdrawn Shop 1");
        
        Seller withdrawnSeller2 = createSampleSeller();
        withdrawnSeller2.setSellerId(3L);
        withdrawnSeller2.setStatus(MemberStatus.WITHDRAWN);
        withdrawnSeller2.setShopName("Withdrawn Shop 2");

        List<Seller> withdrawnList = Arrays.asList(withdrawnSeller1, withdrawnSeller2);
        when(sellerDAO.findWithdrawnMembers()).thenReturn(withdrawnList);

        // when
        List<Seller> result = sellerSVC.getWithdrawnMembers();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(seller -> MemberStatus.WITHDRAWN.equals(seller.getStatus())));
        verify(sellerDAO).findWithdrawnMembers();
    }

    // ==================== Edge Case ?ŒìŠ¤??====================

    @Test
    @DisplayName("null ?ë§¤??ë¹„ì¦ˆ?ˆìŠ¤ ë¡œì§ ?ŒìŠ¤??)
    void businessLogic_with_null_seller() {
        // when & then
        assertFalse(sellerSVC.canLogin(null));
        assertFalse(sellerSVC.isWithdrawn(null));
        
        Map<String, String> gubunInfo = sellerSVC.getGubunInfo(null);
        assertThat(gubunInfo.get("code")).isEqualTo("UNKNOWN");
        assertThat(gubunInfo.get("name")).isEqualTo("?????†ìŒ");
        
        Map<String, String> statusInfo = sellerSVC.getStatusInfo(null);
        assertThat(statusInfo.get("code")).isEqualTo("UNKNOWN");
        assertThat(statusInfo.get("name")).isEqualTo("?????†ìŒ");

        Map<String, String> shopInfo = sellerSVC.getShopInfo(null);
        assertThat(shopInfo.get("code")).isEqualTo("UNKNOWN");
        assertThat(shopInfo.get("name")).isEqualTo("?????†ìŒ");
    }

    @Test
    @DisplayName("?˜ëª»???±ê¸‰ ì½”ë“œ ì²˜ë¦¬")
    void getGubunInfo_invalid_code() {
        // given
        testSeller.setGubun("INVALID_CODE");

        // when
        Map<String, String> gubunInfo = sellerSVC.getGubunInfo(testSeller);

        // then
        assertThat(gubunInfo.get("code")).isEqualTo(MemberGubun.NEW.getCode());
        assertThat(gubunInfo.get("name")).isEqualTo(MemberGubun.NEW.getDescription());
    }

    @Test
    @DisplayName("ë¹??ì ëª?ì²˜ë¦¬")
    void getShopInfo_empty_shop_name() {
        // given
        testSeller.setShopName("");
        testSeller.setBizRegNo("");

        // when
        Map<String, String> shopInfo = sellerSVC.getShopInfo(testSeller);

        // then
        assertThat(shopInfo.get("code")).isEqualTo(testSeller.getSellerId().toString());
        assertThat(shopInfo.get("name")).isEqualTo("");
    }
}
