package com.sunline.ccs.service.handler.sub;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S20030Req;
import com.sunline.ccs.service.protocol.S20030Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.DdIndicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNMMcDirectDebit
 * @see 描述： 查询/设定绑定借记卡号
 *
 * @see 创建日期： 2015年06月26日上午 10:26:03
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMMcDirectDebit {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;

	/**
	 * @see 方法名：handler
	 * @see 描述：查询/设定绑定借记卡号handler
	 * @see 创建日期：2015年6月26日上午10:53:52
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public S20030Resp handler(S20030Req req) throws ProcessException {
		LogTools.printLogger(logger, "S20030", "查询/设定绑定借记卡号", req, true);
		S20030Resp resp = new S20030Resp();
		// 检查上送请求报文字段
		CheckUtil.checkCardNo(req.getCard_no());
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		if (Constants.OPT_ONE.equals(req.getOpt())) {
			if (StringUtils.isNotBlank(req.getDd_bank_acct_no()) && (req.getDd_bank_acct_no().length() < 13 || req.getDd_bank_acct_no().length() > 19)) {
				throw new ProcessException(Constants.ERRB035_CODE, Constants.ERRB035_MES);
			}
		}
		// 方法中有为空校验
		List<CcsAcct> CcsAcctlist = custAcctCardQueryFacility.getAcctByCardNbr(req.getCard_no());
		CcsAcct acct = null;
		for (CcsAcct _CcsAcct : CcsAcctlist) {
			if (_CcsAcct.getAcctType().equals(AccountType.E)) {
				acct = _CcsAcct;
			} else {
				continue;
			}
		}
		CheckUtil.rejectNull(acct, Constants.ERRC012_CODE, Constants.ERRC012_MES);
		if (req.getOpt().equals(Constants.OPT_ONE)) {
			// 默认还款类型为全额还款，写死
			acct.setDdInd(DdIndicator.F);
			if (StringUtils.isNotBlank(req.getDd_bank_name())) {
				acct.setDdBankName(req.getDd_bank_name());
			}
			if (StringUtils.isNotBlank(req.getDd_bank_branch())) {
				acct.setDdBankBranch(req.getDd_bank_branch());
			}
			if (StringUtils.isNotBlank(req.getDd_bank_acct_no())) {
				acct.setDdBankAcctNbr(req.getDd_bank_acct_no());
			}
			if (StringUtils.isNotBlank(req.getDd_bank_acct_name())) {
				if (StringUtils.trim(acct.getName()).equals(StringUtils.trim(req.getDd_bank_acct_name()))) {
					acct.setDdBankAcctName(req.getDd_bank_acct_name());
				} else {
					throw new ProcessException(Constants.ERRB036_CODE, Constants.ERRB036_MES);
				}
			}
			// TODO 发短信
		}
		resp.setCard_no(req.getCard_no());
		resp.setDd_bank_name(acct.getDdBankName());
		resp.setDd_bank_branch(acct.getDdBankBranch());
		resp.setDd_bank_acct_no(acct.getDdBankAcctNbr());
		resp.setDd_bank_acct_name(acct.getDdBankAcctName());
		LogTools.printLogger(logger, "S20030", "查询/设定绑定借记卡号", resp, false);
		return resp;
	}
}
