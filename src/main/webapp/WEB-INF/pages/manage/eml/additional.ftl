    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <title><@s.text name='manage.metadata.additional.title'/></title>
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

    <form class="needs-validation track-unsaved" action="metadata-${section}.do?r=${resource.shortname}" method="post" novalidate>

        <#include "eml_heading.ftl"/>

        <#include "metadata_section_select.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout main-content-container">
                <main class="bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "eml_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">
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
