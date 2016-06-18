package com.sunline.ccs.batch.cc9200;

import java.util.ArrayList;
import java.util.List;

import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPostingTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ppy.dictionary.exchange.CcExportAccountItem;
import com.sunline.ppy.dictionary.exchange.CcExportAddressItem;
import com.sunline.ppy.dictionary.exchange.CcExportCardItem;
import com.sunline.ppy.dictionary.exchange.CcExportContactItem;
import com.sunline.ppy.dictionary.exchange.CcExportCustomerItem;
import com.sunline.ppy.dictionary.exchange.CPSExportMonLaundYXItem;
import com.sunline.ppy.dictionary.exchange.CcExportTxnItem;
import com.sunline.ppy.dictionary.exchange.SynchroCardAcctInfoItem;
import com.sunline.ppy.dictionary.report.ccs.T47IDDepositItem;
import com.sunline.ppy.dictionary.report.ccs.T47TransactionItem;
//import com.sunline.smsd.service.sdk.PayRemindMsgItem;

/**
 * 
 * @see 类名：S9201MasterData
 * @see 描述：卸数文件
                                    来源：
                                    账户主表(TM_ACCOUNT)
                                    客户主表(TM_CUSTOMER)
                                    客户额度表(TM_CUSTLIMIT_O)
                                    联系信息表(TM_CONTACT)
                                    地址信息表(TM_ADDRESS)
                                    卡片主表(TM_CARD)
                                    待入账交易表(TT_TXN_POST)
                                    入账交易历史表(TM_TXN_HST)
 *
 * @see 创建日期：   2015-6-24下午5:31:29
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class S9201MasterData{

	/**
	 * 数据源
	 */
	private CcsAcct account;
	private CcsCustomer customer;
	private CcsCustomerCrlmt custLimitO;
	private List<CcsLinkman> listContact = new ArrayList<CcsLinkman>();
	private List<CcsAddress> listAddress = new ArrayList<CcsAddress>();
	private List<CcsCard> listCard = new ArrayList<CcsCard>();
	private List<CcsPostingTmp> listTxnPost = new ArrayList<CcsPostingTmp>();
	private List<CcsTxnHst> listTxnHst = new ArrayList<CcsTxnHst>();
	private List<CcsPlan> listPlan = new ArrayList<CcsPlan>();
	
	/**
	 * 送催收文件
	 */
	private CcExportAccountItem ctsAccount;
	private CcExportCustomerItem ctsCustomer;
	private List<CcExportContactItem> ctsContact = new ArrayList<CcExportContactItem>();
	private List<CcExportAddressItem> ctsAddress = new ArrayList<CcExportAddressItem>();
	private List<CcExportCardItem> ctsCard = new ArrayList<CcExportCardItem>();
	private List<CcExportTxnItem> ctsTxn = new ArrayList<CcExportTxnItem>();
	
	/**
	 * 送反欺诈文件
	 */
	private CcExportAccountItem rdsAccount;
	private CcExportCustomerItem rdsCustomer;
	private List<CcExportContactItem> rdsContact = new ArrayList<CcExportContactItem>();
	private List<CcExportAddressItem> rdsAddress = new ArrayList<CcExportAddressItem>();
	private List<CcExportCardItem> rdsCard = new ArrayList<CcExportCardItem>();
	private List<CcExportTxnItem> rdsTxn = new ArrayList<CcExportTxnItem>();
	
	/**
	 * 送征审文件
	 */
	private List<SynchroCardAcctInfoItem> apsAccountCard = new ArrayList<SynchroCardAcctInfoItem>();
	/**
	 * 玉溪反洗钱接口文件
	 */
	private List<CPSExportMonLaundYXItem> yxMonLaund = new ArrayList<CPSExportMonLaundYXItem>();

	
	/**
	 * 阜新反洗钱接口文件
	 */
	private T47IDDepositItem t47IdDepositItem;
	private List<T47TransactionItem> t47TransactionItems = new ArrayList<T47TransactionItem>();
	
	/**
	 * 送短信文件
	 */
