package com.sunline.ccs.service.nfcontrol;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.param.def.NfContrlServMapping;
import com.sunline.ccs.param.def.NfControlField;
import com.sunline.ccs.service.protocol.ServiceControlResult;
import com.sunline.ark.support.utils.CodeMarkUtils;

/** 
 * @see 类名：ServiceControl
 * @see 描述：判断受卡片状态以及锁定码等信息的影响是否可以做此交易
 *
 * @see 创建日期：   2015年6月24日 下午2:43:27
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Component
public class ServiceControl implements BeanFactoryAware {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UnifiedParameterFacility unifiedParameterService;

	private BeanFactory beanFactory = null;

	public ServiceControlResult processControl(String serviceCode, String cardNbr) {
		logger.debug("处理服务控制{}，卡号{}", serviceCode,CodeMarkUtils.subCreditCard(cardNbr));
		NfContrlServMapping contrlMaping = unifiedParameterService.retrieveParameterObject(serviceCode, NfContrlServMapping.class);
		// 如果contrlMapping如果为空，没找到匹配的服务码，说明该服务不受控制，直接返回交易可以通过
		if (contrlMaping == null || contrlMaping.fieldCodeMap.isEmpty())
			return new ServiceControlResult();
		StringBuffer block = new StringBuffer();
		ArrayList<ServiceControlResult> resulsts = new ArrayList<ServiceControlResult>();
		for (String key : contrlMaping.fieldCodeMap.keySet()) {
			if (key.startsWith("block")) {
				block.append(StringUtils.delete(key, "block-"));
				continue;
			}
			if (beanFactory.containsBean(key)) {
				IControlFieldStateQuery servControl = (IControlFieldStateQuery) beanFactory.getBean(key);
				// 获取到卡片的状态
				boolean state = servControl.process(cardNbr);
				if (state) {
					Boolean keyValue = contrlMaping.fieldCodeMap.get(key);
					// false 为不能通过 true 为可以通过
					if (!keyValue) {
						NfControlField nfConrField = unifiedParameterService.loadParameter(key, NfControlField.class);
						ServiceControlResult result = new ServiceControlResult();
						result.setReturnCode(CtlFieldReturnCode.valueOf(key).getReturnCode());
						result.setReturnMessage(nfConrField.fieldName);
						result.setPass(false);
						resulsts.add(result);
					}
				}
			}
		}
		// 单独处理block部分
		BlockFieldStateQuery blockFieldState = (BlockFieldStateQuery) beanFactory.getBean("BlockField");
		ArrayList<ServiceControlResult> blockResultList = blockFieldState.process(cardNbr, block.toString());
		resulsts.addAll(blockResultList);

		/**
		 * 此处合并了ServiceControlResult的返回结果，即如果存在多个ServiceControlResult的情况下，
		 * returncode 返回第一个，而returnmessage 返回多个的合并
		 */
		
		boolean flag = true;
		String returnCode = "";
		int i=0;
		ServiceControlResult result = new ServiceControlResult();
		StringBuffer sb = new StringBuffer();
		for (ServiceControlResult tmp : resulsts) {
			flag &= tmp.isPass();
			if (tmp.isNotPass()){
				sb.append(tmp.getReturnMessage() + ",");
				if(i==0) returnCode = tmp.getReturnCode();
			}
			i++;
		}
		String msg = sb.toString();
		if(msg.endsWith(",")){
			msg = msg.substring(0,msg.length()-1);
		}

		 result.setPass(flag);
		 result.setReturnMessage(msg);
		 result.setReturnCode(returnCode);
		return result;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

}
