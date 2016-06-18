package com.sunline.ccs.batch.cc8300;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemProcessor;

import com.sunline.ccs.batch.cc8000.U8001AuthMemo;
import com.sunline.ccs.batch.cc8000.U8001InitializeAuthMemo;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoDelTmp;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoHst;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;

/**
 * @see 类名：P8301AuthMemoDel
 * @see 描述：根据deletingList删除unmatchO中的交易
 *
 * @see 创建日期：   2015-6-24下午2:28:55
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P8301AuthMemoDel implements ItemProcessor<CcsAuthmemoDelTmp, U8001AuthMemo> {

	@PersistenceContext
	private EntityManager em;

	@Override
	public U8001AuthMemo process(CcsAuthmemoDelTmp item) throws Exception {
		CcsAuthmemoO m= em.find(CcsAuthmemoO.class, item.getLogKv());
		if(null != m){
			//增加转移历史
			CcsAuthmemoHst hst = new CcsAuthmemoHst();
			// 赋值
			hst.updateFromMap(m.convertToMap());
			// 保存对象
			em.persist(hst);
			
			em.remove(m);
			return U8001InitializeAuthMemo.setItemFromMap(m.convertToMap());
		}
		return null;
	}
}
