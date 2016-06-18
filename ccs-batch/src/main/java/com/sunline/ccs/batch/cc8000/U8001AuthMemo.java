package com.sunline.ccs.batch.cc8000;

import java.math.BigDecimal;
import java.util.Date;

import com.sunline.ppy.dictionary.enums.AccountType;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.BlackWhiteCode;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.InputSource;
import com.sunline.ppy.dictionary.enums.ManualAuthFlag;
import com.sunline.ppy.dictionary.enums.PasswordType;
import com.sunline.ppy.dictionary.enums.PasswordVerifyResult;
import com.sunline.ppy.dictionary.enums.TrackVerifyResult;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.AuthReason;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ark.support.cstruct.CChar;

/**
 * @see 类名：U8001AuthMemo
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期：   2015-6-24下午2:25:47
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class U8001AuthMemo {
	@CChar( value = 12, order = 100 )
	public String org;

	@CChar( value = 9, order = 200 )
	public Integer logKv;

	@CChar( value = 20, zeroPadding = true, order = 300 )
	public Long acctNo;

	@CChar( value = 1, order = 400 )
	public AccountType acctType;

	@CChar( value = 19, zeroPadding = true, order = 500 )
	public String logicCardNbr;

	@CChar( value = 9, order = 600 )
	public Integer acqRefNo;

	@CChar( value = 15, order = 700 )
	public BigDecimal txnAmt;

	@CChar( value = 3, order = 800 )
	public String txnCurrCd;

	@CChar( value = 6, order = 900 )
	public String authCode;

	@CChar( value = 40, order = 1000 )
	public String acqNameAddr;

	@CChar( value = 15, precision = 2, order = 1100 )
	public BigDecimal chbTxnAmt;

	@CChar( value = 3, order = 1200 )
	public String chbCurrCd;

	@CChar( value = 15, order = 1300 )
	public InputSource channel;

	@CChar( value = 4, order = 1400 )
	public String mcc;

	@CChar( value = 9, order = 1500 )
	public String acqBranchId;

	@CChar( value = 11, order = 1600 )
	public String fwdInstId;

	@CChar( value = 10, order = 1700 )
	public String transmissionTimestamp;

	@CChar( value = 4, order = 1800 )
	public String settleDate;

	@CChar( value = 20, order = 1900 )
	public AuthTransDirection txnDirection;

	@CChar( value = 1, order = 2000 )
	public AuthTransStatus txnStatus;

	@CChar( value = 20, order = 2100 )
	public AuthTransType txnType;

	@CChar( value = 14, datePattern = "yyyyMMddHHmmss", order = 2200 )
	public Date logOlTime;

	@CChar( value = 8, datePattern = "yyyyMMdd", order = 2300 )
	public Date logBizDate;

	@CChar( value = 4, order = 2400 )
	public String mti;

	@CChar( value = 20, order = 2500 )
	public AuthTransType origTxnType;

	@CChar( value = 11, order = 2600 )
	public String origFwdInstId;

	@CChar( value = 11, order = 2700 )
	public String origAcqInstId;

	@CChar( value = 4, order = 2800 )
	public String origTxnMti;

	@CChar( value = 8, datePattern = "yyyyMMdd", order = 2900 )
	public Date origTransDate;

	@CChar( value = 9, order = 3000 )
	public Integer origTraceNo;

	@CChar( value = 6, order = 3100 )
	public String origTxnProc;

	@CChar( value = 15, precision = 2, order = 3200 )
	public BigDecimal origTxnAmt;

	@CChar( value = 9, order = 3300 )
	public Integer origLogKv;

	@CChar( value = 38, order = 3400 )
	public String origTxnVal1;

	@CChar( value = 40, order = 3500 )
	public String origTxnVal2;

	@CChar( value = 15, precision = 2, order = 3600 )
	public BigDecimal origChbTxnAmt;

	@CChar( value = 8, datePattern = "yyyyMMdd", order = 3700 )
	public Date origBizDate;

	@CChar( value = 14, datePattern = "yyyyMMddHHmmss", order = 3800 )
	public Date lastReversalDate;

	@CChar( value = 9, order = 3900 )
	public Integer voidCount;

	@CChar( value = 1, order = 4000 )
	public ManualAuthFlag manualAuthFlag;

	@CChar( value = 40, order = 4100 )
	public String operaId;

	@CChar( value = 2, order = 4200 )
	public String brand;

	@CChar( value = 6, order = 4300 )
	public String productCd;

	@CChar( value = 2, order = 4400 )
	public String mccType;

	@CChar( value = 4, order = 4500 )
	public AuthReason finalReason;

	@CChar( value = 1, order = 4600 )
	public AuthAction finalAction;

	@CChar( value = 15, precision = 2, order = 4700 )
	public BigDecimal compAmt;

	@CChar( value = 1, order = 4800 )
	public String finalUpdDirection;

	@CChar( value = 15, precision = 2, order = 4900 )
	public BigDecimal finalUpdAmt;

	@CChar( value = 1, order = 5000 )
	public Indicator icInd;

	@CChar( value = 1, order = 5100 )
	public Indicator the3dsecureType;

	@CChar( value = 1, order = 5200 )
	public Indicator vipStatus;

	@CChar( value = 15, precision = 2, order = 5300 )
	public BigDecimal currBal;

	@CChar( value = 15, precision = 2, order = 5400 )
	public BigDecimal cashAmt;

	@CChar( value = 15, precision = 2, order = 5500 )
	public BigDecimal otb;

	@CChar( value = 15, precision = 2, order = 5600 )
	public BigDecimal cashOtb;

	@CChar( value = 15, precision = 2, order = 5700 )
	public BigDecimal custOtb;

	@CChar( value = 20, order = 5800 )
	public BlackWhiteCode cardBlackFlag;

	@CChar( value = 20, order = 5900 )
	public BlackWhiteCode merchantBlackFlag;

	@CChar( value = 4, order = 6000 )
	public String expireDate;

	@CChar( value = 20, order = 6100 )
	public TrackVerifyResult trackOneResult;

	@CChar( value = 20, order = 6200 )
	public TrackVerifyResult trackTwoResult;

	@CChar( value = 20, order = 6300 )
	public TrackVerifyResult trackThreeResult;

	@CChar( value = 1, order = 6400 )
	public PasswordType pwdType;

	@CChar( value = 20, order = 6500 )
	public PasswordVerifyResult checkPwdResult;

	@CChar( value = 9, order = 6600 )
	public Integer payPwdErrNum;

	@CChar( value = 20, order = 6700 )
	public PasswordVerifyResult checkCvvResult;

	@CChar( value = 20, order = 6800 )
	public PasswordVerifyResult checkCvv2Result;

	@CChar( value = 20, order = 6900 )
	public PasswordVerifyResult checkIcvnResult;

	@CChar( value = 20, order = 7000 )
	public PasswordVerifyResult checkArqcResult;

	@CChar( value = 20, order = 7100 )
	public PasswordVerifyResult checkAtcResult;

	@CChar( value = 20, order = 7200 )
	public PasswordVerifyResult checkCvrResult;

	@CChar( value = 20, order = 7300 )
	public PasswordVerifyResult checkTvrResult;

	@CChar( value = 40, order = 7400 )
	public String rejReason;

	@CChar( value = 15, precision = 2, order = 7500 )
	public BigDecimal unmatchCr;

	@CChar( value = 15, precision = 2, order = 7600 )
	public BigDecimal unmatchDb;

	@CChar( value = 20, order = 7700 )
	public String b002;

	@CChar( value = 6, order = 7800 )
	public String b003;

	@CChar( value = 10, order = 7900 )
	public String b007;

	@CChar( value = 6, order = 8000 )
	public String b011;

	@CChar( value = 3, order = 8100 )
	public String b022;

	@CChar( value = 4, order = 8200 )
	public String b025;

	@CChar( value = 15, order = 8300 )
	public String b032;

	@CChar( value = 40, order = 8400 )
	public String b033;

	@CChar( value = 2, order = 8500 )
	public String b039;

	@CChar( value = 15, order = 8600 )
	public String b042;

	@CChar( value = 103, order = 8700 )
	public String b060;

	@CChar( value = 203, order = 8800 )
	public String b061;

	@CChar( value = 42, order = 8900 )
	public String b090;

	@CChar( value = 20, order = 9000 )
	public String operaTermId;

	@CChar( value = 9, order = 9100 )
	public Integer jpaVersion;

	@CChar( value = 15, precision = 2, order = 9200 )
	public BigDecimal b004;

	@CChar( value = 15, precision = 2, order = 9300 )
	public BigDecimal b006;

	@CChar( value = 3, order = 9400 )
	public String b049;

	@CChar( value = 3, order = 9500 )
	public String b051;

	@CChar( value = 12, order = 9600 )
	public String b037;

	@CChar( value = 15, precision = 2, order = 9700 )
	public BigDecimal b028;

	@CChar( value = 999, order = 9800 )
	public String b048;

	@CChar( value = 40, order = 9900 )
	public String b054;

	@CChar( value = 27, order = 10000 )
	public String accountBlockCode;

	@CChar( value = 27, order = 10100 )
	public String cardBlockCode;

	@CChar( value = 27, order = 10200 )
	public String mediumBlockCode;
}
