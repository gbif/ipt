    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <link rel="stylesheet" href="${baseURL}/styles/contacts.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>
    <#-- runs on DOMContentLoaded, after Bootstrap and html5sortable from the footer -->
    <script src="${baseURL}/js/contacts.js"></script>
    <title><@s.text name='manage.metadata.contacts.title'/></title>
    <script>
        $(document).ready(function () {
            $('#metadata-section').change(function () {
                var metadataSection = $('#metadata-section').find(':selected').val()
                $(location).attr('href', 'metadata-' + metadataSection + '.do?r=${resource.shortname!r!}');
            });

            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>
    <#include "/WEB-INF/pages/macros/user_id_directories.ftl"/>

    <#assign currentMetadataPage = "contacts"/>
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <#-- the dataset roles, in the order of the role chips -->
    <#assign partyFlags = ["contact", "creator", "metadataProvider"]/>

    <#function t key>
        <#return action.getText(key)/>
    </#function>

    <#-- errors posted back for one field, shown under it; form-validation.js marks the field with the same id -->
    <#--
      The validator's "a last name, organisation or position is required" rule also puts "<field> is required" under
      each of the three fields; the card says it once (data-name-error), so those messages are left out here.
    -->
    <#assign nameRuleMessages = ["eml.contact.lastName", "eml.contact.organisation", "eml.contact.position"]
        ?map(labelKey -> action.getText("validation.required", [action.getText(labelKey)]))/>

    <#--
      Errors posted back for one field, shown under it. The list is written even when every message was left out, so
      form-validation.js still finds it by id and marks the field (field-error-<name> marks the element with id <name>).
    -->
    <#macro ferr name tpl>
        <#if !tpl>
            <#local posted = (action.fieldErrors[name])![]/>
            <#if posted?has_content>
                <ul id="field-error-${name}" class="invalid-feedback list-unstyled field-error d-block"><#list posted?filter(m -> !nameRuleMessages?seq_contains(m)) as message><li>${message}</li></#list></ul>
            </#if>
        </#if>
    </#macro>

    <#-- has any field error under a form path, e.g. eml.parties[2]. -->
    <#function hasErrorsUnder prefix>
        <#list (action.fieldErrors!{})?keys as key>
            <#if key?starts_with(prefix)>
                <#return true/>
            </#if>
        </#list>
        <#return false/>
    </#function>

    <#-- full identifier as shown to the user: the directory URL followed by the identifier -->
    <#function fullIdentifier userId>
        <#local identifier = (userId.identifier)!""/>
        <#local directory = userIdDirecotriesExtended[(userId.directory)!""]!((userId.directory)!"")/>
        <#if identifier?matches("^https?://.*", "i") || directory == "">
            <#return identifier/>
        </#if>
        <#return directory + identifier/>
    </#function>

    <#-- remove button of a repeatable value -->
    <#macro removeValueButton>
        <button type="button" class="party-icon-btn party-icon-danger" data-act="rm-rep" aria-label="${t('eml.party.removeValue')}" title="${t('eml.party.removeValue')}">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
        </button>
    </#macro>

    <#-- a list of plain text values of an agent: position, email, home page, phone, address lines -->
    <#macro repeatable base field path labelKey addKey values tpl type="text" placeholder="" startWithOne=false>
        <#local rows = values/>
        <#if startWithOne && !rows?has_content>
            <#local rows = [""]/>
        </#if>
        <div class="col-md-6" data-rep="${field}">
            <label class="form-label" data-rep-label><@s.text name=labelKey/></label>
            <div data-rep-rows>
                <#list rows as value>
                    <#local name = base + path + "[" + value?index + "]"/>
                    <div class="party-rep-row-wrap" data-rep-row>
                        <div class="party-rep-row">
                            <input class="form-control" type="${type}" id="${name}" name="${name}" data-name="${path}[]" value="${value!}" placeholder="${placeholder}"/>
                            <@removeValueButton/>
                        </div>
                        <@ferr name=name tpl=tpl/>
                    </div>
                </#list>
            </div>
            <button type="button" class="party-add-link" data-act="add-rep">+ <@s.text name=addKey/></button>
            <template data-rep-template>
                <div class="party-rep-row-wrap" data-rep-row>
                    <div class="party-rep-row">
                        <input class="form-control" type="${type}" data-name="${path}[]" value="" placeholder="${placeholder}"/>
                        <@removeValueButton/>
                    </div>
                </div>
            </template>
            <#if !tpl>
                <#-- e.g. "a last name, organisation or position is required", posted for the position list -->
                <@ferr name="${base}${path}" tpl=tpl/>
            </#if>
        </div>
    </#macro>

    <#macro identifierRow base index userId tpl>
        <#local dirName = base + "agent.userIds[" + index + "].directory"/>
        <#local idName = base + "agent.userIds[" + index + "].identifier"/>
        <#local directory = userIdDirecotriesExtended[(userId.directory)!""]!((userId.directory)!"")/>
        <div class="party-rep-row-wrap" data-rep-row data-id-row>
            <div class="party-rep-row">
                <input class="form-control" type="text" data-id-full value="${fullIdentifier(userId)}" placeholder="${t('eml.party.identifier.placeholder')}" aria-label="${t('eml.party.identifier.label')}"/>
                <input type="hidden" id="${idName}" name="${idName}" data-name="agent.userIds[].identifier" value="${(userId.identifier)!}"/>
                <@removeValueButton/>
            </div>
            <div class="party-id-detect" data-id-detect aria-live="polite"></div>
            <select class="form-select party-id-directory d-none" id="${dirName}" name="${dirName}" data-name="agent.userIds[].directory" aria-label="${t('eml.contact.directory')}">
                <#list userIdDirectories?keys as key>
                    <option value="${key}" <#if key == directory>selected</#if>>${userIdDirectories.get(key)}</option>
                </#list>
                <#-- a directory from the EML that the IPT doesn't list is kept, so saving doesn't drop it -->
                <#if directory?has_content && !userIdDirectories?keys?seq_contains(directory)>
                    <option value="${directory}" selected>${directory}</option>
                </#if>
            </select>
            <@ferr name=idName tpl=tpl/>
            <@ferr name=dirName tpl=tpl/>
        </div>
    </#macro>

    <#--
      One card per person or organisation. All inputs carry data-name, their path inside the party; contacts.js turns
      it into the form name eml.parties[i].<path> after every add, remove and move. The template card (tpl) has no
      posted errors and is indexed when added.
    -->
    <#macro partyCard index party tpl=false>
        <#local base = "eml.parties[" + index + "]."/>
        <#local agent = (party.agent)!{}/>
        <#local hasErrors = !tpl && hasErrorsUnder(base)/>
        <div class="party-card<#if hasErrors> open has-errors</#if>" data-party>
            <#list partyFlags as flag>
                <input type="hidden" id="${base}${flag}" name="${base}${flag}" data-name="${flag}" data-flag="${flag}" value="${((party[flag])!false)?c}"/>
            </#list>
            <span data-other-roles>
                <#list (party.associatedPartyRoles)![] as role>
                    <input type="hidden" name="${base}associatedPartyRoles" data-name="associatedPartyRoles" value="${role}"/>
                </#list>
            </span>

            <div class="party-summary" data-act="toggle" tabindex="0" role="button" aria-expanded="${hasErrors?c}">
                <span class="party-grip" title="${t('eml.party.dragToReorder')}" aria-hidden="true">
                    <svg viewBox="0 0 24 24"><path d="M9 4a2 2 0 1 1 0 4 2 2 0 0 1 0-4zm6 0a2 2 0 1 1 0 4 2 2 0 0 1 0-4zM9 10a2 2 0 1 1 0 4 2 2 0 0 1 0-4zm6 0a2 2 0 1 1 0 4 2 2 0 0 1 0-4zm-6 6a2 2 0 1 1 0 4 2 2 0 0 1 0-4zm6 0a2 2 0 1 1 0 4 2 2 0 0 1 0-4z"/></svg>
                </span>
                <span class="party-who">
                    <span class="party-name" data-summary-name></span>
                    <span class="party-sub" data-summary-sub></span>
                </span>
                <span class="party-chips" data-summary-chips></span>
                <span class="party-roles" role="group" aria-label="${t('eml.party.roles')}">
                    <#list partyFlags as flag>
                        <button type="button" class="party-role" data-act="role" data-flag="${flag}" aria-pressed="false"><@s.text name="eml.party.chip.${flag}"/></button>
                    </#list>
                    <span data-other-role-chips></span>
                    <span class="dropdown">
                        <button type="button" class="party-role party-role-add" data-act="other-roles-menu" data-bs-toggle="dropdown" aria-expanded="false" data-bs-auto-close="true"></button>
                        <div class="dropdown-menu dropdown-menu-end party-menu party-menu-scroll" data-other-roles-menu></div>
                    </span>
                </span>
                <span class="party-order">
                    <button type="button" class="party-icon-btn" data-act="up" aria-label="${t('eml.party.moveUp')}" title="${t('eml.party.moveUp')}">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M7.41 15.41 12 10.83l4.59 4.58L18 14l-6-6-6 6z"/></svg>
                    </button>
                    <button type="button" class="party-icon-btn" data-act="down" aria-label="${t('eml.party.moveDown')}" title="${t('eml.party.moveDown')}">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M7.41 8.59 12 13.17l4.59-4.58L18 10l-6 6-6-6z"/></svg>
                    </button>
                </span>
                <span class="dropdown">
                    <button type="button" class="party-icon-btn" data-act="copy-menu" data-bs-toggle="dropdown" aria-expanded="false" aria-label="${t('eml.party.copyFrom.title')}" title="${t('eml.party.copyFrom')}">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"/></svg>
                    </button>
                    <div class="dropdown-menu dropdown-menu-end party-menu" data-copy-menu></div>
                </span>
                <button type="button" class="party-icon-btn party-icon-danger" data-act="remove" aria-label="${t('eml.party.remove')}" title="${t('eml.party.remove')}">
                    <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/></svg>
                </button>
                <span class="party-icon-btn party-chev" aria-hidden="true">
                    <svg viewBox="0 0 24 24"><path d="M7.41 8.59 12 13.17l4.59-4.58L18 10l-6 6-6-6z"/></svg>
                </span>
            </div>

            <div class="party-body">
                <#-- the name rule, above the fields it is about; contacts.js shows it only while none of them is filled in -->
                <div class="party-alert party-alert-danger" data-name-error hidden><@s.text name="eml.party.nameRule"/></div>
                <#-- shown live by contacts.js; it replaces the validator's eml.parties[i].associatedPartyRoles field error -->
                <div class="party-alert party-alert-danger" data-role-error hidden><@s.text name="eml.party.role.required"/></div>

                <div class="row g-2">
                    <div class="col-md-3">
                        <label class="form-label" data-for="agent.firstName" for="${base}agent.firstName"><@s.text name="eml.contact.firstName"/></label>
                        <input class="form-control" type="text" id="${base}agent.firstName" name="${base}agent.firstName" data-name="agent.firstName" value="${(agent.firstName)!}"/>
                        <@ferr name="${base}agent.firstName" tpl=tpl/>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label" data-for="agent.lastName" for="${base}agent.lastName"><@s.text name="eml.contact.lastName"/></label>
                        <input class="form-control" type="text" id="${base}agent.lastName" name="${base}agent.lastName" data-name="agent.lastName" data-name-field value="${(agent.lastName)!}"/>
                        <@ferr name="${base}agent.lastName" tpl=tpl/>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label" data-for="agent.organisation" for="${base}agent.organisation"><@s.text name="eml.contact.organisation"/></label>
                        <input class="form-control" type="text" id="${base}agent.organisation" name="${base}agent.organisation" data-name="agent.organisation" data-name-field value="${(agent.organisation)!}"/>
                        <@ferr name="${base}agent.organisation" tpl=tpl/>
                    </div>
                    <@repeatable base=base field="position" path="agent.position" labelKey="eml.contact.position" addKey="eml.party.add.position" values=(agent.position)![] tpl=tpl/>
                </div>

                <fieldset class="party-fieldset">
                    <legend><@s.text name="eml.party.contactDetails"/></legend>
                    <div class="row g-2">
                        <@repeatable base=base field="email" path="agent.email" labelKey="eml.contact.email" addKey="eml.party.add.email" values=(agent.email)![] tpl=tpl type="email" placeholder="name@example.org" startWithOne=true/>
                        <@repeatable base=base field="homepage" path="agent.homepage" labelKey="eml.contact.homepage" addKey="eml.party.add.homepage" values=(agent.homepage)![] tpl=tpl type="url" placeholder="https://"/>
                        <@repeatable base=base field="phone" path="agent.phone" labelKey="eml.contact.phone" addKey="eml.party.add.phone" values=(agent.phone)![] tpl=tpl/>
                    </div>
                </fieldset>

                <fieldset class="party-fieldset">
                    <legend>
                        <@s.text name="eml.party.identifiers"/>
                        <a tabindex="0" role="button" class="popover-link" data-bs-toggle="popover" data-bs-trigger="focus" data-bs-html="true"
                           data-bs-content="<@s.text name='eml.contact.directory.help'/><br><br><@s.text name='eml.contact.identifier.help'/>">
                            <i class="bi bi-info-circle text-gbif-primary px-1"></i>
                        </a>
                    </legend>
                    <div class="row g-2">
                        <div class="col-md-6" data-rep="identifier">
                            <label class="form-label" data-rep-label><@s.text name="eml.party.identifier.label"/></label>
                            <div data-rep-rows>
                                <#list (agent.userIds)![] as userId>
                                    <@identifierRow base=base index=userId?index userId=userId tpl=tpl/>
                                </#list>
                            </div>
                            <button type="button" class="party-add-link" data-act="add-rep">+ <@s.text name="eml.party.add.identifier"/></button>
                            <template data-rep-template>
                                <@identifierRow base="" index=0 userId={} tpl=true/>
                            </template>
                        </div>
                    </div>
                </fieldset>

                <details class="party-more" data-address>
                    <summary><@s.text name="eml.party.address"/><span data-address-summary></span></summary>
                    <div class="row g-2 mt-1">
                        <@repeatable base=base field="address" path="agent.address.address" labelKey="eml.contact.address.address" addKey="eml.party.add.address" values=(agent.address.address)![] tpl=tpl/>
                        <div class="col-md-6"></div>
                        <div class="col-md-4">
                            <label class="form-label" data-for="agent.address.city" for="${base}agent.address.city"><@s.text name="eml.contact.address.city"/></label>
                            <input class="form-control" type="text" id="${base}agent.address.city" name="${base}agent.address.city" data-name="agent.address.city" value="${(agent.address.city)!}"/>
                            <@ferr name="${base}agent.address.city" tpl=tpl/>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label" data-for="agent.address.province" for="${base}agent.address.province"><@s.text name="eml.contact.address.province"/></label>
                            <input class="form-control" type="text" id="${base}agent.address.province" name="${base}agent.address.province" data-name="agent.address.province" value="${(agent.address.province)!}"/>
                            <@ferr name="${base}agent.address.province" tpl=tpl/>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label" data-for="agent.address.country" for="${base}agent.address.country"><@s.text name="eml.contact.address.country"/></label>
                            <select class="form-select party-country" id="${base}agent.address.country" name="${base}agent.address.country" data-name="agent.address.country">
                                <#list countries?keys as code>
                                    <option value="${code}" <#if code == ((agent.address.country)!"")>selected</#if>>${countries.get(code)}</option>
                                </#list>
                            </select>
                            <@ferr name="${base}agent.address.country" tpl=tpl/>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label" data-for="agent.address.postalCode" for="${base}agent.address.postalCode"><@s.text name="eml.contact.address.postalCode"/></label>
                            <input class="form-control" type="text" id="${base}agent.address.postalCode" name="${base}agent.address.postalCode" data-name="agent.address.postalCode" value="${(agent.address.postalCode)!}"/>
                            <@ferr name="${base}agent.address.postalCode" tpl=tpl/>
                        </div>
                    </div>
                </details>

                <details class="party-more" data-more>
                    <summary><@s.text name="eml.party.more"/> – <@s.text name="eml.contact.salutation"/><span data-more-summary></span></summary>
                    <div class="row g-2 mt-1">
                        <div class="col-md-3">
                            <label class="form-label" data-for="agent.salutation" for="${base}agent.salutation"><@s.text name="eml.contact.salutation"/></label>
                            <input class="form-control" type="text" id="${base}agent.salutation" name="${base}agent.salutation" data-name="agent.salutation" value="${(agent.salutation)!}"/>
                            <@ferr name="${base}agent.salutation" tpl=tpl/>
                        </div>
                    </div>
                </details>
            </div>
        </div>
    </#macro>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form id="contacts-form" class="needs-validation track-unsaved" action="metadata-${section}.do" method="post" novalidate>
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

        <#-- rest of the auto-generated citation after the authors, see Resource.generateResourceCitation -->
        <#assign citationTail = ""/>
        <#if resource.isCitationAutoGenerated()>
            <#attempt>
                <#assign nextVersion = resource.getNextVersion().toPlainString()/>
                <#if eml.pubDate??>
                    <#assign citationYear = eml.pubDate?datetime?string("yyyy")/>
                <#else>
                    <#assign citationYear = .now?string("yyyy")/>
                </#if>
                <#assign citationTitle = (resource.title?has_content)?then(resource.title?trim, resource.shortname)/>
                <#assign citationPublisher = (resource.organisation.name)!""/>
                <#assign citationType = (resource.coreType?has_content)?then(resource.coreType?lower_case?cap_first + " dataset", "")/>
                <#if resource.doi??>
                    <#assign citationIdentifier = resource.doi.url.toString()/>
                <#elseif (eml.citation.identifier)?has_content>
                    <#assign citationIdentifier = eml.citation.identifier/>
                <#else>
                    <#assign citationIdentifier = cfg.getResourceVersionUri(resource.shortname, nextVersion)/>
                </#if>
                <#assign citationTail = " (" + citationYear + "). " + citationTitle + ". Version " + nextVersion + ". "
                    + citationPublisher?has_content?then(citationPublisher?trim + ". ", "") + citationType + ". " + citationIdentifier/>
            <#recover>
                <#assign citationTail = ""/>
            </#attempt>
        </#if>

        <div class="container-fluid bg-body">
            <div class="container bd-layout main-content-container">
                <main class="bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "eml_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">
                        <div class="my-3 p-3">
                            <section id="parties"
                                     data-shortname="${resource.shortname}"
                                     data-people-url="${baseURL}/manager-api/suggest-people?r=${resource.shortname?url}"
                                     data-citation-tail="${citationTail}"
                                     data-citation-auto="${resource.isCitationAutoGenerated()?c}">
                                <#-- the page title says what this is; no section heading for the only section -->
                                <p class="party-intro">
                                    <@s.text name="eml.party.description"/>
                                    <a tabindex="0" role="button" class="popover-link" data-bs-toggle="popover" data-bs-trigger="focus" data-bs-html="true"
                                       data-bs-content="<@s.text name='eml.party.plural.help'/>" aria-label="${t('eml.party.plural')}">
                                        <i class="bi bi-info-circle text-gbif-primary px-1"></i>
                                    </a>
                                </p>

                                <div class="party-toolbar">
                                    <div class="party-filters" role="group" aria-label="${t('eml.party.filter.label')}">
                                        <button type="button" class="party-ftab active" data-act="filter" data-filter="all" aria-pressed="true">
                                            <@s.text name="eml.party.filter.all"/> <span class="party-count" data-count="all"></span>
                                        </button>
                                        <#list partyFlags as flag>
                                            <button type="button" class="party-ftab" data-act="filter" data-filter="${flag}" data-required="${(flag != 'metadataProvider')?c}" aria-pressed="false">
                                                <@s.text name="eml.party.filter.${flag}"/> <span class="party-count" data-count="${flag}"></span><span class="party-ftab-state" data-state="${flag}"></span>
                                            </button>
                                        </#list>
                                    </div>
                                    <button type="button" class="btn btn-sm btn-outline-secondary party-toggle-all" data-act="toggle-all"></button>
                                </div>

                                <div class="party-citation" data-citation aria-live="polite">
                                    <div class="party-citation-lbl">
                                        <span><@s.text name="eml.party.citation.preview"/></span>
                                        <#if resource.isCitationAutoGenerated() && citationTail?has_content>
                                            <span class="party-citation-order"><@s.text name="eml.party.citation.order"/></span>
                                        </#if>
                                    </div>
                                    <#if resource.isCitationAutoGenerated() && citationTail?has_content>
                                        <div class="party-citation-txt" data-citation-text></div>
                                    <#else>
                                        <div class="party-citation-manual"><@s.text name="eml.party.citation.manual"/></div>
                                    </#if>
                                </div>

                                <div id="party-items">
                                    <#list eml.parties as party>
                                        <@partyCard index=party?index party=party/>
                                    </#list>
                                </div>
                                <div class="party-empty" data-empty hidden></div>

                                <div class="party-add-bar">
                                    <#-- link-style actions, as on the other metadata pages -->
                                    <button type="button" class="metadata-action-link custom-link" data-act="add">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary" aria-hidden="true"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
                                        </span>
                                        <span data-add-label><@s.text name="eml.party.add"/></span>
                                    </button>
                                    <span class="dropdown">
                                        <button type="button" class="metadata-action-link custom-link" data-act="copy-menu" data-bs-toggle="dropdown" aria-expanded="false">
                                            <span>
                                                <svg viewBox="0 0 24 24" class="link-icon link-icon-primary" aria-hidden="true"><path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"/></svg>
                                            </span>
                                            <span><@s.text name="eml.party.copyFrom"/></span>
                                        </button>
                                        <div class="dropdown-menu party-menu" data-copy-menu></div>
                                    </span>
                                </div>

                                <template id="party-template">
                                    <@partyCard index=0 party={} tpl=true/>
                                </template>
                            </section>

                            <#-- texts and role names for contacts.js -->
                            <div id="party-i18n" hidden
                                 data-new="${t('eml.party.new')}"
                                 data-add="${t('eml.party.add')}"
                                 data-add-contact="${t('eml.party.add.contact')}"
                                 data-add-creator="${t('eml.party.add.creator')}"
                                 data-add-metadata-provider="${t('eml.party.add.metadataProvider')}"
                                 data-chip-contact="${t('eml.party.chip.contact')}"
                                 data-chip-creator="${t('eml.party.chip.creator')}"
                                 data-chip-metadata-provider="${t('eml.party.chip.metadataProvider')}"
                                 data-filter-required="${t('eml.party.filter.required')}"
                                 data-filter-empty="${t('eml.party.filter.empty')}"
                                 data-empty="${t('eml.party.empty')}"
                                 data-expand-all="${t('eml.party.expandAll')}"
                                 data-collapse-all="${t('eml.party.collapseAll')}"
                                 data-removed="${t('eml.party.removed')}"
                                 data-moved="${t('eml.party.moved')}"
                                 data-role-add="${t('eml.party.role.add')}"
                                 data-role-remove="${t('eml.party.role.remove')}"
                                 data-author-position="${t('eml.party.authorPosition')}"
                                 data-other-roles-add="${t('eml.party.otherRoles.add')}"
                                 data-other-roles-add-first="${t('eml.party.otherRoles.addFirst')}"
                                 data-other-roles-menu="${t('eml.party.otherRoles.menu')}"
                                 data-other-roles-cited="${t('eml.party.otherRoles.cited')}"
                                 data-other-roles-cited-title="${t('eml.party.otherRoles.citedTitle')}"
                                 data-needs-name="${t('eml.party.needsName')}"
                                 data-needs-role="${t('eml.party.needsRole')}"
                                 data-has-errors="${t('eml.party.hasErrors')}"
                                 data-identifier-recognised="${t('eml.party.identifier.recognised')}"
                                 data-identifier-orcid-format="${t('eml.party.identifier.orcidFormat')}"
                                 data-identifier-unknown="${t('eml.party.identifier.unknown')}"
                                 data-copy-this-page="${t('eml.party.copyFrom.thisPage')}"
                                 data-copy-replace="${t('eml.party.copyFrom.replace')}"
                                 data-copy-nobody="${t('eml.party.copyFrom.nobody')}"
                                 data-copy-no-role="${t('eml.party.copyFrom.noRole')}"
                                 data-copy-another="${t('eml.party.copyFrom.anotherResource')}"
                                 data-copied="${t('eml.party.copied')}"
                                 data-copied-pick-role="${t('eml.party.copied.pickRole')}"
                                 data-picker-loading="${t('eml.party.picker.loading')}"
                                 data-picker-no-match="${t('eml.party.picker.noMatch')}"
                                 data-picker-none="${t('eml.party.picker.none')}"
                                 data-picker-failed="${t('eml.party.picker.failed')}"
                                 data-picker-used-in="${t('eml.party.picker.usedIn')}"
                                 data-picker-personnel="${t('eml.party.picker.personnel')}"
                                 data-citation-no-creators="${t('eml.party.citation.noCreators')}"
                                 data-country-placeholder="${t('eml.country.selection')}"
                                 data-no-results="${selectNoResultsFound}">
                                <#list roles?keys as role>
                                    <#if role?has_content>
                                        <span data-role="${role}" data-description="${action.getText('eml.party.otherRoles.description.' + role, '')}">${roles.get(role)}</span>
                                    </#if>
                                </#list>
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

    <div id="party-picker" class="modal fade" tabindex="-1" aria-labelledby="party-picker-title" aria-hidden="true">
        <div class="modal-dialog modal-dialog-scrollable">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title fs-6" id="party-picker-title"><@s.text name="eml.party.picker.title"/></h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="${t('button.cancel')}"></button>
                </div>
                <div class="modal-body">
                    <input type="search" class="form-control mb-2" id="party-picker-search" placeholder="${t('eml.party.picker.search')}" aria-label="${t('eml.party.picker.search')}"/>
                    <div class="party-picker-results" id="party-picker-results" role="listbox" aria-labelledby="party-picker-title"></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal"><@s.text name="button.cancel"/></button>
                    <button type="button" class="btn btn-sm btn-outline-gbif-primary" id="party-picker-ok" disabled><@s.text name="button.copy"/></button>
                </div>
            </div>
        </div>
    </div>

    <div class="party-toast" id="party-toast" role="status" aria-live="polite"></div>

    <#include "/WEB-INF/pages/inc/footer.ftl">
