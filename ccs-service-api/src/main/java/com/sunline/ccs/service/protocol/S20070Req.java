package com.sunline.ccs.service.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 预销户
* @author fanghj
 * @time 2014-3-26 下午2:23:35
 */
public class S20070Req implements Serializable {

	private static final long serialVersionUID = 5127130489160199042L;

	/**
     * 贷款卡号
     */
    public String card_no;

    /**
     * 操作码
     */
    public String opt;

	public String getCard_no() {
		return card_no;
	}

	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	public String getOpt() {
		return opt;
	}

	public void setOpt(String opt) {
		this.opt = opt;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}

