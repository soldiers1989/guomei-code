package com.sunline.ccs.batch.tools;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.sunline.ppy.dictionary.exchange.MsxfMerchantTranFlow;

public class RamdomFileLine {

	@Test
	public void process() throws IllegalAccessException{
		MsxfMerchantTranFlow r1 = new MsxfMerchantTranFlow();
		MakeData.setDefaultValue(r1);
		
		List<MsxfMerchantTranFlow> list = new ArrayList<MsxfMerchantTranFlow>();
		list.add(r1);
		FileItemTest.printLine(list , MsxfMerchantTranFlow.class);
	}
}
