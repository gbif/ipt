<%@ include file="/common/taglibs.jsp"%>
<div id="actions">
	<label>Resource Configuration</label>
	<ul class="plain">								
		<li><a href="<s:url action="sources" namespace="/manage"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="dataResource.sources"/> </a></li>
		<li><a href="<s:url action="transformations" namespace="/manage"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="dataResource.transformations"/> </a></li>
		<li><a href="<s:url action="mappings" namespace="/manage"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="dataResource.mappings"/> </a></li>
		<li><a href="<s:url action="cache" namespace="/manage"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="dataResource.cache"/> </a></li>
		<li><a href="<s:url action="validation" namespace="/manage"><s:param name="resource_id" value="%{resource_id}" /></s:url>"></a><s:text name="dataResource.validation"/></li>
	</ul>
</div>