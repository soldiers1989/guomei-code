package com.sunline.ccs.facility;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysema.query.jpa.hibernate.HibernateSubQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCard;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardLmMapping;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardO;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardUsage;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.infrastructure.shared.model.CcsCardUsage;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardO;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardUsage;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.QCcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.QCcsPlan;
import com.sunline.ccs.infrastructure.shared.model.QCcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsStatement;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.PlanType;
import com.sunline.ppy.dictionary.enums.PointAdjustIndicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
* @author fanghj
 * @version 创建时间：2012-7-30 下午6:48:43
 * 
 */

@Service
public class CustAcctCardFacility {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private RCcsCustomer rCustomer;
	@Autowired
	private RCcsAcct rAcct;
	@Autowired
	private RCcsAcctO rAcctO;
	@Autowired
	private RCcsCard rCard;
	@Autowired
	private RCcsCardO rCardO;
	@Autowired
	private RCcsCustomerCrlmt rCustomerCrLmt;
	@Autowired
	private RCcsCardLmMapping rCcsCardLmMapping;
	@Autowired
	private RCcsCardUsage rCcsCardStmt;
	@PersistenceContext
	private EntityManager em;

	private QCcsCustomer qCustomer = QCcsCustomer.ccsCustomer;
	private QCcsCustomerCrlmt qCustomerCrLmt = QCcsCustomerCrlmt.ccsCustomerCrlmt;
	private QCcsLinkman qCcsLinkman = QCcsLinkman.ccsLinkman;
	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	private QCcsAcctO qAcctO = QCcsAcctO.ccsAcctO;
	private QCcsCard qCard = QCcsCard.ccsCard;
	private QCcsCardO qCardO = QCcsCardO.ccsCardO;
	private QCcsCardLmMapping qCardLmMaping = QCcsCardLmMapping.ccsCardLmMapping;
	private QCcsCardUsage qCardUsage = QCcsCardUsage.ccsCardUsage;
	private QCcsStatement qStatement = QCcsStatement.ccsStatement;
	private QCcsPlan qPlan = QCcsPlan.ccsPlan;
	private QCcsPointsReg qTmPointReg = QCcsPointsReg.ccsPointsReg;
	
	
	/**
	 * CcsCardLmMapping getCcsCardLmMappingBycardNbr(这里用一句话描述这个方法的作用)
	 * 
	 * @param cardNbr
	 * @return CcsCardLmMapping
	 * @exception
	 * @since 1.0.0
	 */
	public CcsCardLmMapping getCardLmMappingByCardNbr(String cardNbr) {
		return rCcsCardLmMapping.findOne(qCardLmMaping.cardNbr.eq(cardNbr).and(qCardLmMaping.org.eq(OrganizationContextHolder.getCurrentOrg())));
	}

	/**
	 * 根据logicCardNbr返回CcsCardLmMapping对象列表
	 * 
	 * @param logicCardNbr
	 * @return
	 */
	public List<CcsCardLmMapping> getCardLmMappingByLogicCardNbr(String logicCardNbr) {
		JPAQuery query = new JPAQuery(em);
		List<CcsCardLmMapping> CcsCardLmMappingList = query.from(qCardLmMaping)
				.where(qCardLmMaping.logicCardNbr.eq(logicCardNbr).and(qCardLmMaping.org.eq(OrganizationContextHolder.getCurrentOrg()))).list(qCardLmMaping);
		return CcsCardLmMappingList;
	}

