package com.gzf.demo.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sun.security.jca.GetInstance.Instance;

import com.gzf.springmvc.annotation.GZFAutowrited;
import com.gzf.springmvc.annotation.GZFController;
import com.gzf.springmvc.annotation.GZFRequestMapping;
import com.gzf.springmvc.annotation.GZFService;


public class GDispatcherServlet extends HttpServlet {
	
	private Properties contextConfig = new Properties(); 
	
	private List<String> classNames = new ArrayList<String>();
	
	private Map<String,Object> ioc = new HashMap<String, Object>();
	
	private Map<String, Method> handlerMapping = new HashMap<String, Method>();
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		
		System.out.println("-------------����..-----------");
		
		//1.���������ļ�
		doLoadConfig(config.getInitParameter("contextConfigLocation"));
		//2.ɨ�赽���������
		doScanner(contextConfig.getProperty("com.gzf.scaPackage"));
		//3.��ʼ��ɨ�赽����
		doInstance();
		//4.ʵ������ע��
		doAutowrited();
		//5.��ʼ��HandlerMapping
		initHandlerMapping();
		
	}
	
	private void initHandlerMapping() {
		if(ioc.isEmpty()){
			return;
		}
		for (Map.Entry<String, Object> entry : ioc.entrySet()) {
			Class<?> clazz = entry.getValue().getClass();
			if(clazz.isAnnotationPresent(GZFController.class)){
				return;
			}
			String baseUrl = "";
			if(clazz.isAnnotationPresent(GZFRequestMapping.class)){
				GZFRequestMapping requestMapping = clazz.getAnnotation(GZFRequestMapping.class);
				baseUrl = requestMapping.value();
			}
			Method [] methods = clazz.getMethods();
			for (Method method : methods) {
				GZFRequestMapping requestMapping = clazz.getAnnotation(GZFRequestMapping.class);
				String url = requestMapping.value();
				url = (baseUrl + url);
				handlerMapping.put(url, method);
			}
			
		}
	}

	private void doAutowrited() {
		if(ioc.isEmpty()){
			return;
		}
		for (Map.Entry<String, Object> entry : ioc.entrySet()) {
			Field [] fields = entry.getValue().getClass().getDeclaredFields();
			for (Field field : fields) {
				if(!field.isAnnotationPresent(GZFAutowrited.class)){
					continue;
				}
				GZFAutowrited autowrited = field.getAnnotation(GZFAutowrited.class);
				String beanName = autowrited.value().trim();
				if("".equals(beanName)){
					beanName = field.getType().getName();
				}
				
				field.setAccessible(true);
				
				try {
					field.set(entry.getValue(), ioc.get(beanName));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
				
				
			}
		}
	}

	private void doInstance() {
		try{
			
			if(classNames.isEmpty()){
				return;
			}
			for (String className : classNames) {
				Class<?> clazz = Class.forName(className);
				//��ע������ʼ��
				if(clazz.isAnnotationPresent(GZFController.class)){
					Object obj = clazz.newInstance();
					String beanName = lowerFirstCase(className);
					//��ʼ��IOC���� beanNameСд����
					ioc.put(beanName, obj);
				}else if(clazz.isAnnotationPresent(GZFService.class)){
					//2.����Ǹ��ӿ�,��ʵ���ำֵ����
					Class<?>[] interfaces = clazz.getInterfaces();
					for (Class<?> i : interfaces) {
						System.out.println("i.getName:"+i.getName());
						Object instance = clazz.newInstance();
						ioc.put(i.getName(), instance);
					}
					//1.����ָ��Bean����,���ָ����ָ������д��IOC
					GZFService service = clazz.getAnnotation(GZFService.class);
					String beanName = service.value();
					if("".equals(beanName.trim())){
						beanName = lowerFirstCase(clazz.getSimpleName());
					}
					
					
					
					
					
				}else{
					continue;
				}
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void doScanner(String scanPackage) {
		URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
		File classDir = new File(url.getFile());
		for (File file : classDir.listFiles()) {
			System.out.println("---"+file.getName());
			if(file.isDirectory()){
				doScanner(scanPackage + "." +file.getName());
			}else{
				String className = scanPackage + "." +file.getName().replace(".class", "");
				System.out.println(className);
				classNames.add(className);
			}
		}
	}

	private void doLoadConfig(String contextConfigLocation) {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
		try {
			contextConfig.load(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(null != is){
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		System.out.println("------------GET------------");
//		super.doGet(req, resp);
		this.doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.out.println("------------POST------------");
		doDispatcher(req,resp);
	}
	
	private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String url = req.getRequestURI();
		String contextPath = req.getContextPath();
		url = url.replace(contextPath, "").replaceAll("/*", "/");
		if(!handlerMapping.containsKey(url)){
			resp.getWriter().write("404 Not Found");
		}
		Method m = handlerMapping.get(url);
		System.out.println(m);
	}

	private String lowerFirstCase(String str){
		char [] chars = str.toCharArray();
		chars[0] += 32;
		return String.valueOf(chars);
	}
}
