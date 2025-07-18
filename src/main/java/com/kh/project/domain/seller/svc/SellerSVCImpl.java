package com.kh.project.domain.seller.svc;

import com.kh.project.domain.buyer.dao.BuyerDAO;
import com.kh.project.domain.entity.MemberStatus;
import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.entity.ServiceUsage;
import com.kh.project.domain.seller.dao.SellerDAO;
import com.kh.project.web.exception.BusinessException;
import com.kh.project.web.form.member.SellerEditForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SellerSVCImpl implements SellerSVC {

    private final SellerDAO sellerDAO;
    private final BuyerDAO buyerDAO;

    // ============= 인터페이스 구현 메서드들 =============

    @Override
    public Seller save(Seller seller) {
        log.info("판매자 회원가입: email={}", seller.getEmail());

        // 이메일 중복 체크 (모든 상태)
        if (sellerDAO.existsByEmail(seller.getEmail())) {
            throw new BusinessException("이미 등록된 이메일입니다. 탈퇴 후 재가입은 불가능합니다.");
        }

        // 사업자번호 중복 체크 (모든 상태)
        if (sellerDAO.existsByBizRegNo(seller.getBizRegNo())) {
            throw new BusinessException("이미 등록된 사업자등록번호입니다. 탈퇴 후 재가입은 불가능합니다.");
        }



        return sellerDAO.save(seller);
    }

    @Override
    public Seller join(Seller seller) {
        log.info("판매자 스마트 회원가입 시작: email={}", seller.getEmail());

        // 1. 기존 회원 조회 (탈퇴 회원 포함)
        Optional<Seller> existingSellerOpt = sellerDAO.findByEmail(seller.getEmail());

        if (existingSellerOpt.isPresent()) {
            // 2. 기존 회원이 있으면 (활성/탈퇴 상관없이) 재가입 불가
            log.warn("이미 등록된 판매자 계정 (재가입 불가): email={}", seller.getEmail());
            throw new BusinessException("이미 등록된 이메일입니다. 탈퇴 후 재가입은 불가능합니다.");
        } else {
            // 3. 신규 회원가입
            log.info("신규 판매자 회원가입: email={}", seller.getEmail());
            return save(seller); // 기존 save 메서드 재사용
        }
    }

    @Override
    public Optional<Seller> findById(Long sellerId) {
        log.debug("판매자 조회: sellerId={}", sellerId);
        return sellerDAO.findById(sellerId);
    }

    @Override
    public Optional<Seller> findByEmail(String email) {
        log.debug("판매자 이메일 조회: email={}", email);
        return sellerDAO.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        log.debug("이메일 중복 체크: email={}", email);
        return sellerDAO.existsByEmail(email) || buyerDAO.existsByEmail(email);
    }

    @Override
    public boolean existsByBizRegNo(String bizRegNo) {
        log.debug("사업자등록번호 중복 체크: bizRegNo={}", bizRegNo);
        return sellerDAO.existsByBizRegNo(bizRegNo);
    }

    @Override
    public boolean existsByShopName(String shopName) {
        log.debug("상호명 중복 체크: shopName={}", shopName);
        return sellerDAO.existsByShopName(shopName);
    }

    @Override
    public Optional<Seller> findByBizRegNo(String bizRegNo) {
        log.debug("사업자등록번호로 판매자 조회: bizRegNo={}", bizRegNo);
        return sellerDAO.findByBizRegNo(bizRegNo);
    }

    @Override
    public int update(Long sellerId, Seller seller) {
        log.info("판매자 정보 수정: sellerId={}", sellerId);

        // 수정하려는 상호명이 다른 활성 회원과 중복되는지 확인
        if (seller.getShopName() != null) {
            Optional<Seller> existingSeller = sellerDAO.findById(sellerId);
            if (existingSeller.isPresent()) {
                String currentShopName = existingSeller.get().getShopName();


            }
        }

        int result = sellerDAO.update(sellerId, seller);
        log.info("판매자 정보 수정 완료: sellerId={}, 수정된 행 수={}", sellerId, result);
        return result;
    }

    @Override
    public int withdrawWithReason(Long sellerId, String reason) {
        log.info("판매자 탈퇴 처리 - sellerId={}", sellerId);

        // 판매자 조회
        Optional<Seller> sellerOpt = sellerDAO.findById(sellerId);
        if (sellerOpt.isEmpty()) {
            throw new BusinessException("존재하지 않는 판매자입니다.");
        }

        Seller seller = sellerOpt.get();

        // 탈퇴 가능 여부 체크 (Service 메서드 사용)
        if (!canWithdraw(seller)) {
            String blockReason = getWithdrawBlockReason(seller);
            log.warn("탈퇴 불가 상태: sellerId={}, reason={}", sellerId, blockReason);
            throw new BusinessException(blockReason != null ? blockReason : "탈퇴할 수 없습니다.");
        }

        // 탈퇴 처리
        log.info("탈퇴 가능 확인 완료, 탈퇴 진행: sellerId={}", sellerId);

        int result = sellerDAO.withdrawWithReason(sellerId, reason);
        log.info("판매자 탈퇴 처리 완료: sellerId={}, 처리된 행 수={}", sellerId, result);
        return result;
    }

    @Override
    public List<Seller> findWithdrawnMembers() {
        log.debug("탈퇴 회원 목록 조회");
        return sellerDAO.findWithdrawnMembers();
    }

    @Override
    public List<Seller> findAll() {
        log.debug("전체 판매자 목록 조회");
        return sellerDAO.findAll();
    }

    @Override
    public int reactivate(String email, String password) {
        log.info("탈퇴 회원 재활성화: email={}", email);

        // 재활성화 대상 확인
        Optional<Seller> sellerOpt = sellerDAO.findByEmail(email);
        if (sellerOpt.isEmpty()) {
            log.warn("재활성화할 회원을 찾을 수 없음: email={}", email);
            throw new BusinessException("해당 이메일의 회원을 찾을 수 없습니다.");
        }

        Seller seller = sellerOpt.get();
        if (!isWithdrawn(seller)) {
            log.warn("탈퇴하지 않은 회원의 재활성화 시도: email={}", email);
            throw new BusinessException("탈퇴하지 않은 회원입니다.");
        }

        int result = sellerDAO.reactivate(email, password);
        if (result > 0) {
            log.info("탈퇴 회원 재활성화 성공: email={}", email);
        } else {
            log.warn("탈퇴 회원 재활성화 실패: email={}", email);
        }

        return result;
    }

    @Override
    public int rejoin(Seller seller) {
        log.info("탈퇴 회원 재가입: email={}", seller.getEmail());

        // 재가입 대상 확인
        Optional<Seller> existingSellerOpt = sellerDAO.findByEmail(seller.getEmail());
        if (existingSellerOpt.isEmpty()) {
            log.warn("재가입할 회원을 찾을 수 없음: email={}", seller.getEmail());
            throw new BusinessException("해당 이메일의 회원을 찾을 수 없습니다.");
        }

        Seller existingSeller = existingSellerOpt.get();
        if (!isWithdrawn(existingSeller)) {
            log.warn("탈퇴하지 않은 회원의 재가입 시도: email={}", seller.getEmail());
            throw new BusinessException("탈퇴하지 않은 회원입니다.");
        }

        // 상호명 중복 체크 (활성 회원만)


        int result = sellerDAO.rejoin(seller);
        if (result > 0) {
            log.info("탈퇴 회원 재가입 성공: email={}", seller.getEmail());
        } else {
            log.warn("탈퇴 회원 재가입 실패: email={}", seller.getEmail());
        }

        return result;
    }

    // ============= 비즈니스 로직 메서드들 =============

    /**
     * 탈퇴 여부 확인
     */
    public boolean isWithdrawn(Seller seller) {
        return seller != null && seller.getWithdrawnAt() != null;
    }

    /**
     * 활성 상태 여부 확인
     */
    public boolean isActive(Seller seller) {
        return seller != null &&
            seller.getStatus() != null &&
            MemberStatus.ACTIVE.getCode().equals(seller.getStatus()) &&
            !isWithdrawn(seller);
    }

    /**
     * 로그인 가능 여부 확인
     */
    public boolean canLogin(Seller seller) {
        if (seller == null || seller.getStatus() == null) return false;

        try {
            MemberStatus memberStatus = MemberStatus.fromCode(seller.getStatus());
            return memberStatus.canLogin() && !isWithdrawn(seller);
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 상태 코드: sellerId={}, status={}",
                seller.getSellerId(), seller.getStatus());
            return false;
        }
    }

    /**
     * 서비스 이용현황 확인
     */
    public ServiceUsage getServiceUsage(Seller seller) {
        // 1차 프로젝트에서는 기본적으로 이용현황 없음
        return ServiceUsage.NONE;
    }

    /**
     * 탈퇴 가능 여부 확인
     */
    public boolean canWithdraw(Seller seller) {
        if (!isActive(seller)) return false;

        ServiceUsage usage = getServiceUsage(seller);
        return usage.canWithdraw();
    }

    /**
     * 탈퇴 불가 사유 반환
     */
    public String getWithdrawBlockReason(Seller seller) {
        if (seller == null) return "판매자 정보가 없습니다.";
        if (isWithdrawn(seller)) return "이미 탈퇴한 계정입니다.";
        if (!isActive(seller)) return "비활성 계정입니다.";

        ServiceUsage usage = getServiceUsage(seller);
        if (!usage.canWithdraw()) {
            return "서비스 이용 중이므로 탈퇴할 수 없습니다.";
        }

        return null; // 탈퇴 가능
    }

    /**
     * 상태 표시 문자열 반환
     */
    public String getStatusDisplay(Seller seller) {
        if (seller == null || seller.getStatus() == null) {
            return "알 수 없음";
        }
        try {
            return MemberStatus.fromCode(seller.getStatus()).getCode();
        } catch (IllegalArgumentException e) {
            return "알 수 없음";
        }
    }

    /**
     * 로그인 검증 (이메일 + 상태 확인)
     */
    public boolean validateLogin(String email) {
        Optional<Seller> sellerOpt = findByEmail(email);
        if (sellerOpt.isEmpty()) {
            return false;
        }

        return canLogin(sellerOpt.get());
        // 비밀번호 검증은 Controller에서 PasswordEncoder로 처리
    }

    /**
     * 수정 폼에서 Seller 엔티티로 업데이트
     */
    public void updateSellerFromForm(Seller seller, SellerEditForm form) {
        if (seller == null || form == null) return;

        // 비밀번호가 입력된 경우에만 업데이트
        if (form.getPassword() != null && !form.getPassword().trim().isEmpty()) {
            seller.setPassword(form.getPassword()); // Controller에서 암호화 처리
        }

        seller.setTel(form.getTel());

        // 주소 정보 결합
        String fullAddress = buildFullAddress(
            form.getPostNumber(),
            form.getShopAddress(),
            form.getDetailAddress()
        );
        seller.setShopAddress(fullAddress);

        // postNumber 설정
        if (form.getPostNumber() != null) {
            seller.setPostNumber(form.getPostNumber());
        }
    }

    /**
     * 판매자 대시보드용 상태 정보 생성
     */
    public Map<String, Object> createDashboardInfo(Seller seller) {
        if (seller == null) return new HashMap<>();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("sellerId", seller.getSellerId());
        dashboard.put("shopName", seller.getShopName() != null ? seller.getShopName() : "");
        dashboard.put("email", seller.getEmail() != null ? seller.getEmail() : "");
        dashboard.put("status", getStatusDisplay(seller) != null ? getStatusDisplay(seller) : "");
        
        try {
            ServiceUsage serviceUsage = getServiceUsage(seller);
            dashboard.put("serviceUsage", serviceUsage != null ? serviceUsage.getDescription() : "");
        } catch (Exception e) {
            dashboard.put("serviceUsage", "");
        }
        
        dashboard.put("canWithdraw", canWithdraw(seller));
        dashboard.put("withdrawBlockReason", getWithdrawBlockReason(seller) != null ? getWithdrawBlockReason(seller) : "");

        return dashboard;
    }

    // ============= Private 헬퍼 메서드들 =============

    /**
     * 주소 파싱 (구매자와 동일한 방식)
     */
    private String[] parseAddress(String shopAddress) {
        if (shopAddress == null || shopAddress.trim().isEmpty()) {
            return new String[]{"", ""};
        }
        
        // "(우편번호) 주소" 형태에서 우편번호 부분만 제거
        String address = shopAddress.replaceFirst("^\\(\\d+\\)\\s*", "").trim();
        
        return new String[]{address, ""};
    }

    /**
     * 전체 주소 결합
     */
    private String buildFullAddress(String postcode, String address, String detailAddress) {
        String pc = postcode != null ? postcode.trim() : "";
        String addr = address != null ? address.trim() : "";
        String detail = detailAddress != null ? detailAddress.trim() : "";
        return String.format("%s|%s|%s", pc, addr, detail);
    }
}