/**
 * 
 */
package com.sunline.ccs.ui.server;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
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
import com.sunline.ccs.infrastructure.shared.model.CcsOpOperateLog;
import com.sunline.ccs.infrastructure.shared.model.QCcsOpOperateLog;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.ark.client.utils.StringUtils;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 操作日志查询
 * 
 * @author songyc
 *
 */
@Controller
@RequestMapping(value = "/t9001Server")
public class QueryOpeLogServer {
    private static final long serialVersionUID = 1L;
    private Logger logger = LoggerFactory.getLogger(getClass());
    @PersistenceContext
    private EntityManager em;

    /**
     * @see 方法名：getCurrentUserLogList
     * @see 描述：获取操作日志列表
     * @see 创建日期：2015年6月24日下午6:31:25
     * @author songyanchao
     * 
     * @param request
     * @param cardNo
     * @param beginDate
     * @param endDate
     * @return
     * @throws ProcessException
     * 
     * @see 修改记录：
     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
     */
    @ResponseBody()
    @RequestMapping(value = "/getOperateLogList", method = {RequestMethod.POST})
    public FetchResponse getCurrentUserLogList(@RequestBody FetchRequest request) throws ProcessException {
	// 取得参数
	Date beginDate = null;
	Date endDate = null;
	String cardNbr = (String)request.getParameter(CcsOpOperateLog.P_CardNbr);
	if (request.getParameter("beginDate") != null) {
	    beginDate = new Date((Long)request.getParameter("beginDate"));
	}
	if (request.getParameter("endDate") != null) {
	    endDate = new Date((Long)request.getParameter("endDate"));
	}
	logger.info("getCurrentUserLogList:cardNo[" + CodeMarkUtils.subCreditCard(cardNbr) + "],beginDate[" + beginDate
		+ "],endDate[" + endDate + "]", cardNbr, beginDate, endDate);
	QCcsOpOperateLog qTmOperLog = QCcsOpOperateLog.ccsOpOperateLog;

	JPAQuery query = new JPAQuery(em).from(qTmOperLog);
	{
	    // 条件限于当前操作员
	    query =
		    query.where(qTmOperLog.org.eq(OrganizationContextHolder.getCurrentOrg()))
			    .where(qTmOperLog.opId.eq(OrganizationContextHolder.getUsername()));

	    if (StringUtils.isNotBlank(cardNbr)) {
		query = query.where(qTmOperLog.cardNbr.eq(cardNbr.trim()));
	    }

	    if (null != beginDate) {
		beginDate = DateUtils.truncate(beginDate, Calendar.DATE);
		query = query.where(qTmOperLog.opTime.goe(beginDate));
	    }
	    if (null != endDate) {
		endDate = DateUtils.truncate(DateUtils.addDays(endDate, 1), Calendar.DATE);
		query = query.where(qTmOperLog.opTime.lt(endDate));
	    }

	    query = query.orderBy(new OrderSpecifier<Long>(Order.DESC, qTmOperLog.opSeq));
	}

	return new JPAQueryFetchResponseBuilder(request, query)
		.addFieldMapping(CcsOpOperateLog.P_BranchId, qTmOperLog.branchId)
		.addFieldMapping(CcsOpOperateLog.P_CardNbr, qTmOperLog.cardNbr)
		.addFieldMapping(CcsOpOperateLog.P_CustId, qTmOperLog.custId)
		.addFieldMapping(CcsOpOperateLog.P_ServiceCode, qTmOperLog.serviceCode)
		.addFieldMapping(CcsOpOperateLog.P_RelatedDesc, qTmOperLog.relatedDesc)
		.addFieldMapping(CcsOpOperateLog.P_RelatedKey, qTmOperLog.relatedKey)
		.addFieldMapping(CcsOpOperateLog.P_OpSeq, qTmOperLog.opSeq)
		.addFieldMapping(CcsOpOperateLog.P_OpId, qTmOperLog.opId)
		.addFieldMapping(CcsOpOperateLog.P_OpTime, qTmOperLog.opTime)
		.addFieldMapping(CcsOpOperateLog.P_Org, qTmOperLog.org).build();
    }

}
