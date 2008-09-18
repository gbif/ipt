<%@ include file="/common/taglibs.jsp"%>

<s:url id="occResourceStatsUrl" action="occResourceStatsBy%{action}" namespace="/" escapeAmp="false">
    <s:param name="resource_id" value="%{resource_id}" />
    <s:param name="filter" value="%{id}" />
   	<s:param name="foo" value="bar"/>
</s:url>

<s:a href="%{occResourceStatsUrl}" >
	<img width="<s:property value="%{width}"/>" height="<s:property value="%{height}"/>" src="<s:property value="%{chartUrl}"/>"/>
</s:a>
	
