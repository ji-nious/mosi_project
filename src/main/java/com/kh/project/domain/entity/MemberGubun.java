package com.kh.project.domain.entity;

import lombok.Getter;

/**
 * 회원 등급 Enum
 */
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
}
