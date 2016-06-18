//package com.sunline.ccs.service.process.test;
//
//import java.math.BigDecimal;
//import java.text.DateFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.transaction.TransactionConfiguration;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.sunline.acm.service.api.GlobalManagementService;
//import com.sunline.ark.support.OrganizationContextHolder;
//import com.sunline.ccs.facility.BlockCodeUtils;
//import com.sunline.ccs.facility.CustAcctCardFacility;
//import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
//import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
//import com.sunline.ccs.infrastructure.server.repos.RCcsAcctNbr;
//import com.sunline.ccs.infrastructure.server.repos.RCcsAcctO;
//import com.sunline.ccs.infrastructure.server.repos.RCcsAddress;
//import com.sunline.ccs.infrastructure.server.repos.RCcsCard;
//import com.sunline.ccs.infrastructure.server.repos.RCcsCardUsage;
//import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
//import com.sunline.ccs.infrastructure.server.repos.RCcsCustomerCrlmt;
//import com.sunline.ccs.infrastructure.server.repos.RCcsEmployee;
//import com.sunline.ccs.infrastructure.server.repos.RCcsLinkman;
//import com.sunline.ccs.infrastructure.server.repos.RCcsLoanReg;
//import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
//import com.sunline.ccs.infrastructure.shared.model.CcsCardnbrGrt;
//import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
//import com.sunline.ccs.otb.AcctOTB;
//import com.sunline.ccs.param.def.AccountAttribute;
//import com.sunline.ccs.param.def.Fee;
//import com.sunline.ccs.param.def.LoanFeeDef;
//import com.sunline.ccs.param.def.LoanPlan;
//import com.sunline.ccs.param.def.ProductCredit;
//import com.sunline.ccs.param.def.enums.AuthTransDirection;
//import com.sunline.ccs.service.context.TxnContext;
//import com.sunline.ccs.service.context.TxnInfo;
//import com.sunline.ccs.service.entity.S30001LoanReq;
//import com.sunline.ccs.service.handler.SunshineCommService;
//import com.sunline.ccs.service.handler.sunshine.TNRSunshineLoan;
//import com.sunline.ccs.service.handler.sunshine.UnifiedParameterFacilityMock;
//import com.sunline.ccs.service.process.CustomerServiceImpl;
//import com.sunline.pcm.param.def.Product;
//import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
//import com.sunline.ppy.dictionary.enums.AuthTransType;
//import com.sunline.ppy.dictionary.enums.Gender;
//import com.sunline.ppy.dictionary.enums.IdType;
//import com.sunline.ppy.dictionary.enums.InputSource;
//import com.sunline.ppy.dictionary.enums.LoanAction;
//import com.sunline.ppy.dictionary.enums.LoanUsage;
//import com.sunline.ppy.dictionary.exception.ProcessException;
//
//
///** 
//* @author lizz
//* @version 创建时间：2015-8-19 下午15:15
//* 测试类
//*/ 
//
//@RunWith(SpringJUnit4ClassRunner.class)
////@ContextConfiguration("/test-service.xml")
////@ContextConfiguration(locations="classpath:/service-context-test.xml")
//@ContextConfiguration("/service-context-test.xml")
//@Transactional
//@TransactionConfiguration(defaultRollback = false)
//public class SunshineServiceImplTest {
//	
//	@Autowired
//	private RCcsAddress rCcsAddress;
//	@Autowired
//	private RCcsLinkman rCcsLinkman;
//	
//	@Autowired
//	private CustomerServiceImpl cpsCustomerServiceImpl;
//	@Autowired
//	private TNRSunshineLoan tnrSunshineLoan;
//	@Autowired
//    private RCcsCard rCcsCard;
//	 @Autowired
//	    private UnifiedParameterFacility unifiedParameterFacility;
//	    @Autowired
//	    private UnifiedParameterFacilityMock unifiedParameterFacilityMock;
//	    @Autowired
//	    CustAcctCardFacility queryFacility;
//	    @Autowired
//	    private RCcsCustomer rCustomer;
//	    @Autowired
//	    private RCcsAddress rAddress;
//	    @Autowired
//	    private RCcsLinkman rLinkman;
//	    @Autowired
//	    private RCcsAcct rAcct;
//	    @Autowired
//	    private RCcsAcctO rAcctO;
//	    @Autowired
//	    private RCcsEmployee rCcsEmployee;
//		@PersistenceContext
//		private EntityManager em;
//	    @Autowired
//	    private GlobalManagementService globalManagementService;
//	    @Autowired
//	    private BlockCodeUtils blockCodeUtils;
//	    @Autowired
//	    private RCcsAcctNbr rCcsAcctNbr;
//	    @Autowired
//		private CustAcctCardFacility custAcctCardQueryFacility;
//		@Autowired
//		private UnifiedParamFacilityProvide unifiedParamFacilityProvide;
//		@Autowired
//		private SunshineCommService sunshineCommService;
//		@Autowired
//	    private RCcsCardUsage rCcsCardUsage;
//		@Autowired
//		private RCcsCustomerCrlmt rCustomerCrLmt;
//		@Autowired
//		private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
//		@Autowired
//		private UnifiedParameterFacilityMock unifiedParameterFacilityProvideMock;
//		@Autowired
//		private RCcsLoanReg rCcsLoanReg;
//		@Autowired
//		private AcctOTB accountOTB;
//
////	private Integer cust_Id = Integer.valueOf(DataFixtures.getColunmValue(0,CcsCustomer.TABLE_NAME, 
////			DataFixtures.getColumnName(CcsCustomer.class, CcsCustomer.P_CustId)).toString());
////	private String org = DataFixtures.getColunmValue(0,CcsCardLmMapping.TABLE_NAME, 
////			DataFixtures.getColumnName(CcsCardLmMapping.class, CcsCardLmMapping.P_Org)).toString();
//
////	private String id_No = DataFixtures.getColunmValue(0,CcsCustomer.TABLE_NAME, 
////			DataFixtures.getColumnName(CcsCustomer.class, CcsCustomer.P_IdNo)).toString();
////	private String id_Type = DataFixtures.getColunmValue(0,CcsCustomer.TABLE_NAME, 
////			DataFixtures.getColumnName(CcsCustomer.class, CcsCustomer.P_IdType)).toString();
////	
////	private String card_No = DataFixtures.getColunmValue(0,CcsCardLmMapping.TABLE_NAME, 
////			DataFixtures.getColumnName(CcsCardLmMapping.class, CcsCardLmMapping.P_CardNbr)).toString();
//	
//
//	/**
//	 * 
//	 */
//	@Before
//	public void setupDatabase() {
//		OrganizationContextHolder.setCurrentOrg("000000000001");
//		OrganizationContextHolder.setUsername("dfd");
//	}
//	
//
//
////	/**
////	 * 
////	 */
////	@Test
////	public void testNF0000(){
////		try {
////			String addType = DataFixtures.getColunmValue(0, CcsAddress.TABLE_NAME,
////					DataFixtures.getColumnName(CcsAddress.class, CcsAddress.P_AddrType)).toString();
////			Map<String,Serializable> ccsCustomerMap = cpsCustomerServiceImpl.NF0000(card_No, AddressType.valueOf(addType));
////			Assert.assertNotNull(ccsCustomerMap);
////		} catch (ProcessException e) {			
////			e.printStackTrace();
////		}
////	}
////	/**
////	 * 
////	 */
////	@Test
////	public void testNF1101() {
////		try {
////			Map<String, Serializable> map = cpsCustomerServiceImpl.NF1101(card_No);
////			Assert.assertNotNull(map);
////			Assert.assertEquals((String) map.get(CcsCustomer.P_CustId).toString(), this.cust_Id.toString());
////		} catch (ProcessException e) {
////			
////			e.printStackTrace();
////		}
////	}
////
////	/**
////	 * 
////	 */
////	@Test
////	public void testNF1102() {
////
////		try {
////			Map<String, Serializable> map = cpsCustomerServiceImpl.NF1102(IdType.valueOf(this.id_Type), this.id_No);
////			Assert.assertNotNull(map);
////			Assert.assertEquals((String) map.get(CcsCustomer.P_CustId).toString(), this.cust_Id.toString());
////		} catch (ProcessException e) {
////			
////			e.printStackTrace();
////		}
////	}
////
////	/**
////	 * 
////	 */
////	@Test
////	public void testNF1103() {
////		try {
////			Map<String, Serializable> customermap = cpsCustomerServiceImpl.NF1103(cust_Id);
////			Assert.assertNotNull(customermap);
////			String cust_id = customermap.get(CcsCustomer.P_CustId).toString();
////			Assert.assertEquals(cust_Id.toString(), cust_id);
////		} catch (ProcessException e) {
////			e.printStackTrace();
////		}
////	}
////	
////	/**
////	 * 
////	 */
////	@Test
////	public void testNF1105() {
////		try {
////			List<Map<String, Serializable>> address = cpsCustomerServiceImpl.NF1105(cust_Id);
////			for(Map<String, Serializable> tmaddressmap :address){
////				Assert.assertEquals(tmaddressmap.get(CcsAddress.P_CustId), cust_Id);
////			}
////		} catch (ProcessException e) {
////			e.printStackTrace();
////		}
////	}
////	
////	/**
////	 * 
////	 */
////	@Test
////	public void testNF1107() {
////		try {
////			List<Map<String, Serializable>> tmContact = cpsCustomerServiceImpl.NF1107(cust_Id);
////			for(Map<String, Serializable> tmcontactmap :tmContact){
////				Assert.assertEquals(tmcontactmap.get(CcsLinkman.P_CustId), cust_Id);
////			}
////		} catch (ProcessException e) {
////			e.printStackTrace();
////		}
////	}
////
////	/**
////	 * 
////	 */
////	@Test
////	public void testNF1201() {
////		Map<String, Serializable> customermap;
////		try {
////			customermap = cpsCustomerServiceImpl.NF1103(cust_Id);
////			customermap.put(CcsCustomer.P_IdNo, id_No);
////			cpsCustomerServiceImpl.NF1201(customermap);
////			Assert.assertEquals(cpsCustomerServiceImpl.NF1103(cust_Id).get(CcsCustomer.P_IdNo), id_No);
////		} catch (ProcessException e) {
////			
////			e.printStackTrace();
////		}
////	}
////
////	/**
////	 * 
////	 */
////	@Test
////	public void testNF1202() {
////		try {
////		Iterator<Map<String, Serializable>> addrlist = cpsCustomerServiceImpl.NF1105(this.cust_Id).iterator();
////		Assert.assertTrue(addrlist.hasNext());
////		Map<String, Serializable> addrmap = addrlist.next();
////		Long address_id = (Long) addrmap.get(CcsAddress.P_AddrId);
////		String str_city = "beijing";
////		addrmap.put(CcsAddress.P_City, str_city);		
////			//cpsCustomerServiceImpl.NF1202(this.cust_Id, addrmap);
////		Assert.assertEquals(str_city, rCcsAddress.findOne(address_id).getCity());
////		} catch (ProcessException e) {
////			
////			e.printStackTrace();
////		}
////	}
////
////	/**
////	 * 
////	 */
////	@Test
////	public void testNF1203() {
////		try {
////			Iterator<Map<String, Serializable>> contactlist = cpsCustomerServiceImpl.NF1107(this.cust_Id).iterator();
////			Assert.assertTrue(contactlist.hasNext());
////			Map<String, Serializable> contactmap = contactlist.next();
////			Long contact_id = (Long) contactmap.get(CcsLinkman.P_LinkmanId);
////			String str_mobile_no = "1351234568";
////			contactmap.put(CcsLinkman.P_MobileNo, str_mobile_no);		
////				//cpsCustomerServiceImpl.NF1203(this.cust_Id, contactmap,);
////				CcsLinkman rt = rCcsLinkman.findOne(contact_id);
////			Assert.assertEquals(str_mobile_no, rt.getMobileNo());
////			} catch (ProcessException e) {
////				
////				e.printStackTrace();
////			}
////	}
//	
//	@Test
//	public void testGetMaxCardNbr() {
//		try {
//			Product product = new Product();
//			product.cardnoRangeCeil = "1000";
//			Long l = tnrSunshineLoan.getMaxCardNbr(product);
//			Assert.assertNotNull(l);
//			System.out.println(l);
//		} catch (ProcessException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	/**
//	 * 产生唯一流水号
//	 * @return
//	 */
//	@Test
//	public void testGenerateFlowNo(){
//		try {
//			DateFormat df = new SimpleDateFormat("yyDS");
//			Calendar c = Calendar.getInstance();
//			int hour = c.get(Calendar.HOUR_OF_DAY);
//			int minute = c.get(Calendar.MINUTE);
//			int second = c.get(Calendar.SECOND);
//			StringBuffer sb = new StringBuffer(df.format(c.getTime()));
//			sb.append(hour*60*60+minute*60+second);
//			System.out.println(sb);//1523633655132
//		} catch (ProcessException e) {
//			e.printStackTrace();
//		}
//		
//	}
//	 /**
//     * 
//     * @see 方法名：getStringForLength
//     * @see 描述：获得给定长度字符串
//     * @see 创建日期：2015年8月17日
//     * @author lizz
//     * 
//     * @param c
//     * @param length
//     * @return
//     * 
//     * @see 修改记录：
//     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
//     */
//	@Test
//    public void getStringForLength() {
//		try {
//			char c = 'l';
//			int l = 4;
//			String rTest = "llll";
//			String sb = "";
//			sb = tnrSunshineLoan.getStringForLength(c, l).toString();
//			Assert.assertEquals(rTest, sb);
//		} catch (ProcessException e) {
//			e.printStackTrace();
//		}
//	
////	return sb;
//    }
//	
//	 /**
//     * 
//     * @see 方法名：formatCardNbr
//     * @see 描述：生成格式化的卡号
//     * @see 创建日期：2015年8月17日
//     * @author lizz
//     * 
//     * @param config
//     * @param product
//     * @return
//     * 
//     * @see 修改记录：
//     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
//     */
//	@Test
//    public void testGetNextCardNbr() {
//		CcsCardnbrGrt config;
//		Product product ;
//		String cardNbr = "";
//		String testCardNbr = "9000";
//		String testFormatCardNbr = "82090002";
//		try {
//			product = new Product();
//			product.productCode = "480001";
//			product.cardnoRangeFlr = "9000";
//			product.bin = "820";
////			System.out.println( OrganizationContextHolder.getCurrentOrg());
//			config = tnrSunshineLoan.getNextCardNbr(product, "000000000001");
//			cardNbr = tnrSunshineLoan.formatCardNbr(config, product);
//			Assert.assertEquals(testCardNbr, config.getCurrValue());
//			Assert.assertEquals(testCardNbr, cardNbr);
//		} catch (ProcessException e) {
//			e.printStackTrace();
//		}
//	
//    }
//	 /**
//     * 
//     * @see 方法名：createCardNbr
//     * @see 描述：根据产品和机构获得卡号
//     * @see 创建日期：2015年8月14日上午11:26:54
//     * @author lizz
//     * 
//     * @param productCode
//     * @param org
//     * @return
//     * @throws ProcessException
//     * 
//     * @see 修改记录：
//     * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
//     */
//	@Test
//    public void testCreateCardNbr() {
//		Product product ;
//		String testCreateCardNbr = "9000";
//		String createCardNbr = "";
//		try {
//			product = new Product();
//			product.productCode = "480001";
//			product.cardnoRangeFlr = "9000";
//			product.bin = "820";
//			createCardNbr = tnrSunshineLoan.createCardNbr(product.productCode, "000000000001");
//			Assert.assertEquals(testCreateCardNbr, createCardNbr);
//		} catch (ProcessException e) {
//			e.printStackTrace();
//		}
//	
//    }
//	
//	/**
//     * @see 方法名：mergeCustomer
//     * @see 描述：客户信息维护
//     */
//	@Test
//    public void testMergeCustomer() throws ParseException {
//		TxnContext context = initContext();
//		tnrSunshineLoan.mergeCustomer(context);
//    }
//	 /**
//     * @see 方法名：mergeCustomerCrLmt
//     * @see 描述： 获取(创建)客户层信用额度
//     */
//	@Test
//	 public void testMergeCustomerCrLmt() {
//		TxnContext context = initContext();
//		tnrSunshineLoan.mergeCustomerCrLmt(context);
//	    }
//
//    /**
//     * 
//     * @see 方法名：mergeCard
//     */
//	@Test
//    public void testMergeCard() throws ParseException {
////    	S30001LoanReq req = new S30001LoanReq();
////    	req.dataid="10010101010";
////    	req.org = OrganizationContextHolder.getCurrentOrg();
////    	req.productcd = "480001";
////    	req.card_no = "982322311";
////    	Product product = new Product();
////    	product.productCode = "480001";
////		product.cardnoRangeFlr = "9000";
////		product.bin = "820";
////		ProductCredit productCredit = new ProductCredit();
////		Fee fee = new Fee();
////		productCredit.fee = fee;
////		productCredit.fee.firstCardFeeInd = null;
////		CcsCustomer cust = new CcsCustomer();
////		cust.setCustId(300L);
////		CcsAcct acct = new CcsAcct();
////    	acct.setAcctNbr(82382823L);
//    	
//    	TxnContext context = initContext();
//    	tnrSunshineLoan.mergeCard(context);
//    	
//    }
//	
//	@Test
//    public void testMergeAcct() throws ParseException {
//    	Long acctNo =82382823L;
//		TxnContext context = initContext();
//    	tnrSunshineLoan.mergeAcct(acctNo, context);
//    	
//    }
//	 /**
//     * @see 方法名：mergeAcctNbr
//     * @see 描述：创建(获取)账号
//     */
//	@Test
//	public void testMergeAcctNbr() throws ParseException {
//		TxnContext context = initContext();
//		Long acctNo = null;
//		acctNo = tnrSunshineLoan.mergeAcctNbr(context);
//		System.out.println(acctNo);
//	}
//	
//	@Test
//	public void testMergeAcctAndO() throws ParseException {
//		TxnContext context = initContext();
//		tnrSunshineLoan.mergeAcctAndO(context);
//	}
//	
//	@Test
//	public void testBizProc() throws ParseException {
//		TxnContext context = initContext();
//		String payJson = "";
//		tnrSunshineLoan.bizProc(context, payJson);
//	}
//	
//	@Test
//	public void testHandler() throws ParseException {
//		TxnContext context = initContext();
//		String payJson = "";
//		tnrSunshineLoan.handler(this.getReq());
//	}
//	
//	/**
//	 * 初始化交易中间信息
//	 * @param req
//	 * @param txnInfo
//	 */
//	private void initTxnInfo(S30001LoanReq req, TxnInfo txnInfo) {
//		txnInfo.setBizDate(req.getBizDate());
//		txnInfo.setTransDirection(AuthTransDirection.Normal);
//		txnInfo.setTransType(AuthTransType.AgentCredit);
//		txnInfo.setGuarantyid(req.getGuarantyid());
//		txnInfo.setSysOlTime(new Date());
//		txnInfo.setChannelId(req.getChannelId());
//		txnInfo.setRequestTime(req.getRequestTime());
//		txnInfo.setServiceSn(req.getServiceSn());
//		txnInfo.setMti("0208");
//		txnInfo.setProcCode("480001");
//		txnInfo.setInputSource(InputSource.THIR);
//		txnInfo.setProductCd(req.productcd);
//		txnInfo.setLoanAction(LoanAction.A);
//		txnInfo.setLoanUsage(LoanUsage.L);
//		txnInfo.setTransAmt(req.businesssum);
//		txnInfo.setOrg(OrganizationContextHolder.getCurrentOrg());
//	}
//	/**
//	 * 初始化交易中间信息
//	 * @param req
//	 * @param txnInfo
//	 */
//	private TxnContext initContext() {
//		S30001LoanReq req = this.getReq();
//		TxnContext context = new TxnContext();
//    	Product product = new Product();
//    	product = unifiedParameterFacilityMock.loadProduct();
//		context.setProduct(product);
//		
//		ProductCredit productCredit = new ProductCredit();
//		Fee fee = new Fee();
//		productCredit.fee = fee;
//		productCredit.fee.firstCardFeeInd = null;
//		context.setProductCredit(productCredit);
//		
//		CcsCustomer cust = new CcsCustomer();
//		cust.setCustId(300L);
//		cust.setCustLmtId(50L);
//		CcsAcct acct = new CcsAcct();
//    	acct.setAcctNbr(82382823L);
//    	context.setAccount(acct);
//    	
//    	Long acctNo =82382823L;
//    	AccountAttribute acctAttr = new AccountAttribute();
//    	acctAttr = unifiedParameterFacilityMock.loadAcctAttr();
//    	context.setAccAttr(acctAttr);
//    	
//    	LoanPlan loanPlan = unifiedParameterFacilityMock.loanPlan();
//    	context.setLoanPlan(loanPlan);
//    	
//    	LoanFeeDef loanFeeDef = unifiedParameterFacilityMock.loanFeeDef();
//    	context.setLoanFeeDef(loanFeeDef);
//    	
//		TxnInfo txnInfo = new TxnInfo();
//		String payJson = "";
//		String retJson = "";
//		initTxnInfo(req, txnInfo);
//		context.setTxnInfo(txnInfo);
//		txnInfo.setTransAmt(BigDecimal.valueOf(100000));
//		context.setSunshineRequestInfo(req);
//		context.setCustomer(cust);
//		
//		return context;
//	}
//
//
//	private S30001LoanReq getReq() {
//		S30001LoanReq req = new S30001LoanReq();
//    	req.dataid="10010101010";
////    	req.org = OrganizationContextHolder.getCurrentOrg();
//    	req.productcd = "480001";
////    	req.card_no = "982322311";
//    	req.putpaycardid = "982322311";
//    	req.customername = "lzz";
//    	req.sex = Gender.F;
//    	req.workcorp = "马上";
//    	req.mobile = "18374834387";
//    	req.productcd = "480001";
//    	req.bankname = "农行";
// 	    req.bankcode = "010328";
// 	    req.bankcardowner = "lzz";
// 	    req.contractno = "000348291";
// 	    req.guarantyid = "000000001";
// 	    req.setBizDate(new Date());
// 	    req.setChannelId("13");
// 	    req.setRequestTime("201508251010");
// 	    req.setServiceSn("YG20150825205750000111");
// 	    req.businesssum = BigDecimal.valueOf(100000);
// 	    req.certtype = IdType.I;
// 	    req.certid = "8237493279827472";
// 	    req.monthlywages = BigDecimal.valueOf(10000);
// 	    req.loanterm = "12";
// 	    req.confirmvaluemonthrate = BigDecimal.valueOf(0.1);
// 	   DateFormat format = new SimpleDateFormat("yyyy-MM-dd");  
//		try {
//			req.birthday = format.parse("1980-12-31");
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return req;
//	}
//	
//
//}
