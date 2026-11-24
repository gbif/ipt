<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.DataPackageFieldTranslationAction" -->
<#-- Value translation of one mapped data package field: what each source value is published as. Values list, save bar
     and delete dialog: macros/translation.ftl; behaviour and the field's type / constraint checks: js/translation.js -->
<#include "/WEB-INF/pages/macros/icon.ftl"/>
<#include "/WEB-INF/pages/inc/header.ftl">
<#include "/WEB-INF/pages/macros/translation.ftl"/>
<#assign mappingUrl = "dataPackageMapping.do?r=${resource.shortname}&id=${(mapping.dataPackageSchema.identifier)!?url}&mid=${mid}"/>
<#assign pageUrl = "dataPackageFieldTranslation.do?r=${resource.shortname}&id=${(mapping.dataPackageSchema.identifier)!?url}&field=${field.name?url}&mid=${mid}"/>
<#assign constraints = field.constraints!{}/>
<title><@s.text name="manage.translation.title"/>: ${field.name}</title>
<style>
    /* where the sticky toolbar sits: just under the fixed navbar */
    :root { --navbar-height: 120px; --ov-bars-height: 57px; }
</style>
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2-ipt.css">
<link rel="stylesheet" href="${baseURL}/styles/overview.css">
<link rel="stylesheet" href="${baseURL}/styles/mapping.css">
<link rel="stylesheet" href="${baseURL}/styles/translation.css">
<script src="${baseURL}/js/select2/select2-4.0.13.full.min.js"></script>
<script src="${baseURL}/js/translation.js"></script>
<#assign currentMenu = "manage"/>
<#include "/WEB-INF/pages/inc/menu.ftl">

<#macro processDescription description>
    ${description?replace("`(.*?)`", "<code>$1</code>", "r")?replace("\\[(.*)\\]\\((.*)\\)", "<a href='$2'>$1</a>", "r")?no_esc}
</#macro>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container-fluid border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3 ov-head ov-head-left">
                <div class="ov-grow">
                    <div class="fs-smaller">
                        <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                            <ol class="breadcrumb mb-0">
                                <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                                <li class="breadcrumb-item ov-crumb-resource"><a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a></li>
                                <li class="breadcrumb-item"><a href="${mappingUrl}"><#if (mapping.dataPackageTableSchemaName.name)?has_content>${mapping.dataPackageTableSchemaName.name}<#else><@s.text name="breadcrumb.manage.overview.mapping"/></#if></a></li>
                                <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.mapping.translation"/></li>
                            </ol>
                        </nav>
                    </div>
                    <#-- the field; "Value translation" is the pill below -->
                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal">${field.name}</h1>
                    <div class="ov-meta mp-source-line">
                        <span class="mp-kind-pill"><@s.text name="manage.translation.title"/></span>
                        <#if field.type?has_content><span><@s.text name="schema.field.type"/>: ${field.type}</span></#if>
                        <#if (constraints.vocabulary)?has_content><span><@s.text name="manage.translation.allowed"/>: ${constraints.vocabulary?size}</span></#if>
                    </div>
                </div>

                <div class="ov-acts">
                    <button type="submit" id="save" name="save" value="save" form="translation-form" class="btn btn-sm btn-gbif-solid top-button"><@icon name="save"/> <@s.text name="button.save"/></button>
                    <a class="btn btn-sm btn-outline-secondary top-button" role="button" href="${mappingUrl}"><@s.text name="button.back"/></a>
                    <#-- more actions last, as on the resource overview -->
                    <div class="dropdown">
                        <button type="button" class="btn btn-sm btn-outline-secondary top-button ov-more-btn" data-bs-toggle="dropdown" aria-expanded="false" title="<@s.text name='manage.overview.moreActions'/>" aria-label="<@s.text name='manage.overview.moreActions'/>"><@icon name="more_horiz"/></button>
                        <ul class="dropdown-menu dropdown-menu-end">
                            <li><a class="dropdown-item" href="dataPackageFieldTranslationReload.do?r=${resource.shortname}&field=${field.name?url}&mid=${mid}"><@s.text name="manage.translation.reload"/></a></li>
                            <li><button type="button" class="dropdown-item text-gbif-danger" data-bs-toggle="modal" data-bs-target="#tr-delete-modal"><@s.text name="button.delete"/></button></li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <@i18n/>

    <main class="container main-content-container pt-4 pb-5">
        <#-- the field's definition and what its values must be; js/translation.js checks translations against the data-* -->
        <section class="mp-section" id="tr-field"
                 data-type="${field.type!}"
                 <#if (constraints.pattern)??>data-pattern="${constraints.pattern}"</#if>
                 <#if (constraints.minimum)??>data-minimum="${constraints.minimum?c}"</#if>
                 <#if (constraints.maximum)??>data-maximum="${constraints.maximum?c}"</#if>
                 data-required="${((constraints.required)!false)?c}">
            <div class="mp-sec-head"><h2><@s.text name="manage.translation.field.title"/></h2></div>
            <div class="tr-def">
                <#if field.description?has_content>
                    <p><@processDescription field.description/></p>
                <#else>
                    <p class="text-muted"><@s.text name="basic.no.description"/></p>
                </#if>
                <dl class="tr-facts">
                    <#if field.type?has_content><dt><@s.text name="schema.field.type"/></dt><dd>${field.type}</dd></#if>
                    <#if field.format?? && field.format != 'default'><dt><@s.text name="schema.field.format"/></dt><dd><code>${field.format}</code></dd></#if>
                    <#if ((constraints.required)!false) || (constraints.unique)?? || (constraints.minimum)?? || (constraints.maximum)?? || (constraints.pattern)??>
                        <dt><@s.text name="schema.field.constraints"/></dt>
                        <dd>
                            <#if (constraints.required)!false>required </#if>
                            <#if (constraints.unique)??>unique <code>${constraints.unique?string}</code> </#if>
                            <#if (constraints.minimum)??>minimum <code>${constraints.minimum}</code> </#if>
                            <#if (constraints.maximum)??>maximum <code>${constraints.maximum}</code> </#if>
                            <#if (constraints.pattern)??>pattern <code>${constraints.pattern}</code></#if>
                        </dd>
                    </#if>
                    <#if (field.example)?has_content>
                        <dt><@s.text name="basic.examples"/></dt>
                        <dd>
                            <#if field.example?is_collection><#list field.example as ex><code>${ex}</code><#sep>, </#sep></#list>
                            <#elseif field.example?is_boolean><code>${field.example?string("true", "false")}</code>
                            <#else><code>${field.example}</code></#if>
                        </dd>
                    </#if>
                    <#if (constraints.vocabulary)?has_content>
                        <dt><@s.text name="manage.translation.allowed"/></dt>
                        <dd><#list constraints.vocabulary as v><code>${v}</code><#sep>, </#sep></#list></dd>
                    </#if>
                </dl>
            </div>
        </section>

        <@valuesSection formAction="dataPackageFieldTranslation.do" automapUrl=(constraints.vocabulary)?has_content?then("dataPackageFieldTranslationAutomap.do?r=${resource.shortname}&field=${field.name?url}&mid=${mid}", "")>
            <input type="hidden" name="r" value="${resource.shortname}"/>
            <input type="hidden" name="id" value="${(mapping.dataPackageSchema.identifier)!}"/>
            <input type="hidden" name="mid" value="${mid}"/>
            <input type="hidden" name="field" value="${field.name}"/>
        </@valuesSection>
    </main>

    <@saveBar discardUrl=pageUrl/>
    <@deleteModal fieldName=field.name/>

<#include "/WEB-INF/pages/inc/footer.ftl">
