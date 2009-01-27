<head>
    <title><@s.text name="resource.title"/></title>
    <meta name="resource" content="${resource.title}"/>
    <meta name="submenu" content="meta"/>
</head>
	

<img class="right" src="${cfg.getResourceLogoUrl(resource_id)}" />
<h2>${resource.title}</h2>	

<@s.form>

<table>	
 <tr>
	<th>GUID</th>
	<td><a href="${cfg.getResourceUrl(resource.guid)}">${resource.guid}</a></td>
 </tr>
 <tr>
	<th>EML</th>
	<td><a href="${cfg.getEmlUrl(resource.guid)}">EML</a></td>
 </tr>
</table>

<div id="basics">
	<fieldset>
		<h2>${resource.title!}</h2>
		<table>	
			<tr>
			  <th>title</th>
			  <td>${resource.title!}</td>
			</tr>
			<tr>
			  <th>status</th>
			  <td>${resource.status!}</td>
			</tr>
			<tr>
			  <th>contactName</th>
			  <td>${resource.contactName!}</td>
			</tr>
			<tr>
			  <th>contactEmail</th>
			  <td>${resource.contactEmail!}</td>
			</tr>
			<tr>
			  <th>description</th>
			  <td>${resource.description!}</td>
			</tr>
		</table>
	</fieldset>
</div>

<div>
	<fieldset>
		<h2>resourceCreator</h2>
		<table>
			<tr>
			  <th>eml.resourceCreator.firstName</th>
			  <td>${eml.resourceCreator.firstName!}</td>
			</tr>
			<tr>
			  <th>eml.resourceCreator.lastName</th>
			  <td>${eml.resourceCreator.lastName!}</td>
			</tr>
			<tr>
			  <th>eml.resourceCreator.organisation</th>
			  <td>${eml.resourceCreator.organisation!}</td>
			</tr>
			<tr>
			  <th>eml.resourceCreator.position</th>
			  <td>${eml.resourceCreator.position!}</td>
			</tr>
			<tr>
			  <th>eml.resourceCreator.phone</th>
			  <td>${eml.resourceCreator.phone!}</td>
			</tr>
		</table>
	</fieldset>
</div>

</@s.form>
