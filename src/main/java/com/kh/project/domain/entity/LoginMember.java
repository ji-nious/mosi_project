package com.kh.project.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 회원 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginMember {

    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String memberType;

    /**
     * 구매자 여부 확인
     */
    public boolean isBuyer() {
        return MemberType.BUYER.getCode().equals(memberType);
    }

    /**
     * 판매자 여부 확인
     */
    public boolean isSeller() {
        return MemberType.SELLER.getCode().equals(memberType);
    }

    /**
     * 구매자 로그인 정보 생성
     */
    public static LoginMember buyer(Long buyerId, String email) {
        return LoginMember.builder()
                .id(buyerId)
                .email(email)
                .memberType(MemberType.BUYER.getCode())
                .build();
    }

    /**
     * 판매자 로그인 정보 생성
     */
    public static LoginMember seller(Long sellerId, String email) {
        return LoginMember.builder()
                .id(sellerId)
                .email(email)
                .memberType(MemberType.SELLER.getCode())
                .build();
    }
}
