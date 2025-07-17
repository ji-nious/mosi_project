package com.kh.project.web;

import com.kh.project.domain.entity.Product;
import com.kh.project.domain.product.svc.ProductSVC;
import com.kh.project.web.product.DetailForm;
import com.kh.project.web.product.SaveForm;
import com.kh.project.web.product.UpdateForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/seller")
public class ProductController {
  private final ProductSVC productSVC;

  @GetMapping("/list/{sid}")
  public String sellerPage(@PathVariable("sid") Long sid , Model model){
    List<Product> ids = productSVC.findByIds(sid);
    model.addAttribute("products",ids);
    model.addAttribute("sellerId",sid);
    return "seller/product_list";
  }

  // 판매글 등록 페이지
  @GetMapping("/add/{sid}")
  public String savePage(@PathVariable("sid") Long sid , Model model){
    model.addAttribute("product",new SaveForm());
    model.addAttribute("sid",sid);
    return "seller/product_form";  // 임시 수정 필요 !!
  }

  // 판매글 조회(개별) 상세 페이지
  @GetMapping("/product/{pid}")
  public String detailPage(@PathVariable("pid") Long pid,Model model){

    Optional<Product> optionalProduct = productSVC.findById(pid);
    if(optionalProduct.isPresent()) {
      Product product = optionalProduct.get();
      DetailForm detailForm = new DetailForm();
      BeanUtils.copyProperties(product,detailForm);
      model.addAttribute("detailForm",detailForm);
      return "seller/detail";
    }else {return "error/404";} // 임시 수정 필요 !!



  }

  // 판매글 등록 처리
  @PostMapping("/add/{sid}")
  public String saveProduct(@PathVariable("sid") Long sid, @ModelAttribute SaveForm saveForm, RedirectAttributes redirectAttributes){
    Product product = new Product();
    BeanUtils.copyProperties(saveForm,product);

    productSVC.saveProduct(product,sid);

    redirectAttributes.addAttribute("sid",sid);
    return "redirect:/seller/list/" + sid;
  }

  // 판매글 수정 페이지
  @GetMapping("/product/{pid}/edit")
  public String updatePage(@PathVariable("pid") Long pid ,Model model ){

    // 🔥 기존 상품 정보 불러오기!
    Optional<Product> optionalProduct = productSVC.findById(pid);

    if(optionalProduct.isPresent()) {
      Product product = optionalProduct.get();
      // 🔥 불러온 상품 정보를 UpdateForm 객체에 담아주기
      UpdateForm updateForm = new UpdateForm();
      BeanUtils.copyProperties(product, updateForm); // Product -> UpdateForm 복사

      model.addAttribute("updateForm", updateForm); // 채워진 폼 객체를 모델에 담기
      model.addAttribute("pid", pid);
      return "seller/sellerUpdate";  // 요 뷰 이름으로 페이지를 보여줄 거임!
    } else {
      // 🔥 상품 정보 못 찾으면 에러 페이지나 목록으로 리다이렉트
      return "error/404"; // 임시 에러 페이지 또는 "redirect:/seller/{sid}" 등으로 처리
    }
  }

  //판매글 수정 처리
  @PostMapping("/product/{pid}/edit")
  public String updateProduct(@PathVariable("pid") Long pid , UpdateForm updateForm , RedirectAttributes redirectAttributes){
    Product product = new Product();
    BeanUtils.copyProperties(updateForm,product);
    int i = productSVC.updateById(pid, product);

    redirectAttributes.addAttribute("pid",pid);
    return "redirect:/seller/product/{pid}";
  }

  // 판매글 단건 삭제 처리
  @PostMapping("/product/{pid}/delete") //
  public String deleteProduct(@PathVariable("pid") Long pid,
                              @RequestParam("sid") Long sid,
                              RedirectAttributes redirectAttributes) {
    productSVC.deleteById(pid);
    redirectAttributes.addAttribute("sid", sid); // 리다이렉트 URL에 sid 값을 추가
    return "redirect:/seller/list/{sid}"; // /seller/list/{sid} 주소로 이동
  }

 //삭제처리
  @PostMapping("/products/delete")
  public String deleteProducts(@RequestParam("productIds") List<Long> pids,
                               @RequestParam("sid") Long sid,
                               RedirectAttributes redirectAttributes) {
    log.info("Deleting products with pids: {}", pids);
    productSVC.deleteByIds(pids);
    redirectAttributes.addAttribute("sid", sid);
    return "redirect:/seller/list/" + sid; //
  }


}
