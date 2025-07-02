package com.KDT.mosi.domain.entity.bbs;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BbsReport {
  private Long bbsId;
  private Long memberId;
  private String reason;
  private LocalDateTime reportDate;
}
