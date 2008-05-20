package org.gbif.provider.webapp.action;

import com.opensymphony.xwork2.Preparable;
import org.appfuse.service.GenericManager;
import org.gbif.provider.model.Datasource;
import org.appfuse.webapp.action.BaseAction;

import java.util.List;

public class DatasourceAction extends BaseAction implements Preparable {
    private GenericManager<Datasource, Long> datasourceManager;
    private List datasources;
    private Datasource datasource;
    private Long  id;

    public void setDatasourceManager(GenericManager<Datasource, Long> datasourceManager) {
        this.datasourceManager = datasourceManager;
    }

    public List getDatasources() {
        return datasources;
    }

    /**
     * Grab the entity from the database before populating with request parameters
     */
    public void prepare() {
        if (getRequest().getMethod().equalsIgnoreCase("post")) {
            // prevent failures on new
            String datasourceId = getRequest().getParameter("datasource.id");
            if (datasourceId != null && !datasourceId.equals("")) {
                datasource = datasourceManager.get(new Long(datasourceId));
            }
        }
    }

    public String list() {
        datasources = datasourceManager.getAll();
        return SUCCESS;
    }

    public void setId(Long  id) {
        this. id =  id;
    }

    public Datasource getDatasource() {
        return datasource;
    }

    public void setDatasource(Datasource datasource) {
        this.datasource = datasource;
    }

    public String delete() {
        datasourceManager.remove(datasource.getId());
        saveMessage(getText("datasource.deleted"));

        return SUCCESS;
    }

    public String edit() {
        if (id != null) {
            datasource = datasourceManager.get(id);
        } else {
            datasource = new Datasource();
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

        boolean isNew = (datasource.getId() == null);

        datasourceManager.save(datasource);

        String key = (isNew) ? "datasource.added" : "datasource.updated";
        saveMessage(getText(key));

        if (!isNew) {
            return INPUT;
        } else {
            return SUCCESS;
        }
    }
}