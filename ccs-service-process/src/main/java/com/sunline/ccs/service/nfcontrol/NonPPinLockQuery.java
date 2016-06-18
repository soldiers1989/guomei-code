
package com.sunline.ccs.service.nfcontrol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsCardO;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ark.support.utils.CodeMarkUtils;


/** 
 * @see 类名：NonPPinLockQuery
 * @see 描述：查询交易密码是否锁定，锁定true，没有锁定false
 *
 * @see 创建日期：   2015年6月24日 下午2:41:53
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class NonPPinLockQuery implements IControlFieldStateQuery{
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private UnifiedParamFacilityProvide unifiedParaFacilityProvide;
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	/*
	 * 查询交易密码是否锁定，锁定true，没有锁定false
	 * (non-Javadoc)
	 * @see com.sunline.ccs.service.provide.IControlFieldStateQuery#process(java.lang.String)
	 */
	@Override
	public boolean process(String cardNbr) throws ProcessException {
		logger.debug("查询交易密码是否锁定开始,卡号后四位{}", CodeMarkUtils.subCreditCard(cardNbr));
		CcsCardO CcsCardO = custAcctCardQueryFacility.getCardOByCardNbr(cardNbr);
		CheckUtil.rejectNull(CcsCardO, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		
		ProductCredit productCredit = unifiedParaFacilityProvide.productCredit(CcsCardO.getProductCd());
		logger.debug("查询交易密码是否锁定结束");
		
		return CcsCardO.getPinTries() >= productCredit.pinTry;
	}

}
