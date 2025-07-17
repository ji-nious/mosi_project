package com.kh.project.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class Seller {

  private Long sellerId;
  private String email;
  private String password;
  private String bizRegNo;
  private String shopName;
  private String name;
  private String shopAddress;
  private String tel;
  private byte[] pic;
  private Integer postNumber;
  private String status;

  // 공통 필드
  private Date cdate;
  private Date udate;
  private Date withdrawnAt;
  private String withdrawnReason;

  /**
   * 기본 생성자에서 기본값 설정
   */
  public Seller() {
    this.status = "활성화";
  }

  /**
   * 필수 필드 생성자
   */
  public Seller(String email, String password, String bizRegNo, String shopName, String name, String shopAddress, String tel) {
    this();
    this.email = email;
    this.password = password;
    this.bizRegNo = bizRegNo;
    this.shopName = shopName;
    this.name = name;
    this.shopAddress = shopAddress;
    this.tel = tel;
  }

  @Override
  public String toString() {
    return String.format("Seller{sellerId=%d, email='%s', shopName='%s'}",
        sellerId, email, shopName);
  }
}