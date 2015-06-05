[#ftl]
[#setting date_format="yyyy-MM-dd"]
[#setting time_format="hh:mm:ss"]
[#setting datetime_format="iso"]
[#setting locale="en"]
[#setting url_escaping_charset="UTF-8"]
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/elements/1.1/">
  <head>
  <link rel="stylesheet" type="text/css" media="all" href="${baseURL}/styles/reset.css" />
	<link rel="stylesheet" type="text/css" media="all" href="${baseURL}/styles/text.css" />
	<link rel="stylesheet" type="text/css" media="all" href="${baseURL}/styles/960_24_col.css" />
 	<link rel="stylesheet" type="text/css" href="${baseURL}/styles/main.css" />
  <link rel="stylesheet" type="text/css" media="all" href="${baseURL}/styles/jquery/dataTable.css"/>
 	<link rel="shortcut icon" href="${baseURL}/images/icons/favicon.ico" type="image/x-icon" />
	<link href="${baseURL}/rss.do" title="Latest Resources" rel="alternate" type="application/rss+xml" />
  <link media="all" type="text/css" href="${baseURL}/styles/jquery/jquery-ui.min-1.11.0.css" rel="stylesheet" />
  <!-- for css overrides needed for customizations -->
  <link rel="stylesheet" type="text/css" href="${baseURL}/styles/custom.css" />
  <!-- for support of old browsers, like IE8. See http://modernizr.com/docs/#html5inie -->
  <script type="text/javascript" src="${baseURL}/js/modernizr.js"></script>
  <script type="text/javascript" src="${baseURL}/js/jquery/jquery-1.11.1.min.js"></script>
  <script type="text/javascript" src="${baseURL}/js/jquery/jquery-ui.min-1.11.0.js"></script>
	<script type="text/javascript" src="${baseURL}/js/global.js"></script>

[#-- GOOGLE ANALYTICS - asynchroneous: http://code.google.com/apis/analytics/docs/tracking/asyncTracking.html --]
[#if cfg.gbifAnalytics || (cfg.analyticsKey!"")?length>1] 
<script type="text/javascript">
  var _gaq = _gaq || [];
	[#if (cfg.analyticsKey!"")?length>1] 
	  _gaq.push(['_setAccount', '${cfg.analyticsKey}']);
	  _gaq.push(['_trackPageview']);
	[/#if]
	[#if cfg.gbifAnalytics] 
	  _gaq.push(['gbif._setAccount', '${cfg.getProperty("dev.analytics.gbif")}']);
	  _gaq.push(['gbif._trackPageview']);
	[/#if]
  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();
</script>
[/#if]

    [#-- Metadata used by browsers (title in browser toolbar, bookmark when added to favorites), search engines (keywords) --]
    [#assign metaKeywords = "GBIF, Global Biodiversity Information Facility, IPT, Integrated Publishing Toolkit, checklist, occurrence, metadata, DwC-A, Darwin Core, Darwin Core Archive, biodiversity data, data paper, EML" /]
    [#assign registeredIpt = action.getRegisteredIpt()/]
    [#if resource?? && eml??]
      <title>${eml.title!"IPT"}</title>
      <meta name="description" content="${eml.description!}" charset="UTF-8"}" />
      [#if eml.subject?has_content]
        <meta name="keywords" content="${eml.subject?replace(";", ",")}" charset="UTF-8" />
      [/#if]
    [#elseif registeredIpt??]
      <title>${registeredIpt.name!"IPT"}</title>
      <meta name="description" content="${registeredIpt.description!}" charset="UTF-8"}" />
      <meta name="keywords" content="${metaKeywords}" charset="UTF-8" />
    [#else]
      <title>IPT</title>
      <meta name="description" content="The Integrated Publishing Toolkit (IPT) is a tool developed by the Global Biodiversity Information Facility (GBIF) to provide an easy and efficient way of publishing biodiversity data." charset="UTF-8"}" />
      <meta name="keywords" content="${metaKeywords}" charset="UTF-8" />
    [/#if]
    <meta name="generator" content="IPT ${cfg.version!}" />

<script type="text/javascript">
$(document).ready(function(){
[#-- see global.js for function defs --]
    initForm();
	initMenu();
});
</script>

[#assign currentMenu = "home"]