<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="manage.home.title"/> - ${r!"shortname"}</h1>

<p>Welcome dear manager!</p>
<p>Please manage this great resource <strong><em>${resource!"resource"}</em></strong></p>

<#include "/WEB-INF/pages/inc/footer.ftl">
