<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.CamtrapMetadataAction" -->
    <#include "/WEB-INF/pages/macros/icon.ftl"/>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.datapackagemetadata.camtrap.basic.title'/></title>
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2-ipt.css">
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <link rel="stylesheet" href="${baseURL}/styles/contacts.css">
    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/eml.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>
    <script src="${baseURL}/js/dp-metadata.js"></script>
    <script>
        $(document).ready(function () {
            function select2Options(placeholder, search) {
                return {
                    placeholder: placeholder,
                    language: {
                        noResults: function () {
                            return '${selectNoResultsFound}';
                        }
                    },
                    minimumResultsForSearch: search,
                    width: "100%",
                    allowClear: true,
                    theme: 'bootstrap4'
                };
            }
            var rolePlaceholder = '${action.getText("datapackagemetadata.contributor.role.select")?js_string}';
            var licensePlaceholder = '${action.getText("datapackagemetadata.license.select")?js_string}';

            // people (contact, principal investigator, contributor) have first and last names, the title follows them;
            // organizations (publisher, rights holder) only have the title
            function isPersonRole(role) {
                return !role || role === 'CONTACT' || role === 'PRINCIPAL_INVESTIGATOR' || role === 'CONTRIBUTOR';
            }

            function showRoleFields($card, initial) {
                var person = isPersonRole($card.find('select[id$="role"]').val());
                var $title = $card.find('input[id$="title"]');
                var $firstName = $card.find('input[id$="firstName"]');
                var $lastName = $card.find('input[id$="lastName"]');
                if (!initial) {
                    if (person) {
                        // the organization name becomes the names
                        var names = ($title.val() || '').trim().split(/\s+/).filter(Boolean);
                        if (!$firstName.val() && !$lastName.val() && names.length) {
                            $lastName.val(names.length === 1 ? names[0] : names.slice(1).join(' '));
                            $firstName.val(names.length === 1 ? '' : names[0]);
                        }
                    } else {
                        $title.val((($firstName.val() || '') + ' ' + ($lastName.val() || '')).trim());
                        $firstName.val('');
                        $lastName.val('');
                    }
                    $lastName.removeClass('is-invalid');
                }
                $card.find('.title-col-wrapper').toggleClass('d-none', person);
                $card.find('.firstName-col-wrapper, .lastName-col-wrapper').toggleClass('d-none', !person);
            }

            var contributors = dpList({
                key: 'contributor',
                prefix: 'metadata.contributors',
                onAdd: function ($card) {
                    $card.find('select[id$="role"]').select2(select2Options(rolePlaceholder, 15));
                    showRoleFields($card, true);
                }
            });

            $('#contributor-items')
                .on('input', 'input[id$="firstName"], input[id$="lastName"]', function () {
                    var $card = $(this).closest('.item');
                    var name = (($card.find('input[id$="firstName"]').val() || '') + ' ' + ($card.find('input[id$="lastName"]').val() || '')).trim();
                    $card.find('input[id$="title"]').val(name);
                    contributors.updateTitle($card);
                })
                .on('change', 'select[id$="role"]', function () {
                    var $card = $(this).closest('.item');
                    showRoleFields($card, false);
                    contributors.updateTitle($card);
                });

            $('#contributor-items > .item').each(function () {
                showRoleFields($(this), true);
            });
            $('#contributor-items select[id$="role"]').select2(select2Options(rolePlaceholder, 15));

            dpList({key: 'source', prefix: 'metadata.sources'});

            $('#license-data select').select2(select2Options(licensePlaceholder, 'Infinity'));
            $('#license-media select').select2(select2Options(licensePlaceholder, 15));

            // scroll to the first error
            var $invalid = $(".is-invalid").first();
            if ($invalid.length) {
                $('body, html').animate({scrollTop: $invalid.offset().top - 100});
            }

            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "basic"/>
    <#assign savebarAction = "camtrap-metadata-basic"/>
    <#assign materialHelpIcons = true/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <#function t key>
        <#return action.getText(key)/>
    </#function>

    <#-- one contributor; index "" is the template (fields named metadata.contributors.*, renumbered when added) -->
    <#macro contributorCard index="" item={}>
        <#local base = index?has_content?then("metadata.contributors[" + index + "]", "metadata.contributors")/>
        <#local person = !(item.role)?? || item.role == "contributor" || item.role == "contact" || item.role == "principalInvestigator"/>
        <div <#if index?has_content>id="contributor-item-${index}"<#else>id="baseItem-contributor" style="display: none;"</#if> class="item dp-card">
            <div class="dp-card-head">
                <span class="dp-card-title" data-card-title data-title-from='input[id$="title"]' data-empty="${t('datapackagemetadata.contributor')}"></span>
                <a id="contributor-removeLink<#if index?has_content>-${index}</#if>" href="" class="party-icon-btn party-icon-danger remove-item" title="${removeContributorLink}" aria-label="${removeContributorLink}"><@icon name="delete"/></a>
            </div>
            <div class="row g-3">
                <div class="col-12 title-col-wrapper<#if person> d-none</#if>">
                    <@input name="${base}.title" value=person?then(((item.firstName!"") + " " + (item.lastName!""))?trim, item.title!"") help="i18n" i18nkey="datapackagemetadata.contributor.title" requiredField=true />
                </div>
                <div class="col-lg-6 firstName-col-wrapper<#if !person> d-none</#if>">
                    <@input name="${base}.firstName" value=item.firstName!"" i18nkey="datapackagemetadata.contributor.firstName" />
                </div>
                <div class="col-lg-6 lastName-col-wrapper<#if !person> d-none</#if>">
                    <@input name="${base}.lastName" value=item.lastName!"" i18nkey="datapackagemetadata.contributor.lastName" requiredField=true />
                </div>
                <div class="col-lg-6">
                    <@select name="${base}.role" help="i18n" includeEmpty=true compareValues=true options=contributorRoles i18nkey="datapackagemetadata.contributor.role" value=(item.role)!"" />
                </div>
                <div class="col-lg-6">
                    <@input name="${base}.organization" value=item.organization!"" help="i18n" i18nkey="datapackagemetadata.contributor.organization" />
                </div>
                <div class="col-lg-6">
                    <@input name="${base}.email" value=item.email!"" help="i18n" i18nkey="datapackagemetadata.contributor.email" />
                </div>
                <div class="col-lg-6">
                    <@input name="${base}.path" value=item.path!"" help="i18n" i18nkey="datapackagemetadata.contributor.path" />
                </div>
            </div>
        </div>
    </#macro>

    <#-- one source; index "" is the template -->
    <#macro sourceCard index="" item={}>
        <#local base = index?has_content?then("metadata.sources[" + index + "]", "metadata.sources")/>
        <div <#if index?has_content>id="source-item-${index}"<#else>id="baseItem-source" style="display: none;"</#if> class="item dp-card">
            <div class="dp-card-head">
                <span class="dp-card-title" data-card-title data-title-from='input[id$="title"]' data-empty="${t('datapackagemetadata.source')}"></span>
                <a id="source-removeLink<#if index?has_content>-${index}</#if>" href="" class="party-icon-btn party-icon-danger remove-item" title="${removeSourceLink}" aria-label="${removeSourceLink}"><@icon name="delete"/></a>
            </div>
            <div class="row g-3">
                <div class="col-lg-6">
                    <@input name="${base}.title" value=item.title!"" help="i18n" i18nkey="datapackagemetadata.source.title" requiredField=true />
                </div>
                <div class="col-lg-6">
                    <@input name="${base}.version" value=item.version!"" help="i18n" i18nkey="datapackagemetadata.source.version" />
                </div>
                <div class="col-lg-6">
                    <@input name="${base}.path" value=item.path!"" help="i18n" i18nkey="datapackagemetadata.source.path" />
                </div>
                <div class="col-lg-6">
                    <@input name="${base}.email" value=item.email!"" help="i18n" i18nkey="datapackagemetadata.source.email" />
                </div>
            </div>
        </div>
    </#macro>

    <#assign removeContributorLink = (t('manage.metadata.removethis') + " " + t('datapackagemetadata.contributor'))?lower_case?cap_first/>
    <#assign addContributorLink = (t('manage.metadata.addnew') + " " + t('datapackagemetadata.contributor'))?lower_case?cap_first/>
    <#assign removeSourceLink = (t('manage.metadata.removethis') + " " + t('datapackagemetadata.source'))?lower_case?cap_first/>
    <#assign addSourceLink = (t('manage.metadata.addnew') + " " + t('datapackagemetadata.source'))?lower_case?cap_first/>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form id="camtrap-basic-form" class="needs-validation track-unsaved" action="camtrap-metadata-${section}.do" method="post" novalidate>
        <input type="hidden" name="r" value="${resource.shortname}" />

        <#include "metadata_heading.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout main-content-container">
                <main class="bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "metadata_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">
                        <div class="my-md-3 p-3" id="s-general" data-toc="${t('manage.datapackagemetadata.camtrap.section.general')}">
                            <@textinline name="manage.datapackagemetadata.camtrap.section.general"/>
                            <div class="row g-3 mt-0">
                                <div class="col-12">
                                    <@input name="metadata.title" value=metadata.title! help="i18n" i18nkey="datapackagemetadata.title" requiredField=true />
                                </div>
                                <div class="col-12">
                                    <@text name="metadata.description" value=metadata.description! help="i18n" i18nkey="datapackagemetadata.description" requiredField=true />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="metadata.homepage" value=metadata.homepage! help="i18n" i18nkey="datapackagemetadata.homepage" type="url" />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="metadata.image" value=metadata.image! help="i18n" i18nkey="datapackagemetadata.image" type="url" />
                                </div>
                            </div>
                        </div>

                        <div class="my-3 p-3" id="s-contributors" data-toc="${t('datapackagemetadata.contributors')}">
                            <@textinline name="datapackagemetadata.contributors" help="i18n" requiredField=true />
                            <div id="contributor-items" class="dp-cards">
                                <#list metadata.contributors as item>
                                    <#if item?has_content>
                                        <@contributorCard index=item_index item=item/>
                                    </#if>
                                </#list>
                            </div>
                            <p id="contributor-none" class="dp-none" hidden><@s.text name="manage.datapackagemetadata.camtrap.noContributor"/></p>
                            <div class="add-area">
                                <a id="plus-contributor" href=""><@icon name="add"/> <span>${addContributorLink}</span></a>
                            </div>
                        </div>

                        <div class="my-3 p-3" id="s-licenses" data-toc="${t('datapackagemetadata.licenses')}">
                            <#assign dataLicenseIndex = -1/>
                            <#assign mediaLicenseIndex = -1/>
                            <#list metadata.licenses! as item>
                                <#if (item.scope)?has_content && item.scope == "data">
                                    <#assign dataLicenseIndex = item_index/>
                                <#elseif (item.scope)?has_content && item.scope == "media">
                                    <#assign mediaLicenseIndex = item_index/>
                                </#if>
                            </#list>
                            <#-- a missing licence gets a free index, so the two never share one -->
                            <#assign dataLicenseName = (dataLicenseIndex > -1)?then((metadata.licenses[dataLicenseIndex].name)!"", "")/>
                            <#assign mediaLicenseName = (mediaLicenseIndex > -1)?then((metadata.licenses[mediaLicenseIndex].name)!"", "")/>
                            <#if (dataLicenseIndex < 0)><#assign dataLicenseIndex = (mediaLicenseIndex == 0)?then(1, 0)/></#if>
                            <#if (mediaLicenseIndex < 0)><#assign mediaLicenseIndex = (dataLicenseIndex == 1)?then(0, 1)/></#if>

                            <@textinline name="datapackagemetadata.licenses" help="i18n" requiredField=true />
                            <div class="row g-3 mt-0">
                                <div class="col-lg-6" id="license-data">
                                    <input type="hidden" name="metadata.licenses[${dataLicenseIndex}].scope" value="DATA">
                                    <@select name="metadata.licenses[${dataLicenseIndex}].name" help="i18n" includeEmpty=true options=gbifSupportedLicenseNames i18nkey="datapackagemetadata.license.name.data" value=dataLicenseName requiredField=true/>
                                </div>
                                <div class="col-lg-6" id="license-media">
                                    <input type="hidden" name="metadata.licenses[${mediaLicenseIndex}].scope" value="MEDIA">
                                    <@select name="metadata.licenses[${mediaLicenseIndex}].name" help="i18n" includeEmpty=true options=openDefinitionLicenseNames i18nkey="datapackagemetadata.license.name.media" value=mediaLicenseName requiredField=true />
                                </div>
                            </div>
                        </div>

                        <div class="my-3 p-3" id="s-sources" data-toc="${t('datapackagemetadata.sources')}">
                            <@textinline name="datapackagemetadata.sources" help="i18n" />
                            <div id="source-items" class="dp-cards">
                                <#list metadata.sources as item>
                                    <@sourceCard index=item_index item=item/>
                                </#list>
                            </div>
                            <p id="source-none" class="dp-none" hidden><@s.text name="manage.datapackagemetadata.camtrap.noSource"/></p>
                            <div class="add-area">
                                <a id="plus-source" href=""><@icon name="add"/> <span>${addSourceLink}</span></a>
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>

        <#include "/WEB-INF/pages/manage/eml/eml_savebar.ftl"/>
    </form>

    <@contributorCard/>
    <@sourceCard/>

    <#include "/WEB-INF/pages/manage/eml/unsaved_changes_modal.ftl">

    <#include "/WEB-INF/pages/inc/footer.ftl">
