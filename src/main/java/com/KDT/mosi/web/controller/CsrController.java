package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.bbs.bbs.svc.BbsSVC;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/csr")
@RequiredArgsConstructor
public class CsrController {
  final private BbsSVC bbsSVC;

  @GetMapping("/bbs")
  public String bbs() {
    return "csr/bbs/allForm";
  }

  //게시글조회
  @GetMapping("/bbs/{id}")
  public String findById(
      @PathVariable("id") Long id,
      Model model
  ){
    model.addAttribute("bbsId", id);

    return "csr/bbs/detailForm";
  }

  @GetMapping("/bbs/add")
  public String bbsAdd() {
    return "csr/bbs/addForm";
  }


}
