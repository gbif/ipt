<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.DataPackageMappingAction" -->
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.mapping.title'/></title>
<#assign currentMenu = "manage"/>
<link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
<script src="${baseURL}/js/select2/select2-4.0.13.full.min.js"></script>

<style>
    .select2-container--default .select2-results__option--highlighted[aria-selected] {
        background-color: rgba(0, 0, 0, .08) !important;
    }

    .select2-results__option--highlighted .select2-result__title {
        color: #575757 !important;
    }

    .select2-result__title {
        color: #575757;
        font-weight: 700;
        word-wrap: break-word;
        line-height: 1.1;
        margin-bottom: 4px;
    }

    .select2-results__option--highlighted .select2-result__title {
        color: white;
    }

    .select2-result {
        padding-top: 4px;
        padding-bottom: 3px;
    }

    .select2-result__meta {
        margin-left: 10px;
    }

    .select2-result__description, .select2-result__comments {
        font-size: 13px;
        color: #777;
        margin-top: 4px;
    }

    .select2-result__comments {
        font-style: italic;
    }

    .select2-result__type {
        display: inline-block;
        color: #777;
        font-size: 11px;
    }

    .select2-result__type {
        margin-right: 1em;
    }
</style>

<script>
    $(document).ready(function () {
        <#if dataPackageSchema.name == "dwc-dp">
        const dataPackageTables = [
            <#list dataPackageSchema.tableSchemas as tableSchema>
            {
                "identifier": "${tableSchema.identifier?js_string}",
                "name": "${tableSchema.name?js_string}",
                "title": "${tableSchema.title?js_string}",
                "description": "${tableSchema.description?js_string}",
                "comments": "${tableSchema.comments?js_string}",
                "examples": "${tableSchema.examples?js_string}",
                "totalFields": ${tableSchema.fields?size}
            }<#if tableSchema_has_next>, </#if>
            </#list>
        ];

        var tables;

        fetch('${baseURL}/js/dwc-dp/dwc-dp-types-minimal.json')
            .then(response => response.json())
            .then(dataPackageTypes => {
                tables = dataPackageTables.map(schema => ({
                    id: schema.name,
                    text: schema.title,
                    name: schema.name,
                    description: schema.description,
                    totalFields: schema.totalFields,
                    comments: schema.comments,
                    examples: schema.examples,
                    type: dataPackageTypes[schema.name]?.type || ""
                }));

                $("#newTableSchemas\\[0\\]").select2({
                    data: tables,
                    placeholder: 'Select a table schema',
                    templateResult: formatDataPackage,
                    templateSelection: formatDataPackageSelection,
                    width: "100%",
                    theme: 'bootstrap4'
                });
            });

        $("#newSources\\[0\\]").select2({
            placeholder: 'Select a source',
            language: {
                noResults: function () {
                    return '${selectNoResultsFound}';
                }
            },
            width: "100%",
            theme: 'bootstrap4'
        });

        function formatDataPackage (schema) {
            var $container = $(
                "<div class='select2-result clearfix'>" +
                "<div class='select2-result__meta'>" +
                "<div class='select2-result__title'></div>" +
                "<div class='select2-result__description'></div>" +
                "<div class='select2-result__comments'></div>" +
                "<div class='select2-result__statistics'>" +
                "<div class='select2-result__type'>" +
                "<i class='bi bi-circle-fill fs-smaller-2 color-dwc-dp-" + schema.type + "'></i>" +
                "<span class='ms-1'>" + schema.type + "</span>" +
                "<span class='ms-1'>|</span>" +
                "<span class='ms-1'>" + schema.totalFields + " fields</span>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</div>"
            );

            $container.find(".select2-result__title").text(schema.text);
            $container.find(".select2-result__description").text(schema.description);
            $container.find(".select2-result__comments").text(schema.comments);

            return $container;
        }

        function formatDataPackageSelection (repo) {
            return repo.full_name || repo.text;
        }
        </#if>

        $("#schemaFile").select2({
            placeholder: '',
            language: {
                noResults: function () {
                    return '${selectNoResultsFound}';
                }
            },
            width: "100%",
            minimumResultsForSearch: 15,
            theme: 'bootstrap4'
        });
        $("#source").select2({
            placeholder: '',
            language: {
                noResults: function () {
                    return '${selectNoResultsFound}';
                }
            },
            width: "100%",
            minimumResultsForSearch: 15,
            theme: 'bootstrap4'
        });

        function calculateNumberOfItems(name) {
            return $("#mappings .item").length + 1;
        }

        $("#plus-mapping").click(function (event) {
            event.preventDefault();
            addNewMappingItem(true);
        });

        function addNewMappingItem(effects) {
            var mappingItems = calculateNumberOfItems("mapping");
            var newItem = $('#baseItem').clone();
            if (effects) newItem.hide();
            newItem.appendTo('#mappings');

            if (effects) {
                newItem.slideDown('slow');
            }

            setMappingItemIndex(newItem, mappingItems);

            <#if dataPackageSchema.name == 'dwc-dp'>
            $("#newTableSchemas\\[" + mappingItems + "\\]").select2({
                data: tables,
                placeholder: 'Select a table schema',
                templateResult: formatDataPackage,
                templateSelection: formatDataPackageSelection,
                width: "100%",
                theme: 'bootstrap4'
            });

            $("#newSources\\[" + mappingItems + "\\]").select2({
                placeholder: 'Select a source',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                theme: 'bootstrap4'
            });
            </#if>
        }

        $(".removeMappingLink").click(function (event) {
            removeMappingItem(event);
        });

        function removeMappingItem(event) {
            event.preventDefault();
            var $target = $(event.target);
            if (!$target.is('a')) {
                $target = $(event.target).closest('a');
            }
            $('#mapping-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
                $(this).remove();
                $("#mappings .item").each(function (index) {
                    setMappingItemIndex($(this), index + 1);
                });

            });
        }

        function setMappingItemIndex(item, index) {
            item.attr("id", "mapping-item-" + index);

            $("#mapping-item-" + index + " [id^='mapping-removeLink']").attr("id", "mapping-removeLink-" + index);
            $("#mapping-removeLink-" + index).click(function (event) {
                removeMappingItem(event);
            });

            $("#mapping-item-" + index + " [id^='newTableSchemas']").attr("id", "newTableSchemas[" + index + "]").attr("name", function () {
                return $(this).attr("id");
            });
            $("#mapping-item-" + index + " [for^='newTableSchemas']").attr("for", "newTableSchemas[" + index + "]");

            $("#mapping-item-" + index + " [id^='newSources']").attr("id", "newSources[" + index + "]").attr("name", function () {
                return $(this).attr("id");
            });
            $("#mapping-item-" + index + " [for^='newSources']").attr("for", "additionalSource[" + index + "]");
        }
    });
