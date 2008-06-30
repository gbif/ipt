/**
 * 
 */
package org.gbif.scheduler.model;

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

import com.ibiodiversity.harvest.JSONUtils;

/**
 * Not that the JSON params and info are serialized as JSON internally
 * @author timrobertson
 */
@SuppressWarnings("unchecked")
@Entity
public class LogEvent extends BaseObject implements Comparable {
	private static final long serialVersionUID = -6372406371118429847L;
	private Long id;
	private BioDatasource bioDatasource;
	private String instanceId;
	private Long groupId;
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
	
	public LogEvent(BioDatasource bioDatasource, String instanceId,
			Long groupId, int level, User user, String message) {
		this.bioDatasource = bioDatasource;
		this.instanceId = instanceId;
		this.groupId = groupId;
		this.level = level;
		this.user = user;
		this.message = message;
	}
	
	public LogEvent(BioDatasource bioDatasource, String instanceId,
			Long groupId, int level, User user, String message,
			String[] messageParams) {
		this.bioDatasource = bioDatasource;
		this.instanceId = instanceId;
		this.groupId = groupId;
		this.level = level;
		this.user = user;
		this.message = message;
		setMessageParams(messageParams);
	}

	public LogEvent(BioDatasource bioDatasource, String instanceId,
			Long groupId, int level, User user, String message,
			String[] messageParams, String infoAsJSON) {
		this.bioDatasource = bioDatasource;
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
	@ManyToOne
	public BioDatasource getBioDatasource() {
		return bioDatasource;
	}
	public void setBioDatasource(BioDatasource bioDatasource) {
		this.bioDatasource = bioDatasource;
	}
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public Long getGroupId() {
		return groupId;
	}
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
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
				.append(this.groupId, myClass.groupId).append(this.user,
						myClass.user).append(this.infoAsJSON,
						myClass.infoAsJSON).append(this.message,
						myClass.message).append(this.bioDatasource,
						myClass.bioDatasource).append(this.id, myClass.id)
				.append(this.instanceId, myClass.instanceId).toComparison();
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
				.append(this.user, rhs.user).append(this.infoAsJSON,
						rhs.infoAsJSON).append(this.message, rhs.message)
				.append(this.bioDatasource, rhs.bioDatasource).append(this.id,
						rhs.id).append(this.instanceId, rhs.instanceId)
				.isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(-501640229, 879575769).append(this.messageParamsAsJSON).append(
				this.level).append(this.groupId).append(this.user).append(
				this.infoAsJSON).append(this.message)
				.append(this.bioDatasource).append(this.id).append(
						this.instanceId).toHashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("id", this.id).append("info",
				this.getInfoAsJSON()).append("instanceId", this.instanceId).append(
				"messageParams", this.getMessageParams()).append("user",
				this.user).append("groupId", this.groupId).append("message",
				this.message).append("bioDatasource", this.bioDatasource)
				.append("level", this.level).toString();
	}
}
