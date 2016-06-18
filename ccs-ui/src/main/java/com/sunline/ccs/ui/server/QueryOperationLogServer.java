/**
 * 
 */
package com.sunline.ccs.ui.server;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.Order;
import com.mysema.query.types.OrderSpecifier;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ark.support.utils.DateUtils;
import com.sunline.ccs.infrastructure.shared.model.CcsOpOperateLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsOpOperateLog;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：QueryOperationLogServer
 * @see 描述：所有用户操作日志查询
 *
 * @see 创建日期： 2015年6月23日下午9:22:54
 * @author yanjingfeng
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Controller
@RequestMapping(value = "/queryOperationLogServer")
public class QueryOperationLogServer {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager em;

    @ResponseBody()
    @RequestMapping(value = "/getAllUserLogList", method = {RequestMethod.POST})
    public FetchResponse<?> getAllUserLogList(@RequestBody FetchRequest request) throws ProcessException {

	// 取得参数
	Date beginDate = null;
	Date endDate = null;
	String cardNbr = (String)request.getParameter(CcsOpOperateLog.P_CardNbr);
	if (request.getParameter("beginDate") != null) {
	    beginDate = new Date(Long.parseLong((String)request.getParameter("beginDate")));
	}
	if (request.getParameter("endDate") != null) {
	    endDate = new Date(Long.parseLong((String)request.getParameter("endDate")));
	}
	logger.info("getCurrentUserLogList:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "],开始时间[" + beginDate
		+ "],结束时间[" + endDate);
	// 
	QCcsOpOperateLog qCcsOpOperateLog = QCcsOpOperateLog.ccsOpOperateLog;
	JPAQuery query = new JPAQuery(em).from(qCcsOpOperateLog);
	// 条件限于当前机构
	query = query.where(qCcsOpOperateLog.org.eq(OrganizationContextHolder.getCurrentOrg()));

	if (StringUtils.isNotBlank(cardNbr)) {
	    query = query.where(qCcsOpOperateLog.cardNbr.eq(cardNbr.trim()));
	}

	if (null != beginDate) {
	    beginDate = DateUtils.truncate(beginDate, Calendar.DATE);
	    query = query.where(qCcsOpOperateLog.opTime.goe(beginDate));
	}
	if (null != endDate) {
	    endDate = DateUtils.truncate(DateUtils.addDays(endDate, 1), Calendar.DATE);
	    query = query.where(qCcsOpOperateLog.opTime.lt(endDate));
	}

	query = query.orderBy(new OrderSpecifier<Long>(Order.DESC, qCcsOpOperateLog.opSeq));

	return new JPAQueryFetchResponseBuilder(request, query)
		.addFieldMapping(CcsOpOperateLog.P_BranchId, qCcsOpOperateLog.branchId)
		.addFieldMapping(CcsOpOperateLog.P_CardNbr, qCcsOpOperateLog.cardNbr)
		.addFieldMapping(CcsOpOperateLog.P_CustId, qCcsOpOperateLog.custId)
		.addFieldMapping(CcsOpOperateLog.P_ServiceCode, qCcsOpOperateLog.serviceCode)
		.addFieldMapping(CcsOpOperateLog.P_RelatedDesc, qCcsOpOperateLog.relatedDesc)
		.addFieldMapping(CcsOpOperateLog.P_RelatedKey, qCcsOpOperateLog.relatedKey)
		.addFieldMapping(CcsOpOperateLog.P_OpSeq, qCcsOpOperateLog.opSeq)
		.addFieldMapping(CcsOpOperateLog.P_OpId, qCcsOpOperateLog.opId)
		.addFieldMapping(CcsOpOperateLog.P_OpTime, qCcsOpOperateLog.opTime)
		.addFieldMapping(CcsOpOperateLog.P_Org, qCcsOpOperateLog.org).build();
    }

}
