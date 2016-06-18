package com.sunline.ccs.param.def.enums;

import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * AcqId对账信息
 * @author wanghl
 *
 */
public enum AcqIdContactChnl {
	/**
	 * 马上消费金融股份有限公司
	 */
	E10000000("10000000","10000000",Indicator.Y,"10000000",Indicator.N,"10000000",Indicator.Y,"10000000",Indicator.N),
	/**
	 * 阳光财产保险股份有限公司
	 */
	E20000000("20000000","20000000",Indicator.Y,"10000000",Indicator.N,"20000000",Indicator.Y,"10000000",Indicator.N),
	/**
	 * 天翼电子商务有限公司广东分公司
	 */
	E20000001("20000001","20000001",Indicator.Y,"10000000",Indicator.N,"20000001",Indicator.Y,"10000000",Indicator.N),
	/**
	 * 百度股份有限公司
	 */
	E20000002("20000002","20000002",Indicator.Y,"10000000",Indicator.N,"20000002",Indicator.N,"10000000",Indicator.N),
	/**
	 * 深圳市腾讯计算机系统有限公司
	 */
	E20000003("20000003","20000003",Indicator.Y,"10000000",Indicator.N,"20000003",Indicator.Y,"10000000",Indicator.N),
	/**
	 * 深圳市随手科技有限公司 卡牛
	 */
	E20000004("20000004","20000004",Indicator.Y,"10000000",Indicator.N,"20000004",Indicator.Y,"10000000",Indicator.N),
	/**
	 * 重庆百货大楼股份有限公司
	 */
	E20000005("20000005","20000005",Indicator.N,"10000000",Indicator.N,"20000005",Indicator.N,"10000000",Indicator.N),
	/**
	 * 上海融之家金融信息服务有限公司 融之家/网贷之家
	 */
	E20000006("20000006","20000006",Indicator.Y,"10000000",Indicator.N,"20000006",Indicator.Y,"10000000",Indicator.N),
	/**
	 * 上海迅银互联网金融信息服务有限公司
	 */
	E20000007("20000007","20000007",Indicator.Y,"10000000",Indicator.N,"20000007",Indicator.Y,"10000000",Indicator.N),
	/**
	 * 广州卡宝宝互联网金融信息服务股份有限公司
	 */
	E20000008("20000008","20000008",Indicator.Y,"10000000",Indicator.N,"20000008",Indicator.Y,"10000000",Indicator.N);
	
	private String acqId;
	/**
	 * 外部放款对账方
	 */
	private String externalLoanCoAcqId; 
	/**
	 * 外部放款是否强制不对账
	 */
	private Indicator externalLoanNoCheckInd;
	/**
	 * 直接放款对账方
	 */
	private String directLoanCoAcqId;
	/**
	 * 直接对账是否强制不对账
	 */
	private Indicator directLoanNoCheckInd;
	/**
	 * 外部扣款对账方
	 */
	private String externalCutCoAcqId;
	/**
	 * 外部扣款是否强制不对账
	 */
	private Indicator externalCutNoCheckInd;
	/**
	 * 直接扣款对账方
	 */
	private String directCutCoAcqId;
	/**
	 * 直接扣款是否强制不对账
	 */
	private Indicator directCutNoCheckInd;
	
	private AcqIdContactChnl(String acqId, String externalLoanCoAcqId,Indicator externalLoanNoCheckInd,
			String directLoanCoAcqId,Indicator directLoanNoCheckInd,
			String externalCutCoAcqId,Indicator externalCutNoCheckInd,
			String directCutCoAcqId,Indicator directCutNoCheckInd){
		
		this.acqId = acqId;
		this.externalLoanCoAcqId = externalLoanCoAcqId;
		this.externalLoanNoCheckInd = externalLoanNoCheckInd;
		this.directLoanCoAcqId = directLoanCoAcqId;
		this.directLoanNoCheckInd = directLoanNoCheckInd;
		this.externalCutCoAcqId = externalCutCoAcqId;
		this.externalCutNoCheckInd = externalCutNoCheckInd;
		this.directCutCoAcqId = directCutCoAcqId;
		this.directCutNoCheckInd = directCutNoCheckInd;
	}

	/**
	 * @return the acqId
	 */
	public String getAcqId() {
		return acqId;
	}
	/**
	 * @return 外部放款对账方
	 */
	public String getExternalLoanCoAcqId() {
		return externalLoanCoAcqId;
	}

	/**
	 * @return 外部放款是否强制不对账
	 */
	public Indicator getExternalLoanNoCheckInd() {
		return externalLoanNoCheckInd;
	}

	/**
	 * @return 直接放款对账方
	 */
	public String getDirectLoanCoAcqId() {
		return directLoanCoAcqId;
	}

	/**
	 * @return 直接对账是否强制不对账
	 */
	public Indicator getDirectLoanNoCheckInd() {
		return directLoanNoCheckInd;
	}

	/**
	 * @return 外部扣款对账方
	 */
	public String getExternalCutCoAcqId() {
		return externalCutCoAcqId;
	}

	/**
	 * @return 外部扣款是否强制不对账
	 */
	public Indicator getExternalCutNoCheckInd() {
		return externalCutNoCheckInd;
	}

	/**
	 * @return 直接扣款对账方
	 */
	public String getDirectCutCoAcqId() {
		return directCutCoAcqId;
	}

	/**
	 * @return 直接扣款对账方
	 */
	public Indicator getDirectCutNoCheckInd() {
		return directCutNoCheckInd;
	}
}
