<%@ include file="/common/taglibs.jsp"%>
<div id="actions"><label><s:text name="eml.submenu.title"/></label>
<ul class="plain">
  <li><a
    href="<s:url action="resource" namespace="/manage"><s:param name="resourceId" value="%{resourceId}" /></s:url>"
  > <s:text name="metadata.heading.basic" /> </a></li>
  <li><a
    href="<s:url action="organisationForm" namespace="/manage/meta"><s:param name="resourceId" value="%{resourceId}" /></s:url>"
  > <s:text name="metadata.heading.organisation" /> </a></li>
  <li><a
    href="<s:url action="AssociatedPartiesForm" namespace="/manage/meta"><s:param name="resourceId" value="%{resourceId}" /></s:url>"
  > <s:text name="metadata.heading.associatedParties" /> </a></li>
  <li><a
    href="<s:url action="geocoverageForm" namespace="/manage/meta"><s:param name="resourceId" value="%{resourceId}" /></s:url>"
  > <s:text name="metadata.heading.geocoverages" /> </a></li>
  <li><a
    href="<s:url action="taxcoverageForm" namespace="/manage/meta"><s:param name="resourceId" value="%{resourceId}" /></s:url>"
  > <s:text name="metadata.heading.taxcoverages" /> </a></li>
  <li><a
    href="<s:url action="tempcoverageForm" namespace="/manage/meta"><s:param name="resourceId" value="%{resourceId}" /></s:url>"
  > <s:text name="metadata.heading.tempcoverages" /> </a></li>
  <li><a
    href="<s:url action="projectForm" namespace="/manage/meta"><s:param name="resourceId" value="%{resourceId}" /></s:url>"
  > <s:text name="metadata.heading.projects" /> </a></li>
  <li><a
    href="<s:url action="methodsForm" namespace="/manage/meta"><s:param name="resourceId" value="%{resourceId}" /></s:url>"
  > <s:text name="metadata.heading.methods" /> </a></li>
  <li><a
    href="<s:url action="citationsForm" namespace="/manage/meta"><s:param name="resourceId" value="%{resourceId}" /></s:url>"
  > <s:text name="metadata.heading.citations" /> </a></li>
  <li><a
    href="<s:url action="physicalDataForm" namespace="/manage/meta"><s:param name="resourceId" value="%{resourceId}" /></s:url>"
  > <s:text name="metadata.heading.physicalData" /> </a></li>
  <li><a
    href="<s:url action="rightsForm" namespace="/manage/meta"><s:param name="resourceId" value="%{resourceId}" /></s:url>"
  > <s:text name="metadata.heading.additionalMetadata" /> </a></li>
</ul>
</div>
