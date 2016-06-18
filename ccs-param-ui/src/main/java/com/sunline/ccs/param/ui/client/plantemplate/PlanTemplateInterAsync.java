package com.sunline.ccs.param.ui.client.plantemplate;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.BucketDef;
import com.sunline.ccs.param.def.InterestTable;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.enums.BucketType;
/*
import com.sunline.ppy.dictionary.enums.BucketType;
import com.sunline.ccs.param.def.BucketDef;
import com.sunline.ccs.param.def.InterestTable;
import com.sunline.ccs.param.def.PlanTemplate;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
*/
public interface PlanTemplateInterAsync {

	void addPlantemplate(Map<String, Serializable> map, Map<BucketType, BucketDef> intParameterBuckets, AsyncCallback<Void> callback);

	void getPlantemplate(String key, AsyncCallback<PlanTemplate> callback);

	void getPlantemplateList(FetchRequest request, AsyncCallback<FetchResponse> callback);

	void updatePlantemplate(Map<String, Serializable> map, Map<BucketType, BucketDef> intParameterBuckets, AsyncCallback<Void> callback);

	void addInterest(InterestTable interest, AsyncCallback<Void> callback);

	void deletePlantemplate(List<String> keys, AsyncCallback<Void> callback);

}
