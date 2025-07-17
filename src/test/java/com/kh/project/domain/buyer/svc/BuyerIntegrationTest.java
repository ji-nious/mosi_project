package com.kh.project.domain.buyer.svc;

import com.kh.project.domain.buyer.dao.BuyerDAO;
import com.kh.project.domain.entity.Buyer;
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
 * 구매???�원가???�합 ?�스???�나리오
 * ?�제 ?�이?�베?�스?� 모든 계층???�합???�경?�서 ?�스?? */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("구매???�원가???�합 ?�스??)
class BuyerIntegrationTest {

    @Autowired
    private BuyerSVC buyerSVC;

    @Autowired
    private BuyerDAO buyerDAO;

    private Buyer createValidBuyer() {
        return Buyer.builder()
                .name("김구매??)
                .nickname("buyer123")
                .email("buyer@test.com")
                .password("password123")
                .tel("010-1234-5678")
                .gender("?�성")
                .birth(LocalDate.of(1990, 1, 1))
                .postcode("12345")
                .address("?�울??강남�??�스?�로 123")
                .detailAddress("101??)
                .memberGubun(MemberGubun.BRONZE.getCode())
                .memberStatus(MemberStatus.ACTIVE.getCode())
                .build();
    }

    @BeforeEach
    void setUp() {
        // ?�스???�이???�리
        buyerDAO.deleteAll();
    }

    @Test
    @DisplayName("?�합 ?�스??1: ?�상 ?�원가???�로??)
    void integrationTest_normalSignupFlow() {
        // Given - ?�효??구매???�이??        Buyer buyer = createValidBuyer();

        // When - ?�원가???�행
        Buyer savedBuyer = buyerSVC.join(buyer);

        // Then - ?�원가???�공 검�?        assertNotNull(savedBuyer);
        assertNotNull(savedBuyer.getId());
        assertEquals(buyer.getEmail(), savedBuyer.getEmail());
        assertEquals(buyer.getNickname(), savedBuyer.getNickname());
        assertEquals(MemberStatus.ACTIVE.getCode(), savedBuyer.getMemberStatus());

        // ?�이?�베?�스 ?�??검�?        Optional<Buyer> foundBuyer = buyerDAO.findByEmail(buyer.getEmail());
        assertTrue(foundBuyer.isPresent());
        assertEquals(buyer.getEmail(), foundBuyer.get().getEmail());
    }

    @Test
    @DisplayName("?�합 ?�스??2: ?�메??중복 ???�원가???�패")
    void integrationTest_emailDuplicateFailure() {
        // Given - �?번째 ?�원가??        Buyer firstBuyer = createValidBuyer();
        buyerSVC.join(firstBuyer);

        // When & Then - 같�? ?�메?�로 ??번째 ?�원가???�도
        Buyer secondBuyer = createValidBuyer();
        secondBuyer.setNickname("differentNickname");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            buyerSVC.join(secondBuyer);
        });

        assertEquals("?��? 가?�된 ?�메?�입?�다.", exception.getMessage());
    }

    @Test
    @DisplayName("?�합 ?�스??3: ?�네??중복 ???�원가???�패")
    void integrationTest_nicknameDuplicateFailure() {
        // Given - �?번째 ?�원가??        Buyer firstBuyer = createValidBuyer();
        buyerSVC.join(firstBuyer);

        // When & Then - 같�? ?�네?�으�???번째 ?�원가???�도
        Buyer secondBuyer = createValidBuyer();
        secondBuyer.setEmail("different@test.com");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            buyerSVC.join(secondBuyer);
        });

        assertEquals("?��? ?�용 중인 ?�네?�입?�다.", exception.getMessage());
    }

    @Test
    @DisplayName("?�합 ?�스??4: ?�퇴???�원 ?��????�공")
    void integrationTest_withdrawnMemberRejoinSuccess() {
        // Given - ?�원가?????�퇴
        Buyer buyer = createValidBuyer();
        Buyer savedBuyer = buyerSVC.join(buyer);
        buyerSVC.withdraw(savedBuyer.getId(), "개인?�유");

        // When - ?�일???�메?�로 ?��????�도
        Buyer rejoinBuyer = createValidBuyer();
        rejoinBuyer.setNickname("newNickname"); // ?�네??변�?
        // Then - ?��????�공
        Buyer rejoinedBuyer = buyerSVC.join(rejoinBuyer);
        assertNotNull(rejoinedBuyer);
        assertEquals(buyer.getEmail(), rejoinedBuyer.getEmail());
        assertEquals("newNickname", rejoinedBuyer.getNickname());
        assertEquals(MemberStatus.ACTIVE.getCode(), rejoinedBuyer.getMemberStatus());
    }

    @Test
    @DisplayName("?�합 ?�스??5: ?�원가????즉시 로그???�공")
    void integrationTest_signupThenLoginSuccess() {
        // Given - ?�원가??        Buyer buyer = createValidBuyer();
        buyerSVC.join(buyer);

        // When - 로그???�도
        Buyer loginBuyer = buyerSVC.login(buyer.getEmail(), buyer.getPassword());

        // Then - 로그???�공
        assertNotNull(loginBuyer);
        assertEquals(buyer.getEmail(), loginBuyer.getEmail());
        assertTrue(buyerSVC.canLogin(loginBuyer));
    }

    @Test
    @DisplayName("?�합 ?�스??6: ?�원가????중복 체크 기능 검�?)
    void integrationTest_duplicateCheckAfterSignup() {
        // Given - ?�원가??        Buyer buyer = createValidBuyer();
        buyerSVC.join(buyer);

        // When & Then - 중복 체크 검�?        assertTrue(buyerSVC.existsByEmail(buyer.getEmail()));
        assertTrue(buyerSVC.existsByNickname(buyer.getNickname()));
        assertFalse(buyerSVC.existsByEmail("nonexistent@test.com"));
        assertFalse(buyerSVC.existsByNickname("nonexistentNickname"));
    }

    @Test
    @DisplayName("?�합 ?�스??7: ?�원가????초기 ?�태 검�?)
    void integrationTest_initialStateAfterSignup() {
        // Given - ?�원가??        Buyer buyer = createValidBuyer();
        Buyer savedBuyer = buyerSVC.join(buyer);

        // When - 초기 ?�태 조회
        Map<String, Object> serviceUsage = buyerSVC.getServiceUsage(savedBuyer.getId());

        // Then - 초기 ?�태 검�?        assertTrue((Boolean) serviceUsage.get("canWithdraw"));
        assertEquals(0, serviceUsage.get("orderCount"));
        assertEquals(0, serviceUsage.get("pointBalance"));
        assertEquals(0, serviceUsage.get("disputeCount"));
    }

    @Test
    @DisplayName("?�합 ?�스??8: ?�못???�이?�로 ?�원가?????�외 발생")
    void integrationTest_invalidDataSignupFailure() {
        // Given - ?�못???�메???�식
        Buyer invalidBuyer = createValidBuyer();
        invalidBuyer.setEmail("invalid-email");

        // When & Then - ?�이???�효??검�??�패
        assertThrows(Exception.class, () -> {
            buyerSVC.join(invalidBuyer);
        });
    }

    @Test
    @DisplayName("?�합 ?�스??9: ?�체 ?�원가???�로?�스 종합 검�?)
    void integrationTest_completeSignupProcessValidation() {
        // Given - ?�효??구매???�이??        Buyer buyer = createValidBuyer();

        // When - ?�원가???�행
        Buyer savedBuyer = buyerSVC.join(buyer);

        // Then - 종합 검�?        // 1. 기본 ?�보 검�?        assertEquals(buyer.getName(), savedBuyer.getName());
        assertEquals(buyer.getEmail(), savedBuyer.getEmail());
        assertEquals(buyer.getTel(), savedBuyer.getTel());

        // 2. ?�스???�정 검�?        assertEquals(MemberGubun.BRONZE.getCode(), savedBuyer.getMemberGubun());
        assertEquals(MemberStatus.ACTIVE.getCode(), savedBuyer.getMemberStatus());

        // 3. 로그??가???�태 검�?        assertTrue(buyerSVC.canLogin(savedBuyer));
        assertFalse(buyerSVC.isWithdrawn(savedBuyer));

        // 4. ?�비???�용 가???�태 검�?        assertTrue(buyerSVC.canWithdraw(savedBuyer.getId()));

        // 5. ?�이?�베?�스 ?��???검�?        Optional<Buyer> dbBuyer = buyerDAO.findByEmail(buyer.getEmail());
        assertTrue(dbBuyer.isPresent());
        assertEquals(savedBuyer.getId(), dbBuyer.get().getId());
    }
} 
