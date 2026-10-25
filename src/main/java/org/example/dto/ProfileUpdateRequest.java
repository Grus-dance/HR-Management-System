package org.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 个人用户可编辑字段；锁定字段不在其中，因此无法被修改。
 */
@Data
public class ProfileUpdateRequest {

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 80, message = "邮箱过长")
    private String email;

    @Size(max = 200, message = "地址过长")
    private String address;

    @Size(max = 40, message = "紧急联系人过长")
    private String emergencyContact;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "紧急联系电话格式不正确")
    private String emergencyPhone;
}
