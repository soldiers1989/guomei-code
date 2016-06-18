package com.sunline.ccs.batch.tools;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunline.ark.support.cstruct.CStruct;
import com.sunline.ppy.dictionary.exchange.MsxfTranFlow;
import com.sunline.ppy.dictionary.exchange.TpsTranFlow;

public class FileItemTest {
	public static Logger logger = LoggerFactory.getLogger(FileItemTest.class);
	public static void main(String[] args) throws Exception {
		/*String[] ss = {
				"1114136|20180107|3091.62|SUCCESS|0|操作成功|0100|0|6282423987109378063|吴晓东|01|888899990020000012|北京市|北京市|预约结清",
				"1114137|20180107|3091.62||00101|系统处理中，请稍候查询|0100|0|6282423987109378063|吴晓东|01|888899990020000012|北京市|北京市|预约结清",
				"1114138|20180107|3091.62||00102|无效卡号|0100|0|6282423987109378063|吴晓东|01|888899990020000012|北京市|北京市|预约结清",
				"1114140|20180107|3091.62|SUCCESS|0|操作成功|0|6282423987109378063|吴晓东|01|888899990020000012|北京市|北京市|预约结清",
				"1114141|20180107|3091.62||00101|系统处理中，请稍候查询|0|6282423987109378063|吴晓东|01|888899990020000012|北京市|北京市|预约结清",
				"1114142|20180107|3091.62||00102|无效卡号|0100|0|6282423987109378063|吴晓东|01|888899990020000012|北京市|北京市|预约结清",
				"1114144|20180107|3091.62||00101|系统处理中，请稍候查询|0100|0|6282423987109378063|吴晓东|01|888899990020000012|北京市|北京市|预约结清",
				"1114145|20180107|3091.62||00102|无效卡号|0100|0|6282423987109378063|吴晓东|01|888899990020000012|北京市|北京市|预约结清"
		};
		for(int i=0; i<ss.length; i++){
			checkFileLine(ss[i],MsxfTranFlow.class);
		}*/
//		checkFilELINE(
//				"1114136|20180107|3091.62|SUCCESS|0|操作成功|0100|0|6282423987109378063|吴晓东|01|888899990020000012|北京市|北京市|预约结清"
//				,MSXFTRANFLOW.CLASS);
		checkFileLine(
				"000000000001DTBANK        0208480001  MS    6000007000000960   300000      156300000      15607041753591607041754005835365345          1382160023000000230000       马上                                                                                       0           0           00230000      99999999       "
				,TpsTranFlow.class);
//		checkFileLine(
//				"1114141|20180107|3091.62||00101|系统处理中请稍候查询|0|6282423987109378063|吴晓东|01|888899990020000012|北京市|北京市|预约结清"
//				,MsxfTranFlow.class);
		
//		printFileLineMsxfTranFlow();
//		printScale();
//		charset();
	}
	
	public static <T> void printLine(List<T> list, Class<T> clazz) {
		CStruct<T> cs = new CStruct<T>(clazz);
		for(T flow : list){
			System.out.println(cs.writeLine(flow, "|"));
		}
	}
	
	public static <T> T parseLine(String line, Class<T> clazz){
		CStruct<T> t = new CStruct<T>(clazz);
		T lineItem = t.parseLine(line.split("\\|"));
		return lineItem;
	}
	
	public static <T> String checkFileLine(String line, Class<T> clazz) throws Exception{
		T t = parseLine(line, clazz);
		String output = ReflectionToStringBuilder.toString(t, ToStringStyle.MULTI_LINE_STYLE);
		System.out.println(output);
		return output;
	}
	private static List<MsxfTranFlow> printFileLineMsxfTranFlow() {
		List<MsxfTranFlow> list = new ArrayList<MsxfTranFlow>();
		MsxfTranFlow f = new MsxfTranFlow();
		f.channelSerial = "1";
		f.txnAmt = new BigDecimal(200).setScale(2);
		f.cardNo="111111";
		f.msDdReturnCode = "00";
		f.returnMessage = "马上流水001";
		list.add(f);
		MsxfTranFlow f2 = new MsxfTranFlow();
		f2.channelSerial = "2";
		f2.txnAmt = new BigDecimal(200).setScale(2);
		f2.cardNo="222222";
		f2.msDdReturnCode = "00";
		f2.returnMessage = "马上流水002";
		list.add(f2);
		
		MsxfTranFlow f3 = new MsxfTranFlow();
		f3.channelSerial = "3";
		f3.txnAmt = new BigDecimal(200).setScale(2);
		f3.cardNo="333333";
		f3.msDdReturnCode = "03";
		f3.returnMessage = "马上流水003";
		list.add(f3);
		
		MsxfTranFlow f4 = new MsxfTranFlow();
		f4.channelSerial = "4";
		f4.txnAmt = new BigDecimal(200).setScale(2);
		f4.cardNo="444444";
		f4.msDdReturnCode = "03";
		f4.returnMessage = "马上流水003";
		list.add(f4);
		
		printLine(list, MsxfTranFlow.class);
		return list;
	}
	
	public static void printScale(){
		BigDecimal num = new BigDecimal("0.0560");
		System.out.println(num);
		System.out.println(num.equals(new BigDecimal("0.056")));
//		num = num.setScale(2);
//		System.out.println("scale:2:"+num);
		
//		num = num.setScale(2, RoundingMode.HALF_UP);
//		System.out.println("scale:2,halfUp:"+num);
		
	}
	
	public static void charset(){
		boolean isSupp = Charset.isSupported("GBK");
		System.out.println(isSupp);
	}
}
