package com.kh.project.domain.buyer.svc;

import com.kh.project.domain.buyer.dao.BuyerDAO;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.domain.entity.MemberGubun;
import com.kh.project.web.common.CodeNameInfo;
import com.kh.project.web.exception.BusinessException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BuyerSVC 포괄적 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BuyerSVC 포괄적 테스트")
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
        buyer.setPassword("password123");
        buyer.setName("테스트구매자");
        buyer.setNickname("테스터");
        buyer.setTel("010-1234-5678");
        buyer.setGender("남성");
        buyer.setBirth(new Date());
        buyer.setAddress("부산시 해운대구");
        buyer.setGubun(MemberGubun.NEW.getCode());
        buyer.setStatus("활성화");
        buyer.setCdate(new Date());
        return buyer;
    }

    // ==================== 회원가입 테스트 ====================

    @Test
    @DisplayName("회원가입 - 성공")
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
    @DisplayName("회원가입 - 실패 (이메일 중복)")
    void join_fail_email_exists() {
        // given
        when(buyerDAO.existsByEmail(testBuyer.getEmail())).thenReturn(true);

        // when & then
        assertThrows(BusinessException.class, () -> {
            buyerSVC.join(testBuyer);
        });

        verify(buyerDAO).existsByEmail(testBuyer.getEmail());
        verify(buyerDAO, never()).save(any(Buyer.class));
    }

    @Test
    @DisplayName("회원가입 - 실패 (닉네임 중복)")
    void join_fail_nickname_exists() {
        // given
        when(buyerDAO.existsByEmail(testBuyer.getEmail())).thenReturn(false);
        when(buyerDAO.existsByNickname(testBuyer.getNickname())).thenReturn(true);

        // when & then
        assertThrows(BusinessException.class, () -> {
            buyerSVC.join(testBuyer);
        });

        verify(buyerDAO).existsByEmail(testBuyer.getEmail());
        verify(buyerDAO).existsByNickname(testBuyer.getNickname());
        verify(buyerDAO, never()).save(any(Buyer.class));
    }

    // ==================== 로그인 테스트 ====================

    @Test
    @DisplayName("로그인 - 성공")
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
    @DisplayName("로그인 - 실패 (존재하지 않는 이메일)")
    void login_fail_user_not_found() {
        // given
        when(buyerDAO.findByEmail("notfound@email.com")).thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class, () -> {
            buyerSVC.login("notfound@email.com", "password");
        });

        verify(buyerDAO).findByEmail("notfound@email.com");
    }

    @Test
    @DisplayName("로그인 - 실패 (잘못된 비밀번호)")
    void login_fail_wrong_password() {
        // given
        when(buyerDAO.findByEmail(testBuyer.getEmail())).thenReturn(Optional.of(testBuyer));

        // when & then
        assertThrows(BusinessException.class, () -> {
            buyerSVC.login(testBuyer.getEmail(), "wrongpassword");
        });

        verify(buyerDAO).findByEmail(testBuyer.getEmail());
    }

    @Test
    @DisplayName("로그인 - 실패 (탈퇴한 회원)")
    void login_fail_withdrawn_user() {
        // given
        testBuyer.setStatus("탈퇴");
        testBuyer.setWithdrawnAt(new Date());
        when(buyerDAO.findByEmail(testBuyer.getEmail())).thenReturn(Optional.of(testBuyer));

        // when & then
        assertThrows(BusinessException.class, () -> {
            buyerSVC.login(testBuyer.getEmail(), testBuyer.getPassword());
        });

        verify(buyerDAO).findByEmail(testBuyer.getEmail());
    }

    // ==================== 정보 조회 테스트 ====================

    @Test
    @DisplayName("ID로 회원 조회 - 성공")
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
    @DisplayName("ID로 회원 조회 - 실패 (존재하지 않음)")
    void findById_not_found() {
        // given
        when(buyerDAO.findById(999L)).thenReturn(Optional.empty());

        // when
        Optional<Buyer> foundBuyer = buyerSVC.findById(999L);

        // then
        assertFalse(foundBuyer.isPresent());
        verify(buyerDAO).findById(999L);
    }

    // ==================== 정보 수정 테스트 ====================

    @Test
    @DisplayName("회원 정보 수정 - 성공")
    void update_success() {
        // given
        Buyer updateBuyer = new Buyer();
        updateBuyer.setName("수정된이름");
        updateBuyer.setTel("010-9999-8888");
        
        when(buyerDAO.update(eq(1L), any(Buyer.class))).thenReturn(1);

        // when
        int updatedRows = buyerSVC.update(1L, updateBuyer);

        // then
        assertEquals(1, updatedRows);
        verify(buyerDAO).update(eq(1L), any(Buyer.class));
    }

    @Test
    @DisplayName("회원 정보 수정 - 실패 (존재하지 않는 회원)")
    void update_fail_user_not_found() {
        // given
        Buyer updateBuyer = new Buyer();
        updateBuyer.setName("수정된이름");
        
        when(buyerDAO.update(eq(999L), any(Buyer.class))).thenReturn(0);

        // when
        int updatedRows = buyerSVC.update(999L, updateBuyer);

        // then
        assertEquals(0, updatedRows);
        verify(buyerDAO).update(eq(999L), any(Buyer.class));
    }

    // ==================== 탈퇴 테스트 ====================

    @Test
    @DisplayName("회원 탈퇴 - 성공")
    void withdraw_success() {
        // given
        String reason = "서비스 불만족";
        when(buyerDAO.withdrawWithReason(1L, reason)).thenReturn(1);

        // when
        int withdrawnRows = buyerSVC.withdraw(1L, reason);

        // then
        assertEquals(1, withdrawnRows);
        verify(buyerDAO).withdrawWithReason(1L, reason);
    }

    @Test
    @DisplayName("회원 탈퇴 - 실패 (존재하지 않는 회원)")
    void withdraw_fail_user_not_found() {
        // given
        String reason = "서비스 불만족";
        when(buyerDAO.withdrawWithReason(999L, reason)).thenReturn(0);

        // when
        int withdrawnRows = buyerSVC.withdraw(999L, reason);

        // then
        assertEquals(0, withdrawnRows);
        verify(buyerDAO).withdrawWithReason(999L, reason);
    }

    // ==================== 중복 체크 테스트 ====================

    @Test
    @DisplayName("이메일 중복 체크 - 중복됨")
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
    @DisplayName("이메일 중복 체크 - 중복 안됨")
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
    @DisplayName("닉네임 중복 체크 - 중복됨")
    void existsByNickname_true() {
        // given
        when(buyerDAO.existsByNickname("테스터")).thenReturn(true);

        // when
        boolean exists = buyerSVC.existsByNickname("테스터");

        // then
        assertTrue(exists);
        verify(buyerDAO).existsByNickname("테스터");
    }

    @Test
    @DisplayName("닉네임 중복 체크 - 중복 안됨")
    void existsByNickname_false() {
        // given
        when(buyerDAO.existsByNickname("새닉네임")).thenReturn(false);

        // when
        boolean exists = buyerSVC.existsByNickname("새닉네임");

        // then
        assertFalse(exists);
        verify(buyerDAO).existsByNickname("새닉네임");
    }

    // ==================== 비즈니스 로직 테스트 ====================

    @Test
    @DisplayName("로그인 가능 여부 체크 - 가능")
    void canLogin_true() {
        // given
        testBuyer.setStatus("활성화");
        testBuyer.setWithdrawnAt(null);

        // when
        boolean canLogin = buyerSVC.canLogin(testBuyer);

        // then
        assertTrue(canLogin);
    }

    @Test
    @DisplayName("로그인 가능 여부 체크 - 불가능 (탈퇴)")
    void canLogin_false_withdrawn() {
        // given
        testBuyer.setStatus("탈퇴");
        testBuyer.setWithdrawnAt(new Date());

        // when
        boolean canLogin = buyerSVC.canLogin(testBuyer);

        // then
        assertFalse(canLogin);
    }

    @Test
    @DisplayName("탈퇴 여부 체크 - 탈퇴함")
    void isWithdrawn_true() {
        // given
        testBuyer.setStatus("탈퇴");
        testBuyer.setWithdrawnAt(new Date());

        // when
        boolean isWithdrawn = buyerSVC.isWithdrawn(testBuyer);

        // then
        assertTrue(isWithdrawn);
    }

    @Test
    @DisplayName("탈퇴 여부 체크 - 탈퇴 안함")
    void isWithdrawn_false() {
        // given
        testBuyer.setStatus("활성화");
        testBuyer.setWithdrawnAt(null);

        // when
        boolean isWithdrawn = buyerSVC.isWithdrawn(testBuyer);

        // then
        assertFalse(isWithdrawn);
    }

    @Test
    @DisplayName("등급 정보 조회")
    void getGubunInfo() {
        // given
        testBuyer.setGubun(MemberGubun.BRONZE.getCode());

        // when
        CodeNameInfo gubunInfo = buyerSVC.getGubunInfo(testBuyer);

        // then
        assertNotNull(gubunInfo);
        assertEquals(MemberGubun.BRONZE.getCode(), gubunInfo.getCode());
        assertEquals(MemberGubun.BRONZE.getDescription(), gubunInfo.getName());
    }

    @Test
    @DisplayName("상태 정보 조회")
    void getStatusInfo() {
        // given
        testBuyer.setStatus("활성화");

        // when
        CodeNameInfo statusInfo = buyerSVC.getStatusInfo(testBuyer);

        // then
        assertNotNull(statusInfo);
        assertEquals("활성화", statusInfo.getCode());
    }

    // ==================== 등급 승급 테스트 ====================

    @Test
    @DisplayName("등급 승급 - 성공")
    void upgradeGubun_success() {
        // given
        when(buyerDAO.update(eq(1L), any(Buyer.class))).thenReturn(1);

        // when & then
        assertDoesNotThrow(() -> {
            buyerSVC.upgradeGubun(1L, MemberGubun.BRONZE.getCode());
        });

        verify(buyerDAO).update(eq(1L), any(Buyer.class));
    }

    // ==================== 관리 기능 테스트 ====================

    @Test
    @DisplayName("탈퇴 회원 목록 조회")
    void getWithdrawnMembers() {
        // given
        Buyer withdrawnBuyer1 = createSampleBuyer();
        withdrawnBuyer1.setBuyerId(2L);
        withdrawnBuyer1.setStatus("탈퇴");
        
        Buyer withdrawnBuyer2 = createSampleBuyer();
        withdrawnBuyer2.setBuyerId(3L);
        withdrawnBuyer2.setStatus("탈퇴");

        List<Buyer> withdrawnList = Arrays.asList(withdrawnBuyer1, withdrawnBuyer2);
        when(buyerDAO.findWithdrawnMembers()).thenReturn(withdrawnList);

        // when
        List<Buyer> result = buyerSVC.getWithdrawnMembers();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(buyer -> "탈퇴".equals(buyer.getStatus())));
        verify(buyerDAO).findWithdrawnMembers();
    }

    // ==================== Edge Case 테스트 ====================

    @Test
    @DisplayName("null 회원 비즈니스 로직 테스트")
    void businessLogic_with_null_buyer() {
        // when & then
        assertFalse(buyerSVC.canLogin(null));
        assertFalse(buyerSVC.isWithdrawn(null));
        
        CodeNameInfo gubunInfo = buyerSVC.getGubunInfo(null);
        assertEquals("UNKNOWN", gubunInfo.getCode());
        assertEquals("알 수 없음", gubunInfo.getName());
        
        CodeNameInfo statusInfo = buyerSVC.getStatusInfo(null);
        assertEquals("UNKNOWN", statusInfo.getCode());
        assertEquals("알 수 없음", statusInfo.getName());
    }

    @Test
    @DisplayName("잘못된 등급 코드 처리")
    void getGubunInfo_invalid_code() {
        // given
        testBuyer.setGubun("INVALID_CODE");

        // when
        CodeNameInfo gubunInfo = buyerSVC.getGubunInfo(testBuyer);

        // then
        assertNotNull(gubunInfo);
        assertEquals(MemberGubun.NEW.getCode(), gubunInfo.getCode());
        assertEquals(MemberGubun.NEW.getDescription(), gubunInfo.getName());
    }
}
