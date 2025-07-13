package com.kh.project.web.common.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class SellerSignupForm {
  @NotBlank(message = "이메일을 입력해주세요.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  private String email;
  
  @NotBlank(message = "비밀번호를 입력해주세요.")
  @Size(min = 4, message = "비밀번호는 최소 4자 이상이어야 합니다.")
  private String password;
  
  private String passwordConfirm;
  
  @NotBlank(message = "사업자등록번호를 입력해주세요.")
  @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자등록번호 형식: 000-00-00000")
  private String businessNumber;
  
  @NotBlank(message = "상호명을 입력해주세요.")
  @Size(max = 100, message = "상호명은 100자 이내여야 합니다.")
  private String storeName;
  
  @NotBlank(message = "대표자명을 입력해주세요.")
  @Size(max = 30, message = "대표자명은 30자 이내여야 합니다.")
  private String name;
  
  private String postcode;
  
  @NotBlank(message = "주소를 입력해주세요.")
  private String address;
  
  private String detailAddress;
  
  @NotBlank(message = "전화번호를 입력해주세요.")
  @Pattern(regexp = "^0\\d{1,2}-\\d{3,4}-\\d{4}$", message = "전화번호 형식: 지역번호-0000-0000")
  private String tel;
  
  private LocalDate birth;
  
  // 비밀번호 확인 검증
  public boolean isPasswordMatching() {
    return password != null && password.equals(passwordConfirm);
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