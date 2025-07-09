package com.kh.project.domain.entity;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;
import java.util.List;

/**
 * 회원 등급 Enum
 */
@Slf4j
@Getter
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

  public static MemberGubun getDefaultGrade() {
    return NEW;
  }

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

  public static String getDescriptionByCode(String code) {
    return fromCodeOrDefault(code).getDescription();
  }

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

  public static List<MemberGubun> getAllGrades() {
    return Arrays.asList(values());
  }
}
