package com.sunline.ccs.batch.cc8000;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemProcessor;

import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoHst;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;

/**
 * @see 类名：P8001AuthMemoHst
 * @see 描述：备份unmatchO到历史表，并生成文件
 *
 * @see 创建日期：   2015-6-24下午2:26:45
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class P8001AuthMemoHst implements ItemProcessor<CcsAuthmemoO, U8001AuthMemo> {

	@PersistenceContext
	private EntityManager em;

	@Override
	public U8001AuthMemo process(CcsAuthmemoO item) throws Exception {
		CcsAuthmemoHst hst = new CcsAuthmemoHst();
		// 赋值
		hst.updateFromMap(item.convertToMap());
		// 保存对象
		em.persist(hst);
		// 输出文件
		return U8001InitializeAuthMemo.setItemFromMap(item.convertToMap());
	}
}
