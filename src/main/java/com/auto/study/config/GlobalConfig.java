package com.auto.study.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalConfig {

    public static String port;

    //登录接口
    public static String loginUrl         = "https://nwcedu.shiwen123.com/api/user/login";

    //获取课程信息
    public static String workListUrl      = "https://nwcedu.shiwen123.com/api/homework/getMyWorkList";

    //获取直播信息(id)
    public static String workListResUrl   = "https://nwcedu.shiwen123.com/api/homework/getMyWorkResourceList";

    //获取直播详情
    public static String liveDetailUrl    = "https://nwcedu.shiwen123.com/api/live/getLiveDetail";

    //直播打卡
    public static String punchIn          = "https://nwcedu.shiwen123.com/api/live/punchIn";

    //进度更新
    public static String updateReadStatus = "https://nwcedu.shiwen123.com/api/homework/updateHomeWorkResourceReadStatus";

    //进度更新
    public static String renewal          = "https://nwcedu.shiwen123.com/index/log/renewal";

    //进度更新
    public static String recordLog        = "https://nwcedu.shiwen123.com/api/live/recordLog";

    //获取回答表信息
    public static String answerSheetInfo  = "https://nwcedu.shiwen123.com/index/testpaper/AnswerSheetInfo";
    //添加回答表(new type=3时)
    public static String addAnswerSheet   = "https://nwcedu.shiwen123.com/index/testpaper/addAnswerSheet";

    //获取试卷信息
    public static String getTestPaper     = "https://nwcedu.shiwen123.com/index/testpaper/getTestpaperInfo";

    //获取试卷信息
    public static String addAnswer        = "https://nwcedu.shiwen123.com/index/testpaper/addAnswerSheetDetail";

    //提交试卷
    public static String endAnswer        = "https://nwcedu.shiwen123.com/index/testpaper/endAnswerSheet";

    @Value("${server.port}")
    public void setPort(String port) {
        GlobalConfig.port = port;
    }

}
