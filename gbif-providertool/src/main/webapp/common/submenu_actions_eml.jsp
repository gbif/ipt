<%@ include file="/common/taglibs.jsp"%>
<div id="actions">
	<label>Editing Stages</label>
	<ul class="plain">								
		<li><a href="<s:url action="creatorForm"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="eml.resourceCreator"/> </a></li>
		<li><a href="<s:url action="geocoverageForm"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="eml.geographicCoverage"/> </a></li>
		<li><a href="<s:url action="taxcoverageForm"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="eml.taxonomicCoverage"/> </a></li>
		<li><a href="<s:url action="tempcoverageForm"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="eml.temporalCoverage"/> </a></li>
		<li><a href="<s:url action="rightsForm"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="eml.intellectualRights"/> </a></li>
		<li><a href="<s:url action="projectForm"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="eml.researchProject"/> </a></li>
		<li><a href="<s:url action="methodsForm"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="eml.methods"/> </a></li>
	</ul>
</div>
