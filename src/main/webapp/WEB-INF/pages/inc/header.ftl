[#ftl output_format="HTML"]
[#setting date_format="yyyy-MM-dd"]
[#setting time_format="hh:mm:ss"]
[#setting datetime_format="iso"]
[#setting locale="en"]
[#setting url_escaping_charset="UTF-8"]
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:dc="http://purl.org/dc/elements/1.1/"
      xmlns:foaf="http://xmlns.com/foaf/0.1/">
  <head>

	<link rel="stylesheet" type="text/css" media="all" href="${baseURL}/styles/reset.css" />
	<link rel="stylesheet" type="text/css" media="all" href="${baseURL}/styles/text.css" />
	<link rel="stylesheet" type="text/css" media="all" href="${baseURL}/styles/960_24_col.css" />
 	<link rel="stylesheet" type="text/css" href="${baseURL}/styles/main.css" />
	<link rel="stylesheet" type="text/css" media="all" href="${baseURL}/styles/jquery/dataTable.css"/>
 	<link rel="shortcut icon" href="${baseURL}/images/icons/favicon-16x16.png" type="image/x-icon" />
	<link href="${baseURL}/rss.do" title="Latest Resources" rel="alternate" type="application/rss+xml" />
	<link media="all" type="text/css" href="${baseURL}/styles/jquery/jquery-ui.min-1.12.1.css" rel="stylesheet" />
  	<link rel="stylesheet" type="text/css" href="${baseURL}/styles/font-awesome.min.css" media="all" />	
  <!-- for css overrides needed for customizations -->
	<link rel="stylesheet" type="text/css" href="${baseURL}/styles/custom.css" />
  <!-- for support of old browsers, like IE8. See http://modernizr.com/docs/#html5inie -->
	  <script type="text/javascript" src="${baseURL}/js/modernizr.js"></script>
	  <script type="text/javascript" src="${baseURL}/js/jquery/jquery-3.2.1.min.js"></script>
	  <script type="text/javascript" src="${baseURL}/js/jquery/jquery-ui.min-1.12.1.js"></script>
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
    [#assign registeredIpt = action.getRegisteredIpt()!""/]
    [#if resource?? && eml??]
      <meta name="description" content="${eml.description!}" charset="UTF-8" />
      [#if eml.subject?has_content]
        <meta name="keywords" content="${eml.subject?replace(";", ",")}" charset="UTF-8" />
      [/#if]
      <meta name="foaf:topic" content="${cfg.getResourceUri(resource.shortname)}/#dataset"/>
      <meta name="foaf:isPrimaryTopicOf" content="${cfg.getResourceUri(resource.shortname)}">
    [#elseif registeredIpt?has_content]
      <meta name="description" content="${registeredIpt.description!}" charset="UTF-8" />
      <meta name="keywords" content="${metaKeywords}" charset="UTF-8" />
    [#else]
      <meta name="description" content="The Integrated Publishing Toolkit (IPT) is a tool developed by the Global Biodiversity Information Facility (GBIF) to provide an easy and efficient way of publishing biodiversity data." charset="UTF-8"}" />
      <meta name="keywords" content="${metaKeywords}" charset="UTF-8" />
    [/#if]
    <meta name="generator" content="IPT ${cfg.version!}" />
    <meta name="inventory" content="${baseURL}/inventory/dataset"/>
    <meta name="foaf:seeAlso" content="${baseURL}/dcat"/>
    <meta http-equiv="X-UA-Compatible" content="IE=9" />
<script type="text/javascript">
$(document).ready(function(){
[#-- see global.js for function defs --]
    initForm();
	initMenu();
});
</script>

[#assign currentMenu = "home"]