<#include "/WEB-INF/pages/inc/header.ftl">
 <title><@s.text name="admin.extension.title"/></title>
 <#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.extension.title"/> ${extension.title}</h1>

<table class="simple">
    <tr>
         <th><@s.text name="basic.title"/></th><td>${extension.title}</td>
    </tr>
    <tr>
         <th><@s.text name="basic.name"/></th><td>${extension.name}</td>
    </tr>
    <tr>
         <th><@s.text name="basic.namespace"/></th><td>${extension.namespace}</td>
    </tr>
    <tr>
         <th><@s.text name="extension.rowtype"/></th><td>${extension.rowType}</td>
    </tr>
    <tr>
         <th><@s.text name="basic.description"/></th><td>${extension.description}</td>
    </tr>
    <#if extension.link?has_content>
    <tr>
         <th><@s.text name="basic.link"/></th>
		<td>
         <a href="${extension.link}">${extension.link}</a>
		</td>
    </tr>
    </#if>
   </table>

	
   
<p>
	<a href="extensions.do"><button><@s.text name="button.back"/></button></a>
</p>
<br/>

<h1><@s.text name="admin.extension.properties"/></h1>
                                   
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
			<#if p.link?has_content><@s.text name="basic.seealso"/> <a href="${p.link}">${p.link}</a></#if>              	
      	</div>
      	<div>
          	<em><@s.text name="basic.examples"/></em>: 
			${p.examples}
      	</div>
      	<#if p.vocabulary?exists>
      	<div>
	      	<em><@s.text name="extension.vocabulary"/></em>: 
	      	<a href="vocabulary.do?id=${p.vocabulary.uri}">${p.vocabulary.title}</a>
      	</div>
      	</#if>
      	<div class="details">
      		<table>
          		<tr><th><@s.text name="extension.prop.qname"/></th><td>${p.qualname}</td></tr>
          		<tr><th><@s.text name="basic.namespace"/></th><td>${p.namespace}</td></tr>
          		<tr><th><@s.text name="extension.prop.group"/></th><td>${p.group!}</td></tr>
          		<tr><th><@s.text name="extension.prop.type"/></th><td>${p.type}</td></tr>
          		<tr><th><@s.text name="extension.prop.required"/></th><td>${p.required?string}</td></tr>
      		</table>
      	</div>
  </div>
</div>
</#list>

<#include "/WEB-INF/pages/inc/footer.ftl">
