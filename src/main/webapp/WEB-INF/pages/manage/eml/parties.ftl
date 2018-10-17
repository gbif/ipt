<#escape x as x?html>
<#setting number_format="#####.##">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.metadata.parties.title'/></title>
<#include "/WEB-INF/pages/macros/metadata_agent.ftl"/>
<script type="text/javascript">
$(document).ready(function(){
    initHelp();
});
</script>
<#assign sideMenuEml=true />
 <#assign currentMenu="manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1><span class="superscript"><@s.text name='manage.overview.title.label'/></span>
    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
</h1>
<div class="grid_17 suffix_1">
<h2 class="subTitle"><@s.text name='manage.metadata.parties.title'/></h2>
<form class="topForm" action="metadata-${section}.do" method="post">
    <p><@s.text name='manage.metadata.parties.intro'/></p>

    <!-- retrieve some link names one time -->
    <#assign copyLink><@s.text name="eml.resourceCreator.copyLink"/></#assign>
    <#assign removeLink><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.parties.item'/></#assign>
    <#assign addLink><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.parties.item'/></#assign>

    <div id="associatedParty-items">
		<#list eml.associatedParties as item>
			<div id="associatedParty-item-${item_index}" class="item clearfix">
				<div class="columnLinks">
					<div class="halfcolumn">
  	  					<a id="associatedParty-copyDetails-${item_index}" href="">[ ${copyLink?lower_case?cap_first} ]</a>
    				</div>
					<div class="halfcolumn">
		      			<a id="associatedParty-removeLink-${item_index}" class="removeAssociatedPartyLink" href="">[ ${removeLink?lower_case?cap_first} ]</a>
		    		</div>
		    	</div>
				<div class="halfcolumn">
		  			<@input name="eml.associatedParties[${item_index}].firstName" i18nkey="eml.associatedParties.firstName"/>
				</div>
			  	<div class="halfcolumn">
		  			<@input name="eml.associatedParties[${item_index}].lastName" i18nkey="eml.associatedParties.lastName" requiredField=true/>
		  		</div>
		  		<div class="halfcolumn">
		  			<@input name="eml.associatedParties[${item_index}].position" i18nkey="eml.associatedParties.position" requiredField=true />
		  		</div>
  				<div class="halfcolumn">
		  			<@input name="eml.associatedParties[${item_index}].organisation" i18nkey="eml.associatedParties.organisation" requiredField=true />
		  		</div>
		  		<div class="halfcolumn">
		  			<@input name="eml.associatedParties[${item_index}].address.address" i18nkey="eml.associatedParties.address.address" />
				</div>
			  	<div class="halfcolumn">
		  			<@input name="eml.associatedParties[${item_index}].address.city" i18nkey="eml.associatedParties.address.city" />
		  		</div>
		  		<div class="halfcolumn">
		  			<@input name="eml.associatedParties[${item_index}].address.province" i18nkey="eml.associatedParties.address.province" />
				</div>
  				<div class="halfcolumn countryList">
		  			<@select name="eml.associatedParties[${item_index}].address.country" help="i18n" options=countries i18nkey="eml.associatedParties.address.country" value="${eml.associatedParties[item_index].address.country!}"/>
		  		</div>
		  		<div class="halfcolumn">
		  			<@input name="eml.associatedParties[${item_index}].address.postalCode" i18nkey="eml.associatedParties.address.postalCode" />
				</div>
  				<div class="halfcolumn">
		  			<@input name="eml.associatedParties[${item_index}].phone" i18nkey="eml.associatedParties.phone" />
		  		</div>
		  		<div class="halfcolumn">
		  			<@input name="eml.associatedParties[${item_index}].email" i18nkey="eml.associatedParties.email" />
				</div>
  		  <div class="halfcolumn">
		  	  <@input name="eml.associatedParties[${item_index}].homepage" i18nkey="eml.associatedParties.homepage" type="url" />
		    </div>
        <div class="halfcolumn">
          <#if eml.associatedParties[item_index].userIds[0]??>
            <@select name="eml.associatedParties[${item_index}].userIds[0].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" value="${eml.associatedParties[item_index].userIds[0].directory!}"/>
          <#else>
            <@select name="eml.associatedParties[${item_index}].userIds[0].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" value=""/>
          </#if>
        </div>
        <div class="halfcolumn">
          <#if eml.associatedParties[item_index].userIds[0]??>
            <@input name="eml.associatedParties[${item_index}].userIds[0].identifier" help="i18n" i18nkey="eml.contact.identifier" value="${eml.associatedParties[item_index].userIds[0].identifier}"/>
          <#else>
            <@input name="eml.associatedParties[${item_index}].userIds[0].identifier" help="i18n" i18nkey="eml.contact.identifier" value=""/>
          </#if>
        </div>
        <div class="halfcolumn">
          <@select name="eml.associatedParties[${item_index}].role" i18nkey="eml.associatedParties.role" help="i18n" value="${eml.associatedParties[item_index].role!}" options=roles />
        </div>
		  	</div>
		</#list>
	</div>
	<div class="addNew"><a id="plus-associatedParty" href="">${addLink?lower_case?cap_first}</a></div>
	<div id='buttons' class="buttons">
	   	<@s.submit cssClass="button" name="save" key="button.save"/>
		<@s.submit cssClass="button" name="cancel" key="button.cancel"/>
	</div>
	<!-- internal parameter -->
	<input name="r" type="hidden" value="${resource.shortname}" />
