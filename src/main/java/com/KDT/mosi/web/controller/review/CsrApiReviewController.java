package com.KDT.mosi.web.controller.review;

import com.KDT.mosi.domain.entity.review.ReviewEdit;
import com.KDT.mosi.domain.entity.review.ReviewProduct;
import com.KDT.mosi.domain.review.svc.ReviewSVC;
import com.KDT.mosi.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Base64;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
public class CsrApiReviewController {

  private final ReviewSVC reviewSVC;

  @GetMapping("/add/{orderItemId}")
  public String bbs(@PathVariable("orderItemId") Long orderItemId,
                    @AuthenticationPrincipal CustomUserDetails user,
                    Model model) {

    Long memberId = user.getMember().getMemberId();

    Optional<ReviewProduct> reviewProductOpt = reviewSVC.summaryFindById(orderItemId, memberId);

    ReviewProduct reviewProduct = reviewProductOpt.orElse(null);
    model.addAttribute("reviewProduct", reviewProduct);

    // byte[] -> Base64 data URI
    String imageSrc = null;
    if (reviewProduct != null) {
      byte[] data = reviewProduct.getImageData();
      if (data != null && data.length > 0) {
        String mime = (reviewProduct.getMimeType() != null && !reviewProduct.getMimeType().isBlank())
            ? reviewProduct.getMimeType()
            : "image/jpeg";
        String base64 = Base64.getEncoder().encodeToString(data);
        imageSrc = "data:" + mime + ";base64," + base64;
      }
    }
    model.addAttribute("imageSrc", imageSrc);
    return "review/review_writeForm";
//    return "review/write";
  }

  @GetMapping("/edit/{reviewId}")
  public String editReview(@PathVariable("reviewId") Long reviewId,
                           @AuthenticationPrincipal CustomUserDetails user,
                           Model model) {

    Long memberId = user.getMember().getMemberId();

    // 1) 본인 검증 + 수정용 데이터
    ReviewEdit review = reviewSVC.findReviewId(reviewId, memberId)
        .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

    // 2) productId로 요약 정보 조회
    ReviewProduct reviewProduct = reviewSVC.summaryFindByProductId(review.getProductId())
        .orElse(null);

    log.info("reviewProduct={}",reviewProduct.getOptionType());

    model.addAttribute("review", review);
    model.addAttribute("reviewProduct", reviewProduct);

    // 3) 이미지 data URI
    String imageSrc = null;
    if (reviewProduct != null) {
      byte[] data = reviewProduct.getImageData();
      if (data != null && data.length > 0) {
        String mime = (reviewProduct.getMimeType() != null && !reviewProduct.getMimeType().isBlank())
            ? reviewProduct.getMimeType()
            : "image/jpeg";
        String base64 = Base64.getEncoder().encodeToString(data);
        imageSrc = "data:" + mime + ";base64," + base64;
      }
    }
    model.addAttribute("imageSrc", imageSrc);

    return "review/review_writeForm";
  }


  @GetMapping("/seller/list")
  public String reviewListSeller(
      @AuthenticationPrincipal CustomUserDetails user,
      Model model
  ) {
    if (user == null) return "redirect:/login";

    return "review/seller_review_list";             // 목록 뷰 (앞서 만든 HTML 템플릿)
  }

  @GetMapping("/buyer/list")
  public String reviewListBuyer(
      @AuthenticationPrincipal CustomUserDetails user,
      Model model
  ) {
    if (user == null) return "redirect:/login";

    return "review/buyer_review_list";             // 목록 뷰 (앞서 만든 HTML 템플릿)
  }

}
