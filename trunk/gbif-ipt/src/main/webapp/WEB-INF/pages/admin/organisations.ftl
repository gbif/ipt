<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name="title"/></title>
<script type="text/javascript">
$(document).ready(function(){
	$('#organisation\\.key').click(function() {
	$('#organisation\\.name').val($('#organisation\\.key :selected').text());	
})
});
</script>	
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name="admin.organisation.title"/></h1>

<#include "/WEB-INF/pages/macros/forms.ftl"> 


<#list linkedOrganisations as org>	
<a name="${org.name}"></a>          
<div class="definition">	
  <div class="title">
  	<div class="head">
        <a href="organisation.do?id=${org.key}">${org.name}</a>
  	</div>
  	<div class="actions">
	  <form action='organisation.do' method='post'>
		<input type='hidden' name='id' value='${org.name}' />
		<input type='submit' name='delete' value='Remove' />
  	  </form>
  	</div>
  </div>
  <div class="body">
      	<div>
			${org.name!}
			<#if org.name?has_content><br/><@s.text name="basic.seealso"/> <a href="${org.key}">${org.name}</a></#if>              	
      	</div>
      	<div class="details">
      		<table>
          		<tr><th><@s.text name="organisation.key"/></th><td>${org.key}</td></tr>
          		<tr><th><@s.text name="basic.name"/></th><td>${org.name}</td></tr>
      		</table>
      	</div>
  </div>
</div>
</#list>


<@s.form id="organisationsForm" cssClass="ftlTopForm" action="organisation.do" method="post">
	<@s.hidden id="organisation.name" name="organisation.name" value="b" required="true"/>
	<@selectList name="organisation.key" options="organisations" objValue="key" objTitle="name" keyBase="admin." value="" size=15/>  
	<@input name="organisation.password" keyBase="admin." type="text"/>
	<@input name="organisation.alias" keyBase="admin." type="text"/>
   <div class="buttons">
 	<@s.submit cssClass="button" name="save" key="button.save"/>
  </div>	  
</@s.form>

<#include "/WEB-INF/pages/inc/footer.ftl">
