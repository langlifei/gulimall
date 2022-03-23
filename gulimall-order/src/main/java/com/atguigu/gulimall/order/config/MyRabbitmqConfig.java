package com.atguigu.gulimall.order.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: zz
 * @Date: 2022/3/23 22:59
 */
@Configuration
public class MyRabbitmqConfig {

    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
