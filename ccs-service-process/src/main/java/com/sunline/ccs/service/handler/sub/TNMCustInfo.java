package com.sunline.ccs.service.handler.sub;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.utils.MapBuilder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.server.repos.RCcsAddress;
import com.sunline.ccs.infrastructure.server.repos.RCcsCard;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsAddress;
import com.sunline.ccs.infrastructure.shared.model.QCcsCard;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S11010Req;
import com.sunline.ccs.service.protocol.S11010Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * @see 类名：TNMCustInfo
 * @see 描述： 客户基本资料查询/维护
 *
 * @see 创建日期： 2015年06月25日下午 05:12:47
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMCustInfo {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private CustAcctCardFacility queryFacility;
/*	@Autowired
	private DownMsgFacility downMsgFacility;
*/	@Resource(name = "mmCardService")
	private MmCardService mmCardService;

	@Autowired
	private RCcsCard rCcsCard;
	@Autowired
	private RCcsCustomer rCcsCustomer;
	@Autowired
	private RCcsAddress rCcsAddress;

	private QCcsAddress qCcsAddress = QCcsAddress.ccsAddress;
	private QCcsCard qCcsCard = QCcsCard.ccsCard;

	/**
	 * @see 方法名：handler
	 * @see 描述：客户基本资料查询/维护handler
	 * @see 创建日期：2015年6月25日下午6:02:22
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
	public S11010Resp handler(S11010Req req) throws ProcessException {
		LogTools.printLogger(logger, "S11010", "客户基本资料查询/维护", req, true);

		// 验证卡号或证件类型+证件号码至少有一项有效
		boolean isCardNo = StringUtils.isNotBlank(req.getCard_no()) && StringUtils.isNumeric(req.getCard_no());
		boolean isId = CheckUtil.isIdNo(req.getId_type(), req.getId_no());
		if (!isCardNo && !isId) {
			throw new ProcessException(Constants.ERRB020_CODE, Constants.ERRB020_MES);
		}
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		// 获取客户
		CcsCustomer ccsCustomer;
		if (isId) {
			ccsCustomer = queryFacility.getCustomerById(req.getId_no(), req.getId_type());
		} else {
			ccsCustomer = queryFacility.getCustomerByCardNbr(req.getCard_no());
		}
		CheckUtil.rejectNull(ccsCustomer, Constants.ERRB015_CODE, Constants.ERRB015_MES);

		// 若更新
		if (Constants.OPT_ONE.equals(req.getOpt())) {
			// 检查上送的客户信息是否合法，不合法则抛出对应的错误异常信息
			if (StringUtils.isNotBlank(req.getMobile_no()) && !CheckUtil.isPhone(req.getMobile_no())) {
				throw new ProcessException(Constants.ERRB028_CODE, Constants.ERRB028_MES);
			}
			// 电话号码校验
			if (StringUtils.isNotBlank(req.getHome_phone())
					&& !(CheckUtil.isPhoneNr(req.getHome_phone()) || CheckUtil.isPhoneNrWithoutCode(req.getHome_phone())
							|| CheckUtil.isPhoneNrWithoutLine(req.getHome_phone()) || CheckUtil.isPhone(req.getHome_phone()))) {
				throw new ProcessException(Constants.ERRB027_CODE, Constants.ERRB027_MES);
			}

			// 更新上送信息
			if (req.getTitle() != null) {
				ccsCustomer.setTitle(req.getTitle());
			}
			if (StringUtils.isNotBlank(req.getName())) {
				// 修改客户姓名时，同步修改账户上姓名
				List<CcsAcct> CcsAccts = queryFacility.getAcctByCustId(ccsCustomer.getCustId());
				for (CcsAcct CcsAcct : CcsAccts) {
					CcsAcct.setName(req.getName());
				}
				ccsCustomer.setName(req.getName());

			}
			if (req.getGender() != null) {
				ccsCustomer.setGender(req.getGender());
			}
			if (req.getOccupation() != null) {
				ccsCustomer.setOccupation(req.getOccupation());
			}
			if (StringUtils.isNotBlank(req.getBankmember_no())) {
				ccsCustomer.setInternalStaffId(req.getBankmember_no());
			}
			if (StringUtils.isNotBlank(req.getNationality())) {
				ccsCustomer.setNationality(req.getNationality());
			}
			if (req.getMarital_status() != null) {
				ccsCustomer.setMaritalStatus(req.getMarital_status());
			}
			if (req.getQualification() != null) {
				ccsCustomer.setEducation(req.getQualification());
			}
			Organization cpsOrg = unifiedParameterFacility.loadParameter(null, Organization.class);
			if (StringUtils.isNotBlank(req.getHome_phone())) {

				String homePhone = ccsCustomer.getHomePhone();
				if (StringUtils.isNotBlank(req.getHome_phone()) && !StringUtils.equals(homePhone, req.getHome_phone())) {
					// 更新
					ccsCustomer.setHomePhone(req.getHome_phone());

					// 更新地址信息表中客户家庭电话
					BooleanExpression booleanExpression = qCcsAddress.custId.eq(ccsCustomer.getCustId()).and(qCcsAddress.addrType.eq(AddressType.H));
					CcsAddress address = rCcsAddress.findOne(booleanExpression);
					if (address != null) {
						address.setPhone(req.getHome_phone());
					}

					// 预留家庭电话变更提醒
					// messageService.sendMessage(MessageCategory.M01,
					// ccsCustomer.getName(), ccsCustomer.getGender(),
					// ccsCustomer.getMobileNo(), ccsCustomer.getEmail(), new
					// Date(),
					// new MapBuilder<String, Object>().add("oldPhone",
					// homePhone).add("newPhone",
					// req.getHome_phone()).add("phoneType", "家庭电话").build());
/*					downMsgFacility.sendMessage(cpsOrg.messageTemplates.get(CPSMessageCategory.CPS001), null, ccsCustomer.getName(), ccsCustomer.getGender(),
							ccsCustomer.getMobileNo(), new Date(),
							new MapBuilder<String, Object>().add("oldPhone", homePhone).add("newPhone", req.getHome_phone()).add("phoneType", "家庭电话").build());
*/				}
			}

			if (StringUtils.isNotBlank(req.getMobile_no())) {
				String phone = ccsCustomer.getMobileNo();
				if (!StringUtils.equals(phone, req.getMobile_no())) {
					// 更新
					ccsCustomer.setMobileNo(req.getMobile_no());
					List<CcsAcct> CcsAcctList = custAcctCardQueryFacility.getAcctByCustId(ccsCustomer.getCustId());
					for (CcsAcct CcsAcct : CcsAcctList) {
						CcsAcct.setMobileNo(req.getMobile_no());
					}
					// 预留手机号码变更提醒
					// messageService.sendMessage(MessageCategory.M01,
					// ccsCustomer.getName(), ccsCustomer.getGender(),
					// ccsCustomer.getMobileNo(), ccsCustomer.getEmail(), new
					// Date(),
					// new MapBuilder<String, Object>().add("oldPhone",
					// phone).add("newPhone",
					// req.getMobile_no()).add("phoneType", "手机号码").build());
					// messageService.sendMessage(MessageCategory.M01,
					// ccsCustomer.getName(), ccsCustomer.getGender(),
					// ccsCustomer.getMobileNo(), ccsCustomer.getEmail(), new
					// Date(),
					// new MapBuilder<String, Object>().add("oldPhone",
					// phone).add("newPhone",
					// req.getMobile_no()).add("phoneType", "手机号码").build());
/*					downMsgFacility.sendMessage(cpsOrg.messageTemplates.get(CPSMessageCategory.CPS001), null, ccsCustomer.getName(), ccsCustomer.getGender(),
							ccsCustomer.getMobileNo(), new Date(), new MapBuilder<String, Object>().add("oldPhone", phone).add("newPhone", req.getMobile_no())
									.add("phoneType", "手机号码").build());
*/
				}
			}

			if (StringUtils.isNotBlank(req.getEmail())) {
				String email = ccsCustomer.getEmail();
				if (!StringUtils.equals(email, req.getEmail())) {
					// 更新
					ccsCustomer.setEmail(req.getEmail());
					List<CcsAcct> CcsAcctList = custAcctCardQueryFacility.getAcctByCustId(ccsCustomer.getCustId());
					for (CcsAcct CcsAcct : CcsAcctList) {
						CcsAcct.setEmail(req.getEmail());
					}
					// 邮箱变更发送短信
/*					downMsgFacility.sendMessage(cpsOrg.messageTemplates.get(CPSMessageCategory.CPS021), req.getCard_no(), ccsCustomer.getName(),
							ccsCustomer.getGender(), ccsCustomer.getMobileNo(), new Date(),
							new MapBuilder<String, Object>().add("oldEmail", email).add("newEmail", req.getEmail()).build());
*/				}
			}
			if (StringUtils.isNotBlank(req.getCorp_name())) {
				ccsCustomer.setCorpName(req.getCorp_name());
			}
			if (StringUtils.isNotBlank(req.getEmb_name())) {
				// 同步修改MPS中凸印姓名
				ccsCustomer.setOncardName(req.getEmb_name());
				Iterable<CcsCard> CcsCards = rCcsCard.findAll(qCcsCard.custId.eq(ccsCustomer.getCustId()));
				for (CcsCard CcsCard : CcsCards) {
					mmCardService.MS3220(CcsCard.getLastestMediumCardNbr(), req.getEmb_name());
				}
			}

			rCcsCustomer.save(ccsCustomer);
		}

		// 构建返回报文对象
		S11010Resp resp = new S11010Resp();
		resp.setId_no(ccsCustomer.getIdNo());
		resp.setId_type(ccsCustomer.getIdType());
		resp.setTitle(ccsCustomer.getTitle());
		resp.setName(ccsCustomer.getName());
		resp.setGender(ccsCustomer.getGender());
		resp.setBirthday(ccsCustomer.getBirthday());
		resp.setOccupation(ccsCustomer.getOccupation());
		resp.setBankmember_no(ccsCustomer.getInternalStaffId());
		resp.setNationality(ccsCustomer.getNationality());
		resp.setMarital_status(ccsCustomer.getMaritalStatus());
		resp.setQualification(ccsCustomer.getEducation());
		resp.setHome_phone(ccsCustomer.getHomePhone());
		resp.setMobile_no(ccsCustomer.getMobileNo());
		resp.setEmail(ccsCustomer.getEmail());
		resp.setSetup_date(ccsCustomer.getSetupDate());
		resp.setCorp_name(ccsCustomer.getCorpName());
		resp.setEmb_name(ccsCustomer.getOncardName());
		resp.setBank_customer_id(ccsCustomer.getInternalCustomerId());
		LogTools.printLogger(logger, "S11010", "客户基本资料查询/维护", resp, false);
		return resp;
	}
}
