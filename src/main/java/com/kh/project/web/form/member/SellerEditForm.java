package com.kh.project.web.form.member;

import com.kh.project.web.validation.PasswordMatching;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@PasswordMatching
public class SellerEditForm {

  @NotBlank(message = "이메일을 입력해주세요.")
  @Size(max = 20, message = "이메일은 20자 이내여야 합니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  private String email;

  @NotBlank(message = "비밀번호를 입력해주세요.")
  @Size(min = 8, max = 15)
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,15}$", 
           message = "비밀번호는 영문, 숫자, 특수문자를 모두 포함한 8~15자여야 합니다.")
  private String password;
  
  @NotBlank(message = "비밀번호 확인을 입력해주세요.")
  @Size(min = 8, max = 15)
  private String passwordConfirm;

  @NotBlank(message = "사업자번호를 입력해주세요.")
  @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자등록번호는 하이픈(-) 을 포함하여 12자리를 입력해주세요.")
  private String bizRegNo;

  @NotBlank(message = "상호명을 입력해주세요.")
  @Size(max = 10, message = "상호명은 10자 이내여야 합니다.")
  private String shopName;

  @NotBlank(message = "대표자명을 입력해주세요.")
  @Size(max = 10, message = "대표자명은 10자 이내여야 합니다.")
  private String name;

  @NotBlank(message = "전화번호를 입력해주세요.")
  @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호는 하이픈(-)을 포함하여 입력해주세요.")
  private String tel;

  private String postNumber;

  @NotBlank(message = "주소를 입력해주세요.")
  private String shopAddress;

  @NotBlank(message = "상세주소를 입력해주세요.")
  private String detailAddress;

  /**
   * 비밀번호 확인 검증
   */
  public boolean isPasswordMatching() {
    if (password == null || passwordConfirm == null) {
      return false;
    }
    return password.equals(passwordConfirm);
  }
} 