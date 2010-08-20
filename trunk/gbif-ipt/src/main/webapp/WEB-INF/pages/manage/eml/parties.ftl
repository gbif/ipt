<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.metadata.parties.title'/></title>
<#include "/WEB-INF/pages/macros/metadata.ftl"/>
<#assign sideMenuEml=true />
<#include "/WEB-INF/pages/inc/menu.ftl">
<h1><@s.text name='manage.metadata.parties.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>
<p><@s.text name='manage.metadata.parties.intro'/></p>
<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm" action="metadata-${section}.do" method="post">
<div id="items">
<#assign next_agent_index=0 />
<#list eml.associatedParties as agent>
	<#assign next_agent_index=agent_index+1>
	<div id="item-${agent_index}" class="item">
	<div class="newline"></div>
	<div class="right">
      <a id="removeLink-${agent_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.parties.item'/> ]</a>
    </div>
    <div class="newline"></div>
	<div class="2col">
  		<@input name="eml.associatedParties[${agent_index}].firstName" i18nkey="eml.associatedParties.firstName" size=40/>
  		<@input name="eml.associatedParties[${agent_index}].lastName" i18nkey="eml.associatedParties.lastName" size=40/>
  	</div>
	<div class="2col">
  		<@input name="eml.associatedParties[${agent_index}].phone" i18nkey="eml.associatedParties.phone" size=40/>
  		<@select name="eml.associatedParties[${agent_index}].role" i18nkey="eml.associatedParties.role" value="${eml.associatedParties[agent_index].role}" options=roleOptions />
  	</div>
  	<div class="newline"></div>
	<div class="horizontal_dotted_line_large_foo" id="separator"></div>
	<div class="newline"></div>
  	</div>
</#list>
</div>
<a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.parties.item'/></a>
<div id='buttons' class="buttons">
    <@s.submit name="save" key="button.save"/>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>	
</form>
<div id="baseItem" class="item" style="display:none;">
	<div class="newline"></div>
	<div class="right">
      <a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.parties.item'/> ]</a>
    </div>
    <div class="newline"></div>
	<div class="2col">
  		<@input name="firstName" i18nkey="eml.associatedParties.firstName" value="" size=40/>
  		<@input name="lastName" i18nkey="eml.associatedParties.lastName"  value="" size=40/>
  	</div>
	<div class="2col">
  		<@input name="phone" i18nkey="eml.associatedParties.phone" value="" size=40/>
  		<@select name="role" i18nkey="eml.associatedParties.role" value="" options=roleOptions />
  	</div>
  	<div class="newline"></div>
	<div class="horizontal_dotted_line_large_foo" id="separator"></div>
	<div class="newline"></div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
