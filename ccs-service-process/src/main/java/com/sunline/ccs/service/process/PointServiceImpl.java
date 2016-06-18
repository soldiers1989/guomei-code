package com.sunline.ccs.service.process;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ppy.api.CcPointService;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.PointAdjustIndicator;
import com.sunline.ppy.dictionary.enums.PostTxnType;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.facility.Card2ProdctAcctFacility;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnUnstatement;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsPointsReg;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.util.CPSServProBusUtil;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.service.QueryRequest;
import com.sunline.ark.support.service.QueryResult;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;


/** 
 * @see 类名：PointServiceImpl
 * @see 描述：积分服务类接口
 *
 * @see 创建日期：   2015年6月24日 下午2:48:17
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class PointServiceImpl implements CcPointService {
	// 调整方向 A增加 S减少
	private static final String NF5201_ADJUSTTYPE_A = "A";
	private static final String NF5201_ADJUSTTYPE_S = "S";

	QCcsCard qCcsCard = QCcsCard.ccsCard;
	QCcsCardLmMapping qCcsCardLmMapping = QCcsCardLmMapping.ccsCardLmMapping;
	QCcsTxnUnstatement qTmtxnUnstmt = QCcsTxnUnstatement.ccsTxnUnstatement;
	QCcsTxnHst qCcsTxnHst = QCcsTxnHst.ccsTxnHst;
    QCcsPointsReg qCcsPointsReg = QCcsPointsReg.ccsPointsReg;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private RCcsPointsReg rCcsPointsReg;

	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	
	@Autowired
	private Card2ProdctAcctFacility cardNbrTOProdctAcctFacility;
	
	

	@PersistenceContext
	private EntityManager em;

	@Override
	@Transactional
	public QueryResult<Map<String, Serializable>> NF5102(QueryRequest queryRequest, String cardNbr, Date startDate, Date endDate) throws ProcessException {
		log.info("NF5102:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "],开始时间[" + startDate + "],结束时间[" + endDate + "]");
		JPAQuery query = new JPAQuery(em);
		BooleanExpression be = qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardLmMapping.cardNbr.eq(cardNbr)).and(qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr)).and(qCcsCard.acctNbr.eq(qCcsTxnHst.acctNbr))
				.and(qCcsTxnHst.stmtDate.between(startDate, endDate)).and(qCcsTxnHst.points.gt(0));
		List<CcsTxnHst> tCcsTxnHstList = query.from(qCcsCard, qCcsCardLmMapping, qCcsTxnHst).where(be).offset(queryRequest.getFirstRow()).limit(queryRequest.getLastRow()).list(qCcsTxnHst);
		JPAQuery query1 = new JPAQuery(em);
		int totalRow = (int) query1.from(qCcsCard, qCcsCardLmMapping, qCcsTxnHst).where(be).offset(queryRequest.getFirstRow()).count();
		List<Map<String, Serializable>> list = new ArrayList<Map<String, Serializable>>();
		for (CcsTxnHst tmTxnHst : tCcsTxnHstList) {
			list.add(tmTxnHst.convertToMap());
		}
		return CPSServProBusUtil.genQueryResultTOListMap(queryRequest, totalRow, list);
	}

	@Override
	@Transactional
	public void NF5201(String cardNbr, String adjustType, BigDecimal points) throws ProcessException {
		log.info("NF5201:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "],调整方向[" + adjustType + "],积分值[" + points + "]");
		boolean flag =true;//单币种，双币种账户标志； true 为双币种 false 单币种账户
		//获取产品信息；
		ProductCredit productCredit = cardNbrTOProdctAcctFacility.CardNoToProductCr(cardNbr);
		//判断是否为单币种账户；
		if(productCredit != null){
			//如果为外币账户参数id为空，说明该账户为单币种账户
			if(productCredit.dualAccountAttributeId == null){
				flag = false;
			}
		}
		CcsAcct CcsAcct = queryFacility.getAcctByCardNbr(cardNbr, cardNbrTOProdctAcctFacility.acct_attribute(productCredit).accountType);
		if (adjustType.equals(NF5201_ADJUSTTYPE_A)) {
			addPoint(cardNbr, CcsAcct, points, PointAdjustIndicator.I);
		} else if (adjustType.equals(NF5201_ADJUSTTYPE_S)) {
			if(flag){
				// 双币种积分调整过程，先调整外币积分，在调整外币积分，在调整本币积分
				CcsAcct dualCcsAcct = queryFacility.getAcctByCardNbr(cardNbr, cardNbrTOProdctAcctFacility.dualacct_attribute(productCredit).accountType);
				//外币币种积分够扣减
				if(dualCcsAcct.getPointsBal().compareTo(points)>=0){
					subtractPoint(cardNbr, dualCcsAcct,points,PointAdjustIndicator.A);
				}else{
					//外币账户积分不够减，先扣减外币币种积分，在减少本币币种积分
					BigDecimal currPoint = points.subtract(dualCcsAcct.getPointsBal());//计算本币账户需要减少的积分
					subtractPoint(cardNbr, dualCcsAcct,points,PointAdjustIndicator.A);//减少外币账户积分
					subtractPoint(cardNbr, CcsAcct,currPoint,PointAdjustIndicator.A);//减少本币账户积分
				}			
			}else{
				CcsAcct currCcsAcct = queryFacility.getAcctByCardNbr(cardNbr, cardNbrTOProdctAcctFacility.acct_attribute(productCredit).accountType);
				subtractPoint(cardNbr, currCcsAcct,points,PointAdjustIndicator.A);
			}			
			subtractPoint(cardNbr, CcsAcct, points, PointAdjustIndicator.A);
		}	
	}

	@Override
	@Transactional
	public void NF5202(String cardNbr, BigDecimal points) throws ProcessException {
		log.info("NF5202:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "],积分值[" + points + "]");
		CcsAcct CcsAcct = queryFacility.getAcctByCardNbr(cardNbr, AccountType.C);
		CheckUtil.rejectNull(CcsAcct, "卡号[" + cardNbr + "]查询不到对应的账户信息");
		if (CcsAcct.getPointsBal().subtract(countPoint(CcsAcct)).intValue() < points.intValue()) {
			throw new ProcessException("积分余额不足");
		}
		CcsPointsReg tmPointReg = subtractPoint(cardNbr, CcsAcct, points, PointAdjustIndicator.D);
		rCcsPointsReg.save(tmPointReg);
	}

	/**
	 * 积分增加
	 * 
	 * @param cardNbr
	 *            TODO
	 * @param CcsAcct
	 *            TODO
	 * @return TODO
	 */
	private CcsPointsReg addPoint(String cardNbr, CcsAcct CcsAcct, BigDecimal points, PointAdjustIndicator adjustInd) {
		CcsPointsReg tmPointReg = genCcsPointsReg(CcsAcct, cardNbr, points,adjustInd);		
		rCcsPointsReg.save(tmPointReg);
		return tmPointReg;
	}

	/**
	 * 积分减少
	 * 
	 * @param cardNbr
	 *            TODO
	 * @param CcsAcct
	 * @param points
	 *            TODO
	 * @return TODO
	 * 
	 * @throws ProcessException
	 */
	private CcsPointsReg subtractPoint(String cardNbr, CcsAcct CcsAcct, BigDecimal points, PointAdjustIndicator adjustInd) throws ProcessException {
		if (CcsAcct.getPointsBal().subtract(countPoint(CcsAcct)).compareTo(points) < 0) {
			throw new ProcessException("积分余额不足");
		}
		CcsPointsReg tmPointReg = genCcsPointsReg(CcsAcct, cardNbr,points, adjustInd);
		rCcsPointsReg.save(tmPointReg);
		return tmPointReg;
	}

	/**
	 * 计算积分减少和兑换的总和 countPoint
	 * 
	 * @param CcsAcct
	 * @return BigDecimal
	 * @exception
	 * @since 1.0.0
	 */
	private BigDecimal countPoint(CcsAcct CcsAcct) {
		List<CcsPointsReg> tmPointList = queryFacility.getPointsRegByacctNbrAcctType(CcsAcct, PointAdjustIndicator.A, PointAdjustIndicator.D);
		BigDecimal points = BigDecimal.ZERO;
		for (CcsPointsReg tmPointReg : tmPointList) {
			points = points.add(tmPointReg.getPoints());
		}
		return points;
	}

	/**
	 * 生成积分接口文件
	 * 
	 * @param CcsAcct
	 * @param cardNbr
	 * @param adjustType
	 * @param postTxnType
	 * @param points
	 * @return
	 */
	private CcsPointsReg genCcsPointsReg(CcsAcct CcsAcct, String cardNbr,BigDecimal points, PointAdjustIndicator adjustInd) {
		CcsPointsReg tmPointReg = new CcsPointsReg();
		tmPointReg.setAcctNbr(CcsAcct.getAcctNbr());
		tmPointReg.setAcctType(CcsAcct.getAcctType());
		tmPointReg.setPoints(points);
		tmPointReg.setAdjInd(adjustInd);
		tmPointReg.setCardNbr(cardNbr);
		tmPointReg.setOrg(CcsAcct.getOrg());
		tmPointReg.setPostTxnType(PostTxnType.P);
		tmPointReg.setTxnDate(unifiedParameterFacilityProvide.BusinessDate());
		tmPointReg.setRequestTime(new Date());
		return tmPointReg;
	}
	/**
	 * 获取未入账积分信息
	 * @param cardNbr
	 * @return
	 * @throws
	 * 
	 * */
	@Override
	@Transactional
	public QueryResult<Map<String, Serializable>> NF5103(
			QueryRequest queryRequest, String cardNbr) throws ProcessException {
		log.info("NF5103:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
		CheckUtil.checkCardNo(cardNbr);
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsCardLmMapping.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCardLmMapping.cardNbr.eq(cardNbr)).and(qCcsCardLmMapping.logicCardNbr.eq(qCcsCard.logicCardNbr).and(qCcsCard.acctNbr.eq(qCcsPointsReg.acctNbr)));
		List<CcsPointsReg> tmPointRegs = query.from(qCcsCardLmMapping,qCcsCard,qCcsPointsReg).where(booleanExpression).offset(queryRequest.getFirstRow()).limit(queryRequest.getLastRow()).list(qCcsPointsReg);
		JPAQuery query2 = new JPAQuery(em);
		int totalRow = (int) query2.from(qCcsCardLmMapping,qCcsCard,qCcsPointsReg).where(booleanExpression).count();
		
		List<Map<String, Serializable>> list = new ArrayList<Map<String, Serializable>>();
		for (CcsPointsReg tmPointReg : tmPointRegs) {
			list.add(tmPointReg.convertToMap());
		}
		return CPSServProBusUtil.genQueryResultTOListMap(queryRequest, totalRow, list);
		
	}
	
	
	
}
