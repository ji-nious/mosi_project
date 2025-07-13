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
 * 구매자 조회/수정/탈퇴 통합 테스트 시나리오
 * 실제 데이터베이스와 모든 계층이 통합된 환경에서 테스트
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("구매자 조회/수정/탈퇴 통합 테스트")
class BuyerCrudIntegrationTest {

    @Autowired
    private BuyerSVC buyerSVC;

    @Autowired
    private BuyerDAO buyerDAO;

    private Buyer testBuyer;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 정리
        buyerDAO.deleteAll();
        
        // 테스트용 구매자 생성
        testBuyer = Buyer.builder()
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
        
        testBuyer = buyerSVC.join(testBuyer);
    }

    @Test
    @DisplayName("통합 테스트 1: 회원 정보 조회 - ID로 조회")
    void integrationTest_findById() {
        // When - ID로 회원 조회
        Optional<Buyer> foundBuyer = buyerSVC.findById(testBuyer.getId());

        // Then - 조회 성공 검증
        assertTrue(foundBuyer.isPresent());
        assertEquals(testBuyer.getId(), foundBuyer.get().getId());
        assertEquals(testBuyer.getEmail(), foundBuyer.get().getEmail());
        assertEquals(testBuyer.getNickname(), foundBuyer.get().getNickname());
    }

    @Test
    @DisplayName("통합 테스트 2: 회원 정보 조회 - 이메일로 조회")
    void integrationTest_findByEmail() {
        // When - 이메일로 회원 조회
        Optional<Buyer> foundBuyer = buyerSVC.findByEmail(testBuyer.getEmail());

        // Then - 조회 성공 검증
        assertTrue(foundBuyer.isPresent());
        assertEquals(testBuyer.getEmail(), foundBuyer.get().getEmail());
        assertEquals(testBuyer.getId(), foundBuyer.get().getId());
    }

    @Test
    @DisplayName("통합 테스트 3: 회원 정보 수정 - 기본 정보 수정")
    void integrationTest_updateBasicInfo() {
        // Given - 수정할 정보
        Buyer updateBuyer = Buyer.builder()
                .name("김구매자수정")
                .nickname("modifiedBuyer")
                .tel("010-9999-8888")
                .gender("여성")
                .birth(LocalDate.of(1995, 5, 15))
                .postcode("54321")
                .address("부산시 해운대구 수정로 456")
                .detailAddress("202호")
                .build();

        // When - 정보 수정 실행
        int updateCount = buyerSVC.update(testBuyer.getId(), updateBuyer);

        // Then - 수정 성공 검증
        assertEquals(1, updateCount);

        // 수정된 정보 조회 검증
        Optional<Buyer> updatedBuyer = buyerSVC.findById(testBuyer.getId());
        assertTrue(updatedBuyer.isPresent());
        assertEquals("김구매자수정", updatedBuyer.get().getName());
        assertEquals("modifiedBuyer", updatedBuyer.get().getNickname());
        assertEquals("010-9999-8888", updatedBuyer.get().getTel());
        assertEquals("여성", updatedBuyer.get().getGender());
        assertEquals("부산시 해운대구 수정로 456", updatedBuyer.get().getAddress());
    }

    @Test
    @DisplayName("통합 테스트 4: 회원 정보 수정 - 닉네임 중복 시 실패")
    void integrationTest_updateNicknameDuplicateFailure() {
        // Given - 다른 회원 생성
        Buyer anotherBuyer = Buyer.builder()
                .name("다른구매자")
                .nickname("anotherBuyer")
                .email("another@test.com")
                .password("password123")
                .tel("010-1111-2222")
                .memberGubun(MemberGubun.BRONZE.getCode())
                .memberStatus(MemberStatus.ACTIVE.getCode())
                .build();
        buyerSVC.join(anotherBuyer);

        // When & Then - 기존 닉네임으로 수정 시도
        Buyer updateBuyer = Buyer.builder()
                .nickname("anotherBuyer")
                .build();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            buyerSVC.update(testBuyer.getId(), updateBuyer);
        });

        assertEquals("이미 사용 중인 닉네임입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("통합 테스트 5: 비밀번호 확인 검증")
    void integrationTest_passwordVerification() {
        // When & Then - 올바른 비밀번호 확인
        assertTrue(buyerSVC.checkPassword(testBuyer.getId(), "password123"));

        // When & Then - 잘못된 비밀번호 확인
        assertFalse(buyerSVC.checkPassword(testBuyer.getId(), "wrongPassword"));
    }

    @Test
    @DisplayName("통합 테스트 6: 회원 탈퇴 - 정상 탈퇴")
    void integrationTest_withdrawSuccess() {
        // Given - 탈퇴 가능한 상태 확인
        assertTrue(buyerSVC.canWithdraw(testBuyer.getId()));

        // When - 회원 탈퇴 실행
        int withdrawResult = buyerSVC.withdraw(testBuyer.getId(), "개인사유");

        // Then - 탈퇴 성공 검증
        assertEquals(1, withdrawResult);

        // 탈퇴 후 상태 검증
        Optional<Buyer> withdrawnBuyer = buyerSVC.findById(testBuyer.getId());
        assertTrue(withdrawnBuyer.isPresent());
        assertEquals(MemberStatus.WITHDRAWN.getCode(), withdrawnBuyer.get().getMemberStatus());
        assertTrue(buyerSVC.isWithdrawn(withdrawnBuyer.get()));
        assertFalse(buyerSVC.canLogin(withdrawnBuyer.get()));
    }

    @Test
    @DisplayName("통합 테스트 7: 탈퇴 후 로그인 차단")
    void integrationTest_loginBlockedAfterWithdraw() {
        // Given - 회원 탈퇴
        buyerSVC.withdraw(testBuyer.getId(), "개인사유");

        // When & Then - 탈퇴한 회원 로그인 시도
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            buyerSVC.login(testBuyer.getEmail(), testBuyer.getPassword());
        });

        assertEquals("탈퇴한 회원입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("통합 테스트 8: 탈퇴 후 재활성화")
    void integrationTest_reactivateAfterWithdraw() {
        // Given - 회원 탈퇴
        buyerSVC.withdraw(testBuyer.getId(), "개인사유");

        // When - 재활성화 시도
        Optional<Buyer> reactivatedBuyer = buyerSVC.reactivate(testBuyer.getEmail(), testBuyer.getPassword());

        // Then - 재활성화 성공 검증
        assertTrue(reactivatedBuyer.isPresent());
        assertEquals(MemberStatus.ACTIVE.getCode(), reactivatedBuyer.get().getMemberStatus());
        assertTrue(buyerSVC.canLogin(reactivatedBuyer.get()));
        assertFalse(buyerSVC.isWithdrawn(reactivatedBuyer.get()));
    }

    @Test
    @DisplayName("통합 테스트 9: 서비스 이용 현황 조회")
    void integrationTest_serviceUsageInfo() {
        // When - 서비스 이용 현황 조회
        Map<String, Object> serviceUsage = buyerSVC.getServiceUsage(testBuyer.getId());

        // Then - 이용 현황 검증
        assertNotNull(serviceUsage);
        assertTrue(serviceUsage.containsKey("canWithdraw"));
        assertTrue(serviceUsage.containsKey("orderCount"));
        assertTrue(serviceUsage.containsKey("pointBalance"));
        assertTrue(serviceUsage.containsKey("disputeCount"));
        assertTrue(serviceUsage.containsKey("refundCount"));

        // 초기 상태 검증
        assertTrue((Boolean) serviceUsage.get("canWithdraw"));
        assertEquals(0, serviceUsage.get("orderCount"));
        assertEquals(0, serviceUsage.get("pointBalance"));
        assertEquals(0, serviceUsage.get("disputeCount"));
        assertEquals(0, serviceUsage.get("refundCount"));
    }

    @Test
    @DisplayName("통합 테스트 10: 회원 등급 정보 조회")
    void integrationTest_memberGradeInfo() {
        // When - 회원 등급 정보 조회
        var gubunInfo = buyerSVC.getGubunInfo(testBuyer);
        var statusInfo = buyerSVC.getStatusInfo(testBuyer);

        // Then - 등급 정보 검증
        assertNotNull(gubunInfo);
        assertEquals(MemberGubun.BRONZE.getCode(), gubunInfo.getCode());
        assertEquals(MemberGubun.BRONZE.getDescription(), gubunInfo.getName());

        assertNotNull(statusInfo);
        assertEquals(MemberStatus.ACTIVE.getCode(), statusInfo.getCode());
        assertEquals(MemberStatus.ACTIVE.getDescription(), statusInfo.getName());
    }

    @Test
    @DisplayName("통합 테스트 11: 전체 CRUD 시나리오 종합 검증")
    void integrationTest_completeCrudScenario() {
        // 1. 조회 (Read)
        Optional<Buyer> foundBuyer = buyerSVC.findById(testBuyer.getId());
        assertTrue(foundBuyer.isPresent());

        // 2. 수정 (Update)
        Buyer updateBuyer = Buyer.builder()
                .name("수정된이름")
                .nickname("modifiedNickname")
                .tel("010-9999-9999")
                .build();
        
        int updateResult = buyerSVC.update(testBuyer.getId(), updateBuyer);
        assertEquals(1, updateResult);

        // 3. 수정 확인
        Optional<Buyer> updatedBuyer = buyerSVC.findById(testBuyer.getId());
        assertTrue(updatedBuyer.isPresent());
        assertEquals("수정된이름", updatedBuyer.get().getName());
        assertEquals("modifiedNickname", updatedBuyer.get().getNickname());

        // 4. 탈퇴 (Delete - 논리적 삭제)
        int withdrawResult = buyerSVC.withdraw(testBuyer.getId(), "테스트 완료");
        assertEquals(1, withdrawResult);

        // 5. 탈퇴 확인
        Optional<Buyer> withdrawnBuyer = buyerSVC.findById(testBuyer.getId());
        assertTrue(withdrawnBuyer.isPresent());
        assertTrue(buyerSVC.isWithdrawn(withdrawnBuyer.get()));

        // 6. 재활성화
        Optional<Buyer> reactivatedBuyer = buyerSVC.reactivate(testBuyer.getEmail(), testBuyer.getPassword());
        assertTrue(reactivatedBuyer.isPresent());
        assertFalse(buyerSVC.isWithdrawn(reactivatedBuyer.get()));

        // 7. 최종 상태 검증
        assertTrue(buyerSVC.canLogin(reactivatedBuyer.get()));
        assertEquals(MemberStatus.ACTIVE.getCode(), reactivatedBuyer.get().getMemberStatus());
    }
} 