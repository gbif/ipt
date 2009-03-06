<?xml version="1.0"?>
<rss version="2.0" xmlns:ipt="http://ipt.gbif.org/" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#">
<#escape x as x?xml>
  <channel>
    <title>${cfg.ipt.title}</title>
    <link>${cfg.baseUrl!}</link>
    <description>Resource metadata of ${cfg.ipt.title}</description>
    <language>en-us</language>
    <!-- Tue, 10 Jun 2003 04:00:00 GMT -->
    <pubDate>${resources[0].modified?datetime?string.medium}</pubDate>
    <lastBuildDate>${resources[0].modified?datetime?string.medium}</lastBuildDate>
    <generator>GBIF IPT <@s.text name="webapp.version"/></generator>
    <#if admin??>
    <webMaster>${cfg.contactEmail}</webMaster>
	</#if>  
    <docs>http://cyber.law.harvard.edu/rss/rss.html</docs>
    <ttl>15</ttl>
 
	<#list resources as res>
    <item>
      <title>${res.title}</title>
      <link>${cfg.getResourceUrl(res.guid)}</link>
      <ipt:emlLink>${cfg.getEmlUrl(res.guid)}</ipt:emlLink>
	  <#if res.link??>
      <ipt:homeLink>${res.link}</ipt:homeLink>
	  </#if>
	  <#if res.contactName??>
      <ipt:contact>${res.contactName} <#if res.contactEmail??>&lt;${res.contactEmail}&gt;</#if></ipt:contact>
	  </#if>
      <description>&lt;img src="${cfg.getResourceLogoUrl(res.id)}" align="left" style="padding-right:5px;" /&gt; 
      ${res.description} &lt;a href="${cfg.getEmlUrl(res.guid)}"&gt;EML&lt;/a&gt;</description>
      <pubDate>${res.modified?datetime?string.medium}</pubDate>
      <guid>${res.guid}</guid>
    </item>
 	</#list>
 
  </channel>
</#escape>
</rss>
