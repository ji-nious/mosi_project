package com.kh.project.web.common;

import com.kh.project.domain.entity.MemberGubun;
import lombok.extern.slf4j.Slf4j;

/**
 * 회원 등급 관련 유틸리티 클래스
 * 등급 코드 변환, 유효성 검사, 기본값 처리 등의 기능 제공
 */
@Slf4j
public class MemberGubunUtils {

  /**
   * 기본 등급 반환
   * 
   * @return MemberGubun - 기본 등급 (NEW)
   */
  public static MemberGubun getDefaultGrade() {
    return MemberGubun.NEW;
  }

  /**
   * 코드로 등급 조회 (실패시 기본값 반환)
   * 
   * @param code 등급 코드
   * @return MemberGubun - 해당 등급 또는 기본 등급
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
   * 
   * @param code 등급 코드
   * @return String - 등급 설명 (예: "신규회원", "브론즈")
   */
  public static String getDescriptionByCode(String code) {
    return fromCodeOrDefault(code).getDescription();
  }

  /**
   * 등급 코드 유효성 검사
   * 
   * @param code 검사할 등급 코드
   * @return boolean - 유효한 코드인 경우 true
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
   * 안전한 valueOf (예외 발생시 기본값 반환)
   * 
   * @param code 등급 코드
   * @return MemberGubun - 해당 등급 또는 기본 등급
   */
  public static MemberGubun valueOf(String code) {
    return fromCodeOrDefault(code);
  }
} 