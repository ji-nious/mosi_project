package com.kh.project.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class Buyer {

  private Long buyerId;
  private String email;
  private String password;
  private String name;
  private String nickname;
  private String tel;
  private String gender;
  private Date birth;
  private String postNumber;
  private String address;
  private MemberGubun memberGubun;
  private byte[] pic;
  private String status;
  private Date cdate;
  private Date udate;
  private Date withdrawnAt;
  private String withdrawnReason;

  /**
   * 기본 생성자에서 기본값 설정
   */
  public Buyer() {
    this.memberGubun = MemberGubun.NEW;
    this.status = MemberStatus.ACTIVE.getCode();  // "ACTIVE"
  }

  /**
   * 필수 필드 생성자
   */
  public Buyer(String email, String password, String name, String nickname, String tel) {
    this();
    this.email = email;
    this.password = password;
    this.name = name;
    this.nickname = nickname;
    this.tel = tel;
  }

  @Override
  public String toString() {
    return String.format("Buyer{buyerId=%d, email='%s', nickname='%s'}",
        buyerId, email, nickname);
  }
}