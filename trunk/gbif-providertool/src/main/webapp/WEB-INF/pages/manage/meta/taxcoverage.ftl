<head>
    <title>EML - <@s.text name="eml.taxonomicCoverage"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
</head>

<h1><@s.text name="eml.taxonomicMetadata"/></h1>
<div class="horizontal_dotted_line_large_foo"></div>
<@s.form id="emlForm" action="taxcoverage" method="post" validate="false">
<fieldset>
	<legend><!--<@s.text name="eml.taxonomicMetadata"/>--></legend>
	<@s.hidden name="resource_id" value="${resource_id?c}"/>
	<@s.hidden name="nextPage" value="tempcoverage"/>

	<@s.textarea key="eml.taxonomicCoverageDescription" cssClass="text xlarge slim"/>
	<@s.textarea key="taxonomicClassification" label="%{getText('eml.taxonomicClassification')}" cssClass="text xlarge slim"/>
</fieldset>
<div class="breakRight">
	<@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
	<@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
</div>
<h1 class="modifiedh1Secondary"><@s.text name="eml.lowestCommonTaxon"/></h1>
<div class="horizontal_dotted_line_large_foo"></div>
<fieldset style="padding-top:10px;">
	<legend><!--<@s.text name="eml.lowestCommonTaxon"/>--></legend>
	<div>
		<div class="left">
			<@s.textfield key="eml.lowestCommonTaxon.scientificName" label="%{getText('taxonKeyword.scientificName')}" required="true" cssClass="text large" />
		</div>
		<div class="left">
			<@s.textfield key="eml.lowestCommonTaxon.commonName" label="%{getText('taxonKeyword.commonName')}" required="false" cssClass="text medium" />
		</div>
		<div class="modifiedCombo">
			<@s.select key="eml.lowestCommonTaxon.rank" label="%{getText('taxonKeyword.rank')}" list="allRanks" cssClass="text small"/>
		</div>
	</div>
</fieldset>

	<div class="breakRight">
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
	</div>
</@s.form>
