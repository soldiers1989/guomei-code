package com.sunline.ccs.facility;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.param.def.Organization;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.param.def.enums.CPSMessageCategory;

/**
 * 获取短信代码逻辑，如果产品中存在对应的短信类型，就从产品级获取，否则从机构级获取
* @author fanghj
 * 
 * 产品参数新增【启用机构层短信模板】，启用时保持原逻辑；禁用时若产品级短信模板不存在则不发送短信
* @author fanghj
 *
 */
@Service
public class FetchSmsNbrFacility {

	@Autowired
	private UnifiedParameterFacility unifiedParameterFacility;

	public String fetchMsgCd(String productCd, CPSMessageCategory category) {
		String msgCd = null;
		if(StringUtils.isNotBlank(productCd)){
			ProductCredit productCr = unifiedParameterFacility.loadParameter(productCd, ProductCredit.class);
			msgCd = productCr.messageTemplates.get(category);
			if(productCr.useOrgMessageTemplate == Indicator.N){
				return msgCd;
			}
		}
		if (StringUtils.isBlank(msgCd)) {
			Organization org = unifiedParameterFacility.loadParameter(null,Organization.class);
			msgCd = org.messageTemplates.get(category);
		}
		return msgCd;
	}
}
