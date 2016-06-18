package com.sunline.ccs.batch.cc8100;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemProcessor;

import com.sunline.ccs.batch.cc8000.U8001AuthMemo;
import com.sunline.ccs.batch.cc8000.U8001InitializeAuthMemo;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoHst;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;

/**
 * @see 类名：P8101AuthMemoClean
 * @see 描述： 根据条件删除unmatchO中的反向交易
 *
 * @see 创建日期：   2015-6-24下午2:27:29
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P8101AuthMemoClean implements ItemProcessor<CcsAuthmemoO, U8001AuthMemo> {

	@PersistenceContext
	private EntityManager em;

	@Override
	public U8001AuthMemo process(CcsAuthmemoO item) throws Exception {
		//增加转移历史
		CcsAuthmemoHst hst = new CcsAuthmemoHst();
		// 赋值
		hst.updateFromMap(item.convertToMap());
		// 保存对象
		em.persist(hst);
		
		em.remove(item);
		return U8001InitializeAuthMemo.setItemFromMap(item.convertToMap());
	}
}
