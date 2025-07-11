package com.kh.project.domain.seller.svc;

import com.kh.project.domain.entity.MemberStatus;
import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.seller.dao.SellerDAO;
import com.kh.project.web.exception.MemberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 판매자 Service 계층 JUnit + Mockito 테스트
 * 
 * 테스트 목적:
 * - 핵심 비즈니스 로직 검증
 * - 예외 상황 처리 확인
 * - DAO와의 상호작용 검증
 * 
 * @author 개발팀
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("판매자 Service 계층 테스트")
class SellerSVCImplJunitMockitoTest {

    @Mock
    private SellerDAO sellerDAO;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SellerSVCImpl sellerSVC;

    private Seller testSeller;

    @BeforeEach
    void setUp() {
        testSeller = new Seller();
        testSeller.setSellerId(1L);
        testSeller.setEmail("test@shop.com");
        testSeller.setPassword("encodedPassword123!");
        testSeller.setName("김대표");
        testSeller.setStoreName("테스트상점");
        testSeller.setBusinessNumber("123-45-67890");
        testSeller.setTel("02-1234-5678");
        testSeller.setPostcode("12345");
        testSeller.setAddress("서울시 강남구");
        testSeller.setDetailAddress("테헤란로 123");
        testSeller.setStatus(MemberStatus.ACTIVE);
        testSeller.setCreatedAt(LocalDateTime.now());
        testSeller.setUpdatedAt(LocalDateTime.now());
    }

    // ========================= 회원가입 테스트 =========================
    
