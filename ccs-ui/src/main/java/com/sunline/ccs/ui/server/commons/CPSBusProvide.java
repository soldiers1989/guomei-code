package com.sunline.ccs.ui.server.commons;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.PointAdjustIndicator;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.ccs.infrastructure.server.repos.QCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
//import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
//import com.sunline.ccs.infrastructure.server.repos.RCcsCard;
import com.sunline.ccs.infrastructure.server.repos.RCcsCard;
//import com.sunline.ccs.infrastructure.server.repos.QCcsCardO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardO;
//import com.sunline.ccs.infrastructure.server.repos.RCcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomerCrlmt;
//import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
//import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
//import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
//import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
//import com.sunline.ccs.infrastructure.shared.model.QCcsCardThresholdCtrl;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardThresholdCtrl;
//import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
//import com.sunline.ccs.infrastructure.shared.model.QCcsCardO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardO;
//import com.sunline.ccs.infrastructure.shared.model.QCcsCardUsage;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardUsage;
//import com.sunline.ccs.infrastructure.shared.model.QCcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomerCrlmt;
//import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
//import com.sunline.ccs.infrastructure.shared.model.QCcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsPointsReg;
//import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
//import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
//import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
//import com.sunline.ccs.infrastructure.shared.model.CcsCardThresholdCtrl;
import com.sunline.ccs.infrastructure.shared.model.CcsCardThresholdCtrl;
//import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
//import com.sunline.ccs.infrastructure.shared.model.CcsCardUsage;
import com.sunline.ccs.infrastructure.shared.model.CcsCardUsage;
//import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
//import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
//import com.sunline.ccs.infrastructure.shared.model.CcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.CcsPointsReg;
import com.sunline.ark.support.OrganizationContextHolder;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;

/**
 * @author fanghj
 * @author linxc 2015年6月22日 
 *
 */

@Service
public class CPSBusProvide {

	QCcsCardLmMapping qTmCardMediaMap = QCcsCardLmMapping.ccsCardLmMapping;

	QCcsCard qTmCard = QCcsCard.ccsCard;

	QCcsCustomer qTmCustomer = QCcsCustomer.ccsCustomer;

	QCcsCustomerCrlmt qTmCustLimitO = QCcsCustomerCrlmt.ccsCustomerCrlmt;

	QCcsAcct qTmAccount = QCcsAcct.ccsAcct;

	QCcsPointsReg qTmPointReg = QCcsPointsReg.ccsPointsReg;

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private RCcsCustomer rTmCustomer;

	@Autowired
	private RCcsAcct rTmAccount;

	@Autowired
	private RCcsAcctO rTmAccountO;

	@Autowired
	private RCcsCard rTmCard;

	@Autowired
	private RCcsCardO rTmCardO;

	@Autowired
	private RCcsCustomerCrlmt rTmCustLimitO;

