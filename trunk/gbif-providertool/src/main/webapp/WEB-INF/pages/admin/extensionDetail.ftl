<head>
    <title>${extension.name} Extension/></title>
    <meta name="heading" content="${extension.name} Extension"/>
</head>

<table>	
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
	<td><#if extension.link??><a href="${extension.link}" target="_blank">${extension.link}</a></#if></td>
 </tr>
<#if extension.type??> 
 <tr>
	<th><@s.text name='extension.type'/></th>
	<td>${extension.type!"core"}</td>
 </tr>
 <tr>
	<th>Installed</th>
	<td>${extension.installed?string}</td>
 </tr>
</#if>
 <tr>
	<th><@s.text name='extension.properties'/></th>
	<td>
		<ul class="plain">
		<#list extension.properties as p>
			<li>
			  	<#if p.link??>
					<a class="info" href="${p.link}" target="_blank">${p.name}<span><strong>Qualified Name: </strong>${p.qualName!}<br/><strong>Namespace: </strong>${p.namespace!}</span></a>
			  	<#else>
					<a class="info">${p.name}<span><strong>Qualified Name: </strong>${p.qualName!}<br/><strong>Namespace: </strong>${p.namespace!}</span></a>
			  	</#if>
			  	
			    <#if !(p.terms?size==0)>
			    	<span class="info">
			    		&lt;term info&gt;
				    	<span><ul class="plain">
						<#list p.terms as t>
							<li>${t}</li>
						</#list>
						</ul></span>
					</span>
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