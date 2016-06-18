package com.sunline.ccs.param.def.enums;

import com.sunline.ark.support.meta.EnumInfo;

/**
 * 计息开始日期类型
 */
@EnumInfo({"C|账单日", "P|入账日", "T|交易日"})
public enum IntAccumFrom
{
    /**
     * 账单日
     */
    C("账单日"),
    /**
     * 入账日
     */
    P("入账日"),
    /**
     * 交易日
     */
    T("交易日");
    
    /**
     * 描述
     */
    private String desc;
    
    /**
     * <默认构造函数>
     */
    private IntAccumFrom(String desc)
    {
        this.desc = desc;
    }
    
    /**
     * 获取描述
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public String getDesc()
    {
        return desc;
    }
    
    /**
     * 获取描述
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public String getKeyLabelDesc()
    {
        return this.name() + " - " + desc;
    }
}
