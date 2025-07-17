package com.kh.project.web.product;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateForm {
  private Long productId;
  private Long sellerId;
  private String title;
  private String productName;
  private Long deliveryFee;
  private String deliveryInformation;
  private String deliveryMethod;
  private String CountryOfOrigin;
  private String content;
  private Long price;
  private Long quantity;
  private String thumbnail;
  private String status;
  private LocalDateTime udate;
}
