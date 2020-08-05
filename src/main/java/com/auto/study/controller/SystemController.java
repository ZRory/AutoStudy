package com.auto.study.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.auto.study.domain.UserRes;
import com.auto.study.domain.UserVo;
import com.auto.study.service.SystemService;

@RestController
@RequestMapping(value = "/system")
public class SystemController {

    @Autowired
    private SystemService systemService;

    @RequestMapping(value = "/initClient", method = RequestMethod.POST)
    public String initNewClient() throws Exception {
        UserRes newUser = systemService.initNewClient();
        return newUser.getId();
    }

    @RequestMapping(value = "/userLogin", method = RequestMethod.POST)
    public String userLogin(String id, String account, String password, String checkCode) throws Exception {
        return systemService.userLogin(id, account, password, checkCode);
    }

    @RequestMapping(value = "/userList", method = RequestMethod.POST)
    public List<UserVo> userList(String account) throws Exception {
        List<UserRes> userList = systemService.userList();
        if (!StringUtils.isEmpty(account)) {
            userList = userList.stream().filter(x -> x.getMobile().equals(account)).collect(Collectors.toList());
        }
        List<UserVo> result = new ArrayList<UserVo>();
        for (UserRes userRes : userList) {
            if (!userRes.getLoginState()) {
                continue;
            }
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(userRes, userVo);
            userVo.setName("*" + userVo.getName().substring(1));
            long homeCount = userRes.getHomeWrok().stream().filter(x -> x.getStatusCode() == 5).count();
            userVo.setHomeProcess(homeCount + "/" + userRes.getHomeWrok().size());
            long liveCount = userRes.getLiveWrok().stream().filter(x -> x.getStatusCode() == 5).count();
            userVo.setLiveProcess(userRes.getLiveWrok().stream().filter(x -> x.isNotStart()).count() + "/" + liveCount
                    + "/" + userRes.getLiveWrok().size());
            long pushCount = userRes.getPushWrok().stream().filter(x -> x.getStatusCode() == 5).count();
            userVo.setPushProcess(pushCount + "/" + userRes.getPushWrok().size());
            long testCount = userRes.getTestWrok().stream().filter(x -> x.getStatusCode() == 5).count();
            userVo.setTestProcess(testCount + "/" + userRes.getTestWrok().size());
            if (homeCount + liveCount + pushCount + testCount == userRes.getHomeWrok().size()
                    + userRes.getLiveWrok().size() + userRes.getPushWrok().size() + userRes.getTestWrok().size()) {
                userVo.setStudyState(2);
            }
            result.add(userVo);
        }
        return result;
    }

    @RequestMapping(value = "/startStudy", method = RequestMethod.POST)
    public void startStudy(String id) throws Exception {
        UserRes user = systemService.getUserResById(id);
        user.setStudyState(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    systemService.startTestWork(user);
                    systemService.startPushWork(user);
                    systemService.startLiveWork(user);
                } catch (Exception e) {
                    user.setStudyState(0);
                }
            }
        });
    }

    @RequestMapping(value = "/userDelete", method = RequestMethod.POST)
    public void userDelete(String id) throws Exception {
        systemService.userDelete(id);
    }

    @RequestMapping(value = "/resolveWork", method = RequestMethod.POST)
    public boolean resolveWork(String id) throws Exception {
        UserRes user = systemService.getUserResById(id);
        systemService.resolveWorkList(user);
        return true;
    }

}
