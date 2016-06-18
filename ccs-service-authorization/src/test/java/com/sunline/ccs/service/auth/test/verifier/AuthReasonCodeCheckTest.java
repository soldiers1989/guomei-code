package com.sunline.ccs.service.auth.test.verifier;
//package com.sunline.ccs.service.auth.test.verifier;
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
//import com.sunline.ppy.dictionary.enums.AuthTransType;
//import com.sunline.ppy.dictionary.enums.ExpiryDateFlag;
//import com.sunline.ppy.dictionary.enums.Indicator;
//import com.sunline.ppy.dictionary.enums.InputSource;
//import com.sunline.ppy.dictionary.enums.PasswordVerifyResult;
//import com.sunline.pcm.param.def.Product;
//import com.sunline.pcm.param.test.BmpParamGenerator;
//import com.sunline.pcm.service.sdk.ParameterServiceMock;
//import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
//import com.sunline.ccs.param.def.AuthProduct;
//import com.sunline.ccs.param.def.CountryCtrl;
//import com.sunline.ccs.param.def.enums.AuthFlagAction;
//import com.sunline.ccs.param.def.enums.AuthTransTerminal;
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
//@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
//public class AuthReasonCodeCheckTest {
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
//
//	}
//	
//	/**
//	 * 初始化YakMsg
//	 * @return
//	 */
//	private YakMessage initYakMessage() {
//		YakMessage msg = new YakMessage();
//		msg.getHeadAttributes().put(11, "0100");
//		
//		msg.getBodyAttributes().put(2, "6200480204984198");
//		msg.getBodyAttributes().put(3, "30");
//		msg.getBodyAttributes().put(4, "1");
//		msg.getBodyAttributes().put(6, "1");
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
//		
//		msg.getBodyAttributes().put(45, "6012323232323232");
//		msg.getBodyAttributes().put(48, "ASIN0101234567890");
//		msg.getBodyAttributes().put(49, "156");
//		msg.getBodyAttributes().put(52, "10");
//		msg.getBodyAttributes().put(60, "1234577512555901234567890");
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
//		return msg;
//	}
//
//	/**
//	 * 初始化AuthProduct
//	 * @return
//	 */
//	public AuthProduct initAuthParameter(){
//		AuthProduct authProduct = parameterFacility.loadParameter(product.productCode, AuthProduct.class);
//		// [Ccm_005] 检查针对MCC的交易限制
//		authProduct.checkEnabled.put(CheckType.MccCodeCheckFlag, false);
//
//		// [Ccm_001] 检查针对国家的交易限制
//		authProduct.checkEnabled.put(CheckType.CountryCodeCheckFlag, true);	
//		CountryCtrl countryCtrl = parameterFacility.loadParameter("156", CountryCtrl.class);
//		countryCtrl.validInd = false;	
//		
//		return authProduct;
//	}
//
//	/**
//	 * 测试目的 : [Rsn_001] 交易/终端渠道交叉检查
//	 * 测试组装的基本要素 : AuthTransType.Inq 和 AuthTransTerminal.ATM
//	 * 测试判断标准: 返回responsCode = 00 通过
//	 */
//	public String doAuthReasonCode001CheckTest() {
//		YakMessage msg = initYakMessage();
//		initAuthParameter();
//		return authService.authorize(msg).getBody(39);
//	}
//
//	/**
//	 * 测试目的 : [Rsn_001] 交易/终端渠道交叉检查
//	 * 测试组装的基本要素 : AuthTransType.Inq 和 AuthTransTerminal.ATM
//	 * 测试判断标准: 返回Reanso: TS01
//	 * 测试控制台输出：1. 命中原因[TS01]，对应行为[D]；2. 排序后reasoncd =TS01优先级= 9000
//	 */
//	public String doAuthReasonCode002CheckTest() {
//		YakMessage msg = initYakMessage();
//		AuthProduct ap = initAuthParameter();
//		ap.transTypeTerminalEnabled.remove(AuthTransType.Inq);
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Rsn_001] 交易/终端渠道交叉检查
//	 * 测试组装的基本要素 : AuthTransType.Cash 和 AuthTransTerminal.ATM
//	 * 测试判断标准: 返回responsCode = 00 通过
//	 */
//	public String doAuthReasonCode003CheckTest() {
//		YakMessage msg = initYakMessage();
//		msg.getHeadAttributes().put(11, "0100");
//		msg.getBodyAttributes().put(03, "01");
//		AuthProduct ap = initAuthParameter();
//		ap.terminalEnabled.put(AuthTransTerminal.ATM, AuthFlagAction.Yes);
//		
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	/**
//	 * 测试目的 : [Rsn_001] 交易/终端渠道交叉检查
//	 * 测试组装的基本要素 : AuthTransType.Cash 和 AuthTransTerminal.EB
//	 * 测试判断标准: 返回Reanso: TS01
//	 * 测试控制台输出：1. 命中原因[TS01]，对应行为[D]；2. 排序后reasoncd =TS01优先级= 9000
//	 */
//	public String doAuthReasonCode004CheckTest() {
//		YakMessage msg = initYakMessage();
//		msg.getHeadAttributes().put(11, "0100");
//		msg.getBodyAttributes().put(03, "01");
//		msg.getBodyAttributes().put(40, "150");
//		msg.getBodyAttributes().put(18, "1001");
//		AuthProduct ap = initAuthParameter();
//		ap.checkEnabled.put(CheckType.CountryCodeCheckFlag, false);
//		
//		return authService.authorize(msg).getBody(39);
//	}
//	
//	@Test
//	public void authReasonCdCheckTest() {
//		// 查询 ATM（默认通过）
//		Assert.assertEquals("00", doAuthReasonCode001CheckTest());
//		Assert.assertEquals("58", doAuthReasonCode002CheckTest());
//
//		// 取现 ATM/EB
//		Assert.assertEquals("00", doAuthReasonCode003CheckTest());
//		Assert.assertEquals("58", doAuthReasonCode004CheckTest());
//		
//	}
//}
