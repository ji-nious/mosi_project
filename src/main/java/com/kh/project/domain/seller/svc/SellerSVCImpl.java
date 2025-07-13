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

    // 1. 이메일로 기존 회원 정보 조회 (탈퇴 회원 포함)
    Optional<Seller> existingSellerOpt = sellerDAO.findByEmail(seller.getEmail());

    if (existingSellerOpt.isPresent()) {
      Seller existingSeller = existingSellerOpt.get();
      
      // 2. 이미 존재하는 회원 상태에 따라 분기
      if (existingSeller.isWithdrawn()) {
        // 2-1. 탈퇴한 회원이면, 전체 정보로 재활성화 시도
        log.info("탈퇴한 판매자 계정 재활성화 시도: email={}", seller.getEmail());
        
        // 중복 체크 (기존 정보와 다른 경우에만)
        if (!seller.getBizRegNo().equals(existingSeller.getBizRegNo()) && 
            sellerDAO.existsByBizRegNo(seller.getBizRegNo())) {
          throw new BusinessException("이미 등록된 사업자등록번호입니다.");
        }
        
        if (!seller.getShopName().equals(existingSeller.getShopName()) && 
            sellerDAO.existsByShopName(seller.getShopName())) {
          throw new BusinessException("이미 사용중인 상점명입니다.");
        }
        
        if (!seller.getName().equals(existingSeller.getName()) && 
            sellerDAO.existsByName(seller.getName())) {
          throw new BusinessException("이미 등록된 대표자명입니다.");
        }
        
        if (!seller.getShopAddress().equals(existingSeller.getShopAddress()) && 
            sellerDAO.existsByShopAddress(seller.getShopAddress())) {
          throw new BusinessException("이미 등록된 사업장 주소입니다.");
        }
        
        int updatedRows = sellerDAO.rejoin(seller);
        if (updatedRows == 0) {
          throw new BusinessException("계정 재활성화에 실패했습니다.");
        }
        
        return sellerDAO.findByEmail(seller.getEmail())
          .orElseThrow(() -> new BusinessException("재활성화된 계정을 찾을 수 없습니다."));

      } else {
        // 2-2. 활성/정지 등 다른 상태의 회원이면 중복 오류
        log.warn("이미 존재하는 활성 계정으로 가입 시도: email={}", seller.getEmail());
        throw new MemberException.EmailDuplicationException(seller.getEmail());
      }
    }

    // 3. 신규 회원 가입 - 중복 체크 (이메일은 이미 1단계에서 체크됨)
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

    // 1. 탈퇴한 회원인지 확인
    if (seller.isWithdrawn()) {
      log.warn("탈퇴한 판매자의 로그인 시도: email={}", email);
      throw new MemberException.AlreadyWithdrawnException();
    }

    // 2. 활성 상태 계정인지 확인
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

    // 1. 탈퇴 가능 여부 확인
    if (!canWithdraw(sellerId)) {
      Map<String, Object> usage = getServiceUsage(sellerId);
      @SuppressWarnings("unchecked")
      List<String> blockReasons = (List<String>) usage.get("withdrawBlockReasons");
      String reasonText = String.join(", ", blockReasons);
      throw new BusinessException("탈퇴할 수 없습니다. 사유: " + reasonText);
    }

    // 2. 판매자 존재 여부 확인
    Optional<Seller> sellerOpt = sellerDAO.findById(sellerId);
    if (sellerOpt.isEmpty()) {
      throw new MemberException.MemberNotFoundException();
    }

    Seller seller = sellerOpt.get();
    if (seller.isWithdrawn()) {
      throw new MemberException.AlreadyWithdrawnException();
    }

    // 3. 탈퇴 사유 설정
    String withdrawReason = (reason != null && !reason.trim().isEmpty()) ?
        reason : "사업 종료";

    // 4. 탈퇴 처리
    int updatedRows = sellerDAO.withdrawWithReason(sellerId, withdrawReason);
    if (updatedRows == 0) {
      throw new BusinessException("판매자 탈퇴 처리에 실패했습니다: " + sellerId);
    }

    log.info("판매자 탈퇴 완료: sellerId={}, reason={}", sellerId, withdrawReason);
    return updatedRows;
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

    // 가입 기간 검사 - 제거됨 (즉시 탈퇴 가능)

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

  @Override
  public Optional<Seller> reactivate(String email, String password) {
    log.info("판매자 계정 재활성화 시도: email={}", email);
    
    int updatedRows = sellerDAO.reactivate(email, password);
    if (updatedRows == 0) {
      log.warn("일치하는 탈퇴 계정이 없어 재활성화에 실패했습니다: email={}", email);
      return Optional.empty();
    }
    
    log.info("판매자 계정 재활성화 성공: email={}", email);
    return sellerDAO.findByEmail(email);
  }
}
