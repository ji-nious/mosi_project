package com.kh.project.web.common.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerEditForm {

  // 이메일 (읽기 전용)
  private String email;
  
  // 비밀번호 (필수)
  @NotBlank(message = "비밀번호를 입력해주세요.")
  @Size(min = 8, max = 15, message = "")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,15}$", 
           message = "비밀번호는 영문, 숫자, 특수문자를 모두 포함한 8~15자여야 합니다.")
  private String password;
  
  @NotBlank(message = "비밀번호 확인을 입력해주세요.")
  @Size(min = 8, max = 15, message = "")
  private String passwordConfirm;
  
  // 이름 (필수)
  @NotBlank(message = "이름을 입력해주세요.")
  @Size(max = 15, message = "이름은 15자 이내여야 합니다.")
  private String name;
  
  // 닉네임 (필수)
  @NotBlank(message = "닉네임을 입력해주세요.")
  @Size(max = 8, message = "닉네임은 8자 이내여야 합니다.")
  private String nickname;
  
  // 전화번호
  @NotBlank(message = "전화번호를 입력해주세요.")
  @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식: 010-0000-0000")
  private String tel;
  
  // 성별
  private String gender;
  
  // 주소 (선택사항)
  private String postcode;  // 화면 입력용 (문자열)
  private String address;
  private String detailAddress;
  
  // 생년월일
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private Date birth;
  
  // 생년월일 유효성 검증
  public boolean isValidBirth() {
    if (birth == null) {
      return true; // null은 허용 (선택사항)
    }
    return birth.before(new Date()); // 현재 날짜 이전이어야 함
  }
  
  // 비밀번호 확인 검증
  public boolean isPasswordMatching() {
    if (password == null || passwordConfirm == null) {
      return false;
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