</script>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<form class="topForm" action="dataPackageMappingSourceCreate.do" method="post">
    <div class="container-fluid bg-body border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container p-3 my-3">
                <div class="text-center fs-smaller">
                    <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                        <ol class="breadcrumb justify-content-center mb-0">
                            <li class="breadcrumb-item"><a href="/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                            <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.mapping"/></li>
                        </ol>
                    </nav>
                </div>

                <div class="text-center">
                    <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                        <@s.text name="manage.mapping.title"/>
                    </h1>

                    <div class="text-center fs-smaller">
                        <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                    </div>

                    <div class="my-2">
                        <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/>
                        <a href="resource.do?r=${resource.shortname}" class="button btn btn-sm btn-outline-secondary top-button"><@s.text name="button.cancel"/></a>
                    </div>

                </div>
            </div>
        </div>
    </div>

    <div class="container-fluid bg-body">
        <div class="container pt-4">
            <p class="fst-italic text-center">${dataPackageSchema.description}</p>

            <input type="hidden" name="r" value="${resource.shortname}" />
            <input type="hidden" name="id" value="${dataPackageSchema.identifier}" />
            <input type="hidden" name="schemaName" value="${dataPackageSchema.name}" />
            <input type="hidden" name="mid" value="${mid!}" />
            <input id="showAllValue" type="hidden" name="showAll" value="${Parameters.showAll!"true"}" />

            <p class="text-center"><@s.text name='manage.mapping.source.help'/></p>

            <div id="mappings">
                <div class="row">
                    <div class="col-sm-6">
                        <#if dataPackageSchema.name=="dwc-dp">
                            <@selectList name="newTableSchemas[0]" options="" objValue="name" objTitle="name" i18nkey="${dataPackageSchema.shortTitle!'manage.mapping.tableSchema'}" requiredField=true />
                        <#else>
                            <@selectList name="newTableSchemas[0]" options=dataPackageSchema.tableSchemas objValue="name" objTitle="name" i18nkey="${dataPackageSchema.shortTitle!'manage.mapping.tableSchema'}" requiredField=true />
                        </#if>
                    </div>
                    <div class="col-sm-6">
                        <@selectList name="newSources[0]" options=resource.sources objValue="name" objTitle="name" i18nkey="manage.mapping.source" requiredField=true />
                    </div>
                </div>
            </div>

            <div class="mb-4">
                <div class="addNew my-2">
                    <a id="plus-mapping" href="" class="metadata-action-link">
                        <span>
                            <svg viewBox="0 0 24 24" class="link-icon">
                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                            </svg>
                        </span>
                        <span><@s.text name="manage.mapping.add"/></span>
                    </a>
                </div>
            </div>


            <div id="baseItem" class="row item" style="display: none;">
                <div class="columnLinks mt-2 d-flex justify-content-end">
                    <a id="mapping-removeLink" href="" class="metadata-action-link removeMappingLink">
                        <span>
                            <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                            </svg>
                        </span>
                        <span><@s.text name="manage.mapping.delete"/></span>
                    </a>
                </div>
                <div class="col-sm-6">
                    <#if dataPackageSchema.name=="dwc-dp">
                        <@selectList name="newTableSchemasBase" options="" objValue="name" objTitle="name" i18nkey="${dataPackageSchema.shortTitle!'manage.mapping.tableSchema'}" requiredField=true />
                    <#else>
                        <@selectList name="newTableSchemasBase" options=dataPackageSchema.tableSchemas objValue="name" objTitle="name" i18nkey="${dataPackageSchema.shortTitle!'manage.mapping.tableSchema'}" requiredField=true />
                    </#if>
                </div>
                <div class="col-sm-6">
                    <@selectList name="newSourcesBase" options=resource.sources objValue="name" objTitle="name" i18nkey="manage.mapping.source" requiredField=true />
                </div>
            </div>
        </div>
    </div>
</form>

<#include "/WEB-INF/pages/inc/footer.ftl">
