package org.gbif.scheduler.webapp.action;

import com.opensymphony.xwork2.Preparable;
import org.appfuse.service.GenericManager;
import org.appfuse.webapp.action.BaseAction;

import com.googlecode.jsonplugin.annotations.SMDMethod;

import org.gbif.scheduler.model.Job;
import org.gbif.scheduler.service.JobManager;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class JobAction extends BaseAction implements Preparable {
    private JobManager jobManager;
    private List jobs;
    private Job job;
    private Long  id;

    public void setJobManager(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    public List getJobs() {
        return jobs;
    }

    /**
     * Grab the entity from the database before populating with request parameters
     */
    public void prepare() {
        if (getRequest().getMethod().equalsIgnoreCase("post")) {
            // prevent failures on new
            String jobId = getRequest().getParameter("job.id");
            if (jobId != null && !jobId.equals("")) {
                job = jobManager.get(new Long(jobId));
            }
        }
    }

    public String list() {
        jobs = jobManager.getAll();
        return SUCCESS;
    }

    public void setId(Long  id) {
        this. id =  id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public String delete() {
        jobManager.remove(job.getId());
        saveMessage(getText("job.deleted"));

        return SUCCESS;
    }

    public String edit() {
        if (id != null) {
            job = jobManager.get(id);
        } else {
            job = new Job();
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

        boolean isNew = (job.getId() == null);

        jobManager.save(job);

        String key = (isNew) ? "job.added" : "job.updated";
        saveMessage(getText(key));

        if (!isNew) {
            return INPUT;
        } else {
            return SUCCESS;
        }
    }
    
    @SMDMethod
    public List<Job> getAll() throws IOException {
    	return jobManager.getAllJobs();
    }     
    
}