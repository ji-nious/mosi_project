package com.kh.project.domain.buyer.svc;

import com.kh.project.domain.buyer.dao.BuyerDAO;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.domain.entity.MemberGubun;
import com.kh.project.domain.entity.MemberStatus;

import com.kh.project.web.common.form.MemberStatusInfo;
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
 * 구매자 서비스 구현
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BuyerSVCImpl implements BuyerSVC {

  private final BuyerDAO buyerDAO;

  @Override
  @Transactional
  public Buyer join(Buyer buyer) {
    log.info("구매자 회원가입 처리 시작: email={}", buyer.getEmail());

    // 1. 이메일로 기존 회원 정보 조회
    Optional<Buyer> existingBuyerOpt = buyerDAO.findByEmail(buyer.getEmail());

    if (existingBuyerOpt.isPresent()) {
      Buyer existingBuyer = existingBuyerOpt.get();
      
      // 2. 이미 존재하는 회원 상태에 따라 분기
      if (existingBuyer.isWithdrawn()) {
        // 2-1. 탈퇴한 회원이면, 전체 정보로 재활성화 시도
        log.info("탈퇴한 계정 재활성화 시도: email={}", buyer.getEmail());
        
        // 닉네임 중복 체크 (기존 닉네임과 다른 경우에만)
        if (!buyer.getNickname().equals(existingBuyer.getNickname()) && 
            buyerDAO.existsByNickname(buyer.getNickname())) {
          throw new BusinessException("이미 사용중인 닉네임입니다: " + buyer.getNickname());
        }
        
        int updatedRows = buyerDAO.rejoin(buyer);
        if (updatedRows == 0) {
          throw new BusinessException("계정 재활성화에 실패했습니다.");
        }
        
        return buyerDAO.findByEmail(buyer.getEmail())
          .orElseThrow(() -> new BusinessException("재활성화된 계정을 찾을 수 없습니다."));

      } else {
        // 2-2. 활성/정지 등 다른 상태의 회원이면 중복 오류
        log.warn("이미 존재하는 활성 계정으로 가입 시도: email={}", buyer.getEmail());
        throw new BusinessException("이미 사용중인 이메일입니다: " + buyer.getEmail());
      }
    }

    // 3. 신규 회원 가입
    log.info("신규 회원 가입 진행: email={}", buyer.getEmail());
    
    // 닉네임 중복 체크
    if (buyerDAO.existsByNickname(buyer.getNickname())) {
      throw new BusinessException("이미 사용중인 닉네임입니다: " + buyer.getNickname());
    }

    // 신규 회원으로 저장
    return buyerDAO.save(buyer);
  }

  @Override
  @Transactional(readOnly = true)
  public Buyer login(String email, String password) {
    // 이메일을 소문자로 변환 (대소문자 구분 방지)
    email = email.toLowerCase().trim();
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
  @SuppressWarnings("unchecked")
  public int withdraw(Long buyerId, String reason) {
    // 1. 탈퇴 가능 여부 확인
    if (!canWithdraw(buyerId)) {
      MemberStatusInfo statusInfo = getServiceUsage(buyerId);
      Map<String, Object> usage = statusInfo.toMap();
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
  public MemberStatusInfo getServiceUsage(Long buyerId) {
    log.info("구매자 서비스 이용현황 조회: buyerId={}", buyerId);

    Optional<Buyer> buyerOpt = buyerDAO.findById(buyerId);
    if (buyerOpt.isEmpty()) {
      throw new MemberException.MemberNotFoundException();
    }

    // TODO: 실제 데이터베이스에서 조회하도록 구현
    // 현재는 기본값 0으로 설정 (적립금, 쿠폰, 주문 테이블 구현 후 실제 조회 로직 추가)
    
    // 구현 예정:
    // int points = pointDAO.getPointBalance(buyerId);
    // int coupons = couponDAO.countUsableCouponsByBuyerId(buyerId);
    // int activeOrders = orderDAO.countActiveOrdersByBuyerId(buyerId);
    // int shippingOrders = orderDAO.countOrdersByBuyerIdAndStatus(buyerId, "SHIPPING");
    
    int points = 0;            // 보유 적립금
    int coupons = 0;           // 사용 가능한 쿠폰
    int activeOrders = 0;      // 진행 중인 주문
    int shippingOrders = 0;    // 배송 중인 주문
    
    return MemberStatusInfo.builder()
        .canWithdraw(true)
        .points(points)
        .coupons(coupons)
        .activeOrders(activeOrders)
        .shippingOrders(shippingOrders)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public boolean canWithdraw(Long buyerId) {
    MemberStatusInfo statusInfo = getServiceUsage(buyerId);
    return statusInfo.isCanWithdraw();
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
  public Map<String, String> getGubunInfo(Buyer buyer) {
    if (buyer == null || buyer.getMemberGubun() == null) {
      return Map.of("code", "UNKNOWN", "name", "알 수 없음");
    }
    
    MemberGubun gubun = MemberGubun.fromCodeOrDefault(buyer.getMemberGubun());
    String code = gubun.getCode();
    String name = gubun.getDescription();
    
    return Map.of("code", code, "name", name);
  }

  @Override
  public Map<String, String> getStatusInfo(Buyer buyer) {
    if (buyer == null || buyer.getStatus() == null) {
      return Map.of("code", "UNKNOWN", "name", "알 수 없음");
    }
    
    String code = buyer.getStatus();
    String name = MemberStatus.getDescriptionByCode(buyer.getStatus());
    
    return Map.of("code", code, "name", name);
  }
}