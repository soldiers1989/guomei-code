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
import com.sunline.ccs.infrastructure.server.repos.RCcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.QCcsLinkman;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S11040Contact;
import com.sunline.ccs.service.protocol.S11040Req;
import com.sunline.ccs.service.protocol.S11040Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.IdentificationCodeUtil;
import com.sunline.ccs.service.util.PageTools;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/**
 * @see 类名：TNMLinkMan
 * @see 描述： 客户联系人信息查询/维护
 *
 * @see 创建日期： 2015年06月25日下午 05:12:47
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNMLinkMan {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility queryFacility;
/*	@Autowired
	private DownMsgFacility downMsgFacility;
*/	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;

	@Autowired
	private RCcsLinkman rCcsLinkMan;

	@PersistenceContext
	private EntityManager em;
	private QCcsLinkman qCcsLinkman = QCcsLinkman.ccsLinkman;

	/**
	 * @see 方法名：handler
	 * @see 描述： 客户联系人信息查询/维护handler
	 * @see 创建日期：2015年6月25日下午6:04:57
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
	public S11040Resp handler(S11040Req req) throws ProcessException {
		LogTools.printLogger(logger, "S11040", "客户联系人信息查询/维护", req, true);

		// 验证证件类型、证件号码
		if (!CheckUtil.isIdNo(req.getId_type(), req.getId_no())) {
			throw new ProcessException(Constants.ERRB021_CODE, Constants.ERRB021_MES);
		}
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO) && !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)
				&& !StringUtils.equals(req.getOpt(), Constants.OPT_TWO) && !StringUtils.equals(req.getOpt(), Constants.OPT_THREE)) {
			throw new ProcessException(Constants.ERRS004_CODE, Constants.ERRS004_MES);
		}

		// 更新时，上送信息校验
		if (Constants.OPT_ONE.equals(req.getOpt()) || Constants.OPT_TWO.equals(req.getOpt())) {
			// 必须上送与持卡人关系
			CheckUtil.rejectNull(req.getRelationship(), Constants.ERRB024_CODE, Constants.ERRB024_MES);
			if (req.getBirthday() != null && req.getContact_id_no() != null) {
				// 生日和身份证号交叉验证,TODO 此校验存在问题
				// if (req.id_type == IdType.I &&
				// !CheckUtil.isBirthday(req.getBirthday(), req.getId_no())) {
				// throw new ProcessException(Constants.ERRB029_CODE,
				// Constants.ERRB029_MES);
				// }
				// 生日和身份证号交叉验证
				CheckUtil.rejectNull(req.getContact_id_type(), Constants.ERRB025_CODE, Constants.ERRB025_MES);
				if (req.getContact_id_type() == IdType.I) {
					if (!CheckUtil.isBirthday(req.getBirthday(), req.getContact_id_no())) {
						throw new ProcessException(Constants.ERRB029_CODE, Constants.ERRB029_MES);
					}
				}
			}

			// 移动电话校验
			if (StringUtils.isNotBlank(req.getMobile_no()) && !CheckUtil.isPhone(req.getMobile_no())) {
				throw new ProcessException(Constants.ERRB028_CODE, Constants.ERRB028_MES);
			}

			// 公司电话号码校验
			if (StringUtils.isNotBlank(req.getCorp_phone())
					&& !(CheckUtil.isPhoneNr(req.getCorp_phone()) || CheckUtil.isPhoneNrWithoutCode(req.getCorp_phone())
							|| CheckUtil.isPhoneNrWithoutLine(req.getCorp_phone()) || CheckUtil.isPhone(req.getCorp_phone()))) {
				throw new ProcessException(Constants.ERRB027_CODE, Constants.ERRB027_MES);
			}
		}

		// 获取客户
		CcsCustomer ccsCustomer = queryFacility.getCustomerById(req.getId_no(), req.getId_type());
		CheckUtil.rejectNull(ccsCustomer, Constants.ERRB015_CODE, Constants.ERRB015_MES);

		// 获取联系人信息
		JPAQuery query = new JPAQuery(em);
		JPAQuery query2 = new JPAQuery(em);
		BooleanExpression booleanExpression = qCcsLinkman.custId.eq(ccsCustomer.getCustId());
		List<CcsLinkman> tmContactList = new ArrayList<CcsLinkman>();
		if (Constants.OPT_THREE.equals(req.getOpt())) {
			// 必须上送与持卡人关系
			CheckUtil.rejectNull(req.getRelationship(), Constants.ERRB024_CODE, Constants.ERRB024_MES);
			tmContactList = query2.from(qCcsLinkman).where(booleanExpression).list(qCcsLinkman);
			if (tmContactList.size() < 2) {
				throw new ProcessException(Constants.ERRB105_CODE, Constants.ERRB105_MES);
			}
		}
		if (req.getRelationship() != null) {
			booleanExpression = booleanExpression.and(qCcsLinkman.relationship.eq(req.getRelationship()));
		}

		if (Constants.OPT_ZERO.equals(req.getOpt())) {
			tmContactList = query.from(qCcsLinkman).where(booleanExpression).offset(req.getFirstrow())
					.limit(PageTools.calculateLmt(req.getFirstrow(), req.getLastrow())).list(qCcsLinkman);
		}
		if (Constants.OPT_ONE.equals(req.getOpt())) {
			tmContactList = query.from(qCcsLinkman).where(booleanExpression).list(qCcsLinkman);
			if (tmContactList == null || tmContactList.size() == 0) {
				throw new ProcessException(Constants.ERRB085_CODE, Constants.ERRB085_MES);
			}
		}
		ArrayList<S11040Contact> contacts = new ArrayList<S11040Contact>();
		// 若新增
		if (Constants.OPT_TWO.equals(req.getOpt())) {
			tmContactList = query2.from(qCcsLinkman).where(booleanExpression).list(qCcsLinkman);
			if (tmContactList.size() > 0) {
				throw new ProcessException(Constants.ERRB102_CODE, Constants.ERRB102_MES);
			} else {
				S11040_OPT_TWO(req, ccsCustomer);
			}
			tmContactList = query.from(qCcsLinkman).where(booleanExpression).list(qCcsLinkman);
		}
		// 若删除
		if (Constants.OPT_THREE.equals(req.getOpt())) {
			tmContactList = query.from(qCcsLinkman).where(booleanExpression).list(qCcsLinkman);
		}
		for (CcsLinkman tmContact : tmContactList) {
			S11040Contact contact = new S11040Contact();

			// 若更新
			if (Constants.OPT_ONE.equals(req.getOpt())) {
				S11040_OPT_ONE(req, tmContact, ccsCustomer);
			}
			// 若删除
			if (Constants.OPT_THREE.equals(req.getOpt())) {
				S11040_OPT_THREE(tmContact, ccsCustomer);
			}

			contact.setRelationship(tmContact.getRelationship());
			contact.setName(tmContact.getName());
			contact.setGender(tmContact.getGender());
			contact.setMobile_no(tmContact.getMobileNo());
			contact.setBirthday(tmContact.getBirthday());
			contact.setCorp_name(tmContact.getCorpName());
			contact.setContact_id_type(tmContact.getIdType());
			contact.setContact_id_no(tmContact.getIdNo());
			contact.setCorp_phone(tmContact.getCorpTelephNbr());
			contact.setCorp_fax(tmContact.getCorpFax());
			contact.setCorp_post(tmContact.getCorpPosition());

			contacts.add(contact);
		}

		// 总记录数
		JPAQuery totalQuery = new JPAQuery(em);
		int totalRows = (int) totalQuery.from(qCcsLinkman).where(booleanExpression).count();

		// 构建响应报文对象
		S11040Resp resp = new S11040Resp();
		resp.setNextpage_flg(Indicator.N);
		// 若查询则判断是否有下一页
		if (Constants.OPT_ZERO.equals(req.getOpt())) {
			resp.setNextpage_flg(PageTools.hasNextPage(req.getLastrow(), totalRows));
		}
		resp.setTotal_rows(totalRows);
		resp.setFirstrow(req.getFirstrow());
		resp.setLastrow(req.getLastrow());
		resp.setId_type(req.getId_type());
		resp.setId_no(req.getId_no());
		resp.setContacts(contacts);
		LogTools.printLogger(logger, "S11040", "客户联系人信息查询/维护", resp, false);
		return resp;
	}

	/**
	 * @see 方法名：S11040_OPT_ONE
	 * @see 描述：修改客户联系人信息
	 * @see 创建日期：2015年6月25日下午6:05:50
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @param tmContact
	 * @param ccsCustomer
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void S11040_OPT_ONE(S11040Req req, CcsLinkman tmContact, CcsCustomer ccsCustomer) {
		if (StringUtils.isNotBlank(req.getName())) {
			tmContact.setName(req.getName());
		}
		if (req.getGender() != null) {
			tmContact.setGender(req.getGender());
		}
		if (StringUtils.isNotBlank(req.getMobile_no())) {
			tmContact.setMobileNo(req.getMobile_no());
		}
		if (req.getBirthday() != null) {
			tmContact.setBirthday(req.getBirthday());
		}
		if (StringUtils.isNotBlank(req.getCorp_name())) {
			tmContact.setCorpName(req.getCorp_name());
		}
		if (req.getContact_id_type() != null) {
			tmContact.setIdType(req.getContact_id_type());
		}
		if (StringUtils.isNotBlank(req.getContact_id_no())) {
			// 如果是身份证或临时身份证，则证件后的字母需转换为大写
			if (req.getContact_id_type() != null && (req.getContact_id_type().equals(IdType.I) || req.getContact_id_type().equals(IdType.T))) {
				// 验证身份证是否合法
				if (IdentificationCodeUtil.isIdentityCode(req.getContact_id_no())) {
					tmContact.setIdNo(req.getContact_id_no().toString().toUpperCase());
				} else {
					throw new ProcessException(Constants.ERRB056_CODE, Constants.ERRB056_MES);
				}
			} else {
				tmContact.setIdNo(req.getContact_id_no());
			}
		}
		if (StringUtils.isNotBlank(req.getCorp_phone())) {
			tmContact.setCorpTelephNbr(req.getCorp_phone());
		}
		if (StringUtils.isNotBlank(req.getCorp_fax())) {
			tmContact.setCorpFax(req.getCorp_fax());
		}
		if (req.getCorp_post() != null) {
			tmContact.setCorpPosition(req.getCorp_post());
		}

		rCcsLinkMan.save(tmContact);

		// 联系人变更提醒
		// messageService.sendMessage(MessageCategory.M13,
		// ccsCustomer.getName(),
		// ccsCustomer.getGender(), ccsCustomer.getMobileNo(),
		// ccsCustomer.getEmail(), new Date(),
		// new MapBuilder<String, Object>().add("contactName",
		// tmContact.getName()).add("relationship",
		// tmContact.getRelationship()).build());
		Organization cpsOrg = unifiedParameterFacility.loadParameter(null, Organization.class);
/*		downMsgFacility.sendMessage(cpsOrg.messageTemplates.get(CPSMessageCategory.CPS013), null, ccsCustomer.getName(), ccsCustomer.getGender(),
				ccsCustomer.getMobileNo(), new Date(),
				new MapBuilder<String, Object>().add("contactName", tmContact.getName()).add("relationship", tmContact.getRelationship()).build());

*/	}

	/**
	 * @see 方法名：S11040_OPT_THREE
	 * @see 描述：删除客户联系人信息
	 * @see 创建日期：2015年6月25日下午6:06:11
	 * @author yanjingfeng
	 * 
	 * @param tmContact
	 * @param ccsCustomer
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void S11040_OPT_THREE(CcsLinkman tmContact, CcsCustomer ccsCustomer) {
		rCcsLinkMan.delete(tmContact.getLinkmanId());
		Organization cpsOrg = unifiedParameterFacility.loadParameter(null, Organization.class);
		// TODO 没有卡号获取不到卡产品代码，获取不了短信模板编号
		// messageService.sendMessage(MessageCategory.M13,
		// ccsCustomer.getName(),
		// ccsCustomer.getGender(), ccsCustomer.getMobileNo(),
		// ccsCustomer.getEmail(),
		// new Date(), new MapBuilder().add("beDeletedcontactName",
		// contactInfo.get(CcsLinkman.P_Name)).add("relationship",
		// contactInfo.get(CcsLinkman.P_Relationship)).build());
/*		downMsgFacility.sendMessage(cpsOrg.messageTemplates.get(CPSMessageCategory.CPS013), null, ccsCustomer.getName(), ccsCustomer.getGender(),
				ccsCustomer.getMobileNo(), new Date(),
				new MapBuilder().add("beDeletedcontactName", tmContact.getCorpName()).add("relationship", tmContact.getRelationship()).build());
*/
	}

	/**
	 * @see 方法名：S11040_OPT_TWO
	 * @see 描述：新增客户联系人信息
	 * @see 创建日期：2015年6月25日下午6:06:21
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @param ccsCustomer
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	public void S11040_OPT_TWO(S11040Req req, CcsCustomer ccsCustomer) {
		CcsLinkman inputCcsLinkman = new CcsLinkman();
		inputCcsLinkman.setOrg(OrganizationContextHolder.getCurrentOrg());
		inputCcsLinkman.setCustId(ccsCustomer.getCustId());
		inputCcsLinkman.setBirthday(req.getBirthday());
		inputCcsLinkman.setCorpFax(req.getCorp_fax());
		inputCcsLinkman.setCorpName(req.getCorp_name());
		inputCcsLinkman.setCorpTelephNbr(req.getCorp_phone());
		inputCcsLinkman.setCorpPosition(req.getCorp_post());
		inputCcsLinkman.setName(req.getName());
		inputCcsLinkman.setRelationship(req.getRelationship());
		inputCcsLinkman.setGender(req.getGender());
		inputCcsLinkman.setMobileNo(req.getMobile_no());
		// 如果是身份证或临时身份证，则证件后的字母需转换为大写
		if (req.getContact_id_type() != null && (req.getContact_id_type().equals(IdType.I) || req.getContact_id_type().equals(IdType.T))) {
			// 验证身份证是否合法
			if (IdentificationCodeUtil.isIdentityCode(req.getContact_id_no())) {
				inputCcsLinkman.setIdNo(req.getContact_id_no().toString().toUpperCase());
			} else {
				throw new ProcessException(Constants.ERRB056_CODE, Constants.ERRB056_MES);
			}
		} else {
			inputCcsLinkman.setIdNo(req.getContact_id_no());
		}
		inputCcsLinkman.setIdType(req.getContact_id_type());
		rCcsLinkMan.save(inputCcsLinkman);
	}

}
