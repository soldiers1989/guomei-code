package com.sunline.ccs.param.ui.client.program;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.ccs.param.def.Program;
import com.sunline.kylin.web.ark.client.rpc.FetchRequest;
import com.sunline.kylin.web.ark.client.rpc.FetchResponse;
import com.sunline.ppy.dictionary.exception.ProcessException;
/*
import com.sunline.ppy.dictionary.exception.ProcessException;
import com.sunline.ccs.param.def.Program;
import com.sunline.ark.gwt.shared.datasource.FetchRequest;
import com.sunline.ark.gwt.shared.datasource.FetchResponse;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
*/
@RemoteServiceRelativePath("rpc/programServer")
public interface ProgramInter extends RemoteService {
	FetchResponse getProgramList(FetchRequest request);
	
	void deleteProgram(List<String> keys) throws ProcessException;
	
	void addProgram(Program program) throws ProcessException;
	
	Program getProgram(String key);
	
	void updateProgram(Program program) throws ProcessException;
}
