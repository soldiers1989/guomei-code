package com.sunline.ccs.ui.server;

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
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ccs.infrastructure.server.repos.RCcsCardExpList;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCardExpList;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsCardExpList;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.ui.server.commons.CPSBusProvide;
import com.sunline.ccs.ui.server.commons.CheckUtil;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.ppy.dictionary.enums.RenewResult;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 授信到期审核server
 * @author linxc 2015年6月22日 
 *
 */
@Controller
@RequestMapping(value = "/changeCardExaminationServer")
public class ChangeCardExaminationServer {

	Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private CPSBusProvide cpsBusProvide;

	@Autowired
	private GlobalManagementService globalManagementService;

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private RCcsCardExpList rCcsCardExpList;

	@Autowired
	private OpeLogUtil opeLogUtil;

	private QCcsCustomer qCcsCustomer = QCcsCustomer.ccsCustomer;

	private QCcsCardExpList qCcsCardExpList = QCcsCardExpList.ccsCardExpList;

	@ResponseBody()
	@RequestMapping(value = "/getExpiryCheckList", method = {RequestMethod.POST })
	public FetchResponse getExpiryCheckList(@RequestBody FetchRequest request) throws FlatException {
		String cardNbr = request.getParameter("cardNbr").toString();
		log.info("授权到期查询开始，卡号后四位：" + CodeMarkUtils.subCreditCard(cardNbr));
		// 检查卡号
		CheckUtil.checkCardNo(cardNbr);
		cpsBusProvide.getTmCardOTocardNbr(cardNbr); // 查找卡号是否存在
		CcsCard card = cpsBusProvide.getTmCardTocardNbr(cardNbr); // 查找卡号是否存在
		JPAQuery query = new JPAQuery(em);
		query = query.from(qCcsCustomer, qCcsCardExpList).where(
				qCcsCardExpList.cardNbr.eq(cardNbr).and(qCcsCustomer.custId.eq(card.getCustId()))
						.and(qCcsCardExpList.renewRejectCd.isNull())
						.and(qCcsCardExpList.org.eq(OrganizationContextHolder.getCurrentOrg())));
		return new JPAQueryFetchResponseBuilder(request, query)
				.addFieldMapping(CcsCardExpList.P_ListId, qCcsCardExpList.listId)
				.addFieldMapping(CcsCardExpList.P_CardNbr, qCcsCardExpList.cardNbr)
				.addFieldMapping(CcsCardExpList.P_LogicCardNbr, qCcsCardExpList.logicCardNbr)
				.addFieldMapping(CcsCardExpList.P_CardExpireDate, qCcsCardExpList.cardExpireDate)
				.addFieldMapping(CcsCustomer.P_Name, qCcsCustomer.name)
				.addFieldMapping(CcsCustomer.P_IdType, qCcsCustomer.idType)
				.addFieldMapping(CcsCustomer.P_IdNo, qCcsCustomer.idNo).build();
	}

	/**
	 * 授信到期审核处理
	 */
	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/updateRenewRejectCd", method = {RequestMethod.POST })
	public void updateRenewRejectCd(@RequestBody Map<String, Object> values) throws FlatException {
		Long listId = Long.valueOf((String)values.get(CcsCardExpList.P_ListId));
		String remark = String.valueOf(values.get("renewRejectCd"));
		log.info("卡片到期审核，到期审核ID[" + listId + "]，审核原因[" + remark + "]");
		CcsCardExpList expiryCheck = rCcsCardExpList.findOne(qCcsCardExpList.listId.eq(listId));
		if(expiryCheck == null) {
			throw new ProcessException("找不到授信到期处理信息");
		}
		expiryCheck.setRenewRejectCd(RenewResult.valueOf(remark));
		expiryCheck.setProcDate(globalManagementService.getSystemStatus().getBusinessDate());
		rCcsCardExpList.save(expiryCheck);
		// 记录操作日志
		opeLogUtil.cardholderServiceLog("3307", expiryCheck.getListId(), expiryCheck.getCardNbr(),
				expiryCheck.getCardNbr(), "授信到期审核结果：" + remark);
	}
}
