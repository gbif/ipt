/**
 * 
 */
package org.gbif.scheduler.webapp.listener;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.gbif.scheduler.dao.JobDao;
import org.gbif.scheduler.scheduler.Scheduler;

/**
 * @author timrobertson
 */
public class SchedulerStartupListener extends ContextLoaderListener implements
		ServletContextListener {
	private static final Log logger = LogFactory.getLog(SchedulerStartupListener.class);
	
	/* (non-Javadoc)
	 * @see org.springframework.web.context.ContextLoaderListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		super.contextDestroyed(event);
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.context.ContextLoaderListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void contextInitialized(ServletContextEvent event) {
		logger.info("Creating scheduler");
		ServletContext context = event.getServletContext();
        ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
        try {
        	JobDao jobDao = (JobDao) ctx.getBean("jobDao");
        	Scheduler scheduler = new Scheduler(InetAddress.getLocalHost().getHostName(), ctx, event.getServletContext(), jobDao);
        	scheduler.start();
        	
            
        } catch (NoSuchBeanDefinitionException n) {
        	logger.info("No such bean definition: " + n);
            // ignore, should only happen when testing
        } catch (UnknownHostException e) {
        	logger.error("Cannot start scheduler as cannot get the machines host name: " + e.getMessage(), e);
        }
	}
	
	/*
	class Watcher implements Runnable {
		boolean appStopping = false;
		GenericDao<SchedulerState, Long> schedulerStateDao;
		int instanceId;
		public Watcher(GenericDao<SchedulerState, Long> schedulerStateDao, int instanceId) {
			this.schedulerStateDao=schedulerStateDao;
			this.instanceId=instanceId;
		}
		public void run() {
			while(!appStopping) {
				logger.info("Watcher scheduler for state change...");
				SchedulerState state = schedulerStateDao.get(new Long(instanceId));
				if (state.getState()==SchedulerManager.STATE_STARTING) {
					logger.info("Moving the scheduler to running");
					state.setState(SchedulerManager.STATE_RUNNING);
					schedulerStateDao.save(state);
				} else if (state.getState()==SchedulerManager.STATE_STOPPING) {
					logger.info("Moving the scheduler to running");
					state.setState(SchedulerManager.STATE_STOPPED);
					schedulerStateDao.save(state);
				}
				// 7500 is Quartz default so... 
				try {
					Thread.sleep(7500);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	*/
}
