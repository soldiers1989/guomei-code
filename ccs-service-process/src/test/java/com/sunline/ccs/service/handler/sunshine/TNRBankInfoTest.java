package com.sunline.ccs.service.handler.sunshine;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.service.entity.S10001AlterBankInfoReq;

@RunWith(SpringJUnit4ClassRunner.class)//第一句是用哪个类跑 写死的
@ContextConfiguration("/service-context.xml")//第二句话是你的容器配置文件
@Transactional//事务控制
public class TNRBankInfoTest {
	
	@Autowired
	private RCcsLoan rCcsLoan;
	
	@Autowired
	private TNRBankInfo tnrBankInfo;
	
	@Test
	public void test() throws Exception{
		S10001AlterBankInfoReq s1000 = new S10001AlterBankInfoReq();
		s1000.setBankcity("北京");
		s1000.setBankcitycode("100000");
		s1000.setBankcode("100000");
		s1000.setBankname("北京交通银行");
		s1000.setBankowner("消费");
		s1000.setBankprovince("北京");
		s1000.setBankprovincecode("100000");
		s1000.setGuarantyid("10000000");
		s1000.setNewputpaycardid("1101001002000003");
		tnrBankInfo.handler(s1000);
		
	}
	
	public static <T> void setNotNullField(Class<T> type, T t) throws Exception{
		Field[] fields = type.getDeclaredFields();
		for(Field field : fields){
			System.out.println(Modifier.toString(field.getModifiers()));
			if(Modifier.isPublic(field.getModifiers()))
				continue;
			field.setAccessible(true);
			Column column = field.getAnnotation(Column.class);
			
			try {
				if(!column.nullable()){
					Class<?> fieldType = field.getType();
					if(fieldType.equals(String.class)){
						field.set(t, UUID.randomUUID().toString().substring(0, column.length()>36?35:(column.length()-1)));
					}else if(fieldType.equals(Date.class)){
						field.set(t, new Date());
					}else if (fieldType.equals(BigDecimal.class)){
						field.set(t, BigDecimal.ZERO);
					}else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)){
						field.set(t, Integer.valueOf(0));
					}else if (fieldType.equals(Long.class) || fieldType.equals(long.class)){
						field.set(t, Long.valueOf(0));
					}else if(fieldType.isEnum()){
						field.set(t, fieldType.getEnumConstants()[0]);
					}else{
						throw new Exception();
					}
				}
			} catch (Exception e) {
				System.out.println("出错域:"+field.getName());
				throw e;
			}
			
		}
	}
	
	public static void main(String[] args) throws Exception{
		CcsLoan acct = new CcsLoan();
		
		setNotNullField(CcsLoan.class, acct);
		System.out.println(acct.getAcctNbr());
	}
	
	

}
