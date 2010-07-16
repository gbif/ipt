package org.gbif.ipt.action.manage;

import java.util.ArrayList;
import java.util.List;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.model.Resource;

public class HomeAction extends BaseAction{
	private List<Resource> resources = new ArrayList<Resource>();
	
	public String menu(){
		return SUCCESS;
	}
	
	
	
	
	public List<Resource> getResources() {
		return resources;
	}
	
}
