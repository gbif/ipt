<%@ include file="/common/taglibs.jsp"%>

<s:url id="occResourceStatsUrl" action="occResourceStatsBy%{action}" namespace="/" escapeAmp="false"/>
<s:a href="%{occResourceStatsUrl}" >
	<img src="<s:property value="chartUrl" escape="false"/>" />
</s:a>
	
