package org.gbif.provider.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.gbif.provider.service.RegistryManager;

public class RegistryManagerImpl extends HttpBaseManager implements RegistryManager{
	public static final String REGISTRY_ORG_URL = "http://gbrds.gbif.org/registry/organization";
	public static final String REGISTRY_SERVICE_URL = "http://gbrds.gbif.org/registry/service";
	public static final String REGISTRY_NODE_URL = "http://gbrds.gbif.org/registry/node";

	public List<URI> listExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean registerOrg() {
		// need to register a new organisation?
		if (StringUtils.trimToNull(cfg.getOrg().getUddiID())!=null){
			log.warn("The organisation is already registered");
			return false;
		}		
		setRegistryCredentials();
		// http://code.google.com/p/gbif-registry/wiki/ExplanationUDDI#CREATE_ORGANIZATION
        NameValuePair[] data = {
                new NameValuePair("organizationName", StringUtils.trimToEmpty(cfg.getOrg().getTitle())),
                new NameValuePair("organizationDescription", StringUtils.trimToEmpty(cfg.getOrg().getDescription())),
                new NameValuePair("homepageURL", StringUtils.trimToEmpty(cfg.getOrg().getLink())),
                new NameValuePair("primaryContactName", StringUtils.trimToEmpty(cfg.getOrg().getContactName())),
                new NameValuePair("primaryContactEmail", StringUtils.trimToEmpty(cfg.getOrg().getContactEmail())),
                new NameValuePair("endorsingNodeKey", StringUtils.trimToEmpty(cfg.getOrgNode()))
        };
        return executePost(REGISTRY_ORG_URL,  data, true);
	}

	private void setRegistryCredentials() {
		try {
			URI geoURI = new URI(REGISTRY_ORG_URL);
			String domain = geoURI.getHost();
			AuthScope scope = new AuthScope(domain, -1);
			setCredentials(scope, cfg.getOrg().getUddiID(), cfg.getOrgPassword());		
		} catch (URISyntaxException e) {
			log.error("Exception setting the registry credentials", e);
		}
	}

	
	/* Service Binding: IPT RSS feed
	 * Service Binding: IPT EML
	 * (non-Javadoc)
	 * @see org.gbif.provider.service.RegistryManager#registerIPT()
	 */
	public boolean registerIPT() {
		// need to register a new organisation?
		if (StringUtils.trimToNull(cfg.getIpt().getUddiID())==null){
			log.warn("This IPT is already registered");
			return false;
		}
		setRegistryCredentials();
		log.warn("IPT service registration not implemented");
		// register RSS resource feed

		// register IPT EML
		
		return false;
	}

	/* 
	 * Potential services to register if available: 
	 * Service Binding: TAPIR Intermediate
	 * Service Binding: DwC Archive
	 * Service Binding: TCS Archive
	 * Service Binding: EML
	 * Service Binding: WMS
	 * Service Binding: WFS
	 * 
	 * (non-Javadoc)
	 * @see org.gbif.provider.service.RegistryManager#registerResource(java.lang.Long)
	 */
	public boolean registerResource(Long resourceId) {
		return false;
	}

	
	private String getOrganisationUri(){
		return String.format("%s/%s", REGISTRY_ORG_URL, cfg.getOrg().getUddiID());
	}
	
	public boolean testLogin() {
		// http://server:port/registration/organization/30?op=login
		setRegistryCredentials();
        NameValuePair[] params = {
                new NameValuePair("op", "login")
        };
		return executeGet(getOrganisationUri(), params, true);
	}
}
