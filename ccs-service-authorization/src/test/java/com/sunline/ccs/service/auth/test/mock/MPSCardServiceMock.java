package com.sunline.ccs.service.auth.test.mock;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.sunline.ppy.api.MmCardService;
import com.sunline.ppy.dictionary.enums.CntType;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ppy.dictionary.exchange.ApplyFileItem;

public class MPSCardServiceMock implements MmCardService {

	@Override
	public String MS3101(String cardNbr) throws ProcessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Serializable> MS3102(String cardNbr) throws ProcessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Serializable>> MS3104(String cardNbr) throws ProcessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void MS3201(String cardNbr, boolean needPwdLetter) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MR3201(String cardNbr, boolean needPwdLetter) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3202(String cardNbr) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3202(List<String> cards) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3203(String cardNbr) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3203(List<String> cards) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3204(String cardNbr, String type) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3205(String cardNbr) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3206(String cardNbr) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3207(String cardNbr) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3208(String cardNbr, String password, String newPassword) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3209(String cardNbr, String password, String newPassword) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3210(String cardNbr, String encryptedPassword) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3211(String cardNbr, String encryptedPassword) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3212(String cardNbr) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3213(String cardNbr) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3301(String cardNbr, String newCardNo) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3302(String cardNbr, String newCardNo) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3303(String cardNbr) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean MS3501(String cardNbr) throws ProcessException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean MS3502(String cardNbr) throws ProcessException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean MS3503(String cardNbr) throws ProcessException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void MS3504(String cardNbr) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3505(String cardNbr) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3506(String cardNbr, String type) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3508(String cardNbr, String type) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3507(String cardNbr) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3509(String cardNbr) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public void MS3401(String cardNbr, String expiryDate, CntType cntType) throws ProcessException {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<CntType, Boolean> MS3105(String cardNbr, String expiryDate) throws ProcessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void MS3220(String arg0, String arg1) throws ProcessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<CntType, Integer> MS3106(String cardNbr, String expiryDate)
			throws ProcessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void MS3304(String cardNbr, String changeReson, Indicator urgentFlg,
			Indicator feeInd, String newCardNo) throws ProcessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void MS3305(String cardNbr, String changeReson, Indicator urgentFlg,
			Indicator feeInd, String newCardNo) throws ProcessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setErrCnt(CntType cntType, String cardNbr, Date expiryDate)
			throws ProcessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String MS3306(ApplyFileItem afi) throws Exception {
	    // TODO Auto-generated method stub
	    return null;
	}

}
