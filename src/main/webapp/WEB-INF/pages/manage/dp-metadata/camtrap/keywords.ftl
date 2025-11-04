<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.DataPackageMetadataAction" -->
    <#include "/WEB-INF/pages/inc/header.ftl">
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <title><@s.text name='manage.datapackagemetadata.camtrap.keywords.title'/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <script>
        $(document).ready(function(){
            var keywordItems = calcNumberOfItems("keyword");

            function calcNumberOfItems(name) {
                var lastItem = $("#" + name + "-items .item:last-child").attr("id");
                if (lastItem !== undefined)
                    return parseInt(lastItem.split("-")[2]);
                else
                    return -1;
            }

            $("#plus-keyword").click(function (event) {
                event.preventDefault();
                addNewKeywordItem(true);
            });

            function addNewKeywordItem(effects) {
                var newItem = $('#baseItem-keyword').clone();
                if (effects) newItem.hide();
                newItem.appendTo('#keyword-items');

                if (effects) {
                    newItem.slideDown('slow');
                }

                setKeywordItemIndex(newItem, ++keywordItems);
            }

            function removeKeywordItem(event) {
                event.preventDefault();
                var $target = $(event.target);
                if (!$target.is('a')) {
                    $target = $(event.target).closest('a');
                }
                $('#keyword-item-' + $target.attr("id").split("-")[2]).slideUp('slow', function () {
                    $(this).remove();
                    $("#keyword-items .item").each(function (index) {
                        setKeywordItemIndex($(this), index);
                    });
                    calcNumberOfItems("keyword");
                });
            }

            function setKeywordItemIndex(item, index) {
                item.attr("id", "keyword-item-" + index);

                $("#keyword-item-" + index + " [id^='keyword-removeLink']").attr("id", "keyword-removeLink-" + index);
                $("#keyword-removeLink-" + index).click(function (event) {
                    removeKeywordItem(event);
                });

                $("#keyword-item-" + index + " [id^='metadata.keyword']").attr("id", "metadata.keywords[" + index + "]").attr("name", function () {
                    return $(this).attr("id");
                });
            }

            $(".removeKeywordLink").click(function (event) {
                removeKeywordItem(event);
            });

            // scroll to the error if present
            var invalidElements = $(".is-invalid");

            if (invalidElements !== undefined && invalidElements.length > 0) {
                var invalidElement = invalidElements.first();
                var pos = invalidElement.offset().top - 100;
                // scroll to the element
                $('body, html').animate({scrollTop: pos});
            }

            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "keywords"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form class="needs-validation track-unsaved" action="camtrap-metadata-${section}.do" method="post" novalidate>
        <input type="hidden" name="r" value="${resource.shortname}" />

        <div class="container-fluid bg-body border-bottom">
            <div class="container bg-body border rounded-2 mb-4">
                <div class="container my-3 p-3">
                    <div class="text-center fs-smaller">
                        <div class="text-center fs-smaller">
                            <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                                <ol class="breadcrumb justify-content-center mb-0">
                                    <li class="breadcrumb-item"><a href="/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                                    <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                                    <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.metadata"/></li>
                                </ol>
                            </nav>
                        </div>
                    </div>

                    <div class="text-center">
                        <h1 class="py-2 mb-0 text-gbif-header fs-2 fw-normal">
                            <@s.text name='manage.datapackagemetadata.camtrap.keywords.title'/>
                        </h1>
                    </div>

                    <div class="text-center fs-smaller">
                        <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">
                            <#if resource.title?has_content>
                                ${resource.title}
                            <#else>
                                ${resource.shortname}
                            </#if>
                        </a>
                    </div>

                    <div class="text-center mt-2">
                        <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/>
                        <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" name="cancel" key="button.back"/>
                    </div>
                </div>
            </div>
        </div>

        <#--        <#include "metadata_section_select.ftl"/>-->

        <div class="container-fluid bg-body">
            <div class="container bd-layout main-content-container">
                <main class="bd-main bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "metadata_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">

                        <div class="my-md-3 p-3">
                            <p class="mb-4"><@s.text name="datapackagemetadata.keywords.intro"/></p>

                            <#assign removeKeywordLink><@s.text name='manage.metadata.removethis'/> <@s.text name='datapackagemetadata.keyword'/></#assign>
                            <#assign removeKeywordLink = removeKeywordLink?markup_string>
                            <#assign addKeywordLink><@s.text name='manage.metadata.addnew'/> <@s.text name='datapackagemetadata.keyword'/></#assign>
                            <#assign addKeywordLink = addKeywordLink?markup_string>

                            <!-- List of Keywords -->
                            <div>
                                <div id="keyword-items">
                                    <#list metadata.keywords as item>
                                        <div id="keyword-item-${item_index}" class="item clearfix row g-3 border-bottom pb-3 mt-1">
                                            <div class="d-flex">
                                                <div class="flex-grow-1">
                                                    <@input name="metadata.keywords[${item_index}]" i18nkey="datapackagemetadata.keyword" withLabel=false />
                                                </div>
                                                <div class="columnLinks mt-2 d-flex justify-content-end">
                                                    <a id="keyword-removeLink-${item_index}" href="" class="removeKeywordLink metadata-action-link custom-link">
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
                                <div class="addNew col-12 mt-2">
                                    <a id="plus-keyword" class="metadata-action-link custom-link" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon link-icon-primary">
                                                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                            </svg>
                                        </span>
                                        <span>${addKeywordLink?lower_case?cap_first}</span>
                                    </a>
                                </div>
                            </div>
                        </div>

                    </div>
                </main>
            </div>
        </div>
    </form>

    <div id="baseItem-keyword" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display: none;">
        <div class="d-flex">
            <div class="flex-grow-1">
                <@input name="metadata.keyword" i18nkey="datapackagemetadata.keyword" withLabel=false />
            </div>
            <div class="columnLinks mt-2 d-flex justify-content-end">
                <a id="keyword-removeLink" href="" class="removeKeywordLink metadata-action-link custom-link">
                    <span>
                        <svg viewBox="0 0 24 24" class="link-icon link-icon-neutral">
                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"></path>
                        </svg>
                    </span>
                </a>
            </div>
        </div>
    </div>

    <#include "/WEB-INF/pages/manage/eml/unsaved_changes_modal.ftl">

    <#include "/WEB-INF/pages/inc/footer.ftl">
