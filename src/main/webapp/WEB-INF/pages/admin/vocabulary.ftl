<#include "/WEB-INF/pages/inc/header.ftl">
 <title><@s.text name="admin.vocabulary.title"/></title>
 <#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.vocabulary.title"/> ${vocabulary.title}</h1>

<table class="simple">
    <tr>
         <th><@s.text name="basic.title"/></th><td>${vocabulary.title}</td>
    </tr>
    <tr>
         <th><@s.text name="basic.identifier"/></th><td>${vocabulary.uri}</td>
    </tr>
    <tr>
         <th><@s.text name="basic.description"/></th><td>${vocabulary.description!}</td>
    </tr>
    <tr>
         <th><@s.text name="basic.keywords"/></th><td>${vocabulary.subject!}</td>
    </tr>
    <#if vocabulary.link?has_content>
    <tr>
         <th><@s.text name="basic.link"/></th>
		<td>
         <a href="${vocabulary.link}">${vocabulary.link}</a>
		</td>
    </tr>
    </#if>
    <tr>
         <th><@s.text name="basic.lastModified"/></th><td>${vocabulary.lastUpdate?datetime?string.medium}</td>
    </tr>
   </table>
<br/>

<h1><@s.text name="vocabulary.concepts"/></h1>
                               
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
			<#if c.link?has_content><br/><a href="${c.link}"><@s.text name="basic.seealso"/> ${c.link}</a></#if>              	
      	</div>
      	</#if>
      	<div>
          	<@s.text name="vocabulary.terms.pref"/>:
          	<em><#list c.preferredTerms as t>${t.title} <span class="small">[${t.lang}]</span>; </#list></em>
      	</div>
      	<div>
          	<@s.text name="vocabulary.terms.alt"/>: 
          	<em><#list c.alternativeTerms as t>${t.title} <span class="small">[${t.lang}]</span>; </#list></em>
      	</div>
      	<div class="details">
      		<table>
          		<tr><th><@s.text name="basic.identifier"/></th><td>${c.identifier}</td></tr>
          		<tr><th><@s.text name="vocabulary.uri"/></th><td>${c.uri}</td></tr>
      		</table>
      	</div>
  </div>
</div>
</#list>

<#include "/WEB-INF/pages/inc/footer.ftl">