</form>
</div>

<div id="baseItem-associatedParty" class="item clearfix" style="display:none;">
	<div class="columnLinks">
		<div class="halfcolumn">
  			<a id="associatedParty-copyDetails" href="">[ ${copyLink}  ]</a>
    	</div>
    	<div class="halfcolumn">
      		<a id="associatedParty-removeLink" class="removeAssociatedPartyLink" href="">[ ${removeLink?lower_case?cap_first} ]</a>
    	</div>
    </div>
	<div class="halfcolumn">
  		<@input name="firstName" i18nkey="eml.associatedParties.firstName" />
  	</div>
  	<div class="halfcolumn">
  		<@input name="lastName" i18nkey="eml.associatedParties.lastName" requiredField=true />
  	</div>
  	<div class="halfcolumn">
  		<@input name="position" i18nkey="eml.associatedParties.position" requiredField=true />
  	</div>
  	<div class="halfcolumn">
  		<@input name="organisation" i18nkey="eml.associatedParties.organisation" requiredField=true />
  	</div>
  	<div class="halfcolumn">
  		<@input name="address" i18nkey="eml.associatedParties.address.address" />
  	</div>
  	<div class="halfcolumn">
  		<@input name="city" i18nkey="eml.associatedParties.address.city" />
  	</div>
  	<div class="halfcolumn">
  		<@input name="province" i18nkey="eml.associatedParties.address.province" />
  	</div>
  	<div class="halfcolumn countryList">
  		<@select name="country" options=countries help="i18n" i18nkey="eml.associatedParties.address.country" />
  	</div>
	<div class="halfcolumn">
  		<@input name="postalCode" i18nkey="eml.associatedParties.address.postalCode" />
  	</div>
  	<div class="halfcolumn">
  		<@input name="phone" i18nkey="eml.associatedParties.phone" />
  	</div>
  	<div class="halfcolumn">
  		<@input name="email" i18nkey="eml.associatedParties.email" />
  	</div>
	  <div class="halfcolumn">
  		<@input name="homepage" i18nkey="eml.associatedParties.homepage" />
  	</div>
    <div class="halfcolumn">
      <@select name="directory" options=userIdDirectories help="i18n" i18nkey="eml.contact.directory" />
    </div>
    <div class="halfcolumn">
      <@input name="identifier" help="i18n" i18nkey="eml.contact.identifier" />
    </div>
    <div class="halfcolumn">
      <@select name="role" i18nkey="eml.associatedParties.role" help="i18n" options=roles />
    </div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
