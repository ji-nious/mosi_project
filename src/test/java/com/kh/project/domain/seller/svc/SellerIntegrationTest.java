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
 * 판매자 회원가입 통합 테스트 시나리오
 * 실제 데이터베이스와 모든 계층이 통합된 환경에서 테스트
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("판매자 회원가입 통합 테스트")
class SellerIntegrationTest {

    @Autowired
    private SellerSVC sellerSVC;

    @Autowired
    private SellerDAO sellerDAO;

    private Seller createValidSeller() {
        return Seller.builder()
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
    }

    @BeforeEach
    void setUp() {
        // 테스트 데이터 정리
        sellerDAO.deleteAll();
    }

    @Test
    @DisplayName("통합 테스트 1: 정상 판매자 회원가입 플로우")
    void integrationTest_normalSellerSignupFlow() {
        // Given - 유효한 판매자 데이터
        Seller seller = createValidSeller();

        // When - 회원가입 실행
        Seller savedSeller = sellerSVC.join(seller);

        // Then - 회원가입 성공 검증
        assertNotNull(savedSeller);
        assertNotNull(savedSeller.getId());
        assertEquals(seller.getEmail(), savedSeller.getEmail());
        assertEquals(seller.getBizRegNo(), savedSeller.getBizRegNo());
        assertEquals(seller.getShopName(), savedSeller.getShopName());
        assertEquals(MemberStatus.ACTIVE.getCode(), savedSeller.getMemberStatus());

        // 데이터베이스 저장 검증
        Optional<Seller> foundSeller = sellerDAO.findByEmail(seller.getEmail());
        assertTrue(foundSeller.isPresent());
        assertEquals(seller.getEmail(), foundSeller.get().getEmail());
    }

