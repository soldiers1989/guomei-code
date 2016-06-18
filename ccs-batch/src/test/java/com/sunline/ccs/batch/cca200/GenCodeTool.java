package com.sunline.ccs.batch.cca200;

import java.util.ArrayList;
import java.util.List;

import com.sunline.ppy.dictionary.exchange.MsxfMerchantTranFlow;


public class GenCodeTool {
	public static void main(String[] args) {
		EVRGenTestTool.methodString(MsxfMerchantTranFlow.class, "item", "");
//		genResource();
	}
	
	public static void genResource(){
		String[][] ss = {
				{"211", "MsLoanRpt","马上贷放款结果查询报表"},
				{"212", "MsLoanRepayRpt","马上贷还款结果查询报表"},
				{"213", "MsLoanBalanceRpt","马上贷贷款余额查询报表"},
				{"221", "MCATLoanRpt","随借随还放款结果查询报表"},
				{"222", "MCATLoanRepayRpt","随借随还还款结果查询报表"},
				{"223", "MCATLoanBalanceRpt","随借随还贷款余额查询报表"}
		};
		List<RscDesc> list = new ArrayList<RscDesc>();
		for(String[] s : ss){
			RscDesc r = new RscDesc();
			r.id = s[0];
			r.name = s[1];
			r.desc = s[2];
			r.resHead = "resA";
			r.stepHead = "cca";
			list.add(r);
		}
		for(RscDesc s : list){
			System.out.println("	<bean id=\""+ s.resHead+s.id +"\" class=\"com.sunline.ark.batch.ResourceFactoryBean\" scope=\"step\">");
			System.out.println("		<description>"+ s.desc +"</description>");
			System.out.println("		<property name=\"resource\" value=\"file:#{env.batchWorkDir}/#{new java.text.SimpleDateFormat('yyyyMMdd').format(batchStatusFacility.getBatchDate())}/#{env.msUploadDir}/"+ s.name +"\" />");
			System.out.println("	</bean>");
		}
		for(RscDesc s : list){
			System.out.println("	<batch:step id=\""+s.stepHead+s.id+s.name+"\" next=\""+s.stepHead+s.id+s.name+"\">");
			System.out.println("		<batch:description>"+s.desc+"</batch:description>");
			System.out.println("		<batch:tasklet>");
			System.out.println("			<batch:chunk reader=\"ra"+s.id+"\" writer=\"wa"+s.id+"\"  commit-interval=\"#{env['commitInterval'] ?: 100}\" />");
			System.out.println("		</batch:tasklet>");
			System.out.println("	</batch:step>");
		}
				
	}
}
class RscDesc{
	public String id;
	public String name;
	public String desc;
	public String resHead;
	public String stepHead;
}
