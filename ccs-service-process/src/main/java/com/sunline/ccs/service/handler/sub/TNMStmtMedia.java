package com.sunline.ccs.service.handler.sub;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ark.support.utils.MapBuilder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.FetchSmsNbrFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S12021Req;
import com.sunline.ccs.service.protocol.S12021Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.enums.StmtMediaType;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * @see 类名：TNMStmtMedia
 * @see 描述： 账单介质设定
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMStmtMedia {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private FetchSmsNbrFacility fetchMsgCdService;
	@Autowired
	private CustAcctCardFacility queryFacility;
/*	@Autowired
	private DownMsgFacility downMsgFacility;
*/
	/**
	 * @see 方法名：handler
	 * @see 描述：账单介质设定handler
	 * @see 创建日期：2015年6月25日下午6:10:21
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
	public S12021Resp handler(S12021Req req) throws ProcessException {
		LogTools.printLogger(logger, "S12021", "账单介质设定", req, true);
		boolean isSendMessageM14 = false;
		boolean isSendMessageM19 = false;

		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		if (BscSuppIndicator.S.equals(CcsCard.getBscSuppInd())) {
			throw new ProcessException(Constants.ERRB069_CODE, Constants.ERRB069_MES);
		}

		S12021Resp resp = new S12021Resp();
		// 校验
		CheckUtil.checkCardNo(req.getCard_no());
		// 介质类型不能为空
		CheckUtil.rejectNull(req.getStmt_media_type(), Constants.ERRB032_CODE, Constants.ERRB032_MES);

		List<CcsAcct> CcsAcctList = queryFacility.getAcctByCardNbr(req.getCard_no());

		CcsCustomer ccsCustomer = queryFacility.getCustomerByCardNbr(req.getCard_no());

		String productCd = "";

		StmtMediaType oldStmtMediaType = null;

		String oldEmail = null;

		// 如果介质设定为email和B|both
		if (req.getStmt_media_type() == StmtMediaType.E || req.getStmt_media_type() == StmtMediaType.B) {
			// email检查,为空不报错
			CheckUtil.checkMail(req.getEmail());

			if (StringUtils.isNotBlank(req.getEmail()) && !req.getEmail().equals(ccsCustomer.getEmail())) {
				oldEmail = ccsCustomer.getEmail();
				ccsCustomer.setEmail(req.getEmail());
				resp.setEmail(req.getEmail());
				isSendMessageM19 = true;
			}

			// 获取账户信息
			for (CcsAcct CcsAcct : CcsAcctList) {
				productCd = CcsAcct.getProductCd();
				// 设置为MAIL，但客户&账户&上送EMAIL为空
				if (StringUtils.isBlank(ccsCustomer.getEmail()) && StringUtils.isBlank(CcsAcct.getEmail()) && StringUtils.isBlank(req.getEmail())) {
					throw new ProcessException(Constants.ERRB018_CODE, Constants.ERRB018_MES);
				}

				// 原账单介质类型
				oldStmtMediaType = CcsAcct.getStmtMediaType();
				// 旧email
				oldEmail = CcsAcct.getEmail();

				if (req.getStmt_media_type() == oldStmtMediaType) {
					throw new ProcessException(Constants.ERRB034_CODE, Constants.ERRB034_MES);
				}
				CcsAcct.setStmtMediaType(req.stmt_media_type);
				isSendMessageM14 = true;

				if (StringUtils.isNotBlank(ccsCustomer.getEmail()) && !ccsCustomer.getEmail().equals(CcsAcct.getEmail())) {
					CcsAcct.setEmail(ccsCustomer.getEmail());
					isSendMessageM19 = true;
				}
				resp.setEmail(CcsAcct.getEmail());
			}
		} else if (req.getStmt_media_type() == StmtMediaType.P) {
			for (CcsAcct CcsAcct : CcsAcctList) {
				productCd = CcsAcct.getProductCd();
				// 设定账单介质类型
				CcsAcct.setStmtMediaType(req.stmt_media_type);
				oldStmtMediaType = CcsAcct.getStmtMediaType();
				oldEmail = CcsAcct.getEmail();
				resp.setEmail(CcsAcct.getEmail());
			}
			isSendMessageM14 = true;
		}

		// ProductCredit product =
		// unifiedParameterFacility.loadParameter(productCd,ProductCredit.class);

		if (isSendMessageM14) {
			// 账单介质类型变更提醒
			// messageService.sendMessage(MessageCategory.M14,
			// CcsAcct.getProductCd(), req.getCard_no(), ccsCustomer.getName(),
			// ccsCustomer.getGender(), ccsCustomer.getMobileNo(),
			// ccsCustomer.getEmail(), new Date(), new MapBuilder<String,
			// Object>().add("oldMediaType", stmtMediaType).add("newMediaType",
			// req.stmt_media_type).build());

/*			downMsgFacility.sendMessage(fetchMsgCdService.fetchMsgCd(productCd, CPSMessageCategory.CPS014), req.getCard_no(), ccsCustomer.getName(),
					ccsCustomer.getGender(), ccsCustomer.getMobileNo(), new Date(),
					new MapBuilder<String, Object>().add("oldMediaType", oldStmtMediaType).add("newMediaType", req.stmt_media_type).build());
*/		}

		if (isSendMessageM19) {
			// EMAIL地址变更提醒
			// messageService.sendMessage(MessageCategory.M19,
			// CcsAcct.getProductCd(), req.getCard_no(), ccsCustomer.getName(),
			// ccsCustomer.getGender(), ccsCustomer.getMobileNo(),
			// ccsCustomer.getEmail(), new Date(), new MapBuilder<String,
			// Object>().add("oldEmail", email).add("newEmail",
			// req.getEmail()).build());
/*			downMsgFacility.sendMessage(fetchMsgCdService.fetchMsgCd(productCd, CPSMessageCategory.CPS021), req.getCard_no(), ccsCustomer.getName(),
					ccsCustomer.getGender(), ccsCustomer.getMobileNo(), new Date(),
					new MapBuilder<String, Object>().add("oldEmail", oldEmail).add("newEmail", req.getEmail()).build());
*/		}

		// 构建响应报文对象
		resp.setCard_no(req.getCard_no());
		resp.setStmt_media_type(req.getStmt_media_type());
		LogTools.printLogger(logger, "S12021", "账单介质设定", resp, false);
		return resp;
	}
}
