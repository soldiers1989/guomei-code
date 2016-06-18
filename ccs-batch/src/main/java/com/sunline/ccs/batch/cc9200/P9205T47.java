package com.sunline.ccs.batch.cc9200;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.sunline.ccs.facility.BlockCodeUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.param.def.TxnCd;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.report.ccs.T47IDDepositItem;
import com.sunline.ppy.dictionary.report.ccs.T47TransactionItem;

/**
 * @see 类名：P9205T47
 * @see 描述：生成反洗钱接口文件(阜新)
 *
 * @see 创建日期：   2015-6-24下午5:18:12
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P9205T47 implements ItemProcessor<S9201MasterData, S9201MasterData> {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	@Autowired
	private UnifiedParameterFacility parameterFacility;
	
	@Override
	public S9201MasterData process(S9201MasterData item) throws Exception {
		try {
			//对私存款账户信息
			item.setT47IdDepositItem(makeT47IDDepositItem(item.getAccount(), item.getCustomer()));
			//交易流水信息
			for(CcsPostingTmp post : item.getListTxnPost()){
				item.getT47TransactionItems().add(makeT47TransactionItem(item.getCustomer(), item.getAccount(), post));
			}
			return item;
		} catch (Exception e) {
			logger.error("反洗钱接口文件生成异常, 账户{}{}", item.getAccount().getAcctNbr(), item.getAccount().getAcctType());
			throw e;
		}
	}

	
	/**
	 * 
	 * @see 方法名：makeT47IDDepositItem 
	 * @see 描述：对私存款账户信息
	 * @see 创建日期：2015-6-24下午5:28:44
	 * @author ChengChun
	 *  
	 * @param acct
	 * @param cust
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private T47IDDepositItem makeT47IDDepositItem(CcsAcct acct, CcsCustomer cust) {
		T47IDDepositItem idDeposit = new T47IDDepositItem();
		
		idDeposit.acctNum = acct.getAcctNbr().toString();
		idDeposit.partyId = cust.getInternalCustomerId();//银行行内统一客户号
		idDeposit.acctTypeCd = "0017";
		idDeposit.amlAcctTypeCd = "0017";
		idDeposit.acctCategoryCd = "04";
		idDeposit.ibPropertyCd = "";
		idDeposit.ibTypeCd = "";
		idDeposit.organkey = acct.getOwningBranch();
		idDeposit.currencyCd = acct.getAcctType().getCurrencyCode();//文档中为"CNY"
		idDeposit.subjectno = "2016";
		idDeposit.openDt = acct.getSetupDate();
		idDeposit.acctProcessingDt = null;
		idDeposit.matureDt = null;
		idDeposit.lastOccurDt = null;
		idDeposit.closeDt = acct.getClosedDate();
		idDeposit.openAmt = BigDecimal.ZERO;
		idDeposit.lastAmtVal = null;
		idDeposit.amtVal = acct.getCurrBal();
		idDeposit.tokenId = null;
		idDeposit.cardNo = acct.getDefaultLogicCardNbr();
		idDeposit.cashTransferCd = "1";
		idDeposit.acctStatusCd = getAcctStatusCd(acct.getBlockCode());
		idDeposit.intCalMethodCd = "1";
		idDeposit.custManager = null;
		idDeposit.AcctModifierNum = null;// account.getDefaultLogicCardNbr();//不填
		idDeposit.hostCustId = cust.getInternalCustomerId(); //同客户号
		idDeposit.partyChnName = acct.getName();
		idDeposit.lastUpdDt = null;
		idDeposit.acctOrgNum = acct.getOwningBranch();
		idDeposit.bizTypeCd = null;
		idDeposit.perdCd = null;
		
		return idDeposit;
	}

	/**
	 * @see 方法名：makeT47TransactionItem 
	 * @see 描述：交易流水信息
	 * @see 创建日期：2015-6-24下午5:29:02
	 * @author ChengChun
	 *  
	 * @param cust
	 * @param acct
	 * @param post
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private T47TransactionItem makeT47TransactionItem(CcsCustomer cust, CcsAcct acct, CcsPostingTmp post) {
		T47TransactionItem transaction = new T47TransactionItem();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		
		transaction.transactionkey = "D" + sdf.format(post.getTxnTime()) + post.getTxnSeq();
		transaction.cbPk = "D" + sdf.format(post.getTxnTime()) + post.getTxnSeq();
		transaction.txNo = post.getTxnSeq().toString();
		transaction.voucherNo = null;
		transaction.organkey = getOrg(acct, post);// 待检测
		transaction.txDt = post.getTxnDate();
		transaction.dtTime = post.getTxnTime();
		transaction.acctNum = post.getAcctNbr().toString();
		transaction.partyId = cust.getInternalCustomerId();// 待检测
		transaction.partyClassCd = "I";
		transaction.txCd = "9999";
		transaction.cbTxCd = post.getOrigTxnCode(); // ----
		transaction.busTypeCd = post.getTxnShortDesc(); //getBusTypeCdByTxnCd(tmTxnHst.getTxnCode());// 待检测
		transaction.txTypeCd = getTxTypeCdByTxnCode(post.getTxnCode());// 待检测
		transaction.debitCredit = (post.getDbCrInd() == null ? "" : post.getDbCrInd().name());// C or D
		transaction.receivePayCd = getReceivePayCd(post.getDbCrInd());
		transaction.subjectno = "2016";
		transaction.currencyCd = post.getTxnCurrency();
		transaction.currCd = "1";
		transaction.amt = post.getTxnAmt();
		transaction.cnyAmt = post.getPostAmt();
		transaction.usdAmt = null;// 不填
		transaction.amtVal = null;//
		transaction.cashTransFlag = getCashTransFlag(post.getTxnCode());// 待检测
		transaction.remitTypeCd = "9";
		transaction.des = post.getTxnDesc();
		transaction.overareaInd = "0";
		transaction.settleTypeCd = "10";
		transaction.useDes = post.getTxnDesc();
		transaction.oppSysId = null;
		transaction.oppIsparty = null;// 0 非行内客户 1 行内客户
		transaction.oppArea = null;
		transaction.oppOrganType = "14";// 13或14
		transaction.oppOrgankey = getOrg(acct, post);// 待检测
		transaction.oppOrganname = "";// 取那个字段？
		transaction.oppPartyId = null;
		transaction.oppName = "";// 对方客户名？
		transaction.oppAcctNum = "";// 对方账号？
		transaction.oppTxDt = null;
		transaction.oppAcctTypeCd = null;
		transaction.oppCardType = null;
		transaction.oppCardNo = null;
		transaction.oppPartyClassCd = "I";// I or C
		transaction.cancelInd = null;
		transaction.amtCd = null;
		transaction.batchInd = null;
		transaction.teller = null;
		transaction.reInd = null;
		transaction.handleStatusCd = "0";
		transaction.partyChnName = acct.getName();// 待检测
		transaction.addtional = null;
		transaction.reDt = null;
		transaction.txDirect = null;
		transaction.tokenNo = null;
		transaction.hostCustId = cust.getInternalCustomerId();
		transaction.channel = getChanel(post.getTxnCode());// 根据交易码填写，只填1\4\5\9即可
		transaction.calInd = "1";
		transaction.ruleInd = "3";
		transaction.temp1 = null;
		transaction.oppCountry = "CHN";
		transaction.tsctkey = "0000000";
		transaction.txGoCountry = "CHN";
		transaction.txGoArea = "000000";// 六位地区代码，国外地区填 000000
		transaction.txOccurCountry = "CHN";
		transaction.txOccurArea = "000000";
		transaction.agentName = null;
		transaction.agentCardType = null;
		transaction.agentCardNo = null;
		transaction.agentCountry = null;
		transaction.orgTransRela = null;
		transaction.cashInd = getCashIndByTxnType(post.getTxnCode());
		transaction.oppPbcPartyClassCd = null;
		transaction.checkTeller = null;
		transaction.lastUpdUsr = null;
		transaction.oppOffShoreInd = "0";
		transaction.bizTypeCd = null;
		transaction.validateInd = null;
		transaction.validateInd2 = null;
		
		return transaction;
	}  
        /**
         * @see 方法名：getReceivePayCd 
         * @see 描述：TODO 方法描述
         * @see 创建日期：2015-6-24下午5:29:23
         * @author ChengChun
         *  
         * @param ind
         * @return
         * 
         * @see 修改记录： 
         * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
         */
	private String getReceivePayCd(DbCrInd ind) {
		if(ind==null) return "";
		
		switch (ind) {
		case D : return "01"; 
		case C : return "02";
		default: return "";
		}
	}
        /**
         * @see 方法名：getCashTransFlag 
         * @see 描述：TODO 方法描述
         * @see 创建日期：2015-6-24下午5:29:30
         * @author ChengChun
         *  
         * @param txnCode
         * @return
         * 
         * @see 修改记录： 
         * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
         */
	private String getCashTransFlag(String txnCode) {
		TxnCd txnCd = parameterFacility.loadParameter(txnCode, TxnCd.class);
		switch (txnCd.txnType){
		case T04 : return "1";
		case T06 : return "2";
		default: return "";
		}
	}

	/**
	 * @see 方法名：getAcctStatusCd 
	 * @see 描述：根据锁定码获取账户状态
	 * @see 创建日期：2015-6-24下午5:29:39
	 * @author ChengChun
	 *  
	 * @param blockCode
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private String getAcctStatusCd(String blockCode) {
		if (blockCodeUtils.isExists(blockCode, "P")) {
			return "1";
		} else {
			return "0";
		}
	}


	/**
	 * @see 方法名：getTxTypeCdByTxnCode 
	 * @see 描述：根据txnCode获取交易类型
	 * @see 创建日期：2015-6-24下午5:29:56
	 * @author ChengChun
	 *  
	 * @param txnCode
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private String getTxTypeCdByTxnCode(String txnCode) {
		TxnCd txnCd = parameterFacility.loadParameter(txnCode, TxnCd.class);
		switch (txnCd.txnType) {
		case T04: return "01";
		case T06: return "02";
		default: return "09";
		}
	}

	/**
	 * @see 方法名：getOrg 
	 * @see 描述： 获取机构号
	 * @see 创建日期：2015-6-24下午5:30:13
	 * @author ChengChun
	 *  
	 * @param acct
	 * @param post
	 * @return
	 * 
	 * @see 修改记录： 
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	private String getOrg(CcsAcct acct, CcsPostingTmp post) {
		String aOrg;
		if (post.getAcqBranchIq() == null) {
			aOrg = acct.getOwningBranch();
		} else {
			aOrg = post.getAcqBranchIq();
		}
		return aOrg;
	}
        /**
         * @see 方法名：getChanel 
         * @see 描述：TODO 方法描述
         * @see 创建日期：2015-6-24下午5:30:33
         * @author ChengChun
         *  
         * @param txnCode
         * @return
         * 
         * @see 修改记录： 
         * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
         */
	private String getChanel(String txnCode) {
		String chanel = "9";
		if (txnCode != null) {
			if ("T811".equals(txnCode) || "T813".equals(txnCode)
					|| "T831".equals(txnCode)) {
				chanel = "4";
			} else if ("T815".equals(txnCode) || "T835".equals(txnCode)
					|| "T861".equals(txnCode)) {
				chanel = "1";
			} else if ("T801".equals(txnCode) || "T802".equals(txnCode)
					|| "T803".equals(txnCode) || "T804".equals(txnCode)
					|| "T805".equals(txnCode) || "T806".equals(txnCode)
					|| "T807".equals(txnCode) || "T808".equals(txnCode)) {
				chanel = "5";
			} else {
				chanel = "9";
			}
		}
		return chanel;
	}
        /**
         * @see 方法名：getCashIndByTxnType 
         * @see 描述：TODO 方法描述
         * @see 创建日期：2015-6-24下午5:30:42
         * @author ChengChun
         *  
         * @param txnCode
         * @return
         * 
         * @see 修改记录： 
         * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
         */
	private String getCashIndByTxnType(String txnCode) {
		TxnCd txnCd = parameterFacility.retrieveParameterObject(txnCode,
				TxnCd.class);
		String cashInd = "";
		if (txnCd != null) {
			if (txnCd.txnType == null) {
				cashInd = "";
			} else {
				String txnType = txnCd.txnType.name();
				if (txnType.equals("T04")) {
					cashInd = "00";
				} else {
					cashInd = "01";
				}
			}
		}

		return cashInd;
	}
}
