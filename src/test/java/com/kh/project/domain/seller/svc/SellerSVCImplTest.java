package com.kh.project.domain.seller.svc;

import com.kh.project.domain.seller.dao.SellerDAO;
import com.kh.project.domain.entity.Seller;
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
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("SellerSVC 포괄적 테스트")
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
        seller.setPassword("shoppassword");
        seller.setBizRegNo("111-22-33333");
        seller.setShopName("My Awesome Shop");
        seller.setName("John Doe");
        seller.setShopAddress("부산시 부산진구 서면로");
        seller.setTel("010-1234-5678");
        seller.setGubun(MemberGubun.NEW.getCode());
        seller.setStatus("ACTIVE");
        seller.setCdate(new Date());
        return seller;
    }

    // ==================== 회원가입 테스트 ====================

    @Test
    @DisplayName("판매자 회원가입 - 성공")
    void join_success() {
        // given: 이메일과 사업자 번호가 중복되지 않는다고 가정
        when(sellerDAO.existsByEmail(testSeller.getEmail())).thenReturn(false);
        when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(false);
        when(sellerDAO.existsByShopName(testSeller.getShopName())).thenReturn(false);
        when(sellerDAO.save(any(Seller.class))).thenReturn(testSeller);

        // when: 회원가입 실행
        Seller joinedSeller = sellerSVC.join(testSeller);

        // then: 결과 검증
        assertNotNull(joinedSeller);
        assertEquals("111-22-33333", joinedSeller.getBizRegNo());
        assertEquals("My Awesome Shop", joinedSeller.getShopName());
        assertEquals(MemberGubun.NEW.getCode(), joinedSeller.getGubun());
        
        verify(sellerDAO).existsByEmail(testSeller.getEmail());
        verify(sellerDAO).existsByBizRegNo(testSeller.getBizRegNo());
        verify(sellerDAO).existsByShopName(testSeller.getShopName());
        verify(sellerDAO).save(any(Seller.class));
    }

    @Test
    @DisplayName("판매자 회원가입 - 실패 (이메일 중복)")
    void join_fail_email_exists() {
        // given
        when(sellerDAO.existsByEmail(testSeller.getEmail())).thenReturn(true);

        // when & then
        assertThrows(BusinessException.class, () -> {
            sellerSVC.join(testSeller);
        });

        verify(sellerDAO).existsByEmail(testSeller.getEmail());
        verify(sellerDAO, never()).save(any(Seller.class));
    }

    @Test
    @DisplayName("판매자 회원가입 - 실패 (사업자 번호 중복)")
    void join_fail_biz_reg_no_exists() {
        // given: 이메일은 중복이 아니지만, 사업자 번호는 중복이라고 가정
        when(sellerDAO.existsByEmail(testSeller.getEmail())).thenReturn(false);
        when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(true);

        // when & then: BusinessException 예외가 발생하는지 검증
        assertThrows(BusinessException.class, () -> {
            sellerSVC.join(testSeller);
        });

        verify(sellerDAO).existsByEmail(testSeller.getEmail());
        verify(sellerDAO).existsByBizRegNo(testSeller.getBizRegNo());
        verify(sellerDAO, never()).save(any(Seller.class));
    }

    @Test
    @DisplayName("판매자 회원가입 - 실패 (상호명 중복)")
    void join_fail_shop_name_exists() {
        // given
        when(sellerDAO.existsByEmail(testSeller.getEmail())).thenReturn(false);
        when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(false);
        when(sellerDAO.existsByShopName(testSeller.getShopName())).thenReturn(true);

        // when & then
        assertThrows(BusinessException.class, () -> {
            sellerSVC.join(testSeller);
        });

        verify(sellerDAO).existsByEmail(testSeller.getEmail());
        verify(sellerDAO).existsByBizRegNo(testSeller.getBizRegNo());
        verify(sellerDAO).existsByShopName(testSeller.getShopName());
        verify(sellerDAO, never()).save(any(Seller.class));
    }

    // ==================== 로그인 테스트 ====================

    @Test
    @DisplayName("판매자 로그인 - 성공")
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
    @DisplayName("판매자 로그인 - 실패 (사용자 없음)")
    void login_fail_user_not_found() {
        // given: DAO가 이메일로 조회하면, 결과가 없다고(empty) 가정
        when(sellerDAO.findByEmail("seller@shop.com")).thenReturn(Optional.empty());

        // when & then: BusinessException 예외가 발생하는지 검증
        assertThrows(BusinessException.class, () -> {
            sellerSVC.login("seller@shop.com", "shoppassword");
        });

        verify(sellerDAO).findByEmail("seller@shop.com");
    }

    @Test
    @DisplayName("판매자 로그인 - 실패 (잘못된 비밀번호)")
    void login_fail_wrong_password() {
        // given
        when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.of(testSeller));

        // when & then
        assertThrows(BusinessException.class, () -> {
            sellerSVC.login(testSeller.getEmail(), "wrongpassword");
        });

        verify(sellerDAO).findByEmail(testSeller.getEmail());
    }

    @Test
    @DisplayName("판매자 로그인 - 실패 (탈퇴한 회원)")
    void login_fail_withdrawn_user() {
        // given
        testSeller.setStatus("WITHDRAWN");
        testSeller.setWithdrawnAt(new Date());
        when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.of(testSeller));

        // when & then
        assertThrows(BusinessException.class, () -> {
            sellerSVC.login(testSeller.getEmail(), testSeller.getPassword());
        });

        verify(sellerDAO).findByEmail(testSeller.getEmail());
    }

    // ==================== 정보 조회 테스트 ====================

    @Test
    @DisplayName("ID로 판매자 조회 - 성공")
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
    @DisplayName("ID로 판매자 조회 - 실패 (존재하지 않음)")
    void findById_not_found() {
        // given
        when(sellerDAO.findById(999L)).thenReturn(Optional.empty());

        // when
        Optional<Seller> foundSeller = sellerSVC.findById(999L);

        // then
        assertFalse(foundSeller.isPresent());
        verify(sellerDAO).findById(999L);
    }

    // ==================== 정보 수정 테스트 ====================

    @Test
    @DisplayName("판매자 정보 수정 - 성공")
    void update_success() {
        // given
        Seller updateSeller = new Seller();
        updateSeller.setShopName("Updated Shop Name");
        updateSeller.setShopAddress("부산시 동래구 온천천로");
        
        when(sellerDAO.update(eq(1L), any(Seller.class))).thenReturn(1);

        // when
        int updatedRows = sellerSVC.update(1L, updateSeller);

        // then
        assertEquals(1, updatedRows);
        verify(sellerDAO).update(eq(1L), any(Seller.class));
    }

    @Test
    @DisplayName("판매자 정보 수정 - 실패 (존재하지 않는 판매자)")
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

    // ==================== 탈퇴 테스트 ====================

    @Test
    @DisplayName("판매자 탈퇴 - 성공")
    void withdraw_success() {
        // given
        String reason = "사업 종료";
        when(sellerDAO.withdrawWithReason(1L, reason)).thenReturn(1);

        // when
        int withdrawnRows = sellerSVC.withdraw(1L, reason);

        // then
        assertEquals(1, withdrawnRows);
        verify(sellerDAO).withdrawWithReason(1L, reason);
    }

    @Test
    @DisplayName("판매자 탈퇴 - 실패 (존재하지 않는 판매자)")
    void withdraw_fail_seller_not_found() {
        // given
        String reason = "사업 종료";
        when(sellerDAO.withdrawWithReason(999L, reason)).thenReturn(0);

        // when
        int withdrawnRows = sellerSVC.withdraw(999L, reason);

        // then
        assertEquals(0, withdrawnRows);
        verify(sellerDAO).withdrawWithReason(999L, reason);
    }

    // ==================== 중복 체크 테스트 ====================

    @Test
    @DisplayName("이메일 중복 체크 - 중복됨")
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
    @DisplayName("이메일 중복 체크 - 중복 안됨")
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
    @DisplayName("사업자등록번호 중복 체크 - 중복됨")
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
    @DisplayName("사업자등록번호 중복 체크 - 중복 안됨")
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
    @DisplayName("상호명 중복 체크 - 중복됨")
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
    @DisplayName("상호명 중복 체크 - 중복 안됨")
    void existsByShopName_false() {
        // given
        when(sellerDAO.existsByShopName("New Shop")).thenReturn(false);

        // when
        boolean exists = sellerSVC.existsByShopName("New Shop");

        // then
        assertFalse(exists);
        verify(sellerDAO).existsByShopName("New Shop");
    }

    // ==================== 비즈니스 로직 테스트 ====================

    @Test
    @DisplayName("로그인 가능 여부 체크 - 가능")
    void canLogin_true() {
        // given
        testSeller.setStatus("ACTIVE");
        testSeller.setWithdrawnAt(null);

        // when
        boolean canLogin = sellerSVC.canLogin(testSeller);

        // then
        assertTrue(canLogin);
    }

    @Test
    @DisplayName("로그인 가능 여부 체크 - 불가능 (탈퇴)")
    void canLogin_false_withdrawn() {
        // given
        testSeller.setStatus("WITHDRAWN");
        testSeller.setWithdrawnAt(new Date());

        // when
        boolean canLogin = sellerSVC.canLogin(testSeller);

        // then
        assertFalse(canLogin);
    }

    @Test
    @DisplayName("탈퇴 여부 체크 - 탈퇴함")
    void isWithdrawn_true() {
        // given
        testSeller.setStatus("WITHDRAWN");
        testSeller.setWithdrawnAt(new Date());

        // when
        boolean isWithdrawn = sellerSVC.isWithdrawn(testSeller);

        // then
        assertTrue(isWithdrawn);
    }

    @Test
    @DisplayName("탈퇴 여부 체크 - 탈퇴 안함")
    void isWithdrawn_false() {
        // given
        testSeller.setStatus("ACTIVE");
        testSeller.setWithdrawnAt(null);

        // when
        boolean isWithdrawn = sellerSVC.isWithdrawn(testSeller);

        // then
        assertFalse(isWithdrawn);
    }

    @Test
    @DisplayName("등급 정보 조회")
    void getGubunInfo() {
        // given
        testSeller.setGubun(MemberGubun.SILVER.getCode());

        // when
        CodeNameInfo gubunInfo = sellerSVC.getGubunInfo(testSeller);

        // then
        assertNotNull(gubunInfo);
        assertEquals(MemberGubun.SILVER.getCode(), gubunInfo.getCode());
        assertEquals(MemberGubun.SILVER.getDescription(), gubunInfo.getName());
    }

    @Test
    @DisplayName("상태 정보 조회")
    void getStatusInfo() {
        // given
        testSeller.setStatus("ACTIVE");

        // when
        CodeNameInfo statusInfo = sellerSVC.getStatusInfo(testSeller);

        // then
        assertNotNull(statusInfo);
        assertEquals("ACTIVE", statusInfo.getCode());
    }

    @Test
    @DisplayName("상점 정보 조회")
    void getShopInfo() {
        // when
        CodeNameInfo shopInfo = sellerSVC.getShopInfo(testSeller);

        // then
        assertNotNull(shopInfo);
        assertEquals(testSeller.getBizRegNo(), shopInfo.getCode());
        assertEquals(testSeller.getShopName(), shopInfo.getName());
    }

    // ==================== 등급 승급 테스트 ====================

    @Test
    @DisplayName("등급 승급 - 성공")
    void upgradeGubun_success() {
        // given
        when(sellerDAO.update(eq(1L), any(Seller.class))).thenReturn(1);

        // when & then
        assertDoesNotThrow(() -> {
            sellerSVC.upgradeGubun(1L, MemberGubun.BRONZE.getCode());
        });

        verify(sellerDAO).update(eq(1L), any(Seller.class));
    }

    // ==================== 사업자등록번호 검증 테스트 ====================

    @Test
    @DisplayName("사업자등록번호 검증 - 유효함")
    void validateBizRegNo_valid() {
        // given
        String validBizRegNo = "123-45-67890";

        // when
        boolean isValid = sellerSVC.validateBizRegNo(validBizRegNo);

        // then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("사업자등록번호 검증 - 무효함 (null)")
    void validateBizRegNo_invalid_null() {
        // when
        boolean isValid = sellerSVC.validateBizRegNo(null);

        // then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("사업자등록번호 검증 - 무효함 (형식 오류)")
    void validateBizRegNo_invalid_format() {
        // given
        String invalidBizRegNo = "12345";

        // when
        boolean isValid = sellerSVC.validateBizRegNo(invalidBizRegNo);

        // then
        assertFalse(isValid);
    }

    // ==================== 관리 기능 테스트 ====================

    @Test
    @DisplayName("탈퇴 판매자 목록 조회")
    void getWithdrawnMembers() {
        // given
        Seller withdrawnSeller1 = createSampleSeller();
        withdrawnSeller1.setSellerId(2L);
        withdrawnSeller1.setStatus("WITHDRAWN");
        withdrawnSeller1.setShopName("Withdrawn Shop 1");
        
        Seller withdrawnSeller2 = createSampleSeller();
        withdrawnSeller2.setSellerId(3L);
        withdrawnSeller2.setStatus("WITHDRAWN");
        withdrawnSeller2.setShopName("Withdrawn Shop 2");

        List<Seller> withdrawnList = Arrays.asList(withdrawnSeller1, withdrawnSeller2);
        when(sellerDAO.findWithdrawnMembers()).thenReturn(withdrawnList);

        // when
        List<Seller> result = sellerSVC.getWithdrawnMembers();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(seller -> "WITHDRAWN".equals(seller.getStatus())));
        verify(sellerDAO).findWithdrawnMembers();
    }

    // ==================== Edge Case 테스트 ====================

    @Test
    @DisplayName("null 판매자 비즈니스 로직 테스트")
    void businessLogic_with_null_seller() {
        // when & then
        assertFalse(sellerSVC.canLogin(null));
        assertFalse(sellerSVC.isWithdrawn(null));
        
        CodeNameInfo gubunInfo = sellerSVC.getGubunInfo(null);
        assertEquals("UNKNOWN", gubunInfo.getCode());
        assertEquals("알 수 없음", gubunInfo.getName());
        
        CodeNameInfo statusInfo = sellerSVC.getStatusInfo(null);
        assertEquals("UNKNOWN", statusInfo.getCode());
        assertEquals("알 수 없음", statusInfo.getName());

        CodeNameInfo shopInfo = sellerSVC.getShopInfo(null);
        assertEquals("UNKNOWN", shopInfo.getCode());
        assertEquals("알 수 없음", shopInfo.getName());
    }

    @Test
    @DisplayName("잘못된 등급 코드 처리")
    void getGubunInfo_invalid_code() {
        // given
        testSeller.setGubun("INVALID_CODE");

        // when
        CodeNameInfo gubunInfo = sellerSVC.getGubunInfo(testSeller);

        // then
        assertNotNull(gubunInfo);
        assertEquals(MemberGubun.NEW.getCode(), gubunInfo.getCode());
        assertEquals(MemberGubun.NEW.getDescription(), gubunInfo.getName());
    }

    @Test
    @DisplayName("빈 상점명 처리")
    void getShopInfo_empty_shop_name() {
        // given
        testSeller.setShopName("");
        testSeller.setBizRegNo("");

        // when
        CodeNameInfo shopInfo = sellerSVC.getShopInfo(testSeller);

        // then
        assertNotNull(shopInfo);
        assertEquals("", shopInfo.getCode());
        assertEquals("", shopInfo.getName());
    }
}