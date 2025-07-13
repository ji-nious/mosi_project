package com.kh.project.domain.buyer.svc;

import com.kh.project.domain.entity.Buyer;
import com.kh.project.web.common.CodeNameInfo;
import com.kh.project.web.common.dto.MemberStatusInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 구매자 서비스 인터페이스
 */
public interface BuyerSVC {
  
  /**
   * 구매자 회원가입
   * @param buyer 구매자 정보
   * @return Buyer - 저장된 구매자 정보
   */
  Buyer join(Buyer buyer);
  
  /**
   * 구매자 로그인
   */
  Buyer login(String email, String password);
  
  /**
   * 구매자 정보 조회
   * @return Optional<Buyer> - 조회된 구매자 정보
   */
  Optional<Buyer> findById(Long buyerId);

  /**
   * 이메일로 구매자 조회
   * @param email 이메일 주소
   * @return Optional<Buyer> - 조회된 구매자 정보
   */
  Optional<Buyer> findByEmail(String email);
  
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

  /**
   * 탈퇴한 회원 계정 재활성화
   * @param email 대상 이메일
   * @param password 새로운 비밀번호
   * @return 재활성화된 구매자 정보
   */
  Optional<Buyer> reactivate(String email, String password);

  // 관리 기능
  List<Buyer> getWithdrawnMembers();

  // 프로세스 설계서: 비밀번호 재확인과 서비스 이용현황 조회
  /**
   * 서비스 이용현황 조회 및 탈퇴 가능성 판단
   */
  MemberStatusInfo getServiceUsage(Long buyerId);
  
  /**
   * 탈퇴 가능 여부 판단
   */
  boolean canWithdraw(Long buyerId);
}
