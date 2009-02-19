<head>
    <title>${extension.name} Extension/></title>
    <meta name="decorator" content="fullsize"/>
    <meta name="menu" content="AdminMenu"/>
    <meta name="heading" content="${extension.name} Extension"/>
</head>

<div class="horizontal_dotted_line_xlarge"></div>
<table class="extensionTable">	
 <tr>
	<th><@s.text name='extension.name'/></th>
	<td>${extension.name}</td>
 </tr>
 <tr>
	<th><@s.text name='extension.tablename'/></th>
	<td>${tableName}</td>
 </tr>
 <tr>
	<th><@s.text name='extension.link'/></th>
	<td><#if extension.link??><img src="/images/assets/info_on.png"/><a href="${e.link}" target="_blank"> view info</a><#else><img src="/images/assets/info_off.png"/> unaviable</#if></td>
 </tr>
<#if extension.type??> 
 <tr>
	<th><@s.text name='extension.type'/></th>
	<td>${extension.type!"core"}</td>
 </tr>
 <tr>
	<th>Installed</th>
	<td><#if extension.installed==true><img src="/images/assets/checktrue.png"/> yes<#else><img src="/images/assets/checkfalse.png"/> no</#if></td>
 </tr>
</#if>
 <tr>
	<th><@s.text name='extension.properties'/></th>
	<td>
		<ul class="plain">
		<#list extension.properties as p>
			<li>
				<#if p??>
				  	<#if p.link??>
						<a class="info" href="${p.link}" target="_blank">
				  	<#else>
						<a class="info">
				  	</#if>
					${p.name}<span><strong>Qualified Name: </strong>${p.qualName!}<br/><strong>Namespace: </strong>${p.namespace!}<br/><strong>Group: </strong>${p.group!}</span></a>
				  	
				    <#if p.vocabulary??>
				    	<span class="info">
				    		--&gt; <a href="vocabulary.html?id=${p.vocabulary.id?c}">vocabulary</a>
						</span>
					</#if>
			  	<#else>
			  	 NULL property
			  	</#if>
			</li>
		</#list>
		</ul>
	</td>
 </tr>
</table>

<@s.form action="extensionDetail">
    <@s.hidden name="id" value="${extension.id}"/>
	<@s.submit action="extensions" cssClass="button" key="button.done" theme="simple"/>
	<#if extension.installed && extension.type??>
		<@s.submit action="delExtension" cssClass="button" key="button.remove" theme="simple" onclick="return confirmDelete('extension')"/>
	<#else>
		<#if extension.type??>
			<@s.submit action="addExtension" cssClass="button" key="button.install" theme="simple"/>
		</#if>
	</#if>
</@s.form>