<?xml version="1.0"?>
<rss version="2.0" 
	xmlns:foaf="http://xmlns.com/foaf/0.1/" 
	xmlns:ipt="http://ipt.gbif.org/"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#">
<#escape x as x?xml>
  <channel>
    <title>${ipt.name!}</title>
    <link>${baseURL!}</link>
    <description>Resource metadata <#if ipt.name??>of ${ipt.name}</#if></description>
    <language>en-us</language>
    <!-- RFC-822 date-time  / Wed, 02 Oct 2010 13:00:00 GMT -->
    <#if ipt.created??>
      <pubDate>${ipt.created?string("EEE, dd MMM yyyy HH:mm:ss Z")}</pubDate>
    </#if>
    <#if resources[0]??>
      <lastBuildDate>${resources[0].modified?string("EEE, dd MMM yyyy HH:mm:ss Z")}</lastBuildDate>
    </#if>
    <generator>GBIF IPT ${cfg.version!}</generator>
    <#if ipt.primaryContactEmail??>
      <webMaster>${ipt.primaryContactEmail} (${ipt.primaryContactFirstName!}) (${ipt.primaryContactLastName!})</webMaster>
    </#if>  
    <docs>http://cyber.law.harvard.edu/rss/rss.html</docs>
    <ttl>15</ttl>
    <#if cfg.hasLocation()>
      <geo:Point>
        <geo:lat>${cfg.latitude?c}</geo:lat>
        <geo:long>${cfg.longitude?c}</geo:long>
      </geo:Point>
    </#if>
 	  <#list resources as res>
      <item>
        <title>${res.title!}<#if res.emlVersion?has_content> - Version ${res.emlVersion}</#if></title>
        <link>${cfg.getResourceUrl(res.shortname)}</link>
        <description>${res.description!} &lt;a href="${cfg.getResourceEmlUrl(res.shortname)}"&gt;EML&lt;/a&gt;</description>
        <author>${res.creator.email}</author>
        <#if res.lastPublished??>
          <ipt:eml>${cfg.getResourceEmlUrl(res.shortname)}</ipt:eml>
	        <dc:publisher>${res.eml.contact.firstName!} ${res.eml.contact.lastName!} ${res.eml.contact.organisation!}<#if res.eml.contact.email??>&lt;${res.eml.contact.email}&gt;</#if></dc:publisher>
	        <dc:creator>${res.eml.getResourceCreator().firstName!} ${res.eml.getResourceCreator().lastName!} ${res.eml.getResourceCreator().organisation!}<#if res.eml.getResourceCreator().email??>&lt;${res.eml.getResourceCreator().email}&gt;</#if></dc:creator>
          <#if (res.recordsPublished>0)>
            <ipt:dwca>${cfg.getResourceArchiveUrl(res.shortname)}</ipt:dwca>
          </#if>
        </#if>
	      <#if res.link??>
	        <foaf:homepage>${res.link}</foaf:homepage>
	      </#if>
        <#if res.lastPublished??>
          <pubDate>${res.lastPublished?string("EEE, dd MMM yyyy HH:mm:ss Z")}</pubDate>
        </#if>
        <#-- guid is a string that uniquely identifies the RSS item. For RSS readers to detect that a resource was
        republished, news about the new resource version must become a new RSS item, uniquely determined via the EML packageId. -->
        <#if res.eml.packageId??>
          <guid isPermaLink="false">${res.eml.packageId}</guid>
        </#if>
      </item>
 	  </#list>
  </channel>
</#escape>
</rss>
