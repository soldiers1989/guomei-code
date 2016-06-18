package com.sunline.ccs.param.ui.client.plantemplate;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.BucketDef;
import com.sunline.ccs.param.def.InterestTable;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.exception.ProcessException;
/*
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.BucketDef;
import com.sunline.ccs.param.def.InterestTable;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
*/
@RemoteServiceRelativePath("rpc/planTemplateServer")
public interface PlanTemplateInter extends RemoteService {
	FetchResponse getPlantemplateList(FetchRequest request);

	void addPlantemplate(Map<String, Serializable> map, Map<BucketType, BucketDef> intParameterBuckets) throws ProcessException;

	void updatePlantemplate(Map<String, Serializable> map, Map<BucketType, BucketDef> intParameterBuckets) throws ProcessException;

	PlanTemplate getPlantemplate(String key);

	void addInterest(InterestTable interest) throws ProcessException;
	
	void deletePlantemplate(List<String> keys) throws ProcessException;
}
