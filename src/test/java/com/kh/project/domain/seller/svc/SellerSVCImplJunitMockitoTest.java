package com.kh.project.domain.seller.svc;

import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.seller.dao.SellerDAO;
import com.kh.project.web.common.form.MemberStatusInfo;
import com.kh.project.web.exception.BusinessValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ?ë§¤???œë¹„??JUnit + Mockito ?ŒìŠ¤?? */
@ExtendWith(MockitoExtension.class)
@DisplayName("?ë§¤???œë¹„???ŒìŠ¤??)
class SellerSVCImplJunitMockitoTest {

    @Mock
    private SellerDAO sellerDAO;

    @InjectMocks
    private SellerSVCImpl sellerSVC;

    private Seller testSeller;
    private Seller withdrawnSeller;

    @BeforeEach
    void setUp() {
        testSeller = createTestSeller();
        withdrawnSeller = createWithdrawnSeller();
    }

    @Nested
    @DisplayName("?Œì›ê°€???ŒìŠ¤??)
    class JoinTest {

        @Test
        @DisplayName("? ê·œ ?Œì›ê°€???±ê³µ")
        void join_newMember_success() {
            // given
            when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.empty());
            when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(false);
            when(sellerDAO.existsByShopName(testSeller.getShopName())).thenReturn(false);
            when(sellerDAO.existsByName(testSeller.getName())).thenReturn(false);
            when(sellerDAO.existsByShopAddress(testSeller.getShopAddress())).thenReturn(false);
            when(sellerDAO.save(any(Seller.class))).thenReturn(testSeller);

            // when
            Seller result = sellerSVC.join(testSeller);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(testSeller.getEmail());
            assertThat(result.getShopName()).isEqualTo(testSeller.getShopName());
            assertThat(result.getBizRegNo()).isEqualTo(testSeller.getBizRegNo());
            
            verify(sellerDAO).findByEmail(testSeller.getEmail());
            verify(sellerDAO).existsByBizRegNo(testSeller.getBizRegNo());
            verify(sellerDAO).existsByShopName(testSeller.getShopName());
            verify(sellerDAO).existsByName(testSeller.getName());
            verify(sellerDAO).existsByShopAddress(testSeller.getShopAddress());
            verify(sellerDAO).save(any(Seller.class));
        }

        @Test
        @DisplayName("?ˆí‡´ ?Œì› ?¬í™œ?±í™” ?±ê³µ")
        void join_withdrawnMember_reactivate_success() {
            // given
            when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.of(withdrawnSeller));
            when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(false);
            when(sellerDAO.existsByShopName(testSeller.getShopName())).thenReturn(false);
            when(sellerDAO.existsByName(testSeller.getName())).thenReturn(false);
            when(sellerDAO.existsByShopAddress(testSeller.getShopAddress())).thenReturn(false);
            when(sellerDAO.rejoin(any(Seller.class))).thenReturn(1);
            when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.of(testSeller));

            // when
            Seller result = sellerSVC.join(testSeller);

            // then
            assertThat(result).isNotNull();
            verify(sellerDAO).rejoin(any(Seller.class));
        }

        @Test
        @DisplayName("?Œì›ê°€???¤íŒ¨ - ?œì„± ?´ë©”??ì¤‘ë³µ")
        void join_fail_activeEmail_duplicate() {
            // given
            when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.of(testSeller));

            // when & then
            assertThatThrownBy(() -> sellerSVC.join(testSeller))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("?´ë? ?¬ìš©ì¤‘ì¸ ?´ë©”?¼ì…?ˆë‹¤");

            verify(sellerDAO).findByEmail(testSeller.getEmail());
            verify(sellerDAO, never()).save(any(Seller.class));
        }

        @Test
        @DisplayName("?Œì›ê°€???¤íŒ¨ - ?¬ì—…?ë“±ë¡ë²ˆ??ì¤‘ë³µ")
        void join_fail_bizRegNo_duplicate() {
            // given
            when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.empty());
            when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> sellerSVC.join(testSeller))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("?´ë? ?±ë¡???¬ì—…?ë“±ë¡ë²ˆ?¸ì…?ˆë‹¤");

            verify(sellerDAO).findByEmail(testSeller.getEmail());
            verify(sellerDAO).existsByBizRegNo(testSeller.getBizRegNo());
            verify(sellerDAO, never()).save(any(Seller.class));
        }

        @Test
        @DisplayName("?Œì›ê°€???¤íŒ¨ - ?í˜¸ëª?ì¤‘ë³µ")
        void join_fail_shopName_duplicate() {
            // given
            when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.empty());
            when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(false);
            when(sellerDAO.existsByShopName(testSeller.getShopName())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> sellerSVC.join(testSeller))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("?´ë? ?¬ìš©ì¤‘ì¸ ?í˜¸ëª…ì…?ˆë‹¤");

            verify(sellerDAO).findByEmail(testSeller.getEmail());
            verify(sellerDAO).existsByBizRegNo(testSeller.getBizRegNo());
            verify(sellerDAO).existsByShopName(testSeller.getShopName());
            verify(sellerDAO, never()).save(any(Seller.class));
        }

        @Test
        @DisplayName("?Œì›ê°€???¤íŒ¨ - ?€?œìëª?ì¤‘ë³µ")
        void join_fail_name_duplicate() {
            // given
            when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.empty());
            when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(false);
            when(sellerDAO.existsByShopName(testSeller.getShopName())).thenReturn(false);
            when(sellerDAO.existsByName(testSeller.getName())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> sellerSVC.join(testSeller))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("?´ë? ?±ë¡???€?œìëª…ì…?ˆë‹¤");

            verify(sellerDAO).findByEmail(testSeller.getEmail());
            verify(sellerDAO).existsByBizRegNo(testSeller.getBizRegNo());
            verify(sellerDAO).existsByShopName(testSeller.getShopName());
            verify(sellerDAO).existsByName(testSeller.getName());
            verify(sellerDAO, never()).save(any(Seller.class));
        }

        @Test
        @DisplayName("?Œì›ê°€???¤íŒ¨ - ?¬ì—…??ì£¼ì†Œ ì¤‘ë³µ")
        void join_fail_shopAddress_duplicate() {
            // given
            when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.empty());
            when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(false);
            when(sellerDAO.existsByShopName(testSeller.getShopName())).thenReturn(false);
            when(sellerDAO.existsByName(testSeller.getName())).thenReturn(false);
            when(sellerDAO.existsByShopAddress(testSeller.getShopAddress())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> sellerSVC.join(testSeller))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("?´ë? ?±ë¡???¬ì—…??ì£¼ì†Œ?…ë‹ˆ??);

            verify(sellerDAO).findByEmail(testSeller.getEmail());
            verify(sellerDAO).existsByBizRegNo(testSeller.getBizRegNo());
            verify(sellerDAO).existsByShopName(testSeller.getShopName());
            verify(sellerDAO).existsByName(testSeller.getName());
            verify(sellerDAO).existsByShopAddress(testSeller.getShopAddress());
            verify(sellerDAO, never()).save(any(Seller.class));
        }

        @Test
        @DisplayName("?Œì›ê°€???¤íŒ¨ - ?˜ëª»???¬ì—…?ë“±ë¡ë²ˆ???•ì‹")
        void join_fail_invalid_bizRegNo_format() {
            // given
            testSeller.setBizRegNo("invalid-format");

            // when & then
            assertThatThrownBy(() -> sellerSVC.join(testSeller))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("?¬ë°”ë¥´ì? ?Šì? ?¬ì—…?ë“±ë¡ë²ˆ???•ì‹?…ë‹ˆ??);

            verify(sellerDAO, never()).save(any(Seller.class));
        }
    }

    @Nested
    @DisplayName("ë¡œê·¸???ŒìŠ¤??)
    class LoginTest {

        @Test
        @DisplayName("ë¡œê·¸???±ê³µ")
        void login_success() {
            // given
            String email = "test@shop.com";
            String password = "password123";
            testSeller.setPassword(password);
            
            when(sellerDAO.findByEmail(email)).thenReturn(Optional.of(testSeller));

            // when
            Seller result = sellerSVC.login(email, password);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.canLogin()).isTrue();
            
            verify(sellerDAO).findByEmail(email);
        }

        @Test
        @DisplayName("ë¡œê·¸???¤íŒ¨ - ì¡´ì¬?˜ì? ?ŠëŠ” ?´ë©”??)
        void login_fail_email_not_found() {
            // given
            String email = "notfound@shop.com";
            when(sellerDAO.findByEmail(email)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sellerSVC.login(email, "password"))
                .isInstanceOf(BusinessValidationException.class);

            verify(sellerDAO).findByEmail(email);
        }

        @Test
        @DisplayName("ë¡œê·¸???¤íŒ¨ - ë¹„ë?ë²ˆí˜¸ ë¶ˆì¼ì¹?)
        void login_fail_wrong_password() {
            // given
            String email = "test@shop.com";
            testSeller.setPassword("correctPassword");
            
            when(sellerDAO.findByEmail(email)).thenReturn(Optional.of(testSeller));

            // when & then
            assertThatThrownBy(() -> sellerSVC.login(email, "wrongPassword"))
                .isInstanceOf(BusinessValidationException.class);

            verify(sellerDAO).findByEmail(email);
        }

        @Test
        @DisplayName("ë¡œê·¸???¤íŒ¨ - ?ˆí‡´???Œì›")
        void login_fail_withdrawn_member() {
            // given
            String email = "withdrawn@shop.com";
            
            when(sellerDAO.findByEmail(email)).thenReturn(Optional.of(withdrawnSeller));

            // when & then
            assertThatThrownBy(() -> sellerSVC.login(email, "password"))
                .isInstanceOf(BusinessValidationException.class);

            verify(sellerDAO).findByEmail(email);
        }

        @Test
        @DisplayName("ë¡œê·¸???¤íŒ¨ - ë¹„í™œ?±í™”???Œì›")
        void login_fail_inactive_member() {
            // given
            String email = "inactive@shop.com";
            testSeller.setStatus("ë¹„í™œ?±í™”");
            
            when(sellerDAO.findByEmail(email)).thenReturn(Optional.of(testSeller));

            // when & then
            assertThatThrownBy(() -> sellerSVC.login(email, "password123"))
                .isInstanceOf(BusinessValidationException.class);

            verify(sellerDAO).findByEmail(email);
        }
    }

    @Nested
    @DisplayName("?•ë³´ ?˜ì • ?ŒìŠ¤??)
    class UpdateTest {

        @Test
        @DisplayName("?•ë³´ ?˜ì • ?±ê³µ")
        void update_success() {
            // given
            Long sellerId = 1L;
            Seller updateSeller = new Seller();
            updateSeller.setShopName("ë³€ê²½ëœ?ì ");
            updateSeller.setTel("02-9999-8888");
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));
            when(sellerDAO.existsByShopName("ë³€ê²½ëœ?ì ")).thenReturn(false);
            when(sellerDAO.update(sellerId, updateSeller)).thenReturn(1);

            // when
            int result = sellerSVC.update(sellerId, updateSeller);

            // then
            assertThat(result).isEqualTo(1);
            
            verify(sellerDAO).findById(sellerId);
            verify(sellerDAO).existsByShopName("ë³€ê²½ëœ?ì ");
            verify(sellerDAO).update(sellerId, updateSeller);
        }

        @Test
        @DisplayName("?•ë³´ ?˜ì • ?¤íŒ¨ - ?í˜¸ëª?ì¤‘ë³µ")
        void update_fail_shopName_duplicate() {
            // given
            Long sellerId = 1L;
            Seller updateSeller = new Seller();
            updateSeller.setShopName("ì¤‘ë³µ?í˜¸");
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));
            when(sellerDAO.existsByShopName("ì¤‘ë³µ?í˜¸")).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> sellerSVC.update(sellerId, updateSeller))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("?´ë? ?¬ìš©ì¤‘ì¸ ?í˜¸ëª…ì…?ˆë‹¤");

            verify(sellerDAO).findById(sellerId);
            verify(sellerDAO).existsByShopName("ì¤‘ë³µ?í˜¸");
            verify(sellerDAO, never()).update(anyLong(), any(Seller.class));
        }

        @Test
        @DisplayName("?•ë³´ ?˜ì • ?±ê³µ - ê°™ì? ?í˜¸ëª?? ì?")
        void update_success_same_shopName() {
            // given
            Long sellerId = 1L;
            Seller updateSeller = new Seller();
            updateSeller.setShopName("ê¸°ì¡´?í˜¸");
            testSeller.setShopName("ê¸°ì¡´?í˜¸");
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));
            when(sellerDAO.update(sellerId, updateSeller)).thenReturn(1);

            // when
            int result = sellerSVC.update(sellerId, updateSeller);

            // then
            assertThat(result).isEqualTo(1);
            
            verify(sellerDAO).findById(sellerId);
            verify(sellerDAO, never()).existsByShopName(anyString());
            verify(sellerDAO).update(sellerId, updateSeller);
        }
    }

    @Nested
    @DisplayName("?ˆí‡´ ?ŒìŠ¤??)
    class WithdrawTest {

        @Test
        @DisplayName("?ˆí‡´ ?±ê³µ")
        void withdraw_success() {
            // given
            Long sellerId = 1L;
            String reason = "?¬ì—… ì¢…ë£Œ";
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));
            when(sellerDAO.withdrawWithReason(sellerId, reason)).thenReturn(1);

            // when
            int result = sellerSVC.withdraw(sellerId, reason);

            // then
            assertThat(result).isEqualTo(1);
            
            verify(sellerDAO).findById(sellerId);
            verify(sellerDAO).withdrawWithReason(sellerId, reason);
        }

        @Test
        @DisplayName("?ˆí‡´ ?¤íŒ¨ - ì¡´ì¬?˜ì? ?ŠëŠ” ?Œì›")
        void withdraw_fail_member_not_found() {
            // given
            Long sellerId = 999L;
            String reason = "?¬ì—… ì¢…ë£Œ";
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sellerSVC.withdraw(sellerId, reason))
                .isInstanceOf(BusinessValidationException.class);

            verify(sellerDAO).findById(sellerId);
            verify(sellerDAO, never()).withdrawWithReason(anyLong(), anyString());
        }

        @Test
        @DisplayName("?ˆí‡´ ?¤íŒ¨ - ?´ë? ?ˆí‡´???Œì›")
        void withdraw_fail_already_withdrawn() {
            // given
            Long sellerId = 1L;
            String reason = "?¬ì—… ì¢…ë£Œ";
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(withdrawnSeller));

            // when & then
            assertThatThrownBy(() -> sellerSVC.withdraw(sellerId, reason))
                .isInstanceOf(BusinessValidationException.class);

            verify(sellerDAO).findById(sellerId);
            verify(sellerDAO, never()).withdrawWithReason(anyLong(), anyString());
        }

        @Test
        @DisplayName("?ˆí‡´ ?¤íŒ¨ - ?ˆí‡´ ì²˜ë¦¬ ?¤íŒ¨")
        void withdraw_fail_process_failed() {
            // given
            Long sellerId = 1L;
            String reason = "?¬ì—… ì¢…ë£Œ";
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));
            when(sellerDAO.withdrawWithReason(sellerId, reason)).thenReturn(0);

            // when & then
            assertThatThrownBy(() -> sellerSVC.withdraw(sellerId, reason))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("?ë§¤???ˆí‡´ ì²˜ë¦¬???¤íŒ¨?ˆìŠµ?ˆë‹¤");

            verify(sellerDAO).findById(sellerId);
            verify(sellerDAO).withdrawWithReason(sellerId, reason);
        }
    }

    @Nested
    @DisplayName("ë¹„ë?ë²ˆí˜¸ ?•ì¸ ?ŒìŠ¤??)
    class CheckPasswordTest {

        @Test
        @DisplayName("ë¹„ë?ë²ˆí˜¸ ?•ì¸ ?±ê³µ")
        void checkPassword_success() {
            // given
            Long sellerId = 1L;
            String password = "password123";
            testSeller.setPassword(password);
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));

            // when
            boolean result = sellerSVC.checkPassword(sellerId, password);

            // then
            assertThat(result).isTrue();
            
            verify(sellerDAO).findById(sellerId);
        }

        @Test
        @DisplayName("ë¹„ë?ë²ˆí˜¸ ?•ì¸ ?¤íŒ¨ - ë¶ˆì¼ì¹?)
        void checkPassword_fail_wrong_password() {
            // given
            Long sellerId = 1L;
            String correctPassword = "correctPassword";
            String inputPassword = "wrongPassword";
            testSeller.setPassword(correctPassword);
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));

            // when
            boolean result = sellerSVC.checkPassword(sellerId, inputPassword);

            // then
            assertThat(result).isFalse();
            
            verify(sellerDAO).findById(sellerId);
        }

        @Test
        @DisplayName("ë¹„ë?ë²ˆí˜¸ ?•ì¸ ?¤íŒ¨ - null ë¹„ë?ë²ˆí˜¸")
        void checkPassword_fail_null_password() {
            // given
            Long sellerId = 1L;

            // when
            boolean result = sellerSVC.checkPassword(sellerId, null);

            // then
            assertThat(result).isFalse();
            
            verify(sellerDAO, never()).findById(anyLong());
        }

        @Test
        @DisplayName("ë¹„ë?ë²ˆí˜¸ ?•ì¸ ?¤íŒ¨ - ì¡´ì¬?˜ì? ?ŠëŠ” ?Œì›")
        void checkPassword_fail_member_not_found() {
            // given
            Long sellerId = 999L;
            String password = "password123";
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.empty());

            // when
            boolean result = sellerSVC.checkPassword(sellerId, password);

            // then
            assertThat(result).isFalse();
            
            verify(sellerDAO).findById(sellerId);
        }
    }

    @Nested
    @DisplayName("?œë¹„???´ìš©?„í™© ?ŒìŠ¤??)
    class ServiceUsageTest {

        @Test
        @DisplayName("?œë¹„???´ìš©?„í™© ì¡°íšŒ ?±ê³µ")
        void getServiceUsage_success() {
            // given
            Long sellerId = 1L;
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));

            // when
            MemberStatusInfo result = sellerSVC.getServiceUsage(sellerId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isCanWithdraw()).isTrue();
            assertThat(result.getTotalProducts()).isEqualTo(0);
            assertThat(result.getActiveProducts()).isEqualTo(0);
            assertThat(result.getMonthlyRevenue()).isEqualTo(0);
            assertThat(result.getActiveOrders()).isEqualTo(0);
            assertThat(result.getPreparingOrders()).isEqualTo(0);
            assertThat(result.getShippingOrders()).isEqualTo(0);
            assertThat(result.getPendingAmount()).isEqualTo(0);
            
            verify(sellerDAO).findById(sellerId);
        }

        @Test
        @DisplayName("?œë¹„???´ìš©?„í™© ì¡°íšŒ ?¤íŒ¨ - ?Œì› ?†ìŒ")
        void getServiceUsage_fail_member_not_found() {
            // given
            Long sellerId = 999L;
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sellerSVC.getServiceUsage(sellerId))
                .isInstanceOf(BusinessValidationException.class);

            verify(sellerDAO).findById(sellerId);
        }
    }

    @Nested
    @DisplayName("ì¤‘ë³µ ì²´í¬ ?ŒìŠ¤??)
    class DuplicateCheckTest {

        @Test
        @DisplayName("?´ë©”??ì¤‘ë³µ ì²´í¬ - ì¡´ì¬??)
        void existsByEmail_true() {
            // given
            String email = "test@shop.com";
            when(sellerDAO.existsByEmail(email)).thenReturn(true);

            // when
            boolean result = sellerSVC.existsByEmail(email);

            // then
            assertThat(result).isTrue();
            verify(sellerDAO).existsByEmail(email);
        }

        @Test
        @DisplayName("?¬ì—…?ë“±ë¡ë²ˆ??ì¤‘ë³µ ì²´í¬ - ì¡´ì¬??)
        void existsByBizRegNo_true() {
            // given
            String bizRegNo = "123-45-67890";
            when(sellerDAO.existsByBizRegNo(bizRegNo)).thenReturn(true);

            // when
            boolean result = sellerSVC.existsByBizRegNo(bizRegNo);

            // then
            assertThat(result).isTrue();
            verify(sellerDAO).existsByBizRegNo(bizRegNo);
        }

        @Test
        @DisplayName("?í˜¸ëª?ì¤‘ë³µ ì²´í¬ - ì¡´ì¬??)
        void existsByShopName_true() {
            // given
            String shopName = "?ŒìŠ¤?¸ìƒ??;
            when(sellerDAO.existsByShopName(shopName)).thenReturn(true);

            // when
            boolean result = sellerSVC.existsByShopName(shopName);

            // then
            assertThat(result).isTrue();
            verify(sellerDAO).existsByShopName(shopName);
        }

        @Test
        @DisplayName("?€?œìëª?ì¤‘ë³µ ì²´í¬ - ì¡´ì¬??)
        void existsByName_true() {
            // given
            String name = "ê¹€?€??;
            when(sellerDAO.existsByName(name)).thenReturn(true);

            // when
            boolean result = sellerSVC.existsByName(name);

            // then
            assertThat(result).isTrue();
            verify(sellerDAO).existsByName(name);
        }

        @Test
        @DisplayName("?¬ì—…??ì£¼ì†Œ ì¤‘ë³µ ì²´í¬ - ì¡´ì¬??)
        void existsByShopAddress_true() {
            // given
            String shopAddress = "?œìš¸??ê°•ë‚¨êµ??Œí—¤?€ë¡?123";
            when(sellerDAO.existsByShopAddress(shopAddress)).thenReturn(true);

            // when
            boolean result = sellerSVC.existsByShopAddress(shopAddress);

            // then
            assertThat(result).isTrue();
            verify(sellerDAO).existsByShopAddress(shopAddress);
        }
    }

    @Nested
    @DisplayName("? í‹¸ë¦¬í‹° ë©”ì„œ???ŒìŠ¤??)
    class UtilityMethodTest {

        @Test
        @DisplayName("?¬ì—…?ë“±ë¡ë²ˆ??? íš¨??ê²€ì¦?- ? íš¨??)
        void validateBizRegNo_valid() {
            // given
            String validBizRegNo = "123-45-67890";

            // when
            boolean result = sellerSVC.validateBizRegNo(validBizRegNo);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("?¬ì—…?ë“±ë¡ë²ˆ??? íš¨??ê²€ì¦?- ë¬´íš¨??)
        void validateBizRegNo_invalid() {
            // given
            String invalidBizRegNo = "invalid-format";

            // when
            boolean result = sellerSVC.validateBizRegNo(invalidBizRegNo);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("?¬ì—…?ë“±ë¡ë²ˆ??? íš¨??ê²€ì¦?- null")
        void validateBizRegNo_null() {
            // when
            boolean result = sellerSVC.validateBizRegNo(null);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("ë¡œê·¸??ê°€???¬ë? ?•ì¸ - ê°€??)
        void canLogin_true() {
            // given
            testSeller.setStatus("?œì„±??);
            testSeller.setWithdrawnAt(null);

            // when
            boolean result = sellerSVC.canLogin(testSeller);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("ë¡œê·¸??ê°€???¬ë? ?•ì¸ - ë¶ˆê???(?ˆí‡´)")
        void canLogin_false_withdrawn() {
            // given
            testSeller.setStatus("?ˆí‡´");
            testSeller.setWithdrawnAt(new Date());

            // when
            boolean result = sellerSVC.canLogin(testSeller);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("?ˆí‡´ ?¬ë? ?•ì¸ - ?ˆí‡´??)
        void isWithdrawn_true() {
            // when
            boolean result = sellerSVC.isWithdrawn(withdrawnSeller);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("?ˆí‡´ ?¬ë? ?•ì¸ - ?ˆí‡´?˜ì? ?ŠìŒ")
        void isWithdrawn_false() {
            // when
            boolean result = sellerSVC.isWithdrawn(testSeller);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("?ˆí‡´ ê°€???¬ë? ?•ì¸")
        void canWithdraw_true() {
            // given
            Long sellerId = 1L;
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));

            // when
            boolean result = sellerSVC.canWithdraw(sellerId);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("ì¡°íšŒ ?ŒìŠ¤??)
    class FindTest {

        @Test
        @DisplayName("IDë¡?ì¡°íšŒ ?±ê³µ")
        void findById_success() {
            // given
            Long sellerId = 1L;
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));

            // when
            Optional<Seller> result = sellerSVC.findById(sellerId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getSellerId()).isEqualTo(sellerId);
            verify(sellerDAO).findById(sellerId);
        }

        @Test
        @DisplayName("IDë¡?ì¡°íšŒ ?¤íŒ¨ - ì¡´ì¬?˜ì? ?ŠìŒ")
        void findById_not_found() {
            // given
            Long sellerId = 999L;
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.empty());

            // when
            Optional<Seller> result = sellerSVC.findById(sellerId);

            // then
            assertThat(result).isEmpty();
            verify(sellerDAO).findById(sellerId);
        }

        @Test
        @DisplayName("?ˆí‡´ ?Œì› ëª©ë¡ ì¡°íšŒ")
        void getWithdrawnMembers_success() {
            // given
            List<Seller> withdrawnMembers = Arrays.asList(withdrawnSeller);
            when(sellerDAO.findWithdrawnMembers()).thenReturn(withdrawnMembers);

            // when
            List<Seller> result = sellerSVC.getWithdrawnMembers();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isWithdrawn()).isTrue();
            verify(sellerDAO).findWithdrawnMembers();
        }
    }

    // ?ŒìŠ¤???°ì´???ì„± ë©”ì„œ??    private Seller createTestSeller() {
        Seller seller = new Seller();
        seller.setSellerId(1L);
        seller.setEmail("test@shop.com");
        seller.setPassword("password123");
        seller.setName("ê¹€?€??);
        seller.setShopName("?ŒìŠ¤?¸ìƒ??);
        seller.setBizRegNo("123-45-67890");
        seller.setShopAddress("?œìš¸??ê°•ë‚¨êµ??Œí—¤?€ë¡?123");
        seller.setTel("02-1234-5678");
        seller.setMemberGubun("NEW");
        seller.setStatus("?œì„±??);
        seller.setCdate(new Date());
        seller.setUdate(new Date());
        return seller;
    }

    private Seller createWithdrawnSeller() {
        Seller seller = new Seller();
        seller.setSellerId(2L);
        seller.setEmail("withdrawn@shop.com");
        seller.setPassword("password123");
        seller.setName("ë°•ë???);
        seller.setShopName("?ˆí‡´?ì ");
        seller.setBizRegNo("987-65-43210");
        seller.setShopAddress("?œìš¸???œì´ˆêµ??œì´ˆ?€ë¡?456");
        seller.setTel("02-9876-5432");
        seller.setMemberGubun("BRONZE");
        seller.setStatus("?ˆí‡´");
        seller.setCdate(new Date());
        seller.setUdate(new Date());
        seller.setWithdrawnAt(new Date());
        seller.setWithdrawnReason("?¬ì—… ì¢…ë£Œ");
        return seller;
    }
} 
