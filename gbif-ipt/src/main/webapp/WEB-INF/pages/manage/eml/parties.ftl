<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.metadata.parties.title'/></title>
	<script type="text/javascript" src="${baseURL}/js/jconfirmaction.jquery.js"></script>
	<style>
	.2col-lft, .2col-rgt{
		width: 45%;
	}
	</style>
<script type="text/javascript">
$(document).ready(function(){
	$('.confirm').jConfirmAction({question : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});
});
</script>

	<#assign sideMenuEml=true />
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name='manage.metadata.parties.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>

<p><@s.text name='manage.metadata.parties.intro'/></p>


<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm" action="metadata-${section}.do" method="post">

<#assign next_agent_index=0 />
<#list eml.associatedParties as agent>
	<#assign next_agent_index=agent_index+1>
	<div class="2col">
  		<@input name="eml.associatedParties[${agent_index}].firstName" keyBase="" size=40/>
  		<@input name="eml.associatedParties[${agent_index}].lastName" keyBase="" size=40/>
  	</div>
	<div class="2col">
  		<@input name="eml.associatedParties[${agent_index}].phone" keyBase="" size=40/>
  		<@select name="eml.associatedParties[${agent_index}].role" value="${eml.associatedParties[agent_index].role}" keyBase="" options=roleOptions />
  	</div>
  	<hr/>
</#list>

<h2>New Party</h2>
	<div class="2col">
  		<@input name="eml.associatedParties[${next_agent_index}].firstName" value="" size=40/>
  		<@input name="eml.associatedParties[${next_agent_index}].lastName" value="" size=40/>
  	</div>
	<div class="2col">
  		<@input name="eml.associatedParties[${next_agent_index}].phone" value="" size=40/>
  		<@select name="eml.associatedParties[${next_agent_index}].role" value="" options=roleOptions />
  	</div>
  
  <div class="buttons">
 	<@s.submit name="save" key="button.save"/>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>	
</form>


<#include "/WEB-INF/pages/inc/footer.ftl">
