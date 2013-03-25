<head>
    <title><@s.text name='extension.title'/> ${extension.name}/></title>
    <meta name="decorator" content="fullsize"/>
    <meta name="menu" content="AdminMenu"/>
    <meta name="heading" content="<@s.text name='extension.title'/> ${extension.name}"/>
	<script type="text/javascript">  
		$(document).ready(function(){
			$("table.propertyInfo").hide();			
			$("table.propertyInfo").click(function (e) {
				$(this).slideToggle("fast");				
		    });
			$("a.property").click(function (e) {
				$("table.propertyInfo", $(this).parent()).slideToggle("fast");				
		    });
		});
	</script>
	<style>
		.propertyInfo{
		 	margin-top: 5px;
		 	padding: 8px;
			background-color: #eee;
			border: 1px solid #333;
		}
	</style>
</head>

<table class="extensionTable">	
 <tr>
	<th><@s.text name='extension.name'/></th>
	<td>${extension.name}</td>
 </tr>
 <tr>
	<th><@s.text name='extension.namespace'/></th>
	<td>${extension.namespace}</td>
 </tr>
 <tr>
	<th><@s.text name='extension.tablename'/></th>
	<td>${tableName}</td>
 </tr>
 <tr>
	<th><@s.text name='extension.link'/></th>
	<td><#if extension.link??><a href="${extension.link}" target="_blank">${extension.link}</a><#else> unavailable</#if></td>
 </tr>
 <tr>
	<th><@s.text name='extension.install'/></th>
	<td><#if extension.installed==true><img src="<@s.url value='/images/assets/bullet_green.gif'/>"/> yes<#else><img src="<@s.url value='/images/assets/bullet_delete.gif'/>"/> no</#if></td>
 </tr>
 <tr>
	<th><@s.text name='extension.properties'/></th>
	<td>
	  <#list properties?keys as group>
	    <br/>
	    <h4>${group}</h4>
		<ul class="plain">
		<#list properties[group] as p>
			<li>
				<#if p??>
				<a class="property">${p.name}</a>
				<table class="propertyInfo">
					<tr>
						<th><@s.text name='extension.properties.qualifiedname'/></th>
						<td>${p.qualName!}</td>
					</tr>
					<tr>
						<th><@s.text name='extension.properties.namespace'/></th>
						<td>${p.namespace!}</td>
					</tr>
					<tr>
						<th><@s.text name='extension.properties.group'/></th>
						<td>${p.group!}</td>
					</tr>
					<tr>
						<th><@s.text name='extension.properties.name'/></th>
						<td>${p.name!}</td>
					</tr>
				  	<#if p.link??>
					<tr>
						<th><@s.text name='extension.properties.documentation'/></th>
						<td><a href="${p.link}" target="_blank">${p.link}</a></td>
					</tr>
					</#if>
				    <#if p.vocabulary??>
					<tr>
						<th><@s.text name='extension.properties.vocabulary'/></th>
						<td><a href="vocabulary.html?id=${p.vocabulary.id?c}">${p.vocabulary.title}</a></td>
					</tr>
					</#if>
				</table>
			  	<#else>
			  	 NULL property
			  	</#if>
			</li>
		</#list>
		</ul>
	  </#list>
	</td>
 </tr>
</table>

<@s.form action="extensionDetail">
    <@s.hidden name="id" value="${extension.id}"/>
	<@s.submit action="extensions" cssClass="button" key="button.done" theme="simple"/>
	<#if extension.installed && !extension.core>
		<@s.submit action="delExtension" cssClass="button" key="button.remove" theme="simple" onclick="return confirmDelete('extension')"/>
	<#else>
		<#if !extension.core>
			<@s.submit action="addExtension" cssClass="button" key="button.install" theme="simple"/>
		</#if>
	</#if>
</@s.form>