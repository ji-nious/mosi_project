package com.kh.project.web.form.member;

import com.kh.project.web.validation.PasswordMatching;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@PasswordMatching
public class SellerSignupForm {

  @NotBlank(message = "이메일을 입력해주세요.")
  @Size(max = 20, message = "이메일은 20자 이내여야 합니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  private String email;

  @NotBlank(message = "비밀번호를 입력해주세요.")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,15}$", 
           message = "비밀번호는 영문, 숫자, 특수문자를 모두 포함한 8~15자여야 합니다.")
  private String password;
  
  @NotBlank(message = "비밀번호 확인을 입력해주세요.")
  @Size(min = 8, max = 15)
  private String passwordConfirm;
  
  @NotBlank(message = "사업자등록번호를 입력해주세요.")
  @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자등록번호는 하이픈(-) 을 포함하여 12자리로 입력해주세요.")
  private String businessNumber;
  
  @NotBlank(message = "상호명을 입력해주세요.")
  @Size(max = 10, message = "상호명은 10자 이내여야 합니다.")
  private String storeName;
  
  @NotBlank(message = "대표자명을 입력해주세요.")
  @Size(max = 10, message = "대표자명은 10자 이내여야 합니다.")
  private String name;

  @NotBlank(message = "전화번호를 입력해주세요.")
  @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호는 하이픈(-)을 포함하여 입력해주세요.")
  private String tel;

  private String postcode;

  @NotBlank(message = "주소를 입력해주세요.")
  private String address;
  
  @NotBlank(message = "상세주소를 입력해주세요.")
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