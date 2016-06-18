package com.sunline.ccs.http;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunline.ppy.dictionary.enums.MsPayfrontError;
import com.sunline.ppy.dictionary.enums.MsRespCode;
import com.sunline.ppy.dictionary.exception.ProcessException;

@Service
public class HttpClient{
	
	private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
	
	@Value("#{env.connectionTimeout}")
	private int connectionTimeout;
	
	@Value("#{env.waitTimeout}")
	private int waitTimeout;
	
	@Value("#{env.maxConnectionsPerHost}")
	private int maxConnectionsPerHost;
	
	@Value("#{env.maxTotalConnections}")
	private int maxTotalConnections;
	
	private String reqXml;
	
	private String msgType;
	
//	@Value("#{env.ALIPAY_URL}")
//	private String ALIPAY_URL;
	
//	private static final String charSet = "UTF-8";
	
  private HttpConnectionManager connectionManager;
  
  public void getConnection() {
  	
  	if (connectionManager == null) {
  		// 创建一个线程安全的HTTP连接池
          connectionManager = new MultiThreadedHttpConnectionManager();
          HttpConnectionManagerParams params = new HttpConnectionManagerParams();
          // 连接建立超时
          params.setConnectionTimeout(connectionTimeout);
          // 数据等待超时
          params.setSoTimeout(waitTimeout);
          // 默认每个Host最多10个连接S
          params.setDefaultMaxConnectionsPerHost(maxConnectionsPerHost);
          // 最大连接数（所有Host加起来）
          params.setMaxTotalConnections(maxTotalConnections);
          connectionManager.setParams(params);
		}
     
  }

  /**
   * 调用支付前置
   * @param json
   * @param uri
   * @return
   */
  public String sendMsHttpPost(String json ,String uri,String defCharset){
  	if(logger.isDebugEnabled())
  		logger.debug("发送报文[{}]",json);
  	getConnection();
      // 发送报文
      org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient(connectionManager);
      PostMethod postMethod = new PostMethod(uri);
      String resJson = "";
//      postMethod.addRequestHeader("Content-Type", "application/json");
      postMethod.addRequestHeader("Content-Type","application/x-www-form-urlencoded;charset="+defCharset);
      try {
			Map<String ,String> paramMap = this.json2Map(json);
			
			if (null != paramMap) {
	            Set<String> keys = paramMap.keySet();
	            for (String key : keys) {
	                if (StringUtils.isNotBlank(key)) {
	                    String value = paramMap.get(key);
	                    postMethod.setParameter(key, value);
	                }
	            }
	        }
			
          httpClient.executeMethod(postMethod);
          resJson = postMethod.getResponseBodyAsString();
          logger.debug("响应报文[{}]",resJson);
      } catch (UnsupportedEncodingException e) {
      	logger.error("交易报文编码异常[{}]",e);
      	throw new ProcessException(MsPayfrontError.E_90001.getCode(),MsPayfrontError.E_90001.getDesc());
      } catch (HttpException e) {
      	logger.error("交易http连接异常[{}]",e);
      	throw new ProcessException(MsPayfrontError.E_90002.getCode(),MsPayfrontError.E_90002.getDesc());
		} catch (IOException e) {
			logger.error("交易http读取异常[{}]",e);
			throw new ProcessException(MsPayfrontError.E_90003.getCode(),MsPayfrontError.E_90003.getDesc());
		} catch (Exception e){
			logger.error("交易报文编码异常[{}]",e);
      	throw new ProcessException(MsPayfrontError.E_90001.getCode(),MsPayfrontError.E_90001.getDesc());
		}finally {
          postMethod.releaseConnection();
      }
     
		return resJson;
  }
  
