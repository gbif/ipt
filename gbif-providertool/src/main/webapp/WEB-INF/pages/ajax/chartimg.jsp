<%@ include file="/common/taglibs.jsp"%>

<s:url id="occResourceStatsUrl" action="occResourceStatsBy%{action}" namespace="/">
</s:url>
<s:a href="%{occResourceStatsUrl}" >
	<img src="<s:property value="chartUrl"/>" />
</s:a>
	
