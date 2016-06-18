package com.sunline.ccs.service.handler.sub;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ark.support.utils.MapBuilder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.server.repos.RCcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAddress;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S11030AddressInfo;
import com.sunline.ccs.service.protocol.S11030Req;
import com.sunline.ccs.service.protocol.S11030Resp;
import com.sunline.ccs.service.util.CPSServProBusUtil;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.PageTools;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * @see 类名：TNMAddress
 * @see 描述： 客户地址信息查询/维护
 *
 * @see 创建日期： 2015年06月25日下午 05:12:47
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMAddress {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;
	@Autowired
	private CustAcctCardFacility queryFacility;
/*	@Autowired
	private DownMsgFacility downMsgFacility;
*/
	@Autowired
	private RCcsAddress rCcsAddress;

	@PersistenceContext
	private EntityManager em;
	private QCcsAddress qCcsAddress = QCcsAddress.ccsAddress;
	private QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;

	/**
	 * @see 方法名：handler
	 * @see 描述：客户地址信息查询/维护handler
	 * @see 创建日期：2015年6月25日下午5:39:13
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
	public S11030Resp handler(S11030Req req) throws ProcessException {
		LogTools.printLogger(logger, "S11030", "客户地址信息查询/维护", req, true);

		// 验证证件类型、证件号码
		if (!CheckUtil.isIdNo(req.getId_type(), req.getId_no())) {
			throw new ProcessException(Constants.ERRB021_CODE, Constants.ERRB021_MES);
		}
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)
				&& !StringUtils.equals(req.getOpt(), Constants.OPT_TWO) && !StringUtils.equals(req.getOpt(), Constants.OPT_THREE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		// 获取客户
		CcsCustomer ccsCustomer = queryFacility.getCustomerById(req.getId_no(), req.getId_type());
		CheckUtil.rejectNull(ccsCustomer, Constants.ERRB015_CODE, Constants.ERRB015_MES);
		// 主查询
		JPAQuery query = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsAddress.custId.eq(ccsCustomer.getCustId());
		if (req.getAddr_type() != null) {
			booleanExpression = booleanExpression.and(qCcsAddress.addrType.eq(req.getAddr_type()));
		}
		List<CcsAddress> addressList;
		if (Constants.OPT_ZERO.equals(req.getOpt())) {
			addressList = query.from(qCcsAddress).where(booleanExpression).offset(req.getFirstrow())
					.limit(PageTools.calculateLmt(req.getFirstrow(), req.getLastrow())).list(qCcsAddress);
		} else {
			addressList = query.from(qCcsAddress).where(booleanExpression).list(qCcsAddress);
		}

		// 若新增
		if (Constants.OPT_TWO.equals(req.getOpt())) {
			if (addressList.size() > 0) {
				throw new ProcessException(Constants.ERRB101_CODE, Constants.ERRB101_MES);
			} else {
				S11030_OPT_TWO(req, ccsCustomer);
			}
		}
		// 若更新
		if (Constants.OPT_ONE.equals(req.getOpt()) && addressList.size() == 0) {
			throw new ProcessException(Constants.ERRB117_CODE, Constants.ERRB117_MES);
		}
		// 若删除
		if (Constants.OPT_THREE.equals(req.getOpt()) && addressList.size() == 0) {
			throw new ProcessException(Constants.ERRB118_CODE, Constants.ERRB118_MES);
		}

		ArrayList<S11030AddressInfo> addresses = new ArrayList<S11030AddressInfo>();
		for (CcsAddress address : addressList) {
			// 若删除
			if (Constants.OPT_THREE.equals(req.getOpt())) {
				List<CcsAddress> ccsAddresses = (List<CcsAddress>) rCcsAddress.findAll(qCcsAddress.custId.eq(ccsCustomer.getCustId()));
				if (ccsAddresses.size() == 1) {
					throw new ProcessException(Constants.ERRB122_CODE, Constants.ERRB122_MES);
				}
				S11030_OPT_THREE(req, address, ccsCustomer);
			}
			// 若更新
			if (Constants.OPT_ONE.equals(req.getOpt())) {
				S11030_OPT_ONE(req, address, ccsCustomer);
			}

			S11030AddressInfo s11030address = new S11030AddressInfo();
			s11030address.setAddr_type(address.getAddrType());
			s11030address.setCountry_cd(address.getCountryCode());
			s11030address.setState(address.getState());
			s11030address.setCity(address.getCity());
			s11030address.setDistrict(address.getDistrict());
			s11030address.setZip(address.getPostcode());
			s11030address.setPhone(address.getPhone());
			s11030address.setAddress(address.getAddress());

			addresses.add(s11030address);
		}

		// 总记录数
		JPAQuery totalQuery = new JPAQuery(em);
		int totalRows = (int) totalQuery.from(qCcsAddress).where(booleanExpression).count();

		// 构建响应报文对象
		S11030Resp resp = new S11030Resp();
		resp.setTotal_rows(totalRows);
		if (req.getLastrow() != null) {
			resp.setNextpage_flg(PageTools.hasNextPage(req.getLastrow(), totalRows));
		}
		resp.setFirstrow(req.getFirstrow());
		resp.setLastrow(req.getLastrow());
		resp.setAddress_s(addresses);
		LogTools.printLogger(logger, "S11030", "客户地址信息查询/维护", resp, false);
		return resp;
	}

	/**
	 * @see 方法名：S11030_OPT_ONE
	 * @see 描述：修改客户地址信息
	 * @see 创建日期：2015年6月25日下午5:43:11
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @param address
	 * @param ccsCustomer
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void S11030_OPT_ONE(S11030Req req, CcsAddress address, CcsCustomer ccsCustomer) {
		// 必须上送地址类型
		CheckUtil.rejectNull(req.getAddr_type(), Constants.ERRB023_CODE, Constants.ERRB023_MES);
		// 邮政编码校验
		if (StringUtils.isNotBlank(req.getZip()) && !CheckUtil.checkPostcode(req.getZip())) {
			throw new ProcessException(Constants.ERRB026_CODE, Constants.ERRB026_MES);
		}
		// 电话号码校验
		if (StringUtils.isNotBlank(req.getPhone())
				&& !(CheckUtil.isPhoneNr(req.getPhone()) || CheckUtil.isPhoneNrWithoutCode(req.getPhone()) || CheckUtil.isPhoneNrWithoutLine(req.getPhone()) || CheckUtil
						.isPhone(req.getPhone()))) {
			throw new ProcessException(Constants.ERRB027_CODE, Constants.ERRB027_MES);
		}
		if (StringUtils.isNotBlank(req.getCountry_cd())) {
			address.setCountryCode(req.getCountry_cd());
		}
		if (StringUtils.isNotBlank(req.getState())) {
			address.setState(req.getState());
		}
		if (StringUtils.isNotBlank(req.getCity())) {
			address.setCity(req.getCity());
		}
		if (StringUtils.isNotBlank(req.getDistrict())) {
			address.setDistrict(req.getDistrict());
		}
		if (StringUtils.isNotBlank(req.getZip())) {
			address.setPostcode(req.getZip());
		}

		String phone = address.getPhone();
		Organization cpsOrg = unifiedParameterFacility.loadParameter(null, Organization.class);
		if (StringUtils.isNotBlank(req.getPhone()) && !StringUtils.equals(phone, req.getPhone())) {
			// 更新
			address.setPhone(req.getPhone());

			// 更新客户表中客户家庭电话
			if (req.getAddr_type().equals(AddressType.H)) {
				ccsCustomer.setHomePhone(req.getPhone());
			}

			// 预留电话变更提醒
/*			downMsgFacility.sendMessage(
					cpsOrg.messageTemplates.get(CPSMessageCategory.CPS001),
					null,
					ccsCustomer.getName(),
					ccsCustomer.getGender(),
					ccsCustomer.getMobileNo(),
					new Date(),
					new MapBuilder<String, Object>().add("oldPhone", phone).add("newPhone", req.getPhone())
							.add("phoneType", CPSServProBusUtil.getEnumInfo(address.getAddrType()) + "对应的电话").build());
*/		}

		String addressStr = address.getAddress();
		if (StringUtils.isNotBlank(req.getAddress()) && !StringUtils.equals(addressStr, req.getAddress())) {
			// 更新
			address.setAddress(req.getAddress());
			// 预留地址变更提醒
/*			downMsgFacility.sendMessage(cpsOrg.messageTemplates.get(CPSMessageCategory.CPS002), null, ccsCustomer.getName(), ccsCustomer.getGender(),
					ccsCustomer.getMobileNo(), new Date(),
					new MapBuilder<String, Object>().add("newAddress", address.getAddress()).add("addressType", req.getAddr_type()).build());
*/		}

		rCcsAddress.save(address);

		// 判断修改的地址是否为账单的邮寄地址，如果是，需要同步更新
		JPAQuery accountQuery = new JPAQuery(em);
		BooleanExpression bp = qCcsAcct.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsAcct.custId.eq(ccsCustomer.getCustId()))
				.and(qCcsAcct.stmtMailAddrInd.eq(req.getAddr_type()));
		List<CcsAcct> accountList = accountQuery.from(qCcsAcct).where(bp).list(qCcsAcct);
		for (CcsAcct acct : accountList) {
			acct.setStmtCountryCode(address.getCountryCode());
			acct.setStmtCity(address.getCity());
			acct.setStmtDistrict(address.getDistrict());
			acct.setStmtState(address.getState());
			acct.setStmtPostcode(address.getPostcode());
			acct.setStmtAddress(address.getAddress());
		}
	}

	/**
	 * @see 方法名：S11030_OPT_THREE
	 * @see 描述：删除客户地址信息
	 * @see 创建日期：2015年6月25日下午5:43:27
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @param address
	 * @param ccsCustomer
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void S11030_OPT_THREE(S11030Req req, CcsAddress address, CcsCustomer ccsCustomer) {
		// 必须上送地址类型
		CheckUtil.rejectNull(req.getAddr_type(), Constants.ERRB023_CODE, Constants.ERRB023_MES);
		// 判断修改的地址是否为账单的邮寄地址，如果是，不能删除
		List<CcsAcct> lists = queryFacility.getAcctByCustId(ccsCustomer.getCustId());
		for (CcsAcct CcsAcct : lists) {
			if (CcsAcct.getStmtMailAddrInd().equals(req.getAddr_type())) {
				throw new ProcessException(Constants.ERRB103_CODE, Constants.ERRB103_MES);
			}
		}
		rCcsAddress.delete(address.getAddrId());
		Organization cpsOrg = unifiedParameterFacility.loadParameter(null, Organization.class);
/*		downMsgFacility.sendMessage(cpsOrg.messageTemplates.get(CPSMessageCategory.CPS002), null, ccsCustomer.getName(), ccsCustomer.getGender(),
				ccsCustomer.getMobileNo(), new Date(),
				new MapBuilder().add("beDeletedAddress", address.getAddress()).add("beDeletedaddressType", address.getAddrType()).build());
*/
	}

	/**
	 * @see 方法名：S11030_OPT_TWO
	 * @see 描述：新增客户地址信息
	 * @see 创建日期：2015年6月25日下午5:43:39
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @param ccsCustomer
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void S11030_OPT_TWO(S11030Req req, CcsCustomer ccsCustomer) {
		// 必须上送地址类型
		CheckUtil.rejectNull(req.getAddr_type(), Constants.ERRB023_CODE, Constants.ERRB023_MES);
		CheckUtil.rejectNull(req.getAddress(), Constants.ERRB119_CODE, Constants.ERRB119_MES);
		CheckUtil.rejectNull(req.getCountry_cd(), Constants.ERRB120_CODE, Constants.ERRB120_MES);
		CheckUtil.rejectNull(req.getPhone(), Constants.ERRB121_CODE, Constants.ERRB121_MES);
		// 邮政编码校验
		if (StringUtils.isNotBlank(req.getZip()) && !CheckUtil.checkPostcode(req.getZip())) {
			throw new ProcessException(Constants.ERRB026_CODE, Constants.ERRB026_MES);
		}
		// 电话号码校验
		if (StringUtils.isNotBlank(req.getPhone())
				&& !(CheckUtil.isPhoneNr(req.getPhone()) || CheckUtil.isPhoneNrWithoutCode(req.getPhone()) || CheckUtil.isPhoneNrWithoutLine(req.getPhone()) || CheckUtil
						.isPhone(req.getPhone()))) {
			throw new ProcessException(Constants.ERRB027_CODE, Constants.ERRB027_MES);
		}
		CcsAddress address = new CcsAddress();
		address.setOrg(OrganizationContextHolder.getCurrentOrg());
		address.setCustId(ccsCustomer.getCustId());
		address.setAddrType(req.getAddr_type());
		address.setAddress(req.getAddress());
		address.setCountryCode(req.getCountry_cd());
		address.setPhone(req.getPhone());
		address.setState(req.getState());
		address.setCity(req.getCity());
		address.setDistrict(req.getDistrict());
		address.setPostcode(req.getZip());
		rCcsAddress.save(address);
	}

}
