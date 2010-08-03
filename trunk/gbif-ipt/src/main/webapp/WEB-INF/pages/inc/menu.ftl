[#ftl]

 	</head>
 	<body>
		<div id="wrapper">

		    <div id="topmenu">
		        <ul>
		      [#if (Session.curr_user)??] 
		        	<li>Logged in as <em>${Session.curr_user.email}</em></li>
		        	<li[#if currentMenu=="account"] class="current"[/#if]><a href="${baseURL}/account.do">[@s.text name="menu.account"/]</a></li>
		        	<li[#if currentMenu=="logout"] class="current"[/#if]><a href="${baseURL}/logout.do">[@s.text name="menu.logout"/]</a></li>
			  [#else]
		        	<li>
				        <form id="login-form" action="${baseURL}/login" method="post">
				        	<input type="text" size="15" name="email" value="admin" class="form-reset" />
		        			<input type="password" size="15" name="password" value="admin" class="form-reset" />
		        			<input type="submit" value="Login" id="login-submit"/>
				        </form>
					</li>
			  [/#if]
				    <li id="locale">
				    	<a href="#"><img src="${baseURL}/images/flags/flag_${localeLanguage}.gif"/></a>
					    <ul id="availableLocales">
				        	[#list ["en","de","es","fr"] as lang]
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
		        <form action="${baseURL}/search" method="get">
		            <div>
		                <input type="text" name="q" id="search-input" [#if !q??] class="form-reset" value='search ${searchText!" resources"}'[#else] value='${q}'[/#if] />
		                <input type="submit" value="Search" id="search-submit" />
		        	</div>
		        </form>
		    </div>

[#if sideMenuItems?exists]		    
			<div id="sidebar">
				[#if sideMenuTitle?exists]<h2>${sideMenuTitle}</h2>[/#if]
				<ul>
				[#list sideMenuItems as it]
				 <li[#if currentSideMenu?exists && currentSideMenu==it] class="current"[/#if]><a href="${it}.do">[@s.text name="submenu.${it}"/]</a></li>
				[/#list]
				</ul>
			</div>
			
			<div id="content" class="fixed">			
[#else]

			<div id="content">			
[/#if]


			[@s.actionmessage/]
			[@s.actionerror/]
			