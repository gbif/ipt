<?xml version="1.0"?>
<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:ipt="http://ipt.gbif.org/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#">
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
    <webMaster>${ipt.primaryContactEmail} (${ipt.primaryContactName})</webMaster>
    </#if>  
    <docs>http://cyber.law.harvard.edu/rss/rss.html</docs>
    <ttl>15</ttl>
 	<#list resources as res>
    <item>
      <title>${res.title!}</title>
      <link>${cfg.getResourceUrl(res.shortname)}</link>
      <description>${res.description!} &lt;a href="${cfg.getResourceEmlUrl(res.shortname)}"&gt;EML&lt;/a&gt;</description>      
      <ipt:emlLink>${cfg.getResourceEmlUrl(res.shortname)}</ipt:emlLink>
	  <#if res.link??>
	  <ipt:homeLink>${res.link}</ipt:homeLink>
	  </#if>
	  <#if res.contactName??>
	  <ipt:contact>${res.contactName} <#if res.contactEmail??>&lt;${res.contactEmail}&gt;</#if></ipt:contact>
	  </#if>
      <#if res.modified??>
      <pubDate>${res.modified?string("EEE, dd MMM yyyy HH:mm:ss Z")}</pubDate>
      </#if>
      <#if res.key??>
      <guid isPermaLink="false">${res.key}</guid>
      </#if>
    </item>
 	</#list>
  </channel>
</#escape>
</rss>
