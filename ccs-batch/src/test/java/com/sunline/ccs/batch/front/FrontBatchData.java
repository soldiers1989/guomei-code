package com.sunline.ccs.batch.front;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.SequenceGenerator;

import org.apache.commons.lang.time.DateUtils;

import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsOrder;
import com.sunline.ccs.infrastructure.shared.model.CcsOrderHst;
import com.sunline.ccs.infrastructure.shared.model.CcsOutsideDdTxn;
import com.sunline.ccs.infrastructure.shared.model.CcsRepayHst;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanUsage;
import com.sunline.ppy.dictionary.enums.OrderStatus;

public class FrontBatchData {
	private static final String org = "000000000001";
	public static CcsAcct genAcct() throws Exception {
		CcsAcct acct = new CcsAcct();
		FrontBatchData.setNotNullField(CcsAcct.class, acct);
		acct.setOrg(org);
		acct.setAcctNbr(1111l);
		acct.setAcctType(AccountType.A);
		acct.setProductCd("123");
		acct.setCurrBal(BigDecimal.valueOf(2000));
		acct.setCustId(1L);
		acct.setContrNbr("11111");
		return acct;
	}

	public static CcsRepaySchedule genSchedule() {
		CcsRepaySchedule schedule = new CcsRepaySchedule();
		
		schedule.setLoanTermPrin(BigDecimal.valueOf(1000));//本金
		schedule.setLoanTermInt(BigDecimal.valueOf(100));//利息
		schedule.setLoanTermFee(BigDecimal.valueOf(10));//手续费
		schedule.setLoanStampdutyAmt(BigDecimal.valueOf(1));//印花税
		schedule.setLoanInsuranceAmt(BigDecimal.valueOf(150));//保费
		schedule.setLoanLifeInsuAmt(BigDecimal.valueOf(0));
		
		// 试算
		schedule.setLoanId(1l);
		schedule.setCurrTerm(1);
		schedule.setLoanPmtDueDate(new Date());
		return schedule;
	}
	
	public static CcsLoan genLoan() throws Exception {
		CcsLoan loan = new CcsLoan();
		FrontBatchData.setNotNullField(CcsLoan.class, loan);

		loan.setLoanStatus(LoanStatus.A);
		
//		loan.setLoanId(1l); // persist异常
		loan.setCurrTerm(1);
		loan.setInterestRate(BigDecimal.ZERO);//利息率
		loan.setFloatRate(BigDecimal.ZERO);
		loan.setInsuranceRate(BigDecimal.ZERO);//保费率
		loan.setUnstmtStampdutyAmt(BigDecimal.ZERO);//印花税
		loan.setUnstmtLifeInsuAmt(BigDecimal.ZERO);//寿险计划费
		loan.setUnstmtPrin(BigDecimal.valueOf(1000));//本金
		
		loan.setLoanCode("123");
		loan.setAcctNbr(1111l);
		loan.setAcctType(AccountType.A);
		loan.setContrNbr("11111");
		return loan;
	}
	
	// 逾期1个月
	public static CcsLoan genCcsLoan1(){
		CcsLoan loan = new CcsLoan();
		
		loan.setLoanStatus(LoanStatus.A);
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, -1);
		loan.setOverdueDate(c.getTime());
		
