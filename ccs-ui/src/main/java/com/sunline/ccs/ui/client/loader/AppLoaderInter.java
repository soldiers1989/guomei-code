package com.sunline.ccs.ui.client.loader;

//import com.sunline.pcm.ui.common.client.LoaderInter;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sunline.kylin.web.flat.client.data.ClientContext;

/**
 * 加载模块的服务接口
* @author fanghj
 *
 */
@RemoteServiceRelativePath("rpc/appLoaderServer")
//public interface AppLoaderInter extends LoaderInter {
public interface AppLoaderInter {
	// TODO nova改造，暂时修改，原集成与BMP
	ClientContext getClientContext();
}
