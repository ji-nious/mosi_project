package com.kh.project.domain.buyer.svc;

import com.kh.project.domain.buyer.dao.BuyerDAO;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.domain.entity.MemberGubun;
import com.kh.project.domain.entity.MemberStatus;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * BuyerSVC ?�괄???�위 ?�스?? */
@ExtendWith(MockitoExtension.class)
@DisplayName("BuyerSVC ?�괄???�스??)
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
        buyer.setName("?�스?�구매자");
        buyer.setNickname("?�스??);
        buyer.setTel("010-1234-5678");
        buyer.setGender("?�성");
        buyer.setBirth(new Date());
        buyer.setAddress("부?�시 ?�운?��?);
        buyer.setGubun(MemberGubun.NEW.getCode());
        buyer.setStatus(MemberStatus.ACTIVE);
        buyer.setCdate(new Date());
        return buyer;
    }

    // ==================== ?�원가???�스??====================

    @Test
    @DisplayName("?�원가??- ?�공")
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
    @DisplayName("?�원가??- ?�패 (?�메??중복)")
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
    @DisplayName("?�원가??- ?�패 (?�네??중복)")
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

    // ==================== 로그???�스??====================

    @Test
    @DisplayName("로그??- ?�공")
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
    @DisplayName("로그??- ?�패 (존재?��? ?�는 ?�메??")
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
    @DisplayName("로그??- ?�패 (?�못??비�?번호)")
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
    @DisplayName("로그??- ?�패 (?�퇴???�원)")
    void login_fail_withdrawn_user() {
        // given
        testBuyer.setStatus(MemberStatus.WITHDRAWN);
        testBuyer.setWithdrawnAt(new Date());
        when(buyerDAO.findByEmail(testBuyer.getEmail())).thenReturn(Optional.of(testBuyer));

        // when & then
        assertThrows(BusinessException.class, () -> {
            buyerSVC.login(testBuyer.getEmail(), testBuyer.getPassword());
        });

        verify(buyerDAO).findByEmail(testBuyer.getEmail());
    }

    // ==================== ?�보 조회 ?�스??====================

    @Test
    @DisplayName("ID�??�원 조회 - ?�공")
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
    @DisplayName("ID�??�원 조회 - ?�패 (존재?��? ?�음)")
    void findById_not_found() {
        // given
        when(buyerDAO.findById(999L)).thenReturn(Optional.empty());

        // when
        Optional<Buyer> foundBuyer = buyerSVC.findById(999L);

        // then
        assertFalse(foundBuyer.isPresent());
        verify(buyerDAO).findById(999L);
    }

    // ==================== ?�보 ?�정 ?�스??====================

    @Test
    @DisplayName("?�원 ?�보 ?�정 - ?�공")
    void update_success() {
        // given
        Buyer updateBuyer = new Buyer();
        updateBuyer.setName("?�정?�이�?);
        updateBuyer.setTel("010-9999-8888");
        
        when(buyerDAO.update(eq(1L), any(Buyer.class))).thenReturn(1);

        // when
        int updatedRows = buyerSVC.update(1L, updateBuyer);

        // then
        assertEquals(1, updatedRows);
        verify(buyerDAO).update(eq(1L), any(Buyer.class));
    }

    @Test
    @DisplayName("?�원 ?�보 ?�정 - ?�패 (존재?��? ?�는 ?�원)")
    void update_fail_user_not_found() {
        // given
        Buyer updateBuyer = new Buyer();
        updateBuyer.setName("?�정?�이�?);
        
        when(buyerDAO.update(eq(999L), any(Buyer.class))).thenReturn(0);

        // when
        int updatedRows = buyerSVC.update(999L, updateBuyer);

        // then
        assertEquals(0, updatedRows);
        verify(buyerDAO).update(eq(999L), any(Buyer.class));
    }

    // ==================== ?�퇴 ?�스??====================

    @Test
    @DisplayName("?�원 ?�퇴 - ?�공")
    void withdraw_success() {
        // given
        String reason = "?�비??불만�?;
        when(buyerDAO.withdrawWithReason(1L, reason)).thenReturn(1);

        // when
        int withdrawnRows = buyerSVC.withdraw(1L, reason);

        // then
        assertEquals(1, withdrawnRows);
        verify(buyerDAO).withdrawWithReason(1L, reason);
    }

    @Test
    @DisplayName("?�원 ?�퇴 - ?�패 (존재?��? ?�는 ?�원)")
    void withdraw_fail_user_not_found() {
        // given
        String reason = "?�비??불만�?;
        when(buyerDAO.withdrawWithReason(999L, reason)).thenReturn(0);

        // when
        int withdrawnRows = buyerSVC.withdraw(999L, reason);

        // then
        assertEquals(0, withdrawnRows);
        verify(buyerDAO).withdrawWithReason(999L, reason);
    }

    // ==================== 중복 체크 ?�스??====================

    @Test
    @DisplayName("?�메??중복 체크 - 중복??)
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
    @DisplayName("?�메??중복 체크 - 중복 ?�됨")
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
    @DisplayName("?�네??중복 체크 - 중복??)
    void existsByNickname_true() {
        // given
        when(buyerDAO.existsByNickname("?�스??)).thenReturn(true);

        // when
        boolean exists = buyerSVC.existsByNickname("?�스??);

        // then
        assertTrue(exists);
        verify(buyerDAO).existsByNickname("?�스??);
    }

    @Test
    @DisplayName("?�네??중복 체크 - 중복 ?�됨")
    void existsByNickname_false() {
        // given
        when(buyerDAO.existsByNickname("?�닉?�임")).thenReturn(false);

        // when
        boolean exists = buyerSVC.existsByNickname("?�닉?�임");

        // then
        assertFalse(exists);
        verify(buyerDAO).existsByNickname("?�닉?�임");
    }

    // ==================== 비즈?�스 로직 ?�스??====================

    @Test
    @DisplayName("로그??가???��? 체크 - 가??)
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
    @DisplayName("로그??가???��? 체크 - 불�???(?�퇴)")
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
    @DisplayName("?�퇴 ?��? 체크 - ?�퇴??)
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
    @DisplayName("?�퇴 ?��? 체크 - ?�퇴 ?�함")
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
    @DisplayName("?�원 ?�급 ?�보 조회 - ?�상")
    void getGubunInfo_Success() {
        // given
        testBuyer.setGubun(MemberGubun.GOLD);

        // when
        Map<String, String> gubunInfo = buyerSVC.getGubunInfo(testBuyer);

        // then
        assertThat(gubunInfo).isNotNull();
        assertThat(gubunInfo.get("code")).isEqualTo("GOLD");
        assertThat(gubunInfo.get("name")).isEqualTo("골드");
    }

    @Test
    @DisplayName("?�원 ?�태 ?�보 조회 - ?�상")
    void getStatusInfo_Success() {
        // given
        testBuyer.setStatus(MemberStatus.ACTIVE);

        // when
        Map<String, String> statusInfo = buyerSVC.getStatusInfo(testBuyer);

        // then
        assertThat(statusInfo).isNotNull();
        assertThat(statusInfo.get("code")).isEqualTo("?�성??);
        assertThat(statusInfo.get("name")).isEqualTo("?�성??);
    }

    @Test
    @DisplayName("?�원 ?�보 조회 - null??경우")
    void getInfo_Null() {
        // when
        Map<String, String> gubunInfo = buyerSVC.getGubunInfo(null);
        Map<String, String> statusInfo = buyerSVC.getStatusInfo(null);

        // then
        assertThat(gubunInfo.get("code")).isEqualTo("UNKNOWN");
        assertThat(statusInfo.get("code")).isEqualTo("UNKNOWN");
    }

    @Test
    @DisplayName("?�원 ?�급 ?�보 조회 - ?�급??null??경우")
    void getGubunInfo_NullGubun() {
        // given
        testBuyer.setGubun(null);

        // when
        Map<String, String> gubunInfo = buyerSVC.getGubunInfo(testBuyer);

        // then
        assertThat(gubunInfo.get("code")).isEqualTo("UNKNOWN");
        assertThat(gubunInfo.get("name")).isEqualTo("?????�음");
    }

    // ==================== ?�급 ?�급 ?�스??====================

    @Test
    @DisplayName("?�급 ?�급 - ?�공")
    void upgradeGubun_success() {
        // given
        when(buyerDAO.update(eq(1L), any(Buyer.class))).thenReturn(1);

        // when & then
        assertDoesNotThrow(() -> {
            buyerSVC.upgradeGubun(1L, MemberGubun.BRONZE.getCode());
        });

        verify(buyerDAO).update(eq(1L), any(Buyer.class));
    }

    // ==================== 관�?기능 ?�스??====================

    @Test
    @DisplayName("?�퇴 ?�원 목록 조회")
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

    // ==================== Edge Case ?�스??====================

    @Test
    @DisplayName("null ?�원 비즈?�스 로직 ?�스??)
    void businessLogic_with_null_buyer() {
        // when & then
        assertFalse(buyerSVC.canLogin(null));
        assertFalse(buyerSVC.isWithdrawn(null));
        
        Map<String, String> gubunInfo = buyerSVC.getGubunInfo(null);
        assertThat(gubunInfo.get("code")).isEqualTo("UNKNOWN");
        assertThat(gubunInfo.get("name")).isEqualTo("?????�음");
        
        Map<String, String> statusInfo = buyerSVC.getStatusInfo(null);
        assertThat(statusInfo.get("code")).isEqualTo("UNKNOWN");
        assertThat(statusInfo.get("name")).isEqualTo("?????�음");
    }

    @Test
    @DisplayName("?�못???�급 코드 처리")
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
