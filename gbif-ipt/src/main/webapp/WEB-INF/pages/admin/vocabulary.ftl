<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="admin.vocabulary.title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.vocabulary.title"/> ${vocabulary.title}</h1>

<table class="simple">
    <tr>
         <th>Title</th><td>${vocabulary.title}</td>
    </tr>
    <tr>
         <th>Identifier</th><td>${vocabulary.uri}</td>
    </tr>
    <tr>
         <th>Description</th><td>${vocabulary.description!}</td>
    </tr>
    <tr>
         <th>Keywords</th><td>${vocabulary.subject!}</td>
    </tr>
    <#if vocabulary.link?has_content>
    <tr>
         <th>Link</th>
		<td>
         <a href="${vocabulary.link}">${vocabulary.link}</a>
		</td>
    </tr>
    </#if>
   </table>
<br/>

<h1><@s.text name="admin.vocabulary.concepts"/></h1>
                               
<#list vocabulary.concepts as c>	
<a name="${c.identifier}"></a>          
<div class="definition">	
  <div class="title">
  	<div class="head">
		${c.identifier}
  	</div>
  </div>
  <div class="body">
  		<#if c.description?has_content || c.link?has_content>
      	<div>
			${c.description!}
			<#if c.link?has_content><br/><a href="${c.link}">See also ${c.link}</a></#if>              	
      	</div>
      	</#if>
      	<div>
          	<em>Preferred Terms</em>:
          	<#list c.preferredTerms as t>${t.title} [${t.lang}]; </#list>
      	</div>
      	<div>
          	<em>Alternative Terms</em>: 
          	<#list c.alternativeTerms as t>${t.title} [${t.lang}]; </#list>
      	</div>
      	<div class="details">
      		<table>
          		<tr><th>Identifier</th><td>${c.identifier}</td></tr>
          		<tr><th>URI</th><td>${c.uri}</td></tr>
      		</table>
      	</div>
  </div>
</div>
</#list>

<#include "/WEB-INF/pages/inc/footer.ftl">
