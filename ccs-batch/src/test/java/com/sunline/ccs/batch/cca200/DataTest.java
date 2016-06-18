package com.sunline.ccs.batch.cca200;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.sunline.ccs.batch.cc1300.P1351LoadLhpRepayment;
import com.sunline.ccs.batch.tools.FileItemTest;
import com.sunline.ccs.batch.tools.MakeData;
//import com.sunline.ppy.dictionary.exchange.LhpOuterRepaymentInterfaceItem;

public class DataTest {
	private Logger logger  = LoggerFactory.getLogger(this.getClass());
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	@Test
	public void dateAddTest(){
		Calendar cld = Calendar.getInstance();
		cld.set(2015, 1, 28);
		System.out.println(sdf.format(cld.getTime()));
		System.out.println(sdf.format(DateUtils.addMonths(cld.getTime(), 1)));
		System.out.println("-----------------------");
		cld.set(2015, 2, 31);
		System.out.println(sdf.format(cld.getTime()));
		System.out.println(sdf.format(DateUtils.addMonths(cld.getTime(), 1)));
		System.out.println("-----------------------");
		cld.set(2015, 3, 30);
		System.out.println(sdf.format(cld.getTime()));
		System.out.println(sdf.format(DateUtils.addMonths(cld.getTime(), 1)));
		
	}
	
	@Test
	public void calendarAddTest() {
		try {
			Calendar cldEndDateMCEI = Calendar.getInstance();
			cldEndDateMCEI.setTime(sdf.parse("20150131"));
			cldEndDateMCEI.add(Calendar.MONTH, +1);
			System.out.println(sdf.format(cldEndDateMCEI.getTime()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void uuidGen(){
		UUID uuid = UUID.randomUUID();
		System.out.println("uuid：[" + uuid.toString().replace("-", "") + "]");
	}
	
	@Test
	public void getSetTest(){
		TestClass t = new TestClass();
		t.setNum(new BigDecimal("100.111111111"));
		System.out.println(t.getNum().toPlainString());
		BigDecimal b = t.getNum();
		b = b.setScale(4, RoundingMode.HALF_UP);
		
		System.out.println(b.toPlainString());
		System.out.println(t.getNum().toPlainString());
		System.out.println(t.getNum() == b);
	}
	
	@Test
	public void genRandomFileLine() throws IllegalAccessException{
//		List<LhpOuterRepaymentInterfaceItem> list = new ArrayList<LhpOuterRepaymentInterfaceItem>();
//		LhpOuterRepaymentInterfaceItem i1 = new LhpOuterRepaymentInterfaceItem();
//		LhpOuterRepaymentInterfaceItem i2 = new LhpOuterRepaymentInterfaceItem();
//		LhpOuterRepaymentInterfaceItem i3 = new LhpOuterRepaymentInterfaceItem();
//		LhpOuterRepaymentInterfaceItem i4 = new LhpOuterRepaymentInterfaceItem();
//		MakeData.setDefaultValue(i1);
//		MakeData.setDefaultValue(i2);
//		MakeData.setDefaultValue(i3);
//		MakeData.setDefaultValue(i4);
//		list.add(i1);
//		list.add(i2);
//		list.add(i3);
//		list.add(i4);
//		FileItemTest.printLine(list, LhpOuterRepaymentInterfaceItem.class);
//		
//		P1351LoadLhpRepayment p = new P1351LoadLhpRepayment();
		
	}
	
	class TestClass{
		private BigDecimal num;
		
		public BigDecimal getNum() {
			return num;
		}
		public void setNum(BigDecimal num) {
			this.num = num;
		}
		
	}

}
