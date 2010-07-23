<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
	    <meta name="copyright" lang="en" content="GBIF" />
		<title><@s.text name="admin.config.setup.title"/></title>
		<style>
body {
	background-color: #cccccc;
	margin-bottom: 40px;
    text-align: left;
    font-family: arial, helvetica, verdana, sans-serif;
    font-size: 12px;
    line-height: 1em;
    color: #666666;
	
}

h1 {
	color: #077A2D;
	font-size: 30px;
}
a {
	color: #005418;
}
div#wrapper {
	position:relative;
	margin-left:auto;
	margin-right:auto;
	width:950px;
	background-color: #ffffff;
	padding:10px;
	margin-top: 30px;
	margin-bottom: 30px;
	padding-bottom:0px;
	padding-top: 0px;
}
div#content { 
	padding: 10px;
}
div#footer{
	margin-top: 30px;
	height: 30px;
	text-align: center;
	padding-top: 20px;
	background-color: #E6E6E6;
	margin-left:-10px;
	margin-right:-10px;
}
ul.fielderror{
	list-style:none; 
	color: #d8424f;
	padding: 0px;
	margin: 10px 0px 5px 0px;
}
.errorMessage{
	list-style:none; 
	color: #d8424f;
	padding: 0px;
	margin: 0px;
}
.actionMessage{
	list-style:none; 
	color: #6dd03e;
	padding: 0px;
	margin: 0px;
}		
		</style>
 	</head>
 	<body>
		<div id="wrapper">

			<div id="content">			

			<div id="logo">
				<img src="http://rs.gbif.org/style/logo.gif">					
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