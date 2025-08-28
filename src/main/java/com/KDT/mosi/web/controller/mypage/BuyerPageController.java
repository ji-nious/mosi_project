package com.KDT.mosi.web.controller.mypage;

import com.KDT.mosi.domain.entity.BuyerPage;
import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.member.svc.MemberSVC;
import com.KDT.mosi.domain.mypage.buyer.svc.BuyerPageSVC;
import com.KDT.mosi.web.form.mypage.buyerpage.BuyerPageUpdateForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/buyer")
public class  BuyerPageController {

  private final BuyerPageSVC buyerPageSVC;
  private final MemberSVC memberSVC;

  // 🔒 로그인한 회원 ID 가져오기
  private Long getLoginMemberId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Object principal = auth.getPrincipal();

    if (principal instanceof com.KDT.mosi.security.CustomUserDetails userDetails) {
      return userDetails.getMember().getMemberId();
    }

    throw new IllegalStateException("로그인 사용자 정보를 확인할 수 없습니다.");
  }


  // ✅ 마이페이지 조회
  @GetMapping("/{memberId}")
  public String view(@PathVariable("memberId") Long memberId, Model model) {
    if (!getLoginMemberId().equals(memberId)) return "error/403";


    Optional<Member>  om = memberSVC.findById(memberId);
    Optional<BuyerPage> ob = buyerPageSVC.findByMemberId(memberId);
    if (om.isEmpty()) return "error/404";

    Member member = om.get();
    model.addAttribute("member", member);

    if (ob.isPresent()) {
      BuyerPage page = ob.get();

      // 🔽Member → BuyerPage 데이터 동기화
//      page.setTel(member.getTel());
//      page.setZonecode(member.getZonecode());
//      page.setAddress(member.getAddress());
//      page.setDetailAddress(member.getDetailAddress());
//      page.setNotification(member.getNotification());

      member.setNickname(page.getNickname());   // 닉네임도 맞춰줌

      model.addAttribute("buyerPage", page);
      return "mypage/buyerpage/viewBuyerPage";
    }
    return "redirect:/mypage/buyer/" + memberId + "/edit";
  }


  // ✅ 프로필 이미지 조회
  @GetMapping("/{memberId}/image")
  @ResponseBody
  public ResponseEntity<byte[]> image(@PathVariable("memberId") Long memberId) {
    Optional<BuyerPage> optional = buyerPageSVC.findByMemberId(memberId);

    if (optional.isPresent() && optional.get().getImage() != null) {
      byte[] image = optional.get().getImage();
      MediaType mediaType = MediaType.IMAGE_JPEG;

      try {
        String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(image));
        if (contentType != null) {
          mediaType = MediaType.parseMediaType(contentType);
        }
      } catch (IOException e) {
        log.warn("이미지 content type 분석 실패, 기본 JPEG 사용");
      }

      return ResponseEntity.ok()
          .contentType(mediaType)
          .body(image);
    }

    // ✅ 이미지가 없을 경우 기본 이미지 파일을 바이트 배열로 반환
    try {
      ClassPathResource defaultImage = new ClassPathResource("static/img/default-profile.png");
      byte[] imageBytes = defaultImage.getInputStream().readAllBytes();

      return ResponseEntity.ok()
          .contentType(MediaType.IMAGE_PNG)
          .body(imageBytes);

    } catch (IOException e) {
      log.error("기본 이미지 로드 실패", e);
      return ResponseEntity.notFound().build();
    }
  }


  // ✅ 수정 폼
  @GetMapping("/{memberId}/edit")
  public String editForm(@PathVariable("memberId") Long memberId, Model model) {
    if (!getLoginMemberId().equals(memberId)) {
      return "error/403";
    }

    // 1. member 먼저 조회
    Optional<Member> optionalMember = memberSVC.findById(memberId);
    if (optionalMember.isEmpty()) {
      return "error/403";
    }
    Member member = optionalMember.get();

    // ✅ 핵심 포인트: 먼저 등록 (템플릿 파싱 전에 반드시 model에 존재해야 함)
    model.addAttribute("member", member);

    // 2. buyerPage 있으면 form 구성
    return buyerPageSVC.findByMemberId(memberId)
        .map(entity -> {
          log.info("🔍 BuyerPage image is null? {}", entity.getImage() == null);

          BuyerPageUpdateForm form = new BuyerPageUpdateForm();
          form.setPageId(entity.getPageId());
          form.setMemberId(entity.getMemberId());
          form.setIntro(entity.getIntro());
          form.setNickname(entity.getNickname() != null ? entity.getNickname() : member.getNickname());
          form.setTel(member.getTel());
          form.setName(member.getName());
          form.setZonecode(entity.getZonecode());
          form.setAddress(entity.getAddress());
          form.setDetailAddress(entity.getDetailAddress());
          form.setNotification(member.getNotification());

          model.addAttribute("form", form);
          model.addAttribute("buyerPage", entity);
          return "mypage/buyerpage/editBuyerPage";
        })
        .orElseGet(() -> {
          BuyerPage newPage = new BuyerPage();
          newPage.setMemberId(memberId);
          newPage.setNickname(member.getNickname());

          // ✅ 여기서 create() 대신 saveOrUpdate() 호출
          Long pageId = buyerPageSVC.saveOrUpdate(newPage);

          BuyerPage entity = newPage;
          entity.setPageId(pageId); // PK 동기화

          BuyerPageUpdateForm form = new BuyerPageUpdateForm();
          form.setPageId(pageId);
          form.setMemberId(memberId);
          form.setNickname(member.getNickname());
          form.setTel(member.getTel());
          form.setName(member.getName());
          form.setZonecode(member.getZonecode());
          form.setAddress(member.getAddress());
          form.setDetailAddress(member.getDetailAddress());
          form.setNotification(member.getNotification());

          if (entity.getImage() != null) {
            form.setDeleteImage(false);
          }

          model.addAttribute("form", form);
          model.addAttribute("buyerPage", entity);
          return "mypage/buyerpage/editBuyerPage";
        });

  }


  // ✅ 마이페이지 수정 처리
  @PostMapping("/{memberId}")
  public String update(
      @PathVariable("memberId") Long memberId,
      @Valid @ModelAttribute("form") BuyerPageUpdateForm form,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes,
      Model model,
      HttpSession session) throws IOException {

    log.info("🟢 update() 진입: memberId = {}", memberId);
    log.info("📥 [form 전체값 확인] {}", form);

    // ───────────────────────────
    // 1. 권한·세션 검증
    // ───────────────────────────
    Long loginMemberId = getLoginMemberId();
    if (!loginMemberId.equals(memberId)) {
      log.warn("🚫 접근 차단: loginId={} ≠ urlId={}", loginMemberId, memberId);
      return "error/403";
    }

    // ───────────────────────────
    // 2. 검증 오류 처리
    // ───────────────────────────
    if (bindingResult.hasErrors()) {
      bindingResult.getFieldErrors()
          .forEach(err -> log.warn("❌ Validation error - {} : {}", err.getField(), err.getDefaultMessage()));

      memberSVC.findById(memberId)
          .ifPresent(m -> model.addAttribute("member", m));

      return "mypage/buyerpage/editBuyerPage";
    }

    // ───────────────────────────
    // 3. 닉네임 중복 검사 (Member + BuyerPage)
    // ───────────────────────────
    String currentNickname   = buyerPageSVC.findByMemberId(memberId)
        .map(BuyerPage::getNickname)
        .orElse(null);
    String requestedNickname = form.getNickname();

    boolean nicknameExistsInMember   = memberSVC.isExistNickname(requestedNickname);
    boolean nicknameExistsInBuyerPage = buyerPageSVC.existsByNickname(requestedNickname);

    if (requestedNickname != null
        && !requestedNickname.equals(currentNickname)
        && (nicknameExistsInMember || nicknameExistsInBuyerPage)) {

      bindingResult.rejectValue("nickname", "duplicate", "이미 사용 중인 닉네임입니다.");
      memberSVC.findById(memberId).ifPresent(m -> model.addAttribute("member", m));
      return "mypage/buyerpage/editBuyerPage";
    }

    // ───────────────────────────
    // 4. Member 기본 정보 갱신 (먼저 실행)
    // ───────────────────────────
    Member member = new Member();
    member.setMemberId(memberId);
    member.setName(form.getName());
    member.setTel(form.getTel());
    member.setZonecode(form.getZonecode());
    member.setAddress(form.getAddress());
    member.setDetailAddress(form.getDetailAddress());
    member.setNotification("Y".equals(form.getNotification()) ? "Y" : "N");
    member.setNickname(form.getNickname());
    log.info("✅ 컨트롤러에서 최종 Member.nickname = {}", member.getNickname());

    if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
      member.setPic(form.getImageFile().getBytes());
    }
    if (form.getPasswd() != null && !form.getPasswd().isBlank()) {
      member.setPasswd(form.getPasswd());
    }

    memberSVC.modify(memberId, member);

    // ───────────────────────────
    // 5. BuyerPage 갱신 (나중에 실행)
    // ───────────────────────────
    BuyerPage buyerPage = new BuyerPage();
    buyerPage.setPageId(form.getPageId());
    buyerPage.setMemberId(memberId);
    buyerPage.setNickname(requestedNickname);
    buyerPage.setIntro(form.getIntro());
    buyerPage.setTel(form.getTel());
    buyerPage.setAddress(form.getAddress());
    buyerPage.setZonecode(form.getZonecode());
    buyerPage.setDetailAddress(form.getDetailAddress());
    buyerPage.setNotification(form.getNotification());

    if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
      buyerPage.setImage(form.getImageFile().getBytes());
    } else {
      buyerPageSVC.findById(form.getPageId()).ifPresent(existing -> {
        buyerPage.setImage(existing.getImage());
      });
    }

    buyerPageSVC.update(form.getPageId(), buyerPage);

    // ───────────────────────────
    // 6. 세션 정보 동기화
    // ───────────────────────────
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember != null) {
      loginMember.setNickname(requestedNickname);
      loginMember.setTel(form.getTel());
      loginMember.setZonecode(form.getZonecode());
      loginMember.setAddress(form.getAddress());
      loginMember.setDetailAddress(form.getDetailAddress());
      session.setAttribute("loginMember", loginMember);
    }

    // ───────────────────────────
    // 7. 리다이렉트
    // ───────────────────────────
    redirectAttributes.addFlashAttribute("msg", "마이페이지 정보가 성공적으로 수정되었습니다.");
    return "redirect:/mypage/buyer/" + memberId + "/edit";
  }



  @GetMapping
  public String buyerMypageHome(Model model) {
    Long loginMemberId = getLoginMemberId();

    // MEMBER 조회
    Optional<Member> optionalMember = memberSVC.findById(loginMemberId);
    if (optionalMember.isEmpty()) {
      return "error/403"; // 로그인 정보 없음
    }
    Member member = optionalMember.get();

    // BUYER_PAGE 조회
    Optional<BuyerPage> optionalBuyerPage = buyerPageSVC.findByMemberId(loginMemberId);

    log.info("✅ buyerMypageHome - loginMemberId = {}", loginMemberId);
    log.info("✅ buyerMypageHome - member = {}", member);

    if (optionalBuyerPage.isPresent()) {
      BuyerPage buyerPage = optionalBuyerPage.get();
      log.info("✅ buyerMypageHome - buyerPage.nickname = {}", buyerPage.getNickname());
      model.addAttribute("buyerPage", buyerPage);
    }

    model.addAttribute("memberId", loginMemberId);
    model.addAttribute("member", member);
    model.addAttribute("activePath", "/mypage/buyer");

    return "mypage/buyerpage/buyerMypageHome";
  }




}



