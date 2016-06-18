package com.sunline.ccs.batch.cc3000.cancle;

import com.sunline.ccs.infrastructure.shared.model.CcsLoan;

public interface AutoCancle {
	//停止计息费锁定码
	public static final String I_CODE = "I";
	void cancle(CcsLoan loan);
	
}
