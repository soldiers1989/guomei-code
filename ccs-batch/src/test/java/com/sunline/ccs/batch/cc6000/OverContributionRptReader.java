package com.sunline.ccs.batch.cc6000;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

import javassist.Modifier;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.sunline.ark.support.cstruct.CChar;
import com.sunline.ppy.dictionary.report.ccs.OverContributionRptItem;

public class OverContributionRptReader implements ItemReader<OverContributionRptItem> {
	private static Logger logger = LoggerFactory.getLogger(OverContributionRptReader.class);
	private int i = 1;
	@Override
	public OverContributionRptItem read() throws Exception,
			UnexpectedInputException, ParseException,
			NonTransientResourceException {
		OverContributionRptItem item = new OverContributionRptItem();
		setRandomFieldValue(item);
		logger.info("文件第[{}]行", i++);
		if(i>=10){
			return null;
		}
		return item;
	}
	public static <T> void setRandomFieldValue(T t){
		Field[] fields = t.getClass().getDeclaredFields();
		for(Field field : fields){
			field.setAccessible(true);
			Class<?> ftype = field.getType();
			CChar check = field.getAnnotation(CChar.class);
			Random r = new Random();
			
			try {
				if(ftype.equals(Date.class)){
					field.set(t, new Date());
				}else if(ftype.equals(String.class)){
					field.set(t, RandomStringUtils.random((int)check.value(), "ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
				}else if(ftype.equals(Integer.class)){
					field.set(t, 0);
				}else if(ftype.equals(BigDecimal.class)){
					field.set(t, BigDecimal.ZERO.setScale(2));
				}else if(ftype.equals(Long.class)){
					field.set(t, 0L);
				}else if(ftype.isEnum() || Modifier.isEnum(ftype.getModifiers()) ){
					field.set(t, ftype.getEnumConstants()[0]);
				}else{
					logger.warn("Type Not Handled[{}]", ftype.getCanonicalName());
				}
			} catch (Exception e) {
				logger.warn("字段[{}]字段类型[{}]无法赋值", field.getName(), field.getType());
				e.printStackTrace();
				continue;
			}
		}
	}
}
