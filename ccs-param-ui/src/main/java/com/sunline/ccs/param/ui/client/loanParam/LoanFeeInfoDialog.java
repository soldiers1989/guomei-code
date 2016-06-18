package com.sunline.ccs.param.ui.client.loanParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.sunline.ccs.infrastructure.client.ui.UEarlyRepayDef;
import com.sunline.ccs.infrastructure.client.ui.ULoanFeeDef;
import com.sunline.ccs.infrastructure.client.ui.UReplaceEarlyRepayDef;
import com.sunline.ccs.infrastructure.client.ui.i18n.LoanFeeDefConstants;
import com.sunline.ccs.param.def.enums.CalcMethod;
import com.sunline.ccs.param.def.enums.LoanFeeDefStatus;
import com.sunline.ccs.param.def.enums.PrepaymentFeeMethod;
import com.sunline.kylin.web.ark.client.helper.BooleanColumnHelper;
import com.sunline.kylin.web.ark.client.helper.DecimalColumnHelper;
import com.sunline.kylin.web.ark.client.helper.EnumColumnHelper;
import com.sunline.kylin.web.ark.client.helper.EventEnum;
import com.sunline.kylin.web.ark.client.helper.SelectItem;
import com.sunline.kylin.web.ark.client.helper.SelectItem.SelectType;
import com.sunline.kylin.web.ark.client.helper.TextColumnHelper;
import com.sunline.kylin.web.ark.client.ui.ClientUtils;
import com.sunline.kylin.web.ark.client.ui.KylinDialog;
import com.sunline.kylin.web.ark.client.ui.KylinForm;
import com.sunline.kylin.web.ark.client.ui.KylinGrid;
import com.sunline.kylin.web.core.client.rpc.RPC;
import com.sunline.kylin.web.core.client.rpc.RpcCallback;
import com.sunline.pcm.infrastructure.client.ui.UMulct;
import com.sunline.ppy.dictionary.enums.LoanFeeMethod;
import com.sunline.ui.core.client.common.Editor;
import com.sunline.ui.core.client.data.Data;
import com.sunline.ui.core.client.data.ListData;
import com.sunline.ui.core.client.data.MapData;
import com.sunline.ui.core.client.enums.EditorType;
import com.sunline.ui.core.client.function.IFunction;
import com.sunline.ui.core.client.util.DataUtil;
import com.sunline.ui.dialog.client.Dialog;
import com.sunline.ui.event.client.IClickEventListener;
public class LoanFeeInfoDialog extends KylinDialog{
	
	@Inject
	private LoanParamConstants loanParamConstants;
	
	private Map<String, KylinForm> forms;
	
	private KylinForm loanParamForm;  //贷款基本信息
	
	private KylinForm mulctForm;    //罚金参数信息
	
	private KylinForm loanForm; //贷款手续费参数信息
	
	private KylinForm rescheduleForm; //展期信息
	
	private KylinForm prepaymentFeeForm; //提前还款信息
	
	private KylinForm systolicPhaseFeeForm; //缩期还款信息
	
//	private KylinForm lateFeeForm; //滞纳金信息
	
	private KylinForm insuranceForm; //保费信息
	
	private KylinForm lifeInsuFeeForm; //寿险计划包信息
	
	private KylinForm stampTaxForm; //印花税信息
	
	private KylinForm installmentForm;//分期手续费信息
	
	private KylinForm replaceFeeForm;//代收服务费
	
	private KylinForm prepayPkgForm;//提前还款计划包信息
	@Inject
	private ULoanFeeDef uLoanFeeDef;
	@Inject
	private UMulct uMulct;
	
	private EnumColumnHelper<CalcMethod> loanFeeCalcMethod;
	
	private BooleanColumnHelper rescheduleInd;
	
	private EnumColumnHelper<CalcMethod> rescheduleCalcMethod;
	
	private BooleanColumnHelper systolicPhaseInd;
	
	@Inject
	private LoanFeeDefConstants constants;
	
	private EnumColumnHelper<CalcMethod> systolicPhaseCalcMethod;
	
//	private EnumColumnHelper<Indicator> lateFeeCharge;
	
	private EnumColumnHelper<LoanFeeMethod> insuranceCollectMethod;
	private EnumColumnHelper<LoanFeeMethod> lifeInsuFeeMethod;
	private EnumColumnHelper<LoanFeeMethod> stampTaxMethod;
	private EnumColumnHelper<PrepaymentFeeMethod> prepayPkgCalMethod;
	private EnumColumnHelper<PrepaymentFeeMethod> insuranceCalcMethod;
	private EnumColumnHelper<PrepaymentFeeMethod> lifeInsuFeeCalcMethod;
	private EnumColumnHelper<PrepaymentFeeMethod> stampTaxCalcMethod;
	private EnumColumnHelper<PrepaymentFeeMethod> installmentFeeCalMethod;
	private EnumColumnHelper<CalcMethod> replaceFeeCalMethod;
	
//	private KylinGrid ratesGrid;
	private KylinGrid earlyRepayGrid;
	
	private KylinGrid replaceEarlyRepayGrid;// 代收提前还款手续费表
	
//	@Inject
//	private URateDef uRateDef;
	
	@Inject
	private UEarlyRepayDef uEarlyRepayDef;
	
	@Inject
	private UReplaceEarlyRepayDef uReplaceEarlyRepayDef;
	
	private KylinGrid loanFeeInfoGrid;
	
	private Map<String, MapData> loanFeeDefMap;
	
	private String key;
	
	private LoanFeeDefStatus initStatus  = LoanFeeDefStatus.I;
	
	/**
	 * 判断是否在添加模式
	 */
	private boolean add = false;
	
	public boolean isAdd() {
		return add;
	}

	public void setAdd(boolean add) {
		this.add = add;
	}
	
	//判断子产品编号是否重复
	private void validateLoanFeeDefId(){
		String id = loanParamForm.getFieldValue(uLoanFeeDef.LoanFeeDefId().getName());
		if(loanFeeDefMap.containsKey(id)){
			Dialog.alert("编号与其他子产品编号重复");
			loanParamForm.setFieldValue(uLoanFeeDef.LoanFeeDefId().getName(), "");          //清空值
		}
	}
	
