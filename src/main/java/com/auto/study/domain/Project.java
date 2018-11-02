package com.auto.study.domain;

import java.util.List;

public class Project {

	private String id;
	private long allTimes;
	private List<Class> classes;
	private boolean finished;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getAllTimes() {
		return allTimes;
	}

	public void setAllTimes(long allTimes) {
		this.allTimes = allTimes;
	}

	public List<Class> getClasses() {
		return classes;
	}

	public void setClasses(List<Class> classes) {
		this.classes = classes;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

}
