package org.gbif.ipt.action.portal;


import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.manage.ResourceManager;

import com.google.inject.Inject;
import com.opensymphony.xwork2.ActionSupport;
import org.gbif.ipt.task.GenerateDCAT;

import java.io.*;

public class DCATAction extends ActionSupport {

    private final AppConfig cfg;
    private final ResourceManager resourceManager;

    private InputStream dcatInfo;

    @Inject
    public DCATAction(AppConfig cfg, ResourceManager resourceManager) {
        this.cfg = cfg;
        this.resourceManager = resourceManager;
    }

    public String execute() {
        GenerateDCAT dc = new GenerateDCAT(cfg);
        String out = "";
        out += dc.createPrefixes() + "\n\n";
        out += dc.createDCATCatalog() +"\n\n";
        for (Resource res : resourceManager.list()) {
            out += dc.createDCATDataset(res) + "\n\n";
            out += dc.createDCATDistribution(res) + "\n\n";
        }
        try {
            dcatInfo = new ByteArrayInputStream(out.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return SUCCESS;
    }

    public InputStream getDcatInfo() {
        return dcatInfo;
    }

}
