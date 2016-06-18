package com.sunline.ccs.service.handler.sub;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S12040Req;
import com.sunline.ccs.service.protocol.S12040Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNRCardFeeWaive
 * @see 描述： 未收年费减免
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNRCardFeeWaive {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility queryFacility;

	/**
	 * @see 方法名：handler
	 * @see 描述：未收年费减免handler
	 * @see 创建日期：2015年6月25日下午6:19:55
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
	public S12040Resp handler(S12040Req req) throws ProcessException {
		LogTools.printLogger(logger, "S12040", "未收年费减免", req, true);
		CheckUtil.checkCardNo(req.getCard_no());
		CcsCard ccsCard = queryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(ccsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		// 为了支持一次减免多次年费，暂时屏蔽掉干判断
		// if (CcsCard.getNextCardFeeDate() != null &&
		// DateTools.dateCompare(DateUtils.addYears(unifiedParaFacilityProvide.BusinessDate(),
		// req.getWave_years()), CcsCard.getNextCardFeeDate()) < 0) {
		// throw new
		// ProcessException(Constants.ERRB049_CODE,Constants.ERRB049_MES);
		// }
		if (ccsCard.getNextCardFeeDate() == null) {
		    throw new ProcessException(Constants.ERRB089_CODE,Constants.ERRB089_MES);
		}
		Date lastCardFeeDate = ccsCard.getNextCardFeeDate();
		Date newNextCardFeeDate = DateUtils.addYears(ccsCard.getNextCardFeeDate(), req.getWave_years());
		ccsCard.setNextCardFeeDate(newNextCardFeeDate);

		S12040Resp resp = new S12040Resp();
		resp.setCard_no(req.getCard_no());
		resp.setLast_card_fee_date(lastCardFeeDate);
		resp.setNext_card_fee_date(newNextCardFeeDate);
		LogTools.printLogger(logger, "S12040", "未收年费减免", resp, false);
		return resp;
	}
}