	@Override
	protected Widget createContent() {
		setWidth(1200);
		setHeight(500);
		setShowMinimizeButton(true);
		setIsModal(true);
		setTitle(loanParamConstants.loanTermDetail());
		VerticalPanel layout = new VerticalPanel();
		forms = new HashMap<String, KylinForm>();
		
		loanParamForm = new KylinForm();
		{
			loanParamForm.setCol(3);
			loanParamForm.getSetting().labelWidth(140).labelAlign("right");
			loanParamForm.setField(
					uLoanFeeDef.LoanFeeDefId().required(true).setGroup("贷款参数").setGroupicon("skins/icons/communication.gif")
					.bindEvent("change", new IFunction(){
						
						@Override
						public void function(){
							validateLoanFeeDefId();                         //不管是不是新增都要判断子产品编号是否重复
						}
					}),
					uLoanFeeDef.LoanFeeDefStatus().asSelectItem(SelectType.KEY_LABLE).required(true),
					uLoanFeeDef.InitTerm().required(true),
					uLoanFeeDef.InterestRate().required(true),
					uLoanFeeDef.CompoundIntTableId().required(true),
					uLoanFeeDef.PenaltyIntTableId().required(true),
					uLoanFeeDef.PaymentIntervalUnit().required(true).asSelectItem(SelectType.KEY_LABLE),
					uLoanFeeDef.PaymentIntervalPeriod().required(true),
					uLoanFeeDef.InterestAdjMethod().required(true).asSelectItem(SelectType.KEY_LABLE),
					//最大最小允许分期
					uLoanFeeDef.MaxAmount().required(true),
					uLoanFeeDef.MinAmount().required(true)
					,new TextColumnHelper(uLoanFeeDef.RiskTableId().getName(),uLoanFeeDef.RiskTableId().getDisplay(),3).asSelectItem(),
					uLoanFeeDef.HesitationDays(),
					uLoanFeeDef.ReturnMaxDays(),        //免费退货最长天数   2015.12.12 chenpy
					uLoanFeeDef.PremiumReturnInd().asSelectItem(SelectType.LABLE),	//是否退还趸交费
					uLoanFeeDef.ReplacePenaltyRate(), //代收罚息费率
					uLoanFeeDef.WavieGraceIntInd()  //是否免收宽限期利息
					
		 );
		}
		forms.put("loanParamForm", loanParamForm);
		layout.add(loanParamForm);
		
		mulctForm = new KylinForm();
		{
			mulctForm.setCol(3);
			mulctForm.getSetting().labelWidth(140).labelAlign("right");
			mulctForm.setField(
					uLoanFeeDef.CpdToleLmt().required(true).setGroup("罚金参数").setGroupicon("skins/icons/communication.gif"),
					uLoanFeeDef.DpdToleLmt().required(true),
					uLoanFeeDef.MulctTableId().required(true).asSelectItem(),
					uLoanFeeDef.ReplaceMulctTableId().asSelectItem()  // 代收罚金表ID
					);
		}
		
		forms.put("mulctForm", mulctForm);
		layout.add(mulctForm);
		
		
		
		
		
		/*不再使用--lsy20151019
		lateFeeForm = new KylinForm();
		{
			lateFeeForm.setCol(3);
			lateFeeForm.getSetting().labelWidth(140).labelAlign("right");
			lateFeeCharge = uLoanFeeDef.LateFeeCharge().asSelectItem(SelectType.KEY_LABLE).bindEvent("change", new IFunction() {
				@Override
				public void function() {
					validateLateFeeCharge();
				}
			});
			lateFeeForm.setField(
					lateFeeCharge.required(true).setGroup("滞纳金参数").setGroupicon("skins/icons/communication.gif"),
					uLoanFeeDef.MinAgeCd(),
					uLoanFeeDef.Threshold(),
					uLoanFeeDef.MinCharge(),
					uLoanFeeDef.MaxCharge(),
					uLoanFeeDef.YearMaxCharge(),
					uLoanFeeDef.YearMaxCnt(),
					uLoanFeeDef.CalcBaseInd(),
					uLoanFeeDef.TierInd()
					);
			
		}
		forms.put("lateFeeForm", lateFeeForm);
		layout.add(lateFeeForm);
		
		ratesGrid = new KylinGrid();
		ratesGrid.getSetting().usePager(false);
		
		ratesGrid.getSetting().enabledEdit(true);
		
		ratesGrid.setColumns(uRateDef.Rate().setDisplay("滞纳金费用比率").setColumnWidth(150).setColunmEditor(new Editor().type(EditorType.TEXT)),
		                     uRateDef.RateBase().setDisplay("滞纳金费用固定附加").setColumnWidth(150).setColunmEditor(new Editor().type(EditorType.TEXT)),
		                     uRateDef.RateCeil().setColumnWidth(150).setColunmEditor(new Editor().type(EditorType.TEXT))
				);
		ratesGrid.setHeader(ClientUtils.createAddItem(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
            	ratesGrid.getUi().addEditRow();
            }
        }), ClientUtils.createDeleteItem(new IClickEventListener()
        {
            @Override
            public void onClick()
            {
            	ratesGrid.getUi().deleteSelectedRow();
            }
        }));
		layout.add(ratesGrid);
		*/
		
		loanForm = new KylinForm();
		{
			loanFeeCalcMethod = uLoanFeeDef.LoanFeeCalcMethod().asSelectItem(SelectType.KEY_LABLE).bindEvent("change", new IFunction() {
				@Override
				public void function() {
					validateLoanFeeCalc();
				}
			});
			loanForm.setCol(3);
			loanForm.getSetting().labelWidth(140).labelAlign("right");
			loanForm.setField(
					uLoanFeeDef.LoanFeeMethod().required(true).asSelectItem(SelectType.KEY_LABLE).setGroup("分期手续费参数").setGroupicon("skins/icons/communication.gif"),
						loanFeeCalcMethod.required(true),
						uLoanFeeDef.FeeAmount().required(true),
						uLoanFeeDef.FeeRate().required(true)
//						这个在1.2.2的生产的页面显示时被改为分期手续费，这里做个备注
//						uLoanFeeDef.LoanFeeMethod().required(true).asSelectItem(SelectType.KEY_LABLE).setDisplay("分期手续费收取方式").setGroup("分期手续费参数").setGroupicon("skins/icons/communication.gif"),
//						loanFeeCalcMethod.required(true).setDisplay("分期手续费计算方式"),
//						uLoanFeeDef.FeeAmount().required(true).setDisplay("分期手续费固定金额"),
//						uLoanFeeDef.FeeRate().required(true).setDisplay("分期手续费收取比例")
					);
		}
		forms.put("loanForm", loanForm);
		layout.add(loanForm);
		
		rescheduleForm = new KylinForm();
		{
			//如果“允许展期”勾选了，则“展期手续费计算方式”、“展期手续费收取方式”必须填写
			rescheduleInd = uLoanFeeDef.RescheduleInd().asCheckBoxItem().bindEvent("change", new IFunction() {
				@Override
				public void function() {
					validateRescheDule();
				}
			});
			//如果“展期手续费计算方式”为R则展期手续费比例必须填写，如果为A则展期手续费金额必须填写
			rescheduleCalcMethod = uLoanFeeDef.RescheduleCalcMethod().asSelectItem(SelectType.KEY_LABLE).bindEvent("change", new IFunction() {
				@Override
				public void function() {
					validateRescheDuleCalcMethod();
				}
			});
			
			rescheduleForm.setCol(3);
			rescheduleForm.getSetting().labelWidth(140).labelAlign("right");
			rescheduleForm.setField(rescheduleInd.setGroup("是否展期").setGroupicon("skins/icons/communication.gif"),
					uLoanFeeDef.RescheduleFeeMethod(),
					rescheduleCalcMethod.setNewline(true),
					uLoanFeeDef.RescheduleFeeAmount(),
					uLoanFeeDef.RescheduleFeeRate(),
					uLoanFeeDef.RescheduleMinAmount(),
					uLoanFeeDef.RescheduleMaxAmount()
					);
			
		}
		
		forms.put("rescheduleForm", rescheduleForm);
		layout.add(rescheduleForm);
		
		//提前还款表单
		prepaymentFeeForm = new KylinForm();
		{
			prepaymentFeeForm.setCol(3);
			prepaymentFeeForm.getSetting().labelWidth(140).labelAlign("right");
			prepaymentFeeForm.setField(
					uLoanFeeDef.PrepaymentFeeMethod().asSelectItem(SelectType.KEY_LABLE).required(true).asSelectItem(SelectType.LABLE).required(true).setGroup("提前还款参数").setGroupicon("skins/icons/communication.gif"),
					uLoanFeeDef.AppointEarlySettleEnable().asSelectItem(SelectType.LABLE),
					uLoanFeeDef.AppointEarlySettleDate().required(true),
					uLoanFeeDef.PrepayApplyCutDay().required(true),            //新增提前还款申请扣款提前日
					uLoanFeeDef.PrepaymentCalMethod().asSelectItem(SelectType.LABLE).required(true),        //新增提前还款计算方式
					uLoanFeeDef.ReplacePrepaymentFeeMethod().asSelectItem(SelectType.LABLE)   //代收提前还款手续费收取 
				);
			
		}
		forms.put("prepaymentFeeForm", prepaymentFeeForm);
		layout.add(prepaymentFeeForm);
		earlyRepayGrid = new KylinGrid();
		earlyRepayGrid.getSetting().usePager(false);
		
		earlyRepayGrid.getSetting().enabledEdit(true);
		
		earlyRepayGrid.setColumns(uEarlyRepayDef.AdCurPeriod().setColumnWidth(200).setColunmEditor(new Editor().type(EditorType.TEXT)),
		                          uEarlyRepayDef.AdFeeAmt().setColumnWidth(150).setColunmEditor(new Editor().type(EditorType.TEXT)),
		                          uEarlyRepayDef.AdFeeScale().setColumnWidth(150).setColunmEditor(new Editor().type(EditorType.TEXT))
			);
		earlyRepayGrid.setHeader(ClientUtils.createAddItem(new IClickEventListener()
		{
		    @Override
		    public void onClick()
		    {
			earlyRepayGrid.getUi().addEditRow();
		    }
		}), ClientUtils.createDeleteItem(new IClickEventListener()
		{
		    @Override
		    public void onClick()
		    {
			earlyRepayGrid.getUi().deleteSelectedRow();
		    }
		}));
		layout.add(earlyRepayGrid);
		
		// 代收提前还款手续费表
		replaceEarlyRepayGrid = new KylinGrid();
		replaceEarlyRepayGrid.getSetting().usePager(false);
		
		replaceEarlyRepayGrid.getSetting().enabledEdit(true);
		
		replaceEarlyRepayGrid.setColumns(uReplaceEarlyRepayDef.ReplaceAdCurPeriod().setColumnWidth(200).setColunmEditor(new Editor().type(EditorType.TEXT)),
		                          uReplaceEarlyRepayDef.ReplaceAdFeeAmt().setColumnWidth(150).setColunmEditor(new Editor().type(EditorType.TEXT)),
		                         uReplaceEarlyRepayDef.ReplaceAdFeeScale().setColumnWidth(150).setColunmEditor(new Editor().type(EditorType.TEXT))
			);
		replaceEarlyRepayGrid.setHeader(ClientUtils.createAddItem(new IClickEventListener()
		{
		    @Override
		    public void onClick()
		    {
		    	replaceEarlyRepayGrid.getUi().addEditRow();
		    }
		}), ClientUtils.createDeleteItem(new IClickEventListener()
		{
		    @Override
		    public void onClick()
		    {
		    	replaceEarlyRepayGrid.getUi().deleteSelectedRow();
		    }
		}));
		layout.add(replaceEarlyRepayGrid);

		prepayPkgForm = new KylinForm();
		{
			prepayPkgForm.setCol(3);
			prepayPkgForm.getSetting().labelWidth(140).labelAlign("right");
			prepayPkgCalMethod=uLoanFeeDef.PrepayPkgFeeCalMethod().asSelectItem(SelectType.KEY_LABLE).bindEvent("change", new IFunction() {
				@Override
				public void function() {
					validatePrepayPkgFeeCalMethod();
				}
			});
			prepayPkgForm.setField(
					uLoanFeeDef.PrepayPkgFeeMethod().asSelectItem(SelectType.KEY_LABLE).required(true).setGroup("灵活还款计划包收取方式").setGroupicon("skins/icons/communication.gif"),
					prepayPkgCalMethod.required(true),
                    uLoanFeeDef.PrepayPkgFeeAmount(),
                    uLoanFeeDef.PrepayPkgFeeAmountRate(),
                    uLoanFeeDef.DelayApplyAdvDays().setNewline(true),   //延期还款申请距离还款日提前天数
                    uLoanFeeDef.DelayApplyMax(),						//延期还款申请最大次数
                    uLoanFeeDef.DelayMaxTerm(),							//延期还款每次延期最大期数
                    uLoanFeeDef.DelayAccuMaxTerm(),						//延期还款累计延期最大期数
                    uLoanFeeDef.DelayFristApplyTerm(),					//延期还款首次申请足额还款期数
                    uLoanFeeDef.DelayApplyAgainTerm(),					//延期还款再次申请距离上次足额偿还期数
                    uLoanFeeDef.PayDateExpireAdvDays().setNewline(true),	//变更还款日次月生效申请提前天数
                    uLoanFeeDef.PayDateFirstApplyTerm(),				//变更还款日首次申请足额还款期数
                    uLoanFeeDef.PayDateApplyAgainTerm(),				//变更还款日再次申请距离上次足额偿还期数
                    uLoanFeeDef.PayDateAccuMax(),						//变更还款日累计变更最大次数
                    uLoanFeeDef.DisPrepaymentApplyTerm().setNewline(true)		//变更还款日累计变更最大次数
                    
			);
			
		}
		forms.put("prepayPkgForm", prepayPkgForm);
		layout.add(prepayPkgForm);
		
		systolicPhaseFeeForm = new KylinForm();
		{
			systolicPhaseFeeForm.setCol(3);
			systolicPhaseFeeForm.getSetting().labelWidth(140).labelAlign("right");
			DecimalColumnHelper shortedMinPmtDueHelper = new DecimalColumnHelper("shortedMinPmtDue", constants.shortedMinPmtDue(),  16, BigDecimal.valueOf(0.01), BigDecimal.valueOf(9999999999999.99), 2);
			
			//如果“允许展期”勾选了，则“展期手续费计算方式”、“展期手续费收取方式”必须填写
			systolicPhaseInd = uLoanFeeDef.ShortedRescInd().asCheckBoxItem().bindEvent("change", new IFunction() {
				@Override
				public void function() {
					validateSystolicPhaseInd();
				}
			});
			//如果“展期手续费计算方式”为R则展期手续费比例必须填写，如果为A则展期手续费金额必须填写
			systolicPhaseCalcMethod = uLoanFeeDef.ShortedRescCalcMethod().asSelectItem(SelectType.KEY_LABLE).bindEvent("change", new IFunction() {
				@Override
				public void function() {
					validateSystolicPhaseCalcMethod();
				}
			});
			
			systolicPhaseFeeForm.setField(
					systolicPhaseInd.setGroup("是否缩期").setGroupicon("skins/icons/communication.gif"),
					systolicPhaseCalcMethod.setNewline(true),
					uLoanFeeDef.ShortedRescFeeAmount(),
					uLoanFeeDef.ShortedRescFeeAmountRate(),
					shortedMinPmtDueHelper,
					uLoanFeeDef.ShorteRescdMinAmount()
					
					);
		}
		forms.put("systolicPhaseFeeForm", systolicPhaseFeeForm);
		layout.add(systolicPhaseFeeForm);
		
		insuranceForm = new KylinForm();
		{
		    insuranceForm.setCol(3);
		    insuranceForm.getSetting().labelWidth(140).labelAlign("right");
		    insuranceCollectMethod = uLoanFeeDef.InsCollMethod().asSelectItem(SelectType.KEY_LABLE);
		    insuranceCalcMethod = uLoanFeeDef.InsCalcMethod().asSelectItem(SelectType.KEY_LABLE).bindEvent(EventEnum.onchange,new IFunction(){
				@Override
				public void function() {
					if(!"".equals(insuranceForm.getFieldValue(uLoanFeeDef.InsCalcMethod().getName()))&&insuranceForm.getFieldValue(uLoanFeeDef.InsCalcMethod().getName())!=null){
						if("R".equals(insuranceForm.getFieldValue(uLoanFeeDef.InsCalcMethod().getName()))){
							insuranceForm.setFieldValue(uLoanFeeDef.InsAmt().getName(),"");
							insuranceForm.setFieldReadOnly(uLoanFeeDef.InsRate().getName(), true);
							insuranceForm.setFieldReadOnly(uLoanFeeDef.InsAmt().getName(), false);
							insuranceForm.setFieldRequired(uLoanFeeDef.InsRate().getName(), true);
							insuranceForm.setFieldRequired(uLoanFeeDef.InsAmt().getName(), false);
						}else{
							insuranceForm.setFieldValue(uLoanFeeDef.InsRate().getName(),"");
							insuranceForm.setFieldReadOnly(uLoanFeeDef.InsRate().getName(), false);
							insuranceForm.setFieldReadOnly(uLoanFeeDef.InsAmt().getName(), true);
							insuranceForm.setFieldRequired(uLoanFeeDef.InsRate().getName(), false);
							insuranceForm.setFieldRequired(uLoanFeeDef.InsAmt().getName(), true);
						}
						
					}
				}
		    	
		    });
		    insuranceForm.setField(
		                         insuranceCollectMethod.required(true).setGroup("保费参数").setGroupicon("skins/icons/communication.gif"),
		                         insuranceCalcMethod.required(true),
		                         uLoanFeeDef.InsRate(),
		                         uLoanFeeDef.InsAmt()
			    );
		    
		}
		forms.put("insuranceForm", insuranceForm);
		layout.add(insuranceForm);
		
		lifeInsuFeeForm = new KylinForm();
		{
			lifeInsuFeeForm.setCol(3);
			lifeInsuFeeForm.getSetting().labelWidth(140).labelAlign("right");
			lifeInsuFeeMethod = uLoanFeeDef.LifeInsuFeeMethod().asSelectItem(SelectType.KEY_LABLE);
			lifeInsuFeeCalcMethod = uLoanFeeDef.LifeInsuFeeCalMethod().asSelectItem(SelectType.KEY_LABLE).bindEvent(EventEnum.onchange,new IFunction(){

				@Override
				public void function() {
					if(!"".equals(lifeInsuFeeForm.getFieldValue(uLoanFeeDef.LifeInsuFeeCalMethod().getName()))&&lifeInsuFeeForm.getFieldValue(uLoanFeeDef.LifeInsuFeeCalMethod().getName())!=null){
						if("R".equals(lifeInsuFeeForm.getFieldValue(uLoanFeeDef.LifeInsuFeeCalMethod().getName()))){
							lifeInsuFeeForm.setFieldValue(uLoanFeeDef.LifeInsuFeeAmt().getName(),"");
							lifeInsuFeeForm.setFieldReadOnly(uLoanFeeDef.LifeInsuFeeRate().getName(), true);
							lifeInsuFeeForm.setFieldReadOnly(uLoanFeeDef.LifeInsuFeeAmt().getName(), false);
							lifeInsuFeeForm.setFieldRequired(uLoanFeeDef.LifeInsuFeeRate().getName(), true);
							lifeInsuFeeForm.setFieldRequired(uLoanFeeDef.LifeInsuFeeAmt().getName(), false);
						}else{
							lifeInsuFeeForm.setFieldValue(uLoanFeeDef.LifeInsuFeeRate().getName(),"");
							lifeInsuFeeForm.setFieldReadOnly(uLoanFeeDef.LifeInsuFeeRate().getName(), false);
							lifeInsuFeeForm.setFieldReadOnly(uLoanFeeDef.LifeInsuFeeAmt().getName(), true);
							lifeInsuFeeForm.setFieldRequired(uLoanFeeDef.LifeInsuFeeRate().getName(), false);
							lifeInsuFeeForm.setFieldRequired(uLoanFeeDef.LifeInsuFeeAmt().getName(), true);
						}
						
					}
				}
		    	
		    });
			lifeInsuFeeForm.setField(
		    					lifeInsuFeeMethod.required(true).setGroup("寿险计划包参数").setGroupicon("skins/icons/communication.gif"),
		    					lifeInsuFeeCalcMethod.required(true),
		                         uLoanFeeDef.LifeInsuFeeRate(),
		                         uLoanFeeDef.LifeInsuFeeAmt()
			    );
		    
		}
		forms.put("lifeInsuFeeForm", lifeInsuFeeForm);
		layout.add(lifeInsuFeeForm);
		
		stampTaxForm = new KylinForm();
		{
		    stampTaxForm.setCol(3);
		    stampTaxForm.getSetting().labelWidth(140).labelAlign("right");
		    stampTaxMethod = uLoanFeeDef.StampMethod().asSelectItem(SelectType.KEY_LABLE);
		    stampTaxCalcMethod = uLoanFeeDef.StampCalcMethod().asSelectItem(SelectType.KEY_LABLE).bindEvent(EventEnum.onchange,new IFunction(){

				@Override
				public void function() {
					if(!"".equals(stampTaxForm.getFieldValue(uLoanFeeDef.StampCalcMethod().getName()))&&stampTaxForm.getFieldValue(uLoanFeeDef.StampCalcMethod().getName())!=null){
						if("R".equals(stampTaxForm.getFieldValue(uLoanFeeDef.StampCalcMethod().getName()))){
							stampTaxForm.setFieldValue(uLoanFeeDef.StampAMT().getName(),"");
							stampTaxForm.setFieldReadOnly(uLoanFeeDef.StampRate().getName(), true);
							stampTaxForm.setFieldReadOnly(uLoanFeeDef.StampAMT().getName(), false);
							stampTaxForm.setFieldRequired(uLoanFeeDef.StampRate().getName(), true);
							stampTaxForm.setFieldRequired(uLoanFeeDef.StampAMT().getName(), false);
						}else{
							stampTaxForm.setFieldValue(uLoanFeeDef.StampRate().getName(),"");
							stampTaxForm.setFieldReadOnly(uLoanFeeDef.StampRate().getName(), false);
							stampTaxForm.setFieldReadOnly(uLoanFeeDef.StampAMT().getName(), true);
							stampTaxForm.setFieldRequired(uLoanFeeDef.StampRate().getName(), false);
							stampTaxForm.setFieldRequired(uLoanFeeDef.StampAMT().getName(), true);
						}
						
					}
				}
		    	
		    });
		    stampTaxForm.setField(
		                         stampTaxMethod.required(true).setGroup("印花税参数").setGroupicon("skins/icons/communication.gif"),
		                         stampTaxCalcMethod.required(true),
		                         uLoanFeeDef.StampRate(),
		                         uLoanFeeDef.StampAMT(),
		                         uLoanFeeDef.IsOffsetRate().asSelectItem(SelectType.LABLE).required(true),
		                         uLoanFeeDef.StampCustomInd().asSelectItem(SelectType.LABLE).required(true)
			    );
		    
		}
		forms.put("stampTaxForm", stampTaxForm);
		layout.add(stampTaxForm);
		
		installmentForm = new KylinForm();
		{
			installmentForm.setCol(3);
			installmentForm.getSetting().labelWidth(140).labelAlign("right");
			installmentFeeCalMethod = uLoanFeeDef.InstallmentFeeCalMethod().asSelectItem(SelectType.KEY_LABLE).bindEvent(EventEnum.onchange,new IFunction(){

				@Override
				public void function() {
					if(!"".equals(installmentForm.getFieldValue(uLoanFeeDef.InstallmentFeeCalMethod().getName()))&&installmentForm.getFieldValue(uLoanFeeDef.InstallmentFeeCalMethod().getName())!=null){
						if("R".equals(installmentForm.getFieldValue(uLoanFeeDef.InstallmentFeeCalMethod().getName()))){
							installmentForm.setFieldValue(uLoanFeeDef.InstallmentFeeAmt().getName(),"");
							installmentForm.setFieldReadOnly(uLoanFeeDef.InstallmentFeeRate().getName(), true);
							installmentForm.setFieldReadOnly(uLoanFeeDef.InstallmentFeeAmt().getName(), false);
							installmentForm.setFieldRequired(uLoanFeeDef.InstallmentFeeRate().getName(), true);
							installmentForm.setFieldRequired(uLoanFeeDef.InstallmentFeeAmt().getName(), false);
						}else{
							installmentForm.setFieldValue(uLoanFeeDef.InstallmentFeeRate().getName(),"");
							installmentForm.setFieldReadOnly(uLoanFeeDef.InstallmentFeeRate().getName(), false);
							installmentForm.setFieldReadOnly(uLoanFeeDef.InstallmentFeeAmt().getName(), true);
							installmentForm.setFieldRequired(uLoanFeeDef.InstallmentFeeRate().getName(), false);
							installmentForm.setFieldRequired(uLoanFeeDef.InstallmentFeeAmt().getName(), true);
						}
						
					}
				}
		    	
		    });
			installmentForm.setField(
							uLoanFeeDef.InstallmentFeeMethod().required(true).setGroup("贷款服务费参数").setGroupicon("skins/icons/communication.gif"),
							installmentFeeCalMethod.required(true),
							uLoanFeeDef.InstallmentFeeAmt(),
		                    uLoanFeeDef.InstallmentFeeRate()
//							这个在1.2.2的生产的页面显示时被改为贷款服务费，这里做个备注
//		                    uLoanFeeDef.InstallmentFeeMethod().required(true).setDisplay("贷款服务费收取方式").setGroup("贷款服务费参数").setGroupicon("skins/icons/communication.gif"),
//		                    installmentFeeCalMethod.required(true).setDisplay("贷款服务费计算方式"),
//		                    uLoanFeeDef.InstallmentFeeAmt().setDisplay("贷款服务费固定金额"),
//		                    uLoanFeeDef.InstallmentFeeRate().setDisplay("贷款服务费收取比例")
			    );
		    
		}
		forms.put("installmentForm", installmentForm);
		layout.add(installmentForm);
		
		replaceFeeForm = new KylinForm();           //增加代收服务费 2015.12.12 chenpy
		{
			replaceFeeForm.setCol(3);
			replaceFeeForm.getSetting().labelWidth(140).labelAlign("right");
			replaceFeeCalMethod = uLoanFeeDef.ReplaceFeeCalMethod().asSelectItem(SelectType.KEY_LABLE).bindEvent(EventEnum.onchange, new IFunction() {
				@Override
				public void function() {
					if(!"".equals(replaceFeeForm.getFieldValue(uLoanFeeDef.ReplaceFeeCalMethod().getName())) && replaceFeeForm.getFieldValue(uLoanFeeDef.ReplaceFeeCalMethod().getName()) != null){
						if("R".equals(replaceFeeForm.getFieldValue(uLoanFeeDef.ReplaceFeeCalMethod().getName()))){      //如果是按比例计算
							replaceFeeForm.setFieldValue(uLoanFeeDef.ReplaceFeeAmt().getName(), "");
							replaceFeeForm.setFieldReadOnly(uLoanFeeDef.ReplaceFeeRate().getName(), true);
							replaceFeeForm.setFieldReadOnly(uLoanFeeDef.ReplaceFeeAmt().getName(), false);
							replaceFeeForm.setFieldRequired(uLoanFeeDef.ReplaceFeeRate().getName(), true);
							replaceFeeForm.setFieldRequired(uLoanFeeDef.ReplaceFeeAmt().getName(), false);
						}else{
							replaceFeeForm.setFieldValue(uLoanFeeDef.ReplaceFeeRate().getName(), "");        //按金额
							replaceFeeForm.setFieldReadOnly(uLoanFeeDef.ReplaceFeeRate().getName(), false);
							replaceFeeForm.setFieldReadOnly(uLoanFeeDef.ReplaceFeeAmt().getName(), true);
							replaceFeeForm.setFieldRequired(uLoanFeeDef.ReplaceFeeRate().getName(), false);
							replaceFeeForm.setFieldRequired(uLoanFeeDef.ReplaceFeeAmt().getName(), true);
						}
					}
				}
			});
			replaceFeeForm.setField(
					uLoanFeeDef.ReplaceFeeMethod().required(true).asSelectItem(SelectType.LABLE).setGroup("代收服务费").setGroupicon("skins/icons/communication.gif"),
					replaceFeeCalMethod.required(true),
					uLoanFeeDef.ReplaceFeeAmt(),
					uLoanFeeDef.ReplaceFeeRate()
					);
		}
		forms.put("replaceFeeForm", replaceFeeForm);
		layout.add(replaceFeeForm);
		
		addConfirmButton(new IClickEventListener() {
			@Override
			public void onClick() {
				earlyRepayGrid.getUi().endEdit();
				replaceEarlyRepayGrid.getUi().endEdit();
				//1. 获取所有表单的值，保存到LoanFeeDef对象里边
				//2. 并放到一个Map<int, LoanFeeDef>对象里边 key为LoanNum
				//3. 重新刷新列表数据（需要转格式（对象中的值拼装，再加上一个map的key）
				MapData loanParamFormData = null;
				MapData mulctFormData = null;
				MapData loanFormData = null;
				MapData rescheduleFormData = null;
				MapData prepaymentFeeFormData = null;
				MapData systolicPhaseFeeFormData = null;
//				MapData lateFeeFormData = null;
				MapData installmentFormData= null;
				MapData insuranceFormData= null;
				MapData stampTaxFormData= null;
				MapData lifeInsuFeeFormData=null;
				MapData prepayPkgFormData=null;
				MapData replaceFeeFormData = null;
				if(loanParamForm.valid()){
					loanParamFormData = loanParamForm.getSubmitData().asMapData();
					if(!loanFeeDefMap.isEmpty()){
						
						if(! isAdd() && ! initStatus.equals(LoanFeeDefStatus.I) && loanParamFormData.getString(uLoanFeeDef.LoanFeeDefStatus().getName()).equals("I")){
							Dialog.alert("子产品状态不能从其他状态重新改为新建");
							return ;
						}
						
						//判断最大最小金额
						BigDecimal minMount=new BigDecimal(loanParamForm.getFieldValue(uLoanFeeDef.MinAmount().getName()));
						BigDecimal maxMount=new BigDecimal(loanParamForm.getFieldValue(uLoanFeeDef.MaxAmount().getName()));
						//如果最小金额大于最大金额
						if(minMount.compareTo(maxMount)==1){
							Dialog.alert(uLoanFeeDef.MinAmount().getDisplay()+"不能大于"+uLoanFeeDef.MinAmount().getDisplay()+"!");
							return;
						}
						//判断区间金额交叉
						int currTerm=loanParamFormData.getInteger(uLoanFeeDef.InitTerm().getName());
						List<MapData> currTermMap=new ArrayList<MapData>();
						for(Entry<String, MapData> entry:loanFeeDefMap.entrySet()){
							LoanFeeDefStatus status = LoanFeeDefStatus.A;
							if(entry.getValue().getString(uLoanFeeDef.LoanFeeDefStatus().getName()) != null){
								 status = LoanFeeDefStatus.valueOf(entry.getValue().getString(uLoanFeeDef.LoanFeeDefStatus().getName()));
							}
							if(entry.getValue().getInteger(uLoanFeeDef.InitTerm().getName())==currTerm && ! status.equals(LoanFeeDefStatus.P)) currTermMap.add(entry.getValue());
						}
						for(int i=0;i<currTermMap.size();i++){
							//如果是编辑状态，则跳过与自己比较
							if(loanParamFormData.getInteger(uLoanFeeDef.LoanFeeDefId().getName())
									.compareTo(currTermMap.get(i).getInteger(uLoanFeeDef.LoanFeeDefId().getName()))==0) continue;
							if(compareRange(loanParamFormData.getBigDecimal(uLoanFeeDef.MinAmount().getName()),
									loanParamFormData.getBigDecimal(uLoanFeeDef.MaxAmount().getName()),
									currTermMap.get(i).getBigDecimal(uLoanFeeDef.MinAmount().getName()),
									currTermMap.get(i).getBigDecimal(uLoanFeeDef.MaxAmount().getName())
									)){
								Dialog.alert("同一期数分期允许金额区间不允许交叉，请检查!");
								return;
							}
						}
					}
						
				}
				if(mulctForm.valid()){
					mulctFormData = mulctForm.getSubmitData().asMapData();
				}
				if(loanForm.valid()){
					loanFormData = loanForm.getSubmitData().asMapData();
				}
				if(rescheduleForm.valid()){
					rescheduleFormData = rescheduleForm.getSubmitData().asMapData();
				}
				if(prepaymentFeeForm.valid()){
					
					Integer appointEarlySettleDate = Integer.parseInt(prepaymentFeeForm.getFieldValue(uLoanFeeDef.AppointEarlySettleDate().getName()));
					Integer prepayApplyCutDay = Integer.parseInt(prepaymentFeeForm.getFieldValue(uLoanFeeDef.PrepayApplyCutDay().getName()));
					if(appointEarlySettleDate.compareTo(prepayApplyCutDay) <= 0){
						Dialog.alert(uLoanFeeDef.AppointEarlySettleDate().getDisplay()+"应大于"+uLoanFeeDef.PrepayApplyCutDay().getDisplay());
							return ;
					}
					
					prepaymentFeeFormData = prepaymentFeeForm.getSubmitData().asMapData();
				}
				if(systolicPhaseFeeForm.valid()){
					systolicPhaseFeeFormData = systolicPhaseFeeForm.getSubmitData().asMapData();
				}
//				if(lateFeeForm.valid()){
//					lateFeeFormData = lateFeeForm.getSubmitData().asMapData();
//				}
				if(installmentForm.valid()){
					installmentFormData=installmentForm.getSubmitData().asMapData();
				}
				if(insuranceForm.valid()){
					insuranceFormData=insuranceForm.getSubmitData().asMapData();
				}
				if(stampTaxForm.valid()){
					stampTaxFormData=stampTaxForm.getSubmitData().asMapData();
				}
				if(lifeInsuFeeForm.valid()){
					lifeInsuFeeFormData=lifeInsuFeeForm.getSubmitData().asMapData();
				}
				if(prepayPkgForm.valid()){
					prepayPkgFormData=prepayPkgForm.getSubmitData().asMapData();
				}
				if(replaceFeeForm.valid()){
					replaceFeeFormData = replaceFeeForm.getSubmitData().asMapData();
				}
//				ListData rateListData = ratesGrid.getGrid().getData();
				ListData earlyListData = earlyRepayGrid.getGrid().getData();
				ListData replaceEarlyListData = replaceEarlyRepayGrid.getGrid().getData();
				
				MapData loanFeeDefData = new MapData();
				
				MapData.extend(loanFeeDefData, loanParamFormData, true);
				
				MapData.extend(loanFeeDefData, mulctFormData, true);
				
				MapData.extend(loanFeeDefData, loanFormData, true);
				
				MapData.extend(loanFeeDefData, rescheduleFormData, true);
				
				MapData.extend(loanFeeDefData, prepaymentFeeFormData, true);
				
				MapData.extend(loanFeeDefData, systolicPhaseFeeFormData, true);
				
//				MapData.extend(loanFeeDefData, lateFeeFormData, true);
				
				MapData.extend(loanFeeDefData, installmentFormData, true);

				MapData.extend(loanFeeDefData, insuranceFormData, true);
				
				MapData.extend(loanFeeDefData, stampTaxFormData, true);
				
				MapData.extend(loanFeeDefData, lifeInsuFeeFormData, true);

				MapData.extend(loanFeeDefData, prepayPkgFormData, true);
				
				MapData.extend(loanFeeDefData, replaceFeeFormData, true);

//				loanFeeDefData.put("chargeRates", rateListData);
				loanFeeDefData.put("earlyRepayDefs", earlyListData);
				loanFeeDefData.put("replaceEarlyRepayDef", replaceEarlyListData);
				
				if(isAdd()){	
					if(loanFeeDefMap.containsKey(loanParamForm.getFieldValue(uLoanFeeDef.LoanFeeDefId().getName()))){
						Dialog.alert("该贷款周期已定义");
						return;
					}else{
						loanFeeDefMap.put(loanParamForm.getFieldValue(uLoanFeeDef.LoanFeeDefId().getName()), loanFeeDefData);
					}
				}else{
					loanFeeDefMap.put(loanParamForm.getFieldValue(uLoanFeeDef.LoanFeeDefId().getName()), loanFeeDefData);
				}
				
				//重新拼装数据，刷新列表
				//列表双击事件在列表进行父页面进行定义
				//事件双击存储的LoanFeeDef信息存放在loanFeeDefMap，父页面通过getLoanFeeDef获取
				loanFeeInfoGrid.loadData(StringToData(getGridData(loanFeeDefMap)));
				clearValues();
				hide();
			}
		});
		addCancelButton(new IClickEventListener() {
			@Override
			public void onClick() {
				updateView();
				hide();
			}
		});
		
		return layout;
	}
	
