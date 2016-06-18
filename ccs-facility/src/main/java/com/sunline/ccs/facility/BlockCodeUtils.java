package com.sunline.ccs.facility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.param.def.BlockCode;
import com.sunline.ccs.param.def.enums.AuthAction;
import com.sunline.ccs.param.def.enums.PaymentCalcMethod;


/**
 * 锁定码处理
* @author fanghj
 *
 */
@Service
public class BlockCodeUtils {

	/**
	 * 获取参数工具类
	 */
	@Autowired
	private UnifiedParameterFacility unifiedParameter;
	

	private static final String AGE_CD_LIST = "123456789";
	
	/**
	 * 获取优先级最高的锁定码参数
	 * 如果输入的锁定码列表在参数中找不到对应的锁定码,返回null
	 * @param blockCodes
	 * @return 优先级最高的BlockCode
	 */
	public BlockCode getFirstByPriority(String blockcodes)
	{
		//	判断锁定码列表是否为空
		if (blockcodes == null){
			return null;
		}
		
		//	校验锁定码
		validate(blockcodes);
		
		//	锁定码列表
		List<BlockCode> list = getBlockCodeList(blockcodes);
		
		//	判断列表是否为空
		if (list.isEmpty())
			//	列表为空，返回null
			return null;
		else
		{
			//	有锁定码，排序，返回优先级最高的blockCode
			Collections.sort(list);
			return list.get(0);
		}
	}
	
	/**
	 * 解析锁定码，并获取所有锁定码的参数列表
	 * @param blockCodes
	 * @return blockcode参数对象列表
	 */
	public List<BlockCode> getBlockCodeList(String blockcodes)
	{
		//	锁定码列表
		List<BlockCode> list = new ArrayList<BlockCode>();
		
		//	依次解析锁定码
		if (blockcodes != null){
			for (Character c : blockcodes.toCharArray())
			{
				BlockCode code = unifiedParameter.loadParameter(c.toString(), BlockCode.class);
				list.add(code);
			}
		}
		return list;
	}
	
