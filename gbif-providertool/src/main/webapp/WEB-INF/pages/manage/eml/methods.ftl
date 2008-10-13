<@s.form id="emlForm" action="dataset" method="get" validate="false">
<fieldset>
	<legend><@s.text name="eml.methods"/></legend>
	<@s.hidden name="resource_id" value="${resource_id}"/>
	<@s.hidden name="backPage" value="project"/>

	<@s.textfield key="eml.title" required="true" cssClass="text xlarge"/>
	<@s.textarea key="eml.abstract" required="true" cssClass="text xlarge"/>
	
	<@s.label key="eml.title" cssClass="text xlarge"/>
	<@s.label key="eml.abstract" cssClass="text xlarge"/>
	<li id="wwgrp_editDatasetForm_eml_dataset_language" class="wwgrp"> 
		<@s.select key="eml.language" list="isoLanguageI18nCodeMap" cssClass="text medium" value="defaultDatasetLanguage"/>
	</li>
	<@s.label key="eml.pubDate" cssClass="text medium"/>
	
    <@s.submit cssClass="button" key="button.back"   method="back"/>
    <@s.submit cssClass="button" key="button.cancel" method="cancel" />
    <@s.submit cssClass="button" key="button.done" method="save" theme="simple"/>
</fieldset>
</@s.form>