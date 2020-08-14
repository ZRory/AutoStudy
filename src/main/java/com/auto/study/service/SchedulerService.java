package com.auto.study.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.auto.study.domain.UserRes;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhanghangtian
 * @since 2020年8月7日 下午3:34:28
 */
@Slf4j
@Service
@EnableScheduling
public class SchedulerService {

    @Autowired
    private SystemService systemService;

    @Scheduled(cron = "0 0 4,12,20 * * ?")
    public void autoStudy() {
        log.info("开始定时自动学习");
        List<UserRes> userList = systemService.userList();
        for (UserRes userRes : userList) {
            try {
                systemService.resolveWorkList(userRes);
            } catch (Exception e) {
                log.error("解析课程失败:{}", userRes.getName(), e);
            }
        }

        for (UserRes user : userList) {
            user.setStudyState(1);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        systemService.startTestWork(user);
                        systemService.startPushWork(user);
                        systemService.startLiveWork(user);
                    } catch (Exception e) {
                        log.error("课程学习失败:", e);
                    }
                    user.setStudyState(0);
                }
            }).start();
        }
    }

}
