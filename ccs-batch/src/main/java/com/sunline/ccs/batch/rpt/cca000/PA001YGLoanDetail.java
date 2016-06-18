package com.sunline.ccs.batch.rpt.cca000;

import java.math.RoundingMode;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.sdk.BatchStatusFacility;
import com.sunline.ccs.batch.rpt.cca000.items.YGLoanDetailItem;
import com.sunline.ccs.infrastructure.shared.model.CcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoanReg;
import com.sunline.ccs.infrastructure.shared.model.QCcsOrder;
import com.sunline.ppy.dictionary.enums.LoanStatus;

/**
 * 送阳光-贷款明细文件
 * @author wanghl
 *
 */
public class PA001YGLoanDetail implements ItemProcessor<SA001YGLoanDetailInfo, YGLoanDetailItem>{
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private BatchStatusFacility batchStatusFacility;
	
	private QCcsOrder qOrder = QCcsOrder.ccsOrder;

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Override
	public YGLoanDetailItem process(SA001YGLoanDetailInfo info) throws Exception {
		YGLoanDetailItem fileItem = new YGLoanDetailItem();
		CcsLoan loan = info.getLoan();
		CcsLoanReg reg = info.getLoanReg();
		CcsCustomer cust = info.getCustomer();
		if(loan != null){
			logger.info("账户："+loan.getAcctNbr()+"账户类型："+loan.getAcctType()+"借据号："+loan.getDueBillNo());
		}
		if(reg != null){
			logger.info("账户："+reg.getAcctNbr()+"账户类型："+reg.getAcctType()+"借据号："+reg.getDueBillNo());
		}
		fileItem.customeName = cust.getName();
		fileItem.certType = SA000ItemUtil.getYgIdType(cust.getIdType());
		fileItem.certID = cust.getIdNo();
		fileItem.dpsacct = info.getDdBankAcctNbr();
		
		if(info.getIsLoanEstablished()){
			getLoanInfo(fileItem, loan);
		}else{
			getRegInfo(fileItem, reg);
		}
		
		return fileItem;
	}

	/**
	 * @param fileItem
	 * @param reg
	 */
	private void getRegInfo(YGLoanDetailItem fileItem, CcsLoanReg reg) {
		fileItem.guarantyID = reg.getGuarantyId();
		fileItem.contractNo = reg.getContrNbr();
		fileItem.putOutNo = reg.getDueBillNo();
		fileItem.putOutDate = null;
		fileItem.businessSum = reg.getLoanInitPrin().setScale(2, RoundingMode.HALF_UP); 
		fileItem.loanTerm = reg.getLoanInitTerm();  
		fileItem.businessRate = reg.getInterestRate(); 
		
		switch (reg.getLoanRegStatus()) {
		case C:
			fileItem.status = "02";
			break;
		case F:
			fileItem.status = "01";
			String code = getOrderCode(reg);
			if("00206".equals(code))
				fileItem.reanson="00";
			else
				fileItem.reanson = ""; 
			break;

		default:
			break;
		}
		
		fileItem.fineRateType = "0"; //FIXME 罚息利率方式 获取
		fileItem.fineRateFloat = "0";//没有罚息浮动比率 设0
		
		fileItem.finishType = "";
		fileItem.finishDate = null;
	}

	private String getOrderCode(CcsLoanReg reg) {
		String code = new JPAQuery(em).from(qOrder)
				.where(qOrder.dueBillNo.eq(reg.getDueBillNo())
						.and(qOrder.optDatetime.eq(batchStatusFacility.getBatchDate())))
				.singleResult(qOrder.code);
		return code;
	}

	/**
	 * @param fileItem
	 * @param loan
	 */
	private void getLoanInfo(YGLoanDetailItem fileItem, CcsLoan loan) {
		fileItem.guarantyID = loan.getGuarantyId();
		fileItem.contractNo = loan.getContrNbr();
		fileItem.putOutNo = loan.getDueBillNo();
		fileItem.putOutDate = loan.getActiveDate(); //FIXME 放款时间 获取
		fileItem.businessSum = loan.getLoanInitPrin().setScale(2); 
		fileItem.loanTerm = loan.getLoanInitTerm();  
		fileItem.businessRate = loan.getInterestRate(); 
		fileItem.fineRateType = "0"; //FIXME 罚息利率方式 获取
		fileItem.fineRateFloat = "0";//没有罚息浮动比率 设0
		fileItem.maturityDate = loan.getLoanExpireDate();
		//结清终止的贷款
		if(loan.getLoanStatus().equals(LoanStatus.T)){
			logger.info("结清终止loanId[{}]借据[{}]",loan.getTerminalReasonCd(),loan.getDueBillNo());
			switch (loan.getTerminalReasonCd()) {
			case P :
				fileItem.finishType = "02";
				fileItem.finishDate = loan.getPaidOutDate();
				break;
			case C :
				fileItem.finishType = "03";
				fileItem.finishDate = loan.getPaidOutDate();
				break;
			default:
				break;
			}
			fileItem.status = null;
			
		}else if(loan.getLoanStatus().equals(LoanStatus.F)){
			fileItem.finishType = "01";
			fileItem.finishDate = loan.getPaidOutDate();
		}else {//新生成 的贷款
			logger.info("新贷款[{}]借据[{}]",loan.getLoanId(),loan.getDueBillNo());
			fileItem.status = "00";
			fileItem.finishType = "";
			fileItem.finishDate = null;
		}
	}


}
