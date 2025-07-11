package com.kh.project.domain.entity;

import lombok.Getter;

/**
 * 회원 등급 Enum
 * 구매자와 판매자의 등급 체계를 정의
 */
@Getter
public enum MemberGubun {
  /** 신규회원 */
  NEW("NEW", "신규회원"),
  /** 브론즈 등급 */
  BRONZE("BRONZE", "브론즈"),
  /** 실버 등급 */
  SILVER("SILVER", "실버"),
  /** 골드 등급 */
  GOLD("GOLD", "골드");

  /** 등급 코드 */
  private final String code;
  /** 등급 설명 */
  private final String description;

  /**
   * 회원 등급 생성자
   * 
   * @param code 등급 코드
   * @param description 등급 설명
   */
  MemberGubun(String code, String description) {
    this.code = code;
    this.description = description;
  }
}
