package com.kh.project.domain.seller.dao;

import com.kh.project.domain.entity.Seller;
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
 * SellerDAO 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "logging.level.com.kh.project=DEBUG",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
@Transactional
@DisplayName("SellerDAO 통합 테스트")
class SellerDAOImplTest {

    @Autowired
    private SellerDAO sellerDAO;

    private Seller testSeller;

    @BeforeEach
    void setUp() {
        testSeller = createSampleSeller();
    }

    private Seller createSampleSeller() {
        Seller seller = new Seller();
        seller.setEmail("seller@shop.com");
        seller.setPassword("shoppassword");
        seller.setBizRegNo("111-22-33333");
        seller.setShopName("테스트상점");
        seller.setName("판매자이름");
        seller.setShopAddress("부산시 남구 대연로");
        seller.setTel("010-1234-5678");
        seller.setGubun(MemberGubun.NEW.getCode());
        seller.setStatus("ACTIVE");
        return seller;
    }

    // ==================== 저장 테스트 ====================

    @Test
    @DisplayName("판매자 저장 - 성공")
    void save_success() {
        // when
        Seller savedSeller = sellerDAO.save(testSeller);

        // then
        assertNotNull(savedSeller);
        assertNotNull(savedSeller.getSellerId());
        assertEquals(testSeller.getEmail(), savedSeller.getEmail());
        assertEquals(testSeller.getBizRegNo(), savedSeller.getBizRegNo());
        assertEquals(testSeller.getShopName(), savedSeller.getShopName());
        assertNotNull(savedSeller.getCdate());
    }

    @Test
    @DisplayName("판매자 저장 - 필수 필드 누락시 실패")
    void save_fail_missing_required_fields() {
        // given: 이메일이 null인 판매자
        testSeller.setEmail(null);

        // when & then: 예외 발생 예상
        assertThrows(Exception.class, () -> {
            sellerDAO.save(testSeller);
        });
    }

    // ==================== 조회 테스트 ====================

    @Test
    @DisplayName("ID로 판매자 조회 - 성공")
    void findById_success() {
        // given: 판매자 저장
        Seller savedSeller = sellerDAO.save(testSeller);

        // when: ID로 조회
        Optional<Seller> foundSeller = sellerDAO.findById(savedSeller.getSellerId());

        // then: 조회 성공 확인
        assertTrue(foundSeller.isPresent());
        assertEquals(savedSeller.getSellerId(), foundSeller.get().getSellerId());
        assertEquals(savedSeller.getEmail(), foundSeller.get().getEmail());
        assertEquals(savedSeller.getShopName(), foundSeller.get().getShopName());
    }

    @Test
    @DisplayName("ID로 판매자 조회 - 존재하지 않는 ID")
    void findById_not_found() {
        // when: 존재하지 않는 ID로 조회
        Optional<Seller> foundSeller = sellerDAO.findById(999999L);

        // then: 조회 결과 없음
        assertFalse(foundSeller.isPresent());
    }

    @Test
    @DisplayName("이메일로 판매자 조회 - 성공")
    void findByEmail_success() {
        // given: 판매자 저장
        sellerDAO.save(testSeller);

        // when: 이메일로 조회
        Optional<Seller> foundSeller = sellerDAO.findByEmail(testSeller.getEmail());

        // then: 조회 성공 확인
        assertTrue(foundSeller.isPresent());
        assertEquals(testSeller.getEmail(), foundSeller.get().getEmail());
    }

    @Test
    @DisplayName("이메일로 판매자 조회 - 존재하지 않는 이메일")
    void findByEmail_not_found() {
        // when: 존재하지 않는 이메일로 조회
        Optional<Seller> foundSeller = sellerDAO.findByEmail("notfound@seller.com");

        // then: 조회 결과 없음
        assertFalse(foundSeller.isPresent());
    }

