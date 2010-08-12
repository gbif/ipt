<#-- 
	something doesnt work right when calling macros inside macro definitions
	thats why we use this include instead to keep the help code in one place
 -->
<#if help?has_content>
<img class="infoImg" src="${baseURL}/images/info.gif" />
<div class="info">
	<#if help=="i18n"><@s.text name="${keyBase}${name}.help"/><#else><#if help?has_content>${help}</#if></#if>
	<#if (helpOptions?exists && helpOptions?size>0)>
	<p>Options:</p>
	<ol>
	  <#list helpOptions?keys as val>
	    <li><a href="#" val="${val}">${helpOptions[val]}</a></li>
	  </#list>
	</ol>
	</#if>
</div>
<#else>
<img style="visibility:hidden" src="${baseURL}/images/info.gif" />
</#if>
