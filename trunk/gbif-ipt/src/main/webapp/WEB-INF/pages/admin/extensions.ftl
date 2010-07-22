<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="admin.extensions.title"/></title>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.extensions.title"/></h1>

<#list extensions as ext>	
<a name="${ext.rowType}"></a>          
<div class="definition">	
  <div class="title">
  	<div class="head">
        <a href="extension.do?id=${ext.rowType}">${ext.title}</a>
  	</div>
  	<div class="actions">
	  <form action='extension.do' method='post'>
		<input type='hidden' name='id' value='${ext.rowType}' />
		<input type='submit' name='delete' value='Remove' />
  	  </form>
  	</div>
  </div>
  <div class="body">
      	<div>
			${ext.description!}
			<#if ext.link?has_content><br/>See also <a href="${ext.link}">${ext.link}</a></#if>              	
      	</div>
      	<div class="details">
      		<table>
          		<tr><th>Properties</th><td>${ext.properties?size}</td></tr>
          		<tr><th>Name</th><td>${ext.name}</td></tr>
          		<tr><th>Namespace</th><td>${ext.namespace}</td></tr>
          		<tr><th>RowType</th><td>${ext.rowType}</td></tr>
          		<tr><th>Keywords</th><td>${ext.subject!}</td></tr>
      		</table>
      	</div>
  </div>
</div>
</#list>

<hr/>

<h3>Extensions available from the GBIF registry</h3>

<#list gbrdsExtensions as ext>
<a name="${ext.rowType}"></a>          
<div class="definition">	
  <div class="title">
  	<div class="head">
		${ext.title}
  	</div>
  	<div class="actions">
	  <form action='extension.do' method='post'>
		<input type='hidden' name='url' value='${ext.url}' />
		<input type='submit' name='install' value='Install' />
  	  </form>
  	</div>
  </div>
  <div class="body">
      	<div>
		${ext.description!}
      	</div>
      	<div class="details">
      		<table>
          		<tr><th>RowType</th><td>${ext.rowType}</td></tr>
          		<tr><th>Keywords</th><td>${ext.subject!}</td></tr>
      		</table>
      	</div>
  </div>
</div>
</#list>

<#include "/WEB-INF/pages/inc/footer.ftl">
