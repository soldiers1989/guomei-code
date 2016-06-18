package com.sunline.ccs.service.auth.test.handler;
//package com.sunline.ccs.service.auth.test.processor;
//
//import java.math.BigDecimal;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//
//import junit.framework.Assert;
//
//import org.apache.commons.codec.binary.Hex;
//import org.apache.commons.lang.StringUtils;
//import org.apache.commons.lang.time.DateUtils;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.sunline.ppy.api.AuthorizationService;
//import com.sunline.ppy.api.CustomAttributesKey;
//import com.sunline.ppy.api.MediumInfo;
//import com.sunline.ppy.dictionary.enums.AccountType;
//import com.sunline.ppy.dictionary.enums.AddressType;
//import com.sunline.ppy.dictionary.enums.DdIndicator;
//import com.sunline.ppy.dictionary.enums.DualBillingInd;
//import com.sunline.ppy.dictionary.enums.ExpiryDateFlag;
//import com.sunline.ppy.dictionary.enums.Gender;
//import com.sunline.ppy.dictionary.enums.Indicator;
//import com.sunline.ppy.dictionary.enums.InputSource;
//import com.sunline.ppy.dictionary.enums.ManualAuthFlag;
//import com.sunline.ppy.dictionary.enums.PasswordVerifyResult;
//import com.sunline.ppy.dictionary.enums.ProductType;
//import com.sunline.ppy.dictionary.enums.SmsInd;
//import com.sunline.ppy.dictionary.enums.StmtMediaType;
//import com.sunline.pcm.param.def.Product;
//import com.sunline.pcm.param.test.BmpParamGenerator;
//import com.sunline.pcm.service.sdk.ParameterServiceMock;
//import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
//import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
//import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
//import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
//import com.sunline.ccs.infrastructure.shared.model.CcsAcctKey;
//import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
//import com.sunline.ccs.infrastructure.shared.model.CcsAcctOKey;
//import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
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
//public class AuthProcessorTest {
//	
//	@Autowired
//	private AuthorizationService authService;
//	
//	@PersistenceContext
//	private EntityManager em;
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
//
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
//	@Autowired
//	private RCcsAcctO rCcsAcctO;
//	
//	@Autowired
//	private RCcsLoanReg rTmloanReg; 
//	
//	private Product product;
//	
//	public Date  today;
//	
//	@Before
//	public void setup() throws ParseException
//	{
//		OrganizationContextHolder.setCurrentOrg("123456789");
//		  today = new SimpleDateFormat("yyyyMMdd").parse("20120803");
//		globalMock.setupBatchDate(today, DateUtils.addDays(today, -1));
//		
//		product = bmpParamGenerator.loadParameters(parameterMock, null);
//		product.productType = ProductType.M;
//		defaultParamGenerator.loadParameters(parameterMock, product.productCode);
//		
//		card = dataGenerator.createActMainCard(product.productCode, today);
//		
//		MediumInfo info = new MediumInfo();
//		info.setArqcVerifyResult(PasswordVerifyResult.Approve);
//		info.setLogicCardNbr(card.getCcsCardO().get(0).getLogicCardNbr());
//		mediumMock.setMediumInfo(info);
//		
//		
//	}
//	
//	
//	/**
//	 * 测试目的 :测试借记[消费]
//	 * 测试基本要素：  MTI =0100 B003ProcCode = 00 ProcessId = authRetailProcessor
//	 * 测试判断标准: 返回responsCode = 00 通过
//	 *  
//	 */
//	@Test
//	public void TestAuthRetailProcessor()
//	{
//		YakMessage msg = new YakMessage();
//		msg.getHeadAttributes().put(11, "0100");
//		msg.getBodyAttributes().put(2, "6200480204984198");
//		msg.getBodyAttributes().put(3, "00");
//		msg.getBodyAttributes().put(4, "111111");
//		msg.getBodyAttributes().put(6, "1000");
//		msg.getBodyAttributes().put(7, "0512153056");
//		msg.getBodyAttributes().put(11, "000007");
//		msg.getBodyAttributes().put(14, "1209");
//		msg.getBodyAttributes().put(18, "5999");
//		msg.getBodyAttributes().put(19, "156");
//		msg.getBodyAttributes().put(22, "022");
//		msg.getBodyAttributes().put(32, "12345678");
//		msg.getBodyAttributes().put(33, "12345678");
//		msg.getBodyAttributes().put(35, "6200480204984198=12095678901234567890");
//		msg.getBodyAttributes().put(37, "123456");
//		msg.getBodyAttributes().put(38, "600001");
//		msg.getBodyAttributes().put(42, "4001");
//		msg.getBodyAttributes().put(45, "6012323232323232");
//		msg.getBodyAttributes().put(48, "495030323530303120202020202020202020202020202020202020202020202020204e3030303030202020202020202020202020202020202020202020202020");
//		msg.getBodyAttributes().put(49, "156");
////		msg.getBodyAttributes().put(52, "10");
////		msg.getBodyAttributes().put(60, "111115661111111");
//
//		msg.getCustomAttributes().put("cardActiveFlag", Indicator.Y);
//		msg.getCustomAttributes().put("blockCodes", "1234ABCD");
//		msg.getCustomAttributes().put("expiryDateFlag", ExpiryDateFlag.Space);
//		msg.getCustomAttributes().put("passwordVerifyResult",	PasswordVerifyResult.Approve);
//		msg.getCustomAttributes().put(CustomAttributesKey.BUSINESS_DATE_KEY_NAME,today);
//		msg.getCustomAttributes().put(CustomAttributesKey.INPUT_SOURCE,InputSource.BANK);
//		msg.getCustomAttributes().put(CustomAttributesKey.MTI, "0200");
//		msg.getCustomAttributes().put(CustomAttributesKey.MANUAL_AUTH_FLAG,ManualAuthFlag.A);
//
//		AuthProduct authProduct = parameterFacility.loadParameter(product.productCode, AuthProduct.class);
//		// [Ccm_005] 检查针对MCC的交易限制
//		authProduct.checkEnabled.put(CheckType.MccCodeCheckFlag, false);
//		// [Ccm_001] 检查针对国家的交易限制
//		authProduct.checkEnabled.put(CheckType.CountryCodeCheckFlag, false);
//		 
//		YakMessage yak;
//		try {
//			yak = authService.authorize(msg);
//		} catch (Exception e) {
//			yak = msg;
//			e.printStackTrace();
//		}
//		for(int i=1; i<=128;i++){
//			if( yak.getBodyAttributes().containsKey(i)){
//				System.out.println(i+"----"+yak.getBodyAttributes().get(i));
//			}
//		}
//		for(CcsAcctO a : rCcsAcctO.findAll()){
//			System.out.println(a.convertToMap());
//		}
//		
//		for(CcsLoanReg r : rTmloanReg.findAll()){
//			System.out.println(r.convertToMap());
//		}
//		Assert.assertEquals("11",yak.getBodyAttributes().get(39));
//	}
//}
