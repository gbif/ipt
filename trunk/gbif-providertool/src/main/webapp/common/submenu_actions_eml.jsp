<%@ include file="/common/taglibs.jsp"%>
<div id="actions">
	<s:if test="resource.id>0">
	<label>Editing Stages</label>
	<ul class="plain">								
		<li><a href="<s:url action="resource" namespace="/manage"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="eml.basic"/> </a></li>
		<li><a href="<s:url action="creatorForm" namespace="/manage/meta"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="eml.resourceCreator"/> </a></li>
		<li><a href="<s:url action="geocoverageForm" namespace="/manage/meta"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="eml.geographicCoverage"/> </a></li>
		<li><a href="<s:url action="taxcoverageForm" namespace="/manage/meta"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="eml.taxonomicCoverage"/> </a></li>
		<li><a href="<s:url action="tempcoverageForm" namespace="/manage/meta"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="eml.temporalCoverage"/> </a></li>
		<li><a href="<s:url action="rightsForm" namespace="/manage/meta"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="eml.intellectualRights"/> </a></li>
		<li><a href="<s:url action="projectForm" namespace="/manage/meta"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="eml.researchProject"/> </a></li>
		<li><a href="<s:url action="methodsForm" namespace="/manage/meta"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="eml.methods"/> </a></li>
	</ul>
	</s:if>
</div>
