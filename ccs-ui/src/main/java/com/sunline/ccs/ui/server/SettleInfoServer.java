package com.sunline.ccs.ui.server;


import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gwt.user.client.ui.HTML;
import com.mysema.query.jpa.impl.JPAQuery;
import com.sunline.acm.service.api.GlobalManagementService;
import com.sunline.ark.support.OrganizationContextHolder;
import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.server.repos.RCcsCustomer;
import com.sunline.ccs.infrastructure.server.repos.RCcsLoan;
import com.sunline.ccs.infrastructure.shared.model.CcsLoan;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsCustomer;
import com.sunline.ccs.infrastructure.shared.model.QCcsLoan;
import com.sunline.kylin.web.ark.client.JPAQueryFetchResponseBuilder;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.ppy.dictionary.enums.LoanStatus;

/**
 * 结清信息查询
 * 
/** 
 * @see 类名：SettleInfoServer
 * @see 描述：结清信息查询
 *
 * @see 创建日期：   2015年8月27日上午11:05:29
 * @author songyanchao
 * 
 * @see 修改记录：
 * @see [编号：日期_设计来源]，[修改人：***]，[方法名：***]
 */
@Controller
@RequestMapping(value = "/settleInfoServer")
public class SettleInfoServer{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private OperatorAuthUtil operatorAuthUtil;
	
	QCcsLoan qCcsLoan = QCcsLoan.ccsLoan;
	@Autowired
	private GlobalManagementService globalManagementService;
	@Autowired
	RCcsLoan rCcsLoan;
	@Autowired
	RCcsAcct rCcsAcct;
	@Autowired
	RCcsCustomer rCcsCustomer;
	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/printSettleInfo", method = {RequestMethod.POST})
	public String printSettleInfo(@RequestBody String loanId) throws FlatException{
		//for testing
		//OutputStream out=null;
		ByteArrayInputStream bs=new ByteArrayInputStream("Content-type:text/html;charset=utf-8".getBytes());
		OutputStream out=null;
		JPAQuery query=new JPAQuery(em).from(qCcsLoan).where(qCcsLoan.org.trim().eq(OrganizationContextHolder.getCurrentOrg()));
			CcsLoan ccsLoan=rCcsLoan.findOne(Long.parseLong(loanId));
		return setParam(ccsLoan);
	}
	
//	public HTML setParam(Map<String,String> map){
	public String setParam(CcsLoan ccsLoan){
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		QCcsCustomer qCcsCustomer= QCcsCustomer.ccsCustomer;
		Long custId=rCcsAcct.findOne(qCcsAcct.contrNbr.eq(ccsLoan.getContrNbr())).getCustId();
		
		//使用Calendar取代SimpleDateFormat
		Calendar calendar=Calendar.getInstance();
		if(calendar instanceof GregorianCalendar){
			calendar.setTime(globalManagementService.getSystemStatus().getBusinessDate());
		}
		String html="";
		try{
			html+=
				"<div style=\"font-family:FangSong;margin:10px\">"
				+ "<p style=\"font-size:25px;text-align:center;font-family:SimSun;font-weight:bold;line-height:1.5;\">提前还款结清证明</p>"
				+ "<br>"
				+ "<b style=\"font-size:18px;line-height:1.5;\">尊敬的用户：</b>"
				+ "<p style=\"font-size:18px;text-indent:2em;font-size:18px;text-indent:2em;line-height:1.5;\">您作为借款人与我公司签订的《个人信保贷款合同》，合同主要内容如下：</p>"
				+ "<p style=\"font-size:18px;text-indent:2em;font-size:18px;text-indent:2em;line-height:1.5;\">贷款合同编号： "
				+ ccsLoan.getContrNbr()
				+ "</p>"
				+ "<p style=\"font-size:18px;text-indent:2em;font-size:18px;text-indent:2em;line-height:1.5;\">借款人： "
				+ rCcsAcct.findOne(qCcsAcct.contrNbr.eq(ccsLoan.getContrNbr())).getName()
				+ " 身份证号： "
				+ rCcsCustomer.findOne(qCcsCustomer.custId.eq(custId)).getIdNo()
				+ "</p>"
				+ "<p style=\"font-size:18px;text-indent:2em;font-size:18px;text-indent:2em;line-height:1.5;\">贷款总额： "
				+ ccsLoan.getLoanInitPrin()
				+ "元整（￥ "
				+ ccsLoan.getLoanInitPrin()
				+ "）</p>"
				+ "<p style=\"font-size:18px;text-indent:2em;font-size:18px;text-indent:2em;line-height:1.5;\">借款期限： "
				+ ccsLoan.getLoanInitTerm()
				+ "个月        放款日期： "
				+ ccsLoan.getActiveDate()
				+ "</p>"
				//完成状态的贷款，与提前结清的贷款
				+ new String(ccsLoan.getLoanStatus()==LoanStatus.F?"<p style=\"font-size:18px;text-indent:2em;font-size:18px;text-indent:2em;line-height:1.5;\">您于 "
						:"<p style=\"font-size:18px;text-indent:2em;font-size:18px;text-indent:2em;line-height:1.5;\">您于 "
				//以下字段可能为空
				+ new String(ccsLoan.getTerminalDate()==null?"":ccsLoan.getTerminalDate().toString().substring(0, 4))
				+ "年 "
				+ new String(ccsLoan.getTerminalDate()==null?"":ccsLoan.getTerminalDate().toString().substring(5, 7))
				+ "月  "
				+ new String(ccsLoan.getTerminalDate()==null?"":ccsLoan.getTerminalDate().toString().substring(8, 10))
				+ "日提交提前还款申请，并于 ")
				+ new String(ccsLoan.getPaidOutDate()==null?"":ccsLoan.getPaidOutDate().toString().substring(0, 4))
				+ "年  "
				+ new String(ccsLoan.getPaidOutDate()==null?"":ccsLoan.getPaidOutDate().toString().substring(5, 7))
				+ "月  "
				+ new String(ccsLoan.getPaidOutDate()==null?"":ccsLoan.getPaidOutDate().toString().substring(8, 10))
				+ "日将应还款项付至了我公司指定的还款账户"
				+ new String(ccsLoan.getLoanStatus()==LoanStatus.F?"。":"，还款金额为       "
				+ new String(ccsLoan.getAdvPmtAmt()==null?"":ccsLoan.getAdvPmtAmt().toString())
				+ " 元整（￥）。")
				+ "</p>"
				+ "<br>"
				+ "<p style=\"font-size:18px;text-indent:2em;font-size:18px;text-indent:2em;line-height:1.5;\">我公司在此证明：您在前述贷款合同项下的应还贷款本金、相应利息及相关手续费均已偿还，您在该合同项下的所有还款义务已经全部履行完毕。</p>"
				+ "<p style=\"font-size:18px;text-indent:2em;font-size:18px;text-indent:2em;line-height:1.5;\">本证明仅作为贷款提前结清证明，不作他用。特此证明。</p>"
				+ "<br><br><p style=\"font-size:18px;text-align:right;line-height:1.5;margin-top:100px\">马上消费金融股份有限公司</p>"
				+ "<p style=\"font-size:18px;text-align:right\">"
				+ calendar.get(Calendar.YEAR)
				+ " 年"
				+ calendar.get(Calendar.MONTH)+1
				+ " 月"
				+ calendar.get(Calendar.DATE)
				+ " 日</p>"
				+ "</div>"
				;
		}catch(Exception e){
			throw new FlatException("格式错误，请检查!");
		}
		return html;
	}
	
