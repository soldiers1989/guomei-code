package com.sunline.ccs.batch.cc1500;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.LineItem;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctCrlmtAdjLog;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.param.def.AccountAttribute;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.AdjState;
import com.sunline.ccs.service.api.Constants;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.LoanStatus;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.exchange.AcctLmtAdjFile;

public class P1501AdjLimit implements ItemProcessor<LineItem<AcctLmtAdjFile>,Object>{
	@Autowired
	private RCcsAcct rCcsAcct;
	@Autowired
	private RCcsAcctO rCcsAcctO;
	@Autowired
	private RCcsLoan rCcsLoan;
	@Autowired
	private RCcsAcctCrlmtAdjLog rCcsAcctCrlmtAdjLog;
	@Autowired
	private RCcsCustomerCrlmt rTmCustLimitO;
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	@Autowired
	private BatchStatusFacility statusFacility;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public Object process(LineItem<AcctLmtAdjFile> item) throws Exception {
		logger.debug("批量调整额度开始.....");
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		QCcsAcctO qCcsAcctO = QCcsAcctO.ccsAcctO;
		QCcsCustomerCrlmt qTmCustLimitO = QCcsCustomerCrlmt.ccsCustomerCrlmt;
		QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
		CcsAcctCrlmtAdjLog adjLog = new CcsAcctCrlmtAdjLog();
		AcctLmtAdjFile adjItem=item.getLineObject();

		//公共部分
		adjLog.setOpId(Constants.OP_USER_BATCH);
		adjLog.setProcDate(new Date());
		adjLog.setCreateUser(Constants.OP_USER_BATCH);
		adjLog.setCreateTime(new Date());
		adjLog.setLstUpdTime(new Date());
		adjLog.setLstUpdUser(Constants.OP_USER_BATCH);
		adjLog.setOpTime(new Date());
		adjLog.setExpireDate(adjItem.expireDate);//有效期
		
		if(StringUtils.isEmpty(adjItem.acctNbr)||(adjItem.creditLmt==null&&adjItem.expireDate==null)||StringUtils.isEmpty(adjItem.contrNbr)
			||StringUtils.isEmpty(adjItem.name)||adjItem.acctType==null||StringUtils.isEmpty(adjItem.org)){
			logger.error("输入信息格式异常");
			if(!StringUtils.isEmpty(adjItem.acctNbr)){
				adjLog.setAcctNbr(Long.valueOf(adjItem.acctNbr));
			}
			adjLog.setOrg(adjItem.org);
			adjLog.setAdjState(AdjState.R);
			adjLog.setCreditLmtOrig(adjItem.creditLmt);
			adjLog.setAcctType(adjItem.acctType);
			adjLog.setRejReason("输入信息格式异常");
			rCcsAcctCrlmtAdjLog.save(adjLog);
			return null;
		}
		logger.debug("机构号["+adjItem.org+"]"+"账户号["+adjItem.acctNbr+"],"+"账户类型["+adjItem.acctType+"],"+"合同号["+adjItem.contrNbr+"]");
		logger.debug("姓名["+adjItem.name+"],"+"新额度["+adjItem.creditLmt+"],"+"新有效期["+adjItem.expireDate+"]");
		
		// 设置组织
		OrganizationContextHolder.setCurrentOrg(adjItem.org);
		
		try{
			CcsAcct ccsAcct = rCcsAcct.findOne(qCcsAcct.acctNbr.eq(Long.valueOf(adjItem.acctNbr)).and(qCcsAcct.acctType.eq(adjItem.acctType)).and(qCcsAcct.org.eq(adjItem.org)));
			CcsAcctO ccsAcctO = rCcsAcctO.findOne(qCcsAcctO.acctNbr.eq(Long.valueOf(adjItem.acctNbr)).and(qCcsAcctO.acctType.eq(adjItem.acctType)).and(qCcsAcctO.org.eq(adjItem.org)));
			CcsLoan ccsLoan = rCcsLoan.findOne(qCcsLoan.contrNbr.eq(adjItem.contrNbr).and(qCcsLoan.org.eq(adjItem.org)));
			//获取账户层参数
			AccountAttribute accountAttribute=null;
			if(ccsAcct!=null){
				ProductCredit pc = parameterFacility.loadParameter(ccsAcct.getProductCd(), ProductCredit.class);
				accountAttribute = parameterFacility.loadParameter(pc.accountAttributeId, AccountAttribute.class);
			}
			
			String rejectReason=this.check(ccsAcct,ccsAcctO,ccsLoan,accountAttribute,adjItem);
			
			//公共部分
			adjLog.setAcctNbr(Long.valueOf(adjItem.acctNbr));
			adjLog.setAcctType(adjItem.acctType);
			adjLog.setOrg(adjItem.org);
			adjLog.setProcDate(new Date());
			if(ccsLoan!=null) adjLog.setCardNbr(ccsLoan.getCardNbr());
			
			//调额拒绝/失败
			if(rejectReason!=null){
				adjLog.setRejReason(rejectReason);
				adjLog.setAdjState(AdjState.R);
				if(ccsAcct!=null){
					adjLog.setCreditLmtOrig(ccsAcct.getCreditLmt());
				}
				rCcsAcctCrlmtAdjLog.save(adjLog);
				return null;
			}
			
			adjLog.setCreditLmtOrig(ccsAcct.getCreditLmt());
			
			//操作==调额
			if(adjItem.creditLmt!=null){
				ccsAcct.setCreditLmt(adjItem.creditLmt);
				ccsAcctO.setCreditLmt(adjItem.creditLmt);
				//获取客户级额度
				CcsCustomerCrlmt tmCustLimitO = rTmCustLimitO.findOne(qTmCustLimitO.custLmtId.eq(ccsAcct.getCustLmtId()).and(qTmCustLimitO.org.eq(adjItem.org)));
				//获取默认超限比例
				BigDecimal vorlmRate = accountAttribute.ovrlmtRate;
				//如果账户级额度大于客户级额度，则提升客户级额度
				BigDecimal creditLimit = adjItem.creditLmt;
				//客户级额度 = 账户额度 * (1+账户层默认超限比例)
				creditLimit = creditLimit.add(creditLimit.multiply(vorlmRate)).setScale(0, BigDecimal.ROUND_HALF_UP);
				if (creditLimit.compareTo(tmCustLimitO.getCreditLmt()) > 0){
				    tmCustLimitO.setCreditLmt(creditLimit);
				}
				adjLog.setCreditLmtNew(ccsAcct.getCreditLmt());
				ccsAcct.setLastLimitAdjDate(statusFacility.getBatchDate());
			}
			
			//操作==调有效期
			/**
			 * @调额调整有效期规则
			 * @Lisy
			 * @20160421
			 * 
			 * 调整剩余期数规则：1.只调整随借随还账户；
			 * 					2.调整幅度根据两个有效期之间的账单日个数
			 * 					3.延长有效期，计前不计后；缩短有效期，计后不计前
			 * 					4.当前有效期不为账单日(已经过调整)的同样适用
			 * 例如，开户日期20160421，共12期，当前日期20161020（当前5期），当前有效期为20170421
			 * 	新有效期		|调整幅度(月份)		|新剩余期数
			 * 	20170721		3					10				
			 * 	20170720		3					10
			 * 	20170722		4					11
			 * 	
			 * 例如,新有效期为20170220的调整，到20170121时,剩余期数为1,发生最后一期信用计划转移,贷款终止
			 * 
			 */
			if(adjItem.expireDate!=null){
				ccsAcct.setAcctExpireDate(adjItem.expireDate);
				// 同一天不调整
				if(ccsLoan.getLoanExpireDate().compareTo(adjItem.expireDate)!=0){
					if(ccsLoan!=null){
						// 仅随借随还贷款修改剩余期数
						if(ccsLoan.getLoanType()==LoanType.MCAT){
							int cycleDay = Integer.parseInt(ccsAcct.getCycleDay());
							Calendar c = GregorianCalendar.getInstance();
							int count = 0;
							Date dateTmp = ccsLoan.getLoanExpireDate();
							logger.debug("当前有效期["+ccsLoan.getLoanExpireDate()+"],新有效期["+adjItem.expireDate+"]");
							logger.debug("比较结果["+(ccsLoan.getLoanExpireDate().compareTo(adjItem.expireDate)<=0)+"]");
							if(ccsLoan.getLoanExpireDate().compareTo(adjItem.expireDate)<=0){
								// 有效期延长，计前(当前有效期)不计后(新有效期)
//								logger.debug("期数延长///");
								for(count = 0;dateTmp.compareTo(adjItem.expireDate)<0;){
									c.setTime(dateTmp);
									if(c.get(Calendar.DAY_OF_MONTH)==cycleDay){
										//跨账单日个数
										count++;
//										logger.debug("当前计数：["+count+"]");
									}
//									logger.debug("当前日期：["+dateTmp+"]");
									dateTmp = DateUtils.addDays(dateTmp, 1);
								}
							}else{
								// 有效期缩短，计后(新有效期)不计前(当前有效期)
								// 防止假设账单日为25,由4.25->3.25,count=+2,但实际应+1
//								logger.debug("期数缩短///");
								dateTmp = DateUtils.addDays(dateTmp, -1);
								for(count = 0;dateTmp.compareTo(adjItem.expireDate)>=0;){
									c.setTime(dateTmp);
									if(c.get(Calendar.DAY_OF_MONTH)==cycleDay){
										//跨账单日个数
										count--;
//										logger.debug("当前计数：["+count+"]");
									}
//									logger.debug("当前日期：["+dateTmp+"]");
									dateTmp = DateUtils.addDays(dateTmp, -1);
								}
							}
							logger.debug("当前剩余期数：["+ccsLoan.getRemainTerm()+"]期数修正：["+count+"]");
							int newRemainTerm = ccsLoan.getRemainTerm() + count;
							if (newRemainTerm > 0) {
								ccsLoan.setRemainTerm(newRemainTerm);
							} else {
								// 防止可能产生的无法结清
								ccsLoan.setRemainTerm(0);
							}
							ccsLoan.setLoanExpireDate(adjItem.expireDate);
						}
					}
				}
			}
			
			adjLog.setAdjState(AdjState.A);
			rCcsAcctCrlmtAdjLog.saveAndFlush(adjLog);
			logger.debug("调额成功");
		}catch(Exception e){
			logger.error("账户/客户调额异常"+"账户号["+adjItem.acctNbr+"],"+"合同号["+adjItem.contrNbr+"]");
			logger.error("异常信息："+e.getMessage());
			return null;
		}
		return null;
	}

