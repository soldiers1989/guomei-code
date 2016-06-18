package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.util.List;

import com.sunline.ccs.param.def.enums.BucketObject;
import com.sunline.ark.support.meta.PropertyInfo;
import com.sunline.ark.support.meta.ReferEnums;

/**
 * BNP优先级参数
 */
public class PaymentHierarchy implements Serializable {

	private static final long serialVersionUID = -8173329801729058383L;

	/**
	 * 主键
	 */
	@PropertyInfo(name="还款冲销编号", length=9)
	public Integer pmtHierId;
	
	/**
	 * 描述
	 */
	@PropertyInfo(name="描述", length=40)
	public String description;
	
	/**
	 * BNP优先级
	 */
	@ReferEnums(BucketObject.class)
	public List<BucketObject> paymentHier;
}
