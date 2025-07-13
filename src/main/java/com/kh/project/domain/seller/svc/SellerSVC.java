package com.kh.project.domain.seller.svc;

import com.kh.project.domain.entity.Seller;
import com.kh.project.web.common.form.MemberStatusInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 판매자 서비스
 */
public interface SellerSVC {
  
  /**
   * 회원가입
   */
  Seller join(Seller seller);
  
  /**
   * 로그인
   */
  Seller login(String email, String password);
  
  /**
   * 정보 조회
   */
  Optional<Seller> findById(Long sellerId);
  
  /**
   * 정보 수정
   */
  int update(Long sellerId, Seller seller);
  
  /**
   * 회원 탈퇴
   */
  int withdraw(Long sellerId, String reason);

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
   * 사업장 주소 중복 체크
   */
  boolean existsByShopAddress(String shopAddress);

  /**
   * 비밀번호 확인
   */
  boolean checkPassword(Long sellerId, String password);

  /**
   * 로그인 가능 여부 확인
   */
  boolean canLogin(Seller seller);
  
  /**
   * 탈퇴 여부 확인
   */
  boolean isWithdrawn(Seller seller);
  
  /**
   * 회원 등급 정보 조회
   */
  Map<String, String> getGubunInfo(Seller seller);
  
  /**
   * 회원 상태 정보 조회
   */
  Map<String, String> getStatusInfo(Seller seller);
  
  /**
   * 상점 정보 조회
   */
  Map<String, String> getShopInfo(Seller seller);
  
  /**
   * 탈퇴 회원 목록 조회
   */
  List<Seller> getWithdrawnMembers();

  /**
   * 사업자등록번호 검증
   */
  boolean validateBizRegNo(String bizRegNo);

  /**
   * 서비스 이용현황 조회
   */
  MemberStatusInfo getServiceUsage(Long sellerId);
  
  /**
   * 탈퇴 가능 여부 판단
   */
  boolean canWithdraw(Long sellerId);
}