	@Transactional
	@ResponseBody()
	@RequestMapping(value = "/getSettleInfoList", method = {RequestMethod.POST})
	public FetchResponse getSettleInfoList(@RequestBody FetchRequest request) throws FlatException {
	    	String guarantyId = (String)request.getParameter("guarantyId");
	    	String contrNbr = (String)request.getParameter("contrNbr");
	    	String terminalDate = (String)request.getParameter("terminalDate");
	    	SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		JPAQuery query = new JPAQuery(em).from(qCcsLoan).where(qCcsLoan.org.trim().eq(OrganizationContextHolder.getCurrentOrg()));
		{
			
			query=query.where(qCcsLoan.loanStatus.eq(LoanStatus.T).or(qCcsLoan.loanStatus.eq(LoanStatus.F)));
//			query=query.where(qCcsLoan.paidOutDate.isNotNull());
			if(guarantyId != null&&!"null".equals(guarantyId)){
				query = query.where(qCcsLoan.guarantyId.eq(guarantyId));
			}
			if(contrNbr != null&&!"null".equals(contrNbr)){
				query = query.where(qCcsLoan.contrNbr.eq(contrNbr));
			}
			if(terminalDate!=null&&!"null".equals(terminalDate)){
					query= query.where(qCcsLoan.terminalDate.eq(new Date(Long.parseLong(terminalDate))));
			}
		}
				
		FetchResponse response =  new JPAQueryFetchResponseBuilder(request, query)
		.addFieldMapping(qCcsLoan)
                .build();
		return response;
	}
}
