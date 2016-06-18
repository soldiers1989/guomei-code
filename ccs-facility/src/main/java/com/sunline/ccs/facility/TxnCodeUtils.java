package com.sunline.ccs.facility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.pcm.service.sdk.UnifiedParameterFacility;
import com.sunline.ccs.param.def.TxnCd;

/**
 * 获取出账单对应的交易码信息
* @author fanghj
 * 
 * 
 * */
@Service
public class TxnCodeUtils {
	@Autowired
	private UnifiedParameterFacility unifiedParameterService; 
	
	/**
	  * @return List<String>
      * @throws ProcessException
      */
	public  List<String> getTxnCode() throws ProcessException {

		Map<String, TxnCd> txnCdMap = unifiedParameterService.retrieveParameterObject(TxnCd.class);
		ArrayList<String> arrayList = new ArrayList<String>();
		Collection<TxnCd> txnValues = txnCdMap.values();
		for (TxnCd txnCd : txnValues) {
			if (txnCd != null && txnCd.stmtInd != null && txnCd.stmtInd) {
				arrayList.add(txnCd.txnCd);
			}
		}

		return arrayList;

	}
}
