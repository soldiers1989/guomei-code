package com.sunline.ccs.service.process.test.util; 

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;

/** 
* @author fanghj
* @version 创建时间：2012-7-26 下午2:30:51 
* 测试类数据初始化
*/ 

public class DataInitializer implements InitializingBean{
	
	private DataSource dataSource;

	private String dataFile;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		DataFixtures.reloadData(dataSource, dataFile);
	}


}
 
