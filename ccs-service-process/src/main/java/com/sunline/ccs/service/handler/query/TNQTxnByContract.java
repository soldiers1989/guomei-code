package com.sunline.ccs.service.handler.query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.CcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.CcsTxnHst;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcctO;
import com.sunline.ccs.infrastructure.shared.model.QCcsAuthmemoO;
import com.sunline.ccs.infrastructure.shared.model.QCcsTxnHst;
import com.sunline.ccs.param.def.enums.AuthTransDirection;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.QueryCommService;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.msentity.TNQTxnByContractReq;
import com.sunline.ccs.service.msentity.TNQTxnByContractResp;
import com.sunline.ccs.service.msentity.TNQContractTxnQueryRespInfo;
import com.sunline.ccs.service.util.EnumInfoUtils;
import com.sunline.ppy.dictionary.enums.AuthTransStatus;
import com.sunline.ppy.dictionary.enums.AuthTransType;
import com.sunline.ppy.dictionary.enums.DbCrInd;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 按合同号交易查询（包含入账和未入账）
 * @author wanghl
 *
 */
@Service
public class TNQTxnByContract{
	public Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private QueryCommService queryCommService;
	@Autowired
	AppapiCommService appapiCommService;
	@PersistenceContext
	private EntityManager em;
	private QCcsAcct qAcct = QCcsAcct.ccsAcct;
	private QCcsAcctO qAccto = QCcsAcctO.ccsAcctO;
	private QCcsAuthmemoO qAuthMemoO = QCcsAuthmemoO.ccsAuthmemoO;
	private QCcsTxnHst qTxnHst = QCcsTxnHst.ccsTxnHst;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	private SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyyMMddHHmmss");
	
