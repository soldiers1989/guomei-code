package com.sunline.ccs.service.api;

import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.service.protocol.S11010Req;
import com.sunline.ccs.service.protocol.S11010Resp;
import com.sunline.ccs.service.protocol.S11030Req;
import com.sunline.ccs.service.protocol.S11030Resp;
import com.sunline.ccs.service.protocol.S11040Req;
import com.sunline.ccs.service.protocol.S11040Resp;
import com.sunline.ccs.service.protocol.S11050Req;
import com.sunline.ccs.service.protocol.S11050Resp;
import com.sunline.ccs.service.protocol.S12020Req;
import com.sunline.ccs.service.protocol.S12020Resp;
import com.sunline.ccs.service.protocol.S12021Req;
import com.sunline.ccs.service.protocol.S12021Resp;
import com.sunline.ccs.service.protocol.S12030Req;
import com.sunline.ccs.service.protocol.S12040Req;
import com.sunline.ccs.service.protocol.S12040Resp;
import com.sunline.ccs.service.protocol.S12041Req;
import com.sunline.ccs.service.protocol.S12041Resp;
import com.sunline.ccs.service.protocol.S12100Req;
import com.sunline.ccs.service.protocol.S12100Resp;
import com.sunline.ccs.service.protocol.S12101Req;
import com.sunline.ccs.service.protocol.S12101Resp;
import com.sunline.ccs.service.protocol.S12110Req;
import com.sunline.ccs.service.protocol.S12110Resp;
import com.sunline.ccs.service.protocol.S12111Req;
import com.sunline.ccs.service.protocol.S12111Resp;
import com.sunline.ccs.service.protocol.S14040Req;
import com.sunline.ccs.service.protocol.S14040Resp;
import com.sunline.ccs.service.protocol.S14050Req;
import com.sunline.ccs.service.protocol.S14051Req;
import com.sunline.ccs.service.protocol.S14070Req;
import com.sunline.ccs.service.protocol.S14080Req;
import com.sunline.ccs.service.protocol.S14080Resp;
import com.sunline.ccs.service.protocol.S14090Req;
import com.sunline.ccs.service.protocol.S14091Req;
import com.sunline.ccs.service.protocol.S14092Req;
import com.sunline.ccs.service.protocol.S14100Req;
import com.sunline.ccs.service.protocol.S14120Req;
import com.sunline.ccs.service.protocol.S14170Req;
import com.sunline.ccs.service.protocol.S14170Resp;
import com.sunline.ccs.service.protocol.S14180Req;
import com.sunline.ccs.service.protocol.S15010Req;
import com.sunline.ccs.service.protocol.S15010Resp;
import com.sunline.ccs.service.protocol.S15011Req;
import com.sunline.ccs.service.protocol.S15011Resp;
import com.sunline.ccs.service.protocol.S15012Req;
import com.sunline.ccs.service.protocol.S15030Req;
import com.sunline.ccs.service.protocol.S15030Resp;
import com.sunline.ccs.service.protocol.S15050Req;
import com.sunline.ccs.service.protocol.S15050Resp;
import com.sunline.ccs.service.protocol.S15051Req;
import com.sunline.ccs.service.protocol.S15051Resp;
import com.sunline.ccs.service.protocol.S16040Req;
import com.sunline.ccs.service.protocol.S16050Req;
import com.sunline.ccs.service.protocol.ServiceControlResult;

/**
 * 非金融后台服务管理类交易接口
 * 
* @author fanghj
 *
 */
public interface OperateService {
	/**
	 * 卡片限额设定
	 * 
	 * @param S15030Req
	 * @return S15030Resp
	 * @throws ProcessException TODO
	 */
	S15030Resp S15030(S15030Req req) throws ProcessException;

	/**
	 * 消费凭密设定
	 * 
	 * @param S14080Req
	 * @return S14080Resp
	 * @throws ProcessException TODO
	 */
	S14080Resp S14080(S14080Req req) throws ProcessException;

	/**
	 * 卡片激活
	 * 
	 * @param S14040Req
	 * @return S14040Resp
	 * @throws ProcessException TODO
	 */
	S14040Resp S14040(S14040Req req) throws ProcessException;

	/**
	 * 卡片挂失/解挂
	 * 
	 * @param S14050Req
	 * @throws ProcessException TODO
	 */
	void S14050(S14050Req req) throws ProcessException;

	
	/**
	 * 止付（临时挂失）\解除止付（临时解挂）
	 * 
	 * @param req
	 * @throws ProcessException
	 */
	void S14051(S14051Req req) throws ProcessException;

