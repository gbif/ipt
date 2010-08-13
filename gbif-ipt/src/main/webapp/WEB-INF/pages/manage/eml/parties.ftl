<#include "/WEB-INF/pages/inc/header.ftl">
	<title><@s.text name='manage.metadata.parties.title'/></title>
	<script type="text/javascript" src="${baseURL}/js/jconfirmaction.jquery.js"></script>
	<style>
	.2col-lft, .2col-rgt{
		width: 45%;
	}
	.right{
	float: right;
	text-align: right;
	padding-left: 15px;
	padding-bottom: 15px;
	}
	</style>

<script type="text/javascript">
$(document).ready(function(){
	$('.confirm').jConfirmAction({question : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>"});
});
</script>

<script type="text/javascript" charset="utf-8">
	$(function() {
		var nparties= $("#parties div:last-child").attr("id");
		var partiesCount = 10;
		// alert(nparties);
		$("#add").click(function() {
			partiesCount++;
				var newParty=$('#party-0').clone();
				newParty.attr("id","party-"+partiesCount);
				//(".removeLink").attr("id", "removeLink-"+partiesCount);
				newParty.appendTo('#parties').show('slow');
		});
		
		$(".removeLink").click(function(event) {
			var $target = $(event.target);
			var index=$target.attr("id").split("-")[1];
			$('#party-'+index).slideUp('slow', function() {});
  			// TODO reorder parties indexes after remove
			// $('#party0').remove();
		});
		
	});
</script>

	<#assign sideMenuEml=true />
<#include "/WEB-INF/pages/inc/menu.ftl">

<h1><@s.text name='manage.metadata.parties.title'/>: <em>${ms.resource.title!ms.resource.shortname}</em></h1>

<p><@s.text name='manage.metadata.parties.intro'/></p>


<#include "/WEB-INF/pages/macros/forms.ftl"/>
<form class="topForm" action="metadata-${section}.do" method="post">
<div id=parties>
<#assign next_agent_index=0 />
<#list eml.associatedParties as agent>
	<#assign next_agent_index=agent_index+1>
	<div id="party-${agent_index}">
	<div class="right">
      <a id="removeLink-${agent_index}" class="removeLink" href="" onclick="return false;">[ <@s.text name='metadata.removethis'/> <@s.text name='manage.metadata.parties.item'/> ]</a>
    </div>
    </br>
	<div class="2col">
  		<@input name="eml.associatedParties[${agent_index}].firstName" keyBase="" size=40/>
  		<@input name="eml.associatedParties[${agent_index}].lastName" keyBase="" size=40/>
  	</div>
	<div class="2col">
  		<@input name="eml.associatedParties[${agent_index}].phone" keyBase="" size=40/>
  		<@select name="eml.associatedParties[${agent_index}].role" value="${eml.associatedParties[agent_index].role}" keyBase="" options=roleOptions />
  	</div>
  	</div>
</#list>
</div>
<h2>New Party</h2>
	<div class="2col">
  		<@input name="eml.associatedParties[${next_agent_index}].firstName" value="" size=40/>
  		<@input name="eml.associatedParties[${next_agent_index}].lastName" value="" size=40/>
  	</div>
	<div class="2col">
  		<@input name="eml.associatedParties[${next_agent_index}].phone" value="" size=40/>
  		<@select name="eml.associatedParties[${next_agent_index}].role" value="" options=roleOptions />
  	</div>
  
  <div id='buttons' class="buttons">
  	<div class="right">
      <a id="add" href="" onclick="return false;">[ <@s.text name='metadata.addnew'/> <@s.text name='manage.metadata.parties.item'/> ]</a>
    </div>
    <@s.submit name="save" key="button.save"/>
 	<@s.submit name="cancel" key="button.cancel"/>
  </div>	
</form>

<#include "/WEB-INF/pages/inc/footer.ftl">
