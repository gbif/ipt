package org.gbif.provider.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.gbif.provider.model.ChecklistResource;
import org.gbif.provider.model.DataResource;
import org.gbif.provider.model.OccurrenceResource;
import org.gbif.provider.model.Resource;
import org.gbif.provider.model.ResourceMetadata;
import org.gbif.provider.model.voc.ServiceType;
import org.gbif.provider.model.xml.NewRegistryEntryHandler;
import org.gbif.provider.model.xml.ResourceMetadataHandler;
import org.gbif.provider.service.RegistryException;
import org.gbif.provider.service.RegistryManager;
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


	public String registerOrg() throws RegistryException{
		// need to register a new organisation?
		if (StringUtils.trimToNull(cfg.getOrg().getUddiID())!=null){
			String msg = "Organisation is already registered";
			log.warn(msg);
    		throw new IllegalArgumentException(msg);
		}
		setRegistryCredentials();
		// http://code.google.com/p/gbif-registry/wiki/ExplanationUDDI#CREATE_ORGANIZATION
        NameValuePair[] data = {
                new NameValuePair("nodeKey", StringUtils.trimToEmpty(cfg.getOrgNode())),
                new NameValuePair("name", StringUtils.trimToEmpty(cfg.getOrg().getTitle())),
                new NameValuePair("description", StringUtils.trimToEmpty(cfg.getOrg().getDescription())),
                new NameValuePair("homepageURL", StringUtils.trimToEmpty(cfg.getOrg().getLink())),
                new NameValuePair("primaryContactName", StringUtils.trimToEmpty(cfg.getOrg().getContactName())),
                new NameValuePair("primaryContactEmail", StringUtils.trimToEmpty(cfg.getOrg().getContactEmail()))
        };
        String result = executePost(REGISTRY_ORG_URL,  data, true);
        if (result!=null){
            // read new UDDI ID
			try {
				saxParser.parse(getStream(result), newRegistryEntryHandler);
				cfg.setOrgPassword(newRegistryEntryHandler.password);
				String key = newRegistryEntryHandler.key;
				if (StringUtils.trimToNull(key)==null){
					key = newRegistryEntryHandler.organisationKey;
				}
				cfg.getOrg().setUddiID(key);
				log.info("A new organisation has been registered with GBIF under node "+ cfg.getOrgNode() +" and with key "+key);
	            return key;        	
			} catch (Exception e) {
				throw new RegistryException("Error reading registry response", e);
			}
        }
		throw new RegistryException("No registry response or no key returned");
	}

	private void setRegistryCredentials() {
		try {
			URI geoURI = new URI(REGISTRY_ORG_URL);
			setCredentials(geoURI.getHost(), cfg.getOrg().getUddiID(), cfg.getOrgPassword());		
		} catch (URISyntaxException e) {
			log.error("Exception setting the registry credentials", e);
		}
	}

	
	/* Service Binding: IPT RSS feed
	 * Service Binding: IPT EML
	 * (non-Javadoc)
	 * @see org.gbif.provider.service.RegistryManager#registerIPT()
	 */
	public String registerIPT() throws RegistryException{
		if (StringUtils.trimToNull(cfg.getIpt().getUddiID())!=null){
			String msg = "IPT is already registered";
			log.warn(msg);
    		throw new IllegalArgumentException(msg);
		}
		setRegistryCredentials();
		// registering IPT resource
		String key = registerResource(cfg.getIpt());
		if (key!=null){
			log.info("The IPT has been registered with GBIF as resource "+ cfg.getIpt().getUddiID());
			// RSS resource feed service
	    	registerService(key, ServiceType.RSS, cfg.getAtomFeedURL());
			// IPT EML service
	    	registerService(key, ServiceType.EML, cfg.getEmlUrl("ipt"));
	        return key;        	
		}
		log.warn("Failed to register IPT with GBIF as a new resource");
		throw new RegistryException("No registry response or no key returned");
	}

	private String registerResource(ResourceMetadata meta) throws RegistryException{
		// registering IPT resource
        NameValuePair[] data = {
                new NameValuePair("organizationKey", StringUtils.trimToEmpty(cfg.getOrg().getUddiID())),
                new NameValuePair("name", StringUtils.trimToEmpty(meta.getTitle())), // name
                new NameValuePair("description", StringUtils.trimToEmpty(meta.getDescription())), // description
                new NameValuePair("homepageURL", StringUtils.trimToEmpty(meta.getLink())),
                new NameValuePair("primaryContactName", StringUtils.trimToEmpty(meta.getContactName())),
                new NameValuePair("primaryContactEmail", StringUtils.trimToEmpty(meta.getContactEmail()))
        };
        String result = executePost(REGISTRY_RESOURCE_URL,  data, true);
        if (result!=null){
            // read new UDDI ID
			try {
				saxParser.parse(getStream(result), newRegistryEntryHandler);
				String key = newRegistryEntryHandler.key;
				if (StringUtils.trimToNull(key)==null){
					key = newRegistryEntryHandler.resourceKey;
				}
				meta.setUddiID(key);
				if (meta.getUddiID()!=null){
					log.info("A new resource has been registered with GBIF. Key = "+ key);
					return key;
				}
			} catch (Exception e) {
				throw new RegistryException("Error reading registry response", e);
			}
        }
		throw new RegistryException("No registry response or no key returned");
	}
	
	private String registerService(String resourceKey, ServiceType serviceType, String accessPointURL) throws RegistryException{
		NameValuePair[] data = {
                new NameValuePair("resourceKey", StringUtils.trimToEmpty(resourceKey)),
                new NameValuePair("accessPointType", serviceType.code),
                new NameValuePair("accessPointURL", StringUtils.trimToEmpty(accessPointURL))
        };
        String result = executePost(REGISTRY_SERVICE_URL,  data, true);
        if (result!=null){
            // read new UDDI ID
			try {
				saxParser.parse(getStream(result), newRegistryEntryHandler);
				String key = newRegistryEntryHandler.key;
				if (StringUtils.trimToNull(key)==null){
					key = newRegistryEntryHandler.serviceKey;
				}
				log.info("A new IPT service has been registered with GBIF. Key = "+ key);
				return key;
			} catch (Exception e) {
				throw new RegistryException("Error reading registry response", e);
			}
        }		
		throw new RegistryException("No registry response or no key returned");
	}
	private String registerService(Resource resource, ServiceType serviceType, String accessPointURL) throws RegistryException{
		// validate that service is not already registered
    	if (resource.getServices().containsKey(serviceType)){
    		throw new IllegalArgumentException("Service is already registered");
    	}
    	String key = registerService(resource.getUddiID(), serviceType, accessPointURL);
    	resource.getServices().put(serviceType, key);
		return key;
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
	public String registerResource(Resource resource) throws RegistryException{
		if (!resource.isPublished()){
			String msg = "Resource " +resource.getId()+ " needs to be published before it can be registered with GBIF";
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}
		String resourceKey = registerResource(resource.getMeta());
		log.info("Resource "+resource.getId()+" has been registered with GBIF. Key = "+ resource.getUddiID());
		try {
			registerService(resource, ServiceType.EML, cfg.getEmlUrl(resource.getGuid()));
		} catch (Exception e) {
			log.error(e);
		}
		if (DataResource.class.isAssignableFrom(resource.getClass())){
			try {
				registerService(resource, ServiceType.DWC_ARCHIVE, cfg.getArchiveUrl(resource.getGuid()));
			} catch (Exception e) {
				log.error(e);
			}
		}
		if (resource instanceof OccurrenceResource){
			try {
				registerService(resource, ServiceType.TAPIR, cfg.getTapirEndpoint(resource.getId()));
			} catch (Exception e) {
				log.error(e);
			}
			try {
				registerService(resource, ServiceType.WFS, cfg.getWfsEndpoint(resource.getId()));
			} catch (Exception e) {
				log.error(e);
			}
			try {
				registerService(resource, ServiceType.WMS, cfg.getWmsEndpoint(resource.getId()));
			} catch (Exception e) {
				log.error(e);
			}
		}
		if (resource instanceof ChecklistResource){
			try {
				registerService(resource, ServiceType.TCS_RDF, cfg.getArchiveTcsUrl(resource.getGuid()));
			} catch (Exception e) {
				log.error(e);
			}
		}
		return resourceKey;
	}

	public Collection<String> listAllExtensions() {
		Collection<String> urls = new LinkedList<String>();
		urls.add("http://gbrds.gbif.org/resources/extensions/vernacularName.xml");
		return urls;
	}

	public Collection<String> listAllThesauri() {
		Collection<String> urls = new LinkedList<String>();
		urls.add("http://gbrds.gbif.org/resources/thesauri/lang.xml");
		return urls;
	}

	
	private String getOrganisationUri(){
		return String.format("%s/%s", REGISTRY_ORG_URL, cfg.getOrg().getUddiID());
	}
	private String getIptUri(){
		return String.format("%s/%s", REGISTRY_RESOURCE_URL, cfg.getIpt().getUddiID());
	}

	public boolean testLogin() {
		// http://server:port/registration/organization/30?op=login
		setRegistryCredentials();
        NameValuePair[] params = {
                new NameValuePair("op", "login")
        };
		return executeGet(getOrganisationUri(), params, true)!=null;
	}


	public void updateIPT() throws RegistryException {
		if (!cfg.isIptRegistered()){
			String msg = "IPT is not registered. Cannot update";
			log.warn(msg);
    		throw new IllegalStateException(msg);
		}
		setRegistryCredentials();
        NameValuePair[] data = {
                new NameValuePair("name", StringUtils.trimToEmpty(cfg.getIpt().getTitle())),
                new NameValuePair("description", StringUtils.trimToEmpty(cfg.getIpt().getDescription())),
                new NameValuePair("homepageURL", StringUtils.trimToEmpty(cfg.getIpt().getLink())),
                new NameValuePair("primaryContactName", StringUtils.trimToEmpty(cfg.getIpt().getContactName())),
                new NameValuePair("primaryContactEmail", StringUtils.trimToEmpty(cfg.getIpt().getContactEmail()))
        };
        String result = executePut(getIptUri(),  data, true);
        if (result==null){
    		throw new RegistryException("Bad registry response");
        }
	}


	public void updateOrg() throws RegistryException {
		if (!cfg.isOrgRegistered()){
			String msg = "Organisation is not registered. Cannot update";
			log.warn(msg);
    		throw new IllegalStateException(msg);
		}
		setRegistryCredentials();
        NameValuePair[] data = {
                //new NameValuePair("nodeKey", StringUtils.trimToEmpty(cfg.getOrgNode())),
                new NameValuePair("name", StringUtils.trimToEmpty(cfg.getOrg().getTitle())),
                new NameValuePair("description", StringUtils.trimToEmpty(cfg.getOrg().getDescription())),
                new NameValuePair("homepageURL", StringUtils.trimToEmpty(cfg.getOrg().getLink())),
                new NameValuePair("primaryContactName", StringUtils.trimToEmpty(cfg.getOrg().getContactName())),
                new NameValuePair("primaryContactEmail", StringUtils.trimToEmpty(cfg.getOrg().getContactEmail()))
        };
        String result = executePut(getOrganisationUri(),  data, true);
        if (result==null){
    		throw new RegistryException("Bad registry response");
        }
	}


	public void updateResource(Resource resource) throws RegistryException {
		if (!resource.isRegistered()){
			String msg = "Resource is not registered. Cannot update";
			log.warn(msg);
    		throw new IllegalStateException(msg);
		}
		setRegistryCredentials();
        NameValuePair[] data = {
                //new NameValuePair("nodeKey", StringUtils.trimToEmpty(cfg.getOrgNode())),
                new NameValuePair("name", StringUtils.trimToEmpty(cfg.getOrg().getTitle())),
                new NameValuePair("description", StringUtils.trimToEmpty(cfg.getOrg().getDescription())),
                new NameValuePair("homepageURL", StringUtils.trimToEmpty(cfg.getOrg().getLink())),
                new NameValuePair("primaryContactName", StringUtils.trimToEmpty(cfg.getOrg().getContactName())),
                new NameValuePair("primaryContactEmail", StringUtils.trimToEmpty(cfg.getOrg().getContactEmail()))
        };
        String result = executePut(resource.getRegistryUrl(),  data, true);
        if (result==null){
    		throw new RegistryException("Bad registry response");
        }
	}
	
	public void deleteResource(Resource resource) throws RegistryException {
		if (!resource.isRegistered()){
			String msg = "Resource is not registered";
			log.warn(msg);
    		throw new IllegalStateException(msg);
		}
		setRegistryCredentials();
        String result = executeDelete(resource.getRegistryUrl(),  true);
        if (result==null){
    		throw new RegistryException("Bad registry response");
        }
	}
	
}
