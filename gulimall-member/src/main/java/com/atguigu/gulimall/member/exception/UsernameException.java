package com.atguigu.gulimall.member.exception;

/**
 * @Author: zz
 * @Date: 2022/2/16 22:52
 */
public class UsernameException extends RuntimeException {


    public UsernameException() {
        super("存在相同的用户名");
    }
}