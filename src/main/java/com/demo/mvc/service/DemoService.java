package com.demo.mvc.service;

import java.util.Map;

public interface DemoService {
	
	int insert(Map<String, Object> map);

	int delete(Map<String, Object> map);

	int update(Map<String, Object> map);

	int query(String key);
	
}
