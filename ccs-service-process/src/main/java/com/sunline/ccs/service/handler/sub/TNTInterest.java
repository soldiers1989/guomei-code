package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.UnifiedParamFacilityProvide;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S14022Req;
import com.sunline.ccs.service.protocol.S14022Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNTInterest
 * @see 描述：利息试算功能（还款试算）
 *
 * @see 创建日期： 2015年6月24日 下午4:05:40
 * @author yuyang
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNTInterest {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CustAcctCardFacility custAcctCardQueryFacility;
	@Autowired
	private UnifiedParamFacilityProvide unifiedParameterFacilityProvide;
	@PersistenceContext
	public EntityManager em;

	@Transactional
	public S14022Resp handler(S14022Req req) throws ProcessException {
		LogTools.printLogger(logger, "S14022", "利息试算", req, true);

		// 校验上送报文域
		CheckUtil.checkCardNo(req.getCard_no());
		CheckUtil.checkCurrCd(req.getCurr_cd(), true);
		// 获取账户信息
		CcsAcct CcsAcct = custAcctCardQueryFacility.getAcctByCardNbrCurrency(req.getCard_no(), req.getCurr_cd());
		CheckUtil.rejectNull(CcsAcct, Constants.ERRS001_CODE, Constants.ERRS001_MES);
		CcsAcctO CcsAcctO = custAcctCardQueryFacility.getAcctOByAcctNbr(CcsAcct.getAcctType(), CcsAcct.getAcctNbr());
		CheckUtil.rejectNull(CcsAcctO, Constants.ERRS001_CODE, Constants.ERRS001_MES);

		// 账户余额+未匹配的借记金额-未匹配的贷记金额
		BigDecimal qualGraceBalNoInt = CcsAcctO.getCurrBal().add(CcsAcctO.getMemoDb()).subtract(CcsAcctO.getMemoCr());
		// 计算利息
		BigDecimal noDeferInt = custAcctCardQueryFacility.getPlan_NODEFBNP_INT_ACRUByAcctNbr(CcsAcctO.getAcctNbr(), CcsAcctO.getAcctType());
		// 如果是在GraceData之后，则需要加上延迟累计的利息
		if (DateUtils.truncatedCompareTo(unifiedParameterFacilityProvide.BusinessDate(), CcsAcct.getGraceDate(), Calendar.DATE) > 0) {
			noDeferInt = noDeferInt.add(custAcctCardQueryFacility.getPlan_BEG_DEFBNP_INT_ACRUByAcctNbr(CcsAcctO.getAcctNbr(), CcsAcctO.getAcctType()));
		}

		S14022Resp resp = new S14022Resp();

		resp.setCard_no(req.getCard_no());// 贷款卡号
		resp.setCurr_cd(req.getCurr_cd());// 币种
		// resp.setQual_grace_bal(qualGraceBalNoInt.add(noDeferInt));// 全部应还款额
		BigDecimal qualGraceBal = qualGraceBalNoInt.add(noDeferInt);// 全部应还款额小于0按0修正
		resp.setQual_grace_bal(qualGraceBal.compareTo(new BigDecimal("0")) < 0 ? new BigDecimal("0") : qualGraceBal);
		resp.setCurr_bal(CcsAcctO.getCurrBal());
		resp.setUnmatch_db(CcsAcctO.getMemoDb());
		resp.setUnmatch_cr(CcsAcctO.getMemoCr());
		resp.setUnpost_interest(noDeferInt);

		LogTools.printLogger(logger, "S14022", "利息试算", resp, false);
		return resp;
	}

}
