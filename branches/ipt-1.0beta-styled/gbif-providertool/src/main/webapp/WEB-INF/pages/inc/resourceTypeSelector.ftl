<div class="right">
<@s.form id="resourceForm" method="get">
	<@s.select id="resourceType" name="resourceType" value="resourceType" list="resourceTypes" emptyOption="false" style="display: inline" theme="simple"/>
</@s.form>
</div>

<script>
function updateResources(){
	$('resourceForm').submit();
};
$('resourceType').observe('change', updateResources);
</script>	

<br class="break"/>