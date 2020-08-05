package com.auto.study.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhanghangtian
 * @since 2020年8月4日 下午4:36:49
 */
@Getter
@AllArgsConstructor
public enum WorkTypeEnum {

    LIVE(310, "直播学习任务"),
    PUSH(247, "推送学习任务"),
    WORK(232, "作业任务"),
    TEST(750, "试卷评测");

    private Integer code;
    private String  desc;

    public static WorkTypeEnum getWorkTypeEnumByCode(Integer code) {
        WorkTypeEnum[] values = WorkTypeEnum.values();
        for (WorkTypeEnum workTypeEnum : values) {
            if (workTypeEnum.getCode().equals(code)) {
                return workTypeEnum;
            }
        }
        return null;
    }

}
