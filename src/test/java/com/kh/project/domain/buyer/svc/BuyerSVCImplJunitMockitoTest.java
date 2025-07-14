package com.kh.project.domain.buyer.svc;

import com.kh.project.domain.buyer.dao.BuyerDAO;
import com.kh.project.domain.entity.Buyer;
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
import java.util.Optional;
import java.util.List;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * êµ¬ë§¤???œë¹„??JUnit + Mockito ?ŒìŠ¤?? */
@ExtendWith(MockitoExtension.class)
@DisplayName("êµ¬ë§¤???œë¹„???ŒìŠ¤??)
class BuyerSVCImplJunitMockitoTest {

    @Mock
    private BuyerDAO buyerDAO;

    @InjectMocks
    private BuyerSVCImpl buyerSVC;

    private Buyer testBuyer;
    private Buyer withdrawnBuyer;

    @BeforeEach
    void setUp() {
        testBuyer = createTestBuyer();
        withdrawnBuyer = createWithdrawnBuyer();
    }

    @Nested
    @DisplayName("?Œì›ê°€???ŒìŠ¤??)
    class JoinTest {

        @Test
        @DisplayName("? ê·œ ?Œì›ê°€???±ê³µ")
        void join_newMember_success() {
            // given
            when(buyerDAO.findByEmail(testBuyer.getEmail())).thenReturn(Optional.empty());
            when(buyerDAO.existsByNickname(testBuyer.getNickname())).thenReturn(false);
            when(buyerDAO.save(any(Buyer.class))).thenReturn(testBuyer);

            // when
            Buyer result = buyerSVC.join(testBuyer);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(testBuyer.getEmail());
            assertThat(result.getNickname()).isEqualTo(testBuyer.getNickname());
            
            verify(buyerDAO).findByEmail(testBuyer.getEmail());
            verify(buyerDAO).existsByNickname(testBuyer.getNickname());
            verify(buyerDAO).save(any(Buyer.class));
        }

        @Test
        @DisplayName("?ˆí‡´ ?Œì› ?¬í™œ?±í™” ?±ê³µ")
        void join_withdrawnMember_reactivate_success() {
            // given
            when(buyerDAO.findByEmail(testBuyer.getEmail())).thenReturn(Optional.of(withdrawnBuyer));
            when(buyerDAO.existsByNickname(testBuyer.getNickname())).thenReturn(false);
            when(buyerDAO.rejoin(any(Buyer.class))).thenReturn(1);
            when(buyerDAO.findByEmail(testBuyer.getEmail())).thenReturn(Optional.of(testBuyer));

            // when
            Buyer result = buyerSVC.join(testBuyer);

            // then
            assertThat(result).isNotNull();
            verify(buyerDAO).rejoin(any(Buyer.class));
        }

        @Test
        @DisplayName("?Œì›ê°€???¤íŒ¨ - ?œì„± ?´ë©”??ì¤‘ë³µ")
        void join_fail_activeEmail_duplicate() {
            // given
            when(buyerDAO.findByEmail(testBuyer.getEmail())).thenReturn(Optional.of(testBuyer));

            // when & then
            assertThatThrownBy(() -> buyerSVC.join(testBuyer))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("?´ë? ?¬ìš©ì¤‘ì¸ ?´ë©”?¼ì…?ˆë‹¤");

            verify(buyerDAO).findByEmail(testBuyer.getEmail());
            verify(buyerDAO, never()).save(any(Buyer.class));
        }

        @Test
        @DisplayName("?Œì›ê°€???¤íŒ¨ - ?‰ë„¤??ì¤‘ë³µ")
        void join_fail_nickname_duplicate() {
            // given
            when(buyerDAO.findByEmail(testBuyer.getEmail())).thenReturn(Optional.empty());
            when(buyerDAO.existsByNickname(testBuyer.getNickname())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> buyerSVC.join(testBuyer))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("?´ë? ?¬ìš©ì¤‘ì¸ ?‰ë„¤?„ì…?ˆë‹¤");

            verify(buyerDAO).findByEmail(testBuyer.getEmail());
            verify(buyerDAO).existsByNickname(testBuyer.getNickname());
            verify(buyerDAO, never()).save(any(Buyer.class));
        }

        @Test
        @DisplayName("?¬í™œ?±í™” ?¤íŒ¨ - ?¬í™œ?±í™” ì²˜ë¦¬ ?¤íŒ¨")
        void join_fail_reactivate_failed() {
            // given
            when(buyerDAO.findByEmail(testBuyer.getEmail())).thenReturn(Optional.of(withdrawnBuyer));
            when(buyerDAO.existsByNickname(testBuyer.getNickname())).thenReturn(false);
            when(buyerDAO.rejoin(any(Buyer.class))).thenReturn(0);

            // when & then
            assertThatThrownBy(() -> buyerSVC.join(testBuyer))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("ê³„ì • ?¬í™œ?±í™”???¤íŒ¨?ˆìŠµ?ˆë‹¤");

            verify(buyerDAO).rejoin(any(Buyer.class));
        }
    }

    @Nested
    @DisplayName("ë¡œê·¸???ŒìŠ¤??)
    class LoginTest {

        @Test
        @DisplayName("ë¡œê·¸???±ê³µ")
        void login_success() {
            // given
            String email = "test@buyer.com";
            String password = "password123";
            testBuyer.setPassword(password);
            
            when(buyerDAO.findByEmail(email)).thenReturn(Optional.of(testBuyer));

            // when
            Buyer result = buyerSVC.login(email, password);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.canLogin()).isTrue();
            
            verify(buyerDAO).findByEmail(email);
        }

        @Test
        @DisplayName("ë¡œê·¸???¤íŒ¨ - ì¡´ì¬?˜ì? ?ŠëŠ” ?´ë©”??)
        void login_fail_email_not_found() {
            // given
            String email = "notfound@test.com";
            when(buyerDAO.findByEmail(email)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> buyerSVC.login(email, "password"))
                .isInstanceOf(BusinessValidationException.class);

            verify(buyerDAO).findByEmail(email);
        }

        @Test
        @DisplayName("ë¡œê·¸???¤íŒ¨ - ë¹„ë?ë²ˆí˜¸ ë¶ˆì¼ì¹?)
        void login_fail_wrong_password() {
            // given
            String email = "test@buyer.com";
            testBuyer.setPassword("correctPassword");
            
            when(buyerDAO.findByEmail(email)).thenReturn(Optional.of(testBuyer));

            // when & then
            assertThatThrownBy(() -> buyerSVC.login(email, "wrongPassword"))
                .isInstanceOf(BusinessValidationException.class);

            verify(buyerDAO).findByEmail(email);
        }

        @Test
        @DisplayName("ë¡œê·¸???¤íŒ¨ - ?ˆí‡´???Œì›")
        void login_fail_withdrawn_member() {
            // given
            String email = "withdrawn@test.com";
            
            when(buyerDAO.findByEmail(email)).thenReturn(Optional.of(withdrawnBuyer));

            // when & then
            assertThatThrownBy(() -> buyerSVC.login(email, "password"))
                .isInstanceOf(BusinessValidationException.class);

            verify(buyerDAO).findByEmail(email);
        }

        @Test
        @DisplayName("ë¡œê·¸???¤íŒ¨ - ë¹„í™œ?±í™”???Œì›")
        void login_fail_inactive_member() {
            // given
            String email = "inactive@test.com";
            testBuyer.setStatus("ë¹„í™œ?±í™”");
            
            when(buyerDAO.findByEmail(email)).thenReturn(Optional.of(testBuyer));

            // when & then
            assertThatThrownBy(() -> buyerSVC.login(email, "password123"))
                .isInstanceOf(BusinessValidationException.class);

            verify(buyerDAO).findByEmail(email);
        }
    }

    @Nested
    @DisplayName("?•ë³´ ?˜ì • ?ŒìŠ¤??)
    class UpdateTest {

        @Test
        @DisplayName("?•ë³´ ?˜ì • ?±ê³µ")
        void update_success() {
            // given
            Long buyerId = 1L;
            Buyer updateBuyer = new Buyer();
            updateBuyer.setName("ë³€ê²½ëœ?´ë¦„");
            updateBuyer.setNickname("ë³€ê²½ëœ?‰ë„¤??);
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));
            when(buyerDAO.existsByNickname("ë³€ê²½ëœ?‰ë„¤??)).thenReturn(false);
            when(buyerDAO.update(buyerId, updateBuyer)).thenReturn(1);

            // when
            int result = buyerSVC.update(buyerId, updateBuyer);

            // then
            assertThat(result).isEqualTo(1);
            
            verify(buyerDAO).findById(buyerId);
            verify(buyerDAO).existsByNickname("ë³€ê²½ëœ?‰ë„¤??);
            verify(buyerDAO).update(buyerId, updateBuyer);
        }

        @Test
        @DisplayName("?•ë³´ ?˜ì • ?¤íŒ¨ - ?‰ë„¤??ì¤‘ë³µ")
        void update_fail_nickname_duplicate() {
            // given
            Long buyerId = 1L;
            Buyer updateBuyer = new Buyer();
            updateBuyer.setNickname("ì¤‘ë³µ?‰ë„¤??);
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));
            when(buyerDAO.existsByNickname("ì¤‘ë³µ?‰ë„¤??)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> buyerSVC.update(buyerId, updateBuyer))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("?´ë? ?¬ìš©ì¤‘ì¸ ?‰ë„¤?„ì…?ˆë‹¤");

            verify(buyerDAO).findById(buyerId);
            verify(buyerDAO).existsByNickname("ì¤‘ë³µ?‰ë„¤??);
            verify(buyerDAO, never()).update(anyLong(), any(Buyer.class));
        }

        @Test
        @DisplayName("?•ë³´ ?˜ì • ?±ê³µ - ê°™ì? ?‰ë„¤??? ì?")
        void update_success_same_nickname() {
            // given
            Long buyerId = 1L;
            Buyer updateBuyer = new Buyer();
            updateBuyer.setNickname("ê¸°ì¡´?‰ë„¤??);
            testBuyer.setNickname("ê¸°ì¡´?‰ë„¤??);
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));
            when(buyerDAO.update(buyerId, updateBuyer)).thenReturn(1);

            // when
            int result = buyerSVC.update(buyerId, updateBuyer);

            // then
            assertThat(result).isEqualTo(1);
            
            verify(buyerDAO).findById(buyerId);
            verify(buyerDAO, never()).existsByNickname(anyString());
            verify(buyerDAO).update(buyerId, updateBuyer);
        }
    }

    @Nested
    @DisplayName("?ˆí‡´ ?ŒìŠ¤??)
    class WithdrawTest {

        @Test
        @DisplayName("?ˆí‡´ ?±ê³µ")
        void withdraw_success() {
            // given
            Long buyerId = 1L;
            String reason = "?œë¹„??ë¶ˆë§Œì¡?;
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));
            when(buyerDAO.withdrawWithReason(buyerId, reason)).thenReturn(1);

            // when
            int result = buyerSVC.withdraw(buyerId, reason);

            // then
            assertThat(result).isEqualTo(1);
            
            verify(buyerDAO).findById(buyerId);
            verify(buyerDAO).withdrawWithReason(buyerId, reason);
        }

        @Test
        @DisplayName("?ˆí‡´ ?¤íŒ¨ - ì¡´ì¬?˜ì? ?ŠëŠ” ?Œì›")
        void withdraw_fail_member_not_found() {
            // given
            Long buyerId = 999L;
            String reason = "?œë¹„??ë¶ˆë§Œì¡?;
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> buyerSVC.withdraw(buyerId, reason))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("êµ¬ë§¤?ë? ì°¾ì„ ???†ìŠµ?ˆë‹¤");

            verify(buyerDAO).findById(buyerId);
            verify(buyerDAO, never()).withdrawWithReason(anyLong(), anyString());
        }

        @Test
        @DisplayName("?ˆí‡´ ?¤íŒ¨ - ?ˆí‡´ ì²˜ë¦¬ ?¤íŒ¨")
        void withdraw_fail_process_failed() {
            // given
            Long buyerId = 1L;
            String reason = "?œë¹„??ë¶ˆë§Œì¡?;
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));
            when(buyerDAO.withdrawWithReason(buyerId, reason)).thenReturn(0);

            // when & then
            assertThatThrownBy(() -> buyerSVC.withdraw(buyerId, reason))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("êµ¬ë§¤???ˆí‡´ ì²˜ë¦¬???¤íŒ¨?ˆìŠµ?ˆë‹¤");

            verify(buyerDAO).findById(buyerId);
            verify(buyerDAO).withdrawWithReason(buyerId, reason);
        }
    }

    @Nested
    @DisplayName("ë¹„ë?ë²ˆí˜¸ ?•ì¸ ?ŒìŠ¤??)
    class CheckPasswordTest {

        @Test
        @DisplayName("ë¹„ë?ë²ˆí˜¸ ?•ì¸ ?±ê³µ")
        void checkPassword_success() {
            // given
            Long buyerId = 1L;
            String password = "password123";
            testBuyer.setPassword(password);
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));

            // when
            boolean result = buyerSVC.checkPassword(buyerId, password);

            // then
            assertThat(result).isTrue();
            
            verify(buyerDAO).findById(buyerId);
        }

        @Test
        @DisplayName("ë¹„ë?ë²ˆí˜¸ ?•ì¸ ?¤íŒ¨ - ë¶ˆì¼ì¹?)
        void checkPassword_fail_wrong_password() {
            // given
            Long buyerId = 1L;
            String correctPassword = "correctPassword";
            String inputPassword = "wrongPassword";
            testBuyer.setPassword(correctPassword);
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));

            // when
            boolean result = buyerSVC.checkPassword(buyerId, inputPassword);

            // then
            assertThat(result).isFalse();
            
            verify(buyerDAO).findById(buyerId);
        }

        @Test
        @DisplayName("ë¹„ë?ë²ˆí˜¸ ?•ì¸ ?¤íŒ¨ - null ë¹„ë?ë²ˆí˜¸")
        void checkPassword_fail_null_password() {
            // given
            Long buyerId = 1L;

            // when
            boolean result = buyerSVC.checkPassword(buyerId, null);

            // then
            assertThat(result).isFalse();
            
            verify(buyerDAO, never()).findById(anyLong());
        }

        @Test
        @DisplayName("ë¹„ë?ë²ˆí˜¸ ?•ì¸ ?¤íŒ¨ - ì¡´ì¬?˜ì? ?ŠëŠ” ?Œì›")
        void checkPassword_fail_member_not_found() {
            // given
            Long buyerId = 999L;
            String password = "password123";
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.empty());

            // when
            boolean result = buyerSVC.checkPassword(buyerId, password);

            // then
            assertThat(result).isFalse();
            
            verify(buyerDAO).findById(buyerId);
        }
    }

    @Nested
    @DisplayName("?œë¹„???´ìš©?„í™© ?ŒìŠ¤??)
    class ServiceUsageTest {

        @Test
        @DisplayName("?œë¹„???´ìš©?„í™© ì¡°íšŒ ?±ê³µ")
        void getServiceUsage_success() {
            // given
            Long buyerId = 1L;
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));

            // when
            MemberStatusInfo result = buyerSVC.getServiceUsage(buyerId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isCanWithdraw()).isTrue();
            assertThat(result.getPoints()).isEqualTo(0);
            assertThat(result.getCoupons()).isEqualTo(0);
            assertThat(result.getActiveOrders()).isEqualTo(0);
            assertThat(result.getShippingOrders()).isEqualTo(0);
            
            verify(buyerDAO).findById(buyerId);
        }

        @Test
        @DisplayName("?œë¹„???´ìš©?„í™© ì¡°íšŒ ?¤íŒ¨ - ?Œì› ?†ìŒ")
        void getServiceUsage_fail_member_not_found() {
            // given
            Long buyerId = 999L;
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> buyerSVC.getServiceUsage(buyerId))
                .isInstanceOf(BusinessValidationException.class);

            verify(buyerDAO).findById(buyerId);
        }
    }

    @Nested
    @DisplayName("ì¤‘ë³µ ì²´í¬ ?ŒìŠ¤??)
    class DuplicateCheckTest {

        @Test
        @DisplayName("?´ë©”??ì¤‘ë³µ ì²´í¬ - ì¡´ì¬??)
        void existsByEmail_true() {
            // given
            String email = "test@buyer.com";
            when(buyerDAO.existsByEmail(email)).thenReturn(true);

            // when
            boolean result = buyerSVC.existsByEmail(email);

            // then
            assertThat(result).isTrue();
            verify(buyerDAO).existsByEmail(email);
        }

        @Test
        @DisplayName("?´ë©”??ì¤‘ë³µ ì²´í¬ - ì¡´ì¬?˜ì? ?ŠìŒ")
        void existsByEmail_false() {
            // given
            String email = "new@buyer.com";
            when(buyerDAO.existsByEmail(email)).thenReturn(false);

            // when
            boolean result = buyerSVC.existsByEmail(email);

            // then
            assertThat(result).isFalse();
            verify(buyerDAO).existsByEmail(email);
        }

        @Test
        @DisplayName("?‰ë„¤??ì¤‘ë³µ ì²´í¬ - ì¡´ì¬??)
        void existsByNickname_true() {
            // given
            String nickname = "ê¸°ì¡´?‰ë„¤??;
            when(buyerDAO.existsByNickname(nickname)).thenReturn(true);

            // when
            boolean result = buyerSVC.existsByNickname(nickname);

            // then
            assertThat(result).isTrue();
            verify(buyerDAO).existsByNickname(nickname);
        }

        @Test
        @DisplayName("?‰ë„¤??ì¤‘ë³µ ì²´í¬ - ì¡´ì¬?˜ì? ?ŠìŒ")
        void existsByNickname_false() {
            // given
            String nickname = "?ˆë¡œ?´ë‹‰?¤ì„";
            when(buyerDAO.existsByNickname(nickname)).thenReturn(false);

            // when
            boolean result = buyerSVC.existsByNickname(nickname);

            // then
            assertThat(result).isFalse();
            verify(buyerDAO).existsByNickname(nickname);
        }
    }

    @Nested
    @DisplayName("?¬í™œ?±í™” ?ŒìŠ¤??)
    class ReactivateTest {

        @Test
        @DisplayName("?¬í™œ?±í™” ?±ê³µ")
        void reactivate_success() {
            // given
            String email = "test@buyer.com";
            String password = "password123";
            
            when(buyerDAO.reactivate(email, password)).thenReturn(1);
            when(buyerDAO.findByEmail(email)).thenReturn(Optional.of(testBuyer));

            // when
            Optional<Buyer> result = buyerSVC.reactivate(email, password);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo(email);
            
            verify(buyerDAO).reactivate(email, password);
            verify(buyerDAO).findByEmail(email);
        }

        @Test
        @DisplayName("?¬í™œ?±í™” ?¤íŒ¨ - ?¼ì¹˜?˜ëŠ” ?ˆí‡´ ê³„ì • ?†ìŒ")
        void reactivate_fail_no_matching_account() {
            // given
            String email = "notfound@buyer.com";
            String password = "password123";
            
            when(buyerDAO.reactivate(email, password)).thenReturn(0);

            // when
            Optional<Buyer> result = buyerSVC.reactivate(email, password);

            // then
            assertThat(result).isEmpty();
            
            verify(buyerDAO).reactivate(email, password);
            verify(buyerDAO, never()).findByEmail(anyString());
        }
    }

    @Nested
    @DisplayName("? í‹¸ë¦¬í‹° ë©”ì„œ???ŒìŠ¤??)
    class UtilityMethodTest {

        @Test
        @DisplayName("ë¡œê·¸??ê°€???¬ë? ?•ì¸ - ê°€??)
        void canLogin_true() {
            // given
            testBuyer.setStatus("?œì„±??);
            testBuyer.setWithdrawnAt(null);

            // when
            boolean result = buyerSVC.canLogin(testBuyer);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("ë¡œê·¸??ê°€???¬ë? ?•ì¸ - ë¶ˆê???(?ˆí‡´)")
        void canLogin_false_withdrawn() {
            // given
            testBuyer.setStatus("?ˆí‡´");
            testBuyer.setWithdrawnAt(new Date());

            // when
            boolean result = buyerSVC.canLogin(testBuyer);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("?ˆí‡´ ?¬ë? ?•ì¸ - ?ˆí‡´??)
        void isWithdrawn_true() {
            // when
            boolean result = buyerSVC.isWithdrawn(withdrawnBuyer);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("?ˆí‡´ ?¬ë? ?•ì¸ - ?ˆí‡´?˜ì? ?ŠìŒ")
        void isWithdrawn_false() {
            // when
            boolean result = buyerSVC.isWithdrawn(testBuyer);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("?ˆí‡´ ê°€???¬ë? ?•ì¸")
        void canWithdraw_true() {
            // given
            Long buyerId = 1L;
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));

            // when
            boolean result = buyerSVC.canWithdraw(buyerId);

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
            Long buyerId = 1L;
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));

            // when
            Optional<Buyer> result = buyerSVC.findById(buyerId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getBuyerId()).isEqualTo(buyerId);
            verify(buyerDAO).findById(buyerId);
        }

        @Test
        @DisplayName("IDë¡?ì¡°íšŒ ?¤íŒ¨ - ì¡´ì¬?˜ì? ?ŠìŒ")
        void findById_not_found() {
            // given
            Long buyerId = 999L;
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.empty());

            // when
            Optional<Buyer> result = buyerSVC.findById(buyerId);

            // then
            assertThat(result).isEmpty();
            verify(buyerDAO).findById(buyerId);
        }

        @Test
        @DisplayName("?´ë©”?¼ë¡œ ì¡°íšŒ ?±ê³µ")
        void findByEmail_success() {
            // given
            String email = "test@buyer.com";
            when(buyerDAO.findByEmail(email)).thenReturn(Optional.of(testBuyer));

            // when
            Optional<Buyer> result = buyerSVC.findByEmail(email);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo(email);
            verify(buyerDAO).findByEmail(email);
        }

        @Test
        @DisplayName("?ˆí‡´ ?Œì› ëª©ë¡ ì¡°íšŒ")
        void getWithdrawnMembers_success() {
            // given
            List<Buyer> withdrawnMembers = Arrays.asList(withdrawnBuyer);
            when(buyerDAO.findWithdrawnMembers()).thenReturn(withdrawnMembers);

            // when
            List<Buyer> result = buyerSVC.getWithdrawnMembers();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isWithdrawn()).isTrue();
            verify(buyerDAO).findWithdrawnMembers();
        }
    }

    // ?ŒìŠ¤???°ì´???ì„± ë©”ì„œ??    private Buyer createTestBuyer() {
        Buyer buyer = new Buyer();
        buyer.setBuyerId(1L);
        buyer.setEmail("test@buyer.com");
        buyer.setPassword("password123");
        buyer.setName("?ŒìŠ¤?¸êµ¬ë§¤ì");
        buyer.setNickname("?ŒìŠ¤?¸ë‹‰?¤ì„");
        buyer.setTel("010-1234-5678");
        buyer.setGender("?¨ì„±");
        buyer.setAddress("?œìš¸??ê°•ë‚¨êµ??Œí—¤?€ë¡?123");
        buyer.setMemberGubun("NEW");
        buyer.setStatus("?œì„±??);
        buyer.setCdate(new Date());
        buyer.setUdate(new Date());
        return buyer;
    }

    private Buyer createWithdrawnBuyer() {
        Buyer buyer = new Buyer();
        buyer.setBuyerId(2L);
        buyer.setEmail("withdrawn@buyer.com");
        buyer.setPassword("password123");
        buyer.setName("?ˆí‡´êµ¬ë§¤??);
        buyer.setNickname("?ˆí‡´?‰ë„¤??);
        buyer.setTel("010-9876-5432");
        buyer.setGender("?¬ì„±");
        buyer.setAddress("?œìš¸???œì´ˆêµ??œì´ˆ?€ë¡?456");
        buyer.setMemberGubun("BRONZE");
        buyer.setStatus("?ˆí‡´");
        buyer.setCdate(new Date());
        buyer.setUdate(new Date());
        buyer.setWithdrawnAt(new Date());
        buyer.setWithdrawnReason("?œë¹„??ë¶ˆë§Œì¡?);
        return buyer;
    }
} 
