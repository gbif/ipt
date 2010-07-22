<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="admin.extension.title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.extension.title"/> ${extension.title}</h1>

<table class="simple">
    <tr>
         <th>Title</th><td>${extension.title}</td>
    </tr>
    <tr>
         <th>Name</th><td>${extension.name}</td>
    </tr>
    <tr>
         <th>Namespace</th><td>${extension.namespace}</td>
    </tr>
    <tr>
         <th>RowType</th><td>${extension.rowType}</td>
    </tr>
    <tr>
         <th>Description</th><td>${extension.description}</td>
    </tr>
    <#if extension.link?has_content>
    <tr>
         <th>Link</th>
		<td>
         <a href="${extension.link}">${extension.link}</a>
		</td>
    </tr>
    </#if>
   </table>
<p><a href="extensions.do">back to extension list</a></p>
<br/>

<h1>Extension Properties</h1>
                                   
<#list extension.properties as p>	
<a name="${p.qualname}"></a>          
<div class="definition">	
  <div class="title">
  	<div class="head">
		${p.name}
  	</div>
  </div>
  <div class="body">
      	<div>
			${p.description}
			<#if p.description?has_content><br/></#if>              	
			<#if p.link?has_content>See also <a href="${p.link}">${p.link}</a></#if>              	
      	</div>
      	<div>
          	<em>Examples</em>: 
			${p.examples}
      	</div>
      	<#if p.vocabulary?exists>
      	<div>
	      	<em>Vocabulary</em>: 
	      	<a href="vocabulary.do?id=${p.vocabulary.uri}">${p.vocabulary.title}</a>
      	</div>
      	</#if>
      	<div class="details">
      		<table>
          		<tr><th>Qualname</th><td>${p.qualname}</td></tr>
          		<tr><th>Namespace</th><td>${p.namespace}</td></tr>
          		<tr><th>Group</th><td>${p.group!}</td></tr>
          		<tr><th>Length</th><td>${p.columnLength}</td></tr>
          		<tr><th>Required</th><td>${p.required?string}</td></tr>
      		</table>
      	</div>
  </div>
</div>
</#list>

<#include "/WEB-INF/pages/inc/footer.ftl">
