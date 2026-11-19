    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <title><@s.text name='manage.metadata.physical.title'/></title>
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

            const sortable_physical_data = initAndGetSortable('#items');

            sortable_physical_data[0].addEventListener('sortupdate', changeInputNamesAfterDragging);
            sortable_physical_data[0].addEventListener('drag', dragScroll);

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

                    $("div#" + elementId + " input[id$='name']").attr("name", "eml.physicalData[" + index + "].name");
                    $("div#" + elementId + " input[id$='charset']").attr("name", "eml.physicalData[" + index + "].charset");
                    $("div#" + elementId + " input[id$='distributionUrl']").attr("name", "eml.physicalData[" + index + "].distributionUrl");
                    $("div#" + elementId + " input[id$='format']").attr("name", "eml.physicalData[" + index + "].format");
                    $("div#" + elementId + " input[id$='formatVersion']").attr("name", "eml.physicalData[" + index + "].formatVersion");
                });

                hideProcessing();
            }

            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>

    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <#assign currentMetadataPage = "physical"/>
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/macros/icon.ftl"/>
    <link rel="stylesheet" href="${baseURL}/styles/overview.css">
    <link rel="stylesheet" href="${baseURL}/styles/eml.css">
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form class="needs-validation track-unsaved" action="metadata-${section}.do" method="post" novalidate>
        <#include "eml_heading.ftl"/>

        <#include "metadata_section_select.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout main-content-container">
                <main class="bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "eml_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">
                        <div class="my-md-3 p-3" id="s-homepage" data-toc="${action.getText('eml.distributionUrl')}">
                            <p class="mb-0">
                                <@s.text name='manage.metadata.physical.intro'/>
                            </p>

                            <div class="mt-2">
                                <@input name="eml.distributionUrl" value=eml.distributionUrl! i18nkey="eml.distributionUrl" type="url" />
                            </div>
                        </div>

                        <div class="my-md-3 p-3" id="s-other-formats" data-toc="${action.getText('eml.physicalData.other')}">

                            <div class="listBlock">
                                <@textinline name="eml.physicalData.other" help="i18n"/>

                                <div id="items">
                                    <#list eml.physicalData as item>
                                        <div id="item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="handle mt-2 d-flex justify-content-end">
                                                <a id="removeLink-${item_index}" class="removeLink metadata-action-link custom-link" href="">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                                        </svg>
                                                    </span>
                                                    <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.physical.item'/></span>
                                                </a>
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="eml.physicalData[${item_index}].name" value=item.name! i18nkey="eml.physicalData.name" requiredField=true />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="eml.physicalData[${item_index}].charset" value=item.charset! i18nkey="eml.physicalData.charset" help="i18n" requiredField=true />
                                            </div>
                                            <div class="fullcolumn">
                                                <@input name="eml.physicalData[${item_index}].distributionUrl" value=item.distributionUrl! i18nkey="eml.physicalData.distributionUrl" help="i18n" requiredField=true type="url" />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="eml.physicalData[${item_index}].format" value=item.format! i18nkey="eml.physicalData.format" help="i18n" requiredField=true />
                                            </div>
                                            <div class="col-lg-6">
                                                <@input name="eml.physicalData[${item_index}].formatVersion" value=item.formatVersion! i18nkey="eml.physicalData.formatVersion" help="i18n"/>
                                            </div>
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
                                        <span><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.physical.item'/></span>
                                    </a>
                                </div>
                            </div>

                            <!-- internal parameter -->
                            <input name="r" type="hidden" value="${resource.shortname}" />

                            <div id="baseItem" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display:none;">
                                <div class="handle mt-2 d-flex justify-content-end">
                                    <a id="removeLink" class="removeLink metadata-action-link custom-link" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-danger">
                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                                            </svg>
                                        </span>
                                        <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.physical.item'/></span>
                                    </a>
                                </div>
                                <div class="col-lg-6">
                                    <@input name="name" i18nkey="eml.physicalData.name" requiredField=true />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="charset" i18nkey="eml.physicalData.charset" help="i18n" requiredField=true />
                                </div>
                                <@input name="distributionUrl" i18nkey="eml.physicalData.distributionUrl" help="i18n" requiredField=true />
                                <div class="col-lg-6">
                                    <@input name="format" i18nkey="eml.physicalData.format" help="i18n" requiredField=true />
                                </div>
                                <div class="col-lg-6">
                                    <@input name="formatVersion" i18nkey="eml.physicalData.formatVersion" help="i18n"/>
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