	public String getGridData(Map<String, MapData> loanFeeDefMap){
		StringBuffer gridData = new StringBuffer("[");
		
		int appendIndex = 0;
		
		for (Entry<String, MapData> entry : loanFeeDefMap.entrySet()){
			String loanFeeDefId = entry.getKey();
			MapData loanFeeInfoMapData = entry.getValue();
			gridData.append("{");
			gridData.append("\"loanFeeDefId\":\"" + loanFeeDefId +"\",");
			gridData.append("\"" + uLoanFeeDef.LoanFeeCalcMethod().getName() + "\":\"" + loanFeeInfoMapData.getString(uLoanFeeDef.LoanFeeCalcMethod().getName())+"\",");
			gridData.append("\"" + uLoanFeeDef.FeeAmount().getName() + "\":\"" + loanFeeInfoMapData.getString(uLoanFeeDef.FeeAmount().getName())+"\",");
			gridData.append("\"" + uLoanFeeDef.InitTerm().getName() + "\":\"" + loanFeeInfoMapData.getString(uLoanFeeDef.InitTerm().getName())+"\",");
			gridData.append("\"" + uLoanFeeDef.MinAmount().getName() + "\":\"" + loanFeeInfoMapData.getString(uLoanFeeDef.MinAmount().getName())+"\",");
			gridData.append("\"" + uLoanFeeDef.MaxAmount().getName() + "\":\"" + loanFeeInfoMapData.getString(uLoanFeeDef.MaxAmount().getName())+"\",");
			gridData.append("\"" + uLoanFeeDef.FeeRate().getName() + "\":\"" + loanFeeInfoMapData.getString(uLoanFeeDef.FeeRate().getName())+"\"");
			gridData.append("}");
			appendIndex ++;
			if(appendIndex<=loanFeeDefMap.size()-1){
				gridData.append(",");
			}
		}
		gridData.append("]");
		return gridData.toString();
	}
	
