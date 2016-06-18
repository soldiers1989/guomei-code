package com.sunline.ccs.param.ui.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ccs.param.def.LoanPlan;
import com.sunline.kylin.web.ark.client.data.SelectOptionEntry;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.pcm.facility.ParameterFacility;
import com.sunline.pcm.facility.ParameterFetchResponseFacility;
import com.sunline.pcm.param.def.ElectronicTemplet;
import com.sunline.pcm.param.def.GroupCtrl;
import com.sunline.pcm.param.def.Mulct;
import com.sunline.pcm.param.def.Product;
import com.sunline.ppy.dictionary.exception.ProcessException;

@Controller
@RequestMapping(value = { "loanPlanServer", "/loanPlanServer" })
@Transactional
public class LoanPlanServer {

	@Autowired
	private ParameterFetchResponseFacility parameterFetchResponseFacility;

	@Autowired
	private ParameterFacility parameterFacility;

	@SuppressWarnings("rawtypes")
	@ResponseBody
	@RequestMapping(value = "/getLoanPlanList", method = { RequestMethod.POST })
	public FetchResponse getLoanPlanList(@RequestBody FetchRequest request) {
		String loanType4Query = (String) request.getCriteriaMap().get("loanType4Query");
		if (StringUtils.isEmpty(loanType4Query)) {
			loanType4Query = "loanPlan";
		}
		return parameterFetchResponseFacility.getLoanFetchResponse(request, LoanPlan.class,
				loanType4Query);
	}

	@ResponseBody
	@RequestMapping(value = "/updateLoanPlan", method = { RequestMethod.POST })
	public void updateLoanPlan(@RequestBody LoanPlan loanPlan) throws ProcessException {
		parameterFacility.updateParameterObject(loanPlan.loanCode, loanPlan);
	}

	@ResponseBody
	@RequestMapping(value = "/getLoanPlan", method = { RequestMethod.POST })
	public LoanPlan getLoanPlan(@RequestBody String key) {

		return parameterFacility.getParameterObject(key, LoanPlan.class);
	}

	@ResponseBody
	@RequestMapping(value = "/addLoanPlan", method = { RequestMethod.POST })
	public void addLoanPlan(@RequestBody LoanPlan loanPlan) throws ProcessException {
		parameterFacility.addNewParameter(loanPlan.loanCode, loanPlan);
	}

	@ResponseBody
	@RequestMapping(value = "/deleteLoanPlan", method = { RequestMethod.POST })
	public void deleteLoanPlan(@RequestBody List<String> keys) throws ProcessException {

		for (String key : keys) {
			parameterFacility.deleteParameterObject(key, LoanPlan.class);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/getFineList", method = { RequestMethod.POST })
	public List<Mulct> getFineList() {
		List<Mulct> fineList = parameterFacility.getParameterObject(Mulct.class);
		return fineList;
	}

	@ResponseBody()
	@RequestMapping(value = "/getProductCode", method = { RequestMethod.POST })
	public List<SelectOptionEntry> getProductCode() throws FlatException {
		List<Product> productList = parameterFacility.getParameterObject(Product.class);
		List<SelectOptionEntry> uiShowData = new ArrayList<SelectOptionEntry>();
		if (productList != null && !productList.isEmpty()) {
			for (Product product : productList) {
				SelectOptionEntry entry = new SelectOptionEntry(product.productCode,
						product.description);
				uiShowData.add(entry);
			}
		}
		return uiShowData;
	}

	@ResponseBody()
	@RequestMapping(value = "/getRiskId", method = { RequestMethod.POST })
	public List<SelectOptionEntry> getRiskId() throws FlatException {
		List<ElectronicTemplet> eTempletList = parameterFacility
				.getParameterObject(ElectronicTemplet.class);
		List<SelectOptionEntry> uiShowData = new ArrayList<SelectOptionEntry>();
		if (eTempletList != null && !eTempletList.isEmpty()) {
			for (ElectronicTemplet eTemplet : eTempletList) {
				SelectOptionEntry entry = new SelectOptionEntry(eTemplet.templetId,
						eTemplet.templetDesc);
				uiShowData.add(entry);
			}
		}
		return uiShowData;
	}

	@ResponseBody()
	@RequestMapping(value = "/getGroupCtrlId", method = { RequestMethod.POST })
	public List<SelectOptionEntry> getGroupCtrlId() throws FlatException {
		List<GroupCtrl> groupCtrlList = parameterFacility
				.getParameterObject(GroupCtrl.class);
		List<SelectOptionEntry> uiShowData = new ArrayList<SelectOptionEntry>();
		if (groupCtrlList != null && !groupCtrlList.isEmpty()) {
			for (GroupCtrl groupCtrl : groupCtrlList) {
				SelectOptionEntry entry = new SelectOptionEntry(groupCtrl.groupCtrlId,
						groupCtrl.desc);
				uiShowData.add(entry);
			}
		}
		return uiShowData;
	}
}