		return loan;
	}
	
	// 逾期3个月
	public static CcsLoan genCcsLoan2(){
		CcsLoan loan = new CcsLoan();
		
		loan.setLoanStatus(LoanStatus.A);
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, -3);
		loan.setOverdueDate(c.getTime());
		
		return loan;
	}
	
	/**
	 * 设置表对象非空域, 只适用Nova
	 * 仅方便设置无用域
	 * @param type
	 * @param t
	 * @throws Exception
	 */
	public static <T> void setNotNullField(Class<T> type, T t) throws Exception{
		Field[] fields = type.getDeclaredFields();
		for(Field field : fields){
			field.setAccessible(true);
			if(Modifier.isPublic(field.getModifiers()))
				continue;
			if(field.get(t)!=null)
				continue;
			if(field.getAnnotation(SequenceGenerator.class)!=null)
				continue;
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
	
	public static CcsOrder genSubrogationOrder(){
		CcsOrder order = new CcsOrder();
		
		order.setBusinessDate(new Date());
		order.setLoanUsage(LoanUsage.O);
		order.setOrderStatus(OrderStatus.E);
		order.setTxnAmt(BigDecimal.valueOf(1000));
		
		return order;
	}
	
	private static int userId = 0;
	
	public static CcsOrder genOrder(){
		CcsOrder order = new CcsOrder();
		
		order.setBusinessDate(new Date());
		order.setOrderStatus(null);
		Random random = new Random();
		order.setTxnAmt(BigDecimal.valueOf(random.nextDouble()*1000).setScale(2, BigDecimal.ROUND_UP));
		
		order.setChannelId(InputSource.SUNS);
		order.setOpenBankId("0102");
		order.setCardType("0");
		order.setCardNo("8888888888888888");
		order.setUsrName("测试"+String.format("%04d", ++userId));
		order.setCertType("01");
		order.setCertId("36220220000101"+String.format("%04d", userId));
		order.setPurpose("测试扣款");
		order.setPriv1("T110E5");
		
		return order;
	}
	
	/**
	 * 结算保费
	 * @return
	 */
	public static CcsRepayHst genCcsRepayHst1(){
		CcsRepayHst repayHst = new CcsRepayHst();
		
		repayHst.setAcqId("123456");
		repayHst.setBnpType(BucketObject.ctdIns);
		repayHst.setBatchDate(new Date());
		repayHst.setRepayAmt(BigDecimal.valueOf(100));
		
		return repayHst;
	}
	
	/**
	 * 结算提前结清手续费
	 * @return
	 */
	public static CcsRepayHst genCcsRepayHst2(){
		CcsRepayHst repayHst = new CcsRepayHst();
		
		repayHst.setAcqId("123456");
		repayHst.setBnpType(BucketObject.ctdTxnFee);
		repayHst.setBatchDate(new Date());
		repayHst.setRepayAmt(BigDecimal.valueOf(10));
		
		return repayHst;
	}
	
	/**
	 * 结算追偿费
	 * @return
	 */
	public static CcsOrderHst genCcsOrderHst(){
		CcsOrderHst orderHst = new CcsOrderHst();
		
		orderHst.setOrderId(1l);
		
		orderHst.setAcqId("123456");
		orderHst.setLoanUsage(LoanUsage.S);
		orderHst.setOrderStatus(OrderStatus.S);
		orderHst.setOptDatetime(new Date());
		orderHst.setTxnAmt(BigDecimal.ONE);
		
		return orderHst;
	}

	public static void main(String[] args) throws Exception{
		CcsAcct acct = new CcsAcct();
		acct.setAcctNbr(111l);
		setNotNullField(CcsAcct.class, acct);
		
		System.out.println(acct.getAcctNbr());
		
		System.out.println(DateUtils.truncate(new Date(1441349943531l), Calendar.DATE));
	}

	public static CcsCustomer genCustomer() throws Exception {
		CcsCustomer cust = new CcsCustomer();
		FrontBatchData.setNotNullField(CcsCustomer.class, cust);
		cust.setCustId(1L);
		return cust;
	}

	public static CcsOutsideDdTxn genCcsOutsideDdTxn() throws Exception {
		CcsOutsideDdTxn ccsOutsideDdTxn = new CcsOutsideDdTxn();
		FrontBatchData.setNotNullField(CcsOutsideDdTxn.class, ccsOutsideDdTxn);

		ccsOutsideDdTxn.setAcctNbr(11111L);
		ccsOutsideDdTxn.setAcctType(AccountType.E);
		ccsOutsideDdTxn.setOrg(org);
		ccsOutsideDdTxn.setContrNbr("11111");
		ccsOutsideDdTxn.setTxnAmt(new BigDecimal("12345.67"));
		return ccsOutsideDdTxn;
	}
	
}