	public Data StringToData(String gridDataString){
		Data data = new Data();
//		先用DataUtil的convertDataType方法
//		将你的String转成JavaScriptObject在放到Data中去
		data.setJsData(DataUtil.convertDataType(gridDataString));
		return data;
	}
	
	public void setLoanFeeDefMap(Map<String, MapData> loanFeeDefMap){
		this.loanFeeDefMap = loanFeeDefMap;
	}

	public void setLoanNum(String key) {
		this.key = key;
	}
	
	
	public void setGrid(KylinGrid loanFeeGrid){
		this.loanFeeInfoGrid = loanFeeGrid;
	}
	
	/**
	 * 清除各表表内数据
	 */
	private void clearValues() {
		for (Entry<String, KylinForm> entry : forms.entrySet()){
			KylinForm form = entry.getValue();
			form.getUi().clear();
		}
//		ratesGrid.loadData(StringToData("[]"));
		earlyRepayGrid.loadData(StringToData("[]"));
		replaceEarlyRepayGrid.loadData(StringToData("[]"));
	}

	@Override
	protected void updateView() {
		clearValues();
		loanParamForm.setFieldReadOnly(uLoanFeeDef.LoanFeeDefId().getName(), true);
		loanParamForm.setFieldReadOnly(uLoanFeeDef.InitTerm().getName(), true);
		if(!isAdd()){
			//如果是编辑，则需要进行赋值
			//周期为key的值
			Data loanFeeDef = loanFeeDefMap.get(key);
			loanParamForm.setFormData(loanFeeDef);
			mulctForm.setFormData(loanFeeDef);
			loanForm.setFormData(loanFeeDef);
			rescheduleForm.setFormData(loanFeeDef);
			prepaymentFeeForm.setFormData(loanFeeDef);
			systolicPhaseFeeForm.setFormData(loanFeeDef);
//			lateFeeForm.setFormData(loanFeeDef);
			installmentForm.setFormData(loanFeeDef);
			insuranceForm.setFormData(loanFeeDef);
			stampTaxForm.setFormData(loanFeeDef);
			lifeInsuFeeForm.setFormData(loanFeeDef);
			prepayPkgForm.setFormData(loanFeeDef);
			replaceFeeForm.setFormData(loanFeeDef);
			
//			ratesGrid.loadData(loanFeeDef.asMapData().getData("chargeRates"));
			
			String v = loanParamForm.getFieldValue(uLoanFeeDef.LoanFeeDefStatus().getName());            //现在有些子产品状态是空的  为防止错误  增加判断
			if(v != null){
				LoanFeeDefStatus s = LoanFeeDefStatus.valueOf(v);
				if(s != null && ! s.equals(LoanFeeDefStatus.I)){                //如果子产品状态不是新建
					loanParamForm.setFieldReadOnly(uLoanFeeDef.LoanFeeDefId().getName(), false);
					loanParamForm.setFieldReadOnly(uLoanFeeDef.InitTerm().getName(), false);
				}
				initStatus = s;
			}
			
			earlyRepayGrid.loadData(loanFeeDef.asMapData().getData("earlyRepayDefs"));
			if (loanFeeDef.asMapData().getData("replaceEarlyRepayDef") != null) {
				replaceEarlyRepayGrid.loadData(loanFeeDef.asMapData().getData("replaceEarlyRepayDef"));
			}
			
			
		}
		validateRescheDule();
		validateSystolicPhaseInd();
		setMulctTableId();
		RPC.ajax("rpc/loanPlanServer/getRiskId", new RpcCallback<Data>(){

			@Override
			public void onSuccess(Data result) {
				SelectItem<String> si=new SelectItem<String>();
				si.setValue(result.asListData());
				loanParamForm.setFieldSelectData(uLoanFeeDef.RiskTableId().getName(), si);
			}
			
		});
	}
	
