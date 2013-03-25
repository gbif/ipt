package org.gbif.scheduler.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A simplified version of Quartz Trigger and Job model
 * This is flattened into one object
 * Only support a one time trigger (Quartz simple trigger)
 * that has a datetime from which it is eligible for execution.
 * 
 * Execution tracks the instance identifier, to allow for multi-server
 * operation
 * 
 * @author timrobertson
 */
@Entity
@Table(name="job")
public class Job extends BaseObject implements Comparable {
	/**
	 * Since we are persisting and serializing this is probably important
	 */
	private static final long serialVersionUID = 585151693731975545L;
	private Long id;
	
	// type name and description are used for 'human' identification
	// for example one could choose:
	// jobGroup: datasource[1]
	// name: inventory
	// description: TAPIR inventory of datasource 1
	private String jobGroup;
	private String name;
	private String description;
	
	// running jobGroup is an optional logical grouping of JOBs
	// such only 1 JOB from the running jobGroup will be executed at any time
	// this means that one can queue up jobs for sequential operation
	// typically one would use the datasource URL to ensure that JOBs are sequentially
	// operated on the URL (metadata, inventory then harvest for example)
	private String runningGroup;
	
	// the Runnable or Launchable class to execute, and the data to receive
	private String jobClassName;
	private String dataAsJSON;
	
	// running parameters associated with the job indicating the creation, its 
	// time for eligible execution, when it was started and the instance that is 
	// running the job (for multi-server use)
	private Date created = new Date();
	private Date nextFireTime = new Date();
	private Date started;
	private String instanceId;
	private int repeatInDays = 0;
	

	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getJobGroup() {
		return jobGroup;
	}
	public void setJobGroup(String group) {
		this.jobGroup = group;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getRunningGroup() {
		return runningGroup;
	}
	public void setRunningGroup(String runningGroup) {
		this.runningGroup = runningGroup;
	}
	public String getJobClassName() {
		return jobClassName;
	}
	public void setJobClassName(String jobClassName) {
		this.jobClassName = jobClassName;
	}
	@Column(columnDefinition="text")
	public String getDataAsJSON() {
		return dataAsJSON;
	}
	public void setDataAsJSON(String dataAsJSON) {
		this.dataAsJSON = dataAsJSON;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getNextFireTime() {
		return nextFireTime;
	}
	public void setNextFireTime(Date nextFireTime) {
		this.nextFireTime = nextFireTime;
	}
	public Date getStarted() {
		return started;
	}
	public void setStarted(Date started) {
		this.started = started;
	}
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public int getRepeatInDays() {
		return repeatInDays;
	}
	public void setRepeatInDays(int repeatInDays) {
		this.repeatInDays = repeatInDays;
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		Job myClass = (Job) object;
		return new CompareToBuilder().append(this.created, myClass.created)
				.append(this.jobClassName, myClass.jobClassName).append(
						this.jobGroup, myClass.jobGroup).append(this.started,
						myClass.started).append(this.nextFireTime,
						myClass.nextFireTime).append(this.runningGroup,
						myClass.runningGroup).append(this.description,
						myClass.description).append(this.dataAsJSON,
						myClass.dataAsJSON).append(this.name, myClass.name)
				.append(this.id, myClass.id).append(this.instanceId,
						myClass.instanceId).toComparison();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof Job)) {
			return false;
		}
		Job rhs = (Job) object;
		return new EqualsBuilder().append(
				this.created, rhs.created).append(this.jobClassName,
				rhs.jobClassName).append(this.jobGroup, rhs.jobGroup).append(
				this.started, rhs.started).append(this.nextFireTime,
				rhs.nextFireTime).append(this.runningGroup, rhs.runningGroup)
				.append(this.description, rhs.description).append(
						this.dataAsJSON, rhs.dataAsJSON).append(this.name,
						rhs.name).append(this.id, rhs.id).append(
						this.instanceId, rhs.instanceId).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(492324217, 1592433253).append(this.created)
				.append(this.jobClassName).append(this.jobGroup).append(
						this.started).append(this.nextFireTime).append(
						this.runningGroup).append(this.description).append(
						this.dataAsJSON).append(this.name).append(this.id)
				.append(this.instanceId).toHashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this)
			.append("id", this.id)
			.append("jobGroup", this.jobGroup)
			.append("name", this.name)
			.append("description", this.description)
			.append("nextFireTime",this.nextFireTime)
			.append("started", this.started)
			.append("runningGroup",this.runningGroup)
			.append("instanceId", this.instanceId)
			.append("created", this.created)
			.append("jobClassName",this.jobClassName)
			.append("dataAsJSON", this.dataAsJSON)
			.toString();
	}
	
}
