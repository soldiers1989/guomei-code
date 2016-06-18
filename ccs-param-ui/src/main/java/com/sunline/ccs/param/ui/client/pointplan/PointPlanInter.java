package com.sunline.ccs.param.ui.client.pointplan;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.PointPlan;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;
/*
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.PointPlan;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
*/
@RemoteServiceRelativePath("rpc/pointPlanServer")
public interface PointPlanInter extends RemoteService {
	FetchResponse getPointPlanList(FetchRequest request);

	void addPointPlan(PointPlan plan) throws ProcessException;

	void updatePointPlan(PointPlan plan) throws ProcessException;
	
	void deletePointPlan(List<String> keys) throws ProcessException;

	PointPlan getPointPlan(String key);
}
