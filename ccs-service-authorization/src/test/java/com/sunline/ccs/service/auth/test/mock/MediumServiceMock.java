package com.sunline.ccs.service.auth.test.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunline.ppy.api.MediumInfo;
import com.sunline.ppy.api.MediumService;
import com.sunline.ppy.dictionary.enums.PasswordType;
import com.sunline.ppy.dictionary.enums.PasswordVerifyResult;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ark.support.service.YakMessage;

/**
 * 模拟MPS服务. 理论上要放在mps-service-sdk中的以便于其它系统使用，暂时放在这里。
* @author fanghj
 *
 */
public class MediumServiceMock implements MediumService {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private MediumInfo mediumInfo;

	/**
	 * 这里只简单把预置的 MediumInfo 对象返回 
	 */
	@Override
	public MediumInfo verifyAuthMedium(YakMessage message) {
		logger.debug("获取MediumInfo对象");
		return mediumInfo;
	}

	public MediumInfo getMediumInfo() {
		return mediumInfo;
	}

	/**
	 * 设置 {@link #verifyAuthMedium(YakMessage)}的返回值
	 */
	public void setMediumInfo(MediumInfo mediumInfo) {
		this.mediumInfo = mediumInfo;
	}

	@Override
	public boolean isValidCvv2(String arg0, String arg1, String arg2)
			throws ProcessException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValidExpiryDate(String arg0, String arg1)
			throws ProcessException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValidQueryPin(String arg0, String arg1)
			throws ProcessException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValidTransPin(String arg0, String arg1)
			throws ProcessException {
		// TODO Auto-generated method stub
		return false;
	}
}
