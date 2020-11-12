package org.gbif.ipt.action.admin;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gbif.ipt.action.POSTAction;
import org.gbif.ipt.config.AppConfig;
import org.gbif.ipt.config.Constants;
import org.gbif.ipt.model.Resource;
import org.gbif.ipt.service.admin.RegistrationManager;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.struts2.SimpleTextProvider;

import java.math.BigDecimal;

public class DeleteVersionAction extends POSTAction {

    private static final Logger LOG = LogManager.getLogger(DeleteVersionAction.class);

    protected final ResourceManager resourceManager;
    protected Resource resource;
    protected String version;

    @Inject
    public DeleteVersionAction(SimpleTextProvider textProvider, AppConfig cfg, RegistrationManager registrationManager, ResourceManager resourceManager) {
        super(textProvider, cfg, registrationManager);
        this.resourceManager = resourceManager;
    }

    @Override
    public void prepare() {
        super.prepare();
        // look for resource parameter
        String res = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_RESOURCE));
        resource = resourceManager.get(res);
        if (resource == null) {
            notFound = true;
        }
        version = StringUtils.trimToNull(req.getParameter(Constants.REQ_PARAM_VERSION));
        if (version == null) {
            notFound = true;
        }
    }

    @Override
    public String execute() {
        resourceManager.removeVersion(resource, new BigDecimal(version));
        return SUCCESS;
    }
}
