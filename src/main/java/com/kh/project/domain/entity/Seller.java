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
 * 판매자 엔티티
 * 판매자의 기본 정보, 사업자 정보, 등급, 상태 관리
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seller {

  private Long sellerId;

  @NotBlank(message = "이메일은 필수입니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  @Size(max = 50, message = "이메일은 50자 이내여야 합니다.")
  private String email;

  @NotBlank(message = "비밀번호는 필수입니다.")
  @Size(min = 4, max = 100, message = "비밀번호는 4~100자여야 합니다.")
  private String password;

  @NotBlank(message = "사업자등록번호는 필수입니다.")
  @Size(max = 30, message = "사업자등록번호는 30자 이내여야 합니다.")
  @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자등록번호 형식: 000-00-00000")
  private String bizRegNo;

  @NotBlank(message = "가게 이름은 필수입니다.")
  @Size(max = 100, message = "가게 이름은 100자 이내여야 합니다.")
  private String shopName;

  @NotBlank(message = "대표자명은 필수입니다.")
  @Size(max = 30, message = "대표자명은 30자 이내여야 합니다.")
  private String name;

  @NotBlank(message = "가게 주소는 필수입니다.")
  @Size(max = 200, message = "가게 주소는 200자 이내여야 합니다.")
  private String shopAddress;

  @NotBlank(message = "가게 전화번호는 필수입니다.")
  @Size(max = 13, message = "전화번호는 13자 이내여야 합니다.")
  @Pattern(regexp = "^(0\\d{1,2})-\\d{3,4}-\\d{4}$", message = "전화번호 형식: 지역번호-0000-0000")
  private String tel;

  /** 회원 등급 코드 (NEW, BRONZE, SILVER, GOLD) */
  private String memberGubun = "NEW";

  private byte[] pic;
  private String status = "활성화";
  private Date cdate;
  private Date udate;
  private Date withdrawnAt;
  private String withdrawnReason;

  /**
   * 로그인 가능 여부 확인
   * 
   * @return boolean - 활성화 상태이고 탈퇴하지 않은 경우 true
   */
  public boolean canLogin() {
    return "활성화".equals(this.status) && this.withdrawnAt == null;
  }

  /**
   * 탈퇴 여부 확인
   * 
   * @return boolean - 탈퇴 상태인 경우 true
   */
  public boolean isWithdrawn() {
    return "탈퇴".equals(this.status);
  }

  /**
   * 사업자등록번호 유효성 검사
   * 
   * @return boolean - 올바른 형식인 경우 true
   */
  public boolean isValidBizRegNo() {
    if (bizRegNo == null) return false;
    return bizRegNo.matches("^\\d{3}-\\d{2}-\\d{5}$");
  }

  /**
   * 회원 등급 업그레이드
   * 
   * @param newGubun 새로운 회원 등급
   */
  public void upgradeMemberGubun(MemberGubun newGubun) {
    this.memberGubun = newGubun.getCode();
  }

  /**
   * 현재 회원 등급 객체 조회
   * 
   * @return MemberGubun - 현재 등급 객체
   */
  public MemberGubun getCurrentMemberGubun() {
    return com.kh.project.web.common.MemberGubunUtils.fromCodeOrDefault(this.memberGubun);
  }

  /**
   * 회원 등급 설명 조회
   * 
   * @return String - 등급 설명 (예: "신규회원", "브론즈회원")
   */
  public String getMemberGubunDescription() {
    return getCurrentMemberGubun().getDescription();
  }
}