<%@ include file="/common/taglibs.jsp"%>
<%@ include file="/common/taglibs.jsp"%>
<header>
<title><s:text name="eml.dataset"/> - <s:text name="eml.dataset.creator"/></title>
</header>

<h1><s:text name="eml.dataset"/> - <s:text name="eml.dataset.creator"/></h1>

<s:form id="editDsitributionForm" action="initCreator" method="post" validate="false">

<s:set name="eml" value="eml" scope="request"/>

<fieldset id="distribution.online" class="metadata">
<legend><s:text name="eml.dataset.distribution.online"/></legend>
<input type="hidden" name="eml.dataset.distribution.scope" value="document"/>
<table>
<tr>
	<td><input type="radio" name="" checked="checked"/></td>
	<td><s:textfield key="eml.dataset.distribution.online.url" required="true" cssClass="text medium"/></td>
</tr>
<tr>
    <td><input type="radio" name=""</td>
    <td>
		<s:textfield key="eml.dataset.distribution.connection.connectionDefinition.schemeName" required="true" cssClass="text medium"/>
		<s:textfield key="eml.dataset.distribution.connection.connectionDefinition.description" required="true" cssClass="text medium"/>
    </td>
</tr>
</table>
</fieldset>
</s:form>