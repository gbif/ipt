<head>
    <title>EML - <@s.text name="eml.temporalCoverage"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="submenu" content="eml"/>
    
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

<@s.form id="emlForm" action="tempcoverage" method="post" validate="false">
<fieldset>
	<legend><@s.text name="eml.temporalCoverage"/></legend>
	<@s.hidden name="resource_id" value="${resource_id?c}"/>
	<@s.hidden name="backPage" value="taxcoverage"/>
	<@s.hidden name="nextPage" value="rights"/>

	<div>
		<div class="left">
			<@s.textfield id="beginDate" key="eml.temporalCoverage.start" required="true" cssClass="text medium" />
		</div>
		<div class="left" id="endDateDiv">
			<@s.textfield id="endDate" key="eml.temporalCoverage.end" required="true" cssClass="text medium" />
		</div>
		<div class="left">
			<@s.checkbox key="eml.temporalCoverage.single" value="false" onclick="javascript:toggleSingleDate(this);" />
		</div>
	</div>
</fieldset>

<fieldset>
	<legend><@s.text name="eml.keywords"/></legend>
	<@s.textarea key="keywords" label="" required="false" cssClass="text xlarge"/>
</fieldset>

	<div class="break" />
    <@s.submit cssClass="button" key="button.back" method="back" theme="simple"/>
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.next" name="next" theme="simple"/>
</@s.form>
