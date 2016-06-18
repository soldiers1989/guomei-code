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
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsAddress;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S12020Req;
import com.sunline.ccs.service.protocol.S12020Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * @see 类名：TNMStmtAddress
 * @see 描述： 账单寄送地址设定
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMStmtAddress {

	private Logger logger = LoggerFactory.getLogger(getClass());

/*	@Autowired
	private DownMsgFacility downMsgFacility;
*/	@Autowired
	private FetchSmsNbrFacility fetchMsgCdService;
	@Autowired
	private CustAcctCardFacility queryFacility;

	@Autowired
	private RCcsAcct rCcsAcct;
	@Autowired
	private RCcsAddress rCcsAddress;
	@Autowired
	private RCcsCustomer rCcsCustomer;

	/**
	 * @see 方法名：handler
	 * @see 描述：账单寄送地址设定handler
	 * @see 创建日期：2015年6月25日下午6:09:42
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
	public S12020Resp handler(S12020Req req) throws ProcessException {
		LogTools.printLogger(logger, "S12020", "账单寄送地址设定", req, true);
		S12020Resp resp = new S12020Resp();
		// 校验
		CheckUtil.checkCardNo(req.getCard_no());
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}
		// 获取账户列表
		List<CcsAcct> CcsAcctList = queryFacility.getAcctByCardNbr(req.getCard_no());
		for (CcsAcct CcsAcct : CcsAcctList) {
			if (Constants.OPT_ZERO.equals(req.getOpt())) {
				resp.setCard_no(req.getCard_no());
				if (CcsAcct.getStmtMailAddrInd() == null) {
					continue;
				}
				List<CcsAddress> addressList = rCcsAddress.findByCustIdAndAddrType(CcsAcct.getCustId(), CcsAcct.getStmtMailAddrInd());
				CcsAddress address = addressList.isEmpty() ? null : addressList.get(0);
				CheckUtil.rejectNull(address, Constants.ERRB099_CODE, Constants.ERRB099_MES);
				setS12020Resp(resp, req, address);
				break;
			}
			if (Constants.OPT_ONE.equals(req.getOpt())) {
				boolean isSendMessage = false;
				CheckUtil.rejectNull(req.getAddr_type(), Constants.ERRB031_CODE, Constants.ERRB031_MES);
				// 上送寄送地址与原寄送地址相同，拒绝
				if (req.getAddr_type() == CcsAcct.getStmtMailAddrInd()) {
					throw new ProcessException(Constants.ERRB030_CODE, Constants.ERRB030_MES);
				}
				// 原账单寄送地址类型
				AddressType addressType = CcsAcct.getStmtMailAddrInd();
				// 更新账单寄送地址类型
				CcsAcct.setStmtMailAddrInd(req.getAddr_type());
				if (!isSendMessage) {
					// 账单寄送地址变更提醒
					CcsCustomer ccsCustomer = rCcsCustomer.findOne(CcsAcct.getCustId());
					// ProductCredit product =
					// unifiedParameterFacility.loadParameter(CcsAcct.getProductCd(),
					// ProductCredit.class);
/*					downMsgFacility.sendMessage(fetchMsgCdService.fetchMsgCd(CcsAcct.getProductCd(), CPSMessageCategory.CPS012), req.getCard_no(),
							ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), new Date(),
							new MapBuilder<String, Object>().add("oldAddressType", addressType).add("newAddressType", CcsAcct.getStmtMailAddrInd()).build());
*/					// 构建响应报文对象
					List<CcsAddress> addressList = rCcsAddress.findByCustIdAndAddrType(CcsAcct.getCustId(), req.getAddr_type());
					CcsAddress address = addressList.isEmpty() ? null : addressList.get(0);
					CheckUtil.rejectNull(address, Constants.ERRB099_CODE, Constants.ERRB099_MES);
					CcsAcct.setStmtAddress(address.getAddress());
					CcsAcct.setStmtCity(address.getCity());
					CcsAcct.setStmtCountryCode(address.getCountryCode());
					CcsAcct.setStmtDistrict(address.getDistrict());
					CcsAcct.setStmtMailAddrInd(address.getAddrType());
					CcsAcct.setStmtState(address.getState());
					CcsAcct.setStmtPostcode(address.getPostcode());
					rCcsAcct.save(CcsAcct);
					setS12020Resp(resp, req, address);
					isSendMessage = true;
				}
				break;
			}
		}
		LogTools.printLogger(logger, "S12020", "账单寄送地址设定", resp, false);
		return resp;
	}

	/**
	 * @see 方法名：setS12020Resp
	 * @see 描述：将值赋到resp中
	 * @see 创建日期：2015年6月25日下午6:09:54
	 * @author yanjingfeng
	 * 
	 * @param resp
	 * @param req
	 * @param address
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void setS12020Resp(S12020Resp resp, S12020Req req, CcsAddress address) {
		resp.setCard_no(req.getCard_no());
		resp.setAddr_type(address.getAddrType());
		resp.setStmt_country_cd(address.getCountryCode());
		resp.setStmt_state(address.getState());
		resp.setStmt_city(address.getCity());
		resp.setStmt_district(address.getDistrict());
		resp.setStmt_address(address.getAddress());
		resp.setStmt_zip(address.getPostcode());
	}

}