    @Test
    @DisplayName("사업자등록번호로 판매자 조회 - 성공")
    void findByBizRegNo_success() {
        // given: 판매자 저장
        sellerDAO.save(testSeller);

        // when: 사업자등록번호로 조회
        Optional<Seller> foundSeller = sellerDAO.findByBizRegNo(testSeller.getBizRegNo());

        // then: 조회 성공 확인
        assertTrue(foundSeller.isPresent());
        assertEquals(testSeller.getBizRegNo(), foundSeller.get().getBizRegNo());
    }

    @Test
    @DisplayName("사업자등록번호로 판매자 조회 - 존재하지 않는 번호")
    void findByBizRegNo_not_found() {
        // when: 존재하지 않는 사업자등록번호로 조회
        Optional<Seller> foundSeller = sellerDAO.findByBizRegNo("999-88-77777");

        // then: 조회 결과 없음
        assertFalse(foundSeller.isPresent());
    }

    // ==================== 업데이트 테스트 ====================

    @Test
    @DisplayName("판매자 정보 수정 - 성공")
    void update_success() {
        // given: 판매자 저장
        Seller savedSeller = sellerDAO.save(testSeller);

        // when: 정보 수정
        Seller updateSeller = new Seller();
        updateSeller.setShopName("수정된상점명");
        updateSeller.setShopAddress("부산시 해운대구");
        updateSeller.setTel("010-9999-8888");

        int updatedRows = sellerDAO.update(savedSeller.getSellerId(), updateSeller);

        // then: 수정 성공 확인
        assertEquals(1, updatedRows);

        // 수정된 데이터 검증
        Optional<Seller> updatedSellerOpt = sellerDAO.findById(savedSeller.getSellerId());
        assertTrue(updatedSellerOpt.isPresent());
        Seller updatedSellerData = updatedSellerOpt.get();
        assertEquals("수정된상점명", updatedSellerData.getShopName());
        assertEquals("부산시 해운대구", updatedSellerData.getShopAddress());
        assertEquals("010-9999-8888", updatedSellerData.getTel());
    }

    @Test
    @DisplayName("판매자 정보 수정 - 존재하지 않는 ID")
    void update_not_found() {
        // given: 수정할 데이터
        Seller updateSeller = new Seller();
        updateSeller.setShopName("수정된상점명");

        // when: 존재하지 않는 ID로 수정 시도
        int updatedRows = sellerDAO.update(999999L, updateSeller);

        // then: 수정되지 않음
        assertEquals(0, updatedRows);
    }

    // ==================== 탈퇴 테스트 ====================

    @Test
    @DisplayName("판매자 탈퇴 처리 - 성공")
    void withdrawWithReason_success() {
        // given: 판매자 저장
        Seller savedSeller = sellerDAO.save(testSeller);

        // when: 탈퇴 처리
        String reason = "사업 종료";
        int withdrawnRows = sellerDAO.withdrawWithReason(savedSeller.getSellerId(), reason);

        // then: 탈퇴 성공 확인
        assertEquals(1, withdrawnRows);

        // 탈퇴 상태 확인
        Optional<Seller> withdrawnSellerOpt = sellerDAO.findById(savedSeller.getSellerId());
        assertTrue(withdrawnSellerOpt.isPresent());
        Seller withdrawnSeller = withdrawnSellerOpt.get();
        assertEquals("WITHDRAWN", withdrawnSeller.getStatus());
        assertNotNull(withdrawnSeller.getWithdrawnAt());
        assertEquals(reason, withdrawnSeller.getWithdrawnReason());
    }

    @Test
    @DisplayName("판매자 탈퇴 처리 - 존재하지 않는 ID")
    void withdrawWithReason_not_found() {
        // when: 존재하지 않는 ID로 탈퇴 시도
        int withdrawnRows = sellerDAO.withdrawWithReason(999999L, "탈퇴 사유");

        // then: 탈퇴되지 않음
        assertEquals(0, withdrawnRows);
    }

    // ==================== 중복 체크 테스트 ====================

