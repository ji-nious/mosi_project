package com.kh.project.domain.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 구매자 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Buyer {

  private Long buyerId;

  @NotBlank(message = "이메일은 필수입니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  @Size(max = 50, message = "이메일은 50자 이내여야 합니다.")
  private String email;

  @NotBlank(message = "비밀번호를 입력해주세요.")
  @Size(min = 8, max = 15, message = "")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,15}$", 
           message = "비밀번호는 영문, 숫자, 특수문자를 모두 포함한 8~15자여야 합니다.")
  private String password;

  @NotBlank(message = "이름을 입력해주세요.")
  @Size(max = 15, message = "이름은 15자 이내여야 합니다.")
  private String name;

  @NotBlank(message = "닉네임을 입력해주세요.")
  @Size(max = 8, message = "닉네임은 8자 이내여야 합니다.")
  private String nickname;

  @NotBlank(message = "전화번호를 입력해주세요.")
  @Size(max = 13, message = "전화번호는 13자 이내여야 합니다.")
  @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식: 010-0000-0000")
  private String tel;

  @Size(max = 6, message = "성별은 6자 이내여야 합니다.")
  private String gender;

  private Date birth;
  
  private Number postNumber; // Oracle POST_NUMBER 필드

  @Size(max = 200, message = "주소는 200자 이내여야 합니다.")
  private String address;

  private MemberGubun memberGubun = MemberGubun.NEW;
  private byte[] pic;
  private Integer status = 1; // 표준화: 1=활성화
  private Date cdate;
  private Date udate;
  private Date withdrawnAt;
  private String withdrawnReason;

  /**
   * 로그인 가능 여부 확인
   */
  public boolean canLogin() {
    return status != null && status == 1 && this.withdrawnAt == null;
  }

  /**
   * 탈퇴 여부 확인
   */
  public boolean isWithdrawn() {
    return status != null && status == 0 || this.withdrawnAt != null;
  }
}
