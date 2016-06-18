package com.sunline.ccs.service.auth.test.verifier;
//package com.sunline.ccs.service.auth.test.verifier;
//
//import static org.hamcrest.Matchers.equalTo;
//import static org.junit.Assert.assertThat;
//
//import java.math.BigDecimal;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import org.apache.commons.lang.time.DateUtils;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.annotation.DirtiesContext.ClassMode;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.sunline.ppy.api.AuthorizationService;
//import com.sunline.ppy.api.CustomAttributesKey;
//import com.sunline.ppy.api.MediumInfo;
//import com.sunline.ppy.dictionary.enums.ExpiryDateFlag;
//import com.sunline.ppy.dictionary.enums.Indicator;
//import com.sunline.ppy.dictionary.enums.InputSource;
//import com.sunline.ppy.dictionary.enums.PasswordVerifyResult;
//import com.sunline.pcm.param.def.Product;
//import com.sunline.pcm.param.test.BmpParamGenerator;
//import com.sunline.pcm.service.sdk.ParameterServiceMock;
//import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
//import com.sunline.ccs.param.def.AuthMccStateCurrXVerify;
//import com.sunline.ccs.param.def.AuthProduct;
//import com.sunline.ccs.param.def.CountryCtrl;
//import com.sunline.ccs.param.def.CurrencyCtrl;
//import com.sunline.ccs.param.def.MccCtrl;
//import com.sunline.ccs.param.def.enums.CheckType;
//import com.sunline.ccs.service.auth.test.mock.MediumServiceMock;
//import com.sunline.ccs.test.CPSDataSet;
//import com.sunline.ccs.test.DefaultParamGenerator;
//import com.sunline.ccs.test.TestDataGenerator;
//import com.sunline.acm.service.sdk.GlobalManagementServiceMock;
//import com.sunline.ark.support.OrganizationContextHolder;
//import com.sunline.ark.support.service.YakMessage;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration("/test-service.xml")
//@Transactional
//@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
//public class AuthCountryCurrencyMccCheck001Test {
//	
//	@Autowired
//	private AuthorizationService authService;
//	
//	@Autowired
//	private MediumServiceMock mediumMock;
//	
//	@Autowired
//	private GlobalManagementServiceMock globalMock;
//	
//	@Autowired
//	private TestDataGenerator dataGenerator;
//	
//	@Autowired
//	private ParameterServiceMock parameterMock;
//
//	@Autowired
//	private UnifiedParameterFacility parameterFacility;
//	/**
//	 * 默认参数生成工具
//	 */
//	@Autowired
//	private DefaultParamGenerator defaultParamGenerator;
//
//	/**
//	 * bmp参数生成工具
//	 */
//	@Autowired
//	private BmpParamGenerator bmpParamGenerator;
//
//	private CPSDataSet card;
//	
//	private Product product;
//	
//	@Before
//	public void setup() throws ParseException
//	{
//		OrganizationContextHolder.setCurrentOrg("123456789012");
//		Date today = new SimpleDateFormat("yyyyMMdd").parse("20120919");
//		globalMock.setupBatchDate(today, DateUtils.addDays(today, -1));
//		
//		product = bmpParamGenerator.loadParameters(parameterMock, null);
//		defaultParamGenerator.loadParameters(parameterMock, product.productCode);
//		
//		card = dataGenerator.createActMainCard(product.productCode, today);
//		
//		MediumInfo info = new MediumInfo();
//		info.setArqcVerifyResult(PasswordVerifyResult.Approve);
//		info.setLogicCardNbr(card.getCcsCardO().get(0).getLogicCardNbr());
//		mediumMock.setMediumInfo(info);
//	}
//	
//	private YakMessage initYakMessage() {
//		
//		YakMessage msg = new YakMessage();
//		msg.getHeadAttributes().put(11, "0100");
//		
//		msg.getBodyAttributes().put(2, "6200480204984198");
//		msg.getBodyAttributes().put(3, "000000");
//		msg.getBodyAttributes().put(4, "1");
//		msg.getBodyAttributes().put(14, "1234");
//		msg.getBodyAttributes().put(18, "8911");
//		msg.getBodyAttributes().put(19, "156");
//		msg.getBodyAttributes().put(22, "661");
//		msg.getBodyAttributes().put(23, "");
//		msg.getBodyAttributes().put(35,"6200480204984198=12345678901234567890");
//		msg.getBodyAttributes().put(38, "600001");
//		
//		// 特殊交易商户控制表(Key)
//		msg.getBodyAttributes().put(42, "10001");
//		msg.getBodyAttributes().put(45, "6012323232323232");
//		msg.getBodyAttributes().put(48, "ASIN0101234567890");
//		msg.getBodyAttributes().put(49, "156");
//		msg.getBodyAttributes().put(52, "10");
//		msg.getBodyAttributes().put(61, "1168461641684964494984949849489497479468497940");
//
//		msg.getCustomAttributes().put("cardActiveFlag", Indicator.Y);
//		msg.getCustomAttributes().put("blockCodes", "1234ABCD");
//		msg.getCustomAttributes().put("expiryDateFlag", ExpiryDateFlag.Space);
//		msg.getCustomAttributes().put("passwordVerifyResult",	PasswordVerifyResult.Approve);
//		msg.getCustomAttributes().put(CustomAttributesKey.BUSINESS_DATE_KEY_NAME,new Date());
//		msg.getCustomAttributes().put(CustomAttributesKey.INPUT_SOURCE,InputSource.CUP);
//		msg.getCustomAttributes().put(CustomAttributesKey.MTI, "0100");
//		
//		/** 针对单个规则,设置相关限制标志 **/
//		AuthProduct authProduct = parameterFacility.loadParameter(product.productCode, AuthProduct.class);
//		// 避免命中[Ccm_005] 检查针对MCC的交易限制
//		authProduct.checkEnabled.put(CheckType.MccCodeCheckFlag, false);
//		// 避免命中[Ccm_008]、[Ccm_009]、[Ccm_010]
//		authProduct.checkEnabled.put(CheckType.CountryCurrencyMccCrossCheckFlag, false);
//		
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter(product.productCode, authProduct);
//		
//		return msg;
//	}
//	
//	private String doCountryLimitTest( Boolean countryCodeCheckFlag, String f19, Boolean validInd ) {
//		 
//		YakMessage msg = initYakMessage();
//
//		msg.getBodyAttributes().put( 19, f19 );
//
//		/** 针对单个规则,设置相关限制标志 **/
//		AuthProduct authProduct = parameterFacility.loadParameter( product.productCode, AuthProduct.class );
//		// 检查针对国家的交易限制
//		authProduct.checkEnabled.put( CheckType.CountryCodeCheckFlag, countryCodeCheckFlag );
//		
//		CountryCtrl countryCtrl = parameterFacility.loadParameter( "156", CountryCtrl.class );
//		countryCtrl.validInd = validInd;
//		
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter( product.productCode, authProduct );
//		parameterMock.putParameter( "156", countryCtrl );
//
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//	}
//	
//	/**
//	 * 测试目的 : [Ccm_001] 检查针对国家的交易限制
//	 * 测试组装的基本要素 :  
//	 * 		国家码检查启用标志 = Y
//	 *		第19域 国家码 ！= 空
//	 *		是否禁止此国家标志 = true
//	 * 测试判断标准:  返回Reanso: TC01
//	 * 测试控制台输出：命中原因[TC01] 
//	 *  
//	 */
//	@Test
//	public void countryLimitTest() {
//		/** 
//		 * param1:国家码检查启用标志 = TRUE
//		 * param2:第19域 国家码存在
//		 * param3:是否禁止此国家标志 = FALSE
//		 */
//		assertThat(doCountryLimitTest( Boolean.TRUE, "156", Boolean.FALSE ), equalTo( "05" ) ); 
//		/** 
//		 * param1:国家码检查启用标志 = FALSE
//		 * param2:第19域 国家码存在
//		 * param3:是否禁止此国家标志 = TRUE
//		 */
//		assertThat(doCountryLimitTest( Boolean.FALSE, "156", Boolean.TRUE ), equalTo( "00" ) ); 
//		/** 
//		 * param1:国家码检查启用标志 = TRUE
//		 * param2:第19域 国家码不存在
//		 * param3:是否禁止此国家标志 = TRUE
//		 */
//		assertThat(doCountryLimitTest( Boolean.TRUE, "", Boolean.TRUE ), equalTo( "00" ) );
//		/** 
//		 * param1:国家码检查启用标志 = TRUE
//		 * param2:第19域 国家码存在
//		 * param3:是否禁止此国家标志 = TRUE
//		 */
//		assertThat(doCountryLimitTest( Boolean.TRUE, "156", Boolean.TRUE ), equalTo( "00" ) );
//		/** 
//		 * param1:国家码检查启用标志 = FALSE
//		 * param2:第19域 国家码存在
//		 * param3:是否禁止此国家标志 = FALSE
//		 */
//		assertThat(doCountryLimitTest( Boolean.FALSE, "156", Boolean.FALSE ), equalTo( "00" ) );
//	}
//	
//	private String doCountryBaseCurrLimitTest( Boolean countryCodeCheckFlag, String f19, String f51, BigDecimal maxTxnAmtLcl, String f04 ) {
//		 
//		YakMessage msg = initYakMessage();
//		
//		msg.getBodyAttributes().put(  4,  f04 );
//		msg.getBodyAttributes().put( 19,  f19 );		
//		msg.getBodyAttributes().put( 51,  f51 );
//
//		/** 针对单个规则,设置相关限制标志 **/
//		AuthProduct authProduct = parameterFacility.loadParameter( product.productCode, AuthProduct.class );
//		// [Ccm_001] 检查针对国家的交易限制
//		authProduct.checkEnabled.put( CheckType.CountryCodeCheckFlag, countryCodeCheckFlag );	
//
//		CountryCtrl countryCtrl = parameterFacility.loadParameter( "156", CountryCtrl.class );
//		countryCtrl.validInd = false;
//		countryCtrl.maxTxnAmtLcl = maxTxnAmtLcl;
//		
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter( product.productCode, authProduct );
//		parameterMock.putParameter( "156", countryCtrl );
//		
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//		 
//	}
//
//	/**
//	 * 测试目的 : [Ccm_002] 检查针对国家的本币单笔交易限额
//	 * 测试组装的基本要素 :  
//	 *		国家码检查启用标志 = Y
//	 *		第19域 国家码 ！= 空
//	 *		币种标识 = 本币币种
//	 *		本币单笔交易限额 <入账币种金额
//	 * 测试判断标准:  返回Reanso: TC02
//	 * 测试控制台输出：命中原因[TC02] 
//	 *  
//	 */
//	@Test
//	public void countryBaseCurrLimitTest() {
//		/**
//		 *	param1: 国家码检查启用标志 = TRUE
//		 *	param2: 第19域 国家码 存在
//		 *	param3: 第51域 持卡人账户货币代码 不存在
//		 *	param4: 本币单笔交易限额 = 0
//		 *	param5: 入账币种金额 = 1
//		 */
//		assertThat(doCountryBaseCurrLimitTest( Boolean.TRUE, "156", "", BigDecimal.valueOf( 0 ), "1" ), equalTo( "05" ) ); 
//		/**
//		 *	param1: 国家码检查启用标志 = TRUE
//		 *	param2: 第19域 国家码 存在
//		 *	param3: 第51域 持卡人账户货币代码 存在
//		 *	param4: 本币单笔交易限额 = 0
//		 *	param5: 入账币种金额 = 1
//		 */
//		assertThat(doCountryBaseCurrLimitTest( Boolean.TRUE, "156", "840", BigDecimal.valueOf( 0 ), "1" ), equalTo( "05" ) ); 
//		/**
//		 *	param1: 国家码检查启用标志 = FALSE
//		 *	param2: 第19域 国家码 存在
//		 *	param3: 第51域 持卡人账户货币代码 不存在
//		 *	param4: 本币单笔交易限额 = 0
//		 *	param5: 入账币种金额 = 1
//		 */
//		assertThat(doCountryBaseCurrLimitTest( Boolean.FALSE, "156", "", BigDecimal.valueOf( 0 ), "1" ), equalTo( "00" ) ); 
//		/**
//		 *	param1: 国家码检查启用标志 = TRUE
//		 *	param2: 第19域 国家码 不存在
//		 *	param3: 第51域 持卡人账户货币代码 不存在
//		 *	param4: 本币单笔交易限额 = 0
//		 *	param5: 入账币种金额 = 1
//		 */
//		assertThat(doCountryBaseCurrLimitTest( Boolean.TRUE, "", "", BigDecimal.valueOf( 0 ), "1" ), equalTo( "00" ) ); 
//		/**
//		 *	param1: 国家码检查启用标志 = TRUE
//		 *	param2: 第19域 国家码 不存在
//		 *	param3: 第51域 持卡人账户货币代码 存在
//		 *	param4: 本币单笔交易限额 = 0
//		 *	param5: 入账币种金额 = 1
//		 */
//		assertThat(doCountryBaseCurrLimitTest( Boolean.TRUE, "", "840", BigDecimal.valueOf( 0 ), "1" ), equalTo( "00" ) ); 
//		/**
//		 *	param1: 国家码检查启用标志 = TRUE
//		 *	param2: 第19域 国家码 不存在(不设置,默认取156人民币)
//		 *	param3: 第51域 持卡人账户货币代码 不存在
//		 *	param4: 本币单笔交易限额 = 10
//		 *	param5: 第4域入账币种金额 = 1
//		 */
//		assertThat(doCountryBaseCurrLimitTest( Boolean.TRUE, "", "", BigDecimal.valueOf( 10 ), "1" ), equalTo( "00" ) ); 
//	}
//	
//	public String doCountryForeignCurrencyLimitTest( Boolean countryCodeCheckFlag, String f19, String f51, BigDecimal maxTxnAmtFrg, String f06 ) {
//		 
//		YakMessage msg = initYakMessage();
//		
//		msg.getBodyAttributes().put(  6, f06 );
//		msg.getBodyAttributes().put( 19, f19 );
//		msg.getBodyAttributes().put( 51, f51 );
//
//		/** 针对单个规则,设置相关限制标志 **/
//		AuthProduct authProduct = parameterFacility.loadParameter( product.productCode, AuthProduct.class );
//		// [Ccm_001] 检查针对国家的交易限制
//		authProduct.checkEnabled.put( CheckType.CountryCodeCheckFlag, countryCodeCheckFlag );	
//	
//		CountryCtrl countryCtrl = parameterFacility.loadParameter( "156", CountryCtrl.class );
//		countryCtrl.maxTxnAmtFrg = maxTxnAmtFrg;
//		// 避免命中[Ccm_001] 检查针对国家的交易限制
//		countryCtrl.validInd = Boolean.TRUE;
//		
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter( product.productCode, authProduct );
//		parameterMock.putParameter( "156", countryCtrl );
//		
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//		 
//	}
//	
//	/**
//	 * 测试目的 : [Ccm_003] 检查针对国家的外币单笔交易限额
//	 * 测试组装的基本要素 :  
//	 *		国家码检查启用标志 = Y
//	 *		第19域 国家码 ！= 空
//	 *		币种标识 = 外币币种
//	 *		外币单笔交易限额 < 入账币种金额 
//	 * 测试判断标准:  返回Reanso: TC03
//	 * 测试控制台输出：命中原因[TC03] 
//	 *  
//	 */
//	@Test
//	public void countryForeignCurrencyLimitTest() {
//		/**
//		 *	param1: 国家码检查启用标志 = TRUE
//		 *	param2: 第19域 国家码 存在(不设置,默认取156人民币)
//		 *	param3: 第51域 持卡人账户货币代码 存在
//		 *	param4: 外币单笔交易限额 = 0
//		 *	param5: 第6域入账币种金额 = 1
//		 */
//		assertThat(doCountryForeignCurrencyLimitTest( Boolean.TRUE, "156", "840", BigDecimal.valueOf( 0 ), "1" ), equalTo( "05" ) ); 
//		/**
//		 *	param1: 国家码检查启用标志 = FALSE
//		 *	param2: 第19域 国家码 存在(不设置,默认取156人民币)
//		 *	param3: 第51域 持卡人账户货币代码 存在
//		 *	param4: 外币单笔交易限额 = 0
//		 *	param5: 第6域入账币种金额 = 1
//		 */
//		assertThat(doCountryForeignCurrencyLimitTest( Boolean.FALSE, "156", "840", BigDecimal.valueOf( 0 ), "1" ), equalTo( "00" ) ); 
//		/**
//		 *	param1: 国家码检查启用标志 = TRUE
//		 *	param2: 第19域 国家码 不存在(不设置,默认取156人民币)
//		 *	param3: 第51域 持卡人账户货币代码 存在
//		 *	param4: 外币单笔交易限额 = 0
//		 *	param5: 第6域入账币种金额 = 1
//		 */
//		assertThat(doCountryForeignCurrencyLimitTest( Boolean.TRUE, "", "840", BigDecimal.valueOf( 0 ), "1" ), equalTo( "00" ) ); 
//		/**
//		 *	param1: 国家码检查启用标志 = TRUE
//		 *	param2: 第19域 国家码 存在(不设置,默认取156人民币)
//		 *	param3: 第51域 持卡人账户货币代码 存在
//		 *	param4: 外币单笔交易限额 = 10
//		 *	param5: 第6域入账币种金额 = 1
//		 */
//		assertThat(doCountryForeignCurrencyLimitTest( Boolean.TRUE, "156", "840", BigDecimal.valueOf( 10 ), "1" ), equalTo( "00" ) ); 
//	}
//	
//	private String doCurrencyLimitTest( Boolean currencyCodeCheckFlag, Boolean validInd ) {
//		 
//		YakMessage msg = initYakMessage();
//
//		/** 针对单个规则,设置相关限制标志 **/
//		AuthProduct authProduct = parameterFacility.loadParameter( product.productCode, AuthProduct.class );
//		// [Ccm_004] 检查针对币种的交易限制
//		authProduct.checkEnabled.put( CheckType.CurrencyCodeCheckFlag, currencyCodeCheckFlag );
//		
//		CurrencyCtrl currencyCtrl = parameterFacility.loadParameter( "156", CurrencyCtrl.class );
//		currencyCtrl.validInd = validInd;
//		
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter( product.productCode, authProduct );
//		parameterMock.putParameter( "156", currencyCtrl );
//		
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//		 
//	}
//	
//	/**
//	 * 测试目的 : [Ccm_004] 检查针对币种的交易限制
//	 * 测试组装的基本要素 :  
//	 *		币种码检查启用标志 = Y
//	 *		是否禁止此币种 = true
//	 * 测试判断标准:  返回Reanso: TU01
//	 * 测试控制台输出：命中原因[TU01] 
//	 *  
//	 */
//	@Test
//	public void currencyLimitTest() {
//		/**
//		 *	param1: 币种码检查启用标志 = TRUE
//		 *	param2: 是否禁止此币种 = FALSE
//		 */
//		assertThat(doCurrencyLimitTest( Boolean.TRUE, Boolean.FALSE ), equalTo("13")); 
//		/**
//		 *	param1: 币种码检查启用标志 = FALSE
//		 *	param2: 是否禁止此币种 = TRUE
//		 */
//		assertThat(doCurrencyLimitTest( Boolean.FALSE, Boolean.TRUE ), equalTo("00"));  
//		/**
//		 *	param1: 币种码检查启用标志 = FALSE
//		 *	param2: 是否禁止此币种 = FALSE
//		 */
//		assertThat(doCurrencyLimitTest( Boolean.FALSE, Boolean.FALSE ), equalTo("00")); 
//		/**
//		 *	param1: 币种码检查启用标志 = TRUE
//		 *	param2: 是否禁止此币种 = TRUE
//		 */
//		assertThat(doCurrencyLimitTest( Boolean.TRUE, Boolean.TRUE ), equalTo("00")); 
//	}
//	
//	private String doMccTxnTest( Boolean mccCodeCheckFlag, String f18, Boolean validInd ) {
//		 
//		YakMessage msg = initYakMessage();
//		msg.getBodyAttributes().put( 18, f18 );
//	
//		AuthProduct authProduct = parameterFacility.loadParameter( product.productCode, AuthProduct.class );
//		// [Ccm_005] 检查针对MCC的交易限制
//		authProduct.checkEnabled.put( CheckType.MccCodeCheckFlag, mccCodeCheckFlag );
//
//		MccCtrl mccCtrl = parameterFacility.loadParameter( "8911|CUP", MccCtrl.class );
//		mccCtrl.validInd = validInd;
//		
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter( product.productCode, authProduct );
//		parameterMock.putParameter( "8911|CUP", mccCtrl );
//		
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//		 
//	}
//	
//	/**
//	 * 	[Ccm_005] 检查针对MCC的交易限制
//	 *	MCC检查启用标志 = Y
//	 * 	第18域 mcc ！= 空
//	 * 	是否禁止此币种 = false
//	 */
//	@Test
//	public void mccTxnTest() {
//		/**
//		 *	param1: MCC检查启用标志 = TRUE
//		 *	param2: 第18域 mcc存在
//		 *	param3: 是否禁止此币种 = FALSE
//		 */
//		assertThat(doMccTxnTest( Boolean.TRUE, "8911", Boolean.FALSE ), equalTo( "03" ) ); 
//		/**
//		 *	param1: MCC检查启用标志 = FALSE
//		 *	param2: 第18域 mcc存在
//		 *	param3: 是否禁止此币种 = TRUE
//		 */
//		assertThat(doMccTxnTest( Boolean.FALSE, "8911", Boolean.TRUE ), equalTo( "00" ) ); 
//		/**
//		 *	param1: MCC检查启用标志 = TRUE
//		 *	param2: 第18域 mcc存在
//		 *	param3: 是否禁止此币种 = TRUE
//		 */
//		assertThat(doMccTxnTest( Boolean.TRUE, "8911", Boolean.TRUE ), equalTo( "00" ) ); 
//		/**
//		 *	param1: MCC检查启用标志 = FALSE
//		 *	param2: 第18域 mcc存在
//		 *	param3: 是否禁止此币种 = FALSE
//		 */
//		assertThat(doMccTxnTest( Boolean.FALSE, "8911", Boolean.FALSE ), equalTo( "00" ) ); 
//	}
//	
//	private String doMccBaseCurrSingleTxnTest( Boolean mccCodeCheckFlag, String f04, String f51, BigDecimal maxTxnAmtLcl ) {
//		 
//		YakMessage msg = initYakMessage();
//		msg.getBodyAttributes().put(  4, f04 );
//		msg.getBodyAttributes().put( 51, f51 );
//	
//		AuthProduct authProduct = parameterFacility.loadParameter( product.productCode, AuthProduct.class );
//		// [Ccm_005] 检查针对MCC的交易限制
//		authProduct.checkEnabled.put( CheckType.MccCodeCheckFlag, mccCodeCheckFlag );
//		
//		MccCtrl mccCtrl = parameterFacility.loadParameter( "8911|CUP", MccCtrl.class );
//		mccCtrl.maxTxnAmtLcl = maxTxnAmtLcl;
//		// 避开"[Ccm_005] 检查针对MCC的交易限制"规则
//		mccCtrl.validInd = Boolean.TRUE;
//		
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter( product.productCode, authProduct );
//		parameterMock.putParameter( "8911|CUP", mccCtrl );
//		
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//		 
//	}
//	
//	/**
//	 * 	[Ccm_006] 检查针对MCC的本币单笔交易限额
//	 * 	MCC检查启用标志 = Y
//	 *	第18域 MCC ！= 空(无法测试，load不到参数，系统抛出异常)
//	 *	币种标识 = 本币币种
//	 *	本币币单笔交易限额 < 入账币种金额
//	 */
//	@Test
//	public void mccBaseCurrSingleTxnTest() {
//		/**
//		 *	param1: MCC检查启用标志 = TRUE
//		 *	param2: 第4域 = 10
//		 *	param3: 第51域 = ""(不存在)
//		 *	param4: 本币币单笔交易限额 = 0
//		 */
//		assertThat(doMccBaseCurrSingleTxnTest( Boolean.TRUE, "10", "", BigDecimal.valueOf( 0 ) ), equalTo( "61" ) ); 
//		/**
//		 *	param1: MCC检查启用标志 = FALSE
//		 *	param2: 第4域 = 10
//		 *	param3: 第51域 = ""(不存在)
//		 *	param4: 本币币单笔交易限额 = 0
//		 */
//		assertThat(doMccBaseCurrSingleTxnTest( Boolean.FALSE, "10", "", BigDecimal.valueOf( 0 ) ), equalTo( "00" ) ); 
//		/**
//		 *	param1: MCC检查启用标志 = TRUE
//		 *	param2: 第4域 = 10
//		 *	param3: 第51域 = 840美元
//		 *	param4: 本币币单笔交易限额 = 0
//		 */
//		assertThat(doMccBaseCurrSingleTxnTest( Boolean.TRUE, "10", "840", BigDecimal.valueOf( 0 ) ), equalTo( "00" ) ); 
//		/**
//		 *	param1: MCC检查启用标志 = TRUE
//		 *	param2: 第4域 = 1
//		 *	param3: 第51域 = 840美元
//		 *	param4: 本币币单笔交易限额 = 10
//		 */
//		assertThat(doMccBaseCurrSingleTxnTest( Boolean.TRUE, "1", "840", BigDecimal.valueOf( 10 ) ), equalTo( "00" ) ); 
//	}
//	
//	private String doMccForeignCurrSingleTxnTest( Boolean mccCodeCheckFlag, String f06, String f51, BigDecimal maxTxnAmtFrg ) {
//		 
//		YakMessage msg = initYakMessage();
//		msg.getBodyAttributes().put(  6, f06 );
//		msg.getBodyAttributes().put( 51, f51 );
//	
//		AuthProduct authProduct = parameterFacility.loadParameter( product.productCode, AuthProduct.class );
//		// [Ccm_005] 检查针对MCC的交易限制
//		authProduct.checkEnabled.put( CheckType.MccCodeCheckFlag, mccCodeCheckFlag );
//		
//		MccCtrl mccCtrl = parameterFacility.loadParameter( "8911|CUP", MccCtrl.class );
//		mccCtrl.maxTxnAmtFrg = maxTxnAmtFrg;
//		// 避开“[Ccm_005] 检查针对MCC的交易限制”规则
//		mccCtrl.validInd = Boolean.TRUE;
//		
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter( product.productCode, authProduct );
//		parameterMock.putParameter( "8911|CUP", mccCtrl );
//		
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//		 
//	}
//	
//	/**
//	 * 	[Ccm_007] 检查针对MCC的外币单笔交易限额
//	 * 	MCC检查启用标志 = Y
//	 *	第18域 MCC ！= 空(无法测试，load不到参数，系统抛出异常)
//	 *	币种标识 = 外币币种
//	 *	外币币单笔交易限额 <入账币种金额
//	 */
//	@Test
//	public void mccForeignCurrSingleTxnTest() {
//		/**
//		 *	param1: MCC检查启用标志 = TRUE
//		 *	param2: 第6域 = 10
//		 *	param3: 第51域 = "840"美元
//		 *	param4: 外币币单笔交易限额 = 0
//		 */
//		assertThat(doMccForeignCurrSingleTxnTest( Boolean.TRUE, "10", "840", BigDecimal.valueOf( 0 ) ), equalTo( "05" ) );
//		/**
//		 *	param1: MCC检查启用标志 = FALSE
//		 *	param2: 第6域 = 10
//		 *	param3: 第51域 = "840"美元
//		 *	param4: 外币币单笔交易限额 = 0
//		 */
//		assertThat(doMccForeignCurrSingleTxnTest( Boolean.FALSE, "10", "840", BigDecimal.valueOf( 0 ) ), equalTo( "00" ) ); 
//		/**
//		 * 	第6域和第51域必须同时存在,否则返回码=30,6域51域组合校验错误;
//		 *	param1: MCC检查启用标志 = TRUE
//		 *	param2: 第6域 = 10
//		 *	param3: 第51域 = ""美元不存在
//		 *	param4: 外币币单笔交易限额 = 0
//		 */
//		assertThat(doMccForeignCurrSingleTxnTest( Boolean.TRUE, "10", "", BigDecimal.valueOf( 0 ) ), equalTo( "30" ) ); 
//		/**
//		 *	param1: MCC检查启用标志 = TRUE
//		 *	param2: 第6域 = 1
//		 *	param3: 第51域 = "840"美元
//		 *	param4: 外币币单笔交易限额 = 10
//		 */
//		assertThat(doMccForeignCurrSingleTxnTest( Boolean.TRUE, "1", "840", BigDecimal.valueOf( 10 ) ), equalTo( "00" ) ); 
//	}
//	
//	private String doCountryCurrMccGroupTest( Boolean countryCurrencyMccFlag, Boolean forbiddenFlag, Boolean currentActiveFlag) {
//		 
//		YakMessage msg = initYakMessage();
//	
//		AuthProduct authProduct = parameterFacility.loadParameter( product.productCode, AuthProduct.class );
//		// 国家/币种/mcc交叉检查启用标志
//		authProduct.checkEnabled.put( CheckType.CountryCurrencyMccCrossCheckFlag, countryCurrencyMccFlag );
//		
//		AuthMccStateCurrXVerify mscx = parameterFacility.loadParameter( "CUP|8911|156|156", AuthMccStateCurrXVerify.class );
//		mscx.forbiddenFlag = forbiddenFlag;
//		mscx.currentActiveFlag = currentActiveFlag;
//		
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter( product.productCode, authProduct );
//		parameterMock.putParameter( "CUP|8911|156|156", mscx );
//		
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//		 
//	}
//	
//	/**
//	 * 	[Ccm_008] 检查国家/币种/mcc是否禁止此组合交易
//	 * 	国家/币种/mcc交叉检查启用标志 = Y
//	 *	是否禁止此组合交易 == Y
//	 *	当前是否生效标志 = Y
//	 */
//	@Test
//	public void countryCurrMccGroupTest() {
//		/**
//		 *	param1: 国家/币种/mcc交叉检查启用标志 = TRUE
//		 *	param2: 是否禁止此组合交易 = TRUE
//		 *	param3: 当前是否生效标志 = TRUE
//		 */
//		assertThat(doCountryCurrMccGroupTest( Boolean.TRUE, Boolean.TRUE, Boolean.TRUE ), equalTo("03")); 
//		/**
//		 *	param1: 国家/币种/mcc交叉检查启用标志 = FALSE
//		 *	param2: 是否禁止此组合交易 = TRUE
//		 *	param3: 当前是否生效标志 = TRUE
//		 */
//		assertThat(doCountryCurrMccGroupTest( Boolean.FALSE, Boolean.TRUE, Boolean.TRUE ), equalTo("00")); 
//		/**
//		 *	param1: 国家/币种/mcc交叉检查启用标志 = TRUE
//		 *	param2: 是否禁止此组合交易 = FALSE
//		 *	param3: 当前是否生效标志 = TRUE
//		 */
//		assertThat(doCountryCurrMccGroupTest( Boolean.TRUE, Boolean.FALSE, Boolean.TRUE ), equalTo("00")); 
//		/**
//		 *	param1: 国家/币种/mcc交叉检查启用标志 = TRUE
//		 *	param2: 是否禁止此组合交易 = TRUE
//		 *	param3: 当前是否生效标志 = FALSE
//		 */
//		assertThat(doCountryCurrMccGroupTest( Boolean.TRUE, Boolean.TRUE, Boolean.FALSE ), equalTo("00")); 
//	}
//	
//	private String doCountryCurrencyMccBaseCurrSingleTxnTest( Boolean countryCurrencyMccFlag, 
//																Boolean forbiddenFlag, 
//																  Boolean currentActiveFlag, 
//																    String f04, 
//																      String f51, 
//																        BigDecimal maxAmtLcl ) {
//		 
//		YakMessage msg = initYakMessage();
//		msg.getBodyAttributes().put(  4, f04 );
//		msg.getBodyAttributes().put( 51, f51 );
//	
//		AuthProduct authProduct = parameterFacility.loadParameter( product.productCode, AuthProduct.class );
//		// 国家/币种/mcc交叉检查启用标志
//		authProduct.checkEnabled.put( CheckType.CountryCurrencyMccCrossCheckFlag, countryCurrencyMccFlag );
//		
//		AuthMccStateCurrXVerify mscx = parameterFacility.loadParameter( "CUP|8911|156|156", AuthMccStateCurrXVerify.class );
//		mscx.forbiddenFlag = forbiddenFlag;
//		mscx.currentActiveFlag = currentActiveFlag;
//		mscx.maxAmtLcl = maxAmtLcl;
//		
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter( product.productCode, authProduct );
//		parameterMock.putParameter( "CUP|8911|156|156", mscx );
//		
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//		 
//	}
//	
//	/**
//	 * 	[Ccm_009] 检查国家/币种/mcc的本币单笔交易限额
//	 * 	国家/币种/mcc交叉检查启用标志 = Y
//	 *	是否禁止此组合交易 == Y
//	 *	当前是否生效标志 = Y
//	 *	第51域不存在，代表是本币
//	 *	本币币单笔交易限额 < 国家/币种/mcc交叉的交易金额 
//	 */
//	@Test
//	public void countryCurrencyMccBaseCurrSingleTxnTest() {
//		/**
//		 *	param1: 国家/币种/mcc交叉检查启用标志 = TRUE
//		 *	param2: 是否禁止此组合交易 = TRUE
//		 *	param3: 当前是否生效标志 = TRUE
//		 *	param4: 第4域交易金额 = 10
//		 *	param5：第51域持卡人账户币种 = ""(不存在)
//		 *	param6: 本币币单笔交易限额 = 0
//		 */
//		assertThat(doCountryCurrencyMccBaseCurrSingleTxnTest( Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, "10", "", BigDecimal.valueOf( 0 ) ), equalTo( "03" ) ); 
//		/**
//		 *	param1: 国家/币种/mcc交叉检查启用标志 = FALSE
//		 *	param2: 是否禁止此组合交易 = TRUE
//		 *	param3: 当前是否生效标志 = TRUE
//		 *	param4: 第4域交易金额 = 10
//		 *	param5：第51域持卡人账户币种 = ""(不存在)
//		 *	param6: 本币币单笔交易限额 = 0
//		 */
//		assertThat(doCountryCurrencyMccBaseCurrSingleTxnTest( Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, "10", "", BigDecimal.valueOf( 0 ) ), equalTo( "00" ) ); 
//		/**
//		 *	param1: 国家/币种/mcc交叉检查启用标志 = TRUE
//		 *	param2: 是否禁止此组合交易 = FALSE
//		 *	param3: 当前是否生效标志 = TRUE
//		 *	param4: 第4域交易金额 = 10
//		 *	param5：第51域持卡人账户币种 = ""(不存在)
//		 *	param6: 本币币单笔交易限额 = 0
//		 */
//		assertThat(doCountryCurrencyMccBaseCurrSingleTxnTest( Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, "10", "", BigDecimal.valueOf( 0 ) ), equalTo( "00" ) );
//		/**
//		 *	param1: 国家/币种/mcc交叉检查启用标志 = TRUE
//		 *	param2: 是否禁止此组合交易 = TRUE
//		 *	param3: 当前是否生效标志 = FALSE
//		 *	param4: 第4域交易金额 = 10
//		 *	param5：第51域持卡人账户币种 = ""(不存在)
//		 *	param6: 本币币单笔交易限额 = 0
//		 */
//		assertThat(doCountryCurrencyMccBaseCurrSingleTxnTest( Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, "10", "", BigDecimal.valueOf( 0 ) ), equalTo( "00" ) ); 
//		/**
//		 *	param1: 国家/币种/mcc交叉检查启用标志 = TRUE
//		 *	param2: 是否禁止此组合交易 = TRUE
//		 *	param3: 当前是否生效标志 = FALSE
//		 *	param4: 第4域交易金额 = 1
//		 *	param5：第51域持卡人账户币种 = ""(不存在)
//		 *	param6: 本币币单笔交易限额 = 100
//		 */
//		assertThat(doCountryCurrencyMccBaseCurrSingleTxnTest( Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, "1", "", BigDecimal.valueOf( 100 ) ), equalTo( "00" ) ); 
//	}
//	
//	private String doCountryCurrencyMccForeignCurrencySingleTxnTest( Boolean countryCurrencyMccFlag, 
//																	   Boolean forbiddenFlag, 
//																	   	 Boolean currentActiveFlag, 
//																	   	   String f06, 
//																	   	     String f51, 
//																	   	       BigDecimal maxAmtFrg ) {
//		 
//		YakMessage msg = initYakMessage();
//		msg.getBodyAttributes().put(  6, f06 );
//		msg.getBodyAttributes().put( 51, f51 );
//	
//		AuthProduct authProduct = parameterFacility.loadParameter( product.productCode, AuthProduct.class );
//		// 国家/币种/mcc交叉检查启用标志
//		authProduct.checkEnabled.put( CheckType.CountryCurrencyMccCrossCheckFlag, countryCurrencyMccFlag );
//		
//		AuthMccStateCurrXVerify mscx = parameterFacility.loadParameter( "CUP|8911|156|156", AuthMccStateCurrXVerify.class );
//		mscx.forbiddenFlag = forbiddenFlag;
//		mscx.currentActiveFlag = currentActiveFlag;
//		mscx.maxAmtFrg = maxAmtFrg;
//		
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter( product.productCode, authProduct );
//		parameterMock.putParameter( "CUP|8911|156|156", mscx );
//		
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//		 
//	}
//	
//	/**
//	 * 	[Ccm_010] 检查国家/币种/mcc的外币单笔交易限额
//	 * 	国家/币种/mcc交叉检查启用标志 = Y
//	 *	是否禁止此组合交易 == Y
//	 *	当前是否生效标志 = Y
//	 *	外币币单笔交易限额 < 国家/币种/mcc交叉的交易金额 
//	 */
//	@Test
//	public void countryCurrencyMccForeignCurrencySingleTxnTest() {
//		/**
//		 *	param1: 国家/币种/mcc交叉检查启用标志 = TRUE
//		 *	param2: 是否禁止此组合交易 = TRUE
//		 *	param3: 当前是否生效标志 = TRUE
//		 *	param4: 第6域交易金额 = 10
//		 *	param5：第51域持卡人账户币种 = "840"(美元)
//		 *	param6: 外币币单笔交易限额 = 0
//		 */
//		assertThat(doCountryCurrencyMccForeignCurrencySingleTxnTest( Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, "10", "840", BigDecimal.valueOf( 0 ) ), equalTo( "03" ) ); 
//		/**
//		 *	param1: 国家/币种/mcc交叉检查启用标志 = FALSE
//		 *	param2: 是否禁止此组合交易 = TRUE
//		 *	param3: 当前是否生效标志 = TRUE
//		 *	param4: 第6域交易金额 = 10
//		 *	param5：第51域持卡人账户币种 = "840"(美元)
//		 *	param6: 外币币单笔交易限额 = 0
//		 */
//		assertThat(doCountryCurrencyMccForeignCurrencySingleTxnTest( Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, "10", "840", BigDecimal.valueOf( 0 ) ), equalTo( "00" ) );  
//		/**
//		 *	param1: 国家/币种/mcc交叉检查启用标志 = TRUE
//		 *	param2: 是否禁止此组合交易 = FALSE
//		 *	param3: 当前是否生效标志 = TRUE
//		 *	param4: 第6域交易金额 = 10
//		 *	param5：第51域持卡人账户币种 = "840"(美元)
//		 *	param6: 外币币单笔交易限额 = 0
//		 */
//		assertThat(doCountryCurrencyMccForeignCurrencySingleTxnTest( Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, "10", "840", BigDecimal.valueOf( 0 ) ), equalTo( "00" ) ); 
//		/**
//		 *	param1: 国家/币种/mcc交叉检查启用标志 = TRUE
//		 *	param2: 是否禁止此组合交易 = TRUE
//		 *	param3: 当前是否生效标志 = FALSE
//		 *	param4: 第6域交易金额 = 10
//		 *	param5：第51域持卡人账户币种 = "840"(美元)
//		 *	param6: 外币币单笔交易限额 = 0
//		 */
//		assertThat(doCountryCurrencyMccForeignCurrencySingleTxnTest( Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, "10", "840", BigDecimal.valueOf( 0 ) ), equalTo( "00" ) ); 
//		/**
//		 *	param1: 国家/币种/mcc交叉检查启用标志 = TRUE
//		 *	param2: 是否禁止此组合交易 = TRUE
//		 *	param3: 当前是否生效标志 = FALSE
//		 *	param4: 第6域交易金额 = 10
//		 *	param5：第51域持卡人账户币种 = "840"(美元)
//		 *	param6: 外币币单笔交易限额 = 100
//		 */
//		assertThat(doCountryCurrencyMccForeignCurrencySingleTxnTest( Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, "1", "840", BigDecimal.valueOf( 100 ) ), equalTo( "00" ) ); 
//	}
//
//}