	//获取罚金列表
	private void setMulctTableId() {
	    RPC.ajax("rpc/loanPlanServer/getFineList", new RpcCallback<Data>() {
		@Override
		public void onSuccess(Data data) {
			if(data != null){
			    LinkedHashMap<String, String> financeOrgMap = new LinkedHashMap<String, String>();
			    ListData fineList = data.asListData();
			    if(fineList != null && fineList.size()>0){
				for(int i =0;i<fineList.size();i++){
				    String fineTableId = fineList.get(i).asMapData().getString(uMulct.MulctTableId().getName());
				    String fineName = fineList.get(i).asMapData().getString(uMulct.MulctName().getName());
				    financeOrgMap.put(fineTableId,fineName);
				}
				mulctForm.setFieldSelectData(uMulct.MulctTableId().getName(), new SelectItem<String>().setValue(financeOrgMap));
				mulctForm.setFieldSelectData(uLoanFeeDef.ReplaceMulctTableId().getName(), new SelectItem<String>().setValue(financeOrgMap));
			    }
			}
		}
	
	});
	    
	}

	//无用
	protected void updateModel(MapData loanPlanData) {
		MapData loanFeeDefGridData = new MapData();
		for(Entry<String, MapData> entry : loanFeeDefMap.entrySet()){
			loanFeeDefGridData.put(entry.getKey(), entry.getValue());
		}
		
		loanPlanData.put("loanFeeDefMap", loanFeeDefGridData);
		
	}
	