//	private PayRemindMsgItem payRemindMsg;

	public List<CPSExportMonLaundYXItem> getYxMonLaund() {
		return yxMonLaund;
	}

	public void setYxMonLaund(List<CPSExportMonLaundYXItem> yxMonLaund) {
		this.yxMonLaund = yxMonLaund;
	}

	public CcsAcct getAccount() {
		return account;
	}

	public CcsCustomer getCustomer() {
		return customer;
	}

	public CcsCustomerCrlmt getCustLimitO() {
		return custLimitO;
	}

	public List<CcsLinkman> getListContact() {
		return listContact;
	}

	public List<CcsAddress> getListAddress() {
		return listAddress;
	}

	public List<CcsCard> getListCard() {
		return listCard;
	}

	public List<CcsTxnHst> getListTxnHst() {
		return listTxnHst;
	}

	public CcExportAccountItem getCtsAccount() {
		return ctsAccount;
	}

	public CcExportCustomerItem getCtsCustomer() {
		return ctsCustomer;
	}

	public List<CcExportContactItem> getCtsContact() {
		return ctsContact;
	}

	public List<CcExportAddressItem> getCtsAddress() {
		return ctsAddress;
	}

	public List<CcExportCardItem> getCtsCard() {
		return ctsCard;
	}

	public List<CcExportTxnItem> getCtsTxn() {
		return ctsTxn;
	}

	public CcExportAccountItem getRdsAccount() {
		return rdsAccount;
	}

	public CcExportCustomerItem getRdsCustomer() {
		return rdsCustomer;
	}

	public List<CcExportContactItem> getRdsContact() {
		return rdsContact;
	}

	public List<CcExportAddressItem> getRdsAddress() {
		return rdsAddress;
	}

	public List<CcExportCardItem> getRdsCard() {
		return rdsCard;
	}

	public List<CcExportTxnItem> getRdsTxn() {
		return rdsTxn;
	}

	public List<SynchroCardAcctInfoItem> getApsAccountCard() {
		return apsAccountCard;
	}

	public void setAccount(CcsAcct account) {
		this.account = account;
	}

	public void setCustomer(CcsCustomer customer) {
		this.customer = customer;
	}

	public void setCustLimitO(CcsCustomerCrlmt custLimitO) {
		this.custLimitO = custLimitO;
	}

	public void setListContact(List<CcsLinkman> listContact) {
		this.listContact = listContact;
	}

	public void setListAddress(List<CcsAddress> listAddress) {
		this.listAddress = listAddress;
	}

	public void setListCard(List<CcsCard> listCard) {
		this.listCard = listCard;
	}

	public void setListTxnHst(List<CcsTxnHst> listTxnHst) {
		this.listTxnHst = listTxnHst;
	}

	public void setCtsAccount(CcExportAccountItem ctsAccount) {
		this.ctsAccount = ctsAccount;
	}

	public void setCtsCustomer(CcExportCustomerItem ctsCustomer) {
		this.ctsCustomer = ctsCustomer;
	}

	public void setCtsContact(List<CcExportContactItem> ctsContact) {
		this.ctsContact = ctsContact;
	}

	public void setCtsAddress(List<CcExportAddressItem> ctsAddress) {
		this.ctsAddress = ctsAddress;
	}

	public void setCtsCard(List<CcExportCardItem> ctsCard) {
		this.ctsCard = ctsCard;
	}

	public void setCtsTxn(List<CcExportTxnItem> ctsTxn) {
		this.ctsTxn = ctsTxn;
	}

	public void setRdsAccount(CcExportAccountItem rdsAccount) {
		this.rdsAccount = rdsAccount;
	}

	public void setRdsCustomer(CcExportCustomerItem rdsCustomer) {
		this.rdsCustomer = rdsCustomer;
	}

	public void setRdsContact(List<CcExportContactItem> rdsContact) {
		this.rdsContact = rdsContact;
	}

	public void setRdsAddress(List<CcExportAddressItem> rdsAddress) {
		this.rdsAddress = rdsAddress;
	}

	public void setRdsCard(List<CcExportCardItem> rdsCard) {
		this.rdsCard = rdsCard;
	}

	public void setRdsTxn(List<CcExportTxnItem> rdsTxn) {
		this.rdsTxn = rdsTxn;
	}

	public void setApsAccountCard(List<SynchroCardAcctInfoItem> apsAccountCard) {
		this.apsAccountCard = apsAccountCard;
	}

	public List<CcsPlan> getListPlan() {
		return listPlan;
	}

	public void setListPlan(List<CcsPlan> listPlan) {
		this.listPlan = listPlan;
	}
	
	public List<CcsPostingTmp> getListTxnPost() {
		return listTxnPost;
	}

	public void setListTxnPost(List<CcsPostingTmp> listTxnPost) {
		this.listTxnPost = listTxnPost;
	}
	
	public T47IDDepositItem getT47IdDepositItem() {
		return t47IdDepositItem;
	}

	public void setT47IdDepositItem(T47IDDepositItem t47IdDepositItem) {
		this.t47IdDepositItem = t47IdDepositItem;
	}

	public List<T47TransactionItem> getT47TransactionItems() {
		return t47TransactionItems;
	}

	public void setT47TransactionItems(List<T47TransactionItem> t47TransactionItems) {
		this.t47TransactionItems = t47TransactionItems;
	}

/*	public PayRemindMsgItem getPayRemindMsg() {
		return payRemindMsg;
	}

	public void setPayRemindMsg(PayRemindMsgItem payRemindMsg) {
		this.payRemindMsg = payRemindMsg;
	}*/
	
}
