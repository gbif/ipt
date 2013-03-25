/**
 * 
 */
package org.gbif.scheduler.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A launch action is "something" that can be run.
 * Typically they are associated with a datasource type, in order for
 * extensible operations that can be written and attached to types of data
 * sources.
 * This is an idea in progress, but the goal is to be able to write a new 
 * action (e.g. create a new index of names) and then add it to the datasource
 * types that support it, and then seemlessly add new links to the web application,
 * so that the operation can be run.
 * 
 * Can be run as something that creates a new instance, or else gets an instance and
 * runs it
 * 
 * fullClassName: org.gbif.scheduler.Hello
 * methodname: hello
 * methodParams: world,null,yes
 * 
 * results in:
 * 
 * new org.gbif.scheduler.Hello()
 *   .hello(
 *   	getWorld(),
 *   	null,
 *   	getYes()
 *   );
 * 
 * from the class executing this launchAction
 *  
 * instanceParam: springHello
 * methodname: hello
 * methodParams: world,null,yes
 * 
 * results in:
 * 
 * getSpringHello()
 *   .hello(
 *   	getWorld(),
 *   	null,
 *   	getYes()
 *   );
 * 
 * from the class executing this launchAction
 *  
 *  
 * @author timrobertson
 */
@SuppressWarnings("unchecked")
@Entity
public class LaunchAction extends BaseObject implements Comparable {
	private static final long serialVersionUID = -5126289262066823835L;
	private Long id;
	// the i18n key associated with this action
	private String i18nKey;
	// the class that will be instanciated and called
	private String fullClassName;
	// the instance class that will be reused
	private String instanceParam;
	private String methodName;
	// use comma separated list of params to pull from the calling class
	// e.g. id, null, name
	private String methodParams;
	
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getI18nKey() {
		return i18nKey;
	}
	public void setI18nKey(String key) {
		i18nKey = key;
	}
	public String getFullClassName() {
		return fullClassName;
	}
	public void setFullClassName(String fullClassName) {
		this.fullClassName = fullClassName;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public String getMethodParams() {
		return methodParams;
	}
	public void setMethodParams(String methodParams) {
		this.methodParams = methodParams;
	}
	public String getInstanceParam() {
		return instanceParam;
	}
	public void setInstanceParam(String instanceParam) {
		this.instanceParam = instanceParam;
	}
	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object object) {
		LaunchAction myClass = (LaunchAction) object;
		return new CompareToBuilder().append(this.i18nKey, myClass.i18nKey)
				.append(this.instanceParam, myClass.instanceParam).append(
						this.fullClassName, myClass.fullClassName).append(
						this.methodParams, myClass.methodParams).append(
						this.methodName, myClass.methodName).append(this.id,
						myClass.id).toComparison();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof LaunchAction)) {
			return false;
		}
		LaunchAction rhs = (LaunchAction) object;
		return new EqualsBuilder().append(
				this.i18nKey, rhs.i18nKey).append(this.instanceParam,
				rhs.instanceParam)
				.append(this.fullClassName, rhs.fullClassName).append(
						this.methodParams, rhs.methodParams).append(
						this.methodName, rhs.methodName)
				.append(this.id, rhs.id).isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(186118373, 844149715).append(this.i18nKey).append(
				this.instanceParam).append(this.fullClassName).append(
				this.methodParams).append(this.methodName).append(this.id)
				.toHashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("methodParams",
				this.methodParams).append("id", this.id).append("I18nKey",
				this.getI18nKey()).append("methodName", this.methodName)
				.append("fullClassName", this.fullClassName).append(
						"instanceParam", this.instanceParam).toString();
	}
	
}
