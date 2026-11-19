<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.MetadataAction" -->
    <#include "/WEB-INF/pages/inc/header.ftl">
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <#include "/WEB-INF/pages/macros/user_id_directories.ftl"/>
    <title><@s.text name='manage.metadata.basic.title'/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-4.0.13.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2/select2-bootstrap4.min.css">
    <link rel="stylesheet" href="${baseURL}/styles/select2-ipt.css">
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <script src="${baseURL}/js/select2/select2-4.0.13.min.js"></script>
    <#include "/WEB-INF/pages/macros/docbook_editor.ftl"/>
    <@docBookEditorAssets/>
    <@translationAssets loadSelect2=false/>
    <script>
        $(document).ready(function(){
            $('#metadata-section').change(function () {
                var metadataSection = $('#metadata-section').find(':selected').val()
                $(location).attr('href', 'metadata-' + metadataSection + '.do?r=${resource.shortname!r!}');
            });

            // scroll to the error if present
            var invalidElements = $(".is-invalid, .bd-content .invalid-feedback li");

            if (invalidElements !== undefined && invalidElements.length > 0) {
                var invalidElement = invalidElements.first();
                var pos = invalidElement.offset().top - 100;
                // scroll to the element
                $('body, html').animate({scrollTop: pos});
            }

            // the translated fields follow the metadata language
            $('select#eml\\.metadataLanguage').on('change', function () {
                window.iptTranslations.setDefault(this.value);
            });

            ['select#eml\\.metadataLanguage', 'select#eml\\.language'].forEach(function (selector) {
                $(selector).select2({
                    placeholder: '',
                    language: {
                        noResults: function () {
                            return '${selectNoResultsFound}';
                        }
                    },
                    width: "100%",
                    theme: 'bootstrap4'
                });
            });
            $('select#eml\\.updateFrequency').select2({
                placeholder: '',
                width: "100%",
                minimumResultsForSearch: 'Infinity',
                theme: 'bootstrap4'
            });

            // the logo: a file (uploaded when the form is saved) or a link to an image. The preview follows both
            var logoPreviewUrl = null;
            var $logoUrl = $("#eml\\.logoUrl");
            var $logoLink = $("#logo-link");
            var uploadedUrl = $logoUrl.data("uploaded") || "";
            function showLogo(src) {
                $("#logo-broken").prop("hidden", true);
                $("#logo-none").prop("hidden", !!src);
                $("#logo-image").prop("hidden", !src).attr("src", src || null);
                $("#buttonRemove").prop("hidden", !src);
            }
            $("#logo-image").on("error", function () {
                if (this.getAttribute("src")) {
                    $(this).prop("hidden", true);
                    $("#logo-broken").prop("hidden", false);
                }
            });
            $("#file").on("change", function () {
                if (logoPreviewUrl) {
                    URL.revokeObjectURL(logoPreviewUrl);
                    logoPreviewUrl = null;
                }
                var file = this.files[0];
                if (file) {
                    logoPreviewUrl = URL.createObjectURL(file);
                    showLogo(logoPreviewUrl);
                    $("#removeLogo").val("false");
                }
            });
            // a typed link replaces the logo; emptied again, an uploaded file is used again
            $logoLink.on("change", function () {
                var link = this.value.trim();
                $logoUrl.val(link || uploadedUrl);
                if (!$("#file").val()) {
                    showLogo(link || uploadedUrl);
                }
            });

            $("#buttonRemove").click(function (event) {
                event.preventDefault();
                if (logoPreviewUrl) {
                    URL.revokeObjectURL(logoPreviewUrl);
                    logoPreviewUrl = null;
                }
                uploadedUrl = "";
                $logoLink.val("").attr("placeholder", "https://");
                $logoUrl.val("");
                $("#file").val("");
                $("#removeLogo").val("true");
                showLogo("");
                this.dispatchEvent(new Event("input", {bubbles: true}));
            });

            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>
    <style>
        .form-control, .form-select {
            min-height: calc(1.5em + .5rem + 2px);
            padding: .25rem .5rem;
            font-size: .875rem;
            border-radius: .2rem;
        }

        /* select2 at the same size (select2-ipt.css) */
        body .select2-container--bootstrap4 {
            --s2-font-size: .875rem;
            --s2-height: calc(1.5em + .5rem + 2px);
            --s2-padding-x: .5rem;
            --s2-radius: .2rem;
        }
    </style>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "basic"/>
    <#include "/WEB-INF/pages/macros/icon.ftl"/>
    <#-- help popovers with the Material info icon (macros/help_icon.ftl) -->
    <#assign materialHelpIcons = true/>
    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/eml.css">
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

<div class="container px-0">
    <#include "/WEB-INF/pages/inc/action_alerts.ftl">
</div>

<form id="basic-metadata-form" class="needs-validation track-unsaved" action="metadata-${section}.do?r=${resource.shortname}" method="post" enctype="multipart/form-data" novalidate>
    <#-- the logo is uploaded with this form (was the Additional Metadata page, #3319) -->
    <input id="removeLogo" name="removeLogo" type="hidden" value="false" />
    <input type="hidden" name="r" value="${resource.shortname}" />

    <#include "eml_heading.ftl"/>

    <#include "metadata_section_select.ftl"/>

    <div class="container-fluid bg-body">
        <div class="container bd-layout main-content-container">
            <main class="bd-main">
                <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                    <#include "eml_sidebar.ftl"/>
                </div>

                <div class="bd-content">

                    <p class="px-3 pt-3 mb-0"><@s.text name="manage.metadata.basic.required.message" /></p>

                    <div class="my-md-3 p-3" id="s-languages" data-toc="${action.getText('manage.metadata.section.languages')}">
                        <@textinline name="manage.metadata.section.languages"/>

                        <div class="row g-3 mt-0">
                            <div class="col-lg-6">
                                <@select name="eml.language" help="i18n" options=languages value="${languageIso3!'eng'}" requiredField=true />
                            </div>

                            <div class="col-lg-6">
                                <@select name="eml.metadataLanguage" help="i18n" options=languages value="${metadataLanguageIso3!'eng'}" requiredField=true />
                            </div>
                        </div>
                    </div>

                    <div class="my-3 p-3" id="s-title" data-toc="${action.getText('eml.title')}">
                        <@textinline name="eml.title" help="i18n" requiredField=true/>

                        <div class="mt-2">
                            <input class="form-control" type="text" id="eml.title" name="eml.title" value="${eml.title!}" required aria-label="${action.getText('eml.title')}"/>
                            <@s.fielderror id="field-error-eml.title" cssClass="invalid-feedback list-unstyled field-error my-1" fieldName="eml.title"/>
                            <script>
                                <#-- JSON from Gson, which escapes <, > and & -->
                                window.iptTranslations.titleField({
                                    input: document.getElementById("eml.title"),
                                    translations: ${action.titleTranslationsJson?no_esc},
                                    label: "${action.getText('eml.title')?js_string?no_esc}",
                                    hint: "${action.getText('manage.metadata.translations.hint.title')?js_string?no_esc}"
                                });
                            </script>
                            <#-- soft hints on the title in the metadata language, they never stop the save -->
                            <div id="title-hint" class="bm-hint" hidden>
                                <@icon name="warning"/> <span></span>
                            </div>
                        </div>

                        <div class="row g-3 mt-0">
                            <div class="col-lg-6">
                                <@input name="eml.shortName" value=eml.shortName! help="i18n" />
                            </div>
                        </div>

                        <#-- chosen on the Publishing settings page -->
                        <div class="bm-ro mt-3">
                            <span><@s.text name="manage.metadata.basic.organisation"/>: <b>${(resource.organisation.name)!action.getText("eml.publishingOrganisation.none")}</b></span>
                            <a class="ms-auto" href="${baseURL}/manage/publication-settings.do?r=${resource.shortname}"><@s.text name="manage.metadata.basic.organisation.change"/></a>
                        </div>
                    </div>

                    <#-- an uploaded logo is served by the IPT itself (MetadataAction.save points the logo URL at logo.do) -->
                    <#assign logoUrl = eml.logoUrl!""/>
                    <#assign logoUploaded = logoUrl?ends_with("/logo.do?r=" + resource.shortname?url("UTF-8"))/>
                    <div class="my-3 p-3" id="s-logo" data-toc="${action.getText('manage.metadata.section.logo')}">
                        <@textinline name="manage.metadata.section.logo" i18nkey="eml.logoUrl" help="i18n"/>

                        <#-- on the page grid like the other fields: inputs as wide as Short Name, the preview beside them -->
                        <div class="row g-3 mt-0">
                            <div class="col-lg-6 bm-logo-input">
                                <div class="d-flex text-smaller"><label for="file" class="form-label"><@s.text name="manage.metadata.basic.logo.upload"/></label></div>
                                <@s.file cssClass="form-control" name="file" id="file" accept="image/jpeg,image/png,image/gif"/>
                                <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="file"/>
                                <div class="bm-logo-note"><@s.text name="manage.metadata.basic.logo.formats"/></div>

                                <div class="d-flex text-smaller mt-3"><label for="logo-link" class="form-label"><@s.text name="manage.metadata.basic.logo.link"/></label></div>
                                <#-- the stored value is posted from the hidden input: an uploaded file's internal URL isn't shown -->
                                <input type="url" class="form-control" id="logo-link" value="<#if !logoUploaded>${logoUrl}</#if>" placeholder="<#if logoUploaded>${action.getText('manage.metadata.basic.logo.uploaded')}<#else>https://</#if>">
                                <input type="hidden" id="eml.logoUrl" name="eml.logoUrl" value="${logoUrl}" data-uploaded="${logoUploaded?then(logoUrl, '')}">
                                <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="eml.logoUrl"/>
                            </div>

                            <div class="col-lg-6">
                                <div class="d-flex text-smaller"><span class="form-label"><@s.text name="manage.metadata.basic.logo.preview"/></span></div>
                                <div class="bm-logo-preview" id="resourcelogo">
                                    <img id="logo-image" alt="${action.getText('manage.metadata.section.logo')}"<#if logoUrl?has_content> src="${logoUrl}"<#else> hidden</#if>>
                                    <span class="bm-logo-empty" id="logo-none"<#if logoUrl?has_content> hidden</#if>><@s.text name="manage.metadata.basic.logo.none"/></span>
                                    <span class="bm-logo-empty" id="logo-broken" hidden><@s.text name="manage.metadata.basic.logo.broken"/></span>
                                </div>
                                <button type="button" class="btn btn-sm btn-link bm-logo-remove" id="buttonRemove"<#if !logoUrl?has_content> hidden</#if>><@icon name="delete"/> <@s.text name="manage.metadata.basic.logo.remove"/></button>
                            </div>
                        </div>
                    </div>

                    <#if (resource.coreType) != "dwc-dp">
                    <#assign coreType = (resource.coreType!"")?lower_case/>
                    <#assign typeLocked = resourceHasCore == "true"/>
                    <#-- which types each subtype belongs to (MetadataAction groups the subtype vocabulary) -->
                    <#assign subtypeLabels = {}/>
                    <#assign subtypeTypes = {}/>
                    <#list {"occurrence": occurrenceSubtypesMap, "checklist": checklistSubtypesMap, "samplingevent": samplingEventSubtypesMap} as type, subtypes>
                        <#list subtypes as key, label>
                            <#if key?has_content>
                                <#assign subtypeLabels = subtypeLabels + {key: label}/>
                                <#assign subtypeTypes = subtypeTypes + {key: (subtypeTypes[key]!"") + " " + type}/>
                            </#if>
                        </#list>
                    </#list>
                    <#assign subtype = (resource.subtype!"")?lower_case/>
                    <div class="my-3 p-3" id="s-type" data-toc="${action.getText('manage.metadata.section.type')}">
                        <@textinline name="manage.metadata.section.type" requiredField=true/>

                        <#if typeLocked>
                            <#-- fixed by the core mapping: only the type itself, nothing to choose; resource.coreType isn't posted, so it keeps its value -->
                            <div class="bm-ro mt-2">
                                <@icon name="lock"/>
                                <b>${types[coreType]!resource.coreType}</b>
                                <span><@s.text name="manage.metadata.basic.type.locked"/></span>
                            </div>
                        <#else>
                            <div class="bm-cards mt-2" role="radiogroup" aria-label="${action.getText('resource.coreType')}">
                                <#list types as key, label>
                                    <#if key?has_content>
                                        <label class="geo-mode bm-choice">
                                            <input type="radio" class="form-check-input flex-shrink-0" name="resource.coreType" value="${key}"<#if key == coreType> checked</#if>>
                                            <span>
                                                <span class="geo-mode-label">${label}</span>
                                                <span class="geo-mode-desc"><@s.text name="manage.metadata.basic.type.${key}"/></span>
                                            </span>
                                        </label>
                                    </#if>
                                </#list>
                            </div>
                        </#if>
                        <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="resource.coreType"/>

                        <div id="subtype-part" class="mt-3"<#if !(coreType?has_content && subtypeTypes?values?join(" ")?contains(coreType))> hidden</#if>>
                            <div class="d-flex text-smaller">
                                <a tabindex="0" role="button" class="popover-link" data-bs-toggle="popover" data-bs-trigger="focus" data-bs-html="true"
                                   data-bs-content="${action.getText('resource.subtype.help')}"><@icon name="info_outlined" class="text-gbif-primary help-icon-m"/></a>
                                <span class="form-label"><@s.text name="resource.subtype"/></span>
                            </div>
                            <input type="hidden" id="resource.subtype" name="resource.subtype" value="${subtype}"/>
                            <div class="bm-chips" role="group" aria-label="${action.getText('resource.subtype')}">
                                <#list subtypeLabels as key, label>
                                    <button type="button" class="bm-chip" data-subtype="${key}" data-types="${subtypeTypes[key]} "
                                            aria-pressed="${(key == subtype)?c}"<#if !(subtypeTypes[key] + " ")?contains(" " + coreType + " ")> hidden</#if>>${label}</button>
                                </#list>
                            </div>
                        </div>
                    </div>
                    </#if>

                    <#assign selectedLicence = licenseKeySelected!""/>
                    <#assign unsupportedLicence = eml.intellectualRights?has_content && !selectedLicence?has_content/>
                    <div class="my-3 p-3" id="s-licence" data-toc="${action.getText('eml.intellectualRights.license')}">
                        <@textinline name="eml.intellectualRights.license" help="i18n" requiredField=true/>

                        <#if unsupportedLicence>
                            <#-- e.g. from an uploaded EML: only the licences GBIF supports can be chosen. The text stays until one is -->
                            <div class="ov-banner bad compact mt-2 mb-2">
                                <span class="ov-ic text-gbif-danger"><@icon name="error"/></span>
                                <span class="ov-grow"><@s.text name="validation.license.gbif.unsupported"><@s.param>${action.getLicenseLabel(eml)}</@s.param></@s.text></span>
                            </div>
                        </#if>

                        <#-- the Creative Commons icons of the public resource page (images/icons/cc) -->
                        <#assign licenceIcons = {"cczero": ["cc", "zero"], "ccby": ["cc", "by"], "ccbync": ["cc", "by", "nc"]}/>
                        <#assign licencePerms = {"cczero": ["+anyUse", "+noAttribution"], "ccby": ["+anyUse", "+attribution"], "ccbync": ["+attribution", "-nonCommercial"]}/>
                        <div class="bm-cards mt-2" role="radiogroup" aria-label="${action.getText('eml.intellectualRights.license')}">
                            <#-- the licence text is what the EML stores -->
                            <#if unsupportedLicence>
                                <input type="radio" class="d-none" name="eml.intellectualRights" value="${eml.intellectualRights}" checked aria-hidden="true" tabindex="-1">
                            </#if>
                            <#list licenses as key, licenceName>
                                <#if !key?has_content || !licenseTexts[key]??><#continue></#if>
                                <#assign licenceText = licenseTexts[key]/>
                                <label class="geo-mode bm-choice">
                                    <input type="radio" class="form-check-input flex-shrink-0" name="eml.intellectualRights" value="${licenceText}"<#if key == selectedLicence> checked</#if>>
                                    <span>
                                        <span class="geo-mode-label">
                                            <#if licenceIcons[key]??><span class="bm-cc"><#list licenceIcons[key] as i><img src="${baseURL}/images/icons/cc/${i}.svg" alt=""></#list></span></#if>
                                            ${licenceName}
                                        </span>
                                        <#if licencePerms[key]??>
                                            <ul class="bm-perm">
                                                <#list licencePerms[key] as perm>
                                                    <li><#if perm?starts_with("+")><@icon name="check" class="bm-y"/><#else><@icon name="close" class="bm-n"/></#if> <@s.text name="manage.metadata.basic.licence.${perm?substring(1)}"/></li>
                                                </#list>
                                            </ul>
                                        </#if>
                                    </span>
                                </label>
                            </#list>
                        </div>
                        <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="eml.intellectualRights.license"/>
                        <p class="text-smaller text-muted mt-2 mb-0"><@s.text name="eml.intellectualRights.license.disclaimer"/></p>
                    </div>

                    <div class="my-3 p-3" id="s-description" data-toc="${action.getText('eml.description')}">
                        <!-- Descriptions, broken into one or more paragraphs -->
                        <@textinline name="eml.description" help="i18n" i18nkey="" requiredField=true/>

                        <div class="mt-2">
                            <@docBookEditor name="eml.description" id="description" value=eml.description! label=action.getText("eml.description") translatable=true translationHint=action.getText("manage.metadata.translations.hint.description")/>
                        </div>
                    </div>

                    <#-- Purpose, Introduction, Getting Started (were the Additional Description page) and Additional information
                         (was the Additional Metadata page, #3319): rarely filled in, so an empty one is folded to a row with an
                         Add link. A filled one is always open -->
                    <#macro optionalPart id heading description value>
                        <#local partFilled = value?replace("<[^>]*>", "", "r")?replace("&nbsp;", " ")?trim?has_content/>
                        <div class="my-3 px-3 py-2 bm-opt<#if !partFilled> folded</#if>" id="${id}" data-toc="${action.getText(heading)}">
                            <@textinline name=heading help="i18n"/>
                            <p class="bm-opt-desc text-smaller fst-italic mb-0"><@s.text name=description/></p>
                            <button type="button" class="btn btn-link metadata-action-link custom-link bm-opt-add"><@icon name="add" class="link-icon-primary"/> <span><@s.text name="manage.metadata.basic.optional.add"/></span></button>
                            <div class="bm-opt-body mt-3">
                                <#nested>
                            </div>
                        </div>
                    </#macro>

                    <div class="bm-opt-group mx-3 mt-5">
                        <span class="bm-opt-group-title"><@s.text name="manage.metadata.basic.optional"/></span>
                        <span class="bm-opt-group-note"><@s.text name="manage.metadata.basic.optional.note"/></span>
                    </div>

                    <@optionalPart id="s-purpose" heading="eml.purpose" description="eml.purpose.description" value=eml.purpose!"">
                        <@docBookEditor name="eml.purpose" id="purpose" value=eml.purpose! label=action.getText("eml.purpose")/>
                    </@optionalPart>

                    <@optionalPart id="s-introduction" heading="manage.metadata.introduction" description="manage.metadata.introduction.description" value=eml.introduction!"">
                        <@docBookEditor name="eml.introduction" id="introduction" value=eml.introduction! label=action.getText("manage.metadata.introduction")/>
                    </@optionalPart>

                    <@optionalPart id="s-getting-started" heading="manage.metadata.gettingStarted" description="manage.metadata.gettingStarted.description" value=eml.gettingStarted!"">
                        <@docBookEditor name="eml.gettingStarted" id="gettingStarted" value=eml.gettingStarted! label=action.getText("manage.metadata.gettingStarted")/>
                    </@optionalPart>

                    <@optionalPart id="s-additional-info" heading="eml.additionalInfo" description="manage.metadata.basic.additionalInfo.description" value=eml.additionalInfo!"">
                        <textarea id="eml.additionalInfo" name="eml.additionalInfo" class="form-control" rows="5" aria-label="${action.getText('eml.additionalInfo')}">${eml.additionalInfo!}</textarea>
                    </@optionalPart>

                    <#-- Keywords (were a page of their own, #3319). The sets of the GBIF dataset type and subtype vocabularies are
                         made from the Dataset type part on every save (ResourceManagerImpl.updateKeywordsWithDatasetTypeAndSubtype),
                         so they're only shown, not posted. Each other set is a card: keywords as chips, and its vocabulary -->
                    <#assign autoThesauri = ["gbif dataset type vocabulary: http://rs.gbif.org/vocabulary/gbif/dataset_type_2015-07-10.xml",
                                             "gbif dataset subtype vocabulary: http://rs.gbif.org/vocabulary/gbif/dataset_subtype.xml"]/>
                    <#assign autoKeywords = []/>
                    <#assign userSets = []/>
                    <#list eml.keywords as set>
                        <#if autoThesauri?seq_contains((set.keywordThesaurus!"")?lower_case)>
                            <#assign autoKeywords = autoKeywords + [set.keywordsString!""]/>
                        <#else>
                            <#assign userSets = userSets + [set]/>
                        </#if>
                    </#list>

                    <#macro keywordSet index set="" errors=true>
                        <#-- work around a bug that turns an empty keyword list into "null" (gbif-metadata-profile) -->
                        <#local keywordsString = (set?is_hash)?then(set.keywordsString!"", "")/>
                        <#if keywordsString?lower_case == "null"><#local keywordsString = ""/></#if>
                        <#local base = "eml.keywords[" + index + "]."/>
                        <div class="kw-set">
                            <div class="kw-head handle">
                                <div class="d-flex text-smaller">
                                    <a tabindex="0" role="button" class="popover-link" data-bs-toggle="popover" data-bs-trigger="focus" data-bs-html="true"
                                       data-bs-content="${action.getText('eml.keywords.keywordsString.help')}"><@icon name="info_outlined" class="text-gbif-primary help-icon-m"/></a>
                                    <label class="form-label mb-0" data-for="keywordsString" for="${base}keywordsString-input"><@s.text name="eml.keywords.keywordsString"/> <span class="text-gbif-danger">*</span></label>
                                </div>
                                <button type="button" class="kw-remove" title="${action.getText('manage.metadata.basic.keywords.remove')}" aria-label="${action.getText('manage.metadata.basic.keywords.remove')}"><@icon name="delete"/></button>
                            </div>
                            <div class="form-control kw-chips">
                                <#list keywordsString?split(",") as keyword>
                                    <#if keyword?trim?has_content>
                                        <span class="kw-chip"><span>${keyword?trim}</span><button type="button" class="kw-chip-x" aria-label="${action.getText('manage.metadata.basic.keywords.removeKeyword', [keyword?trim])}"><@icon name="close"/></button></span>
                                    </#if>
                                </#list>
                                <input type="text" class="kw-entry" id="${base}keywordsString-input" data-for="keywordsString-input" placeholder="${action.getText('manage.metadata.basic.keywords.placeholder')}">
                            </div>
                            <input type="hidden" class="kw-value" name="${base}keywordsString" data-name="keywordsString" value="${keywordsString}">
                            <#if errors><@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="${base}keywordsString"/></#if>

                            <div class="d-flex text-smaller mt-2">
                                <a tabindex="0" role="button" class="popover-link" data-bs-toggle="popover" data-bs-trigger="focus" data-bs-html="true"
                                   data-bs-content="${action.getText('eml.keywords.keywordThesaurus.help')}"><@icon name="info_outlined" class="text-gbif-primary help-icon-m"/></a>
                                <label class="form-label" data-for="keywordThesaurus" for="${base}keywordThesaurus"><@s.text name="eml.keywords.keywordThesaurus"/> <span class="text-gbif-danger">*</span></label>
                            </div>
                            <input type="text" class="form-control" id="${base}keywordThesaurus" name="${base}keywordThesaurus" data-name="keywordThesaurus" value="${(set?is_hash)?then(set.keywordThesaurus!"", "")}" placeholder="${action.getText('manage.metadata.basic.keywords.vocabulary.placeholder')}">
                            <#if errors><@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="${base}keywordThesaurus"/></#if>
                        </div>
                    </#macro>

                    <div class="my-3 p-3" id="s-keywords" data-toc="${action.getText('manage.metadata.keywords.title')}">
                        <@textinline name="manage.metadata.keywords.title"/>
                        <p class="text-smaller text-muted mt-1 mb-2"><@s.text name="manage.metadata.basic.keywords.intro"/></p>

                        <#if autoKeywords?has_content>
                            <div class="bm-ro mb-3">
                                <span><@s.text name="manage.metadata.basic.keywords.auto"/></span>
                                <#list autoKeywords as keyword><b>${keyword}</b><#sep>, </#sep></#list>
                            </div>
                        </#if>

                        <div id="kw-sets" class="kw-sets">
                            <#list userSets as set>
                                <@keywordSet index=set?index set=set/>
                            </#list>
                        </div>
                        <p id="kw-none" class="text-smaller text-muted mb-0"<#if userSets?has_content> hidden</#if>><@s.text name="manage.metadata.basic.keywords.none"/></p>

                        <button type="button" id="kw-add" class="btn btn-link metadata-action-link custom-link bm-opt-add mt-2"><@icon name="add" class="link-icon-primary"/> <span><@s.text name="manage.metadata.basic.keywords.add"/></span></button>
                        <template id="kw-template"><@keywordSet index=0 errors=false/></template>
                    </div>

                    <div class="my-3 p-3" id="s-maintenance" data-toc="${action.getText('eml.maintenance')}">
                        <@textinline name="eml.maintenance" help="i18n"/>

                        <div class="row g-3 mt-2">
                            <div class="col-lg-6">
                                <@select name="eml.updateFrequency" i18nkey="eml.updateFrequency" help="i18n" options=frequencies value="${eml.updateFrequency.identifier!'unknown'}" requiredField=true />
                            </div>
                        </div>

                        <#-- the update frequency defaults to the auto-publishing interval: say whether they still match -->
                        <#if resource.publicationMode.name() == "AUTO_PUBLISH_ON" && (resource.updateFrequency.identifier)??>
                            <#assign autoInterval = resource.updateFrequency.identifier/>
                            <div id="auto-publishing" class="bm-sync mt-2" data-interval="${autoInterval}">
                                <span class="bm-sync-ok"><@icon name="check" class="bm-y"/> <@s.text name="manage.metadata.basic.autoPublishing.matches"/></span>
                                <span class="bm-sync-diff">
                                    <@s.text name="manage.metadata.basic.autoPublishing"><@s.param>${frequencies[autoInterval]!autoInterval}</@s.param></@s.text>
                                    <a href="${baseURL}/manage/publication-settings.do?r=${resource.shortname}"><@s.text name="manage.publicationSettings.title"/></a>
                                </span>
                            </div>
                        </#if>

                        <div class="row g-3 mt-0">
                            <div class="col-12">
                                <!-- Maintenance Update Frequency -->
                                <@text name="eml.updateFrequencyDescription" value=eml.updateFrequencyDescription! i18nkey="eml.updateFrequencyDescription" help="i18n" />
                                <#-- "Other maintenance period" says nothing without a description -->
                                <div id="freq-desc-error" class="invalid-feedback"><@s.text name="manage.metadata.basic.otherPeriod.required"/></div>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </div>

    <#include "eml_savebar.ftl"/>
</form>

<script>
    (function () {
        // subtypes follow the type: only the chosen type's chips show, a click picks one, a second click clears it
        var subtypeInput = document.getElementById("resource.subtype");
        var subtypePart = document.getElementById("subtype-part");
        function showSubtypes(type) {
            var any = false;
            document.querySelectorAll(".bm-chip").forEach(function (chip) {
                var fits = !!type && chip.dataset.types.indexOf(" " + type + " ") !== -1;
                chip.hidden = !fits;
                any = any || fits;
            });
            subtypePart.hidden = !any;
        }
        function pickSubtype(value) {
            subtypeInput.value = value;
            document.querySelectorAll(".bm-chip").forEach(function (chip) {
                chip.setAttribute("aria-pressed", String(chip.dataset.subtype === value));
            });
            subtypeInput.dispatchEvent(new Event("input", {bubbles: true}));
        }
        if (subtypeInput) {
            document.querySelectorAll("input[name='resource.coreType']").forEach(function (radio) {
                radio.addEventListener("change", function () {
                    pickSubtype("");
                    showSubtypes(this.value);
                });
            });
            document.querySelectorAll(".bm-chip").forEach(function (chip) {
                chip.addEventListener("click", function () {
                    pickSubtype(subtypeInput.value === chip.dataset.subtype ? "" : chip.dataset.subtype);
                });
            });
        }

        // soft hints on the title in the metadata language, saving is never blocked
        var title = document.getElementById("eml.title");
        var titleHint = document.getElementById("title-hint");
        var hints = {
            fileName: "${action.getText('manage.metadata.basic.titleHint.fileName')?js_string?no_esc}",
            caps: "${action.getText('manage.metadata.basic.titleHint.caps')?js_string?no_esc}",
            short: "${action.getText('manage.metadata.basic.titleHint.short')?js_string?no_esc}"
        };
        function checkTitle() {
            var value = title.value.trim();
            var hint = null;
            if (!value) {
                hint = null;
            } else if (/\.(csv|tsv|txt|xlsx?|ods|zip|xml|json)$/i.test(value) || /\S_\S/.test(value)) {
                hint = hints.fileName;
            } else if (value.length > 8 && value === value.toUpperCase() && value !== value.toLowerCase()) {
                hint = hints.caps;
            } else if (value.split(/\s+/).length < 3) {
                hint = hints.short;
            }
            titleHint.hidden = !hint;
            titleHint.querySelector("span").textContent = hint || "";
        }
        if (title && titleHint) {
            title.addEventListener("input", checkTitle);
            checkTitle();
        }

        // the folded optional parts: open on Add, on a jump from "On this page" or the URL, and when they have an error
        function unfold(part, focus) {
            if (!part || !part.classList.contains("folded")) {
                return;
            }
            part.classList.remove("folded");
            if (focus) {
                var field = part.querySelector(".ProseMirror, textarea");
                field && field.focus();
            }
        }
        document.querySelectorAll(".bm-opt").forEach(function (part) {
            part.querySelector(".bm-opt-add").addEventListener("click", function () { unfold(part, true); });
            if (part.querySelector(".invalid-feedback li")) {
                unfold(part, false);
            }
        });
        document.addEventListener("click", function (e) {
            var link = e.target.closest("a[href^='#s-']");
            if (link) {
                unfold(document.getElementById(link.getAttribute("href").substring(1)), false);
            }
        });
        if (location.hash) {
            unfold(document.getElementById(location.hash.substring(1)), false);
        }

        // keyword sets: keywords as chips (posted as one comma-separated value), add, remove and reorder the sets
        var kwSets = document.getElementById("kw-sets");
        function kwChanged() {
            kwSets.dispatchEvent(new Event("input", {bubbles: true}));
        }
        function kwRenumber() {
            kwSets.querySelectorAll(".kw-set").forEach(function (set, index) {
                var base = "eml.keywords[" + index + "].";
                set.querySelectorAll("[data-name]").forEach(function (field) {
                    field.name = base + field.dataset.name;
                    if (field.type !== "hidden") {
                        field.id = base + field.dataset.name;
                    }
                });
                set.querySelector(".kw-entry").id = base + "keywordsString-input";
                set.querySelectorAll("label[data-for]").forEach(function (label) {
                    label.htmlFor = base + label.dataset.for;
                });
            });
            document.getElementById("kw-none").hidden = !!kwSets.querySelector(".kw-set");
        }
        function kwSync(set) {
            var words = [];
            set.querySelectorAll(".kw-chip > span").forEach(function (chip) { words.push(chip.textContent); });
            set.querySelector(".kw-value").value = words.join(", ");
        }
        function kwChip(text) {
            var chip = document.createElement("span");
            chip.className = "kw-chip";
            var label = document.createElement("span");
            label.textContent = text;
            var x = document.createElement("button");
            x.type = "button";
            x.className = "kw-chip-x";
            x.setAttribute("aria-label", "${action.getText('manage.metadata.basic.keywords.removeKeyword', ['{0}'])?js_string?no_esc}".replace("{0}", text));
            x.innerHTML = '<@icon name="close"/>';
            chip.appendChild(label);
            chip.appendChild(x);
            return chip;
        }
        // the typed text becomes chips: commas separate keywords
        function kwFlush(entry) {
            var set = entry.closest(".kw-set");
            var existing = [];
            set.querySelectorAll(".kw-chip > span").forEach(function (chip) { existing.push(chip.textContent.toLowerCase()); });
            entry.value.split(",").forEach(function (word) {
                word = word.trim();
                if (word && existing.indexOf(word.toLowerCase()) === -1) {
                    entry.before(kwChip(word));
                    existing.push(word.toLowerCase());
                }
            });
            var had = entry.value.trim() !== "";
            entry.value = "";
            kwSync(set);
            if (had) {
                kwChanged();
            }
        }
        kwSets.addEventListener("keydown", function (e) {
            var entry = e.target.closest(".kw-entry");
            if (!entry) {
                return;
            }
            if (e.key === "Enter" || e.key === ",") {
                e.preventDefault();
                kwFlush(entry);
            } else if (e.key === "Backspace" && !entry.value) {
                var last = entry.previousElementSibling;
                if (last && last.classList.contains("kw-chip")) {
                    last.remove();
                    kwSync(entry.closest(".kw-set"));
                    kwChanged();
                }
            }
        });
        kwSets.addEventListener("paste", function (e) {
            var entry = e.target.closest(".kw-entry");
            if (entry) {
                setTimeout(function () { kwFlush(entry); }, 0);
            }
        });
        kwSets.addEventListener("focusout", function (e) {
            var entry = e.target.closest(".kw-entry");
            entry && kwFlush(entry);
        });
        kwSets.addEventListener("click", function (e) {
            var x = e.target.closest(".kw-chip-x");
            if (x) {
                var set = x.closest(".kw-set");
                x.closest(".kw-chip").remove();
                kwSync(set);
                kwChanged();
                return;
            }
            var remove = e.target.closest(".kw-remove");
            if (remove) {
                remove.closest(".kw-set").remove();
                kwRenumber();
                kwChanged();
                return;
            }
            // a click in the box types into it
            var box = e.target.closest(".kw-chips");
            box && box.querySelector(".kw-entry").focus();
        });
        document.getElementById("kw-add").addEventListener("click", function () {
            var set = document.getElementById("kw-template").content.firstElementChild.cloneNode(true);
            kwSets.appendChild(set);
            kwRenumber();
            if (typeof initInfoPopovers === "function") {
                initInfoPopovers(set);
            }
            set.querySelector(".kw-entry").focus();
            kwChanged();
        });
        // html5sortable is loaded by inc/footer.ftl, after this script
        document.addEventListener("DOMContentLoaded", function () {
            sortable("#kw-sets", {forcePlaceholderSize: true, placeholderClass: "kw-placeholder", handle: ".handle", items: ".kw-set"});
            kwSets.addEventListener("sortupdate", function () {
                kwRenumber();
                kwChanged();
            });
        });
        // text still in a box when saving becomes keywords too
        document.getElementById("basic-metadata-form").addEventListener("submit", function () {
            kwSets.querySelectorAll(".kw-entry").forEach(kwFlush);
        }, true);

        // maintenance: match with the auto-publishing interval, and a description for "Other maintenance period"
        var frequency = $("select#eml\\.updateFrequency");
        var frequencyDescription = document.getElementById("eml.updateFrequencyDescription");
        var frequencyError = document.getElementById("freq-desc-error");
        var autoPublishing = document.getElementById("auto-publishing");
        var descriptionLabel = document.querySelector("label[for='eml.updateFrequencyDescription']");
        var requiredMark = document.createElement("span");
        requiredMark.className = "text-gbif-danger";
        requiredMark.textContent = " *";
        descriptionLabel && descriptionLabel.appendChild(requiredMark);

        function needsDescription() {
            return frequency.val() === "otherMaintenancePeriod";
        }
        function checkMaintenance(showError) {
            var other = needsDescription();
            requiredMark.hidden = !other;
            frequencyDescription.required = other;
            var missing = other && !frequencyDescription.value.trim();
            if (showError || !missing) {
                frequencyDescription.classList.toggle("is-invalid", missing);
                frequencyError.classList.toggle("d-block", missing);
            }
            if (autoPublishing) {
                autoPublishing.classList.toggle("matches", frequency.val() === autoPublishing.dataset.interval);
            }
            return !missing;
        }
        frequency.on("change", function () { checkMaintenance(false); });
        frequencyDescription.addEventListener("input", function () { checkMaintenance(false); });
        checkMaintenance(false);

        document.getElementById("basic-metadata-form").addEventListener("submit", function (e) {
            if (!checkMaintenance(true)) {
                e.preventDefault();
                e.stopImmediatePropagation();
                frequencyDescription.focus();
                frequencyDescription.scrollIntoView({block: "center"});
            }
        }, true);
    })();
</script>

<#include "/WEB-INF/pages/manage/eml/unsaved_changes_modal.ftl">

<#include "/WEB-INF/pages/inc/footer.ftl">
