<#escape x as x?html>
<#setting number_format="#####.##">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.metadata.parties.title'/></title>
<#include "/WEB-INF/pages/macros/metadata.ftl"/>
<script type="text/javascript">
$(document).ready(function(){
    initHelp();
});
</script>
<#assign sideMenuEml=true />
 <#assign currentMenu="manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<h1><span class="superscript">Resource Title</span>
    <a class="tooltip" href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}
        <span class="classic">Click the title to go to the <em>resource overview</em> page.<div class="arrow-wrap"><div class="arrow"></div></div></span>
    </a>
</h1>
<div class="grid_17 suffix_1">
<h2 class="subTitle"><@s.text name='manage.metadata.parties.title'/></h2>
<form class="topForm" action="metadata-${section}.do" method="post">
    <p><@s.text name='manage.metadata.parties.intro'/></p>
	<div id="items">
		<#list eml.associatedParties as item>
			<div id="item-${item_index}" class="item clearfix">
				<div class="halfcolumn">
  	  				<a id="copyDetails-${item_index}" href="">[ <@s.text name="eml.resourceCreator.copyLink" />  ]</a>
    			</div>
				<div class="halfcolumn">
		      		<a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.parties.item'/> ]</a>
		    	</div>
				<div class="halfcolumn">
		  			<@input name="eml.associatedParties[${item_index}].firstName" i18nkey="eml.associatedParties.firstName"/>
				</div>
			  	<div class="halfcolumn">			  			
		  			<@input name="eml.associatedParties[${item_index}].lastName" i18nkey="eml.associatedParties.lastName"/>
		  		</div>
		  		<div class="halfcolumn">
		  			<@input name="eml.associatedParties[${item_index}].position" i18nkey="eml.associatedParties.position" />
		  		</div>
  				<div class="halfcolumn">	
		  			<@input name="eml.associatedParties[${item_index}].organisation" i18nkey="eml.associatedParties.organisation" />
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
  				<div class="halfcolumn">			  			
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
		  			<@input name="eml.associatedParties[${item_index}].homepage" i18nkey="eml.associatedParties.homepage" />
		  		</div> 
	  				<@select name="eml.associatedParties[${item_index}].role" i18nkey="eml.associatedParties.role" help="i18n" value="${eml.associatedParties[item_index].role!}" options=roles />  		
		  	</div>
		</#list>
	</div>
	<a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.parties.item'/></a>
	<div id='buttons' class="buttons">
	   	<@s.submit cssClass="button" name="save" key="button.save"/>
		<@s.submit cssClass="button" name="cancel" key="button.cancel"/>
	</div>
	<!-- internal parameter -->
	<input name="r" type="hidden" value="${resource.shortname}" />		
</form>
</div>

<div id="baseItem" class="item clearfix" style="display:none;">
	<div class="halfcolumn">
  	  <a id="copyDetails" href="">[ <@s.text name="eml.resourceCreator.copyLink" />  ]</a>
    </div>
    <div class="halfcolumn">
      <a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.parties.item'/> ]</a>
    </div>
	<div class="halfcolumn">
  		<@input name="firstName" i18nkey="eml.associatedParties.firstName" />
  	</div>
  	<div class="halfcolumn">
  		<@input name="lastName" i18nkey="eml.associatedParties.lastName"  />
  	</div>
  	<div class="halfcolumn">
  		<@input name="position" i18nkey="eml.associatedParties.position" />
  	</div>
  	<div class="halfcolumn">  		
  		<@input name="organisation" i18nkey="eml.associatedParties.organisation" />
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
  	<div class="halfcolumn">  		
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
  		<@select name="role" i18nkey="eml.associatedParties.role" help="i18n" options=roles />
  	</div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>