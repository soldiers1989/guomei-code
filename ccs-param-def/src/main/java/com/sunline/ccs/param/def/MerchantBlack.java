package com.sunline.ccs.param.def;

import com.sunline.ark.support.meta.PropertyInfo;
import java.io.Serializable;

/**
 * 商户黑名单表
 */
public class MerchantBlack implements Serializable {

	private static final long serialVersionUID = 6347674172121346177L;

	/**
     * 国家代码
     */
    @PropertyInfo(name="国家代码", length=3)
    public String countryCd;

    /**
     * 商户代码
     */
    @PropertyInfo(name="商户代码", length=15)
    public String merchantId;

    /**
     * 商户名称
     */
    @PropertyInfo(name="商户名称", length=128)
    public String merchantName;

    /**
     * 收单机构编号
     */
    @PropertyInfo(name="收单机构编号", length=11)
    public String acqInstId;

    /**
     * MCC
     */
    @PropertyInfo(name="MCC", length=4)
    public String mcc;

    /**
     * 商户地址
     */
    @PropertyInfo(name="商户地址", length=200)
    public String mercAddr;

    /**
     * 商户地址-区/县
     */
    @PropertyInfo(name="所在区/县", length=40)
    public String mercAddrDistrict;

    /**
     * 商户地址-城市
     */
    @PropertyInfo(name="所在城市", length=40)
    public String mercAddrCity;

    /**
     * 商户地址-省份
     */
    @PropertyInfo(name="所在省份", length=40)
    public String mercAddrState;

    /**
     * 商户地址-国家代码
     */
    @PropertyInfo(name="所在国家代码", length=3)
    public String mercAddrCtryCd;

    /**
     * 商户地址-邮政编码
     */
    @PropertyInfo(name="所在邮政编码", length=10)
    public String mercAddrZip;

    /**
     * 联系电话
     */
    @PropertyInfo(name="联系电话", length=20)
    public String phone;

    /**
     * 联系人姓名
     */
    @PropertyInfo(name="联系人姓名", length=80)
    public String contactName;
}
