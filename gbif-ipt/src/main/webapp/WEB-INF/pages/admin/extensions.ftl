<#include "/WEB-INF/pages/inc/header.ftl">
 <title><@s.text name="admin.home.manageExtensions"/></title>
 <#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<div class="grid_23">
<h1><@s.text name="admin.extension.coreTypes"/></h1>
  <p>
  <@s.text name="admin.extension.no.coreTypes.installed.help"><@s.param>${cfg.registryUrl}</@s.param></@s.text>
  </p>
<#assign count=0>

<#list extensions as ext>
<#if ext.core>
<#assign count=count+1>
<a name="${ext.rowType}"></a>          
<div class="definition">	
  <div class="title">
  	<div class="head">
        <a href="extension.do?id=${ext.rowType}">${ext.title}</a>
  	</div>
  	<div class="actions">
	  <form action='extension.do' method='post'>
		<input type='hidden' name='id' value='${ext.rowType}' />
		<@s.submit name="delete" key="button.remove"/>
  	  </form>
  	</div>
  </div>
  <div class="body">
      	<div>
			<p>${ext.description!}
			<#if ext.link?has_content><br/><@s.text name="basic.seealso"/> <a href="${ext.link}">${ext.link}</a></#if></p>             	
      	</div>
      	<div class="details">
      		<table>
          		<tr><th><@s.text name="extension.properties"/></th><td>${ext.properties?size}</td></tr>
          		<tr><th><@s.text name="basic.name"/></th><td>${ext.name}</td></tr>
          		<tr><th><@s.text name="basic.namespace"/></th><td>${ext.namespace}</td></tr>
          		<tr><th><@s.text name="extension.rowtype"/></th><td>${ext.rowType}</td></tr>
          		<tr><th><@s.text name="basic.keywords"/></th><td>${ext.subject!}</td></tr>
      		</table>
      	</div>
  </div>
</div>
</#if>
</#list>
<#if count=0>
  <p class="warn">
    <@s.text name="admin.extension.no.coreTypes.installed"/>
  </p>
  <p>
    <img src="${baseURL}/images/warning.gif"/>
    <@s.text name="admin.extension.no.coreTypes.installed.debug"><@s.param>${cfg.registryUrl}</@s.param></@s.text>
  </p>
</#if>
</div>

<div class="grid_23">
<h1><@s.text name="admin.extension.extensions"/></h1>
  <p>
  <@s.text name="admin.extension.no.extensions.installed.help"><@s.param>${cfg.registryUrl}</@s.param></@s.text>
  </p>
<#assign count=0>
<#list extensions as ext>
<#if !ext.core>
<#assign count=count+1>
<a name="${ext.rowType}"></a>          
<div class="definition">	
  <div class="title">
  	<div class="head">
        <a href="extension.do?id=${ext.rowType}">${ext.title}</a>
  	</div>
  	<div class="actions">
	  <form action='extension.do' method='post'>
		<input type='hidden' name='id' value='${ext.rowType}' />
		<@s.submit name="delete" key="button.remove"/>
  	  </form>
  	</div>
  </div>
  <div class="body">
      	<div>
      	    <p>${ext.description!}
			<#if ext.link?has_content><br/><@s.text name="basic.seealso"/> <a href="${ext.link}">${ext.link}</a></#if></p>             	
      	</div>
      	<div class="details">
      		<table>
          		<tr><th><@s.text name="extension.properties"/></th><td>${ext.properties?size}</td></tr>
          		<tr><th><@s.text name="basic.name"/></th><td>${ext.name}</td></tr>
          		<tr><th><@s.text name="basic.namespace"/></th><td>${ext.namespace}</td></tr>
          		<tr><th><@s.text name="extension.rowtype"/></th><td>${ext.rowType}</td></tr>
          		<tr><th><@s.text name="basic.keywords"/></th><td>${ext.subject!}</td></tr>
      		</table>
      	</div>
  </div>
  <div class="clearfix"></div>
</div>
</#if>
</#list>
<#if count=0>
  <p class="warn">
    <@s.text name="admin.extension.no.extensions.installed"/>
  </p>
</#if>

<#if (numVocabs>0)>
</div>
<hr/>
<div class="grid_23">
<h3><@s.text name="extension.vocabularies.title"/></h3>

  <p>
    <@s.text name="admin.extensions.vocabularies.update.help"/>
  </p>
  <p>
  <@s.text name="extension.vocabularies.last.update"><@s.param>${dateFormat}</@s.param></@s.text>

  <form action='extensions.do' method='post'>
    <@s.submit name="updateVocabs" key="button.update"/>
	<@s.text name="extension.vocabularies.number"><@s.param>${numVocabs}</@s.param></@s.text>
  </form>
  </p>
</#if>

</div>
<hr/>

<div class="grid_23">
<h3><@s.text name="extension.further.title"/></h3>
  <p>
    <@s.text name="extension.further.title.help"/>
  </p>

<#assign count=0>
<#list newExtensions as ext>
<#assign count=count+1>
<div class="definition">	
  <div class="title">
  	<div class="head">
		${ext.title}
  	</div>
  	<div class="actions">
	  <form action='extension.do' method='post'>
		<input type='hidden' name='url' value='${ext.url}' />
		<@s.submit name="install" key="button.install"/>
  	  </form>
  	</div>
  </div>
  <div class="body">
      	<div>
		<p>${ext.description!}</p>
      	</div>
      	<div class="details">
      		<table>
          		<tr><th><@s.text name="extension.rowtype"/></th><td>${ext.rowType!}</td></tr>
          		<tr><th><@s.text name="basic.keywords"/></th><td>${ext.subject!}</td></tr>
      		</table>
      	</div>
  </div>
</div>
</#list>
<#if count=0>
	<@s.text name="extension.already.installed"/>
</#if>
</div>
<#include "/WEB-INF/pages/inc/footer.ftl">
