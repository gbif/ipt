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
                var suffix = target.attr('id').substring(4);
                var input = $("#fVal" + suffix);
                if (!target.val().trim()) {
                    input.prop('disabled', false);
                } else {
                    // deactivate input
                    input.val('');
                    input.prop('disabled', true);
                }
            }

            activateDeactivateAllStaticInputs();

            function hideFields() {
                showAll = false;
                $("#showAllValue").val("false");
                $("#toggleFields").text("<@s.text name="manage.mapping.showAll" />");
                $('div.mappingRow').not('.required').each(function(index) {
                    // always show all mapped and required fields
                    if ($(".fidx", this).val() === "" && $(".fval", this).val() === ""){
                        $(this).hide();
                    }
                });

                if($('#filterComp option:selected').val() === "") {
                    $('#filterSection').hide();
                }
            }

            // show only required and mapped fields
            $("#toggleFields").click(function() {
                if(showAll) {
                    hideFields();
                } else {
                    showAll = true;
                    $("#showAllValue").val("true");
                    $("#toggleFields").text("<@s.text name="manage.mapping.hideEmpty"/>");
                    $('div.mappingRow').show();
                    $(".groupmenu").show();
                }
            });

            // Collapse/uncollapse source examples
            $(".sample").click(function() {
                if ($(this).hasClass("text-uncollapse")) {
                    $(this).removeClass("text-uncollapse")
                } else {
                    $(this).addClass("text-uncollapse")
                }
            });

            $(".fidx").change(function() {
                activateDeactivateStaticInput($(this));
            });

            var showAll=${Parameters.showAll!"true"};
            if (!showAll){
                hideFields();
            }

            $('.confirm').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>", buttonType: "danger"});
        });
    </script>

    <#assign currentMenu = "manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl"/>
    <#include "/WEB-INF/pages/macros/forms.ftl"/>
    <#include "/WEB-INF/pages/macros/popover.ftl"/>
