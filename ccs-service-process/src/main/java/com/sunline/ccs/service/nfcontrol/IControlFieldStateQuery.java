package com.sunline.ccs.service.nfcontrol;

import com.sunline.ppy.dictionary.exception.ProcessException;

public interface IControlFieldStateQuery {

	public boolean process(String cardNbr) throws ProcessException;

}
