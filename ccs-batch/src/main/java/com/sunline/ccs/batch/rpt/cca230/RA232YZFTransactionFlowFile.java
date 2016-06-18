package com.sunline.ccs.batch.rpt.cca230;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.batch.KeyBasedStreamReader;
import com.sunline.ccs.batch.rpt.common.RptParamFacility;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrderHst;
import com.sunline.ccs.param.def.consts.CooperationCode;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.CommandType;
import com.sunline.ppy.dictionary.enums.LoanUsage;

public class RA232YZFTransactionFlowFile  extends KeyBasedStreamReader<RA232YZFKey, RA232YZFKey> {
	private static final Logger logger = LoggerFactory.getLogger(RA232YZFTransactionFlowFile.class);
	@PersistenceContext
    private EntityManager em;
	@Autowired
	BatchStatusFacility batchStatusFacility;
	@Autowired
	private RptParamFacility codeProv;

	private QCcsOrderHst ccsOrderHst = QCcsOrderHst.ccsOrderHst;
	private QCcsOrder ccsOrder = QCcsOrder.ccsOrder;
	private QCcsAcct ccsAcct = QCcsAcct.ccsAcct;
	private QCcsCustomer qCcsCustomer = QCcsCustomer.ccsCustomer;
	
	/**
	 * @方法描述 查询出翼支付前一天的所有新生成、结清的合同
	 * @开发人员 汪成林
	 * @开发时间 2015年11月25日20:11:51
	 * @修改时间 2015年11月25日20:12:02
	 */
	@Override
	protected List<RA232YZFKey> loadKeys() {
		logger.info("--------------翼支付每日整理前一天的所有新生成、结清的合同批量任务开始:");
		//1、前置所需参数
		//声明一个结果集合
		List<RA232YZFKey> keyList = new ArrayList<RA232YZFKey>();
		//获取业务时间(即业务开始时间)
		Date batchDate = batchStatusFacility.getBatchDate();
		Date lastBatchDate = batchStatusFacility.getLastBatchDate();
		Date batchendDate = batchDate;//取时间 
	    Calendar calendar = new GregorianCalendar(); 
	    calendar.setTime(batchendDate); 
	    calendar.add(calendar.DATE,1);//把日期往后增加一天.整数往后推,负数往前移动 
	    batchendDate=calendar.getTime();//这个时间就是日期往后推一天的结果
		//获取业务时间最后时间
	    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		logger.info("--------------查询前一天的所有新生成、结清的合同批量任务当前日期:{}",simpleDateFormat.format(batchDate));
		//2、业务查询(查询出时间为当前业务时间的所有新生成、结清的提现或者还款的CcsOrderHst数据)
		List<Long> acctTupleorder = new JPAQuery(em).from(ccsOrder)
				.where(ccsOrder.loanUsage.in(LoanUsage.L,LoanUsage.N)
					.and(ccsOrder.txnType.ne(AuthTransType.Inq))
					.and(ccsOrder.optDatetime.gt(lastBatchDate))
					.and(ccsOrder.optDatetime.loe(batchDate))
					.and(ccsOrder.commandType.ne(CommandType.BDB))
					.and(ccsOrder.acqId.eq(CooperationCode.YZF_ACQ_ID)))
				.distinct()
				.list(ccsOrder.orderId);
		logger.info("--------------成功查询出前一天的所有新生成、结清的合同批量订单数据,查询到当日总共有:{}条数据需要生成文件",acctTupleorder.size());
		//订单表long循环赋值给结果map
		for (int i = 0; i < acctTupleorder.size(); i++) {
			RA232YZFKey rA232YZFKey = new RA232YZFKey();
			rA232YZFKey.setOrderId(acctTupleorder.get(i));
			rA232YZFKey.setType("O");
			keyList.add(rA232YZFKey);
		}
		//2、业务查询(查询出时间为当前业务时间的所有新生成、结清的提现或者还款的CcsOrderHst数据)
		List<Long> acctTupleorderHst = new JPAQuery(em).from(ccsOrderHst)
				.where(ccsOrderHst.loanUsage.in(LoanUsage.L,LoanUsage.N)
					.and(ccsOrderHst.txnType.ne(AuthTransType.Inq))
					.and(ccsOrderHst.optDatetime.gt(lastBatchDate))
					.and(ccsOrderHst.optDatetime.loe(batchDate))
					.and(ccsOrderHst.commandType.ne(CommandType.BDB))
					.and(ccsOrderHst.acqId.eq(CooperationCode.YZF_ACQ_ID)))
				.distinct()
				.list(ccsOrderHst.orderId);
		logger.info("--------------成功查询出前一天的所有新生成、结清的合同批量订单历史数据,查询到当日总共有:{}条数据需要生成文件",acctTupleorderHst.size());
		//订单历史表long循环赋值给结果map
		for (int i = 0; i < acctTupleorderHst.size(); i++) {
			RA232YZFKey rA232YZFKeyHst = new RA232YZFKey();
			rA232YZFKeyHst.setOrderId(acctTupleorderHst.get(i));
			rA232YZFKeyHst.setType("H");
			keyList.add(rA232YZFKeyHst);
		}
		return keyList;
	}

	@Override
	protected RA232YZFKey loadItemByKey(RA232YZFKey ra232YZFKey) {
		return ra232YZFKey;
	}

}