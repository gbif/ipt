[#ftl]
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
	    <meta name="copyright" lang="en" content="GBIF" />
		<title>[@s.text name="admin.config.setup.title"/]</title>
	  <link rel="stylesheet" type="text/css" media="all" href="${baseURL}/styles/reset.css" />
		<link rel="stylesheet" type="text/css" media="all" href="${baseURL}/styles/text.css" />
		<link rel="stylesheet" type="text/css" media="all" href="${baseURL}/styles/960_24_col.css" />
 		<link rel="stylesheet" type="text/css" href="${baseURL}/styles/main.css"/>
 		<link rel="shortcut icon" href="${baseURL}/images/icons/favicon.ico" type="image/x-icon" />

<!--[if IE 8]>
   <script type="text/javascript">
      document.createElement('header');
      document.createElement('nav');
      document.createElement('section');
      document.createElement('article');
      document.createElement('aside');
      document.createElement('footer');
   </script>
<![endif]-->

		<script type="text/javascript" src="${baseURL}/js/jquery/jquery.min-1.5.1.js"></script>				
		<script type="text/javascript" src="${baseURL}/js/global.js"></script>
		<style>
      h1 {
      	color: #077A2D;
      	font-size: 30px;
      }
	  #region-user-second { line-height: 18px; }
		</style>
    <script type="text/javascript">
      $(document).ready(function(){
        	initHelp();	
      	[#-- see global.js for function defs --]
      	initMenu();
      	langs("${localeLanguage}","${baseURL}");  
      });
    </script>

 	</head>
 	<body>
		<div class="page clearfix" id="page">
			<header id="section-header" class="section section-header">
        <div id="zone-user-wrapper" class="zone-wrapper zone-user-wrapper clearfix">
        <div id="zone-user" class="zone zone-user clearfix container_24">
          <aside class="prefix_12 grid_12 region region-user-second account" id="region-user-second">
            <div class="region-inner region-user-second-inner">
              <ul id="language-menu">       
                <li><a href="#"><img src="${baseURL}/images/flags/flag_${localeLanguage}.gif"/></a>
                  <ul>                                                
                    <!-- add more languages as translations become available. -->                           
                    <!-- #list ["en","es","fr","de"] as lang -->
                    <!-- modify global.js langs() also -->
                    [#list ["en","fr","es", "zh"] as lang]
                      [#if localeLanguage!=lang]
                      <li><a href="?request_locale=${lang}"><img src="${baseURL}/images/flags/flag_${lang}.gif"/></a></li>
                      [/#if]
                    [/#list]
                  </ul>
                </li>
              </ul>   
            </div>
          </aside>
        </div>
      </div>
      <div id="zone-branding-wrapper" class="zone-wrapper zone-branding-wrapper clearfix">
        <div id="zone-branding" class="zone zone-branding clearfix container_24">
          <div class="grid_11 region region-branding" id="region-branding">
            <div class="region-inner region-branding-inner">
              <div class="branding-data clearfix">
                <div class="logo-img"><a href="/" rel="home" title="GBIF Logo" class="active">
                  <img src="${baseURL}/styles/logo.png" />
                  </a>
                </div>
                <hgroup class="site-name-slogan">        
                  <h1 class="site-name"><a href="/" rel="home" title="Home" class="active">GBIF Integrated Publishing Toolkit</a><span class="logoSuperscript">(IPT)</span></h1>
                  <h6 class="site-slogan">free and open access to biodiversity data</h6>
                </hgroup>
              </div>
            </div>
          </div>
      	</div>
      </div>
    </header>
    <section id="section-content" class="section section-content menu-pull">
      <div id="zone-content-wrapper" class="zone-wrapper zone-content-wrapper clearfix">  
        <div id="zone-content" class="zone zone-content clearfix container_24"> 
          <div class="grid_18 region region-content" id="region-content">
      		