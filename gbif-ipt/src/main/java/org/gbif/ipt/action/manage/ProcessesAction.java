package org.gbif.ipt.action.manage;

import static org.gbif.ipt.config.Constants.SESSION_PROCESSES_DWCA;

import org.gbif.ipt.action.BaseAction;
import org.gbif.ipt.service.manage.ResourceManager;
import org.gbif.ipt.task.StatusReport;

import com.google.inject.Inject;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ProcessesAction extends BaseAction {
  @Inject
  private ResourceManager resourceManager;
  private Map<String, StatusReport> reports = new HashMap<String, StatusReport>();
  private Date now = new Date();

  @Override
  public String execute() throws Exception {
    // see if any resources are marked in session
    if (session.containsKey(SESSION_PROCESSES_DWCA)) {
      List<String> resources = (List<String>) session.get(SESSION_PROCESSES_DWCA);
      // show all processes for the current user
      Iterator<String> iter = resources.iterator();
      while (iter.hasNext()) {
        String shortname = iter.next();
        StatusReport report = resourceManager.status(shortname);
        reports.put(shortname, report);
        if (report.isCompleted()) {
          // finito, remove from session
          log.debug("Resource " + shortname + " unlocked, removed from session");
          iter.remove();
        }
      }
    }
    // check if complete - in that case remove from session
    return SUCCESS;
  }

  public Date getNow() {
    return now;
  }

  public Map<String, StatusReport> getReports() {
    return reports;
  }

}
