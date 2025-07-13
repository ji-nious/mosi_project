package com.kh.project.web.common.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BuyerSignupForm {
  @NotBlank(message = "이름을 입력해주세요.")
  @Size(max = 45, message = "이름은 45자 이내여야 합니다.")
  private String name;
  
  @NotBlank(message = "닉네임을 입력해주세요.")
  @Size(max = 24, message = "닉네임은 24자 이내여야 합니다.")
  private String nickname;
  
  @NotBlank(message = "이메일을 입력해주세요.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  @Size(max = 50, message = "이메일은 50자 이내여야 합니다.")
  private String email;
  
  @NotBlank(message = "비밀번호를 입력해주세요.")
  @Size(min = 4, max = 100, message = "비밀번호는 4~100자여야 합니다.")
  private String password;
  
  @NotBlank(message = "전화번호를 입력해주세요.")
  @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식: 010-0000-0000")
  private String tel;
  
  @Size(max = 6, message = "성별은 6자 이내여야 합니다.")
  private String gender;
  
  private LocalDate birth;
  
  // 생년월일 검증 메서드
  public boolean isValidBirth() {
    if (birth == null) return true; // null은 허용
    return birth.isBefore(LocalDate.now()); // 오늘 이전 날짜만 허용
  }
  
  private String postcode;
  
  @NotBlank(message = "주소를 입력해주세요.")
  @Size(max = 200, message = "주소는 200자 이내여야 합니다.")
  private String address;
  
  private String detailAddress;
  
  // 주소 통합 메서드
  public String getFullAddress() {
    if (postcode != null && address != null) {
      return String.format("(%s) %s %s", postcode, address, 
             detailAddress != null ? detailAddress : "").trim();
    }
    return address;
  }
} 