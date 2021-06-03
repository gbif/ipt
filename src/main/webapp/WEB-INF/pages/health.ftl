<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="portal.health.title"/></title>
<#assign currentMenu = "health"/>
<#--Custom styles only for about page-->
<style>
  h1 {
    font-size: 20px !important;
    text-align: center !important;
    padding-top: .5rem !important;
    padding-bottom: .5rem !important;
    margin-bottom: .5rem !important;
    color: #4e565f !important;
  }

  .tableRightCell {
    text-align: right
  }

  h3 {
    text-transform: uppercase;
    font-size: 14px;
    font-weight: 400;
    color: #666;
    padding: .5rem 1rem;
    margin-bottom: 0;
    background-color: rgba(0,0,0,.03);
    border-bottom: 1px solid rgba(0,0,0,.125);
  }

</style>
<#include "/WEB-INF/pages/inc/menu.ftl">

<main class="container">

  <div class="my-3 p-3 bg-body rounded shadow-sm">
    <h1><@s.text name="portal.health.title"/></h1>
  </div>

  <div class="row">

    <div class="col-lg-6 my-3">
      <h3><@s.text name="portal.health.network"/></h3>
      <div class="p-3 bg-body rounded shadow-sm">
        <div class="mx-md-4 mx-2">
          <table class="table">
            <tbody>
              <tr>
                <td><@s.text name="portal.health.network.registry.url"/></td>
                <td class="tableRightCell"><a href="${networkRegistryURL}">${networkRegistryURL}</a></td>
              </tr>
              <tr>
                <td><@s.text name="portal.health.network.registry.access"/></td>
                <td class="tableRightCell ${networkRegistry?string("text-gbif-primary", "text-gbif-danger")}">
                  <#if networkRegistry>
                    <@s.text name="portal.health.operational"/>
                  <#else>
                    <@s.text name="portal.health.failed"/>
                  </#if>
                </td>
              </tr>
              <tr>
                <td><@s.text name="portal.health.network.repository.url"/></td>
                <td class="tableRightCell"><a href="${networkRepositoryURL}">${networkRepositoryURL}</a></td>
              </tr>
              <tr>
                <td><@s.text name="portal.health.network.repository.access"/></td>
                <td class="tableRightCell ${networkRegistry?string("text-gbif-primary", "text-gbif-danger")}">
                  <#if networkRepository>
                    <@s.text name="portal.health.operational"/>
                  <#else>
                    <@s.text name="portal.health.failed"/>
                  </#if>
                </td>
              </tr>
              <tr>
                <td><@s.text name="portal.health.network.public.access.url"/></td>
                <td class="tableRightCell"><a href="${networkPublicAccessURL}">${networkPublicAccessURL}</a></td>
              </tr>
              <tr>
                <td><@s.text name="portal.health.network.public.access.access"/></td>
                <td class="tableRightCell ${networkPublicAccess?string("text-gbif-primary", "text-gbif-danger")}">
                  <#if networkPublicAccess>
                    <@s.text name="portal.health.operational"/>
                  <#else>
                    <@s.text name="portal.health.failed"/>
                  </#if>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div class="col-lg-6 my-3">
      <h3><@s.text name="portal.health.disk.usage"/></h3>
      <div class="p-3 bg-body rounded shadow-sm">
        <div class="mx-md-4 mx-2">
          <table class="table">
            <tbody>
            <tr>
              <td><@s.text name="portal.health.disk.usage.used.ratio"/></td>
              <td>
                <div class="progress">
                  <div class="progress-bar <#if diskUsedRatio gt 80>bg-gbif-danger<#else>bg-gbif-primary</#if>" role="progressbar" style="width: ${diskUsedRatio}%;" aria-valuenow="${diskUsedRatio}" aria-valuemin="0" aria-valuemax="100">${diskUsedRatio}%</div>
                </div>
              </td>
            </tr>
            <tr>
              <td><@s.text name="portal.health.disk.usage.total.size"/></td>
              <td class="tableRightCell">${hrDiskTotal}</td>
            </tr>
            <tr>
              <td><@s.text name="portal.health.disk.usage.used.size"/></td>
              <td class="tableRightCell">${hrDiskUsed}</td>
            </tr>
            <tr>
              <td><@s.text name="portal.health.disk.usage.free.size"/></td>
              <td class="tableRightCell">${hrDiskFree}</td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

  </div>

  <div class="row">

    <div class="col-lg-6 my-3">
      <h3><@s.text name="portal.health.file.permissions"/></h3>
      <div class="p-3 bg-body rounded shadow-sm">
        <div class="mx-md-4 mx-2">
          <table class="table">
            <tbody>
            <tr>
              <td><@s.text name="portal.health.file.permissions.config.directory"/></td>
              <td class="tableRightCell">
                <#if readConfigDir>
                  <span class="text-gbif-primary"><@s.text name="portal.health.file.permissions.operational"/></span>
                <#else>
                  <span class="text-gbif-danger"><@s.text name="portal.health.file.permissions.no.access"/></span>
                </#if>
              </td>
            </tr>
            <tr>
              <td><@s.text name="portal.health.file.permissions.logs.directory"/></td>
              <td class="tableRightCell">
                <#if readLogDir && writeLogDir>
                  <span class="text-gbif-primary"><@s.text name="portal.health.file.permissions.operational"/></span>
                <#elseif readLogDir && !writeLogDir>
                  <span class="text-gbif-danger"><@s.text name="portal.health.file.permissions.read.only"/></span>
                <#else>
                  <span class="text-gbif-danger"><@s.text name="portal.health.file.permissions.no.access"/></span>
                </#if>
            </tr>
            <tr>
              <td><@s.text name="portal.health.file.permissions.tmp.directory"/></td>
              <td class="tableRightCell">
                <#if readTmpDir && writeTmpDir>
                  <span class="text-gbif-primary"><@s.text name="portal.health.file.permissions.operational"/></span>
                <#elseif readTmpDir && !writeTmpDir>
                  <span class="text-gbif-danger"><@s.text name="portal.health.file.permissions.read.only"/></span>
                <#else>
                  <span class="text-gbif-danger"><@s.text name="portal.health.file.permissions.no.access"/></span>
                </#if>
            </tr>
            <tr>
              <td><@s.text name="portal.health.file.permissions.resources.directory"/></td>
              <td class="tableRightCell">
                <#if readResourcesDir && writeResourcesDir && readSubResourcesDir && writeSubResourcesDir>
                  <span class="text-gbif-primary"><@s.text name="portal.health.file.permissions.operational"/></span>
                <#elseif readResourcesDir && writeResourcesDir && readSubResourcesDir && !writeSubResourcesDir>
                  <span class="text-gbif-danger"><@s.text name="portal.health.file.permissions.at.least.one.read.only"/></span>
                <#elseif readResourcesDir && writeResourcesDir && !readSubResourcesDir>
                  <span class="text-gbif-danger"><@s.text name="portal.health.file.permissions.at.least.one.no.access"/></span>
                <#elseif readResourcesDir && !writeResourcesDir>
                  <span class="text-gbif-danger"><@s.text name="portal.health.file.permissions.read.only"/></span>
                <#else>
                  <span class="text-gbif-danger"><@s.text name="portal.health.file.permissions.no.access"/></span>
                </#if>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div class="col-lg-6 my-3">
      <h3><@s.text name="portal.health.system"/></h3>
      <div class="p-3 bg-body rounded shadow-sm">
        <div class="mx-md-4 mx-2">
          <table class="table">
            <tbody>
            <tr>
              <td><@s.text name="portal.health.system.os.version"/></td>
              <td class="tableRightCell">
                <#if !loggedIn>
                  <@s.text name="portal.health.please.log.in"/>
                <#else>
                  ${osName} ${osVersion}
                </#if>
              </td>
            </tr>
            <tr>
              <td><@s.text name="portal.health.system.java.version"/></td>
              <td class="tableRightCell">
                <#if !loggedIn>
                  <@s.text name="portal.health.please.log.in"/>
                <#else>
                  ${javaVersion}
                </#if>
              </td>
            </tr>
            <tr>
              <td><@s.text name="portal.health.system.app.server.version"/></td>
              <td class="tableRightCell">
                <#if !loggedIn>
                  <@s.text name="portal.health.please.log.in"/>
                <#else>
                  ${appServerVersion}
                </#if>
              </td>
            </tr>
            <tr>
              <td><@s.text name="portal.health.system.ipt.mode"/></td>
              <td class="tableRightCell">
                <#if !loggedIn>
                  <@s.text name="portal.health.please.log.in"/>
                <#else>
                  ${iptMode}
                </#if>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

  </div>

</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
