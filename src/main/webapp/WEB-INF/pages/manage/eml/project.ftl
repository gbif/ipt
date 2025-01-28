<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>
    <title><@s.text name='manage.metadata.project.title'/></title>
    <script>
        $(document).ready(function () {
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

            // reordering
            function initAndGetSortable(selector) {
                return sortable(selector, {
                    forcePlaceholderSize: true,
                    placeholderClass: 'border',
                    handle: '.handle'
                });
            }

            const sortable_temporals = initAndGetSortable('#personnel-items');

            sortable_temporals[0].addEventListener('sortupdate', changeInputNamesAfterDragging);
            sortable_temporals[0].addEventListener('drag', dragScroll);

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

            function changeInputNamesAfterDragging(e) {
                displayProcessing();
                var contactItems = $("#personnel-items div.item");

                contactItems.each(function (index) {
                    var elementId = $(this)[0].id;

                    $("div#" + elementId + " input[id$='firstName']").attr("name", "eml.project.personnel[" + index + "].firstName");
                    $("div#" + elementId + " input[id$='lastName']").attr("name", "eml.project.personnel[" + index + "].lastName");
                    $("div#" + elementId + " select[id$='directory']").attr("name", "eml.project.personnel[" + index + "].userIds[0].directory");
                    $("div#" + elementId + " input[id$='identifier']").attr("name", "eml.project.personnel[" + index + "].userIds[0].identifier");
                    $("div#" + elementId + " select[id$='role']").attr("name", "eml.project.personnel[" + index + "].role");
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

            $('[id^="eml.project.personnel"][id$=".role"]').select2({
                placeholder: '${action.getText("eml.agent.role.selection")?js_string}',
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
            $('[id^="eml.project.personnel"][id$=".directory"]').select2({
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

            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>
    <style>
        .popover {
            width: 50%;
            max-width: 600px;
        }

        .form-control, .form-select {
            min-height: calc(1.5em + .5rem + 2px);
            padding: .25rem .5rem;
            font-size: .875rem;
            border-radius: .2rem;
        }

        .select2-container--bootstrap4 .select2-selection--single {
            height: calc(1.5em + .5rem + 2px) !important;
            font-size: .875rem !important;
        }

        .select2-container--bootstrap4 .select2-selection--single .select2-selection__rendered {
            line-height: calc(1.5em + .5rem) !important;
        }

        .select2-container--bootstrap4 .select2-selection--single .select2-selection__placeholder {
            line-height: calc(1.5em + .5rem) !important;
        }

        .select2-container--bootstrap4 .select2-selection__clear {
            margin-top: .625em !important;
        }

        .select2-results__option, .select2-search__field {
            font-size: .875rem;
        }
    </style>
    <#assign currentMetadataPage = "project"/>
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>
    <#include "/WEB-INF/pages/macros/user_id_directories.ftl"/>
    <#include "/WEB-INF/pages/macros/metadata_agent.ftl"/>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form id="project-form" class="needs-validation" action="metadata-${section}.do" method="post" novalidate>
        <div class="container-fluid bg-body border-bottom">
            <div class="container bg-body border rounded-2 mb-4">
                <div class="container my-3 p-3">
                    <div class="text-center fs-smaller">
                        <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                            <ol class="breadcrumb justify-content-center mb-0">
                                <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                                <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                                <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.metadata"/></li>
                            </ol>
                        </nav>
                    </div>

                    <div class="text-center">
                        <h1 class="py-2 mb-0 text-gbif-header fs-2 fw-normal">
                            <@s.text name='manage.metadata.project.title'/>
                        </h1>
                    </div>

                    <div class="text-center fs-smaller">
                        <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                    </div>

                    <div class="text-center mt-2">
                        <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save" />
                        <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" name="cancel" key="button.back" />
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
                            <p class="mb-0">
                                <@s.text name='manage.metadata.project.intro'/>
                            </p>

                            <!-- retrieve some link names one time -->
                            <#assign copyLink><@s.text name="eml.metadataAgent.copyLink"/></#assign>
                            <#assign removeLink><@s.text name='manage.metadata.removethis'/> <@s.text name='rtf.project.personnel'/></#assign>
                            <#assign removeRelatedProjectLink><@s.text name='manage.metadata.removethis'/> <@s.text name='rtf.project.relatedProject'/></#assign>
                            <#assign removeAwardLink><@s.text name='manage.metadata.removethis'/> <@s.text name='rtf.project.award'/></#assign>
                            <#assign addLink><@s.text name='manage.metadata.addnew'/> <@s.text name='rtf.project.personnel'/></#assign>
                            <#assign addRelatedProjectLink><@s.text name='manage.metadata.addnew'/> <@s.text name='rtf.project.relatedProject'/></#assign>
                            <#assign addAwardLink><@s.text name='manage.metadata.addnew'/> <@s.text name='rtf.project.award'/></#assign>

                            <div class="row g-3 mt-1 mb-2">
                                <@input name="eml.project.title" requiredField=true/>
                                <@input name="eml.project.identifier" help="i18n"/>
                                <@text name="eml.project.description" help="i18n"/>
                                <@text name="eml.project.funding" help="i18n"/>
                                <@text name="eml.project.studyAreaDescription.descriptorValue" help="i18n" />
                                <@text name="eml.project.designDescription" help="i18n" />
                            </div>

                        </div>

                        <div class="my-md-3 p-3">
                            <!-- List of project awards -->
                            <div class="listBlock">
                                <@textinline name="eml.project.award" help="i18n"/>

                                <div id="award-items">
                                    <#list eml.project.awards as item>
                                        <div id="award-item-${item_index}" class="item row g-3 border-bottom pb-3 mt-1">
                                            <div class="handle columnLinks mt-2 d-flex justify-content-end">
                                                <div>
                                                    <a id="award-removeLink-${item_index}" class="removeAwardlLink metadata-action-link" href="">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${removeAwardLink?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                            </div>
                                            <div class="col-12">
                                                <@input name="eml.project.awards[" + item_index + "].title" help="i18n" i18nkey="eml.project.award.title" requiredField=true/>
                                            </div>
                                            <div class="col-lg-4">
                                                <@input name="eml.project.awards[" + item_index + "].funderName" help="i18n" i18nkey="eml.project.award.funderName" requiredField=true />
                                            </div>
                                            <div class="col-lg-4">
                                                <@input name="eml.project.awards[" + item_index + "].awardNumber" help="i18n" i18nkey="eml.project.award.awardNumber" />
                                            </div>
                                            <div class="col-lg-4">
                                                <@input name="eml.project.awards[" + item_index + "].awardUrl" help="i18n" i18nkey="eml.project.award.awardUrl" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="eml.project.awards[" + item_index + "].funderIdentifiers[0]" help="i18n" i18nkey="eml.project.award.funderIdentifier" />
                                            </div>
                                        </div>
                                    </#list>
                                </div>

                                <div class="addNew col-12 mt-2">
                                    <a id="plus-award" href="" class="metadata-action-link">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span>${addAwardLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>
                        </div>

                        <div class="my-md-3 p-3">
                            <!-- List of related projects -->
                            <div class="listBlock">
                                <@textinline name="eml.project.relatedProjects" help="i18n"/>

                                <div id="relatedProjects-items">
                                    <#list eml.project.relatedProjects as item>
                                        <div id="relatedProject-item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="handle columnLinks mt-2 d-flex justify-content-end">
                                                <div>
                                                    <a id="relatedProject-removeLink-${item_index}" class="removeRelatedProjectLink metadata-action-link" href="">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${removeRelatedProjectLink?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                            </div>
                                            <div class="col-12">
                                                <@input name="eml.project.relatedProjects[" + item_index + "].title"  i18nkey="eml.project.relatedProject.title" requiredField=true/>
                                            </div>
                                            <div class="col-12">
                                                <@input name="eml.project.relatedProjects[" + item_index + "].identifier"  i18nkey="eml.project.relatedProject.identifier" />
                                            </div>
                                            <div class="col-12">
                                                <@text name="eml.project.relatedProjects[" + item_index + "].description"  i18nkey="eml.project.relatedProject.description" />
                                            </div>
                                            <div class="col-12">
                                                <div id="relatedProject-${item_index}-personnel">
                                                    <div class="d-flex text-smaller">
                                                        <label for="eml.project.relatedProject.personnel" class="form-label mb-0">
                                                            <@s.text name="eml.project.relatedProject.personnel"/> <span class="text-gbif-danger">&#42;</span>
                                                        </label>
                                                    </div>
                                                    <#list (eml.project.relatedProjects[item_index].personnel)! as personnel>
                                                        <div id="relatedProject-${item_index}-personnel-${personnel_index}" class="relatedProject-personnel-item clearfix row g-3 border-bottom pb-3 mt-1">
                                                            <div class="col-12 mt-auto py-1 d-flex justify-content-between">
                                                                <div>
                                                                    <div class="btn-group">
                                                                        <a id="dropdown-relatedProject-${item_index}-personnel-copy-${personnel_index}" href="#" class="metadata-action-link dropdown-toggle" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                                                            <span>
                                                                                <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                                                    <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                                                </svg>
                                                                            </span>
                                                                            <span>${copyLink?lower_case?cap_first}</span>
                                                                        </a>
                                                                        <ul class="dropdown-menu" aria-labelledby="dropdown-relatedProject-${item_index}-personnel-copy-${personnel_index}">
                                                                            <li><a id="relatedProject-${item_index}-personnel-from-contact-${personnel_index}" class="relatedProject-personnel-copy-from-contact-link dropdown-item menu-link w-100 dropdown-button fs-smaller-2" href="#"><@s.text name="eml.metadataAgent.fromContact"/></a></li>
                                                                            <li><a id="relatedProject-${item_index}-personnel-copy-${personnel_index}" class="relatedProject-personnel-copy-personnel-link dropdown-item menu-link w-100 dropdown-button fs-smaller-2" href="#"><@s.text name="eml.metadataAgent.fromAnother"/></a></li>
                                                                        </ul>
                                                                    </div>
                                                                </div>
                                                                <div>
                                                                    <a id="relatedProject-personnel-remove-${item_index}-${personnel_index}" class="removeRelatedProjectPersonnelLink metadata-action-link" href="">
                                                                        <span>
                                                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                                            </svg>
                                                                        </span>
                                                                        <span>${removeLink?lower_case?cap_first}</span>
                                                                    </a>
                                                                </div>
                                                            </div>
                                                            <div class="col-lg-5">
                                                                <@input name="eml.project.relatedProjects[${item_index}].personnel[${personnel_index}].firstName" i18nkey="eml.project.personnel.firstName" />
                                                            </div>
                                                            <div class="col-lg-5">
                                                                <@input name="eml.project.relatedProjects[${item_index}].personnel[${personnel_index}].lastName" i18nkey="eml.project.personnel.lastName" requiredField=true />
                                                            </div>
                                                            <div class="col-lg-2">
                                                                <@input name="eml.project.relatedProjects[${item_index}].personnel[${personnel_index}].salutation" i18nkey="eml.project.personnel.salutation" />
                                                            </div>
                                                            <div class="col-lg-6">
                                                                <@select name="eml.project.relatedProjects[${item_index}].personnel[${personnel_index}].userIds[0].directory" value="${(eml.project.relatedProjects[item_index].personnel[personnel_index].userIds[0].directory)!}" options=userIdDirectories help="i18n" i18nkey="eml.contact.directory" />
                                                            </div>
                                                            <div class="col-lg-6">
                                                                <@input name="eml.project.relatedProjects[${item_index}].personnel[${personnel_index}].userIds[0].identifier" help="i18n" i18nkey="eml.contact.identifier" />
                                                            </div>
                                                            <div class="col-lg-6">
                                                                <@select name="eml.project.relatedProjects[${item_index}].personnel[${personnel_index}].role" value="${(eml.project.relatedProjects[item_index].personnel[personnel_index].role)!}" i18nkey="eml.associatedParties.role" help="i18n" options=roles />
                                                            </div>
                                                        </div>
                                                    </#list>
                                                </div>
                                                <div class="row">
                                                    <div class="col mt-auto py-1">
                                                        <a id="plus-relatedProject-personnel-${item_index}" href="" class="metadata-action-link add-relatedProject-personnel">
                                                            <span>
                                                                <svg viewBox="0 0 24 24" class="link-icon">
                                                                    <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                                </svg>
                                                            </span>
                                                            <span>${addLink?lower_case?cap_first}</span>
                                                        </a>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </#list>
                                </div>

                                <div class="addNew col-12 mt-2">
                                    <a id="plus-relatedProject" href="" class="metadata-action-link">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span>${addRelatedProjectLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>
                        </div>

                        <div class="my-md-3 p-3">
                            <!-- List of personnel -->
                            <div class="listBlock">
                                <@textinline name="eml.project.personnel" help="i18n" requiredField=true />

                                <div id="personnel-items">
                                    <#list eml.project.personnel as item>
                                        <div id="personnel-item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="handle columnLinks mt-2 d-flex justify-content-between">
                                                <div>
                                                    <div class="btn-group">
                                                        <a id="dropdown-personnel-copy-${item_index}" href="#" class="metadata-action-link dropdown-toggle" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                                            <span>
                                                                <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                                    <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                                </svg>
                                                            </span>
                                                            <span>${copyLink?lower_case?cap_first}</span>
                                                        </a>
                                                        <ul class="dropdown-menu" aria-labelledby="dropdown-personnel-copy-${item_index}">
                                                            <li><a id="personnel-from-contact-${item_index}" class="dropdown-item menu-link w-100 dropdown-button fs-smaller-2" href="#"><@s.text name="eml.metadataAgent.fromContact"/></a></li>
                                                            <li><a id="personnel-copy-${item_index}" class="dropdown-item menu-link w-100 dropdown-button fs-smaller-2" href="#"><@s.text name="eml.metadataAgent.fromAnother"/></a></li>
                                                        </ul>
                                                    </div>
                                                </div>
                                                <div>
                                                    <a id="personnel-removeLink-${item_index}" class="removePersonnelLink metadata-action-link" href="">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${removeLink?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                            </div>
                                            <div class="col-lg-5">
                                                <@input name="eml.project.personnel[${item_index}].firstName" i18nkey="eml.project.personnel.firstName"/>
                                            </div>
                                            <div class="col-lg-5">
                                                <@input name="eml.project.personnel[${item_index}].lastName" i18nkey="eml.project.personnel.lastName" requiredField=true/>
                                            </div>
                                            <div class="col-lg-2">
                                                <@input name="eml.project.personnel[${item_index}].salutation" i18nkey="eml.project.personnel.salutation" />
                                            </div>
                                            <div class="col-lg-6">
                                                <#if eml.project.personnel[item_index]?? && eml.project.personnel[item_index].userIds[0]??>
                                                    <@select name="eml.project.personnel[${item_index}].userIds[0].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" value="${userIdDirecotriesExtended[eml.project.personnel[item_index].userIds[0].directory!]!}"/>
                                                <#else>
                                                    <@select name="eml.project.personnel[${item_index}].userIds[0].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" value=""/>
                                                </#if>
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="eml.project.personnel[${item_index}].userIds[0].identifier" help="i18n" i18nkey="eml.contact.identifier" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@select name="eml.project.personnel[${item_index}].role" i18nkey="eml.associatedParties.role" help="i18n" value="${eml.project.personnel[item_index].role!}" options=roles />
                                            </div>
                                        </div>
                                    </#list>
                                </div>

                                <div class="addNew col-12 mt-2">
                                    <a id="plus-personnel" href="" class="metadata-action-link">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span>${addLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>

                            <!-- internal parameter -->
                            <input name="r" type="hidden" value="${resource.shortname}" />

                            <div id="baseItem" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display:none;">
                                <div class="handle columnLinks mt-2 d-flex justify-content-between">
                                    <div>
                                        <div class="btn-group">
                                            <a id="dropdown-personnel-copy" href="#" class="metadata-action-link dropdown-toggle" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                            <span>
                                                <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                    <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                </svg>
                                            </span>
                                                <span>${copyLink?lower_case?cap_first}</span>
                                            </a>
                                            <ul class="dropdown-menu" aria-labelledby="personnel-creator-copy">
                                                <li><a id="personnel-from-contact" class="dropdown-item menu-link w-100 dropdown-button fs-smaller-2" href="#"><@s.text name="eml.metadataAgent.fromContact"/></a></li>
                                                <li><a id="personnel-copy" class="dropdown-item menu-link w-100 dropdown-button fs-smaller-2" href="#"><@s.text name="eml.metadataAgent.fromAnother"/></a></li>
                                            </ul>
                                        </div>
                                    </div>
                                    <div class="text-end">
                                        <a id="personnel-removeLink" class="removePersonnelLink metadata-action-link" href="">
                                            <span>
                                                <svg viewBox="0 0 24 24" class="link-icon">
                                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                </svg>
                                            </span>
                                            <span>${removeLink?lower_case?cap_first}</span>
                                        </a>
                                    </div>
                                </div>
                                <div class="col-lg-5">
                                    <@input name="firstName" i18nkey="eml.project.personnel.firstName" />
                                </div>
                                <div class="col-lg-5">
                                    <@input name="lastName" i18nkey="eml.project.personnel.lastName" requiredField=true />
                                </div>
                                <div class="col-lg-2">
                                    <@input name="salutation" i18nkey="eml.project.personnel.salutation" />
                                </div>
                                <div class="col-lg-6">
                                    <@select name="directory" options=userIdDirectories help="i18n" i18nkey="eml.contact.directory" />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="identifier" help="i18n" i18nkey="eml.contact.identifier" />
                                </div>
                                <div class="col-lg-6">
                                    <@select name="role" i18nkey="eml.associatedParties.role" help="i18n" options=roles />
                                </div>
                            </div>

                            <div id="baseItem-relatedProject" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display: none;">
                                <div class="handle columnLinks mt-2 d-flex justify-content-end">
                                    <div>
                                        <a id="relatedProject-removeLink" class="removeRelatedProjectLink metadata-action-link" href="">
                                            <span>
                                                <svg viewBox="0 0 24 24" class="link-icon">
                                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                </svg>
                                            </span>
                                            <span>${removeRelatedProjectLink?lower_case?cap_first}</span>
                                        </a>
                                    </div>
                                </div>
                                <div class="col-12">
                                    <@input name="relatedProjecttitle" i18nkey="eml.project.relatedProject.title" requiredField=true/>
                                </div>
                                <div class="col-12">
                                    <@input name="relatedProjectidentifier" i18nkey="eml.project.relatedProject.identifier" />
                                </div>
                                <div class="col-12">
                                    <@text name="relatedProjectdescription" i18nkey="eml.project.relatedProject.description" />
                                </div>
                                <div class="col-12">
                                    <div id="relatedProject-personnel">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.project.relatedProject.personnel" class="form-label mb-0">
                                                <@s.text name="eml.project.relatedProject.personnel"/>  <span class="text-gbif-danger">&#42;</span>
                                            </label>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col mt-auto py-1">
                                            <a id="plus-relatedProject-personnel" href="" class="add-relatedProject-personnel metadata-action-link">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon">
                                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                    </svg>
                                                </span>
                                                <span>${addLink?lower_case?cap_first}</span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div id="baseItem-award" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display: none;">
                                <div class="handle columnLinks mt-2 d-flex justify-content-end">
                                    <div>
                                        <a id="award-removeLink" class="removeAwardlLink metadata-action-link" href="">
                                            <span>
                                                <svg viewBox="0 0 24 24" class="link-icon">
                                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                </svg>
                                            </span>
                                            <span>${removeAwardLink?lower_case?cap_first}</span>
                                        </a>
                                    </div>
                                </div>
                                <div class="col-12">
                                    <@input name="awardtitle" i18nkey="eml.project.award.title" help="i18n" requiredField=true/>
                                </div>
                                <div class="col-lg-4">
                                    <@input name="awardfunderName" i18nkey="eml.project.award.funderName" help="i18n" requiredField=true />
                                </div>
                                <div class="col-lg-4">
                                    <@input name="awardawardNumber" i18nkey="eml.project.award.awardNumber" help="i18n" />
                                </div>
                                <div class="col-lg-4">
                                    <@input name="awardawardUrl" i18nkey="eml.project.award.awardUrl" help="i18n" />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="awardfunderIdentifiers[0]" help="i18n" i18nkey="eml.project.award.funderIdentifier" />
                                </div>
                            </div>

                            <div id="baseItem-personnel" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display:none;">
                                <div class="handle columnLinks mt-2 d-flex justify-content-between">
                                    <div>
                                        <div class="btn-group">
                                            <a id="dropdown-personnel-copy" href="#" class="metadata-action-link dropdown-toggle" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                                <span>
                                                    <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                        <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                    </svg>
                                                </span>
                                                <span>${copyLink?lower_case?cap_first}</span>
                                            </a>
                                            <ul class="dropdown-menu" aria-labelledby="personnel-creator-copy">
                                                <li><a id="personnel-from-contact" class="dropdown-item menu-link w-100 dropdown-button fs-smaller-2" href="#"><@s.text name="eml.metadataAgent.fromContact"/></a></li>
                                                <li><a id="personnel-copy" class="dropdown-item menu-link w-100 dropdown-button fs-smaller-2" href="#"><@s.text name="eml.metadataAgent.fromAnother"/></a></li>
                                            </ul>
                                        </div>
                                    </div>
                                    <div class="text-end">
                                        <a id="personnel-removeLink" class="removePersonnelLink metadata-action-link" href="">
                                            <span>
                                                <svg viewBox="0 0 24 24" class="link-icon">
                                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                </svg>
                                            </span>
                                            <span>${removeLink?lower_case?cap_first}</span>
                                        </a>
                                    </div>
                                </div>
                                <div class="col-lg-5">
                                    <@input name="firstName" i18nkey="eml.project.personnel.firstName" />
                                </div>
                                <div class="col-lg-5">
                                    <@input name="lastName" i18nkey="eml.project.personnel.lastName" requiredField=true />
                                </div>
                                <div class="col-lg-2">
                                    <@input name="salutation" i18nkey="eml.project.personnel.salutation" />
                                </div>
                                <div class="col-lg-6">
                                    <@select name="directory" options=userIdDirectories help="i18n" i18nkey="eml.contact.directory" />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="identifier" help="i18n" i18nkey="eml.contact.identifier" />
                                </div>
                                <div class="col-lg-6">
                                    <@select name="role" i18nkey="eml.associatedParties.role" help="i18n" options=roles />
                                </div>
                            </div>

                            <div id="baseItem-relatedProject-personnel" class="relatedProject-personnel-item clearfix row g-3 border-bottom pb-3 mt-1" style="display:none;">
                                <div class="handle columnLinks mt-2 d-flex justify-content-between">
                                    <div>
                                        <div class="btn-group">
                                            <a id="dropdown-personnel-copy" href="#" class="metadata-action-link dropdown-toggle" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                                <span>
                                                    <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                        <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                    </svg>
                                                </span>
                                                <span>${copyLink?lower_case?cap_first}</span>
                                            </a>
                                            <ul class="dropdown-menu" aria-labelledby="personnel-creator-copy">
                                                <li><a id="personnel-from-contact" class="relatedProject-personnel-copy-from-contact-link dropdown-item menu-link w-100 dropdown-button fs-smaller-2" href="#"><@s.text name="eml.metadataAgent.fromContact"/></a></li>
                                                <li><a id="personnel-copy" class="relatedProject-personnel-copy-personnel-link dropdown-item menu-link w-100 dropdown-button fs-smaller-2" href="#"><@s.text name="eml.metadataAgent.fromAnother"/></a></li>
                                            </ul>
                                        </div>
                                    </div>
                                    <div class="text-end">
                                        <a id="personnel-removeLink" class="removeRelatedProjectPersonnelLink metadata-action-link" href="">
                                            <span>
                                                <svg viewBox="0 0 24 24" class="link-icon">
                                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                </svg>
                                            </span>
                                            <span>${removeLink?lower_case?cap_first}</span>
                                        </a>
                                    </div>
                                </div>
                                <div class="col-lg-5">
                                    <@input name="firstName" i18nkey="eml.project.personnel.firstName" />
                                </div>
                                <div class="col-lg-5">
                                    <@input name="lastName" i18nkey="eml.project.personnel.lastName" requiredField=true />
                                </div>
                                <div class="col-lg-2">
                                    <@input name="salutation" i18nkey="eml.project.personnel.salutation" />
                                </div>
                                <div class="col-lg-6">
                                    <@select name="directory" options=userIdDirectories help="i18n" i18nkey="eml.contact.directory" />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="identifier" help="i18n" i18nkey="eml.contact.identifier" />
                                </div>
                                <div class="col-lg-6">
                                    <@select name="role" i18nkey="eml.associatedParties.role" help="i18n" options=roles />
                                </div>
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>
    </form>

    <div id="copy-agent-modal" class="modal fade" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <h5 class="modal-title w-100" id="staticBackdropLabel"><@s.text name="eml.metadataAgent.copy"/></h5>
                    <button type="button" class="close" data-bs-dismiss="modal" aria-label="Close">×</button>
                </div>
                <div class="modal-body" style="text-align: left !important;">
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
