<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1><@s.text name="manage.home.title"/></h1>
<br/>

<#if (resources?size>0)>
<table>
	<tr>
		<th></th>
		<th>Name</th>
		<th>Type</th>
		<th>Last modified</th>
		<th>Visible to</th>
		<th>Visibility options</th>
		<#-- see if the ADMIN has enabled registrations -->
		<#if registrationAllowed>
		<th>Registered</th>
		</#if>
	</tr>
<#list resources as r>
  <tr>
	<td>
		<button>Delete</button>
	</td>
	<td><a href="resource.do?r=${r.shortname}">${r.title!r.shortname}</a></td>
	<td>${r.type!"???"}</td>
	<td>${r.modified?date}</td>
	<td>
		<#if r.status=='PRIVATE'>
			You and ${(r.managers?size)!0} others
		<#else>
			Everyone
		</#if>
	</td>
	<td>
		<#if r.status=='PRIVATE'>
			<button>Allow everyone</button>
		<#elseif r.status=='PUBLIC'>
			<button>Restrict to managers</button>
		</#if>
	</td>
	<#if registrationAllowed>
	<td>
		<#if r.status=='REGISTERED'>
			Yes
		<#elseif r.status=='PUBLIC'>
			<button>Register</button>
		<#else>
			No
		</#if>
	</td>
	</#if>
  </tr>
</#list>
</table>

<#else>
	<p>None, please create a resource or ask existing managers to add you to their resource(s).</p>
</#if>


<br/>
<hr/>
<h3>Create New Resource</h3>

<@s.form cssClass="ftlTopForm" action="create.do" method="post">
  	<@input name="shortname" keyBase="manage.resource.create." size=40/>
  <div>
	<@s.fielderror cssClass="fielderror" fieldName="file"/>
	<label for="file"><@s.text name="manage.resource.create.file"/></label>
    <@s.file name="file" key="manage.resource.create.file" required="false"/>
  </div>
  
  <div class="buttons">
 	<@s.submit cssClass="button" name="create" key="manage.home.button.createNewResource"/>
  </div>	
</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">
