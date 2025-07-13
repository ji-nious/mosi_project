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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 구매자 회원가입 통합 테스트 시나리오
 * 실제 데이터베이스와 모든 계층이 통합된 환경에서 테스트
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("구매자 회원가입 통합 테스트")
class BuyerIntegrationTest {

    @Autowired
    private BuyerSVC buyerSVC;

    @Autowired
    private BuyerDAO buyerDAO;

    private Buyer createValidBuyer() {
        return Buyer.builder()
                .name("김구매자")
                .nickname("buyer123")
                .email("buyer@test.com")
                .password("password123")
                .tel("010-1234-5678")
                .gender("남성")
                .birth(LocalDate.of(1990, 1, 1))
                .postcode("12345")
                .address("서울시 강남구 테스트로 123")
                .detailAddress("101호")
                .memberGubun(MemberGubun.BRONZE.getCode())
                .memberStatus(MemberStatus.ACTIVE.getCode())
                .build();
    }

    @BeforeEach
    void setUp() {
        // 테스트 데이터 정리
        buyerDAO.deleteAll();
    }

    @Test
    @DisplayName("통합 테스트 1: 정상 회원가입 플로우")
    void integrationTest_normalSignupFlow() {
        // Given - 유효한 구매자 데이터
        Buyer buyer = createValidBuyer();

        // When - 회원가입 실행
        Buyer savedBuyer = buyerSVC.join(buyer);

        // Then - 회원가입 성공 검증
        assertNotNull(savedBuyer);
        assertNotNull(savedBuyer.getId());
        assertEquals(buyer.getEmail(), savedBuyer.getEmail());
        assertEquals(buyer.getNickname(), savedBuyer.getNickname());
        assertEquals(MemberStatus.ACTIVE.getCode(), savedBuyer.getMemberStatus());

        // 데이터베이스 저장 검증
        Optional<Buyer> foundBuyer = buyerDAO.findByEmail(buyer.getEmail());
        assertTrue(foundBuyer.isPresent());
        assertEquals(buyer.getEmail(), foundBuyer.get().getEmail());
    }

