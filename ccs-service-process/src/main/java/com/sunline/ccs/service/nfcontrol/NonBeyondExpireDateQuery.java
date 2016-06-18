
package com.sunline.ccs.service.nfcontrol;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.service.util.DataTypeUtils;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.utils.CodeMarkUtils;

/** 
 * @see 类名：NonBeyondExpireDateQuery
 * @see 描述：是否有效期之外,是true，不是false
 *
 * @see 创建日期：   2015年6月24日 下午2:40:23
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class NonBeyondExpireDateQuery implements IControlFieldStateQuery{

	private Logger logger  = LoggerFactory.getLogger(getClass());
	
	private static final String P_ExpiryDate = "origExpiryDate";
	
	@Resource(name = "mmCardService")
	private MmCardService mmCardService;
	@Autowired
	private GlobalManagementService globalManagementService;
	/*
	 * 是否已过有效期查询，没过期true，过期false
	 * (non-Javadoc)
	 * @see com.sunline.ccs.service.provide.IControlFieldStateQuery#process(java.lang.String)
	 */
	@Override
	public boolean process(String cardNbr) throws ProcessException {
		logger.debug("开始执行是否已过有效期查询,卡号后四位{}",CodeMarkUtils.subCreditCard(cardNbr) ); 
		// 调用MPS服务获取介质卡片信息  
		Map<String, Serializable> map = mmCardService.MS3102(cardNbr);
		
		Date expiryDate = DataTypeUtils.getDateValue(map.get(P_ExpiryDate));
		Date date = globalManagementService.getSystemStatus().getBusinessDate();
		
		logger.debug("开始执行是否已过有效期查询结束");
		//没过期true，过期false
		return expiryDate.before(date);
	}

}
