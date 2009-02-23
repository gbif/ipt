<%@ include file="/common/taglibs.jsp"%>
<div id="fullsearch">
	<label>Fulltext Search</label>
	<s:form action="%{resourceType}Search" namespace="/" name="search" theme="simple" method="get">
		<s:hidden name="resource_id" value="%{resource_id}" />
		<s:textfield name="q" theme="simple"/>
	</s:form>
</div>