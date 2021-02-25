<h4 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-success text-center">About this IPT installation</h4>
<#if hostingOrganisation?? && hostingOrganisation.name??>
<p class="text-muted mx-md-4 mx-2">This is a default IPT hosted by ${hostingOrganisation.name}</p>

<p class="text-muted mx-md-4 mx-2">You can use the following variables about the hosting organization:</p>
<ul class="list-unstyled text-muted">
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
  <p class="text-muted mx-md-4 mx-2">This IPT installation has not been registered yet.</p>
</#if>
