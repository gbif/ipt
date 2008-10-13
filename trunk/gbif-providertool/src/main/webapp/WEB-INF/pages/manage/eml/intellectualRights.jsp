<%@ include file="/common/taglibs.jsp"%>
<header>
<title><s:text name="eml.dataset"/> - <s:text name="eml.dataset.eml.dataset.intellectualRights"/></title>
</header>

<h1><s:text name="eml.dataset"/> - <s:text name="eml.dataset.eml.dataset.intellectualRights"/></h1>

<s:form id="editIntellectualRightsForm" action="initIntellectualRights" method="post" validate="false">

<s:set name="eml" value="eml" scope="request"/>

<fieldset id="intellectualRights" class="metadata">
<legend><s:text name="eml.dataset.intellectualRights"/></legend>

<s:textarea key="eml.dataset.intellectualRights.para" required="true" cssClass="text large"/>

</fieldset>

<li class="buttonBar bottom">
<s:submit cssClass="button" key="button.cancel" theme="simple" method="cancel"/>
<s:submit cssClass="button" key="button.back" theme="simple" method="back"/>
<s:submit cssClass="button" key="button.next" theme="simple"/>
</li>

</s:form>