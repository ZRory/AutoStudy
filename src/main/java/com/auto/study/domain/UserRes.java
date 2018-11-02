package com.auto.study.domain;

import com.auto.study.domain.util.HttpClientUtil;

public class UserRes {

	private String id;
	private String account;
	private String password;
	private boolean loginState;
	private int studyState;
	private String name;
	private Project project;
	private HttpClientUtil httpClientUtil;

	public UserRes() {
		super();
		httpClientUtil = new HttpClientUtil();
	}

	public UserRes(String id, String account, String password, boolean loginState, int studyState, String name,
			Project project, HttpClientUtil httpClientUtil) {
		super();
		this.id = id;
		this.account = account;
		this.password = password;
		this.loginState = loginState;
		this.studyState = studyState;
		this.name = name;
		this.project = project;
		this.httpClientUtil = httpClientUtil;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isLoginState() {
		return loginState;
	}

	public void setLoginState(boolean loginState) {
		this.loginState = loginState;
	}

	public int getStudyState() {
		return studyState;
	}

	public void setStudyState(int studyState) {
		this.studyState = studyState;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public HttpClientUtil getHttpClientUtil() {
		return httpClientUtil;
	}

	public void setHttpClientUtil(HttpClientUtil httpClientUtil) {
		this.httpClientUtil = httpClientUtil;
	}

}
