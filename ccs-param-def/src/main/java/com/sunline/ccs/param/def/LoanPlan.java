package com.sunline.ccs.param.def;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sunline.ark.support.meta.PropertyInfo;
import com.sunline.ccs.param.def.enums.LoanPlanStatus;
import com.sunline.ccs.param.def.enums.PrepaymentFeeInd;
import com.sunline.ccs.param.def.enums.PrepaymentInd;
import com.sunline.ccs.param.def.enums.ReturnFeeInd;
import com.sunline.ccs.param.def.enums.ReturnInd;
import com.sunline.ccs.param.def.enums.ReturnPointInd;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.LoanMold;
import com.sunline.ppy.dictionary.enums.LoanType;
import com.sunline.ppy.dictionary.enums.Ownership;
import com.sunline.ppy.dictionary.enums.PenaltyAccuBase;

/**
 * 分期计划参数
 * 
 * @author fanghj
 *
 */
public class LoanPlan implements Serializable {

	private static final long serialVersionUID = -3475071837459343780L;

	/**
	 * 分期代码
	 */
	@PropertyInfo(name = "分期代码", length = 4)
	public String loanCode;

	/**
	 * 分期计划描述
	 */
	@PropertyInfo(name = "描述", length = 40)
	public String description;

	/**
	 * 分期类型
	 */
	@PropertyInfo(name = "分期类型", length = 1)
	public LoanType loanType;

	/**
	 * 分期中止账龄
	 */
	@PropertyInfo(name = "中止账龄", length = 1)
	public String terminateAgeCd;

	/**
	 * 分期产品有效期
	 */
	@PropertyInfo(name = "分期产品有效期", length = 8)
	public Date loanValidity;

	/**
	 * 分期产品状态
	 */
	@PropertyInfo(name = "分期产品状态", length = 1)
	public LoanPlanStatus loanStaus;

	/**
	 * 退货支持标志
	 */
	@PropertyInfo(name = "退货支持标志", length = 1)
	public ReturnInd returnInd;

	/**
	 * 退货退积分标志
	 */
	@PropertyInfo(name = "退货退积分标志", length = 1)
	public ReturnPointInd returnPointInd;

	/**
	 * 退货退手续费标志
	 */
	@PropertyInfo(name = "退货退手续费标志", length = 1)
	public ReturnFeeInd returnFeeInd;

	/**
	 * 提前还款支持标识
	 */
	@PropertyInfo(name = "提前还款支持标识", length = 1)
	public PrepaymentInd prepaymentInd;

	/**
	 * 提前还款退手续费标志
	 */
	@PropertyInfo(name = "提前还款退手续费标志", length = 1)
	public PrepaymentFeeInd prepaymentFeeInd;

	/**
	 * 临时额度是否参与分期
	 */
	@PropertyInfo(name = "临时额度是否参与分期", length = 1)
	public Boolean useTemplimit;

	/**
	 * 约定扣款联系扣款日
	 */
	@PropertyInfo(name = "约定扣款联系扣款日", length = 2)
	public Integer dDMode;

	/**
	 * 贷款转出计划
	 */
	@PropertyInfo(name = "贷款转出计划", length = 6)
	public String loanXfrOutPlan;

	/**
	 * 贷款转入计划
	 */
	@PropertyInfo(name = "贷款转入计划", length = 6)
	public String loanXfrInPlan;

	/**
	 * 贷款支持的最短期限(默认以月为单位)
	 */
	@PropertyInfo(name = "贷款最短周期(月)", length = 2)
	public Integer minCycle;

	/**
	 * 贷款支持的最长期限(默认以月为单位)
	 */
	@PropertyInfo(name = "贷款最长周期(月)", length = 2)
	public Integer maxCycle;

	/**
	 * 受支持的交易代码
	 */
	public List<String> txnCdList;

	/**
	 * 分期计划计价方式列表 Key - 计价方式键值（对应唯一的期数+最小分期金额+最大分子金额组）
	 * 				       Value - 计价方式
	 */
	public Map<Integer, LoanFeeDef> loanFeeDefMap;

	// 马上消费 start

	/**
	 * 放款类型
	 */
	@PropertyInfo(name = "放款类型", length = 1)
	public LoanMold loanMold;

	/**
	 * 所属
	 */
	@PropertyInfo(name = "所有方", length = 1)
	public Ownership  ownership; 


	 /**
     * 产品代码
     */
    @PropertyInfo(name = "产品代码", length = 6)
    public String productCode;
    
    //马上贷二期参数
    /**
     * 分群控制表ID
     */
    @PropertyInfo(name = "分群控制表ID",length = 4)
    public String groupId;
    /**
     * 合同模板编号
     */
    @PropertyInfo(name ="合同模板编号",length = 8)
    public String contractTemplateId;
	/**
	 * CPD逾期终止天数
	 */
	@PropertyInfo(name = "CPD逾期终止天数", length = 4)
	public Integer cpdOverdueEndDays;
	
    /**
     * 是否自动豁免
     */
    @PropertyInfo(name="是否自动豁免", length=1)
    public Indicator isAutoWaive;
    
    /**
	 * 允许自动豁免最小CPD天数
	 */
	@PropertyInfo(name = "允许自动豁免最小CPD天数", length = 3)
	public Integer autoWaiveCpdDays;
	
	/**
     * 允许自动豁免最大欠款
     */
    @PropertyInfo(name="允许自动豁免最大欠款金额", length=15, precision=2)
    public BigDecimal autoWaiveAMT;
    
    /**
     * 是否逾期计息
     */
    @PropertyInfo(name="是否逾期计息", length=1)
    public Indicator isOverAccruIns;
    
    //安逸花
    @PropertyInfo(name="罚息累计基数",length=1)
    public PenaltyAccuBase penaltyAccuBase;
    
    //兜底
    /**
     * 溢缴款转出距离账单日提前天数
     */
    @PropertyInfo(name="溢缴款转出距离账单日提前天数",length=2)
    public Integer depositEarlyDays;
}
