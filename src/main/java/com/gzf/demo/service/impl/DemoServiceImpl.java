package com.gzf.demo.service.impl;

import com.gzf.demo.service.DemoService;
import com.gzf.springmvc.annotation.GZFService;

@GZFService
public class DemoServiceImpl implements DemoService {

	@Override
	public String sayHello(String name) {
		return "ÄãºÃ" + name;
	}

}
