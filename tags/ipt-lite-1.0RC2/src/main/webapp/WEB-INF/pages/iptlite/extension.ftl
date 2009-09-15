<head>
    <title>${extension.name} Extension/></title>
    <meta name="menu" content="ManageMenu"/>
    <meta name="decorator" content="fullsize"/>
    <meta name="heading" content="${extension.name} Extension"/>
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
	<th><@s.text name='extension.link'/></th>
	<td><#if extension.link??><a href="${extension.link}" target="_blank">${extension.link}</a><#else> unavailable</#if></td>
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
						<th>Qualified Name:</th>
						<td>${p.qualName!}</td>
					</tr>
					<tr>
						<th>Namespace:</th>
						<td>${p.namespace!}</td>
					</tr>
					<tr>
						<th>Group:</th>
						<td>${p.group!}</td>
					</tr>
					<tr>
						<th>Name:</th>
						<td>${p.name!}</td>
					</tr>
				  	<#if p.link??>
					<tr>
						<th>Documentation</th>
						<td><a href="${p.link}" target="_blank">${p.link}</a></td>
					</tr>
					</#if>
				    <#if p.vocabulary??>
					<tr>
						<th>Vocabulary</th>
						<td><a href="vocab.html?id=${p.vocabulary.id?c}">${p.vocabulary.title}</a></td>
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
	<@s.submit action="extensions" cssClass="button" key="button.back" theme="simple"/>
</@s.form>