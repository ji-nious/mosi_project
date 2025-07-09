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
   * 구매자 탈퇴 (상태 변경)
   */
  int deleteById(Long buyerId);

  /**
   * 이메일로 구매자 조회
   */
  Optional<Buyer> findByEmail(String email);

  List<Buyer> findAll();
  List<Buyer> findWithdrawnMembers();
  
  /**
   * 이메일 중복 체크
   */
  boolean existsByEmail(String email);
  
  /**
   * 닉네임 중복 체크
   */
  boolean existsByNickname(String nickname);

  int withdrawWithReason(Long buyerId, String reason);
}
