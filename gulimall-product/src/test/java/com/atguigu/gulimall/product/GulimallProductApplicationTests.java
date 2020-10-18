package com.atguigu.gulimall.product;

import com.aliyun.oss.OSSClient;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

	@Autowired
	BrandService brandService;

	@Autowired(required = false)
	OSSClient ossClient;

	@Test
	public void contextLoads() {
		BrandEntity brandEntity = new BrandEntity();
		brandEntity.setName("华为");
		brandService.save(brandEntity);
		System.out.println("修改成功");
	}

	@Test
	public void uploadFile() throws FileNotFoundException {
		InputStream inputStream = new FileInputStream("C:\\Users\\allen\\Desktop\\v2-92644b887433e8b7624e1c49718d826d_r.jpg");
		ossClient.putObject("langlifei","d826d_r.jpg",inputStream);
		ossClient.shutdown();
		System.out.println("上传成功");
	}

}
