<#include "/WEB-INF/pages/inc/header.ftl">
 <title><@s.text name="admin.home.manageExtensions"/></title>
 <#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.extension.coreTypes"/></h1>

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
			${ext.description!}
			<#if ext.link?has_content><br/><@s.text name="basic.seealso"/> <a href="${ext.link}">${ext.link}</a></#if>              	
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
	<@s.text name="admin.extension.no.coreTypes.installed"/>
</#if>

<div class="newline"></div>
<div class="newline"></div>
<h1><@s.text name="admin.extension.extensions"/></h1>

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
			${ext.description!}
			<#if ext.link?has_content><br/><@s.text name="basic.seealso"/> <a href="${ext.link}">${ext.link}</a></#if>              	
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
	<@s.text name="admin.extension.no.extensions.installed"/>
</#if>

<#if (numVocabs>0)>
<hr/>
<h3><@s.text name="extension.vocabularies.title"/></h3>
<p>
	<@s.text name="extension.vocabularies.last.update"><@s.param>${dateFormat}</@s.param></@s.text>
	  <form action='extensions.do' method='post'>
	  	<@s.submit name="updateVocabs" key="button.update"/>
	<@s.text name="extension.vocabularies.number"><@s.param>${numVocabs}</@s.param></@s.text>
  	  </form>
</p>
</#if>
<hr/>

<h3><@s.text name="extension.further.title"/></h3>

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
		${ext.description!}
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

<#include "/WEB-INF/pages/inc/footer.ftl">
