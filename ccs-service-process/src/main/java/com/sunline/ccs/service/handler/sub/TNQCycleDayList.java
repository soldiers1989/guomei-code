package com.sunline.ccs.service.handler.sub;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.service.protocol.S12111Item;
import com.sunline.ccs.service.protocol.S12111Req;
import com.sunline.ccs.service.protocol.S12111Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQCycleDayList
 * @see 描述： 账单日列表查询
 *
 * @see 创建日期： 2015年06月25日下午 03:00:33
 * @author yanjingfeng
 *
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQCycleDayList {

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * @see 方法名：handler
	 * @see 描述：账单日列表查询handler
	 * @see 创建日期：2015年6月25日下午6:13:04
	 * @author yanjingfeng
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException
	 * 
	 * @see 修改记录：
	 * @see [编号：日期]，[修改人：*** ]，[修改说明：***]
	 */
	@Transactional
	public S12111Resp handler(S12111Req req) throws ProcessException {

		LogTools.printLogger(logger, "S12111", "账单日列表查询", req, true);

		S12111Resp resp = new S12111Resp();

		// 校验上送报文卡号的合法性
		CheckUtil.checkCardNo(req.getCard_no());
		// TODO
		ArrayList<S12111Item> items = new ArrayList<S12111Item>();

		for (int i = 1; i < 29; i++) {
			S12111Item item = new S12111Item();
			item.setCycle_day(i);

			items.add(item);
		}
		resp.setItems(items);

		LogTools.printLogger(logger, "S12111", "账单日列表查询", resp, false);

		return resp;
	}
}
