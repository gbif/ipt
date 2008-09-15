<%@ include file="/common/taglibs.jsp"%>

<s:url id="occResourceStatsUrl" action="occResourceStatsBy%{action}" namespace="/" escapeAmp="false" includeParams="get"/>
<s:a href="%{occResourceStatsUrl}" >
	<img width="<s:property value="%{width}"/>" height="<s:property value="%{height}"/>" src="<s:property value="%{chartUrl}"/>"/>
</s:a>
	
