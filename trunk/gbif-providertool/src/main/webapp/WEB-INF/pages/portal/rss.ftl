<?xml version="1.0"?>
<rss version="2.0" xmlns:ipt="http://ipt.gbif.org/">
<#escape x as x?xml>
  <channel>
    <title>${cfg.title}</title>
    <link>${cfg.baseUrl!}</link>
    <description>Resource metadata of ${cfg.title}</description>
    <language>en-us</language>
    <!-- Tue, 10 Jun 2003 04:00:00 GMT -->
    <pubDate>${pubDate?datetime?string.medium}</pubDate>
    <lastBuildDate>${pubDate?datetime?string.medium}</lastBuildDate>
    <generator>GBIF IPT <@s.text name="webapp.version"/></generator>
    <#if admin??>
    <webMaster>${cfg.contactEmail}</webMaster>
	</#if>  
    <docs>http://cyber.law.harvard.edu/rss/rss.html</docs>
    <ttl>15</ttl>
 
	<#list resources as res>
    <item>
      <title>${res.title}</title>
	  <#if res.link??>
      <link>${res.link}</link>
	  </#if>
	  <#if res.emlUrl??>
      <ipt:emlLink>${res.emlUrl}</ipt:emlLink>
	  </#if>
	  <#if res.contactName??>
      <ipt:contact>${res.contactName} <#if res.contactEmail??>&lt;${res.contactEmail}&gt;</#if></ipt:contact>
	  </#if>
      <description>&lt;img src="${cfg.getResourceLogoUrl(res.id)}" align="left" style="padding-right:5px;" /&gt; 
      ${res.description}</description>
      <pubDate>${res.modified?datetime?string.medium}</pubDate>
      <guid>${res.guid}</guid>
    </item>
 	</#list>
 
  </channel>
</#escape>
</rss>
