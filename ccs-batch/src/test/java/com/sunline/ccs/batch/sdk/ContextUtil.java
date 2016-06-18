package com.sunline.ccs.batch.sdk;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class ContextUtil implements ApplicationContextAware {

	private static ApplicationContext context;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		context = applicationContext;
		
	}
	
	public ApplicationContext getApplicationContext(){
		return context;
	}
	
	public Object getBean(String name){
		return context.getBean(name);
	}
	
	
}
