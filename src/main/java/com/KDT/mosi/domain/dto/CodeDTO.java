package com.KDT.mosi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeDTO {
  private String codeId;   //코드
  private String codeName;  //디코드
}