	/**
	 * 销卡/销卡撤销
	 * 
	 * @param S14120Req
	 * @throws ProcessException TODO
	 */
	void S14120(S14120Req req) throws ProcessException;

	/**
	 * 冻结/解冻
	 * 
	 * @param S14180Req
	 * @throws ProcessException TODO
	 */
	void S14180(S14180Req req) throws ProcessException;

	/**
	 * 卡片交易/查询密码锁定解除
	 * 
	 * @param S14100Req
	 * @throws ProcessException TODO
	 */
	void S14100(S14100Req req) throws ProcessException;
	
	/**
	 * 账户信息验证
	 * 
	 * @param S16040Req
	 * @throws ProcessException TODO
	 */
	void S16040(S16040Req req) throws ProcessException;
	
	/**
	 * 第三方快捷支付开通验证
	 * @param req
	 * @throws ProcessException
	 */
	void S16050(S16050Req req) throws ProcessException;
	
	/**
	 * 查询密码/交易密码的设置
	 * @param S14090Req
	 * @throws ProcessException TODO
	 */
	void S14090(S14090Req req) throws ProcessException;
	
	/**
	 * 查询密码/交易密码的修改
	 * @param S14091Req
	 * @throws ProcessException TODO
	 */
	void S14091(S14091Req req) throws ProcessException;

	/**
	 * 挂失换卡/损坏换卡
	 * 
	 * @param req
	 * @throws ProcessException TODO
	 */
	void S14070(S14070Req req) throws ProcessException;

	/**
	 * 补打账单
	 * 
	 * @param req
	 * @throws ProcessException TODO
	 */
	void S12030(S12030Req req) throws ProcessException;
	
	
	/**
	 * 账单日列表查询
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S12111Resp S12111(S12111Req req) throws ProcessException;

	/**
	 * 4.2.1.	客户基本资料查询/维护
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S11010Resp S11010(S11010Req req) throws ProcessException;

	/**
	 * 4.2.2.	客户地址信息查询/维护
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S11030Resp S11030(S11030Req req) throws ProcessException;

	/**
	 * 4.2.3.	客户联系人信息查询/维护
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S11040Resp S11040(S11040Req req) throws ProcessException;

	/**
	 * 4.2.4.	账单寄送地址设定
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S12020Resp S12020(S12020Req req) throws ProcessException;

	/**
	 * 4.2.5.	账单介质设定
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S12021Resp S12021(S12021Req req) throws ProcessException;

	/**
	 * 已收年费减免
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S12041Resp S12041(S12041Req req) throws ProcessException;

	/**
	 * 4.2.6.	约定还款签约/取消
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S12100Resp S12100(S12100Req req) throws ProcessException;
	
	/**
	 * 短信发送设置
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S12101Resp S12101(S12101Req req) throws ProcessException;

	/**
	 * 4.2.7.	临时额度设定
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S15010Resp S15010(S15010Req req) throws ProcessException;
	
	/**
	 * 永久额度调整(直接调成，仅限内管使用）
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException
	 */
	public S15011Resp S15011(S15011Req req) throws ProcessException;
	
	/**
	 * 永久额度调整（带复核）
	 * @param req
	 * @throws ProcessException
	 */
	public void S15012(S15012Req req) throws ProcessException;

	/**
	 * 4.2.8.	取现额度比例设定
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S15050Resp S15050(S15050Req req) throws ProcessException;

	/**
	 * 4.2.9.	分期额度比例设定
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S15051Resp S15051(S15051Req req) throws ProcessException;

	/**
	 * 4.2.10.	账单日修改
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S12110Resp S12110(S12110Req req) throws ProcessException;

	/**
	 * 4.2.11.	预留问题答案维护
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S11050Resp S11050(S11050Req req) throws ProcessException;

	/**
	 * 4.2.12.	卡片寄送地址设定
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S14170Resp S14170(S14170Req req) throws ProcessException;
	/**
	 * 4.2.25.	未收年费减免
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public S12040Resp S12040(S12040Req req) throws ProcessException;
	/**
	 * 4.2.31.	客服/内管解锁服务
	 * 
	 * @param req
	 * @return
	 * @throws ProcessException TODO
	 */
	public void S14092(S14092Req req) throws ProcessException;
	
	/**
	 * 判断受卡片状态以及锁定码等信息的影响是否可以做此交易
	 * @param servCode
	 * @param cardNo TODO
	 * @return
	 * @throws ProcessException
	 */
	public ServiceControlResult serviceControl(String servCode, String cardNo) throws ProcessException;

}
