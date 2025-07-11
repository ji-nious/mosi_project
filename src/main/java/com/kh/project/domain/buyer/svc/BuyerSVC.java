package com.kh.project.domain.buyer.svc;

import com.kh.project.domain.entity.Buyer;
import com.kh.project.web.common.CodeNameInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 구매자 서비스 인터페이스
 */
public interface BuyerSVC {
  
  /**
   * 구매자 회원가입
   */
  Buyer join(Buyer buyer);
  
  /**
   * 구매자 로그인
   */
  Buyer login(String email, String password);
  
  /**
   * 구매자 정보 조회
   */
  Optional<Buyer> findById(Long buyerId);
  
  /**
   * 구매자 정보 수정
   */
  int update(Long buyerId, Buyer buyer);

  /**
   * 구매자 탈퇴 (논리 삭제)
   */
  int withdraw(Long buyerId, String reason);

  /**
   * 이메일 중복 체크
   */
  boolean existsByEmail(String email);

  /**
   * 닉네임 중복 체크
   */
  boolean existsByNickname(String nickname);

  /**
   * 비밀번호 확인
   */
  boolean checkPassword(Long buyerId, String password);

  // 비즈니스 로직
  boolean canLogin(Buyer buyer);
  boolean isWithdrawn(Buyer buyer);
  CodeNameInfo getGubunInfo(Buyer buyer);
  CodeNameInfo getStatusInfo(Buyer buyer);

  // 관리 기능
  List<Buyer> getWithdrawnMembers();

  // 프로세스 설계서: 비밀번호 재확인과 서비스 이용현황 조회
  /**
   * 서비스 이용현황 조회 및 탈퇴 가능성 판단
   */
  Map<String, Object> getServiceUsage(Long buyerId);
  
  /**
   * 탈퇴 가능 여부 판단
   */
  boolean canWithdraw(Long buyerId);
}
