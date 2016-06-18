/**
 * 
 */
package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsCard;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S14021Req;
import com.sunline.ccs.service.protocol.S14021Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ccs.service.util.DataTypeUtils;
import com.sunline.pcm.param.def.Product;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQProductFee
 * @see 描述：固定费用查询
 *
 * @see 创建日期： 2015-6-25下午5:08:36
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQProductFee {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	@PersistenceContext
	public EntityManager em;

	@Transactional
	public S14021Resp handler(S14021Req req) throws ProcessException {

		LogTools.printLogger(logger, "14021", "固定费用查询", req, true);

		// 校验卡号是否合法
		CheckUtil.checkCardNo(req.getCard_no());

		// 获取卡片信息
		CcsCard CcsCard = custAcctCardQueryFacility.getCardByCardNbr(req.getCard_no());
		CheckUtil.rejectNull(CcsCard, Constants.ERRB001_CODE, Constants.ERRB001_MES);

		// 根据卡片信息中产品代码获取卡产品信息
		Product product = unifiedParameterService.loadParameter(CcsCard.getProductCd(), Product.class);
		// 根据卡片信息中产品代码获取卡产品贷记属性参数
		ProductCredit productCredit = unifiedParameterService.loadParameter(CcsCard.getProductCd(), ProductCredit.class);

		S14021Resp resp = new S14021Resp();

		resp.setProduct_name(product.description);// 产品名称
		resp.setCardclass(product.cardClass);// 卡等级
		resp.setBrand(product.brand);// 卡品牌
		if (productCredit.customerServiceFee.get("S14100") != null) {
			resp.setPasswd_reset_fee(DataTypeUtils.nullConvert(productCredit.customerServiceFee.get("S14100").fee, 2, BigDecimal.ROUND_HALF_UP));// 密码重置费
		}
		if (productCredit.customerServiceFee.get("S14050") != null) {
			resp.setCard_loss_fee(DataTypeUtils.nullConvert(productCredit.customerServiceFee.get("S14050").fee, 2, BigDecimal.ROUND_HALF_UP));// 卡片挂失费
		}
		if (productCredit.customerServiceFee.get("S12030") != null) {
			resp.setRepint_stmt_fee(DataTypeUtils.nullConvert(productCredit.customerServiceFee.get("S12030").fee, 2, BigDecimal.ROUND_HALF_UP));// 补打账单费
		}
		if (productCredit.customerServiceFee.get("S14070") != null) {
			resp.setCard_issue_fee(DataTypeUtils.nullConvert(productCredit.customerServiceFee.get("S14070").fee, 2, BigDecimal.ROUND_HALF_UP));// 卡片工本费
		}
		resp.setFirst_card_fee_ind(productCredit.fee.firstCardFeeInd);// 首次年费收取方式
		resp.setB_card_fee(productCredit.fee.primCardFee);// 主卡年费
		resp.setS_card_fee(productCredit.fee.suppCardFee);// 附卡年费
		resp.setUrgent_fee(productCredit.fee.urgentFee);// 加急费
		resp.setSms_fee(productCredit.fee.smsFee);// 短信费

		LogTools.printLogger(logger, "14021", "固定费用查询", resp, false);
		return resp;

	}

}
