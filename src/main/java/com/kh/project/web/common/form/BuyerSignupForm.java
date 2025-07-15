package com.kh.project.web.common.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;

@Data
public class BuyerSignupForm {
  @NotBlank(message = "이름을 입력해주세요.")
  @Size(max = 15, message = "이름은 15자 이내여야 합니다.")
  private String name;
  
  @NotBlank(message = "닉네임을 입력해주세요.")
  @Size(max = 8, message = "닉네임은 8자 이내여야 합니다.")
  private String nickname;
  
  @NotBlank(message = "이메일을 입력해주세요.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  @Size(max = 50, message = "이메일은 50자 이내여야 합니다.")
  private String email;
  
  // 비밀번호
  @NotBlank(message = "비밀번호를 입력해주세요.")
  @Size(min = 8, max = 15, message = "")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,15}$", 
           message = "비밀번호는 영문, 숫자, 특수문자를 모두 포함한 8~15자여야 합니다.")
  private String password;
  
  @NotBlank(message = "비밀번호 확인을 입력해주세요.")
  @Size(min = 8, max = 15, message = "")
  private String passwordConfirm;
  
  @NotBlank(message = "전화번호를 입력해주세요.")
  @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식: 010-0000-0000")
  private String tel;
  
  @Size(max = 6, message = "성별은 6자 이내여야 합니다.")
  private String gender;
  
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private Date birth;
  
  // 생년월일 검증 메서드
  public boolean isValidBirth() {
    if (birth == null) return true; // null은 허용
    return birth.before(new Date()); // 현재 날짜 이전만 허용
  }
  
  @NotBlank(message = "우편번호를 입력해주세요.")
  private String postcode;
  
  @Size(max = 66, message = "주소는 66자 이내여야 합니다.")
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