	/**
	 * json转Map
	 * @param jsonStr
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> json2Map(String jsonStr) {
		if(logger.isDebugEnabled())
			logger.debug("json转Map:[{}]",jsonStr);
		
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> map = new HashMap<String, String>();
		try {
			map = mapper.readValue(jsonStr, Map.class);
		} catch (Exception e) {
			if(logger.isErrorEnabled())
				logger.error(e.getMessage(),e);
			throw new ProcessException(MsRespCode.E_9998.getCode(),MsRespCode.E_9998.getMessage());
		}
		return map;
	}
	
  /**
   * 调用支付前置
   * @param json
   * @param uri
   * @return
   */
  public String sendMsHttpGet(String json ,String uri,String defCharset){
  	if(logger.isDebugEnabled())
  		logger.debug("发送报文[{}]",json);
  	getConnection();
      // 发送报文
      org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient(connectionManager);
      GetMethod getMethod = new GetMethod(uri);
      String resJson = "";
//      postMethod.addRequestHeader("Content-Type","application/x-www-form-urlencoded;charset="+defCharset);
      org.apache.commons.httpclient.params.HttpMethodParams httpMethodParams = new  org.apache.commons.httpclient.params.HttpMethodParams();
      httpMethodParams.setContentCharset(defCharset);
      getMethod.setParams(httpMethodParams);
      try {
			Map<String ,String> paramMap = this.json2Map(json);

			if (null != paramMap) {
	            Set<String> keys = paramMap.keySet();
	            NameValuePair nameValuePair[] = new NameValuePair[paramMap.entrySet().size()];
	            int i=0;
	            for (String key : keys) {
	                if (StringUtils.isNotBlank(key)) {
	                    String value = paramMap.get(key);
	                    nameValuePair[i]=new NameValuePair(key,value);
	                    i++;
	                }
	            }
              String queryString = EncodingUtil.formUrlEncode(nameValuePair, defCharset);
				getMethod.setQueryString(queryString);

	        }
			
          httpClient.executeMethod(getMethod);
          resJson = getMethod.getResponseBodyAsString();
          logger.debug("响应报文[{}]",resJson);
      } catch (UnsupportedEncodingException e) {
      	logger.error("交易报文编码异常[{}]",e);
      	throw new ProcessException(MsPayfrontError.E_90001.getCode(),MsPayfrontError.E_90001.getDesc());
      } catch (HttpException e) {
      	logger.error("交易http连接异常[{}]",e);
      	throw new ProcessException(MsPayfrontError.E_90002.getCode(),MsPayfrontError.E_90002.getDesc());
		} catch (IOException e) {
			logger.error("交易http读取异常[{}]",e);
			throw new ProcessException(MsPayfrontError.E_90003.getCode(),MsPayfrontError.E_90003.getDesc());
		} catch (Exception e){
			logger.error("交易报文编码异常[{}]",e);
      	throw new ProcessException(MsPayfrontError.E_90001.getCode(),MsPayfrontError.E_90001.getDesc());
		}finally {
          getMethod.releaseConnection();
      }
     
		return resJson;
  }
  
  
  /**
   * post请求上传文件
   * @param uri
   * @param fileBodyName 远程主机接收的文件名(文件流参数名)
   * @param file 要发送的文件
   * @param params 请求参数,无传null即可
   * @param defCharset
   * @return
   */
  public String uploadWithPost(String uri, String fileBodyName, File file, Map<String, String> params, String defCharset){
		
		String respData = "";
		org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeout); //连接超时
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, waitTimeout); //读取超时
		org.apache.http.client.methods.HttpPost httpPost = new org.apache.http.client.methods.HttpPost(uri);
		
		//Charset用来保证文件域中文名不乱码,非文件域中文不乱码的话还要像下面StringBody中再设置一次Charset
		org.apache.http.entity.mime.MultipartEntity reqEntity = new org.apache.http.entity.mime.MultipartEntity(org.apache.http.entity.mime.HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName(defCharset));
		try{
			reqEntity.addPart(fileBodyName, new org.apache.http.entity.mime.content.FileBody(file));
			if(null != params){
				for(Map.Entry<String,String> entry : params.entrySet()){
					reqEntity.addPart(entry.getKey(), new org.apache.http.entity.mime.content.StringBody(entry.getValue(), Charset.forName(defCharset)));
				}
				httpPost.setEntity(reqEntity);
			}
			org.apache.http.HttpResponse response = httpClient.execute(httpPost);
			org.apache.http.HttpEntity entity = response.getEntity();
			if(null != entity){
				respData = org.apache.http.util.EntityUtils.toString(entity, org.apache.http.entity.ContentType.getOrDefault(entity).getCharset());
			}
			return respData;
		}catch(org.apache.http.conn.ConnectTimeoutException cte){
			logger.error("上传文件连接超时[{}]", cte);
			throw new RuntimeException("请求通信[" + uri + "]时连接超时", cte);
		}catch(java.net.SocketTimeoutException ste){
			logger.error("上传文件读取超时[{}]", ste);
			throw new RuntimeException("请求通信[" + uri + "]时读取超时", ste);
		}catch(Exception e){
			logger.error("上传文件异常[{}]", e);
			throw new RuntimeException("请求通信[" + uri + "]时遇到异常", e);
		}finally{
			httpClient.getConnectionManager().shutdown();
		}
	}
  
  /**
   * 压测挡板
   * @return
   */
  public String testResutl(){
  	StringBuffer sbuf = new StringBuffer();
  	sbuf.append("{");
  	sbuf.append("\"code\": \"0\",");
  	sbuf.append("\"data\": {");
  	sbuf.append("\"errorCode\": \"0\",");
  	sbuf.append("\"errorMessage\": \"操作成功\"");
  	sbuf.append("},");
  	sbuf.append("\"message\": \"操作成功\"");
  	sbuf.append("}");
  	return sbuf.toString();
  }
  
  /**
  * @Description 直接传进来已经拼接好的JSON字符串，返回数据
  * @author 鹏宇
  * @date 2015-12-18 下午9:07:35
   */
	public String send(String uri, String jsonReq) {
		getConnection();
      // 发送报文
      org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient(connectionManager);
      PostMethod method = new PostMethod(uri);
      method.addRequestHeader("Content-Type", "application/json");
      try {
      	logger.debug("HTTP发送原始报文{}",jsonReq);
      	method.setRequestEntity(new StringRequestEntity(jsonReq,null, "UTF-8"));
          httpClient.executeMethod(method);
          String jsonRes = method.getResponseBodyAsString();
          logger.debug("HTTP响应原始报文{}",jsonRes);
          return jsonRes;
      } catch (UnsupportedEncodingException e) {
      	logger.error("交易报文编码异常[{}]",e.getMessage());
      	throw new ProcessException(MsPayfrontError.E_90001.getCode(),MsPayfrontError.E_90001.getDesc());
      } catch (HttpException e) {
      	logger.error("交易http连接异常[{}]",e.getMessage());
      	throw new ProcessException("审批接口连接异常,请稍后再试");
		} catch (IOException e) {
			logger.error("交易http读取异常[{}]",e.getMessage());
			throw new ProcessException("审批接口连接异常,请稍后再试");
		} catch (Exception e) {
      	logger.error("发送异常[{}]",e.getMessage());
      	throw new ProcessException(e.getMessage());
      } finally {
          method.releaseConnection();
      }
  }
	
  
//	public void send() {
//		getConnection();
//      // 发送报文
//      org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient(connectionManager);
//      PostMethod method = new PostMethod(ALIPAY_URL);
//      method.addRequestHeader("Content-Type", "application/json");
//      try {
//      	//数字签名
////      	reqXml = SignUtil.sign(DocumentUtil.getDocFromString(reqXml), keyService.getMyPrivateKey(), msgType);
//      	logger.debug("批量查询发送给支付宝报文{}",reqXml);
//      	method.setRequestEntity(new StringRequestEntity(reqXml,null, charSet));
//          httpClient.executeMethod(method);
//          String resXml = method.getResponseBodyAsString();
//          logger.debug("批量查询支付宝响应报文{}",resXml);
//          
//      } catch (Exception e) {
//      	logger.error("批量查询交易发送查询结果,异常内容[{}]",e.getMessage());
//         e.printStackTrace();
//      } finally {
//          method.releaseConnection();
//      }
//     
//  }

	public String getReqXml() {
		return reqXml;
	}

	public void setReqXml(String reqXml) {
		this.reqXml = reqXml;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	
}
