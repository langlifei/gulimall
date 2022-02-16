package com.atguigu.gulimall.member.exception;

/**
 * @Author: zz
 * @Date: 2022/2/16 22:52
 */
public class PhoneException extends RuntimeException {

    public PhoneException() {
        super("存在相同的手机号");
    }
}