<html>
	<head>
	<title><@s.text name="title"/></title>
	</head>
	<body>
		<h1><@s.text name="admin.config.setup.title"/></h1>
		<p><@s.text name="admin.config.setup.welcome"/></p>
		
		<@s.form action="setup.do" method="post">
			<@s.textfield key="admin.config.setup.datadir" name="dataDirPath" size="80" required="true"/>
		 	<@s.submit cssClass="button" name="save" key="button.save"/>
		</@s.form>
	</body>
</html>