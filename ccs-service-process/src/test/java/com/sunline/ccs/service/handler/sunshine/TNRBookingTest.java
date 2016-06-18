package com.sunline.ccs.service.handler.sunshine;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.service.entity.S11001BookingReq;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/service-context.xml")
@Transactional
public class TNRBookingTest {
	
	@Autowired
	private TNRBooking tnrBooking;

	@Test
	public void test(){
		
		S11001BookingReq req = new S11001BookingReq();
		req.setGuarantyid("10000000");
		req.setType("1");
		req.setCaldate(new Date());
		tnrBooking.handler(req);
	}
}
