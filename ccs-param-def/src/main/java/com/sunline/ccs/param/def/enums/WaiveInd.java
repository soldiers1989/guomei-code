package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 收费标识
* @author fanghj
 *
 */
@EnumInfo({
	"A|无条件免费",
	"N|无条件收费",
	"O|发卡分行免费"
})
public enum WaiveInd {
	
     /**
     * 无条件免费always_waive
     */
    A,
    /**
     * 无条件收费never_waive
     */
    N,
    /**
     * 发卡分行免费issuer_owningBranch_acq_waive
     */
    O
}
