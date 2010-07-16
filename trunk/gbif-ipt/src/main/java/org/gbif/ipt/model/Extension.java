package org.gbif.ipt.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** A Darwin Core extension definition
 * @author markus
 *
 */
public class Extension {
	private String title; // humna title
	private String name; // table, file & xml tag naming. no whitespace allowed
	private URL url;
	private String rowType;
	private String subject;
	private String description;
	private String namespace;
	private URL link; // to documentation
	private boolean installed;
	private List<ExtensionProperty> properties = new ArrayList<ExtensionProperty>();
	private boolean core = false;
	private Date modified = new Date();

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name.replaceAll("\\s", "_");
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String getRowType() {
		return rowType;
	}

	public void setRowType(String rowType) {
		this.rowType = rowType;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public URL getLink() {
		return link;
	}

	public void setLink(URL link) {
		this.link = link;
	}
	public void setLink(String link) {
		URL url;
		try {
			url = new URL(link);
			this.link = url;
		} catch (MalformedURLException e) {
		}
	}

	public boolean isInstalled() {
		return installed;
	}

	public void setInstalled(boolean installed) {
		this.installed = installed;
	}

	public List<ExtensionProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<ExtensionProperty> properties) {
		this.properties = properties;
	}

	public boolean isCore() {
		return core;
	}

	public void setCore(boolean core) {
		this.core = core;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}


	public void addProperty(ExtensionProperty property) {
		property.setExtension(this);
		properties.add(property);
	}

	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Extension object) {
		return new CompareToBuilder().append(this.url, object.url).toComparison();
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Extension)) {
			return false;
		}
		Extension rhs = (Extension) object;
		return new EqualsBuilder().append(this.url, rhs.url).isEquals();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = 17;
		result = (url!= null ? url.hashCode() : 0);
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
		result = 31 * result + (link != null ? link.hashCode() : 0);
		return result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", this.name).append("url",this.url).toString();
	}

}
