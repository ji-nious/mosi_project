package com.kh.project.domain;

import com.kh.project.domain.buyer.svc.BuyerSVC;
import com.kh.project.domain.entity.LoginMember;
import com.kh.project.domain.seller.svc.SellerSVC;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
     * 현재 로그인한 사용자 정보 조회
     */

    public LoginMember getCurrentUserInfo(HttpSession session) {
        if (session == null) {
            return null;
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null) {
            return null;
        }
        
        return loginMember;
    }

    /**
     * 사용자 ID 조회
     */
    public Long getCurrentUserId(HttpSession session) {
        LoginMember loginMember = getCurrentUserInfo(session);
        return loginMember != null ? loginMember.getId() : null;
    }

    /**
     * 사용자 이메일 조회
     */
    public String getCurrentUserEmail(HttpSession session) {
        LoginMember loginMember = getCurrentUserInfo(session);
        return loginMember != null ? loginMember.getEmail() : null;
    }

    /**
     * 현재 로그인한 사용자가 특정 ID의 사용자인지 확인
     */
    public boolean isCurrentUser(HttpSession session, Long userId) {
        LoginMember sessionMember = (LoginMember) session.getAttribute("loginMember");
        return sessionMember != null && sessionMember.getId().equals(userId);
    }

    /**
     * 세션에 로그인 정보 저장
     */
    public void setLoginSession(HttpSession session, Long id, String email, String memberType) {
        LoginMember loginMember = new LoginMember();
        loginMember.setId(id);
        loginMember.setEmail(email);
        loginMember.setMemberType(memberType);
        
        session.setAttribute("loginMember", loginMember);
        session.setMaxInactiveInterval(1800);
        
        log.info("로그인 세션 생성: id={}, email={}, memberType={}", id, email, memberType);
    }

    /**
     * 로그인 여부 확인
     */
    public boolean isLoggedIn(HttpSession session) {
        if (session == null) {
            return false;
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        return loginMember != null;
    }

    /**
     * 로그아웃 처리
     */
    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
            log.info("로그아웃 처리 완료");
        }
    }

    /**
     * 현재 사용자 타입 확인
     */
    public String getCurrentUserType(HttpSession session) {
        LoginMember loginMember = getCurrentUserInfo(session);
        return loginMember != null ? loginMember.getMemberType() : null;
    }

    /**
     * 구매자 여부 확인
     */
    public boolean isBuyer(HttpSession session) {
        String userType = getCurrentUserType(session);
        return "BUYER".equals(userType);
    }

    /**
     * 판매자 여부 확인
     */
    public boolean isSeller(HttpSession session) {
        String userType = getCurrentUserType(session);
        return "SELLER".equals(userType);
    }

    /**
     * 현재 로그인한 사용자의 상세 정보 조회
     */
    public Object getCurrentUserDetails(HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null) {
            return null;
        }
        
        try {
            if ("BUYER".equals(loginMember.getMemberType())) {
                return buyerSVC.findById(loginMember.getId()).orElse(null);
            } else if ("SELLER".equals(loginMember.getMemberType())) {
                return sellerSVC.findById(loginMember.getId()).orElse(null);
            }
        } catch (Exception e) {
            log.error("사용자 상세 정보 조회 실패: {}", e.getMessage());
        }
        
        return null;
    }
} 