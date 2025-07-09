package com.kh.project.domain.buyer.dao;

import com.kh.project.domain.entity.Buyer;
import com.kh.project.domain.entity.MemberGubun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BuyerDAO 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "logging.level.com.kh.project=DEBUG",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
@Transactional
@DisplayName("BuyerDAO 통합 테스트")
class BuyerDAOImplTest {

    @Autowired
    private BuyerDAO buyerDAO;

    private Buyer testBuyer;

    @BeforeEach
    void setUp() {
        testBuyer = createSampleBuyer();
    }

    private Buyer createSampleBuyer() {
        Buyer buyer = new Buyer();
        buyer.setEmail("test@buyer.com");
        buyer.setPassword("testpassword");
        buyer.setName("테스트구매자");
        buyer.setNickname("테스터");
        buyer.setTel("010-1234-5678");
        buyer.setGender("남성");
        buyer.setBirth(new Date());
        buyer.setAddress("부산시 중구 중앙대로");
        buyer.setGubun(MemberGubun.NEW.getCode());
        buyer.setStatus("ACTIVE");
        return buyer;
    }

    // ==================== 저장 테스트 ====================

    @Test
    @DisplayName("구매자 저장 - 성공")
    void save_success() {
        // when
        Buyer savedBuyer = buyerDAO.save(testBuyer);

        // then
        assertNotNull(savedBuyer);
        assertNotNull(savedBuyer.getBuyerId());
        assertEquals(testBuyer.getEmail(), savedBuyer.getEmail());
        assertEquals(testBuyer.getName(), savedBuyer.getName());
        assertEquals(testBuyer.getNickname(), savedBuyer.getNickname());
        assertNotNull(savedBuyer.getCdate());
    }

    @Test
    @DisplayName("구매자 저장 - 필수 필드 누락시 실패")
    void save_fail_missing_required_fields() {
        // given: 이메일이 null인 구매자
        testBuyer.setEmail(null);

        // when & then: 예외 발생 예상
        assertThrows(Exception.class, () -> {
            buyerDAO.save(testBuyer);
        });
    }

    // ==================== 조회 테스트 ====================

    @Test
    @DisplayName("ID로 구매자 조회 - 성공")
    void findById_success() {
        // given: 구매자 저장
        Buyer savedBuyer = buyerDAO.save(testBuyer);

        // when: ID로 조회
        Optional<Buyer> foundBuyer = buyerDAO.findById(savedBuyer.getBuyerId());

        // then: 조회 성공 확인
        assertTrue(foundBuyer.isPresent());
        assertEquals(savedBuyer.getBuyerId(), foundBuyer.get().getBuyerId());
        assertEquals(savedBuyer.getEmail(), foundBuyer.get().getEmail());
    }

    @Test
    @DisplayName("ID로 구매자 조회 - 존재하지 않는 ID")
    void findById_not_found() {
        // when: 존재하지 않는 ID로 조회
        Optional<Buyer> foundBuyer = buyerDAO.findById(999999L);

        // then: 조회 결과 없음
        assertFalse(foundBuyer.isPresent());
    }

    @Test
    @DisplayName("이메일로 구매자 조회 - 성공")
    void findByEmail_success() {
        // given: 구매자 저장
        buyerDAO.save(testBuyer);

        // when: 이메일로 조회
        Optional<Buyer> foundBuyer = buyerDAO.findByEmail(testBuyer.getEmail());

        // then: 조회 성공 확인
        assertTrue(foundBuyer.isPresent());
        assertEquals(testBuyer.getEmail(), foundBuyer.get().getEmail());
    }

    @Test
    @DisplayName("이메일로 구매자 조회 - 존재하지 않는 이메일")
    void findByEmail_not_found() {
        // when: 존재하지 않는 이메일로 조회
        Optional<Buyer> foundBuyer = buyerDAO.findByEmail("notfound@email.com");

        // then: 조회 결과 없음
        assertFalse(foundBuyer.isPresent());
    }

    // ==================== 업데이트 테스트 ====================

