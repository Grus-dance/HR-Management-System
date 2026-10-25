package org.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 一般用户新建员工 + 账号。
 */
@Data
public class EmployeeCreateRequest {

    @NotBlank(message = "请输入员工编号")
    @Pattern(regexp = "^[A-Z]\\d{4}$", message = "员工编号格式应为 1 位大写字母 + 4 位数字，如 X1001")
    private String empNo;

    @NotBlank(message = "请输入姓名")
    @Size(max = 40, message = "姓名过长")
    private String name;

    @NotBlank(message = "请选择性别")
    @Pattern(regexp = "^[MF]$", message = "性别只能是 M 或 F")
    private String gender;

    private LocalDate birthDate;

    @Pattern(regexp = "^$|^\\d{17}[\\dXx]$", message = "身份证号格式不正确")
    private String idCard;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 200, message = "地址过长")
    private String address;

    @NotBlank(message = "请选择部门")
    private String department;

    @NotBlank(message = "请输入职位")
    private String position;

    @NotBlank(message = "请选择薪酬等级")
    private String gradeCode;

    @NotNull(message = "请选择入职日期")
    private LocalDate hireDate;

    @Size(max = 20, message = "学历过长")
    private String education;

    @Size(max = 80, message = "毕业院校过长")
    private String school;

    @Size(max = 80, message = "专业过长")
    private String major;

    @Size(max = 40, message = "紧急联系人过长")
    private String emergencyContact;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "紧急联系电话格式不正确")
    private String emergencyPhone;
}
