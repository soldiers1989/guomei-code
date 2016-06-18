package com.sunline.ccs.service.handler.sub;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.server.repos.RCcsStmtReprintReg;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsStatement;
import com.sunline.ccs.infrastructure.shared.model.CcsStmtReprintReg;
import com.sunline.ccs.param.def.Fee;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S12030Req;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.DateTools;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNRStmtReprint
 * @see 描述： 补打账单
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNRStmtReprint {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private GlobalManagementService globalManagementService;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParaFacilityProvide;
	@Autowired
	private Common common;

	@Autowired
	private RCcsStmtReprintReg rCcsReprintReg;

	/**
	 * @see 方法名：handler
	 * @see 描述：补打账单handler
	 * @see 创建日期：2015年6月25日下午6:20:20
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public void handler(S12030Req req) throws ProcessException {
		LogTools.printLogger(logger, "S12030", "补打账单", req, true);
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkStmtDate(req.getStmt_date(), false);

		CcsCard CcsCard = queryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		List<Long> CcsAcctList = queryFacility.getDistinctAcctNbrListByCardNbr(req.getCard_no());

		// 获取系统业务日期
		Date busDate = unifiedParaFacilityProvide.BusinessDate();
		Date start = DateTools.parseStmtDate(req.getStmt_date());
		Date end = DateUtils.addMonths(start, 1);
		for (Long acctNbr : CcsAcctList) {
			CcsStatement ccsStatement = new CcsStatement();
			try {
				ccsStatement = queryFacility.getCcsStatementByStmtDate(acctNbr, start, end).get(0);
			} catch (ProcessException e) {
				throw new ProcessException(Constants.ERRB068_CODE, Constants.ERRB068_MES);
			}

			if (ccsStatement.getStmtFlag().equals(Indicator.Y)) {
				CcsStmtReprintReg ccsStmtReprintReg = new CcsStmtReprintReg();
				ccsStmtReprintReg.setAcctNbr(ccsStatement.getAcctNbr());
				ccsStmtReprintReg.setOrg(ccsStatement.getOrg());
				ccsStmtReprintReg.setStmtDate(ccsStatement.getStmtDate());
				ccsStmtReprintReg.setTxnDate(globalManagementService.getSystemStatus().getBusinessDate());
				ccsStmtReprintReg.setRequestTime(new Date());
				ccsStmtReprintReg.setCardNbr(req.getCard_no());
				rCcsReprintReg.save(ccsStmtReprintReg);
			} else {
				throw new ProcessException(Constants.ERRB081_CODE, Constants.ERRB081_MES);
			}

			Fee fee = unifiedParaFacilityProvide.productCredit(CcsCard.getProductCd()).fee;
			if (!DateUtils.addMonths(ccsStatement.getStmtDate(), fee.waiveMonthReprintStmt).after(busDate)) {
				if (req.getFee_ind() == Indicator.Y) {
					CcsAcct CcsAcct = queryFacility.getAcctByAcctNbr(ccsStatement.getAcctType(), acctNbr);
					// 当账户不免除服务费时，才收取客服费
					if (CcsAcct.getWaiveSvcfeeInd() == Indicator.N) {
						common.cssfeeReg(req.getCard_no(), "S12030");
					}
				}

			}
		}
		LogTools.printLogger(logger, "S12030", "补打账单", null, false);
	}
}
