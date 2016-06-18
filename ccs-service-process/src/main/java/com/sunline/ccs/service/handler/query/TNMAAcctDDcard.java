package com.sunline.ccs.service.handler.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.SunshineCommService;
import com.sunline.ccs.service.msentity.TNMAAcctDDcardReq;
import com.sunline.ccs.service.msentity.TNMAAcctDDcardResp;
import com.sunline.ccs.service.msentity.TNMAAcctDDcardRespSubContrNbr;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 美借修改银行卡信息
 * 	银行卡修改为合同级别，即只修改该合同对应账户银行卡
 * @author ymk
 *
 */
@Service
public class TNMAAcctDDcard {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private AppapiCommService appapiCommService;
	@Autowired
	private SunshineCommService sunshineCommService;
	@Autowired
	private RCcsAcct rCcsAcct;
	@Autowired
	private RCcsCustomer rCcsCustomer;
	
	public TNMAAcctDDcardResp handler(TNMAAcctDDcardReq req) throws ProcessException {
		LogTools.printLogger(log, "TNMAAcctDDcardReq", "修改银行卡信息", req, true);
		LogTools.printObj(log, req, "请求参数TNMAAcctDDcardReq");
		TxnInfo txnInfo = new TxnInfo();
		
		if(req.getUuid() == null && req.getContractNo() == null){
			throw new ProcessException(MsRespCode.E_1008.getCode(), MsRespCode.E_1008.getMessage());
		}
		
		TNMAAcctDDcardResp resp = new TNMAAcctDDcardResp();

		try{
			//检查开户行号
			sunshineCommService.checkBankCode(req.getDdBankBranch());

			QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
			QCcsCustomer qCust = QCcsCustomer.ccsCustomer;
			CcsCustomer customer = null;
			
			if(StringUtils.isNotBlank(req.getContractNo())){
//				CcsLoan loan = rCcsLoan.findOne(qCcsLoan.contrNbr.eq(req.getContractNo()));
				CcsAcct acct = rCcsAcct.findOne(qCcsAcct.contrNbr.eq(req.getContractNo()));
				if (null == acct) {
					throw new ProcessException(MsRespCode.E_1015.getCode(), MsRespCode.E_1015.getMessage());
				}
				// 账户锁定码包含"P" 销户
				if (StringUtils.isNotEmpty(acct.getBlockCode())
				 && acct.getBlockCode().toUpperCase().indexOf("P") > -1  ) {
					throw new ProcessException(MsRespCode.E_1014.getCode(), MsRespCode.E_1014.getMessage());
				}

				customer = rCcsCustomer.findOne(acct.getCustId());
			}else{   
				Iterator<CcsCustomer> customerIt = rCcsCustomer.findAll(qCust.internalCustomerId.eq(req.getUuid())).iterator();
//				Iterator<CcsCustomer> customerIt = rCcsCustomer.findAll(qCust.custId.eq(Long.parseLong(req.getUuid()))).iterator();
				if(customerIt.hasNext()){
					System.out.println("1");
					customer = customerIt.next();//FIXME 为啥会有多条。。。
					System.out.println("2");
				}
			}
			
			if (null == customer)
				throw new ProcessException(MsRespCode.E_1023.getCode(),MsRespCode.E_1023.getMessage());
			
			List<TNMAAcctDDcardRespSubContrNbr> subContrNbrList= new ArrayList<TNMAAcctDDcardRespSubContrNbr>();
			
			if(null==req.getContractNo()){
				
				Iterable<CcsAcct> acctIt = rCcsAcct.findAll(qCcsAcct.custId.in(customer.getCustId()));
				//更新客户的账户银行卡信息，返回合同列表
				List<String> contractNbrs = updateAcct(acctIt.iterator(), req);
				for(String contrNbr: contractNbrs){
					TNMAAcctDDcardRespSubContrNbr subContrNbr = new TNMAAcctDDcardRespSubContrNbr();
					subContrNbr.setContrNbr(contrNbr);
					subContrNbrList.add(subContrNbr);
				}
				rCcsAcct.save(acctIt);
			}else{
				CcsAcct acct = rCcsAcct.findOne(qCcsAcct.custId.eq(customer.getCustId()));
				log.info("更新账户[{}]银行卡信息",acct.getAcctNbr());
				acct.setDdBankAcctNbr(req.getNewBankAcctNbr());
				acct.setDdBankAcctName(req.getDdBankAcctName());
				acct.setDdBankBranch(req.getDdBankBranch());
				acct.setDdBankName(req.getDdBankName());
				acct.setDdBankProvince(req.getDdBankProvince());
				acct.setDdBankProvinceCode(req.getDdBankProvCode());
				acct.setDdBankCity(req.getDdBankCity());
				acct.setDdBankCityCode(req.getDdBankCityCode());
				TNMAAcctDDcardRespSubContrNbr subContrNbr = new TNMAAcctDDcardRespSubContrNbr();
				subContrNbr.setContrNbr(acct.getContrNbr());
				subContrNbrList.add(subContrNbr);
				rCcsAcct.save(acct);
			}
			
			
			resp.setContractList(subContrNbrList);
			resp.setUuid(customer.getInternalCustomerId());
			//保存账户更新
//			rCcsAcct.save(acct);
			
			txnInfo.setResponsCode(MsRespCode.E_0000.getCode());
			txnInfo.setResponsDesc(MsRespCode.E_0000.getMessage());
		}catch (ProcessException pe){
			if(log.isErrorEnabled())
				log.error(pe.getMessage(), pe);
			appapiCommService.preException(pe, pe, txnInfo);
		}catch (Exception e) {
			if(log.isErrorEnabled())
				log.error(e.getMessage(), e);
			appapiCommService.preException(e, null, txnInfo);
		}finally{
			LogTools.printLogger(log, "TNMAAcctDDcardResp", "修改银行卡信息", resp, false);
		}
		this.setResponse(resp, req, txnInfo);
		LogTools.printObj(log, resp, "返回信息TNMAAcctDDcardResp");
		return resp;
	}


