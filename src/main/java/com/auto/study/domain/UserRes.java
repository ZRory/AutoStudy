package com.auto.study.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.auto.study.domain.util.HttpClientUtil;

import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRes implements Serializable {

    private static final long serialVersionUID = -6904184947090848843L;
    private String            id;
    private Integer           userId;
    private String            mobile;
    private String            password;
    private Boolean           loginState       = false;
    private Integer           studyState;
    private String            name;
    private String            token;
    private HttpClientUtil    httpClientUtil;

    //直播课
    private List<Work>        liveWrok         = new ArrayList<Work>();
    //推送课
    private List<Work>        pushWrok         = new ArrayList<Work>();
    //作业任务
    private List<Work>        homeWrok         = new ArrayList<Work>();
    //测试任务
    private List<Work>        testWrok         = new ArrayList<Work>();

}
