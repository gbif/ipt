<h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-primary text-center">About this IPT installation</h5>
<#if hostingOrganisation?? && hostingOrganisation.name??>
<p class="text-muted mx-md-4 mx-2">This is a default IPT hosted by ${hostingOrganisation.name}</p>

<p class="text-muted mx-md-4 mx-2">You can use the following variables about the hosting organization:</p>
<ul class="list-group text-muted mx-md-4 mx-2">
  <li class="list-group-item">description = ${hostingOrganisation.description!}</li>
  <li class="list-group-item">name = ${hostingOrganisation.name!}</li>
  <li class="list-group-item">alias = ${hostingOrganisation.alias!}</li>
  <li class="list-group-item">homepageURL = ${hostingOrganisation.homepageURL!}</li>
  <li class="list-group-item">primaryContactType = ${hostingOrganisation.primaryContactType!}</li>
  <li class="list-group-item">primaryContactName = ${hostingOrganisation.primaryContactName!}</li>
  <li class="list-group-item">primaryContactDescription = ${hostingOrganisation.primaryContactDescription!}</li>
  <li class="list-group-item">primaryContactAddress = ${hostingOrganisation.primaryContactAddress!}</li>
  <li class="list-group-item">primaryContactEmail = ${hostingOrganisation.primaryContactEmail!}</li>
  <li class="list-group-item">primaryContactPhone = ${hostingOrganisation.primaryContactPhone!}</li>
  <li class="list-group-item">nodeKey = ${hostingOrganisation.nodeKey!}</li>
  <li class="list-group-item">nodeName = ${hostingOrganisation.nodeName!}</li>
  <li class="list-group-item">nodeContactEmail = ${hostingOrganisation.nodeContactEmail!}</li>
</ul>
<#else>
  <p class="text-muted text-center">This IPT installation has not been registered yet.</p>
</#if>
