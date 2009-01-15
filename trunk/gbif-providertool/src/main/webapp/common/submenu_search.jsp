<%@ include file="/common/taglibs.jsp"%>
<div id="fullsearch">
	<label>Fulltext Search</label>
	<s:form action="occSearch" namespace="/" name="search" theme="simple" >
		<s:param name="resource_id" value="resource_id" />
		<s:textfield name="q" theme="simple"/>
	</s:form>
</div>