	//贷款服务费表单-逻辑
	private void validateLoanFeeCalc(){
		String loanFeeCalcMethodValue = loanForm.getFieldValue(uLoanFeeDef.LoanFeeCalcMethod().getName());
		if(loanFeeCalcMethodValue != null){
			if("R".equals(loanFeeCalcMethodValue)){
				loanForm.setFieldRequired(uLoanFeeDef.FeeRate().getName(), true);
				loanForm.setFieldReadOnly(uLoanFeeDef.FeeRate().getName(), true);

				loanForm.setFieldValue(uLoanFeeDef.FeeAmount().getName(), "");
				loanForm.setFieldReadOnly(uLoanFeeDef.FeeAmount().getName(), false);
				loanForm.setFieldRequired(uLoanFeeDef.FeeAmount().getName(), false);
			}else if("A".equals(loanFeeCalcMethodValue)){
				loanForm.setFieldValue(uLoanFeeDef.FeeRate().getName(), "");
				loanForm.setFieldRequired(uLoanFeeDef.FeeRate().getName(), false);
				loanForm.setFieldReadOnly(uLoanFeeDef.FeeRate().getName(), false);
				loanForm.setFieldRequired(uLoanFeeDef.FeeAmount().getName(), true);
				loanForm.setFieldReadOnly(uLoanFeeDef.FeeAmount().getName(), true);
			}
		}
	}
	
