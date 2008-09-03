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
 
	<#list resources as res>
    <item>
      <title>${res.title}</title>
	  <#if res.link??>
      <link>${res.link}</link>
	  </#if>
      <description>${res.description}</description>
	  <enclosure url="${res.logoUrl}" type="image" />
      <pubDate>${res.modified?datetime?string.medium}</pubDate>
      <guid>${res.guid}</guid>
    </item>
 	</#list>
 
  </channel>
</#escape>
</rss>
