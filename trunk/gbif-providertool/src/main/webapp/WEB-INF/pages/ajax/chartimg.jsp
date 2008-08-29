<%@ include file="/common/taglibs.jsp"%>

<s:url id="occResourceStatsUrl" action="occResourceStatsBy%{action}" namespace="/" escapeAmp="false" includeParams="get"/>
<s:a href="%{occResourceStatsUrl}" >
	<img src="<s:property value="chartUrl" escape="false"/>" />
</s:a>
	
