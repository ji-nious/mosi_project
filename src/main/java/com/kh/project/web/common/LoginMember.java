package com.kh.project.web.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 회원 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginMember {

    private Long id;
    private String email;
    private String memberType;

    /**
     * 구매자 로그인 정보 생성
     */
    public static LoginMember buyer(Long buyerId, String email) {
        return new LoginMember(buyerId, email, "BUYER");
    }

    /**
     * 판매자 로그인 정보 생성
     */
    public static LoginMember seller(Long sellerId, String email) {
        return new LoginMember(sellerId, email, "SELLER");
    }
}
