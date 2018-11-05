package com.auto.study.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.auto.study.domain.UserListVo;
import com.auto.study.domain.UserRes;
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

	@RequestMapping(value = "/getCheckCode", method = RequestMethod.GET)
	public void getCheckCode(String id, HttpServletResponse response) throws Exception {
		ServletOutputStream outputStream = response.getOutputStream();
		systemService.getCheckCode(id, outputStream);
	}

	@RequestMapping(value = "/userLogin", method = RequestMethod.POST)
	public String userLogin(String id, String account, String password, String checkCode) throws Exception {
		return systemService.userLogin(id, account, password, checkCode);
	}

	@RequestMapping(value = "/userList", method = RequestMethod.POST)
	public ArrayList<UserListVo> userList() throws Exception {
		List<UserRes> userList = systemService.userList();
		ArrayList<UserListVo> userListVos = new ArrayList<>();
		for (UserRes userRes : userList) {
			UserListVo userListVo = new UserListVo();
			if (userRes.getProject() != null) {
				if (userRes.getProject().isFinished()) {
					userRes.setStudyState(2);
				}
			}
			BeanUtils.copyProperties(userRes, userListVo);
			userListVos.add(userListVo);
		}
		return userListVos;
	}

	@RequestMapping(value = "/userDelete", method = RequestMethod.POST)
	public void userDelete(String id) throws Exception {
		systemService.userDelete(id);
	}

	@RequestMapping(value = "/startStudy", method = RequestMethod.POST)
	public String startStudy(String id) throws Exception {
		return systemService.startStudy(id);
	}

}
