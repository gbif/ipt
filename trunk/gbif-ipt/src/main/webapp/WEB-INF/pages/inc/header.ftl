[#ftl]
[#setting date_format="yyyy-MM-dd"]
[#setting time_format="dd/MM/yy"]
[#setting datetime_format="dd/MM/yy"]
[#setting locale="en"]
[#-- 
[#if localeLanguage??]
 [#setting locale=localeLanguage]
[/#if]
--]
[#setting url_escaping_charset="UTF-8"]
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
    [#assign iptIntro = "The Integrated Publishing Toolkit (IPT) is a tool developed by the Global Biodiversity Information Facility (GBIF) to provide an easy and efficient way of publishing biodiversity data." /]
    [#assign keywords = "GBIF, Global Biodiversity Information Facility, IPT, Integrated Publishing Toolkit, checklist, occurrence, metadata, DwC-A, Darwin Core, Darwin Core Archive, biodiversity data, data paper, EML" /]
    [#if hostingOrganisation?exists && hostingOrganisation.name??]
      [#assign hostDescription = " This IPT is hosted by" + hostingOrganisation.name + "."/]
      [#assign hostKeyword = ", " + hostingOrganisation.name + "." /]
    [/#if]
    <meta name="copyright" lang="en" content="GBIF" />
    <meta name="description" lang="en" content="${iptIntro}${hostDescription!""}" />
    <meta name="keywords" lang="en" content="${keywords}${hostKeyword!"."}" />
    <link rel="stylesheet" type="text/css" media="all" href="${baseURL}/styles/reset.css" />
	<link rel="stylesheet" type="text/css" media="all" href="${baseURL}/styles/text.css" />
	<link rel="stylesheet" type="text/css" media="all" href="${baseURL}/styles/960_24_col.css" />
 	<link rel="stylesheet" type="text/css" href="${baseURL}/styles/main.css" />
  <link rel="stylesheet" type="text/css" media="all" href="${baseURL}/styles/jquery/dataTable.css"/>
 	<link rel="shortcut icon" href="${baseURL}/images/icons/favicon.ico" type="image/x-icon" />
	<link href="${baseURL}/rss.do" title="Latest Resources" rel="alternate" type="application/rss+xml" />
	<link media="all" type="text/css" href="${baseURL}/styles/jquery/jquery-ui-1.8.3.css" rel="stylesheet" />
  <!-- for css overrides needed for customizations -->
  <link rel="stylesheet" type="text/css" href="${baseURL}/styles/custom.css" />
  <!-- for support of old browsers, like IE8. See http://modernizr.com/docs/#html5inie -->
  <script type="text/javascript" src="${baseURL}/js/modernizr.js"></script>
	<script type="text/javascript" src="${baseURL}/js/jquery/jquery.min-1.5.1.js"></script>				
	<script type="text/javascript" src="${baseURL}/js/jquery/jquery-ui.min-1.8.3.js"></script>
	<script type="text/javascript" src="${baseURL}/js/global.js"></script>
	<script type="text/javascript" src="${baseURL}/js/sorttable.js"></script>
 

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

<script type="text/javascript">
$(document).ready(function(){
[#-- see global.js for function defs --]
    initForm();
	initMenu();
});
</script>

[#assign currentMenu = "home"]