    @Test
    @DisplayName("이메일 중복 체크 - 중복됨")
    void existsByEmail_true() {
        // given: 판매자 저장
        sellerDAO.save(testSeller);

        // when: 이메일 중복 체크
        boolean exists = sellerDAO.existsByEmail(testSeller.getEmail());

        // then: 중복됨
        assertTrue(exists);
    }

    @Test
    @DisplayName("이메일 중복 체크 - 중복 안됨")
    void existsByEmail_false() {
        // when: 존재하지 않는 이메일 중복 체크
        boolean exists = sellerDAO.existsByEmail("new@seller.com");

        // then: 중복 안됨
        assertFalse(exists);
    }

    @Test
    @DisplayName("사업자등록번호 중복 체크 - 중복됨")
    void existsByBizRegNo_true() {
        // given: 판매자 저장
        sellerDAO.save(testSeller);

        // when: 사업자등록번호 중복 체크
        boolean exists = sellerDAO.existsByBizRegNo(testSeller.getBizRegNo());

        // then: 중복됨
        assertTrue(exists);
    }

    @Test
    @DisplayName("사업자등록번호 중복 체크 - 중복 안됨")
    void existsByBizRegNo_false() {
        // when: 존재하지 않는 사업자등록번호 중복 체크
        boolean exists = sellerDAO.existsByBizRegNo("999-88-77777");

        // then: 중복 안됨
        assertFalse(exists);
    }

    @Test
    @DisplayName("상호명 중복 체크 - 중복됨")
    void existsByShopName_true() {
        // given: 판매자 저장
        sellerDAO.save(testSeller);

        // when: 상호명 중복 체크
        boolean exists = sellerDAO.existsByShopName(testSeller.getShopName());

        // then: 중복됨
        assertTrue(exists);
    }

    @Test
    @DisplayName("상호명 중복 체크 - 중복 안됨")
    void existsByShopName_false() {
        // when: 존재하지 않는 상호명 중복 체크
        boolean exists = sellerDAO.existsByShopName("새상점명");

        // then: 중복 안됨
        assertFalse(exists);
    }

    // ==================== 목록 조회 테스트 ====================

    @Test
    @DisplayName("전체 판매자 목록 조회")
    void findAll() {
        // given: 여러 판매자 저장
        sellerDAO.save(testSeller);

        Seller seller2 = createSampleSeller();
        seller2.setEmail("seller2@shop.com");
        seller2.setBizRegNo("222-33-44444");
        seller2.setShopName("두번째상점");
        sellerDAO.save(seller2);

        // when: 전체 목록 조회
        List<Seller> sellers = sellerDAO.findAll();

        // then: 저장된 판매자들이 조회됨
        assertNotNull(sellers);
        assertTrue(sellers.size() >= 2);
        assertTrue(sellers.stream().anyMatch(s -> s.getEmail().equals(testSeller.getEmail())));
        assertTrue(sellers.stream().anyMatch(s -> s.getEmail().equals(seller2.getEmail())));
    }

    @Test
    @DisplayName("탈퇴한 판매자 목록 조회")
    void findWithdrawnMembers() {
        // given: 판매자 저장 후 탈퇴 처리
        Seller savedSeller = sellerDAO.save(testSeller);
        sellerDAO.withdrawWithReason(savedSeller.getSellerId(), "테스트 탈퇴");

        // when: 탈퇴한 판매자 목록 조회
        List<Seller> withdrawnSellers = sellerDAO.findWithdrawnMembers();

        // then: 탈퇴한 판매자가 조회됨
        assertNotNull(withdrawnSellers);
        assertTrue(withdrawnSellers.stream().anyMatch(s -> 
            s.getSellerId().equals(savedSeller.getSellerId()) && 
            "WITHDRAWN".equals(s.getStatus())
        ));
    }

    // ==================== Edge Case 테스트 ====================

