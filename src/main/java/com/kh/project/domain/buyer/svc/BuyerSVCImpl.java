package com.kh.project.domain.buyer.svc;

import com.kh.project.domain.buyer.dao.BuyerDAO;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.domain.entity.MemberGubun;
import com.kh.project.domain.entity.MemberStatus;
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
 * 구매자 서비스 구현체
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BuyerSVCImpl implements BuyerSVC {

  private final BuyerDAO buyerDAO;

  @Override
  public Buyer join(Buyer buyer) {
    log.info("구매자 회원가입 시도: email={}", buyer.getEmail());

    // 1. 이메일로 회원 조회
    Optional<Buyer> existingBuyerOpt = buyerDAO.findByEmail(buyer.getEmail());

    if (existingBuyerOpt.isPresent()) {
      Buyer existingBuyer = existingBuyerOpt.get();
      // 2. 이미 존재하는 회원 처리
      if (existingBuyer.isWithdrawn()) {
        // 2-1. 탈퇴한 회원이면 재가입 처리
        log.info("탈퇴한 회원 재가입 시도: email={}", buyer.getEmail());
        buyer.setStatus("활성화"); // 상태 명시적 설정
        int updatedRows = buyerDAO.rejoin(buyer);
        if (updatedRows == 0) {
          throw new MemberException.MemberNotFoundException(); // 재가입 대상이 사라진 경우
        }
        log.info("회원 재가입 성공: email={}", buyer.getEmail());
        // rejoin은 ID를 반환하지 않으므로, 기존 ID를 설정하여 반환
        buyer.setBuyerId(existingBuyer.getBuyerId()); 
        return buyer;

      } else {
        // 2-2. 활성/정지 등 다른 상태의 회원이면 중복 오류
        throw new MemberException.EmailDuplicationException(buyer.getEmail());
      }
    }

    // 3. 신규 회원 가입
    // 닉네임 중복 체크 (신규 가입 시에만)
    if (buyer.getNickname() != null && buyerDAO.existsByNickname(buyer.getNickname())) {
      throw new BusinessException("이미 사용중인 닉네임입니다.");
    }

    // 기본값 설정
    buyer.setStatus("활성화");

    Buyer savedBuyer = buyerDAO.save(buyer);
    log.info("구매자 회원가입 성공: buyerId={}, email={}", savedBuyer.getBuyerId(), savedBuyer.getEmail());

    return savedBuyer;
  }

  @Override
  @Transactional(readOnly = true)
  public Buyer login(String email, String password) {
    log.info("구매자 로그인 시도: email={}", email);

    Buyer buyer = buyerDAO.findByEmail(email)
        .orElseThrow(() -> new MemberException.LoginFailedException());

    // 1. 탈퇴한 회원인지 확인
    if (buyer.isWithdrawn()) {
      log.warn("탈퇴한 회원의 로그인 시도: email={}", email);
      throw new MemberException.AlreadyWithdrawnException();
    }

    // 2. 활성 상태 계정인지 확인
    if (!buyer.canLogin()) {
      log.warn("로그인 불가능한 상태: email={}, status={}", email, buyer.getStatus());
      throw new MemberException.LoginFailedException();
    }

    // 3. 비밀번호 확인
    if (!buyer.getPassword().equals(password)) {
      log.warn("비밀번호 불일치: email={}", email);
      throw new MemberException.LoginFailedException();
    }

    log.info("구매자 로그인 성공: email={}", email);
    return buyer;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Buyer> findById(Long buyerId) {
    return buyerDAO.findById(buyerId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Buyer> findByEmail(String email) {
    return buyerDAO.findByEmail(email);
  }

  @Override
  public int update(Long buyerId, Buyer buyer) {
    log.info("구매자 정보 수정: buyerId={}", buyerId);

    // 중복 체크
    if (buyer.getNickname() != null) {
      Optional<Buyer> existingBuyer = buyerDAO.findById(buyerId);
      if (existingBuyer.isPresent() &&
          !buyer.getNickname().equals(existingBuyer.get().getNickname()) &&
          buyerDAO.existsByNickname(buyer.getNickname())) {
        throw new BusinessException("이미 사용중인 닉네임입니다.");
      }
    }

    return buyerDAO.update(buyerId, buyer);
  }

  @Override
  public int withdraw(Long buyerId, String reason) {
    // 1. 탈퇴 가능 여부 확인
    if (!canWithdraw(buyerId)) {
      Map<String, Object> usage = getServiceUsage(buyerId);
      List<String> blockReasons = (List<String>) usage.get("withdrawBlockReasons");
      String reasonText = String.join(", ", blockReasons);
      throw new BusinessException("탈퇴할 수 없습니다. 사유: " + reasonText);
    }
    
    // 2. 구매자 존재 여부 확인 (canWithdraw에서 이미 확인하지만, 안전을 위해 유지)
    Optional<Buyer> buyer = buyerDAO.findById(buyerId);
    if (buyer.isEmpty()) {
      throw new BusinessException("구매자를 찾을 수 없습니다: " + buyerId);
    }

    // 3. 탈퇴 처리
    int updatedRows = buyerDAO.withdrawWithReason(buyerId, reason);
    if (updatedRows == 0) {
      throw new BusinessException("구매자 탈퇴 처리에 실패했습니다: " + buyerId);
    }

    log.info("구매자 탈퇴 완료: buyerId={}, reason={}", buyerId, reason);
    return updatedRows;
  }

  /**
   * 회원 등급 업그레이드
   */
  @Override
  @Transactional(readOnly = true)
  public boolean existsByEmail(String email) {
    return buyerDAO.existsByEmail(email);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsByNickname(String nickname) {
    return buyerDAO.existsByNickname(nickname);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean checkPassword(Long buyerId, String password) {
    if (password == null) {
      log.warn("비밀번호 확인 실패: 입력된 비밀번호가 null입니다. buyerId={}", buyerId);
      return false;
    }

    Optional<Buyer> buyerOpt = buyerDAO.findById(buyerId);
    if (buyerOpt.isEmpty()) {
      log.warn("비밀번호 확인 실패: 구매자를 찾을 수 없습니다. buyerId={}", buyerId);
      return false;
    }

    Buyer buyer = buyerOpt.get();
    String inputPassword = password.trim();
    String storedPassword = buyer.getPassword();
    
    boolean isValid = storedPassword.equals(inputPassword);
    
    log.info("비밀번호 확인: buyerId={}, 입력비밀번호길이={}, 저장비밀번호길이={}, 일치여부={}", 
             buyerId, inputPassword.length(), 
             storedPassword != null ? storedPassword.length() : 0, isValid);
    
    if (!isValid) {
      log.warn("비밀번호 불일치: buyerId={}, 입력='{}', 저장='{}'", 
               buyerId, inputPassword, storedPassword);
    }
    
    return isValid;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Buyer> getWithdrawnMembers() {
    return buyerDAO.findWithdrawnMembers();
  }

  @Override
  @Transactional(readOnly = true)
  public Map<String, Object> getServiceUsage(Long buyerId) {
    log.info("구매자 서비스 이용현황 조회: buyerId={}", buyerId);

    Optional<Buyer> buyerOpt = buyerDAO.findById(buyerId);
    if (buyerOpt.isEmpty()) {
      throw new MemberException.MemberNotFoundException();
    }

    Buyer buyer = buyerOpt.get();

    // 서비스 이용현황 기본값
    int activeOrderCount = 0;
    int completedOrderCount = 5;
    int unresolvedDisputeCount = 0;
    int openInquiryCount = 0;
    int pointBalance = 0;
    int pendingRefundAmount = 0;

    // 탈퇴 불가 사유 검사
    List<String> withdrawBlockReasons = new java.util.ArrayList<>();
    
    if (activeOrderCount > 0) {
      withdrawBlockReasons.add("진행중인 주문이 " + activeOrderCount + "건 있습니다.");
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
    if (buyer.getCdate() != null) {
      long daysSinceJoin = (System.currentTimeMillis() - buyer.getCdate().getTime()) / (1000 * 60 * 60 * 24);
      if (daysSinceJoin < 1) {
        withdrawBlockReasons.add("가입 후 1일이 지나지 않아 탈퇴할 수 없습니다.");
      }
    }

    boolean canWithdraw = withdrawBlockReasons.isEmpty();

    Map<String, Object> serviceUsage = new HashMap<>();
    serviceUsage.put("memberId", buyerId);
    serviceUsage.put("memberType", "BUYER");
    serviceUsage.put("joinDate", buyer.getCdate());
    serviceUsage.put("activeOrderCount", activeOrderCount);
    serviceUsage.put("completedOrderCount", completedOrderCount);
    serviceUsage.put("unresolvedDisputeCount", unresolvedDisputeCount);
    serviceUsage.put("openInquiryCount", openInquiryCount);
    serviceUsage.put("pointBalance", pointBalance);
    serviceUsage.put("pendingRefundAmount", pendingRefundAmount);
    serviceUsage.put("canWithdraw", canWithdraw);
    serviceUsage.put("withdrawBlockReasons", withdrawBlockReasons);

    log.info("구매자 서비스 이용현황 조회 완료: buyerId={}, canWithdraw={}, blockReasons={}", 
             buyerId, canWithdraw, withdrawBlockReasons.size());

    return serviceUsage;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean canWithdraw(Long buyerId) {
    Map<String, Object> usage = getServiceUsage(buyerId);
    return (Boolean) usage.get("canWithdraw");
  }

  @Override
  public Optional<Buyer> reactivate(String email, String password) {
    log.info("구매자 계정 재활성화 시도: email={}", email);
    
    int updatedRows = buyerDAO.reactivate(email, password);
    if (updatedRows == 0) {
      log.warn("일치하는 탈퇴 계정이 없어 재활성화에 실패했습니다: email={}", email);
      return Optional.empty();
    }
    
    log.info("구매자 계정 재활성화 성공: email={}", email);
    return buyerDAO.findByEmail(email);
  }

  @Override
  public boolean canLogin(Buyer buyer) {
    return buyer != null && buyer.canLogin();
  }

  @Override
  public boolean isWithdrawn(Buyer buyer) {
    return buyer != null && buyer.isWithdrawn();
  }

  @Override
  public CodeNameInfo getGubunInfo(Buyer buyer) {
    if (buyer == null || buyer.getMemberGubun() == null) {
      return CodeNameInfo.of("UNKNOWN", "알 수 없음");
    }

    String code = buyer.getMemberGubun();
    String name = MemberGubunUtils.getDescriptionByCode(code);

    return CodeNameInfo.of(code, name);
  }

  @Override
  public CodeNameInfo getStatusInfo(Buyer buyer) {
    if (buyer == null || buyer.getStatus() == null) {
      return CodeNameInfo.of("UNKNOWN", "알 수 없음");
    }

    String code = buyer.getStatus();
    String name = MemberStatus.getDescriptionByCode(code);

    return CodeNameInfo.of(code, name);
  }
}