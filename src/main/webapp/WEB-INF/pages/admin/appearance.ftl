<#ftl output_format="HTML">
<#-- @ftlvariable name="" type="org.gbif.ipt.action.admin.AppearanceAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name="admin.home.manageUI"/></title>
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/appearance.css">
<script src="${baseURL}/js/appearance.js"></script>

<#assign currentMenu = "admin"/>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/icon.ftl"/>

<#-- the GBIF mark, as in inc/menu.ftl -->
<#macro gbifMark>
    <svg class="ap-mark" viewBox="0 0 539.7 523.9" aria-hidden="true" focusable="false">
        <path d="M230.7,255.5c0-102.2,49.9-190.7,198.4-190.7C429.1,167.2,361.7,255.5,230.7,255.5"/>
        <path d="M468.6,523.9c27.8,0,49.2-4,71.1-12c0-80.9-48.3-138.7-133.5-180.4c-65.2-32.7-145.5-49.7-218.8-49.7C219.5,185.4,196.1,65.7,165,0c-34.5,68.8-56,186.8-22.9,282.8C77,287.6,25.4,315.9,3.6,353.3c-1.6,2.8-5,8.9-3,10c1.6,0.8,4.1-1.7,5.6-3.1c23.5-21.8,54.6-32.4,84.5-32.4c69.1,0,117.8,57.3,152.3,91.7C317.1,493.5,389.4,524.1,468.6,523.9"/>
    </svg>
</#macro>

<#-- a scheme as data attributes, read by js/appearance.js when its button is pressed -->
<#macro schemeData cs>data-primary-color="${cs.primaryColor}" data-navbar-color="${cs.navbarColor}" data-navbar-link-color="${cs.navbarLinkColor}" data-navbar-gbif-logo-color="${cs.navbarGbifLogoColor}" data-navbar-active-tab-color="${cs.navbarActiveTabColor}" data-link-color="${cs.linkColor}"</#macro>

<#-- a scheme button: the bar with its menu text and logo, and the button colour -->
<#macro schemeButton key cs isDefault=false>
    <button type="button" class="ap-scheme" data-scheme="${key}" <@schemeData cs/> aria-pressed="false">
        <span class="ap-scheme-bar" style="background:${cs.navbarColor};color:${cs.navbarLinkColor}">
            <span class="ap-scheme-logo" style="background:${cs.navbarGbifLogoColor}"></span>
            <span class="ap-scheme-menu">Aa</span>
            <span class="ap-scheme-tab" style="background:${cs.navbarActiveTabColor}"></span>
        </span>
        <span class="ap-scheme-page"><span class="ap-scheme-btn" style="background:${cs.primaryColor}"></span><span class="ap-scheme-link" style="background:${cs.linkColor}"></span></span>
        <span class="ap-scheme-name"><@s.text name="admin.uiManagement.scheme.${key}"/><#if isDefault> <span class="ap-scheme-default"><@s.text name="admin.uiManagement.scheme.default"/></span></#if></span>
    </button>
</#macro>

<#-- one colour: the label, where it is used, a picker and a hex field, and its contrast checks (filled in by js) -->
<#macro colourRow field value checks="" off=false>
    <div class="ap-row<#if off> ap-row-off</#if>" data-field="${field}" data-checks="${checks}">
        <div class="ap-row-text">
            <label for="${field}-hex" class="ap-label"><@s.text name="admin.uiManagement.${field}"/></label>
            <div class="ap-where"><@s.text name="admin.uiManagement.${field}.where"/></div>
            <#if off><div class="ap-where ap-off-note"><@s.text name="admin.uiManagement.navbarGbifLogoColor.customLogo"/></div></#if>
        </div>
        <div class="ap-row-input">
            <div class="ap-pick">
                <input type="color" class="ap-swatch" id="${field}-picker" value="${(value?matches('#[0-9a-fA-F]{6}'))?then(value?lower_case, '#000000')}" aria-label="<@s.text name='admin.uiManagement.${field}'/>">
                <input type="text" class="form-control form-control-sm ap-hex" id="${field}-hex" name="colorScheme.${field}" value="${value}" maxlength="7" spellcheck="false" autocomplete="off">
            </div>
            <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="colorScheme.${field}"/>
            <div class="ap-checks" aria-live="polite"></div>
        </div>
    </div>