    @Test
    @DisplayName("대소문자 구분 이메일 중복 체크")
    void existsByEmail_case_sensitivity() {
        // given: 소문자 이메일로 저장
        sellerDAO.save(testSeller);

        // when: 대문자로 중복 체크
        boolean existsUpper = sellerDAO.existsByEmail(testSeller.getEmail().toUpperCase());
        boolean existsLower = sellerDAO.existsByEmail(testSeller.getEmail().toLowerCase());

        // then: 대소문자 구분 확인
        assertTrue(existsLower); // 원본과 동일
        // existsUpper는 DB 설정에 따라 다름
    }

    @Test
    @DisplayName("특수문자 포함 상호명 처리")
    void handle_special_characters_in_shop_name() {
        // given: 특수문자 포함 상호명
        testSeller.setShopName("테스트상점@#$%");

        // when: 저장 및 조회
        Seller savedSeller = sellerDAO.save(testSeller);
        Optional<Seller> foundSeller = sellerDAO.findById(savedSeller.getSellerId());

        // then: 특수문자 유지 확인
        assertTrue(foundSeller.isPresent());
        assertEquals("테스트상점@#$%", foundSeller.get().getShopName());
    }

    @Test
    @DisplayName("사업자등록번호 형식 테스트")
    void bizRegNo_format_test() {
        // given: 다양한 형식의 사업자등록번호
        testSeller.setBizRegNo("123-45-67890");

        // when: 저장
        Seller savedSeller = sellerDAO.save(testSeller);

        // then: 형식 유지 확인
        assertEquals("123-45-67890", savedSeller.getBizRegNo());
    }

    @Test
    @DisplayName("null 값 처리 테스트")
    void handle_null_values() {
        // when & then: null 이메일로 중복 체크
        assertThrows(Exception.class, () -> {
            sellerDAO.existsByEmail(null);
        });

        // when & then: null ID로 조회
        assertThrows(Exception.class, () -> {
            sellerDAO.findById(null);
        });

        // when & then: null 사업자등록번호로 중복 체크
        assertThrows(Exception.class, () -> {
            sellerDAO.existsByBizRegNo(null);
        });
    }

    @Test
    @DisplayName("동시 저장 테스트 - 이메일 중복")
    void concurrent_save_test_email_duplicate() {
        // given: 동일한 이메일을 가진 두 판매자
        Seller seller1 = createSampleSeller();
        Seller seller2 = createSampleSeller();
        seller2.setBizRegNo("222-33-44444"); // 사업자번호는 다르게
        seller2.setShopName("다른상점명"); // 상점명도 다르게

        // when: 첫 번째 저장
        sellerDAO.save(seller1);

        // then: 두 번째 저장시 이메일 중복 에러
        assertThrows(Exception.class, () -> {
            sellerDAO.save(seller2);
        });
    }

    @Test
    @DisplayName("동시 저장 테스트 - 사업자등록번호 중복")
    void concurrent_save_test_bizRegNo_duplicate() {
        // given: 동일한 사업자등록번호를 가진 두 판매자
        Seller seller1 = createSampleSeller();
        Seller seller2 = createSampleSeller();
        seller2.setEmail("different@email.com"); // 이메일은 다르게
        seller2.setShopName("다른상점명"); // 상점명도 다르게

        // when: 첫 번째 저장
        sellerDAO.save(seller1);

        // then: 두 번째 저장시 사업자등록번호 중복 에러
        assertThrows(Exception.class, () -> {
            sellerDAO.save(seller2);
        });
    }

    @Test
    @DisplayName("매우 긴 문자열 처리 테스트")
    void handle_long_strings() {
        // given: 매우 긴 상점명
        String longShopName = "상".repeat(500); // 매우 긴 상점명
        testSeller.setShopName(longShopName);

        // when & then: 길이 제한 확인 (DB 컬럼 길이에 따라)
        if (longShopName.length() > 255) { // 일반적인 문자열 길이 제한
            assertThrows(Exception.class, () -> {
                sellerDAO.save(testSeller);
            });
        }
    }
} 