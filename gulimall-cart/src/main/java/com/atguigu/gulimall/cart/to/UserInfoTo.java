package com.atguigu.gulimall.cart.to;

import lombok.Data;

/**
 * @Author: zz
 * @Date: 2022/3/13 15:02
 */
@Data
public class UserInfoTo {

    private Long userId;

    private String userKey;

    /**
     * 是否临时用户
     */
    private Boolean tempUser = false;

}
