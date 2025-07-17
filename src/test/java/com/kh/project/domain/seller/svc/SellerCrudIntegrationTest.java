package com.kh.project.domain.seller.svc;

import com.kh.project.domain.seller.dao.SellerDAO;
import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.entity.MemberGubun;
import com.kh.project.domain.entity.MemberStatus;
import com.kh.project.web.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ?�매??조회/?�정/?�퇴 ?�합 ?�스???�나리오
 * ?�제 ?�이?�베?�스?� 모든 계층???�합???�경?�서 ?�스?? */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("?�매??조회/?�정/?�퇴 ?�합 ?�스??)
class SellerCrudIntegrationTest {

    @Autowired
    private SellerSVC sellerSVC;

    @Autowired
    private SellerDAO sellerDAO;

    private Seller testSeller;

    @BeforeEach
    void setUp() {
        // ?�스???�이???�리
        sellerDAO.deleteAll();
        
        // ?�스?�용 ?�매???�성
        testSeller = Seller.builder()
                .email("seller@test.com")
                .password("password123")
                .bizRegNo("123-45-67890")
                .shopName("?�스?�상??)
                .name("김?�매??)
                .postcode("12345")
                .address("?�울??강남�??�스?�로 123")
                .detailAddress("101??)
                .tel("02-1234-5678")
                .birth(LocalDate.of(1980, 3, 15))
                .memberGubun(MemberGubun.BRONZE.getCode())
                .memberStatus(MemberStatus.ACTIVE.getCode())
                .build();
        
        testSeller = sellerSVC.join(testSeller);
    }

    @Test
    @DisplayName("?�합 ?�스??1: ?�매???�보 조회 - ID�?조회")
    void integrationTest_findById() {
        // When - ID�??�매??조회
        Optional<Seller> foundSeller = sellerSVC.findById(testSeller.getId());

        // Then - 조회 ?�공 검�?        assertTrue(foundSeller.isPresent());
        assertEquals(testSeller.getId(), foundSeller.get().getId());
        assertEquals(testSeller.getEmail(), foundSeller.get().getEmail());
        assertEquals(testSeller.getShopName(), foundSeller.get().getShopName());
    }

    @Test
    @DisplayName("?�합 ?�스??2: ?�매???�보 조회 - ?�메?�로 조회")
    void integrationTest_findByEmail() {
        // When - ?�메?�로 ?�매??조회
        Optional<Seller> foundSeller = sellerSVC.findByEmail(testSeller.getEmail());

        // Then - 조회 ?�공 검�?        assertTrue(foundSeller.isPresent());
        assertEquals(testSeller.getEmail(), foundSeller.get().getEmail());
        assertEquals(testSeller.getId(), foundSeller.get().getId());
    }

    @Test
    @DisplayName("?�합 ?�스??3: ?�매???�보 ?�정 - 기본 ?�보 ?�정")
    void integrationTest_updateBasicInfo() {
        // Given - ?�정???�보
        Seller updateSeller = Seller.builder()
                .shopName("?�정?�상??)
                .name("?�정?��??�자")
                .tel("02-9999-8888")
                .postcode("54321")
                .address("부?�시 ?�운?��??�정�?456")
                .detailAddress("202??)
                .birth(LocalDate.of(1985, 6, 20))
                .build();

        // When - ?�보 ?�정 ?�행
        int updateCount = sellerSVC.update(testSeller.getId(), updateSeller);

        // Then - ?�정 ?�공 검�?        assertEquals(1, updateCount);

        // ?�정???�보 조회 검�?        Optional<Seller> updatedSeller = sellerSVC.findById(testSeller.getId());
        assertTrue(updatedSeller.isPresent());
        assertEquals("?�정?�상??, updatedSeller.get().getShopName());
        assertEquals("?�정?��??�자", updatedSeller.get().getName());
        assertEquals("02-9999-8888", updatedSeller.get().getTel());
        assertEquals("부?�시 ?�운?��??�정�?456", updatedSeller.get().getAddress());
    }

    @Test
    @DisplayName("?�합 ?�스??4: ?�매???�보 ?�정 - ?�호�?중복 ???�패")
    void integrationTest_updateShopNameDuplicateFailure() {
        // Given - ?�른 ?�매???�성
        Seller anotherSeller = Seller.builder()
                .email("another@test.com")
                .password("password123")
                .bizRegNo("987-65-43210")
                .shopName("?�른?�점")
                .name("?�른?�매??)
                .tel("02-1111-2222")
                .memberGubun(MemberGubun.BRONZE.getCode())
                .memberStatus(MemberStatus.ACTIVE.getCode())
                .build();
        sellerSVC.join(anotherSeller);

        // When & Then - 기존 ?�호명으�??�정 ?�도
        Seller updateSeller = Seller.builder()
                .shopName("?�른?�점")
                .build();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sellerSVC.update(testSeller.getId(), updateSeller);
        });

        assertEquals("?��? ?�용 중인 ?�호명입?�다.", exception.getMessage());
    }

    @Test
    @DisplayName("?�합 ?�스??5: ?�매???�보 ?�정 - ?�?�자�?중복 ???�패")
    void integrationTest_updateNameDuplicateFailure() {
        // Given - ?�른 ?�매???�성
        Seller anotherSeller = Seller.builder()
                .email("another@test.com")
                .password("password123")
                .bizRegNo("987-65-43210")
                .shopName("?�른?�점")
                .name("?�른?�매??)
                .tel("02-1111-2222")
                .memberGubun(MemberGubun.BRONZE.getCode())
                .memberStatus(MemberStatus.ACTIVE.getCode())
                .build();
        sellerSVC.join(anotherSeller);

        // When & Then - 기존 ?�?�자명으�??�정 ?�도
        Seller updateSeller = Seller.builder()
                .name("?�른?�매??)
                .build();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sellerSVC.update(testSeller.getId(), updateSeller);
        });

        assertEquals("?��? ?�록???�?�자명입?�다.", exception.getMessage());
    }

    @Test
    @DisplayName("?�합 ?�스??6: 비�?번호 ?�인 검�?)
    void integrationTest_passwordVerification() {
        // When & Then - ?�바�?비�?번호 ?�인
        assertTrue(sellerSVC.checkPassword(testSeller.getId(), "password123"));

        // When & Then - ?�못??비�?번호 ?�인
        assertFalse(sellerSVC.checkPassword(testSeller.getId(), "wrongPassword"));
    }

    @Test
    @DisplayName("?�합 ?�스??7: ?�매???�퇴 - ?�상 ?�퇴")
    void integrationTest_withdrawSuccess() {
        // Given - ?�퇴 가?�한 ?�태 ?�인
        assertTrue(sellerSVC.canWithdraw(testSeller.getId()));

        // When - ?�매???�퇴 ?�행
        int withdrawResult = sellerSVC.withdraw(testSeller.getId(), "?�업 종료");

        // Then - ?�퇴 ?�공 검�?        assertEquals(1, withdrawResult);

        // ?�퇴 ???�태 검�?        Optional<Seller> withdrawnSeller = sellerSVC.findById(testSeller.getId());
        assertTrue(withdrawnSeller.isPresent());
        assertEquals(MemberStatus.WITHDRAWN.getCode(), withdrawnSeller.get().getMemberStatus());
        assertTrue(sellerSVC.isWithdrawn(withdrawnSeller.get()));
        assertFalse(sellerSVC.canLogin(withdrawnSeller.get()));
    }

    @Test
    @DisplayName("?�합 ?�스??8: ?�퇴 ??로그??차단")
    void integrationTest_loginBlockedAfterWithdraw() {
        // Given - ?�매???�퇴
        sellerSVC.withdraw(testSeller.getId(), "?�업 종료");

        // When & Then - ?�퇴???�매??로그???�도
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sellerSVC.login(testSeller.getEmail(), testSeller.getPassword());
        });

        assertEquals("?�퇴???�원?�니??", exception.getMessage());
    }

    @Test
    @DisplayName("?�합 ?�스??9: ?�퇴 ???�활?�화")
    void integrationTest_reactivateAfterWithdraw() {
        // Given - ?�매???�퇴
        sellerSVC.withdraw(testSeller.getId(), "?�업 종료");

        // When - ?�활?�화 ?�도
        Optional<Seller> reactivatedSeller = sellerSVC.reactivate(testSeller.getEmail(), testSeller.getPassword());

        // Then - ?�활?�화 ?�공 검�?        assertTrue(reactivatedSeller.isPresent());
        assertEquals(MemberStatus.ACTIVE.getCode(), reactivatedSeller.get().getMemberStatus());
        assertTrue(sellerSVC.canLogin(reactivatedSeller.get()));
        assertFalse(sellerSVC.isWithdrawn(reactivatedSeller.get()));
    }

    @Test
    @DisplayName("?�합 ?�스??10: ?�비???�용 ?�황 조회")
    void integrationTest_serviceUsageInfo() {
        // When - ?�비???�용 ?�황 조회
        Map<String, Object> serviceUsage = sellerSVC.getServiceUsage(testSeller.getId());

        // Then - ?�용 ?�황 검�?        assertNotNull(serviceUsage);
        assertTrue(serviceUsage.containsKey("canWithdraw"));
        assertTrue(serviceUsage.containsKey("orderCount"));
        assertTrue(serviceUsage.containsKey("productCount"));
        assertTrue(serviceUsage.containsKey("disputeCount"));
        assertTrue(serviceUsage.containsKey("pointBalance"));
        assertTrue(serviceUsage.containsKey("refundCount"));

        // 초기 ?�태 검�?        assertTrue((Boolean) serviceUsage.get("canWithdraw"));
        assertEquals(0, serviceUsage.get("orderCount"));
        assertEquals(0, serviceUsage.get("productCount"));
        assertEquals(0, serviceUsage.get("disputeCount"));
        assertEquals(0, serviceUsage.get("pointBalance"));
        assertEquals(0, serviceUsage.get("refundCount"));
    }

    @Test
    @DisplayName("?�합 ?�스??11: ?�매???�급 �??�점 ?�보 조회")
    void integrationTest_sellerGradeAndShopInfo() {
        // When - ?�매???�급 �??�점 ?�보 조회
        var gubunInfo = sellerSVC.getGubunInfo(testSeller);
        var statusInfo = sellerSVC.getStatusInfo(testSeller);
        var shopInfo = sellerSVC.getShopInfo(testSeller);

        // Then - ?�급 ?�보 검�?        assertNotNull(gubunInfo);
        assertEquals(MemberGubun.BRONZE.getCode(), gubunInfo.getCode());
        assertEquals(MemberGubun.BRONZE.getDescription(), gubunInfo.getName());

        assertNotNull(statusInfo);
        assertEquals(MemberStatus.ACTIVE.getCode(), statusInfo.getCode());
        assertEquals(MemberStatus.ACTIVE.getDescription(), statusInfo.getName());

        assertNotNull(shopInfo);
        assertEquals(testSeller.getShopName(), shopInfo.getCode());
        assertEquals(testSeller.getShopName(), shopInfo.getName());
    }

    @Test
    @DisplayName("?�합 ?�스??12: ?�업?�등록번???�효??검�?)
    void integrationTest_bizRegNoValidation() {
        // When & Then - ?�효???�업?�등록번??검�?        assertTrue(sellerSVC.validateBizRegNo("123-45-67890"));
        assertTrue(sellerSVC.validateBizRegNo("999-99-99999"));

        // When & Then - ?�효?��? ?��? ?�업?�등록번??검�?        assertFalse(sellerSVC.validateBizRegNo("123456789"));
        assertFalse(sellerSVC.validateBizRegNo("123-456-789"));
        assertFalse(sellerSVC.validateBizRegNo("12-34-56789"));
        assertFalse(sellerSVC.validateBizRegNo(""));
        assertFalse(sellerSVC.validateBizRegNo(null));
    }

    @Test
    @DisplayName("?�합 ?�스??13: ?�매???�퇴 ???�비???�용 ?�황 검??)
    void integrationTest_withdrawServiceUsageCheck() {
        // Given - 초기 ?�태?�서 ?�퇴 가???�인
        assertTrue(sellerSVC.canWithdraw(testSeller.getId()));

        // When - ?�비???�용 ?�황 조회
        Map<String, Object> serviceUsage = sellerSVC.getServiceUsage(testSeller.getId());

        // Then - ?�퇴 가??조건 검�?        assertTrue((Boolean) serviceUsage.get("canWithdraw"));
        assertEquals(0, serviceUsage.get("orderCount"));
        assertEquals(0, serviceUsage.get("productCount"));
        assertEquals(0, serviceUsage.get("disputeCount"));
        assertEquals(0, serviceUsage.get("pointBalance"));
        assertEquals(0, serviceUsage.get("refundCount"));
    }

    @Test
    @DisplayName("?�합 ?�스??14: ?�체 CRUD ?�나리오 종합 검�?)
    void integrationTest_completeCrudScenario() {
        // 1. 조회 (Read)
        Optional<Seller> foundSeller = sellerSVC.findById(testSeller.getId());
        assertTrue(foundSeller.isPresent());

        // 2. ?�정 (Update)
        Seller updateSeller = Seller.builder()
                .shopName("?�정?�상?�명")
                .name("?�정?��??�자�?)
                .tel("02-9999-9999")
                .address("?�정?�주??)
                .build();
        
        int updateResult = sellerSVC.update(testSeller.getId(), updateSeller);
        assertEquals(1, updateResult);

        // 3. ?�정 ?�인
        Optional<Seller> updatedSeller = sellerSVC.findById(testSeller.getId());
        assertTrue(updatedSeller.isPresent());
        assertEquals("?�정?�상?�명", updatedSeller.get().getShopName());
        assertEquals("?�정?��??�자�?, updatedSeller.get().getName());

        // 4. ?�퇴 (Delete - ?�리????��)
        int withdrawResult = sellerSVC.withdraw(testSeller.getId(), "?�스???�료");
        assertEquals(1, withdrawResult);

        // 5. ?�퇴 ?�인
        Optional<Seller> withdrawnSeller = sellerSVC.findById(testSeller.getId());
        assertTrue(withdrawnSeller.isPresent());
        assertTrue(sellerSVC.isWithdrawn(withdrawnSeller.get()));

        // 6. ?�활?�화
        Optional<Seller> reactivatedSeller = sellerSVC.reactivate(testSeller.getEmail(), testSeller.getPassword());
        assertTrue(reactivatedSeller.isPresent());
        assertFalse(sellerSVC.isWithdrawn(reactivatedSeller.get()));

        // 7. 최종 ?�태 검�?        assertTrue(sellerSVC.canLogin(reactivatedSeller.get()));
        assertEquals(MemberStatus.ACTIVE.getCode(), reactivatedSeller.get().getMemberStatus());
    }

    @Test
    @DisplayName("?�합 ?�스??15: ?�매??고유 ?�약 조건 검�?)
    void integrationTest_sellerUniqueConstraints() {
        // Given - ?�재 ?�매???�보 조회
        Optional<Seller> currentSeller = sellerSVC.findById(testSeller.getId());
        assertTrue(currentSeller.isPresent());

        // When & Then - 고유 ?�약 조건 검�?        assertTrue(sellerSVC.existsByEmail(currentSeller.get().getEmail()));
        assertTrue(sellerSVC.existsByBizRegNo(currentSeller.get().getBizRegNo()));
        assertTrue(sellerSVC.existsByShopName(currentSeller.get().getShopName()));
        assertTrue(sellerSVC.existsByName(currentSeller.get().getName()));
        assertTrue(sellerSVC.existsByShopAddress(currentSeller.get().getAddress()));

        // 존재?��? ?�는 값들 검�?        assertFalse(sellerSVC.existsByEmail("nonexistent@test.com"));
        assertFalse(sellerSVC.existsByBizRegNo("999-99-99999"));
        assertFalse(sellerSVC.existsByShopName("존재?��??�는?�점"));
        assertFalse(sellerSVC.existsByName("존재?��??�는?�?�자"));
        assertFalse(sellerSVC.existsByShopAddress("존재?��??�는주소"));
    }
} 
