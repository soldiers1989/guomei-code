package com.sunline.ccs.batch.cc8200;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemProcessor;

import com.sunline.ccs.batch.cc8000.U8001AuthMemo;
import com.sunline.ccs.batch.cc8000.U8001InitializeAuthMemo;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoInqLog;

/**
 * @see 类名：P8201AuthInqLogClean
 * @see 描述：根据条件删除CcsAuthmemoInqLog中的交易
 *
 * @see 创建日期：   2015-6-24下午2:28:16
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P8201AuthInqLogClean implements ItemProcessor<CcsAuthmemoInqLog, U8001AuthMemo> {

	@PersistenceContext
	private EntityManager em;

	@Override
	public U8001AuthMemo process(CcsAuthmemoInqLog item) throws Exception {
		CcsAuthmemoInqLog inq = em.find(CcsAuthmemoInqLog.class, item.getLogKv());
		if (null != inq) {
			em.remove(inq);
			return U8001InitializeAuthMemo.setItemFromMap(inq.convertToMap());
		}
		return null;
	}
}
