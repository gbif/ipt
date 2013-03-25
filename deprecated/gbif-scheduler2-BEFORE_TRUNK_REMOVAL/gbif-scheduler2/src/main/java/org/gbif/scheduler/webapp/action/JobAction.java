package org.gbif.scheduler.webapp.action;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.gbif.scheduler.model.Job;
import org.gbif.scheduler.service.JobManager;

import com.googlecode.jsonplugin.annotations.SMDMethod;
import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.util.LocalizedTextUtil;

public class JobAction implements Preparable {
    private JobManager jobManager;
    private List jobs;
    private Job job;
    private Long  id;

    public static final String INPUT = "input";
    public static final String CANCEL = "cancel";
    public static final String SUCCESS = "success";
    
    protected static LocalizedTextUtil localizedTextUtil = new LocalizedTextUtil();
    
    /**
     * Indicator if the user clicked cancel
     */
    protected String cancel;

    /**
     * Indicator for the page the user came from.
     */
    protected String from;

    /**
     * Set to "delete" when a "delete" request parameter is passed in
     */
    protected String delete;

    /**
     * Set to "save" when a "save" request parameter is passed in
     */
    protected String save;
    
    
    
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

    /**
     * Convenience method to get the request
     * @return current request
     */
    protected HttpServletRequest getRequest() {
        return ServletActionContext.getRequest();
    }

	public String getCancel() {
		return cancel;
	}

	public void setCancel(String cancel) {
		this.cancel = cancel;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getDelete() {
		return delete;
	}

	public void setDelete(String delete) {
		this.delete = delete;
	}

	public String getSave() {
		return save;
	}

	public void setSave(String save) {
		this.save = save;
	}

	public JobManager getJobManager() {
		return jobManager;
	}

	public Long getId() {
		return id;
	}

	public void setJobs(List jobs) {
		this.jobs = jobs;
	}
    
}