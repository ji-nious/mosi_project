import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.order.dto.OrderResponse;
import com.KDT.mosi.domain.order.request.OrderFormRequest;
import com.KDT.mosi.domain.order.svc.OrderSVC;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderSVC orderSVC;

    /**
     * 주문서 페이지
     */
    @GetMapping(produces = "text/html")
    public String orderFormPage(
        @RequestParam("cartItemIds") List<Long> cartItemIds,
        HttpSession session, Model model) {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        model.addAttribute("cartItemIds", cartItemIds);
        return "order/order-form";
    }

    /**
     * 주문서 데이터 조회 API
     */
    @GetMapping(value = "/form", produces = "application/json")
    @ResponseBody
    public ResponseEntity<OrderResponse> getOrderForm(
        @RequestParam("cartItemIds") List<Long> cartItemIds,
        HttpSession session) {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            OrderResponse response = orderSVC.getOrderForm(loginMember.getMemberId(), cartItemIds);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 주문 생성 API
     */
    @PostMapping(produces = "application/json")
    @ResponseBody
    public ResponseEntity<OrderResponse> createOrder(
        @Valid @RequestBody OrderFormRequest request,
        HttpSession session) {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            OrderResponse response = orderSVC.createOrderAndPay(loginMember.getMemberId(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 주문 완료 페이지 (Step 8: 페이지 분기 처리)
     */
    @GetMapping("/complete")
    public String orderCompletePage(
        @RequestParam("orderCode") String orderCode,
        HttpSession session, Model model) {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/login";
        }

        model.addAttribute("orderCode", orderCode);
        return "order/order-complete";
    }

    /**
     * 주문내역 확인 이동 (Step 8-1)
     */
    @GetMapping("/complete/to-history")
    public String redirectToOrderHistory() {
        return "redirect:/mypage/orders";
    }

    /**
     * 쇼핑 계속하기 이동 (Step 8-2)
     */
    @GetMapping("/complete/continue-shopping")
    public String continueShopping() {
        return "redirect:/";
    }
}