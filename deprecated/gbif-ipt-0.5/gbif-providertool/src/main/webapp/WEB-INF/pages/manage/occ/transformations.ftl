<head>
    <title><@s.text name="occResourceOverview.title"/></title>
    <meta name="resource" content="<@s.property value="resource.title"/>"/>
    <meta name="submenu" content="manage_resource"/>    
</head>

<h1>Missing Feature</h1>

<div class="separator"></div>

<p>
Transformations will allow you to adjust your data according to certain patterns found frequently in data structures before it gets imported into the IPT cache.
Each transformation results in a new view on your source data that can be used later on to define the final mappings to the IPT data model.<br/>
Common data formats are preconfigured, so if you happen to have your data in these formats select the matching preconfiguration.
Otherwise you can always configure you own set of transformations. 
</p>

<fieldset>
<legend>Your Transformation Views</legend>
	<i>(could also be visualised with graphviz)</i>
	<div class="newline">
	  <@s.form action="transformations" method="post">
		<div class="left">
			<strong>1) common names</strong>
			<span>union of 3 columns from <i>specimen</i></span>
		</div>
		<div class="right">
			<div class="left">
				<@s.submit cssClass="button right" key="button.delete" onclick="return confirmDelete('transformation')" />
			</div>
			<div class="left">
				<@s.submit cssClass="button right" key="button.edit" />
			</div>
		</div>
	  </@s.form>
	</div>
	<div class="newline">
	  <@s.form action="transformations" method="post">
		<div class="left">
			<strong>2) taxonomy</strong>
			<span>acceptedID added to <i>taxon</i></span>
		</div>
		<div class="right">
			<div class="left">
				<@s.submit cssClass="button right" key="button.delete" onclick="return confirmDelete('transformation')" />
			</div>
			<div class="left">
				<@s.submit cssClass="button right" key="button.edit" />
			</div>
		</div>
	  </@s.form>
	</div>
	
	<div class="break">
	  <@s.form action="transformations" method="post">
	  <div>
		<div class="left">
		 	<@s.select id="transformationType" key="transformations.types" required="false" emptyOption="false" list="transformationTypes"/>
		</div>
		<div class="right">
			<li class="wwgrp">
				<div class="wwlbl">&nbsp;</div>
				<@s.submit cssClass="button right" key="button.add"/>
			</li>
		</div>
	  </div>
	  <div>
	 	<img id="transformationTypeImage" src="<@s.url value="/images/transformations/hierarchy.png"/>" width="400" height="75" alt="links to help page"/>
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



<fieldset>
<legend>Common formats</legend>
	<p>Only visible if no transformation has yet been configured...</p>
	<p>If your data matches one of the following formats, select the mathing one to load a set of preconfigured transformations.
		You will need to configure each of them in the next steps.
	</p>
	<div class="break">
		<h3>Denormalised with explicit hierarchy</h3>
		<p>Denormalised checklist with explicit taxonomic hierarchy in several columns</p>
		<img src="<@s.url value='/images/transformations/denorm_explicit.png'/>"/>
	</div>
	<div class="break">
		<h3>Denormalised with implied hierarchy</h3>
		<p>Denormalised checklist with implied taxonomic hierarchy hidden in sequence of records</p>
		<img src="<@s.url value='/images/transformations/denorm_implied.png'/>"/>
	</div>
	<div class="break">
		<h3>...</h3>
	</div>
</fieldset>


<div class="break">
<@s.form action="mappings" method="get">
  <@s.hidden key="resource_id"/>
  <@s.submit cssClass="button" key="button.next" theme="simple"/>
</@s.form>
</div>
