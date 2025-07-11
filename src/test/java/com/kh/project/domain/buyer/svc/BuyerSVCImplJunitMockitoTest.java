package com.kh.project.domain.buyer.svc;

import com.kh.project.domain.buyer.dao.BuyerDAO;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.domain.entity.MemberGubun;
import com.kh.project.web.exception.BusinessException;
import com.kh.project.web.exception.MemberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 + Mockito를 활용한 구매자 서비스 테스트
 * 
 * 테스트 범위:
 * - 회원가입 (성공/실패 케이스)
 * - 로그인 (성공/실패 케이스) 
 * - 정보 수정 (권한/검증)
 * - 탈퇴 처리 (비밀번호 확인)
 * - 등급 관련 유틸리티
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("구매자 서비스 JUnit+Mockito 테스트")
class BuyerSVCImplJunitMockitoTest {

    @Mock
    private BuyerDAO buyerDAO;

    @InjectMocks
    private BuyerSVCImpl buyerSVC;

    private Buyer testBuyer;

    @BeforeEach
    void setUp() {
        testBuyer = createTestBuyer();
    }

    // ==================== 회원가입 테스트 ====================
    
    @Nested
    @DisplayName("회원가입 테스트")
    class JoinTest {

        @Test
        @DisplayName("회원가입 성공 - 모든 정보 정상")
        void join_success() {
            // given
            when(buyerDAO.existsByEmail(testBuyer.getEmail())).thenReturn(false);
            when(buyerDAO.existsByNickname(testBuyer.getNickname())).thenReturn(false);
            when(buyerDAO.save(any(Buyer.class))).thenReturn(testBuyer);

            // when
            Buyer result = buyerSVC.join(testBuyer);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(testBuyer.getEmail());
            assertThat(result.getMemberGubun()).isEqualTo("NEW");
            assertThat(result.getStatus()).isEqualTo("활성화");
            
            verify(buyerDAO).existsByEmail(testBuyer.getEmail());
            verify(buyerDAO).existsByNickname(testBuyer.getNickname());
            verify(buyerDAO).save(any(Buyer.class));
        }

        @Test
        @DisplayName("회원가입 실패 - 이메일 중복")
        void join_fail_email_duplicate() {
            // given
            when(buyerDAO.existsByEmail(testBuyer.getEmail())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> buyerSVC.join(testBuyer))
                .isInstanceOf(MemberException.EmailDuplicationException.class);

            verify(buyerDAO).existsByEmail(testBuyer.getEmail());
            verify(buyerDAO, never()).save(any(Buyer.class));
        }

        @Test
        @DisplayName("회원가입 실패 - 닉네임 중복")
        void join_fail_nickname_duplicate() {
            // given
            when(buyerDAO.existsByEmail(testBuyer.getEmail())).thenReturn(false);
            when(buyerDAO.existsByNickname(testBuyer.getNickname())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> buyerSVC.join(testBuyer))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 사용중인 닉네임");

            verify(buyerDAO).existsByEmail(testBuyer.getEmail());
            verify(buyerDAO).existsByNickname(testBuyer.getNickname());
            verify(buyerDAO, never()).save(any(Buyer.class));
        }
    }

    // ==================== 로그인 테스트 ====================
    
    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("로그인 성공 - 정상 케이스")
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
                .isInstanceOf(MemberException.LoginFailedException.class);

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
                .isInstanceOf(MemberException.LoginFailedException.class);

            verify(buyerDAO).findByEmail(email);
        }

        @Test
        @DisplayName("로그인 실패 - 탈퇴한 회원")
        void login_fail_withdrawn_member() {
            // given
            String email = "withdrawn@test.com";
            testBuyer.setStatus("탈퇴");
            testBuyer.setWithdrawnAt(new Date());
            
            when(buyerDAO.findByEmail(email)).thenReturn(Optional.of(testBuyer));

            // when & then
            assertThatThrownBy(() -> buyerSVC.login(email, testBuyer.getPassword()))
                .isInstanceOf(MemberException.LoginFailedException.class);

            verify(buyerDAO).findByEmail(email);
        }
    }

    // ==================== 정보 수정 테스트 ====================
    
    @Nested
    @DisplayName("정보 수정 테스트")
    class UpdateTest {

        @Test
        @DisplayName("정보 수정 성공 - 닉네임 변경")
        void update_success_nickname_change() {
            // given
            Long buyerId = 1L;
            Buyer updateBuyer = new Buyer();
            updateBuyer.setNickname("새로운닉네임");
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));
            when(buyerDAO.existsByNickname("새로운닉네임")).thenReturn(false);
            when(buyerDAO.update(buyerId, updateBuyer)).thenReturn(1);

            // when
            int result = buyerSVC.update(buyerId, updateBuyer);

            // then
            assertThat(result).isEqualTo(1);
            
            verify(buyerDAO).findById(buyerId);
            verify(buyerDAO).existsByNickname("새로운닉네임");
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
                .hasMessageContaining("이미 사용중인 닉네임");

            verify(buyerDAO).findById(buyerId);
            verify(buyerDAO).existsByNickname("중복닉네임");
            verify(buyerDAO, never()).update(anyLong(), any(Buyer.class));
        }
    }

    // ==================== 탈퇴 테스트 ====================
    
    @Nested
    @DisplayName("탈퇴 테스트")
    class WithdrawTest {

        @Test
        @DisplayName("탈퇴 성공 - 정상 처리")
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
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> buyerSVC.withdraw(buyerId, "탈퇴사유"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("구매자를 찾을 수 없습니다");

            verify(buyerDAO).findById(buyerId);
            verify(buyerDAO, never()).withdrawWithReason(anyLong(), anyString());
        }
    }

    // ==================== 비밀번호 확인 테스트 ====================
    
    @Nested
    @DisplayName("비밀번호 확인 테스트")
    class CheckPasswordTest {

        @Test
        @DisplayName("비밀번호 확인 성공")
        void checkPassword_success() {
            // given
            Long buyerId = 1L;
            String password = "correctPassword";
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
            testBuyer.setPassword("correctPassword");
            
            when(buyerDAO.findById(buyerId)).thenReturn(Optional.of(testBuyer));

            // when
            boolean result = buyerSVC.checkPassword(buyerId, "wrongPassword");

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
    }

    // ==================== 서비스 이용현황 테스트 ====================
    
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
            Map<String, Object> result = buyerSVC.getServiceUsage(buyerId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.get("memberId")).isEqualTo(buyerId);
            assertThat(result.get("memberType")).isEqualTo("BUYER");
            assertThat(result.get("canWithdraw")).isInstanceOf(Boolean.class);
            assertThat(result.get("withdrawBlockReasons")).isNotNull();
            
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
                .isInstanceOf(MemberException.MemberNotFoundException.class);

            verify(buyerDAO).findById(buyerId);
        }
    }

    // ==================== 유틸리티 메서드 ====================
    
    private Buyer createTestBuyer() {
        Buyer buyer = new Buyer();
        buyer.setBuyerId(1L);
        buyer.setEmail("test@buyer.com");
        buyer.setPassword("password123");
        buyer.setName("테스트구매자");
        buyer.setNickname("테스터");
        buyer.setTel("010-1234-5678");
        buyer.setGender("남성");
        buyer.setAddress("부산시 중구 중앙대로");
        buyer.setMemberGubun("NEW");
        buyer.setStatus("활성화");
        buyer.setCdate(new Date());
        return buyer;
    }
} 