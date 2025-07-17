package com.kh.project.domain.seller.svc;

import com.kh.project.domain.seller.dao.SellerDAO;
import com.kh.project.domain.entity.Seller;
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
@DisplayName("SellerSVC ?�괄???�스??)
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
        seller.setShopAddress("부?�시 부?�진�??�면�?);
        seller.setTel("010-1234-5678");
        seller.setGubun(MemberGubun.NEW.getCode());
        seller.setStatus(MemberStatus.ACTIVE);
        seller.setCdate(new Date());
        return seller;
    }

    // ==================== ?�원가???�스??====================

    @Test
    @DisplayName("?�매???�원가??- ?�공")
    void join_success() {
        // given: ?�메?�과 ?�업??번호가 중복?��? ?�는?�고 가??        when(sellerDAO.existsByEmail(testSeller.getEmail())).thenReturn(false);
        when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(false);
        when(sellerDAO.existsByShopName(testSeller.getShopName())).thenReturn(false);
        when(sellerDAO.save(any(Seller.class))).thenReturn(testSeller);

        // when: ?�원가???�행
        Seller joinedSeller = sellerSVC.join(testSeller);

        // then: 결과 검�?        assertNotNull(joinedSeller);
        assertEquals("111-22-33333", joinedSeller.getBizRegNo());
        assertEquals("My Awesome Shop", joinedSeller.getShopName());
        assertEquals(MemberGubun.NEW.getCode(), joinedSeller.getGubun());
        
        verify(sellerDAO).existsByEmail(testSeller.getEmail());
        verify(sellerDAO).existsByBizRegNo(testSeller.getBizRegNo());
        verify(sellerDAO).existsByShopName(testSeller.getShopName());
        verify(sellerDAO).save(any(Seller.class));
    }

    @Test
    @DisplayName("?�매???�원가??- ?�패 (?�메??중복)")
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
    @DisplayName("?�매???�원가??- ?�패 (?�업??번호 중복)")
    void join_fail_biz_reg_no_exists() {
        // given: ?�메?��? 중복???�니지�? ?�업??번호??중복?�라�?가??        when(sellerDAO.existsByEmail(testSeller.getEmail())).thenReturn(false);
        when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(true);

        // when & then: BusinessException ?�외가 발생?�는지 검�?        assertThrows(BusinessValidationException.class, () -> {
            sellerSVC.join(testSeller);
        });

        verify(sellerDAO).existsByEmail(testSeller.getEmail());
        verify(sellerDAO).existsByBizRegNo(testSeller.getBizRegNo());
        verify(sellerDAO, never()).save(any(Seller.class));
    }

    @Test
    @DisplayName("?�매???�원가??- ?�패 (?�호�?중복)")
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

    // ==================== 로그???�스??====================

    @Test
    @DisplayName("?�매??로그??- ?�공")
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
    @DisplayName("?�매??로그??- ?�패 (?�용???�음)")
    void login_fail_user_not_found() {
        // given: DAO가 ?�메?�로 조회?�면, 결과가 ?�다�?empty) 가??        when(sellerDAO.findByEmail("seller@shop.com")).thenReturn(Optional.empty());

        // when & then: BusinessException ?�외가 발생?�는지 검�?        assertThrows(BusinessValidationException.class, () -> {
            sellerSVC.login("seller@shop.com", "ShopPass123!");
        });

        verify(sellerDAO).findByEmail("seller@shop.com");
    }

    @Test
    @DisplayName("?�매??로그??- ?�패 (?�못??비�?번호)")
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
    @DisplayName("?�매??로그??- ?�패 (?�퇴???�원)")
    void login_fail_withdrawn_user() {
        // given
        testSeller.setStatus(MemberStatus.WITHDRAWN);
        testSeller.setWithdrawnAt(new Date());
        when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.of(testSeller));

        // when & then
        assertThrows(BusinessException.class, () -> {
            sellerSVC.login(testSeller.getEmail(), testSeller.getPassword());
        });

        verify(sellerDAO).findByEmail(testSeller.getEmail());
    }

    // ==================== ?�보 조회 ?�스??====================

    @Test
    @DisplayName("ID�??�매??조회 - ?�공")
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
    @DisplayName("ID�??�매??조회 - ?�패 (존재?��? ?�음)")
    void findById_not_found() {
        // given
        when(sellerDAO.findById(999L)).thenReturn(Optional.empty());

        // when
        Optional<Seller> foundSeller = sellerSVC.findById(999L);

        // then
        assertFalse(foundSeller.isPresent());
        verify(sellerDAO).findById(999L);
    }

    // ==================== ?�보 ?�정 ?�스??====================

    @Test
    @DisplayName("?�매???�보 ?�정 - ?�공")
    void update_success() {
        // given
        Seller updateSeller = new Seller();
        updateSeller.setShopName("Updated Shop Name");
        updateSeller.setShopAddress("부?�시 ?�래�??�천천로");
        
        when(sellerDAO.update(eq(1L), any(Seller.class))).thenReturn(1);

        // when
        int updatedRows = sellerSVC.update(1L, updateSeller);

        // then
        assertEquals(1, updatedRows);
        verify(sellerDAO).update(eq(1L), any(Seller.class));
    }

    @Test
    @DisplayName("?�매???�보 ?�정 - ?�패 (존재?��? ?�는 ?�매??")
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

    // ==================== ?�퇴 ?�스??====================

    @Test
    @DisplayName("?�매???�퇴 - ?�공")
    void withdraw_success() {
        // given
        String reason = "?�업 종료";
        when(sellerDAO.withdrawWithReason(1L, reason)).thenReturn(1);

        // when
        int withdrawnRows = sellerSVC.withdraw(1L, reason);

        // then
        assertEquals(1, withdrawnRows);
        verify(sellerDAO).withdrawWithReason(1L, reason);
    }

    @Test
    @DisplayName("?�매???�퇴 - ?�패 (존재?��? ?�는 ?�매??")
    void withdraw_fail_seller_not_found() {
        // given
        String reason = "?�업 종료";
        when(sellerDAO.withdrawWithReason(999L, reason)).thenReturn(0);

        // when
        int withdrawnRows = sellerSVC.withdraw(999L, reason);

        // then
        assertEquals(0, withdrawnRows);
        verify(sellerDAO).withdrawWithReason(999L, reason);
    }

    // ==================== 중복 체크 ?�스??====================

    @Test
    @DisplayName("?�메??중복 체크 - 중복??)
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
    @DisplayName("?�메??중복 체크 - 중복 ?�됨")
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
    @DisplayName("?�업?�등록번??중복 체크 - 중복??)
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
    @DisplayName("?�업?�등록번??중복 체크 - 중복 ?�됨")
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
    @DisplayName("?�호�?중복 체크 - 중복??)
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
    @DisplayName("?�호�?중복 체크 - 중복 ?�됨")
    void existsByShopName_false() {
        // given
        when(sellerDAO.existsByShopName("New Shop")).thenReturn(false);

        // when
        boolean exists = sellerSVC.existsByShopName("New Shop");

        // then
        assertFalse(exists);
        verify(sellerDAO).existsByShopName("New Shop");
    }

    // ==================== 비즈?�스 로직 ?�스??====================

    @Test
    @DisplayName("로그??가???��? 체크 - 가??)
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
    @DisplayName("로그??가???��? 체크 - 불�???(?�퇴)")
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
    @DisplayName("?�퇴 ?��? 체크 - ?�퇴??)
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
    @DisplayName("?�퇴 ?��? 체크 - ?�퇴 ?�함")
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
    @DisplayName("?�원 ?�급 ?�보 조회 - ?�상")
    void getGubunInfo_Success() {
        // given
        testSeller.setGubun(MemberGubun.GOLD);

        // when
        Map<String, String> gubunInfo = sellerSVC.getGubunInfo(testSeller);

        // then
        assertThat(gubunInfo).isNotNull();
        assertThat(gubunInfo.get("code")).isEqualTo("GOLD");
        assertThat(gubunInfo.get("name")).isEqualTo("골드");
    }

    @Test
    @DisplayName("?�원 ?�태 ?�보 조회 - ?�상")
    void getStatusInfo_Success() {
        // given
        testSeller.setStatus(MemberStatus.ACTIVE);

        // when
        Map<String, String> statusInfo = sellerSVC.getStatusInfo(testSeller);

        // then
        assertThat(statusInfo).isNotNull();
        assertThat(statusInfo.get("code")).isEqualTo("?�성??);
        assertThat(statusInfo.get("name")).isEqualTo("?�성??);
    }

    @Test
    @DisplayName("?�점 ?�보 조회 - ?�상")
    void getShopInfo_Success() {
        // when
        Map<String, String> shopInfo = sellerSVC.getShopInfo(testSeller);

        // then
        assertThat(shopInfo).isNotNull();
        assertThat(shopInfo.get("code")).isEqualTo(testSeller.getSellerId().toString());
        assertThat(shopInfo.get("name")).isEqualTo(testSeller.getShopName());
    }

    @Test
    @DisplayName("?�원 ?�보 조회 - null??경우")
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
    @DisplayName("?�원 ?�급 ?�보 조회 - ?�급??null??경우")
    void getGubunInfo_NullGubun() {
        // given
        testSeller.setGubun(null);

        // when
        Map<String, String> gubunInfo = sellerSVC.getGubunInfo(testSeller);

        // then
        assertThat(gubunInfo.get("code")).isEqualTo("UNKNOWN");
        assertThat(gubunInfo.get("name")).isEqualTo("?????�음");
    }

    @Test
    @DisplayName("?�점 ?�보 조회 - ?�점명이 null??경우")
    void getShopInfo_NullShopName() {
        // given
        testSeller.setShopName(null);

        // when
        Map<String, String> shopInfo = sellerSVC.getShopInfo(testSeller);

        // then
        assertThat(shopInfo.get("code")).isEqualTo(testSeller.getSellerId().toString());
        assertThat(shopInfo.get("name")).isNull();
    }

    // ==================== ?�급 ?�급 ?�스??====================

    @Test
    @DisplayName("?�급 ?�급 - ?�공")
    void upgradeGubun_success() {
        // given
        when(sellerDAO.update(eq(1L), any(Seller.class))).thenReturn(1);

        // when & then
        assertDoesNotThrow(() -> {
            sellerSVC.upgradeGubun(1L, MemberGubun.BRONZE.getCode());
        });

        verify(sellerDAO).update(eq(1L), any(Seller.class));
    }

    // ==================== ?�업?�등록번??검�??�스??====================

    @Test
    @DisplayName("?�업?�등록번??검�?- ?�효??)
    void validateBizRegNo_valid() {
        // given
        String validBizRegNo = "123-45-67890";

        // when
        boolean isValid = sellerSVC.validateBizRegNo(validBizRegNo);

        // then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("?�업?�등록번??검�?- 무효??(null)")
    void validateBizRegNo_invalid_null() {
        // when
        boolean isValid = sellerSVC.validateBizRegNo(null);

        // then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("?�업?�등록번??검�?- 무효??(?�식 ?�류)")
    void validateBizRegNo_invalid_format() {
        // given
        String invalidBizRegNo = "12345";

        // when
        boolean isValid = sellerSVC.validateBizRegNo(invalidBizRegNo);

        // then
        assertFalse(isValid);
    }

    // ==================== 관�?기능 ?�스??====================

    @Test
    @DisplayName("?�퇴 ?�매??목록 조회")
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

    // ==================== Edge Case ?�스??====================

    @Test
    @DisplayName("null ?�매??비즈?�스 로직 ?�스??)
    void businessLogic_with_null_seller() {
        // when & then
        assertFalse(sellerSVC.canLogin(null));
        assertFalse(sellerSVC.isWithdrawn(null));
        
        Map<String, String> gubunInfo = sellerSVC.getGubunInfo(null);
        assertThat(gubunInfo.get("code")).isEqualTo("UNKNOWN");
        assertThat(gubunInfo.get("name")).isEqualTo("?????�음");
        
        Map<String, String> statusInfo = sellerSVC.getStatusInfo(null);
        assertThat(statusInfo.get("code")).isEqualTo("UNKNOWN");
        assertThat(statusInfo.get("name")).isEqualTo("?????�음");

        Map<String, String> shopInfo = sellerSVC.getShopInfo(null);
        assertThat(shopInfo.get("code")).isEqualTo("UNKNOWN");
        assertThat(shopInfo.get("name")).isEqualTo("?????�음");
    }

    @Test
    @DisplayName("?�못???�급 코드 처리")
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
    @DisplayName("�??�점�?처리")
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
