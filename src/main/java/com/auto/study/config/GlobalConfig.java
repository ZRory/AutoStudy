package com.auto.study.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalConfig {

	public static String port;
	public static String mainPageUrl = "http://47.92.44.63:89/";
	public static String checkCodeUrl = "http://hnpi.newzhihui.cn/validate.do";
	public static String loginUrl = "http://47.92.44.63:89/api/login";
	public static String userCenter = "http://47.92.44.63:89/api/getlist?page=1&pageSize=200";
	public static String studyPage = "http://hnpi.newzhihui.cn/";
	public static String updateItemRate = "http://hnpi.newzhihui.cn/study.do?action=updateItemRate";
	public static String examPaper = "http://hnpi.newzhihui.cn/examPaper.do?action=intoExamPaper";
	public static String updateRate = "http://47.92.44.63:89/api/setGiveUpList?id=";
	
	@Value("${server.port}")
	public void setPort(String port) {
		GlobalConfig.port = port;
	}

}
