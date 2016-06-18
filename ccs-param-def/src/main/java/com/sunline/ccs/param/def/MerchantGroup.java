package com.sunline.ccs.param.def;

import com.sunline.ark.support.meta.PropertyInfo;
import java.io.Serializable;

/**
 * 商户分组定义
 */
public class MerchantGroup implements Serializable {

	private static final long serialVersionUID = -2334051444735798663L;

	/**
     * 商户组ID
     */
    @PropertyInfo(name="商户组ID", length=4)
    public String merGroupId;

    /**
     * 商户组名称
     */
    @PropertyInfo(name="商户组名称", length=40)
    public String merGroupName;

    /**
     * 商户组备注
     */
    @PropertyInfo(name="商户组备注", length=200)
    public String merGroupDesc;
}
