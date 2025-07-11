package com.kh.project.domain.seller.svc;

import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.entity.MemberGubun;
import com.kh.project.domain.entity.MemberStatus;
import com.kh.project.domain.seller.dao.SellerDAO;
import com.kh.project.web.common.CodeNameInfo;
import com.kh.project.web.common.MemberGubunUtils;
import com.kh.project.web.exception.BusinessException;
import com.kh.project.web.exception.MemberException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 판매자 서비스 구현체
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SellerSVCImpl implements SellerSVC {

  private final SellerDAO sellerDAO;

  @Override
  public Seller join(Seller seller) {
    log.info("판매자 회원가입 시도: email={}, bizRegNo={}", seller.getEmail(), seller.getBizRegNo());

    // 중복 체크
    if (sellerDAO.existsByEmail(seller.getEmail())) {
      throw new MemberException.EmailDuplicationException(seller.getEmail());
    }
    if (sellerDAO.existsByBizRegNo(seller.getBizRegNo())) {
      throw new BusinessException("이미 등록된 사업자등록번호입니다.");
    }
    if (sellerDAO.existsByShopName(seller.getShopName())) {
      throw new BusinessException("이미 사용중인 상점명입니다.");
    }
    if (sellerDAO.existsByName(seller.getName())) {
      throw new BusinessException("이미 등록된 대표자명입니다.");
    }
    if (sellerDAO.existsByShopAddress(seller.getShopAddress())) {
      throw new BusinessException("이미 등록된 사업장 주소입니다.");
    }

    // 사업자번호 형식 검증
    if (!validateBizRegNo(seller.getBizRegNo())) {
      throw new BusinessException("올바르지 않은 사업자등록번호 형식입니다.");
    }

    // 기본값 설정
    seller.setStatus("활성화");

    Seller savedSeller = sellerDAO.save(seller);
    log.info("판매자 회원가입 성공: sellerId={}, email={}", savedSeller.getSellerId(), savedSeller.getEmail());

    return savedSeller;
  }

  @Override
  @Transactional(readOnly = true)
  public Seller login(String email, String password) {
    log.info("판매자 로그인 시도: email={}", email);

    Seller seller = sellerDAO.findByEmail(email)
        .orElseThrow(() -> new MemberException.LoginFailedException());

    if (!seller.canLogin()) {
      log.warn("로그인 불가능한 상태: email={}, status={}", email, seller.getStatus());
      throw new MemberException.LoginFailedException();
    }

    if (!seller.getPassword().equals(password)) {
      log.warn("비밀번호 불일치: email={}", email);
      throw new MemberException.LoginFailedException();
    }

    log.info("판매자 로그인 성공: email={}", email);
    return seller;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Seller> findById(Long sellerId) {
    return sellerDAO.findById(sellerId);
  }

  @Override
  public int update(Long sellerId, Seller seller) {
    log.info("판매자 정보 수정: sellerId={}", sellerId);

    // 중복 체크
    if (seller.getShopName() != null) {
      Optional<Seller> existingSeller = sellerDAO.findById(sellerId);
      if (existingSeller.isPresent() &&
          !seller.getShopName().equals(existingSeller.get().getShopName()) &&
          sellerDAO.existsByShopName(seller.getShopName())) {
        throw new BusinessException("이미 사용중인 상점명입니다.");
      }
    }

    return sellerDAO.update(sellerId, seller);
  }

  @Override
  public int withdraw(Long sellerId, String reason) {
    log.info("판매자 탈퇴 처리: sellerId={}, 사유={}", sellerId, reason);

    Optional<Seller> sellerOpt = sellerDAO.findById(sellerId);
    if (sellerOpt.isEmpty()) {
      throw new MemberException.MemberNotFoundException();
    }

    Seller seller = sellerOpt.get();
    if (seller.isWithdrawn()) {
      throw new MemberException.AlreadyWithdrawnException();
    }

    // 탈퇴 사유 설정
    String withdrawReason = (reason != null && !reason.trim().isEmpty()) ?
        reason : "사업 종료";

    return sellerDAO.withdrawWithReason(sellerId, withdrawReason);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByEmail(String email) {
    return sellerDAO.existsByEmail(email);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByBizRegNo(String bizRegNo) {
    return sellerDAO.existsByBizRegNo(bizRegNo);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByShopName(String shopName) {
    return sellerDAO.existsByShopName(shopName);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByName(String name) {
    return sellerDAO.existsByName(name);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByShopAddress(String shopAddress) {
    return sellerDAO.existsByShopAddress(shopAddress);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean checkPassword(Long sellerId, String password) {
    if (password == null) return false;

    return sellerDAO.findById(sellerId)
        .map(seller -> seller.getPassword().equals(password.trim()))
        .orElse(false);
  }

  /**
   * 회원 등급 업그레이드
   */
  @Override
  @Transactional(readOnly = true)
  public List<Seller> getWithdrawnMembers() {
    log.info("탈퇴한 판매자 목록 조회");
    return sellerDAO.findWithdrawnMembers();
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> getServiceUsage(Long sellerId) {
    log.info("판매자 서비스 이용현황 조회: sellerId={}", sellerId);

    Optional<Seller> sellerOpt = sellerDAO.findById(sellerId);
    if (sellerOpt.isEmpty()) {
      throw new MemberException.MemberNotFoundException();
    }

    Seller seller = sellerOpt.get();

    // 서비스 이용현황 기본값
    int activeOrderCount = 0;
    int activeSaleCount = 0;
    int completedOrderCount = 3;
    int unresolvedDisputeCount = 0;
    int openInquiryCount = 0;
    int pointBalance = 0;
    int pendingRefundAmount = 0;

    // 탈퇴 불가 사유 검사
    List<String> withdrawBlockReasons = new java.util.ArrayList<>();
    
    if (activeOrderCount > 0) {
      withdrawBlockReasons.add("진행중인 주문이 " + activeOrderCount + "건 있습니다.");
    }
    
    if (activeSaleCount > 0) {
      withdrawBlockReasons.add("판매중인 상품이 " + activeSaleCount + "개 있습니다.");
    }
    
    if (unresolvedDisputeCount > 0) {
      withdrawBlockReasons.add("미해결 분쟁이 " + unresolvedDisputeCount + "건 있습니다.");
    }
    
    if (pointBalance > 0) {
      withdrawBlockReasons.add("사용하지 않은 포인트가 " + pointBalance + "P 있습니다.");
    }
    
    if (pendingRefundAmount > 0) {
      withdrawBlockReasons.add("환불 대기 금액이 " + pendingRefundAmount + "원 있습니다.");
    }

    // 가입 기간 검사
    if (seller.getCdate() != null) {
      long daysSinceJoin = (System.currentTimeMillis() - seller.getCdate().getTime()) / (1000 * 60 * 60 * 24);
      if (daysSinceJoin < 3) {
        withdrawBlockReasons.add("사업자 가입 후 3일이 지나지 않아 탈퇴할 수 없습니다.");
      }
    }

    boolean canWithdraw = withdrawBlockReasons.isEmpty();

    Map<String, Object> serviceUsage = new HashMap<>();
    serviceUsage.put("memberId", sellerId);
    serviceUsage.put("memberType", "SELLER");
    serviceUsage.put("joinDate", seller.getCdate());
    serviceUsage.put("activeOrderCount", activeOrderCount);
    serviceUsage.put("activeSaleCount", activeSaleCount);
    serviceUsage.put("completedOrderCount", completedOrderCount);
    serviceUsage.put("unresolvedDisputeCount", unresolvedDisputeCount);
    serviceUsage.put("openInquiryCount", openInquiryCount);
    serviceUsage.put("pointBalance", pointBalance);
    serviceUsage.put("pendingRefundAmount", pendingRefundAmount);
    serviceUsage.put("canWithdraw", canWithdraw);
    serviceUsage.put("withdrawBlockReasons", withdrawBlockReasons);

    log.info("판매자 서비스 이용현황 조회 완료: sellerId={}, canWithdraw={}, blockReasons={}", 
             sellerId, canWithdraw, withdrawBlockReasons.size());

    return serviceUsage;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean canWithdraw(Long sellerId) {
    Map<String, Object> usage = getServiceUsage(sellerId);
    return (Boolean) usage.get("canWithdraw");
  }

  @Override
  public boolean validateBizRegNo(String bizRegNo) {
    if (bizRegNo == null || bizRegNo.trim().isEmpty()) {
      return false;
    }

    // 형식 검증
    return bizRegNo.matches("^\\d{3}-\\d{2}-\\d{5}$");
  }

  @Override
  public boolean canLogin(Seller seller) {
    return seller != null && seller.canLogin();
  }

  @Override
  public boolean isWithdrawn(Seller seller) {
    return seller != null && seller.isWithdrawn();
  }

  @Override
  public CodeNameInfo getGubunInfo(Seller seller) {
    if (seller == null || seller.getMemberGubun() == null) {
      return CodeNameInfo.of("UNKNOWN", "알 수 없음");
    }

    String code = seller.getMemberGubun();
    String name = MemberGubunUtils.getDescriptionByCode(code);

    return CodeNameInfo.of(code, name);
  }

  @Override
  public CodeNameInfo getStatusInfo(Seller seller) {
    if (seller == null || seller.getStatus() == null) {
      return CodeNameInfo.of("UNKNOWN", "알 수 없음");
    }

    String code = seller.getStatus();
    String name = MemberStatus.getDescriptionByCode(code);

    return CodeNameInfo.of(code, name);
  }

  @Override
  public CodeNameInfo getShopInfo(Seller seller) {
    if (seller == null) {
      return null;
    }

    return CodeNameInfo.of(seller.getSellerId().toString(), seller.getShopName());
  }
}
