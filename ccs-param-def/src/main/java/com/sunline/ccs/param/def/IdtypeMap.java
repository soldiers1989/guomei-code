package com.sunline.ccs.param.def;

import com.sunline.ark.support.meta.PropertyInfo;
import java.io.Serializable;

/**
 * 证件类型映射表
 */
public class IdtypeMap implements Serializable {

	private static final long serialVersionUID = 6616641883751727601L;

	/**
     * 国际组织
     * VISA
     * MC
     * CUP
     * JCB
     */
    @PropertyInfo(name="国际组织", length=8)
    public String institutionId;

    /**
     * 系统内证件类型
     */
    @PropertyInfo(name="系统内证件类型", length=1)
    public String idtypeSystem;

    /**
     * 发卡机构证件类型
     */
    @PropertyInfo(name="发卡机构证件类型", length=1)
    public String idtypeOrg;
}
