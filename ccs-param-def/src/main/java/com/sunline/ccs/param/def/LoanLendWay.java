package com.sunline.ccs.param.def;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 现金分期放款方式
* @author fanghj
 *
 */

@EnumInfo({
	"B|批量放款",
	"O|实时放款"
})
public enum LoanLendWay {

	/**
	 * 批量放款(Batch)
	 */
	B,
	/**
	 * 实时放款(Online)
	 */
	O
}
