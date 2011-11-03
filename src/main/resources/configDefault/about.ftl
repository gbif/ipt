<h1>About this IPT installation</h1>
<#if host.name??>
<p>This is a default IPT hosted by ${host.name}</p>

<p>You can use the following variables about the hosting organisation:</p>
<ul>
  <li>description = ${host.description!}</li>
  <li>name = ${host.name!}</li>
  <li>alias = ${host.alias!}</li>
  <li>homepageURL = ${host.homepageURL!}</li>
  <li>primaryContactType = ${host.primaryContactType!}</li>
  <li>primaryContactName = ${host.primaryContactName!}</li>
  <li>primaryContactDescription = ${host.primaryContactDescription!}</li>
  <li>primaryContactAddress = ${host.primaryContactAddress!}</li>
  <li>primaryContactEmail = ${host.primaryContactEmail!}</li>
  <li>primaryContactPhone = ${host.primaryContactPhone!}</li>
  <li>nodeKey = ${host.nodeKey!}</li>
  <li>nodeName = ${host.nodeName!}</li>
  <li>nodeContactEmail = ${host.nodeContactEmail!}</li>
</ul>
<#else>
This IPT installation has not been registered yet.
</#if>