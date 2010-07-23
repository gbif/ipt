<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
	    <meta name="copyright" lang="en" content="GBIF" />
		<title><@s.text name="admin.config.setup.title"/></title>
 		<link rel="stylesheet" type="text/css" href="styles/main.css"/>
		<style>
h1 {
	color: #077A2D;
	font-size: 30px;
}
		</style>
 	</head>
 	<body>
		<div id="wrapper">

			<div id="content">			

			<div id="logo">
				<img src="styles/logo.jpg">					
			</div>
		<h1><@s.text name="admin.config.setup.title"/></h1>
		<p><@s.text name="admin.config.setup.welcome"/></p>
		
			<@s.actionmessage/>
			<@s.actionerror/>

		<@s.form action="setup.do" method="post">
			<@s.textfield key="admin.config.setup.datadir" name="dataDirPath" size="80" required="true"/>
		 	<@s.submit cssClass="button" name="save" key="button.save"/>
		</@s.form>

			</div>
			<div id="footer">
			    <span>Version ${cfg.version!"???"}&nbsp;|&nbsp;
			        <span>
			            <a href="http://code.google.com/p/gbif-providertoolkit/"><@s.text name="footer.projectHome"/></a>&nbsp;|&nbsp;
			            <a href="http://code.google.com/p/gbif-providertoolkit/issues/list"><@s.text name="footer.bugReport"/></a>&nbsp;|&nbsp;
			         	<span>
			       			 &copy; 2010 <a href="http://www.gbif.org">GBIF</a>
			    		</span>
			        </span>
			    </span>
			</div>			
		</div>
	</body>
</html>