    @Test
    @DisplayName("판매자 회원가입 - 성공")
    void signup_Success() {
        // Given
        when(sellerDAO.findByEmail("test@shop.com")).thenReturn(Optional.empty());
        when(sellerDAO.findByStoreName("테스트상점")).thenReturn(Optional.empty());
        when(sellerDAO.findByBusinessNumber("123-45-67890")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword123!");
        when(sellerDAO.save(any(Seller.class))).thenReturn(1L);

        // When
        Long sellerId = sellerSVC.signup(testSeller);

        // Then
        assertThat(sellerId).isEqualTo(1L);
        
        // 비밀번호 암호화 검증
        verify(passwordEncoder).encode(anyString());
        verify(sellerDAO).save(any(Seller.class));
    }

    @Test
    @DisplayName("판매자 회원가입 - 이메일 중복")
    void signup_EmailDuplicate() {
        // Given
        when(sellerDAO.findByEmail("test@shop.com")).thenReturn(Optional.of(testSeller));

        // When & Then
        assertThatThrownBy(() -> sellerSVC.signup(testSeller))
                .isInstanceOf(MemberException.class)
                .hasMessage("이미 사용 중인 이메일입니다.");
        
        verify(sellerDAO, never()).save(any(Seller.class));
    }

    @Test
    @DisplayName("판매자 회원가입 - 상호명 중복")
    void signup_StoreNameDuplicate() {
        // Given
        when(sellerDAO.findByEmail(anyString())).thenReturn(Optional.empty());
        when(sellerDAO.findByStoreName("테스트상점")).thenReturn(Optional.of(testSeller));

        // When & Then
        assertThatThrownBy(() -> sellerSVC.signup(testSeller))
                .isInstanceOf(MemberException.class)
                .hasMessage("이미 사용 중인 상호명입니다.");
        
        verify(sellerDAO, never()).save(any(Seller.class));
    }

    @Test
    @DisplayName("판매자 회원가입 - 사업자등록번호 중복")
    void signup_BusinessNumberDuplicate() {
        // Given
        when(sellerDAO.findByEmail(anyString())).thenReturn(Optional.empty());
        when(sellerDAO.findByStoreName(anyString())).thenReturn(Optional.empty());
        when(sellerDAO.findByBusinessNumber("123-45-67890")).thenReturn(Optional.of(testSeller));

        // When & Then
        assertThatThrownBy(() -> sellerSVC.signup(testSeller))
                .isInstanceOf(MemberException.class)
                .hasMessage("이미 등록된 사업자등록번호입니다.");
        
        verify(sellerDAO, never()).save(any(Seller.class));
    }

    // ========================= 로그인 테스트 =========================

    @Test
    @DisplayName("판매자 로그인 - 성공")
    void login_Success() {
        // Given
        when(sellerDAO.findByEmail("test@shop.com")).thenReturn(Optional.of(testSeller));
        when(passwordEncoder.matches("rawPassword123!", "encodedPassword123!")).thenReturn(true);

        // When
        Seller loginSeller = sellerSVC.login("test@shop.com", "rawPassword123!");

        // Then
        assertThat(loginSeller).isNotNull();
        assertThat(loginSeller.getEmail()).isEqualTo("test@shop.com");
        assertThat(loginSeller.getStoreName()).isEqualTo("테스트상점");
    }

    @Test
    @DisplayName("판매자 로그인 - 이메일 없음")
    void login_EmailNotFound() {
        // Given
        when(sellerDAO.findByEmail("nonexist@shop.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sellerSVC.login("nonexist@shop.com", "password123!"))
                .isInstanceOf(MemberException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("판매자 로그인 - 비밀번호 오류")
    void login_WrongPassword() {
        // Given
        when(sellerDAO.findByEmail("test@shop.com")).thenReturn(Optional.of(testSeller));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword123!")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> sellerSVC.login("test@shop.com", "wrongPassword"))
                .isInstanceOf(MemberException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("판매자 로그인 - 탈퇴한 회원")
    void login_WithdrawnMember() {
        // Given
        testSeller.setStatus(MemberStatus.WITHDRAWN);
        when(sellerDAO.findByEmail("test@shop.com")).thenReturn(Optional.of(testSeller));

        // When & Then
        assertThatThrownBy(() -> sellerSVC.login("test@shop.com", "password123!"))
                .isInstanceOf(MemberException.class)
                .hasMessage("탈퇴한 회원입니다.");
    }

    // ========================= 정보수정 테스트 =========================

    @Test
    @DisplayName("판매자 정보수정 - 성공")
    void updateInfo_Success() {
        // Given
        Seller updateSeller = new Seller();
        updateSeller.setSellerId(1L);
        updateSeller.setStoreName("수정된상점");
        updateSeller.setTel("02-9999-8888");
        updateSeller.setPostcode("54321");
        updateSeller.setAddress("부산시 해운대구");
        updateSeller.setDetailAddress("센텀로 456");

        when(sellerDAO.findByStoreName("수정된상점")).thenReturn(Optional.empty());
        when(sellerDAO.updateInfo(any(Seller.class))).thenReturn(1);

        // When
        sellerSVC.updateInfo(updateSeller);

        // Then
        verify(sellerDAO).updateInfo(updateSeller);
    }

    @Test
    @DisplayName("판매자 정보수정 - 상호명 중복")
    void updateInfo_StoreNameDuplicate() {
        // Given
        Seller updateSeller = new Seller();
        updateSeller.setSellerId(1L);
        updateSeller.setStoreName("중복상점");

        Seller anotherSeller = new Seller();
        anotherSeller.setSellerId(2L);
        anotherSeller.setStoreName("중복상점");

        when(sellerDAO.findByStoreName("중복상점")).thenReturn(Optional.of(anotherSeller));

        // When & Then
        assertThatThrownBy(() -> sellerSVC.updateInfo(updateSeller))
                .isInstanceOf(MemberException.class)
                .hasMessage("이미 사용 중인 상호명입니다.");
        
        verify(sellerDAO, never()).updateInfo(any(Seller.class));
    }

    // ========================= 탈퇴 테스트 =========================

    @Test
    @DisplayName("판매자 탈퇴 - 성공")
    void withdraw_Success() {
        // Given
        when(sellerDAO.findById(1L)).thenReturn(Optional.of(testSeller));
        when(sellerDAO.withdraw(1L)).thenReturn(1);

        // When
        sellerSVC.withdraw(1L);

        // Then
        verify(sellerDAO).withdraw(1L);
    }

    @Test
    @DisplayName("판매자 탈퇴 - 회원 없음")
    void withdraw_SellerNotFound() {
        // Given
        when(sellerDAO.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sellerSVC.withdraw(99L))
                .isInstanceOf(MemberException.class)
                .hasMessage("존재하지 않는 회원입니다.");
        
        verify(sellerDAO, never()).withdraw(anyLong());
    }

    // ========================= 비밀번호 확인 테스트 =========================

    @Test
    @DisplayName("비밀번호 확인 - 성공")
    void checkPassword_Success() {
        // Given
        when(sellerDAO.findById(1L)).thenReturn(Optional.of(testSeller));
        when(passwordEncoder.matches("rawPassword123!", "encodedPassword123!")).thenReturn(true);

        // When
        boolean result = sellerSVC.checkPassword(1L, "rawPassword123!");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("비밀번호 확인 - 실패")
    void checkPassword_Fail() {
        // Given
        when(sellerDAO.findById(1L)).thenReturn(Optional.of(testSeller));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword123!")).thenReturn(false);

        // When
        boolean result = sellerSVC.checkPassword(1L, "wrongPassword");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("비밀번호 확인 - null 입력")
    void checkPassword_NullInput() {
        // When
        boolean result = sellerSVC.checkPassword(1L, null);

        // Then
        assertThat(result).isFalse();
        verify(sellerDAO, never()).findById(anyLong());
    }

    // ========================= 서비스 이용현황 테스트 =========================

    @Test
    @DisplayName("서비스 이용현황 조회 - 성공")
    void getServiceUsage_Success() {
        // Given
        when(sellerDAO.findById(1L)).thenReturn(Optional.of(testSeller));

        // When
        Seller seller = sellerSVC.getServiceUsage(1L);

        // Then
        assertThat(seller).isNotNull();
        assertThat(seller.getSellerId()).isEqualTo(1L);
        assertThat(seller.getStoreName()).isEqualTo("테스트상점");
    }

    @Test
    @DisplayName("서비스 이용현황 조회 - 회원 없음")
    void getServiceUsage_SellerNotFound() {
        // Given
        when(sellerDAO.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sellerSVC.getServiceUsage(99L))
                .isInstanceOf(MemberException.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }
} 