	private String check(CcsAcct acct, CcsAcctO acctO, CcsLoan loan,AccountAttribute accountAttribute, AcctLmtAdjFile item) {
		if(acct==null||acctO==null||acct.getAcctExpireDate().before(statusFacility.getBatchDate()))
			return "无效合同";
		if(loan!=null){
			if(LoanStatus.F.equals(loan.getLoanStatus())||LoanStatus.T.equals(loan.getLoanStatus()))
				return "合同已经终止或完成";
		}
		if(acct.getBlockCode()!=null){
			if(acct.getBlockCode().contains("T"))
				return "冻结账户";
		}
		if(accountAttribute==null){
			return "未获取到对应账户参数";
		}
		if(!acct.getContrNbr().equals(item.contrNbr)){
			return "合同号-账户号不匹配";
		}
		//调额
		if(item.creditLmt!=null){
			if(item.creditLmt.compareTo(BigDecimal.ZERO)<0)
				return "调整额度不应小于0";
			//额度的小数部分不为0
			if(item.creditLmt.subtract(new BigDecimal(item.creditLmt.intValue())).compareTo(BigDecimal.ZERO)!=0){
				return "调整额度包含小数";
			}else{
				item.creditLmt = new BigDecimal(item.creditLmt.intValue());
			}
			if(item.creditLmt.compareTo(acct.getCreditLmt())==0)
				return "调整额度与当前额度一致";
			//如果取不到上次调额日期，则使用开户日期
			Date lastLimitAdjDate=acct.getLastLimitAdjDate()==null?acct.getSetupDate():acct.getLastLimitAdjDate();
			if(DateUtils.getIntervalDays(lastLimitAdjDate,statusFacility.getBatchDate())<accountAttribute.creditLimitAdjustInterval.intValue()){
				return "上次额度调整日期"+DateUtils.formatDate2String(lastLimitAdjDate,"yyyy-MM-dd")+",至少需在"+accountAttribute.creditLimitAdjustInterval.intValue()+"天后才能再次调额";
			}
		}
		//调有效期
		if(item.expireDate!=null){
			if(!item.expireDate.after(statusFacility.getBatchDate()))
				return "无效的调整日期,当前日期["+statusFacility.getBatchDate()+"]";
		}
		
		return null;
	}
	
