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
		<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>				
		<script type="text/javascript" src="${baseURL}/js/jquery.cookie.min.js"></script>
		<script type="text/javascript" src="${baseURL}/js/json2.min.js"></script>
		<script type="text/javascript" src="${baseURL}/js/global.js"></script>
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
    initForm();
	readUserPrefCookie();
	$('#topmenu > li').bind('mouseover', jsddm_open);
	$('#topmenu > li').bind('mouseout',  jsddm_timer);	
	// Simple Drop-Down Menu
	// taken from http://javascript-array.com/scripts/jquery_simple_drop_down_menu/
	var timeout    = 500;
	var closetimer = 0;
	var ddmenuitem = 0;
	function jsddm_open() {  
		jsddm_canceltimer();
   		jsddm_close();
   		ddmenuitem = $(this).find('ul').css('visibility', 'visible');
   	}
	function jsddm_close() { 
		if(ddmenuitem) ddmenuitem.css('visibility', 'hidden');
	}
	function jsddm_timer() {
		closetimer = window.setTimeout(jsddm_close, timeout);
	}
	function jsddm_canceltimer() {  
		if(closetimer) {
			window.clearTimeout(closetimer);
	      closetimer = null;
	    }
	}	
});
</script>

[#assign currentMenu = "home"]