package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * @about 自助類型 非自助/无卡自助/有卡自助
* @author fanghj
 * @date 2012-8-10 上午11:28:54
 */
@EnumInfo({ "NoSelfService|非自助", "NoCardSelfService|无卡自助","ACardSelfService|有卡自助" })
/**
 * 自助類型
 */
public enum AutoType {
	NoSelfService, NoCardSelfService, ACardSelfService
}
