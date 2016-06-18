package com.sunline.ccs.param.ui.client.program;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sunline.ccs.param.def.Program;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
/*
import com.sunline.ccs.param.def.Program;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;
*/
public interface ProgramInterAsync {
	void getProgramList(FetchRequest request, AsyncCallback<FetchResponse> callback);
	
	void deleteProgram(List<String> keys, AsyncCallback<Void> callback);

	void addProgram(Program program, AsyncCallback<Void> callback);

	void getProgram(String programId, AsyncCallback<Program> callback);

	void updateProgram(Program program, AsyncCallback<Void> callback);
}
