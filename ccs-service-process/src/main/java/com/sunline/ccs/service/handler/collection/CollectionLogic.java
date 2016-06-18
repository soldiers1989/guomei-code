package com.sunline.ccs.service.handler.collection;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.facility.DateUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ppy.dictionary.enums.MsPayfrontError;
/**
 * 催收扣款通知逻辑
 * @author zhengjf
 *
 */
@Service
public class CollectionLogic {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	GlobalManagementService globalManagementService;
	
	@PersistenceContext
	private EntityManager em;
	@Autowired
	MsCSNoticeFacility mscsNoticeFacility;
	

	
	public void sendCSPlatform(TxnContext context) {

		//如果交易为代扣，并成功
		if (MsPayfrontError.S_0.getRespCode().equals(context.getTxnInfo().getResponsCode())){
			CollectionReq req=new CollectionReq();
			//发送到催收平台
			try{
				req=getCollectionReq(context);
				mscsNoticeFacility.sendCSNotice(req);
			}catch(Exception e){
				//只打印日志，再往前抛异常，保证交易正常结束
				logger.error("组装发送催收平台报文异常",e);
			}
		}
		
	}
	/**
	 * 算出合同总罚金
	 * @param context
	 * @return
	 */
	private BigDecimal getBigDecimal(TxnContext context) {
		QCcsPlan qCcsPlan = QCcsPlan.ccsPlan;
		BigDecimal amt = BigDecimal.ZERO;
		
		List<CcsPlan> ccsPlanList=new JPAQuery(em).from(qCcsPlan).where(
				qCcsPlan.acctNbr.eq(context.getAccount().getAcctNbr()).and(qCcsPlan.acctType.eq(context.getAccount().getAcctType()))).list(qCcsPlan);
		if (ccsPlanList!=null) {
			//循环plan算出罚金
			for (CcsPlan ccsPlan : ccsPlanList) {
				//当期罚金加已出账单罚金
				amt=ccsPlan.getCtdMulctAmt().add(ccsPlan.getPastMulctAmt()).add(amt);
			}
		}
		return amt;
	}
	/**
	 * 组装报文
	 * @param context
	 * @return
	 */
	private CollectionReq getCollectionReq(TxnContext context) {
		CollectionReq req=new CollectionReq();
		int cpd;
		BigDecimal overdueTotalAmount=BigDecimal.ZERO;
		BigDecimal totalRemainingAmount=BigDecimal.ZERO;
		//当前业务日期
		Date bizDate = globalManagementService.getSystemStatus().getBusinessDate();
		//逾期天数,如果cpd起始日期weinull 则返回0
		if (context.getLoan()==null||context.getLoan().getCpdBeginDate()==null) {
			cpd=0;
		}else {
			cpd=DateUtils.getIntervalDays(context.getLoan().getCpdBeginDate(),bizDate);
		}
		
		req.setContr_nbr(context.getAccount().getContrNbr());
		req.setUnique_id(context.getCustomer().getInternalCustomerId());
		req.setOverdue_preiod(cpd);
		//合同全部应还款额
		overdueTotalAmount = context.getAccount().getTotDueAmt().
				add(context.getAccounto().getMemoDb()).subtract(context.getAccounto().getMemoCr());
		//欠款总额
		totalRemainingAmount = context.getAccount().getCurrBal().
				add(context.getAccounto().getMemoDb()).subtract(context.getAccounto().getMemoCr());
		//预防钱数为负
		if (overdueTotalAmount.compareTo(BigDecimal.ZERO)<0) {
			overdueTotalAmount = BigDecimal.ZERO;
		}
		if (totalRemainingAmount.compareTo(BigDecimal.ZERO)<0) {
			totalRemainingAmount = BigDecimal.ZERO;
		}
		
		req.setOverdueTotalAmount(overdueTotalAmount);
		//BigDecimal.valueOf(25.65));
		req.setTotalRemainingAmount(totalRemainingAmount);
		//BigDecimal.valueOf(15.65));
		req.setPenalty(getBigDecimal(context));
		//BigDecimal.valueOf(5.65));
		return req;
	}
}
