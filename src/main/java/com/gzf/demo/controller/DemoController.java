package com.gzf.demo.controller;

import com.gzf.demo.service.DemoService;
import com.gzf.springmvc.annotation.GZFAutowrited;
import com.gzf.springmvc.annotation.GZFController;
import com.gzf.springmvc.annotation.GZFRequestMapping;

@GZFController
@GZFRequestMapping("/demo")
public class DemoController {
	
	@GZFAutowrited
	private DemoService demoService;
	
	@GZFRequestMapping("/hello")
	public void hello(String name){
		System.out.println(demoService.sayHello(name));
	}
}