	/**
	 * 根据卡号查询客户CcsCustomer信息
	 * 
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public CcsCustomer getCustomerByCardNbr(String cardNbr) {
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCardLmMaping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCardLmMaping.cardNbr.eq(cardNbr))
				.and(qCardLmMaping.logicCardNbr.eq(qCard.logicCardNbr)).and(qCard.custId.eq(qCustomer.custId));
		CcsCustomer CcsCustomer = query.from(qCardLmMaping, qCard, qCustomer).where(booleanExpression).singleResult(qCustomer);
		return CcsCustomer;
	}

	/**
	 * 根据证件查询客户CcsCustomer信息
	 * 
	 * @param idNo
	 * @param idType
	 * @return
	 * @throws ProcessException
	 */
	public CcsCustomer getCustomerById(String idNo, IdType idType) throws ProcessException {
		List<CcsCustomer> list = rCustomer.findByIdNoAndOrgAndIdType(idNo, OrganizationContextHolder.getCurrentOrg(), idType);
		if (list != null && list.size() > 0) {
			return rCustomer.findByIdNoAndOrgAndIdType(idNo, OrganizationContextHolder.getCurrentOrg(), idType).get(0);
		}
		return null;
	}

	/**
	 * 根据custLmtId查询客户额度信息
	 * 
	 * @param custLmtId
	 * @return
	 */
	public CcsCustomerCrlmt getCustomerCrLmtByCustLmtId(Long custLmtId) {
		CcsCustomerCrlmt customerCrLmt = rCustomerCrLmt.findOne(qCustomerCrLmt.custLmtId.eq(custLmtId));
		return customerCrLmt;
	}

