package org.gbif.provider.webapp.action;

import com.opensymphony.xwork2.Preparable;
import org.appfuse.service.GenericManager;
import org.gbif.provider.model.ResourceMetadata;
import org.appfuse.webapp.action.BaseAction;

import java.util.List;

public class ResourceMetadataAction extends BaseAction implements Preparable {
    private GenericManager<ResourceMetadata, Long> resourceMetadataManager;
    private List resourceMetadatas;
    private ResourceMetadata resourceMetadata;
    private Long  id;

    public void setResourceMetadataManager(GenericManager<ResourceMetadata, Long> resourceMetadataManager) {
        this.resourceMetadataManager = resourceMetadataManager;
    }

    public List getResourceMetadatas() {
        return resourceMetadatas;
    }

    /**
     * Grab the entity from the database before populating with request parameters
     */
    public void prepare() {
        if (getRequest().getMethod().equalsIgnoreCase("post")) {
            // prevent failures on new
            String resourceMetadataId = getRequest().getParameter("resourceMetadata.id");
            if (resourceMetadataId != null && !resourceMetadataId.equals("")) {
                resourceMetadata = resourceMetadataManager.get(new Long(resourceMetadataId));
            }
        }
    }

    public String list() {
        resourceMetadatas = resourceMetadataManager.getAll();
        return SUCCESS;
    }

    public void setId(Long  id) {
        this. id =  id;
    }

    public ResourceMetadata getResourceMetadata() {
        return resourceMetadata;
    }

    public void setResourceMetadata(ResourceMetadata resourceMetadata) {
        this.resourceMetadata = resourceMetadata;
    }

    public String delete() {
        resourceMetadataManager.remove(resourceMetadata.getId());
        saveMessage(getText("resourceMetadata.deleted"));

        return SUCCESS;
    }

    public String edit() {
        if (id != null) {
            resourceMetadata = resourceMetadataManager.get(id);
        } else {
            resourceMetadata = new ResourceMetadata();
        }

        return SUCCESS;
    }

    public String save() throws Exception {
        if (cancel != null) {
            return "cancel";
        }

        if (delete != null) {
            return delete();
        }

        boolean isNew = (resourceMetadata.getId() == null);

        resourceMetadataManager.save(resourceMetadata);

        String key = (isNew) ? "resourceMetadata.added" : "resourceMetadata.updated";
        saveMessage(getText(key));

        if (!isNew) {
            return INPUT;
        } else {
            return SUCCESS;
        }
    }
}