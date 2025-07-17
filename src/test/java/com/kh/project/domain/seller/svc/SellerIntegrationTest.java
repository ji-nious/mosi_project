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

import static org.junit.jupiter.api.Assertions.*;

/**
 * ?�매???�원가???�합 ?�스???�나리오
 * ?�제 ?�이?�베?�스?� 모든 계층???�합???�경?�서 ?�스?? */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("?�매???�원가???�합 ?�스??)
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
    }

    @BeforeEach
    void setUp() {
        // ?�스???�이???�리
        sellerDAO.deleteAll();
    }

    @Test
    @DisplayName("?�합 ?�스??1: ?�상 ?�매???�원가???�로??)
    void integrationTest_normalSellerSignupFlow() {
        // Given - ?�효???�매???�이??        Seller seller = createValidSeller();

        // When - ?�원가???�행
        Seller savedSeller = sellerSVC.join(seller);

        // Then - ?�원가???�공 검�?        assertNotNull(savedSeller);
        assertNotNull(savedSeller.getId());
        assertEquals(seller.getEmail(), savedSeller.getEmail());
        assertEquals(seller.getBizRegNo(), savedSeller.getBizRegNo());
        assertEquals(seller.getShopName(), savedSeller.getShopName());
        assertEquals(MemberStatus.ACTIVE.getCode(), savedSeller.getMemberStatus());

        // ?�이?�베?�스 ?�??검�?        Optional<Seller> foundSeller = sellerDAO.findByEmail(seller.getEmail());
        assertTrue(foundSeller.isPresent());
        assertEquals(seller.getEmail(), foundSeller.get().getEmail());
    }

    @Test
    @DisplayName("?�합 ?�스??2: ?�메??중복 ???�원가???�패")
    void integrationTest_emailDuplicateFailure() {
        // Given - �?번째 ?�원가??        Seller firstSeller = createValidSeller();
        sellerSVC.join(firstSeller);

        // When & Then - 같�? ?�메?�로 ??번째 ?�원가???�도
        Seller secondSeller = createValidSeller();
        secondSeller.setBizRegNo("987-65-43210");
        secondSeller.setShopName("?�른?�점");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sellerSVC.join(secondSeller);
        });

        assertEquals("?��? 가?�된 ?�메?�입?�다.", exception.getMessage());
    }

    @Test
    @DisplayName("?�합 ?�스??3: ?�업?�등록번??중복 ???�원가???�패")
    void integrationTest_bizRegNoDuplicateFailure() {
        // Given - �?번째 ?�원가??        Seller firstSeller = createValidSeller();
        sellerSVC.join(firstSeller);

        // When & Then - 같�? ?�업?�등록번?�로 ??번째 ?�원가???�도
        Seller secondSeller = createValidSeller();
        secondSeller.setEmail("different@test.com");
        secondSeller.setShopName("?�른?�점");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sellerSVC.join(secondSeller);
        });

        assertEquals("?��? ?�록???�업?�등록번?�입?�다.", exception.getMessage());
    }

    @Test
    @DisplayName("?�합 ?�스??4: ?�호�?중복 ???�원가???�패")
    void integrationTest_shopNameDuplicateFailure() {
        // Given - �?번째 ?�원가??        Seller firstSeller = createValidSeller();
        sellerSVC.join(firstSeller);

        // When & Then - 같�? ?�호명으�???번째 ?�원가???�도
        Seller secondSeller = createValidSeller();
        secondSeller.setEmail("different@test.com");
        secondSeller.setBizRegNo("987-65-43210");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sellerSVC.join(secondSeller);
        });

        assertEquals("?��? ?�용 중인 ?�호명입?�다.", exception.getMessage());
    }

    @Test
    @DisplayName("?�합 ?�스??5: ?�?�자�?중복 ???�원가???�패")
    void integrationTest_nameDuplicateFailure() {
        // Given - �?번째 ?�원가??        Seller firstSeller = createValidSeller();
        sellerSVC.join(firstSeller);

        // When & Then - 같�? ?�?�자명으�???번째 ?�원가???�도
        Seller secondSeller = createValidSeller();
        secondSeller.setEmail("different@test.com");
        secondSeller.setBizRegNo("987-65-43210");
        secondSeller.setShopName("?�른?�점");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sellerSVC.join(secondSeller);
        });

        assertEquals("?��? ?�록???�?�자명입?�다.", exception.getMessage());
    }

    @Test
    @DisplayName("?�합 ?�스??6: ?�점 주소 중복 ???�원가???�패")
    void integrationTest_shopAddressDuplicateFailure() {
        // Given - �?번째 ?�원가??        Seller firstSeller = createValidSeller();
        sellerSVC.join(firstSeller);

        // When & Then - 같�? ?�점 주소�???번째 ?�원가???�도
        Seller secondSeller = createValidSeller();
        secondSeller.setEmail("different@test.com");
        secondSeller.setBizRegNo("987-65-43210");
        secondSeller.setShopName("?�른?�점");
        secondSeller.setName("?�른?�매??);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
            sellerSVC.join(secondSeller);
        });

        assertEquals("?��? ?�록???�점 주소?�니??", exception.getMessage());
    }

    @Test
    @DisplayName("?�합 ?�스??7: ?�퇴???�매???��????�공")
    void integrationTest_withdrawnSellerRejoinSuccess() {
        // Given - ?�원가?????�퇴
        Seller seller = createValidSeller();
        Seller savedSeller = sellerSVC.join(seller);
        sellerSVC.withdraw(savedSeller.getId(), "?�업 종료");

        // When - ?�일???�메?�로 ?��????�도 (?�른 ?�보�?변�?
        Seller rejoinSeller = createValidSeller();
        rejoinSeller.setBizRegNo("999-88-77777");
        rejoinSeller.setShopName("?�로?�상??);
        rejoinSeller.setName("?�로?��??�자");
        rejoinSeller.setAddress("부?�시 ?�운?��??�로?�로 456");

        // Then - ?��????�공
        Seller rejoinedSeller = sellerSVC.join(rejoinSeller);
        assertNotNull(rejoinedSeller);
        assertEquals(seller.getEmail(), rejoinedSeller.getEmail());
        assertEquals("?�로?�상??, rejoinedSeller.getShopName());
        assertEquals(MemberStatus.ACTIVE.getCode(), rejoinedSeller.getMemberStatus());
    }

    @Test
    @DisplayName("?�합 ?�스??8: ?�원가????즉시 로그???�공")
    void integrationTest_signupThenLoginSuccess() {
        // Given - ?�원가??        Seller seller = createValidSeller();
        sellerSVC.join(seller);

        // When - 로그???�도
        Seller loginSeller = sellerSVC.login(seller.getEmail(), seller.getPassword());

        // Then - 로그???�공
        assertNotNull(loginSeller);
        assertEquals(seller.getEmail(), loginSeller.getEmail());
        assertTrue(sellerSVC.canLogin(loginSeller));
    }

    @Test
    @DisplayName("?�합 ?�스??9: ?�업?�등록번???�효??검�?)
    void integrationTest_bizRegNoValidation() {
        // Given - ?�못???�업?�등록번???�식
        Seller invalidSeller = createValidSeller();
        invalidSeller.setBizRegNo("123456789"); // ?�바�??�식???�님

        // When & Then - ?�효??검�??�패
        assertFalse(sellerSVC.validateBizRegNo(invalidSeller.getBizRegNo()));
        
        // ?�바�??�식 검�?        assertTrue(sellerSVC.validateBizRegNo("123-45-67890"));
    }

    @Test
    @DisplayName("?�합 ?�스??10: ?�원가????중복 체크 기능 검�?)
    void integrationTest_duplicateCheckAfterSignup() {
        // Given - ?�원가??        Seller seller = createValidSeller();
        sellerSVC.join(seller);

        // When & Then - 중복 체크 검�?        assertTrue(sellerSVC.existsByEmail(seller.getEmail()));
        assertTrue(sellerSVC.existsByBizRegNo(seller.getBizRegNo()));
        assertTrue(sellerSVC.existsByShopName(seller.getShopName()));
        assertTrue(sellerSVC.existsByName(seller.getName()));
        assertTrue(sellerSVC.existsByShopAddress(seller.getAddress()));

        // 존재?��? ?�는 ?�보 검�?        assertFalse(sellerSVC.existsByEmail("nonexistent@test.com"));
        assertFalse(sellerSVC.existsByBizRegNo("999-99-99999"));
        assertFalse(sellerSVC.existsByShopName("존재?��??�는?�점"));
        assertFalse(sellerSVC.existsByName("존재?��??�는?�?�자"));
        assertFalse(sellerSVC.existsByShopAddress("존재?��??�는주소"));
    }

    @Test
    @DisplayName("?�합 ?�스??11: ?�원가????초기 ?�태 검�?)
    void integrationTest_initialStateAfterSignup() {
        // Given - ?�원가??        Seller seller = createValidSeller();
        Seller savedSeller = sellerSVC.join(seller);

        // When - 초기 ?�태 조회
        Map<String, Object> serviceUsage = sellerSVC.getServiceUsage(savedSeller.getId());

        // Then - 초기 ?�태 검�?        assertTrue((Boolean) serviceUsage.get("canWithdraw"));
        assertEquals(0, serviceUsage.get("orderCount"));
        assertEquals(0, serviceUsage.get("productCount"));
        assertEquals(0, serviceUsage.get("disputeCount"));
        assertEquals(0, serviceUsage.get("pointBalance"));
    }

    @Test
    @DisplayName("?�합 ?�스??12: ?�체 ?�원가???�로?�스 종합 검�?)
    void integrationTest_completeSignupProcessValidation() {
        // Given - ?�효???�매???�이??        Seller seller = createValidSeller();

        // When - ?�원가???�행
        Seller savedSeller = sellerSVC.join(seller);

        // Then - 종합 검�?        // 1. 기본 ?�보 검�?        assertEquals(seller.getEmail(), savedSeller.getEmail());
        assertEquals(seller.getBizRegNo(), savedSeller.getBizRegNo());
        assertEquals(seller.getShopName(), savedSeller.getShopName());
        assertEquals(seller.getName(), savedSeller.getName());
        assertEquals(seller.getTel(), savedSeller.getTel());

        // 2. ?�스???�정 검�?        assertEquals(MemberGubun.BRONZE.getCode(), savedSeller.getMemberGubun());
        assertEquals(MemberStatus.ACTIVE.getCode(), savedSeller.getMemberStatus());

        // 3. 로그??가???�태 검�?        assertTrue(sellerSVC.canLogin(savedSeller));
        assertFalse(sellerSVC.isWithdrawn(savedSeller));

        // 4. ?�비???�용 가???�태 검�?        assertTrue(sellerSVC.canWithdraw(savedSeller.getId()));

        // 5. ?�이?�베?�스 ?��???검�?        Optional<Seller> dbSeller = sellerDAO.findByEmail(seller.getEmail());
        assertTrue(dbSeller.isPresent());
        assertEquals(savedSeller.getId(), dbSeller.get().getId());

        // 6. ?�점 ?�보 검�?        var shopInfo = sellerSVC.getShopInfo(savedSeller);
        assertNotNull(shopInfo);
        assertEquals(seller.getShopName(), shopInfo.getCode());
        assertEquals(seller.getShopName(), shopInfo.getName());
    }

    @Test
    @DisplayName("?�합 ?�스??13: ?�퇴???�매???��?????중복 체크 로직")
    void integrationTest_withdrawnSellerRejoinDuplicateCheck() {
        // Given - �?번째 ?�매???�원가?????�퇴
        Seller firstSeller = createValidSeller();
        Seller savedFirst = sellerSVC.join(firstSeller);
        sellerSVC.withdraw(savedFirst.getId(), "?�업 종료");

        // When - ?�일???�보�??��????�도
        Seller rejoinSeller = createValidSeller();

        // Then - ?��????�공 (?�퇴 ?�원?� 중복 체크?�서 ?�외)
        assertDoesNotThrow(() -> {
            sellerSVC.join(rejoinSeller);
        });

        // ?�로???�매?��? 같�? ?�보�?가???�도 ???�패
        Seller anotherSeller = createValidSeller();
        anotherSeller.setEmail("another@test.com");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            sellerSVC.join(anotherSeller);
        });

        assertEquals("?��? ?�록???�업?�등록번?�입?�다.", exception.getMessage());
    }
} 
