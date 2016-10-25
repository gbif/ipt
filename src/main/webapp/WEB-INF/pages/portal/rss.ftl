<?xml version="1.0"?>
<#setting locale="en_US">
<#assign dateTimeFormat="EEE, dd MMM yyyy HH:mm:ss Z"/>
<rss version="2.0"
	xmlns:ipt="http://ipt.gbif.org/"
  xmlns:atom="http://www.w3.org/2005/Atom"
	xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#">
<#escape x as x?xml>
  <channel>
    <title>${ipt.name!}</title>
    <link>${baseURL!}</link>
    <atom:link href="${baseURL!}/rss.do" rel="self" type="application/rss+xml" />
    <description>Resource metadata <#if ipt.name??>of ${ipt.name}</#if></description>
    <language>en-us</language>
    <#if ipt.created??>
      <!-- RFC-822 date-time / Wed, 02 Oct 2010 13:00:00 GMT -->
      <pubDate>${ipt.created?string(dateTimeFormat)}</pubDate>
    </#if>
    <#if resources[0]??>
      <lastBuildDate>${resources[0].modified?string(dateTimeFormat)}</lastBuildDate>
    </#if>
    <#if ipt.key??>
      <!-- UUID of the IPT making RSS feed available -->
      <ipt:identifier>${ipt.key!}</ipt:identifier>
    </#if>
    <generator>GBIF IPT ${cfg.version!}</generator>
    <#if ipt.primaryContactEmail??>
      <webMaster>${ipt.primaryContactEmail} (${ipt.primaryContactName!})</webMaster>
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
        <title>${res.title!}<#if res.emlVersion?has_content> - Version ${res.emlVersion.toPlainString()}</#if></title>
        <link>${cfg.getResourceUrl(res.shortname)}</link>
        <!-- shows what changed in this version, or shows the resource description if change summary was empty -->
        <description><#if res.getLastPublishedVersionsChangeSummary()?has_content>${res.getLastPublishedVersionsChangeSummary()}<#else>${res.eml.description!}</#if></description>
        <author>${res.creator.email} (${res.creator.getName()!})</author>
        <#if res.lastPublished??>
        <ipt:eml>${cfg.getResourceEmlUrl(res.shortname)}</ipt:eml>
        <#if (res.recordsPublished>0)>
        <ipt:dwca>${cfg.getResourceArchiveUrl(res.shortname)}</ipt:dwca>
        </#if>
        <pubDate>${res.lastPublished?string(dateTimeFormat)}</pubDate>
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