	/**
	 *	获取合并锁定码对象
	 * @param blockCodes
	 * @return
	 */
	public BlockCode getMergedBlockCode(String blockcodes)
	{
			
		BlockCode code = new BlockCode();
		
		code.cardFeeWaiveInd = getMergedCardFeeWaiveInd(blockcodes);
		code.collectionInd = getMergedCollectionInd(blockcodes);
		code.intAccuralInd = getMergedIntAccuralInd(blockcodes);
		code.intWaiveInd = getMergedIntWaiveInd(blockcodes);
		code.lateFeeWaiveInd = getMergedLateFeeWaiveInd(blockcodes);
		code.loanInd = getMergedLoanInd(blockcodes);
		code.ovrlmtFeeWaiveInd = getMergedOvrlmtFeeWaiveInd(blockcodes);
		code.paymentInd = getMergedPaymentInd(blockcodes);
		code.pointEarnInd = getMergedPointEarnInd(blockcodes);
		code.renewInd = getMergedRenewInd(blockcodes);
		code.stmtInd = getMergedStmtInd(blockcodes);
		code.txnFeeWaiveInd = getMergedTxnFeeWaiveInd(blockcodes);
		
		//	TODO Merge 授权相关参数
		return code;
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取入催标志
	 * 任意一个锁定码对应的入催标志为true,则返回true
	 * 所有锁定码对应的入催标志为false，返回false
	 * @param blockCodes
	 * @return false-不入催 true-入催
	 */
	public Boolean getMergedCollectionInd(String blockcodes)
	{
		Boolean collectionInd = false;
		for (BlockCode code : getBlockCodeList(blockcodes))
		{
			collectionInd |= code.collectionInd;
		}
		
		return collectionInd;
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取是否进行日常利息累积标志
	 * 任意一个锁定码对应的否进行日常利息累积标志为false,则返回false
	 * 所有锁定码对应的日常利息累积标志为true，返回true
	 * @param blockCodes
	 * @return false-不累积利息 true-累积利息
	 */
	public Boolean getMergedIntAccuralInd(String blockcodes)
	{
		Boolean intAccuralInd = true;
		for (BlockCode code : getBlockCodeList(blockcodes))
		{
			intAccuralInd &= code.intAccuralInd;
		}
		
		return intAccuralInd;
	}
	
	
	/**
	 * 根据帐户上的锁定码列表，获取是否允许续卡标志
	 * 任意一个锁定码对应的允许续卡标志为false,则返回false
	 * 所有锁定码对应的入催标志为true，返回true
	 * @param blockCodes
	 * @return false-不允许续卡 true-允许续卡
	 */
	public Boolean getMergedRenewInd(String blockcodes)
	{
		Boolean renewInd = true;
		for (BlockCode code : getBlockCodeList(blockcodes))
		{
			renewInd &= code.renewInd;
		}
		
		return renewInd;
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取是否利息减免标志
	 * 任意一个锁定码对应的是否计息标志为true,则返回true
	 * 所有锁定码对应的是否计息标志为false，返回false
	 * @param blockCodes
	 * @return true-减免利息，不入账 false-不减免利息，需入账
	 */
	public Boolean getMergedIntWaiveInd(String blockcodes)
	{
		Boolean intWaiveInd = false;
		for (BlockCode code : getBlockCodeList(blockcodes))
		{
			intWaiveInd |= code.intWaiveInd;
		}
		
		return intWaiveInd;
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取是否免除交易费标志
	 * 任意一个锁定码对应的是否免除交易费标志为true,则返回true
	 * 所有锁定码对应的免除交易费标志为false，返回false
	 * @param blockCodes
	 * @return false-不减免交易费 true-减免交易费
	 */
	public Boolean getMergedTxnFeeWaiveInd(String blockcodes)
	{
		Boolean txnFeeWaiveInd = false;
		for (BlockCode code : getBlockCodeList(blockcodes))
		{
			txnFeeWaiveInd |= code.txnFeeWaiveInd;
		}
		
		return txnFeeWaiveInd;
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取是否免除年费标志
	 * 任意一个锁定码对应的是否免除年费标志为true,则返回true
	 * 所有锁定码对应的是否免除年费标志为false，返回false
	 * @param blockCodes
	 * @return false-收取年费 true-免收年费 
	 */
	public Boolean getMergedCardFeeWaiveInd(String blockcodes)
	{
		Boolean cardFeeWaiveInd = false;
		for (BlockCode code : getBlockCodeList(blockcodes))
		{
			cardFeeWaiveInd |= code.cardFeeWaiveInd;
		}
		
		return cardFeeWaiveInd;
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取是否免除超限费标志
	 * 任意一个锁定码对应的是否免除超限费标志为true,则返回true
	 * 所有锁定码对应的是否免除超限费标志为false，返回false
	 * @param blockCodes
	 * @return false-收取超限费 true-免受超限费
	 */
	public Boolean getMergedOvrlmtFeeWaiveInd(String blockcodes)
	{
		Boolean ovrlmtFeeWaiveInd = false;
		for (BlockCode code : getBlockCodeList(blockcodes))
		{
			ovrlmtFeeWaiveInd |= code.ovrlmtFeeWaiveInd;
		}
		
		return ovrlmtFeeWaiveInd;
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取是否免除滞纳金标志
	 * 任意一个锁定码对应的是否免除滞纳金标志为true,则返回true
	 * 所有锁定码对应的是否免除滞纳金标志为false，返回false
	 * @param blockCodes
	 * @return false-收取滞纳金 true-免收滞纳金
	 */
	public Boolean getMergedLateFeeWaiveInd(String blockcodes)
	{
		Boolean lateFeeWaiveInd = false;
		for (BlockCode code : getBlockCodeList(blockcodes))
		{
			lateFeeWaiveInd |= code.lateFeeWaiveInd;
		}
		
		return lateFeeWaiveInd;
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取是否免除罚金标志
	 * 任意一个锁定码对应的是否免除罚金标志为true,则返回true
	 * 所有锁定码对应的是否免除罚金标志为false，返回false
	 * @param blockCodes
	 * @return false-收取罚金 true-免收罚金
	 */
	public Boolean getMergedMulctInd(String blockcodes)
	{
		Boolean mulctInd = false;
		for (BlockCode code : getBlockCodeList(blockcodes))
		{
			mulctInd |= code.mulctWaiveIND;
		}
		
		return mulctInd;
	}
	/**
	 * 根据帐户上的锁定码列表，获取是否免除服务费标志
	 * 任意一个锁定码对应的是否免除服务费标志为true,则返回true
	 * 所有锁定码对应的是否免除服务费标志为false，返回false
	 * @param blockCodes
	 * @return false-收取服务费 true-免收服务费
	 */
	public Boolean getMergedSvcfeeInd(String blockcodes)
	{
		Boolean svcfeeWaiveIND = false;
		for (BlockCode code : getBlockCodeList(blockcodes))
		{
			svcfeeWaiveIND |= code.svcfeeWaiveIND;
		}
		
		return svcfeeWaiveIND;
	}
	/**
	 * 根据帐户上的锁定码列表，获取是否免除其他费用标志
	 * 任意一个锁定码对应的是否免除其他费用标志为true,则返回true
	 * 所有锁定码对应的是否免除其他费用标志为false，返回false
	 * @param blockCodes
	 * @return false-收取其他费用 true-免收其他费用
	 */
	public Boolean getMergedOtherfeeInd(String blockcodes)
	{
		Boolean otherfeeWaiveIND = false;
		for (BlockCode code : getBlockCodeList(blockcodes))
		{
			otherfeeWaiveIND |= code.otherfeeWaiveIND;
		}
		
		return otherfeeWaiveIND;
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取是否输出账单标志
	 * 任意一个锁定码对应的是否输出账单标志为false,则返回false
	 * 所有锁定码对应的是否输出账单标志为true，返回true
	 * @param blockCodes
	 * @return false-不出账单 true-出账单
	 */
	public Boolean getMergedStmtInd(String blockcodes)
	{
		Boolean stmtInd = true;
		for (BlockCode code : getBlockCodeList(blockcodes))
		{
			stmtInd &= code.stmtInd;
		}
		
		return stmtInd;
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取还款处理方式
	 * 任意一个锁定码对应的还款处理方式标志为B时，返回B,否则返回N
	 * @param blockCodes
	 * @return
	 */
	public PaymentCalcMethod getMergedPaymentInd(String blockcodes)
	{
		PaymentCalcMethod paymentInd = PaymentCalcMethod.N;
		//	TODO 判定S锁定码
		for (BlockCode code : getBlockCodeList(blockcodes))
		{
			if (code.paymentInd == PaymentCalcMethod.B)
				paymentInd = PaymentCalcMethod.B;
		}
		
		return paymentInd;
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取是否累积积分标志
	 * 任意一个锁定码对应的是否累积积分标志为false,则返回false
	 * 所有锁定码对应的是否累积积分标志为true，返回true
	 * @param blockCodes
	 * @return false-不累积积分 true-累积积分
	 */
	public Boolean getMergedPointEarnInd(String blockcodes)
	{
		Boolean pointEarnInd = true;
		for (BlockCode code : getBlockCodeList(blockcodes))
		{
			pointEarnInd &= code.pointEarnInd;
		}
		
		return pointEarnInd;
	}
	
	/**
	 * 根据帐户上的锁定码列表，获取是否允许事后分期交易标志
	 * 任意一个锁定码对应的是否允许事后分期交易标志为false,则返回false
	 * 所有锁定码对应的是否允许事后分期交易标志为true，返回true
	 * @param blockCodes
	 * @return false-不允许分期交易 true-允许分期交易
	 */
	public Boolean getMergedLoanInd(String blockcodes)
	{
		Boolean loanAfterInd = true;
		for (BlockCode code : getBlockCodeList(blockcodes))
		{
			loanAfterInd &= code.loanInd;
		}
		
		return loanAfterInd;
	}
	
	/**
	 * 判断是否允许现金分期
	 * 如果任一锁定码包含不允许取现或不允许分期，返回false，否则返回true
	 * @param blockcodes
	 * @return true-允许现金分期 false-不允许现金分期
	 */
	public boolean isAllowedCashLoan(String blockcodes){
		for(BlockCode code : getBlockCodeList(blockcodes)){
			if(!code.loanInd || !AuthAction.A.equals(code.cashAction)){
				return false;
			}
		}
		return true;
	}
	/**
	 * 在现有的锁定码列表中增加一个锁定码
	 * 对于账龄锁定码，增加一个账龄锁定码会移除其它账龄锁定码
	 * @param blockcodes 原锁定码列表
	 * @param newCode 增加的锁定码
	 * @return
	 */
	public String addBlockCode(String blockcodes, String newCode)
	{
		if (newCode == null)
			return blockcodes;
		
		//	校验锁定码
		validate(blockcodes);

		validate(newCode);

		if (blockcodes == null)
			blockcodes = "";
		
		//	判断锁定码列表是否包含新锁定码
		if (StringUtils.containsAny(blockcodes, newCode))
			//	已包含，直接返回
			return blockcodes;
		else
		{
			//	判断锁定码是否是账龄新锁定码
			if (StringUtils.containsAny(AGE_CD_LIST, newCode))
			{
				//	新锁定码是账龄锁定码
				
				//	移除旧锁定码
				for (int i = 0; i< AGE_CD_LIST.length(); i ++)
				{
					blockcodes = StringUtils.remove(blockcodes, AGE_CD_LIST.charAt(i));
				}
				//	添加新锁定码
				return blockcodes.concat(newCode);
			}
			else
			{
				//	新锁定码不是账龄锁定码
				return blockcodes.concat(newCode);
			}
		}
				
	}
	

	/**
	 * 比较两个blockcode是否相等
	 * @param blockcode1
	 * @param blockcode2
	 * @return
	 */
	public Boolean isEquals(String blockcode1, String blockcode2){
		// 两个输入值都为null时返回true
		if (blockcode1 == null && blockcode2 == null)
			return true;
		// 两个输入值都不为null时进行比较
		else if (blockcode1 != null && blockcode2 != null ){
			List<String> list1 = new ArrayList<String>();
			for (char c : blockcode1.toCharArray()){
				list1.add(String.valueOf(c));
			}
			Collections.sort(list1);
			
			List<String> list2 = new ArrayList<String>();
			for (char c : blockcode2.toCharArray()){
				list2.add(String.valueOf(c));
			}
			Collections.sort(list2);
			return list1.equals(list2); 
		}
		// 一个值为null 一个值不为null时返回false;
		return false;
	}
	
	/**
	 * 移除一个锁定码
	 * @param blockcodes
	 * @param removeCode
	 * @return
	 */
	public String removeBlockCode(String blockcodes, String removeCode)
	{
		//	校验锁定码
		validate(blockcodes);

		validate(removeCode);
		if (blockcodes == null){
			return null;
		}
		if (removeCode == null){
			return blockcodes;
		}
		String bl = blockcodes;
		for (char c : removeCode.toCharArray())
		{
			bl = StringUtils.remove(bl, c);
		}
		return bl;
	}
	
	/**
	 * 判断一个锁定码是否存在
	 * @param blockcodes
	 * @param code
	 * @return
	 */
	public Boolean isExists(String blockcodes, String code)
	{
		//	校验锁定码
		validate(blockcodes);

		validate(code);
		
		return StringUtils.containsAny(blockcodes, code);
	}
	
	/**
	 * 验证锁定码是否在参数中已设置
	 * @param blockcodes
	 */
	private void validate(String blockcodes)
	{
		if (blockcodes != null)
		{
			for (Character c : blockcodes.toCharArray())
			{
				if (unifiedParameter.retrieveParameterObject(c.toString(), BlockCode.class) == null)
				{
					throw new IllegalArgumentException("锁定码:[" + c.toString() + "] 在参数配置中不存在");
				}
			}
		}
	}
	
	/**
	 * 合并BlockCode，取2个blockCode的并集
	 * 
	 * @param blockCode1
	 * @param blockCode2
	 * @return
	 */
	public String unionBlockCodes(String blockCodes1,String blockCodes2){
		if(blockCodes1 == null) blockCodes1 = "";
		if(blockCodes2 == null) blockCodes2 = "";
		
		char[] a = blockCodes1.toCharArray();
		char[] b = blockCodes2.toCharArray();
		Set<Character> set = new LinkedHashSet<Character>();
		for(char _a:a){
			set.add(_a);
		}
		for(char _b:b){
			set.add(_b);
		}
		StringBuilder result = new StringBuilder();
		Iterator<Character> iterator = set.iterator();
		while(iterator.hasNext()){
			result.append(iterator.next());
		}
		
		if("".equals(result)){
			return null;
		}else{
			return result.toString();
		}
	}

}
