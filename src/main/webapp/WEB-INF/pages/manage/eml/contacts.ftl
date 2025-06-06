<#escape x as x?html>
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

            const sortableContacts = initAndGetSortable('#contact-items');
            const sortableCreators = initAndGetSortable('#creator-items');
            const sortableMetadataProviders = initAndGetSortable('#metadataProvider-items');
            const sortableAssociatedParties = initAndGetSortable('#associatedParty-items');

            sortableContacts[0].addEventListener('sortupdate', () => changeInputNamesAfterDragging("contact"));
            sortableContacts[0].addEventListener('drag', dragScroll);
            sortableCreators[0].addEventListener('sortupdate', () => changeInputNamesAfterDragging("creator"));
            sortableCreators[0].addEventListener('drag', dragScroll);
            sortableMetadataProviders[0].addEventListener('sortupdate', () => changeInputNamesAfterDragging("metadataProvider"));
            sortableMetadataProviders[0].addEventListener('drag', dragScroll);
            sortableAssociatedParties[0].addEventListener('sortupdate', () => changeInputNamesAfterDragging("associatedParty"));
            sortableAssociatedParties[0].addEventListener('drag', dragScroll);

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
                const entityNamePlural = getEntityPluralName(entityName);
                var agentItems = $("#" + entityName + "-items div.item");

                agentItems.each(function (index) {
                    // previously elementId
                    var itemId = $(this)[0].id;
                    var params = {
                        entity : {
                            name: entityName,
                            pluralName: entityNamePlural
                        },
                        itemId: itemId,
                        index: index,
                        translations: {
                            role: "${action.getText("eml.agent.role.selection")?js_string}",
                            country: "${action.getText("eml.country.selection")?js_string}",
                            directory: "${action.getText("eml.contact.noDirectory")?js_string}",
                            notFound: "${selectNoResultsFound}"
                        },
                        isNew: false
                    }
                    setAgentRegularInput("firstName", params);
                    setAgentRegularInput("lastName", params);
                    setAgentRegularInput("salutation", params);
                    setAgentRegularInput("organisation", params);
                    setAgentRegularDropdown("role", params);
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
                });

                hideProcessing();
            }

            function getEntityPluralName(entityName) {
                if (entityName === 'associatedParty')
                    return 'associatedParties'
                else return entityName + 's';
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

            $('[id^="eml."][id$=".address.country"]').select2({
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

            $('[id^="eml."][id$=".directory"]').select2({
                placeholder: '${action.getText("eml.associatedParties.noDirectory")?js_string}',
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
            $('[id^="eml.associatedParties"][id$=".role"]').select2({
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

            $('select').change(function () {
                var selectedValue = $(this).val();
                var selectId = $(this).attr('id');

                if (selectId.includes("role")) {
                    var index;
                    var match = selectId.match(/\[(\d+)]/);
                    if (match) {
                        index = match[1];
                    }

                    if (selectedValue === 'originator' || selectedValue === 'metadataProvider') {
                        if (index) $('#associatedParty-item-' + index + ' .contact-citation-banner').show();
                    } else {
                        if (index) $('#associatedParty-item-' + index + ' .contact-citation-banner').hide();
                    }
                }
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
    <#assign copyFromAnotherLink><@s.text name="eml.metadataAgent.copyFromAnother"/></#assign>
    <#assign removeContactLink><@s.text name='manage.metadata.removethis'/> <@s.text name='eml.contact'/></#assign>
    <#assign removeCreatorLink><@s.text name='manage.metadata.removethis'/> <@s.text name='portal.resource.creator'/></#assign>
    <#assign removeMetadataProviderLink><@s.text name='manage.metadata.removethis'/> <@s.text name='eml.metadataProvider'/></#assign>
    <#assign addContactLink><@s.text name='manage.metadata.addnew'/> <@s.text name='eml.contact'/></#assign>
    <#assign addCreatorLink><@s.text name='manage.metadata.addnew'/> <@s.text name='portal.resource.creator'/></#assign>
    <#assign addMetadataProviderLink><@s.text name='manage.metadata.addnew'/> <@s.text name='eml.metadataProvider'/></#assign>
    <#assign addNew><@s.text name='manage.metadata.addnew'/></#assign>

    <form class="needs-validation" action="metadata-${section}.do" method="post" novalidate>
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
                        <div class="my-3 p-3">
                            <!-- Resource Contacts -->
                            <@textinline name="eml.contact.plural" help="i18n" requiredField=true/>
                            <div id="contact-items">
                                <#list eml.contacts as contact>
                                    <div id="contact-item-${contact_index}" class="item row g-3 pb-4 border-bottom">
                                        <div class="handle columnLinks mt-4 col-12 justify-content-end">
                                            <div class="row g-1">
                                                <div class="col-md-6 d-flex justify-content-end justify-content-md-start">
                                                    <a id="contact-copy-${contact_index}" href="" class="metadata-action-link custom-link">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${copyFromAnotherLink?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <div class="col-md-6 d-flex justify-content-end">
                                                    <a id="contact-removeLink-${contact_index}" class="removeAgentLink metadata-action-link custom-link" href="">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${removeContactLink?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-md-5">
                                            <@input name="eml.contacts[${contact_index}].firstName" i18nkey="eml.contact.firstName"/>
                                        </div>
                                        <div class="col-md-5">
                                            <@input name="eml.contacts[${contact_index}].lastName" i18nkey="eml.contact.lastName" />
                                        </div>
                                        <div class="col-md-2">
                                            <@input name="eml.contacts[${contact_index}].salutation" i18nkey="eml.contact.salutation"/>
                                        </div>
                                        <div class="col-md-6">
                                            <@input name="eml.contacts[${contact_index}].organisation" i18nkey="eml.contact.organisation"  />
                                        </div>
                                        <div class="col-12">
                                            <div id="contact-${contact_index}-positions" class="contact-positions">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.contacts.position" class="form-label mb-0">
                                                        <@s.text name="eml.contact.position"/>
                                                    </label>
                                                    <a id="plus-contact-position-${contact_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.contacts[contact_index].position as position>
                                                    <div id="contact-${contact_index}-position-${position_index}" class="position-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.contacts[${contact_index}].position[${position_index}]" i18nkey="eml.contact.position" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="contact-position-remove-${contact_index}-${position_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <div id="contact-${contact_index}-address" class="contact-address">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.contacts.address" class="form-label mb-0">
                                                        <@s.text name="eml.contact.address.address"/>
                                                    </label>
                                                    <a id="plus-contact-address-${contact_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.contacts[contact_index].address.address as address>
                                                    <div id="contact-${contact_index}-address-${address_index}" class="address-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.contacts[${contact_index}].address.address[${address_index}]" i18nkey="eml.contact.address.address" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="contact-address-remove-${contact_index}-${address_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <@input name="eml.contacts[${contact_index}].address.city" i18nkey="eml.contact.address.city" />
                                        </div>
                                        <div class="col-md-6 col-lg-3">
                                            <@input name="eml.contacts[${contact_index}].address.province" i18nkey="eml.contact.address.province" />
                                        </div>
                                        <div class="countryList col-md-6 col-lg-3">
                                            <@select name="eml.contacts[${contact_index}].address.country" help="i18n" options=countries i18nkey="eml.contact.address.country" value="${eml.contacts[contact_index].address.country!}"/>
                                        </div>
                                        <div class="col-md-6 col-lg-2">
                                            <@input name="eml.contacts[${contact_index}].address.postalCode" i18nkey="eml.contact.address.postalCode" />
                                        </div>
                                        <div class="col-12">
                                            <div id="contact-${contact_index}-phones" class="contact-phones">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.contacts.phone" class="form-label mb-0">
                                                        <@s.text name="eml.contact.phone"/>
                                                    </label>
                                                    <a id="plus-contact-phone-${contact_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.contacts[contact_index].phone as phone>
                                                    <div id="contact-${contact_index}-phone-${phone_index}" class="phone-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.contacts[${contact_index}].phone[${phone_index}]" i18nkey="eml.contact.phone" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="contact-phone-remove-${contact_index}-${phone_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <div id="contact-${contact_index}-emails" class="contact-emails">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.contacts.email" class="form-label mb-0">
                                                        <@s.text name="eml.contact.email"/>
                                                    </label>
                                                    <a id="plus-contact-email-${contact_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.contacts[contact_index].email as email>
                                                    <div id="contact-${contact_index}-email-${email_index}" class="email-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.contacts[${contact_index}].email[${email_index}]" i18nkey="eml.contact.email" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="contact-email-remove-${contact_index}-${email_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <div id="contact-${contact_index}-homepages" class="contact-homepages">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.contacts.homepage" class="form-label mb-0">
                                                        <@s.text name="eml.contact.homepage"/>
                                                    </label>
                                                    <a id="plus-contact-homepage-${contact_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.contacts[contact_index].homepage as homepage>
                                                    <div id="contact-${contact_index}-homepage-${homepage_index}" class="homepage-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.contacts[${contact_index}].homepage[${homepage_index}]" i18nkey="eml.contact.homepage" type="url" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="contact-homepage-remove-${contact_index}-${homepage_index}" class="removeSubEntity metadata-action-link" href="">
                                                                        <span>
                                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                                                                                <<path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
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
                                            <div id="contact-${contact_index}-identifiers" class="contact-identifiers">
                                                <div class="d-flex text-smaller">
                                                    <a tabindex="0" role="button"
                                                       class="popover-link"
                                                       data-bs-toggle="popover"
                                                       data-bs-trigger="focus"
                                                       data-bs-html="true"
                                                       data-bs-content="<@s.text name='eml.contact.directory.help'/><br><br><@s.text name='eml.contact.identifier.help'/>">
                                                        <i class="bi bi-info-circle text-gbif-primary px-1"></i>
                                                    </a>
                                                    <label for="eml.contacts.userIds" class="form-label mb-0">
                                                        <@s.text name="eml.contact.identifier"/>
                                                    </label>
                                                    <a id="plus-contact-identifier-${contact_index}" href="" class="metadata-action-link custom-link add-identifier">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list contact.userIds as userId>
                                                    <div id="contact-${contact_index}-identifier-${userId_index}" class="identifier-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-4">
                                                                <@select name="eml.contacts[${contact_index}].userIds[${userId_index}].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" withLabel=false value="${userIdDirecotriesExtended[(userId.directory)!]!}"/>
                                                            </div>
                                                            <div class="col-md-4 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.contacts[${contact_index}].userIds[${userId_index}].identifier" help="i18n" i18nkey="eml.contact.identifier" withLabel=false placeholder="${inputIdentifierPlaceholder}" value="${(userId.identifier)!}"/>
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="contact-identifier-remove-${contact_index}-${userId_index}" class="removeIdentifier metadata-action-link" href="">
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
                                <a id="plus-contact" href="" class="plus-agent metadata-action-link custom-link">
                                    <span>
                                        <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                        </svg>
                                    </span>
                                    <span>${addContactLink?lower_case?cap_first}</span>
                                </a>
                            </div>

                            <div id="baseItem-contact" class="item row g-3 pb-4 border-bottom" style="display:none;">
                                <div class="handle columnLinks mt-4 col-12 justify-content-end">
                                    <div class="row g-1">
                                        <div class="col-md-6 d-flex justify-content-end justify-content-md-start">
                                            <a id="contact-copy" href="" class="metadata-action-link custom-link">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                        <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                    </svg>
                                                </span>
                                                <span>${copyFromAnotherLink?lower_case?cap_first}</span>
                                            </a>
                                        </div>
                                        <div class="col-md-6 d-flex justify-content-end">
                                            <a id="contact-removeLink" class="removeAgentLink metadata-action-link custom-link" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                    </svg>
                                                </span>
                                                <span>${removeContactLink?lower_case?cap_first}</span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-5">
                                    <@input name="eml.contact.firstName" i18nkey="eml.contact.firstName"/>
                                </div>
                                <div class="col-md-5">
                                    <@input name="eml.contact.lastName" i18nkey="eml.contact.lastName" />
                                </div>
                                <div class="col-md-2">
                                    <@input name="eml.contact.salutation" i18nkey="eml.contact.salutation"/>
                                </div>
                                <div class="col-md-6">
                                    <@input name="eml.contact.organisation" i18nkey="eml.contact.organisation"  />
                                </div>
                                <div class="col-12">
                                    <div id="contact-positions" class="contact-positions">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.contacts.position" class="form-label mb-0">
                                                <@s.text name="eml.contact.position"/>
                                            </label>
                                            <a id="plus-contact-position" href="" class="metadata-action-link custom-link add-contact-position">
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
                                    <div id="contact-address" class="contact-address">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.contacts.address" class="form-label mb-0">
                                                <@s.text name="eml.contact.address.address"/>
                                            </label>
                                            <a id="plus-contact-address" href="" class="metadata-action-link custom-link add-agent-contact-info">
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
                                    <@input name="eml.contact.address.city" i18nkey="eml.contact.address.city" />
                                </div>
                                <div class="col-md-6 col-lg-3">
                                    <@input name="eml.contact.address.province" i18nkey="eml.contact.address.province" />
                                </div>
                                <div class="countryList col-md-6 col-lg-3">
                                    <@select name="country" options=countries help="i18n" i18nkey="eml.contact.address.country" />
                                </div>
                                <div class="col-md-6 col-lg-2">
                                    <@input name="eml.contact.address.postalCode" i18nkey="eml.contact.address.postalCode" />
                                </div>
                                <div class="col-12">
                                    <div id="contact-phones" class="contact-phones">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.contacts.phone" class="form-label mb-0">
                                                <@s.text name="eml.contact.phone"/>
                                            </label>
                                            <a id="plus-contact-phone" href="" class="metadata-action-link custom-link add-contact-phone">
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
                                    <div id="contact-emails" class="contact-emails">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.contacts.email" class="form-label mb-0">
                                                <@s.text name="eml.contact.email"/>
                                            </label>
                                            <a id="plus-contact-email" href="" class="metadata-action-link custom-link add-agent-contact-info">
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
                                    <div id="contact-homepages" class="contact-homepages">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.contacts.homepage" class="form-label mb-0">
                                                <@s.text name="eml.contact.homepage"/>
                                            </label>
                                            <a id="plus-contact-homepage" href="" class="metadata-action-link custom-link add-agent-contact-info">
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
                                    <div id="contact-identifiers" class="contact-identifiers">
                                        <div class="d-flex text-smaller">
                                            <a tabindex="0" role="button"
                                               class="popover-link"
                                               data-bs-toggle="popover"
                                               data-bs-trigger="focus"
                                               data-bs-html="true"
                                               data-bs-content="<@s.text name='eml.contact.directory.help'/><br><br><@s.text name='eml.contact.identifier.help'/>">
                                                <i class="bi bi-info-circle text-gbif-primary px-1"></i>
                                            </a>
                                            <label for="eml.contacts.userIds" class="form-label mb-0">
                                                <@s.text name="eml.contact.identifier"/>
                                            </label>
                                            <a id="plus-contact-identifier" href="" class="metadata-action-link custom-link add-identifier">
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
                        </div>

                        <div class="my-3 p-3">
                            <!-- Resource Creators -->
                            <@textinline name="eml.resourceCreator.plural" help="i18n" requiredField=true/>
                            <div id="creator-items">
                                <#list eml.creators as creator>
                                    <div id="creator-item-${creator_index}" class="item row g-3 pb-4 border-bottom">
                                        <div class="handle columnLinks mt-4 col-12">
                                            <div class="row g-1">
                                                <div class="col-md-6 d-flex justify-content-end justify-content-md-start">
                                                    <span class="contact-citation-banner"><@s.text name="eml.contact.citation"/></span>
                                                </div>
                                                <div class="col-md-6 d-flex justify-content-end">
                                                    <div class="btn-group">
                                                        <a id="dropdown-creator-copy-${creator_index}" href="#" class="metadata-action-link dropdown-toggle custom-link" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                                            <span>
                                                                <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
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
                                                    <a id="creator-removeLink-${creator_index}" class="removeAgentLink metadata-action-link custom-link" href="">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${removeCreatorLink?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-md-5">
                                            <@input name="eml.creators[${creator_index}].firstName" i18nkey="eml.resourceCreator.firstName"/>
                                        </div>
                                        <div class="col-md-5">
                                            <@input name="eml.creators[${creator_index}].lastName" i18nkey="eml.resourceCreator.lastName" />
                                        </div>
                                        <div class="col-md-2">
                                            <@input name="eml.creators[${creator_index}].salutation" i18nkey="eml.resourceCreator.salutation"/>
                                        </div>
                                        <div class="col-md-6">
                                            <@input name="eml.creators[${creator_index}].organisation" i18nkey="eml.resourceCreator.organisation"  />
                                        </div>
                                        <div class="col-12">
                                            <div id="creator-${creator_index}-positions" class="creator-positions">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.contacts.position" class="form-label mb-0">
                                                        <@s.text name="eml.contact.position"/>
                                                    </label>
                                                    <a id="plus-creator-position-${creator_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.creators[creator_index].position as position>
                                                    <div id="creator-${creator_index}-position-${position_index}" class="position-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.creators[${creator_index}].position[${position_index}]" i18nkey="eml.resourceCreator.position" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="creator-position-remove-${creator_index}-${position_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <div id="creator-${creator_index}-address" class="creator-address">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.creators.address" class="form-label mb-0">
                                                        <@s.text name="eml.contact.address.address"/>
                                                    </label>
                                                    <a id="plus-creator-address-${creator_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.creators[creator_index].address.address as address>
                                                    <div id="creator-${creator_index}-address-${address_index}" class="address-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.creators[${creator_index}].address.address[${address_index}]" i18nkey="eml.creator.address.address" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="creator-address-remove-${creator_index}-${address_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <@input name="eml.creators[${creator_index}].address.city" i18nkey="eml.resourceCreator.address.city" />
                                        </div>
                                        <div class="col-md-6 col-lg-3">
                                            <@input name="eml.creators[${creator_index}].address.province" i18nkey="eml.resourceCreator.address.province" />
                                        </div>
                                        <div class="countryList col-md-6 col-lg-3">
                                            <@select name="eml.creators[${creator_index}].address.country" help="i18n" options=countries i18nkey="eml.resourceCreator.address.country" value="${eml.creators[creator_index].address.country!}"/>
                                        </div>
                                        <div class="col-md-6 col-lg-2">
                                            <@input name="eml.creators[${creator_index}].address.postalCode" i18nkey="eml.resourceCreator.address.postalCode" />
                                        </div>
                                        <div class="col-12">
                                            <div id="creator-${creator_index}-phones" class="creator-phones">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.creators.phone" class="form-label mb-0">
                                                        <@s.text name="eml.contact.phone"/>
                                                    </label>
                                                    <a id="plus-creator-phone-${creator_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.creators[creator_index].phone as phone>
                                                    <div id="creator-${creator_index}-phone-${phone_index}" class="phone-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.creators[${creator_index}].phone[${phone_index}]" i18nkey="eml.creator.phone" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="creator-phone-remove-${creator_index}-${phone_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <div id="creator-${creator_index}-emails" class="creator-emails">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.creators.email" class="form-label mb-0">
                                                        <@s.text name="eml.contact.email"/>
                                                    </label>
                                                    <a id="plus-creator-email-${creator_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.creators[creator_index].email as email>
                                                    <div id="creator-${creator_index}-email-${email_index}" class="email-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.creators[${creator_index}].email[${email_index}]" i18nkey="eml.resourceCreator.email" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="creator-email-remove-${creator_index}-${email_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <div id="creator-${creator_index}-homepages" class="creator-homepages">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.creators.homepage" class="form-label mb-0">
                                                        <@s.text name="eml.contact.homepage"/>
                                                    </label>
                                                    <a id="plus-creator-homepage-${creator_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.creators[creator_index].homepage as homepage>
                                                    <div id="creator-${creator_index}-homepage-${homepage_index}" class="homepage-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.creators[${creator_index}].homepage[${homepage_index}]" i18nkey="eml.resourceCreator.homepage" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="creator-homepage-remove-${creator_index}-${homepage_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <div id="creator-${creator_index}-identifiers" class="creator-identifiers">
                                                <div class="d-flex text-smaller">
                                                    <a tabindex="0" role="button"
                                                       class="popover-link"
                                                       data-bs-toggle="popover"
                                                       data-bs-trigger="focus"
                                                       data-bs-html="true"
                                                       data-bs-content="<@s.text name='eml.contact.directory.help'/><br><br><@s.text name='eml.contact.identifier.help'/>">
                                                        <i class="bi bi-info-circle text-gbif-primary px-1"></i>
                                                    </a>
                                                    <label for="eml.creators.userIds" class="form-label mb-0">
                                                        <@s.text name="eml.contact.identifier"/>
                                                    </label>
                                                    <a id="plus-creator-identifier-${creator_index}" href="" class="metadata-action-link custom-link add-identifier">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list creator.userIds as userId>
                                                    <div id="creator-${creator_index}-identifier-${userId_index}" class="identifier-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-4">
                                                                <@select name="eml.creators[${creator_index}].userIds[${userId_index}].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" withLabel=false value="${userIdDirecotriesExtended[(userId.directory)!]!}"/>
                                                            </div>
                                                            <div class="col-md-4 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.creators[${creator_index}].userIds[${userId_index}].identifier" help="i18n" i18nkey="eml.contact.identifier" withLabel=false placeholder="${inputIdentifierPlaceholder}" value="${(userId.identifier)!}"/>
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="creator-identifier-remove-${creator_index}-${userId_index}" class="removeIdentifier metadata-action-link" href="">
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
                                <a id="plus-creator" href="" class="plus-agent metadata-action-link custom-link">
                                    <span>
                                        <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                        </svg>
                                    </span>
                                    <span>${addCreatorLink?lower_case?cap_first}</span>
                                </a>
                            </div>

                            <div id="baseItem-creator" class="item row g-3 pb-4 border-bottom" style="display:none;">
                                <div class="handle columnLinks mt-4 col-12">
                                    <div class="row g-1">
                                        <div class="col-md-6 d-flex justify-content-end justify-content-md-start">
                                            <span class="contact-citation-banner"><@s.text name="eml.contact.citation"/></span>
                                        </div>
                                        <div class="col-md-6 d-flex justify-content-end">
                                            <div class="btn-group">
                                                <a id="dropdown-creator-copy" href="#" class="metadata-action-link dropdown-toggle custom-link" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
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
                                            <a id="creator-removeLink" class="removeAgentLink metadata-action-link custom-link" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                    </svg>
                                                </span>
                                                <span>${removeCreatorLink?lower_case?cap_first}</span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-5">
                                    <@input name="eml.creator.firstName" i18nkey="eml.resourceCreator.firstName"/>
                                </div>
                                <div class="col-md-5">
                                    <@input name="eml.creator.lastName" i18nkey="eml.resourceCreator.lastName"/>
                                </div>
                                <div class="col-md-2">
                                    <@input name="eml.creator.salutation" i18nkey="eml.resourceCreator.salutation"/>
                                </div>
                                <div class="col-md-6">
                                    <@input name="eml.creator.organisation" i18nkey="eml.resourceCreator.organisation" />
                                </div>
                                <div class="col-12">
                                    <div id="creator-positions" class="creator-positions">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.creators.position" class="form-label mb-0">
                                                <@s.text name="eml.contact.position"/>
                                            </label>
                                            <a id="plus-creator-position" href="" class="metadata-action-link custom-link add-agent-contact-info">
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
                                    <div id="creator-address" class="creator-address">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.creators.address" class="form-label mb-0">
                                                <@s.text name="eml.contact.address.address"/>
                                            </label>
                                            <a id="plus-creator-address" href="" class="metadata-action-link custom-link add-agent-contact-info">
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
                                    <@input name="eml.creator.address.city" i18nkey="eml.resourceCreator.address.city" />
                                </div>
                                <div class="col-md-6 col-lg-3">
                                    <@input name="eml.creator.address.province" i18nkey="eml.resourceCreator.address.province" />
                                </div>
                                <div class="countryList col-md-6 col-lg-3">
                                    <@select name="country" options=countries help="i18n" i18nkey="eml.resourceCreator.address.country" />
                                </div>
                                <div class="col-md-6 col-lg-2">
                                    <@input name="eml.creator.address.postalCode" i18nkey="eml.resourceCreator.address.postalCode" />
                                </div>
                                <div class="col-12">
                                    <div id="creator-phones" class="creator-phones">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.creators.phone" class="form-label mb-0">
                                                <@s.text name="eml.contact.phone"/>
                                            </label>
                                            <a id="plus-creator-phone" href="" class="metadata-action-link custom-link add-agent-contact-info">
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
                                    <div id="creator-emails" class="creator-emails">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.creators.email" class="form-label mb-0">
                                                <@s.text name="eml.contact.email"/>
                                            </label>
                                            <a id="plus-creator-email" href="" class="metadata-action-link custom-link add-agent-contact-info">
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
                                    <div id="creator-homepages" class="creator-homepages">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.creators.homepage" class="form-label mb-0">
                                                <@s.text name="eml.contact.homepage"/>
                                            </label>
                                            <a id="plus-creator-homepage" href="" class="metadata-action-link custom-link add-agent-contact-info">
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
                                    <div id="creator-identifiers" class="creator-identifiers">
                                        <div class="d-flex text-smaller">
                                            <a tabindex="0" role="button"
                                               class="popover-link"
                                               data-bs-toggle="popover"
                                               data-bs-trigger="focus"
                                               data-bs-html="true"
                                               data-bs-content="<@s.text name='eml.contact.directory.help'/><br><br><@s.text name='eml.contact.identifier.help'/>">
                                                <i class="bi bi-info-circle text-gbif-primary px-1"></i>
                                            </a>
                                            <label for="eml.creators.userIds" class="form-label mb-0">
                                                <@s.text name="eml.contact.identifier"/>
                                            </label>
                                            <a id="plus-creator-identifier" href="" class="metadata-action-link custom-link add-identifier">
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
                        </div>

                        <div class="my-3 p-3">
                            <!-- Metadata Providers -->
                            <@textinline name="eml.metadataProvider.plural" help="i18n"/>
                            <div id="metadataProvider-items">
                                <#list eml.metadataProviders as metadataProvider>
                                    <div id="metadataProvider-item-${metadataProvider_index}" class="item row g-3 pb-4 border-bottom">
                                        <div class="handle columnLinks mt-4 col-12">
                                            <div class="row g-1">
                                                <div class="col-md-6 d-flex justify-content-end justify-content-md-start">
                                                    <span class="contact-citation-banner"><@s.text name="eml.contact.citation"/></span>
                                                </div>
                                                <div class="col-md-6 d-flex justify-content-end">
                                                    <div class="btn-group">
                                                        <a id="dropdown-metadataProvider-copy-${metadataProvider_index}" href="#" class="metadata-action-link dropdown-toggle custom-link" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                                            <span>
                                                                <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
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
                                                    <a id="metadataProvider-removeLink-${metadataProvider_index}" class="removeAgentLink metadata-action-link custom-link" href="">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${removeMetadataProviderLink?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-md-5">
                                            <@input name="eml.metadataProviders[${metadataProvider_index}].firstName" i18nkey="eml.metadataProvider.firstName"/>
                                        </div>
                                        <div class="col-md-5">
                                            <@input name="eml.metadataProviders[${metadataProvider_index}].lastName" i18nkey="eml.metadataProvider.lastName" />
                                        </div>
                                        <div class="col-md-2">
                                            <@input name="eml.metadataProviders[${metadataProvider_index}].salutation" i18nkey="eml.metadataProvider.salutation"/>
                                        </div>
                                        <div class="col-md-6">
                                            <@input name="eml.metadataProviders[${metadataProvider_index}].organisation" i18nkey="eml.metadataProvider.organisation" />
                                        </div>
                                        <div class="col-12">
                                            <div id="metadataProvider-${metadataProvider_index}-positions" class="metadataProvider-positions">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.contacts.position" class="form-label mb-0">
                                                        <@s.text name="eml.contact.position"/>
                                                    </label>
                                                    <a id="plus-metadataProvider-position-${metadataProvider_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.metadataProviders[metadataProvider_index].position as position>
                                                    <div id="metadataProvider-${metadataProvider_index}-position-${position_index}" class="position-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.metadataProviders[${metadataProvider_index}].position[${position_index}]" i18nkey="eml.metadataProvider.position" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="metadataProvider-position-remove-${metadataProvider_index}-${position_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <div id="metadataProvider-${metadataProvider_index}-address" class="metadataProvider-address">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.contacts.address" class="form-label mb-0">
                                                        <@s.text name="eml.contact.address.address"/>
                                                    </label>
                                                    <a id="plus-metadataProvider-address-${metadataProvider_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.metadataProviders[metadataProvider_index].address.address as address>
                                                    <div id="metadataProvider-${metadataProvider_index}-address-${address_index}" class="address-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.metadataProviders[${metadataProvider_index}].address.address[${address_index}]" i18nkey="eml.metadataProvider.address" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="metadataProvider-address-remove-${metadataProvider_index}-${address_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <@input name="eml.metadataProviders[${metadataProvider_index}].address.city" i18nkey="eml.metadataProvider.address.city" />
                                        </div>
                                        <div class="col-md-6 col-lg-3">
                                            <@input name="eml.metadataProviders[${metadataProvider_index}].address.province" i18nkey="eml.metadataProvider.address.province" />
                                        </div>
                                        <div class="countryList col-md-6 col-lg-3">
                                            <@select name="eml.metadataProviders[${metadataProvider_index}].address.country" help="i18n" options=countries i18nkey="eml.metadataProvider.address.country" value="${eml.metadataProviders[metadataProvider_index].address.country!}"/>
                                        </div>
                                        <div class="col-md-6 col-lg-2">
                                            <@input name="eml.metadataProviders[${metadataProvider_index}].address.postalCode" i18nkey="eml.metadataProvider.address.postalCode" />
                                        </div>
                                        <div class="col-12">
                                            <div id="metadataProvider-${metadataProvider_index}-phones" class="metadataProvider-phones">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.metadataProviders.phone" class="form-label mb-0">
                                                        <@s.text name="eml.contact.phone"/>
                                                    </label>
                                                    <a id="plus-metadataProvider-phone-${metadataProvider_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.metadataProviders[metadataProvider_index].phone as phone>
                                                    <div id="metadataProvider-${metadataProvider_index}-phone-${phone_index}" class="phone-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.metadataProviders[${metadataProvider_index}].phone[${phone_index}]" i18nkey="eml.metadataProvider.phone" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="metadataProvider-phone-remove-${metadataProvider_index}-${phone_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <div id="metadataProvider-${metadataProvider_index}-emails" class="metadataProvider-emails">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.metadataProviders.email" class="form-label mb-0">
                                                        <@s.text name="eml.contact.email"/>
                                                    </label>
                                                    <a id="plus-metadataProvider-email-${metadataProvider_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.metadataProviders[metadataProvider_index].email as email>
                                                    <div id="metadataProvider-${metadataProvider_index}-email-${email_index}" class="email-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.metadataProviders[${metadataProvider_index}].email[${email_index}]" i18nkey="eml.metadataProvider.email" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="metadataProvider-email-remove-${metadataProvider_index}-${email_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <div id="metadataProvider-${metadataProvider_index}-homepages" class="metadataProvider-homepages">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.metadataProviders.homepage" class="form-label mb-0">
                                                        <@s.text name="eml.contact.homepage"/>
                                                    </label>
                                                    <a id="plus-metadataProvider-homepage-${metadataProvider_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.metadataProviders[metadataProvider_index].homepage as homepage>
                                                    <div id="metadataProvider-${metadataProvider_index}-homepage-${homepage_index}" class="homepage-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.metadataProviders[${metadataProvider_index}].homepage[${homepage_index}]" i18nkey="eml.metadataProvider.homepage" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="metadataProvider-homepage-remove-${metadataProvider_index}-${homepage_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <div id="metadataProvider-${metadataProvider_index}-identifiers" class="metadataProvider-identifiers">
                                                <div class="d-flex text-smaller">
                                                    <a tabindex="0" role="button"
                                                       class="popover-link"
                                                       data-bs-toggle="popover"
                                                       data-bs-trigger="focus"
                                                       data-bs-html="true"
                                                       data-bs-content="<@s.text name='eml.contact.directory.help'/><br><br><@s.text name='eml.contact.identifier.help'/>">
                                                        <i class="bi bi-info-circle text-gbif-primary px-1"></i>
                                                    </a>
                                                    <label for="eml.contacts.userIds" class="form-label mb-0">
                                                        <@s.text name="eml.contact.identifier"/>
                                                    </label>
                                                    <a id="plus-metadataProvider-identifier-${metadataProvider_index}" href="" class="metadata-action-link custom-link add-identifier">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list metadataProvider.userIds as userId>
                                                    <div id="metadataProvider-${metadataProvider_index}-identifier-${userId_index}" class="identifier-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-4">
                                                                <@select name="eml.metadataProviders[${metadataProvider_index}].userIds[${userId_index}].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" withLabel=false value="${userIdDirecotriesExtended[(userId.directory)!]!}"/>
                                                            </div>
                                                            <div class="col-md-4">
                                                                <@input name="eml.metadataProviders[${metadataProvider_index}].userIds[${userId_index}].identifier" help="i18n" i18nkey="eml.contact.identifier" withLabel=false placeholder="${inputIdentifierPlaceholder}" value="${(userId.identifier)!}"/>
                                                            </div>
                                                            <div class="col-md-4 mb-auto py-1">
                                                                <a id="metadataProvider-identifier-remove-${metadataProvider_index}-${userId_index}" class="removeIdentifier metadata-action-link" href="">
                                                                    <span>
                                                                        <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                                        </svg>
                                                                    </span>
                                                                </a>
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
                                <a id="plus-metadataProvider" href="" class="plus-agent metadata-action-link custom-link">
                                    <span>
                                        <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                        </svg>
                                    </span>
                                    <span>${addMetadataProviderLink?lower_case?cap_first}</span>
                                </a>
                            </div>

                            <div id="baseItem-metadataProvider" class="item row g-3 pb-4 border-bottom" style="display:none;">
                                <div class="handle columnLinks mt-4 col-12">
                                    <div class="row g-1">
                                        <div class="col-md-6 d-flex justify-content-end justify-content-md-start">
                                            <span class="contact-citation-banner"><@s.text name="eml.contact.citation"/></span>
                                        </div>
                                        <div class="col-md-6 d-flex justify-content-end">
                                            <div class="btn-group">
                                                <a id="dropdown-metadataProvider-copy" href="#" class="metadata-action-link dropdown-toggle custom-link" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
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
                                            <a id="metadataProvider-removeLink" class="removeAgentLink metadata-action-link custom-link" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                    </svg>
                                                </span>
                                                <span>${removeMetadataProviderLink?lower_case?cap_first}</span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-5">
                                    <@input name="eml.metadataProvider.firstName" i18nkey="eml.metadataProvider.firstName"/>
                                </div>
                                <div class="col-md-5">
                                    <@input name="eml.metadataProvider.lastName" i18nkey="eml.metadataProvider.lastName" />
                                </div>
                                <div class="col-md-2">
                                    <@input name="eml.metadataProvider.salutation" i18nkey="eml.metadataProvider.salutation"/>
                                </div>
                                <div class="col-md-6">
                                    <@input name="eml.metadataProvider.organisation" i18nkey="eml.metadataProvider.organisation"  />
                                </div>
                                <div class="col-12">
                                    <div id="metadataProvider-positions" class="metadataProvider-positions">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.metadataProviders.position" class="form-label mb-0">
                                                <@s.text name="eml.contact.position"/>
                                            </label>
                                            <a id="plus-metadataProvider-position" href="" class="metadata-action-link custom-link add-agent-contact-info">
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
                                    <div id="metadataProvider-address" class="metadataProvider-address">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.metadataProviders.address" class="form-label mb-0">
                                                <@s.text name="eml.contact.address.address"/>
                                            </label>
                                            <a id="plus-metadataProvider-address" href="" class="metadata-action-link custom-link add-agent-contact-info">
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
                                    <@input name="eml.metadataProvider.address.city" i18nkey="eml.metadataProvider.address.city" />
                                </div>
                                <div class="col-md-6 col-lg-3">
                                    <@input name="eml.metadataProvider.address.province" i18nkey="eml.metadataProvider.address.province" />
                                </div>
                                <div class="countryList col-md-6 col-lg-3">
                                    <@select name="country" options=countries help="i18n" i18nkey="eml.metadataProvider.address.country" />
                                </div>
                                <div class="col-md-6 col-lg-2">
                                    <@input name="eml.metadataProvider.address.postalCode" i18nkey="eml.metadataProvider.address.postalCode" />
                                </div>
                                <div class="col-12">
                                    <div id="metadataProvider-phones" class="metadataProvider-phones">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.metadataProviders.phone" class="form-label mb-0">
                                                <@s.text name="eml.contact.phone"/>
                                            </label>
                                            <a id="plus-metadataProvider-phone" href="" class="metadata-action-link custom-link add-agent-contact-info">
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
                                    <div id="metadataProvider-emails" class="metadataProvider-emails">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.metadataProviders.email" class="form-label mb-0">
                                                <@s.text name="eml.contact.email"/>
                                            </label>
                                            <a id="plus-metadataProvider-email" href="" class="metadata-action-link custom-link add-agent-contact-info">
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
                                    <div id="metadataProvider-homepages" class="metadataProvider-homepages">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.metadataProviders.homepage" class="form-label mb-0">
                                                <@s.text name="eml.contact.homepage"/>
                                            </label>
                                            <a id="plus-metadataProvider-homepage" href="" class="metadata-action-link custom-link add-agent-contact-info">
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
                                    <div id="metadataProvider-identifiers" class="metadataProvider-identifiers">
                                        <div class="d-flex text-smaller">
                                            <a tabindex="0" role="button"
                                               class="popover-link"
                                               data-bs-toggle="popover"
                                               data-bs-trigger="focus"
                                               data-bs-html="true"
                                               data-bs-content="<@s.text name='eml.contact.directory.help'/><br><br><@s.text name='eml.contact.identifier.help'/>">
                                                <i class="bi bi-info-circle text-gbif-primary px-1"></i>
                                            </a>
                                            <label for="eml.metadataProviders.userIds" class="form-label mb-0">
                                                <@s.text name="eml.contact.identifier"/>
                                            </label>
                                            <a id="plus-metadataProvider-identifier" href="" class="metadata-action-link custom-link add-identifier">
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

                        <div class="my-3 p-3">
                            <!-- Associated parties -->
                            <@textinline name="manage.metadata.parties.title" help="i18n"/>

                            <!-- retrieve some link names one time -->
                            <#assign copyLink><@s.text name="eml.metadataAgent.copyLink"/></#assign>
                            <#assign removeLink><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.parties.item'/></#assign>
                            <#assign addLink><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.parties.item'/></#assign>
                            <#assign copyFromAnotherLink><@s.text name="eml.metadataAgent.copyFromAnother"/></#assign>
                            <#assign inputIdentifierPlaceholder><@s.text name="eml.contact.identifier"/></#assign>
                            <#assign addNew><@s.text name='manage.metadata.addnew'/></#assign>

                            <div id="associatedParty-items">
                                <#list eml.associatedParties as item>
                                    <div id="associatedParty-item-${item_index}" class="item row g-3 pb-4 border-bottom">
                                        <div class="handle columnLinks mt-4 col-12">
                                            <div class="row g-1">
                                                <div class="col-md-6 d-flex justify-content-end justify-content-md-start">
                                                    <#if item.role?? && (item.role == 'originator' || item.role == 'metadataProvider')>
                                                    <span class="contact-citation-banner"><@s.text name="eml.contact.citation"/></span>
                                                    </#if>
                                                </div>
                                                <div class="col-md-6 d-flex justify-content-end">
                                                    <a id="associatedParty-copy-${item_index}" href="" class="metadata-action-link custom-link">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${copyFromAnotherLink?lower_case?cap_first}</span>
                                                    </a>
                                                    <a id="associatedParty-removeLink-${item_index}" class="removeAgentLink metadata-action-link custom-link" href="">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${removeLink?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-md-5">
                                            <@input name="eml.associatedParties[${item_index}].firstName" i18nkey="eml.associatedParties.firstName"/>
                                        </div>
                                        <div class="col-md-5">
                                            <@input name="eml.associatedParties[${item_index}].lastName" i18nkey="eml.associatedParties.lastName" />
                                        </div>
                                        <div class="col-md-2">
                                            <@input name="eml.associatedParties[${item_index}].salutation" i18nkey="eml.associatedParties.salutation"/>
                                        </div>
                                        <div class="col-md-6">
                                            <@input name="eml.associatedParties[${item_index}].organisation" i18nkey="eml.associatedParties.organisation"  />
                                        </div>
                                        <div class="col-md-6">
                                            <@select name="eml.associatedParties[${item_index}].role" i18nkey="eml.associatedParties.role" help="i18n" value="${(eml.associatedParties[item_index].role)!}" options=roles />
                                        </div>
                                        <div class="col-12">
                                            <div id="associatedParty-${item_index}-positions" class="associatedParty-positions">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.associatedParties.position" class="form-label mb-0">
                                                        <@s.text name="eml.contact.position"/>
                                                    </label>
                                                    <a id="plus-associatedParty-position-${item_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.associatedParties[item_index].position as position>
                                                    <div id="associatedParty-${item_index}-position-${position_index}" class="position-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.associatedParties[${item_index}].position[${position_index}]" i18nkey="eml.associatedParties.position" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="associatedParty-position-remove-${item_index}-${position_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <div id="associatedParty-${item_index}-address" class="associatedParty-address">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.associatedParties.address" class="form-label mb-0">
                                                        <@s.text name="eml.contact.address.address"/>
                                                    </label>
                                                    <a id="plus-associatedParty-address-${item_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.associatedParties[item_index].address.address as address>
                                                    <div id="associatedParty-${item_index}-address-${address_index}" class="address-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.associatedParties[${item_index}].address.address[${address_index}]" i18nkey="eml.associatedParties.address.address" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="associatedParty-address-remove-${item_index}-${address_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <@input name="eml.associatedParties[${item_index}].address.city" i18nkey="eml.associatedParties.address.city" />
                                        </div>
                                        <div class="col-md-6 col-lg-3">
                                            <@input name="eml.associatedParties[${item_index}].address.province" i18nkey="eml.associatedParties.address.province" />
                                        </div>
                                        <div class="countryList col-md-6 col-lg-3">
                                            <@select name="eml.associatedParties[${item_index}].address.country" help="i18n" options=countries i18nkey="eml.associatedParties.address.country" value="${eml.associatedParties[item_index].address.country!}"/>
                                        </div>
                                        <div class="col-md-6 col-lg-2">
                                            <@input name="eml.associatedParties[${item_index}].address.postalCode" i18nkey="eml.associatedParties.address.postalCode" />
                                        </div>
                                        <div class="col-12">
                                            <div id="associatedParty-${item_index}-phones" class="associatedParty-phones">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.associatedParties.phone" class="form-label mb-0">
                                                        <@s.text name="eml.contact.phone"/>
                                                    </label>
                                                    <a id="plus-associatedParty-phone-${item_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.associatedParties[item_index].phone as phone>
                                                    <div id="associatedParty-${item_index}-phone-${phone_index}" class="phone-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.associatedParties[${item_index}].phone[${phone_index}]" i18nkey="eml.associatedParties.phone" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="associatedParty-phone-remove-${item_index}-${phone_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <div id="associatedParty-${item_index}-emails" class="associatedParty-emails">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.associatedParties.email" class="form-label mb-0">
                                                        <@s.text name="eml.contact.email"/>
                                                    </label>
                                                    <a id="plus-associatedParty-email-${item_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.associatedParties[item_index].email as email>
                                                    <div id="associatedParty-${item_index}-email-${email_index}" class="email-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.associatedParties[${item_index}].email[${email_index}]" i18nkey="eml.associatedParties.email" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="associatedParty-email-remove-${item_index}-${email_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <div id="associatedParty-${item_index}-homepages" class="associatedParty-homepages">
                                                <div class="d-flex text-smaller">
                                                    <label for="eml.associatedParties.homepage" class="form-label mb-0">
                                                        <@s.text name="eml.contact.homepage"/>
                                                    </label>
                                                    <a id="plus-associatedParty-homepage-${item_index}" href="" class="metadata-action-link custom-link add-agent-contact-info">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list eml.associatedParties[item_index].homepage as homepage>
                                                    <div id="associatedParty-${item_index}-homepage-${homepage_index}" class="homepage-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-6 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.associatedParties[${item_index}].homepage[${homepage_index}]" i18nkey="eml.associatedParties.homepage" type="url" withLabel=false />
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="associatedParty-homepage-remove-${item_index}-${homepage_index}" class="removeSubEntity metadata-action-link" href="">
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
                                            <div id="associatedParty-${item_index}-identifiers" class="associatedParty-identifiers">
                                                <div class="d-flex text-smaller">
                                                    <a tabindex="0" role="button"
                                                       class="popover-link"
                                                       data-bs-toggle="popover"
                                                       data-bs-trigger="focus"
                                                       data-bs-html="true"
                                                       data-bs-content="<@s.text name='eml.contact.directory.help'/><br><br><@s.text name='eml.contact.identifier.help'/>">
                                                        <i class="bi bi-info-circle text-gbif-primary px-1"></i>
                                                    </a>
                                                    <label for="eml.associatedParties.userIds" class="form-label mb-0">
                                                        <@s.text name="eml.contact.identifier"/>
                                                    </label>
                                                    <a id="plus-associatedParty-identifier-${item_index}" href="" class="metadata-action-link custom-link add-identifier">
                                                        <span>
                                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                                            </svg>
                                                        </span>
                                                        <span>${addNew?lower_case?cap_first}</span>
                                                    </a>
                                                </div>
                                                <#list item.userIds as userId>
                                                    <div id="associatedParty-${item_index}-identifier-${userId_index}" class="identifier-item">
                                                        <div class="row g-2 mt-0">
                                                            <div class="col-md-4">
                                                                <@select name="eml.associatedParties[${item_index}].userIds[${userId_index}].directory" help="i18n" options=userIdDirectories i18nkey="eml.associatedParties.directory" withLabel=false value="${userIdDirecotriesExtended[(userId.directory)!]!}"/>
                                                            </div>
                                                            <div class="col-md-4 d-flex">
                                                                <div class="flex-grow-1">
                                                                    <@input name="eml.associatedParties[${item_index}].userIds[${userId_index}].identifier" help="i18n" i18nkey="eml.associatedParties.identifier" withLabel=false placeholder="${inputIdentifierPlaceholder}" value="${(userId.identifier)!}"/>
                                                                </div>
                                                                <div class="mb-auto py-1 ps-1">
                                                                    <a id="associatedParty-identifier-remove-${item_index}-${userId_index}" class="removeIdentifier metadata-action-link" href="">
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

                            <div class="addNew col-12 mt-2">
                                <a id="plus-associatedParty" href="" class="plus-agent metadata-action-link custom-link">
                                    <span>
                                        <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                        </svg>
                                    </span>
                                    <span>${addLink?lower_case?cap_first}</span>
                                </a>
                            </div>

                            <div id="baseItem-associatedParty" class="item row g-3 pb-4 border-bottom" style="display:none;">
                                <div class="handle columnLinks mt-4 col-12">
                                    <div class="row g-1">
                                        <div class="col-md-6 d-flex justify-content-end justify-content-md-start">
                                            <span class="contact-citation-banner" style="display: none;"><@s.text name="eml.contact.citation"/></span>
                                        </div>
                                        <div class="col-md-6 d-flex justify-content-end">
                                            <a id="associatedParty-copy" href="" class="metadata-action-link custom-link">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                        <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                    </svg>
                                                </span>
                                                <span>${copyFromAnotherLink?lower_case?cap_first}</span>
                                            </a>
                                            <a id="associatedParty-removeLink" class="removeAgentLink metadata-action-link custom-link" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                    </svg>
                                                </span>
                                                <span>${removeLink?lower_case?cap_first}</span>
                                            </a>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-5">
                                    <@input name="firstName" i18nkey="eml.associatedParties.firstName"/>
                                </div>
                                <div class="col-md-5">
                                    <@input name="lastName" i18nkey="eml.associatedParties.lastName" />
                                </div>
                                <div class="col-md-2">
                                    <@input name="salutation" i18nkey="eml.associatedParties.salutation"/>
                                </div>
                                <div class="col-md-6">
                                    <@input name="organisation" i18nkey="eml.associatedParties.organisation"  />
                                </div>
                                <div class="col-md-6">
                                    <@select name="role" i18nkey="eml.associatedParties.role" help="i18n" options=roles />
                                </div>
                                <div class="col-12">
                                    <div id="associatedParty-positions" class="associatedParty-positions">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.associatedParties..position" class="form-label mb-0">
                                                <@s.text name="eml.associatedParties.position"/>
                                            </label>
                                            <a id="plus-associatedParty-position" href="" class="metadata-action-link custom-link add-agent-contact-info">
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
                                    <div id="associatedParty-address" class="associatedParty-address">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.associatedParties..address" class="form-label mb-0">
                                                <@s.text name="eml.associatedParties.address.address"/>
                                            </label>
                                            <a id="plus-associatedParty-address" href="" class="metadata-action-link custom-link add-agent-contact-info">
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
                                    <@input name="address.city" i18nkey="eml.associatedParties.address.city" />
                                </div>
                                <div class="col-md-6 col-lg-3">
                                    <@input name="address.province" i18nkey="eml.associatedParties.address.province" />
                                </div>
                                <div class="countryList col-md-6 col-lg-3">
                                    <@select name="country" options=countries help="i18n" i18nkey="eml.associatedParties.address.country" />
                                </div>
                                <div class="col-md-6 col-lg-2">
                                    <@input name="address.postalCode" i18nkey="eml.associatedParties.address.postalCode" />
                                </div>
                                <div class="col-12">
                                    <div id="associatedParty-phones" class="associatedParty-phones">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.associatedParties..phone" class="form-label mb-0">
                                                <@s.text name="eml.associatedParties.phone"/>
                                            </label>
                                            <a id="plus-associatedParty-phone" href="" class="metadata-action-link custom-link add-agent-contact-info">
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
                                    <div id="associatedParty-emails" class="associatedParty-emails">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.associatedParties..email" class="form-label mb-0">
                                                <@s.text name="eml.associatedParties.email"/>
                                            </label>
                                            <a id="plus-associatedParty-email" href="" class="metadata-action-link custom-link add-agent-contact-info">
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
                                    <div id="associatedParty-homepages" class="associatedParty-homepages">
                                        <div class="d-flex text-smaller">
                                            <label for="eml.associatedParties..homepage" class="form-label mb-0">
                                                <@s.text name="eml.associatedParties.homepage"/>
                                            </label>
                                            <a id="plus-associatedParty-homepage" href="" class="metadata-action-link custom-link add-agent-contact-info">
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
                                    <div id="associatedParty-identifiers" class="associatedParty-identifiers">
                                        <div class="d-flex text-smaller">
                                            <a tabindex="0" role="button"
                                               class="popover-link"
                                               data-bs-toggle="popover"
                                               data-bs-trigger="focus"
                                               data-bs-html="true"
                                               data-bs-content="<@s.text name='eml.associatedParties.directory.help'/><br><br><@s.text name='eml.associatedParties.identifier.help'/>">
                                                <i class="bi bi-info-circle text-gbif-primary px-1"></i>
                                            </a>
                                            <label for="eml.associatedParties..userIds" class="form-label mb-0">
                                                <@s.text name="eml.associatedParties.identifier"/>
                                            </label>
                                            <a id="plus-associatedParty-identifier" href="" class="metadata-action-link custom-link add-identifier">
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

                            <div id="baseItem-position" class="position-item" style="display: none;">
                                <div class="row g-2 mt-0">
                                    <div class="col-md-6 d-flex">
                                        <@input name="baseItem-position-input" i18nkey="eml.contact.position" value="" withLabel=false />
                                    </div>
                                    <div class="mb-auto py-1 ps-1">
                                        <a id="baseItem-position-remove" class="removeSubEntity metadata-action-link" href="">
                                            <span>
                                                <svg viewBox="0 0 24 24" class="link-icon">
                                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                </svg>
                                            </span>
                                        </a>
                                    </div>
                                </div>
                            </div>

                            <div id="baseItem-phone" class="phone-item" style="display: none;">
                                <div class="row g-2 mt-0">
                                    <div class="col-md-6 d-flex">
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

                            <div id="baseItem-email" class="email-item" style="display: none;">
                                <div class="row g-2 mt-0">
                                    <div class="col-md-6 d-flex">
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

                            <div id="baseItem-homepage" class="homepage-item" style="display: none;">
                                <div class="row g-2 mt-0">
                                    <div class="col-md-6 d-flex">
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

                            <div id="baseItem-identifier" class="identifier-item" style="display: none;">
                                <div class="row g-2 mt-0">
                                    <div class="col-md-4">
                                        <@select name="baseItem-directory-select" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" value="" withLabel=false/>
                                    </div>
                                    <div class="col-md-4">
                                        <@input name="baseItem-identifier-input" help="i18n" i18nkey="eml.contact.identifier" value="" withLabel=false placeholder="${inputIdentifierPlaceholder}"/>
                                    </div>
                                    <div class="col-md-4 mb-auto py-1">
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
