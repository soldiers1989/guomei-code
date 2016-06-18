package com.sunline.ccs.service.handler.sunshine;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.service.entity.S32001SubrogationReq;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/service-context.xml")
@Transactional
public class TNRSubrogationTest {
	
	@Autowired
	private TNRSubrogation tSubrogation;
	
	@Test
	public void test(){
		S32001SubrogationReq req= new S32001SubrogationReq();
		req.setBusinesssum(new BigDecimal(3200));
		req.setGuarantyid("10000000");
		tSubrogation.handler(req);		
	}

}
