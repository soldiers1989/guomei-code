package com.sunline.ccs.batch.cc9000;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.report.ccs.T9001To1104G17RptItem;
/**
 * @see 类名：R9001Rpt1104G17
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期：   2015-6-24下午2:33:52
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class R9001Rpt1104G17 implements ItemReader<T9001To1104G17RptItem> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private BatchStatusFacility batchFacility;
	
	private Iterator<T9001To1104G17RptItem> data;

	@Override
	public T9001To1104G17RptItem read() throws Exception {
		if (data == null) {
			data = this.getMap().values().iterator();
			logger.debug(String.valueOf(data.hasNext()));
		}

		logger.debug(String.valueOf(data.hasNext()));
		if (!data.hasNext()) return null;
		
		return data.next();
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private Map<String, T9001To1104G17RptItem> getMap() throws Exception {
		logger.debug("生成1104报表数据");
		
		Map<String, T9001To1104G17RptItem> g17Map = new HashMap<String, T9001To1104G17RptItem>();
		
		Date batchDate = batchFacility.getBatchDate();
		Date lastDayOfQuarter = DateUtils.getLastDayOfQuarter(batchDate);
		
		// 非季度末则不执行此报表
		if (!DateUtils.truncatedEquals(batchDate, lastDayOfQuarter, Calendar.DATE)) return g17Map;
		
		Date firstDayOfQuarter = DateUtils.getFirstDayOfQuarter(batchDate);
		Date firstDayOfYear = DateUtils.getFirstDayOfYear(batchDate);


		// ----------------------------------------1.发卡指标----------------------------------------
		this.set1(g17Map, lastDayOfQuarter);
		
		// ----------------------------------------2.交易指标----------------------------------------
		this.set2(g17Map, firstDayOfQuarter, lastDayOfQuarter);
		
		// ----------------------------------------3.资金状况----------------------------------------
		this.set3(g17Map);
		
		// ----------------------------------------4.本年累计收入----------------------------------------
		this.set4(g17Map, firstDayOfYear, lastDayOfQuarter);
		
		// ----------------------------------------5.损失准备----------------------------------------
		this.set5(g17Map);
		
		// ----------------------------------------6.逾期状况----------------------------------------
		this.set6(g17Map);
		
		// ----------------------------------------7.业务受理环境建设情况----------------------------------------
		this.set7(g17Map);

		return g17Map;
	}

	/** 1.发卡指标
	 * @param query
	 * @param g17Map
	 */
	private void set1(Map<String, T9001To1104G17RptItem> g17Map, Date lastDayOfQuarter) {
		QCcsCard qTmCard = QCcsCard.ccsCard;
		// 1.1总卡量（张）,剔除销卡和过期卡
		// 1.1.1其中： 睡眠卡（张）
		List<Tuple> tmCardList = new JPAQuery(em).from(qTmCard)
				.where(qTmCard.cardExpireDate.after(lastDayOfQuarter).and(qTmCard.blockCode.notLike("%C%").or(qTmCard.blockCode.isNull())))
				.groupBy(qTmCard.org).list(qTmCard.org, qTmCard.logicCardNbr.count());
		logger.debug("set1：" + tmCardList.size());
		for (Tuple o : tmCardList) {
			String org = o.get(qTmCard.org);
			Integer value = o.get(qTmCard.logicCardNbr.count()).intValue();
			
			logger.debug("cell_02：" + org + "cell_02：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_02 = value;
			g17Map.get(org).cell_03 = Integer.valueOf(0);
		}
		
		QCcsAcct qTmAccount = QCcsAcct.ccsAcct;
		QCcsCustomer qTmCustomer = QCcsCustomer.ccsCustomer;
		// 1.2总户数（户）
		// 1.2.1其中：睡眠户（户）
		List<Tuple> tmAccountList = new JPAQuery(em).from(qTmAccount, qTmCustomer)
				.where(qTmAccount.custId.eq(qTmCustomer.custId)
						.and(qTmCustomer.custLmtId.isNotNull())
						.and((qTmAccount.blockCode.notLike("%C%").and(qTmAccount.blockCode.notLike("%P%"))).or(qTmAccount.blockCode.isNull())))
				.groupBy(qTmAccount.org).list(qTmAccount.org, qTmAccount.acctNbr.count());
		for (Tuple o : tmAccountList) {
			String org = o.get(qTmAccount.org);
			Integer value = o.get(qTmAccount.acctNbr.count()).intValue();

			logger.debug("cell_04：" + org + "cell_04：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_04 = value;
			g17Map.get(org).cell_05 = Integer.valueOf(0);
		}
	}
	
	private void set2(Map<String, T9001To1104G17RptItem> g17Map, Date firstDayOfQuarter, Date lastDayOfQuarter) {
		List<String> txnCodeList;
		List<Tuple> tmTxnHstList;
		QCcsTxnHst qTmTxnHst = QCcsTxnHst.ccsTxnHst;
		// 2.1本期消费金额（万元）
		txnCodeList = new ArrayList<String>();
		txnCodeList.add("T801"); // 本行消费  
		txnCodeList.add("T803"); // 银联跨行消费
		txnCodeList.add("T805"); // 银联境外消费 
		txnCodeList.add("T807"); // 手工压单消费
		txnCodeList.add("T821"); // 本行预授权完成
		txnCodeList.add("T823"); // 跨行预授权完成
		txnCodeList.add("T825"); // 银联境外预授权完成
		txnCodeList.add("T833"); // 第三方消费
		tmTxnHstList = new JPAQuery(em).from(qTmTxnHst)
				.where(qTmTxnHst.txnCode.in(txnCodeList).and(qTmTxnHst.postDate.between(firstDayOfQuarter, lastDayOfQuarter)))
				.groupBy(qTmTxnHst.org).list(qTmTxnHst.org, qTmTxnHst.txnAmt.sum());
		for (Tuple o : tmTxnHstList) {
			String org = o.get(qTmTxnHst.org);
			BigDecimal value = o.get(qTmTxnHst.txnAmt.sum());

			logger.debug("cell_07：" + org + "cell_07：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_07 = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		// 2.2本期取现金额（万元）
		txnCodeList = new ArrayList<String>();
		txnCodeList.add("T811"); // 本行 ATM 取现
		txnCodeList.add("T813"); // 跨行 ATM 取现
		txnCodeList.add("T815"); // 本行柜台取现 
		txnCodeList.add("T817"); // 跨行柜台取现
		txnCodeList.add("T819"); // 银联境外取现
		tmTxnHstList = new JPAQuery(em).from(qTmTxnHst)
				.where(qTmTxnHst.txnCode.in(txnCodeList).and(qTmTxnHst.postDate.between(firstDayOfQuarter, lastDayOfQuarter)))
				.groupBy(qTmTxnHst.org).list(qTmTxnHst.org, qTmTxnHst.txnAmt.sum());
		for (Tuple o : tmTxnHstList) {
			String org = o.get(qTmTxnHst.org);
			BigDecimal value = o.get(qTmTxnHst.txnAmt.sum());

			logger.debug("cell_08：" + org + "cell_08：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_08 = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		// 2.3本期转账金额（万元）
		txnCodeList = new ArrayList<String>();
		txnCodeList.add("T831"); // 本行ATM转出
		txnCodeList.add("T835"); // 本行柜台转出
		tmTxnHstList = new JPAQuery(em).from(qTmTxnHst)
				.where(qTmTxnHst.txnCode.in(txnCodeList).and(qTmTxnHst.postDate.between(firstDayOfQuarter, lastDayOfQuarter)))
				.groupBy(qTmTxnHst.org).list(qTmTxnHst.org, qTmTxnHst.txnAmt.sum());
		for (Tuple o : tmTxnHstList) {
			String org = o.get(qTmTxnHst.org);
			BigDecimal value = o.get(qTmTxnHst.txnAmt.sum());

			logger.debug("cell_09：" + org + "cell_09：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_09 = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		// 2.4本期还款金额（万元）
		txnCodeList = new ArrayList<String>();
		txnCodeList.add("T840"); // 本行还款
		txnCodeList.add("T842"); // 转账还款
		txnCodeList.add("T844"); // 银联跨行还款 
		txnCodeList.add("T846"); // 本行约定还款
		tmTxnHstList = new JPAQuery(em).from(qTmTxnHst)
				.where(qTmTxnHst.txnCode.in(txnCodeList).and(qTmTxnHst.postDate.between(firstDayOfQuarter, lastDayOfQuarter)))
				.groupBy(qTmTxnHst.org).list(qTmTxnHst.org, qTmTxnHst.txnAmt.sum());
		for (Tuple o : tmTxnHstList) {
			String org = o.get(qTmTxnHst.org);
			BigDecimal value = o.get(qTmTxnHst.txnAmt.sum());

			logger.debug("cell_10：" + org + "cell_10：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_10 = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
	}

	private void set3(Map<String, T9001To1104G17RptItem> g17Map) {
		List<Tuple> tmAccountList;
		QCcsAcct qTmAccount = QCcsAcct.ccsAcct;
		// 3.1存款余额（万元），当前余额小于零
		tmAccountList = new JPAQuery(em).from(qTmAccount).where(qTmAccount.currBal.lt(BigDecimal.ZERO))
				.groupBy(qTmAccount.org).list(qTmAccount.org, qTmAccount.currBal.sum());
		for (Tuple o : tmAccountList) {
			String org = o.get(qTmAccount.org);
			BigDecimal value = o.get(qTmAccount.currBal.sum());

			logger.debug("cell_12：" + org + "cell_12：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_12 = value.abs().setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		// 3.2授信额度（万元），包括临时额度
		tmAccountList = new JPAQuery(em).from(qTmAccount).groupBy(qTmAccount.org).list(qTmAccount.org, qTmAccount.creditLmt.sum());
		for (Tuple o : tmAccountList) {
			String org = o.get(qTmAccount.org);
			BigDecimal value = o.get(qTmAccount.creditLmt.sum());

			logger.debug("cell_13：" + org + "cell_13：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_13 = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		// 3.3应收账款余额（万元），当前余额大于零
		tmAccountList = new JPAQuery(em).from(qTmAccount).where(qTmAccount.currBal.gt(BigDecimal.ZERO))
				.groupBy(qTmAccount.org).list(qTmAccount.org, qTmAccount.currBal.sum());
		for (Tuple o : tmAccountList) {
			String org = o.get(qTmAccount.org);
			BigDecimal value = o.get(qTmAccount.currBal.sum());

			logger.debug("cell_14：" + org + "cell_14：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_14 = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		// 3.3.1其中：生息的应收账款余额
		// 3.4循环信用账户户数（户），首次消费日期不等于null
		// 3.5循环信用账户透支余额（万元），首次消费日期不等于null
		tmAccountList = new JPAQuery(em).from(qTmAccount).where(qTmAccount.firstRetlAmt.isNotNull())
				.groupBy(qTmAccount.org).list(qTmAccount.org, qTmAccount.acctNbr.count(), qTmAccount.currBal.sum());
		for (Tuple o : tmAccountList) {
			String org = o.get(qTmAccount.org);
			Integer value = o.get(qTmAccount.acctNbr.count()).intValue();
			BigDecimal value1 = o.get(qTmAccount.currBal.sum());

			logger.debug("cell_16：" + org + "cell_16：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_16 = value;
			g17Map.get(org).cell_17 = value1.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
	}

	private void set4(Map<String, T9001To1104G17RptItem> g17Map, Date firstDayOfYear, Date lastDayOfQuarter) {
		List<String> txnCodeList;
		List<Tuple> tmTxnHstList;
		Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
		QCcsTxnHst qTmTxnHst = QCcsTxnHst.ccsTxnHst;
		// 4.1年费收入（万元）
		txnCodeList = new ArrayList<String>();
		txnCodeList.add("G107"); // 年费入账
		txnCodeList.add("T505"); // 年费借记调整
		txnCodeList.add("T506"); // 年费贷记调整
		tmTxnHstList = new JPAQuery(em).from(qTmTxnHst)
				.where(qTmTxnHst.txnCode.in(txnCodeList).and(qTmTxnHst.postDate.between(firstDayOfYear, lastDayOfQuarter)))
				.groupBy(qTmTxnHst.org, qTmTxnHst.dbCrInd)
				.list(qTmTxnHst.org, qTmTxnHst.dbCrInd, qTmTxnHst.postAmt.sum());
		map.clear();
		for (Tuple o : tmTxnHstList) {
			updateMapFromList(map, o.get(qTmTxnHst.org), o.get(qTmTxnHst.dbCrInd), o.get(qTmTxnHst.postAmt.sum()));
		}
		for(String org : map.keySet()){
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_19 = map.get(org).setScale(2, BigDecimal.ROUND_HALF_UP);
			
			logger.debug("cell_19：" + org + "cell_19：" + map.get(org));
		}
		
		// 4.2佣金收入（万元）
		txnCodeList = new ArrayList<String>();
		//txnCodeList.add("G203"); // 银联发卡方应收手续费
		//txnCodeList.add("G205"); // 行内发卡方应收手续费
		txnCodeList.add("T801"); // 本行消费  
		txnCodeList.add("T803"); // 银联跨行消费
		txnCodeList.add("T805"); // 银联境外消费 
		txnCodeList.add("T807"); // 手工压单消费
		txnCodeList.add("T821"); // 本行预授权完成
		txnCodeList.add("T823"); // 跨行预授权完成
		txnCodeList.add("T825"); // 银联境外预授权完成
		tmTxnHstList = new JPAQuery(em).from(qTmTxnHst)
				.where(qTmTxnHst.txnCode.in(txnCodeList).and(qTmTxnHst.postDate.between(firstDayOfYear, lastDayOfQuarter)))
				.groupBy(qTmTxnHst.org).list(qTmTxnHst.org, qTmTxnHst.feeProfit.sum());
		for (Tuple o : tmTxnHstList) {
			String org = o.get(qTmTxnHst.org);
			BigDecimal value = o.get(qTmTxnHst.feeProfit.sum());

			logger.debug("cell_20：" + org + "cell_20：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_20 = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		// 4.3利息收入（万元）
		txnCodeList = new ArrayList<String>();
		txnCodeList.add("G101"); // 消费利息入账
		txnCodeList.add("T503"); // 消费利息借记调整
		txnCodeList.add("T504"); // 消费利息贷记调整
		txnCodeList.add("G103"); // 取现利息入账
		txnCodeList.add("T603"); // 取现利息借记调整
		txnCodeList.add("T604"); // 取现利息贷记调整
		txnCodeList.add("T613"); // 取现利息借记红字调整 //FIXME 是否统计
		txnCodeList.add("G105"); // 分期利息入账
		txnCodeList.add("T703"); // 分期利息借记调整
		txnCodeList.add("T704"); // 分期利息贷记调整
		txnCodeList.add("G123"); // 转出分期利息入账
		txnCodeList.add("G125"); // 溢缴款利息入账
		txnCodeList.add("G127"); // 账户利息入账
		tmTxnHstList = new JPAQuery(em).from(qTmTxnHst)
				.where(qTmTxnHst.txnCode.in(txnCodeList).and(qTmTxnHst.postDate.between(firstDayOfYear, lastDayOfQuarter)))
				.groupBy(qTmTxnHst.org, qTmTxnHst.dbCrInd)
				.list(qTmTxnHst.org, qTmTxnHst.dbCrInd, qTmTxnHst.postAmt.sum());
		map.clear();
		for (Tuple o : tmTxnHstList) {
			updateMapFromList(map, o.get(qTmTxnHst.org), o.get(qTmTxnHst.dbCrInd), o.get(qTmTxnHst.postAmt.sum()));
		}
		for(String org : map.keySet()){
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_21 = map.get(org).setScale(2, BigDecimal.ROUND_HALF_UP);
			
			logger.debug("cell_21：" + org + "cell_21：" + map.get(org));
		}
		// 4.4惩罚性收入（万元）
		txnCodeList = new ArrayList<String>();
		txnCodeList.add("G109"); // 超限费入账
		txnCodeList.add("T507"); // 超限费借记调整
		txnCodeList.add("T508"); // 超限费贷记调整
		txnCodeList.add("G111"); // 滞纳金入账
		txnCodeList.add("T509"); // 滞纳金借记调整
		txnCodeList.add("T510"); // 滞纳金贷记调整
		tmTxnHstList = new JPAQuery(em).from(qTmTxnHst)
				.where(qTmTxnHst.txnCode.in(txnCodeList).and(qTmTxnHst.postDate.between(firstDayOfYear, lastDayOfQuarter)))
				.groupBy(qTmTxnHst.org, qTmTxnHst.dbCrInd)
				.list(qTmTxnHst.org, qTmTxnHst.dbCrInd, qTmTxnHst.postAmt.sum());
		map.clear();
		for (Tuple o : tmTxnHstList) {
			updateMapFromList(map, o.get(qTmTxnHst.org), o.get(qTmTxnHst.dbCrInd), o.get(qTmTxnHst.postAmt.sum()));
		}
		for(String org : map.keySet()){
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_22 = map.get(org).setScale(2, BigDecimal.ROUND_HALF_UP);
			
			logger.debug("cell_22：" + org + "cell_22：" + map.get(org));
		}
		
		// 4.5其他收入（万元）
		txnCodeList = new ArrayList<String>();
		txnCodeList.add("G113"); // 分期手续费入账
		txnCodeList.add("G114"); // 商户分期手续费退还
		txnCodeList.add("T705"); // 分期手续费借记调整
		txnCodeList.add("T706"); // 分期手续费贷记调整
		txnCodeList.add("G115"); // 本行ATM取现费
		txnCodeList.add("T605"); // ATM本行取现费借记调整
		txnCodeList.add("T606"); // ATM本行取现费贷记调整
		txnCodeList.add("G117"); // 跨行ATM取现费
		txnCodeList.add("T609"); // 跨行取现费借记调整
		txnCodeList.add("G610"); // 跨行取现费贷记调整
		txnCodeList.add("G119"); // 本行柜台取现费 
		txnCodeList.add("T607"); // 本行柜台取现费借记调整
		txnCodeList.add("T608"); // 本行柜台取现费贷记调整 
		txnCodeList.add("G121"); // 紧急取现费  
		txnCodeList.add("T851"); // 密码重置费
		txnCodeList.add("T511"); // 密码重置费借记调整
		txnCodeList.add("T512"); // 密码重置费贷记调整
		txnCodeList.add("T853"); // 账单费
		txnCodeList.add("T513"); // 账单费借记调整
		txnCodeList.add("T514"); // 账单费贷记调整
		txnCodeList.add("T855"); // 卡片工本费
		txnCodeList.add("T515"); // 卡片工本费借记调整
		txnCodeList.add("T516"); // 卡片工本费贷记调整
		txnCodeList.add("T857"); // 卡片挂失费 
		txnCodeList.add("T517"); // 卡片挂失费借记调整
		txnCodeList.add("T518"); // 卡片挂失费贷记调整
		tmTxnHstList = new JPAQuery(em).from(qTmTxnHst)
				.where(qTmTxnHst.txnCode.in(txnCodeList).and(qTmTxnHst.postDate.between(firstDayOfYear, lastDayOfQuarter)))
				.groupBy(qTmTxnHst.org, qTmTxnHst.dbCrInd)
				.list(qTmTxnHst.org, qTmTxnHst.dbCrInd, qTmTxnHst.postAmt.sum());
		map.clear();
		for (Tuple o : tmTxnHstList) {
			updateMapFromList(map, o.get(qTmTxnHst.org), o.get(qTmTxnHst.dbCrInd), o.get(qTmTxnHst.postAmt.sum()));
		}
		for(String org : map.keySet()){
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_23 = map.get(org).setScale(2, BigDecimal.ROUND_HALF_UP);
			
			logger.debug("cell_23：" + org + "cell_23：" + map.get(org));
		}
		
		// 4.6收入合计（万元）
		for (String org : g17Map.keySet()) {
			g17Map.get(org).cell_24 = (g17Map.get(org).cell_19 == null ? BigDecimal.ZERO : g17Map.get(org).cell_19)
								.add(g17Map.get(org).cell_20 == null ? BigDecimal.ZERO : g17Map.get(org).cell_20)    
								.add(g17Map.get(org).cell_21 == null ? BigDecimal.ZERO : g17Map.get(org).cell_21)
								.add(g17Map.get(org).cell_22 == null ? BigDecimal.ZERO : g17Map.get(org).cell_22)
								.add(g17Map.get(org).cell_23 == null ? BigDecimal.ZERO : g17Map.get(org).cell_23);
		}
	}

	private void set5(Map<String, T9001To1104G17RptItem> g17Map) {
		// 5.1损失准备余额（万元）
		// 5.2本期冲销（万元）
		// 5.3本年累计冲销（万元）
		// 5.4本年累计伪冒损失（万元）
		// 5.5伪冒损失准备余额（万元）
	}

	private void set6(Map<String, T9001To1104G17RptItem> g17Map) {
		CcsAcct a = new CcsAcct();
		a.getFirstRetlAmt();
		a.getAgeCode();
		
		List<Tuple> tmAccountList;
		QCcsAcct qTmAccount = QCcsAcct.ccsAcct;
		// 6.1逾期账户户数（户）
		// 6.2逾期账户授信额度（万元），包含临时额度
		// 6.4逾期的透支余额（万元）
		tmAccountList = new JPAQuery(em).from(qTmAccount)
				.where(qTmAccount.ageCode.ne("C").and(qTmAccount.ageCode.goe("2")))
				.groupBy(qTmAccount.org).list(qTmAccount.org, qTmAccount.acctNbr.count(), qTmAccount.creditLmt.sum(), qTmAccount.currBal.sum());
		for (Tuple o : tmAccountList) {
			String org = o.get(qTmAccount.org);
			Integer value = o.get(qTmAccount.acctNbr.count()).intValue();
			BigDecimal value1 = o.get(qTmAccount.creditLmt.sum());
			BigDecimal value2 = o.get(qTmAccount.currBal.sum());

			logger.debug("cell_32：" + org + "cell_32：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_32 = value;
			g17Map.get(org).cell_33 = value1.setScale(2, BigDecimal.ROUND_HALF_UP);
			g17Map.get(org).cell_35 = value2.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		// 6.3未逾期的透支余额(M0)（万元）
		tmAccountList = new JPAQuery(em).from(qTmAccount)
				.where(qTmAccount.ageCode.in("C","0","1").and(qTmAccount.currBal.gt(BigDecimal.ZERO))).groupBy(qTmAccount.org).list(qTmAccount.org, qTmAccount.currBal.sum());
		for (Tuple o : tmAccountList) {
			String org = o.get(qTmAccount.org);
			BigDecimal value = o.get(qTmAccount.currBal.sum());

			logger.debug("cell_34：" + org + "cell_34：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_34 = value.abs().setScale(2, BigDecimal.ROUND_HALF_UP);
			logger.debug("cell_34：" + g17Map.get(org).cell_34);
		}
		// 6.4.1 1-30天(M1)
		tmAccountList = new JPAQuery(em).from(qTmAccount)
				.where(qTmAccount.ageCode.eq("2")).groupBy(qTmAccount.org).list(qTmAccount.org, qTmAccount.currBal.sum());
		for (Tuple o : tmAccountList) {
			String org = o.get(qTmAccount.org);
			BigDecimal value = o.get(qTmAccount.currBal.sum());

			logger.debug("cell_36：" + org + "cell_36：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_36 = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		// 6.4.2 31-60天(M2)	
		tmAccountList = new JPAQuery(em).from(qTmAccount)
				.where(qTmAccount.ageCode.eq("3")).groupBy(qTmAccount.org).list(qTmAccount.org, qTmAccount.currBal.sum());
		for (Tuple o : tmAccountList) {
			String org = o.get(qTmAccount.org);
			BigDecimal value = o.get(qTmAccount.currBal.sum());

			logger.debug("cell_37：" + org + "cell_37：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_37 = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		// 6.4.3 61-90天(M3)	
		tmAccountList = new JPAQuery(em).from(qTmAccount)
				.where(qTmAccount.ageCode.eq("4")).groupBy(qTmAccount.org).list(qTmAccount.org, qTmAccount.currBal.sum());
		for (Tuple o : tmAccountList) {
			String org = o.get(qTmAccount.org);
			BigDecimal value = o.get(qTmAccount.currBal.sum());

			logger.debug("cell_38：" + org + "cell_38：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_38 = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		// 6.4.4 91-120天(M4)
		tmAccountList = new JPAQuery(em).from(qTmAccount)
				.where(qTmAccount.ageCode.eq("5")).groupBy(qTmAccount.org).list(qTmAccount.org, qTmAccount.currBal.sum());
		for (Tuple o : tmAccountList) {
			String org = o.get(qTmAccount.org);
			BigDecimal value = o.get(qTmAccount.currBal.sum());

			logger.debug("cell_39：" + org + "cell_39：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_39 = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		// 6.4.5 121-150天(M5)
		tmAccountList = new JPAQuery(em).from(qTmAccount)
				.where(qTmAccount.ageCode.eq("6")).groupBy(qTmAccount.org).list(qTmAccount.org, qTmAccount.currBal.sum());
		for (Tuple o : tmAccountList) {
			String org = o.get(qTmAccount.org);
			BigDecimal value = o.get(qTmAccount.currBal.sum());

			logger.debug("cell_40：" + org + "cell_40：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_40 = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		// 6.4.6 151-180天(M6)
		tmAccountList = new JPAQuery(em).from(qTmAccount)
				.where(qTmAccount.ageCode.eq("7")).groupBy(qTmAccount.org).list(qTmAccount.org, qTmAccount.currBal.sum());
		for (Tuple o : tmAccountList) {
			String org = o.get(qTmAccount.org);
			BigDecimal value = o.get(qTmAccount.currBal.sum());

			logger.debug("cell_41：" + org + "cell_41：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_41 = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
		// 6.4.7 超过180天(M6+)
		tmAccountList = new JPAQuery(em).from(qTmAccount)
				.where(qTmAccount.ageCode.ne("C").and(qTmAccount.ageCode.gt("7")))
				.groupBy(qTmAccount.org).list(qTmAccount.org, qTmAccount.currBal.sum());
		for (Tuple o : tmAccountList) {
			String org = o.get(qTmAccount.org);
			BigDecimal value = o.get(qTmAccount.currBal.sum());

			logger.debug("cell_42：" + org + "cell_42：" + value);
			
			this.isContainsKey(org, g17Map);
			g17Map.get(org).cell_42 = value.setScale(2, BigDecimal.ROUND_HALF_UP);
		}
	}

	private void set7(Map<String, T9001To1104G17RptItem> g17Map) {
		// 7.1银行网点数（个）	
		// 7.2自助机具台数（台）	
		// 7.3特约商户数（户）	
		// 7.4POS设备台数（台）	
	}
	
	/** 判断map中是否存在key，不存在新建
	 * @param org
	 * @param g17Map
	 */
	private void isContainsKey(String org, Map<String, T9001To1104G17RptItem> g17Map){
		if (!g17Map.containsKey(org)) {
			T9001To1104G17RptItem g17 = new T9001To1104G17RptItem();
			g17.org = org;
			g17Map.put(org, g17);
		}
	}
	
	/**
	 * 按借贷记符号统计金额并存放在map中
	 * 
	 * @param map
	 * @param org
	 * @param sign
	 * @param value
	 */
	private void updateMapFromList(Map<String, BigDecimal> map, String org, DbCrInd sign, BigDecimal value) {
		if(map.containsKey(org)){
			map.put(org, map.get(org).add(getSign(sign, value)));
		}else{
			map.put(org, getSign(sign, value));
		}
	}

	/**
	 * 获取带借贷记方向的金额
	 * 
	 * @param sign
	 * @param value
	 * @return
	 */
	private BigDecimal getSign(DbCrInd sign, BigDecimal value) {
		if(sign == DbCrInd.C){
			return value.negate();
		}else if(sign == DbCrInd.D){
			return value;
		}else{
			return BigDecimal.ZERO;
		}
	}

}
