<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.metadata.parties.title'/></title>
	<script type="text/javascript" src="${baseURL}/js/jconfirmaction.jquery.js"></script>
<script type="text/javascript">
$(document).ready(function(){
	$('.confirm').jConfirmAction({question : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});	

	var	partiesCount=-1;
	var lastParty = $("#parties .party:last-child").attr("id");
	if(lastParty != undefined){
		var partiesCount=parseInt(lastParty.split("-")[1]);
	}
	
	$("#add").click(function(event) {
		partiesCount++;
		event.preventDefault();
		// to add more parties, clone the first one and change it's attributes
		var newParty=$('#baseParty').clone();
		newParty.hide();
		newParty.attr("id","party-"+partiesCount);
		newParty.appendTo('#parties').slideDown('slow');
		
		$("#party-"+partiesCount+" input").attr("id",function() {return "eml.associatedParties["+partiesCount+"]."+ $(this).attr("id"); });
		$("#party-"+partiesCount+" select").attr("id",function() {return "eml.associatedParties["+partiesCount+"]."+ $(this).attr("id"); });
		$("#party-"+partiesCount+" label").attr("for",function() {return "eml.associatedParties["+partiesCount+"]."+ $(this).attr("id"); });
		
		$("#party-"+partiesCount+" input").attr("name",function() {return $(this).attr("id"); });
		$("#party-"+partiesCount+" select").attr("name",function() {return $(this).attr("id"); });
		
		//$("#party-"+partiesCount+" input").attr("value","");
		//$("#party-"+partiesCount+" select").attr("value","");
		
		$("#party-"+partiesCount+" .removeLink").attr("id", "removeLink-"+partiesCount);
		$("#removeLink-"+partiesCount).click(function(event) {
			removeParty(event);
		});
		
	});
		
	$(".removeLink").click(function(event) {
		removeParty(event);
	});
		
	function removeParty(event){
		event.preventDefault();
		var $target = $(event.target);
		var index=$target.attr("id").split("-")[1];
		$('#party-'+index).slideUp('slow', function() { $(this).remove(); } );
	}
		
});
</script>


	<#assign sideMenuEml=true />
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name='manage.metadata.parties.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>

<p><@s.text name='manage.metadata.parties.intro'/></p>


<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm" action="metadata-${section}.do" method="post">
<div id="parties">
<#assign next_agent_index=0 />
<#list eml.associatedParties as agent>
	<#assign next_agent_index=agent_index+1>
	<div id="party-${agent_index}" class="party">
	<div class="right">
      <a id="removeLink-${agent_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.parties.item'/> ]</a>
    </div>
    </br>
	<div class="2col">
  		<@input name="eml.associatedParties[${agent_index}].firstName" i18nkey="eml.associatedParties.firstName" size=40/>
  		<@input name="eml.associatedParties[${agent_index}].lastName" i18nkey="eml.associatedParties.lastName" size=40/>
  	</div>
	<div class="2col">
  		<@input name="eml.associatedParties[${agent_index}].phone" i18nkey="eml.associatedParties.phone" size=40/>
  		<@select name="eml.associatedParties[${agent_index}].role" i18nkey="eml.associatedParties.role" value="${eml.associatedParties[agent_index].role}" options=roleOptions />
  	</div>
  	</div>
</#list>
</div>
<div id='buttons' class="buttons">
  	<a id="add" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.parties.item'/></a></br></br>
    <@s.submit name="save" key="button.save"/>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>	
</form>
<div id="baseParty" class="party" style="display:none;">
	<div class="right">
      <a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.parties.item'/> ]</a>
    </div>
    </br>
	<div class="2col">
  		<@input name="firstName" i18nkey="eml.associatedParties.firstName" size=40/>
  		<@input name="lastName" i18nkey="eml.associatedParties.lastName"  size=40/>
  	</div>
	<div class="2col">
  		<@input name="phone" i18nkey="eml.associatedParties.phone" size=40/>
  		<@select name="role" i18nkey="eml.associatedParties.role" value="" options=roleOptions />
  	</div>
</div>

<#include "/WEB-INF/pages/inc/footer.ftl">
