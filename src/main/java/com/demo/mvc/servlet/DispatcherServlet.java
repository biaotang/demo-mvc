package com.demo.mvc.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.demo.mvc.annotation.Controller;
import com.demo.mvc.annotation.Qualifier;
import com.demo.mvc.annotation.RequestMapping;
import com.demo.mvc.annotation.Service;

public class DispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	//读取配置
	private Properties properties = new Properties();
	//类的全路径名集合
	private List<String> classNames = new ArrayList<>();
	//ioc
	private Map<String, Object> ioc = new HashMap<>();
	//保存uri和controller关系
	private Map<String, Method> handlerMapping = new HashMap<>();
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		//1.加载配置文件
		doLoadConfig(config.getInitParameter("contextConfigLocation"));
		
		//2.初始化所有相关联的类，扫描用户设定的包下面的所有类
		doScanner(properties.getProperty("scanPackage"));
		
		//3.拿到扫描的类，通过反射机制，实例化并且放到ioc容器中（k-v beanName-bean） beanName默认首字母小写
		doInstance();
		
		//4.初始化HandlerMapping(将url和method对应上)、url-实例 存放到ioc
		initHandlerMapping();
		
		//5.实现注入
		doIoc();
		
	}

	private void doIoc() {
		if (ioc.isEmpty()) {
			return ;
		}
		for (Entry<String, Object> entry : ioc.entrySet()) {
			//获取所有属性
			Field fields[] = entry.getValue().getClass().getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);//可以访问私有属性
				if (field.isAnnotationPresent(Qualifier.class)) {
					String value = field.getAnnotation(Qualifier.class).value();
					field.setAccessible(true);
					String key;
					//注解有参数就是用参数作为引用，没参数使用字段名字作为引用
					if (!"".equals(value) && value != null) {
						key = value;
					} else {
						key = field.getName();
					}
					try {
						field.set(entry.getValue(), ioc.get(key));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void initHandlerMapping() {
		if (ioc.isEmpty()) {
			return;
		}
		try {
			//存放controller的 url-method
			Map<String, Object> url_method = new HashMap<>();
			for(Entry<String, Object> entry : ioc.entrySet()) {
				Class<? extends Object> clazz = entry.getValue().getClass();
				String baseUrl = "";
				if (clazz.isAnnotationPresent(RequestMapping.class)) {
					RequestMapping annotation = clazz.getAnnotation(RequestMapping.class);
					baseUrl = annotation.value();
				}
				Method[] methods = clazz.getMethods();
				for(Method method : methods){
					if (method.isAnnotationPresent(RequestMapping.class)) {
						RequestMapping annotation = method.getAnnotation(RequestMapping.class);
						String url = annotation.value();
						
						url = (baseUrl + "/" + url).replaceAll("/+", "/");
						handlerMapping.put(url, method);
						url_method.put(url, clazz.newInstance());
						System.out.println("HandlerMapping：" + url + "," + method);
					}
				}
				ioc.putAll(url_method);
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}

	private void doInstance() {
		if (classNames.isEmpty()) {
			return ;
		}
		classNames.forEach(className -> {
			try {
				//通过反射实例化
				Class<?> clazz = Class.forName(className);
				if (clazz.isAnnotationPresent(Controller.class)) {
					Controller annotation = clazz.getAnnotation(Controller.class);
					String key = annotation.value();
					if (!"".equals(key) && key != null) {
						ioc.put(key, clazz.newInstance());
					} else {
						ioc.put(toLowerFirstWord(clazz.getSimpleName()), clazz.newInstance());
					}
				} else if (clazz.isAnnotationPresent(Service.class)) {
					Service annotation = clazz.getAnnotation(Service.class);
					String key = annotation.value();
					if (!"".equals(key) && key != null) {
						ioc.put(key, clazz.newInstance());
					} else {
						ioc.put(toLowerFirstWord(clazz.getSimpleName()), clazz.newInstance());
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private void doScanner(String packageName) {
		URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
		File dir = new File(url.getFile());
		Arrays.asList(dir.listFiles()).forEach(file -> {
			if (file.isDirectory()) {
				//递归读取package
				doScanner(packageName + "." + file.getName());
			} else {
				String className = packageName + "." + file.getName().replace(".class", "");
				classNames.add(className);
				System.out.println("容器扫描到的类有：" + packageName + "." + file.getName());
			}
		});
	}
	
	private void doLoadConfig(String initParameter) {
		//将web.xml中的contextConfigLocation对应value值的文件加载到流
		try (
			InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(initParameter);
			){
			properties.load(resourceAsStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doDispatcher(req, resp);
	}
	
	//转发请求,根据url找到相应的method
	public void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (handlerMapping.isEmpty()) {
			return ;
		}
		String url = req.getRequestURI();
		String contextPath = req.getContextPath();
		
		//拼接url并把多个 /替换成一个
		url = url.replace(contextPath, "").replaceAll("/+", "/");
		
		if (!this.handlerMapping.containsKey(url)) {
			resp.getWriter().write("404 NOT FOUND");
			return ;
		}
		Method method = this.handlerMapping.get(url);
		
		//获取方法的参数列表
		Class<?>[] parameterTypes = method.getParameterTypes();
		
		//获取请求的参数
		Map<String, String[]> parameterMap = req.getParameterMap();
		
		//保存参数值
		Object[] paramValues = new Object[parameterTypes.length];
		
		//方法的参数列表
		for (int i = 0; i < parameterTypes.length; i++) {
			String requestParam = parameterTypes[i].getSimpleName();
			if (requestParam.equals("HttpServletRequest")) {
				paramValues[i] = req;
				continue ;
			}
			if (requestParam.equals("HttpServletResponse")) {
				paramValues[i] = resp;
				continue ;
			}
			if (requestParam.equals("String")) {
				for (Entry<String, String[]> param : parameterMap.entrySet()) {
					String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "");
					paramValues[i] = value;
				}
			}
		}
		//利用反射机制来调用
		try {
			method.invoke(this.ioc.get(url), paramValues);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *	把字符串的首字母小写
	 */
	private String toLowerFirstWord(String name) {
		char[] charArray = name.toCharArray();
		charArray[0] += 32;
		return String.valueOf(charArray);
	}
	
}
