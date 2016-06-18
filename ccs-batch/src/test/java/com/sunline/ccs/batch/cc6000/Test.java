package com.sunline.ccs.batch.cc6000;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.sunline.ccs.facility.DateUtils;


public class Test {

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date from = df.parse("2015-05-13 23:00:00");
		Date to = df.parse("2015-05-14 01:00:00");
		long interval = to.getTime()-from.getTime();
		int result = (int)(interval/(1000*3600*24));
		System.out.println(result);
		
		String s = "1232141/dfaer/dsfaf[false]";
		if(s.endsWith("[false]")){
			System.out.println("[false]");
		}
		
		
//		String ip = "10.16.18.19";
//		int port = 8888;
//		GetJsonForQeq g = new GetJsonForQeq();
//		String xml=g.getYGJosnInfo();
////		String xml="json";
//		String rep="";
//		String urlStr="http://"+ip+":"+port;
//		System.out.println("url：" + urlStr);
//		System.out.println("req: " + xml);
//		try {
//			URL url = new URL(urlStr);
//			URLConnection con = url.openConnection();
//			con.setDoOutput(true);
//			con.setRequestProperty("Pragma:", "no-cache");
//			con.setRequestProperty("Cache-Control", "no-cache");
//			con.setRequestProperty("Content-Type", "application/json");
//
//			OutputStreamWriter out = new OutputStreamWriter(con
//					.getOutputStream());
//			
//			long bc = System.currentTimeMillis();
//			out.write(new String(xml.getBytes("UTF-8")));
//			out.flush();
//			out.close();
//		
//			BufferedReader br = new BufferedReader(new InputStreamReader(con
//					.getInputStream(),"UTF-8"));
//			String line = "";
//			for (line = br.readLine(); line != null; line = br.readLine()) {
//				rep = rep +line;
//				System.out.println(line);
//			}
//			long cc = System.currentTimeMillis();
//		    System.out.println("耗时:" + (bc-cc));
//			br.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//			rep="reperror";
//		}   
		BigDecimal bal = new BigDecimal(1000);
		BigDecimal rate = new BigDecimal(1000);
		BigDecimal bd = new BigDecimal(1.0/360).setScale(20,RoundingMode.HALF_UP);
		System.out.println(bd);
		bd = bd.multiply(bal).multiply(rate).multiply(BigDecimal.valueOf(1));
		
		bal = new BigDecimal(1000);
		rate = new BigDecimal(1100);
		BigDecimal bds = bal.compareTo(rate)>0?bal:rate;
		System.out.println(bds);
		
		
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date nextStmtDate = sdf.parse("2015-01-29");
			Calendar c = Calendar.getInstance();
			c.setTime(nextStmtDate);
			int nextStmtDay = c.get(Calendar.DAY_OF_MONTH);
			Date stmtDay = DateUtils.addMonths(nextStmtDate, 1);
			c.add(Calendar.MONTH, 1); 
			c.set(Calendar.DAY_OF_MONTH,2);
			
			System.out.println(nextStmtDay);
			System.out.println(sdf.format(stmtDay));
			System.out.println(sdf.format(c.getTime()));
	}

}
