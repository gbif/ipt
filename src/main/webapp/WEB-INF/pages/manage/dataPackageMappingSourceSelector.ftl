<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.DataPackageMappingAction" -->

<#escape x as x?html>
<#include "/WEB-INF/pages/inc/header.ftl">
<title><@s.text name='manage.mapping.title'/></title>
<#assign currentMenu = "manage"/>
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
<link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
<script src="${baseURL}/js/select2/select2-4.0.13.full.min.js"></script>

<script>
    $(document).ready(function () {
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
            console.log($("#mappings .item"))
            console.log($("#mappings .item").length)
            return $("#mappings .item").length;
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

            console.log("mapping items: " + mappingItems)
            setMappingItemIndex(newItem, mappingItems);
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
                    console.log("#mappings .item")
                    console.log(index)
                    console.log($(this))
                    setMappingItemIndex($(this), index);
                });
                // console.log(mappingItems)
                // calculateNumberOfItems("mapping");
                // console.log(mappingItems)

            });
        }

        function setMappingItemIndex(item, index) {
            item.attr("id", "mapping-item-" + index);

            $("#mapping-item-" + index + " [id^='mapping-removeLink']").attr("id", "mapping-removeLink-" + index);
            $("#mapping-removeLink-" + index).click(function (event) {
                removeMappingItem(event);
            });

            console.log(index)
            console.log($("#mapping-item-" + index + " [id^='additionalTableSchema']"))
            $("#mapping-item-" + index + " [id^='additionalTableSchema']").attr("id", "additionalTableSchema[" + index + "]").attr("name", function () {
                return $(this).attr("id");
            });
            $("#mapping-item-" + index + " [for^='additionalTableSchema']").attr("for", "additionalTableSchema[" + index + "]");

            console.log(index)
            console.log($("#mapping-item-" + index + " [id^='source']"))
            $("#mapping-item-" + index + " [id^='source']").attr("id", "source[" + index + "]").attr("name", function () {
                return $(this).attr("id");
            });
            $("#mapping-item-" + index + " [for^='source']").attr("for", "source[" + index + "]");
        }
    });
</script>
<#include "/WEB-INF/pages/inc/menu.ftl">
<#include "/WEB-INF/pages/macros/forms.ftl"/>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<form class="topForm" action="dataPackageMapping.do" method="post">
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
                        <@selectList name="tableSchema" options=dataPackageSchema.tableSchemas objValue="name" objTitle="name" i18nkey="${dataPackageSchema.shortTitle!'manage.mapping.tableSchema'}" requiredField=true />
                    </div>
                    <div class="col-sm-6">
                        <@selectList name="source" options=resource.sources objValue="name" objTitle="name" i18nkey="manage.mapping.source" requiredField=true />
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
                        <span>Add mapping</span>
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
                        <span>Remove mapping</span>
                    </a>
                </div>
                <div class="col-sm-6">
                    <@selectList name="additionalTableSchema" options=dataPackageSchema.tableSchemas objValue="name" objTitle="name" i18nkey="${dataPackageSchema.shortTitle!'manage.mapping.tableSchema'}" requiredField=true />
                </div>
                <div class="col-sm-6">
                    <@selectList name="source" options=resource.sources objValue="name" objTitle="name" i18nkey="manage.mapping.source" requiredField=true />
                </div>
            </div>
        </div>
    </div>
</form>

<#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
