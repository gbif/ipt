<head>
    <title><@s.text name="manage.transformations.title"/></title>
    <meta name="resource" content="<@s.property value="resource.title"/>"/>
	<meta name="heading" content="<@s.text name='manage.transformations.heading'/>"/>       
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>    
<script>
function sorry(){
	alert('<@s.text name='manage.transformations.alert'/>');
	return false;
};
</script>	

</head>

<p class="explMt">
<@s.text name='manage.transformations.explanation'/>
</p>

<h2><@s.text name='manage.transformations.heading'/></h2>
<div class="horizontal_dotted_line_large_soft_nm"></div>
<fieldset>
	<#list transformations as t>
	<div class="newline">
	  <@s.form action="transformations" method="post">
	    <@s.hidden name="resourceId" value="${resourceId}"/>
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
<h2><@s.text name='manage.transformations.common'/></h2>
<div class="horizontal_dotted_line_large_soft_nm"></div>
<fieldset>
	<p>
	<@s.text name='manage.transformations.onlyvisible'/>
	</p>
	<p>
	<@s.text name='manage.transformations.ifmatches'/>
	</p>
	<div class="break2">
		<h2 class="modifiedh2"><@s.text name='manage.transformations.denormalisedexplicit'/></h2>
		<p><@s.text name='manage.transformations.denormalisedexplicit.explanation'/></p>
		<img class="transformationImage" src="<@s.url value='/images/transformations/denorm_explicit.png'/>"/>
	</div>
	<div class="minibreak">
		<h2 class="modifiedh2"><@s.text name='manage.transformations.denormalisedimplied'/></h2>
		<p><@s.text name='manage.transformations.denormalisedimplied.explanation'/></p>
		<img class="transformationImage" src="<@s.url value='/images/transformations/denorm_implied.png'/>"/>
	</div>
	<div class="break">
		<h2 class="modifiedh2">...</h2>
	</div>
</fieldset>
</#if>

<div class="break">
<@s.form action="mappings" method="get">
	<@s.hidden key="resourceId"/>
	<div class="breakRight">  
		<@s.submit cssClass="button" key="button.next" theme="simple"/>
	</div>
</@s.form>
</div>