package com.sunline.ccs.service.protocol;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sunline.ppy.dictionary.enums.Indicator;

/**
 * 积分明细查询
 * 
* @author fanghj
 * @date 2013-4-20  上午11:15:46
 * @version 1.0
 */
public class S17011Resp implements Serializable {

	private static final long serialVersionUID = 3279763311918500789L;
	
	/**
     * 卡号
     */
    public String card_no ;

    /**
     * 开始位置
     */
    public Integer firstrow ;

    /**
     * 结束位置
     */
    public Integer lastrow ;
    
    /**
     * 是否有下页标志
     */
    public Indicator nextpage_flg;
    
    public ArrayList<S17011TxnPoint> txn_points;
    
    public Integer total_rows;

    public Integer getTotal_rows() {
    		return total_rows;
    	}

    	public void setTotal_rows(Integer total_rows) {
    		this.total_rows = total_rows;
    	}
    

	public ArrayList<S17011TxnPoint> getTxn_points() {
		return txn_points;
	}

	public void setTxn_points(ArrayList<S17011TxnPoint> txn_points) {
		this.txn_points = txn_points;
	}

	public Indicator getNextpage_flg() {
		return nextpage_flg;
	}

	public void setNextpage_flg(Indicator nextpage_flg) {
		this.nextpage_flg = nextpage_flg;
	}

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
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

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
    
}

