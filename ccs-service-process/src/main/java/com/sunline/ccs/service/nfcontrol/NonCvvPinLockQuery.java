
package com.sunline.ccs.service.nfcontrol;

import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.CntType;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：NonCvvPinLockQuery
 * @see 描述：查询CVV是否锁定，锁定true，没锁定false
 *
 * @see 创建日期：   2015年6月24日 下午2:40:50
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class NonCvvPinLockQuery implements IControlFieldStateQuery{
	
	private Logger logger  = LoggerFactory.getLogger(getClass());
	
	@Resource(name = "mmCardService")
	private MmCardService mmCardService;

	/*
	 *  查询CVV是否锁定，锁定true，没锁定false
	 * (non-Javadoc)
	 * @see com.sunline.ccs.service.provide.IControlFieldStateQuery#process(java.lang.String)
	 */
	@Override
	public boolean process(String cardNbr) throws ProcessException {
		logger.debug("查询CVV是否锁定开始");
		Map<CntType, Boolean> map = mmCardService.MS3105(cardNbr, "");
		logger.debug("查询CVV是否锁定结束");
		
		return map.get(CntType.cvn);
	}

}
