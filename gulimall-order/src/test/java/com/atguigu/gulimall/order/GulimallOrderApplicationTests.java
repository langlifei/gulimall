package com.atguigu.gulimall.order;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {

	@Autowired
	private AmqpAdmin amqpAdmin;

	@Test
	public void contextLoads() {
	}

	@Test
	public void createExchange() {
		amqpAdmin.declareExchange(new DirectExchange("hello-java-atiguigu"));
	}

}
