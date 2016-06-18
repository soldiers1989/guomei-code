package com.sunline.ccs.batch.sdk;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ark.support.def.HasMapping;

public abstract class AbstractPrepareData {

//	private Logger logger = LoggerFactory.getLogger(DataPrepare.class);

	@Autowired
	private REntityUtil entityUtil;
 
	public abstract void prepareData() throws Exception;
	
	public void save(List<HasMapping> entityList) throws Exception {
		entityUtil.save(entityList);
	}
	
	public <T> T saveEntity(T t) throws Exception {
		return entityUtil.saveEntity(t);
	}
	
	public Date getBatchDate(){
		return entityUtil.getBatchDate();
	}
	
}
