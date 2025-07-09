package com.kh.project.web.common.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SellerSignupForm {
  private String email;
  private String password;
  private String passwordConfirm;
  private String businessNumber;
  private String storeName;
  private String name;
  private String postcode;
  private String address;
  private String detailAddress;
  private String tel;
  private LocalDate birth;
} 