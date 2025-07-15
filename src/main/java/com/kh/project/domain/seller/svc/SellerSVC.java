package com.kh.project.domain.seller.svc;

import com.kh.project.domain.entity.Seller;

import java.util.List;
import java.util.Optional;

/**
 * 판매자 서비스
 */
public interface SellerSVC {
  
  /**
   * 판매자 저장
   */
  Seller save(Seller seller);
  
  /**
   * 스마트 회원가입 (탈퇴 회원도 바로 재가입 가능)
   */
  Seller join(Seller seller);
  
  /**
   * ID로 판매자 조회
   */
  Optional<Seller> findById(Long sellerId);
  
  /**
   * 이메일로 판매자 조회
   */
  Optional<Seller> findByEmail(String email);

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

  /**
   * 대표자명 중복 체크
   */
  boolean existsByName(String name);

  /**
   * 사업자등록번호로 판매자 조회
   */
  Optional<Seller> findByBizRegNo(String bizRegNo);

  /**
   * 판매자 정보 수정
   */
  int update(Long sellerId, Seller seller);
  
  /**
   * 판매자 탈퇴
   */
  int withdrawWithReason(Long sellerId, String reason);
  
  /**
   * 탈퇴 회원 목록 조회
   */
  List<Seller> findWithdrawnMembers();

  /**
   * 전체 판매자 목록 조회
   */
  List<Seller> findAll();

  /**
   * 탈퇴 회원 재활성화
   */
  int reactivate(String email, String password);
  
  /**
   * 탈퇴 회원 재가입
   */
  int rejoin(Seller seller);
}
