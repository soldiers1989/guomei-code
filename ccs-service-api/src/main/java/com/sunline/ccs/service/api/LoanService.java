package com.sunline.ccs.service.api;

import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.service.protocol.S12012Req;
import com.sunline.ccs.service.protocol.S12012Resp;
import com.sunline.ccs.service.protocol.S13000Req;
import com.sunline.ccs.service.protocol.S13000Resp;
import com.sunline.ccs.service.protocol.S13001Req;
import com.sunline.ccs.service.protocol.S13001Resp;
import com.sunline.ccs.service.protocol.S13002Req;
import com.sunline.ccs.service.protocol.S13002Resp;
import com.sunline.ccs.service.protocol.S13080Req;
import com.sunline.ccs.service.protocol.S13080Resp;
import com.sunline.ccs.service.protocol.S13081Req;
import com.sunline.ccs.service.protocol.S13081Resp;
import com.sunline.ccs.service.protocol.S13082Req;
import com.sunline.ccs.service.protocol.S13082Resp;
import com.sunline.ccs.service.protocol.S13083Req;
import com.sunline.ccs.service.protocol.S13083Resp;
import com.sunline.ccs.service.protocol.S13084Req;
import com.sunline.ccs.service.protocol.S13084Resp;
import com.sunline.ccs.service.protocol.S13085Req;
import com.sunline.ccs.service.protocol.S13085Resp;
import com.sunline.ccs.service.protocol.S13090Req;
import com.sunline.ccs.service.protocol.S13110Req;
import com.sunline.ccs.service.protocol.S13110Resp;
import com.sunline.ccs.service.protocol.S13120Req;
import com.sunline.ccs.service.protocol.S13120Resp;
import com.sunline.ccs.service.protocol.S13130Req;
import com.sunline.ccs.service.protocol.S13130Resp;

/**
 * 非金融后台服务分期类交易接口
 */
public interface LoanService {
	
	/**
	 * 消费分期参数信息查询
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S13000Resp S13000(S13000Req req) throws ProcessException;
	
	/**
	 * 账单分期参数信息查询
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	
	public S13001Resp S13001(S13001Req req) throws ProcessException;
	
	/**
	 * 现金分期参数查询
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S13002Resp S13002(S13002Req req) throws ProcessException;
	/**
	 * 分期交易信息查询
	 * @param rep
	 * @return
	 * @throws ProcessException TODO
	 * @exception   
	 * @since  1.0.0
	 */
	public S13080Resp S13080(S13080Req rep) throws ProcessException;
	
	/**
	 * 当日分期交易信息查询
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S13083Resp S13083(S13083Req req) throws ProcessException;
	
	/**
	 * 可转分期交易明细查询
	 * @param req 请求报文
	 * @return  返回响应
	 * @throws ProcessException TODO
	 * @throws BusinessProcessException
	 */
	public S13081Resp S13081(S13081Req req) throws ProcessException; 
	
	
	/**
	 * 消费转分期申请 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 * @exception   
	 * @since  1.0.0
	 */
	public S13082Resp S13082(S13082Req req) throws ProcessException;
	
	
	
	/**
	 * 账单可分期金额查询
	 * (这里描述这个方法适用条件 – 可选)  
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 * @exception   
	 * @since  1.0.0
	 */
	public S13110Resp S13110(S13110Req req)throws ProcessException;
	
	
	/**
	 * 账单分期申请
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 * @exception   
	 * @since  1.0.0
	 */
	public S12012Resp S12012(S12012Req req)throws ProcessException;
	
	
	/**
	 * 分期终止/终止撤销
	 * @param req
	 * @throws ProcessException TODO
	 * @exception   
	 * @since  1.0.0
	 */
	public void S13090(S13090Req req)throws ProcessException;
	
	/**
	 * 分期提前还款
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S13120Resp S13120(S13120Req req) throws ProcessException;
	
	/**
	 * 分期展期
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S13130Resp S13130(S13130Req req) throws ProcessException;
	
	/**
	 * 现金分期申请
	 * @param request
	 * @return
	 * @throws ProcessException
	 */
	public S13084Resp S13084(S13084Req request)throws ProcessException;
	
	/**
	 * 分期申请状态查询接口
	 * @param request
	 * @return
	 * @throws ProcessException
	 */
	public S13085Resp S13085(S13085Req request)throws ProcessException;

}
