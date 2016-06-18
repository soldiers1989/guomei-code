package com.sunline.ccs.ui.server;

import java.io.Serializable;
import java.util.ArrayList;
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
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;


//import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoInqLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoInqLog;
//import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
//import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardLmMapping;
//import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoInqLog;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoInqLog;
//import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;
import com.sunline.ccs.infrastructure.shared.model.CcsCardLmMapping;


import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;


/**
 * 当天授权查询类流水查询
* @author fanghj
  * */
@Controller
@RequestMapping(value="/t1311Server")
public class QuerySerialNoServer {
	
//    @Autowired
//    private CPSTransactionService cpsTransactionService;
    @Autowired
    private CPSBusProvide cpsBusProvide;
	@PersistenceContext
    private EntityManager em;
	private QCcsAuthmemoInqLog qTmAuthInqLog = QCcsAuthmemoInqLog.ccsAuthmemoInqLog;
	private QCcsCardLmMapping qTmCardMediaMap = QCcsCardLmMapping.ccsCardLmMapping;
	private QCcsCard qTmCard = QCcsCard.ccsCard;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Transactional
	@ResponseBody()
	@RequestMapping(value="/getCurrentDayData",method={RequestMethod.POST})
	public FetchResponse getCurrentDayData(@RequestBody FetchRequest request) throws FlatException {
		// TODO Auto-generated method stub
		// 卡号
		String cardNo = request.getParameter(CcsCardLmMapping.P_CardNbr) == null ? null 
				: request.getParameter(CcsCardLmMapping.P_CardNbr).toString();
		// 卡账标识
		String cardAccountIndicator = request.getParameter("cardAccountIndicator") == null ? null 
				: request.getParameter("cardAccountIndicator").toString();
		log.info("NF4102:卡号[" + CodeMarkUtils.subCreditCard(cardNo) + "]");
		CheckUtil.checkCardNo(cardNo);
		CheckUtil.rejectNull(cardAccountIndicator, "交易功能码不能为空");
		cpsBusProvide.getTmCardOTocardNbr(cardNo);
		long millisecondsBeginTime=System.currentTimeMillis();
		List<CcsAuthmemoInqLog> tmAuthInqLogList;
     	//int totalRow = 0;
		if (cardAccountIndicator.equals("A")) {
			tmAuthInqLogList = NF4102_A(request, cardNo);
			//totalRow = countNF4102_A(cardNo);
		} else if (cardAccountIndicator.equals("C")) {
			tmAuthInqLogList = NF4102_C(request, cardNo);
		//	totalRow = countNF4102_C(cardNo);
		} else {
			throw new FlatException("无效的交易功能码");
		}
		List<Map<String, Serializable>> tmAuthInqLogMaps = new ArrayList<Map<String, Serializable>>();
		for (CcsAuthmemoInqLog tmAuthInqLog : tmAuthInqLogList) {
			tmAuthInqLogMaps.add(tmAuthInqLog.convertToMap());
		}
      return  FetchRspUtil.toFetchResponse(request, millisecondsBeginTime, tmAuthInqLogMaps);
	}
	/**
	 * @param cardNo 卡号
	 * @return 根据卡号返回查询总数
	 * 
	 * */
  /*  private int countNF4102_C(String cardNo) {
		JPAQuery query = new JPAQuery(em);
		int totalRow = (int) query.from(qTmAuthInqLog, qTmCardMediaMap)
				.orderBy(qTmAuthInqLog.acctNo.asc())
				.where(genExpressionNF4102_C(cardNo))
				.count();
		return totalRow;
	}*/

