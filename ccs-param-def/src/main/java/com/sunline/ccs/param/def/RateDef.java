package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.math.BigDecimal;

import com.sunline.ark.support.meta.PropertyInfo;

public class RateDef implements Serializable {
	
	private static final long serialVersionUID = 1L;
    
	@PropertyInfo(name="对应费用比率", length=7, precision=4)
    public BigDecimal rate;

    @PropertyInfo(name="透支金额分阶最大值", length=15, precision=2)
    public BigDecimal rateCeil;

    @PropertyInfo(name="对应费用固定附加", length=15, precision=2)
    public BigDecimal rateBase;
}
