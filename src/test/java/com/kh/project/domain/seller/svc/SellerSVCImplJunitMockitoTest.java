package com.kh.project.domain.seller.svc;

import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.seller.dao.SellerDAO;
import com.kh.project.web.exception.BusinessException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ?�매???�비??JUnit + Mockito ?�스?? */
@ExtendWith(MockitoExtension.class)
@DisplayName("?�매???�비???�스??)
class SellerSVCImplJunitMockitoTest {

    @Mock
    private SellerDAO sellerDAO;

    @InjectMocks
    private SellerSVCImpl sellerSVC;

    private Seller testSeller;
    private Seller withdrawnSeller;

    @BeforeEach
    void setUp() {
        testSeller = createTestSeller();
        withdrawnSeller = createWithdrawnSeller();
    }

    @Nested
    @DisplayName("?�원가???�스??)
    class JoinTest {

        @Test
        @DisplayName("?�규 ?�원가???�공")
        void join_newMember_success() {
            // given
            when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.empty());
            when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(false);
            when(sellerDAO.existsByShopName(testSeller.getShopName())).thenReturn(false);
            when(sellerDAO.existsByName(testSeller.getName())).thenReturn(false);
            when(sellerDAO.existsByShopAddress(testSeller.getShopAddress())).thenReturn(false);
            when(sellerDAO.save(any(Seller.class))).thenReturn(testSeller);

            // when
            Seller result = sellerSVC.join(testSeller);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(testSeller.getEmail());
            assertThat(result.getShopName()).isEqualTo(testSeller.getShopName());
            assertThat(result.getBizRegNo()).isEqualTo(testSeller.getBizRegNo());
            
            verify(sellerDAO).findByEmail(testSeller.getEmail());
            verify(sellerDAO).existsByBizRegNo(testSeller.getBizRegNo());
            verify(sellerDAO).existsByShopName(testSeller.getShopName());
            verify(sellerDAO).existsByName(testSeller.getName());
            verify(sellerDAO).existsByShopAddress(testSeller.getShopAddress());
            verify(sellerDAO).save(any(Seller.class));
        }

        @Test
        @DisplayName("?�퇴 ?�원 ?�활?�화 ?�공")
        void join_withdrawnMember_reactivate_success() {
            // given
            when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.of(withdrawnSeller));
            when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(false);
            when(sellerDAO.existsByShopName(testSeller.getShopName())).thenReturn(false);
            when(sellerDAO.existsByName(testSeller.getName())).thenReturn(false);
            when(sellerDAO.existsByShopAddress(testSeller.getShopAddress())).thenReturn(false);
            when(sellerDAO.rejoin(any(Seller.class))).thenReturn(1);
            when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.of(testSeller));

            // when
            Seller result = sellerSVC.join(testSeller);

            // then
            assertThat(result).isNotNull();
            verify(sellerDAO).rejoin(any(Seller.class));
        }

        @Test
        @DisplayName("?�원가???�패 - ?�성 ?�메??중복")
        void join_fail_activeEmail_duplicate() {
            // given
            when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.of(testSeller));

            // when & then
            assertThatThrownBy(() -> sellerSVC.join(testSeller))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("?��? ?�용중인 ?�메?�입?�다");

            verify(sellerDAO).findByEmail(testSeller.getEmail());
            verify(sellerDAO, never()).save(any(Seller.class));
        }

        @Test
        @DisplayName("?�원가???�패 - ?�업?�등록번??중복")
        void join_fail_bizRegNo_duplicate() {
            // given
            when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.empty());
            when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> sellerSVC.join(testSeller))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("?��? ?�록???�업?�등록번?�입?�다");

            verify(sellerDAO).findByEmail(testSeller.getEmail());
            verify(sellerDAO).existsByBizRegNo(testSeller.getBizRegNo());
            verify(sellerDAO, never()).save(any(Seller.class));
        }

        @Test
        @DisplayName("?�원가???�패 - ?�호�?중복")
        void join_fail_shopName_duplicate() {
            // given
            when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.empty());
            when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(false);
            when(sellerDAO.existsByShopName(testSeller.getShopName())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> sellerSVC.join(testSeller))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("?��? ?�용중인 ?�호명입?�다");

            verify(sellerDAO).findByEmail(testSeller.getEmail());
            verify(sellerDAO).existsByBizRegNo(testSeller.getBizRegNo());
            verify(sellerDAO).existsByShopName(testSeller.getShopName());
            verify(sellerDAO, never()).save(any(Seller.class));
        }

        @Test
        @DisplayName("?�원가???�패 - ?�?�자�?중복")
        void join_fail_name_duplicate() {
            // given
            when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.empty());
            when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(false);
            when(sellerDAO.existsByShopName(testSeller.getShopName())).thenReturn(false);
            when(sellerDAO.existsByName(testSeller.getName())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> sellerSVC.join(testSeller))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("?��? ?�록???�?�자명입?�다");

            verify(sellerDAO).findByEmail(testSeller.getEmail());
            verify(sellerDAO).existsByBizRegNo(testSeller.getBizRegNo());
            verify(sellerDAO).existsByShopName(testSeller.getShopName());
            verify(sellerDAO).existsByName(testSeller.getName());
            verify(sellerDAO, never()).save(any(Seller.class));
        }

        @Test
        @DisplayName("?�원가???�패 - ?�업??주소 중복")
        void join_fail_shopAddress_duplicate() {
            // given
            when(sellerDAO.findByEmail(testSeller.getEmail())).thenReturn(Optional.empty());
            when(sellerDAO.existsByBizRegNo(testSeller.getBizRegNo())).thenReturn(false);
            when(sellerDAO.existsByShopName(testSeller.getShopName())).thenReturn(false);
            when(sellerDAO.existsByName(testSeller.getName())).thenReturn(false);
            when(sellerDAO.existsByShopAddress(testSeller.getShopAddress())).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> sellerSVC.join(testSeller))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("?��? ?�록???�업??주소?�니??);

            verify(sellerDAO).findByEmail(testSeller.getEmail());
            verify(sellerDAO).existsByBizRegNo(testSeller.getBizRegNo());
            verify(sellerDAO).existsByShopName(testSeller.getShopName());
            verify(sellerDAO).existsByName(testSeller.getName());
            verify(sellerDAO).existsByShopAddress(testSeller.getShopAddress());
            verify(sellerDAO, never()).save(any(Seller.class));
        }

        @Test
        @DisplayName("?�원가???�패 - ?�못???�업?�등록번???�식")
        void join_fail_invalid_bizRegNo_format() {
            // given
            testSeller.setBizRegNo("invalid-format");

            // when & then
            assertThatThrownBy(() -> sellerSVC.join(testSeller))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("?�바르�? ?��? ?�업?�등록번???�식?�니??);

            verify(sellerDAO, never()).save(any(Seller.class));
        }
    }

    @Nested
    @DisplayName("로그???�스??)
    class LoginTest {

        @Test
        @DisplayName("로그???�공")
        void login_success() {
            // given
            String email = "test@shop.com";
            String password = "password123";
            testSeller.setPassword(password);
            
            when(sellerDAO.findByEmail(email)).thenReturn(Optional.of(testSeller));

            // when
            Seller result = sellerSVC.login(email, password);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.canLogin()).isTrue();
            
            verify(sellerDAO).findByEmail(email);
        }

        @Test
        @DisplayName("로그???�패 - 존재?��? ?�는 ?�메??)
        void login_fail_email_not_found() {
            // given
            String email = "notfound@shop.com";
            when(sellerDAO.findByEmail(email)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sellerSVC.login(email, "password"))
                .isInstanceOf(BusinessException.class);

            verify(sellerDAO).findByEmail(email);
        }

        @Test
        @DisplayName("로그???�패 - 비�?번호 불일�?)
        void login_fail_wrong_password() {
            // given
            String email = "test@shop.com";
            testSeller.setPassword("correctPassword");
            
            when(sellerDAO.findByEmail(email)).thenReturn(Optional.of(testSeller));

            // when & then
            assertThatThrownBy(() -> sellerSVC.login(email, "wrongPassword"))
                .isInstanceOf(BusinessException.class);

            verify(sellerDAO).findByEmail(email);
        }

        @Test
        @DisplayName("로그???�패 - ?�퇴???�원")
        void login_fail_withdrawn_member() {
            // given
            String email = "withdrawn@shop.com";
            
            when(sellerDAO.findByEmail(email)).thenReturn(Optional.of(withdrawnSeller));

            // when & then
            assertThatThrownBy(() -> sellerSVC.login(email, "password"))
                .isInstanceOf(BusinessException.class);

            verify(sellerDAO).findByEmail(email);
        }

        @Test
        @DisplayName("로그???�패 - 비활?�화???�원")
        void login_fail_inactive_member() {
            // given
            String email = "inactive@shop.com";
            testSeller.setStatus("비활?�화");
            
            when(sellerDAO.findByEmail(email)).thenReturn(Optional.of(testSeller));

            // when & then
            assertThatThrownBy(() -> sellerSVC.login(email, "password123"))
                .isInstanceOf(BusinessException.class);

            verify(sellerDAO).findByEmail(email);
        }
    }

    @Nested
    @DisplayName("?�보 ?�정 ?�스??)
    class UpdateTest {

        @Test
        @DisplayName("?�보 ?�정 ?�공")
        void update_success() {
            // given
            Long sellerId = 1L;
            Seller updateSeller = new Seller();
            updateSeller.setShopName("변경된?�점");
            updateSeller.setTel("02-9999-8888");
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));
            when(sellerDAO.existsByShopName("변경된?�점")).thenReturn(false);
            when(sellerDAO.update(sellerId, updateSeller)).thenReturn(1);

            // when
            int result = sellerSVC.update(sellerId, updateSeller);

            // then
            assertThat(result).isEqualTo(1);
            
            verify(sellerDAO).findById(sellerId);
            verify(sellerDAO).existsByShopName("변경된?�점");
            verify(sellerDAO).update(sellerId, updateSeller);
        }

        @Test
        @DisplayName("?�보 ?�정 ?�패 - ?�호�?중복")
        void update_fail_shopName_duplicate() {
            // given
            Long sellerId = 1L;
            Seller updateSeller = new Seller();
            updateSeller.setShopName("중복?�호");
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));
            when(sellerDAO.existsByShopName("중복?�호")).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> sellerSVC.update(sellerId, updateSeller))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("?��? ?�용중인 ?�호명입?�다");

            verify(sellerDAO).findById(sellerId);
            verify(sellerDAO).existsByShopName("중복?�호");
            verify(sellerDAO, never()).update(anyLong(), any(Seller.class));
        }

        @Test
        @DisplayName("?�보 ?�정 ?�공 - 같�? ?�호�??��?")
        void update_success_same_shopName() {
            // given
            Long sellerId = 1L;
            Seller updateSeller = new Seller();
            updateSeller.setShopName("기존?�호");
            testSeller.setShopName("기존?�호");
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));
            when(sellerDAO.update(sellerId, updateSeller)).thenReturn(1);

            // when
            int result = sellerSVC.update(sellerId, updateSeller);

            // then
            assertThat(result).isEqualTo(1);
            
            verify(sellerDAO).findById(sellerId);
            verify(sellerDAO, never()).existsByShopName(anyString());
            verify(sellerDAO).update(sellerId, updateSeller);
        }
    }

    @Nested
    @DisplayName("?�퇴 ?�스??)
    class WithdrawTest {

        @Test
        @DisplayName("?�퇴 ?�공")
        void withdraw_success() {
            // given
            Long sellerId = 1L;
            String reason = "?�업 종료";
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));
            when(sellerDAO.withdrawWithReason(sellerId, reason)).thenReturn(1);

            // when
            int result = sellerSVC.withdraw(sellerId, reason);

            // then
            assertThat(result).isEqualTo(1);
            
            verify(sellerDAO).findById(sellerId);
            verify(sellerDAO).withdrawWithReason(sellerId, reason);
        }

        @Test
        @DisplayName("?�퇴 ?�패 - 존재?��? ?�는 ?�원")
        void withdraw_fail_member_not_found() {
            // given
            Long sellerId = 999L;
            String reason = "?�업 종료";
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sellerSVC.withdraw(sellerId, reason))
                .isInstanceOf(BusinessException.class);

            verify(sellerDAO).findById(sellerId);
            verify(sellerDAO, never()).withdrawWithReason(anyLong(), anyString());
        }

        @Test
        @DisplayName("?�퇴 ?�패 - ?��? ?�퇴???�원")
        void withdraw_fail_already_withdrawn() {
            // given
            Long sellerId = 1L;
            String reason = "?�업 종료";
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(withdrawnSeller));

            // when & then
            assertThatThrownBy(() -> sellerSVC.withdraw(sellerId, reason))
                .isInstanceOf(BusinessException.class);

            verify(sellerDAO).findById(sellerId);
            verify(sellerDAO, never()).withdrawWithReason(anyLong(), anyString());
        }

        @Test
        @DisplayName("?�퇴 ?�패 - ?�퇴 처리 ?�패")
        void withdraw_fail_process_failed() {
            // given
            Long sellerId = 1L;
            String reason = "?�업 종료";
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));
            when(sellerDAO.withdrawWithReason(sellerId, reason)).thenReturn(0);

            // when & then
            assertThatThrownBy(() -> sellerSVC.withdraw(sellerId, reason))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("?�매???�퇴 처리???�패?�습?�다");

            verify(sellerDAO).findById(sellerId);
            verify(sellerDAO).withdrawWithReason(sellerId, reason);
        }
    }

    @Nested
    @DisplayName("비�?번호 ?�인 ?�스??)
    class CheckPasswordTest {

        @Test
        @DisplayName("비�?번호 ?�인 ?�공")
        void checkPassword_success() {
            // given
            Long sellerId = 1L;
            String password = "password123";
            testSeller.setPassword(password);
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));

            // when
            boolean result = sellerSVC.checkPassword(sellerId, password);

            // then
            assertThat(result).isTrue();
            
            verify(sellerDAO).findById(sellerId);
        }

        @Test
        @DisplayName("비�?번호 ?�인 ?�패 - 불일�?)
        void checkPassword_fail_wrong_password() {
            // given
            Long sellerId = 1L;
                    String correctPassword = "CorrectPass123!";
        String inputPassword = "WrongPass123!";
            testSeller.setPassword(correctPassword);
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));

            // when
            boolean result = sellerSVC.checkPassword(sellerId, inputPassword);

            // then
            assertThat(result).isFalse();
            
            verify(sellerDAO).findById(sellerId);
        }

        @Test
        @DisplayName("비�?번호 ?�인 ?�패 - null 비�?번호")
        void checkPassword_fail_null_password() {
            // given
            Long sellerId = 1L;

            // when
            boolean result = sellerSVC.checkPassword(sellerId, null);

            // then
            assertThat(result).isFalse();
            
            verify(sellerDAO, never()).findById(anyLong());
        }

        @Test
        @DisplayName("비�?번호 ?�인 ?�패 - 존재?��? ?�는 ?�원")
        void checkPassword_fail_member_not_found() {
            // given
            Long sellerId = 999L;
            String password = "password123";
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.empty());

            // when
            boolean result = sellerSVC.checkPassword(sellerId, password);

            // then
            assertThat(result).isFalse();
            
            verify(sellerDAO).findById(sellerId);
        }
    }

    @Nested
    @DisplayName("?�비???�용?�황 ?�스??)
    class ServiceUsageTest {

        @Test
        @DisplayName("?�비???�용?�황 조회 ?�공")
        void getServiceUsage_success() {
            // given
            Long sellerId = 1L;
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));

            // when
            MemberStatusInfo result = sellerSVC.getServiceUsage(sellerId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.isCanWithdraw()).isTrue();
            assertThat(result.getTotalProducts()).isEqualTo(0);
            assertThat(result.getActiveProducts()).isEqualTo(0);
            assertThat(result.getMonthlyRevenue()).isEqualTo(0);
            assertThat(result.getActiveOrders()).isEqualTo(0);
            assertThat(result.getPreparingOrders()).isEqualTo(0);
            assertThat(result.getShippingOrders()).isEqualTo(0);
            assertThat(result.getPendingAmount()).isEqualTo(0);
            
            verify(sellerDAO).findById(sellerId);
        }

        @Test
        @DisplayName("?�비???�용?�황 조회 ?�패 - ?�원 ?�음")
        void getServiceUsage_fail_member_not_found() {
            // given
            Long sellerId = 999L;
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sellerSVC.getServiceUsage(sellerId))
                .isInstanceOf(BusinessException.class);

            verify(sellerDAO).findById(sellerId);
        }
    }

    @Nested
    @DisplayName("중복 체크 ?�스??)
    class DuplicateCheckTest {

        @Test
        @DisplayName("?�메??중복 체크 - 존재??)
        void existsByEmail_true() {
            // given
            String email = "test@shop.com";
            when(sellerDAO.existsByEmail(email)).thenReturn(true);

            // when
            boolean result = sellerSVC.existsByEmail(email);

            // then
            assertThat(result).isTrue();
            verify(sellerDAO).existsByEmail(email);
        }

        @Test
        @DisplayName("?�업?�등록번??중복 체크 - 존재??)
        void existsByBizRegNo_true() {
            // given
            String bizRegNo = "123-45-67890";
            when(sellerDAO.existsByBizRegNo(bizRegNo)).thenReturn(true);

            // when
            boolean result = sellerSVC.existsByBizRegNo(bizRegNo);

            // then
            assertThat(result).isTrue();
            verify(sellerDAO).existsByBizRegNo(bizRegNo);
        }

        @Test
        @DisplayName("?�호�?중복 체크 - 존재??)
        void existsByShopName_true() {
            // given
            String shopName = "?�스?�상??;
            when(sellerDAO.existsByShopName(shopName)).thenReturn(true);

            // when
            boolean result = sellerSVC.existsByShopName(shopName);

            // then
            assertThat(result).isTrue();
            verify(sellerDAO).existsByShopName(shopName);
        }

        @Test
        @DisplayName("?�?�자�?중복 체크 - 존재??)
        void existsByName_true() {
            // given
            String name = "김?�??;
            when(sellerDAO.existsByName(name)).thenReturn(true);

            // when
            boolean result = sellerSVC.existsByName(name);

            // then
            assertThat(result).isTrue();
            verify(sellerDAO).existsByName(name);
        }

        @Test
        @DisplayName("?�업??주소 중복 체크 - 존재??)
        void existsByShopAddress_true() {
            // given
            String shopAddress = "?�울??강남�??�헤?��?123";
            when(sellerDAO.existsByShopAddress(shopAddress)).thenReturn(true);

            // when
            boolean result = sellerSVC.existsByShopAddress(shopAddress);

            // then
            assertThat(result).isTrue();
            verify(sellerDAO).existsByShopAddress(shopAddress);
        }
    }

    @Nested
    @DisplayName("?�틸리티 메서???�스??)
    class UtilityMethodTest {

        @Test
        @DisplayName("?�업?�등록번???�효??검�?- ?�효??)
        void validateBizRegNo_valid() {
            // given
            String validBizRegNo = "123-45-67890";

            // when
            boolean result = sellerSVC.validateBizRegNo(validBizRegNo);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("?�업?�등록번???�효??검�?- 무효??)
        void validateBizRegNo_invalid() {
            // given
            String invalidBizRegNo = "invalid-format";

            // when
            boolean result = sellerSVC.validateBizRegNo(invalidBizRegNo);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("?�업?�등록번???�효??검�?- null")
        void validateBizRegNo_null() {
            // when
            boolean result = sellerSVC.validateBizRegNo(null);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("로그??가???��? ?�인 - 가??)
        void canLogin_true() {
            // given
            testSeller.setStatus("?�성??);
            testSeller.setWithdrawnAt(null);

            // when
            boolean result = sellerSVC.canLogin(testSeller);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("로그??가???��? ?�인 - 불�???(?�퇴)")
        void canLogin_false_withdrawn() {
            // given
            testSeller.setStatus("?�퇴");
            testSeller.setWithdrawnAt(new Date());

            // when
            boolean result = sellerSVC.canLogin(testSeller);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("?�퇴 ?��? ?�인 - ?�퇴??)
        void isWithdrawn_true() {
            // when
            boolean result = sellerSVC.isWithdrawn(withdrawnSeller);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("?�퇴 ?��? ?�인 - ?�퇴?��? ?�음")
        void isWithdrawn_false() {
            // when
            boolean result = sellerSVC.isWithdrawn(testSeller);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("?�퇴 가???��? ?�인")
        void canWithdraw_true() {
            // given
            Long sellerId = 1L;
            
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));

            // when
            boolean result = sellerSVC.canWithdraw(sellerId);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("조회 ?�스??)
    class FindTest {

        @Test
        @DisplayName("ID�?조회 ?�공")
        void findById_success() {
            // given
            Long sellerId = 1L;
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.of(testSeller));

            // when
            Optional<Seller> result = sellerSVC.findById(sellerId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getSellerId()).isEqualTo(sellerId);
            verify(sellerDAO).findById(sellerId);
        }

        @Test
        @DisplayName("ID�?조회 ?�패 - 존재?��? ?�음")
        void findById_not_found() {
            // given
            Long sellerId = 999L;
            when(sellerDAO.findById(sellerId)).thenReturn(Optional.empty());

            // when
            Optional<Seller> result = sellerSVC.findById(sellerId);

            // then
            assertThat(result).isEmpty();
            verify(sellerDAO).findById(sellerId);
        }

        @Test
        @DisplayName("?�퇴 ?�원 목록 조회")
        void getWithdrawnMembers_success() {
            // given
            List<Seller> withdrawnMembers = Arrays.asList(withdrawnSeller);
            when(sellerDAO.findWithdrawnMembers()).thenReturn(withdrawnMembers);

            // when
            List<Seller> result = sellerSVC.getWithdrawnMembers();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isWithdrawn()).isTrue();
            verify(sellerDAO).findWithdrawnMembers();
        }
    }

    // ?�스???�이???�성 메서??    private Seller createTestSeller() {
        Seller seller = new Seller();
        seller.setSellerId(1L);
        seller.setEmail("test@shop.com");
        seller.setPassword("password123");
        seller.setName("김?�??);
        seller.setShopName("?�스?�상??);
        seller.setBizRegNo("123-45-67890");
        seller.setShopAddress("?�울??강남�??�헤?��?123");
        seller.setTel("02-1234-5678");
        seller.setMemberGubun("NEW");
        seller.setStatus("?�성??);
        seller.setCdate(new Date());
        seller.setUdate(new Date());
        return seller;
    }

    private Seller createWithdrawnSeller() {
        Seller seller = new Seller();
        seller.setSellerId(2L);
        seller.setEmail("withdrawn@shop.com");
        seller.setPassword("password123");
        seller.setName("박�???);
        seller.setShopName("?�퇴?�점");
        seller.setBizRegNo("987-65-43210");
        seller.setShopAddress("?�울???�초�??�초?��?456");
        seller.setTel("02-9876-5432");
        seller.setMemberGubun("BRONZE");
        seller.setStatus("?�퇴");
        seller.setCdate(new Date());
        seller.setUdate(new Date());
        seller.setWithdrawnAt(new Date());
        seller.setWithdrawnReason("?�업 종료");
        return seller;
    }
} 
