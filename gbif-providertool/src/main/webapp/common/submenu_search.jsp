<%@ include file="/common/taglibs.jsp"%>
<div id="fullsearch">
<div class="horizontal_dotted_line_small"></div>
	<label>Fulltext Search</label>
	<s:form action="%{resourceType}Search" namespace="/" name="search" theme="simple" method="get">
		<s:hidden name="resource_id" value="%{resource_id}" />
		<s:textfield name="q" theme="simple"/>
	</s:form>
</div>