package com.sunline.ccs.param.ui.client.pointplan;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.PointPlan;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
/*
import com.sunline.ccs.param.def.PointPlan;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
*/
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;

public interface PointPlanInterAsync {

	void getPointPlanList(FetchRequest request,
			AsyncCallback<FetchResponse> callback);

	void deletePointPlan(List<String> keys, AsyncCallback<Void> callback);

	void getPointPlan(String key, AsyncCallback<PointPlan> callback);

	void addPointPlan(PointPlan plan, AsyncCallback<Void> callback);

	void updatePointPlan(PointPlan plan, AsyncCallback<Void> callback);
	
}