    @Test
    @DisplayName("통합 테스트 2: 이메일 중복 시 회원가입 실패")
    void integrationTest_emailDuplicateFailure() {
        // Given - 첫 번째 회원가입
        Seller firstSeller = createValidSeller();
        sellerSVC.join(firstSeller);

        // When & Then - 같은 이메일로 두 번째 회원가입 시도
        Seller secondSeller = createValidSeller();
        secondSeller.setBizRegNo("987-65-43210");
        secondSeller.setShopName("다른상점");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sellerSVC.join(secondSeller);
        });

        assertEquals("이미 가입된 이메일입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("통합 테스트 3: 사업자등록번호 중복 시 회원가입 실패")
    void integrationTest_bizRegNoDuplicateFailure() {
        // Given - 첫 번째 회원가입
        Seller firstSeller = createValidSeller();
        sellerSVC.join(firstSeller);

        // When & Then - 같은 사업자등록번호로 두 번째 회원가입 시도
        Seller secondSeller = createValidSeller();
        secondSeller.setEmail("different@test.com");
        secondSeller.setShopName("다른상점");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sellerSVC.join(secondSeller);
        });

        assertEquals("이미 등록된 사업자등록번호입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("통합 테스트 4: 상호명 중복 시 회원가입 실패")
    void integrationTest_shopNameDuplicateFailure() {
        // Given - 첫 번째 회원가입
        Seller firstSeller = createValidSeller();
        sellerSVC.join(firstSeller);

        // When & Then - 같은 상호명으로 두 번째 회원가입 시도
        Seller secondSeller = createValidSeller();
        secondSeller.setEmail("different@test.com");
        secondSeller.setBizRegNo("987-65-43210");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sellerSVC.join(secondSeller);
        });

        assertEquals("이미 사용 중인 상호명입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("통합 테스트 5: 대표자명 중복 시 회원가입 실패")
    void integrationTest_nameDuplicateFailure() {
        // Given - 첫 번째 회원가입
        Seller firstSeller = createValidSeller();
        sellerSVC.join(firstSeller);

        // When & Then - 같은 대표자명으로 두 번째 회원가입 시도
        Seller secondSeller = createValidSeller();
        secondSeller.setEmail("different@test.com");
        secondSeller.setBizRegNo("987-65-43210");
        secondSeller.setShopName("다른상점");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sellerSVC.join(secondSeller);
        });

        assertEquals("이미 등록된 대표자명입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("통합 테스트 6: 상점 주소 중복 시 회원가입 실패")
    void integrationTest_shopAddressDuplicateFailure() {
        // Given - 첫 번째 회원가입
        Seller firstSeller = createValidSeller();
        sellerSVC.join(firstSeller);

        // When & Then - 같은 상점 주소로 두 번째 회원가입 시도
        Seller secondSeller = createValidSeller();
        secondSeller.setEmail("different@test.com");
        secondSeller.setBizRegNo("987-65-43210");
        secondSeller.setShopName("다른상점");
        secondSeller.setName("다른판매자");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sellerSVC.join(secondSeller);
        });

        assertEquals("이미 등록된 상점 주소입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("통합 테스트 7: 탈퇴한 판매자 재가입 성공")
    void integrationTest_withdrawnSellerRejoinSuccess() {
        // Given - 회원가입 후 탈퇴
        Seller seller = createValidSeller();
        Seller savedSeller = sellerSVC.join(seller);
        sellerSVC.withdraw(savedSeller.getId(), "사업 종료");

        // When - 동일한 이메일로 재가입 시도 (다른 정보로 변경)
        Seller rejoinSeller = createValidSeller();
        rejoinSeller.setBizRegNo("999-88-77777");
        rejoinSeller.setShopName("새로운상점");
        rejoinSeller.setName("새로운대표자");
        rejoinSeller.setAddress("부산시 해운대구 새로운로 456");

        // Then - 재가입 성공
        Seller rejoinedSeller = sellerSVC.join(rejoinSeller);
        assertNotNull(rejoinedSeller);
        assertEquals(seller.getEmail(), rejoinedSeller.getEmail());
        assertEquals("새로운상점", rejoinedSeller.getShopName());
        assertEquals(MemberStatus.ACTIVE.getCode(), rejoinedSeller.getMemberStatus());
    }

    @Test
    @DisplayName("통합 테스트 8: 회원가입 후 즉시 로그인 성공")
    void integrationTest_signupThenLoginSuccess() {
        // Given - 회원가입
        Seller seller = createValidSeller();
        sellerSVC.join(seller);

        // When - 로그인 시도
        Seller loginSeller = sellerSVC.login(seller.getEmail(), seller.getPassword());

        // Then - 로그인 성공
        assertNotNull(loginSeller);
        assertEquals(seller.getEmail(), loginSeller.getEmail());
        assertTrue(sellerSVC.canLogin(loginSeller));
    }

    @Test
    @DisplayName("통합 테스트 9: 사업자등록번호 유효성 검증")
    void integrationTest_bizRegNoValidation() {
        // Given - 잘못된 사업자등록번호 형식
        Seller invalidSeller = createValidSeller();
        invalidSeller.setBizRegNo("123456789"); // 올바른 형식이 아님

        // When & Then - 유효성 검증 실패
        assertFalse(sellerSVC.validateBizRegNo(invalidSeller.getBizRegNo()));
        
        // 올바른 형식 검증
        assertTrue(sellerSVC.validateBizRegNo("123-45-67890"));
    }

    @Test
    @DisplayName("통합 테스트 10: 회원가입 후 중복 체크 기능 검증")
    void integrationTest_duplicateCheckAfterSignup() {
        // Given - 회원가입
        Seller seller = createValidSeller();
        sellerSVC.join(seller);

        // When & Then - 중복 체크 검증
        assertTrue(sellerSVC.existsByEmail(seller.getEmail()));
        assertTrue(sellerSVC.existsByBizRegNo(seller.getBizRegNo()));
        assertTrue(sellerSVC.existsByShopName(seller.getShopName()));
        assertTrue(sellerSVC.existsByName(seller.getName()));
        assertTrue(sellerSVC.existsByShopAddress(seller.getAddress()));

        // 존재하지 않는 정보 검증
        assertFalse(sellerSVC.existsByEmail("nonexistent@test.com"));
        assertFalse(sellerSVC.existsByBizRegNo("999-99-99999"));
        assertFalse(sellerSVC.existsByShopName("존재하지않는상점"));
        assertFalse(sellerSVC.existsByName("존재하지않는대표자"));
        assertFalse(sellerSVC.existsByShopAddress("존재하지않는주소"));
    }

    @Test
    @DisplayName("통합 테스트 11: 회원가입 후 초기 상태 검증")
    void integrationTest_initialStateAfterSignup() {
        // Given - 회원가입
        Seller seller = createValidSeller();
        Seller savedSeller = sellerSVC.join(seller);

        // When - 초기 상태 조회
        Map<String, Object> serviceUsage = sellerSVC.getServiceUsage(savedSeller.getId());

        // Then - 초기 상태 검증
        assertTrue((Boolean) serviceUsage.get("canWithdraw"));
        assertEquals(0, serviceUsage.get("orderCount"));
        assertEquals(0, serviceUsage.get("productCount"));
        assertEquals(0, serviceUsage.get("disputeCount"));
        assertEquals(0, serviceUsage.get("pointBalance"));
    }

    @Test
    @DisplayName("통합 테스트 12: 전체 회원가입 프로세스 종합 검증")
    void integrationTest_completeSignupProcessValidation() {
        // Given - 유효한 판매자 데이터
        Seller seller = createValidSeller();

        // When - 회원가입 실행
        Seller savedSeller = sellerSVC.join(seller);

        // Then - 종합 검증
        // 1. 기본 정보 검증
        assertEquals(seller.getEmail(), savedSeller.getEmail());
        assertEquals(seller.getBizRegNo(), savedSeller.getBizRegNo());
        assertEquals(seller.getShopName(), savedSeller.getShopName());
        assertEquals(seller.getName(), savedSeller.getName());
        assertEquals(seller.getTel(), savedSeller.getTel());

        // 2. 시스템 설정 검증
        assertEquals(MemberGubun.BRONZE.getCode(), savedSeller.getMemberGubun());
        assertEquals(MemberStatus.ACTIVE.getCode(), savedSeller.getMemberStatus());

        // 3. 로그인 가능 상태 검증
        assertTrue(sellerSVC.canLogin(savedSeller));
        assertFalse(sellerSVC.isWithdrawn(savedSeller));

        // 4. 서비스 이용 가능 상태 검증
        assertTrue(sellerSVC.canWithdraw(savedSeller.getId()));

        // 5. 데이터베이스 일관성 검증
        Optional<Seller> dbSeller = sellerDAO.findByEmail(seller.getEmail());
        assertTrue(dbSeller.isPresent());
        assertEquals(savedSeller.getId(), dbSeller.get().getId());

        // 6. 상점 정보 검증
        var shopInfo = sellerSVC.getShopInfo(savedSeller);
        assertNotNull(shopInfo);
        assertEquals(seller.getShopName(), shopInfo.getCode());
        assertEquals(seller.getShopName(), shopInfo.getName());
    }

    @Test
    @DisplayName("통합 테스트 13: 탈퇴한 판매자 재가입 시 중복 체크 로직")
    void integrationTest_withdrawnSellerRejoinDuplicateCheck() {
        // Given - 첫 번째 판매자 회원가입 후 탈퇴
        Seller firstSeller = createValidSeller();
        Seller savedFirst = sellerSVC.join(firstSeller);
        sellerSVC.withdraw(savedFirst.getId(), "사업 종료");

        // When - 동일한 정보로 재가입 시도
        Seller rejoinSeller = createValidSeller();

        // Then - 재가입 성공 (탈퇴 회원은 중복 체크에서 제외)
        assertDoesNotThrow(() -> {
            sellerSVC.join(rejoinSeller);
        });

        // 새로운 판매자가 같은 정보로 가입 시도 시 실패
        Seller anotherSeller = createValidSeller();
        anotherSeller.setEmail("another@test.com");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sellerSVC.join(anotherSeller);
        });

        assertEquals("이미 등록된 사업자등록번호입니다.", exception.getMessage());
    }
} 