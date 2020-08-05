package com.auto.study.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.auto.study.config.GlobalConfig;
import com.auto.study.domain.UserRes;
import com.auto.study.domain.Work;
import com.auto.study.domain.WorkTypeEnum;
import com.auto.study.domain.util.HttpClientUtil;

@Service
public class SystemService {

    public static List<UserRes> userResList = Collections.synchronizedList(new ArrayList<UserRes>());;

    public UserRes initNewClient() throws Exception {
        UserRes userRes = new UserRes();
        userRes.setId(generateId());
        userRes.setHttpClientUtil(new HttpClientUtil());
        userRes.setStudyState(0);
        //HttpClientUtil httpClientUtil = userRes.getHttpClientUtil();
        //httpClientUtil.sendGetRequestForHtml(GlobalConfig.mainPageUrl);
        userResList.add(userRes);
        return userRes;
    }

    public String userLogin(String id, String account, String password, String checkCode) throws Exception {
        for (UserRes userRes : userResList) {
            if (userRes.getLoginState() && userRes.getMobile() != null && userRes.getMobile().equals(account)) {
                return "{\"result\":\"false\",\"language\":\"zh\",\"error\":\"您已登陆请勿重复登陆!\"}";
            }
        }
        UserRes userRes = getUserResById(id);
        try {
            HashMap<String, String> params = new HashMap<>();
            params.put("phone", account);
            params.put("password", DigestUtils.md5Hex(password).substring(6, 26));
            String result = userRes.getHttpClientUtil().sendPostRequestForHtmlWithParam(GlobalConfig.loginUrl, params);
            JSONObject jsonObject = JSONObject.parseObject(result);
            Integer state = jsonObject.getInteger("code");
            if (0 == state) {
                JSONObject data = jsonObject.getJSONObject("data");
                userRes.setMobile(account);
                userRes.setPassword(password);
                userRes.setLoginState(true);
                userRes.setName(data.getString("User_Nickname"));
                userRes.setToken(data.getString("User_Token"));
                userRes.setUserId(data.getInteger("User_ID"));
                System.out.println("新用户登录:" + userRes.getName());
                //异步处理课程信息
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            resolveWorkList(userRes);
                        } catch (Exception e) {
                            try {
                                resolveWorkList(userRes);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
            return result;
        } catch (Exception e) {
            userDelete(id);
            return "{\"result\":\"false\",\"language\":\"zh\",\"error\":\"登陆异常,请重试!\"}";
        }
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 6);
    }

    public UserRes getUserResById(String id) throws Exception {
        for (UserRes userRes : userResList) {
            if (userRes.getId().equals(id)) {
                return userRes;
            }
        }
        return null;
    }

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

    public Boolean resolveWorkList(UserRes user) throws Exception {
        HttpClientUtil httpClient = user.getHttpClientUtil();
        //获取课程列表
        for (int i = 1; i < 20; i++) {
            String workListUrl = GlobalConfig.workListUrl + "?rows=50&token=" + user.getToken() + "&page=" + i;
            String result = httpClient.sendGetRequestForHtml(workListUrl);
            JSONObject workResult = JSONObject.parseObject(result);
            JSONObject data = workResult.getJSONObject("data");
            JSONArray works = data.getJSONArray("data");
            for (Object object : works) {
                JSONObject workObj = JSON.parseObject(object.toString());
                Work work = new Work();
                work.setGroupId(workObj.getInteger("HomeWork_GroupID"));
                work.setStatusCode(workObj.getInteger("StatusCode"));
                work.setStatusText(workObj.getString("StatusText"));
                work.setProjectId(workObj.getInteger("HomeWork_ProjectID"));
                work.setClassId(workObj.getInteger("HomeWorkUser_MyClassID"));
                work.setWorkId(workObj.getInteger("HomeWork_ID"));
                work.setName(workObj.getString("HomeWork_Name"));
                WorkTypeEnum workTypeEnum = WorkTypeEnum.getWorkTypeEnumByCode(work.getGroupId());
                Optional<Work> exist = null;
                switch (workTypeEnum) {
                    case LIVE:
                        exist = user.getLiveWrok().stream().filter(x -> x.getWorkId().equals(work.getWorkId()))
                                .findAny();
                        if (exist.isPresent()) {
                            BeanUtils.copyProperties(work, exist.get());
                        } else {
                            user.getLiveWrok().add(work);
                        }
                        break;
                    case PUSH:
                        exist = user.getPushWrok().stream().filter(x -> x.getWorkId().equals(work.getWorkId()))
                                .findAny();
                        if (exist.isPresent()) {
                            BeanUtils.copyProperties(work, exist.get());
                        } else {
                            user.getPushWrok().add(work);
                        }
                        break;
                    case TEST:
                        exist = user.getTestWrok().stream().filter(x -> x.getWorkId().equals(work.getWorkId()))
                                .findAny();
                        if (exist.isPresent()) {
                            BeanUtils.copyProperties(work, exist.get());
                        } else {
                            user.getTestWrok().add(work);
                        }
                        break;
                    case WORK:
                        exist = user.getHomeWrok().stream().filter(x -> x.getWorkId().equals(work.getWorkId()))
                                .findAny();
                        if (exist.isPresent()) {
                            BeanUtils.copyProperties(work, exist.get());
                        } else {
                            user.getHomeWrok().add(work);
                        }
                        break;
                    default:
                        break;
                }
            }
            if (data.getInteger("last_page") == i) {
                break;
            }
        }
        //课程基本信息处理完毕
        return true;
    }

    //开始直播课学习
    public void startLiveWork(UserRes user) throws Exception {
        HttpClientUtil httpClient = user.getHttpClientUtil();
        List<Work> liveWroks = user.getLiveWrok();
        for (Work work : liveWroks) {
            //学习完成的不学习
            if (work.getStatusCode() == 5) {
                continue;
            }
            //查询直播课id
            String liveResUrl = GlobalConfig.workListResUrl + "?token=" + user.getToken() + "&homework_id="
                    + work.getWorkId() + "&class_id=" + work.getClassId();
            String liveResString = httpClient.sendGetRequestForHtml(liveResUrl);
            JSONObject liveRes = JSONObject.parseObject(liveResString);
            JSONObject live = JSONObject.parseObject(liveRes.getJSONArray("data").get(0).toString());
            //获取状态码
            if (live.getInteger("StatusCode") == 1) {
                continue;
            }
            work.setResourceId(live.getInteger("HomeWorkResource_ID"));
            work.setLiveId(live.getInteger("Live_ID"));
            work.setFileId(live.getInteger("File_ID"));
            //查询直播时长
            String liveDetailUrl = GlobalConfig.liveDetailUrl + "?token=" + user.getToken() + "&live_id="
                    + work.getLiveId();
            String liveDetailString = httpClient.sendGetRequestForHtml(liveDetailUrl);
            JSONObject liveDetailResp = JSONObject.parseObject(liveDetailString);
            JSONObject liveDetail = liveDetailResp.getJSONObject("data");
            work.setTotalTime(liveDetail.getInteger("Live_TotalTime"));
            String content = liveDetail.getString("Live_CourseContent");
            content = content.substring(content.indexOf(">") + 1, content.lastIndexOf("<"));
            if (content.contains(">") && content.contains("<")) {
                content = content.substring(content.indexOf(">") + 1, content.lastIndexOf("<"));
            }
            work.setContent("学习了:" + content + "!");
            work.setLiveStartTime(LocalDateTime.parse(liveDetail.getString("Live_StartTime").replace(" ", "T")));
            if (work.isNotStart()) {
                System.out.println("直播暂未开始:" + work.getName());
                continue;
            }
            //直播打卡
            String punchInUrl = GlobalConfig.punchIn + "?type=1&token=" + user.getToken() + "&live_id="
                    + work.getLiveId();
            String punchInResp = httpClient.sendGetRequestForHtml(punchInUrl);
            System.out.println("打卡结果:" + punchInResp);
            //课程学习
            for (int i = 0; i <= work.getTotalTime(); i = i + 180) {
                //renewal
                Map<String, String> req = new HashMap<String, String>();
                req.put("Memo", "t_live");
                req.put("mark", "Live_Playback");
                req.put("source", "2");
                req.put("property", "0");
                req.put("msg", "120");
                req.put("tb", "t_live");
                req.put("token", user.getToken());
                req.put("name", work.getName());
                req.put("id", work.getLiveId().toString());
                String renewalResp = httpClient.sendPostRequestForHtmlWithParam(GlobalConfig.renewal, req);
                //System.out.println("renewal记录结果:" + renewalResp);
                String recordLogUrl = GlobalConfig.recordLog + "?duration=180&token=" + user.getToken() + "&file_id="
                        + work.getFileId() + "&total_time=" + work.getTotalTime() + "&learn_time=" + i;
                String recordLogResp = httpClient.sendGetRequestForHtml(recordLogUrl);
                //System.out.println("课程记录结果:" + recordLogResp);
                Thread.sleep(2000);
            }
            //课程二次学习 + 延时
            for (int i = 0; i <= work.getTotalTime(); i = i + 180) {
                //renewal
                Map<String, String> req = new HashMap<String, String>();
                req.put("Memo", "t_live");
                req.put("mark", "Live_Playback");
                req.put("source", "2");
                req.put("property", "0");
                req.put("msg", "120");
                req.put("tb", "t_live");
                req.put("token", user.getToken());
                req.put("name", work.getName());
                req.put("id", work.getLiveId().toString());
                String renewalResp = httpClient.sendPostRequestForHtmlWithParam(GlobalConfig.renewal, req);
                System.out.println("renewal记录结果:" + renewalResp);
                String recordLogUrl = GlobalConfig.recordLog + "?duration=180&token=" + user.getToken() + "&file_id="
                        + work.getFileId() + "&total_time=" + work.getTotalTime() + "&learn_time=" + i;
                String recordLogResp = httpClient.sendGetRequestForHtml(recordLogUrl);
                System.out.println("课程记录结果:" + recordLogResp);

                //随时查询课程状态 如果完成就continue掉,节省学习时间
                liveResString = httpClient.sendGetRequestForHtml(liveResUrl);
                liveRes = JSONObject.parseObject(liveResString);
                live = JSONObject.parseObject(liveRes.getJSONArray("data").get(0).toString());
                //获取状态码
                if (live.getInteger("StatusCode") == 1) {
                    continue;
                }
                Thread.sleep(180000);
            }
            //新增留言
            String contentUrl = "https://nwcedu.shiwen123.com/index/consult/main/ID/" + work.getLiveId()
                    + "/type/at/userID/" + user.getUserId() + "/content/" + work.getContent();
            String contentResp = httpClient.sendGetRequestForHtml(contentUrl);
            System.out.println("留言结果:" + contentResp);

            //更新课程列表
            resolveWorkList(user);
        }
    }

    //开始直播课学习
    public void startPushWork(UserRes user) throws Exception {
        HttpClientUtil httpClient = user.getHttpClientUtil();
        List<Work> pushWorks = user.getPushWrok();
        for (Work work : pushWorks) {
            //学习完成的不学习
            if (work.getStatusCode() == 5) {
                continue;
            }
            //查询直播课id
            String liveResUrl = GlobalConfig.workListResUrl + "?token=" + user.getToken() + "&homework_id="
                    + work.getWorkId() + "&class_id=" + work.getClassId();
            String liveResString = httpClient.sendGetRequestForHtml(liveResUrl);
            JSONObject liveRes = JSONObject.parseObject(liveResString);
            JSONObject live = JSONObject.parseObject(liveRes.getJSONArray("data").get(0).toString());
            //获取状态码
            if (live.getInteger("StatusCode") == 1) {
                continue;
            }
            work.setResourceId(live.getInteger("HomeWorkResource_ID"));
            work.setFileId(live.getInteger("File_ID"));
            //更新资源读状态
            String updateUrl = GlobalConfig.updateReadStatus + "?token=" + user.getToken() + "&resource_id="
                    + work.getResourceId();
            String updateResp = httpClient.sendGetRequestForHtml(updateUrl);
            System.out.println("资源读状态更新结果:" + updateResp);
            //课程学习
            String recordLogUrl = GlobalConfig.recordLog + "?duration=60&token=" + user.getToken() + "&file_id="
                    + work.getFileId() + "&total_time=" + 1 + "&learn_time=" + 60;
            String recordLogResp = httpClient.sendGetRequestForHtml(recordLogUrl);
            System.out.println("课程记录结果:" + recordLogResp);
            Thread.sleep(2000);
            //更新课程列表
            resolveWorkList(user);
        }
    }

    //开始考试
    public void startTestWork(UserRes user) throws Exception {
        HttpClientUtil httpClient = user.getHttpClientUtil();
        List<Work> testWorks = user.getTestWrok();
        for (Work work : testWorks) {
            //学习完成的不学习
            if (work.getStatusCode() == 5) {
                continue;
            }
            //查询test id
            String testResUrl = GlobalConfig.workListResUrl + "?token=" + user.getToken() + "&homework_id="
                    + work.getWorkId() + "&class_id=" + work.getClassId();
            String testResString = httpClient.sendGetRequestForHtml(testResUrl);
            JSONObject testRes = JSONObject.parseObject(testResString);
            JSONObject test = JSONObject.parseObject(testRes.getJSONArray("data").get(0).toString());
            //获取状态码
            if (test.getInteger("StatusCode") == 1) {
                continue;
            }
            work.setResourceId(test.getInteger("HomeWorkResource_ID"));
            work.setLiveId(test.getInteger("Live_ID"));
            work.setFileId(test.getInteger("File_ID"));
            //查询回答表信息
            String answerSheetInfoUrl = GlobalConfig.answerSheetInfo + "?token=" + user.getToken() + "&file_id="
                    + work.getFileId();
            String sheetInfoString = httpClient.sendGetRequestForHtml(answerSheetInfoUrl);
            JSONObject sheetInfo = JSONObject.parseObject(sheetInfoString).getJSONObject("data").getJSONObject("list");
            if (sheetInfo.getInteger("new_type") == 3) {
                //添加回答表
                String addAnswerSheetUrl = GlobalConfig.addAnswerSheet + "?token=" + user.getToken() + "&file_id="
                        + work.getFileId();
                String addAnswerSheet = httpClient.sendGetRequestForHtml(addAnswerSheetUrl);
                System.out.println("添加新测试结果:" + addAnswerSheet);
            }
            //获取试卷信息
            String testPaperUrl = GlobalConfig.getTestPaper + "?token=" + user.getToken() + "&file_id="
                    + work.getFileId();
            String testPaperString = httpClient.sendGetRequestForHtml(testPaperUrl);
            JSONObject testPaperRes = JSONObject.parseObject(testPaperString);
            JSONObject testPaper = testPaperRes.getJSONObject("data").getJSONObject("list");
            Integer fileId = testPaper.getInteger("File_ID");
            Integer recordId = testPaper.getInteger("record_id");
            JSONArray testMain = testPaper.getJSONArray("testpaper");
            //单选题
            for (Object category : testMain) {
                JSONObject tempPaper = JSONObject.parseObject(category.toString());
                JSONArray subjects = tempPaper.getJSONArray("Subject");
                for (Object subjectObj : subjects) {
                    JSONObject subject = JSONObject.parseObject(subjectObj.toString());
                    Integer subjectId = subject.getInteger("Subject_ID");
                    JSONArray options = subject.getJSONArray("option");
                    ArrayList<Map<String, Object>> answers = new ArrayList<Map<String, Object>>();
                    for (Object optionObj : options) {
                        JSONObject option = JSONObject.parseObject(optionObj.toString());
                        if (option.getInteger("status").equals(1)) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("image", 0);
                            hashMap.put("title", 0);
                            hashMap.put("option_id", option.getInteger("Option_ID"));
                            hashMap.put("name", option.getString("Option_Name"));
                            answers.add(hashMap);
                        }
                    }
                    HashMap<String, Object> answerMain = new HashMap<String, Object>();
                    answerMain.put("file_id", fileId);
                    answerMain.put("record_id", recordId);
                    answerMain.put("sheet_array", answers);
                    answerMain.put("subject_id", subjectId);
                    answerMain.put("token", user.getToken());
                    //发送填表请求
                    String answerResp = httpClient.sendPostRequestForHtmlWithParam(GlobalConfig.addAnswer,
                            JSON.toJSONString(answerMain));
                    System.out.println("回答结果:" + answerResp);
                }
            }
            String endAnswerUrl = GlobalConfig.endAnswer + "?token=" + user.getToken() + "&file_id=" + work.getFileId();
            String endResp = httpClient.sendGetRequestForHtml(endAnswerUrl);
            System.out.println("试卷提交结果:" + endResp);

            //更新课程列表
            resolveWorkList(user);
        }
    }

}
