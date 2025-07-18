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
 * 구매??조회/?�정/?�퇴 ?�합 ?�스???�나리오
 * ?�제 ?�이?�베?�스?� 모든 계층???�합???�경?�서 ?�스?? */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("구매??조회/?�정/?�퇴 ?�합 ?�스??)
class BuyerCrudIntegrationTest {

    @Autowired
    private BuyerSVC buyerSVC;

    @Autowired
    private BuyerDAO buyerDAO;

    private Buyer testBuyer;

    @BeforeEach
    void setUp() {
        // ?�스???�이???�리
        buyerDAO.deleteAll();
        
        // ?�스?�용 구매???�성
        testBuyer = Buyer.builder()
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
        
        testBuyer = buyerSVC.join(testBuyer);
    }

    @Test
    @DisplayName("?�합 ?�스??1: ?�원 ?�보 조회 - ID�?조회")
    void integrationTest_findById() {
        // When - ID�??�원 조회
        Optional<Buyer> foundBuyer = buyerSVC.findById(testBuyer.getId());

        // Then - 조회 ?�공 검�?        assertTrue(foundBuyer.isPresent());
        assertEquals(testBuyer.getId(), foundBuyer.get().getId());
        assertEquals(testBuyer.getEmail(), foundBuyer.get().getEmail());
        assertEquals(testBuyer.getNickname(), foundBuyer.get().getNickname());
    }

    @Test
    @DisplayName("?�합 ?�스??2: ?�원 ?�보 조회 - ?�메?�로 조회")
    void integrationTest_findByEmail() {
        // When - ?�메?�로 ?�원 조회
        Optional<Buyer> foundBuyer = buyerSVC.findByEmail(testBuyer.getEmail());

        // Then - 조회 ?�공 검�?        assertTrue(foundBuyer.isPresent());
        assertEquals(testBuyer.getEmail(), foundBuyer.get().getEmail());
        assertEquals(testBuyer.getId(), foundBuyer.get().getId());
    }

    @Test
    @DisplayName("?�합 ?�스??3: ?�원 ?�보 ?�정 - 기본 ?�보 ?�정")
    void integrationTest_updateBasicInfo() {
        // Given - ?�정???�보
        Buyer updateBuyer = Buyer.builder()
                .name("김구매?�수??)
                .nickname("modifiedBuyer")
                .tel("010-9999-8888")
                .gender("?�성")
                .birth(LocalDate.of(1995, 5, 15))
                .postcode("54321")
                .address("부?�시 ?�운?��??�정�?456")
                .detailAddress("202??)
                .build();

        // When - ?�보 ?�정 ?�행
        int updateCount = buyerSVC.update(testBuyer.getId(), updateBuyer);

        // Then - ?�정 ?�공 검�?        assertEquals(1, updateCount);

        // ?�정???�보 조회 검�?        Optional<Buyer> updatedBuyer = buyerSVC.findById(testBuyer.getId());
        assertTrue(updatedBuyer.isPresent());
        assertEquals("김구매?�수??, updatedBuyer.get().getName());
        assertEquals("modifiedBuyer", updatedBuyer.get().getNickname());
        assertEquals("010-9999-8888", updatedBuyer.get().getTel());
        assertEquals("?�성", updatedBuyer.get().getGender());
        assertEquals("부?�시 ?�운?��??�정�?456", updatedBuyer.get().getAddress());
    }

    @Test
    @DisplayName("?�합 ?�스??4: ?�원 ?�보 ?�정 - ?�네??중복 ???�패")
    void integrationTest_updateNicknameDuplicateFailure() {
        // Given - ?�른 ?�원 ?�성
        Buyer anotherBuyer = Buyer.builder()
                .name("?�른구매??)
                .nickname("anotherBuyer")
                .email("another@test.com")
                .password("password123")
                .tel("010-1111-2222")
                .memberGubun(MemberGubun.BRONZE.getCode())
                .memberStatus(MemberStatus.ACTIVE.getCode())
                .build();
        buyerSVC.join(anotherBuyer);

        // When & Then - 기존 ?�네?�으�??�정 ?�도
        Buyer updateBuyer = Buyer.builder()
                .nickname("anotherBuyer")
                .build();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            buyerSVC.update(testBuyer.getId(), updateBuyer);
        });

        assertEquals("?��? ?�용 중인 ?�네?�입?�다.", exception.getMessage());
    }

    @Test
    @DisplayName("?�합 ?�스??5: 비�?번호 ?�인 검�?)
    void integrationTest_passwordVerification() {
        // When & Then - ?�바�?비�?번호 ?�인
        assertTrue(buyerSVC.checkPassword(testBuyer.getId(), "password123"));

        // When & Then - ?�못??비�?번호 ?�인
        assertFalse(buyerSVC.checkPassword(testBuyer.getId(), "wrongPassword"));
    }

    @Test
    @DisplayName("?�합 ?�스??6: ?�원 ?�퇴 - ?�상 ?�퇴")
    void integrationTest_withdrawSuccess() {
        // Given - ?�퇴 가?�한 ?�태 ?�인
        assertTrue(buyerSVC.canWithdraw(testBuyer.getId()));

        // When - ?�원 ?�퇴 ?�행
        int withdrawResult = buyerSVC.withdraw(testBuyer.getId(), "개인?�유");

        // Then - ?�퇴 ?�공 검�?        assertEquals(1, withdrawResult);

        // ?�퇴 ???�태 검�?        Optional<Buyer> withdrawnBuyer = buyerSVC.findById(testBuyer.getId());
        assertTrue(withdrawnBuyer.isPresent());
        assertEquals(MemberStatus.WITHDRAWN.getCode(), withdrawnBuyer.get().getMemberStatus());
        assertTrue(buyerSVC.isWithdrawn(withdrawnBuyer.get()));
        assertFalse(buyerSVC.canLogin(withdrawnBuyer.get()));
    }

    @Test
    @DisplayName("?�합 ?�스??7: ?�퇴 ??로그??차단")
    void integrationTest_loginBlockedAfterWithdraw() {
        // Given - ?�원 ?�퇴
        buyerSVC.withdraw(testBuyer.getId(), "개인?�유");

        // When & Then - ?�퇴???�원 로그???�도
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            buyerSVC.login(testBuyer.getEmail(), testBuyer.getPassword());
        });

        assertEquals("?�퇴???�원?�니??", exception.getMessage());
    }

    @Test
    @DisplayName("?�합 ?�스??8: ?�퇴 ???�활?�화")
    void integrationTest_reactivateAfterWithdraw() {
        // Given - ?�원 ?�퇴
        buyerSVC.withdraw(testBuyer.getId(), "개인?�유");

        // When - ?�활?�화 ?�도
        Optional<Buyer> reactivatedBuyer = buyerSVC.reactivate(testBuyer.getEmail(), testBuyer.getPassword());

        // Then - ?�활?�화 ?�공 검�?        assertTrue(reactivatedBuyer.isPresent());
        assertEquals(MemberStatus.ACTIVE.getCode(), reactivatedBuyer.get().getMemberStatus());
        assertTrue(buyerSVC.canLogin(reactivatedBuyer.get()));
        assertFalse(buyerSVC.isWithdrawn(reactivatedBuyer.get()));
    }

    @Test
    @DisplayName("?�합 ?�스??9: ?�비???�용 ?�황 조회")
    void integrationTest_serviceUsageInfo() {
        // When - ?�비???�용 ?�황 조회
        Map<String, Object> serviceUsage = buyerSVC.getServiceUsage(testBuyer.getId());

        // Then - ?�용 ?�황 검�?        assertNotNull(serviceUsage);
        assertTrue(serviceUsage.containsKey("canWithdraw"));
        assertTrue(serviceUsage.containsKey("orderCount"));
        assertTrue(serviceUsage.containsKey("pointBalance"));
        assertTrue(serviceUsage.containsKey("disputeCount"));
        assertTrue(serviceUsage.containsKey("refundCount"));

        // 초기 ?�태 검�?        assertTrue((Boolean) serviceUsage.get("canWithdraw"));
        assertEquals(0, serviceUsage.get("orderCount"));
        assertEquals(0, serviceUsage.get("pointBalance"));
        assertEquals(0, serviceUsage.get("disputeCount"));
        assertEquals(0, serviceUsage.get("refundCount"));
    }

    @Test
    @DisplayName("?�합 ?�스??10: ?�원 ?�급 ?�보 조회")
    void integrationTest_memberGradeInfo() {
        // When - ?�원 ?�급 ?�보 조회
        var gubunInfo = buyerSVC.getGubunInfo(testBuyer);
        var statusInfo = buyerSVC.getStatusInfo(testBuyer);

        // Then - ?�급 ?�보 검�?        assertNotNull(gubunInfo);
        assertEquals(MemberGubun.BRONZE.getCode(), gubunInfo.getCode());
        assertEquals(MemberGubun.BRONZE.getDescription(), gubunInfo.getName());

        assertNotNull(statusInfo);
        assertEquals(MemberStatus.ACTIVE.getCode(), statusInfo.getCode());
        assertEquals(MemberStatus.ACTIVE.getCode(), statusInfo.getName());
    }

    @Test
    @DisplayName("?�합 ?�스??11: ?�체 CRUD ?�나리오 종합 검�?)
    void integrationTest_completeCrudScenario() {
        // 1. 조회 (Read)
        Optional<Buyer> foundBuyer = buyerSVC.findById(testBuyer.getId());
        assertTrue(foundBuyer.isPresent());

        // 2. ?�정 (Update)
        Buyer updateBuyer = Buyer.builder()
                .name("?�정?�이�?)
                .nickname("modifiedNickname")
                .tel("010-9999-9999")
                .build();
        
        int updateResult = buyerSVC.update(testBuyer.getId(), updateBuyer);
        assertEquals(1, updateResult);

        // 3. ?�정 ?�인
        Optional<Buyer> updatedBuyer = buyerSVC.findById(testBuyer.getId());
        assertTrue(updatedBuyer.isPresent());
        assertEquals("?�정?�이�?, updatedBuyer.get().getName());
        assertEquals("modifiedNickname", updatedBuyer.get().getNickname());

        // 4. ?�퇴 (Delete - ?�리????��)
        int withdrawResult = buyerSVC.withdraw(testBuyer.getId(), "?�스???�료");
        assertEquals(1, withdrawResult);

        // 5. ?�퇴 ?�인
        Optional<Buyer> withdrawnBuyer = buyerSVC.findById(testBuyer.getId());
        assertTrue(withdrawnBuyer.isPresent());
        assertTrue(buyerSVC.isWithdrawn(withdrawnBuyer.get()));

        // 6. ?�활?�화
        Optional<Buyer> reactivatedBuyer = buyerSVC.reactivate(testBuyer.getEmail(), testBuyer.getPassword());
        assertTrue(reactivatedBuyer.isPresent());
        assertFalse(buyerSVC.isWithdrawn(reactivatedBuyer.get()));

        // 7. 최종 ?�태 검�?        assertTrue(buyerSVC.canLogin(reactivatedBuyer.get()));
        assertEquals(MemberStatus.ACTIVE.getCode(), reactivatedBuyer.get().getMemberStatus());
    }
} 
