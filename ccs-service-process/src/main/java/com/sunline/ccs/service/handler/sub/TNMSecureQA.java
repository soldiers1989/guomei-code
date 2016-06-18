package com.sunline.ccs.service.handler.sub;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S11050Req;
import com.sunline.ccs.service.protocol.S11050Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNMSecureQA
 * @see 描述： 预留问题答案维护
 *
 * @see 创建日期： 2015年06月25日下午 02:19:12
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMSecureQA {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private Common common;
	
	@Autowired
	private RCcsCustomer rCustomer;

	/**
	 * @see 方法名：handler
	 * @see 描述：预留问题答案维护handler
	 * @see 创建日期：2015年6月25日下午5:59:19
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
	public S11050Resp handler(S11050Req req) throws ProcessException {
		LogTools.printLogger(logger, "S11050", "预留问题答案维护", req, true);

		// 校验上送域
		CheckUtil.checkCardNo(req.getCard_no());
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		CcsCustomer ccsCustomer = queryFacility.getCustomerByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(ccsCustomer, Constants.ERRB015_CODE, Constants.ERRB015_MES);

		// 更新
		if (Constants.OPT_ONE.equals(req.getOpt())) {
			if (StringUtils.isNotBlank(req.getObligate_question())) {
				ccsCustomer.setSecureQuestion(req.getObligate_question());
			}
			if (StringUtils.isNotBlank(req.getObligate_answer())) {
				ccsCustomer.setSecureAnswer(req.getObligate_answer());
			}

			rCustomer.save(ccsCustomer);

			CcsCard CcsCard = queryFacility.getCardByCardNbr(req.getCard_no());

			// 预留问题答案变更提醒
			// messageService.sendMessage(MessageCategory.M03,
			// CcsCard.getProductCd(), req.getCard_no(), ccsCustomer.getName(),
			// ccsCustomer.getGender(), ccsCustomer.getMobileNo(),
			// ccsCustomer.getEmail(),
			// new Date(), new MapBuilder<String, Object>().build());
			common.sendMsg(CcsCard.getProductCd(), CcsCard.getCustId(), req.getCard_no(), CPSMessageCategory.CPS003);
		}

		S11050Resp resp = new S11050Resp();
		resp.setCard_no(req.getCard_no());
		resp.setObligate_question(ccsCustomer.getSecureQuestion());
		resp.setObligate_answer(ccsCustomer.getSecureAnswer());
		LogTools.printLogger(logger, "S11050", "预留问题答案维护", resp, false);
		return resp;
	}
}