	/**
	 * 如果合同号CONTR_NBR为空，更新账户下所有银行卡信息
	 * @param acctIt 
	 * @param req
	 * @return
	 */
	private List<String> updateAcct(Iterator<CcsAcct> acctIt, TNMAAcctDDcardReq req) {
		List<String> contrList = new ArrayList<String>();
		while(acctIt.hasNext()){
			CcsAcct acct = acctIt.next();
			log.info("更新账户[{}]银行卡信息",acct.getAcctNbr());
			//新的卡号
			acct.setDdBankAcctNbr(req.getNewBankAcctNbr());
			//开户人姓名
			acct.setDdBankAcctName(req.getDdBankAcctName());
			//开户行名称
			acct.setDdBankName(req.getDdBankName());
			//开户行code
			acct.setDdBankBranch(req.getDdBankBranch());
			//开户行所属省
			acct.setDdBankProvince(req.getDdBankProvince());
			//开户行所属省code
			acct.setDdBankProvinceCode(req.getDdBankProvCode());
			//开户行所属市
			acct.setDdBankCity(req.getDdBankCity());
			//开户行所属市code
			acct.setDdBankCityCode(req.getDdBankCityCode());
			
			contrList.add(acct.getContrNbr());
			
		}
		return contrList;
	}
	
	/**
	 * 组装响应报文
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(TNMAAcctDDcardResp resp, TNMAAcctDDcardReq req, TxnInfo txnInfo) {
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if(StringUtils.equals(MsRespCode.E_0000.getCode(), txnInfo.getResponsCode())){
			resp.setStatus("S");//交易成功
		}else{
			resp.setStatus("F");//交易失败
		}
	}

	/**
	 * 更新账户
	 * @param context
	 * @param req
	 */
/*	@Transactional
	private void updateAcct(TxnContext context,S10001AlterBankInfoReq req) {
		if(log.isDebugEnabled())
			log.debug("生成授权码--updateAcct");
		CcsAcct ccsAcct = context.getAccount();
		//新的卡号
		ccsAcct.setDdBankAcctNbr(req.getNewputpaycardid());
		//开户人姓名
		ccsAcct.setDdBankAcctName(req.getBankowner());
		//开户行名称
		ccsAcct.setDdBankName(req.getBankname());
		//开户行code
		ccsAcct.setDdBankBranch(req.getBankcode());
		//开户行所属省
		ccsAcct.setDdBankProvince(req.getBankprovince());
		//开户行所属省code
		ccsAcct.setDdBankProvinceCode(req.getBankprovincecode());
		//开户行所属市
		ccsAcct.setDdBankCity(req.getBankcity());
		//开户行所属市code
		ccsAcct.setDdBankCityCode(req.getBankcitycode());

		rCcsAcct.save(ccsAcct);
		
	}*/
	
	
}
