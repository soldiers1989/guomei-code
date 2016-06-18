//package com.sunline.ccs.ui.client.settings;
//
//import com.google.inject.Inject;
//import com.sunline.ccs.ui.client.images.CPSImages;
//import com.sunline.ccs.ui.client.txn.t1201.T1201Page;
//import com.sunline.ccs.ui.client.txn.t1306.T1306Page;
//import com.sunline.ccs.ui.client.txn.t1310.T1310Page;
//import com.sunline.ccs.ui.client.txn.t1311.T1311Page;
//import com.sunline.ccs.ui.client.txn.t1312.T1312Page;
//import com.sunline.ccs.ui.client.txn.t1502.T1502Page;
//import com.sunline.ccs.ui.client.txn.t1602.T1602Page;
//import com.sunline.ccs.ui.client.txn.t3001.T3001Page;
//import com.sunline.ccs.ui.client.txn.t3002.T3002Page;
//import com.sunline.ccs.ui.client.txn.t3003.T3003Page;
//import com.sunline.ccs.ui.client.txn.t3201.T3201Page;
//import com.sunline.ccs.ui.client.txn.t3204.T3204Page;
//import com.sunline.ccs.ui.client.txn.t3205.T3205Page;
//import com.sunline.ccs.ui.client.txn.t3301.T3301Page;
//import com.sunline.ccs.ui.client.txn.t3302.T3302Page;
//import com.sunline.ccs.ui.client.txn.t3303.T3303Page;
//import com.sunline.ccs.ui.client.txn.t3305.T3305Page;
//import com.sunline.ccs.ui.client.txn.t3306.T3306Page;
//import com.sunline.ccs.ui.client.txn.t3307.T3307Page;
//import com.sunline.ccs.ui.client.txn.t3308.T3308Page;
//import com.sunline.ccs.ui.client.txn.t3309.T3309Page;
//import com.sunline.ccs.ui.client.txn.t3310.T3310Page;
//import com.sunline.ccs.ui.client.txn.t3311.T3311Page;
//import com.sunline.ccs.ui.client.txn.t3402.T3402Page;
//import com.sunline.ccs.ui.client.txn.t3403.T3403Page;
//import com.sunline.ccs.ui.client.txn.t3404.T3404Page;
//import com.sunline.ccs.ui.client.txn.t3500.T3500Page;
//import com.sunline.ccs.ui.client.txn.t9001.T9001Page;
//import com.sunline.ccs.ui.client.txn.t9002.T9002Page;
//import com.sunline.pcm.ui.common.client.DispatcherPage;
//import com.sunline.pcm.ui.common.client.DispatcherPlace;
//import com.sunline.pcm.ui.common.client.DispatcherView;
//
//public class ManagementView extends DispatcherView<ManagementPlace>
//{
//	@Inject
//	private ManagementConstants constants;
//	
//	@Inject
//	private T1602Page t1602Page;
//	
//	@Inject
//	private T3201Page t3201Page; //账务调整
//	
//	@Inject
//	private T3204Page t3204Page;
//	
//	@Inject
//	private T3205Page  t3205Page; //卡片信息查询
//	
////	@Inject
////	private T1205Page t1205Page;
//	
//	@Inject
//	private T3001Page t3001Page;
//	
//	@Inject
//	private T3002Page t3002Page; //账户信息管理
//	
//	@Inject
//	private T3003Page t3003Page; //贷款信息查询
//
//	@Inject
//	private T3301Page t3301Page;
//	
//	@Inject
//	private T3302Page t3302Page;
//	
//	@Inject
//	private T3303Page t3303Page;
//	
//	@Inject
//	private T3305Page t3305Page;
//	
//	@Inject
//	private T3306Page t3306Page; //贷款审核
//	
//	@Inject
//	private T3307Page t3307Page; //授信到期审核
//	
//	@Inject
//	private T3308Page t3308Page; //计提申请
//	
//	@Inject
//	private T3309Page t3309Page; //呆账核销
//	
//	@Inject
//	private T3310Page t3310Page; //现金分期款项发放
//	
//	@Inject
//	private T3311Page t3311Page; //分期申请状态查询
//	
//    @Inject
//    private T3404Page t3404;//锁定码维护
//	
//	@Inject
//	private T3402Page t3402;//授权未入账交易查询
//	@Inject
//	private T3403Page t3403;//挂账交易查询
//	@Inject
//	private T1310Page t1310;//分期交易中止当日撤销
//    @Inject
//    private T1311Page t1311;//当天授权查询类流水查询
//    @Inject
//    private T1312Page t1312;//授权交易历史查询
//    @Inject
//	private T9001Page t9001;//操作日志记录查询
//    @Inject
//    private T9002Page t9002;//所有操作日志记录查询
//    @Inject
//    private T1201Page t1201;//历史账单查询
//    @Inject
//    private T1306Page t1306;//未出账单交易查询
//
//    @Inject
//    private T1502Page t1502Page;//可用额度查询查询
//    
//    @Inject
//    private T3500Page t3500Page;//特定pos分期申请
//    @Inject CPSImages images;
//	
//	@Override
//	protected DispatcherPlace createPlace(DispatcherPage page) {
//		return new ManagementPlace(page);
//	}
//
//	@Override
//	protected void setup() {
//		//账务处理
//		addGroup("acct", constants.accounting(), images.safe());
//		addPage(t1602Page);
//		addPage(t3201Page);
//		addPage(t3204Page);
//		addPage(t3205Page);
////		addPage(t1205Page);
//		addPage(t3001Page);
//		addPage(t3002Page);
//		addPage(t3003Page); //贷款信息查询
//		addPage(t1502Page);
//        //风险控制
//		addGroup("risk", constants.riskControl(), images.bell());
//		addPage(t3301Page);//个人账户冻结
//		addPage(t3302Page);
//		addPage(t3303Page);
//		addPage(t3305Page);
//		addPage(t3306Page); //贷款审核
//		addPage(t3307Page); //授信到期审核
//		addPage(t3308Page); //计提申请
//		addPage(t3309Page); //呆账核销
//		addPage(t3310Page);
//		addPage(t3311Page);
//		
//		//交易管理组
//		addGroup("tran", constants.transManagement(), images.inbox());
//		addPage(t3500Page);
//		addPage(t3402); //授权未入账交易查询
//		addPage(t1310); //分期交易中止当日撤销
//		addPage(t3403); //挂账交易维护
//		addPage(t1311); //当天授权查询类流水查询
//		addPage(t1312);//授权交易历史查询
//		addPage(t1201); //历史账单查询
//		addPage(t1306); //未出账单交易查询
//		addPage(t3404);//锁定码维护
//		//日志管理组
//		addGroup("log", constants.logManagement(), images.nauticalVessel());
//		addPage(t9001); //操作日志记录查询
//		addPage(t9002);//所有操作日志记录查询
//		
////		addPage(statusPage);
////		
////		addPage(processOperationPage);
//	}
//}
