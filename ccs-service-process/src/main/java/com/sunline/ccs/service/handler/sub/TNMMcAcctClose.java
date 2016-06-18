package com.sunline.ccs.service.handler.sub;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S20070Req;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNMMcAcctClose
 * @see 描述： 预销户
 *
 * @see 创建日期： 2015年06月26日上午 10:26:03
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMMcAcctClose {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private TNRCardClose tnrCardClose;

	/**
	 * @see 方法名：handler
	 * @see 描述：预销户handler
	 * @see 创建日期：2015年6月26日上午10:59:54
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional(rollbackFor = Exception.class)
	public void handler(S20070Req req) throws ProcessException {
		LogTools.printLogger(logger, "S20070", "预销户", req, true);
		CheckUtil.checkCardNo(req.getCard_no());
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}
		// 预销户
		if (CheckUtil.equals(req.getOpt(), Constants.OPT_ZERO)) {
			tnrCardClose.S14120_OPT_ZERO(req.getCard_no());
		}
		// 预销户解除
		if (CheckUtil.equals(req.getOpt(), Constants.OPT_ONE)) {
			tnrCardClose.S14120_OPT_ONE(req.getCard_no());
		}
		LogTools.printLogger(logger, "S20070", "预销户/预销户解除", null, false);
	}
}
