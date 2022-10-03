<#escape x as x?html>
    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.metadata.keywords.title'/></title>
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
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <#assign currentMetadataPage = "keywords"/>
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
                            <li class="breadcrumb-item"><a href="/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                            <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                            <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.metadata"/></li>
                        </ol>
                    </nav>
                </div>

                <div class="text-center">
                    <h1 class="py-2 mb-0 text-gbif-header fs-2 fw-normal">
                        <@s.text name='manage.metadata.keywords.title'/>
                    </h1>
                </div>

                <div class="text-center fs-smaller">
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </div>

                <div class="text-center mt-2">
                    <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save" />
                    <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" name="cancel" key="button.back" />
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
                                <@s.text name='manage.metadata.keywords.intro'/>
                            </p>

                            <div class="callout callout-info text-smaller">
                                <@s.text name="manage.metadata.keywords.warning"/>
                            </div>

                            <div id="items">
                                <#list eml.keywords as item>
                                    <div id="item-${item_index}" class="item row g-3 border-bottom pb-3 mt-1">
                                        <div class="d-flex justify-content-end mt-2">
                                            <a id="removeLink-${item_index}" class="removeLink text-smaller" href="">
                                                <span>
                                                    <svg viewBox="0 0 24 24" class="link-icon">
                                                        <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                    </svg>
                                                </span>
                                                <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.keywords.item'/></span>
                                            </a>
                                        </div>
                                        <@input name="eml.keywords[${item_index}].keywordThesaurus" i18nkey="eml.keywords.keywordThesaurus" help="i18n" requiredField=true />
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
                                <a id="plus" href="" class="text-smaller">
                                    <span>
                                        <svg viewBox="0 0 24 24" class="link-icon">
                                            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"></path>
                                        </svg>
                                    </span>
                                    <span><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.keywords.item'/></span>
                                </a>
                            </div>

                            <!-- internal parameter -->
                            <input name="r" type="hidden" value="${resource.shortname}" />


                            <div id="baseItem" class="item row g-3 border-bottom pb-3 mt-1" style="display:none;">
                                <div class="d-flex justify-content-end mt-2">
                                    <a id="removeLink" class="removeLink text-smaller" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                            </svg>
                                        </span>
                                        <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.keywords.item'/></span>
                                    </a>
                                </div>
                                <@input name="keywordThesaurus" i18nkey="eml.keywords.keywordThesaurus" help="i18n" requiredField=true/>
                                <@text name="keywordsString" i18nkey="eml.keywords.keywordsString" help="i18n" requiredField=true/>
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>
    </form>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
