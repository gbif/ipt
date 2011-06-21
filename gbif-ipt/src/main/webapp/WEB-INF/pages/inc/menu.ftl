[#ftl]

 	</head>
 	<body>
		<div id="wrapper">
		    <div id="topwrapper">
		        <ul id="topmenu">
		      [#if (Session.curr_user)??] 
		        	<li>[@s.text name="menu.loggedin"/] <em>${Session.curr_user.email}</em></li>
		        	<li[#if currentMenu=="account"] class="current"[/#if] ><a href="${baseURL}/account.do">[@s.text name="menu.account"/]</a></li>
		        	<li[#if currentMenu=="logout"] class="current"[/#if]><a href="${baseURL}/logout.do">[@s.text name="menu.logout"/]</a></li>
			  [#else]
		        	<li>
				        <form id="login-form" action="${baseURL}/login.do" method="post">
				        	<input type="text" size="25" name="email" value="email" class="form-reset" />
		        			<input type="password" size="20" name="password" value="password" class="form-reset" />
		        			[@s.submit key="portal.login" name="login-submit"/]
				        </form>
					</li>
			  [/#if]
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

			<div id="logo">
				<img src="${baseURL}/styles/logo.jpg">					
			</div>

			<div id="menu">
		        <ul>
			    	<li[#if currentMenu=='home'] class="current"[/#if]><a href="${baseURL}/">[@s.text name="menu.home"/]</a></li>
			    	[#if managerRights]
			    	<li[#if currentMenu=='manage'] class="current"[/#if]><a href="${baseURL}/manage/">[@s.text name="menu.manage"/]</a></li>
			    	[/#if] 
			    	[#if adminRights]
			    	<li[#if currentMenu=='admin'] class="current"[/#if]><a href="${baseURL}/admin/">[@s.text name="menu.admin"/]</a></li>
			    	[/#if]
			    	<li[#if currentMenu=='about'] class="current"[/#if]><a href="${baseURL}/about.do">[@s.text name="menu.about"/]</a></li>
		        </ul>    
		    </div>
		    
		    <div id="search">
		     [#--
		        <form action="${baseURL}/search" method="get">
		            <div>
		                <input type="text" name="q" id="search-input" [#if !q??] class="form-reset" value='search ${searchText!" resources"}'[#else] value='${q}'[/#if] />
		                <input class="default" type="submit" value="Search" id="search-submit" />
		        	</div>
		        </form>
		     --]
		    </div>

[#if sideMenuEml!false]		    
			<div id="sidebar">
				<h2>[@s.text name='manage.metadata.section' /]</h2>
				<ul class="sidebar">
				[#list ["basic", "geocoverage", "taxcoverage","tempcoverage", "keywords", "parties", "project", "methods", "citations", "collections", "physical", "additional"] as it]
				 <li[#if currentSideMenu?exists && currentSideMenu==it] class="current"[#else] class="sidebar"[/#if]><a href="metadata-${it}.do?r=${resource.shortname!r!}">[@s.text name="submenu.${it}"/]</a></li>
				[/#list]
				</ul>
			</div>
			
			<div id="content" class="fixed">			
[#else]

			<div id="content">			
[/#if]


			[@s.actionmessage/]
			[#if warnings?size>0]		    
			 <ul class="warnMessage">
			 [#list warnings as w]
	          <li><span>${w!}</span></li>
			 [/#list]
             </ul>
            [/#if]
			[@s.actionerror/]
            
            <div id="dialog-confirm" title="[@s.text name="basic.confirm"/]" style="display: none;">
			</div>