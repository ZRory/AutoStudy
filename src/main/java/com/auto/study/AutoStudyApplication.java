package com.auto.study;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.auto.study.config.GlobalConfig;

@SpringBootApplication
public class AutoStudyApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoStudyApplication.class, args);
		try {
			System.out.println("提示:X尽量不要使用IE浏览器X,请使用Chrome,火狐,360浏览器等");
			System.out.println("浏览器访问以下网址-->配置学习账号并开始学习:");
			System.out.println(InetAddress.getLocalHost().getHostAddress().toString() + ":" + GlobalConfig.port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}
