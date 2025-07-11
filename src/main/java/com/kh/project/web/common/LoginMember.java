package com.kh.project.web.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 회원 정보 클래스
 * HTTP 세션에 저장되는 로그인된 사용자의 기본 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginMember {

    /** 회원 ID (구매자ID 또는 판매자ID) */
    private Long id;
    /** 이메일 주소 */
    private String email;
    /** 회원 타입 (BUYER, SELLER, ADMIN) */
    private String memberType;

    /**
     * 구매자 로그인 정보 생성
     * 
     * @param buyerId 구매자 ID
     * @param email 이메일 주소
     * @return LoginMember - 구매자 로그인 정보
     */
    public static LoginMember buyer(Long buyerId, String email) {
        return new LoginMember(buyerId, email, "BUYER");
    }

    /**
     * 판매자 로그인 정보 생성
     * 
     * @param sellerId 판매자 ID
     * @param email 이메일 주소
     * @return LoginMember - 판매자 로그인 정보
     */
    public static LoginMember seller(Long sellerId, String email) {
        return new LoginMember(sellerId, email, "SELLER");
    }
}
