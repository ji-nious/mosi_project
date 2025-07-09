package com.kh.project.domain.seller.dao;

import com.kh.project.domain.entity.Seller;

import java.util.List;
import java.util.Optional;

/**
 * 판매자 DAO 인터페이스
 */
public interface SellerDAO {
  
  /**
   * 판매자 회원가입
   */
  Seller save(Seller seller);

  /**
   * ID로 판매자 조회
   */
  Optional<Seller> findById(Long sellerId);

  /**
   * 판매자 정보 수정
   */
  int update(Long sellerId, Seller seller);

  /**
   * 판매자 탈퇴 (상태 변경)
   */
  int deleteById(Long sellerId);

  /**
   * 이메일로 판매자 조회
   */
  Optional<Seller> findByEmail(String email);

  Optional<Seller> findByBizRegNo(String bizRegNo);
  List<Seller> findAll();
  List<Seller> findWithdrawnMembers();

  /**
   * 이메일 중복 체크
   */
  boolean existsByEmail(String email);

  /**
   * 사업자등록번호 중복 체크
   */
  boolean existsByBizRegNo(String bizRegNo);

  /**
   * 상호명 중복 체크
   */
  boolean existsByShopName(String shopName);

  int withdrawWithReason(Long sellerId, String reason);
} 