/**
 * 
 */
package com.sunline.ccs.service.handler.sub;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsGiftGoods;
import com.sunline.ccs.infrastructure.shared.model.QCcsGiftGoods;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S17030Item;
import com.sunline.ccs.service.protocol.S17030Req;
import com.sunline.ccs.service.protocol.S17030Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.PageTools;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQGiftInfo
 * @see 描述：礼品信息查询
 *
 * @see 创建日期： 2015-6-25下午12:21:27
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQGiftInfo {
	@PersistenceContext
	public EntityManager em;
	private Logger logger = LoggerFactory.getLogger(getClass());
	QCcsGiftGoods qCcsGiftGoods = QCcsGiftGoods.ccsGiftGoods;

	@Transactional
	public S17030Resp handler(S17030Req req) throws ProcessException {

		LogTools.printLogger(logger, "S17030", "礼品信息查询", req, true);

		JPAQuery query = new JPAQuery(em);// 主查询
		JPAQuery querycount = new JPAQuery(em);// 查询总页数
		BooleanExpression booleanExpression;

		List<CcsGiftGoods> tmGiftExchList = new ArrayList<CcsGiftGoods>();

		int totalRows = 0;

		// 验证操作码必须有效
		if (!StringUtils.equals(req.getOpt(), Constants.OPT_ZERO)
				&& !StringUtils.equals(req.getOpt(), Constants.OPT_ONE)
				&& !StringUtils.equals(req.getOpt(), Constants.OPT_TWO)) {
			throw new ProcessException(Constants.ERRS004_CODE,
					Constants.ERRS004_MES);
		}

		// 查询所有礼品信息
		if (CheckUtil.equals(req.getOpt(), Constants.OPT_ZERO)) {
			tmGiftExchList = query
					.from(qCcsGiftGoods)
					.where(qCcsGiftGoods.org.eq(OrganizationContextHolder
							.getCurrentOrg()))
					.orderBy(qCcsGiftGoods.giftNbr.desc())
					.offset(req.getFirstrow())
					.limit(PageTools.calculateLmt(req.getFirstrow(),
							req.getLastrow())).list(qCcsGiftGoods);

			totalRows = (int) querycount
					.from(qCcsGiftGoods)
					.where(qCcsGiftGoods.org.eq(OrganizationContextHolder
							.getCurrentOrg())).count();
		}

		// 按照兑换分值范围查询
		if (CheckUtil.equals(req.getOpt(), Constants.OPT_ONE)) {
			if (req.getMin_bonus() != null && req.getMax_bonus() != null
					&& req.getMin_bonus() > req.getMax_bonus()) {
				throw new ProcessException(Constants.ERRB083_CODE,
						Constants.ERRB083_MES);
			}

			booleanExpression = qCcsGiftGoods.org.eq(OrganizationContextHolder
					.getCurrentOrg());
			if (req.getMin_bonus() != null)
				booleanExpression = booleanExpression
						.and(qCcsGiftGoods.giftBonus.goe(req.getMin_bonus()));
			if (req.getMax_bonus() != null)
				booleanExpression = booleanExpression
						.and(qCcsGiftGoods.giftBonus.loe(req.getMax_bonus()));

			// booleanExpression =
			// qCcsGiftGoods.giftBonus.goe(req.getMin_bonus()).and(qCcsGiftGoods.giftBonus.loe(req.getMax_bonus())).and(qCcsGiftGoods.org.eq(OrganizationContextHolder.getCurrentOrg()));

			tmGiftExchList = query
					.from(qCcsGiftGoods)
					.where(booleanExpression)
					.orderBy(qCcsGiftGoods.giftBonus.desc())
					.offset(req.getFirstrow())
					.limit(PageTools.calculateLmt(req.getFirstrow(),
							req.getLastrow())).list(qCcsGiftGoods);

			totalRows = (int) querycount.from(qCcsGiftGoods)
					.where(booleanExpression).count();
		}

		// 按礼品编号查询
		if (CheckUtil.equals(req.getOpt(), Constants.OPT_TWO)) {
			// 校验礼品编号是否合法和是否为空
			CheckUtil.checkGiftNbr(req.getItem_id());

			booleanExpression = qCcsGiftGoods.giftNbr.eq(req.getItem_id()).and(
					qCcsGiftGoods.org.eq(OrganizationContextHolder
							.getCurrentOrg()));

			tmGiftExchList = query
					.from(qCcsGiftGoods)
					.where(booleanExpression)
					.orderBy(qCcsGiftGoods.giftNbr.desc())
					.offset(req.getFirstrow())
					.limit(PageTools.calculateLmt(req.getFirstrow(),
							req.getLastrow())).list(qCcsGiftGoods);

			totalRows = (int) querycount.from(qCcsGiftGoods)
					.where(booleanExpression).count();
		}

		ArrayList<S17030Item> items = new ArrayList<S17030Item>();

		for (CcsGiftGoods tmGiftExch : tmGiftExchList) {
			S17030Item item = new S17030Item();

			item.setItem_id(tmGiftExch.getGiftNbr());
			// item.setMin_bonus(req.getMin_bonus());
			// item.setMax_bonus(req.getMax_bonus());
			item.setItem_brand(tmGiftExch.getGiftBrand());
			item.setItem_brand_desc(tmGiftExch.getGiftBrandDesc());
			item.setItem_name(tmGiftExch.getGiftName());
			item.setItem_desc(tmGiftExch.getGiftDesc());
			item.setExch_bonus(tmGiftExch.getGiftBonus());
			item.setItem_price(tmGiftExch.getGiftPrice());
			items.add(item);
		}

		// 构建响应报文
		S17030Resp resp = new S17030Resp();

		resp.setMin_bonus(req.getMin_bonus());
		resp.setMax_bonus(req.getMax_bonus());
		resp.setFirstrow(req.getFirstrow());
		resp.setLastrow(req.getLastrow());
		resp.setNextpage_flg(PageTools.hasNextPage(req.getLastrow(), totalRows));
		resp.setTotal_rows(totalRows);
		resp.setItems(items);

		LogTools.printLogger(logger, "S17030", "礼品信息查询", resp, false);
		return resp;

	}

}
