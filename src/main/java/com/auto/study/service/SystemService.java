package com.auto.study.service;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.auto.study.config.GlobalConfig;
import com.auto.study.domain.Class;
import com.auto.study.domain.Project;
import com.auto.study.domain.UserRes;
import com.auto.study.domain.util.HttpClientUtil;
import com.auto.study.domain.util.ResolveUtil;

@Service
public class SystemService {

    private static Logger       logger      = LoggerFactory.getLogger(SystemService.class);

    @Autowired
    private ResolveUtil         resolveUtil;

    public static List<UserRes> userResList = Collections.synchronizedList(new ArrayList<UserRes>());;

    public UserRes initNewClient() throws Exception {
        UserRes userRes = new UserRes();
        userRes.setId(generateId());
        userRes.setLoginState(false);
        HttpClientUtil httpClientUtil = userRes.getHttpClientUtil();
        httpClientUtil.sendGetRequestForHtml(GlobalConfig.mainPageUrl);
        userResList.add(userRes);
        return userRes;
    }

    public void getCheckCode(String id, OutputStream outPutStream) throws Exception {
        UserRes userRes = getUserResById(id);
        CloseableHttpResponse response = userRes.getHttpClientUtil()
                .sendGetRequestForResponse(GlobalConfig.checkCodeUrl);
        response.getEntity().writeTo(outPutStream);
        response.close();
    }

    public String userLogin(String id, String account, String password, String checkCode) throws Exception {
        for (UserRes userRes : userResList) {
            if (userRes.getAccount() != null && userRes.getAccount().equals(account)) {
                // userDelete(id);
                return "{\"result\":\"false\",\"language\":\"zh\",\"error\":\"您已登陆请勿重复登陆!\"}";
            }
        }
        UserRes userRes = getUserResById(id);
        HashMap<String, String> params = new HashMap<>();
        params.put("userName", account);
        params.put("password", password);
        String result = userRes.getHttpClientUtil().sendPostRequestForHtmlWithParam(GlobalConfig.loginUrl, params);
        JSONObject jsonObject = JSONObject.parseObject(result);
        Boolean state = Boolean.parseBoolean(jsonObject.get("success").toString());
        if (state) {
            userRes.setAccount(account);
            userRes.setPassword(password);
            userRes.setLoginState(true);
            //userRes.setName(jsonObject.get("name").toString());
            userLoginSucProcess(userRes);
        }
        return "学习完成";
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 5);
    }

    private UserRes getUserResById(String id) throws Exception {
        for (UserRes userRes : userResList) {
            if (userRes.getId().equals(id)) {
                return userRes;
            }
        }
        return null;
    }

    //	private void clearUser() {
    //		Iterator<UserRes> it = userResList.iterator();
    //		while (it.hasNext()) {
    //			UserRes x = it.next();
    //			if (!x.isLoginState()) {
    //				if (x.getAccount() == null) {
    //					it.remove();
    //				}
    //			}
    //		}
    //	}

    public List<UserRes> userList() {
        return userResList;
    }

    public void userDelete(String id) {
        for (UserRes userRes : userResList) {
            if (userRes.getId().equals(id)) {
                userResList.remove(userRes);
                return;
            }
        }
    }

    private void userLoginSucProcess(UserRes userRes) throws Exception {
        Project project = new Project();
        long allTimes = 0L;
        boolean finished = true;
        String centerHtml = userRes.getHttpClientUtil().sendGetRequestForHtml(GlobalConfig.userCenter);
        JSONObject classListObject = JSONObject.parseObject(centerHtml);
        JSONArray classList = classListObject.getJSONArray("list");
        //OID GiveUp duration 时长
        for (Object temp : classList) {
            JSONObject t = JSONObject.parseObject(temp.toString());
            String OID = t.getString("OID");
            int duration = t.getInteger("duration");
            int num = duration / 2 + duration % 2;
            while (num > 0) {
                String result = userRes.getHttpClientUtil().sendGetRequestForHtml(GlobalConfig.updateRate + OID);
                if (result.contains("false")) {
                    System.out.println("ERROR:" + t.getString("name") + "异常");
                }
                num--;
            }
            System.out.println(t.getString("name") + "学习完成");
        }
    }

    public String startStudy(String id) throws Exception {
        UserRes userRes = getUserResById(id);
        if (userRes.getStudyState() == 2) {
            return "学习已完成";
        }
        Project project = userRes.getProject();
        if (project.isFinished()) {
            userRes.setStudyState(2);
            return "学习已完成";
        }
        new Thread() {
            @Override
            public void run() {
                mainStudy(userRes);
            }
        }.start();
        userRes.setStudyState(1);
        return "开始自动学习";
    }

    private void mainStudy(UserRes userRes) {
        Project project = userRes.getProject();
        List<Class> classes = project.getClasses();
        // long allTimes = project.getAllTimes();
        for (Class sClass : classes) {
            if (sClass.isFinished()) {
                continue;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("id", sClass.getId());
            params.put("time", sClass.getTime() + "");
            params.put("itemRate", 100 + "");
            params.put("wareRate", 100 + "");
            try {
                String response = userRes.getHttpClientUtil()
                        .sendPostRequestForHtmlWithParam(GlobalConfig.updateItemRate, params);
                JSONObject jsonObject = JSONObject.parseObject(response);
                String state = jsonObject.get("result").toString();
                if (!"success".equals(state)) {
                    logger.error(response);
                    logger.error("课程进度更新失败：" + userRes.getName() + sClass.getName());
                } else {
                    logger.info("课程进度更新成功：" + userRes.getName() + sClass.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        userRes.setStudyState(0);
        project.setFinished(false);
        try {
            userLoginSucProcess(userRes);
        } catch (Exception e) {
            try {
                userLoginSucProcess(userRes);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

}
