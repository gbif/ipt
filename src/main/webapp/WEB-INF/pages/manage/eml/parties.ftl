<#escape x as x?html>
    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.metadata.parties.title'/></title>
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

    <#assign currentMetadataPage = "parties"/>
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <form class="needs-validation" action="metadata-${section}.do" method="post" novalidate>
        <div class="container-fluid bg-body border-bottom">
            <div class="container pt-2">
                <#include "/WEB-INF/pages/inc/action_alerts.ftl">
            </div>

            <div class="container my-3 p-3">
                <div class="text-center text-uppercase fw-bold fs-smaller-2">
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
                        <@s.text name='manage.metadata.parties.title'/>
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

        <#include "metadata_section_select.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout">

                <main class="bd-main bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "eml_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">
                        <div class="my-md-3 p-3">
                            <p class="mb-0">
                                <@s.text name='manage.metadata.parties.intro'/>
                            </p>

                            <!-- retrieve some link names one time -->
                            <#assign copyLink><@s.text name="eml.resourceCreator.copyLink"/></#assign>
                            <#assign removeLink><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.parties.item'/></#assign>
                            <#assign addLink><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.parties.item'/></#assign>

                            <div id="associatedParty-items">
                                <#list eml.associatedParties as item>
                                    <div id="associatedParty-item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                        <div class="columnLinks mt-2 d-flex justify-content-between">
                                            <div>
                                                <a id="associatedParty-copy-${item_index}" href="" class="text-smaller">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                            <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                        </svg>
                                                    </span>
                                                    <span>${copyLink?lower_case?cap_first}</span>
                                                </a>
                                            </div>
                                            <div class="text-end">
                                                <a id="associatedParty-removeLink-${item_index}" class="removeAssociatedPartyLink text-smaller" href="">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" class="link-icon">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                        </svg>
                                                    </span>
                                                    <span>${removeLink?lower_case?cap_first}</span>
                                                </a>
                                            </div>
                                        </div>
                                        <div class="col-lg-6">
                                            <@input name="eml.associatedParties[${item_index}].firstName" i18nkey="eml.associatedParties.firstName"/>
                                        </div>
                                        <div class="col-lg-6">
                                            <@input name="eml.associatedParties[${item_index}].lastName" i18nkey="eml.associatedParties.lastName" requiredField=true/>
                                        </div>
                                        <div class="col-lg-6">
                                            <@input name="eml.associatedParties[${item_index}].position" i18nkey="eml.associatedParties.position" requiredField=true />
                                        </div>
                                        <div class="col-lg-6">
                                            <@input name="eml.associatedParties[${item_index}].organisation" i18nkey="eml.associatedParties.organisation" requiredField=true />
                                        </div>
                                        <div class="col-lg-6">
                                            <@input name="eml.associatedParties[${item_index}].address.address" i18nkey="eml.associatedParties.address.address" />
                                        </div>
                                        <div class="col-lg-6">
                                            <@input name="eml.associatedParties[${item_index}].address.city" i18nkey="eml.associatedParties.address.city" />
                                        </div>
                                        <div class="col-lg-6">
                                            <@input name="eml.associatedParties[${item_index}].address.province" i18nkey="eml.associatedParties.address.province" />
                                        </div>
                                        <div class="col-lg-6 countryList">
                                            <@select name="eml.associatedParties[${item_index}].address.country" help="i18n" options=countries i18nkey="eml.associatedParties.address.country" value="${eml.associatedParties[item_index].address.country!}"/>
                                        </div>
                                        <div class="col-lg-6">
                                            <@input name="eml.associatedParties[${item_index}].address.postalCode" i18nkey="eml.associatedParties.address.postalCode" />
                                        </div>
                                        <div class="col-lg-6">
                                            <@input name="eml.associatedParties[${item_index}].phone" i18nkey="eml.associatedParties.phone" />
                                        </div>
                                        <div class="col-lg-6">
                                            <@input name="eml.associatedParties[${item_index}].email" i18nkey="eml.associatedParties.email" />
                                        </div>
                                        <div class="col-lg-6">
                                            <@input name="eml.associatedParties[${item_index}].homepage" i18nkey="eml.associatedParties.homepage" type="url" />
                                        </div>
                                        <div class="col-lg-6">
                                            <#if (eml.associatedParties[item_index].userIds[0].directory)??>
                                                <@select name="eml.associatedParties[${item_index}].userIds[0].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" value="${userIdDirecotriesExtended[eml.associatedParties[item_index].userIds[0].directory!]!}"/>
                                            <#else>
                                                <@select name="eml.associatedParties[${item_index}].userIds[0].directory" help="i18n" options=userIdDirectories i18nkey="eml.contact.directory" value=""/>
                                            </#if>
                                        </div>
                                        <div class="col-lg-6">
                                            <#if eml.associatedParties[item_index].userIds[0]??>
                                                <@input name="eml.associatedParties[${item_index}].userIds[0].identifier" help="i18n" i18nkey="eml.contact.identifier" value="${eml.associatedParties[item_index].userIds[0].identifier}"/>
                                            <#else>
                                                <@input name="eml.associatedParties[${item_index}].userIds[0].identifier" help="i18n" i18nkey="eml.contact.identifier" value=""/>
                                            </#if>
                                        </div>
                                        <div class="col-lg-6">
                                            <@select name="eml.associatedParties[${item_index}].role" i18nkey="eml.associatedParties.role" help="i18n" value="${eml.associatedParties[item_index].role!}" options=roles />
                                        </div>
                                    </div>
                                </#list>
                            </div>

                            <div class="addNew col-12 mt-2">
                                <a id="plus-associatedParty" href="" class="text-smaller">
                                    <span>
                                        <svg viewBox="0 0 24 24" class="link-icon">
                                            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                        </svg>
                                    </span>
                                    <span>${addLink?lower_case?cap_first}</span>
                                </a>
                            </div>

                            <!-- internal parameter -->
                            <input name="r" type="hidden" value="${resource.shortname}" />


                            <div id="baseItem-associatedParty" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display:none;">
                                <div class="columnLinks mt-2 d-flex justify-content-between">
                                    <div>
                                        <a id="associatedParty-copy" href="" class="text-smaller">
                                            <span>
                                                <svg viewBox="0 0 24 24" style="fill: #4BA2CE;height: 1em;vertical-align: -0.125em !important;">
                                                    <path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"></path>
                                                </svg>
                                            </span>
                                            <span>${copyLink?lower_case?cap_first}</span>
                                        </a>
                                    </div>
                                    <div class="text-end">
                                        <a id="associatedParty-removeLink" class="removeAssociatedPartyLink text-smaller" href="">
                                            <span>
                                                <svg viewBox="0 0 24 24" class="link-icon">
                                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                </svg>
                                            </span>
                                            <span>${removeLink?lower_case?cap_first}</span>
                                        </a>
                                    </div>
                                </div>
                                <div class="col-lg-6">
                                    <@input name="firstName" i18nkey="eml.associatedParties.firstName" />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="lastName" i18nkey="eml.associatedParties.lastName" requiredField=true />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="position" i18nkey="eml.associatedParties.position" requiredField=true />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="organisation" i18nkey="eml.associatedParties.organisation" requiredField=true />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="address" i18nkey="eml.associatedParties.address.address" />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="city" i18nkey="eml.associatedParties.address.city" />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="province" i18nkey="eml.associatedParties.address.province" />
                                </div>
                                <div class="col-lg-6 countryList">
                                    <@select name="country" options=countries help="i18n" i18nkey="eml.associatedParties.address.country" />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="postalCode" i18nkey="eml.associatedParties.address.postalCode" />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="phone" i18nkey="eml.associatedParties.phone" />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="email" i18nkey="eml.associatedParties.email" />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="homepage" i18nkey="eml.associatedParties.homepage" />
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
                        <div>
                            <button id="copy-agent-button" type="button" class="btn btn-outline-gbif-primary" style="display: none;"><@s.text name="button.copy"/></button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
