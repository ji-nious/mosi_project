package com.kh.project.web.common.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BuyerEditForm {
  
  // 새 비밀번호 (필수)
  @NotBlank(message = "새 비밀번호를 입력해주세요.")
  @Size(min = 4, message = "새 비밀번호는 최소 4자 이상이어야 합니다.")
  private String password;
  
  @NotBlank(message = "비밀번호 확인을 입력해주세요.")
  @Size(min = 4, message = "비밀번호 확인은 최소 4자 이상이어야 합니다.")
  private String passwordConfirm;
  
  // 이름 (필수)
  @NotBlank(message = "이름을 입력해주세요.")
  @Size(max = 30, message = "이름은 30자 이내여야 합니다.")
  private String name;
  
  // 닉네임 (필수)
  @NotBlank(message = "닉네임을 입력해주세요.")
  @Size(max = 30, message = "닉네임은 30자 이내여야 합니다.")
  private String nickname;
  
  // 전화번호
  @Pattern(regexp = "^0\\d{1,2}-\\d{3,4}-\\d{4}$", message = "전화번호 형식: 지역번호-0000-0000")
  private String tel;
  
  // 성별
  private String gender;
  
  // 주소
  private String postcode;
  private String address;
  private String detailAddress;
  
  // 생년월일
  private LocalDate birth;
  
  // 생년월일 검증 메서드
  public boolean isValidBirth() {
    if (birth == null) return true; // null은 허용
    return birth.isBefore(LocalDate.now()); // 오늘 이전 날짜만 허용
  }
  
  // 새 비밀번호 확인 검증
  public boolean isPasswordMatching() {
    if (password == null || passwordConfirm == null) {
      return false; // 둘 중 하나라도 null이면 불일치
    }
    return password.equals(passwordConfirm);
  }
  
  // 주소 통합 메서드
  public String getFullAddress() {
    if (postcode != null && address != null) {
      return String.format("(%s) %s %s", postcode, address, 
             detailAddress != null ? detailAddress : "").trim();
    }
    return address;
  }
} 