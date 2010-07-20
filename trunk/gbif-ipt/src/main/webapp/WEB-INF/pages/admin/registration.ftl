<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.registration.title"/></h1>
${organisations}
<#--
<@s.actionmessage/>
<@s.actionerror/>
 
<@s.form id="registrationForm" cssClass="ftlTopForm" action="save" method="post">
	<@selectList name="organisationList" options="organisations" objValue="key" objTitle="name" keyBase="admin.config." />  

 	<@s.submit cssClass="button" name="save" key="button.save"/>
</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">
-->

