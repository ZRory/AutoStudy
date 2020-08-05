package com.auto.study.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhanghangtian
 * @since 2020年8月4日 下午6:50:39
 */
@Getter
@Setter
public class UserVo {

    private String  id;
    private String  name;
    private Integer studyState;
    private String  liveProcess;
    private String  pushProcess;
    private String  homeProcess;
    private String  testProcess;

}
