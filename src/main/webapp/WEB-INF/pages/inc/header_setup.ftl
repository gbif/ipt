[#ftl output_format="HTML"]
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
	    <meta name="copyright" lang="en" content="GBIF" />
		<title>[@s.text name="admin.config.setup.title"/]</title>
	  <link rel="stylesheet" type="text/css" media="all" href="${baseURL}/styles/reset.css" />
		<link rel="stylesheet" type="text/css" media="all" href="${baseURL}/styles/text.css" />
		<link rel="stylesheet" type="text/css" media="all" href="${baseURL}/styles/960_24_col.css" />
 		<link rel="stylesheet" type="text/css" href="${baseURL}/styles/main.css"/>
 		<link rel="shortcut icon" href="${baseURL}/images/icons/favicon-16x16.png" type="image/x-icon" />
    <!-- for css overrides needed for customizations -->
    <link rel="stylesheet" type="text/css" href="${baseURL}/styles/custom.css" />
    <!-- for support of old browsers, like IE8. See http://modernizr.com/docs/#html5inie -->
    <script type="text/javascript" src="${baseURL}/js/modernizr.js"></script>
    <script type="text/javascript" src="${baseURL}/js/jquery/jquery-1.11.1.min.js"></script>
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
		<div class="page clearfix" id="page">
			<header id="section-header" class="section section-header">
        <div id="zone-user-wrapper" class="zone-wrapper zone-user-wrapper clearfix">
          <div id="zone-user" class="zone zone-user clearfix container_24">
            <div class="region-inner region-branding-inner">
              <div class="branding-data clearfix">
                <div class="logo-img">
                  <a href="${baseURL}" rel="home" title="GBIF Logo" class="active">
                    <img src="${baseURL}/images/GBIF-2015-standard-ipt.png" />
                  </a>
                </div>
                <hgroup class="site-name-slogan">
                  <h1 class="site-name"><a href="${baseURL}" rel="home" title="Home" class="active">Integrated Publishing Toolkit</a><span class="logoSuperscript">(IPT)</span></h1>
                  <h6 class="site-slogan">free and open access to biodiversity data</h6>
                </hgroup>
                <div id="region-user-second" class="region-inner region-user-second-inner">
                  <ul id="language-menu">
                    <li>
                      [#include "/WEB-INF/pages/inc/languages.ftl"/]
                    </li>
                  </ul>
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
      		