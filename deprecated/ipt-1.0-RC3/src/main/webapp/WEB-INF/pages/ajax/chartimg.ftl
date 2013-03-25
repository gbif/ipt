<@s.url id="ResourceStatsUrl" action="%{resourceClass}ResourceStatsBy%{statsBy}" namespace="/" includeParams="none">
	<@s.param name="resourceId" value="%{resourceId}"/>
	<@s.param name="filter" value="%{id}"/>
	<@s.param name="type" value="%{type}"/>
</@s.url>
<a href="${ResourceStatsUrl}" >
	<img width="${width}" height="${height}" src="${chartUrl}"/>
</a>
	
