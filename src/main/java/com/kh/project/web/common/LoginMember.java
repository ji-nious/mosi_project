package com.kh.project.web.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 회원 정보 (세션용)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginMember {

    private Long id;
    private String email;
    private String memberType; // 구매자 또는 판매자

    public static LoginMember buyer(Long buyerId, String email) {
        return new LoginMember(buyerId, email, "BUYER");
    }

    public static LoginMember seller(Long sellerId, String email) {
        return new LoginMember(sellerId, email, "SELLER");
    }
}
