package com.kh.project.web.common;

import com.kh.project.web.exception.MemberException;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * 권한 검증 유틸리티 클래스
 * 로그인 상태 확인 및 본인 정보 접근 권한 검증
 */
@Slf4j
public class AuthUtils {

    /**
     * 세션에서 로그인 회원 정보 조회
     * @param session HTTP 세션
     * @return LoginMember - 로그인된 회원 정보
     * @throws MemberException.LoginFailedException 로그인되지 않은 경우
     */
    public static LoginMember getLoginMember(HttpSession session) {
        if (session == null) {
            throw new MemberException.LoginFailedException();
        }
        
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null) {
            throw new MemberException.LoginFailedException();
        }
        
        return loginMember;
    }

    /**
     * 구매자 권한 검증 (본인만 접근 가능)
     * 
     * @param session HTTP 세션
     * @param targetBuyerId 접근하려는 구매자 ID
     * @throws SecurityException 권한이 없는 경우
     */
    public static void validateBuyerAccess(HttpSession session, Long targetBuyerId) {
        LoginMember loginMember = getLoginMember(session);
        
        if (!"BUYER".equals(loginMember.getMemberType())) {
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
     * 판매자 권한 검증 (본인만 접근 가능)
     * 
     * @param session HTTP 세션
     * @param targetSellerId 접근하려는 판매자 ID
     * @throws SecurityException 권한이 없는 경우
     */
    public static void validateSellerAccess(HttpSession session, Long targetSellerId) {
        LoginMember loginMember = getLoginMember(session);
        
        if (!"SELLER".equals(loginMember.getMemberType())) {
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
     * 
     * @param session HTTP 세션
     * @throws SecurityException 관리자가 아닌 경우
     */
    public static void validateAdminAccess(HttpSession session) {
        LoginMember loginMember = getLoginMember(session);
        
        if (!"ADMIN".equals(loginMember.getMemberType())) {
            log.warn("관리자가 아닌 사용자가 관리자 API 접근 시도: {}", loginMember.getMemberType());
            throw new SecurityException("관리자만 접근 가능합니다.");
        }
    }
} 