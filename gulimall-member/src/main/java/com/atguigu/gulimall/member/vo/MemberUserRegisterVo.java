package com.atguigu.gulimall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @Author: zz
 * @Date: 2022/2/16 22:36
 */
@Data
public class MemberUserRegisterVo {

    private String userName;

    private String password;

    private String phone;
}
