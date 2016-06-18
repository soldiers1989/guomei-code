package com.sunline.ccs.service.api;

import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.service.protocol.S20001Req;
import com.sunline.ccs.service.protocol.S20001Resp;
import com.sunline.ccs.service.protocol.S20010Req;
import com.sunline.ccs.service.protocol.S20010Resp;
import com.sunline.ccs.service.protocol.S20011Req;
import com.sunline.ccs.service.protocol.S20011Resp;
import com.sunline.ccs.service.protocol.S20014Req;
import com.sunline.ccs.service.protocol.S20014Resp;
import com.sunline.ccs.service.protocol.S20020Req;
import com.sunline.ccs.service.protocol.S20020Resp;
import com.sunline.ccs.service.protocol.S20021Req;
import com.sunline.ccs.service.protocol.S20021Resp;
import com.sunline.ccs.service.protocol.S20022Req;
import com.sunline.ccs.service.protocol.S20022Resp;
import com.sunline.ccs.service.protocol.S20023Req;
import com.sunline.ccs.service.protocol.S20023Resp;
import com.sunline.ccs.service.protocol.S20024Req;
import com.sunline.ccs.service.protocol.S20024Resp;
import com.sunline.ccs.service.protocol.S20025Req;
import com.sunline.ccs.service.protocol.S20025Resp;
import com.sunline.ccs.service.protocol.S20026Req;
import com.sunline.ccs.service.protocol.S20026Resp;
import com.sunline.ccs.service.protocol.S20027Req;
import com.sunline.ccs.service.protocol.S20030Req;
import com.sunline.ccs.service.protocol.S20030Resp;
import com.sunline.ccs.service.protocol.S20040Req;
import com.sunline.ccs.service.protocol.S20040Resp;
import com.sunline.ccs.service.protocol.S20050Req;
import com.sunline.ccs.service.protocol.S20050Resp;
import com.sunline.ccs.service.protocol.S20060Req;
import com.sunline.ccs.service.protocol.S20060Resp;
import com.sunline.ccs.service.protocol.S20070Req;
import com.sunline.ccs.service.protocol.S20080Req;
import com.sunline.ccs.service.protocol.S20080Resp;
import com.sunline.ccs.service.protocol.S20090Req;
import com.sunline.ccs.service.protocol.S20090Resp;

/**
 * 小额贷新增的非金融部分
* @author fanghj
 *
 */
public interface MicroCreditService {
	
	/**
	 * 贷款产品信息查询
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S20001Resp S20001(S20001Req req) throws ProcessException;
	
	/**
	 * 贷款产品定价信息查询
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S20010Resp S20010(S20010Req req) throws ProcessException;
	
	/**
	 * 提前全额还款
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S20011Resp S20011(S20011Req req) throws ProcessException;
	
	/**
	 * 贷款账户信息查询
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S20014Resp S20014(S20014Req req) throws ProcessException;
	
	/**
	 * 根据贷款卡号查询已用的贷款
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S20020Resp S20020(S20020Req req) throws ProcessException;
	
	/**
	 * 根据贷款借据号查询贷款信息
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S20021Resp S20021(S20021Req req) throws ProcessException;
	
	/**
	 * 根据证件号码查询已用的贷款
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S20022Resp S20022(S20022Req req) throws ProcessException;
	
	/**
	 * 根据借据号查询还款计划表
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S20023Resp S20023(S20023Req req) throws ProcessException;
	
	/**
	 * 根据借据号查询贷款变更历史
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S20024Resp S20024(S20024Req req) throws ProcessException;
	
	/**
	 * 根据借据号查询贷款还款分配历史
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S20025Resp S20025(S20025Req req) throws ProcessException;
	
	/**
	 * 当日贷款变更申请
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S20026Resp S20026(S20026Req req) throws ProcessException;
	
	/**
	 * 当日贷款变更撤销
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public void S20027(S20027Req req) throws ProcessException;
	/**
	 * 查询/设定绑定借记卡号
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S20030Resp S20030(S20030Req req)throws ProcessException;
	
	/**
	 * 客户自定义浮动利率设定
	 * @param req
	 * @throws ProcessException
	 */
	public S20040Resp S20040(S20040Req req) throws ProcessException;
	
	/**
	 * 贷款交易历史查询
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S20050Resp S20050(S20050Req req) throws ProcessException;
	
	/**
	 * 贷款还款历史查询
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S20060Resp S20060(S20060Req req) throws ProcessException;
	/**
	 * 预销户
	 * @param req
	 * @throws ProcessException
	 */
	public void S20070(S20070Req req) throws ProcessException;
	
	/**
	 * 贷款展期
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S20080Resp S20080(S20080Req req) throws ProcessException;
	
	/**
	 * 贷款缩期
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S20090Resp S20090(S20090Req req) throws ProcessException;
	
}
