<?xml version="1.0"?>
<rss version="2.0">
<#escape x as x?xml>
  <channel>
    <title>${cfg.providerTitle}</title>
    <link>${cfg.appBaseUrl!}</link>
    <description>Resource metadata of ${cfg.providerTitle}</description>
    <language>en-us</language>
    <!-- Tue, 10 Jun 2003 04:00:00 GMT -->
    <pubDate>${pubDate?datetime?string.medium}</pubDate>
    <lastBuildDate>${pubDate?datetime?string.medium}</lastBuildDate>
    <generator>GBIF IPT <@s.text name="webapp.version"/></generator>
    <#if admin??>
    <webMaster>${admin.email}</webMaster>
	</#if>  
    <docs>http://cyber.law.harvard.edu/rss/rss.html</docs>
    <ttl>15</ttl>
 
	<@s.iterator value="resources">
    <item>
      <title>${title}</title>
	  <#if link??>
      <link>${link}</link>
	  </#if>
      <description>${description}</description>
	  <enclosure url="${cfg.appBaseUrl!}/logo.html?id=${id}" type="image" />
      <pubDate>${modified?datetime?string.medium}</pubDate>
      <guid>${guid}</guid>
    </item>
 	</@s.iterator>
 
  </channel>
</#escape>
</rss>
