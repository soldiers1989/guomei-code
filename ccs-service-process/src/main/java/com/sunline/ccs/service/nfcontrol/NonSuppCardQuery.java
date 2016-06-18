
package com.sunline.ccs.service.nfcontrol;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.BscSuppIndicator;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ark.support.utils.CodeMarkUtils;

/** 
 * @see 类名：NonSuppCardQuery
 * @see 描述：主附卡标志查询,主卡返回false，附卡返回true
 *
 * @see 创建日期：   2015年6月24日 下午2:42:14
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class NonSuppCardQuery implements IControlFieldStateQuery{
	private Logger logger  = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	/*
	 * 主附卡标志查询,主卡返回false，附卡返回true
	 * (non-Javadoc)
	 * @see com.sunline.ccs.service.provide.IControlFieldStateQuery#process(java.lang.String)
	 */
	@Override
	public boolean process(String cardNbr) throws ProcessException {
		logger.debug("主附卡标志查询开始，卡号后四位{}",CodeMarkUtils.subCreditCard(cardNbr));
		CheckUtil.checkCardNo(cardNbr);
		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(cardNbr);
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);
		
		logger.debug("主附卡标志查询结束");
		//如果为主卡返回false，附卡返回true
		return CcsCard.getBscSuppInd() == BscSuppIndicator.S ? true : false;
	}

}
