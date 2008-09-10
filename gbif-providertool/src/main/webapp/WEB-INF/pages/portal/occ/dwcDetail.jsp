<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><s:text name="taxon.title"/></title>
    <meta name="resource" content="<s:property value="occResource.title"/>"/>
    <meta name="submenu" content="resource"/>
</head>

<s:push value="dwc">
<h2><s:property value="taxon.fullname"/></h2>	

<s:form>
	<fieldset>
		<legend><s:text name="dwc.taxonomy"/></legend>
		<div id="taxon">
			<s:label key="taxon.fullname"/>
			<s:label key="taxon.name"/>
			<s:label key="taxon.authorship"/>
			<s:label key="taxon.rank"/>
			<s:label key="taxon.code"/>
			<ul class="minimenu">
				<li>
					<a onclick="Effect.toggle('taxonomy', 'blind', { duration: 0.3 }); return false;">(<s:text name="taxon.taxonomy"/>)</a>
				</li>
			</ul>
			<div id="taxonomy" class="hierarchy" style="display:none">
				<ul>
					<li><a href="">Plantae</a></li>
					<ul>
						<li><a href="">Magnoliophyta</a></li>
						<ul>
							<li><a href="">Asterales</a></li>
							<ul>
								<li><a href="">Asteraceae</a></li>
							</ul>
						</ul>
					</ul>
				</ul>
			</div>
		</div>
	</fieldset>
	

	<fieldset>
		<legend><s:text name="dwc.hosting"/></legend>
		<div id="taxon">
			<s:label key="dwc.institutionCode"/>
			<s:label key="dwc.collectionCode"/>
			<s:label key="dwc.catalogNumber"/>
		</div>
	</fieldset>	



	<fieldset>
		<legend><s:text name="dwc.location"/></legend>
		<div id="taxon">
			<s:label key="dwc.collector"/>
			<s:label key="dwc.earliestDateCollected"/>
			<s:label key="dwc.country"/>
			<s:label key="dwc.region"/>
		</div>
	</fieldset>
	
	<fieldset>
		<legend>Extension A</legend>
		<div id="extension_a">
			<s:label key="dwc.country"/>
			<s:label key="dwc.region"/>
		</div>
	</fieldset>
			
	<fieldset>
		<legend>Extension B</legend>
		<div id="extension_b">
			<s:label key="dwc.country"/>
			<s:label key="dwc.region"/>
		</div>
	</fieldset>		

</s:form>

</s:push>


<br class="clearfix" />


<div id="loc-geoserver" class="stats map">
	<label><s:text name="stats.occPointMap"/></label>
	<img src="<s:property value="geoserverMapUrl"/>" />
</div>


<br />
