package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ccs.param.def.enums.TxnType;

/**
 *贷款还款历史查询
* @author fanghj
 *@time 2014-4-1 上午11:03:52
 */
public class S20060Resp implements Serializable{

	private static final long serialVersionUID = 2001133348054393793L;
	/**
     * 是否有下一页标志
     */
	public Indicator nextpage_flg;

    /**
     * 开始位置
     */
    public Integer firstrow;

    /**
     * 结束位置
     */
    public Integer lastrow;

    /**
     * 贷款卡号
     */
    public String card_no;

    /**
     * 起始日期
     */
    public Date start_date;

    /**
     * 截止日期
     */
    public Date end_date;

    /**
     * 交易类型
     */
    public TxnType sett_txn_type;

    public ArrayList<S20060Txn> txns;
    
	public Indicator getNextpage_flg() {
		return nextpage_flg;
	}

	public void setNextpage_flg(Indicator nextpage_flg) {
		this.nextpage_flg = nextpage_flg;
	}

	public Integer getFirstrow() {
		return firstrow;
	}

	public void setFirstrow(Integer firstrow) {
		this.firstrow = firstrow;
	}

	public Integer getLastrow() {
		return lastrow;
	}

	public void setLastrow(Integer lastrow) {
		this.lastrow = lastrow;
	}

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public Date getStart_date() {
		return start_date;
	}

	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}

	public Date getEnd_date() {
		return end_date;
	}

	public void setEnd_date(Date end_date) {
		this.end_date = end_date;
	}

	public TxnType getSett_txn_type() {
		return sett_txn_type;
	}

	public void setSett_txn_type(TxnType sett_txn_type) {
		this.sett_txn_type = sett_txn_type;
	}

	public ArrayList<S20060Txn> getTxns() {
		return txns;
	}

	public void setTxns(ArrayList<S20060Txn> txns) {
		this.txns = txns;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
