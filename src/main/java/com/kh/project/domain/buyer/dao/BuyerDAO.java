package com.kh.project.domain.buyer.dao;

import com.kh.project.domain.entity.Buyer;

import java.util.List;
import java.util.Optional;

/**
 * 구매자 데이터 접근 인터페이스
 * - 구매자 정보 CRUD 및 검증 기능 제공
 */
public interface BuyerDAO {
  
  /**
   * 구매자 회원가입
   */
  Buyer save(Buyer buyer);
  
  /**
   * ID로 구매자 조회
   */
  Optional<Buyer> findById(Long buyerId);
  
  /**
   * 구매자 정보 수정
   */
  int update(Long buyerId, Buyer buyer);
  
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

  int withdrawWithReason(Long buyerId, String reason);

  /**
   * 모든 탈퇴 회원 조회
   * @return List<Buyer> - 탈퇴 회원 목록
   */
  List<Buyer> findWithdrawnMembers();

  /**
   * 모든 구매자 회원 조회 (탈퇴 제외)
   * @return List<Buyer>
   */
  List<Buyer> findAll();

  /**
   * 탈퇴한 회원 재가입
   * @param buyer 재가입할 회원 정보
   * @return int - 수정된 행 수
   */
  int rejoin(Buyer buyer);

  /**
   * 탈퇴한 회원 계정 재활성화
   * @param email 대상 이메일
   * @param password 새로운 비밀번호
   * @return 수정된 행 수
   */
  int reactivate(String email, String password);
}
