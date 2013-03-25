<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="<@s.property value="resource.title"/>"/>
	<meta name="heading" content="<@s.text name='trans.heading'/>"/>       
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>    
<script>
function sorry(){
	alert('Sorry, you cannot add arbitrary transformations for now. Please add term translations during the property mapping step.');
	return false;
};
</script>	

</head>

<p class="explMt">
Transformations will allow you to adjust your data according to certain patterns found frequently in data structures before it gets imported into the IPT cache.
Each transformation results in a new view on your source data that can be used later on to define the final mappings to the IPT data model.<br/>
Common data formats are preconfigured, so if you happen to have your data in these formats select the matching preconfiguration.
Otherwise you can always configure you own set of transformations. 
</p>

<h2>Your Transformation Views</h2>
<div class="horizontal_dotted_line_large_soft_nm"></div>
<fieldset>
	<#list transformations as t>
	<div class="newline">
	  <@s.form action="transformations" method="post">
	    <@s.hidden name="resource_id" value="${resource_id}"/>
	    <@s.hidden name="tid" value="${t.id}"/>
		<div class="left">
			<strong>${t.type!}</strong>
			<span>based on column <i>${t.column!}</i> from source <i>${(t.source.name)!"?"}</i></span>
		</div>
		<div class="right">
			<div class="left">
				<@s.submit cssClass="button right" key="button.delete" method="delete" onclick="return confirmDelete('transformation')" />
			</div>
			<div class="left">
				<@s.submit cssClass="button right" key="button.edit" action="terMapping"/>
			</div>
		</div>
	  </@s.form>
	</div>
	</#list>
	
	<div class="break">
	  <@s.form action="transformations" method="post">
	  <div>
		<div class="left">
		 	<@s.select id="transformationType" key="transformations.types" required="false" emptyOption="false" list="transformationTypes"/>
		</div>
		<div class="right">
			<li class="wwgrp">
				<div class="wwlbl">&nbsp;</div>
				<@s.submit cssClass="button right" key="button.add" onclick="return sorry()"/>
			</li>
		</div>
	  </div>
	  <div>
	 	<img id="transformationTypeImage" src="<@s.url value="/images/transformations/2.png"/>" width="400" height="75" alt="links to help page"/>
	  </div>		
	  </@s.form>
	</div>
</fieldset>

<script>
function updateImage(){
	var url = '<@s.url value="/images/transformations/"/>'+$F("transformationType")+'.png';
	$('transformationTypeImage').src = url;
};
$('transformationType').observe('change', updateImage);
</script>	



<#if (transformations?size<1)>
<h2>Common formats</h2>
<div class="horizontal_dotted_line_large_soft_nm"></div>
<fieldset>
	<p>Only visible if no transformation has yet been configured...</p>
	<p>If your data matches one of the following formats, select the mathing one to load a set of preconfigured transformations.
		You will need to configure each of them in the next steps.
	</p>
	<div class="break2">
		<h2 class="modifiedh2">Denormalised with explicit hierarchy</h2>
		<p>Denormalised checklist with explicit taxonomic hierarchy in several columns</p>
		<img class="transformationImage" src="<@s.url value='/images/transformations/denorm_explicit.png'/>"/>
	</div>
	<div class="minibreak">
		<h2 class="modifiedh2">Denormalised with implied hierarchy</h2>
		<p>Denormalised checklist with implied taxonomic hierarchy hidden in sequence of records</p>
		<img class="transformationImage" src="<@s.url value='/images/transformations/denorm_implied.png'/>"/>
	</div>
	<div class="break">
		<h2 class="modifiedh2">...</h2>
	</div>
</fieldset>
</#if>

<div class="break">
<@s.form action="mappings" method="get">
	<@s.hidden key="resource_id"/>
	<div class="breakRight">  
		<@s.submit cssClass="button" key="button.next" theme="simple"/>
	</div>
</@s.form>
</div>
