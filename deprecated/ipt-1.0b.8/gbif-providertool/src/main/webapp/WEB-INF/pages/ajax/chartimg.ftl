<@s.url id="ResourceStatsUrl" action="%{resourceClass}ResourceStatsBy%{statsBy}" namespace="/" includeParams="none">
	<@s.param name="resource_id" value="%{resource_id}"/>
	<@s.param name="filter" value="%{id}"/>
	<@s.param name="type" value="%{type}"/>
</@s.url>
<a href="${ResourceStatsUrl}" >
	<img width="${width}" height="${height}" src="${chartUrl}"/>
</a>
	
