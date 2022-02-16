package com.atguigu.gulimall.auth.vo;

import lombok.Data;

/**
 * @Author: zz
 * @Date: 2022/2/16 22:28
 */
@Data
public class SocialUser {

    private String access_token;

    private String remind_in;

    private long expires_in;

    private String uid;

    private String isRealName;

}