    @Test
    @DisplayName("통합 테스트 2: 이메일 중복 시 회원가입 실패")
    void integrationTest_emailDuplicateFailure() {
        // Given - 첫 번째 회원가입
        Buyer firstBuyer = createValidBuyer();
        buyerSVC.join(firstBuyer);

        // When & Then - 같은 이메일로 두 번째 회원가입 시도
        Buyer secondBuyer = createValidBuyer();
        secondBuyer.setNickname("differentNickname");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            buyerSVC.join(secondBuyer);
        });

        assertEquals("이미 가입된 이메일입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("통합 테스트 3: 닉네임 중복 시 회원가입 실패")
    void integrationTest_nicknameDuplicateFailure() {
        // Given - 첫 번째 회원가입
        Buyer firstBuyer = createValidBuyer();
        buyerSVC.join(firstBuyer);

        // When & Then - 같은 닉네임으로 두 번째 회원가입 시도
        Buyer secondBuyer = createValidBuyer();
        secondBuyer.setEmail("different@test.com");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            buyerSVC.join(secondBuyer);
        });

        assertEquals("이미 사용 중인 닉네임입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("통합 테스트 4: 탈퇴한 회원 재가입 성공")
    void integrationTest_withdrawnMemberRejoinSuccess() {
        // Given - 회원가입 후 탈퇴
        Buyer buyer = createValidBuyer();
        Buyer savedBuyer = buyerSVC.join(buyer);
        buyerSVC.withdraw(savedBuyer.getId(), "개인사유");

        // When - 동일한 이메일로 재가입 시도
        Buyer rejoinBuyer = createValidBuyer();
        rejoinBuyer.setNickname("newNickname"); // 닉네임 변경

        // Then - 재가입 성공
        Buyer rejoinedBuyer = buyerSVC.join(rejoinBuyer);
        assertNotNull(rejoinedBuyer);
        assertEquals(buyer.getEmail(), rejoinedBuyer.getEmail());
        assertEquals("newNickname", rejoinedBuyer.getNickname());
        assertEquals(MemberStatus.ACTIVE.getCode(), rejoinedBuyer.getMemberStatus());
    }

    @Test
    @DisplayName("통합 테스트 5: 회원가입 후 즉시 로그인 성공")
    void integrationTest_signupThenLoginSuccess() {
        // Given - 회원가입
        Buyer buyer = createValidBuyer();
        buyerSVC.join(buyer);

        // When - 로그인 시도
        Buyer loginBuyer = buyerSVC.login(buyer.getEmail(), buyer.getPassword());

        // Then - 로그인 성공
        assertNotNull(loginBuyer);
        assertEquals(buyer.getEmail(), loginBuyer.getEmail());
        assertTrue(buyerSVC.canLogin(loginBuyer));
    }

    @Test
    @DisplayName("통합 테스트 6: 회원가입 후 중복 체크 기능 검증")
    void integrationTest_duplicateCheckAfterSignup() {
        // Given - 회원가입
        Buyer buyer = createValidBuyer();
        buyerSVC.join(buyer);

        // When & Then - 중복 체크 검증
        assertTrue(buyerSVC.existsByEmail(buyer.getEmail()));
        assertTrue(buyerSVC.existsByNickname(buyer.getNickname()));
        assertFalse(buyerSVC.existsByEmail("nonexistent@test.com"));
        assertFalse(buyerSVC.existsByNickname("nonexistentNickname"));
    }

    @Test
    @DisplayName("통합 테스트 7: 회원가입 후 초기 상태 검증")
    void integrationTest_initialStateAfterSignup() {
        // Given - 회원가입
        Buyer buyer = createValidBuyer();
        Buyer savedBuyer = buyerSVC.join(buyer);

        // When - 초기 상태 조회
        Map<String, Object> serviceUsage = buyerSVC.getServiceUsage(savedBuyer.getId());

        // Then - 초기 상태 검증
        assertTrue((Boolean) serviceUsage.get("canWithdraw"));
        assertEquals(0, serviceUsage.get("orderCount"));
        assertEquals(0, serviceUsage.get("pointBalance"));
        assertEquals(0, serviceUsage.get("disputeCount"));
    }

    @Test
    @DisplayName("통합 테스트 8: 잘못된 데이터로 회원가입 시 예외 발생")
    void integrationTest_invalidDataSignupFailure() {
        // Given - 잘못된 이메일 형식
        Buyer invalidBuyer = createValidBuyer();
        invalidBuyer.setEmail("invalid-email");

        // When & Then - 데이터 유효성 검증 실패
        assertThrows(Exception.class, () -> {
            buyerSVC.join(invalidBuyer);
        });
    }

    @Test
    @DisplayName("통합 테스트 9: 전체 회원가입 프로세스 종합 검증")
    void integrationTest_completeSignupProcessValidation() {
        // Given - 유효한 구매자 데이터
        Buyer buyer = createValidBuyer();

        // When - 회원가입 실행
        Buyer savedBuyer = buyerSVC.join(buyer);

        // Then - 종합 검증
        // 1. 기본 정보 검증
        assertEquals(buyer.getName(), savedBuyer.getName());
        assertEquals(buyer.getEmail(), savedBuyer.getEmail());
        assertEquals(buyer.getTel(), savedBuyer.getTel());

        // 2. 시스템 설정 검증
        assertEquals(MemberGubun.BRONZE.getCode(), savedBuyer.getMemberGubun());
        assertEquals(MemberStatus.ACTIVE.getCode(), savedBuyer.getMemberStatus());

        // 3. 로그인 가능 상태 검증
        assertTrue(buyerSVC.canLogin(savedBuyer));
        assertFalse(buyerSVC.isWithdrawn(savedBuyer));

        // 4. 서비스 이용 가능 상태 검증
        assertTrue(buyerSVC.canWithdraw(savedBuyer.getId()));

        // 5. 데이터베이스 일관성 검증
        Optional<Buyer> dbBuyer = buyerDAO.findByEmail(buyer.getEmail());
        assertTrue(dbBuyer.isPresent());
        assertEquals(savedBuyer.getId(), dbBuyer.get().getId());
    }
} 