	/**
	 * 根据卡号获取所卡片下所有未授权交易列表
	 * 
	 * @param queryRequest
	 * @param cardNo
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	private List<CcsAuthmemoInqLog> NF4102_C(FetchRequest queryRequest, String cardNo) {
		JPAQuery query = new JPAQuery(em);
		List<CcsAuthmemoInqLog> tmAuthInqLog = query
				.from(qTmAuthInqLog, qTmCardMediaMap)
				.orderBy(qTmAuthInqLog.acctNbr.asc())
				.where(genExpressionNF4102_C(cardNo))
//				.offset(queryRequest.getStartRow())
//				.limit(queryRequest.getEndRow())
				.list(qTmAuthInqLog);
		return tmAuthInqLog;
	}

	/**
	 * 生成NF4102_C查询表达式
	 * 
	 * @param queryRequest
	 * @param cardNo
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	private BooleanExpression genExpressionNF4102_C(String cardNo) {
		return qTmCardMediaMap.org
				.eq(OrganizationContextHolder.getCurrentOrg())
				.and(qTmCardMediaMap.cardNbr.eq(cardNo).and(
						qTmCardMediaMap.logicCardNbr
								.eq(qTmAuthInqLog.logicCardNbr)));
	} 

	/**
	 * 根据卡号获取对应账户下所有未授权交易总数
	 * 
	 * @param cardNo
	 * @param startDate
	 * @param endDate
	 * 
	 * @return
	 */
	/*private int countNF4102_A(String cardNo) {
		JPAQuery query = new JPAQuery(em);
		int totalRows = (int) query.from(qTmAuthInqLog, qTmCard, qTmCardMediaMap)
				.orderBy(qTmAuthInqLog.acctNo.asc())
				.where(genExpressionNF4102_A(cardNo))
				.count();
		return totalRows;
	}*/

	/**
	 * 根据卡号获取对应账户下所有未授权交易列表
	 * 
	 * @param queryRequest
	 * @param cardNo
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	private List<CcsAuthmemoInqLog> NF4102_A(FetchRequest queryRequest, String cardNo) {
		JPAQuery query = new JPAQuery(em);
		List<CcsAuthmemoInqLog> tmAuthInqLog = query
				.from(qTmAuthInqLog, qTmCard, qTmCardMediaMap)
				.orderBy(qTmAuthInqLog.acctNbr.asc())
				.where(genExpressionNF4102_A(cardNo))
//				.offset(queryRequest.getStartRow())
//				.limit(queryRequest.getEndRow())
				.list(qTmAuthInqLog);
		return tmAuthInqLog;
	}

	/**
	 * 生成NF4102_A查询表达式
	 * 
	 * @param cardNo
	 * @param startDate
	 * @param endDate
	 * 
	 * @return
	 */
	private BooleanExpression genExpressionNF4102_A(String cardNo) {
		return qTmCardMediaMap.org
				.eq(OrganizationContextHolder.getCurrentOrg())
				.and(qTmCardMediaMap.cardNbr.eq(cardNo)
						.and(qTmCardMediaMap.logicCardNbr
								.eq(qTmCard.logicCardNbr)))
				.and(qTmCard.acctNbr.eq(qTmAuthInqLog.acctNbr));
	}
	

/*	//查询类授权交易流水查询
	@Override
	public QueryResult<Map<String, Serializable>> NF4102(
			QueryRequest queryRequest, String cardAccountIndicator,
			String cardNo) throws FlatException {
		log.info("NF4102:卡号[" + cardNo + "]");
		CheckUtil.checkCardNo(cardNo);
		CheckUtil.rejectNull(cardAccountIndicator, "交易功能码不能为空");
		List<CcsAuthmemoInqLog> tmAuthInqLogList;
		int totalRow = 0;
		if (cardAccountIndicator.equals("A")) {
			tmAuthInqLogList = NF4102_A(queryRequest, cardNo);
			totalRow = countNF4102_A(cardNo);
		} else if (cardAccountIndicator.equals("C")) {
			tmAuthInqLogList = NF4102_C(queryRequest, cardNo);
			totalRow = countNF4102_C(cardNo);
		} else {
			throw new FlatException("无效的交易功能码");
		}
		List<Map<String, Serializable>> tmAuthInqLogMaps = new ArrayList<Map<String, Serializable>>();
		for (CcsAuthmemoInqLog tmAuthInqLog : tmAuthInqLogList) {
			tmAuthInqLogMaps.add(tmAuthInqLog.convertToMap());
		}
		return CPSBusUtil.genQueryResultTOListMap(queryRequest, totalRow,
				tmAuthInqLogMaps);
	}*/
	


}
