package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 验证类型
* @author fanghj
 *
 */
@EnumInfo({
	"CardNotMotoVerifyCvv2|行内无卡消费posmoto手工类强制cvv2",//AIC2.7银联升级-修改描述为行内
	"CardNotElectronVerifyCvv2|行内无卡消费posmoto电子类强制cvv2",//AIC2.7银联升级-修改描述为行内
	"IcArqcVerify|ic卡arqc验证",
	"ManualAuthVerifyCvv2|人工授权交易是否强制验证cvv2",
	"CashVerify|取现强制验密",
	"AtmVerify|ATM交易强制验密",
	"InstalmentExpenseVerify|分期消费强制验密",
	"CardNotExpenseElectronVerifyPassword|无卡消费电子类强制验密"
})
public enum VerifyEnum {
	/**
	 * 行内无卡消费posmoto手工类强制cvv2
	 * AIC2.7银联升级-修改描述为行内
	 */
	CardNotMotoVerifyCvv2,
	/**
	 * 行内无卡消费posmoto电子类强制cvv2
	 * AIC2.7银联升级-修改描述为行内
	 */
	CardNotElectronVerifyCvv2,
	/**
	 * ic卡arqc验证
	 */
	IcArqcVerify,
	/**
	 * 人工授权交易是否强制验证cvv2
	 */
	ManualAuthVerifyCvv2,
	
	CashVerify,
	AtmVerify,
	InstalmentExpenseVerify,
	CardNotExpenseElectronVerifyPassword
}
