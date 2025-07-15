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
 * 구매???�비??JUnit + Mockito ?�스?? */
@ExtendWith(MockitoExtension.class)
@DisplayName("구매???�비???�스??)
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
    @DisplayName("?�원가???�스??)
    class JoinTest {

        @Test
        @DisplayName("?�규 ?�원가???�공")
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
        @DisplayName("?�퇴 ?�원 ?�활?�화 ?�공")
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
        @DisplayName("?�원가???�패 - ?�성 ?�메??중복")
        void join_fail_activeEmail_duplicate() {
            // given
            when(buyerDAO.findByEmail(testBuyer.getEmail())).thenReturn(Optional.of(testBuyer));

            // when & then
            assertThatThrownBy(() -> buyerSVC.join(testBuyer))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("?��? ?�용중인 ?�메?�입?�다");

            verify(buyerDAO).findByEmail(testBuyer.getEmail());
            verify(buyerDAO, never()).save(any(Buyer.class));
        }

        @Test
        @DisplayName("?�원가???�패 - ?�네??중복")
        void join_fail_nickname_duplicate() {
            // given
            when(buyerDAO.findByEmail(testBuyer.getEmail())).thenReturn(Optional.empty());
            when(buyerDAO.existsByNickname(testBuyer.getNickname())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> buyerSVC.join(testBuyer))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("?��? ?�용중인 ?�네?�입?�다");

            verify(buyerDAO).findByEmail(testBuyer.getEmail());
            verify(buyerDAO).existsByNickname(testBuyer.getNickname());
            verify(buyerDAO, never()).save(any(Buyer.class));
        }

        @Test
        @DisplayName("?�활?�화 ?�패 - ?�활?�화 처리 ?�패")
        void join_fail_reactivate_failed() {
            // given
            when(buyerDAO.findByEmail(testBuyer.getEmail())).thenReturn(Optional.of(withdrawnBuyer));
            when(buyerDAO.existsByNickname(testBuyer.getNickname())).thenReturn(false);
            when(buyerDAO.rejoin(any(Buyer.class))).thenReturn(0);

            // when & then
            assertThatThrownBy(() -> buyerSVC.join(testBuyer))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("계정 ?�활?�화???�패?�습?�다");

            verify(buyerDAO).rejoin(any(Buyer.class));
        }
    }

    @Nested
    @DisplayName("로그???�스??)
    class LoginTest {

        @Test
        @DisplayName("로그???�공")
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
        @DisplayName("로그???�패 - 존재?��? ?�는 ?�메??)
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
        @DisplayName("로그???�패 - 비�?번호 불일�?)
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
        @DisplayName("로그???�패 - ?�퇴???�원")
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
        @DisplayName("로그???�패 - 비활?�화???�원")
        void login_fail_inactive_member() {
            // given
            String email = "inactive@test.com";
            testBuyer.setStatus("비활?�화");
            
            when(buyerDAO.findByEmail(email)).thenReturn(Optional.of(testBuyer));

            // when & then
            assertThatThrownBy(() -> buyerSVC.login(email, "password123"))
                .isInstanceOf(BusinessValidationException.class);

            verify(buyerDAO).findByEmail(email);
        }
    }

    @Nested
    @DisplayName("?�보 ?�정 ?�스??)
    class UpdateTest {

        @Test
        @DisplayName("?�보 ?�정 ?�공")
        void update_success() {
            // given
            Long buyerId = 1L;
            Buyer updateBuyer = new Buyer();
            updateBuyer.setName("변경된?�름");
            updateBuyer.setNickname("변경된?�네??);
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));
            when(buyerDAO.existsByNickname("변경된?�네??)).thenReturn(false);
            when(buyerDAO.update(buyerId, updateBuyer)).thenReturn(1);

            // when
            int result = buyerSVC.update(buyerId, updateBuyer);

            // then
            assertThat(result).isEqualTo(1);
            
            verify(buyerDAO).findById(buyerId);
            verify(buyerDAO).existsByNickname("변경된?�네??);
            verify(buyerDAO).update(buyerId, updateBuyer);
        }

        @Test
        @DisplayName("?�보 ?�정 ?�패 - ?�네??중복")
        void update_fail_nickname_duplicate() {
            // given
            Long buyerId = 1L;
            Buyer updateBuyer = new Buyer();
            updateBuyer.setNickname("중복?�네??);
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));
            when(buyerDAO.existsByNickname("중복?�네??)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> buyerSVC.update(buyerId, updateBuyer))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("?��? ?�용중인 ?�네?�입?�다");

            verify(buyerDAO).findById(buyerId);
            verify(buyerDAO).existsByNickname("중복?�네??);
            verify(buyerDAO, never()).update(anyLong(), any(Buyer.class));
        }

        @Test
        @DisplayName("?�보 ?�정 ?�공 - 같�? ?�네???��?")
        void update_success_same_nickname() {
            // given
            Long buyerId = 1L;
            Buyer updateBuyer = new Buyer();
            updateBuyer.setNickname("기존?�네??);
            testBuyer.setNickname("기존?�네??);
            
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
    @DisplayName("?�퇴 ?�스??)
    class WithdrawTest {

        @Test
        @DisplayName("?�퇴 ?�공")
        void withdraw_success() {
            // given
            Long buyerId = 1L;
            String reason = "?�비??불만�?;
            
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
        @DisplayName("?�퇴 ?�패 - 존재?��? ?�는 ?�원")
        void withdraw_fail_member_not_found() {
            // given
            Long buyerId = 999L;
            String reason = "?�비??불만�?;
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> buyerSVC.withdraw(buyerId, reason))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("구매?��? 찾을 ???�습?�다");

            verify(buyerDAO).findById(buyerId);
            verify(buyerDAO, never()).withdrawWithReason(anyLong(), anyString());
        }

        @Test
        @DisplayName("?�퇴 ?�패 - ?�퇴 처리 ?�패")
        void withdraw_fail_process_failed() {
            // given
            Long buyerId = 1L;
            String reason = "?�비??불만�?;
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));
            when(buyerDAO.withdrawWithReason(buyerId, reason)).thenReturn(0);

            // when & then
            assertThatThrownBy(() -> buyerSVC.withdraw(buyerId, reason))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("구매???�퇴 처리???�패?�습?�다");

            verify(buyerDAO).findById(buyerId);
            verify(buyerDAO).withdrawWithReason(buyerId, reason);
        }
    }

    @Nested
    @DisplayName("비�?번호 ?�인 ?�스??)
    class CheckPasswordTest {

        @Test
        @DisplayName("비�?번호 ?�인 ?�공")
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
        @DisplayName("비�?번호 ?�인 ?�패 - 불일�?)
        void checkPassword_fail_wrong_password() {
            // given
            Long buyerId = 1L;
                    String correctPassword = "CorrectPass123!";
        String inputPassword = "WrongPass123!";
            testBuyer.setPassword(correctPassword);
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));

            // when
            boolean result = buyerSVC.checkPassword(buyerId, inputPassword);

            // then
            assertThat(result).isFalse();
            
            verify(buyerDAO).findById(buyerId);
        }

        @Test
        @DisplayName("비�?번호 ?�인 ?�패 - null 비�?번호")
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
        @DisplayName("비�?번호 ?�인 ?�패 - 존재?��? ?�는 ?�원")
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
    @DisplayName("?�비???�용?�황 ?�스??)
    class ServiceUsageTest {

        @Test
        @DisplayName("?�비???�용?�황 조회 ?�공")
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
        @DisplayName("?�비???�용?�황 조회 ?�패 - ?�원 ?�음")
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
    @DisplayName("중복 체크 ?�스??)
    class DuplicateCheckTest {

        @Test
        @DisplayName("?�메??중복 체크 - 존재??)
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
        @DisplayName("?�메??중복 체크 - 존재?��? ?�음")
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
        @DisplayName("?�네??중복 체크 - 존재??)
        void existsByNickname_true() {
            // given
            String nickname = "기존?�네??;
            when(buyerDAO.existsByNickname(nickname)).thenReturn(true);

            // when
            boolean result = buyerSVC.existsByNickname(nickname);

            // then
            assertThat(result).isTrue();
            verify(buyerDAO).existsByNickname(nickname);
        }

        @Test
        @DisplayName("?�네??중복 체크 - 존재?��? ?�음")
        void existsByNickname_false() {
            // given
            String nickname = "?�로?�닉?�임";
            when(buyerDAO.existsByNickname(nickname)).thenReturn(false);

            // when
            boolean result = buyerSVC.existsByNickname(nickname);

            // then
            assertThat(result).isFalse();
            verify(buyerDAO).existsByNickname(nickname);
        }
    }

    @Nested
    @DisplayName("?�활?�화 ?�스??)
    class ReactivateTest {

        @Test
        @DisplayName("?�활?�화 ?�공")
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
        @DisplayName("?�활?�화 ?�패 - ?�치?�는 ?�퇴 계정 ?�음")
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
    @DisplayName("?�틸리티 메서???�스??)
    class UtilityMethodTest {

        @Test
        @DisplayName("로그??가???��? ?�인 - 가??)
        void canLogin_true() {
            // given
            testBuyer.setStatus("?�성??);
            testBuyer.setWithdrawnAt(null);

            // when
            boolean result = buyerSVC.canLogin(testBuyer);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("로그??가???��? ?�인 - 불�???(?�퇴)")
        void canLogin_false_withdrawn() {
            // given
            testBuyer.setStatus("?�퇴");
            testBuyer.setWithdrawnAt(new Date());

            // when
            boolean result = buyerSVC.canLogin(testBuyer);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("?�퇴 ?��? ?�인 - ?�퇴??)
        void isWithdrawn_true() {
            // when
            boolean result = buyerSVC.isWithdrawn(withdrawnBuyer);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("?�퇴 ?��? ?�인 - ?�퇴?��? ?�음")
        void isWithdrawn_false() {
            // when
            boolean result = buyerSVC.isWithdrawn(testBuyer);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("?�퇴 가???��? ?�인")
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
    @DisplayName("조회 ?�스??)
    class FindTest {

        @Test
        @DisplayName("ID�?조회 ?�공")
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
        @DisplayName("ID�?조회 ?�패 - 존재?��? ?�음")
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
        @DisplayName("?�메?�로 조회 ?�공")
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
        @DisplayName("?�퇴 ?�원 목록 조회")
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

    // ?�스???�이???�성 메서??    private Buyer createTestBuyer() {
        Buyer buyer = new Buyer();
        buyer.setBuyerId(1L);
        buyer.setEmail("test@buyer.com");
        buyer.setPassword("password123");
        buyer.setName("?�스?�구매자");
        buyer.setNickname("?�스?�닉?�임");
        buyer.setTel("010-1234-5678");
        buyer.setGender("?�성");
        buyer.setAddress("?�울??강남�??�헤?��?123");
        buyer.setMemberGubun("NEW");
        buyer.setStatus("?�성??);
        buyer.setCdate(new Date());
        buyer.setUdate(new Date());
        return buyer;
    }

    private Buyer createWithdrawnBuyer() {
        Buyer buyer = new Buyer();
        buyer.setBuyerId(2L);
        buyer.setEmail("withdrawn@buyer.com");
        buyer.setPassword("password123");
        buyer.setName("?�퇴구매??);
        buyer.setNickname("?�퇴?�네??);
        buyer.setTel("010-9876-5432");
        buyer.setGender("?�성");
        buyer.setAddress("?�울???�초�??�초?��?456");
        buyer.setMemberGubun("BRONZE");
        buyer.setStatus("?�퇴");
        buyer.setCdate(new Date());
        buyer.setUdate(new Date());
        buyer.setWithdrawnAt(new Date());
        buyer.setWithdrawnReason("?�비??불만�?);
        return buyer;
    }
} 