	public static void main(String args[]) throws ParseException{
		
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
		//当前有效期
		Date date1 = sf.parse("20180228");
		//新有效期
		Date date2 = sf.parse("20180128");
		int cycleDay = 25;
		Calendar c = GregorianCalendar.getInstance();
		int count = 0;
		Date dateTmp = date1;
		try{
		if(date1.compareTo(date2)<=0){
			// 有效期延长，
			for(count = 0;dateTmp.compareTo(date2)<0;){
				c.setTime(dateTmp);
				if(c.get(Calendar.DAY_OF_MONTH)==cycleDay){
					//跨账单日个数
					count++;
				}
				dateTmp = DateUtils.addDays(dateTmp, 1);
				System.out.println("当前日期：[" + dateTmp + "]");
			}
		}else{
			// 有效期缩短，计后(新有效期)不计前(当前有效期)
			// 防止假设账单日为25,由4.25->3.25,count=+2,但实际应+1
			dateTmp = DateUtils.addDays(dateTmp, -1);
			for(count = 0;dateTmp.compareTo(date2)>=0;){
				c.setTime(dateTmp);
				if(c.get(Calendar.DAY_OF_MONTH)==cycleDay){
					//跨账单日个数
					count--;
				}
				dateTmp = DateUtils.addDays(dateTmp, -1);
				System.out.println("当前日期：[" + dateTmp + "]");
			}
		}
		System.out.println("调整幅度：[" + count + "]");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
