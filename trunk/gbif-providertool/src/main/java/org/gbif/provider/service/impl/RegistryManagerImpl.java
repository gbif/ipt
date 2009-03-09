package org.gbif.provider.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.ResourceMetadata;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.model.xml.NewRegistryEntryHandler;
import org.gbif.provider.model.xml.ResourceMetadataHandler;
import org.gbif.provider.service.RegistryManager;
import org.gbif.provider.util.XmlContentHandler;
import org.xml.sax.SAXException;

public class RegistryManagerImpl extends HttpBaseManager implements RegistryManager{
	public static final String REGISTRY_ORG_URL = "http://gbrds.gbif.org/registry/organization";
	public static final String REGISTRY_RESOURCE_URL = "http://gbrds.gbif.org/registry/resource";
	public static final String REGISTRY_SERVICE_URL = "http://gbrds.gbif.org/registry/service";
	public static final String REGISTRY_NODE_URL = "http://gbrds.gbif.org/registry/node";
	private ResourceMetadataHandler metaHandler = new ResourceMetadataHandler();
	private NewRegistryEntryHandler newRegistryEntryHandler = new NewRegistryEntryHandler();
	
    private SAXParser saxParser;

    
    
	public RegistryManagerImpl() throws ParserConfigurationException, SAXException {
		super();
		SAXParserFactory factory = SAXParserFactory.newInstance();		 
		saxParser=factory.newSAXParser();
	}

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
                new NameValuePair("name", StringUtils.trimToEmpty(cfg.getOrg().getTitle())),
                new NameValuePair("description", StringUtils.trimToEmpty(cfg.getOrg().getDescription())),
                new NameValuePair("homepageURL", StringUtils.trimToEmpty(cfg.getOrg().getLink())),
                new NameValuePair("primaryContactName", StringUtils.trimToEmpty(cfg.getOrg().getContactName())),
                new NameValuePair("primaryContactEmail", StringUtils.trimToEmpty(cfg.getOrg().getContactEmail())),
                new NameValuePair("endorsingNodeKey", StringUtils.trimToEmpty(cfg.getOrgNode()))
        };
        String result = executePost(REGISTRY_ORG_URL,  data, true);
        if (result!=null){
            // read new UDDI ID
        	System.out.println(result);
			try {
				saxParser.parse(getStream(result), newRegistryEntryHandler);
				cfg.setOrgPassword(newRegistryEntryHandler.password);
				cfg.getOrg().setUddiID(newRegistryEntryHandler.organisationKey);
				log.info("A new organisation has been registered with GBIF under node "+ cfg.getOrgNode() +" and with key "+cfg.getOrg().getUddiID());
	            return true;        	
			} catch (SAXException e) {
				log.error("Error reading GBIF registry response", e);
			} catch (IOException e) {
				log.error("Error reading GBIF registry response", e);
			}
        }
        return false;
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
		// registering IPT resource
		String key = registerResource(cfg.getIpt());
		if (key!=null){
			cfg.getIpt().setUddiID(key);
			log.info("The IPT has been registered with GBIF as resource "+ cfg.getIpt().getUddiID());
			// RSS resource feed service
	    	registerService(key, ServiceType.RSS, cfg.getAtomFeedURL());
			// IPT EML service
	    	registerService(key, ServiceType.EML, cfg.getEmlUrl("ipt"));
	        return true;        	
		}    	
        return false;        	
	}

	private String registerResource(ResourceMetadata meta){
		// registering IPT resource
        NameValuePair[] data = {
                new NameValuePair("organizationKey", StringUtils.trimToEmpty(cfg.getOrg().getUddiID())),
                new NameValuePair("name", StringUtils.trimToEmpty(meta.getTitle())),
                new NameValuePair("description", StringUtils.trimToEmpty(meta.getDescription())),
                new NameValuePair("homepageURL", StringUtils.trimToEmpty(meta.getLink())),
                new NameValuePair("primaryContactName", StringUtils.trimToEmpty(meta.getContactName())),
                new NameValuePair("primaryContactEmail", StringUtils.trimToEmpty(meta.getContactEmail()))
        };
        String result = executePost(REGISTRY_ORG_URL,  data, true);
        if (result!=null){
            // read new UDDI ID
        	System.out.println(result);
			try {
				saxParser.parse(getStream(result), newRegistryEntryHandler);
				log.info("A new resource has been registered with GBIF. Key = "+ newRegistryEntryHandler.resourceKey);
				return newRegistryEntryHandler.resourceKey;
			} catch (SAXException e) {
				log.error("Error reading GBIF registry response", e);
			} catch (IOException e) {
				log.error("Error reading GBIF registry response", e);
			}
        }
		return null;
	}
	
	private String registerService(String resourceKey, ServiceType serviceType, String accessPointURL){
        NameValuePair[] data = {
                new NameValuePair("resourceKey", StringUtils.trimToEmpty(resourceKey)),
                new NameValuePair("accessPointType", serviceType.tModel),
                new NameValuePair("accessPointURL", StringUtils.trimToEmpty(accessPointURL))
        };
        String result = executePost(REGISTRY_SERVICE_URL,  data, true);
        if (result!=null){
            // read new UDDI ID
			try {
				saxParser.parse(getStream(result), newRegistryEntryHandler);
				log.info("A new IPT service has been registered with GBIF. Key = "+ newRegistryEntryHandler.serviceKey);
				return newRegistryEntryHandler.serviceKey;
			} catch (SAXException e) {
				log.error("Error reading GBIF registry response", e);
			} catch (IOException e) {
				log.error("Error reading GBIF registry response", e);
			}
        }		
		return null;
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
	public boolean registerResource(DataResource resource) {
		log.info("A new resource has been registered with GBIF with key "+  "");
    	registerService(cfg.getIpt().getUddiID(), ServiceType.RSS, cfg.getAtomFeedURL());
    	registerService(cfg.getIpt().getUddiID(), ServiceType.RSS, cfg.getAtomFeedURL());
    	registerService(cfg.getIpt().getUddiID(), ServiceType.RSS, cfg.getAtomFeedURL());
    	registerService(cfg.getIpt().getUddiID(), ServiceType.RSS, cfg.getAtomFeedURL());
    	registerService(cfg.getIpt().getUddiID(), ServiceType.RSS, cfg.getAtomFeedURL());
    	registerService(cfg.getIpt().getUddiID(), ServiceType.RSS, cfg.getAtomFeedURL());
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
		return executeGet(getOrganisationUri(), params, true)!=null;
	}
}