	/**
	 * 根据客户号获取账户列表信息
	 * 
	 * @param custId
	 * @return
	 */
	public List<CcsAcct> getAcctByCustId(Long custId) {
		JPAQuery query = new JPAQuery(em);
		BooleanExpression bp = qAcct.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qAcct.custId.eq(custId));
		return query.from(qAcct).where(bp).list(qAcct);
	}

	/**
	 * 根据客户号获取账户列表信息 CcsAccto
	 * 
	 * @param custId
	 * @return
	 */
	public List<CcsAcctO> getAcctOByCustId(Long custId) {
		JPAQuery query = new JPAQuery(em);
		BooleanExpression bp = qAcctO.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qAcctO.custId.eq(custId));
		return query.from(qAcctO).where(bp).list(qAcctO);
	}

	/**
	 * 根据卡号获取账户列表
	 * 
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public List<CcsAcct> getAcctByCardNbr(String cardNbr) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		List<CcsAcct> CcsAcctlist = query
				.from(qCardLmMaping, qCard, qAcct)
				.where(qCardLmMaping.logicCardNbr.eq(qCard.logicCardNbr).and(
						qCardLmMaping.org.eq(qCard.org).and(qCardLmMaping.cardNbr.eq(cardNbr))
								.and(qCard.acctNbr.eq(qAcct.acctNbr).and(qCardLmMaping.org.eq(OrganizationContextHolder.getCurrentOrg()))))).list(qAcct);
		if (CcsAcctlist.isEmpty()) {
			throw new ProcessException("卡号[" + cardNbr + "]查询不到对应的账户信息");
		}
		return CcsAcctlist;
	}

	/**
	 * 根据证件类型，证件号码返回卡片列表 如果证件类型，证件号码 为主卡，则返回主卡以及对应的附卡信息
	 * 如果证件类型，证件号码为附卡，则返回附卡的卡片信息；
	 * 
	 * @param id_No
	 * @param id_Type
	 * @return
	 * @throws ProcessException
	 *             List<CcsCard>
	 * @exception
	 * @since 1.0.0
	 */
	public List<CcsCard> getCardById(String id_No, IdType id_Type) throws ProcessException {
		List<CcsCard> returnCcsCardList = null;
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExcepression = qCustomer.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCustomer.idNo.eq(id_No)).and(qCustomer.idType.eq(id_Type))
				.and(qCustomer.custId.eq(qCard.custId));
		List<CcsCard> CcsCardList = query.from(qCustomer, qCard).where(booleanExcepression).list(qCard);
		if (!CcsCardList.isEmpty()) {
			returnCcsCardList = new ArrayList<CcsCard>();
			for (CcsCard CcsCard : CcsCardList) {
				returnCcsCardList.add(CcsCard);
				if (CcsCard.getBscSuppInd() == BscSuppIndicator.B) {
					// 如果是主卡,查询主卡对应的附卡
					List<CcsCard> sCcsCard = getSuppCardBylogicCardNbr(CcsCard.getLogicCardNbr());
					returnCcsCardList.addAll(sCcsCard);
				}
			}
		}
		return returnCcsCardList;
	}

	/**
	 * 根据卡号获取账户列表，其中账户列表忽略本外币
	 * 
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public List<Long> getDistinctAcctNbrListByCardNbr(String cardNbr) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		List<Long> acctNbrList = query
				.from(qCardLmMaping, qCard, qAcct)
				.where(qCardLmMaping.logicCardNbr.eq(qCard.logicCardNbr).and(
						qCardLmMaping.org.eq(qCard.org).and(qCardLmMaping.cardNbr.eq(cardNbr))
								.and(qCard.acctNbr.eq(qAcct.acctNbr).and(qCardLmMaping.org.eq(OrganizationContextHolder.getCurrentOrg()))))).distinct().list(qAcct.acctNbr);
		if (acctNbrList.isEmpty()) {
			log.error("卡号[" + cardNbr + "]查询不到对应的账户信息");
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
	public CcsAcct getAcctByCardNbr(String cardNbr, AccountType acctType) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		CcsAcct CcsAcct = query
				.from(qCardLmMaping, qCard, qAcct)
				.where(qCardLmMaping.logicCardNbr.eq(qCard.logicCardNbr).and(
						qCardLmMaping.org.eq(qCard.org).and(qCardLmMaping.cardNbr.eq(cardNbr))
								.and(qCard.acctNbr.eq(qAcct.acctNbr).and(qCardLmMaping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qAcct.acctType.eq(acctType))))))
				.singleResult(qAcct);
		return CcsAcct;
	}

	/**
	 * 根据卡号，币种类型获取账户信息
	 * 
	 * @param cardNbr
	 * @param currency
	 * @return
	 * @throws ProcessException
	 */
	public CcsAcct getAcctByCardNbrCurrency(String cardNbr, String currency) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		CcsAcct CcsAcct = query
				.from(qCardLmMaping, qCard, qAcct)
				.where(qCardLmMaping.logicCardNbr.eq(qCard.logicCardNbr).and(
						qCardLmMaping.org.eq(qCard.org).and(qCardLmMaping.cardNbr.eq(cardNbr))
								.and(qCard.acctNbr.eq(qAcct.acctNbr).and(qCardLmMaping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qAcct.currency.eq(currency))))))
				.singleResult(qAcct);
		return CcsAcct;
	}

	/**
	 * 根据卡号，账户类型获取账户CcsAcctO 信息
	 * 
	 * @param cardNbr
	 * @param acctType
	 * @return
	 * @throws ProcessException
	 */
	public CcsAcctO getAcctOByCardNbr(String cardNbr, AccountType acctType) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		CcsAcctO CcsAcctO = query
				.from(qCardLmMaping, qCard, qAcctO)
				.where(qCardLmMaping.logicCardNbr.eq(qCard.logicCardNbr)
						.and(qCardLmMaping.org.eq(qCard.org))
						.and(qCardLmMaping.cardNbr.eq(cardNbr))
						.and(qCard.acctNbr.eq(qAcctO.acctNbr))
						.and(qCardLmMaping.org.eq(OrganizationContextHolder.getCurrentOrg()))
						.and(qAcctO.acctType.eq(acctType)))
				.singleResult(qAcctO);
		return CcsAcctO;
	}

	/**
	 * 根据卡号获取CcsAcctO信息
	 * 
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public List<CcsAcctO> getAcctOByCardNbr(String cardNbr) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		List<CcsAcctO> CcsCardOLists = query
				.from(qCardLmMaping, qCard, qAcctO)
				.where(qCardLmMaping.logicCardNbr.eq(qCard.logicCardNbr).and(
						qCardLmMaping.org.eq(qCard.org).and(qCardLmMaping.cardNbr.eq(cardNbr))
								.and(qCard.acctNbr.eq(qAcctO.acctNbr).and(qCardLmMaping.org.eq(OrganizationContextHolder.getCurrentOrg()))))).list(qAcctO);
		if (CcsCardOLists.isEmpty()) {
			throw new ProcessException("卡号[" + cardNbr + "]查询不到对应的账户信息");
		}
		return CcsCardOLists;

	}

	/**
	 * 根据卡号获取CcsCardO数据
	 * 
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public CcsCardO getCardOByCardNbr(String cardNbr) {
		JPAQuery query = new JPAQuery(em);
		CcsCardO cardO = query
				.from(qCardLmMaping, qCardO)
				.where(qCardLmMaping.logicCardNbr.eq(qCardO.logicCardNbr).and(
						qCardLmMaping.org.eq(qCardO.org).and(qCardLmMaping.cardNbr.eq(cardNbr).and(qCardLmMaping.org.eq(OrganizationContextHolder.getCurrentOrg()))))).singleResult(qCardO);
		return cardO;
	}

	/**
	 * 根据卡号获取CcsCard数据
	 * 
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public CcsCard getCardByCardNbr(String cardNbr) {
		JPAQuery query = new JPAQuery(em);
		CcsCard CcsCard = query
				.from(qCardLmMaping, qCard)
				.where(qCardLmMaping.logicCardNbr.eq(qCard.logicCardNbr).and(
						qCardLmMaping.org.eq(qCard.org).and(qCardLmMaping.cardNbr.eq(cardNbr).and(qCardLmMaping.org.eq(OrganizationContextHolder.getCurrentOrg()))))).singleResult(qCard);

		return CcsCard;
	}

	/**
	 * 根据卡号获取CcsCardStst数据
	 * 
	 * @param cardNbr
	 * @return
	 * @throws ProcessException
	 */
	public CcsCardUsage getCardUsageByCardNbr(String cardNbr) throws ProcessException {
		JPAQuery query = new JPAQuery(em);

		CcsCardUsage cardUsage = query
				.from(qCardLmMaping, qCardUsage)
				.where(qCardLmMaping.logicCardNbr.eq(qCardUsage.logicCardNbr).and(
						qCardLmMaping.org.eq(qCardUsage.org).and(qCardLmMaping.cardNbr.eq(cardNbr).and(qCardLmMaping.org.eq(OrganizationContextHolder.getCurrentOrg())))))
				.singleResult(qCardUsage);

		return cardUsage;
	}

	/**
	 * 根据logicCardNbr获取CcsCardStst数据
	 * 
	 * @param logicCardNbr
	 * @return
	 * @throws ProcessException
	 */
	public CcsCardUsage getCardUsageBylogicCardNbr(String logicCardNbr) throws ProcessException {
		CcsCardUsage cardUsage = rCcsCardStmt.findOne(qCardUsage.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCardUsage.logicCardNbr.eq(logicCardNbr)));
		return cardUsage;
	}

	/**
	 * 根据逻辑卡获取所有的附卡信息，不包含主卡信息
	 * 
	 * @param cardNbr
	 *            主卡卡号
	 * @return
	 */
	public List<CcsCard> getSuppCardBylogicCardNbr(String logicCardNbr) {
		JPAQuery query = new JPAQuery(em);
		List<CcsCard> CcsCardList = query.from(qCard)
				.where(qCard.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCard.cardBasicNbr.eq(logicCardNbr).and(qCard.bscSuppInd.eq(BscSuppIndicator.S)))).list(qCard);
		return CcsCardList;
	}

	/**
	 * 根据卡号获取所有的附卡信息
	 * 
	 * @param cardNbr
	 *            主卡卡号
	 * @return
	 */
	public List<CcsCard> getSuppCardByCardNbr(String cardNbr) {
		JPAQuery query = new JPAQuery(em);
		List<CcsCard> CcsCardList = query
				.from(qCardLmMaping, qCard)
				.where(qCardLmMaping.logicCardNbr.eq(qCard.cardBasicNbr).and(
						qCardLmMaping.org.eq(qCard.org).and(qCard.bscSuppInd.eq(BscSuppIndicator.S))
								.and(qCardLmMaping.cardNbr.eq(cardNbr).and(qCardLmMaping.org.eq(OrganizationContextHolder.getCurrentOrg()))))).list(qCard);
		return CcsCardList;
	}

	/**
	 * 根据卡号获取所有的附卡信息
	 * 
	 * @param cardNbr
	 * @return
	 */
	public List<CcsCardO> getSuppCardOByCardNbr(String cardNbr) {
		JPAQuery query = new JPAQuery(em);
		List<CcsCardO> CcsCardOList = query
				.from(qCardLmMaping, qCardO)
				.where(qCardLmMaping.logicCardNbr.eq(qCardO.cardBasicNbr).and(qCardO.bscSuppInd.eq(BscSuppIndicator.S))
						.and(qCardLmMaping.org.eq(qCardO.org).and(qCardLmMaping.cardNbr.eq(cardNbr).and(qCardLmMaping.org.eq(OrganizationContextHolder.getCurrentOrg()))))).list(qCardO);
		return CcsCardOList;
	}

	/**
	 * 根据账户号获取账户列表
	 * 
	 * @param acctNbr
	 * @return
	 * @throws ProcessException
	 */
	public List<CcsAcctO> getAcctOByAcctNbr(Long acctNbr) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		List<CcsAcctO> CcsAcctOList = query.from(qAcctO).where(qAcctO.acctNbr.eq(acctNbr).and(qAcctO.org.eq(OrganizationContextHolder.getCurrentOrg()))).list(qAcctO);
		if (CcsAcctOList.isEmpty()) {
			throw new ProcessException("账户号[" + acctNbr + "]查询不到对应的账户信息");
		}
		return CcsAcctOList;
	}

	/**
	 * 根据账户号获取账户列表
	 * 
	 * @param acctNbr
	 * @return
	 * @throws ProcessException
	 */
	public List<CcsAcct> getAcctByAcctNbr(Long acctNbr) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		List<CcsAcct> CcsAcctList = query.from(qAcct).where(qAcct.acctNbr.eq(acctNbr).and(qAcct.org.eq(OrganizationContextHolder.getCurrentOrg()))).list(qAcct);
		if (CcsAcctList.isEmpty()) {
			throw new ProcessException("账户号[" + acctNbr + "]查询不到对应的账户信息");
		}
		return CcsAcctList;
	}

	/**
	 * 根据账户类型，账户号信息返回CcsAcctO数据
	 * 
	 * @param acctType
	 *            TODO
	 * @param acctNbr
	 *            TODO
	 * @param CcsAcct
	 * 
	 * @return
	 * @throws ProcessException
	 */
	public CcsAcctO getAcctOByAcctNbr(AccountType acctType, Long acctNbr) {
		CcsAcctO CcsAcctO = rAcctO.findOne(qAcctO.acctType.eq(acctType).and(qAcctO.acctNbr.eq(acctNbr).and(qAcctO.org.eq(OrganizationContextHolder.getCurrentOrg()))));
		return CcsAcctO;
	}

	/**
	 * 根据账户号获取账户信息
	 * 
	 * @param acctType
	 *            TODO
	 * @param acctNbr
	 *            TODO
	 * @param acctid
	 * 
	 * @return
	 * @throws ProcessException
	 */
	public CcsAcct getAcctByAcctNbr(AccountType acctType, Long acctNbr) throws ProcessException {
		CcsAcct tmacct = rAcct.findOne(qAcct.acctNbr.eq(acctNbr).and(qAcct.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qAcct.acctType.eq(acctType))));
		return tmacct;
	}

	/**
	 * 根据账户号,币种代码获取账户信息
	 * 
	 * @param acctType
	 *            TODO
	 * @param acctNbr
	 *            TODO
	 * @param acctid
	 * 
	 * @return
	 * @throws ProcessException
	 */
	public CcsAcct getAcctByAcctNbrCurrency(String CurrCd, Long acctNbr) throws ProcessException {
		CcsAcct tmacct = rAcct.findOne(qAcct.acctNbr.eq(acctNbr).and(qAcct.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qAcct.currency.eq(CurrCd))));
		return tmacct;
	}

	/**
	 * 根据账号获取该账户号下所有的卡片
	 * 
	 * @param acctNbr
	 * @return
	 * @throws ProcessException
	 */
	public List<CcsCard> getCardListByAcctNbr(Long acctNbr) {
		JPAQuery query = new JPAQuery(em);
		List<CcsCard> CcsCardList = query.from(qCard).where(qCard.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCard.acctNbr.eq(acctNbr))).list(qCard);
		return CcsCardList;
	}

	/**
	 * 根据logicCardNbr获取卡片信息
	 * 
	 * @param logicCardNbr
	 * @return
	 * @throws ProcessException
	 */
	public CcsCard getCardByLogicCardNbr(String logicCardNbr) {
		CcsCard card = rCard.findOne(qCard.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCard.logicCardNbr.eq(logicCardNbr)));

		return card;

	}

	/**
	 * 根据logicCardNbr获取卡片信息
	 * 
	 * @param logicCardNbr
	 * @return
	 * @throws ProcessException
	 */
	public CcsCardO getCardOByLogicCardNbr(String logicCardNbr) {
		QCcsCardO qCcsCardO = QCcsCardO.ccsCardO;
		CcsCardO cardO = rCardO.findOne(qCcsCardO.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardO.logicCardNbr.eq(logicCardNbr)));
		return cardO;
	}

	/**
	 * 根据证件号码，证件类型查询账户列表
	 * 
	 * @param idType
	 *            证件号码
	 * @param idNo
	 *            证件类型
	 * @return
	 * @throws ProcessException
	 */
	public List<CcsAcct> getAcctByIdTypeIdNo(String idType, String idNo) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		BooleanExpression processExcepssion = qCustomer.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCustomer.custId.eq(qCard.custId))
				.and(qCustomer.idType.eq(IdType.valueOf(idType)).and(qCustomer.idNo.eq(idNo)));
		List<CcsAcct> CcsAcctList = query.from(qAcct).where(qAcct.acctNbr.in(new HibernateSubQuery().from(qCustomer, qCard).where(processExcepssion).list(qCard.acctNbr)))
				.list(qAcct);
		if (CcsAcctList.isEmpty()) {
			log.error("证件类型[" + idType + "],证件号码[" + idNo + "]查询不到对应的账户信息");
			throw new ProcessException("证件类型[" + idType + "],证件号码[" + idNo + "]查询不到信息");
		}
		return CcsAcctList;
	}

	/**
	 * 根据证件号码，证件类型查询账户列表
	 * 
	 * @param idType
	 * @param idNo
	 * @return
	 * @throws ProcessException
	 */
	public List<CcsAcctO> getAcctOByIdTypeIdNo(String idType, String idNo) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		BooleanExpression processExcepssion = qCustomer.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCustomer.custId.eq(qAcctO.custId))
				.and(qCustomer.idType.eq(IdType.valueOf(idType)).and(qCustomer.idNo.eq(idNo)));
		List<CcsAcctO> CcsAcctOList = query.from(qCustomer, qAcctO).where(processExcepssion).list(qAcctO);
		if (CcsAcctOList.isEmpty()) {
			throw new ProcessException("证件类型[" + idType + "],证件号码[" + idNo + "]查询不到对应的账户信息");
		}
		return CcsAcctOList;
	}

	/**
	 * 根据客户号，客户类型查询tmplan
	 * 
	 * @param acctNbr
	 * @param acctType
	 * @param planType
	 * @return List<TmPlan>
	 * @exception
	 * @since 1.0.0
	 */
	public List<CcsPlan> getPlanByAcctNbrAcctType(Long acctNbr, AccountType acctType, PlanType planType) {
		JPAQuery query = new JPAQuery(em);
		return query.from(qPlan)
				.where(qPlan.acctNbr.eq(acctNbr).and(qPlan.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qPlan.acctType.eq(acctType)).and(qPlan.planType.eq(planType)))).list(qPlan);
	}

	/**
	 * 获取信用账户上信用计划的非延迟利息
	 * @param acctNbr
	 * @param acctType
	 * @return
	 */
	public BigDecimal getPlan_NODEFBNP_INT_ACRUByAcctNbr(Long acctNbr, AccountType acctType) {
		JPAQuery query = new JPAQuery(em);
		return query.from(qPlan).where(qPlan.acctNbr.eq(acctNbr).and(qPlan.acctType.eq(acctType)).and(qPlan.org.eq(OrganizationContextHolder.getCurrentOrg())))
				.singleResult(qPlan.nodefbnpIntAcru.sum());
	}
	
	
	public BigDecimal getPlan_BEG_DEFBNP_INT_ACRUByAcctNbr(Long acctNbr, AccountType acctType) {
		JPAQuery query = new JPAQuery(em);
		return query.from(qPlan).where(qPlan.acctNbr.eq(acctNbr).and(qPlan.acctType.eq(acctType)).and(qPlan.org.eq(OrganizationContextHolder.getCurrentOrg())))
				.singleResult(qPlan.begDefbnpIntAcru.sum());
	}

	/**
	 * 计算消费计划往期本金之和 totalTmPlan_PastPrincipal
	 * 
	 * @param tmPlanList
	 * @return BigDecimal
	 * @exception
	 * @since 1.0.0
	 */
	public BigDecimal countPlan_PastPrincipal(Long acctNbr, AccountType acctType, PlanType planType) {
		BigDecimal pastPrin = BigDecimal.ZERO;
		for (CcsPlan plan : getPlanByAcctNbrAcctType(acctNbr, acctType, planType)) {
			pastPrin = pastPrin.add(plan.getPastPrincipal());
		}
		return pastPrin;
	}

	/**
	 * 获取积分注册表信息 getTmPointRegByacctNbrAcctType
	 * 
	 * @param CcsAcct
	 * @param acctType
	 * @return List<TmPointReg>
	 * @exception
	 * @since 1.0.0
	 */
	public List<CcsPointsReg> getPointsRegByacctNbrAcctType(CcsAcct CcsAcct, PointAdjustIndicator... adjustInd) {
		JPAQuery query = new JPAQuery(em);
		return query.from(qTmPointReg).where(qTmPointReg.acctNbr.eq(CcsAcct.getAcctNbr()).and(qTmPointReg.adjInd.in(adjustInd))).list(qTmPointReg);
	}

	/**
	 * 根据stmtDate查询当月账单<br>
	 * start <= stmtDate < end
	 * 
	 * @param start
	 *            当月1号
	 * @param end
	 *            下个月1号
	 * @return
	 * @throws ProcessException
	 */
	public List<CcsStatement> getCcsStatementByStmtDate(Long acctNbr, Date start, Date end) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		List<CcsStatement> stmtList = query.from(qStatement)
				.where(qStatement.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qStatement.acctNbr.eq(acctNbr)).and(qStatement.stmtDate.goe(start)).and(qStatement.stmtDate.lt(end)))
				.orderBy(qStatement.stmtDate.desc()).list(qStatement);
		if (stmtList.isEmpty()) {
			throw new ProcessException("账户号[" + acctNbr + "]查询不到对应的账户信息");
		}
		return stmtList;
	}

	/**
	 * 根据客户号查询联系人
	 * 
	 * @param custId
	 * @return
	 * @throws ProcessException
	 */
	public List<CcsLinkman> getCcsLinkmanByCustId(Long custId) throws ProcessException {
		JPAQuery query = new JPAQuery(em);
		List<CcsLinkman> linkmanList = query.from(qCcsLinkman).where(qCcsLinkman.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsLinkman.custId.eq(custId))).list(qCcsLinkman);
		return linkmanList;
	}

	/**
	 * 根据custLimitId查询CcsAcctO
	 * 
	 * @param custLmtId
	 * @return
	 */
	public List<CcsAcctO> getAcctOListByCustLmtId(Long custLmtId) {
		JPAQuery query = new JPAQuery(em);
		return query.from(qAcctO).where(qAcctO.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qAcctO.custLmtId.eq(custLmtId))).list(qAcctO);
	}

}
