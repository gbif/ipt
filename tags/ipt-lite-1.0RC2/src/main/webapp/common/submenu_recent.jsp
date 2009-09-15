<%@ include file="/common/taglibs.jsp"%>
<div id="recentlyViewedResources">
	<s:if test="#session.recentResources">
	<label class="submenulabel">Recent Resources</label>
	<ul class="plain">
		<s:iterator value="#session.recentResources" status="resstatus">
			<s:url id="recentLink" action="resource" includeParams="none">
				<s:param name="resource_id" value="value" />
			</s:url>
			<li><s:a href="%{recentLink}"><s:property value="label"/></s:a></li>
		</s:iterator>
	</ul>
	</s:if>
</div>
