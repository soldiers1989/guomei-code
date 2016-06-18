package com.sunline.ccs.ui.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
//import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoHst;
//import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
//import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
//import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoHst;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoHst;
//import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.ccs.ui.server.commons.DateTools;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.ppy.dictionary.enums.InputSource;

/**
 * 授权交易历史账单查询server
* @author dch
 *
 */
@Controller
@RequestMapping(value="/t1312Server")
public class HistoryTradingRecordServer {
	

	/**
	 * 
	 */
	@Autowired
    private CPSBusProvide cpsBusProvide;
	@PersistenceContext
    private EntityManager em;
	private QCcsAuthmemoHst qTmAuthHst = QCcsAuthmemoHst.ccsAuthmemoHst;
	private QCcsCardLmMapping qTmCardMediaMap = QCcsCardLmMapping.ccsCardLmMapping;
	private QCcsCard qTmCard = QCcsCard.ccsCard;
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private static final String CHANNEL_ALL = "ALL";

	@Transactional
	@ResponseBody()
	@RequestMapping(value="/getAuthHstData",method={RequestMethod.POST})
	public FetchResponse getAuthHstData(@RequestBody FetchRequest request) throws FlatException{
		
		// 卡号
		String cardNo = request.getParameter(CcsCardLmMapping.P_CardNbr) == null ? null 
				: request.getParameter(CcsCardLmMapping.P_CardNbr).toString();
		// 开始日期
		Date beginDate = request.getParameter("beginDate") == null ? null : new Date(Long.parseLong(request.getParameter("beginDate").toString()));
		// 结束日期
		Date endDate = request.getParameter("endDate") == null ? null : new Date(Long.parseLong(request.getParameter("endDate").toString()));
		// 卡账标识
		String cardAccountIndicator = request.getParameter("cardAccountIndicator") == null ? null 
				: request.getParameter("cardAccountIndicator").toString();
		
		String channel = request.getParameter("channel") == null ? null : request.getParameter("channel").toString();
		
		log.info("NF4102:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNo) + "]");
		CheckUtil.checkCardNo(cardNo);
		CheckUtil.rejectNull(cardAccountIndicator, "交易功能码不能为空");
		cpsBusProvide.getTmCardOTocardNbr(cardNo);
		long millisecondsBeginTime=System.currentTimeMillis();
		List<CcsAuthmemoHst> authHstList = new ArrayList<CcsAuthmemoHst>();
		if (cardAccountIndicator.equals("A")) { //查询账户层授权交易历史
			log.info("NF4102_A:卡号后四位["+CodeMarkUtils.subCreditCard(cardNo) + "],交易渠道["+channel+"],日期开始时间["+beginDate+"]");
			authHstList = NF4102_A(request, cardNo, channel, beginDate, endDate);
		} else if (cardAccountIndicator.equals("C")) { //查询卡片层授权交易历史
			log.info("NF4102_C:卡号后四位["+CodeMarkUtils.subCreditCard(cardNo) + "],交易渠道["+channel+"],日期开始时间["+beginDate+"]");
			authHstList = NF4102_C(request, cardNo, channel, beginDate, endDate);
		} else {
			throw new FlatException("无效的交易功能码");
		}
		List<Map<String, Serializable>> tmAuthHstMaps = new ArrayList<Map<String, Serializable>>();
		for (CcsAuthmemoHst authHst : authHstList) {
			tmAuthHstMaps.add(authHst.convertToMap());
		}
		return  FetchRspUtil.toFetchResponse(request, millisecondsBeginTime, tmAuthHstMaps);
	}

	/**
	 * 根据卡号和联机时间获取卡片层所有的授权交易历史查询
	 * @param request
	 * @param cardNo
	 * @param channel 
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	private List<CcsAuthmemoHst> NF4102_C(FetchRequest request, String cardNo, String channel, Date beginDate, Date endDate) {
		JPAQuery query = new JPAQuery(em);
		List<CcsAuthmemoHst> tmAuthHst = query
				.from(qTmAuthHst, qTmCardMediaMap)
				.orderBy(qTmAuthHst.acctNbr.asc())
				.where(genExpressionNF4102_C(cardNo,channel,beginDate,endDate))
//				.offset(request.getStartRow())
//				.limit(request.getEndRow())
				.list(qTmAuthHst);
		
		return tmAuthHst;
	}
	
	/**
	 * 根据卡号和联机时间获取账户层授权交易历史查询
	 * @param request
	 * @param cardNo
	 * @param channel 
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	private List<CcsAuthmemoHst> NF4102_A(FetchRequest request, String cardNo, String channel, Date beginDate, Date endDate) {
		JPAQuery query = new JPAQuery(em);
		List<CcsAuthmemoHst> tmAuthHst = query
				.from(qTmAuthHst, qTmCard, qTmCardMediaMap)
				.orderBy(qTmAuthHst.acctNbr.asc())
				.where(genExpressionNF4102_A(cardNo,channel,beginDate,endDate))
//				.offset(request.getStartRow())
//				.limit(request.getEndRow())
				.list(qTmAuthHst);
		return tmAuthHst;
	}

	/**
	 * 生成卡片层的授权历史交易查询表达式
	 * @param cardNo
	 * @param channel 
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	private Predicate genExpressionNF4102_C(String cardNo, String channel, Date beginDate, Date endDate) {
		BooleanExpression exp = qTmCardMediaMap.org
				.eq(OrganizationContextHolder.getCurrentOrg())
				.and(qTmCardMediaMap.cardNbr.eq(cardNo).and(
						qTmCardMediaMap.logicCardNbr
								.eq(qTmAuthHst.logicCardNbr)));
		if(beginDate != null){
			exp = exp.and(qTmAuthHst.logBizDate.goe(DateTools.startDateStamp(beginDate)));
		}
		if(endDate != null){
			exp = exp.and(qTmAuthHst.logBizDate.loe(DateTools.endDateStamp(endDate)));
		}
		if(!channel.equals(CHANNEL_ALL)){
			exp = exp.and(qTmAuthHst.inputSource.eq(InputSource.valueOf(channel)));
		}
		return exp;
	}


	/**
	 * 生成NF4102_A查询表达式
	 * @param cardNo
	 * @param channel 
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	private Predicate genExpressionNF4102_A(String cardNo, String channel, Date beginDate, Date endDate) {
		BooleanExpression exp = qTmCardMediaMap.org
				.eq(OrganizationContextHolder.getCurrentOrg())
				.and(qTmCardMediaMap.cardNbr.eq(cardNo)
						.and(qTmCardMediaMap.logicCardNbr
								.eq(qTmCard.logicCardNbr)))
				.and(qTmCard.acctNbr.eq(qTmAuthHst.acctNbr));
		if(beginDate != null){
			exp = exp.and(qTmAuthHst.logBizDate.goe(DateTools.startDateStamp(beginDate)));
		}
		if(endDate != null){
			exp = exp.and(qTmAuthHst.logBizDate.loe(DateTools.endDateStamp(endDate)));
		}
		if(!CHANNEL_ALL.equals(channel)){
			exp = exp.and(qTmAuthHst.inputSource.eq(InputSource.valueOf(channel)));
		}
		return exp;
	}

}
