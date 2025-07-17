package com.kh.project.domain.seller.dao;

import com.kh.project.domain.entity.Seller;

import java.util.List;
import java.util.Optional;

public interface SellerDAO {

  /**
   * 판매자 회원가입
   * @param seller 저장할 판매자 정보
   * @return 저장된 판매자 정보
   */
  Seller save(Seller seller);

  /**
   * ID로 판매자 조회
   * @param sellerId 판매자 ID
   * @return 판매자 정보
   */
  Optional<Seller> findById(Long sellerId);

  /**
   * 판매자 정보 수정
   * @param sellerId 판매자 ID
   * @param seller 수정할 판매자 정보
   * @return 수정된 행 수
   */
  int update(Long sellerId, Seller seller);

  /**
   * 이메일로 판매자 조회
   * @param email 이메일
   * @return 판매자 정보
   */
  Optional<Seller> findByEmail(String email);

  /**
   * 사업자등록번호로 판매자 조회
   * @param bizRegNo 사업자등록번호
   * @return 판매자 정보
   */
  Optional<Seller> findByBizRegNo(String bizRegNo);

  /**
   * 전체 판매자 목록 조회
   * @return 전체 판매자 목록
   */
  List<Seller> findAll();

  /**
   * 탈퇴 회원 목록 조회
   * @return 탈퇴 회원 목록
   */
  List<Seller> findWithdrawnMembers();

  /**
   * 이메일 중복 체크 (모든 상태)
   * @param email 확인할 이메일
   * @return 중복 여부
   */
  boolean existsByEmail(String email);

  /**
   * 이메일 중복 체크 (특정 상태 제외)
   * @param email 확인할 이메일
   * @param excludeStatus 제외할 상태
   * @return 중복 여부
   */
  boolean existsByEmailAndStatusNot(String email, String excludeStatus);

  /**
   * 사업자등록번호 중복 체크
   * @param bizRegNo 확인할 사업자등록번호
   * @return 중복 여부
   */
  boolean existsByBizRegNo(String bizRegNo);

  /**
   * 상호명 중복 체크
   * @param shopName 확인할 상호명
   * @return 중복 여부
   */
  boolean existsByShopName(String shopName);

  /**
   * 대표자명 중복 체크
   * @param name 확인할 대표자명
   * @return 중복 여부
   */
  boolean existsByName(String name);

  /**
   * 판매자 탈퇴 처리
   * @param sellerId 판매자 ID
   * @param reason 탈퇴 사유
   * @return 처리된 행 수
   */
  int withdrawWithReason(Long sellerId, String reason);

  /**
   * 탈퇴한 판매자 계정 재활성화
   * @param email 이메일
   * @param password 비밀번호
   * @return 처리된 행 수
   */
  int reactivate(String email, String password);

  /**
   * 탈퇴한 판매자 재가입
   * @param seller 재가입할 판매자 정보
   * @return 처리된 행 수
   */
  int rejoin(Seller seller);

  /**
   * 이메일과 상태로 중복 체크
   * @param email 이메일
   * @param status 상태
   * @return 중복 여부
   */
  boolean existsByEmailAndStatus(String email, String status);

  /**
   * 사업자등록번호와 상태로 중복 체크
   * @param bizRegNo 사업자등록번호
   * @param status 상태
   * @return 중복 여부
   */
  boolean existsByBizRegNoAndStatus(String bizRegNo, String status);

  /**
   * 전화번호와 상태로 중복 체크
   * @param tel 전화번호
   * @param status 상태
   * @return 중복 여부
   */
  boolean existsByTelAndStatus(String tel, String status);

  /**
   * 사업자번호와 상태로 판매자 조회 (복합키)
   * @param bizRegNo 사업자등록번호
   * @param status 상태
   * @return 판매자 정보
   */
  Optional<Seller> findByBizRegNoAndStatus(String bizRegNo, String status);

  /**
   * 활성화된 판매자 조회 (복합키)
   * @param bizRegNo 사업자등록번호
   * @return 활성 판매자 정보
   */
  Optional<Seller> findActiveSeller(String bizRegNo);

  /**
   * 탈퇴한 판매자 조회 (복합키)
   * @param bizRegNo 사업자등록번호
   * @return 탈퇴 판매자 정보
   */
  Optional<Seller> findWithdrawnSeller(String bizRegNo);

  /**
   * 사업자번호의 모든 이력 조회 (복합 키 방식)
   * @param bizRegNo 사업자등록번호
   * @return 모든 이력 목록
   */
  List<Seller> findAllByBizRegNo(String bizRegNo);
}