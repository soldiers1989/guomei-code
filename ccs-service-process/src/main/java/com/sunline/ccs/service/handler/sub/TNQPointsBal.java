package com.sunline.ccs.service.handler.sub;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunline.ccs.facility.CustAcctCardFacility;
import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.service.api.Constants;
import com.sunline.ccs.service.protocol.S17010Req;
import com.sunline.ccs.service.protocol.S17010Resp;
import com.sunline.ccs.service.util.CheckUtil;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * @see 类名：TNQPointsBal
 * @see 描述：积分查询
 *
 * @see 创建日期： 2015-6-25上午11:17:52
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Service
public class TNQPointsBal {
	@PersistenceContext
	public EntityManager em;
	@Autowired
	private CustAcctCardFacility queryFacility;
	@Autowired
	private Common pointComm;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Transactional
	public S17010Resp handler(S17010Req req) throws ProcessException {
		// 校验卡号是否合法
		CheckUtil.checkCardNo(req.getCard_no());
		// 构建响应报文对象
		S17010Resp resp = new S17010Resp();
		// 获取账户信息
		List<CcsAcct> CcsAcctList;
		CcsAcctList = queryFacility.getAcctByCardNbr(req.getCard_no());
		// 获取客户信息
		CcsCustomer ccsCustomer = queryFacility.getCustomerByCardNbr(req.getCard_no());

		if (CcsAcctList == null || CcsAcctList.size() == 0) {
			throw new ProcessException(Constants.ERRB001_CODE, Constants.ERRB001_MES);
		}

		BigDecimal pointsBegBal = BigDecimal.ZERO;// 期初积分余额
		BigDecimal ctdEarnedPoints = BigDecimal.ZERO;// 当期新增积分
		BigDecimal pointsBal = BigDecimal.ZERO;// 积分余额
		BigDecimal ctdDisbPoints = BigDecimal.ZERO;// 当期兑换积分
		BigDecimal ctdAdjPoints = BigDecimal.ZERO;// 当期调整积分

		for (CcsAcct acct : CcsAcctList) {
			pointsBegBal = pointsBegBal.add(acct.getPointsBegBal());
			ctdEarnedPoints = ctdEarnedPoints.add(acct.getCtdPoints());
			pointsBal = pointsBal.add(acct.getPointsBal());
			ctdDisbPoints = ctdDisbPoints.add(acct.getCtdSpendPoints());
			ctdAdjPoints = ctdAdjPoints.add(acct.getCtdAdjPoints());
		}

		resp.setName(ccsCustomer.getName());
		resp.setPoint_begin_bal(pointsBegBal);
		resp.setCtd_earned_points(ctdEarnedPoints);
		resp.setPoint_bal(pointsBal.subtract(pointComm.countReducePoint(CcsAcctList.get(0))));
		resp.setCtd_disb_points(ctdDisbPoints.add(pointComm.countReducePoint(CcsAcctList.get(0))));
		resp.setCtd_adj_points(ctdAdjPoints);

		LogTools.printLogger(logger, "S17010", "积分查询", resp, false);

		return resp;

	}
}
