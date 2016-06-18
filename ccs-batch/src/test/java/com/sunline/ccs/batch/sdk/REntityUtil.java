package com.sunline.ccs.batch.sdk;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.sunline.ark.support.def.HasMapping;

@Service
public class REntityUtil {

//	private Logger logger = LoggerFactory.getLogger(DataPrepare.class);

	@Autowired
	private BatchDateUtil batchDateUtil;
	@Autowired
	private ContextUtil context;
 
	public void save(List<HasMapping> entityList) throws Exception {
		for(HasMapping e : entityList ){
			saveEntity(e);
		}
	}
	
	public <T> T saveEntity(T t) throws Exception {
		
		@SuppressWarnings("unchecked")
		JpaRepository<T, Serializable> jpa = (JpaRepository<T, Serializable>) context.getBean("R" + t.getClass().getSimpleName());
		return jpa.save(t);
	}
	
	public Date getBatchDate(){
		return batchDateUtil.getBatchDate();
	}
	
}
