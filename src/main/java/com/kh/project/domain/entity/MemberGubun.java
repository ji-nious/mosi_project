package com.kh.project.domain.entity;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 회원 등급
 */
@Getter
@Slf4j
public enum MemberGubun {
  NEW("NEW", "신규회원"),
  BRONZE("BRONZE", "브론즈"),
  SILVER("SILVER", "실버"),
  GOLD("GOLD", "골드");

  private final String code;
  private final String description;

  MemberGubun(String code, String description) {
    this.code = code;
    this.description = description;
  }

  /**
   * 기본 등급 반환
   */
  public static MemberGubun getDefaultGrade() {
    return NEW;
  }

  /**
   * 코드로 등급 조회 (실패시 기본값 반환)
   */
  public static MemberGubun fromCodeOrDefault(String code) {
    if (code == null || code.trim().isEmpty()) {
      return getDefaultGrade();
    }
    try {
      return valueOf(code.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      log.warn("잘못된 등급 코드: {}, 기본값 사용", code);
      return getDefaultGrade();
    }
  }

  /**
   * 코드로 등급 설명 조회
   */
  public static String getDescriptionByCode(String code) {
    return fromCodeOrDefault(code).getDescription();
  }

  /**
   * 등급 코드 유효성 검사
   */
  public static boolean isValidCode(String code) {
    if (code == null || code.trim().isEmpty()) {
      return false;
    }
    try {
      valueOf(code.trim().toUpperCase());
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
