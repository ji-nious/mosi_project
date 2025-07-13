package com.kh.project.web.common;

import com.kh.project.domain.entity.MemberGubun;
import lombok.extern.slf4j.Slf4j;

/**
 * 회원 등급 유틸리티
 */
@Slf4j
public class MemberGubunUtils {

  /**
   * 기본 등급 반환
   */
  public static MemberGubun getDefaultGrade() {
    return MemberGubun.NEW;
  }

  /**
   * 코드로 등급 조회 (실패시 기본값 반환)
   */
  public static MemberGubun fromCodeOrDefault(String code) {
    if (code == null || code.trim().isEmpty()) {
      return getDefaultGrade();
    }
    try {
      return MemberGubun.valueOf(code.trim().toUpperCase());
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
      MemberGubun.valueOf(code.trim().toUpperCase());
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * 안전한 valueOf
   */
  public static MemberGubun safeValueOf(String code) {
    return fromCodeOrDefault(code);
  }
} 