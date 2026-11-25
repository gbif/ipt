<#ftl output_format="HTML">
<#-- @ftlvariable name="" type="org.gbif.ipt.action.HealthAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/health.css">
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<title><@s.text name="portal.health.title"/></title>
<#assign currentMenu = "health"/>
<#-- disk sizes, versions and the validator's raw answer are for whoever runs the server -->
<#assign details = adminRights>
<#include "/WEB-INF/pages/inc/menu.ftl">

<#--
  Severity: "bad" for what stops the IPT working (directories, a full disk), "warn" for what it can live without
  for a while (GBIF services, a public URL that the internet can't reach, e.g. an installation on a local network).
-->
<#function dirState read write>
    <#if read && write><#return "rw"><#elseif read><#return "ro"><#else><#return "none"></#if>
</#function>
<#assign configState = dirState(readConfigDir, writeConfigDir)>
<#assign logState = dirState(readLogDir, writeLogDir)>
<#assign tmpState = dirState(readTmpDir, writeTmpDir)>
<#if !readResourcesDir><#assign resourcesState = "none">
<#elseif !writeResourcesDir><#assign resourcesState = "ro">
<#elseif !readSubResourcesDir><#assign resourcesState = "someNone">
<#elseif !writeSubResourcesDir><#assign resourcesState = "someRo">
<#else><#assign resourcesState = "rw">
</#if>

<#-- the checks that failed, for the banner -->
<#assign issues = []>
<#if !networkRegistry><#assign issues += [{"label": action.getText("portal.health.network.registry"), "bad": false}]></#if>
<#if !networkRepository><#assign issues += [{"label": action.getText("portal.health.network.repository"), "bad": false}]></#if>
<#if !networkPublicAccess><#assign issues += [{"label": action.getText("portal.health.network.publicAccess"), "bad": false}]></#if>
<#if diskLevel != "ok"><#assign issues += [{"label": action.getText("portal.health.disk.space"), "bad": diskLevel == "bad"}]></#if>
<#if configState != "rw"><#assign issues += [{"label": action.getText("portal.health.file.permissions.config.directory"), "bad": true}]></#if>
<#if logState != "rw"><#assign issues += [{"label": action.getText("portal.health.file.permissions.logs.directory"), "bad": true}]></#if>
<#if tmpState != "rw"><#assign issues += [{"label": action.getText("portal.health.file.permissions.tmp.directory"), "bad": true}]></#if>
<#if resourcesState != "rw"><#assign issues += [{"label": action.getText("portal.health.file.permissions.resources.directory"), "bad": true}]></#if>
<#assign anyBad = issues?filter(i -> i.bad)?has_content>

<#macro stateIcon level>
    <span class="hl-ic ${level}"><#if level == "ok"><@icon name="check_circle"/><#elseif level == "warn"><@icon name="warning"/><#else><@icon name="error"/></#if></span>
</#macro>

<#-- checked=false: the check itself couldn't run (e.g. the validator was down), so nothing is known either way -->
<#macro remoteRow label ok url checked=true>
    <div class="hl-row">
        <@stateIcon level=ok?then("ok", "warn")/>
        <div class="hl-body">
            <div class="hl-name">${label}</div>
            <#if url?has_content><div class="hl-detail"><a href="${url}" class="resource-table-link">${url}</a></div></#if>
            <#nested>
        </div>
        <span class="hl-state"><#if ok><@s.text name="portal.health.reachable"/><#elseif !checked><@s.text name="portal.health.notChecked"/><#else><@s.text name="portal.health.notReachable"/></#if></span>
    </div>
</#macro>

<#macro dirRow labelKey state>
    <div class="hl-row">
        <@stateIcon level=(state == "rw")?then("ok", "bad")/>
        <div class="hl-body"><div class="hl-name"><@s.text name=labelKey/></div><#nested></div>
        <span class="hl-state">
            <#switch state>
                <#case "rw"><@s.text name="portal.health.file.permissions.read.write"/><#break>
                <#case "ro"><@s.text name="portal.health.file.permissions.read.only"/><#break>
                <#case "someRo"><@s.text name="portal.health.file.permissions.at.least.one.read.only"/><#break>
                <#case "someNone"><@s.text name="portal.health.file.permissions.at.least.one.no.access"/><#break>
                <#default><@s.text name="portal.health.file.permissions.no.access"/>
            </#switch>
        </span>
    </div>
</#macro>

<#macro factRow labelKey>
    <div class="hl-row hl-fact">
        <div class="hl-body"><div class="hl-name"><@s.text name=labelKey/></div></div>
        <span class="hl-state"><#nested></span>
    </div>
</#macro>

<div class="container-fluid bg-body border-bottom">
    <div class="container bg-body border rounded-2 mb-4">
        <div class="container my-3 p-3 ov-head ov-head-left">
            <div class="ov-grow">
                <div class="fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb mb-0">
                            <li class="breadcrumb-item"><a href="${baseURL}/"><@s.text name="breadcrumb.home"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="portal.health.title"/></li>
                        </ol>
                    </nav>
                </div>

                <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal">
                    <@s.text name="portal.health.title"/>
                </h1>

                <div class="ov-meta">
                    <span><@s.text name="portal.health.checked"><@s.param>${.now?string("yyyy-MM-dd HH:mm z")}</@s.param></@s.text></span>
                    <#if !issues?has_content>
                        <span>·</span>
                        <span class="hl-meta-ok"><@icon name="check_circle"/> <@s.text name="portal.health.allPassed"/></span>
                    </#if>
                </div>
            </div>
            <#-- each view runs the remote checks again: no button inviting visitors to repeat them -->
            <#if loggedIn>
                <div class="ov-acts">
                    <a href="${baseURL}/health.do" class="btn btn-sm btn-outline-gbif-primary top-button">
                        <@icon name="autorenew"/> <@s.text name="portal.health.checkAgain"/>
                    </a>
                </div>
            </#if>
        </div>
    </div>
</div>

<main class="container main-content-container mb-5">
    <div class="my-3 hl-page">

        <#if issues?has_content>
            <div class="ov-banner ${anyBad?then('bad', 'warn')}">
                <span class="ov-ic"><#if anyBad><@icon name="error"/><#else><@icon name="warning"/></#if></span>
                <div class="ov-grow">
                    <h2><#if anyBad><@s.text name="portal.health.problems"/><#else><@s.text name="portal.health.warnings"/></#if></h2>
                    <p class="ov-text"><#list issues as i>${i.label}<#sep>, </#sep></#list></p>
                </div>
            </div>
        </#if>

        <div class="hl-grid">
            <#-- GBIF services and the way in from the internet -->
            <section class="ov-card">
                <div class="ov-ch"><h2><@s.text name="portal.health.network"/></h2></div>
                <div class="hl-list">
                    <@remoteRow label=action.getText("portal.health.network.registry") ok=networkRegistry url=networkRegistryURL/>
                    <@remoteRow label=action.getText("portal.health.network.repository") ok=networkRepository url=networkRepositoryURL/>
                    <@remoteRow label=action.getText("portal.health.network.publicAccess") ok=networkPublicAccess url=networkPublicAccessURL checked=networkPublicAccessChecked>
                        <#if !networkPublicAccess>
                            <#if details && networkPublicAccessMessage?has_content>
                                <div class="hl-msg"><#if networkPublicAccessStatus?has_content>${networkPublicAccessStatus}: </#if>${networkPublicAccessMessage}</div>
                            </#if>
                            <#if networkPublicAccessChecked>
                                <div class="hl-hint"><@s.text name="portal.health.network.publicAccess.hint"/></div>
                            </#if>
                        </#if>
                    </@remoteRow>
                </div>
            </section>

            <#if details>
                <#-- the data directory's disk -->
                <section class="ov-card">
                    <div class="ov-ch"><h2><@s.text name="portal.health.disk.usage"/></h2></div>
                    <div class="hl-list">
                        <div class="hl-row">
                            <@stateIcon level=diskLevel/>
                            <div class="hl-body">
                                <div class="hl-name"><@s.text name="portal.health.disk.usage.used.ratio"/>: ${diskUsedRatio}%</div>
                                <div class="hl-bar" role="progressbar" aria-valuenow="${diskUsedRatio}" aria-valuemin="0" aria-valuemax="100" aria-label="<@s.text name="portal.health.disk.usage.used.ratio"/>">
                                    <span class="${diskLevel}" style="width: ${diskUsedRatio}%"></span>
                                </div>
                            </div>
                        </div>
                        <@factRow labelKey="portal.health.disk.usage.total.size">${hrDiskTotal}</@factRow>
                        <@factRow labelKey="portal.health.disk.usage.used.size">${hrDiskUsed}</@factRow>
                        <@factRow labelKey="portal.health.disk.usage.free.size">${hrDiskFree}</@factRow>
                    </div>
                </section>
            </#if>

            <#-- the IPT reads and writes all of these; non-admins get free space here as one state, without sizes -->
            <section class="ov-card">
                <div class="ov-ch"><h2><#if details><@s.text name="portal.health.file.permissions"/><#else><@s.text name="portal.health.dataDirectory"/></#if></h2></div>
                <div class="hl-list">
                    <#if !details>
                        <div class="hl-row">
                            <@stateIcon level=diskLevel/>
                            <div class="hl-body"><div class="hl-name"><@s.text name="portal.health.disk.space"/></div></div>
                            <span class="hl-state"><@s.text name="portal.health.disk.space.${diskLevel}"/></span>
                        </div>
                    </#if>
                    <#-- where the folders below are and who must own them -->
                    <#if details>
                        <@factRow labelKey="portal.health.dataDirectory">${dataDirPath}</@factRow>
                        <@factRow labelKey="portal.health.system.user">${systemUser}</@factRow>
                    </#if>
                    <@dirRow labelKey="portal.health.file.permissions.config.directory" state=configState/>
                    <@dirRow labelKey="portal.health.file.permissions.logs.directory" state=logState/>
                    <@dirRow labelKey="portal.health.file.permissions.tmp.directory" state=tmpState/>
                    <@dirRow labelKey="portal.health.file.permissions.resources.directory" state=resourcesState>
                        <#if details && problemResourceDirs?has_content>
                            <div class="hl-detail"><@s.text name="portal.health.file.permissions.folders"/>: ${problemResourceDirs[0..*5]?join(", ")}<#if (problemResourceDirs?size > 5)> (+${problemResourceDirs?size - 5})</#if></div>
                        </#if>
                    </@dirRow>
                </div>
            </section>

            <#if details>
                <section class="ov-card">
                    <div class="ov-ch"><h2><@s.text name="portal.health.system"/></h2></div>
                    <div class="hl-list">
                        <@factRow labelKey="portal.health.system.ipt.version">${cfg.version!}</@factRow>
                        <@factRow labelKey="portal.health.system.ipt.mode"><#if iptMode == "PRODUCTION"><@s.text name="admin.home.state.production"/><#elseif iptMode == "DEVELOPMENT"><@s.text name="admin.home.state.testMode"/><#else>${iptMode}</#if></@factRow>
                        <@factRow labelKey="portal.health.system.os.version">${osName!} ${osVersion!}</@factRow>
                        <@factRow labelKey="portal.health.system.java.version">${javaVersion!}</@factRow>
                        <@factRow labelKey="portal.health.system.app.server.version">${appServerVersion!}</@factRow>
                    </div>
                </section>
            </#if>
        </div>

        <#if !loggedIn>
            <p class="hl-login"><@icon name="info_outlined"/> <a href="${baseURL}/login.do" class="resource-table-link"><@s.text name="portal.health.please.log.in"/></a></p>
        </#if>
    </div>
</main>

<#include "/WEB-INF/pages/inc/footer.ftl">
