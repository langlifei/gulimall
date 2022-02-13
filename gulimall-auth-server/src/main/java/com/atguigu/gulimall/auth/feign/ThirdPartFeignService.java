package com.atguigu.gulimall.auth.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: zz
 * @Date: 2022/2/13 18:05
 */
@FeignClient("gulimall-third-party")
public interface ThirdPartFeignService {

    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
