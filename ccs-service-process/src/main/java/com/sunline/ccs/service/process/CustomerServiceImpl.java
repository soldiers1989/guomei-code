package com.sunline.ccs.service.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import com.sunline.ark.support.utils.CodeMarkUtils;
import com.sunline.ark.support.utils.MapBuilder;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.server.repos.RCcsAddress;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAddress;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomerCrlmt;
import com.sunline.ccs.infrastructure.shared.model.CcsLinkman;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAddress;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsLinkman;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;
import com.sunline.ccs.service.provide.CallOTBProvide;
import com.sunline.ccs.service.util.CPSServProBusUtil;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.api.CcCustomerService;
import com.sunline.ppy.dictionary.enums.AddressType;
import com.sunline.ppy.dictionary.enums.IdType;
import com.sunline.ppy.dictionary.enums.OperationType;
import com.sunline.ppy.dictionary.exception.ProcessException;
//import com.sunline.smsd.service.sdk.DownMsgFacility;

/** 
 * @see 类名：CustomerServiceImpl
 * @see 描述：客户类服务接口
 *
 * @see 创建日期：   2015年6月24日 下午2:47:37
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class CustomerServiceImpl implements CcCustomerService {

	QCcsCustomer qCcsCustomer = QCcsCustomer.ccsCustomer;
	QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
	QCcsAddress qCcsAddress = QCcsAddress.ccsAddress;
	QCcsLinkman qCcsLinkman = QCcsLinkman.ccsLinkman;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private RCcsCustomer rCustomer;

	@Autowired
	private RCcsCustomer rCcsCustomer;

	@Autowired
	private RCcsAddress rCcsAddress;

	@Autowired
	private RCcsLinkman rCcsLinkman;

	@Autowired
	private CustAcctCardFacility queryFacility;

	@Autowired
	private CallOTBProvide callOTBProvide;
/*    @Autowired
    private DownMsgFacility downMsgFacility;
*///	@Autowired
//	private MessageService messageService;
	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;


	// 根据卡号查询客户基本信息
	// 客户信息 = CcsCustomer+tmCustLimtO+CASH_OTB(取现额度）+OTB(可用额度）
	// 其中如果是附卡的话，不返回tmCustLimtO
	@Override
	@Transactional
	public Map<String, Serializable> NF1101(String cardNbr) throws ProcessException {
		log.info("NF1101:卡号后四位[" + CodeMarkUtils.subCreditCard(cardNbr) + "]");
		CheckUtil.checkCardNo(cardNbr);
		CcsCustomer ccsCustomer = queryFacility.getCustomerByCardNbr(cardNbr);
		CheckUtil.rejectNull(ccsCustomer, "卡号[" + cardNbr + "]查询不到对应的客户信息");
		Map<String, Serializable> ccsCustomerMap = genCcsCustomer(ccsCustomer);
		LogTools.printObj(log, ccsCustomerMap, "NF1101返回数据:");
		return ccsCustomerMap;
	}

	// 根据证件号查询客户对象 客户信息 = CcsCustomer+tmCustLimtO+CASH_OTB(取现额度）+OTB(可用额度）
	// 其中如果是附卡的话，不返回tmCustLimtO
	@Override
	@Transactional
	public Map<String, Serializable> NF1102(IdType idType, String idNo) throws ProcessException {
		log.info("NF1102:证件类型[" + idType + "],证件号码[" + CodeMarkUtils.markIDCard(idNo) + "]");
		if (!CheckUtil.isIdNo(idType, idNo)) {
			throw new ProcessException("非法的证件类型或证件号码");
		}
		// 根据证件类型，证件号码查询TM_CUSTOMER主表
		CcsCustomer ccsCustomer = rCustomer.findOne(qCcsCustomer.idType.eq(idType).and(qCcsCustomer.idNo.eq(idNo).and(qCcsCustomer.org.eq(OrganizationContextHolder.getCurrentOrg()))));
		CheckUtil.rejectNull(ccsCustomer, "证件类型[" + idType + "],证件号码[" + idNo + "]查询不到对应的客户信息");
		Map<String, Serializable> ccsCustomerMap = genCcsCustomer(ccsCustomer);
		LogTools.printObj(log, ccsCustomerMap, "NF1102返回数据:");
		return ccsCustomerMap;
	}

	// 根据客户号查询客户基本信息
	@Override
	@Transactional
	public Map<String, Serializable> NF1103(Integer custID) throws ProcessException {
		log.info("NF1103:客户号[" + custID + "]");
		CheckUtil.checkCustomer(Long.valueOf(custID));
		CcsCustomer ccsCustomer = rCcsCustomer.findOne(qCcsCustomer.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsCustomer.custId.eq(Long.valueOf(custID))));
		CheckUtil.rejectNull(ccsCustomer, "客户号[" + custID + "]查询不到对应的客户信息");
		Map<String, Serializable> ccsCustomerMap = genCcsCustomer(ccsCustomer);
		LogTools.printObj(log, ccsCustomerMap, "NF1103返回数据:");
		return ccsCustomerMap;
	}

	// 根据客户号查询地址信息列表
	@Override
	@Transactional
	public List<Map<String, Serializable>> NF1105(Integer custID) throws ProcessException {
		log.info("NF1105:客户号[" + custID + "]");
		CheckUtil.checkCustomer(Long.valueOf(custID));
		// 根据客户号查询客户地址信息
		Iterator<CcsAddress> iter = rCcsAddress.findAll(qCcsAddress.custId.eq(Long.valueOf(custID)).and(qCcsAddress.org.eq(OrganizationContextHolder.getCurrentOrg()))).iterator();
		if (!iter.hasNext()) {
			log.error("客户号[" + custID + "]查询不到对应的地址信息");
//			throw new ProcessException("客户号[" + custID + "]查询不到对应的地址信息");
		}
		ArrayList<Map<String, Serializable>> addressList = new ArrayList<Map<String, Serializable>>();
		while (iter.hasNext()) {
			addressList.add(iter.next().convertToMap());
		}
		LogTools.printObj(log, addressList, "NF1105返回数据:");
		return addressList;
	}

	// 根据客户号查询联系人信息列表
	@Override
	@Transactional
	public List<Map<String, Serializable>> NF1107(Integer custID) throws ProcessException {
		log.info("NF1107:客户号[" + custID + "]");
		CheckUtil.checkCustomer(Long.valueOf(custID));
		Iterable<CcsLinkman> iter = rCcsLinkman.findAll(qCcsLinkman.custId.eq(Long.valueOf(custID)).and(qCcsLinkman.org.eq(OrganizationContextHolder.getCurrentOrg())));
		ArrayList<Map<String, Serializable>> tmContactList = new ArrayList<Map<String, Serializable>>();
		for (CcsLinkman tmContact : iter) {
			tmContactList.add(tmContact.convertToMap());
		}
		LogTools.printObj(log, tmContactList, "NF1107返回数据:");
		return tmContactList;
	}

	// 更新客户基本信息
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@Transactional
	public void NF1201(Map<String, Serializable> customer) throws ProcessException {
		log.info("NF1201:客户号[" + customer.get(CcsCustomer.P_CustId) + "]");
		LogTools.printObj(log, customer, "上送的客户信息");
		CcsCustomer ccsCustomerOld = new CcsCustomer();
		ccsCustomerOld.updateFromMap(customer);
		// 检查上送的客户信息是否合法，不合法则抛出对应的错误异常信息
		CheckUtil.checkInputCustomer(ccsCustomerOld);
		CcsCustomer ccsCustomer = rCustomer.findOne(qCcsCustomer.custId.eq((Long) customer.get(CcsCustomer.P_CustId)).and(qCcsCustomer.org.eq(OrganizationContextHolder.getCurrentOrg())));
		CheckUtil.rejectNull(ccsCustomer, "查询不到对应的客户信息");

		// 原手机号
		String oldPhone = ccsCustomer.getMobileNo();
		// 原家庭电话
		String oldHomePhone = ccsCustomer.getHomePhone();
		// 原问题答案
		String oldAnswer = ccsCustomer.getSecureAnswer();
		// 原问题
		String oldQuestion = ccsCustomer.getSecureQuestion();

		ccsCustomer.updateFromMap(customer);

		// 新手机号
		String newPhone = ccsCustomer.getMobileNo();
		// 新家庭电话
		String newHomePhone = ccsCustomer.getHomePhone();
		// 新问题答案
		String newAnswer = ccsCustomer.getSecureAnswer();
		// 新问题
		String newQuestion = ccsCustomer.getSecureQuestion();
		
		Organization cpsOrg = unifiedParameterFacility.loadParameter(null, Organization.class); 
		// 预留手机号码变更提醒
		if (!StringUtils.equals(oldPhone, newPhone)) {
//			messageService.sendMessage(MessageCategory.M01, ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), 
//					ccsCustomer.getEmail(), new Date(), 
//					new MapBuilder()
//						.add("oldPhone", oldPhone)
//						.add("newPhone", newPhone)
//						.add("phoneType", "手机号码")
//						.build());
		  // 从机构层发短信
			
/*			downMsgFacility.sendMessage(cpsOrg.messageTemplates.get(CPSMessageCategory.CPS001), null,ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), 
					 new Date(), 
					new MapBuilder()
						.add("oldPhone", oldPhone)
						.add("newPhone", newPhone)
						.add("phoneType", "手机号码")
						.build());
*/		}

		// 预留家庭电话变更提醒
		if (!StringUtils.equals(oldHomePhone, newHomePhone)) {
//			messageService.sendMessage(MessageCategory.M01, ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), 
//					ccsCustomer.getEmail(), new Date(), 
//					new MapBuilder()
//						.add("oldPhone", oldHomePhone)
//						.add("newPhone", newHomePhone)
//						.add("phoneType", "家庭电话")
//						.build());
			//从机构层发短信
/*			downMsgFacility.sendMessage(cpsOrg.messageTemplates.get(CPSMessageCategory.CPS001),null, ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), 
					 new Date(), 
					new MapBuilder()
						.add("oldPhone", oldHomePhone)
						.add("newPhone", newHomePhone)
						.add("phoneType", "家庭电话")
						.build());
*/		}
		
		// 预留问题答案变更提醒
		if (!StringUtils.equals(oldAnswer, newAnswer) || !StringUtils.equals(oldQuestion, newQuestion)) {
//			messageService.sendMessage(MessageCategory.M03, ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), 
//					ccsCustomer.getEmail(), new Date(), new MapBuilder().build());
/*			downMsgFacility.sendMessage(cpsOrg.messageTemplates.get(CPSMessageCategory.CPS001), null,ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), 
					 new Date(), new MapBuilder().build());
*/		}
	}

	// 更新客户信息,如果上送的客户地址类型不存在，则为新增
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@Transactional
	public void NF1202(Integer custID, Map<String, Serializable> addrInfo,OperationType opt) throws ProcessException {
		log.info("NF1202:客户号[" + custID + "]");
		LogTools.printObj(log, addrInfo, "上送的客户地址信息");
		CheckUtil.checkCustomer(Long.valueOf(custID));
		CcsAddress inputCcsAddress = new CcsAddress();
		inputCcsAddress.updateFromMap(addrInfo);
		CheckUtil.rejectNull(inputCcsAddress.getAddrType(), "上送的地址类型不能为空");
		
		Organization cpsOrg = unifiedParameterFacility.loadParameter(null, Organization.class); 
		// 预留地址变更提醒
		CcsCustomer ccsCustomer = rCustomer.findOne(qCcsCustomer.custId.eq(Long.valueOf(custID)).and(qCcsCustomer.org.eq(OrganizationContextHolder.getCurrentOrg())));
		if(opt.equals(OperationType.D)){
		  rCcsAddress.delete(Long.valueOf(addrInfo.get(CcsAddress.P_AddrId).toString()));
//		  messageService.sendMessage(MessageCategory.M02, ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), 
//					ccsCustomer.getEmail(), new Date(), 
//					new MapBuilder()
//						.add("beDeletedAddress", addrInfo.get(CcsAddress.P_Address))
//						.add("beDeletedaddressType", addrInfo.get(CcsAddress.P_AddrType))
//						.build());
/*		 downMsgFacility.sendMessage(cpsOrg.messageTemplates.get(CPSMessageCategory.CPS002), null, ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), 
					 new Date(), 
					new MapBuilder()
						.add("beDeletedAddress", addrInfo.get(CcsAddress.P_Address))
						.add("beDeletedaddressType", addrInfo.get(CcsAddress.P_AddrType))
						.build());
*/		  return;
	   }
		
		// 邮政编码校验
		if (StringUtils.isNotEmpty(inputCcsAddress.getPostcode())) {
			if (!CheckUtil.checkPostcode(inputCcsAddress.getPostcode())) {
				throw new ProcessException("无效的邮政编码");
			}
		}
		// 电话号码校验，可以不空，如果不为空，则需要合法
		if (!StringUtils.isEmpty(inputCcsAddress.getPhone())) {
			if (!(CheckUtil.isPhoneNr(inputCcsAddress.getPhone()) || CheckUtil.isPhoneNrWithoutCode(inputCcsAddress.getPhone()) || CheckUtil.isPhoneNrWithoutLine(inputCcsAddress.getPhone())|| CheckUtil.isPhone(inputCcsAddress.getPhone()))) {
				log.error("非法的电话号码[" + inputCcsAddress.getPhone() + "]");
				throw new ProcessException("电话号码输入错误，只能输入0XX-XXXXXXXX;XXXXXXXX;0XXXXXXXXXX或手机号");
			}
		}
	
		CcsAddress address = rCcsAddress.findOne(qCcsAddress.custId.eq(Long.valueOf(custID)).and(qCcsAddress.addrType.eq(inputCcsAddress.getAddrType()).and(qCcsAddress.org.eq(OrganizationContextHolder.getCurrentOrg()))));
		// 如果找不到对应的地址，则为增加信息的地址
		if (opt.equals(OperationType.A)) {
			inputCcsAddress.setOrg(OrganizationContextHolder.getCurrentOrg());
			inputCcsAddress.setCustId(Long.valueOf(custID));
			rCcsAddress.save(inputCcsAddress);
		} else {
			String oldPhone = address.getPhone();
			String oldAddress = address.getAddress();
			String oldPostcode = address.getPostcode();
			
			address.updateFromMap(addrInfo);
			
			String newPhone = address.getPhone();
			String newAddress = address.getAddress();
			String newPostcode = address.getPostcode();
			
			// 判断修改的地址是否为账单的邮寄地址，如果是，需要同步更新
			JPAQuery query = new JPAQuery(em);
			BooleanExpression bp = qCcsAcct.org.eq(OrganizationContextHolder.getCurrentOrg()).and(qCcsAcct.custId.eq(Long.valueOf(custID)).and(qCcsAcct.stmtMailAddrInd.eq(inputCcsAddress.getAddrType())));
			List<CcsAcct> CcsAcctList = query.from(qCcsAcct).where(bp).list(qCcsAcct);
			for (CcsAcct CcsAcct : CcsAcctList) {
				CcsAcct.setStmtCountryCode(address.getCountryCode());
				CcsAcct.setStmtCity(address.getCity());
				CcsAcct.setStmtDistrict(address.getDistrict());
				CcsAcct.setStmtState(address.getState());
				CcsAcct.setStmtPostcode(address.getPostcode());
				CcsAcct.setStmtAddress(address.getAddress());
			}
			
		
			//此代码有问题，oldPostcode可能为空，会抛空指针异常
		//	if(!oldAddress.equals(newAddress) || !oldPostcode.equals(newPostcode)){	
			if(!oldAddress.equals(newAddress) || (oldPostcode != null && !oldPostcode.equals(newPostcode))){	
//			messageService.sendMessage(MessageCategory.M02, ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), 
//					ccsCustomer.getEmail(), new Date(), 
//					new MapBuilder()
//						.add("newAddress", address.getAddress())
//						.add("addressType", inputCcsAddress.getAddrType())
//						.build());
//			}
				  //从机构层发短信
/*				downMsgFacility.sendMessage(cpsOrg.messageTemplates.get(CPSMessageCategory.CPS002),null,ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), 
					     new Date(), 
						new MapBuilder()
							.add("newAddress", address.getAddress())
							.add("addressType", inputCcsAddress.getAddrType())
							.build());
*/				}
				if (!StringUtils.equals(oldPhone, newPhone)) {
//					messageService.sendMessage(MessageCategory.M01, ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), 
//							ccsCustomer.getEmail(), new Date(), 
//							new MapBuilder()
//								.add("oldPhone", oldPhone)
//								.add("newPhone", newPhone)
//								.add("phoneType", CPSServProBusUtil.getEnumInfo(inputCcsAddress.getAddrType())+"对应的电话")
//								.build());
					 //从机构层发短信
/*				downMsgFacility.sendMessage(cpsOrg.messageTemplates.get(CPSMessageCategory.CPS002),null, ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), 
							new Date(), 
							new MapBuilder()
								.add("oldPhone", oldPhone)
								.add("newPhone", newPhone)
								.add("phoneType", CPSServProBusUtil.getEnumInfo(inputCcsAddress.getAddrType())+"对应的电话")
								.build());
*/			}
		}
	}

	// 更新客户联系人信息
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@Transactional
	public void NF1203(Integer custID, Map<String, Serializable> contactInfo,OperationType operationType) throws ProcessException {
		log.info("NF1203:客户号[" + custID + "],操作项为:["+operationType+"]");
		LogTools.printObj(log, contactInfo, "上送的联系人信息");
		CheckUtil.checkCustomer(Long.valueOf(custID));
		CcsLinkman inputCcsLinkman = new CcsLinkman();
		inputCcsLinkman.updateFromMap(contactInfo);
		CheckUtil.rejectNull(inputCcsLinkman.getRelationship(), "上送的与持卡人关系字段不能为空");
		Organization cpsOrg = unifiedParameterFacility.loadParameter(null, Organization.class);  
		// 预留联系人信息变更短信
		CcsCustomer ccsCustomer = rCcsCustomer.findOne(Long.valueOf(custID));
		if(operationType.equals(OperationType.D)){
			rCcsLinkman.delete(Long.valueOf(contactInfo.get(CcsLinkman.P_LinkmanId).toString()));
			 //TODO 没有卡号获取不到卡产品代码，获取不了短信模板编号
//			messageService.sendMessage(MessageCategory.M13, ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), ccsCustomer.getEmail(),
//					new Date(), new MapBuilder().add("beDeletedcontactName", contactInfo.get(CcsLinkman.P_Name)).add("relationship", contactInfo.get(CcsLinkman.P_Relationship)).build());
/*			downMsgFacility.sendMessage(cpsOrg.messageTemplates.get(CPSMessageCategory.CPS013), null, ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), 
					new Date(), new MapBuilder().add("beDeletedcontactName", contactInfo.get(CcsLinkman.P_Name)).add("relationship", contactInfo.get(CcsLinkman.P_Relationship)).build());
*/			return;
		 }

		if (!CheckUtil.isPhone(inputCcsLinkman.getMobileNo())) {
			log.error("无效的移动电话[" + inputCcsLinkman.getMobileNo() + "]");
			throw new ProcessException("无效的移动电话");
		}

		// 判断证件类型
		if (!CheckUtil.isIdNo(inputCcsLinkman.getIdType(), inputCcsLinkman.getIdNo())) {
			throw new ProcessException("非法的证件类型或证件号码");
		}
		
		// 生日和身份证号交叉验证
		if (inputCcsLinkman.getIdType() == IdType.I) {
			if (!CheckUtil.isBirthday(inputCcsLinkman.getBirthday(),inputCcsLinkman.getIdNo())) {
				throw new ProcessException("生日与身份证不匹配");
			}
		}
		if (!StringUtils.isEmpty(inputCcsLinkman.getCorpTelephNbr())) {
			if (!(CheckUtil.isPhoneNr(inputCcsLinkman.getCorpTelephNbr()) || CheckUtil.isPhoneNrWithoutCode(inputCcsLinkman.getCorpTelephNbr()) || CheckUtil.isPhoneNrWithoutLine(inputCcsLinkman.getCorpTelephNbr()))) {
				log.error("非法的电话号码[" + inputCcsLinkman.getCorpTelephNbr() + "]");
				throw new ProcessException("公司电话输入错误，只能输入0XX-XXXXXXXX-XX;XXXXXXXX;0XXXXXXXXXX或手机号");
			}
		}
		CcsLinkman tmcontact = rCcsLinkman.findOne(qCcsLinkman.custId.eq(Long.valueOf(custID)).and(qCcsLinkman.relationship.eq(inputCcsLinkman.getRelationship()).and(qCcsLinkman.org.eq(OrganizationContextHolder.getCurrentOrg()))));
		// 如果查找的联系人信息为空，则为增加新的联系人信息

         if(operationType.equals(OperationType.A)){
        	 inputCcsLinkman.setOrg(OrganizationContextHolder.getCurrentOrg());
    		 rCcsLinkman.save(inputCcsLinkman);
        	 
         }
         else if(operationType.equals(OperationType.U)){
        	 tmcontact.updateFromMap(contactInfo);
        	 
         }
         
//		messageService.sendMessage(MessageCategory.M13, ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(), ccsCustomer.getEmail(),
//				new Date(), new MapBuilder().add("contactName", tmcontact == null ? inputCcsLinkman.getName() : tmcontact.getName()).add("relationship", tmcontact == null ? inputCcsLinkman.getRelationship() : tmcontact.getRelationship()).build());
/*         downMsgFacility.sendMessage(cpsOrg.messageTemplates.get(CPSMessageCategory.CPS013),null, ccsCustomer.getName(), ccsCustomer.getGender(), ccsCustomer.getMobileNo(),
 				new Date(), new MapBuilder().add("contactName", tmcontact == null ? inputCcsLinkman.getName() : tmcontact.getName()).add("relationship", tmcontact == null ? inputCcsLinkman.getRelationship() : tmcontact.getRelationship()).build());
*/	}

	/**
	 * 客户信息的合并
	 * 
	 * @param ccsCustomer
	 *            客户表 客户限额表
	 * @return 客户信息 = CcsCustomer+tmCustLimtO+CASH_OTB(取现额度）+OTB(可用额度）
	 * @throws ProcessException
	 */
	private Map<String, Serializable> mergeMap(CcsCustomer ccsCustomer, CcsCustomerCrlmt tmCustLimtO) throws ProcessException {
		Map<String, Serializable> mapTmCustor = ccsCustomer.convertToMap();
		if (!CheckUtil.isEmpty(tmCustLimtO)) {
			mapTmCustor.putAll(tmCustLimtO.convertToMap());
			List<CcsAcctO> accounto = queryFacility.getAcctOByCustId(ccsCustomer.getCustId());
			if (accounto.isEmpty()) {
				throw new ProcessException("计算客户层OTB查询不到对应的账户信息");
			}
			callOTBProvide.setCustomerOTB(ccsCustomer, tmCustLimtO, accounto, new Date(), mapTmCustor);
		}
		return mapTmCustor;
	}
	

	/**
	 * 生成TmCustOmer信息
	 * 
	 * @param ccsCustomer
	 * @return
	 * @throws ProcessException
	 */
	private Map<String, Serializable> genCcsCustomer(CcsCustomer ccsCustomer) throws ProcessException {
		// 如果custLmtId为空的话，不返回tmCustLimtO
		if (ccsCustomer.getCustLmtId() != null) {
			return mergeMap(ccsCustomer, queryFacility.getCustomerCrLmtByCustLmtId(ccsCustomer.getCustLmtId()));
		} else {
			return mergeMap(ccsCustomer, null);
		}
	}

	// 根据卡号查询姓名、地址、电话等信息
	@Override
	@Transactional
	public Map<String, Serializable> NF0000(String cardNbr, AddressType addType) throws ProcessException {
		log.info("NF000:卡号[" + CodeMarkUtils.subCreditCard(cardNbr) + "],地址类型[" + addType + "]");
		CheckUtil.checkCardNo(cardNbr);
		CcsCustomer ccsCustomer = queryFacility.getCustomerByCardNbr(cardNbr);
		CheckUtil.rejectNull(ccsCustomer, "卡号[" + cardNbr + "]找不到对应的客户信息");
		CcsAddress address = rCcsAddress.findOne(qCcsAddress.custId.eq(ccsCustomer.getCustId()).and(qCcsAddress.addrType.eq(addType)));
		CheckUtil.rejectNull(address, "卡号[" + cardNbr + "],地址类型[" + addType + "]查询不到对应的客户地址信息");
		Map<String, Serializable> ccsCustomerMap = ccsCustomer.convertToMap();
		ccsCustomerMap.putAll(address.convertToMap());
		LogTools.printObj(log, ccsCustomerMap, "NF000返回数据");
		return ccsCustomerMap;
	}

	

}
