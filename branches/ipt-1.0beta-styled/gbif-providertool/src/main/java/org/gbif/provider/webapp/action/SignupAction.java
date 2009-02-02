package org.gbif.provider.webapp.action;

import org.gbif.provider.util.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;

public class SignupAction extends org.appfuse.webapp.action.SignupAction{
	@Autowired
	private AppConfig iptCfg;

	public AppConfig getIptCfg() {
		return iptCfg;
	}
}
