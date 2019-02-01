# demo-mvc

springmvc原理

1. 读取配置

通过web.xml加载DispathcerServlet和读取配置文件



2. 初始化

springmvc初始化九大组件

​	initMultipartResolver(context);
​	initLocaleResolver(context);
​	initThemeResolver(context);
​	initHandlerMappings(context);
​	initHandlerAdapters(context);
​	initHandlerExceptionResolvers(context);
​	initRequestToViewNameTranslator(context);
​	initViewResolvers(context);
​	initFlashMapManager(context);	

demo中只实现最基本的： 1.加载配置文件；2.扫描用户配置包下的类；3.通过反射机制实例化包下的类，并且放到ioc容器中（Map的键值对beanName-bean）beanName默认是首字母小写；4.初始化HandlerMapping ；5.实现注入



3. 运行

用户发起请求，dispatcher拦截请求，并通过handlerMapping找到相应的Method，通过反射机制调用获取到结果