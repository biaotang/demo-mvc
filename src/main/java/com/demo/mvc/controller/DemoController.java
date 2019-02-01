package com.demo.mvc.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.demo.mvc.annotation.Controller;
import com.demo.mvc.annotation.Qualifier;
import com.demo.mvc.annotation.RequestMapping;
import com.demo.mvc.service.DemoService;

@Controller
@RequestMapping("/demo")
public class DemoController {
	
	@Qualifier("demoService")
	private DemoService demoService;
	
	@RequestMapping("/where")
	public void where(HttpServletRequest request, HttpServletResponse response, String location) {
		
		//调用service方法
		demoService.insert(null);
		demoService.delete(null);
		demoService.update(null);
		demoService.query(null);
		
		try {
			response.setContentType("text/html; charset=UTF-8");
			response.getWriter().write(location);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
