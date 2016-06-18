package com.sunline.ccs.ui.server.commons;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sunline.ccs.infrastructure.server.repos.RCcsAcct;
import com.sunline.ccs.infrastructure.shared.model.CcsAcct;
import com.sunline.ccs.infrastructure.shared.model.QCcsAcct;
import com.sunline.kylin.web.core.client.exception.FlatException;
import com.sunline.ppy.dictionary.enums.InputSource;

@Controller
@RequestMapping("/applyPictureServer")
public class ApplyPictureServer {

	@Value("#{env['msxfImgUrl']?:''}")
	private String msxfImgUrl;

	@Autowired
	private RCcsAcct rCcsAcct;

	@RequestMapping(value = "/showPictureUrl", method = { RequestMethod.POST })
	@ResponseBody
	public String showPictureUrl() {
		return msxfImgUrl;
	}

	@RequestMapping(value = "/getApplicationNo", method = { RequestMethod.POST })
	@ResponseBody
	public String getApplicationNo(@RequestBody String contrNbr) {
		QCcsAcct qCcsAcct = QCcsAcct.ccsAcct;
		try {
			CcsAcct ccsAcct = rCcsAcct.findOne(qCcsAcct.contrNbr.eq(contrNbr));
			return InputSource.SUNS.equals(ccsAcct.getCustSource())?
					contrNbr:ccsAcct.getApplicationNo();
		} catch (Exception e) {
			throw new FlatException("合同号["+contrNbr+"]对应多个账户，请检查");
		}
	}
}
