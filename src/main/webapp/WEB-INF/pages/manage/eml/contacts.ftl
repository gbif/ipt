    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>
    <title><@s.text name='manage.metadata.contacts.title'/></title>
    <script src="${baseURL}/js/metadata-agent.js"></script>
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

            // party order matters: it is the creator (author) order and decides the primary contact
            const sortableParties = initAndGetSortable('#party-items');

            sortableParties[0].addEventListener('sortupdate', () => changeInputNamesAfterDragging("party"));
            sortableParties[0].addEventListener('drag', dragScroll);

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

            function changeInputNamesAfterDragging(entityName) {
                displayProcessing();
                var agentItems = $("#" + entityName + "-items div.item");

                agentItems.each(function (index) {
                    // previously elementId
                    var itemId = $(this)[0].id;
                    var params = {
                        entity : {
                            name: entityName
                        },
                        itemId: itemId,
                        index: index,
                        isNew: false
                    }
                    setAgentRegularInput("firstName", params);
                    setAgentRegularInput("lastName", params);
                    setAgentRegularInput("salutation", params);
                    setAgentRegularInput("organisation", params);
                    setAgentRepeatableInput("position", params);
                    setAgentRepeatableInput("address", params);
                    setAgentRegularInput("city", params);
                    setAgentRegularInput("province", params);
                    setAgentRegularDropdown("country", params);
                    setAgentRegularInput("postalCode", params);
                    setAgentRepeatableInput("phone", params);
                    setAgentRepeatableInput("email", params);
                    setAgentRepeatableInput("homepage", params);
                    setAgentIdentifier(params);
                    setPartyRoleInputs(params);
                });

                hideProcessing();
            }

            var copyAgentModal = $('#copy-agent-modal');
            $('#resource').select2({
                placeholder: '${action.getText("eml.metadataAgent.copy.resource.select")?js_string?no_esc}',
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
                placeholder: '${action.getText("eml.metadataAgent.copy.agentType.select")?js_string?no_esc}',
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
                placeholder: '${action.getText("eml.metadataAgent.copy.agent.select")?js_string?no_esc}',
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

            $('[id^="eml."][id$=".address.country"]').select2({
                placeholder: '${action.getText("eml.country.selection")?js_string?no_esc}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
                allowClear: true,
                theme: 'bootstrap4'
            });

            $('[id^="eml."][id$=".directory"]').select2({
                placeholder: '${action.getText("eml.contact.noDirectory")?js_string?no_esc}',
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
            // other (associated party) roles of the existing parties; new parties get it when added
            $('#party-items .party-associated-roles').select2({
                placeholder: '${action.getText("eml.party.associatedPartyRoles.selection")?js_string?no_esc}',
                language: {
                    noResults: function () {
                        return '${selectNoResultsFound}';
                    }
                },
                width: "100%",
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
    </style>
    <#include "/WEB-INF/pages/macros/user_id_directories.ftl"/>
    <#include "/WEB-INF/pages/macros/metadata_agent.ftl"/>

    <#assign currentMetadataPage = "contacts"/>
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <#assign inputIdentifierPlaceholder><@s.text name="eml.contact.identifier"/></#assign>
    <#assign copyLink><@s.text name="eml.metadataAgent.copyLink"/></#assign>
    <#assign copyLink = copyLink?markup_string>
    <#assign copyFromAnotherLink><@s.text name="eml.metadataAgent.copyFromAnother"/></#assign>
    <#assign copyFromAnotherLink = copyFromAnotherLink?markup_string>
    <#assign removePartyLink><@s.text name='manage.metadata.removethis'/> <@s.text name='eml.party.item'/></#assign>
    <#assign removePartyLink = removePartyLink?markup_string>
    <#assign addPartyLink><@s.text name='manage.metadata.addnew'/> <@s.text name='eml.party.item'/></#assign>
    <#assign addPartyLink = addPartyLink?markup_string>
    <#assign addNew><@s.text name='manage.metadata.addnew'/></#assign>
    <#assign addNew = addNew?markup_string>

    <form class="needs-validation track-unsaved" action="metadata-${section}.do" method="post" novalidate>
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
                            <@s.text name='manage.metadata.contacts.title'/>
                        </h1>
                    </div>

                    <div class="text-center fs-smaller">
                        <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                    </div>

                    <div class="text-center mt-2">
                        <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/>
                        <button type="button" class="btn btn-sm btn-outline-secondary top-button" onclick="window.history.back();">
                            <@s.text name="button.back"/>
                        </button>
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
                        <div class="my-3 p-3">
                            <!-- People and organisations: contacts, creators, metadata providers and associated parties -->
                            <@textinline name="eml.party.plural" help="i18n" requiredField=true/>
                            <div id="party-items">
                                <#list eml.parties as party>
                                    <div id="party-item-${party_index}" class="item row g-3 pb-4 border-bottom">
                                        <div class="handle columnLinks mt-4 col-12 justify-content-end">
                                            <div class="row g-1">
                                                <div class="col-md-6 d-flex justify-content-end justify-content-md-start">
                                                    <a id="party-copy-${party_index}" href="" class="metadata-action-link custom-link">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${copyFromAnotherLink?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <div class="col-md-6 d-flex justify-content-end">
                                                    <a id="party-removeLink-${party_index}" class="removeAgentLink metadata-action-link custom-link" href="">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${removePartyLink?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-md-5">
                                            <@input name="eml.parties[${party_index}].agent.firstName" value=(party.agent.firstName)! i18nkey="eml.contact.firstName"/>
                                        </div>
                                        <div class="col-md-5">
                                            <@input name="eml.parties[${party_index}].agent.lastName" value=(party.agent.lastName)! i18nkey="eml.contact.lastName" />
                                        </div>
                                        <div class="col-md-2">
                                            <@input name="eml.parties[${party_index}].agent.salutation" value=(party.agent.salutation)! i18nkey="eml.contact.salutation"/>
                                        </div>
                                        <div class="col-md-6">
                                            <@input name="eml.parties[${party_index}].agent.organisation" value=(party.agent.organisation)! i18nkey="eml.contact.organisation"  />
                                        </div>
                                        <div class="col-12 party-roles">
                                            <div class="d-flex text-smaller">
                                                <label class="form-label mb-0"><@s.text name="eml.party.roles"/></label>
                                            </div>
                                            <div class="d-flex flex-wrap">
                                                <#list ["creator", "contact", "metadataProvider"] as roleField>
                                                <div class="form-check me-4">
                                                    <input class="form-check-input party-role" type="checkbox" value="true" data-party-field="${roleField}" id="eml.parties[${party_index}].${roleField}" name="eml.parties[${party_index}].${roleField}" <#if party[roleField]>checked</#if>/>
                                                    <label class="form-check-label" data-party-for="${roleField}" for="eml.parties[${party_index}].${roleField}"><@s.text name="eml.party.${roleField}"/></label>
                                                </div>
                                                </#list>
                                            </div>
                                            <div class="mt-2">
                                                <div class="d-flex text-smaller">
                                                    <a tabindex="0" role="button" class="popover-link" data-bs-toggle="popover" data-bs-trigger="focus" data-bs-html="true" data-bs-content="<@s.text name='eml.associatedParties.role.help'/>">
                                                        <i class="bi bi-info-circle text-gbif-primary px-1"></i>
                                                    </a>
                                                    <label class="form-label mb-0" data-party-for="associatedPartyRoles" for="eml.parties[${party_index}].associatedPartyRoles"><@s.text name="eml.party.associatedPartyRoles"/></label>
                                                </div>
                                                <select multiple class="form-select party-associated-roles" data-party-field="associatedPartyRoles" id="eml.parties[${party_index}].associatedPartyRoles" name="eml.parties[${party_index}].associatedPartyRoles">
                                                    <#list roles?keys as roleValue>
                                                        <#if roleValue?has_content>
                                                    <option value="${roleValue}" <#if party.associatedPartyRoles?seq_contains(roleValue)>selected</#if>>${roles.get(roleValue)}</option>
                                                        </#if>
                                                    </#list>
                                                    <#-- roles from EML that are not in the vocabulary are kept -->
                                                    <#list party.associatedPartyRoles as roleValue>
                                                        <#if !roles?keys?seq_contains(roleValue)>
                                                    <option value="${roleValue}" selected>${roleValue}</option>
                                                        </#if>
                                                    </#list>
                                                </select>
                                                <@s.fielderror cssClass="invalid-feedback list-unstyled field-error my-1 d-block" fieldName="eml.parties[${party_index}].associatedPartyRoles"/>
                                            </div>
                                        </div>
                                        <div class="col-12">
                                            <div id="party-${party_index}-positions" class="party-positions">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.parties.position" class="form-label mb-0">
                                                        <@s.text name="eml.contact.position"/>
                                                    </label>
                                                    <a id="plus-party-position-${party_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.parties[party_index].agent.position as position>
                                                    <div id="party-${party_index}-position-${position_index}" class="position-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.parties[${party_index}].agent.position[${position_index}]" value=position! i18nkey="eml.contact.position" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="party-position-remove-${party_index}-${position_index}" class="removeSubEntity metadata-action-link" href="">
                                                                        <span>
                                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                                            </svg>
                                                                        </span>
                                                                    </a>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </#list>
                                            </div>
                                        </div>
                                        <div class="col-12">
                                            <div id="party-${party_index}-address" class="party-address">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.parties.address" class="form-label mb-0">
                                                        <@s.text name="eml.contact.address.address"/>
                                                    </label>
                                                    <a id="plus-party-address-${party_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.parties[party_index].agent.address.address as address>
                                                    <div id="party-${party_index}-address-${address_index}" class="address-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.parties[${party_index}].agent.address.address[${address_index}]" value=address! i18nkey="eml.contact.address.address" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="party-address-remove-${party_index}-${address_index}" class="removeSubEntity metadata-action-link" href="">
                                                                        <span>
                                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                                            </svg>
                                                                        </span>
                                                                    </a>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </#list>
                                            </div>
                                        </div>
                                        <div class="col-md-6 col-lg-4">
                                            <@input name="eml.parties[${party_index}].agent.address.city" value=(party.agent.address.city)! i18nkey="eml.contact.address.city" />
                                        </div>
                                        <div class="col-md-6 col-lg-3">
                                            <@input name="eml.parties[${party_index}].agent.address.province" value=(party.agent.address.province)! i18nkey="eml.contact.address.province" />
                                        </div>
                                        <div class="countryList col-md-6 col-lg-3">
                                            <@select name="eml.parties[${party_index}].agent.address.country" help="i18n" options=countries i18nkey="eml.contact.address.country" value="${eml.parties[party_index].agent.address.country!}"/>
                                        </div>
                                        <div class="col-md-6 col-lg-2">
                                            <@input name="eml.parties[${party_index}].agent.address.postalCode" value=(party.agent.address.postalCode)! i18nkey="eml.contact.address.postalCode" />
                                        </div>
                                        <div class="col-12">
                                            <div id="party-${party_index}-phones" class="party-phones">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.parties.phone" class="form-label mb-0">
                                                        <@s.text name="eml.contact.phone"/>
                                                    </label>
                                                    <a id="plus-party-phone-${party_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.parties[party_index].agent.phone as phone>
                                                    <div id="party-${party_index}-phone-${phone_index}" class="phone-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.parties[${party_index}].agent.phone[${phone_index}]" value=phone! i18nkey="eml.contact.phone" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="party-phone-remove-${party_index}-${phone_index}" class="removeSubEntity metadata-action-link" href="">
                                                                        <span>
                                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                                            </svg>
                                                                        </span>
                                                                    </a>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </#list>
                                            </div>
                                        </div>
                                        <div class="col-12">
                                            <div id="party-${party_index}-emails" class="party-emails">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.parties.email" class="form-label mb-0">
                                                        <@s.text name="eml.contact.email"/>
                                                    </label>
                                                    <a id="plus-party-email-${party_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.parties[party_index].agent.email as email>
                                                    <div id="party-${party_index}-email-${email_index}" class="email-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.parties[${party_index}].agent.email[${email_index}]" value=email! i18nkey="eml.contact.email" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="party-email-remove-${party_index}-${email_index}" class="removeSubEntity metadata-action-link" href="">
                                                                        <span>
                                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                                            </svg>
                                                                        </span>
                                                                    </a>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </#list>
                                            </div>
                                        </div>
                                        <div class="col-12">
                                            <div id="party-${party_index}-homepages" class="party-homepages">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.parties.homepage" class="form-label mb-0">
                                                        <@s.text name="eml.contact.homepage"/>
                                                    </label>
                                                    <a id="plus-party-homepage-${party_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.parties[party_index].agent.homepage as homepage>
                                                    <div id="party-${party_index}-homepage-${homepage_index}" class="homepage-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.parties[${party_index}].agent.homepage[${homepage_index}]" value=homepage! i18nkey="eml.contact.homepage" type="url" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="party-homepage-remove-${party_index}-${homepage_index}" class="removeSubEntity metadata-action-link" href="">
                                                                        <span>
                                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                                            </svg>
                                                                        </span>
                                                                    </a>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </#list>
                                            </div>
                                        </div>
                                        <div class="col-12">
                                            <div id="party-${party_index}-identifiers" class="party-identifiers">
                                                <div class="d-flex text-smaller">
                                                    <a tabindex="0" role="button"
                                                       class="popover-link"
                                                       data-bs-toggle="popover"
                                                       data-bs-trigger="focus"
                                                       data-bs-html="true"
                                                       data-bs-content="<@s.text name='eml.contact.directory.help'/><br><br><@s.text name='eml.contact.identifier.help'/>">
                                                        <i class="bi bi-info-circle text-gbif-primary px-1"></i>
                                                    </a>
                                                    <label for="eml.parties.userIds" class="form-label mb-0">
                                                        <@s.text name="eml.contact.identifier"/>
                                                    </label>
                                                    <a id="plus-party-identifier-${party_index}" href="" class="metadata-action-link custom-link add-identifier">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list party.agent.userIds as userId>
                                                    <div id="party-${party_index}-identifier-${userId_index}" class="identifier-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-4">
                                                                <@select name="eml.parties[${party_index}].agent.userIds[${userId_index}].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" withLabel=false value="${userIdDirecotriesExtended[(userId.directory)!]!}"/>
                                                            </div>
                                                            <div class="col-md-4 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.parties[${party_index}].agent.userIds[${userId_index}].identifier" help="i18n" i18nkey="eml.contact.identifier" withLabel=false placeholder="${inputIdentifierPlaceholder}" value="${(userId.identifier)!}"/>
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="party-identifier-remove-${party_index}-${userId_index}" class="removeIdentifier metadata-action-link" href="">
                                                                        <span>
                                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                                            </svg>
                                                                        </span>
                                                                    </a>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </#list>
                                            </div>
                                        </div>
                                    </div>
                                </#list>
                            </div>

                            <div class="addNew my-2">
                                <a id="plus-party" href="" class="plus-agent metadata-action-link custom-link">
                                    <span>
                                        <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                        </svg>
                                    </span>
                                    <span>${addPartyLink?lower_case?cap_first}</span>
                                </a>
                            </div>

                            <div id="baseItem-party" class="item row g-3 pb-4 border-bottom" style="display:none;">
                                <div class="handle columnLinks mt-4 col-12 justify-content-end">
                                    <div class="row g-1">
                                        <div class="col-md-6 d-flex justify-content-end justify-content-md-start">
                                            <a id="party-copy" href="" class="metadata-action-link custom-link">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                        <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                    </svg>
                                                </span>
                                                <span>${copyFromAnotherLink?lower_case?cap_first}</span>
                                            </a>
                                        </div>
                                        <div class="col-md-6 d-flex justify-content-end">
                                            <a id="party-removeLink" class="removeAgentLink metadata-action-link custom-link" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                    </svg>
                                                </span>
                                                <span>${removePartyLink?lower_case?cap_first}</span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-5">
                                    <@input name="party.agent.firstName" i18nkey="eml.contact.firstName"/>
                                </div>
                                <div class="col-md-5">
                                    <@input name="party.agent.lastName" i18nkey="eml.contact.lastName"/>
                                </div>
                                <div class="col-md-2">
                                    <@input name="party.agent.salutation" i18nkey="eml.contact.salutation"/>
                                </div>
                                <div class="col-md-6">
                                    <@input name="party.agent.organisation" i18nkey="eml.contact.organisation"/>
                                </div>
                                <div class="col-12 party-roles">
                                    <div class="d-flex text-smaller">
                                        <label class="form-label mb-0"><@s.text name="eml.party.roles"/></label>
                                    </div>
                                    <div class="d-flex flex-wrap">
                                        <#list ["creator", "contact", "metadataProvider"] as roleField>
                                        <div class="form-check me-4">
                                            <input class="form-check-input party-role" type="checkbox" value="true" data-party-field="${roleField}" id="party.${roleField}" name="party.${roleField}"/>
                                            <label class="form-check-label" data-party-for="${roleField}" for="party.${roleField}"><@s.text name="eml.party.${roleField}"/></label>
                                        </div>
                                        </#list>
                                    </div>
                                    <div class="mt-2">
                                        <div class="d-flex text-smaller">
                                            <a tabindex="0" role="button" class="popover-link" data-bs-toggle="popover" data-bs-trigger="focus" data-bs-html="true" data-bs-content="<@s.text name='eml.associatedParties.role.help'/>">
                                                <i class="bi bi-info-circle text-gbif-primary px-1"></i>
                                            </a>
                                            <label class="form-label mb-0" data-party-for="associatedPartyRoles" for="party.associatedPartyRoles"><@s.text name="eml.party.associatedPartyRoles"/></label>
                                        </div>
                                        <select multiple class="form-select party-associated-roles" data-party-field="associatedPartyRoles" id="party.associatedPartyRoles" name="party.associatedPartyRoles">
                                            <#list roles?keys as roleValue>
                                                <#if roleValue?has_content>
                                            <option value="${roleValue}">${roles.get(roleValue)}</option>
                                                </#if>
                                            </#list>
                                        </select>
                                    </div>
                                </div>
                                <div class="col-12">
                                    <div id="party-positions" class="party-positions">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.parties.position" class="form-label mb-0">
                                                <@s.text name="eml.contact.position"/>
                                            </label>
                                            <a id="plus-party-position" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                    </svg>
                                                </span>
                                                <span>${addNew?lower_case?cap_first}</span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-12">
                                    <div id="party-address" class="party-address">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.parties.address" class="form-label mb-0">
                                                <@s.text name="eml.contact.address.address"/>
                                            </label>
                                            <a id="plus-party-address" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                    </svg>
                                                </span>
                                                <span>${addNew?lower_case?cap_first}</span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-6 col-lg-4">
                                    <@input name="party.agent.address.city" i18nkey="eml.contact.address.city" />
                                </div>
                                <div class="col-md-6 col-lg-3">
                                    <@input name="party.agent.address.province" i18nkey="eml.contact.address.province" />
                                </div>
                                <div class="countryList col-md-6 col-lg-3">
                                    <@select name="country" options=countries help="i18n" i18nkey="eml.contact.address.country" />
                                </div>
                                <div class="col-md-6 col-lg-2">
                                    <@input name="party.agent.address.postalCode" i18nkey="eml.contact.address.postalCode" />
                                </div>
                                <div class="col-12">
                                    <div id="party-phones" class="party-phones">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.parties.phone" class="form-label mb-0">
                                                <@s.text name="eml.contact.phone"/>
                                            </label>
                                            <a id="plus-party-phone" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                    </svg>
                                                </span>
                                                <span>${addNew?lower_case?cap_first}</span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-12">
                                    <div id="party-emails" class="party-emails">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.parties.email" class="form-label mb-0">
                                                <@s.text name="eml.contact.email"/>
                                            </label>
                                            <a id="plus-party-email" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                    </svg>
                                                </span>
                                                <span>${addNew?lower_case?cap_first}</span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-12">
                                    <div id="party-homepages" class="party-homepages">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.parties.homepage" class="form-label mb-0">
                                                <@s.text name="eml.contact.homepage"/>
                                            </label>
                                            <a id="plus-party-homepage" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                    </svg>
                                                </span>
                                                <span>${addNew?lower_case?cap_first}</span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-12">
                                    <div id="party-identifiers" class="party-identifiers">
                                        <div class="d-flex text-smaller">
                                            <a tabindex="0" role="button"
                                               class="popover-link"
                                               data-bs-toggle="popover"
                                               data-bs-trigger="focus"
                                               data-bs-html="true"
                                               data-bs-content="<@s.text name='eml.contact.directory.help'/><br><br><@s.text name='eml.contact.identifier.help'/>">
                                                <i class="bi bi-info-circle text-gbif-primary px-1"></i>
                                            </a>
                                            <label for="eml.parties.userIds" class="form-label mb-0">
                                                <@s.text name="eml.contact.identifier"/>
                                            </label>
                                            <a id="plus-party-identifier" href="" class="metadata-action-link custom-link add-identifier">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                    </svg>
                                                </span>
                                                <span>${addNew?lower_case?cap_first}</span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div id="baseItem-address" class="address-item" style="display: none;">
                                <div class="row g-2 mt-0">
                                    <div class="col-md-6 d-flex">
                                        <div class="flex-grow-1">
                                            <@input name="baseItem-address-input" i18nkey="eml.contact.address.address" value="" withLabel=false />
                                        </div>
                                        <div class="mb-auto py-1 ps-1">
                                            <a id="baseItem-address-remove" class="removeSubEntity metadata-action-link" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                    </svg>
                                                </span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div id="baseItem-position" class="position-item" style="display: none;">
                                <div class="row g-2 mt-0">
                                    <div class="col-md-6 d-flex">
                                        <div class="flex-grow-1">
                                            <@input name="baseItem-position-input" i18nkey="eml.contact.position" value="" withLabel=false />
                                        </div>
                                        <div class="mb-auto py-1 ps-1">
                                            <a id="baseItem-position-remove" class="removeSubEntity metadata-action-link" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                    </svg>
                                                </span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div id="baseItem-phone" class="phone-item" style="display: none;">
                                <div class="row g-2 mt-0">
                                    <div class="col-md-6 d-flex">
                                        <div class="flex-grow-1">
                                            <@input name="baseItem-phone-input" i18nkey="eml.contact.phone" value="" withLabel=false />
                                        </div>
                                        <div class="mb-auto py-1 ps-1">
                                            <a id="baseItem-phone-remove" class="removeSubEntity metadata-action-link" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                    </svg>
                                                </span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div id="baseItem-email" class="email-item" style="display: none;">
                                <div class="row g-2 mt-0">
                                    <div class="col-md-6 d-flex">
                                        <div class="flex-grow-1">
                                            <@input name="baseItem-email-input" i18nkey="eml.contact.email" value="" withLabel=false />
                                        </div>
                                        <div class="mb-auto py-1 ps-1">
                                            <a id="baseItem-email-remove" class="removeSubEntity metadata-action-link" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                    </svg>
                                                </span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div id="baseItem-homepage" class="homepage-item" style="display: none;">
                                <div class="row g-2 mt-0">
                                    <div class="col-md-6 d-flex">
                                        <div class="flex-grow-1">
                                            <@input name="baseItem-homepage-input" i18nkey="eml.contact.homepage" value="" withLabel=false />
                                        </div>
                                        <div class="mb-auto py-1 ps-1">
                                            <a id="baseItem-homepage-remove" class="removeSubEntity metadata-action-link" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                    </svg>
                                                </span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div id="baseItem-identifier" class="identifier-item" style="display: none;">
                                <div class="row g-2 mt-0">
                                    <div class="col-md-4">
                                        <@select name="baseItem-directory-select" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" value="" withLabel=false/>
                                    </div>
                                    <div class="col-md-4 d-flex">
                                        <div class="flex-grow-1">
                                            <@input name="baseItem-identifier-input" help="i18n" i18nkey="eml.contact.identifier" value="" withLabel=false placeholder="${inputIdentifierPlaceholder}"/>
                                        </div>
                                        <div class="mb-auto py-1 ps-1">
                                            <a id="baseItem-identifier-remove" class="removeIdentifier metadata-action-link" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                    </svg>
                                                </span>
                                            </a>
                                        </div>
                                    </div>
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

    <#include "/WEB-INF/pages/manage/eml/unsaved_changes_modal.ftl">

    <div id="copy-agent-modal" class="modal fade" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
        <div class="modal-dialog modal-confirm modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header flex-column">
                    <img src="${baseURL}/images/logo-modal-success.png" alt="Success" class="modal-image" />
                </div>
                <div class="modal-body" style="text-align: left !important;">
                    <h5 class="modal-title w-100" id="staticBackdropLabel"><@s.text name="eml.metadataAgent.copy"/></h5>
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
                    </div>
                </div>
                <div class="modal-footer justify-content-center">
                    <button id="copy-agent-button" type="button" class="btn btn-sm btn-outline-gbif-primary" style="display: none;">
                        <@s.text name="button.copy"/>
                    </button>
                    <button id="copy-agent-cancel" type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">
                        <@s.text name="button.cancel"/>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <#include "/WEB-INF/pages/inc/footer.ftl">
