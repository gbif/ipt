/**
 * 
 */
package org.gbif.logging.model;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.appfuse.model.BaseObject;
import org.appfuse.model.User;

import org.gbif.util.JSONUtils;

/**
 * Not that the JSON params and info are serialized as JSON internally
 * @author timrobertson
 */
@SuppressWarnings("unchecked")
@Entity
public class LogEvent extends BaseObject implements Comparable {
	private static final long serialVersionUID = -6372406371118429847L;
	private Long id;
	private int sourceType;
	private int sourceId;
	private String instanceId;
	private int groupId;
	private int level;
	private User user;
	private String message;
	private String messageParamsAsJSON;
	private String infoAsJSON;
	private Date timestamp = new Date();

	// based on trace, debug, info, warn, error, fatal
	public static final int LEVEL_UNKNOWN = 0;
	public static final int LEVEL_TRACE = 1;
	public static final int LEVEL_DEBUG = 2;
	public static final int LEVEL_INFO = 3;
	public static final int LEVEL_WARN = 4;
	public static final int LEVEL_ERROR = 5;
	public static final int LEVEL_FATAL = 6;
	
	public LogEvent() {
	}
	
	public LogEvent(int sourceType, int sourceId, String instanceId,
			int groupId, int level, User user, String message) {
		this.sourceId=sourceId;
		this.sourceType=sourceType;
		this.instanceId = instanceId;
		this.groupId = groupId;
		this.level = level;
		this.user = user;
		this.message = message;
	}
	
	public LogEvent(int sourceType, int sourceId, String instanceId,
			int groupId, int level, User user, String message,
			String[] messageParams) {
		this.sourceId=sourceId;
		this.sourceType=sourceType;
		this.instanceId = instanceId;
		this.groupId = groupId;
		this.level = level;
		this.user = user;
		this.message = message;
		setMessageParams(messageParams);
	}

	public LogEvent(int sourceType, int sourceId, String instanceId,
			int groupId, int level, User user, String message,
			String[] messageParams, String infoAsJSON) {
		this.sourceId=sourceId;
		this.sourceType=sourceType;
		this.instanceId = instanceId;
		this.groupId = groupId;
		this.level = level;
		this.user = user;
		this.message = message;
		this.infoAsJSON = infoAsJSON;
		setMessageParams(messageParams);
	}

	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	

	/**
	 * The kind(class) of job/process that generated the logEvent.
	 * Should be used in combination with @sourceId
	 * @return
	 */
	public int getSourceType() {
		return sourceType;
	}
	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}

	/**
	 * The exact id of the job/process that generated this logEvent.
	 * Should be used in combination with @sourceType
	 * @return
	 */
	public int getSourceId() {
		return sourceId;
	}
	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * Instance ID of the thread/scheduler/VM/container
	 * Not used yet, but provides a way to keep logEvents from multiple threads in a single DB
	 * @return
	 */
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	/**
	 * A logical grouping of events, for example by resource/datasource
	 * @return
	 */
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	/**
	 * The user who eventually generated this logEvent by kicking off some process or running an action directly
	 * @return
	 */
	@ManyToOne
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@Column(columnDefinition="text")
	public String getInfoAsJSON() {
		return infoAsJSON;
	}
	public void setInfoAsJSON(String info) {
		this.infoAsJSON = info;
	}
	@Column(columnDefinition="text")
	public String getMessageParamsAsJSON() {
		return messageParamsAsJSON;
	}
	public void setMessageParamsAsJSON(String messageParamsAsJSON) {
		this.messageParamsAsJSON = messageParamsAsJSON;
	}
	@Transient
	public String[] getMessageParams() {
		if (StringUtils.isNotBlank(messageParamsAsJSON)) {
			Map<String, Object> data = JSONUtils.mapFromJSON(messageParamsAsJSON);
			if (data != null
					&& data.containsKey("list")) {
					List<String> list = (List<String>) data.get("list");
					return list.toArray(new String[list.size()]);
			}
		}
		return null;
	}
	@Transient
	public void setMessageParams(String[] messageParams) {
		if (messageParams != null 
				&& messageParams.length>0) {
			Map<String, Object> data = new HashMap<String, Object>();
			List<String> list = new LinkedList<String>();
			for (String param : messageParams) {
				list.add(param);
			}
			data.put("list", list);
			this.messageParamsAsJSON = JSONUtils.jsonFromMap(data);
		}
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		LogEvent myClass = (LogEvent) object;
		return new CompareToBuilder().append(this.messageParamsAsJSON,
				myClass.messageParamsAsJSON).append(this.level, myClass.level)
				.append(this.groupId, myClass.groupId).append(this.sourceId,
						myClass.sourceId).append(this.user, myClass.user)
				.append(this.infoAsJSON, myClass.infoAsJSON).append(
						this.message, myClass.message).append(this.timestamp,
						myClass.timestamp).append(this.sourceType,
						myClass.sourceType).append(this.id, myClass.id).append(
						this.instanceId, myClass.instanceId).toComparison();
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof LogEvent)) {
			return false;
		}
		LogEvent rhs = (LogEvent) object;
		return new EqualsBuilder().append(
				this.messageParamsAsJSON, rhs.messageParamsAsJSON).append(
				this.level, rhs.level).append(this.groupId, rhs.groupId)
				.append(this.sourceId, rhs.sourceId)
				.append(this.user, rhs.user).append(this.infoAsJSON,
						rhs.infoAsJSON).append(this.message, rhs.message)
				.append(this.timestamp, rhs.timestamp).append(this.sourceType,
						rhs.sourceType).append(this.id, rhs.id).append(
						this.instanceId, rhs.instanceId).isEquals();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(1437223807, 1368332751).append(this.messageParamsAsJSON).append(
				this.level).append(this.groupId).append(this.sourceId).append(
				this.user).append(this.infoAsJSON).append(this.message).append(
				this.timestamp).append(this.sourceType).append(this.id).append(
				this.instanceId).toHashCode();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("messageParamsAsJSON",
				this.messageParamsAsJSON).append("id", this.id).append(
				"instanceId", this.instanceId).append("user", this.user)
				.append("messageParams", this.getMessageParams()).append(
						"message", this.message).append("timestamp",
						this.timestamp).append("groupId", this.groupId).append(
						"infoAsJSON", this.infoAsJSON).append("sourceId",
						this.sourceId).append("sourceType", this.sourceType)
				.append("level", this.level).toString();
	}
}
