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
//import junit.framework.Assert;
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
//public class AuthCrossCheck001Test {
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
//	public void setup() throws ParseException {
//		
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
//		
//		msg.getBodyAttributes().put(  2, "6200480204984198" );
//		msg.getBodyAttributes().put(  3, "000000" );
//		msg.getBodyAttributes().put(  4, "1" );
//		msg.getBodyAttributes().put( 14, "1234" );
//		msg.getBodyAttributes().put( 18, "8911" );
//		msg.getBodyAttributes().put( 19, "156" );
//		msg.getBodyAttributes().put( 22, "661" );
//		msg.getBodyAttributes().put( 23, "200" );
//		msg.getBodyAttributes().put( 32, "12345678" );
//		msg.getBodyAttributes().put( 33, "00160998" );
//		msg.getBodyAttributes().put( 35,"6200480204984198=12345678901234567890" );
//		msg.getBodyAttributes().put( 38, "600001" );
//		
//		// 特殊交易商户控制表(Key)
//		msg.getBodyAttributes().put( 42, "10001" );
//		msg.getBodyAttributes().put( 45, "6012323232323232" );
//		msg.getBodyAttributes().put( 48, "ASIN0101234567890" );
//		msg.getBodyAttributes().put( 49, "156" );
//		msg.getBodyAttributes().put( 52, "10" );
//		msg.getBodyAttributes().put( 61, "1168461641684964494984949849489497479468497940");
//		msg.getBodyAttributes().put( 100, "88888888" );
//
//		msg.getCustomAttributes().put( "cardActiveFlag", Indicator.Y );
//		msg.getCustomAttributes().put( "blockCodes", "1234ABCD" );
//		msg.getCustomAttributes().put( "expiryDateFlag", ExpiryDateFlag.Space );
//		msg.getCustomAttributes().put( "passwordVerifyResult",	PasswordVerifyResult.Approve );
//		msg.getCustomAttributes().put( CustomAttributesKey.BUSINESS_DATE_KEY_NAME,new Date() );
//		msg.getCustomAttributes().put( CustomAttributesKey.INPUT_SOURCE,InputSource.CUP );
//		msg.getCustomAttributes().put( CustomAttributesKey.MTI, "0100" );
//		
//		/** 针对单个规则,设置相关限制标志 **/
//		AuthProduct authProduct = parameterFacility.loadParameter( product.productCode, AuthProduct.class );
//		// 避免命中[Ccm_005] 检查针对MCC的交易限制
//		authProduct.checkEnabled.put( CheckType.MccCodeCheckFlag, false );
//		// 避免命中[Ccm_008]、[Ccm_009]、[Ccm_010]
//		authProduct.checkEnabled.put( CheckType.CountryCurrencyMccCrossCheckFlag, false );
//		
//		// 需要将改动后的对象, 重新put到Mock对象里
//		parameterMock.putParameter( product.productCode, authProduct );
//		
//		return msg;
//	}
//
//	public String doAuthInqCardCheckTest( String f02, String f14, String f35 ) {
//		 
//		YakMessage msg = initYakMessage();
//		msg.getBodyAttributes().put(  2, f02 );
//		msg.getBodyAttributes().put( 14, f14 );
//		msg.getBodyAttributes().put( 35, f35 );
//		
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//		 
//	}
//	/**
//	 * 测试目的 : 查询交易的卡号检查(Cro_001)
//	 * 测试组装的基本要素 :  第2域(6200480204984198), 第35域二磁道前16位卡号(5200480204984198)
//	 * 测试判断标准:  返回Reanso: TF05
//	 * 测试控制台输出：1. 命中原因[TF05]，对应行为[null]；2. 排序后reasoncd =TF05优先级= 6100
//	 *  
//	 */
//	@Test
//	public void authInqCardCheckTest() {
//		/** 
//		 * param1:第2域主账号 = 6200480204984198
//		 * param2:第14域卡片有效期 = 1234
//		 * param3:第35域二磁道 = 5200480204984198=12345678901234567890
//		 */
//		assertThat( doAuthInqCardCheckTest( "6200480204984198", "1234", "5200480204984198=12345678901234567890" ), equalTo( "30" ) ); 
//		/** 
//		 * param1:第2域主账号 = 6200480204984198
//		 * param2:第14域卡片有效期 = 1234
//		 * param3:第35域二磁道 = ""
//		 */
//		assertThat( doAuthInqCardCheckTest( "6200480204984198", "1234", "" ), equalTo( "00" ) ); 
//		/** 
//		 * param1:第2域主账号 = 6200480204984198
//		 * param2:第14域卡片有效期 = ""
//		 * param3:第35域二磁道 = 5200480204984198=12345678901234567890
//		 */
//		assertThat( doAuthInqCardCheckTest( "6200480204984198", "", "5200480204984198=12345678901234567890" ), equalTo( "30" ) ); 
//		/** 
//		 * param1:第2域主账号 = ""
//		 * param2:第14域卡片有效期 = 1234
//		 * param3:第35域二磁道 = 5200480204984198=12345678901234567890
//		 */
//		assertThat( doAuthInqCardCheckTest( "", "1234", "5200480204984198=12345678901234567890" ), equalTo( "30" ) ); 
//		/** 
//		 * param1:第2域主账号 = "6200480204984198"
//		 * param2:第14域卡片有效期 = 1234
//		 * param3:第35域二磁道 = 6200480204984198=12345678901234567890
//		 */
//		assertThat( doAuthInqCardCheckTest( "6200480204984198", "1234", "6200480204984198=12345678901234567890" ), equalTo( "00" ) ); 
//	}
//
//
//	public String doAuthInqIcCardCheck_05_95Test( String f22, String f60 ) {
//		 
//		YakMessage msg = initYakMessage();
//		msg.getBodyAttributes().put( 22, f22 );
//		msg.getBodyAttributes().put( 60, f60 );
//
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//		 
//	}
//	
//	/**
//	 * 测试目的 : 查询交易的[Cro_002] IC卡一致性05/95存在检查
//	 * 测试组装的基本要素 :  第22域前两位("05", "95"), 第60.2域的第二位必须不等于5
//	 * 测试判断标准:  返回Reanso: TF05
//	 * 测试控制台输出：1. 命中原因[TF05]，对应行为[null]；2. 排序后reasoncd =TF05优先级= 6100
//	 *  
//	 */
//	@Test
//	public void authInqIcCardCheck_05_95Test() {
//		/** 
//		 * param1:第22域前两位 = "05"
//		 * param2:第60域第5位 != 5
//		 */
//		assertThat( doAuthInqIcCardCheck_05_95Test( "051", "1234999555555Y55901234567890" ), equalTo( "30" ) ); 
//		/** 
//		 * param1:第22域前两位 = "95"
//		 * param2:第60域第5位 != 5
//		 */
//		assertThat( doAuthInqIcCardCheck_05_95Test( "951", "1234999555555Y55901234567890" ), equalTo( "30" ) ); 
//		/** 
//		 * param1:第22域前两位 = "05"
//		 * param2:第60域第5位 = 5
//		 */
//		assertThat( doAuthInqIcCardCheck_05_95Test( "051", "1234955555555Y55901234567890" ), equalTo( "00" ) ); 
//		/** 
//		 * param1:第22域前两位 = "95"
//		 * param2:第60域第5位 = 5
//		 */
//		assertThat( doAuthInqIcCardCheck_05_95Test( "951", "1234955555555Y55901234567890" ), equalTo( "00" ) ); 
//		/** 
//		 * param1:第22域前两位 = "66"
//		 * param2:第60域第5位 != 5
//		 */
//		assertThat( doAuthInqIcCardCheck_05_95Test( "661", "1234905555555Y55901234567890" ), equalTo( "00" ) ); 
//	}
//	
//	public String doAuthInqIcCardCheck_07_98Test( String f22, String f60 ) {
//		
//		YakMessage msg = initYakMessage();
//		msg.getBodyAttributes().put( 22, f22 );
//		msg.getBodyAttributes().put( 60, f60 );
//		
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//		 
//	}
//	
//	/**
//	 * 测试目的 : 查询交易的[Cro_003] IC卡一致性07/98存在检查
//	 * 测试组装的基本要素 :  第22域前两位("07", "98"), 第60.2域的第二位必须不等于6
//	 * 测试判断标准:  返回Reanso: TF05
//	 * 测试控制台输出：1. 命中原因[TF05]，对应行为[null]；2. 排序后reasoncd =TF05优先级= 6100
//	 *  
//	 */
//	@Test
//	public void authInqIcCardCheck_07_98Test() {
//		/** 
//		 * param1:第22域前两位 = "66"
//		 * param2:第60域第5位 = 6
//		 */
//		//assertThat( doAuthInqIcCardCheck_05_95Test( "661", "1234966666666Y55901234567890" ), equalTo( "30" ) ); 
//		/** 
//		 * param1:第22域前两位 = "07"
//		 * param2:第60域第5位 = 6
//		 */
//		//assertThat( doAuthInqIcCardCheck_05_95Test( "071", "1234966666666Y55901234567890" ), equalTo( "00" ) ); 
//		/** 
//		 * param1:第22域前两位 = "98"
//		 * param2:第60域第5位 = 6
//		 */
//		//assertThat( doAuthInqIcCardCheck_05_95Test( "981", "1234966666666Y55901234567890" ), equalTo( "00" ) ); 
//		/** 
//		 * param1:第22域前两位 = "66"
//		 * param2:第60域第5位 != 6
//		 */
//		//assertThat( doAuthInqIcCardCheck_05_95Test( "661", "1234906666666Y55901234567890" ), equalTo( "00" ) ); 
//		/** 
//		 * param1:第22域前两位 = "07"
//		 * param2:第60域第5位 != 6
//		 */
//		//assertThat( doAuthInqIcCardCheck_05_95Test( "071", "1234906666666Y55901234567890" ), equalTo( "30" ) ); 
//		/** 
//		 * param1:第22域前两位 = "98"
//		 * param2:第60域第5位 != 6
//		 */
//		//assertThat( doAuthInqIcCardCheck_05_95Test( "981", "1234906666666Y55901234567890" ), equalTo( "30" ) ); 
//	}
//	
//	public String doAuthInqCardSeriesCheckTest( String f22, String f23, String f60 ) {
//		YakMessage msg = initYakMessage();
//		msg.getBodyAttributes().put( 22, f22 );
//		msg.getBodyAttributes().put( 23, f23 );
//		msg.getBodyAttributes().put( 60, f60 );
//		
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//	}
//	
//	/**
//	 * 测试目的 : 查询交易的[Cro_004] 卡序列号检查
//	 * 测试组装的基本要素 : 第22域服务点输入方式码必须等于05/07/95/98, 第23域卡序列号必须大于0
//	 * 测试判断标准:  返回Reanso: TF05
//	 * 测试控制台输出：1. 命中原因[TF05]，对应行为[null]；2. 排序后reasoncd =TF05优先级= 6100
//	 *  
//	 */
//	@Test
//	public void authInqCardSeriesCheckTest() {
//		/** 
//		 * param1:第22域前两位 != "05", "07", "95", "98"
//		 * param2:第23域 == ""(不存在)
//		 */
//		assertThat( doAuthInqCardSeriesCheckTest( "66", "", "" ), equalTo( "00" ) ); 
//		/** 
//		 * param1:第22域前两位 == "05"
//		 * param2:第23域 == "1"(存在,并大于0)
//		 * param2:第60域第5位 == 5(cross_02的规则)
//		 */
//		assertThat( doAuthInqCardSeriesCheckTest( "05", "100", "1234956666666Y55901234567890" ), equalTo( "00" ) ); 
//		/** 
//		 * param1:第22域前两位 == "95"
//		 * param2:第23域 == "1"(存在,并大于0)
//		 * param2:第60域第5位 == 5(cross_02的规则)
//		 */
//		assertThat( doAuthInqCardSeriesCheckTest( "95", "100", "1234956666666Y55901234567890" ), equalTo( "00" ) ); 
//		/** 
//		 * param1:第22域前两位 == "07"
//		 * param2:第23域 == "1"(存在,并大于0)
//		 * param2:第60域第5位 == 6(cross_03的规则)
//		 */
//		assertThat( doAuthInqCardSeriesCheckTest( "07", "100", "1234966666666Y55901234567890" ), equalTo( "00" ) ); 
//		/** 
//		 * param1:第22域前两位 == "98"
//		 * param2:第23域 == "1"(存在,并大于0)
//		 * param2:第60域第5位 == 6(cross_03的规则)
//		 */
//		assertThat( doAuthInqCardSeriesCheckTest( "98", "100", "1234966666666Y55901234567890" ), equalTo( "00" ) ); 
//		/** 
//		 * param1:第22域前两位 == "05",
//		 * param2:第23域 == "-1"(存在)
//		 * param2:第60域第5位 == 5(cross_02的规则)
//		 */
//		assertThat( doAuthInqCardSeriesCheckTest( "05", "-1", "1234956666666Y55901234567890" ), equalTo( "30" ) ); 
//		/** 
//		 * param1:第22域前两位 == "95",
//		 * param2:第23域 == "-1"(存在)
//		 * param2:第60域第5位 == 5(cross_02的规则)
//		 */
//		assertThat( doAuthInqCardSeriesCheckTest( "95", "-1", "1234956666666Y55901234567890" ), equalTo( "30" ) ); 
//		/** 
//		 * param1:第22域前两位 == "07",
//		 * param2:第23域 == "-1"(存在)
//		 * param2:第60域第5位 == 6(cross_03的规则)
//		 */
//		assertThat( doAuthInqCardSeriesCheckTest( "07", "-1", "1234966666666Y55901234567890" ), equalTo( "30" ) ); 
//		/** 
//		 * param1:第22域前两位 == "98",
//		 * param2:第23域 == "-1"(存在)
//		 * param2:第60域第5位 == 6(cross_03的规则)
//		 */
//		assertThat( doAuthInqCardSeriesCheckTest( "98", "-1", "1234966666666Y55901234567890" ), equalTo( "30" ) ); 
//	}
//	
//	public String doAuthInqPinDataCheckTest( String f03, String f18, String f52 ) {
//		 
//		YakMessage msg = initYakMessage();
//		msg.getBodyAttributes().put(  3, f03 );
//		msg.getBodyAttributes().put( 18, f18 );
//		msg.getBodyAttributes().put( 52, f52 );
//		
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//		 
//	}
//	
//	/** 
//	 * 测试目的 : 查询交易的[Cro_005] ATM个人标识检查
//	 * 测试组装的基本要素 : 8583交易特征信息中的交易终端必须是ATM, 第52域个人标识码必须存在
//	 * 测试判断标准:  返回Reanso: TF05
//	 * 测试控制台输出：1. 命中原因[TF05]，对应行为[null]；2. 排序后reasoncd =TF05优先级= 6100
//	 *  
//	 */
//	@Test 
//	public void authInqPinDataCheckTest() {
//		/** 
//		 * param1:第03域  == "100000"(指定为取现下的交易)
//		 * param2:第18域  == "6011"(ATM)
//		 * param2:第52域  == null
//		 */
//		assertThat( doAuthInqPinDataCheckTest( "010000", "6011", null ), equalTo( "30" ) ); 
//	}
//	
//	/**
//	 * 测试目的 : 消费交易的[Cro_006] 交易类型检查
//	 * 测试组装的基本要素 : 第3域交易处理码等于00/03( 00/03:属于消费类 )的情况下,第18域的商户类型不支持6010柜台和6011ATM
//	 * 测试判断标准:  返回Reanso: TF03
//	 * 测试控制台输出：1. 命中原因[TF03]，对应行为[null]；2. 排序后reasoncd =TF03优先级= 6100
//	 *  
//	 */
//	@Test
//	public void AuthExpenseTransactionTypeCheckTest()
//	{
//		assertThat(doAuthExpenseTransactionTypeCheck( "6011" ), equalTo("30")); 
//		//Assert.assertEquals("30", doAuthExpenseTransactionTypeCheck("6011", "000000"));
//		//Assert.assertEquals("30", doAuthExpenseTransactionTypeCheck("6010", "000000"));
//		 
//	}
//
//	private String doAuthExpenseTransactionTypeCheck( String mcc ) {
//		YakMessage msg = initYakMessage();
//		
//		msg.getBodyAttributes().put(18, mcc);
//		
//		YakMessage yak = authService.authorize( msg );
//		return yak.getBodyAttributes().get( 39 );
//	}
//	
//	/**
//	 * 测试目的 : 消费交易的[Cro_008] 交易金额检查
//	 * 测试组装的基本要素 : 第3域交易处理码等于00的情况下,第4域的交易金额必须大于0
//	 * 测试判断标准:  返回Reanso: TF03
//	 * 测试控制台输出：1. 命中原因[TF03]，对应行为[null]；2. 排序后reasoncd =TF03优先级= 6100
//	 *  
//	 */
//	@Test
//	public void AuthExpenseTransactionAmountCheckTest()
//	{
//		 
//		YakMessage msg = new YakMessage();
//		msg.getHeadAttributes().put(11, "0100");
//		
//		msg.getBodyAttributes().put(2, "6200480204984198");
//		msg.getBodyAttributes().put(3, "00");
//		msg.getBodyAttributes().put(4, "0");
//		msg.getBodyAttributes().put(6, "1");
//		msg.getBodyAttributes().put(14, "1234");
//		msg.getBodyAttributes().put(18, "1001");
//		msg.getBodyAttributes().put(19, "156");
//		msg.getBodyAttributes().put(22, "981");
//		msg.getBodyAttributes().put(23, "100");
//		msg.getBodyAttributes().put(35,"6200480204984198=12345678901234567890");
//		msg.getBodyAttributes().put(38, "600001");
//		msg.getBodyAttributes().put(42, "10001");
//		msg.getBodyAttributes().put(45, "6012323232323232");
//		msg.getBodyAttributes().put(48, "ASIN010123");//ASIN0101234567890
//		msg.getBodyAttributes().put(49, "156");
//		msg.getBodyAttributes().put(52, "52");
//		msg.getBodyAttributes().put(60, "1234965555555Y55901234567890");
//
//		msg.getCustomAttributes().put("cardActiveFlag", Indicator.Y);
//		msg.getCustomAttributes().put("blockCodes", "1234ABCD");
//		msg.getCustomAttributes().put("expiryDateFlag", ExpiryDateFlag.Space);
//		msg.getCustomAttributes().put("passwordVerifyResult",	PasswordVerifyResult.Approve);
//		msg.getCustomAttributes().put(CustomAttributesKey.BUSINESS_DATE_KEY_NAME,new Date());
//		msg.getCustomAttributes().put(CustomAttributesKey.INPUT_SOURCE,InputSource.CUP);
//		msg.getCustomAttributes().put(CustomAttributesKey.MTI, "0100");
//
//		AuthProduct authProduct = parameterFacility.loadParameter(product.productCode, AuthProduct.class);
//		// [Ccm_005] 检查针对MCC的交易限制
//		authProduct.checkEnabled.put(CheckType.MccCodeCheckFlag, false);
//		// [Ccm_001] 检查针对国家的交易限制
//		authProduct.checkEnabled.put(CheckType.CountryCodeCheckFlag, false);
//		
//		
//		
//		YakMessage yak = authService.authorize(msg);
//		Assert.assertEquals("13",yak.getBodyAttributes().get(39));
//		 
//	}
//	
//	/**
//	 * 测试目的 : 消费交易的[Cro_009] 交易金额及持卡人扣账金额检查
//	 * 测试组装的基本要素 : 第4域的交易金额必须大于0 或者 第6域的交易金额必须大于0
//	 * 测试判断标准:  返回Reanso: TF03
//	 * 测试控制台输出：1. 命中原因[TF03]，对应行为[null]；2. 排序后reasoncd =TF03优先级= 6100
//	 *  
//	 */
//	@Test
//	public void AuthExpenseTransactionAmountAndHolderCheckTest()
//	{
//		 
//		YakMessage msg = new YakMessage();
//		msg.getHeadAttributes().put(11, "0100");
//		
//		msg.getBodyAttributes().put(2, "6200480204984198");
//		msg.getBodyAttributes().put(3, "00");
//		msg.getBodyAttributes().put(4, "1");
//		msg.getBodyAttributes().put(6, "-1");
//		msg.getBodyAttributes().put(14, "1234");
//		msg.getBodyAttributes().put(18, "1001");
//		msg.getBodyAttributes().put(19, "156");
//		msg.getBodyAttributes().put(22, "981");
//		msg.getBodyAttributes().put(23, "100");
//		msg.getBodyAttributes().put(35,"6200480204984198=12345678901234567890");
//		msg.getBodyAttributes().put(38, "600001");
//		msg.getBodyAttributes().put(42, "10001");
//		msg.getBodyAttributes().put(45, "6012323232323232");
//		msg.getBodyAttributes().put(48, "ASIN0101234567890");
//		msg.getBodyAttributes().put(49, "156");
//		msg.getBodyAttributes().put(52, "52");
//		msg.getBodyAttributes().put(60, "1234965555555Y55901234567890");
//
//		msg.getCustomAttributes().put("cardActiveFlag", Indicator.Y);
//		msg.getCustomAttributes().put("blockCodes", "1234ABCD");
//		msg.getCustomAttributes().put("expiryDateFlag", ExpiryDateFlag.Space);
//		msg.getCustomAttributes().put("passwordVerifyResult",	PasswordVerifyResult.Approve);
//		msg.getCustomAttributes().put(CustomAttributesKey.BUSINESS_DATE_KEY_NAME,new Date());
//		msg.getCustomAttributes().put(CustomAttributesKey.INPUT_SOURCE,InputSource.CUP);
//		msg.getCustomAttributes().put(CustomAttributesKey.MTI, "0100");
//
//		AuthProduct authProduct = parameterFacility.loadParameter(product.productCode, AuthProduct.class);
//		// [Ccm_005] 检查针对MCC的交易限制
//		authProduct.checkEnabled.put(CheckType.MccCodeCheckFlag, false);
//		// [Ccm_001] 检查针对国家的交易限制
//		authProduct.checkEnabled.put(CheckType.CountryCodeCheckFlag, false);
//		
//		
//		
//		YakMessage yak = authService.authorize(msg);
//		Assert.assertEquals("13",yak.getBodyAttributes().get(39));
//		 
//	}
//
//}
