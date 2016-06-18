package com.sunline.ccs.batch.cc1200;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemProcessor;

import com.sunline.ccs.infrastructure.shared.model.CcsCssfeeReg;
import com.sunline.ppy.dictionary.exchange.CustServFeeNoticeInterfaceItem;


/**
 * @see 类名：P1201LoadCssFee
 * @see 描述：CMD038-MPS客服费用接口转换<p>
 * 			  将外部系统（MPS等）产生的客服费用通知文件，注册到客服费收费注册表。
 *
 * @see 创建日期：   2015-6-23下午7:23:35
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P1201LoadCssFee implements ItemProcessor<CustServFeeNoticeInterfaceItem, Object>{
	@PersistenceContext
	private EntityManager em;
	
	@Override
	public Object process(CustServFeeNoticeInterfaceItem item) throws Exception {
		CcsCssfeeReg cssfeeReg = new CcsCssfeeReg();
		
		cssfeeReg.setCardNbr(item.cardNo);
		cssfeeReg.setOrg(item.org);
		cssfeeReg.setServiceNbr(item.serviceNbr);
		cssfeeReg.setTxnDate(item.txnDate);
		cssfeeReg.setRequestTime(item.requestTime);
		
		em.persist(cssfeeReg);
		
		return null;
	}
}
