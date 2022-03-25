<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.DataSchemaMappingAction" -->

<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl"/>
    <title><@s.text name="manage.mapping.title"/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <script>
        $(document).ready(function(){
            function activateDeactivateAllStaticInputs() {
                $('.fidx').each(function() {
                    activateDeactivateStaticInput($(this));
                });
            }

            function activateDeactivateStaticInput(target) {
                var index = target.attr('id').substring(4);
                var input = $("#fVal"+index);
                if (!target.val().trim()) {
                    input.prop('disabled', false);
                } else {
                    // deactivate input
                    input.val('');
                    input.prop('disabled', true);
                }
            }

            activateDeactivateAllStaticInputs();

            // spy scroll and manage sidebar menu
            $(window).scroll(function () {
                var scrollPosition = $(document).scrollTop();

                $('.bd-toc nav a.sidebar-navigation-link').each(function () {
                    var currentLink = $(this);
                    var anchor = $(currentLink.attr("href"));
                    var sectionId = anchor[0].id.replace("anchor-", "");
                    var section = $("#" + sectionId);
                    var sectionsContainer = $("#sections");

                    if (sectionsContainer.position().top - 100 > scrollPosition) {
                        var removeActiveFromThisLink = $('.bd-toc nav a.active');
                        removeActiveFromThisLink.removeClass('active');
                    } else if (section.position().top - 100  <= scrollPosition
                        && section.position().top + section.height() > scrollPosition) {
                        if (!currentLink.hasClass("active")) {
                            var removeFromThisLink = $('.bd-toc nav a.active');
                            removeFromThisLink.removeClass('active');
                            $(this).addClass('active');
                        }
                    }
                });
            })

            $('.confirm').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>", buttonType: "danger"});
        });
    </script>

    <#assign currentMenu = "manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl"/>
    <#include "/WEB-INF/pages/macros/forms.ftl"/>
    <#include "/WEB-INF/pages/macros/popover.ftl"/>

    <#macro showField field index>
        <div class="row py-1 g-2 mappingRow border-bottom">
            <div class="col-lg-4 pt-1 fs-smaller">
                <#assign fieldPopoverInfo>
                    <#if field.field.description?has_content>${field.field.description}</#if>
                    <#if field.field.example?has_content>
                        <br/><br/>
                        <em><@s.text name="basic.examples"/></em>:
                        <#if field.field.example?is_collection>
                            <#list field.field.example as ex>
                                <code>${ex}</code><#sep>, </#sep>
                            </#list>
                        <#else>
                            <code>${field.field.example}</code>
                        </#if>
                    <#else>
                        <@s.text name="basic.no.description"/>
                    </#if>
                </#assign>
                <@popoverTextInfo fieldPopoverInfo />

                <strong>${field.field.name}</strong>
            </div>

            <div class="col-lg-4">
                <select id="fIdx${index}" class="fidx form-select form-select-sm" name="fields[${index}].index">
                    <option value="" <#if !field.index??> selected="selected"</#if>></option>
                    <#list columns as col>
                        <option value="${col_index}" <#if (field.index!-1)==col_index> selected="selected"</#if>>${col}</option>
                    </#list>
                </select>
            </div>

            <div class="col-lg-4">
                <input id="fVal${index}" class="fval form-control form-control-sm" name="fields[${index}].defaultValue" value="${field.defaultValue!}"/>
            </div>

<#--            <#if field.index??>-->
<#--                <small class="text-truncate"><@sourceSample field.index fieldsIndex/></small>-->
<#--                <div id="fTIdx${fieldsIndex}" class="sample mappingText">-->
<#--                    <small class="mx-3"><@s.text name='manage.mapping.translation' />:</small>-->
<#--                    <a href="translation.do?r=${resource.shortname}&rowtype=${p.extension.rowType?url}&mid=${mid}&term=${p.qualname?url}" class="text-smaller">-->
<#--                        <#if (((field.translation?size)!0)>0)>-->
<#--                            ${(field.translation?size)!0} terms-->
<#--                        <#else>-->
<#--                            <button type="button" class="add btn btn-sm btn-outline-gbif-primary" onclick="window.location.href"><@s.text name="button.add"/></button>-->
<#--                        </#if>-->
<#--                    </a>-->
<#--                </div>-->
<#--            </#if>-->
        </div>
    </#macro>

    <form id="mappingForm" class="needs-validation" action="schemaMapping.do" method="post">
        <div class="container-fluid bg-body border-bottom">

            <div class="container pt-2">
                <#include "/WEB-INF/pages/inc/action_alerts.ftl">
            </div>

            <div class="container p-3 my-3">

                <div class="text-center">
                    <h1 class="pt-2 text-gbif-header fs-4 fw-400 text-center">
                        <@popoverPropertyInfo "manage.mapping.intro"/>
                        <@s.text name='manage.mapping.title'/>
                    </h1>

                    <div class="text-center fs-smaller">
                        <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                    </div>

                    <div class="my-2">
                        <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary" cssStyle="min-width: 100px;" name="save" key="button.save"/>
                        <@s.submit cssClass="confirm btn btn-sm btn-outline-gbif-danger" cssStyle="min-width: 100px;" name="delete" key="button.delete"/>
                        <@s.submit cssClass="button btn btn-sm btn-outline-secondary" cssStyle="min-width: 100px;" name="cancel" key="button.back"/>
                    </div>

                </div>
            </div>
        </div>

        <div class="container-fluid bg-body">
            <div class="container bd-layout">

                <main class="bd-main">

                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <nav id="sidebar-content">
                            <ul>
                                <#list dataSchema.subSchemas as subSchema>
                                    <li>
                                        <a href="#anchor-${subSchema.name}" class="sidebar-navigation-link">${subSchema.title}</a>
                                    </li>
                                </#list>
                            </ul>

                            <ul>
                                <li><a id="toggleFields" class="sidebar-link"><@s.text name='manage.mapping.hideEmpty'/></a></li>
                            </ul>

                            <div class="d-flex align-content-between">
                                <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary me-1" name="save" key="button.save"/>
                                <@s.submit cssClass="confirm btn btn-sm btn-outline-gbif-danger me-1" name="delete" key="button.delete"/>
                                <@s.submit cssClass="button btn btn-sm btn-outline-secondary" name="cancel" key="button.back"/>
                            </div>
                        </nav>
                    </div>

                    <div class="bd-content ps-lg-4">
                        <div>
                            <input type="hidden" name="r" value="${resource.shortname}" />
                            <input type="hidden" name="id" value="${mapping.dataSchema.identifier}" />
                            <input type="hidden" name="mid" value="${mid!}" />
                        </div>

                        <div id="sections" class="mt-4">
                            <span class="anchor anchor-home-resource-page" id="anchor-${dataSchema.subSchemas[0].name}"></span>
                            <div id="${dataSchema.subSchemas[0].name}" class="mt-5">
                                <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fs-5 fw-400">
                                    ${dataSchema.subSchemas[0].title}
                                </h4>
                                <#list fields as field>
                                    <@showField field field_index/>
                                </#list>
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>

    </form>

    <#include "/WEB-INF/pages/inc/footer.ftl"/>
</#escape>