</#macro>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<form id="appearance-form" action="appearance.do" method="post" enctype="multipart/form-data" novalidate
      data-test-mode="${isTest?c}"
      data-text-navbar="<@s.text name='admin.uiManagement.contrast.navbar'/>"
      data-text-stripes="<@s.text name='admin.uiManagement.contrast.stripes'/>"
      data-text-white="<@s.text name='admin.uiManagement.contrast.white'/>"
      data-text-low="<@s.text name='admin.uiManagement.contrast.low'/>"
      data-text-invalid="<@s.text name='admin.uiManagement.error.colour'/>"
      data-text-logo-type="<@s.text name='validation.logo.type'/>"
      data-text-leave="<@s.text name='admin.uiManagement.savebar.leave'/>">
    <input type="hidden" name="removeLogo" id="removeLogo" value="false">
    <#-- icons for the contrast checks, cloned by js/appearance.js -->
    <template id="ap-check-icons"><@icon name="check_circle" class="ap-check-ok"/><@icon name="warning" class="ap-check-low"/></template>

    <#-- the heading every IPT page has: centred, in a box; what it is, then what you can do with it -->
    <div class="container-fluid border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3 ov-head ov-head-left">
                <div class="ov-grow">
                    <div class="fs-smaller">
                        <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                            <ol class="breadcrumb mb-0">
                                <li class="breadcrumb-item"><a href="${baseURL}/admin/"><@s.text name="breadcrumb.admin"/></a></li>
                                <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.admin.ui"/></li>
                            </ol>
                        </nav>
                    </div>
                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal"><@s.text name="admin.home.manageUI"/></h1>
                    <div class="ov-meta"><span><@s.text name="admin.uiManagement.intro"/></span></div>
                </div>
                <div class="ov-acts">
                    <button type="submit" class="btn btn-sm btn-gbif-solid top-button" name="save" value="save"><@icon name="save"/> <@s.text name="button.save"/></button>
                    <a href="${baseURL}/admin/" class="btn btn-sm btn-outline-secondary top-button"><@s.text name="button.back"/></a>
                </div>
            </div>
        </div>
    </div>

    <div class="container-fluid bg-body">
        <div class="container main-content-container">
            <main class="pt-4 pb-5">
                <#if colorSchemeReplaced && previousColorScheme??>
                    <div class="ov-banner ap-banner" role="status">
                        <@icon name="warning" class="ov-ic"/>
                        <div class="ov-grow"><@s.text name="admin.uiManagement.replaced"/></div>
                    </div>
                </#if>

                <section class="ov-card" aria-labelledby="ap-logo-title">
                    <div class="ov-ch"><h2 id="ap-logo-title"><@s.text name="admin.uiManagement.logo"/></h2></div>
                    <div class="ov-sub"><@s.text name="admin.uiManagement.logo.help"/></div>
                    <div class="ap-logo">
                        <div class="ap-logo-preview" id="ap-logo-preview">
                            <span class="ap-logo-default"<#if customLogo> hidden</#if>><@gbifMark/></span>
                            <img id="applogo-image" alt="<@s.text name='admin.uiManagement.iptLogo'/>"<#if customLogo> src="${baseURL}/appLogo.do"<#else> hidden</#if>>
                        </div>
                        <div class="ap-logo-input">
                            <label for="file" class="form-label visually-hidden"><@s.text name="admin.uiManagement.iptLogo"/></label>
                            <input type="file" class="form-control form-control-sm" name="file" id="file" accept="image/png,image/jpeg,image/gif">
                            <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="file"/>
                            <div class="invalid-feedback d-block" id="ap-logo-error" hidden></div>
                            <div class="ap-where"><@s.text name="admin.uiManagement.logo.formats"/></div>
                            <button type="button" class="btn btn-sm btn-link ap-logo-remove" id="buttonRemove"<#if !customLogo> hidden</#if>><@icon name="delete"/> <@s.text name="admin.uiManagement.logo.remove"/></button>
                        </div>
                    </div>
                </section>

                <section class="ov-card" aria-labelledby="ap-schemes-title">
                    <div class="ov-ch"><h2 id="ap-schemes-title"><@s.text name="admin.uiManagement.schemes"/></h2></div>
                    <div class="ov-sub"><@s.text name="admin.uiManagement.schemes.help"/></div>
                    <div class="ap-schemes">
                        <#list presets as key, scheme>
                            <@schemeButton key=key cs=scheme isDefault=(key?index == 0)/>
                        </#list>
                        <#if previousColorScheme??>
                            <@schemeButton key="previous" cs=previousColorScheme/>
                        </#if>
                    </div>
                </section>

                <section class="ov-card" aria-labelledby="ap-navbar-title">
                    <div class="ov-ch"><h2 id="ap-navbar-title"><@s.text name="admin.uiManagement.group.navbar"/></h2></div>
                    <div class="ov-sub"><@s.text name="admin.uiManagement.contrast.help"/></div>
                    <div class="ap-rows">
                        <@colourRow field="navbarColor" value=colorScheme.navbarColor/>
                        <@colourRow field="navbarLinkColor" value=colorScheme.navbarLinkColor checks="navbar:4.5"/>
                        <@colourRow field="navbarActiveTabColor" value=colorScheme.navbarActiveTabColor checks="navbar:3"/>
                        <@colourRow field="navbarGbifLogoColor" value=colorScheme.navbarGbifLogoColor checks="navbar:3" off=customLogo/>
                    </div>
                </section>

                <section class="ov-card" aria-labelledby="ap-pages-title">
                    <div class="ov-ch"><h2 id="ap-pages-title"><@s.text name="admin.uiManagement.group.pages"/></h2></div>
                    <div class="ov-sub"><@s.text name="admin.uiManagement.group.pages.help"/></div>
                    <div class="ap-rows">
                        <@colourRow field="primaryColor" value=colorScheme.primaryColor checks="white:4.5"/>
                        <@colourRow field="linkColor" value=colorScheme.linkColor checks="white:4.5"/>
                    </div>
                </section>
            </main>
        </div>
    </div>

    <div class="ap-savebar" id="ap-savebar" aria-hidden="true">
        <div class="container">
            <span class="ap-grow"><@s.text name="admin.uiManagement.savebar.unsaved"/></span>
            <a class="btn btn-sm btn-outline-secondary" href="${baseURL}/admin/appearance.do" id="ap-discard"><@s.text name="admin.uiManagement.savebar.discard"/></a>
            <button type="submit" class="btn btn-sm btn-gbif-solid" name="save" value="save"><@icon name="save"/> <@s.text name="button.save"/></button>
        </div>
    </div>
</form>

<#include "/WEB-INF/pages/inc/footer.ftl">
