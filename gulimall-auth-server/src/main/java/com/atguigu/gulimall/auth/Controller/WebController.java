package com.atguigu.gulimall.auth.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/login.html")
    public String login(){
        return "login";
    }

    @GetMapping("/reg.html")
    public String reg(){
        return "reg";
    }
}
