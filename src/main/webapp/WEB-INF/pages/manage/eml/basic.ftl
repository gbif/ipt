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

                var nameRights = $("#eml\\.intellectualRights\\.license").val();
                $("#eml\\.intellectualRights\\.license").val(nameRights);

                if (nameRights) {
                    var licenseText=$("input:text#" + nameRights).val();

                    if (licenseText) {
                        $("#intellectualRightsDiv .cc_text").html(licenseText);
                        $("#intellectualRightsDiv").show();
                        $("#intellectualRights").val(licenseText);
                        $("#eml\\.intellectualRights").val(licenseText);

                        $("#disclaimerRights").css('display', '');
                    }
                } else {
                    $("#intellectualRightsDiv .cc_text").html('');
                    $("#intellectualRightsDiv").hide();

                    $("#intellectualRights").val('');
                    $("#disclaimerRights").css('display', 'none');
                    $("#eml\\.intellectualRights").val('');
                }
            });
            // end intellectual rights

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

            // the translated fields follow the metadata language
            $('select#eml\\.metadataLanguage').on('change', function () {
                window.iptTranslations.setDefault(this.value);
            });

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

            // the logo (was the Additional Metadata page, #3319) is uploaded when the form is saved, show a preview of the chosen file until then
            var logoPreviewUrl = null;
            $("#file").on("change", function () {
                if (logoPreviewUrl) {
                    URL.revokeObjectURL(logoPreviewUrl);
                    logoPreviewUrl = null;
                }
                var file = this.files[0];
                if (file) {
                    logoPreviewUrl = URL.createObjectURL(file);
                    $("#resourcelogo").empty().append($("<img>").attr("src", logoPreviewUrl));
                    $("#removeLogo").val("false");
                }
            });

            $("#buttonRemove").click(function (event) {
                event.preventDefault();
                $("#resourcelogo").empty();
                $("#eml\\.logoUrl").val("");
                $("#file").val("");
                $("#removeLogo").val("true");
                this.dispatchEvent(new Event("input", {bubbles: true}));
            });

            // reordering the keyword sets (were the Keywords page, #3319)
            function initAndGetSortable(selector) {
                return sortable(selector, {
                    forcePlaceholderSize: true,
                    placeholderClass: 'border',
                    handle: '.handle'
                });
            }

            const sortable_keywords = initAndGetSortable('#items');

            sortable_keywords[0].addEventListener('sortupdate', changeInputNamesAfterDragging);
            sortable_keywords[0].addEventListener('drag', dragScroll);

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
                var contactItems = $("#items div.item");

                contactItems.each(function (index) {
                    var elementId = $(this)[0].id;

                    $("div#" + elementId + " input[id$='keywordThesaurus']").attr("name", "eml.keywords[" + index + "].keywordThesaurus");
                    $("div#" + elementId + " textarea[id$='keywordsString']").attr("name", "eml.keywords[" + index + "].keywordsString");
                });

                hideProcessing();
            }

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

                    <div class="my-md-3 p-3" id="s-title" data-toc="${action.getText('eml.title')}">

                        <p><@s.text name="manage.metadata.basic.required.message" /></p>

                        <div class="row g-3">
                            <div class="col-12">
                                <@input name="eml.title" value=eml.title! help="i18n" requiredField=true />
                                <script>
                                    <#-- JSON from Gson, which escapes <, > and & -->
                                    window.iptTranslations.titleField({
                                        input: document.getElementById("eml.title"),
                                        translations: ${action.titleTranslationsJson?no_esc},
                                        label: "${action.getText('eml.title')?js_string?no_esc}",
                                        hint: "${action.getText('manage.metadata.translations.hint.title')?js_string?no_esc}"
                                    });
                                </script>
                            </div>
                        </div>

                        <div class="row g-3 mt-0">
                            <div class="col-lg-6">
                                <@input name="eml.shortName" value=eml.shortName! help="i18n" />
                            </div>
                        </div>
                    </div>

                    <div class="my-0 px-3 py-0">
                        <div class="callout callout-info text-smaller">
                            <@s.text name="manage.metadata.basic.organisation.moved"/>
                            <a href="${baseURL}/manage/publication-settings.do?r=${resource.shortname}" target="_blank">
                                <@s.text name="manage.publicationSettings.title"/>
                            </a>.
                        </div>
                    </div>

                    <#if (resource.coreType) != "dwc-dp">
                    <div class="my-3 p-3" id="s-type" data-toc="${action.getText('manage.metadata.section.type')}">
                        <div class="row g-3">
                            <div class="col-lg-6">
                                <@select name="resource.coreType" i18nkey="resource.coreType" help="i18n" options=types value="${resource.coreType!''}" requiredField=true />
                            </div>

                            <div class="col-lg-6">
                                <@select name="resource.subtype" i18nkey="resource.subtype" help="i18n" options=listSubtypes value="${resource.subtype!''}" />
                            </div>
                        </div>
                    </div>
                    </#if>

                    <div class="my-3 p-3" id="s-languages" data-toc="${action.getText('manage.metadata.section.languages')}">
                        <div class="row g-3">
                            <div class="col-lg-6">
                                <@select name="eml.language" help="i18n" options=languages value="${languageIso3!'eng'}" requiredField=true />
                            </div>

                            <div class="col-lg-6">
                                <@select name="eml.metadataLanguage" help="i18n" options=languages value="${metadataLanguageIso3!'eng'}" requiredField=true />
                            </div>
                        </div>
                    </div>

                    <div class="my-3 p-3" id="s-licence" data-toc="${action.getText('eml.intellectualRights.license')}">
                        <div class="row g-3">
                            <!-- Intellectual Rights -->
                            <div class="col-12">
                                <@select name="eml.intellectualRights.license" i18nkey="eml.intellectualRights.license" help="i18n" options=licenses value="${licenseKeySelected!}" requiredField=true/>

                                <#assign gbifLicense = action.getGbifLicense(eml)!""/>
                                <div id="intellectualRightsDiv" class="mt-3 p-3 fs-smaller cc_main_container">
                                    <div class="cc_logo">
                                        <@licenseLogoClass gbifLicense/>
                                    </div>

                                    <div class="cc_text">
                                        <#if eml.intellectualRights?has_content>
                                            <#if gbifLicense == "CC_BY_NC_4_0">
                                                <@s.text name='eml.intellectualRights.licence.ccbync'/>
                                            <#elseif gbifLicense == "CC_BY_4_0">
                                                <@s.text name='eml.intellectualRights.licence.ccby'/>
                                            <#elseif gbifLicense == "CC0_1_0">
                                                <@s.text name='eml.intellectualRights.licence.cczero'/>
                                            <#else>
                                                <!-- e.g. from an uploaded EML: the dropdown only offers the licenses GBIF supports -->
                                                <span class="text-gbif-danger"><@s.text name="validation.license.gbif.unsupported"><@s.param>${action.getLicenseLabel(eml)}</@s.param></@s.text></span>
                                            </#if>
                                        </#if>
                                    </div>
                                </div>
                                <!-- internal parameter -->
                                <input id="eml.intellectualRights" name="eml.intellectualRights" type="hidden" value="${eml.intellectualRights!}" />

                                <!-- Hidden inputs storing license texts used in populating ipr textarea when a different license gets selected -->
                                <#list licenseTexts?keys as k>
                                    <input type="text" id="${k}" value="${licenseTexts[k]}" style="display: none"/>
                                </#list>

                                <div id='disclaimerRights' style='display: none'>
                                    <p class="mt-3">
                                        <@s.text name='eml.intellectualRights.license.disclaimer'/>
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="my-3 p-3" id="s-description" data-toc="${action.getText('eml.description')}">
                        <!-- Descriptions, broken into one or more paragraphs -->
                        <@textinline name="eml.description" help="i18n" i18nkey="" requiredField=true/>

                        <div class="mt-3">
                            <@docBookEditor name="eml.description" id="description" value=eml.description! label=action.getText("eml.description") translatable=true translationHint=action.getText("manage.metadata.translations.hint.description")/>
                        </div>
                    </div>

                    <!-- Purpose, Introduction, Getting Started (were the Additional Description page, #3319) -->
                    <div class="my-3 p-3" id="s-purpose" data-toc="${action.getText('eml.purpose')}">
                        <@textinline name="eml.purpose" help="i18n"/>

                        <div>
                            <p class="mb-3 mt-3 text-smaller fst-italic">
                                <@s.text name='eml.purpose.description'/>
                            </p>
                        </div>

                        <div class="mt-3">
                            <@docBookEditor name="eml.purpose" id="purpose" value=eml.purpose! label=action.getText("eml.purpose")/>
                        </div>
                    </div>

                    <!-- Introduction -->
                    <div class="my-3 p-3" id="s-introduction" data-toc="${action.getText('manage.metadata.introduction')}">
                        <@textinline name="manage.metadata.introduction" help="i18n"/>

                        <div>
                            <p class="mb-3 mt-3 text-smaller fst-italic">
                                <@s.text name='manage.metadata.introduction.description'/>
                            </p>
                        </div>

                        <div class="mt-3">
                            <@docBookEditor name="eml.introduction" id="introduction" value=eml.introduction! label=action.getText("manage.metadata.introduction")/>
                        </div>
                    </div>

                    <!-- Getting Started -->
                    <div class="my-3 p-3" id="s-getting-started" data-toc="${action.getText('manage.metadata.gettingStarted')}">
                        <@textinline name="manage.metadata.gettingStarted" help="i18n"/>

                        <div>
                            <p class="mb-3 mt-3 text-smaller fst-italic">
                                <@s.text name='manage.metadata.gettingStarted.description'/>
                            </p>
                        </div>

                        <div class="mt-3">
                            <@docBookEditor name="eml.gettingStarted" id="gettingStarted" value=eml.gettingStarted! label=action.getText("manage.metadata.gettingStarted")/>
                        </div>
                    </div>

                    <!-- Additional information (was the Additional Metadata page, #3319) -->
                    <div class="my-3 p-3" id="s-additional-info" data-toc="${action.getText('eml.additionalInfo')}">
                        <@textinline name="eml.additionalInfo" help="i18n"/>

                        <div class="mt-2">
                            <textarea id="eml.additionalInfo" name="eml.additionalInfo" class="form-control" rows="5" aria-label="${action.getText('eml.additionalInfo')}">${eml.additionalInfo!}</textarea>
                        </div>
                    </div>

                    <!-- Keywords (were a page of their own, #3319) -->
                    <div class="my-3 p-3" id="s-keywords" data-toc="${action.getText('manage.metadata.keywords.title')}">
                        <@textinline name="manage.metadata.keywords.title"/>

                        <p class="mb-0 mt-2">
                            <@s.text name='manage.metadata.keywords.intro'/>
                        </p>

                        <div id="items">
                            <#list eml.keywords as item>
                                <div id="item-${item_index}" class="item row g-3 border-bottom pb-3 mt-1">
                                    <div class="handle d-flex justify-content-end mt-2">
                                        <a id="removeLink-${item_index}" class="removeLink metadata-action-link custom-link" href="">
                                            <span>
                                                <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                                    <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                </svg>
                                            </span>
                                            <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.keywords.item'/></span>
                                        </a>
                                    </div>
                                    <@input name="eml.keywords[${item_index}].keywordThesaurus" value=(item.keywordThesaurus)! i18nkey="eml.keywords.keywordThesaurus" help="i18n" requiredField=true />
                                    <#-- work around for a bug that converts empty keywordsList into string "null". In this case, nothing should appear in text box -->
                                    <#-- TODO: remove check for "null" after fixing problem in gbif-metadata-profile -->
                                    <#assign keywordList = item.keywordsString />
                                    <#if keywordList?has_content && keywordList?lower_case == "null">
                                        <@text value="" name="eml.keywords[${item_index}].keywordsString" i18nkey="eml.keywords.keywordsString" help="i18n" requiredField=true/>
                                    <#else>
                                        <@text name="eml.keywords[${item_index}].keywordsString" i18nkey="eml.keywords.keywordsString" help="i18n" requiredField=true minlength=2 />
                                    </#if>
                                </div>
                            </#list>
                        </div>

                        <div class="addNew col-12 mt-2">
                            <a id="plus" href="" class="metadata-action-link custom-link">
                                <span>
                                    <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                        <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                    </svg>
                                </span>
                                <span><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.keywords.item'/></span>
                            </a>
                        </div>

                        <div id="baseItem" class="item row g-3 border-bottom pb-3 mt-1" style="display:none;">
                            <div class="handle d-flex justify-content-end mt-2">
                                <a id="removeLink" class="removeLink metadata-action-link custom-link" href="">
                                    <span>
                                        <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                        </svg>
                                    </span>
                                    <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.keywords.item'/></span>
                                </a>
                            </div>
                            <@input name="keywordThesaurus" i18nkey="eml.keywords.keywordThesaurus" help="i18n" requiredField=true/>
                            <@text name="keywordsString" i18nkey="eml.keywords.keywordsString" help="i18n" requiredField=true/>
                        </div>
                    </div>

                    <div class="my-3 p-3" id="s-logo" data-toc="${action.getText('manage.metadata.section.logo')}">
                        <@textinline name="manage.metadata.section.logo"/>

                        <div class="row g-3 mt-0">
                            <div class="col-lg-6">
                                <@input name="eml.logoUrl" value=eml.logoUrl! i18nkey="eml.logoUrl" help="i18n" type="url" />
                                <@s.file cssClass="form-control my-1" name="file" id="file" accept="image/jpeg,image/png,image/gif"/>
                                <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="file"/>
                                <a href="#" class="action-link-button action-link-button-danger" id="buttonRemove">
                                    <svg class="action-link-button-icon" focusable="false" aria-hidden="true" viewBox="0 0 24 24">
                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                    </svg>
                                    <@s.text name="button.remove"/>
                                </a>
                            </div>

                            <div class="col-lg-3 d-flex justify-content-start align-items-center">
                                <div id="resourcelogo">
                                    <#if resource.eml.logoUrl?has_content>
                                        <img src="${resource.eml.logoUrl}" />
                                    </#if>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="my-3 p-3" id="s-maintenance" data-toc="${action.getText('eml.maintenance')}">
                        <@textinline name="eml.maintenance" help="i18n"/>

                        <div class="row g-3 mt-2">
                            <div class="col-lg-6">
                                <@select name="eml.updateFrequency" i18nkey="eml.updateFrequency" help="i18n" options=frequencies value="${eml.updateFrequency.identifier!'unknown'}" requiredField=true />
                            </div>
                        </div>

                        <div class="row g-3 mt-0">
                            <div class="col-12">
                                <!-- Maintenance Update Frequency -->
                                <@text name="eml.updateFrequencyDescription" value=eml.updateFrequencyDescription! i18nkey="eml.updateFrequencyDescription" help="i18n" />
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </div>

    <#include "eml_savebar.ftl"/>
</form>

<#include "/WEB-INF/pages/manage/eml/unsaved_changes_modal.ftl">

<#include "/WEB-INF/pages/inc/footer.ftl">
