package com.kh.project.domain.seller.dao;

import com.kh.project.domain.entity.Seller;

import java.util.List;
import java.util.Optional;

/**
 * 판매자 DAO 인터페이스
 */
public interface SellerDAO {
  
  // 판매자 회원가입
  Seller save(Seller seller);

  // ID로 판매자 조회
  Optional<Seller> findById(Long sellerId);

  // 판매자 정보 수정
  int update(Long sellerId, Seller seller);

  // 이메일로 판매자 조회
  Optional<Seller> findByEmail(String email);

  // 사업자등록번호로 판매자 조회
  Optional<Seller> findByBizRegNo(String bizRegNo);
  
  // 전체 판매자 목록 조회
  List<Seller> findAll();
  
  // 탈퇴 회원 목록 조회
  List<Seller> findWithdrawnMembers();

  // 이메일 중복 체크
  boolean existsByEmail(String email);

  /**
   * 이메일 중복 체크 (특정 상태 제외)
   */
  boolean existsByEmailAndStatusNot(String email, Integer excludeStatus);

  // 사업자등록번호 중복 체크
  boolean existsByBizRegNo(String bizRegNo);

  // 상호명 중복 체크
  boolean existsByShopName(String shopName);

  // 대표자명 중복 체크
  boolean existsByName(String name);

  // 판매자 탈퇴 처리
  int withdrawWithReason(Long sellerId, String reason);

  // 탈퇴한 판매자 계정 재활성화
  int reactivate(String email, String password);

  // 탈퇴한 판매자 재가입 (전체 정보 업데이트)
  int rejoin(Seller seller);

  // ========================================
  // 🎯 1차 프로젝트: 상태별 중복 체크 구현
  // ========================================

  boolean existsByEmailAndStatus(String email, Integer status);
  boolean existsByBizRegNoAndStatus(String bizRegNo, Integer status);
  boolean existsByTelAndStatus(String tel, Integer status);

  // ========================================
  // 🎯 복합 키 전용 메서드들 추가
  // ========================================

  /**
   * 사업자번호와 상태로 판매자 조회 (복합 키 방식, 실무 표준)
   */
  Optional<Seller> findByBizRegNoAndStatus(String bizRegNo, Integer status);

  /**
   * 활성화된 판매자 조회 (복합 키 방식)
   */
  Optional<Seller> findActiveSeller(String bizRegNo);

  /**
   * 탈퇴한 판매자 조회 (복합 키 방식)
   */
  Optional<Seller> findWithdrawnSeller(String bizRegNo);

  /**
   * 사업자번호의 모든 이력 조회 (복합 키 방식)
   */
  List<Seller> findAllByBizRegNo(String bizRegNo);
} 