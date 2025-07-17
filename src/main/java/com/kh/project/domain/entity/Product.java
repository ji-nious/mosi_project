package com.kh.project.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

//게시글 엔티티
@Data
public class Product {
  private Long productId;
  private Long sellerId;
  private String title;
  private String content;
  private String productName;
  private Long deliveryFee;
  private String deliveryInformation;
  private String deliveryMethod;
  private String countryOfOrigin;
  private Long price;
  private Long quantity;
  private String thumbnail;
  private String status;
  private LocalDateTime cdate;
  private LocalDateTime udate;
}
