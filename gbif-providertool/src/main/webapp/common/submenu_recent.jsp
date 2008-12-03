<%@ include file="/common/taglibs.jsp"%>
<div id="recentlyViewedResources">
	<label>Recent Resources</label>
	<ul class="plain">
		<s:iterator value="#session.recentResources" status="resstatus">
			<s:url id="recentLink" action="resource" includeParams="none">
				<s:param name="resource_id" value="value" />
			</s:url>
			<li><s:a href="%{recentLink}"><s:property value="label"/></s:a></li>
		</s:iterator>
		<li><a href="/manage/resources.html?resourceType=occ">All Occurrence Resources</a></li>
		<li><a href="/manage/resources.html?resourceType=tax">All Checklist Resources</a></li>
		<li><a href="/manage/resources.html?resourceType=meta">All Resources</a></li>
	</ul>
</div>