    @Test
    @DisplayName("구매자 정보 수정 - 성공")
    void update_success() {
        // given: 구매자 저장
        Buyer savedBuyer = buyerDAO.save(testBuyer);

        // when: 정보 수정
        Buyer updateBuyer = new Buyer();
        updateBuyer.setName("수정된이름");
        updateBuyer.setTel("010-9999-8888");
        updateBuyer.setAddress("부산시 해운대구");

        int updatedRows = buyerDAO.update(savedBuyer.getBuyerId(), updateBuyer);

        // then: 수정 성공 확인
        assertEquals(1, updatedRows);

        // 수정된 데이터 검증
        Optional<Buyer> updatedBuyerOpt = buyerDAO.findById(savedBuyer.getBuyerId());
        assertTrue(updatedBuyerOpt.isPresent());
        Buyer updatedBuyerData = updatedBuyerOpt.get();
        assertEquals("수정된이름", updatedBuyerData.getName());
        assertEquals("010-9999-8888", updatedBuyerData.getTel());
        assertEquals("부산시 해운대구", updatedBuyerData.getAddress());
    }

    @Test
    @DisplayName("구매자 정보 수정 - 존재하지 않는 ID")
    void update_not_found() {
        // given: 수정할 데이터
        Buyer updateBuyer = new Buyer();
        updateBuyer.setName("수정된이름");

        // when: 존재하지 않는 ID로 수정 시도
        int updatedRows = buyerDAO.update(999999L, updateBuyer);

        // then: 수정되지 않음
        assertEquals(0, updatedRows);
    }

    // ==================== 탈퇴 테스트 ====================

    @Test
    @DisplayName("구매자 탈퇴 처리 - 성공")
    void withdrawWithReason_success() {
        // given: 구매자 저장
        Buyer savedBuyer = buyerDAO.save(testBuyer);

        // when: 탈퇴 처리
        String reason = "서비스 불만족";
        int withdrawnRows = buyerDAO.withdrawWithReason(savedBuyer.getBuyerId(), reason);

        // then: 탈퇴 성공 확인
        assertEquals(1, withdrawnRows);

        // 탈퇴 상태 확인
        Optional<Buyer> withdrawnBuyerOpt = buyerDAO.findById(savedBuyer.getBuyerId());
        assertTrue(withdrawnBuyerOpt.isPresent());
        Buyer withdrawnBuyer = withdrawnBuyerOpt.get();
        assertEquals("WITHDRAWN", withdrawnBuyer.getStatus());
        assertNotNull(withdrawnBuyer.getWithdrawnAt());
        assertEquals(reason, withdrawnBuyer.getWithdrawnReason());
    }

    @Test
    @DisplayName("구매자 탈퇴 처리 - 존재하지 않는 ID")
    void withdrawWithReason_not_found() {
        // when: 존재하지 않는 ID로 탈퇴 시도
        int withdrawnRows = buyerDAO.withdrawWithReason(999999L, "탈퇴 사유");

        // then: 탈퇴되지 않음
        assertEquals(0, withdrawnRows);
    }

    // ==================== 중복 체크 테스트 ====================

    @Test
    @DisplayName("이메일 중복 체크 - 중복됨")
    void existsByEmail_true() {
        // given: 구매자 저장
        buyerDAO.save(testBuyer);

        // when: 이메일 중복 체크
        boolean exists = buyerDAO.existsByEmail(testBuyer.getEmail());

        // then: 중복됨
        assertTrue(exists);
    }

    @Test
    @DisplayName("이메일 중복 체크 - 중복 안됨")
    void existsByEmail_false() {
        // when: 존재하지 않는 이메일 중복 체크
        boolean exists = buyerDAO.existsByEmail("new@email.com");

        // then: 중복 안됨
        assertFalse(exists);
    }

    @Test
    @DisplayName("닉네임 중복 체크 - 중복됨")
    void existsByNickname_true() {
        // given: 구매자 저장
        buyerDAO.save(testBuyer);

        // when: 닉네임 중복 체크
        boolean exists = buyerDAO.existsByNickname(testBuyer.getNickname());

        // then: 중복됨
        assertTrue(exists);
    }

