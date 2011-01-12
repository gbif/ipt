<%@ include file="/common/taglibs.jsp"%>
<div id="actions">
	<label><s:text name="new.resourceactions"/></label>
	<ul class="plain">							
		<li><a href="<s:url value='/manage/resource.html?resourceType=occ'/>"><s:text name="new.resource.occurrence"/></a></li>
		<li><a href="<s:url value='/manage/resource.html?resourceType=tax'/>"><s:text name="new.resource.checklist"/></a></li>
		<li><a href="<s:url value='/manage/resource.html?resourceType=meta'/>"><s:text name="new.resource.metadata"/></a></li>
	</ul>
</div>
