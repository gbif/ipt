[#ftl]
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
	    <meta name="copyright" lang="en" content="GBIF" />
 		<link rel="stylesheet" type="text/css" href="${baseURL}/styles/main.css"/>
		<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>	
		<script type="text/javascript" src="${baseURL}/js/jquery.cookie.min.js"></script>
		<script type="text/javascript" src="${baseURL}/js/json2.min.js"></script>
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

<script type="text/javascript">
$(document).ready(function(){
    $("#locale > a").click(function(e) {
        $("#availableLocales").toggle();
    })
    initForm();
	readUserPrefCookie();    
});
</script>

[#assign currentMenu = "index"]