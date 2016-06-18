package com.sunline.ccs.service.handler.sub;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S14070Req;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNRCardChange
 * @see 描述： 挂失换卡/损坏换卡
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNRCardChange {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private Common common;
	
	@Resource(name = "mmCardService")
	private MmCardService mmCardService;

	/**
	 * @see 方法名：handler
	 * @see 描述：挂失换卡/损坏换卡handler
	 * @see 创建日期：2015年6月25日下午6:13:27
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public void handler(S14070Req req) throws ProcessException {
		LogTools.printLogger(logger, "S14070", "挂失换卡/损坏换卡", req, true);
		CheckUtil.checkCardNo(req.getCard_no());
		switch (req.getChange_reson()) {
		case B1:
			mmCardService.MS3304(req.getCard_no(), null, req.getUrgent_flg(), null, null);
			break;
		case B2:
			mmCardService.MS3304(req.getCard_no(), null, req.getUrgent_flg(), null, null);
			break;
		case D1:
			mmCardService.MS3304(req.getCard_no(), null, req.getUrgent_flg(), null, null);
			break;
		case I1:
			mmCardService.MS3304(req.getCard_no(), null, req.getUrgent_flg(), null, null);
			break;
		case C1:
			mmCardService.MS3304(req.getCard_no(), null, req.getUrgent_flg(), null, null);
			break;
		case C2:
			mmCardService.MS3304(req.getCard_no(), null, req.getUrgent_flg(), null, null);
			break;
		case O1:
			mmCardService.MS3304(req.getCard_no(), null, req.getUrgent_flg(), null, null);
			break;
		case R1:
			mmCardService.MS3305(req.getCard_no(), null, req.getUrgent_flg(), null, null);
			break;
		case R2:
			mmCardService.MS3305(req.getCard_no(), null, req.getUrgent_flg(), null, null);
			break;
		case F1:
			mmCardService.MS3305(req.getCard_no(), null, req.getUrgent_flg(), null, null);
			break;
		case E1:
			// TODO MPS新增加服务
			break;
		default:
			throw new ProcessException(Constants.ERRB014_CODE, Constants.ERRB014_MES);
		}

		// TODO 以账户为准,这里暂时写死,只看本币账户
		CcsAcct CcsAcct = custAcctCardQueryFacility.getAcctByCardNbrCurrency(req.getCard_no(), Constants.CURR_CD_156);
		if (req.getFee_ind() == Indicator.Y) {
			if (CcsAcct.getWaiveSvcfeeInd() == Indicator.N) {
				common.cssfeeReg(req.getCard_no(), "S14070");
			}
		}

		LogTools.printLogger(logger, "S14070", "挂失换卡/损坏换卡", null, false);
	}
}
