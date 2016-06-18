package com.sunline.ccs.service.nfcontrol;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ark.support.utils.CodeMarkUtils;

/** 
 * @see 类名：NonActiveStateQuery
 * @see 描述：查询卡片是否激活，激活返回false，未激活返回ture
 *
 * @see 创建日期：   2015年6月24日 下午2:40:09
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class NonActiveStateQuery implements IControlFieldStateQuery {

	private static final String P_ActiveInd = "activeInd";
	
	private Logger logger  = LoggerFactory.getLogger(getClass());
	
	@Resource(name = "mmCardService")
	private MmCardService mmCardService;
	/*
	 * 查询卡片是否激活，激活返回false，未激活返回ture
	 * (non-Javadoc)
	 * @see com.sunline.ccs.service.provide.IControlFieldStateQuery#process(java.lang.String)
	 */
	@Override
	public boolean process(String cardNbr) throws ProcessException {

		logger.debug("开始执行卡片激活状态的查询,卡号后四位{}",CodeMarkUtils.subCreditCard(cardNbr));
		// 调用MPS服务获取介质卡片信息
		Map<String, Serializable> map = mmCardService.MS3102(cardNbr);
		boolean flag = false;
		Indicator activeInd = Indicator.valueOf(map.get(P_ActiveInd).toString());
		if(activeInd == Indicator.N ){
			flag = true;
		}
		
		logger.debug("卡片激活状态的查询，查询的状态{}",flag);
		
		return flag;
	}
}
