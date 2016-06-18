<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@page
	import="org.springframework.security.core.AuthenticationException"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title>Login Page</title>
<style type="text/css">
body {
	font-family:"宋体";
	margin-left: 0px;
	margin-top: 0px;
	margin-right: 0px;
	margin-bottom: 0px;
	font-size: 12px;
	line-height: 22px;
}
</style>
</head>
<body onload="document.f.j_username.focus();">
	<form name="f" method="post" action="j_spring_security_check">
		<input type="hidden" name="single" value="1">
		<table width="100%" border="0" cellspacing="0" cellpadding="0">
			<tbody>
				<tr>
					<td height="68">&nbsp;</td>
				</tr>
				<tr>
					<td height="30"><table width="959" border="0" align="center"
							cellpadding="0" cellspacing="0">
							<tbody>
								<tr>
									<td align="right">
										<table width="100%" border="0">
											<tbody>
												<tr>
													<td height="65" align="left"><img
														src="img/logo_new.jpg" width="198" height="50"></td>
													<td width="200" valign="bottom"></td>
												</tr>
											</tbody>
										</table>
									</td>
								</tr>
							</tbody>
						</table></td>
				</tr>
				<tr>
					<td height="409" valign="top" background="img/index_bg.gif"><table
							width="959" border="0" align="center" cellpadding="0"
							cellspacing="0">
							<!--index_bg.gif-->
							<tbody>
								<tr>
									<td align="center"><img src="img/index_ban.jpg"
										width="959" height="298"></td>
								</tr>
								<tr>
									<td height="15"></td>
								</tr>
								<tr>
									<td><table width="80%" border="0" align="right"
											cellpadding="0" cellspacing="0">
											<tbody>
												<tr>
													<td width="80" align="right" class="STYLE4">机构：</td>
													<td align="left"><input name="orgCode" class="button"
														id="orgCode" onkeypress="checkUser(event);"></td>
													<td width="80" align="right" class="STYLE4">用户名：</td>
													<td align="left"><input name="j_username"
														class="button" id="j_username"
														onkeypress="checkUser(event);"></td>
													<td width="80" align="right" class="STYLE4">密码：</td>
													<td align="left"><input name="j_password"
														type="password" class="button" id="j_password"
														onkeypress="checkPwd(event);"></td>
													<td width="100px" align="center"><img
														onclick="clickLogin();" src="img/login2010.jpg" width="68"
														height="24" border="0" style="cursor: point"></td>
												</tr>
												<tr>
													<td colspan="10">
														<%
															String error = request.getParameter("error");
															if ("true".equals(error)) {
														%>
														<p>
															<font color='red'>Login fail: <%
																AuthenticationException ex = (AuthenticationException) session
																			.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
																	String errorMsg = ex != null ? ex.getMessage() : "none";
															%> <%=errorMsg%></font>
														</p> <%
 	}
 %>
													</td>
												</tr>
											</tbody>
										</table></td>
								</tr>
							</tbody>
						</table></td>
				</tr>
			</tbody>
		</table>



		<script language="JavaScript">
		
			//得到按键。如13＝回车
			function getPressKey(e){
				var keynum	
				if(window.event){ // IE
					keynum = e.keyCode;
				}
				else if(e.which){ // Netscape/Firefox/Opera
					keynum = e.which;
				}
				
				return keynum;
			}
		
			String.prototype.trim = function() {
				return this.replace(/(^\s*)|(\s*$)/g, "");
			}

			function checkUser(e) {
				if (13 == getPressKey(e)) {
					if ("" == document.f.j_username.value.trim()) {
						alert("请输入用户名");
						return;
					}
					document.f.j_username.focus();
				}
			}

			function checkPwd(e) {
				if (13 == getPressKey(e)) {
					if ("" == document.f.j_password.value.trim()) {
						alert("请输入用户密码");
						return;
					}
					document.f.submit();
				}
			}

			function clickLogin() {
				if ("" == document.f.j_password.value.trim()) {
					alert("请输入用户密码");
					return;
				}
				document.f.submit();
			}
		</script>
	</form>
</body>
</html>