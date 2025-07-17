package com.kh.project.domain.buyer.svc;

import com.kh.project.domain.entity.Buyer;

import java.util.List;
import java.util.Optional;


public interface BuyerSVC {

  /**
   * 구매자 저장 (기본 회원가입)
   * @param buyer 저장할 구매자 정보
   * @return 저장된 구매자 정보
   */
  Buyer save(Buyer buyer);

  /**
   * 스마트 회원가입 (탈퇴 회원도 바로 재가입 가능)
   * - 기존 활성 회원이면 예외 발생
   * - 탈퇴 회원이면 재가입 처리
   * - 신규 회원이면 일반 가입 처리
   *
   * @param buyer 가입할 구매자 정보
   * @return 가입/재가입된 구매자 정보
   */
  Buyer join(Buyer buyer);

  /**
   * ID로 구매자 조회
   * @param buyerId 구매자 ID
   * @return 구매자 정보 (Optional)
   */
  Optional<Buyer> findById(Long buyerId);

  /**
   * 이메일로 구매자 조회
   * @param email 이메일
   * @return 구매자 정보 (Optional)
   */
  Optional<Buyer> findByEmail(String email);

  /**
   * 이메일 중복 체크 (활성 회원만)
   * @param email 확인할 이메일
   * @return 중복 여부
   */
  boolean existsByEmail(String email);

  /**
   * 닉네임 중복 체크 (활성 회원만)
   * @param nickname 확인할 닉네임
   * @return 중복 여부
   */
  boolean existsByNickname(String nickname);

  /**
   * 회원 등급 정보 조회
   * @param buyer 구매자 정보
   * @return 등급 설명 문자열
   */
  String getGubunInfo(Buyer buyer);

  /**
   * 구매자 정보 수정
   * @param buyerId 구매자 ID
   * @param buyer 수정할 구매자 정보
   * @return 수정된 행 수
   */
  int update(Long buyerId, Buyer buyer);

  /**
   * 구매자 탈퇴 (사유와 함께)
   * @param buyerId 구매자 ID
   * @param reason 탈퇴 사유
   * @return 처리된 행 수
   */
  int withdrawWithReason(Long buyerId, String reason);

  /**
   * 탈퇴 회원 목록 조회
   * @return 탈퇴 회원 목록
   */
  List<Buyer> findWithdrawnMembers();

  /**
   * 전체 구매자 목록 조회
   * @return 전체 구매자 목록
   */
  List<Buyer> findAll();

  /**
   * 탈퇴 회원 재활성화 (간단 재활성화)
   * @param email 이메일
   * @param password 비밀번호
   * @return 처리된 행 수
   */
  int reactivate(String email, String password);

  /**
   * 탈퇴 회원 재가입 (전체 정보 업데이트)
   * @param buyer 재가입할 구매자 정보
   * @return 처리된 행 수
   */
  int rejoin(Buyer buyer);
}