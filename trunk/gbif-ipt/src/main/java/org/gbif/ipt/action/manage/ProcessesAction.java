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
  private static final String SESSION_PROCESSES_DONE = "done_processes_dwca";
  private static final int interval = 20000;
  @Inject
  private ResourceManager resourceManager;
  private Map<String, StatusReport> reports = new HashMap<String, StatusReport>();
  private Date now = new Date();
  private boolean allCompleted = false;
  private boolean purge = false;

  @Override
  public String execute() throws Exception {
    // see if any resources are marked in session
    if (session.containsKey(SESSION_PROCESSES_DWCA)) {
      // keep processes recently completed in the session too so we can show them until the last one finished
      if (!session.containsKey(SESSION_PROCESSES_DONE)) {
        session.put(SESSION_PROCESSES_DONE, new HashMap<String, Long>());
      }
      Map<String, Long> completed = (Map<String, Long>) session.get(SESSION_PROCESSES_DONE);

      List<String> resources = (List<String>) session.get(SESSION_PROCESSES_DWCA);
      // show all processes for the current user
      Iterator<String> iter = resources.iterator();
      while (iter.hasNext()) {
        String shortname = iter.next();
        StatusReport report = resourceManager.status(shortname);
        reports.put(shortname, report);
        if (report.isCompleted()) {
          // finito, remove from session
          iter.remove();
          // add to completed
          completed.put(shortname, new Date().getTime());
        }
      }
      // have all resources completed by now?
      if (resources.isEmpty()) {
        allCompleted = true;
      }
      for (String shortname : completed.keySet()) {
        reports.put(shortname, resourceManager.status(shortname));
      }
    } else {
      allCompleted = true;
    }
    // if all are completed remove completed session all together
    if (allCompleted || purge) {
      log.debug("Purging completed processes from session");
      session.remove(SESSION_PROCESSES_DONE);
    }
    return SUCCESS;
  }

  public Date getNow() {
    return now;
  }

  public Map<String, StatusReport> getReports() {
    return reports;
  }

  public boolean isAllCompleted() {
    return allCompleted;
  }

  public void setPurge(boolean purge) {
    this.purge = purge;
  }

}
