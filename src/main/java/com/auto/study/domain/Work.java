package com.auto.study.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * 任务信息
 * 
 * @author zhanghangtian
 * @since 2020年8月4日 下午4:11:33
 */
@Getter
@Setter
public class Work {

    //课程组id
    private Integer       groupId;

    //课程状态coed  5 = 已完成
    private Integer       statusCode;

    //课程名称
    private String        name;

    //课程状态
    private String        statusText;

    //课程id
    private Integer       projectId;

    //班级id
    private Integer       classId;

    //workid
    private Integer       workId;

    //liveId 直播地址
    private Integer       liveId;

    //resourceId 资源id
    private Integer       resourceId;

    //file_id 文件id
    private Integer       fileId;

    //文件时长
    private Integer       totalTime;

    //直播内容
    private String        content;

    //直播开始时间
    private LocalDateTime liveStartTime;

    public boolean isNotStart() {
        if (liveStartTime == null) {
            return false;
        }
        return this.getLiveStartTime().plusMinutes(120L).compareTo(LocalDateTime.now()) > 0;
    }

}
