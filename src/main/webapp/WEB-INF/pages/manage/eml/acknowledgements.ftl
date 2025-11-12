<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.MetadataAction" -->
    <#include "/WEB-INF/pages/inc/header.ftl">
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <link rel="stylesheet" href="${baseURL}/styles/smaller-inputs.css">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/summernote/0.8.20/summernote-bs5.min.css" rel="stylesheet">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/summernote/0.8.20/summernote-bs5.min.js"></script>
    <title><@s.text name='manage.metadata.acknowledgements.title'/></title>
    <script src="${baseURL}/js/docbook/docbook-v2.js"></script>
    <script>
        $(document).ready(function() {
            var docBookAcknowledgements = `${eml.acknowledgements!}`;
            var htmlAcknowledgements = convertToHtml(docBookAcknowledgements);

            const acknowledgementsEditor = $('#acknowledgements-editor');
            acknowledgementsEditor.summernote({
                height: 200,
                minHeight: null,
                maxHeight: null,
                focus: true,
                toolbar: [
                    ['insert', ['codeview']]
                ],
                // clean up HTML and styles when copy/paste
                callbacks: {
                    onPaste: function (e) {
                        e.preventDefault();
                        const clipboardData = (e.originalEvent || e).clipboardData || window.clipboardData;
                        const text = clipboardData.getData('text/plain');
                        const cleaned = text.replace(/\r?\n/g, '<br>'); // keep newlines

                        acknowledgementsEditor.summernote('focus');
                        acknowledgementsEditor.summernote('pasteHTML', cleaned);
                    }
                }
            });

            acknowledgementsEditor.summernote('code', htmlAcknowledgements);

            // Form submission event
            $('#acknowledgements-form').submit(function(event) {
                // Prevent the default form submission
                event.preventDefault();

                // Extract HTML content from Summernote editor
                var htmlContent = $('#acknowledgements-editor').summernote('code');

                const acknowledgementsValidation = validateHTML(htmlContent);
                if (!acknowledgementsValidation.isValid) {
                    $("#html-validation-error-block").show();
                    var errorMessage =
                        '${action.getText("manage.metadata.acknowledgements.unsupportedHtmlInput1")?js_string}'
                        + " " +  acknowledgementsValidation.tag + ". "
                        + '${action.getText("manage.metadata.acknowledgements.unsupportedHtmlInput2")?js_string}';
                    $("#html-validation-error-message").text(errorMessage);
                    $('body, html').animate({scrollTop: 0});
                    return;
                }

                // Convert HTML to DocBook
                var docbookContent = convertToDocBook(htmlContent);

                // Assign DocBook content to a hidden input field
                $('#acknowledgements').val(docbookContent);

                // Submit the form
                this.submit();
            });

            makeSureResourceParameterIsPresentInURL('${resource.shortname}');
        });
    </script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "acknowledgements"/>
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

    <form id="acknowledgements-form" class="needs-validation track-unsaved" action="metadata-${section}.do" method="post" novalidate>
        <input type="hidden" name="r" value="${resource.shortname}" />

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
                            <@s.text name='manage.metadata.acknowledgements.title'/>
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
                        <button type="button" class="btn btn-sm btn-outline-secondary top-button" onclick="window.history.back();">
                            <@s.text name="button.back"/>
                        </button>
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
                        <div class="my-md-3 p-3">
                            <p class="mb-3">
                                <a tabindex="0" role="button"
                                   class="popover-link"
                                   data-bs-toggle="popover"
                                   data-bs-trigger="focus"
                                   data-bs-html="true"
                                   data-bs-content="<@s.text name='manage.metadata.acknowledgements.info' escapeHtml=true/>">
                                    <i class="bi bi-info-circle text-gbif-primary px-1"></i>
                                </a>
                                <@s.text name='manage.metadata.acknowledgements.intro'/>
                            </p>

                            <p class="mb-3 text-smaller fst-italic">
                                <@s.text name='manage.metadata.acknowledgements.description'/>
                            </p>

                            <textarea id="acknowledgements-editor" name="acknowledgements"></textarea>
                            <input id="acknowledgements" type="hidden" name="eml.acknowledgements">
                        </div>
                    </div>
                </main>
            </div>
        </div>
    </form>

    <#include "/WEB-INF/pages/manage/eml/unsaved_changes_modal.ftl">

    <#include "/WEB-INF/pages/inc/footer.ftl">
