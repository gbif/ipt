<head>
    <title>EML - <@s.text name="eml.taxonomicCoverage"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="submenu" content="eml"/>
</head>

<@s.form id="emlForm" action="taxcoverage" method="post" validate="false">
<fieldset>
	<legend><@s.text name="eml.taxonomicMetadata"/></legend>
	<@s.hidden name="resource_id" value="${resource_id?c}"/>
	<@s.hidden name="backPage" value="geocoverage"/>
	<@s.hidden name="nextPage" value="tempcoverage"/>

	<@s.textarea key="eml.taxonomicCoverageDescription" cssClass="text xlarge slim"/>
	<@s.textarea key="taxonomicClassification" label="%{getText('eml.taxonomicClassification')}" cssClass="text xlarge slim"/>
</fieldset>

<fieldset>
	<legend><@s.text name="eml.lowestCommonTaxon"/></legend>
	<div>
		<div class="left">
			<@s.textfield key="eml.lowestCommonTaxon.scientificName" label="%{getText('taxonKeyword.scientificName')}" required="true" cssClass="text large" />
		</div>
		<div class="left">
			<@s.textfield key="eml.lowestCommonTaxon.commonName" label="%{getText('taxonKeyword.commonName')}" required="false" cssClass="text medium" />
		</div>
		<div class="left">
			<@s.select key="eml.lowestCommonTaxon.rank" label="%{getText('taxonKeyword.rank')}" list="allRanks" cssClass="text small"/>
		</div>
	</div>
</fieldset>

	<div class="break" />
    <@s.submit cssClass="button" key="button.back" method="back" theme="simple"/>
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.next" name="next" theme="simple"/>
</@s.form>
