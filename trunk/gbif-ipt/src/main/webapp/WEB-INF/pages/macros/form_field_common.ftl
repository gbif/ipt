<label for="${name}"><#if i18nkey==""><@s.text name="${name}"/><#else><@s.text name="${i18nkey}"/></#if></label>
<#if errorfield==""><@s.fielderror cssClass="fielderror" fieldName="${name}"/><#else><@s.fielderror cssClass="fielderror" fieldName="${errorfield}"/></#if>
<#-- 
	something doesnt work right when calling macros inside macro definitions
	thats why we use this include instead to keep the help code in one place
 -->
<#if help?has_content>
<img class="infoImg" src="${baseURL}/images/info.gif" />
<div class="info">
	<#if help=="i18n"><#if i18nkey==""><@s.text name="${name}.help"/><#else><@s.text name="${i18nkey}.help"/></#if><#else><#if help?has_content>${help}</#if></#if>
	<#if (helpOptions?exists && helpOptions?size>0)>
	<p><@s.text name="help.options"/></p>
	<ol>
	  <#list helpOptions?keys as val>
	    <li><a href="#" val="${val}">${helpOptions[val]}</a></li>
	  </#list>
	</ol>
	</#if>
</div>
<#else>
<#-- quick hack, should be done with some css and without an invisible image -->
<img style="visibility:hidden" src="${baseURL}/images/info.gif" />
</#if>
