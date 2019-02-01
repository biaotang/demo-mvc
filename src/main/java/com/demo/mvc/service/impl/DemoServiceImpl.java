package com.demo.mvc.service.impl;

import java.util.Map;

import com.demo.mvc.annotation.Service;
import com.demo.mvc.service.DemoService;

@Service("demoService")
public class DemoServiceImpl implements DemoService {

	@Override
	public int insert(Map<String, Object> map) {
		System.out.println("I am here in DemoService insert method");
		return 0;
	}

	@Override
	public int delete(Map<String, Object> map) {
		System.out.println("I am here in DemoService delete method");
		return 0;
	}

	@Override
	public int update(Map<String, Object> map) {
		System.out.println("I am here in DemoService update method");
		return 0;
	}

	@Override
	public int query(String key) {
		System.out.println("I am here in DemoService query method");
		return 0;
	}

}
