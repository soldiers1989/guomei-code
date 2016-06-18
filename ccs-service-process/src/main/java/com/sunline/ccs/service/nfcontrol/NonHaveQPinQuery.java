
package com.sunline.ccs.service.nfcontrol;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.exception.ProcessException;


/** 
 * @see 类名：NonHaveQPinQuery
 * @see 描述：是否有查询密码，没有true，有false
 *
 * @see 创建日期：   2015年6月24日 下午2:41:25
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class NonHaveQPinQuery implements IControlFieldStateQuery{
	private Logger logger  = LoggerFactory.getLogger(getClass());
	
	@Resource(name = "mmCardService")
	private MmCardService mmCardService;

	/*
	 * 是否有查询密码，没有true，有false
	 * (non-Javadoc)
	 * @see com.sunline.ccs.service.provide.IControlFieldStateQuery#process(java.lang.String)
	 */
	@Override
	public boolean process(String cardNbr) throws ProcessException {
		logger.debug("查询是否有查询密码开始");
		Boolean bool = mmCardService.MS3501(cardNbr);
		logger.debug("查询是否有查询密码结束");
		// 是否有查询密码，没有true，有false
		return !bool;
	}

}
