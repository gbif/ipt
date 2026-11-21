    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <link rel="stylesheet" href="${baseURL}/styles/contacts.css">
    <script src="${baseURL}/js/people.js"></script>
    <#-- runs on DOMContentLoaded, after Bootstrap and html5sortable from the footer -->
    <script src="${baseURL}/js/project.js"></script>
    <title><@s.text name='manage.metadata.project.title'/></title>
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

    <#assign currentMetadataPage = "project"/>
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/macros/icon.ftl"/>
    <#assign materialHelpIcons = true/>
    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/eml.css">
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <#function t key>
        <#return action.getText(key)/>
    </#function>

    <#-- errors posted back for one field, shown under it; form-validation.js marks the field with the same id -->
    <#macro ferr name tpl>
        <#if !tpl>
            <#local posted = (action.fieldErrors[name])![]/>
            <#if posted?has_content>
                <ul id="field-error-${name}" class="invalid-feedback list-unstyled field-error d-block"><#list posted as message><li>${message}</li></#list></ul>
            </#if>
        </#if>
    </#macro>

    <#-- has any field error under a form path, e.g. eml.project.awards[2]. -->
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

    <#macro icn path>
        <svg viewBox="0 0 24 24" aria-hidden="true"><path d="${path}"/></svg>
    </#macro>
    <#assign binPath = "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
    <#assign copyPath = "M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"/>
    <#assign plusPath = "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>

    <#macro removeValueButton>
        <button type="button" class="party-icon-btn party-icon-danger" data-act="rm-rep" aria-label="${t('eml.party.removeValue')}" title="${t('eml.party.removeValue')}"><@icn binPath/></button>
    </#macro>

    <#-- a plain text input; name is the full form name, path its part inside the card (data-name) -->
    <#macro field base path labelKey value tpl col required=false type="text" placeholder="">
        <div class="${col}">
            <label class="form-label" data-for="${path}" for="${base}${path}"><@s.text name=labelKey/><#if required> <span class="text-gbif-danger">&#42;</span></#if></label>
            <input class="form-control" type="${type}" id="${base}${path}" name="${base}${path}" data-name="${path}" value="${value!}" placeholder="${placeholder}"/>
            <@ferr name="${base}${path}" tpl=tpl/>
        </div>
    </#macro>

    <#macro identifierRow base index userId tpl>
        <#local dirName = base + "userIds[" + index + "].directory"/>
        <#local idName = base + "userIds[" + index + "].identifier"/>
        <#local directory = userIdDirecotriesExtended[(userId.directory)!""]!((userId.directory)!"")/>
        <div class="party-rep-row-wrap" data-rep-row data-id-row>
            <div class="party-rep-row">
                <input class="form-control" type="text" data-id-full value="${fullIdentifier(userId)}" placeholder="${t('eml.party.identifier.placeholder')}" aria-label="${t('eml.party.identifier.label')}"/>
                <input type="hidden" id="${idName}" name="${idName}" data-name="userIds[].identifier" value="${(userId.identifier)!}"/>
                <@removeValueButton/>
            </div>
            <div class="party-id-detect" data-id-detect aria-live="polite"></div>
            <select class="form-select party-id-directory d-none" id="${dirName}" name="${dirName}" data-name="userIds[].directory" aria-label="${t('eml.contact.directory')}">
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
      The header of a card: grip (top-level lists only, html5sortable), name and summary line, up/down, extra buttons
      (nested), remove and fold chevron. project.js fills the name, summary and chips.
    -->
    <#macro cardHeader grip=true>
        <div class="party-summary" data-act="toggle" tabindex="0" role="button" aria-expanded="false">
            <#if grip>
                <span class="party-grip" title="${t('eml.party.dragToReorder')}" aria-hidden="true">
                    <svg viewBox="0 0 24 24"><path d="M9 4a2 2 0 1 1 0 4 2 2 0 0 1 0-4zm6 0a2 2 0 1 1 0 4 2 2 0 0 1 0-4zM9 10a2 2 0 1 1 0 4 2 2 0 0 1 0-4zm6 0a2 2 0 1 1 0 4 2 2 0 0 1 0-4zm-6 6a2 2 0 1 1 0 4 2 2 0 0 1 0-4zm6 0a2 2 0 1 1 0 4 2 2 0 0 1 0-4z"/></svg>
                </span>
            </#if>
            <span class="party-who">
                <span class="party-name" data-summary-name></span>
                <span class="party-sub" data-summary-sub></span>
            </span>
            <span class="party-chips" data-summary-chips></span>
            <span class="party-order">
                <button type="button" class="party-icon-btn" data-act="up" aria-label="${t('eml.party.moveUp')}" title="${t('eml.party.moveUp')}"><@icn "M7.41 15.41 12 10.83l4.59 4.58L18 14l-6-6-6 6z"/></button>
                <button type="button" class="party-icon-btn" data-act="down" aria-label="${t('eml.party.moveDown')}" title="${t('eml.party.moveDown')}"><@icn "M7.41 8.59 12 13.17l4.59-4.58L18 10l-6 6-6-6z"/></button>
            </span>
            <#nested>
            <button type="button" class="party-icon-btn party-icon-danger" data-act="remove" aria-label="${t('eml.party.remove')}" title="${t('eml.party.remove')}"><@icn binPath/></button>
            <span class="party-icon-btn party-chev" aria-hidden="true"><@icn "M7.41 8.59 12 13.17l4.59-4.58L18 10l-6 6-6-6z"/></span>
        </div>
    </#macro>

    <#--
      Add and Copy from links under a list of people. The copy menu is filled by project.js when it opens.
    -->
    <#macro peopleAddBar>
        <div class="party-add-bar">
            <button type="button" class="metadata-action-link custom-link" data-act="add">
                <span><svg viewBox="0 0 24 24" class="link-icon link-icon-primary" aria-hidden="true"><path d="${plusPath}"/></svg></span>
                <span><@s.text name="eml.project.personnel.add"/></span>
            </button>
            <span class="dropdown">
                <button type="button" class="metadata-action-link custom-link" data-act="copy-menu" data-bs-toggle="dropdown" aria-expanded="false">
                    <span><svg viewBox="0 0 24 24" class="link-icon link-icon-primary" aria-hidden="true"><path d="${copyPath}"/></svg></span>
                    <span><@s.text name="eml.party.copyFrom"/></span>
                </button>
                <div class="dropdown-menu party-menu" data-copy-menu></div>
            </span>
        </div>
    </#macro>

    <#--
      One card per project person. Inputs carry data-name, their path inside the card; project.js turns it into the
      form name (eml.project.personnel[i].<path>, or eml.project.relatedProjects[j].personnel[i].<path>) after every
      add, remove and move. The template card (tpl) has no posted errors and is indexed when added.
    -->
    <#macro personCard base person tpl=false nested=false>
        <#local hasErrors = !tpl && hasErrorsUnder(base)/>
        <#local role = (person.role)!""/>
        <div class="party-card<#if hasErrors> open has-errors</#if>" data-item data-kind="person">
            <@cardHeader grip=!nested>
                <span class="dropdown">
                    <button type="button" class="party-icon-btn" data-act="copy-menu" data-bs-toggle="dropdown" aria-expanded="false" aria-label="${t('eml.party.copyFrom.title')}" title="${t('eml.party.copyFrom')}"><@icn copyPath/></button>
                    <div class="dropdown-menu dropdown-menu-end party-menu" data-copy-menu></div>
                </span>
            </@cardHeader>
            <div class="party-body">
                <div class="row g-2">
                    <@field base=base path="firstName" labelKey="eml.contact.firstName" value=(person.firstName)! tpl=tpl col="col-md-4"/>
                    <@field base=base path="lastName" labelKey="eml.contact.lastName" value=(person.lastName)! tpl=tpl col="col-md-4" required=true/>
                    <@field base=base path="salutation" labelKey="eml.contact.salutation" value=(person.salutation)! tpl=tpl col="col-md-4"/>
                    <div class="col-md-6">
                        <label class="form-label" data-for="role" for="${base}role"><@s.text name="eml.associatedParties.role"/></label>
                        <select class="form-select" id="${base}role" name="${base}role" data-name="role">
                            <#list roles?keys as key>
                                <option value="${key}" <#if key == role>selected</#if>>${roles.get(key)}</option>
                            </#list>
                            <#-- a role the vocabulary doesn't list is kept, so saving doesn't drop it -->
                            <#if role?has_content && !roles?keys?seq_contains(role)>
                                <option value="${role}" selected>${role}</option>
                            </#if>
                        </select>
                        <@ferr name="${base}role" tpl=tpl/>
                    </div>
                </div>
                <fieldset class="party-fieldset">
                    <legend>
                        <@s.text name="eml.party.identifiers"/>
                        <a tabindex="0" role="button" class="popover-link" data-bs-toggle="popover" data-bs-trigger="focus" data-bs-html="true"
                           data-bs-content="<@s.text name='eml.contact.directory.help'/><br><br><@s.text name='eml.contact.identifier.help'/>">
                            <@icon name="info_outlined" class="text-gbif-primary help-icon-m"/>
                        </a>
                    </legend>
                    <div class="row g-2">
                        <div class="col-md-8" data-rep="identifier">
                            <#-- the legend names the group; the label stays for screen readers and lists the directories -->
                            <label class="visually-hidden" data-rep-label><@s.text name="eml.party.identifier.label"/></label>
                            <div data-rep-rows>
                                <#list (person.userIds)![] as userId>
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
            </div>
        </div>
    </#macro>

    <#macro awardCard base award tpl=false>
        <#local hasErrors = !tpl && hasErrorsUnder(base)/>
        <div class="party-card<#if hasErrors> open has-errors</#if>" data-item data-kind="award">
            <@cardHeader/>
            <div class="party-body">
                <div class="row g-2">
                    <@field base=base path="title" labelKey="eml.project.award.title" value=(award.title)! tpl=tpl col="col-12" required=true/>
                    <@field base=base path="funderName" labelKey="eml.project.award.funderName" value=(award.funderName)! tpl=tpl col="col-md-6" required=true/>
                    <@field base=base path="awardNumber" labelKey="eml.project.award.awardNumber" value=(award.awardNumber)! tpl=tpl col="col-md-6"/>
                    <@field base=base path="awardUrl" labelKey="eml.project.award.awardUrl" value=(award.awardUrl)! tpl=tpl col="col-md-6" type="url" placeholder="https://"/>
                    <div class="col-md-6" data-rep="funderIdentifier">
                        <label class="form-label" data-rep-label>
                            <@s.text name="eml.project.award.funderIdentifier"/>
                            <a tabindex="0" role="button" class="popover-link" data-bs-toggle="popover" data-bs-trigger="focus" data-bs-html="true"
                               data-bs-content="<@s.text name='eml.project.award.funderIdentifier.help'/>">
                                <@icon name="info_outlined" class="text-gbif-primary help-icon-m"/>
                            </a>
                        </label>
                        <div data-rep-rows>
                            <#list (award.funderIdentifiers)![] as value>
                                <#local name = base + "funderIdentifiers[" + value?index + "]"/>
                                <div class="party-rep-row-wrap" data-rep-row>
                                    <div class="party-rep-row">
                                        <input class="form-control" type="text" id="${name}" name="${name}" data-name="funderIdentifiers[]" value="${value!}"/>
                                        <@removeValueButton/>
                                    </div>
                                    <@ferr name=name tpl=tpl/>
                                </div>
                            </#list>
                        </div>
                        <button type="button" class="party-add-link" data-act="add-rep">+ <@s.text name="eml.project.award.addFunderIdentifier"/></button>
                        <template data-rep-template>
                            <div class="party-rep-row-wrap" data-rep-row>
                                <div class="party-rep-row">
                                    <input class="form-control" type="text" data-name="funderIdentifiers[]" value=""/>
                                    <@removeValueButton/>
                                </div>
                            </div>
                        </template>
                    </div>
                </div>
            </div>
        </div>
    </#macro>

    <#macro relatedProjectCard base project tpl=false>
        <#local hasErrors = !tpl && hasErrorsUnder(base)/>
        <div class="party-card<#if hasErrors> open has-errors</#if>" data-item data-kind="project">
            <@cardHeader/>
            <div class="party-body">
                <div class="row g-2">
                    <@field base=base path="title" labelKey="eml.project.relatedProject.title" value=(project.title)! tpl=tpl col="col-md-8" required=true/>
                    <@field base=base path="identifier" labelKey="eml.project.relatedProject.identifier" value=(project.identifier)! tpl=tpl col="col-md-4"/>
                    <div class="col-12">
                        <label class="form-label" data-for="description" for="${base}description"><@s.text name="eml.project.relatedProject.description"/></label>
                        <textarea class="form-control" rows="3" id="${base}description" name="${base}description" data-name="description">${(project.description)!}</textarea>
                        <@ferr name="${base}description" tpl=tpl/>
                    </div>
                </div>
                <fieldset class="party-fieldset">
                    <legend><@s.text name="eml.project.relatedProject.personnel"/> <span class="text-gbif-danger">&#42;</span></legend>
                    <div class="pp-list" data-list="personnel" data-template="pp-person-nested-template">
                        <div data-items>
                            <#list (project.personnel)![] as person>
                                <@personCard base="${base}personnel[${person?index}]." person=person tpl=tpl nested=true/>
                            </#list>
                        </div>
                        <div class="party-alert party-alert-danger mb-0" data-empty hidden><@s.text name="eml.project.relatedProject.personnel.required"/></div>
                        <@peopleAddBar/>
                    </div>
                </fieldset>
            </div>
        </div>
    </#macro>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form id="project-form" class="needs-validation track-unsaved" action="metadata-${section}.do" method="post" novalidate>
        <#include "eml_heading.ftl"/>

        <#include "metadata_section_select.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout main-content-container">
                <main class="bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "eml_sidebar.ftl"/>
                    </div>

                    <div class="bd-content" id="project-page"
                         data-people-url="${baseURL}/manager-api/suggest-people?r=${resource.shortname?url}">
                        <div class="my-md-3 p-3" id="s-project" data-toc="${t('manage.metadata.section.project')}">
                            <@textinline name="manage.metadata.section.project"/>
                            <p class="party-intro"><@s.text name='manage.metadata.project.intro'/></p>
                            <div class="row g-3">
                                <div class="col-lg-8">
                                    <@input name="eml.project.title" value=(eml.project.title)!/>
                                </div>
                                <div class="col-lg-4">
                                    <@input name="eml.project.identifier" value=(eml.project.identifier)! help="i18n"/>
                                </div>
                                <div class="col-12">
                                    <@text name="eml.project.description" value=(eml.project.description)! help="i18n"/>
                                </div>
                                <div class="col-12">
                                    <@text name="eml.project.funding" value=(eml.project.funding)! help="i18n" rows=3/>
                                </div>
                                <div class="col-12">
                                    <@text name="eml.project.studyAreaDescription.descriptorValue" value=(eml.project.studyAreaDescription.descriptorValue)! help="i18n" rows=3/>
                                </div>
                                <div class="col-12">
                                    <@text name="eml.project.designDescription" value=(eml.project.designDescription)! help="i18n"/>
                                </div>
                            </div>
                        </div>

                        <div class="my-3 p-3" id="s-personnel" data-toc="${t('eml.project.personnel')}">
                            <@textinline name="eml.project.personnel" help="i18n"/>
                            <div class="pp-list" data-list="eml.project.personnel" data-template="pp-person-template">
                                <div data-items>
                                    <#list eml.project.personnel as person>
                                        <@personCard base="eml.project.personnel[${person?index}]." person=person/>
                                    </#list>
                                </div>
                                <div class="party-empty" data-empty hidden><@s.text name="eml.project.personnel.empty"/></div>
                                <@peopleAddBar/>
                            </div>
                        </div>

                        <div class="my-3 p-3" id="s-awards" data-toc="${t('eml.project.award')}">
                            <@textinline name="eml.project.award" help="i18n"/>
                            <div class="pp-list" data-list="eml.project.awards" data-template="pp-award-template">
                                <div data-items>
                                    <#list eml.project.awards as award>
                                        <@awardCard base="eml.project.awards[${award?index}]." award=award/>
                                    </#list>
                                </div>
                                <div class="party-empty" data-empty hidden><@s.text name="eml.project.award.empty"/></div>
                                <div class="party-add-bar">
                                    <button type="button" class="metadata-action-link custom-link" data-act="add">
                                        <span><svg viewBox="0 0 24 24" class="link-icon link-icon-primary" aria-hidden="true"><path d="${plusPath}"/></svg></span>
                                        <span><@s.text name="eml.project.award.add"/></span>
                                    </button>
                                </div>
                            </div>
                        </div>

                        <div class="my-3 p-3" id="s-related" data-toc="${t('eml.project.relatedProjects')}">
                            <@textinline name="eml.project.relatedProjects" help="i18n"/>
                            <div class="pp-list" data-list="eml.project.relatedProjects" data-template="pp-project-template">
                                <div data-items>
                                    <#list eml.project.relatedProjects as project>
                                        <@relatedProjectCard base="eml.project.relatedProjects[${project?index}]." project=project/>
                                    </#list>
                                </div>
                                <div class="party-empty" data-empty hidden><@s.text name="eml.project.relatedProject.empty"/></div>
                                <div class="party-add-bar">
                                    <button type="button" class="metadata-action-link custom-link" data-act="add">
                                        <span><svg viewBox="0 0 24 24" class="link-icon link-icon-primary" aria-hidden="true"><path d="${plusPath}"/></svg></span>
                                        <span><@s.text name="eml.project.relatedProject.add"/></span>
                                    </button>
                                </div>
                            </div>
                        </div>

                        <template id="pp-person-template"><@personCard base="" person={} tpl=true/></template>
                        <template id="pp-person-nested-template"><@personCard base="" person={} tpl=true nested=true/></template>
                        <template id="pp-award-template"><@awardCard base="" award={} tpl=true/></template>
                        <template id="pp-project-template"><@relatedProjectCard base="" project={} tpl=true/></template>

                        <#-- the people of this resource's contacts page, offered in the Copy from menus -->
                        <div id="pp-contacts" hidden>
                            <#list eml.parties as party>
                                <#assign agent = (party.agent)!{}/>
                                <#if (agent.lastName)?has_content || (agent.firstName)?has_content>
                                    <span data-source
                                          data-salutation="${agent.salutation!}" data-first-name="${agent.firstName!}" data-last-name="${agent.lastName!}"
                                          data-roles="${['contact', 'creator', 'metadataProvider']?filter(f -> party[f])?map(f -> t('eml.party.chip.' + f)?lower_case)?join(', ')}">
                                        <#list (agent.userIds)![] as userId>
                                            <#if (userId.identifier)?has_content>
                                                <i data-directory="${userIdDirecotriesExtended[userId.directory!""]!(userId.directory!"")}" data-identifier="${userId.identifier}"></i>
                                            </#if>
                                        </#list>
                                    </span>
                                </#if>
                            </#list>
                        </div>

                        <#-- texts and role names for project.js -->
                        <div id="pp-i18n" hidden
                             data-new-person="${t('eml.project.personnel.new')}"
                             data-new-award="${t('eml.project.award.new')}"
                             data-new-project="${t('eml.project.relatedProject.new')}"
                             data-needs-last-name="${t('eml.project.personnel.needsLastName')}"
                             data-award-needs="${t('eml.project.award.needs')}"
                             data-needs-title="${t('eml.project.relatedProject.needsTitle')}"
                             data-personnel="${t('rtf.project.personnel')}"
                             data-has-errors="${t('eml.party.hasErrors')}"
                             data-removed="${t('eml.party.removed')}"
                             data-moved="${t('eml.party.moved')}"
                             data-identifier-recognised="${t('eml.party.identifier.recognised')}"
                             data-identifier-orcid-format="${t('eml.party.identifier.orcidFormat')}"
                             data-identifier-unknown="${t('eml.party.identifier.unknown')}"
                             data-copy-contacts="${t('eml.project.personnel.copyFromContacts')}"
                             data-copy-this-page="${t('eml.party.copyFrom.thisPage')}"
                             data-copy-replace="${t('eml.party.copyFrom.replace')}"
                             data-copy-nobody="${t('eml.party.copyFrom.nobody')}"
                             data-copy-another="${t('eml.party.copyFrom.anotherResource')}"
                             data-copied="${t('eml.party.copied')}"
                             data-picker-loading="${t('eml.party.picker.loading')}"
                             data-picker-no-match="${t('eml.party.picker.noMatch')}"
                             data-picker-none="${t('eml.party.picker.none')}"
                             data-picker-failed="${t('eml.party.picker.failed')}"
                             data-picker-used-in="${t('eml.party.picker.usedIn')}"
                             data-picker-personnel="${t('eml.party.picker.personnel')}"
                             data-chip-contact="${t('eml.party.chip.contact')}"
                             data-chip-creator="${t('eml.party.chip.creator')}"
                             data-chip-metadata-provider="${t('eml.party.chip.metadataProvider')}">
                            <#-- role names, for the "Used in" line of the picker -->
                            <#list roles?keys as role>
                                <#if role?has_content>
                                    <span data-role="${role}">${roles.get(role)}</span>
                                </#if>
                            </#list>
                        </div>

                        <!-- internal parameter -->
                        <input name="r" type="hidden" value="${resource.shortname}" />
                    </div>
                </main>
            </div>
        </div>

        <#include "eml_savebar.ftl"/>
    </form>

    <#include "/WEB-INF/pages/manage/eml/unsaved_changes_modal.ftl">
    <#include "people_picker.ftl"/>

    <div class="party-toast" id="party-toast" role="status" aria-live="polite"></div>

    <#include "/WEB-INF/pages/inc/footer.ftl">
