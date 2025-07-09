package com.kh.project.web.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 회원 정보 (세션용)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeNameInfo {
  private String code;
  private String name;

  public static CodeNameInfo of(String code, String name) {
    return new CodeNameInfo(code, name);
  }
}