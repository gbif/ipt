<ol class="col four">
  <#list keywords as k>
	<li><a href="<@s.url value='/metaSearch.html?keyword=${k}'/>">${k}</a></li>
  </#list>
</ol>