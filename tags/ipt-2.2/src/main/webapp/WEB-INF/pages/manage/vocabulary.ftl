<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
 <title><@s.text name="admin.vocabulary.title"/></title>
 <#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1>${vocabulary.title}</h1>

<p>${vocabulary.description!}</p>
<#if vocabulary.subject?has_content>
<p><@s.text name="basic.keywords"/>: ${vocabulary.subject}</p>
</#if>
<#if vocabulary.link?has_content>
<p><@s.text name="basic.link"/>: <a href="${vocabulary.link}">${vocabulary.link}</a></p>
</#if>


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
  		<#if c.description?has_content>
      	<div>
			${c.description!}
      	</div>
      	</#if>
  		<#if c.link?has_content>
      	<div>
			<@s.text name="basic.seealso"/>: <a href="${c.link}">${c.link}</a>              	
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
  </div>
</div>
</#list>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>