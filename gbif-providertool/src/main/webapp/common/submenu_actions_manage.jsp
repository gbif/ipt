<%@ include file="/common/taglibs.jsp"%>
<div id="actions">
	<label>Resource Configuration</label>
	<ul class="plain">								
		<li><a href="<s:url action="sources" namespace="/manage"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="sources.heading"/> </a></li>
		<li><a href="<s:url action="mappings" namespace="/manage"><s:param name="resource_id" value="%{resource_id}" /></s:url>"> <s:text name="mappings.heading"/> </a></li>
		<li><a href="<s:url action="validation" namespace="/manage"><s:param name="resource_id" value="%{resource_id}" /></s:url>"></a><s:text name="validation.heading"/></li>
	</ul>
</div>