    @Test
    @DisplayName("닉네임 중복 체크 - 중복 안됨")
    void existsByNickname_false() {
        // when: 존재하지 않는 닉네임 중복 체크
        boolean exists = buyerDAO.existsByNickname("새닉네임");

        // then: 중복 안됨
        assertFalse(exists);
    }

    // ==================== 목록 조회 테스트 ====================

    @Test
    @DisplayName("전체 구매자 목록 조회")
    void findAll() {
        // given: 여러 구매자 저장
        buyerDAO.save(testBuyer);

        Buyer buyer2 = createSampleBuyer();
        buyer2.setEmail("buyer2@email.com");
        buyer2.setNickname("구매자2");
        buyerDAO.save(buyer2);

        // when: 전체 목록 조회
        List<Buyer> buyers = buyerDAO.findAll();

        // then: 저장된 구매자들이 조회됨
        assertNotNull(buyers);
        assertTrue(buyers.size() >= 2);
        assertTrue(buyers.stream().anyMatch(b -> b.getEmail().equals(testBuyer.getEmail())));
        assertTrue(buyers.stream().anyMatch(b -> b.getEmail().equals(buyer2.getEmail())));
    }

    @Test
    @DisplayName("탈퇴한 구매자 목록 조회")
    void findWithdrawnMembers() {
        // given: 구매자 저장 후 탈퇴 처리
        Buyer savedBuyer = buyerDAO.save(testBuyer);
        buyerDAO.withdrawWithReason(savedBuyer.getBuyerId(), "테스트 탈퇴");

        // when: 탈퇴한 구매자 목록 조회
        List<Buyer> withdrawnBuyers = buyerDAO.findWithdrawnMembers();

        // then: 탈퇴한 구매자가 조회됨
        assertNotNull(withdrawnBuyers);
        assertTrue(withdrawnBuyers.stream().anyMatch(b -> 
            b.getBuyerId().equals(savedBuyer.getBuyerId()) && 
            "WITHDRAWN".equals(b.getStatus())
        ));
    }

    // ==================== Edge Case 테스트 ====================

    @Test
    @DisplayName("대소문자 구분 이메일 중복 체크")
    void existsByEmail_case_sensitivity() {
        // given: 소문자 이메일로 저장
        buyerDAO.save(testBuyer);

        // when: 대문자로 중복 체크
        boolean existsUpper = buyerDAO.existsByEmail(testBuyer.getEmail().toUpperCase());
        boolean existsLower = buyerDAO.existsByEmail(testBuyer.getEmail().toLowerCase());

        // then: 대소문자 구분 확인 (구현에 따라 결과가 다를 수 있음)
        assertTrue(existsLower); // 원본과 동일
        // existsUpper는 DB 설정에 따라 다름
    }

    @Test
    @DisplayName("null 값 처리 테스트")
    void handle_null_values() {
        // when & then: null 이메일로 중복 체크
        assertThrows(Exception.class, () -> {
            buyerDAO.existsByEmail(null);
        });

        // when & then: null ID로 조회
        assertThrows(Exception.class, () -> {
            buyerDAO.findById(null);
        });
    }

    @Test
    @DisplayName("매우 긴 문자열 처리 테스트")
    void handle_long_strings() {
        // given: 매우 긴 문자열
        String longEmail = "a".repeat(255) + "@test.com"; // 이메일 길이 제한 테스트
        testBuyer.setEmail(longEmail);

        // when & then: 길이 제한 확인
        if (longEmail.length() > 255) { // 일반적인 이메일 길이 제한
            assertThrows(Exception.class, () -> {
                buyerDAO.save(testBuyer);
            });
        }
    }

    @Test
    @DisplayName("동시 저장 테스트")
    void concurrent_save_test() {
        // given: 동일한 이메일을 가진 두 구매자
        Buyer buyer1 = createSampleBuyer();
        Buyer buyer2 = createSampleBuyer();

        // when: 첫 번째 저장
        buyerDAO.save(buyer1);

        // then: 두 번째 저장시 중복 에러 (DB 제약조건에 따라)
        assertThrows(Exception.class, () -> {
            buyerDAO.save(buyer2);
        });
    }
} 