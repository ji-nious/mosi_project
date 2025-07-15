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
 * 판매자 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seller {

  // 상수 정의
  private static final Integer STATUS_ACTIVE = 1;
  private static final Integer STATUS_WITHDRAWN = 0;

  private Long sellerId;

  @NotBlank(message = "이메일은 필수입니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  @Size(max = 50, message = "이메일은 50자 이내여야 합니다.")
  private String email;

  @NotBlank(message = "비밀번호를 입력해주세요.")
  @Size(min = 8, max = 15, message = "")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,15}$",
          message = "비밀번호는 영문, 숫자, 특수문자를 모두 포함한 8~15자여야 합니다.")
  private String password;

  @NotBlank(message = "사업자 등록번호를 입력해주세요.")
  @Size(max = 30, message = "사업자 등록번호는 30자 이내여야 합니다.")
  private String bizRegNo;

  @NotBlank(message = "상호명을 입력해주세요.")
  @Size(max = 100, message = "상호명은 100자 이내여야 합니다.")
  private String shopName;

  @NotBlank(message = "대표자명을 입력해주세요.")
  @Size(max = 30, message = "대표자명은 30자 이내여야 합니다.")
  private String name;

  @NotBlank(message = "주소를 입력해주세요.")
  @Size(max = 200, message = "주소는 200자 이내여야 합니다.")
  private String shopAddress;

  @NotBlank(message = "전화번호를 입력해주세요.")
  @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호는 하이픈(-)을 포함하여 입력해주세요.")
  private String tel;

  private byte[] pic; // 프로필 이미지 (BLOB 타입)

  private String postNumber; // 우편번호

  private Integer status = STATUS_ACTIVE; // 상태 (기본값: 1)

  // 공통 필드
  private Date cdate;         // 생성 날짜
  private Date udate;         // 수정 날짜
  private Date withdrawnAt;   // 탈퇴 날짜
  private String withdrawnReason; // 탈퇴 사유

  // 생성자 (필수 필드만)
  public Seller(String email, String password, String bizRegNo, String shopName, String name, String shopAddress, String tel) {
    this.email = email;
    this.password = password;
    this.bizRegNo = bizRegNo;
    this.shopName = shopName;
    this.name = name;
    this.shopAddress = shopAddress;
    this.tel = tel;
    this.status = STATUS_ACTIVE;
  }

  // 비즈니스 로직 메서드
  
  /**
   * 로그인 가능 여부
   */
  public boolean canLogin() {
    return STATUS_ACTIVE.equals(this.status) && this.withdrawnAt == null;
  }

  /**
   * 탈퇴 여부
   */
  public boolean isWithdrawn() {
    return STATUS_WITHDRAWN.equals(this.status) || this.withdrawnAt != null;
  }

  /**
   * 활성 상태 여부
   */
  public boolean isActive() {
    return STATUS_ACTIVE.equals(this.status) && this.withdrawnAt == null;
  }

  /**
   * 상태 표시용 메서드 (SellerPageController 호환성)
   */
  public String getStatusDisplay() {
    if (this.status == null) return "알 수 없음";
    
    if (STATUS_ACTIVE.equals(this.status)) {
      return "활성";
    } else if (STATUS_WITHDRAWN.equals(this.status)) {
      return "탈퇴";
    } else {
      return "알 수 없음";
    }
  }

  /**
   * 서비스 이용현황 (하위호환성을 위한 기본값 반환)
   */
  public Integer getServiceUsage() {
    // 기본적으로 서비스 이용 없음(0) 반환
    return 0;
  }

  /**
   * 서비스 이용현황 설정 (하위호환성을 위한 더미 메서드)
   */
  public void setServiceUsage(Integer serviceUsage) {
    // 실제로는 아무것도 하지 않음 (DB에 SERVICE_USAGE 컬럼이 없으므로)
    // 기존 코드 호환성을 위해서만 존재
  }

  @Override
  public String toString() {
    return "Seller{" +
        "sellerId=" + sellerId +
        ", email='" + email + '\'' +
        ", bizRegNo='" + bizRegNo + '\'' +
        ", shopName='" + shopName + '\'' +
        ", name='" + name + '\'' +
        ", status=" + status +
        ", isWithdrawn=" + isWithdrawn() +
        '}';
  }
}