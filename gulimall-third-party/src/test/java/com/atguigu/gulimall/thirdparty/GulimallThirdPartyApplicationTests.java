package com.atguigu.gulimall.thirdparty;

import com.atguigu.gulimall.thirdparty.component.SmsComponent;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallThirdPartyApplicationTests {

	@Autowired
	private SmsComponent smsComponent;

	@Test
	public void contextLoads() {
	}

	@Test
	public void sms(){
		smsComponent.sendSmsCode("17671433098","wo shi ni baba!");
	}

}
