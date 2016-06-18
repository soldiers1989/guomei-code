package com.sunline.ccs.batch.cc9100;

import java.util.ArrayList;
import java.util.List;

import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsPlan;
import com.sunline.ccs.infrastructure.shared.model.CcsRepaySchedule;
/**
 * @see 类名：S9102PBOC
 * @see 描述：TODO 中文描述
 *
 * @see 创建日期：   2015-6-24下午2:48:26
 * @author ChengChun
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
public class S9102PBOC {
	private CcsAcct acct;
	private CcsLoan tmLoan;
	private List<CcsPlan> plans = new ArrayList<CcsPlan>();
	private List<CcsRepaySchedule> schedules = new ArrayList<CcsRepaySchedule>();
	public CcsLoan getTmLoan() {
		return tmLoan;
	}
	public void setTmLoan(CcsLoan tmLoan) {
		this.tmLoan = tmLoan;
	}
	public List<CcsPlan> getPlans() {
		return plans;
	}
	public void setPlans(List<CcsPlan> plans) {
		this.plans = plans;
	}
	public CcsAcct getAcct() {
		return acct;
	}
	public void setAcct(CcsAcct acct) {
		this.acct = acct;
	}
	public List<CcsRepaySchedule> getSchedules() {
		return schedules;
	}
	public void setSchedules(List<CcsRepaySchedule> schedules) {
		this.schedules = schedules;
	}
}
