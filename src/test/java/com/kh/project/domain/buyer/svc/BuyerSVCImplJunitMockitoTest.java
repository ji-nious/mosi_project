package com.kh.project.domain.buyer.svc;

import com.kh.project.domain.buyer.dao.BuyerDAO;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.web.common.form.MemberStatusInfo;
import com.kh.project.web.exception.BusinessException;

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
 * 구매자 서비스 JUnit + Mockito 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("구매자 서비스 테스트")
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
    @DisplayName("회원가입 테스트")
    class JoinTest {

        @Test
        @DisplayName("신규 회원가입 성공")
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
        @DisplayName("탈퇴 회원 재활성화 성공")
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
        @DisplayName("회원가입 실패 - 활성 이메일 중복")
        void join_fail_activeEmail_duplicate() {
            // given
            when(buyerDAO.findByEmail(testBuyer.getEmail())).thenReturn(Optional.of(testBuyer));

            // when & then
            assertThatThrownBy(() -> buyerSVC.join(testBuyer))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 사용중인 이메일입니다");

            verify(buyerDAO).findByEmail(testBuyer.getEmail());
            verify(buyerDAO, never()).save(any(Buyer.class));
        }

        @Test
        @DisplayName("회원가입 실패 - 닉네임 중복")
        void join_fail_nickname_duplicate() {
            // given
            when(buyerDAO.findByEmail(testBuyer.getEmail())).thenReturn(Optional.empty());
            when(buyerDAO.existsByNickname(testBuyer.getNickname())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> buyerSVC.join(testBuyer))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 사용중인 닉네임입니다");

            verify(buyerDAO).findByEmail(testBuyer.getEmail());
            verify(buyerDAO).existsByNickname(testBuyer.getNickname());
            verify(buyerDAO, never()).save(any(Buyer.class));
        }

        @Test
        @DisplayName("재활성화 실패 - 재활성화 처리 실패")
        void join_fail_reactivate_failed() {
            // given
            when(buyerDAO.findByEmail(testBuyer.getEmail())).thenReturn(Optional.of(withdrawnBuyer));
            when(buyerDAO.existsByNickname(testBuyer.getNickname())).thenReturn(false);
            when(buyerDAO.rejoin(any(Buyer.class))).thenReturn(0);

            // when & then
            assertThatThrownBy(() -> buyerSVC.join(testBuyer))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("계정 재활성화에 실패했습니다");

            verify(buyerDAO).rejoin(any(Buyer.class));
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("로그인 성공")
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
        @DisplayName("로그인 실패 - 존재하지 않는 이메일")
        void login_fail_email_not_found() {
            // given
            String email = "notfound@test.com";
            when(buyerDAO.findByEmail(email)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> buyerSVC.login(email, "password"))
                .isInstanceOf(BusinessException.class);

            verify(buyerDAO).findByEmail(email);
        }

        @Test
        @DisplayName("로그인 실패 - 비밀번호 불일치")
        void login_fail_wrong_password() {
            // given
            String email = "test@buyer.com";
            testBuyer.setPassword("correctPassword");
            
            when(buyerDAO.findByEmail(email)).thenReturn(Optional.of(testBuyer));

            // when & then
            assertThatThrownBy(() -> buyerSVC.login(email, "wrongPassword"))
                .isInstanceOf(BusinessException.class);

            verify(buyerDAO).findByEmail(email);
        }

        @Test
        @DisplayName("로그인 실패 - 탈퇴한 회원")
        void login_fail_withdrawn_member() {
            // given
            String email = "withdrawn@test.com";
            
            when(buyerDAO.findByEmail(email)).thenReturn(Optional.of(withdrawnBuyer));

            // when & then
            assertThatThrownBy(() -> buyerSVC.login(email, "password"))
                .isInstanceOf(BusinessException.class);

            verify(buyerDAO).findByEmail(email);
        }

        @Test
        @DisplayName("로그인 실패 - 비활성화된 회원")
        void login_fail_inactive_member() {
            // given
            String email = "inactive@test.com";
            testBuyer.setStatus("비활성화");
            
            when(buyerDAO.findByEmail(email)).thenReturn(Optional.of(testBuyer));

            // when & then
            assertThatThrownBy(() -> buyerSVC.login(email, "password123"))
                .isInstanceOf(BusinessException.class);

            verify(buyerDAO).findByEmail(email);
        }
    }

    @Nested
    @DisplayName("정보 수정 테스트")
    class UpdateTest {

        @Test
        @DisplayName("정보 수정 성공")
        void update_success() {
            // given
            Long buyerId = 1L;
            Buyer updateBuyer = new Buyer();
            updateBuyer.setName("변경된이름");
            updateBuyer.setNickname("변경된닉네임");
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));
            when(buyerDAO.existsByNickname("변경된닉네임")).thenReturn(false);
            when(buyerDAO.update(buyerId, updateBuyer)).thenReturn(1);

            // when
            int result = buyerSVC.update(buyerId, updateBuyer);

            // then
            assertThat(result).isEqualTo(1);
            
            verify(buyerDAO).findById(buyerId);
            verify(buyerDAO).existsByNickname("변경된닉네임");
            verify(buyerDAO).update(buyerId, updateBuyer);
        }

        @Test
        @DisplayName("정보 수정 실패 - 닉네임 중복")
        void update_fail_nickname_duplicate() {
            // given
            Long buyerId = 1L;
            Buyer updateBuyer = new Buyer();
            updateBuyer.setNickname("중복닉네임");
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));
            when(buyerDAO.existsByNickname("중복닉네임")).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> buyerSVC.update(buyerId, updateBuyer))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 사용중인 닉네임입니다");

            verify(buyerDAO).findById(buyerId);
            verify(buyerDAO).existsByNickname("중복닉네임");
            verify(buyerDAO, never()).update(anyLong(), any(Buyer.class));
        }

        @Test
        @DisplayName("정보 수정 성공 - 같은 닉네임 유지")
        void update_success_same_nickname() {
            // given
            Long buyerId = 1L;
            Buyer updateBuyer = new Buyer();
            updateBuyer.setNickname("기존닉네임");
            testBuyer.setNickname("기존닉네임");
            
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
    @DisplayName("탈퇴 테스트")
    class WithdrawTest {

        @Test
        @DisplayName("탈퇴 성공")
        void withdraw_success() {
            // given
            Long buyerId = 1L;
            String reason = "서비스 불만족";
            
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
        @DisplayName("탈퇴 실패 - 존재하지 않는 회원")
        void withdraw_fail_member_not_found() {
            // given
            Long buyerId = 999L;
            String reason = "서비스 불만족";
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> buyerSVC.withdraw(buyerId, reason))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("구매자를 찾을 수 없습니다");

            verify(buyerDAO).findById(buyerId);
            verify(buyerDAO, never()).withdrawWithReason(anyLong(), anyString());
        }

        @Test
        @DisplayName("탈퇴 실패 - 탈퇴 처리 실패")
        void withdraw_fail_process_failed() {
            // given
            Long buyerId = 1L;
            String reason = "서비스 불만족";
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));
            when(buyerDAO.withdrawWithReason(buyerId, reason)).thenReturn(0);

            // when & then
            assertThatThrownBy(() -> buyerSVC.withdraw(buyerId, reason))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("구매자 탈퇴 처리에 실패했습니다");

            verify(buyerDAO).findById(buyerId);
            verify(buyerDAO).withdrawWithReason(buyerId, reason);
        }
    }

    @Nested
    @DisplayName("비밀번호 확인 테스트")
    class CheckPasswordTest {

        @Test
        @DisplayName("비밀번호 확인 성공")
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
        @DisplayName("비밀번호 확인 실패 - 불일치")
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
        @DisplayName("비밀번호 확인 실패 - null 비밀번호")
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
        @DisplayName("비밀번호 확인 실패 - 존재하지 않는 회원")
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
    @DisplayName("서비스 이용현황 테스트")
    class ServiceUsageTest {

        @Test
        @DisplayName("서비스 이용현황 조회 성공")
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
        @DisplayName("서비스 이용현황 조회 실패 - 회원 없음")
        void getServiceUsage_fail_member_not_found() {
            // given
            Long buyerId = 999L;
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> buyerSVC.getServiceUsage(buyerId))
                .isInstanceOf(BusinessException.class);

            verify(buyerDAO).findById(buyerId);
        }
    }

    @Nested
    @DisplayName("중복 체크 테스트")
    class DuplicateCheckTest {

        @Test
        @DisplayName("이메일 중복 체크 - 존재함")
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
        @DisplayName("이메일 중복 체크 - 존재하지 않음")
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
        @DisplayName("닉네임 중복 체크 - 존재함")
        void existsByNickname_true() {
            // given
            String nickname = "기존닉네임";
            when(buyerDAO.existsByNickname(nickname)).thenReturn(true);

            // when
            boolean result = buyerSVC.existsByNickname(nickname);

            // then
            assertThat(result).isTrue();
            verify(buyerDAO).existsByNickname(nickname);
        }

        @Test
        @DisplayName("닉네임 중복 체크 - 존재하지 않음")
        void existsByNickname_false() {
            // given
            String nickname = "새로운닉네임";
            when(buyerDAO.existsByNickname(nickname)).thenReturn(false);

            // when
            boolean result = buyerSVC.existsByNickname(nickname);

            // then
            assertThat(result).isFalse();
            verify(buyerDAO).existsByNickname(nickname);
        }
    }

    @Nested
    @DisplayName("재활성화 테스트")
    class ReactivateTest {

        @Test
        @DisplayName("재활성화 성공")
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
        @DisplayName("재활성화 실패 - 일치하는 탈퇴 계정 없음")
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
    @DisplayName("유틸리티 메서드 테스트")
    class UtilityMethodTest {

        @Test
        @DisplayName("로그인 가능 여부 확인 - 가능")
        void canLogin_true() {
            // given
            testBuyer.setStatus("활성화");
            testBuyer.setWithdrawnAt(null);

            // when
            boolean result = buyerSVC.canLogin(testBuyer);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("로그인 가능 여부 확인 - 불가능 (탈퇴)")
        void canLogin_false_withdrawn() {
            // given
            testBuyer.setStatus("탈퇴");
            testBuyer.setWithdrawnAt(new Date());

            // when
            boolean result = buyerSVC.canLogin(testBuyer);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("탈퇴 여부 확인 - 탈퇴함")
        void isWithdrawn_true() {
            // when
            boolean result = buyerSVC.isWithdrawn(withdrawnBuyer);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("탈퇴 여부 확인 - 탈퇴하지 않음")
        void isWithdrawn_false() {
            // when
            boolean result = buyerSVC.isWithdrawn(testBuyer);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("탈퇴 가능 여부 확인")
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
    @DisplayName("조회 테스트")
    class FindTest {

        @Test
        @DisplayName("ID로 조회 성공")
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
        @DisplayName("ID로 조회 실패 - 존재하지 않음")
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
        @DisplayName("이메일로 조회 성공")
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
        @DisplayName("탈퇴 회원 목록 조회")
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

    // 테스트 데이터 생성 메서드
    private Buyer createTestBuyer() {
        Buyer buyer = new Buyer();
        buyer.setBuyerId(1L);
        buyer.setEmail("test@buyer.com");
        buyer.setPassword("password123");
        buyer.setName("테스트구매자");
        buyer.setNickname("테스트닉네임");
        buyer.setTel("010-1234-5678");
        buyer.setGender("남성");
        buyer.setAddress("서울시 강남구 테헤란로 123");
        buyer.setMemberGubun("NEW");
        buyer.setStatus("활성화");
        buyer.setCdate(new Date());
        buyer.setUdate(new Date());
        return buyer;
    }

    private Buyer createWithdrawnBuyer() {
        Buyer buyer = new Buyer();
        buyer.setBuyerId(2L);
        buyer.setEmail("withdrawn@buyer.com");
        buyer.setPassword("password123");
        buyer.setName("탈퇴구매자");
        buyer.setNickname("탈퇴닉네임");
        buyer.setTel("010-9876-5432");
        buyer.setGender("여성");
        buyer.setAddress("서울시 서초구 서초대로 456");
        buyer.setMemberGubun("BRONZE");
        buyer.setStatus("탈퇴");
        buyer.setCdate(new Date());
        buyer.setUdate(new Date());
        buyer.setWithdrawnAt(new Date());
        buyer.setWithdrawnReason("서비스 불만족");
        return buyer;
    }
} 