package com.gzf.demo.service;

import com.gzf.springmvc.annotation.GZFService;

@GZFService
public interface DemoService {
	String sayHello(String name);
}
