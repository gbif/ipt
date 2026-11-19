    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <title><@s.text name='manage.metadata.additional.title'/></title>
    <script>
        $(document).ready(function () {
            // the logo is uploaded when the form is saved, show a preview of the chosen file until then
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

                    $("div#" + elementId + " input").attr("name", "eml.alternateIdentifiers[" + index + "]");
                });

                hideProcessing();
            }

            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>
    <#assign currentMetadataPage = "additional"/>
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/macros/icon.ftl"/>
    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/eml.css">
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form class="needs-validation track-unsaved" action="metadata-${section}.do?r=${resource.shortname}" method="post" enctype="multipart/form-data" novalidate>
        <input id="removeLogo" name="removeLogo" type="hidden" value="false" />

        <#include "eml_heading.ftl"/>

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
                                <@s.text name='manage.metadata.additional.intro'/>
                            </p>

                            <!-- Resource Logo -->
                            <div id="logofields" class="row g-3 pb-1 mt-1" data-toc="${action.getText('manage.metadata.section.datesLogo')}">
                                <div class="col-lg-6">
                                    <@input name="dateStamp" value=(eml.dateStamp??)?then(eml.dateStamp?date?string("yyyy-MM-dd"), "") i18nkey="eml.dateStamp" help="i18n" disabled=true />
                                </div>

                                <div class="col-lg-6">
                                    <@input name="eml.pubDate" value=(eml.pubDate??)?then(eml.pubDate?date?string("yyyy-MM-dd"), "") i18nkey="eml.pubDate" help="i18n" disabled=true />
                                </div>

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

                            <div class="row g-3 mt-1" id="s-additional-info" data-toc="${action.getText('eml.additionalInfo')}">
                                <!-- Additional info -->
                                <div>
                                    <@text name="eml.additionalInfo" i18nkey="eml.additionalInfo" help="i18n"/>
                                </div>
                            </div>

                        </div>

                        <div class="my-md-3 p-3" id="s-alternative-identifiers" data-toc="${action.getText('manage.metadata.alternateIdentifiers.title')}">
                            <!-- Alternative identifiers -->
                            <div class="listBlock">
                                <@textinline name="manage.metadata.alternateIdentifiers.title" help="i18n"/>
                                <div id="items">
                                    <#list eml.alternateIdentifiers as item>
                                        <div id="item-${item_index}" class="item row g-3 border-bottom pb-3 mt-1">
                                            <div class="handle mt-2 d-flex justify-content-end">
                                                <a id="removeLink-${item_index}" class="removeLink metadata-action-link custom-link" href="">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                        </svg>
                                                    </span>
                                                    <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.alternateIdentifiers.item'/></span>
                                                </a>
                                            </div>
                                            <@input name="eml.alternateIdentifiers[${item_index}]" value=item i18nkey="eml.alternateIdentifier" help="i18n"/>
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
                                        <span><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.alternateIdentifiers.item'/></span>
                                    </a>
                                </div>
                            </div>

                            <div id="baseItem" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display:none;">
                                <div class="handle mt-2 d-flex justify-content-end">
                                    <a id="removeLink" class="removeLink metadata-action-link custom-link" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                            </svg>
                                        </span>
                                        <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.alternateIdentifiers.item'/></span>
                                    </a>
                                </div>
                                <@input name="alternateIdentifiers" i18nkey="eml.alternateIdentifier" help="i18n"/>
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
