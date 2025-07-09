package com.kh.project.web.common.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BuyerSignupForm {
  private String name;
  private String nickname;
  private String email;
  private String password;
  private String tel;
  private String gender;
  private LocalDate birth;
  private String postcode;
  private String address;
  private String detailAddress;
} 