	public TNQTxnByContractResp handler(TNQTxnByContractReq req) {
		TNQTxnByContractResp resp = new TNQTxnByContractResp();
		List<TNQContractTxnQueryRespInfo> txnList = new ArrayList<TNQContractTxnQueryRespInfo>();
		TxnInfo txnInfo = new TxnInfo();
		
		this.checkReq(req);
		
		resp.setTxnList(txnList);
		resp.setTxnCount(0L);
		resp.setContrNbr(req.getContraNbr());
		resp.setPageSize(req.getPageSize());
		
		try {
			//获取账户
			CcsAcct acct = new JPAQuery(em).from(qAcct).where(qAcct.contrNbr.eq(req.getContraNbr())).singleResult(qAcct);
			if(acct == null) throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());
			
			CcsAcctO accto = new JPAQuery(em).from(qAccto)
					.where(qAccto.acctNbr.eq(acct.getAcctNbr()).and(qAccto.acctType.eq(acct.getAcctType())))
					.singleResult(qAccto);
			if(accto == null) throw new ProcessException(MsRespCode.E_1003.getCode(), MsRespCode.E_1003.getMessage());

			//批处理中检查
			queryCommService.batchProcessingCheck(acct, accto);
			
			//筛选条件 - 交易类型
			BooleanExpression authTransExp = 
					qAuthMemoO.authTxnStatus.in(
							AuthTransStatus.N, 
							AuthTransStatus.O, 
							AuthTransStatus.D, 
							AuthTransStatus.P); 
			//筛选条件 - 交易方向
			authTransExp = authTransExp.and(
					qAuthMemoO.txnDirection.in(
							AuthTransDirection.Normal, 
							AuthTransDirection.Confirm
							)
					);
			BooleanExpression txnHstExp = qTxnHst.dbCrInd.notIn(DbCrInd.M);
			
			//筛选条件 - 账户
			authTransExp = authTransExp.and(qAuthMemoO.acctNbr.eq(acct.getAcctNbr())
					.and(qAuthMemoO.acctType.eq(acct.getAcctType())));
			
			txnHstExp = txnHstExp.and(qTxnHst.acctNbr.eq(acct.getAcctNbr())
					.and(qTxnHst.acctType.eq(acct.getAcctType())));
			
			//筛选条件 - 可选的开始结束时间限制
			if(req.getStartTime() != null){
				authTransExp = authTransExp.and(qAuthMemoO.requestTime.goe(sdf.format(req.getStartTime())));
				txnHstExp = txnHstExp.and(qTxnHst.postDate.goe(req.getStartTime()));
			}
			if(req.getEndTime() != null){
				if(req.getStartTime() != null && req.getStartTime().after(req.getEndTime())){
					throw new ProcessException(MsRespCode.E_1043.getCode(), "起始日期不能大于结束日期");
				}
				Calendar cldEndTime = Calendar.getInstance();
				cldEndTime.setTime(req.getEndTime());
				cldEndTime.add(Calendar.DAY_OF_MONTH, 1);
				authTransExp = authTransExp.and(qAuthMemoO.requestTime.lt(sdf.format(cldEndTime.getTime()) ));
				txnHstExp = txnHstExp.and(qTxnHst.postDate.loe(req.getEndTime()));
			}
			
			//分页 - 跳过的交易记录
			long offset = req.getPageSize() * (req.getPagePosition()-1);
			long pageSize = req.getPageSize();
			
			//未入账交易总数量 - 未入账交易放在结果列表前面
			long authMemoCount = new JPAQuery(em).from(qAuthMemoO).where(authTransExp).count();
			long txnHstCount = new JPAQuery(em).from(qTxnHst).where(txnHstExp).count();
			
			List<CcsAuthmemoO> authMemoOList = null;
			List<CcsTxnHst> txnHstList = null;
			
			if(offset + pageSize <= authMemoCount){
				
				authMemoOList = queryAuthMemo(offset, pageSize, authTransExp);
				
			}else if(offset < authMemoCount && authMemoCount < offset + pageSize ){
				
				authMemoOList = queryAuthMemo(offset, pageSize, authTransExp);
				
				txnHstList = queryTxnHst(0, pageSize - authMemoOList.size(), txnHstExp);
				
			}else{
				
				txnHstList = queryTxnHst(offset - authMemoCount, pageSize, txnHstExp);
				
			}
			addToRespTxnList(txnList, authMemoOList, txnHstList);

			resp.setErrorCode(MsRespCode.E_0000.name());
			resp.setErrorMessage(MsRespCode.E_0000.getMessage());
			resp.setTxnCount(txnHstCount + authMemoCount);
		} catch (ProcessException pe) {
			appapiCommService.preException(pe, pe, txnInfo);
			logger.error(pe.getMessage(), pe);
		} catch (Exception e){
			appapiCommService.preException(e, null, txnInfo);
			logger.error(e.getMessage(), e);
		}
		setResponse(resp, txnInfo);
		return resp ;
	}
	private void checkReq(TNQTxnByContractReq req) {
		if(req.getPagePosition() <= 0) throw new ProcessException(MsRespCode.E_1043.getCode(), "显示页数需大于0");
		if(req.getPageSize() <= 0) throw new ProcessException(MsRespCode.E_1043.getCode(), "页大小需大于0");
		if(req.getPageSize() > 50) throw new ProcessException(MsRespCode.E_1043.getCode(), "页大小最大50");
	}
	private void setResponse(MsResponseInfo resp, TxnInfo txnInfo) {
		
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
	}
	
	private List<CcsTxnHst> queryTxnHst(long offset, long pageSize,
			BooleanExpression txnHstExp) {
		return new JPAQuery(em).from(qTxnHst).where(txnHstExp)
				.offset(offset).limit(pageSize).orderBy(qTxnHst.postDate.desc()).orderBy(qTxnHst.txnSeq.desc()).list(qTxnHst);
	}

	private List<CcsAuthmemoO> queryAuthMemo(long offset, long pageSize,
			BooleanExpression authTransExp) {
		return new JPAQuery(em).from(qAuthMemoO).where(authTransExp)
				.offset(offset ).limit(pageSize).orderBy(qAuthMemoO.requestTime.desc()).orderBy(qAuthMemoO.logKv.desc()).list(qAuthMemoO);
	}

	private Integer addToRespTxnList(
			List<TNQContractTxnQueryRespInfo> txnList,
			List<CcsAuthmemoO> authMemoOList, 
			List<CcsTxnHst> txnHstList) {
		//返回记录数
		int count = 0;
		if(authMemoOList != null)
			for(CcsAuthmemoO o : authMemoOList){
				TNQContractTxnQueryRespInfo info = new TNQContractTxnQueryRespInfo();
				info.setMatchInd(Indicator.N);
				info.setProcDate(null);
				info.setTxnAmt(o.getTxnAmt().toString());
				
				String txnDesc = EnumInfoUtils.getEnumInfoFromClass(AuthTransType.class).get(o.getTxnType().name());
				if (txnDesc == null ) txnDesc = "";
				info.setTxnDesc(txnDesc );
				
				info.setTxnTime(o.getRequestTime());
				info.setAuthTransStatus(o.getAuthTxnStatus());
				info.setTxnCode(null);
				txnList.add(info);
				count ++;
			}
		
		if(txnHstList != null)
			for(CcsTxnHst t : txnHstList){
				TNQContractTxnQueryRespInfo info = new TNQContractTxnQueryRespInfo();
				info.setMatchInd(Indicator.Y);
				info.setProcDate(sdf.format(t.getPostDate()));
				info.setTxnAmt(t.getPostAmt().toString());
				info.setTxnDesc(t.getTxnDesc());
				info.setTxnTime(sdfDateTime.format(t.getTxnTime()));
				info.setAuthTransStatus(null);
				info.setTxnCode(t.getTxnCode());
				txnList.add(info);
				count ++;
			}
		return count;
	}

}
