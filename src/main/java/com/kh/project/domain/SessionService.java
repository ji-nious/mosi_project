package com.kh.project.domain;

import com.kh.project.domain.buyer.svc.BuyerSVC;
import com.kh.project.domain.entity.LoginMember;
import com.kh.project.domain.entity.MemberType;
import com.kh.project.domain.seller.svc.SellerSVC;
import com.kh.project.web.exception.UserException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 세션 관리 서비스 - AuthUtils 통합 버전
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final BuyerSVC buyerSVC;
    private final SellerSVC sellerSVC;

    // 상수 정의 - Enum 기반
    private static final String LOGIN_MEMBER_SESSION_KEY = "loginMember";
    private static final int DEFAULT_SESSION_TIMEOUT = 1800; // 30분

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    public LoginMember getCurrentUserInfo(HttpSession session) {
        if (session == null) {
            return null;
        }

        LoginMember loginMember = (LoginMember) session.getAttribute(LOGIN_MEMBER_SESSION_KEY);
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
        LoginMember sessionMember = (LoginMember) session.getAttribute(LOGIN_MEMBER_SESSION_KEY);
        return sessionMember != null && sessionMember.getId().equals(userId);
    }

    /**
     * 세션에 로그인 정보 저장
     */
    public void setLoginSession(HttpSession session, Long id, String email, String memberType, String nickname) {
        LoginMember loginMember = new LoginMember();
        loginMember.setId(id);
        loginMember.setEmail(email);
        loginMember.setMemberType(memberType);
        loginMember.setNickname(nickname);

        session.setAttribute(LOGIN_MEMBER_SESSION_KEY, loginMember);
        session.setMaxInactiveInterval(DEFAULT_SESSION_TIMEOUT);

        log.info("로그인 세션 생성: id={}, email={}, memberType={}, nickname={}", id, email, memberType, nickname);
    }

    /**
     * 로그인 여부 확인
     */
    public boolean isLoggedIn(HttpSession session) {
        if (session == null) {
            return false;
        }

        LoginMember loginMember = (LoginMember) session.getAttribute(LOGIN_MEMBER_SESSION_KEY);
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
     * 구매자 여부 확인 - Enum 사용
     */
    public boolean isBuyer(HttpSession session) {
        String userType = getCurrentUserType(session);
        return MemberType.BUYER.getCode().equals(userType);
    }

    /**
     * 판매자 여부 확인 - Enum 사용
     */
    public boolean isSeller(HttpSession session) {
        String userType = getCurrentUserType(session);
        return MemberType.SELLER.getCode().equals(userType);
    }

    /**
     * 현재 로그인한 사용자의 상세 정보 조회
     */
    public Object getCurrentUserDetails(HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(LOGIN_MEMBER_SESSION_KEY);
        if (loginMember == null) {
            return null;
        }

        try {
            if (MemberType.BUYER.getCode().equals(loginMember.getMemberType())) {
                return buyerSVC.findById(loginMember.getId()).orElse(null);
            } else if (MemberType.SELLER.getCode().equals(loginMember.getMemberType())) {
                return sellerSVC.findById(loginMember.getId()).orElse(null);
            }
        } catch (Exception e) {
            log.error("사용자 상세 정보 조회 실패: {}", e.getMessage());
        }

        return null;
    }

    // ============ AuthUtils 통합 메서드들 ============

    /**
     * 구매자 권한 검증 및 세션 정보 반환
     */
    public LoginMember validateBuyerAccess(HttpSession session, Long targetBuyerId) {
        LoginMember loginMember = getCurrentUserInfo(session);
        if (loginMember == null) {
            log.warn("로그인하지 않은 사용자가 구매자 API 접근 시도");
            throw new UserException.LoginRequired();
        }

        if (!MemberType.BUYER.getCode().equals(loginMember.getMemberType())) {
            log.warn("구매자가 아닌 사용자가 구매자 API 접근 시도: {}", loginMember.getMemberType());
            throw new UserException.AccessDenied("구매자만 접근 가능합니다.");
        }

        if (!loginMember.getId().equals(targetBuyerId)) {
            log.warn("다른 사용자 정보 접근 시도: loginId={}, targetId={}",
                loginMember.getId(), targetBuyerId);
            throw new UserException.AccessDenied("본인의 정보만 접근할 수 있습니다.");
        }

        return loginMember;
    }

    /**
     * 판매자 권한 검증 및 세션 정보 반환
     */
    public LoginMember validateSellerAccess(HttpSession session, Long targetSellerId) {
        LoginMember loginMember = getCurrentUserInfo(session);
        if (loginMember == null) {
            log.warn("로그인하지 않은 사용자가 판매자 API 접근 시도");
            throw new UserException.LoginRequired();
        }

        if (!MemberType.SELLER.getCode().equals(loginMember.getMemberType())) {
            log.warn("판매자가 아닌 사용자가 판매자 API 접근 시도: {}", loginMember.getMemberType());
            throw new UserException.AccessDenied("판매자만 접근 가능합니다.");
        }

        if (!loginMember.getId().equals(targetSellerId)) {
            log.warn("다른 사용자 정보 접근 시도: loginId={}, targetId={}",
                loginMember.getId(), targetSellerId);
            throw new UserException.AccessDenied("본인의 정보만 접근할 수 있습니다.");
        }

        return loginMember;
    }

    /**
     * 구매자 세션 검증 (타겟 ID 없이)
     */
    public LoginMember validateBuyerSession(HttpSession session) {
        LoginMember loginMember = getCurrentUserInfo(session);
        if (loginMember == null) {
            throw new UserException.LoginRequired();
        }

        if (!MemberType.BUYER.getCode().equals(loginMember.getMemberType())) {
            throw new UserException.AccessDenied("구매자만 접근 가능합니다.");
        }

        return loginMember;
    }

    /**
     * 판매자 세션 검증 (타겟 ID 없이)
     */
    public LoginMember validateSellerSession(HttpSession session) {
        LoginMember loginMember = getCurrentUserInfo(session);
        if (loginMember == null) {
            throw new UserException.LoginRequired();
        }

        if (!MemberType.SELLER.getCode().equals(loginMember.getMemberType())) {
            throw new UserException.AccessDenied("판매자만 접근 가능합니다.");
        }

        return loginMember;
    }

    /**
     * 권한 검증 없이 로그인 회원 정보만 조회
     */
    public LoginMember getLoginMemberWithoutValidation(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (LoginMember) session.getAttribute(LOGIN_MEMBER_SESSION_KEY);
    }
}