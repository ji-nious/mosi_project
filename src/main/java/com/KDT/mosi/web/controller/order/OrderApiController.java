package com.KDT.mosi.web.controller.order;

import com.KDT.mosi.domain.entity.Member;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 주문 관련 API 컨트롤러
 * - 주문 과정에서 필요한 데이터 조회
 */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderApiController {

  /**
   * 주문자 정보 조회 (주문페이지 전용)
   */
  @GetMapping("/member-info")
  public ResponseEntity<Map<String, Object>> getOrderMemberInfo(HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    
    if (loginMember == null) {
      return ResponseEntity.status(401).build();
    }

    Map<String, Object> memberInfo = new HashMap<>();
    memberInfo.put("name", loginMember.getName());
    memberInfo.put("tel", loginMember.getTel());
    memberInfo.put("email", loginMember.getEmail());
    
    return ResponseEntity.ok(memberInfo);
  }
}
