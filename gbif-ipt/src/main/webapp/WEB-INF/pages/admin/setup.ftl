<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="admin.config.setup.title"/></title>
 	</head>
 	<body>
		<div id="wrapper">

			<div id="logo">
				<img src="${baseURL}/styles/logo.jpg">					
			</div>
			<div id="content">			

		<h1><@s.text name="admin.config.setup.title"/></h1>
		<p><@s.text name="admin.config.setup.welcome"/></p>
		
		<@s.form action="setup.do" method="post">
			<@s.textfield key="admin.config.setup.datadir" name="dataDirPath" size="80" required="true"/>
		 	<@s.submit cssClass="button" name="save" key="button.save"/>
		</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">
