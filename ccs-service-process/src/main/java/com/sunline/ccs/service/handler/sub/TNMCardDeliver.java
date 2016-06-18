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
import com.sunline.ccs.infrastructure.server.repos.RCcsAddress;
import com.sunline.ccs.infrastructure.server.repos.RCcsCard;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S14170Req;
import com.sunline.ccs.service.protocol.S14170Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.enums.CardFetchMethod;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * @see 类名：TNMCardDeliver
 * @see 描述： 卡片寄送地址设定
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMCardDeliver {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private FetchSmsNbrFacility fetchMsgCdService;
	@Autowired
	private CustAcctCardFacility queryFacility;
/*	@Autowired
	private DownMsgFacility downMsgFacility;
*/
	@Autowired
	private RCcsCard rCcsCard;
	@Autowired
	private RCcsCustomer rCcsCustomer;
	@Autowired
	private RCcsAddress rCcsAddress;

	/**
	 * @see 方法名：handler
	 * @see 描述：卡片寄送地址设定handler
	 * @see 创建日期：2015年6月25日下午5:48:07
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
	public S14170Resp handler(S14170Req req) throws ProcessException {
		LogTools.printLogger(logger, "S14170", "卡片寄送地址设定", req, true);

		// 校验上送域
		CheckUtil.checkCardNo(req.getCard_no());
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		// 获取卡片信息
		CcsCard CcsCard = queryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 获取地址信息
		List<CcsAddress> addressList = rCcsAddress.findByCustIdAndAddrType(CcsCard.getCustId(), CcsCard.getCardDeliverAddrFlag());

		// 更新
		if (Constants.OPT_ONE.equals(req.getOpt()) && req.getCard_fetch_method() != null) {
			// 将卡片寄送给持卡人
			if (CardFetchMethod.A.toString().equals(req.getCard_fetch_method())) {
				// 更新时卡片寄送地址必须上送
				CheckUtil.rejectNull(req.getCard_mailer_ind(), Constants.ERRB031_CODE, Constants.ERRB031_MES);

				// 原卡片寄送地址类型
				AddressType cardMailerInd = CcsCard.getCardDeliverAddrFlag();

				// 设置卡片寄送地址类型
				CcsCard.setCardDeliverAddrFlag(req.getCard_mailer_ind());
				rCcsCard.save(CcsCard);
				// 设置之后再获取地址信息
				addressList = rCcsAddress.findByCustIdAndAddrType(CcsCard.getCustId(), CcsCard.getCardDeliverAddrFlag());
				// 上送的地址类型无对应的地址
				if (addressList == null || addressList.size() == 0) {
					throw new ProcessException(Constants.ERRB077_CODE, Constants.ERRB077_MES);
				}

				// 卡片寄送地址变更提醒
				CcsCustomer ccsCustomer = rCcsCustomer.findOne(CcsCard.getCustId());
/*				downMsgFacility.sendMessage(fetchMsgCdService.fetchMsgCd(CcsCard.getProductCd(), CPSMessageCategory.CPS011), req.getCard_no(), ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), new Date(), new MapBuilder<String, Object>().add("oldAddressType", cardMailerInd)
						.add("newAddressType", CcsCard.getCardDeliverAddrFlag()).build());
*/
			}
			CcsCard.setCardDeliverMethod(CardFetchMethod.valueOf(req.getCard_fetch_method()));
		}

		S14170Resp resp = new S14170Resp();
		for (CcsAddress addr : addressList) {
			resp.setCountry_cd(addr.getCountryCode());
			resp.setState(addr.getState());
			resp.setCity(addr.getCity());
			resp.setDistrict(addr.getDistrict());
			resp.setZip(addr.getPostcode());
			resp.setPhone(addr.getPhone());
			resp.setAddress(addr.getAddress());
		}
		resp.setCard_no(req.getCard_no());
		resp.setCard_mailer_ind(CcsCard.getCardDeliverAddrFlag());
		resp.setCard_fetch_method(CcsCard.getCardDeliverMethod().toString());
		LogTools.printLogger(logger, "S14170", "卡片寄送地址设定", resp, false);
		return resp;
	}
}
