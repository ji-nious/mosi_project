package com.KDT.mosi.web.form.board.bbs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateApi {
  private Long bbsId;
  @NotBlank(message = "제목은 필수 입니다.")
  @Size(min=1,max=33,message = "제목은 33자를 초과할 수 없습니다.")
  private String title;
  @NotBlank(message = "내용은 필수 입니다.")
  private String bcontent;
  private LocalDateTime udateDate;
  private Long memberId;
}
