package com.kh.project.domain.seller.svc;

import com.kh.project.domain.entity.Seller;
import com.kh.project.web.common.CodeNameInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 판매자 서비스 인터페이스
 */
public interface SellerSVC {
  
  /**
   * 판매자 회원가입
   */
  Seller join(Seller seller);
  
  /**
   * 판매자 로그인 인증
   */
  Seller login(String email, String password);
  
  /**
   * 판매자 정보 조회
   */
  Optional<Seller> findById(Long sellerId);
  
  /**
   * 판매자 정보 수정
   */
  int update(Long sellerId, Seller seller);
  
  /**
   * 판매자 탈퇴
   */
  int withdraw(Long sellerId, String reason);

  /**
   * 이메일 중복 체크
   */
  boolean existsByEmail(String email);

  /**
   * 사업자 등록번호 중복 체크
   */
  boolean existsByBizRegNo(String bizRegNo);

  /**
   * 상호명 중복 체크
   */
  boolean existsByShopName(String shopName);

  /**
   * 비밀번호 확인
   */
  boolean checkPassword(Long sellerId, String password);

  boolean canLogin(Seller seller);
  boolean isWithdrawn(Seller seller);
  CodeNameInfo getGubunInfo(Seller seller);
  CodeNameInfo getStatusInfo(Seller seller);
  CodeNameInfo getShopInfo(Seller seller);
  
  /**
   * 회원등급 승급
   */
  void upgradeGubun(Long sellerId, String newGubun);

  /**
   * 관리 기능
   */
  List<Seller> getWithdrawnMembers();

  /**
   * 사업자등록번호 검증
   */
  boolean validateBizRegNo(String bizRegNo);

  // 프로세스 설계서: 비밀번호 재확인과 서비스 이용현황 조회
  /**
   * 서비스 이용현황 조회 및 탈퇴 가능성 판단
   */
  Map<String, Object> getServiceUsage(Long sellerId);
  
  /**
   * 탈퇴 가능 여부 판단
   */
  boolean canWithdraw(Long sellerId);
}
