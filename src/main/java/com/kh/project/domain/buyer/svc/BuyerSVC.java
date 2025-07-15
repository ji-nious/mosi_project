package com.kh.project.domain.buyer.svc;

import com.kh.project.domain.entity.Buyer;

import java.util.List;
import java.util.Optional;

/**
 * 구매자 서비스
 */
public interface BuyerSVC {
  
  /**
   * 구매자 저장
   */
  Buyer save(Buyer buyer);
  
  /**
   * 스마트 회원가입 (탈퇴 회원도 바로 재가입 가능)
   */
  Buyer join(Buyer buyer);
  
  /**
   * ID로 구매자 조회
   */
  Optional<Buyer> findById(Long buyerId);

  /**
   * 이메일로 구매자 조회
   */
  Optional<Buyer> findByEmail(String email);

  /**
   * 이메일 중복 체크
   */
  boolean existsByEmail(String email);

  /**
   * 닉네임 중복 체크
   */
  boolean existsByNickname(String nickname);
  
  /**
   * 회원 등급 정보 조회
   */
  String getGubunInfo(Buyer buyer);
  
  /**
   * 구매자 정보 수정
   */
  int update(Long buyerId, Buyer buyer);

  /**
   * 구매자 탈퇴
   */
  int withdrawWithReason(Long buyerId, String reason);

  /**
   * 탈퇴 회원 목록 조회
   */
  List<Buyer> findWithdrawnMembers();

  /**
   * 전체 구매자 목록 조회
   */
  List<Buyer> findAll();

  /**
   * 탈퇴 회원 재활성화
   */
  int reactivate(String email, String password);
  
  /**
   * 탈퇴 회원 재가입
   */
  int rejoin(Buyer buyer);
}
