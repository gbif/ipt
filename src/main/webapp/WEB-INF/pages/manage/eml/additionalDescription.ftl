<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.MetadataAction" -->
<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <title><@s.text name='manage.metadata.additionalDescription.title'/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <script src="${baseURL}/js/docbook/docbook.js"></script>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/summernote/0.8.20/summernote-bs5.min.css" rel="stylesheet">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/summernote/0.8.20/summernote-bs5.min.js"></script>
    <script>
        $(document).ready(function(){
            // scroll to the error if present
            var invalidElements = $(".is-invalid");

            if (invalidElements !== undefined && invalidElements.length > 0) {
                var invalidElement = invalidElements.first();
                var pos = invalidElement.offset().top - 100;
                // scroll to the element
                $('body, html').animate({scrollTop: pos});
            }

            var docBookPurpose = `${eml.purpose!}`;
            var htmlPurpose = convertToHtml(docBookPurpose);

            var docBookGettingStarted = `${eml.gettingStarted!}`;
            var htmlGettingStarted = convertToHtml(docBookGettingStarted);

            var docBookIntroduction = `${eml.introduction!}`;
            var htmlIntroduction = convertToHtml(docBookIntroduction);

            $('#purpose-editor').summernote({
                height: 200,
                minHeight: null,
                maxHeight: null,
                focus: false,
                toolbar: [
                    ['insert', ['codeview']]
                ]
            });

            $('#purpose-editor').summernote('code', htmlPurpose);

            $('#gettingStarted-editor').summernote({
                height: 200,
                minHeight: null,
                maxHeight: null,
                focus: false,
                toolbar: [
                    ['insert', ['codeview']]
                ]
            });

            $('#gettingStarted-editor').summernote('code', htmlGettingStarted);

            $('#introduction-editor').summernote({
                height: 200,
                minHeight: null,
                maxHeight: null,
                focus: false,
                toolbar: [
                    ['insert', ['codeview']]
                ]
            });

            $('#introduction-editor').summernote('code', htmlIntroduction);

            // Form submission events
            $('#additional-description-form').submit(function(event) {
                // Prevent the default form submission
                event.preventDefault();

                // Extract HTML content from Summernote editor
                var htmlContentPurpose = $('#purpose-editor').summernote('code');
                var htmlContentGettingStarted = $('#gettingStarted-editor').summernote('code');
                var htmlContentIntroduction = $('#introduction-editor').summernote('code');

                const purposeValidation = validateHTML(htmlContentPurpose);
                if (!purposeValidation.isValid) {
                    $("#html-validation-error-block").show();
                    var errorMessagePurpose =
                        '${action.getText("eml.purpose.unsupportedHtmlInput1")?js_string}'
                        + " " + purposeValidation.tag + ". "
                        + '${action.getText("eml.purpose.unsupportedHtmlInput2")?js_string}';
                    $("#html-validation-error-message").text(errorMessagePurpose);
                    $('body, html').animate({scrollTop: 0});
                    return;
                }

                const introductionValidation = validateHTML(htmlContentIntroduction);
                if (!introductionValidation.isValid) {
                    $("#html-validation-error-block").show();
                    var errorMessageIntroduction =
                        '${action.getText("manage.metadata.introduction.unsupportedHtmlInput1")?js_string}'
                        + " " + introductionValidation.tag + ". "
                        + '${action.getText("manage.metadata.introduction.unsupportedHtmlInput2")?js_string}';
                    $("#html-validation-error-message").text(errorMessageIntroduction);
                    $('body, html').animate({scrollTop: 0});
                    return;
                }

                const gettingStartedValidation = validateHTML(htmlContentGettingStarted);
                if (!gettingStartedValidation.isValid) {
                    $("#html-validation-error-block").show();
                    var errorMessageGettingStarted =
                        '${action.getText("manage.metadata.gettingStarted.unsupportedHtmlInput1")?js_string}'
                        + " " + gettingStartedValidation.tag + ". "
                        + '${action.getText("manage.metadata.gettingStarted.unsupportedHtmlInput2")?js_string}';
                    $("#html-validation-error-message").text(errorMessageGettingStarted);
                    $('body, html').animate({scrollTop: 0});
                    return;
                }

                // Convert HTML to DocBook
                var docbookContentPurpose = convertToDocBook(htmlContentPurpose);
                var docbookContentGettingStarted = convertToDocBook(htmlContentGettingStarted);
                var docbookContentIntroduction = convertToDocBook(htmlContentIntroduction);

                // Assign DocBook content to a hidden input field
                $('#purpose').val(docbookContentPurpose);
                $('#gettingStarted').val(docbookContentGettingStarted);
                $('#introduction').val(docbookContentIntroduction);

                // Submit the form
                this.submit();
            });

            makeSureResourceParameterIsPresentInURL();

            function makeSureResourceParameterIsPresentInURL() {
                const currentUrl = window.location.href;
                const url = new URL(currentUrl);

                const searchParams = url.searchParams;

                if (!searchParams.has('r')) {
                    searchParams.set('r', '${resource.shortname}');
                    window.history.replaceState({}, '', url.toString());
                }
            }
        });
    </script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "additionalDescription"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">

        <div id="html-validation-error-block" class="alert alert-danger alert-dismissible fade show d-flex metadata-error-alert" role="alert" style="display: none !important;">
            <div class="me-3">
                <i class="bi bi-exclamation-circle alert-red-2 fs-bigger-2 me-2"></i>
            </div>
            <div class="overflow-x-hidden pt-1">
                <span id="html-validation-error-message"></span>
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </div>

    <form id="additional-description-form" class="needs-validation" action="metadata-${section}.do" method="post" novalidate>
        <div class="container-fluid bg-body border-bottom">
            <div class="container bg-body border rounded-2 mb-4">
                <div class="container my-3 p-3">
                    <div class="text-center fs-smaller">
                        <div class="text-center fs-smaller">
                            <nav style="--bs-breadcrumb-divider: url(&#34;data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='8' height='8'%3E%3Cpath d='M2.5 0L1 1.5 3.5 4 1 6.5 2.5 8l4-4-4-4z' fill='currentColor'/%3E%3C/svg%3E&#34;);" aria-label="breadcrumb">
                                <ol class="breadcrumb justify-content-center mb-0">
                                    <li class="breadcrumb-item"><a href="${baseURL}/manage/"><@s.text name="breadcrumb.manage"/></a></li>
                                    <li class="breadcrumb-item"><a href="resource?r=${resource.shortname}"><@s.text name="breadcrumb.manage.overview"/></a></li>
                                    <li class="breadcrumb-item active" aria-current="page"><@s.text name="breadcrumb.manage.overview.metadata"/></li>
                                </ol>
                            </nav>
                        </div>
                    </div>

                    <div class="text-center">
                        <h1 class="py-2 mb-0 text-gbif-header fs-2 fw-normal">
                            <@s.text name='manage.metadata.additionalDescription.title'/>
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

        <#include "metadata_section_select.ftl"/>

        <div class="container-fluid bg-body">
            <div class="container bd-layout main-content-container">
                <main class="bd-main">
                    <div class="bd-toc mt-4 mb-5 ps-3 mb-lg-5 text-muted">
                        <#include "eml_sidebar.ftl"/>
                    </div>

                    <div class="bd-content">
                        <!-- Purpose -->
                        <div class="my-md-3 p-3">
                            <@textinline name="eml.purpose" help="i18n"/>

                            <div>
                                <p class="mb-3 mt-3 text-smaller fst-italic">
                                    <@s.text name='eml.purpose.description'/>
                                </p>
                            </div>

                            <div class="mt-3">
                                <textarea id="purpose-editor" name="purpose"></textarea>
                                <input id="purpose" type="hidden" name="eml.purpose">
                            </div>
                        </div>

                        <!-- Introduction -->
                        <div class="my-md-3 p-3">
                            <@textinline name="manage.metadata.introduction" help="i18n"/>

                            <div>
                                <p class="mb-3 mt-3 text-smaller fst-italic">
                                    <@s.text name='manage.metadata.introduction.description'/>
                                </p>
                            </div>

                            <div class="mt-3">
                                <textarea id="introduction-editor" name="introduction"></textarea>
                                <input id="introduction" type="hidden" name="eml.introduction">
                            </div>
                        </div>

                        <!-- Getting Started -->
                        <div class="my-md-3 p-3">
                            <@textinline name="manage.metadata.gettingStarted" help="i18n"/>

                            <div>
                                <p class="mb-3 mt-3 text-smaller fst-italic">
                                    <@s.text name='manage.metadata.gettingStarted.description'/>
                                </p>
                            </div>

                            <div class="mt-3">
                                <textarea id="gettingStarted-editor" name="gettingStarted"></textarea>
                                <input id="gettingStarted" type="hidden" name="eml.gettingStarted">
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>
    </form>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
