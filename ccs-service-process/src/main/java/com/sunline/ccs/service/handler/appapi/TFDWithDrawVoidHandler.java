package com.sunline.ccs.service.handler.appapi;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sunline.ccs.facility.LogTools;
import com.sunline.ccs.facility.TxnUtils;
import com.sunline.ccs.facility.order.OrderFacility;
import com.sunline.ccs.infrastructure.server.repos.RCcsAuthmemoO;
import com.sunline.ccs.infrastructure.server.repos.RCcsTxnPost;
import com.sunline.ccs.service.context.TxnContext;
import com.sunline.ccs.service.context.TxnInfo;
import com.sunline.ccs.service.handler.AppapiCommService;
import com.sunline.ccs.service.handler.WithdrawVoidCommService;
import com.sunline.ccs.service.msentity.MsRequestInfo;
import com.sunline.ccs.service.msentity.MsResponseInfo;
import com.sunline.ccs.service.msentity.TFDWithDrawVoidReq;
import com.sunline.ccs.service.msentity.TFDWithDrawVoidResp;
import com.sunline.ppy.dictionary.enums.Indicator;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

/**
 * 放款撤销接口
 * @author wangz
 *
 */
@Service
public class TFDWithDrawVoidHandler extends AbstractHandler {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private AppapiCommService appapiCommService;
	@Autowired
	private WithdrawVoidCommService voidAndRefundCommService;

	@Override
	public MsResponseInfo execute(MsRequestInfo msRequestInfo) {
		LogTools.printLogger(logger, "TFDWithDrawVoidReq", "放款撤销", msRequestInfo,
				true);
		TFDWithDrawVoidReq req = (TFDWithDrawVoidReq) msRequestInfo;
		LogTools.printObj(logger, req, "请求参数TFDWithDrawVoidReq");
		TFDWithDrawVoidResp resp = new TFDWithDrawVoidResp();

		TxnContext context = new TxnContext();
		TxnInfo txnInfo = new TxnInfo();
		context.setTxnInfo(txnInfo);
		context.setMsRequestInfo(req);

		try {
			voidAndRefundCommService.WithDrawVoidProcess(context,req,Indicator.Y);
		} catch (ProcessException pe) {
			if (logger.isErrorEnabled())
				logger.error(pe.getMessage(), pe);

			appapiCommService.preException(pe, pe, txnInfo);
			appapiCommService.saveErrorOrder(context);

		} catch (Exception e) {
			if (logger.isErrorEnabled())
				logger.error(e.getMessage(), e);

			ProcessException pe = new ProcessException();
			appapiCommService.preException(pe, pe, txnInfo);
			appapiCommService.saveErrorOrder(context);

		} finally {
			LogTools.printLogger(logger, "TFDWithDrawVoidResp", "放款撤销", resp, false);
		}

		this.setResponse(resp, req, txnInfo);
		LogTools.printObj(logger, resp, "响应参数TFDWithDrawVoidResp");
		return resp;
	}
	




	
	/**
	 * 响应
	 * @param resp
	 * @param req
	 * @param txnInfo
	 */
	private void setResponse(TFDWithDrawVoidResp resp, TFDWithDrawVoidReq req,
			TxnInfo txnInfo) {
		resp.setContractNo(txnInfo.getContrNo());
		resp.setErrorCode(txnInfo.getResponsCode());
		resp.setErrorMessage(txnInfo.getResponsDesc());
		if (StringUtils.equals(MsRespCode.E_0000.getCode(),
				txnInfo.getResponsCode())) {
			resp.setStatus("S");// 交易成功
		} else {
			resp.setStatus("F");// 交易失败
		}
	}
}
