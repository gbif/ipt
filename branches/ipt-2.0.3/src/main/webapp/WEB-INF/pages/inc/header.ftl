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
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
	    <meta name="copyright" lang="en" content="GBIF" />
 		<link rel="stylesheet" type="text/css" href="${baseURL}/styles/main.css"/>
 		<link rel="shortcut icon" href="${baseURL}/images/icons/favicon.ico" type="image/x-icon" />
		<script type="text/javascript" src="${baseURL}/js/jquery/jquery.min-1.5.1.js"></script>				
		<script type="text/javascript" src="${baseURL}/js/jquery/jquery-ui.min-1.8.3.js"></script>
		<link media="all" type="text/css" href="${baseURL}/styles/jquery/jquery-ui-1.8.3.css" rel="stylesheet">
		<script type="text/javascript" src="${baseURL}/js/global.js"></script>
		<script type="text/javascript" src="${baseURL}/js/sorttable.js"></script>
	    <link href="${baseURL}/rss.do" title="Latest Resources" rel="alternate" type="application/rss+xml" />
 		
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