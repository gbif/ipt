<head>
    <title>EML - <@s.text name="eml.temporalCoverage"/></title>
    <meta name="resource" content="${eml.title!}"/>
    <meta name="menu" content="ManagerMenu"/>
    <meta name="submenu" content="manage_resource"/>
	<meta name="heading" content="<@s.text name='eml.temporalCoverage'/>"/>        
    
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

<div class="break10"></div>
<@s.form id="emlForm" action="tempcoverage" method="post" validate="false">
<fieldset>
	<@s.hidden name="resourceId" value="${resourceId?c}"/>
	<@s.hidden name="nextPage" value="rights"/>

	<div>
		<div class="leftMedium">
			<@s.textfield id="beginDate" key="eml.temporalCoverage.start" required="true" cssClass="text medium" />
		</div>
		<div class="leftMedium" id="endDateDiv">
			<@s.textfield id="endDate" key="eml.temporalCoverage.end" required="true" cssClass="text medium" />
		</div>
		<div class="left">
			<@s.checkbox key="eml.temporalCoverage.single" value="false" onclick="javascript:toggleSingleDate(this);" />
		</div>
		<div class="left">
			<span>e.g. 1999/07/21</span>
		</div>
	</div>
</fieldset>
<div class="breakRightButtons">
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
</div>

<h1 class="modifiedh1Secondary"><@s.text name="eml.keywords"/></h1>
<div class="horizontal_dotted_line_large_foo"></div>
<fieldset style="padding-top:13px;">
	<p>Keywords separated by comma</p>
	<@s.textarea key="keywords" label="" required="false" cssClass="text xlarge"/>
</fieldset>

	<div class="breakRightButtons">
    <@s.submit cssClass="button" key="button.cancel" method="cancel" theme="simple"/>
    <@s.submit cssClass="button" key="button.save" name="next" theme="simple"/>
	</div>    
</@s.form>