	//
	private void validateRescheDuleCalcMethod() {
		String rescheduleCalcMethodValue = rescheduleForm.getFieldValue(uLoanFeeDef.RescheduleCalcMethod().getName());
		if(rescheduleCalcMethodValue != null){
			if("R".equals(rescheduleCalcMethodValue)){
				rescheduleForm.setFieldRequired(uLoanFeeDef.RescheduleFeeRate().getName(), true);
				rescheduleForm.setFieldReadOnly(uLoanFeeDef.RescheduleFeeRate().getName(), true);

				rescheduleForm.setFieldValue(uLoanFeeDef.RescheduleFeeAmount().getName(), "");
				rescheduleForm.setFieldReadOnly(uLoanFeeDef.RescheduleFeeAmount().getName(), false);
				rescheduleForm.setFieldRequired(uLoanFeeDef.RescheduleFeeAmount().getName(), false);
			}else if("A".equals(rescheduleCalcMethodValue)){
				rescheduleForm.setFieldValue(uLoanFeeDef.RescheduleFeeRate().getName(), "");
				rescheduleForm.setFieldRequired(uLoanFeeDef.RescheduleFeeRate().getName(), false);
				rescheduleForm.setFieldReadOnly(uLoanFeeDef.RescheduleFeeRate().getName(), false);
				rescheduleForm.setFieldRequired(uLoanFeeDef.RescheduleFeeAmount().getName(), true);
				rescheduleForm.setFieldReadOnly(uLoanFeeDef.RescheduleFeeAmount().getName(), true);
			}
		}
	}
	
	//提前还款表单-逻辑
	private void validatePrepayPkgFeeCalMethod() {
		String prepayPkgFeeCalMethodValue = prepayPkgForm.getFieldValue(uLoanFeeDef.PrepayPkgFeeCalMethod().getName());
		if(prepayPkgFeeCalMethodValue != null){
			if("R".equals(prepayPkgFeeCalMethodValue)){
				prepayPkgForm.setFieldRequired(uLoanFeeDef.PrepayPkgFeeAmountRate().getName(), true);
				prepayPkgForm.setFieldReadOnly(uLoanFeeDef.PrepayPkgFeeAmountRate().getName(), true);
				prepayPkgForm.setFieldValue(uLoanFeeDef.PrepayPkgFeeAmount().getName(), "");
				prepayPkgForm.setFieldReadOnly(uLoanFeeDef.PrepayPkgFeeAmount().getName(), false);
				prepayPkgForm.setFieldRequired(uLoanFeeDef.PrepayPkgFeeAmount().getName(), false);
			}else if("A".equals(prepayPkgFeeCalMethodValue)){
				prepayPkgForm.setFieldValue(uLoanFeeDef.PrepayPkgFeeAmountRate().getName(), "");
				prepayPkgForm.setFieldRequired(uLoanFeeDef.PrepayPkgFeeAmountRate().getName(), false);
				prepayPkgForm.setFieldReadOnly(uLoanFeeDef.PrepayPkgFeeAmountRate().getName(), false);
				prepayPkgForm.setFieldRequired(uLoanFeeDef.PrepayPkgFeeAmount().getName(), true);
				prepayPkgForm.setFieldReadOnly(uLoanFeeDef.PrepayPkgFeeAmount().getName(), true);
			}
		}
	}
	
