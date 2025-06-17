package com.KDT.mosi.web.form.product;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateForm {

  @NotNull(message = "상품ID는 필수입니다.")
  private Long productId;

  @NotBlank(message = "상품명은 필수입니다.")
  @Size(max = 100)
  private String name;

  @Size(max = 30)
  private String category;

  @NotNull
  @Min(0)
  private Integer price;

  @NotBlank
  @Pattern(regexp = "판매중|판매중단")
  private String status;

  @Size(max = 500)
  private String description;

  @Size(max = 1000)
  private String detail;

  @Pattern(regexp = "[YN]")
  private String guideYn;

  @Min(0)
  private Integer reqMoney;

  @Min(1)
  private Integer reqPeople;

  @Min(0)
  private Integer age;

  @Size(max = 100)
  private String foodInfo;

  @Size(max = 100)
  private String sleepInfo;

  @Size(max = 10)
  private String storeInfo;

  @Pattern(regexp = "[YN]")
  private String promoYn;

  @Size(max = 100)
  private String transportInfo;

  // 추가: 이미지 리스트
  private List<ImageForm> images;

  // 추가: 코스포인트 리스트
  private List<CoursePointForm> coursePoints;
}