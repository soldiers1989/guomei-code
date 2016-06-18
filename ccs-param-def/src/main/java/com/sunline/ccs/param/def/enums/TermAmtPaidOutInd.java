package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 合同状态
* @author yuemk
 *
 */
@EnumInfo({
	"F|还清", 
    "W|未还清",
    "O|未到期"
})
public enum TermAmtPaidOutInd {
	/**
	 * 还清
	 */
	F,
	/**
	 * 未还清
	 */
	W,
	/**
	 * 未到期
	 */
	O
}
