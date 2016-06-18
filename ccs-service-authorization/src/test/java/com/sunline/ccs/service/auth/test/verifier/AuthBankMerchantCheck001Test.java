package com.sunline.ccs.service.auth.test.verifier;
//package com.sunline.ccs.service.auth.test.verifier;
//
//import static org.hamcrest.Matchers.equalTo;
//import static org.junit.Assert.assertThat;
//
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
//import com.sunline.ccs.param.def.AuthProduct;
//import com.sunline.ccs.param.def.MerchantTxnCrtl;
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
//public class AuthBankMerchantCheck001Test {
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
//	/**
//	 * 初始化YakMsg
//	 * @return
//	 */
//	private YakMessage initYakMessage() {
//		
//		YakMessage msg = new YakMessage();
//		msg.getHeadAttributes().put(11, "0100");
//		
//		msg.getBodyAttributes().put(2, "6200480204984198");
//		msg.getBodyAttributes().put(3, "000000");
//		msg.getBodyAttributes().put(4, "1");
//		msg.getBodyAttributes().put(14, "1234");
//		msg.getBodyAttributes().put(18, "6011");
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
//		// 避免命中[Ccm_001] 检查针对国家的交易限制
//		authProduct.checkEnabled.put(CheckType.CountryCodeCheckFlag, false);
//		
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter(product.productCode, authProduct);
//		
//		return msg;
//	}
//	
//	public String doAuthMotoExpenseLimitCheckTest( String f42, Boolean isForceVerifySupportMerchantMotoExpense, Boolean merchantRestrictFlag ) {
//		// 初始化消息报文
//		YakMessage msg = initYakMessage();
//
//		/** 针对单个规则,覆盖初始报文内容,下列5个域决定是posmoto电话(PHT) **/
//		msg.getBodyAttributes().put( 22, "011"						 );
//		msg.getBodyAttributes().put( 18, "8911"						 );
//		msg.getBodyAttributes().put( 60, "1234577509555901234567890" );
//		// 特殊交易商户控制表(Key);
//		msg.getBodyAttributes().put( 42, f42 );
//		msg.getBodyAttributes().remove( 35 );
//		msg.getBodyAttributes().remove( 52 );
//		
//		/** 针对单个规则,设置相关限制标志 **/
//		AuthProduct authProduct = parameterFacility.loadParameter( product.productCode, AuthProduct.class );
//		// 检查启用商户限制标志 = true(支持)
//		authProduct.checkEnabled.put( CheckType.IsForceVerifySupportMerchantMotoExpense, isForceVerifySupportMerchantMotoExpense );
//		authProduct.checkEnabled.put( CheckType.MerchantRestrictFlag, 					 merchantRestrictFlag 					 );
//		
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter( product.productCode, authProduct );
//		
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//		 
//	}
//	
//	/**
//	 * 测试目的 : [Mer_001] 检查moto消费交易限制
//	 * 测试组装的基本要素 :  
//	 * 		交易终端 = Moto消费; 
//	 * 		商户限制参数不存在;(field(42))
//	 * 		检查启用商户限制标志 = true(支持)
//	 *		是否强制验证支持Moto消费交易 = true(支持)
//	 * 测试判断标准:  返回Reanso: TM03
//	 * 测试控制台输出：命中原因[TM03] 排序后reasoncd =TM03优先级= 6000
//	 *  
//	 */
//	@Test
//	public void authMotoExpenseLimitCheckTest() {
//		/** 
//		 * param1:商户ID = 11111, 不存在
//		 * param2:是否强制验证支持Moto消费交易
//		 * param3:商户交易限制启用标志
//		 */
//		assertThat( doAuthMotoExpenseLimitCheckTest( "11111", Boolean.TRUE, Boolean.TRUE ), equalTo( "05" ) ); 
//		/** 
//		 * param1:商户ID = 10001, 存在
//		 * param2:是否强制验证支持Moto消费交易
//		 * param3:商户交易限制启用标志
//		 */
//		assertThat( doAuthMotoExpenseLimitCheckTest( "10001", Boolean.TRUE, Boolean.TRUE ), equalTo( "00" ) ); 
//		/** 
//		 * param1:商户ID=10001, 存在
//		 * param2:是否强制验证支持Moto消费交易 = false
//		 * param3:商户交易限制启用标志
//		 */
//		assertThat( doAuthMotoExpenseLimitCheckTest( "11111", Boolean.FALSE, Boolean.TRUE ), equalTo( "00" ) ); 
//		/** 
//		 * param1:商户ID=10001, 存在
//		 * param2:是否强制验证支持Moto消费交易
//		 * param3:商户交易限制启用标志 = false
//		 */
//		assertThat( doAuthMotoExpenseLimitCheckTest( "11111", Boolean.TRUE, Boolean.FALSE ), equalTo( "00" ) ); 
//	}
//	
//
//	public String doAuthMotoElectronLimitCheckTest( String f42, Boolean motoElectronFlag, Boolean merchantRestrictFlag ) {
//		
//		YakMessage msg = initYakMessage();
//
//		/** 针对单个规则,覆盖初始报文内容,下列5个域决定是posmoto电子(PHE) **/
//		msg.getBodyAttributes().put( 22, "011" 						 );
//		msg.getBodyAttributes().put( 18, "8911" 					 );
//		msg.getBodyAttributes().put( 60, "1234577508555901234567890" );
//		// 特殊交易商户控制表(Key)
//		msg.getBodyAttributes().put( 42, f42 );
//		msg.getBodyAttributes().remove( 35 );
//		
//		/** 针对单个规则,设置相关限制标志 **/
//		AuthProduct authProduct = parameterFacility.loadParameter( product.productCode, AuthProduct.class );
//		// 检查启用商户限制标志 = true(支持)
//		authProduct.checkEnabled.put( CheckType.IsForceVerifySupportElectronicCategoryExpense, motoElectronFlag 	);
//		authProduct.checkEnabled.put( CheckType.MerchantRestrictFlag, 						   merchantRestrictFlag );
//		
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter( product.productCode, authProduct );
//
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//	}
//	
//	/**
//	 * 测试目的 : [Mer_002] 检查电子类消费交易限制
//	 * 测试组装的基本要素 :  
//	 * 		交易终端 = Moto消费; 
//	 * 		商户限制参数不存在;(field(42))
//	 * 		检查启用商户限制标志 = true(支持)
//	 *		是否强制验证支持电子类消费交易 = true(支持)
//	 * 测试判断标准:  返回Reanso: TM03
//	 * 测试控制台输出：命中原因[TM03] 排序后reasoncd =TM03优先级= 6000
//	 *  
//	 */
//	@Test
//	public void authMotoElectronLimitCheckTest() {
//		/** 
//		 * param1:商户ID = 11111, 不存在
//		 * param2:是否强制验证支持电子类消费交易
//		 * param3:商户交易限制启用标志
//		 */
//		assertThat( doAuthMotoElectronLimitCheckTest( "11111", Boolean.TRUE, Boolean.TRUE ), equalTo( "05" ) ); 
//		/** 
//		 * param1:商户ID = 10001, 存在
//		 * param2:是否强制验证支持电子类消费交易
//		 * param3:商户交易限制启用标志
//		 */
//		assertThat( doAuthMotoElectronLimitCheckTest( "10001", Boolean.TRUE, Boolean.TRUE ), equalTo( "00" ) ); 
//		/** 
//		 * param1:商户ID = 11111, 存在
//		 * param2:是否强制验证支持电子类消费交易 = FALSE
//		 * param3:商户交易限制启用标志
//		 */
//		assertThat( doAuthMotoElectronLimitCheckTest( "11111", Boolean.FALSE, Boolean.TRUE ), equalTo( "00" ) ); 
//		/** 
//		 * param1:商户ID = 11111, 存在
//		 * param2:是否强制验证支持电子类消费交易
//		 * param3:商户交易限制启用标志 = FALSE
//		 */
//		assertThat( doAuthMotoElectronLimitCheckTest( "11111", Boolean.TRUE, Boolean.FALSE ), equalTo( "00" ) ); 
//		 
//	}
//	
//	public String doAuthLoanLimitCheckTest( String f42, Boolean instalmentFlag, Boolean merchantRestrictFlag ) {
//		 
//		YakMessage msg = initYakMessage();
//
//		/** 针对单个规则,覆盖初始报文内容,下列5个域决定是POS分期 **/
//		msg.getBodyAttributes().put( 22, "011"  );
//		msg.getBodyAttributes().put( 25, "64"   );
//		msg.getBodyAttributes().put( 18, "8911" );
//		// 特殊交易商户控制表(Key)
//		msg.getBodyAttributes().put( 42, f42 );
//
//		/** 针对单个规则,设置相关限制标志 **/
//		AuthProduct authProduct = parameterFacility.loadParameter( product.productCode, AuthProduct.class );
//		// 检查启用商户限制标志 = true(支持)
//		authProduct.checkEnabled.put( CheckType.IsForceVerifySupportInstalment, instalmentFlag 		 );
//		authProduct.checkEnabled.put( CheckType.MerchantRestrictFlag, 			merchantRestrictFlag );
//
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter( product.productCode, authProduct );
//
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//		
//	}
//	
//	/**
//	 * 测试目的 : [Mer_003] 检查POS分期消费交易限制
//	 * 测试组装的基本要素 :  
//	 * 		交易类型 = 分期消费 
//	 * 		商户限制参数不存在;(field(42))
//	 *		检查启用商户限制标志 = true
//	 *		是否强制验证支持分期消费交易 = true(支持)
//	 * 测试判断标准:  返回Reanso: TM03
//	 * 测试控制台输出：命中原因[TM03] 排序后reasoncd =TM03优先级= 6000
//	 *  
//	 */
//	@Test
//	public void authLoanLimitCheckTest() {
//		/** 
//		 * param1:商户ID = 11111, 不存在
//		 * param2:是否强制验证支持分期消费交易
//		 * param3:商户交易限制启用标志
//		 */
//		assertThat( doAuthLoanLimitCheckTest( "11111", Boolean.TRUE, Boolean.TRUE ), equalTo( "05" ) ); 
//		/** 
//		 * param1:商户ID = 10001, 存在
//		 * param2:是否强制验证支持分期消费交易
//		 * param3:商户交易限制启用标志
//		 */
//		assertThat( doAuthLoanLimitCheckTest( "10001", Boolean.TRUE, Boolean.TRUE ), equalTo( "00" ) ); 
//		/** 
//		 * param1:商户ID = 11111, 不存在
//		 * param2:是否强制验证支持分期消费交易 = FALSE
//		 * param3:商户交易限制启用标志
//		 */
//		assertThat( doAuthLoanLimitCheckTest( "11111", Boolean.FALSE, Boolean.TRUE ), equalTo( "00" ) ); 
//		/** 
//		 * param1:商户ID = 11111, 不存在
//		 * param2:是否强制验证支持分期消费交易
//		 * param3:商户交易限制启用标志 = FALSE
//		 */
//		assertThat( doAuthLoanLimitCheckTest( "11111", Boolean.TRUE, Boolean.FALSE ), equalTo( "00" ) ); 
//		
//	}
//	
//	/**
//	 * 测试目的 : [Mer_004] 检查大额分期消费交易限制（一期不做, 没有配置大额分期的交易类型）
//	 * 测试组装的基本要素 :  
//	 * 		交易类型 = 大额分期消费
//	 * 		商户限制参数不存在;(field(42))
//	 *		检查启用商户限制标志 = true
//	 *		是否强制验证支持大额分期消费交易 = true(支持)
//	 * 测试判断标准:  返回Reanso: TM03
//	 * 测试控制台输出：命中原因[TM03] 排序后reasoncd =TM03优先级= 6000
//	 *  
//	 */
////	@Test
////	public void AuthBigAmountLoanLimitCheckTest()
////	{
////		 
////		YakMessage msg = initYakMessage();
////
////		AuthProduct authProduct = parameterFacility.loadParameter(product.productCode, AuthProduct.class);
////		// [Ccm_005] 检查针对MCC的交易限制
////		authProduct.checkEnabled.put(CheckType.MccCodeCheckFlag, false);
////		// [Ccm_001] 检查针对国家的交易限制
////		authProduct.checkEnabled.put(CheckType.CountryCodeCheckFlag, false);
//////		authProduct.checkEnabled.put(CheckType.IsForceVerifySupportElectronicCategoryExpense, false);
//////		authProduct.checkEnabled.put(CheckType.IsForceVerifySupportInstalment, false);
////		
////		
////
////		msg.getBodyAttributes().put(22, "011");
////		msg.getBodyAttributes().put(25, "64");
////		msg.getBodyAttributes().put(18, "8911");
////		// 特殊交易商户控制表(Key)
////		msg.getBodyAttributes().put(42, "11111");
////		msg.getBodyAttributes().put(60, "1234577508555901234567890");
////		
////		msg.getBodyAttributes().remove( 35 );
////		msg.getBodyAttributes().remove( 52 );
////		
////		// 检查启用商户限制标志 = true(支持)
////		authProduct.checkEnabled.put(CheckType.IsForceVerifySupportBigAmountInstalment, false);
////		authProduct.checkEnabled.put(CheckType.MerchantRestrictFlag, true);
////
////		YakMessage yak = authService.authorize(msg);
////		Assert.assertEquals("00",yak.getBodyAttributes().get(39));
////		 
////	}
//	
//	public String doAuthIsSupportMotoCheckTest( Indicator motoPosInd, Boolean merchantRestrictFlag ) {
//		 
//		YakMessage msg = initYakMessage();
//
//		/** 针对单个规则,覆盖初始报文内容,下列5个域决定是MOTO电话PHT分期 **/
//		msg.getBodyAttributes().put( 22, "011"	);
//		msg.getBodyAttributes().put( 25, "64"	);
//		msg.getBodyAttributes().put( 18, "8911" );
//		// 特殊交易商户控制表(Key)
//		msg.getBodyAttributes().put( 42, "10001" );
//		msg.getBodyAttributes().put( 60, "1234577509555901234567890" );
//		msg.getBodyAttributes().remove( 35 );
//		msg.getBodyAttributes().remove( 52 );
//
//		/** 针对单个规则,设置相关限制标志 **/
//		AuthProduct authProduct = parameterFacility.loadParameter( product.productCode, AuthProduct.class );
//		// 检查启用商户限制标志 = true(支持)
//		authProduct.checkEnabled.put( CheckType.MerchantRestrictFlag, merchantRestrictFlag );
//		
//		/** 获取默认参数中的商户交易控制参数,对商户限制中是否支持moto消费交易标志进行改写,满足各单元测试条件 **/
//		MerchantTxnCrtl mtc = parameterFacility.loadParameter( "10001", MerchantTxnCrtl.class );
//		mtc.supportMotoPosInd = motoPosInd;
//
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter( product.productCode, authProduct );
//		parameterMock.putParameter( "10001", mtc );
//
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//		
//	}
//	
//	/**
//	 * 测试目的 : [Mer_005] 检查是否支持moto消费交易
//	 * 测试组装的基本要素 :  
//	 * 		交易终端 = moto消费
//	 *		检查启用商户限制标志 = true
//	 *		商户限制中是否支持moto消费交易 = N(不支持)
//	 * 测试判断标准:  返回Reanso: TM01
//	 * 测试控制台输出：命中原因[TM01] 
//	 *  
//	 */
//	@Test
//	public void authIsSupportMotoCheckTest() {
//		/** 
//		 * param1:商户限制中是否支持moto消费交易
//		 * param2:检查启用商户限制标志 
//		 */
//		assertThat( doAuthIsSupportMotoCheckTest( Indicator.N, Boolean.TRUE ), equalTo( "05" ) ); 
//		/** 
//		 * param1:商户限制中是否支持moto消费交易 = Y
//		 * param2:检查启用商户限制标志
//		 */
//		assertThat( doAuthIsSupportMotoCheckTest( Indicator.Y, Boolean.TRUE ), equalTo( "00" ) ); 
//		/** 
//		 * param1:商户限制中是否支持moto消费交易
//		 * param2:检查启用商户限制标志 = FALSE
//		 */
//		assertThat( doAuthIsSupportMotoCheckTest( Indicator.N, Boolean.FALSE ), equalTo( "00" ) ); 
//	}
//	
//	public String doAuthIsSupportElectronCheckTest( Indicator motoPosInd, Boolean merchantRestrictFlag ) {
//		 
//		YakMessage msg = initYakMessage();
//
//		/** 针对单个规则,覆盖初始报文内容,下列4个域决定是MOTO电子PHE消费 **/
//		msg.getBodyAttributes().put( 22, "011"  );
//		msg.getBodyAttributes().put( 18, "8911" );
//		// 特殊交易商户控制表(Key)
//		msg.getBodyAttributes().put( 42, "10001" );
//		msg.getBodyAttributes().put( 60, "1234577512555901234567890" );
//		msg.getBodyAttributes().remove( 35 );
//		msg.getBodyAttributes().remove( 52 );
//		
//
//		/** 针对单个规则,设置相关限制标志 **/
//		AuthProduct authProduct = parameterFacility.loadParameter( product.productCode, AuthProduct.class );
//		// 检查启用商户限制标志 = true(支持)
//		authProduct.checkEnabled.put( CheckType.MerchantRestrictFlag, merchantRestrictFlag );
//		
//		/** 获取默认参数中的商户交易控制参数,对商户限制中是否支持moto消费交易标志进行改写,满足各单元测试条件 **/
//		MerchantTxnCrtl mtc = parameterFacility.loadParameter( "10001", MerchantTxnCrtl.class );
//		mtc.supportEmotoInd = motoPosInd;
//
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter( product.productCode, authProduct );
//		parameterMock.putParameter( "10001", mtc );
//
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//		
//	}
//	
//	/**
//	 * 测试目的 : [Mer_006] 检查是否支持电子类消费交易
//	 * 测试组装的基本要素 :  
//	 * 		交易终端 = Moto电子类消费
//	 *		检查启用商户限制标志 = true(支持)
//	 *		商户限制中是否支持moto消费交易 = N(不支持)
//	 * 测试判断标准:  返回Reanso: TM01
//	 * 测试控制台输出：命中原因[TM01] 
//	 *  
//	 */
//	@Test
//	public void authIsSupportElectronCheckTest() {
//		/** 
//		 * param1:商户限制中是否支持moto电子消费交易
//		 * param2:检查启用商户限制标志
//		 */
//		assertThat( doAuthIsSupportElectronCheckTest( Indicator.N, Boolean.TRUE ), equalTo( "05" ) ); 
//		/** 
//		 * param1:商户限制中是否支持moto电子消费交易 = Y
//		 * param2:检查启用商户限制标志
//		 */
//		assertThat( doAuthIsSupportElectronCheckTest( Indicator.Y, Boolean.TRUE ), equalTo( "00" ) ); 
//		/** 
//		 * param1:商户限制中是否支持moto电子消费交易
//		 * param2:检查启用商户限制标志 = FALSE
//		 */
//		assertThat( doAuthIsSupportElectronCheckTest( Indicator.Y, Boolean.FALSE ), equalTo( "00" ) ); 
//	}
//	
//	public String doAuthIsSupportLoanCheckTest( Indicator loadInd, Boolean merchantRestrictFlag ) {
//		 
//		YakMessage msg = initYakMessage();
//
//		msg.getBodyAttributes().put( 22, "011"	);
//		msg.getBodyAttributes().put( 25, "64"	);
//		msg.getBodyAttributes().put( 18, "8911" );
//		// 特殊交易商户控制表(Key)
//		msg.getBodyAttributes().put( 42, "10001" );
//		msg.getBodyAttributes().put( 60, "1234577512555901234567890" );
//		msg.getBodyAttributes().remove( 35 );
//		msg.getBodyAttributes().remove( 52 );
//		
//
//		/** 针对单个规则,设置相关限制标志 **/
//		AuthProduct authProduct = parameterFacility.loadParameter( product.productCode, AuthProduct.class );
//		// 检查启用商户限制标志 = true(支持)
//		authProduct.checkEnabled.put( CheckType.MerchantRestrictFlag, merchantRestrictFlag );
//		
//		MerchantTxnCrtl mtc = parameterFacility.loadParameter( "10001", MerchantTxnCrtl.class );
//		mtc.supportLoanInd = loadInd;
//
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter( product.productCode, authProduct );
//		parameterMock.putParameter( "10001", mtc );
//
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//		
//	}
//	
//	/**
//	 * 测试目的 : [Mer_007] 检查是否支持分期消费交易
//	 * 测试组装的基本要素 :  
//	 * 		交易类型 = 分期消费
//	 *		检查启用商户限制标志 = true(支持)
//	 *		商户限制中是否支持分期消费交易 = N(不支持)
//	 * 测试判断标准:  返回Reanso: TM01
//	 * 测试控制台输出：命中原因[TM01] 
//	 *  
//	 */
//	@Test
//	public void authIsSupportLoanCheckTest() {
//		/** 
//		 * param1:商户限制中是否支持分期消费交易
//		 * param2:检查启用商户限制标志
//		 */
//		assertThat( doAuthIsSupportLoanCheckTest( Indicator.N, Boolean.TRUE ), equalTo( "05" ) ); 
//		/**  
//		 * param1:商户限制中是否支持分期消费交易 = Y
//		 * param2:检查启用商户限制标志
//		 */
//		assertThat( doAuthIsSupportLoanCheckTest( Indicator.Y, Boolean.TRUE ), equalTo( "00" ) ); 
//		/**  
//		 * param1:商户限制中是否支持分期消费交易
//		 * param2:检查启用商户限制标志 = FALSE
//		 */
//		assertThat( doAuthIsSupportLoanCheckTest( Indicator.N, Boolean.FALSE ), equalTo( "00" ) ); 
//	}
//	
//	/**
//	 * 测试目的 : [Mer_008] 检查是否支持大额分期消费交易
//	 * 测试组装的基本要素 :  
//	 * 		交易类型 = 大额分期消费
//	 *		检查启用商户限制标志 = true(支持)
//	 *		商户限制中是否支持大额分期消费交易 = N(不支持)
//	 * 测试判断标准:  返回Reanso: TM01
//	 * 测试控制台输出：命中原因[TM01] 
//	 *  
//	 */
//	/*@Test
//	public void AuthIsSupportBigAmountLoanCheckTest()
//	{
//		 
//		YakMessage msg = initYakMessage();
//
//		msg.getBodyAttributes().put(3, "00");
//		msg.getBodyAttributes().put(22, "011");
//		msg.getBodyAttributes().put(25, "64");
//		msg.getBodyAttributes().put(18, "1001");
//		msg.getBodyAttributes().put(35,"");
//		// 特殊交易商户控制表(Key)
//		msg.getBodyAttributes().put(42, "10001");
//		msg.getBodyAttributes().put(52, "");
//		msg.getBodyAttributes().put(60, "1234577512555901234567890");
//		msg.getCustomAttributes().put(CustomAttributesKey.INPUT_SOURCE,InputSource.CUP);
//		
//		UnifiedParameter<MerchantTxnCrtl> mtc = parameterMock.retrieveParameterObject("10001", MerchantTxnCrtl.class);
//		mtc.getParameter().supportEmotoInd = Indicator.N;
//		
//		// 检查启用商户限制标志 = true(支持)
//		authProduct.checkEnabled.put(CheckType.MerchantRestrictFlag, true);
//
//		YakMessage yak = authService.authorize(msg);
//		Assert.assertEquals("00",yak.getBodyAttributes().get(39));
//		 
//	}*/
//
//}
