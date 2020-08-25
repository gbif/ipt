<h1>About this IPT installation</h1>
<#if hostingOrganisation?? && hostingOrganisation.name??>
<p>This is a default IPT hosted by ${hostingOrganisation.name}</p>

<p>You can use the following variables about the hosting organization:</p>
<ul>
  <li>description = ${hostingOrganisation.description!}</li>
  <li>name = ${hostingOrganisation.name!}</li>
  <li>alias = ${hostingOrganisation.alias!}</li>
  <li>homepageURL = ${hostingOrganisation.homepageURL!}</li>
  <li>primaryContactType = ${hostingOrganisation.primaryContactType!}</li>
  <li>primaryContactName = ${hostingOrganisation.primaryContactName!}</li>
  <li>primaryContactDescription = ${hostingOrganisation.primaryContactDescription!}</li>
  <li>primaryContactAddress = ${hostingOrganisation.primaryContactAddress!}</li>
  <li>primaryContactEmail = ${hostingOrganisation.primaryContactEmail!}</li>
  <li>primaryContactPhone = ${hostingOrganisation.primaryContactPhone!}</li>
  <li>nodeKey = ${hostingOrganisation.nodeKey!}</li>
  <li>nodeName = ${hostingOrganisation.nodeName!}</li>
  <li>nodeContactEmail = ${hostingOrganisation.nodeContactEmail!}</li>
</ul>
<#else>
This IPT installation has not been registered yet.
</#if>