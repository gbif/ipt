<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.MetadataAction" -->
<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <#include "/WEB-INF/pages/macros/user_id_directories.ftl"/>
    <title><@s.text name='manage.metadata.basic.title'/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>
    <#include "/WEB-INF/pages/macros/metadata_agent.ftl"/>
    <script>
        $(document).ready(function(){
            // Ensure core type cannot be changed once set (e.g., after core mapping is done)
            var resourceType = "${resource.coreType!}";
            if (resourceType !== "") {
                if (resourceType.toLowerCase() === "occurrence") {
                    $("#resource\\.coreType").val('occurrence');
                } else if (resourceType.toLowerCase() === "checklist") {
                    $("#resource\\.coreType").val('checklist');
                } else if (resourceType.toLowerCase() === "samplingevent") {
                    $("#resource\\.coreType").val('samplingevent');
                } else if (resourceType.toLowerCase() === "materialentity") {
                    $("#resource\\.coreType").val('materialentity');
                } else if (resourceType.toLowerCase() === "other") {
                    $("#resource\\.coreType").val('other');
                }
            }

            // core type selection is only disabled, if resource has core
            var hasCore="${resourceHasCore!}";
            if (hasCore === "true") {
                $("#resource\\.coreType").attr('disabled','disabled');
            }

            // publishing organisation selection is only disabled, if resource has been registered with GBIF or assigned a DOI (no matter if it's reserved or public).
            var isRegisteredWithGBIF="${resource.key!}";
            var isAssignedDOI="${resource.doi!}";
            if (isRegisteredWithGBIF !== "") {
                $("#id").attr('disabled','disabled');
            } else if (isAssignedDOI !== "") {
                $("#id").attr('disabled','disabled');
            }

            function getList(list){
                var arr=  list.split(",");
                var newlistaOccurrence={};
                for(index in arr ){
                    var val=arr[index].replace(/{|}/g,'');
                    var arr2=val.split('=');
                    var str=arr2[0].replace(/^\s*|\s*$/g,"");
                    newlistaOccurrence[str]=arr2[1];
                }
                return newlistaOccurrence;
            }
            // Populate subtype list depending on core type selected
            $("#resource\\.coreType").change(function(){
                var optionType=$("#resource\\.coreType").val();
                $("#resource\\.subtype").attr('selectedIndex', '0');
                switch(optionType)
                {
                    case 'occurrence':
                        $('#resource\\.subtype >option').remove();
                        var list=getList("${occurrenceSubtypesMap}");
                        $.each(list, function(key, value) {
                            $('#resource\\.subtype').append('<option value="'+key+'">'+value+'</option>');
                        });
                        break;
                    case 'checklist':
                        $('#resource\\.subtype >option').remove();
                        var list=getList("${checklistSubtypesMap}");
                        $.each(list, function(key, value) {
                            $('#resource\\.subtype').append('<option value="'+key+'">'+value+'</option>');
                        });
                        break;
                    case 'samplingevent':
                        $('#resource\\.subtype >option').remove();
                        var list=getList("${samplingEventSubtypesMap}");
                        $.each(list, function(key, value) {
                          $('#resource\\.subtype').append('<option value="'+key+'">'+value+'</option>');
                        });
                        break;
                    case 'other':
                        $('#resource\\.subtype >option').remove();
                        $('#resource\\.subtype').append('<option value="">No subtype</option>');
                        break;
                    default:
                        $('#resource\\.subtype >option').remove();
                        $('#resource\\.subtype').append('<option value=""></option>');
                        break;
                }
            });

            // Here down: related to intellectual rights
            function exists(value) {
                return (typeof value != 'undefined' && value);
            }

            if (exists("${eml.intellectualRights!}")) {
                $("#intellectualRightsDiv").show();
            } else {
                $("#intellectualRights").val('');
                $("#intellectualRightsDiv").hide();
            }

            $("#eml\\.intellectualRights\\.license").change(function() {
                $('.confirm').unbind('click');

                var nameRights=$("#eml\\.intellectualRights\\.license").val();
                $("#eml\\.intellectualRights\\.license").val(nameRights);

                if(nameRights) {

                    var licenseText=$("input:text#" + nameRights).val();

                    if (licenseText) {
                        $("#intellectualRightsDiv").html(licenseText);
                        $("#intellectualRightsDiv").show();
                        $("#intellectualRights").val(licenseText);
                        $("#eml\\.intellectualRights").val(licenseText);

                        $("#disclaimerRigths").css('display', '');
                    }

                } else {
                    $("#intellectualRightsDiv").html('');
                    $("#intellectualRightsDiv").hide();

                    $("#intellectualRights").val('');
                    $("#disclaimerRigths").css('display', 'none');
                    $("#eml\\.intellectualRights").val('');
                }
            });// end intellectual rights

            $('#metadata-section').change(function () {
                var metadataSection = $('#metadata-section').find(':selected').val()
                $(location).attr('href', 'metadata-' + metadataSection + '.do?r=${resource.shortname!r!}');
            });

            // scroll to the error if present
            var invalidElements = $(".is-invalid");

            if (invalidElements !== undefined && invalidElements.length > 0) {
                var invalidElement = invalidElements.first();
                var pos = invalidElement.offset().top - 100;
                // scroll to the element
                $('body, html').animate({scrollTop: pos});
            }

            function initAndGetSortable(selector) {
                return sortable(selector, {
                    forcePlaceholderSize: true,
                    placeholderClass: 'border',
                    handle: '.handle'
                });
            }

            const sortable_contacts = initAndGetSortable('#contact-items');
            const sortable_creators = initAndGetSortable('#creator-items');
            const sortable_metadataProviders = initAndGetSortable('#metadataProvider-items');
            const sortable_description = initAndGetSortable('#items');

            sortable_contacts[0].addEventListener('sortupdate', changeAgentInputNamesAfterDragging);
            sortable_creators[0].addEventListener('sortupdate', changeAgentInputNamesAfterDragging);
            sortable_metadataProviders[0].addEventListener('sortupdate', changeAgentInputNamesAfterDragging);
            sortable_description[0].addEventListener('sortupdate', function(e) {
                // recalculate names!
                displayProcessing();
                var contactItems = $("#items div.item");

                contactItems.each(function (index) {
                    var elementId = $(this)[0].id;

                    $("div#" + elementId + " textarea").attr("name", "eml.description[" + index + "]");
                });

                hideProcessing();
            });

            sortable_contacts[0].addEventListener('drag', dragScroll);
            sortable_creators[0].addEventListener('drag', dragScroll);
            sortable_metadataProviders[0].addEventListener('drag', dragScroll);
            sortable_description[0].addEventListener('drag', dragScroll);

            function dragScroll(e) {
                var cursor = e.pageY;
                var parentWindow = parent.window;
                var pixelsToTop = $(parentWindow).scrollTop();
                var screenHeight = $(parentWindow).height();

                if ((cursor - pixelsToTop) > screenHeight * 0.9) {
                    parentWindow.scrollBy(0, (screenHeight / 30));
                } else if ((cursor - pixelsToTop) < screenHeight * 0.1) {
                    parentWindow.scrollBy(0, -(screenHeight / 30));
                }
            }

            $('select#eml\\.metadataLanguage').select2({
                placeholder: '',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                theme: 'bootstrap4'
            });
            $('select#resource\\.coreType').select2({
                placeholder: '${action.getText("resource.coreType.selection")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                minimumResultsForSearch: 'Infinity',
                theme: 'bootstrap4'
            });
            $('select#id').select2({
                placeholder: '${action.getText("admin.organisation.name.select")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                minimumResultsForSearch: 15,
                theme: 'bootstrap4'
            });
            $('select#eml\\.language').select2({
                placeholder: '',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                theme: 'bootstrap4'});
            $('select#resource\\.subtype').select2({
                placeholder: '${action.getText("resource.subtype.selection")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                allowClear: true,
                minimumResultsForSearch: 'Infinity',
                theme: 'bootstrap4'
            });
            $('select#eml\\.updateFrequency').select2({
                placeholder: '',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                minimumResultsForSearch: 'Infinity',
                theme: 'bootstrap4'
            });
            $('select#eml\\.intellectualRights\\.license').select2({
                placeholder: '${action.getText("eml.intellectualRights.nolicenses")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                allowClear: true,
                minimumResultsForSearch: 'Infinity',
                theme: 'bootstrap4'
            });

            $('[id^="eml.contacts"][id$=".address.country"]').select2({
                placeholder: '${action.getText("eml.country.selection")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                allowClear: true,
                theme: 'bootstrap4'
            });
            $('[id^="eml.contacts"][id$=".directory"]').select2({
                placeholder: '${action.getText("eml.contact.noDirectory")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                minimumResultsForSearch: 'Infinity',
                allowClear: true,
                theme: 'bootstrap4'
            });
            $('[id^="eml.creators"][id$=".address.country"]').select2({
                placeholder: '${action.getText("eml.country.selection")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                allowClear: true,
                theme: 'bootstrap4'
            });
            $('[id^="eml.creators"][id$=".directory"]').select2({
                placeholder: '${action.getText("eml.contact.noDirectory")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                minimumResultsForSearch: 'Infinity',
                allowClear: true,
                theme: 'bootstrap4'
            });
            $('[id^="eml.metadataProviders"][id$=".address.country"]').select2({
                placeholder: '${action.getText("eml.country.selection")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                allowClear: true,
                theme: 'bootstrap4'
            });
            $('[id^="eml.metadataProviders"][id$=".directory"]').select2({
                placeholder: '${action.getText("eml.contact.noDirectory")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                minimumResultsForSearch: 'Infinity',
                allowClear: true,
                theme: 'bootstrap4'
            });

            function changeAgentInputNamesAfterDragging(e) {
                displayProcessing();
                var contactItems = $("#contact-items div.item");

                contactItems.each(function (index) {
                    var elementId = $(this)[0].id;

                    $("div#" + elementId + " input[id$='firstName']").attr("name", "eml.contacts[" + index + "].firstName");
                    $("div#" + elementId + " input[id$='lastName']").attr("name", "eml.contacts[" + index + "].lastName");
                    $("div#" + elementId + " input[id$='position']").attr("name", "eml.contacts[" + index + "].position");
                    $("div#" + elementId + " input[id$='organisation']").attr("name", "eml.contacts[" + index + "].organisation");
                    $("div#" + elementId + " input[id$='address']").attr("name", "eml.contacts[" + index + "].address.address");
                    $("div#" + elementId + " input[id$='city']").attr("name", "eml.contacts[" + index + "].address.city");
                    $("div#" + elementId + " input[id$='province']").attr("name", "eml.contacts[" + index + "].address.province");
                    $("div#" + elementId + " select[id$='country']").attr("name", "eml.contacts[" + index + "].address.country");
                    $("div#" + elementId + " select[id$='country']").trigger("change");
                    $("div#" + elementId + " input[id$='postalCode']").attr("name", "eml.contacts[" + index + "].address.postalCode");
                    $("div#" + elementId + " input[id$='phone']").attr("name", "eml.contacts[" + index + "].phone");
                    $("div#" + elementId + " input[id$='email']").attr("name", "eml.contacts[" + index + "].email");
                    $("div#" + elementId + " input[id$='homepage']").attr("name", "eml.contacts[" + index + "].homepage");
                    $("div#" + elementId + " select[id$='directory']").attr("name", "eml.contacts[" + index + "].userIds[0].directory");
                    $("div#" + elementId + " select[id$='directory']").trigger("change");
                    $("div#" + elementId + " input[id$='identifier']").attr("name", "eml.contacts[" + index + "].userIds[0].identifier");
                });

                hideProcessing();
            }

            var copyAgentModal = $('#copy-agent-modal');
            $('#resource').select2({
                placeholder: '${action.getText("eml.metadataAgent.copy.resource.select")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                dropdownParent: copyAgentModal,
                minimumResultsForSearch: 10,
                width: "100%",
                allowClear: true,
                theme: 'bootstrap4'
            });
            $('#agentType').select2({
                placeholder: '${action.getText("eml.metadataAgent.copy.agentType.select")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                dropdownParent: copyAgentModal,
                minimumResultsForSearch: 10,
                width: "100%",
                allowClear: true,
                theme: 'bootstrap4'
            });
            $('#agent').select2({
                placeholder: '${action.getText("eml.metadataAgent.copy.agent.select")?js_string}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                dropdownParent: copyAgentModal,
                minimumResultsForSearch: 10,
                width: "100%",
                allowClear: true,
                theme: 'bootstrap4'
            });
        });

    </script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "basic"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<form class="needs-validation" action="metadata-${section}.do" method="post" novalidate>
    <div class="container-fluid bg-body border-bottom">
        <div class="container bg-body border rounded-2 mb-4">
            <div class="container my-3 p-3">
                <div class="text-center fs-smaller">
                    <div class="text-center fs-smaller">
                        <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                            <ol class="breadcrumb justify-content-center mb-0">
                                <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                                <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                                <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.metadata"/></li>
                            </ol>
                        </nav>
                    </div>
                </div>

                <div class="text-center">
                    <h1 class="py-2 mb-0 text-gbif-header fs-2 fw-normal">
                        <@s.text name='manage.metadata.basic.title'/>
                    </h1>
                </div>

                <div class="text-center fs-smaller">
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">
                        <#if resource.title?has_content>
                            ${resource.title}
                        <#else>
                            ${resource.shortname}
                        </#if>
                    </a>
                </div>

                <div class="text-center mt-2">
                    <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/>
                    <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" name="cancel" key="button.back"/>
                </div>
            </div>
        </div>
    </div>

    <#include "metadata_section_select.ftl"/>

    <div class="container-fluid bg-body">
        <div class="container bd-layout main-content-container">
            <main class="bd-main">
                <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                    <#include "eml_sidebar.ftl"/>
                </div>

                <div class="bd-content">

                    <div class="my-md-3 p-3">

                        <p><@s.text name="manage.metadata.basic.required.message" /></p>

                        <div class="row g-3">
                            <div class="col-12">
                                <@input name="eml.title" help="i18n" requiredField=true />
                            </div>

                            <div class="col-lg-4">
                                <@select name="eml.metadataLanguage" help="i18n" options=languages value="${metadataLanguageIso3!'eng'}" requiredField=true />
                            </div>

                            <#if (resource.coreType) != "extended-occurrence-dp">
                            <div class="col-lg-4">
                                <@select name="resource.coreType" i18nkey="resource.coreType" help="i18n" options=types value="${resource.coreType!''}" requiredField=true />
                            </div>
                            </#if>

                            <div class="col-lg-4">
                                <#if resource.organisation??>
                                    <@select name="id" i18nkey="eml.publishingOrganisation" help="i18n" options=organisations value="${resource.organisation.key!''}" requiredField=true />
                                <#else>
                                    <@select name="id" i18nkey="eml.publishingOrganisation" help="i18n" options=organisations requiredField=true />
                                </#if>
                            </div>

                            <div class="col-lg-4">
                                <@select name="eml.language" help="i18n" options=languages value="${languageIso3!'eng'}" requiredField=true />
                            </div>

                            <#if (resource.coreType) != "extended-occurrence-dp">
                            <div class="col-lg-4">
                                <@select name="resource.subtype" i18nkey="resource.subtype" help="i18n" options=listSubtypes value="${resource.subtype!''}" />
                            </div>
                            </#if>

                            <div class="col-lg-4">
                                <@select name="eml.updateFrequency" i18nkey="eml.updateFrequency" help="i18n" options=frequencies value="${eml.updateFrequency.identifier!'unkown'}" requiredField=true />
                            </div>

                            <!-- Intellectual Rights -->
                            <div class="col-12">
                                <@select name="eml.intellectualRights.license" i18nkey="eml.intellectualRights.license" help="i18n" options=licenses value="${licenseKeySelected!}" requiredField=true/>

                                <div id="intellectualRightsDiv" class="mt-3 p-3">
                                    <@licenseLogoClass eml.intellectualRights!/>

                                    <#if eml.intellectualRights?has_content>
                                        <#if eml.intellectualRights.contains("CC-BY-NC")>
                                            <#noescape><@s.text name='eml.intellectualRights.licence.ccbync'/></#noescape>
                                        <#elseif eml.intellectualRights.contains("CC-BY")>
                                            <#noescape><@s.text name='eml.intellectualRights.licence.ccby'/></#noescape>
                                        <#elseif eml.intellectualRights.contains("CC0")>
                                            <#noescape><@s.text name='eml.intellectualRights.licence.cczero'/></#noescape>
                                        <#else>
                                            <#noescape>${eml.intellectualRights!}</#noescape>
                                        </#if>
                                    </#if>
                                </div>
                                <!-- internal parameter -->
                                <input id="eml.intellectualRights" name="eml.intellectualRights" type="hidden" value="${eml.intellectualRights!}" />

                                <!-- Hidden inputs storing license texts used in populating ipr textarea when a different license gets selected -->
                                <#list licenseTexts?keys as k>
                                    <input type="text" id="${k}" value="${licenseTexts[k]}" style="display: none"/>
                                </#list>

                                <div id='disclaimerRigths' style='display: none'>
                                    <p class="mt-3">
                                        <@s.text name='eml.intellectualRights.license.disclaimer'/>
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="my-3 p-3">
                        <!-- Descriptions, broken into one or more paragraphs -->
                        <@textinline name="eml.description" help="i18n" requiredField=true/>
                        <div id="items">
                            <#list eml.description as item>
                                <div id="item-${item_index}" class="handle item pb-4 border-bottom">
                                    <div class="handle columnLinks my-2 d-flex justify-content-end">
                                        <a id="removeLink-${item_index}" class="removeLink metadata-action-link mt-1" href="">
                                            <span>
                                                <svg viewBox="0 0 24 24" class="link-icon">
                                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                </svg>
                                            </span>
                                            <span><@s.text name='manage.metadata.removethis'/> <@s.text name='eml.description.item'/></span>
                                        </a>
                                    </div>
                                    <@simpleText name="eml.description[${item_index}]" minlength=5 requiredField=true></@simpleText>
                                </div>
                            </#list>
                        </div>
                        <div class="addNew my-2">
                            <a id="plus" href="" class="metadata-action-link">
                                <span>
                                    <svg viewBox="0 0 24 24" class="link-icon">
                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                    </svg>
                                </span>
                                <span><@s.text name='manage.metadata.addnew'/> <@s.text name='eml.description.item'/></span>
                            </a>
                        </div>

                        <div id="baseItem" class="item pb-4 border-bottom" style="display:none;">
                            <div class="handle columnLinks my-2 d-flex justify-content-end">
                                <a id="removeLink" class="removeLink metadata-action-link" href="">
                                    <span>
                                        <svg viewBox="0 0 24 24" class="link-icon">
                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                        </svg>
                                    </span>
                                    <span><@s.text name='manage.metadata.removethis'/> <@s.text name='eml.description.item'/></span>
                                </a>
                            </div>
                            <@simpleText name=""/>
                        </div>

                        <!-- retrieve some link names one time -->
                        <#assign copyLink><@s.text name="eml.metadataAgent.copyLink"/></#assign>
                        <#assign copyFromAnotherLink><@s.text name="eml.metadataAgent.copyFromAnother"/></#assign>
                        <#assign removeContactLink><@s.text name='manage.metadata.removethis'/> <@s.text name='eml.contact'/></#assign>
                        <#assign removeCreatorLink><@s.text name='manage.metadata.removethis'/> <@s.text name='portal.resource.creator'/></#assign>
                        <#assign removeMetadataProviderLink><@s.text name='manage.metadata.removethis'/> <@s.text name='eml.metadataProvider'/></#assign>
                        <#assign addContactLink><@s.text name='manage.metadata.addnew'/> <@s.text name='eml.contact'/></#assign>
                        <#assign addCreatorLink><@s.text name='manage.metadata.addnew'/> <@s.text name='portal.resource.creator'/></#assign>
                        <#assign addMetadataProviderLink><@s.text name='manage.metadata.addnew'/> <@s.text name='eml.metadataProvider'/></#assign>
                    </div>

                    <div class="my-3 p-3">
                        <!-- Resource Contacts -->
                        <@textinline name="eml.contact.plural" help="i18n" requiredField=true/>
                        <div id="contact-items">
                            <#list eml.contacts as contact>
                                <div id="contact-item-${contact_index}" class="item row g-3 pb-4 border-bottom">
                                    <div class="handle columnLinks mt-4 d-flex justify-content-between">
                                        <div>
                                            <a id="contact-copy-${contact_index}" href="" class="metadata-action-link">
                                                <span>
                                                    <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                        <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                    </svg>
                                                </span>
                                                <span>${copyFromAnotherLink?lower_case?cap_first}</span>
                                            </a>
                                        </div>
                                        <div>
                                            <a id="contact-removeLink-${contact_index}" class="removeContactLink metadata-action-link" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                    </svg>
                                                </span>
                                                <span>${removeContactLink?lower_case?cap_first}</span>
                                            </a>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.contacts[${contact_index}].firstName" i18nkey="eml.contact.firstName"/>
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.contacts[${contact_index}].lastName" i18nkey="eml.contact.lastName" requiredField=true/>
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.contacts[${contact_index}].position" i18nkey="eml.contact.position" requiredField=true />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.contacts[${contact_index}].organisation" i18nkey="eml.contact.organisation" requiredField=true />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.contacts[${contact_index}].address.address" i18nkey="eml.contact.address.address" />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.contacts[${contact_index}].address.city" i18nkey="eml.contact.address.city" />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.contacts[${contact_index}].address.province" i18nkey="eml.contact.address.province" />
                                    </div>
                                    <div class="countryList col-md-6">
                                        <@select name="eml.contacts[${contact_index}].address.country" help="i18n" options=countries i18nkey="eml.contact.address.country" value="${eml.contacts[contact_index].address.country!}"/>
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.contacts[${contact_index}].address.postalCode" i18nkey="eml.contact.address.postalCode" />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.contacts[${contact_index}].phone" i18nkey="eml.contact.phone" />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.contacts[${contact_index}].email" i18nkey="eml.contact.email" />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.contacts[${contact_index}].homepage" i18nkey="eml.contact.homepage" type="url" />
                                    </div>
                                    <div class="col-md-6">
                                        <#if (eml.contacts[contact_index].userIds[0].directory)??>
                                            <@select name="eml.contacts[${contact_index}].userIds[0].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" value="${userIdDirecotriesExtended[eml.contacts[contact_index].userIds[0].directory!]!}"/>
                                        <#else>
                                            <@select name="eml.contacts[${contact_index}].userIds[0].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" value=""/>
                                        </#if>
                                    </div>
                                    <div class="col-md-6">
                                        <#if eml.contacts[contact_index].userIds[0]??>
                                            <@input name="eml.contacts[${contact_index}].userIds[0].identifier" help="i18n" i18nkey="eml.contact.identifier" value="${eml.contacts[contact_index].userIds[0].identifier!}"/>
                                        <#else>
                                            <@input name="eml.contacts[${contact_index}].userIds[0].identifier" help="i18n" i18nkey="eml.contact.identifier" value=""/>
                                        </#if>
                                    </div>
                                </div>
                            </#list>
                        </div>

                        <div class="addNew my-2">
                            <a id="plus-contact" href="" class="metadata-action-link">
                                <span>
                                    <svg viewBox="0 0 24 24" class="link-icon">
                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                    </svg>
                                </span>
                                <span>${addContactLink?lower_case?cap_first}</span>
                            </a>
                        </div>

                        <div id="baseItem-contact" class="item row g-3 pb-4 border-bottom" style="display:none;">
                            <div class="handle columnLinks mt-4 d-flex justify-content-between">
                                <div>
                                    <a id="contact-copy" href="" class="metadata-action-link">
                                        <span>
                                            <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                            </svg>
                                        </span>
                                        <span>${copyFromAnotherLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                                <div class="text-end">
                                    <a id="contact-removeLink" class="removeContactLink metadata-action-link" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                            </svg>
                                        </span>
                                        <span>${removeContactLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.contact.firstName" i18nkey="eml.contact.firstName"/>
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.contact.lastName" i18nkey="eml.contact.lastName" requiredField=true/>
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.contact.position" i18nkey="eml.contact.position" requiredField=true />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.contact.organisation" i18nkey="eml.contact.organisation" requiredField=true />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.contact.address.address" i18nkey="eml.contact.address.address" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.contact.address.city" i18nkey="eml.contact.address.city" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.contact.address.province" i18nkey="eml.contact.address.province" />
                            </div>
                            <div class="countryList col-md-6">
                                <@select name="country" options=countries help="i18n" i18nkey="eml.contact.address.country" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.contact.address.postalCode" i18nkey="eml.contact.address.postalCode" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.contact.phone" i18nkey="eml.contact.phone" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.contact.email" i18nkey="eml.contact.email" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.contact.homepage" i18nkey="eml.contact.homepage" type="url" />
                            </div>
                            <div class="col-md-6">
                                <@select name="eml.contact.userId.directory" options=userIdDirectories help="i18n" i18nkey="eml.contact.directory" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.contact.userId.identifier" help="i18n" i18nkey="eml.contact.identifier" />
                            </div>
                        </div>
                    </div>

                    <div class="my-3 p-3">
                        <!-- Resource Creators -->
                        <@textinline name="eml.resourceCreator.plural" help="i18n" requiredField=true/>
                        <div id="creator-items">
                            <#list eml.creators as creator>
                                <div id="creator-item-${creator_index}" class="item row g-3 pb-4 border-bottom">
                                    <div class="handle columnLinks mt-4 d-flex justify-content-between">
                                        <div>
                                            <div class="btn-group">
                                                <a id="dropdown-creator-copy-${creator_index}" href="#" class="metadata-action-link dropdown-toggle" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                            <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                        </svg>
                                                    </span>
                                                    <span>${copyLink?lower_case?cap_first}</span>
                                                </a>

                                                <ul class="dropdown-menu" aria-labelledby="dropdown-creator-copy-${creator_index}">
                                                    <li><a id="creator-from-contact-${creator_index}" class="dropdown-item menu-link w-100 dropdown-button fs-smaller-2" href="#"><@s.text name="eml.metadataAgent.fromContact"/></a></li>
                                                    <li><a id="creator-copy-${creator_index}" class="dropdown-item menu-link w-100 dropdown-button fs-smaller-2" href="#"><@s.text name="eml.metadataAgent.fromAnother"/></a></li>
                                                </ul>
                                            </div>
                                        </div>
                                        <div class="text-end">
                                            <a id="creator-removeLink-${creator_index}" class="removeCreatorLink metadata-action-link" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                    </svg>
                                                </span>
                                                <span>${removeCreatorLink?lower_case?cap_first}</span>
                                            </a>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.creators[${creator_index}].firstName" i18nkey="eml.resourceCreator.firstName"/>
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.creators[${creator_index}].lastName" i18nkey="eml.resourceCreator.lastName" requiredField=true/>
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.creators[${creator_index}].position" i18nkey="eml.resourceCreator.position" requiredField=true />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.creators[${creator_index}].organisation" i18nkey="eml.resourceCreator.organisation" requiredField=true />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.creators[${creator_index}].address.address" i18nkey="eml.resourceCreator.address.address" />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.creators[${creator_index}].address.city" i18nkey="eml.resourceCreator.address.city" />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.creators[${creator_index}].address.province" i18nkey="eml.resourceCreator.address.province" />
                                    </div>
                                    <div class="countryList col-md-6">
                                        <@select name="eml.creators[${creator_index}].address.country" help="i18n" options=countries i18nkey="eml.resourceCreator.address.country" value="${eml.creators[creator_index].address.country!}"/>
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.creators[${creator_index}].address.postalCode" i18nkey="eml.resourceCreator.address.postalCode" />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.creators[${creator_index}].phone" i18nkey="eml.resourceCreator.phone" />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.creators[${creator_index}].email" i18nkey="eml.resourceCreator.email" />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.creators[${creator_index}].homepage" i18nkey="eml.resourceCreator.homepage" type="url" />
                                    </div>
                                    <div class="col-md-6">
                                        <#if eml.creators[creator_index].userIds[0]??>
                                            <@select name="eml.creators[${creator_index}].userIds[0].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" value="${userIdDirecotriesExtended[eml.creators[creator_index].userIds[0].directory!]!}"/>
                                        <#else>
                                            <@select name="eml.creators[${creator_index}].userIds[0].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" value=""/>
                                        </#if>
                                    </div>
                                    <div class="col-md-6">
                                        <#if eml.creators[creator_index].userIds[0]??>
                                            <@input name="eml.creators[${creator_index}].userIds[0].identifier" help="i18n" i18nkey="eml.contact.identifier" value="${eml.creators[creator_index].userIds[0].identifier!}"/>
                                        <#else>
                                            <@input name="eml.creators[${creator_index}].userIds[0].identifier" help="i18n" i18nkey="eml.contact.identifier" value=""/>
                                        </#if>
                                    </div>
                                </div>
                            </#list>
                        </div>

                        <div class="addNew my-2">
                            <a id="plus-creator" href="" class="metadata-action-link">
                                <span>
                                    <svg viewBox="0 0 24 24" class="link-icon">
                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                    </svg>
                                </span>
                                <span>${addCreatorLink?lower_case?cap_first}</span>
                            </a>
                        </div>

                        <div id="baseItem-creator" class="item row g-3 pb-4 border-bottom" style="display:none;">
                            <div class="handle columnLinks mt-4 d-flex justify-content-between">
                                <div>
                                    <div class="btn-group">
                                        <a id="dropdown-creator-copy" href="#" class="metadata-action-link dropdown-toggle" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                            <span>
                                                <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                    <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                </svg>
                                            </span>
                                            <span>${copyLink?lower_case?cap_first}</span>
                                        </a>
                                        <ul class="dropdown-menu" aria-labelledby="dropdown-creator-copy">
                                            <li><a id="creator-from-contact" class="dropdown-item menu-link w-100 dropdown-button fs-smaller-2" href="#"><@s.text name="eml.metadataAgent.fromContact"/></a></li>
                                            <li><a id="creator-copy" class="dropdown-item menu-link w-100 dropdown-button fs-smaller-2" href="#"><@s.text name="eml.metadataAgent.fromAnother"/></a></li>
                                        </ul>
                                    </div>
                                </div>
                                <div class="text-end">
                                    <a id="creator-removeLink" class="removeCreatorLink metadata-action-link" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                            </svg>
                                        </span>
                                        <span>${removeCreatorLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.creator.firstName" i18nkey="eml.resourceCreator.firstName"/>
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.creator.lastName" i18nkey="eml.resourceCreator.lastName" requiredField=true/>
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.creator.position" i18nkey="eml.resourceCreator.position" requiredField=true />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.creator.organisation" i18nkey="eml.resourceCreator.organisation" requiredField=true />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.creator.address.address" i18nkey="eml.resourceCreator.address.address" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.creator.address.city" i18nkey="eml.resourceCreator.address.city" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.creator.address.province" i18nkey="eml.resourceCreator.address.province" />
                            </div>
                            <div class="countryList col-md-6">
                                <@select name="country" options=countries help="i18n" i18nkey="eml.resourceCreator.address.country" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.creator.address.postalCode" i18nkey="eml.resourceCreator.address.postalCode" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.creator.phone" i18nkey="eml.resourceCreator.phone" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.creator.email" i18nkey="eml.resourceCreator.email" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.creator.homepage" i18nkey="eml.resourceCreator.homepage" type="url" />
                            </div>
                            <div class="col-md-6">
                                <@select name="eml.creator.userId.directory" options=userIdDirectories help="i18n" i18nkey="eml.contact.directory" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.creator.userId.identifier" help="i18n" i18nkey="eml.contact.identifier" />
                            </div>
                        </div>
                    </div>

                    <div class="my-3 p-3">
                        <!-- Metadata Providers -->
                        <@textinline name="eml.metadataProvider.plural" help="i18n"/>
                        <div id="metadataProvider-items">
                            <#list eml.metadataProviders as metadataProvider>
                                <div id="metadataProvider-item-${metadataProvider_index}" class="item row g-3 pb-4 border-bottom">
                                    <div class="handle columnLinks mt-4 d-flex justify-content-between">
                                        <div>
                                            <div class="btn-group">
                                                <a id="dropdown-metadataProvider-copy-${metadataProvider_index}" href="#" class="metadata-action-link dropdown-toggle" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                            <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                        </svg>
                                                    </span>
                                                    <span>${copyLink?lower_case?cap_first}</span>
                                                </a>
                                                <ul class="dropdown-menu" aria-labelledby="dropdown-metadataProvider-copy-${metadataProvider_index}">
                                                    <li><a id="metadataProvider-from-contact-${metadataProvider_index}" class="dropdown-item menu-link w-100 dropdown-button fs-smaller-2" href="#"><@s.text name="eml.metadataAgent.fromContact"/></a></li>
                                                    <li><a id="metadataProvider-copy-${metadataProvider_index}" class="dropdown-item menu-link w-100 dropdown-button fs-smaller-2" href="#"><@s.text name="eml.metadataAgent.fromAnother"/></a></li>
                                                </ul>
                                            </div>
                                        </div>
                                        <div class="text-end">
                                            <a id="metadataProvider-removeLink-${metadataProvider_index}" class="removeMetadataProviderLink metadata-action-link" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                    </svg>
                                                </span>
                                                <span>${removeMetadataProviderLink?lower_case?cap_first}</span>
                                            </a>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.metadataProviders[${metadataProvider_index}].firstName" i18nkey="eml.metadataProvider.firstName"/>
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.metadataProviders[${metadataProvider_index}].lastName" i18nkey="eml.metadataProvider.lastName" requiredField=true/>
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.metadataProviders[${metadataProvider_index}].position" i18nkey="eml.metadataProvider.position" requiredField=true />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.metadataProviders[${metadataProvider_index}].organisation" i18nkey="eml.metadataProvider.organisation" requiredField=true />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.metadataProviders[${metadataProvider_index}].address.address" i18nkey="eml.metadataProvider.address.address" />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.metadataProviders[${metadataProvider_index}].address.city" i18nkey="eml.metadataProvider.address.city" />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.metadataProviders[${metadataProvider_index}].address.province" i18nkey="eml.metadataProvider.address.province" />
                                    </div>
                                    <div class="countryList col-md-6">
                                        <@select name="eml.metadataProviders[${metadataProvider_index}].address.country" help="i18n" options=countries i18nkey="eml.metadataProvider.address.country" value="${eml.metadataProviders[metadataProvider_index].address.country!}"/>
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.metadataProviders[${metadataProvider_index}].address.postalCode" i18nkey="eml.metadataProvider.address.postalCode" />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.metadataProviders[${metadataProvider_index}].phone" i18nkey="eml.metadataProvider.phone" />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.metadataProviders[${metadataProvider_index}].email" i18nkey="eml.metadataProvider.email" />
                                    </div>
                                    <div class="col-md-6">
                                        <@input name="eml.metadataProviders[${metadataProvider_index}].homepage" i18nkey="eml.metadataProvider.homepage" type="url" />
                                    </div>
                                    <div class="col-md-6">
                                        <#if eml.metadataProviders[metadataProvider_index].userIds[0]??>
                                            <@select name="eml.metadataProviders[${metadataProvider_index}].userIds[0].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" value="${userIdDirecotriesExtended[eml.metadataProviders[metadataProvider_index].userIds[0].directory!]!}"/>
                                        <#else>
                                            <@select name="eml.metadataProviders[${metadataProvider_index}].userIds[0].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" value=""/>
                                        </#if>
                                    </div>
                                    <div class="col-md-6">
                                        <#if eml.metadataProviders[metadataProvider_index].userIds[0]??>
                                            <@input name="eml.metadataProviders[${metadataProvider_index}].userIds[0].identifier" help="i18n" i18nkey="eml.contact.identifier" value="${eml.metadataProviders[metadataProvider_index].userIds[0].identifier!}"/>
                                        <#else>
                                            <@input name="eml.metadataProviders[${metadataProvider_index}].userIds[0].identifier" help="i18n" i18nkey="eml.contact.identifier" value=""/>
                                        </#if>
                                    </div>
                                </div>
                            </#list>
                        </div>

                        <div class="addNew my-2">
                            <a id="plus-metadataProvider" href="" class="metadata-action-link">
                                <span>
                                    <svg viewBox="0 0 24 24" class="link-icon">
                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                    </svg>
                                </span>
                                <span>${addMetadataProviderLink?lower_case?cap_first}</span>
                            </a>
                        </div>

                        <div id="baseItem-metadataProvider" class="item row g-3 pb-4 border-bottom" style="display:none;">
                            <div class="handle columnLinks mt-4 d-flex justify-content-between">
                                <div>
                                    <div class="btn-group">
                                        <a id="dropdown-metadataProvider-copy" href="#" class="metadata-action-link dropdown-toggle" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                            <span>
                                                <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                    <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                </svg>
                                            </span>
                                            <span>${copyLink?lower_case?cap_first}</span>
                                        </a>
                                        <ul class="dropdown-menu" aria-labelledby="metadataProvider-creator-copy">
                                            <li><a id="metadataProvider-from-contact" class="dropdown-item menu-link w-100 dropdown-button fs-smaller-2" href="#"><@s.text name="eml.metadataAgent.fromContact"/></a></li>
                                            <li><a id="metadataProvider-copy" class="dropdown-item menu-link w-100 dropdown-button fs-smaller-2" href="#"><@s.text name="eml.metadataAgent.fromAnother"/></a></li>
                                        </ul>
                                    </div>
                                </div>
                                <div class="text-end">
                                    <a id="metadataProvider-removeLink" class="removeMetadataProviderLink metadata-action-link" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                            </svg>
                                        </span>
                                        <span>${removeMetadataProviderLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.metadataProvider.firstName" i18nkey="eml.metadataProvider.firstName"/>
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.metadataProvider.lastName" i18nkey="eml.metadataProvider.lastName" requiredField=true/>
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.metadataProvider.position" i18nkey="eml.metadataProvider.position" requiredField=true />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.metadataProvider.organisation" i18nkey="eml.metadataProvider.organisation" requiredField=true />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.metadataProvider.address.address" i18nkey="eml.metadataProvider.address.address" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.metadataProvider.address.city" i18nkey="eml.metadataProvider.address.city" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.metadataProvider.address.province" i18nkey="eml.metadataProvider.address.province" />
                            </div>
                            <div class="countryList col-md-6">
                                <@select name="country" options=countries help="i18n" i18nkey="eml.metadataProvider.address.country" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.metadataProvider.address.postalCode" i18nkey="eml.metadataProvider.address.postalCode" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.metadataProvider.phone" i18nkey="eml.metadataProvider.phone" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.metadataProvider.email" i18nkey="eml.metadataProvider.email" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.metadataProvider.homepage" i18nkey="eml.metadataProvider.homepage" type="url" />
                            </div>
                            <div class="col-md-6">
                                <@select name="eml.metadataProvider.userId.directory" options=userIdDirectories help="i18n" i18nkey="eml.contact.directory" />
                            </div>
                            <div class="col-md-6">
                                <@input name="eml.metadataProvider.userId.identifier" help="i18n" i18nkey="eml.contact.identifier" />
                            </div>
                        </div>

                        <!-- internal parameter -->
                        <input name="r" type="hidden" value="${resource.shortname}" />
                    </div>
                </div>
            </main>
        </div>
    </div>
</form>

    <div id="copy-agent-modal" class="modal fade" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content" style="text-align: left !important;">
                <div class="modal-header flex-column">
                    <h5 class="modal-title w-100" id="staticBackdropLabel"><@s.text name="eml.metadataAgent.copy"/></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">×</button>
                </div>
                <div class="modal-body">
                    <div class="row g-3">
                        <div class="col-12">
                            <label for="resource" class="form-label">
                                <@s.text name="eml.metadataAgent.copy.resource"/>
                            </label>
                            <select name="resource" id="resource" size="1" class="form-select">
                                <option value=""></option>
                            </select>
                        </div>
                        <div class="col-12">
                            <label for="agentType" class="form-label">
                                <@s.text name="eml.metadataAgent.copy.agentType"/>
                            </label>
                            <select name="agentType" id="agentType" size="1" class="form-select">
                                <option value=""></option>
                                <option value="creators"><@s.text name="eml.metadataAgent.copy.agentType.creator"/></option>
                                <option value="contacts"><@s.text name="eml.metadataAgent.copy.agentType.contact"/></option>
                                <option value="metadataProviders"><@s.text name="eml.metadataAgent.copy.agentType.metadataProvider"/></option>
                                <option value="associatedParties"><@s.text name="eml.metadataAgent.copy.agentType.associatedParty"/></option>
                                <option value="projectPersonnel"><@s.text name="eml.metadataAgent.copy.agentType.projectPersonnel"/></option>
                            </select>
                        </div>
                        <div class="col-12">
                            <label for="agent" class="form-label">
                                <@s.text name="eml.metadataAgent.copy.agent"/>
                            </label>
                            <select name="agent" id="agent" size="1" class="form-select">
                                <option value=""></option>
                            </select>
                        </div>
                        <div class="text-center">
                            <button id="copy-agent-button" type="button" class="btn btn-outline-gbif-primary" style="display: none;"><@s.text name="button.copy"/></button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