	//缩期表单-逻辑
	private void validateSystolicPhaseCalcMethod() {
		String shortedRescCalcMethodValue = systolicPhaseFeeForm.getFieldValue(uLoanFeeDef.ShortedRescCalcMethod().getName());
		if(shortedRescCalcMethodValue != null){
			if("R".equals(shortedRescCalcMethodValue)){
				systolicPhaseFeeForm.setFieldRequired(uLoanFeeDef.ShortedRescFeeAmountRate().getName(), true);
				systolicPhaseFeeForm.setFieldReadOnly(uLoanFeeDef.ShortedRescFeeAmountRate().getName(), true);
				systolicPhaseFeeForm.setFieldValue(uLoanFeeDef.ShortedRescFeeAmount().getName(), "");
				systolicPhaseFeeForm.setFieldReadOnly(uLoanFeeDef.ShortedRescFeeAmount().getName(), false);
				systolicPhaseFeeForm.setFieldRequired(uLoanFeeDef.ShortedRescFeeAmount().getName(), false);
			}else if("A".equals(shortedRescCalcMethodValue)){
				systolicPhaseFeeForm.setFieldValue(uLoanFeeDef.ShortedRescFeeAmountRate().getName(), "");
				systolicPhaseFeeForm.setFieldRequired(uLoanFeeDef.ShortedRescFeeAmountRate().getName(), false);
				systolicPhaseFeeForm.setFieldReadOnly(uLoanFeeDef.ShortedRescFeeAmountRate().getName(), false);
				systolicPhaseFeeForm.setFieldRequired(uLoanFeeDef.ShortedRescFeeAmount().getName(), true);
				systolicPhaseFeeForm.setFieldReadOnly(uLoanFeeDef.ShortedRescFeeAmount().getName(), true);
			}else{
				systolicPhaseFeeForm.setFieldValue(uLoanFeeDef.ShortedRescFeeAmount().getName(), "");
				systolicPhaseFeeForm.setFieldValue(uLoanFeeDef.ShortedRescFeeAmountRate().getName(), "");
				systolicPhaseFeeForm.setFieldRequired(uLoanFeeDef.ShortedRescFeeAmountRate().getName(), true);
				systolicPhaseFeeForm.setFieldReadOnly(uLoanFeeDef.ShortedRescFeeAmountRate().getName(), true);
				systolicPhaseFeeForm.setFieldValue(uLoanFeeDef.ShortedRescFeeAmount().getName(), "");
				systolicPhaseFeeForm.setFieldValue(uLoanFeeDef.ShortedRescFeeAmountRate().getName(), "");
				systolicPhaseFeeForm.setFieldRequired(uLoanFeeDef.ShortedRescFeeAmount().getName(), true);
				systolicPhaseFeeForm.setFieldReadOnly(uLoanFeeDef.ShortedRescFeeAmount().getName(), true);
			}
		}
	}
	/*
	private void validateLateFeeCharge() {
		String lateFeeChargeValue = lateFeeForm.getFieldValue(uLoanFeeDef.LateFeeCharge().getName());
		if("Y".equals(lateFeeChargeValue)){
			lateFeeForm.setFieldRequired(uLoanFeeDef.MinAgeCd().getName(), true);
			lateFeeForm.setFieldRequired(uLoanFeeDef.Threshold().getName(), true);
			lateFeeForm.setFieldRequired(uLoanFeeDef.MinCharge().getName(), true);
			lateFeeForm.setFieldRequired(uLoanFeeDef.MaxCharge().getName(), true);
			lateFeeForm.setFieldRequired(uLoanFeeDef.YearMaxCharge().getName(), true);
			lateFeeForm.setFieldRequired(uLoanFeeDef.YearMaxCnt().getName(), true);
			lateFeeForm.setFieldRequired(uLoanFeeDef.CalcBaseInd().getName(), true);
			lateFeeForm.setFieldRequired(uLoanFeeDef.TierInd().getName(), true);
		}else{
			lateFeeForm.setFieldRequired(uLoanFeeDef.MinAgeCd().getName(), false);
			lateFeeForm.setFieldRequired(uLoanFeeDef.Threshold().getName(), false);
			lateFeeForm.setFieldRequired(uLoanFeeDef.MinCharge().getName(), false);
			lateFeeForm.setFieldRequired(uLoanFeeDef.MaxCharge().getName(), false);
			lateFeeForm.setFieldRequired(uLoanFeeDef.YearMaxCharge().getName(), false);
			lateFeeForm.setFieldRequired(uLoanFeeDef.YearMaxCnt().getName(), false);
			lateFeeForm.setFieldRequired(uLoanFeeDef.CalcBaseInd().getName(), false);
			lateFeeForm.setFieldRequired(uLoanFeeDef.TierInd().getName(), false);
		}
		
	}
	*/
	private void validateRescheDule() {
		Boolean isRescheduleInd = Boolean.parseBoolean(rescheduleForm.getFieldValue(uLoanFeeDef.RescheduleInd().getName()));
		if(isRescheduleInd){
			rescheduleForm.setFormReadOnly(true);
			rescheduleForm.setFieldRequired(uLoanFeeDef.RescheduleFeeMethod().getName(), true);
			rescheduleForm.setFieldRequired(uLoanFeeDef.RescheduleCalcMethod().getName(), true);
			rescheduleForm.setFieldRequired(uLoanFeeDef.RescheduleFeeAmount().getName(), true);
			rescheduleForm.setFieldRequired(uLoanFeeDef.RescheduleFeeRate().getName(), true);
			rescheduleForm.setFieldRequired(uLoanFeeDef.RescheduleMinAmount().getName(), true);
			rescheduleForm.setFieldRequired(uLoanFeeDef.RescheduleMaxAmount().getName(), true);
		}else{
			rescheduleForm.setFieldValue(uLoanFeeDef.RescheduleFeeMethod().getName(), "");
			rescheduleForm.setFieldValue(uLoanFeeDef.RescheduleCalcMethod().getName(), "");
			rescheduleForm.setFieldValue(uLoanFeeDef.RescheduleFeeAmount().getName(), "");
			rescheduleForm.setFieldValue(uLoanFeeDef.RescheduleFeeRate().getName(), "");
			rescheduleForm.setFieldValue(uLoanFeeDef.RescheduleMinAmount().getName(), "");
			rescheduleForm.setFieldValue(uLoanFeeDef.RescheduleMaxAmount().getName(), "");
			rescheduleForm.setFormReadOnly(false);
			rescheduleForm.setFieldReadOnly(uLoanFeeDef.RescheduleInd().getName(), true);
			rescheduleForm.setFieldRequired(uLoanFeeDef.RescheduleFeeMethod().getName(), false);
			rescheduleForm.setFieldRequired(uLoanFeeDef.RescheduleCalcMethod().getName(), false);
			rescheduleForm.setFieldRequired(uLoanFeeDef.RescheduleFeeAmount().getName(), false);
			rescheduleForm.setFieldRequired(uLoanFeeDef.RescheduleFeeRate().getName(), false);
			rescheduleForm.setFieldRequired(uLoanFeeDef.RescheduleMinAmount().getName(), false);
			rescheduleForm.setFieldRequired(uLoanFeeDef.RescheduleMaxAmount().getName(), false);
		}
		
	}
	
	
	
	private void validateSystolicPhaseInd() {
		Boolean isSystolicPhaseInd = Boolean.parseBoolean(systolicPhaseFeeForm.getFieldValue(uLoanFeeDef.ShortedRescInd().getName()));
		if(isSystolicPhaseInd){
			systolicPhaseFeeForm.setFormReadOnly(true);
			systolicPhaseFeeForm.setFieldRequired(uLoanFeeDef.ShortedRescCalcMethod().getName(), true);
			systolicPhaseFeeForm.setFieldRequired(uLoanFeeDef.ShortedRescFeeAmount().getName(), true);
			systolicPhaseFeeForm.setFieldRequired(uLoanFeeDef.ShortedRescFeeAmountRate().getName(), true);
			systolicPhaseFeeForm.setFieldRequired(uLoanFeeDef.ShortedMinPmtDue().getName(), true);
			systolicPhaseFeeForm.setFieldRequired(uLoanFeeDef.ShorteRescdMinAmount().getName(), true);
		}else{
			systolicPhaseFeeForm.setFieldValue(uLoanFeeDef.ShortedRescCalcMethod().getName(), "");
			systolicPhaseFeeForm.setFieldValue(uLoanFeeDef.ShortedRescFeeAmount().getName(), "");
			systolicPhaseFeeForm.setFieldValue(uLoanFeeDef.ShortedRescFeeAmountRate().getName(), "");
			systolicPhaseFeeForm.setFieldValue(uLoanFeeDef.ShortedMinPmtDue().getName(), "");
			systolicPhaseFeeForm.setFieldValue(uLoanFeeDef.ShorteRescdMinAmount().getName(), "");
			systolicPhaseFeeForm.setFormReadOnly(false);
			systolicPhaseFeeForm.setFieldReadOnly(uLoanFeeDef.ShortedRescInd().getName(), true);
			systolicPhaseFeeForm.setFieldRequired(uLoanFeeDef.ShortedRescCalcMethod().getName(), false);
			systolicPhaseFeeForm.setFieldRequired(uLoanFeeDef.ShortedRescFeeAmount().getName(), false);
			systolicPhaseFeeForm.setFieldRequired(uLoanFeeDef.ShortedRescFeeAmountRate().getName(), false);
			systolicPhaseFeeForm.setFieldRequired(uLoanFeeDef.ShortedMinPmtDue().getName(), false);
			systolicPhaseFeeForm.setFieldRequired(uLoanFeeDef.ShorteRescdMinAmount().getName(), false);
		}
	}
	
	//用于分期区间比较
	//1:大于;-1:小于;0:等于
	public Boolean compareRange(BigDecimal range1Min,BigDecimal range1Max,BigDecimal range2Min,BigDecimal range2Max){
		if(range1Min.compareTo(range2Min)== -1 && range1Max.compareTo(range2Min) == -1){
			return false;
		}
		if(range2Min.compareTo(range1Min)== -1 && range2Max.compareTo(range1Min) == -1){
			return false;
		}
//		if(range1Min.compareTo(range2Min)==1&&range1Min.compareTo(range2Max)==-1) return false;
//		if(range1Max.compareTo(range2Min)==1&&range1Max.compareTo(range2Max)==-1) return false;
//		if(range1Min.compareTo(range2Min)==-1&&range1Max.compareTo(range2Min)==-1) return false;
//		if(range1Min.compareTo(range2Min)==1&&range1Max.compareTo(range2Max)==-1) return false;
		return true;
	}
	
}
