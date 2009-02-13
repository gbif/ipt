<head>
    <title>EML - <@s.text name="eml.temporalCoverage"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="submenu" content="manage_resource"/>
    
<script type="text/javascript">
	function toggleSingleDate(checkbox){
		Effect.toggle('endDateDiv', 'appear', { duration: 0.3 });
				
		if(checkbox.checked){
			// $('beginDate').focus();
			$('endDate').value="";
		}
	}
</script>

</head>

<h1 class="modifiedh1"><@s.text name="eml.temporalCoverage"/></h1>
<@s.form id="emlForm" action="tempcoverage" method="post" validate="false">
<fieldset>
	<legend><!--<@s.text name="eml.temporalCoverage"/>--></legend>
	<@s.hidden name="resource_id" value="${resource_id?c}"/>
	<@s.hidden name="nextPage" value="rights"/>

	<div>
		<div class="left">
			<@s.textfield id="beginDate" key="eml.temporalCoverage.start" required="true" cssClass="text medium" />
		</div>
		<div class="left" id="endDateDiv">
			<@s.textfield id="endDate" key="eml.temporalCoverage.end" required="true" cssClass="text medium" />
		</div>
		<div class="left" style="widht: 100px">
			<@s.checkbox key="eml.temporalCoverage.single" value="false" onclick="javascript:toggleSingleDate(this);" />
		</div>
	</div>
</fieldset>

<h1 class="modifiedh1Secondary"><@s.text name="eml.keywords"/></h1>
<fieldset>
	<legend><!--<@s.text name="eml.keywords"/>--></legend>
	<@s.textarea key="keywords" label="" required="false" cssClass="text xlarge"/>
</fieldset>

	<div class="break"></div>
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
</@s.form>
