package com.sunline.ccs.batch.tools;

import com.sunline.ccs.batch.utils.CheckFile;
import com.sunline.ppy.dictionary.exchange.TpsTranFlow;

public class PrintFileLine {

	public static void main(String[] args) throws Exception {
		
		String line = "000000000001DTBANK        0208480001  MS    6000007000000960   300000      156300000      15607041753591607041754005835365345          1382160023000000230000       马上                                                                                       0           0           00230000      99999999       ";
		checkLine(TpsTranFlow.class, line);
	}
	
	public static <T> void checkLine(Class<T> t, String line) throws Exception{
		CheckFile<T> ck = new CheckFile<T>();
		ck.check(line, t);
	}
}
