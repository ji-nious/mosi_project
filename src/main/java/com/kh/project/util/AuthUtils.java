package com.kh.project.util;

import com.kh.project.domain.entity.LoginMember;
import com.kh.project.web.exception.MemberException;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * 권한 검증 유틸리티
 */
@Slf4j
public class AuthUtils {

    /**
     * 세션에서 로그인 회원 정보 조회
     */
    public static LoginMember getLoginMember(HttpSession session) {
        if (session == null) {
            throw new MemberException.LoginFailedException();
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute(CommonConstants.LOGIN_MEMBER_KEY);
        if (loginMember == null) {
            throw new MemberException.LoginFailedException();
        }
        
        return loginMember;
    }

    /**
     * 구매자 권한 검증
     */
    public static void validateBuyerAccess(HttpSession session, Long targetBuyerId) {
        LoginMember loginMember = getLoginMember(session);
        
        if (!CommonConstants.MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
            log.warn("구매자가 아닌 사용자가 구매자 API 접근 시도: {}", loginMember.getMemberType());
            throw new SecurityException("구매자만 접근 가능합니다.");
        }
        
        if (!loginMember.getId().equals(targetBuyerId)) {
            log.warn("다른 사용자 정보 접근 시도: loginId={}, targetId={}", 
                    loginMember.getId(), targetBuyerId);
            throw new SecurityException("본인의 정보만 접근할 수 있습니다.");
        }
    }

    /**
     * 판매자 권한 검증
     */
    public static void validateSellerAccess(HttpSession session, Long targetSellerId) {
        LoginMember loginMember = getLoginMember(session);
        
        if (!CommonConstants.MEMBER_TYPE_SELLER.equals(loginMember.getMemberType())) {
            log.warn("판매자가 아닌 사용자가 판매자 API 접근 시도: {}", loginMember.getMemberType());
            throw new SecurityException("판매자만 접근 가능합니다.");
        }
        
        if (!loginMember.getId().equals(targetSellerId)) {
            log.warn("다른 사용자 정보 접근 시도: loginId={}, targetId={}", 
                    loginMember.getId(), targetSellerId);
            throw new SecurityException("본인의 정보만 접근할 수 있습니다.");
        }
    }

    /**
     * 관리자 권한 검증
     */
    public static void validateAdminAccess(HttpSession session) {
        LoginMember loginMember = getLoginMember(session);
        
        if (!"ADMIN".equals(loginMember.getMemberType())) {
            log.warn("관리자가 아닌 사용자가 관리자 API 접근 시도: {}", loginMember.getMemberType());
            throw new SecurityException("관리자만 접근 가능합니다.");
        }
    }
} 