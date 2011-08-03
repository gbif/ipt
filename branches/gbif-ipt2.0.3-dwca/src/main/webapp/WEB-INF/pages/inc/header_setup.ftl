[#ftl]
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
	    <meta name="copyright" lang="en" content="GBIF" />
		<title>[@s.text name="admin.config.setup.title"/]</title>
 		<link rel="stylesheet" type="text/css" href="styles/main.css"/>
 		<link rel="shortcut icon" href="${baseURL}/images/icons/favicon.ico" type="image/x-icon" />
		<script type="text/javascript" src="${baseURL}/js/jquery/jquery.min-1.5.1.js"></script>				
		<script type="text/javascript" src="${baseURL}/js/global.js"></script>
		<style>
h1 {
	color: #077A2D;
	font-size: 30px;
}
		</style>
<script type="text/javascript">
$(document).ready(function(){
  initHelp();	
[#-- see global.js for function defs --]
	initMenu();  
});
</script>

 	</head>
 	<body>
		<div id="wrapper">
			<div id="topwrapper">
                 <ul id="topmenu">       
                           <li>
                        <a href="#"><img src="${baseURL}/images/flags/flag_${localeLanguage}.gif"/></a>
                        <ul>                                                
                            <!-- add more languages as translations become available. -->                           
                            <!-- #list ["en","es","fr","de"] as lang -->
                            [#list ["en","fr","es"] as lang]
                                [#if localeLanguage!=lang]
                                <li><a href="?request_locale=${lang}"><img src="${baseURL}/images/flags/flag_${lang}.gif"/></a></li>
                                [/#if]
                            [/#list]
                            </ul>
                           </li>
                    </ul>   
            </div>
			<div id="content">			

			<div id="logo">
				<img src="styles/logo.jpg">					
			</div>