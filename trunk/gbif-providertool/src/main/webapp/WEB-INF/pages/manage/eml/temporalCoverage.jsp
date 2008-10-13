\<%@ include file="/common/taglibs.jsp"%>
<s:set name="eml" value="eml" scope="request"/>
<script type="text/javascript">
	function toggleSingleDate(checkbox){
		document.getElementById("beginDate").disabled = checkbox.checked;
		document.getElementById("endDate").disabled = checkbox.checked;
		document.getElementById("singleDate").disabled = !checkbox.checked;
		
		if(checkbox.checked){
			document.getElementById("beginDate").value="";
			document.getElementById("endDate").value="";
			document.getElementById("singleDate").focus();
		} else {
			document.getElementById("beginDate").focus();
			document.getElementById("singleDate").value="";
		}
	}
</script>
<fieldset id="temporalCoverage" class="metadata">
	<legend><s:text name="eml.dataset.coverage.temporalCoverage"/></legend>
	<div class="message" id="successMessages"><s:text name="eml.dataset.coverage.temporalCoverage.help"/></div>
	<input type="hidden" name="eml.dataset.coverage.temporalCoverage.scope" value="document"/>
	<s:set name="singleDateInUse" value="%{eml.dataset.coverage.temporalCoverage!=null && eml.dataset.coverage.temporalCoverage.singleDateTime!=null && eml.dataset.coverage.temporalCoverage.singleDateTime.calendarDate!=null}"/>
	<s:textfield id="beginDate" key="eml.dataset.coverage.temporalCoverage.rangeOfDates.beginDate.calendarDate" required="false" cssClass="text medium" disabled="singleDateInUse"/>
	<s:textfield id="endDate" key="eml.dataset.coverage.temporalCoverage.rangeOfDates.endDate.calendarDate" required="false" cssClass="text medium" disabled="singleDateInUse"/>
	<div>
		<input name="singleDateEnabled" type="checkbox" value="true" onclick="javascript:toggleSingleDate(this);"<c:if test="${singleDateInUse}"> checked="true"</c:if>/> Use single date
	</div>
	<s:textfield id="singleDate" key="eml.dataset.coverage.temporalCoverage.singleDateTime.calendarDate" required="false" cssClass="text medium" disabled="!#singleDateInUse"/>
</fieldset>