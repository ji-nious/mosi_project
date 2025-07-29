package com.KDT.mosi.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/information")
public class InformationController {

  // 정보 메인 페이지
  @GetMapping("")
  public String informationHome() {
    return "information/information";
  }
} 