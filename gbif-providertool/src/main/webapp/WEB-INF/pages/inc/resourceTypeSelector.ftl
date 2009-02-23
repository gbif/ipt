<div class="rightCombo">
<@s.form id="resourceForm" method="get">
	<@s.select id="resourceType" name="resourceType" value="resourceType" list="resourceTypes" emptyOption="false" style="display: inline" theme="simple"/>
</@s.form>
</div>
<div class="break"></div>

<script>
$(document).ready(function(){
	$("#resourceType").change(function(e) {
		$("#resourceForm").submit();
	});
});
</script>	

<br class="break"/>