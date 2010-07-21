<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="admin.extension.title"/></title>
<script type="text/javascript">
$(document).ready(function(){
});
</script>
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.extension.title"/></h1>

<h3>Extensions currently installed in this IPT</h3>

<#list extensions as ext>	
<a name="${ext.rowType}"></a>          
<div class="definition">	
  <div class="title">
  	<div>
		${ext.title}
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
			${ext.description}
			<#if ext.relation?exists><br/><a href="${ext.link}">See also ${ext.link}</a></#if>              	
      	</div>
      	<div class="details">
      		<table>
          		<tr><th>Name</th><td>${ext.name}</td></tr>
          		<tr><th>Namespace</th><td>${ext.namespace}</td></tr>
          		<tr><th>RowType</th><td>${ext.rowType}</td></tr>
          		<tr><th>Keywords</th><td>${ext.subject}</td></tr>
      		</table>
      	</div>
  </div>
</div>
</#list>


<#list gbrdsExtensions as ext>
<a name="${ext.rowType}"></a>          
<div class="definition">	
  <div class="title">
  	<div class="head">
		${ext.title}
  	</div>
  	<div class="actions">
	  <form action='extension.do' method='post'>
		<input type='hidden' name='id' value='${ext.url}' />
		<input type='submit' name='install' value='Install' />
		<input type='submit' name='install' value='Install2' />
  	  </form>
  	</div>
  </div>
  <div class="body">
      	<div>
		${ext.description}
      	</div>
      	<div class="details">
      		<table>
          		<tr><th>RowType</th><td>${ext.rowType}</td></tr>
          		<tr><th>Keywords</th><td>${ext.subject}</td></tr>
      		</table>
      	</div>
  </div>
</div>
</#list>

<#include "/WEB-INF/pages/inc/footer.ftl">
