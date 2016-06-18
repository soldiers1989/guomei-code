/**
 * 
 */
package com.sunline.ccs.loan;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.ccs.param.def.ProductCredit;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;

/**
 * @author lizz
 *
 */
@Service
public class LoanUtil {
	
	@PersistenceContext
    private EntityManager em;
	
	@Autowired
	private UnifiedParameterFacility unifiedParameterService;
	
	public String findLoanCode(CcsAcct acct) {
		QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
		String loanCode = "";
		CcsLoan ccsLoan =  new JPAQuery(em).from(qCcsLoan).where(qCcsLoan.contrNbr.eq(acct.getContrNbr())).singleResult(qCcsLoan);
		if(ccsLoan != null){
			loanCode = ccsLoan.getLoanCode();
		}else {
			ProductCredit product = unifiedParameterService.retrieveParameterObject(acct.getProductCd(), ProductCredit.class);
			if(null != product.loanPlansMap && product.loanPlansMap.size() > 1) {
				loanCode = product.loanPlansMap.get(product.defaultLoanType);
				if(loanCode == null) {
					throw new IllegalArgumentException("产品【"+product.productCd+"】未设置产品默认分期类型");
				}
			}else {
				throw new IllegalArgumentException("请检查产品参数【分期产品】是否正确设置！");
			}
		}
		return loanCode;
	}

}
