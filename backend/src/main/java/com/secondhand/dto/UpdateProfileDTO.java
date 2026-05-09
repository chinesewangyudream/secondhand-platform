package com.secondhand.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileDTO {
    @Size(min = 1, max = 20, message = "昵称长度1-20位")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    private String realName;

    private String address;
}
