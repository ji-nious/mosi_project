package com.kh.project.domain.buyer.svc;

import com.kh.project.domain.entity.Buyer;
import com.kh.project.web.common.CodeNameInfo;
import com.kh.project.web.common.dto.MemberStatusInfo;

import java.util.List;
import java.util.Optional;

/**
 * 구매자 서비스 인터페이스
 */
public interface BuyerSVC {
  
  // 회원가입
  Buyer join(Buyer buyer);
  
  // 로그인
  Buyer login(String email, String password);
  
  // 정보 조회
  Optional<Buyer> findById(Long buyerId);

  // 이메일로 회원 조회
  Optional<Buyer> findByEmail(String email);
  
  // 정보 수정
  int update(Long buyerId, Buyer buyer);

  // 회원 탈퇴
  int withdraw(Long buyerId, String reason);

  // 이메일 중복 체크
  boolean existsByEmail(String email);

  // 닉네임 중복 체크
  boolean existsByNickname(String nickname);

  // 비밀번호 확인
  boolean checkPassword(Long buyerId, String password);

  // 로그인 가능 여부 확인
  boolean canLogin(Buyer buyer);
  
  // 탈퇴 여부 확인
  boolean isWithdrawn(Buyer buyer);
  
  // 회원 등급 정보 조회
  CodeNameInfo getGubunInfo(Buyer buyer);
  
  // 회원 상태 정보 조회
  CodeNameInfo getStatusInfo(Buyer buyer);

  // 탈퇴 회원 계정 재활성화
  Optional<Buyer> reactivate(String email, String password);

  // 탈퇴 회원 목록 조회
  List<Buyer> getWithdrawnMembers();

  // 서비스 이용현황 조회
  MemberStatusInfo getServiceUsage(Long buyerId);
  
  // 탈퇴 가능 여부 판단
  boolean canWithdraw(Long buyerId);
}