<#--    1. Interpret backticked text as code-->
<#--    2. Interpret text like []() as a link-->
    <#macro processDescription description>
        <#noescape>
            ${description?replace("`(.*?)`", "<code>$1</code>", "r")?replace("\\[(.*)\\]\\((.*)\\)", "<a href='$2'>$1</a>", "r")}
        </#noescape>
    </#macro>

    <#macro sourceSample index subschemaName fieldsIndex>
        <div id="fSIdx_${subschemaName}_${fieldsIndex}" class="text-collapse sample mappingText mx-3">
            <@s.text name='manage.mapping.sourceSample' />:
            <em>
                <#list peek as row>
                    <#if row??>
                        <#if row[index]?has_content && row[index]!=" ">
                            <code>${row[index]}</code>
                        <#else>
                            &nbsp;
                        </#if>
                        <#if row_has_next> | </#if>
                    </#if>
                </#list>
            </em>
        </div>
    </#macro>

    <#macro showField subschema field index>
        <#assign fieldsIndex = action.getFieldsIndices().get(field.field.name)/>

        <div class="row py-1 g-2 mappingRow border-bottom text-smaller">
            <div class="col-lg-4 pt-1 fs-smaller">
                <#assign fieldPopoverInfo>
                    <#if field.field.description?has_content>
                        <@processDescription field.field.description />
                    </#if>
                    <#if field.field.constraints?? && (field.field.constraints.unique?? || field.field.constraints.maximum?? || field.field.constraints.minimum?? || field.field.constraints.pattern??)>
                        <br/><br/>
                        <em><@s.text name="schema.field.constraints"/>:</em>
                        <ul>
                            <#if field.field.constraints.unique??>
                                <li>unique <code>${field.field.constraints.unique?string}</code></li>
                            </#if>
                            <#if field.field.constraints.maximum??>
                                <li>maximum <code>${field.field.constraints.maximum}</code></li>
                            </#if>
                            <#if field.field.constraints.minimum??>
                                <li>minimum <code>${field.field.constraints.minimum}</code></li>
                            </#if>
                            <#if field.field.constraints.pattern??>
                                <li>pattern <code>${field.field.constraints.pattern}</code></li>
                            </#if>
                        </ul>
                        <#assign noBrakeForExamples = true />
                    <#else>
                        <#assign noBrakeForExamples = false />
                    </#if>
                    <#if (field.field.example)?has_content>
                        <#if !noBrakeForExamples>
                            <br/><br/>
                        </#if>
                        <em><@s.text name="basic.examples"/></em>:
                        <#if field.field.example?is_collection>
                            <#list field.field.example as ex>
                                <code>${ex}</code><#sep>, </#sep>
                            </#list>
                        <#else>
                            <#if field.field.example?is_boolean>
                                <code>${field.field.example?string("true", "false")}</code>
                            <#else>
                                <code>${field.field.example}</code>
                            </#if>
                        </#if>
                    <#else>
                        <@s.text name="basic.no.description"/>
                    </#if>
                </#assign>
                <@popoverTextInfo fieldPopoverInfo />

                <strong>
                    ${field.field.name}
                    <#if (field.field.constraints.required)?? && (field.field.constraints.required?string) == "true"><span class="text-gbif-danger">&#42;</span></#if>
                </strong>
            </div>

            <div class="col-lg-4">
                <select id="fIdx_${subschema.name}_${index}" class="fidx form-select form-select-sm" name="fields[${index}].index">
                    <option value="" <#if !field.index??> selected="selected"</#if>></option>
                    <#list columns as col>
                        <option value="${col_index}" <#if (field.index!-1)==col_index> selected="selected"</#if>>${col}</option>
                    </#list>
                </select>
            </div>

            <div class="col-lg-4">
                <#if field.field.constraints?? && field.field.constraints.vocabulary??>
                    <div class="input-group input-group-sm">
                        <label class="input-group-text" for="fVal_${subschema.name}_${index}">
                            <i class="bi bi-book"></i>
                        </label>
                        <select id="fVal_${subschema.name}_${index}" class="fval form-select form-select-sm" name="fields[${index}].defaultValue">
                            <option value="" <#if !field.defaultValue??> selected="selected"</#if>></option>
                            <#list field.field.constraints.vocabulary as code>
                                <option value="${code}" <#if (field.defaultValue!"")==code> selected="selected"</#if>>${code}</option>
                            </#list>
                        </select>
                    </div>
                <#else>
                    <input id="fVal_${subschema.name}_${index}" class="fval form-control form-control-sm" name="fields[${index}].defaultValue" value="${field.defaultValue!}"/>
                </#if>
            </div>

            <#if field.index??>
                <small><@sourceSample field.index subschema.name fieldsIndex/></small>
            </#if>
        </div>
    </#macro>

    <form id="mappingForm" class="needs-validation" action="schemaMapping.do" method="post">
        <div class="container-fluid bg-body border-bottom">

            <div class="container my-3">
                <#include "/WEB-INF/pages/inc/action_alerts.ftl">
            </div>

            <div class="container p-3">
                <div class="text-center text-uppercase fw-bold fs-smaller-2">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                            <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.mapping"/></li>
                        </ol>
                    </nav>
                </div>

                <div class="text-center">
                    <h1 class="pt-2 text-gbif-header fs-2 fw-400 text-center">
                        <@popoverPropertyInfo "manage.mapping.intro"/>
                        <@s.text name='manage.mapping.title'/>
                    </h1>

                    <div class="text-center fs-smaller">
                        <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                    </div>

                    <div class="my-2">
                        <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/>
                        <@s.submit cssClass="confirm btn btn-sm btn-outline-gbif-danger top-button" name="delete" key="button.delete"/>
                        <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" name="cancel" key="button.back"/>
                    </div>

                    <p class="mt-3 text-smaller fst-italic">
                        <@s.text var="adminSchemaTitle" name="admin.schema.title"/>
                        <@s.text name='manage.mapping.intro1'>
                            <@s.param>
                                <a href="source.do?r=${resource.shortname}&id=${mapping.source.name}" title="<@s.text name='manage.overview.source.data'/>">
                                    ${mapping.source.name}
                                </a>
                            </@s.param>
                            <@s.param><@s.property value="#adminSchemaTitle.toLowerCase()"/></@s.param>
                            <@s.param><a href="${baseURL}/admin/schema.do?id=${mapping.dataSchema.identifier!}#anchor-${mapping.dataSchemaFile!}" target="_blank">${mapping.dataSchema.name!}/${mapping.dataSchemaFile!}</a></@s.param>
                        </@s.text>
                    </p>
                </div>
            </div>
        </div>

        <div class="container-fluid bg-body">
            <div class="container bd-layout">

                <main class="bd-main">

                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <nav id="sidebar-content">
                            <ul>
                                <li><a id="toggleFields" class="sidebar-link"><@s.text name='manage.mapping.hideEmpty'/></a></li>
                            </ul>

                            <div class="d-flex align-content-between" style="margin-left: -10px;">
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
                            <#list dataSchema.subSchemas as subSchema>
                                <#if (mapping.dataSchemaFile)?? && mapping.dataSchemaFile == subSchema.name>
                                    <span class="anchor anchor-home-resource-page" id="anchor-${subSchema.name}"></span>
                                    <div id="${subSchema.name}" class="mt-5">
                                        <h4 class="pb-2 mb-2 pt-2 text-gbif-header-2 fs-5 fw-400">
                                            ${subSchema.title}
                                        </h4>
                                        <#list fields as field>
                                            <@showField subSchema field field_index/>
                                        </#list>
                                    </div>
                                </#if>
                            </#list>
                        </div>
                    </div>
                </main>
            </div>
        </div>

    </form>

    <#include "/WEB-INF/pages/inc/footer.ftl"/>
</#escape>
