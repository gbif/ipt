<%@ include file="/common/taglibs.jsp"%>

<s:url id="resourceStatsUrl" action="%{resourceClass}ResourceStatsBy%{action}" namespace="/" escapeAmp="false">
    <s:param name="resource_id" value="%{resource_id}" />
    <s:param name="filter" value="%{id}" />
   	<s:param name="type" value="%{type}"/>
</s:url>

<s:a href="%{resourceStatsUrl}" >
	<img width="<s:property value="%{width}"/>" height="<s:property value="%{height}"/>" src="<s:property value="%{chartUrl}"/>"/>
</s:a>
	
