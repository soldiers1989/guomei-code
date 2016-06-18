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
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S20040Req;
import com.sunline.ccs.service.protocol.S20040Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNMMcFloatRate
 * @see 描述： 客户自定义浮动利率设定
 *
 * @see 创建日期： 2015年06月26日上午 10:26:03
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMMcFloatRate {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;

	/**
	 * @see 方法名：handler
	 * @see 描述：客户自定义浮动利率设定handler
	 * @see 创建日期：2015年6月26日上午10:55:38
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
	public S20040Resp handler(S20040Req req) throws ProcessException {
		LogTools.printLogger(logger, "S20040", "浮动比例设定", req, true);
		S20040Resp resp = new S20040Resp();
		CheckUtil.checkCardNo(req.getCard_no());
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}
		// 该方法有为空判断
		List<CcsAcctO> CcsAcctOList = custAcctCardQueryFacility.getAcctOByCardNbr(req.getCard_no());
		CcsAcctO CcsAcctO = null;
		// 得到小额贷账户
		for (CcsAcctO _CcsAcctO : CcsAcctOList) {
			if (_CcsAcctO.getAcctType().equals(AccountType.E)) {
				CcsAcctO = _CcsAcctO;
			} else {
				continue;
			}
		}
		CheckUtil.rejectNull(CcsAcctO, Constants.ERRC012_CODE, Constants.ERRC012_MES);
		if (req.getOpt().equals(Constants.OPT_ONE)) {
			if (req.getFloat_rate() == null) {
				throw new ProcessException(Constants.ERRC014_CODE, Constants.ERRC014_MES);
			}
			CcsAcctO.setFloatRate(req.getFloat_rate());
			// TODO 发短信
		}
		resp.setCard_no(req.getCard_no());
		resp.setFloat_rate(CcsAcctO.getFloatRate());
		LogTools.printLogger(logger, "S20040", "浮动比例设定", resp, false);
		return resp;
	}
}
