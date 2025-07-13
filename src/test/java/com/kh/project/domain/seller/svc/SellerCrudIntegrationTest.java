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
 * 판매자 조회/수정/탈퇴 통합 테스트 시나리오
 * 실제 데이터베이스와 모든 계층이 통합된 환경에서 테스트
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("판매자 조회/수정/탈퇴 통합 테스트")
class SellerCrudIntegrationTest {

    @Autowired
    private SellerSVC sellerSVC;

    @Autowired
    private SellerDAO sellerDAO;

    private Seller testSeller;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 정리
        sellerDAO.deleteAll();
        
        // 테스트용 판매자 생성
        testSeller = Seller.builder()
                .email("seller@test.com")
                .password("password123")
                .bizRegNo("123-45-67890")
                .shopName("테스트상점")
                .name("김판매자")
                .postcode("12345")
                .address("서울시 강남구 테스트로 123")
                .detailAddress("101호")
                .tel("02-1234-5678")
                .birth(LocalDate.of(1980, 3, 15))
                .memberGubun(MemberGubun.BRONZE.getCode())
                .memberStatus(MemberStatus.ACTIVE.getCode())
                .build();
        
        testSeller = sellerSVC.join(testSeller);
    }

    @Test
    @DisplayName("통합 테스트 1: 판매자 정보 조회 - ID로 조회")
    void integrationTest_findById() {
        // When - ID로 판매자 조회
        Optional<Seller> foundSeller = sellerSVC.findById(testSeller.getId());

        // Then - 조회 성공 검증
        assertTrue(foundSeller.isPresent());
        assertEquals(testSeller.getId(), foundSeller.get().getId());
        assertEquals(testSeller.getEmail(), foundSeller.get().getEmail());
        assertEquals(testSeller.getShopName(), foundSeller.get().getShopName());
    }

    @Test
    @DisplayName("통합 테스트 2: 판매자 정보 조회 - 이메일로 조회")
    void integrationTest_findByEmail() {
        // When - 이메일로 판매자 조회
        Optional<Seller> foundSeller = sellerSVC.findByEmail(testSeller.getEmail());

        // Then - 조회 성공 검증
        assertTrue(foundSeller.isPresent());
        assertEquals(testSeller.getEmail(), foundSeller.get().getEmail());
        assertEquals(testSeller.getId(), foundSeller.get().getId());
    }

    @Test
    @DisplayName("통합 테스트 3: 판매자 정보 수정 - 기본 정보 수정")
    void integrationTest_updateBasicInfo() {
        // Given - 수정할 정보
        Seller updateSeller = Seller.builder()
                .shopName("수정된상점")
                .name("수정된대표자")
                .tel("02-9999-8888")
                .postcode("54321")
                .address("부산시 해운대구 수정로 456")
                .detailAddress("202호")
                .birth(LocalDate.of(1985, 6, 20))
                .build();

        // When - 정보 수정 실행
        int updateCount = sellerSVC.update(testSeller.getId(), updateSeller);

        // Then - 수정 성공 검증
        assertEquals(1, updateCount);

        // 수정된 정보 조회 검증
        Optional<Seller> updatedSeller = sellerSVC.findById(testSeller.getId());
        assertTrue(updatedSeller.isPresent());
        assertEquals("수정된상점", updatedSeller.get().getShopName());
        assertEquals("수정된대표자", updatedSeller.get().getName());
        assertEquals("02-9999-8888", updatedSeller.get().getTel());
        assertEquals("부산시 해운대구 수정로 456", updatedSeller.get().getAddress());
    }

    @Test
    @DisplayName("통합 테스트 4: 판매자 정보 수정 - 상호명 중복 시 실패")
    void integrationTest_updateShopNameDuplicateFailure() {
        // Given - 다른 판매자 생성
        Seller anotherSeller = Seller.builder()
                .email("another@test.com")
                .password("password123")
                .bizRegNo("987-65-43210")
                .shopName("다른상점")
                .name("다른판매자")
                .tel("02-1111-2222")
                .memberGubun(MemberGubun.BRONZE.getCode())
                .memberStatus(MemberStatus.ACTIVE.getCode())
                .build();
        sellerSVC.join(anotherSeller);

        // When & Then - 기존 상호명으로 수정 시도
        Seller updateSeller = Seller.builder()
                .shopName("다른상점")
                .build();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sellerSVC.update(testSeller.getId(), updateSeller);
        });

        assertEquals("이미 사용 중인 상호명입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("통합 테스트 5: 판매자 정보 수정 - 대표자명 중복 시 실패")
    void integrationTest_updateNameDuplicateFailure() {
        // Given - 다른 판매자 생성
        Seller anotherSeller = Seller.builder()
                .email("another@test.com")
                .password("password123")
                .bizRegNo("987-65-43210")
                .shopName("다른상점")
                .name("다른판매자")
                .tel("02-1111-2222")
                .memberGubun(MemberGubun.BRONZE.getCode())
                .memberStatus(MemberStatus.ACTIVE.getCode())
                .build();
        sellerSVC.join(anotherSeller);

        // When & Then - 기존 대표자명으로 수정 시도
        Seller updateSeller = Seller.builder()
                .name("다른판매자")
                .build();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sellerSVC.update(testSeller.getId(), updateSeller);
        });

        assertEquals("이미 등록된 대표자명입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("통합 테스트 6: 비밀번호 확인 검증")
    void integrationTest_passwordVerification() {
        // When & Then - 올바른 비밀번호 확인
        assertTrue(sellerSVC.checkPassword(testSeller.getId(), "password123"));

        // When & Then - 잘못된 비밀번호 확인
        assertFalse(sellerSVC.checkPassword(testSeller.getId(), "wrongPassword"));
    }

    @Test
    @DisplayName("통합 테스트 7: 판매자 탈퇴 - 정상 탈퇴")
    void integrationTest_withdrawSuccess() {
        // Given - 탈퇴 가능한 상태 확인
        assertTrue(sellerSVC.canWithdraw(testSeller.getId()));

        // When - 판매자 탈퇴 실행
        int withdrawResult = sellerSVC.withdraw(testSeller.getId(), "사업 종료");

        // Then - 탈퇴 성공 검증
        assertEquals(1, withdrawResult);

        // 탈퇴 후 상태 검증
        Optional<Seller> withdrawnSeller = sellerSVC.findById(testSeller.getId());
        assertTrue(withdrawnSeller.isPresent());
        assertEquals(MemberStatus.WITHDRAWN.getCode(), withdrawnSeller.get().getMemberStatus());
        assertTrue(sellerSVC.isWithdrawn(withdrawnSeller.get()));
        assertFalse(sellerSVC.canLogin(withdrawnSeller.get()));
    }

    @Test
    @DisplayName("통합 테스트 8: 탈퇴 후 로그인 차단")
    void integrationTest_loginBlockedAfterWithdraw() {
        // Given - 판매자 탈퇴
        sellerSVC.withdraw(testSeller.getId(), "사업 종료");

        // When & Then - 탈퇴한 판매자 로그인 시도
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sellerSVC.login(testSeller.getEmail(), testSeller.getPassword());
        });

        assertEquals("탈퇴한 회원입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("통합 테스트 9: 탈퇴 후 재활성화")
    void integrationTest_reactivateAfterWithdraw() {
        // Given - 판매자 탈퇴
        sellerSVC.withdraw(testSeller.getId(), "사업 종료");

        // When - 재활성화 시도
        Optional<Seller> reactivatedSeller = sellerSVC.reactivate(testSeller.getEmail(), testSeller.getPassword());

        // Then - 재활성화 성공 검증
        assertTrue(reactivatedSeller.isPresent());
        assertEquals(MemberStatus.ACTIVE.getCode(), reactivatedSeller.get().getMemberStatus());
        assertTrue(sellerSVC.canLogin(reactivatedSeller.get()));
        assertFalse(sellerSVC.isWithdrawn(reactivatedSeller.get()));
    }

    @Test
    @DisplayName("통합 테스트 10: 서비스 이용 현황 조회")
    void integrationTest_serviceUsageInfo() {
        // When - 서비스 이용 현황 조회
        Map<String, Object> serviceUsage = sellerSVC.getServiceUsage(testSeller.getId());

        // Then - 이용 현황 검증
        assertNotNull(serviceUsage);
        assertTrue(serviceUsage.containsKey("canWithdraw"));
        assertTrue(serviceUsage.containsKey("orderCount"));
        assertTrue(serviceUsage.containsKey("productCount"));
        assertTrue(serviceUsage.containsKey("disputeCount"));
        assertTrue(serviceUsage.containsKey("pointBalance"));
        assertTrue(serviceUsage.containsKey("refundCount"));

        // 초기 상태 검증
        assertTrue((Boolean) serviceUsage.get("canWithdraw"));
        assertEquals(0, serviceUsage.get("orderCount"));
        assertEquals(0, serviceUsage.get("productCount"));
        assertEquals(0, serviceUsage.get("disputeCount"));
        assertEquals(0, serviceUsage.get("pointBalance"));
        assertEquals(0, serviceUsage.get("refundCount"));
    }

    @Test
    @DisplayName("통합 테스트 11: 판매자 등급 및 상점 정보 조회")
    void integrationTest_sellerGradeAndShopInfo() {
        // When - 판매자 등급 및 상점 정보 조회
        var gubunInfo = sellerSVC.getGubunInfo(testSeller);
        var statusInfo = sellerSVC.getStatusInfo(testSeller);
        var shopInfo = sellerSVC.getShopInfo(testSeller);

        // Then - 등급 정보 검증
        assertNotNull(gubunInfo);
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
    @DisplayName("통합 테스트 12: 사업자등록번호 유효성 검증")
    void integrationTest_bizRegNoValidation() {
        // When & Then - 유효한 사업자등록번호 검증
        assertTrue(sellerSVC.validateBizRegNo("123-45-67890"));
        assertTrue(sellerSVC.validateBizRegNo("999-99-99999"));

        // When & Then - 유효하지 않은 사업자등록번호 검증
        assertFalse(sellerSVC.validateBizRegNo("123456789"));
        assertFalse(sellerSVC.validateBizRegNo("123-456-789"));
        assertFalse(sellerSVC.validateBizRegNo("12-34-56789"));
        assertFalse(sellerSVC.validateBizRegNo(""));
        assertFalse(sellerSVC.validateBizRegNo(null));
    }

    @Test
    @DisplayName("통합 테스트 13: 판매자 탈퇴 시 서비스 이용 현황 검사")
    void integrationTest_withdrawServiceUsageCheck() {
        // Given - 초기 상태에서 탈퇴 가능 확인
        assertTrue(sellerSVC.canWithdraw(testSeller.getId()));

        // When - 서비스 이용 현황 조회
        Map<String, Object> serviceUsage = sellerSVC.getServiceUsage(testSeller.getId());

        // Then - 탈퇴 가능 조건 검증
        assertTrue((Boolean) serviceUsage.get("canWithdraw"));
        assertEquals(0, serviceUsage.get("orderCount"));
        assertEquals(0, serviceUsage.get("productCount"));
        assertEquals(0, serviceUsage.get("disputeCount"));
        assertEquals(0, serviceUsage.get("pointBalance"));
        assertEquals(0, serviceUsage.get("refundCount"));
    }

    @Test
    @DisplayName("통합 테스트 14: 전체 CRUD 시나리오 종합 검증")
    void integrationTest_completeCrudScenario() {
        // 1. 조회 (Read)
        Optional<Seller> foundSeller = sellerSVC.findById(testSeller.getId());
        assertTrue(foundSeller.isPresent());

        // 2. 수정 (Update)
        Seller updateSeller = Seller.builder()
                .shopName("수정된상점명")
                .name("수정된대표자명")
                .tel("02-9999-9999")
                .address("수정된주소")
                .build();
        
        int updateResult = sellerSVC.update(testSeller.getId(), updateSeller);
        assertEquals(1, updateResult);

        // 3. 수정 확인
        Optional<Seller> updatedSeller = sellerSVC.findById(testSeller.getId());
        assertTrue(updatedSeller.isPresent());
        assertEquals("수정된상점명", updatedSeller.get().getShopName());
        assertEquals("수정된대표자명", updatedSeller.get().getName());

        // 4. 탈퇴 (Delete - 논리적 삭제)
        int withdrawResult = sellerSVC.withdraw(testSeller.getId(), "테스트 완료");
        assertEquals(1, withdrawResult);

        // 5. 탈퇴 확인
        Optional<Seller> withdrawnSeller = sellerSVC.findById(testSeller.getId());
        assertTrue(withdrawnSeller.isPresent());
        assertTrue(sellerSVC.isWithdrawn(withdrawnSeller.get()));

        // 6. 재활성화
        Optional<Seller> reactivatedSeller = sellerSVC.reactivate(testSeller.getEmail(), testSeller.getPassword());
        assertTrue(reactivatedSeller.isPresent());
        assertFalse(sellerSVC.isWithdrawn(reactivatedSeller.get()));

        // 7. 최종 상태 검증
        assertTrue(sellerSVC.canLogin(reactivatedSeller.get()));
        assertEquals(MemberStatus.ACTIVE.getCode(), reactivatedSeller.get().getMemberStatus());
    }

    @Test
    @DisplayName("통합 테스트 15: 판매자 고유 제약 조건 검증")
    void integrationTest_sellerUniqueConstraints() {
        // Given - 현재 판매자 정보 조회
        Optional<Seller> currentSeller = sellerSVC.findById(testSeller.getId());
        assertTrue(currentSeller.isPresent());

        // When & Then - 고유 제약 조건 검증
        assertTrue(sellerSVC.existsByEmail(currentSeller.get().getEmail()));
        assertTrue(sellerSVC.existsByBizRegNo(currentSeller.get().getBizRegNo()));
        assertTrue(sellerSVC.existsByShopName(currentSeller.get().getShopName()));
        assertTrue(sellerSVC.existsByName(currentSeller.get().getName()));
        assertTrue(sellerSVC.existsByShopAddress(currentSeller.get().getAddress()));

        // 존재하지 않는 값들 검증
        assertFalse(sellerSVC.existsByEmail("nonexistent@test.com"));
        assertFalse(sellerSVC.existsByBizRegNo("999-99-99999"));
        assertFalse(sellerSVC.existsByShopName("존재하지않는상점"));
        assertFalse(sellerSVC.existsByName("존재하지않는대표자"));
        assertFalse(sellerSVC.existsByShopAddress("존재하지않는주소"));
    }
} 