package com.auto.study.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalConfig {

	public static String port;
	public static String mainPageUrl = "http://hnpi.newzhihui.cn/";
	public static String checkCodeUrl = "http://hnpi.newzhihui.cn/validate.do";
	public static String loginUrl = "http://hnpi.newzhihui.cn/frontLogin.do";
	public static String userCenter = "http://hnpi.newzhihui.cn/userCenter.do?action=toLesson";
	public static String studyPage = "http://hnpi.newzhihui.cn/";
	public static String updateItemRate = "http://hnpi.newzhihui.cn/study.do?action=updateItemRate";
	public static String examPaper = "http://hnpi.newzhihui.cn/examPaper.do?action=intoExamPaper";
	
	@Value("${server.port}")
	public void setPort(String port) {
		GlobalConfig.port = port;
	}

}
