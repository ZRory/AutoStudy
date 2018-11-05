package com.auto.study.service;

import java.io.OutputStream;
import java.util.ArrayList;
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

import com.alibaba.fastjson.JSONObject;
import com.auto.study.config.GlobalConfig;
import com.auto.study.domain.Class;
import com.auto.study.domain.Project;
import com.auto.study.domain.UserRes;
import com.auto.study.domain.util.HttpClientUtil;
import com.auto.study.domain.util.ResolveUtil;

@Service
public class SystemService {

	private static Logger logger = LoggerFactory.getLogger(SystemService.class);

	@Autowired
	private ResolveUtil resolveUtil;

	public static List<UserRes> userResList = new ArrayList<UserRes>();

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
		UserRes userRes = getUserResById(id);
		userRes.setAccount(account);
		userRes.setPassword(password);
		HashMap<String, String> params = new HashMap<>();
		params.put("loginName", account);
		params.put("password", password);
		params.put("roleType", "1");
		params.put("vcode", checkCode);
		String result = userRes.getHttpClientUtil().sendPostRequestForHtmlWithParam(GlobalConfig.loginUrl, params);
		JSONObject jsonObject = JSONObject.parseObject(result);
		String state = jsonObject.get("result").toString();
		if ("success".equals(state)) {
			userRes.setLoginState(true);
			userRes.setName(jsonObject.get("name").toString());
			userLoginSucProcess(userRes);
		}
		return result;
	}

	private String generateId() {
		return UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 11);
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
		String stringid = resolveUtil.getElement(centerHtml, "div.item-line a.btn-primary").attr("href");
		project.setId(stringid);
		ArrayList<Class> classes = new ArrayList<>();
		String html = userRes.getHttpClientUtil().sendGetRequestForHtml(GlobalConfig.studyPage + stringid);
		Elements coruseArrays = resolveUtil.getElementArray(html, "div[du-render='electiveCourse'] div.course-list");
		for (Element element : coruseArrays) {
			Elements ulElements = element.getElementsByTag("ul");
			for (Element ulElement : ulElements) {
				Element lastUl = ulElement.select("ul[id]").first();
				Elements lis = lastUl.getElementsByTag("li");
				for (Element li : lis) {
					String id = li.attr("id");
					Class inClass = new Class();
					inClass.setpId(lastUl.attr("id"));
					inClass.setId(id);
					int times = Integer.parseInt(li.getElementById(id + "-times").attr("value"));
					inClass.setTime(times);
					inClass.setName(li.attr("data-original-title"));
					int rate = Integer.parseInt((li.select("span#" + id).html()).replaceAll("%", ""));
					if (rate >= 100) {
						inClass.setFinished(true);
					} else {
						finished = false;
					}
					inClass.setRate(rate);
					classes.add(inClass);
					allTimes += times;
				}
			}
		}
		project.setAllTimes(allTimes);
		project.setFinished(finished);
		project.setClasses(classes);
		if (project.isFinished()) {
			userRes.setStudyState(2);
		}
		userRes.setProject(project);
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
				Thread.sleep(10000);
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
		try {
			userLoginSucProcess(userRes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
