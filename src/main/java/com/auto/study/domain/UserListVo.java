package com.auto.study.domain;

public class UserListVo {

	private String id;
	private String account;
	private boolean loginState;
	private int studyState;
	private String name;

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

}
