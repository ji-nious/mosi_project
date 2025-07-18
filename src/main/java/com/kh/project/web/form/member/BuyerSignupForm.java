package com.kh.project.web.form.member;

import com.kh.project.web.validation.PasswordMatching;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;

@Data
@PasswordMatching
public class BuyerSignupForm {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Size(max = 20, message = "이메일은 20자 이내여야 합니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,15}$",
        message = "비밀번호는 영문, 숫자, 특수문자를 모두 포함한 8~15자여야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm;

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(min = 2, max = 10, message = "이름은 2~10자 사이여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z\\s]*$", message = "이름은 한글 또는 영문만 입력 가능합니다.")
    private String name;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 10, message = "닉네임은 2~10자 사이여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]*$", message = "닉네임은 한글, 영문, 숫자만 입력 가능합니다.")
    private String nickname;

    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호는 하이픈(-) 을 포함하여 입력해주세요.")
    private String tel;

    @Pattern(regexp = "^(남성|여성|)$")
    private String gender;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birth;

    private String postcode;

    @NotBlank(message = "주소를 선택해주세요.")
    private String address;

    @NotBlank(message = "상세주소를 입력해주세요.")
    private String detailAddress;

    /**
     * 전체 주소 조합
     */
    public String getFullAddress() {
        if (postcode != null && address != null && detailAddress != null) {
            return String.format("(%s) %s %s",
                postcode.trim(), address.trim(), detailAddress.trim());
        }
        return "";
    }
}