package com.kh.project.web.common;

import com.kh.project.domain.buyer.svc.BuyerSVC;
import com.kh.project.domain.seller.svc.SellerSVC;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.domain.entity.Seller;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 세션 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final BuyerSVC buyerSVC;
    private final SellerSVC sellerSVC;

    /**
     * 현재 로그인된 사용자 정보 조회
     */
    public UserInfo getCurrentUserInfo(HttpSession session) {
        if (session == null) {
            return null;
        }

        LoginMember loginMember = (LoginMember) session.getAttribute(CommonConstants.LOGIN_MEMBER_KEY);
        if (loginMember == null) {
            return null;
        }

        try {
            if (MemberType.BUYER.getCode().equals(loginMember.getMemberType())) {
                return getBuyerInfo(loginMember.getId());
            } else if (MemberType.SELLER.getCode().equals(loginMember.getMemberType())) {
                return getSellerInfo(loginMember.getId());
            }
        } catch (Exception e) {
            log.error("사용자 정보 조회 실패: memberId={}, memberType={}", 
                     loginMember.getId(), loginMember.getMemberType(), e);
        }

        return null;
    }

    /**
     * 모델에 사용자 정보 추가
     */
    public void addUserInfoToModel(HttpSession session, Model model) {
        UserInfo userInfo = getCurrentUserInfo(session);
        if (userInfo != null) {
            model.addAttribute("userNickname", userInfo.getNickname());
            model.addAttribute("userName", userInfo.getName());
            model.addAttribute("userType", userInfo.getMemberType());
        }
    }

    /**
     * 로그인 세션 생성
     */
    public void createLoginSession(HttpSession session, LoginMember loginMember) {
        session.setAttribute(CommonConstants.LOGIN_MEMBER_KEY, loginMember);
        session.setMaxInactiveInterval(CommonConstants.SESSION_TIMEOUT);
        
        log.info("로그인 세션 생성: memberId={}, memberType={}", 
                loginMember.getId(), loginMember.getMemberType());
    }

    /**
     * 로그아웃 처리
     */
    public void logout(HttpSession session) {
        if (session != null) {
            LoginMember loginMember = (LoginMember) session.getAttribute(CommonConstants.LOGIN_MEMBER_KEY);
            session.invalidate();
            
            if (loginMember != null) {
                log.info("로그아웃 처리 완료: memberId={}, memberType={}", 
                        loginMember.getId(), loginMember.getMemberType());
            }
        }
    }

    /**
     * 구매자 정보 조회
     */
    private UserInfo getBuyerInfo(Long buyerId) {
        Optional<Buyer> buyerOpt = buyerSVC.findById(buyerId);
        if (buyerOpt.isPresent()) {
            Buyer buyer = buyerOpt.get();
            return UserInfo.builder()
                    .id(buyer.getBuyerId())
                    .email(buyer.getEmail())
                    .name(buyer.getName())
                    .nickname(buyer.getNickname())
                    .memberType(MemberType.BUYER.getCode())
                    .build();
        }
        return null;
    }

    /**
     * 판매자 정보 조회
     */
    private UserInfo getSellerInfo(Long sellerId) {
        Optional<Seller> sellerOpt = sellerSVC.findById(sellerId);
        if (sellerOpt.isPresent()) {
            Seller seller = sellerOpt.get();
            return UserInfo.builder()
                    .id(seller.getSellerId())
                    .email(seller.getEmail())
                    .name(seller.getName())
                    .nickname(seller.getShopName())
                    .memberType(MemberType.SELLER.getCode())
                    .build();
        }
        return null;
    }

    /**
     * 비밀번호 확인
     */
    public Map<String, Object> verifyPassword(HttpSession session, String password) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LoginMember loginMember = (LoginMember) session.getAttribute(CommonConstants.LOGIN_MEMBER_KEY);
            if (loginMember == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }
            
            if (password == null || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "비밀번호를 입력해주세요.");
                return response;
            }
            
            boolean isValid = false;
            
            if (CommonConstants.MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
                isValid = buyerSVC.checkPassword(loginMember.getId(), password);
            } else if (CommonConstants.MEMBER_TYPE_SELLER.equals(loginMember.getMemberType())) {
                isValid = sellerSVC.checkPassword(loginMember.getId(), password);
            }
            
            response.put("success", isValid);
            
            if (!isValid) {
                response.put("message", "비밀번호가 틀렸습니다.");
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("비밀번호 확인 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다.");
            return response;
        }
    }
} 