	/**
	 * 根据客户编号查找客户TmCustomer信息
	 * 
	 * @param custId
	 * @return
	 * @throws ProcessException
	 */
	public CcsCustomer getTmCustomerByCustId(Long custId) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qTmCustomer.org.eq(OrganizationContextHolder.getCurrentOrg()).and(
				qTmCustomer.custId.eq(custId));
		CcsCustomer tmCustomer = query.from(qTmCustomer).where(booleanExpression).singleResult(qTmCustomer);
		CheckUtil.rejectNull(tmCustomer, "客户编号[" + custId + "]找不到对应的客户信息");
		return tmCustomer;
	}

	/**
	 * 根据卡号查询客户TmCustomer信息
	 * 
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public CcsCustomer getTmCustomerToCard(String cardNbr) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qTmCardMediaMap.org.eq(OrganizationContextHolder.getCurrentOrg())
				.and(qTmCardMediaMap.cardNbr.eq(cardNbr)).and(qTmCardMediaMap.logicCardNbr.eq(qTmCard.logicCardNbr))
				.and(qTmCard.custId.eq(qTmCustomer.custId));
		CcsCustomer tmCustomer = query.from(qTmCardMediaMap, qTmCard, qTmCustomer).where(booleanExpression)
				.singleResult(qTmCustomer);
		CheckUtil.rejectNull(tmCustomer, "卡号[" + cardNbr + "]找不到对应的客户信息");
		return tmCustomer;
	}

	/**
	 * 根据证件查询客户TmCustomer信息
	 * 
	 * @param idNo
	 * @param idType
	 * @return
	 * @throws ProcessException
	 */
	public CcsCustomer getTmCustomerByIdNoAndIdType(String idNo, IdType idType) throws ProcessException {
		List<CcsCustomer> list = rTmCustomer.findByIdNoAndOrgAndIdType(idNo, OrganizationContextHolder.getCurrentOrg(),
				idType);
		if(list != null && list.size() > 0) {
			return rTmCustomer.findByIdNoAndOrgAndIdType(idNo, OrganizationContextHolder.getCurrentOrg(), idType)
					.get(0);
		}
		return null;
	}

	/**
	 * 根据custLimitId查询客户额度信息
	 * 
	 * @param custLimitId
	 * @return
	 */
	public CcsCustomerCrlmt getTmCustLimitOToCustLimitId(Long custLimitId) {
		CcsCustomerCrlmt tmCustLimitO = rTmCustLimitO.findOne(qTmCustLimitO.custLmtId.eq(custLimitId));
		return tmCustLimitO;
	}

	/**
	 * 根据客户号获取账户列表信息
	 * 
	 * @param custId
	 * @return
	 */
	public List<CcsAcct> getTmAccountToCustId(Long custId) {
		JPAQuery query = new JPAQuery(em);
		BooleanExpression bp = qTmAccount.org.eq(OrganizationContextHolder.getCurrentOrg()).and(
				qTmAccount.custId.eq(custId));
		return query.from(qTmAccount).where(bp).list(qTmAccount);
	}

	/**
	 * 根据卡号获取账户信息
	 * 
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public List<CcsAcct> getTmAccountTocardNbr(String cardNbr) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		List<CcsAcct> tmAccountlist = query
				.from(qTmCardMediaMap, qTmCard, qTmAccount)
				.where(qTmCardMediaMap.logicCardNbr.eq(qTmCard.logicCardNbr).and(
						qTmCardMediaMap.org
								.eq(qTmCard.org)
								.and(qTmCardMediaMap.cardNbr.eq(cardNbr))
								.and(qTmCard.acctNbr.eq(qTmAccount.acctNbr).and(
										qTmCardMediaMap.org.eq(OrganizationContextHolder.getCurrentOrg())))))
				.list(qTmAccount);
		if(tmAccountlist.isEmpty()) {
			throw new ProcessException("卡号[" + cardNbr + "]查询不到对应的账户信息");
		}
		return tmAccountlist;
	}

	/**
	 * 根据卡号和币种获取TmAccount账户信息
	 * 
	 * @param cardNbr
	 * @param currencyCode
	 * @return
	 * @throws ProcessException
	 */
	public CcsAcct getTmAccountTocardNbrAndcurrencyCode(String cardNbr, String currencyCode) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		CcsAcct tmAccount = query
				.from(qTmCardMediaMap, qTmCard, qTmAccount)
				.where(qTmCardMediaMap.logicCardNbr.eq(qTmCard.logicCardNbr).and(
						qTmCardMediaMap.org
								.eq(qTmCard.org)
								.and(qTmCardMediaMap.cardNbr.eq(cardNbr))
								.and(qTmCard.acctNbr.eq(qTmAccount.acctNbr).and(qTmAccount.currency.eq(currencyCode))
										.and(qTmCardMediaMap.org.eq(OrganizationContextHolder.getCurrentOrg())))))
				.singleResult(qTmAccount);
		if(tmAccount == null) {
			throw new ProcessException("卡号[" + cardNbr + "]查询不到对应的账户信息");
		}
		return tmAccount;
	}

	/**
	 * 根据卡号获取账户列表，其中账户列表忽略本外币
	 * 
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public List<Long> getacctNbrListDistinctTocardNbr(String cardNbr) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		List<Long> acctNbrList = query
				.from(qTmCardMediaMap, qTmCard, qTmAccount)
				.where(qTmCardMediaMap.logicCardNbr.eq(qTmCard.logicCardNbr).and(
						qTmCardMediaMap.org
								.eq(qTmCard.org)
								.and(qTmCardMediaMap.cardNbr.eq(cardNbr))
								.and(qTmCard.acctNbr.eq(qTmAccount.acctNbr).and(
										qTmCardMediaMap.org.eq(OrganizationContextHolder.getCurrentOrg())))))
				.distinct().list(qTmAccount.acctNbr);
		if(acctNbrList.isEmpty()) {
			throw new ProcessException("卡号[" + cardNbr + "]查询不到对应的账户信息");
		}
		return acctNbrList;
	}

	/**
	 * 根据卡号，账户类型获取账户信息
	 * 
	 * @param cardNbr
	 * @param acctType
	 * @return
	 * @throws ProcessException
	 */
	public CcsAcct getTmAccountTocardNbr(String cardNbr, AccountType acctType) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		CcsAcct tmAccount = query
				.from(qTmCardMediaMap, qTmCard, qTmAccount)
				.where(qTmCardMediaMap.logicCardNbr.eq(qTmCard.logicCardNbr).and(
						qTmCardMediaMap.org
								.eq(qTmCard.org)
								.and(qTmCardMediaMap.cardNbr.eq(cardNbr))
								.and(qTmCard.acctNbr.eq(qTmAccount.acctNbr).and(
										qTmCardMediaMap.org.eq(OrganizationContextHolder.getCurrentOrg()).and(
												qTmAccount.acctType.eq(acctType)))))).singleResult(qTmAccount);
		CheckUtil.rejectNull(tmAccount, "卡号[" + cardNbr + "]查询不到对应的账户信息");
		return tmAccount;
	}

	/**
	 * 根据卡号，账户类型获取账户TmAccountO 信息
	 * 
	 * @param cardNbr
	 * @param acctType
	 * @return
	 * @throws ProcessException
	 */
	public CcsAcctO getTmAccountOTocardNbr(String cardNbr, AccountType acctType) throws ProcessException {
		QCcsAcctO qTmAccountO = QCcsAcctO.ccsAcctO;
		JPAQuery query = new JPAQuery(em);
		CcsAcctO tmAccountO = query
				.from(qTmCardMediaMap, qTmCard, qTmAccountO)
				.where(qTmCardMediaMap.logicCardNbr.eq(qTmCard.logicCardNbr).and(
						qTmCardMediaMap.org
								.eq(qTmCard.org)
								.and(qTmCardMediaMap.cardNbr.eq(cardNbr))
								.and(qTmCard.acctNbr.eq(qTmAccountO.acctNbr).and(
										qTmCardMediaMap.org.eq(OrganizationContextHolder.getCurrentOrg()).and(
												qTmAccountO.acctType.eq(acctType)))))).singleResult(qTmAccountO);
		CheckUtil.rejectNull(tmAccountO, "卡号[" + cardNbr + "]查询不到对应的账户信息");
		return tmAccountO;
	}

	/**
	 * 根据卡号获取TmAccountO信息
	 * 
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public List<CcsAcctO> getTmAccountOTocardNbr(String cardNbr) throws ProcessException {
		QCcsAcctO qTmAccountO = QCcsAcctO.ccsAcctO;
		JPAQuery query = new JPAQuery(em);
		List<CcsAcctO> tmCardOLists = query
				.from(qTmCardMediaMap, qTmCard, qTmAccountO)
				.where(qTmCardMediaMap.logicCardNbr.eq(qTmCard.logicCardNbr).and(
						qTmCardMediaMap.org
								.eq(qTmCard.org)
								.and(qTmCardMediaMap.cardNbr.eq(cardNbr))
								.and(qTmCard.acctNbr.eq(qTmAccountO.acctNbr).and(
										qTmCardMediaMap.org.eq(OrganizationContextHolder.getCurrentOrg())))))
				.list(qTmAccountO);
		if(tmCardOLists.isEmpty()) {
			throw new ProcessException("卡号[" + cardNbr + "]查询不到对应的账户信息");
		}
		return tmCardOLists;
	}

	/**
	 * 放置OTB数据
	 * 
	 * @param maps
	 */
	public void setOTB(Map<String, Serializable> maps) {
		maps.put(CPSConstants.KEY_CASH_OTB, new BigDecimal(1));
		maps.put(CPSConstants.KEY_OTB, new BigDecimal(1));
	}

	/**
	 * 根据卡号获取TmCardO数据
	 * 
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public CcsCardO getTmCardOTocardNbr(String cardNbr) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		QCcsCardO qTmCardO = QCcsCardO.ccsCardO;
		CcsCardO tmCardO = query
				.from(qTmCardMediaMap, qTmCardO)
				.where(qTmCardMediaMap.logicCardNbr.eq(qTmCardO.logicCardNbr).and(
						qTmCardMediaMap.org.eq(qTmCardO.org).and(
								qTmCardMediaMap.cardNbr.eq(cardNbr).and(
										qTmCardMediaMap.org.eq(OrganizationContextHolder.getCurrentOrg())))))
				.singleResult(qTmCardO);
		CheckUtil.rejectNull(tmCardO, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
		return tmCardO;
	}

	/**
	 * 根据卡号获取TmCard数据
	 * 
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public CcsCard getTmCardTocardNbr(String cardNbr) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		CcsCard tmCard = query
				.from(qTmCardMediaMap, qTmCard)
				.where(qTmCardMediaMap.logicCardNbr.eq(qTmCard.logicCardNbr).and(
						qTmCardMediaMap.org.eq(qTmCard.org).and(
								qTmCardMediaMap.cardNbr.eq(cardNbr).and(
										qTmCardMediaMap.org.eq(OrganizationContextHolder.getCurrentOrg())))))
				.singleResult(qTmCard);
		CheckUtil.rejectNull(tmCard, "卡号:[" + cardNbr + "]查询不到对应的卡片信息");
		return tmCard;
	}

	/**
	 * 根据卡号获取TmCardStst数据
	 * 
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public CcsCardUsage getTmCardStstTocardNbr(String cardNbr) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		QCcsCardUsage qTmCardStst = QCcsCardUsage.ccsCardUsage;
		CcsCardUsage tmCardStst = query
				.from(qTmCardMediaMap, qTmCardStst)
				.where(qTmCardMediaMap.logicCardNbr.eq(qTmCardStst.logicCardNbr).and(
						qTmCardMediaMap.org.eq(qTmCardStst.org).and(
								qTmCardMediaMap.cardNbr.eq(cardNbr).and(
										qTmCardMediaMap.org.eq(OrganizationContextHolder.getCurrentOrg())))))
				.singleResult(qTmCardStst);
		return tmCardStst;
	}

	/**
	 * 根据卡号获取TmCardLimitOverrideO
	 * 
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public CcsCardThresholdCtrl getTmCardLimitOverrideOTocardNbr(String cardNbr) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		QCcsCardThresholdCtrl qTmCardLimitOverrideO = QCcsCardThresholdCtrl.ccsCardThresholdCtrl;
		CcsCardThresholdCtrl tmCardLimitOverrideO = query
				.from(qTmCardMediaMap, qTmCardLimitOverrideO)
				.where(qTmCardMediaMap.logicCardNbr.eq(qTmCardLimitOverrideO.logicCardNbr).and(
						qTmCardMediaMap.cardNbr.eq(cardNbr).and(
								qTmCardMediaMap.org.eq(OrganizationContextHolder.getCurrentOrg()))))
				.singleResult(qTmCardLimitOverrideO);
		return tmCardLimitOverrideO;
	}

	/**
	 * 根据卡号获取所有的附卡信息
	 * 
	 * @param cardNbr
	 * @return
	 */
	public List<CcsCard> getSUPPTmCardTocardNbr(String cardNbr) {
		JPAQuery query = new JPAQuery(em);
		List<CcsCard> tmCardList = query
				.from(qTmCardMediaMap, qTmCard)
				.where(qTmCardMediaMap.logicCardNbr.eq(qTmCard.cardBasicNbr).and(
						qTmCardMediaMap.org.eq(qTmCard.org).and(
								qTmCardMediaMap.cardNbr.eq(cardNbr).and(
										qTmCardMediaMap.org.eq(OrganizationContextHolder.getCurrentOrg())))))
				.list(qTmCard);
		return tmCardList;
	}

	/**
	 * 根据卡号获取所有的附卡信息
	 * 
	 * @param cardNbr
	 * @return
	 */
	public List<CcsCardO> getSUPPTmCardOTocardNbr(String cardNbr) {
		JPAQuery query = new JPAQuery(em);
		QCcsCardO qTmCardO = QCcsCardO.ccsCardO;
		List<CcsCardO> tmCardOList = query
				.from(qTmCardMediaMap, qTmCardO)
				.where(qTmCardMediaMap.logicCardNbr.eq(qTmCardO.cardBasicNbr).and(
						qTmCardMediaMap.org.eq(qTmCardO.org).and(
								qTmCardMediaMap.cardNbr.eq(cardNbr).and(
										qTmCardMediaMap.org.eq(OrganizationContextHolder.getCurrentOrg())))))
				.list(qTmCardO);
		return tmCardOList;
	}

	/**
	 * 根据账户号获取账户列表
	 * 
	 * @param acctNbr
	 * @return
	 * @throws ProcessException
	 */
	public List<CcsAcctO> getTmAccountOTOacctNbr(Long acctNbr) throws ProcessException {
		CheckUtil.checkAccount(acctNbr);
		JPAQuery query = new JPAQuery(em);
		QCcsAcctO qTmAccountO = QCcsAcctO.ccsAcctO;
		List<CcsAcctO> tmAccountOList = query
				.from(qTmAccountO)
				.where(qTmAccountO.acctNbr.eq(acctNbr).and(
						qTmAccountO.org.eq(OrganizationContextHolder.getCurrentOrg()))).list(qTmAccountO);
		if(tmAccountOList.isEmpty()) {
			throw new ProcessException("账户号[" + acctNbr + "]查询不到对应的账户信息");
		}
		return tmAccountOList;
	}

	/**
	 * 根据账户号获取账户列表
	 * 
	 * @param acctNbr
	 * @return
	 * @throws ProcessException
	 */
	public List<CcsAcct> getTmAccountTOacctNbr(Long acctNbr) throws ProcessException {
		CheckUtil.checkAccount(acctNbr);
		JPAQuery query = new JPAQuery(em);
		List<CcsAcct> tmAccountList = query
				.from(qTmAccount)
				.where(qTmAccount.acctNbr.eq(acctNbr).and(qTmAccount.org.eq(OrganizationContextHolder.getCurrentOrg())))
				.list(qTmAccount);
		if(tmAccountList.isEmpty()) {
			throw new ProcessException("账户号[" + acctNbr + "]查询不到对应的账户信息");
		}
		return tmAccountList;
	}

	/**
	 * 根据账户类型，账户号信息返回TMAccountO数据
	 * 
	 * @param acctType TODO
	 * @param acctNbr TODO
	 * @param tmAccount
	 * 
	 * @return
	 * @throws ProcessException
	 */
	public CcsAcctO getTmAccountOToacctNbr(String acctType, Long acctNbr) throws ProcessException {
		QCcsAcctO qTmAccounto = QCcsAcctO.ccsAcctO;
		CcsAcctO tmAccountO = rTmAccountO.findOne(qTmAccounto.acctType.eq(AccountType.valueOf(acctType)).and(
				qTmAccounto.acctNbr.eq(acctNbr).and(qTmAccounto.org.eq(OrganizationContextHolder.getCurrentOrg()))));
		CheckUtil.rejectNull(tmAccountO, "查询不到对应的账户信息表");
		return tmAccountO;
	}

	/**
	 * 根据账户号获取账户信息
	 * 
	 * @param acctType TODO
	 * @param acctNbr TODO
	 * @param acctid
	 * 
	 * @return
	 * @throws ProcessException
	 */
	public CcsAcct getTmAccountTOacctNbr(String acctType, Long acctNbr) throws ProcessException {
		CheckUtil.checkAccount(acctNbr);
		CcsAcct tmacct = rTmAccount.findOne(qTmAccount.acctNbr.eq(acctNbr).and(
				qTmAccount.org.eq(OrganizationContextHolder.getCurrentOrg()).and(
						qTmAccount.acctType.eq(AccountType.valueOf(acctType)))));
		CheckUtil.rejectNull(tmacct, "查询不到对应的账户信息");
		return tmacct;
	}

	/**
	 * 根据账号获取该账户号下所有的卡片
	 * 
	 * @param acctNbr
	 * @return
	 * @throws ProcessException
	 */
	public List<CcsCard> getTmCardListTOAccount(Long acctNbr) throws ProcessException {
		CheckUtil.checkAccount(acctNbr);
		JPAQuery query = new JPAQuery(em);
		List<CcsCard> tmCardList = query.from(qTmCard)
				.where(qTmCard.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qTmCard.acctNbr.eq(acctNbr)))
				.list(qTmCard);
		return tmCardList;
	}

	/**
	 * 根据logiccardNbr获取卡片信息
	 * 
	 * @param logiccardNbr
	 * @return
	 * @throws ProcessException
	 */
	public CcsCard getTmCardToLogiccardNbr(String logiccardNbr) throws ProcessException {
		CcsCard tmCard = rTmCard.findOne(qTmCard.org.eq(OrganizationContextHolder.getCurrentOrg()).and(
				qTmCard.logicCardNbr.eq(logiccardNbr)));
		CheckUtil.rejectNull(tmCard, "查询不到卡片信息");
		return tmCard;
	}

	/**
	 * 根据logiccardNbr获取卡片信息
	 * 
	 * @param logiccardNbr
	 * @return
	 * @throws ProcessException
	 */
	public CcsCardO getTmCardOToLogiccardNbr(String logiccardNbr) throws ProcessException {
		QCcsCardO qTmCardO = QCcsCardO.ccsCardO;
		CcsCardO tmCardO = rTmCardO.findOne(qTmCardO.org.eq(OrganizationContextHolder.getCurrentOrg()).and(
				qTmCardO.logicCardNbr.eq(logiccardNbr)));
		CheckUtil.rejectNull(tmCardO, "查询不到卡片信息");
		return tmCardO;
	}

	/**
	 * 根据证件号码，证件类型查询账户列表
	 * 
	 * @param idType
	 * @param idNo
	 * @return
	 * @throws ProcessException
	 */
	public List<CcsAcct> getTmAccountTOIDtypeIdNo(String idType, String idNo) throws ProcessException {
		CheckUtil.checkId(idType, idNo);
		JPAQuery query = new JPAQuery(em);
		BooleanExpression processExcepssion = qTmCustomer.org.eq(OrganizationContextHolder.getCurrentOrg())
				.and(qTmCustomer.custId.eq(qTmAccount.custId))
				.and(qTmCustomer.idType.eq(IdType.valueOf(idType)).and(qTmCustomer.idNo.eq(idNo)));
		List<CcsAcct> tmAccountList = query.from(qTmCustomer, qTmAccount).where(processExcepssion).list(qTmAccount);
		if(tmAccountList.isEmpty()) {
			throw new ProcessException("证件类型[" + idType + "],证件号码[" + idNo + "]查询不到对应的信息");
		}
		return tmAccountList;
	}

	/**
	 * 根据证件号码，证件类型查询账户列表
	 * 
	 * @param idType
	 * @param idNo
	 * @return
	 * @throws ProcessException
	 */
	public List<CcsAcctO> getTmAccountOTOIDtypeIdNo(String idType, String idNo) throws ProcessException {
		QCcsAcctO qTmAccountO = QCcsAcctO.ccsAcctO;
		JPAQuery query = new JPAQuery(em);
		BooleanExpression processExcepssion = qTmCustomer.org.eq(OrganizationContextHolder.getCurrentOrg())
				.and(qTmCustomer.custId.eq(qTmAccountO.custId))
				.and(qTmCustomer.idType.eq(IdType.valueOf(idType)).and(qTmCustomer.idNo.eq(idNo)));
		List<CcsAcctO> tmAccountOList = query.from(qTmCustomer, qTmAccountO).where(processExcepssion).list(qTmAccountO);
		if(tmAccountOList.isEmpty()) {
			throw new ProcessException("证件类型[" + idType + "],证件号码[" + idNo + "]查询不到对应的信息");
		}
		return tmAccountOList;
	}

	/**
	 * 获取积分注册表信息 getTmPointRegByacctNbrAcctType
	 * 
	 * @param tmAccount
	 * @param acctType
	 * @return List<CcsPointsReg>
	 * @exception
	 * @since 1.0.0
	 */
	public List<CcsPointsReg> getTmPointRegByacctNbrAcctType(CcsAcct tmAccount, PointAdjustIndicator ... adjustInd) {
		JPAQuery query = new JPAQuery(em);
		return query
				.from(qTmPointReg)
				.where(qTmPointReg.acctNbr.eq(tmAccount.getAcctNbr())
						.and(qTmPointReg.acctType.eq(tmAccount.getAcctType())).and(qTmPointReg.adjInd.in(adjustInd)))
				.list(qTmPointReg);
	}

	/**
	 * 根据账户编号获取所有账户TmAccount列表
	 * 
	 * @param acctNbr
	 * @return
	 * @throws ProcessException
	 */
	public List<CcsAcct> getTmAccountListByacctNbr(Long acctNbr) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		List<CcsAcct> tmAccountList = query
				.from(qTmAccount)
				.where(qTmAccount.acctNbr.eq(acctNbr).and(qTmAccount.org.eq(OrganizationContextHolder.getCurrentOrg())))
				.list(qTmAccount);
		return tmAccountList;
	}
}
