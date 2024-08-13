<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.MetadataAction" -->
<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <title><@s.text name='manage.metadata.additionalDescription.title'/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
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

            // Function to convert HTML to DocBook
            function convertToDocBook(html) {
                // Replace <div> with <section>
                html = html.replace(/<div>/g, '<section>').replace(/<\/div>/g, '</section>');

                // Replace <h> with <title>
                html = html
                    .replace(/<h1>/g, '<title>').replace(/<\/h1>/g, '</title>')
                    .replace(/<h2>/g, '<title>').replace(/<\/h2>/g, '</title>')
                    .replace(/<h3>/g, '<title>').replace(/<\/h3>/g, '</title>')
                    .replace(/<h4>/g, '<title>').replace(/<\/h4>/g, '</title>')
                    .replace(/<h5>/g, '<title>').replace(/<\/h5>/g, '</title>');

                // Replace <ul> with <itemizedlist>
                // Replace <ol> with <orderedlist>
                // Replace <li> with <listitem>
                // Also, wrap into <para> where needed
                html = html
                    .replace(/<ul>/g, '<para><itemizedlist>').replace(/<\/ul>/g, '</itemizedlist></para>')
                    .replace(/<ol>/g, '<para><orderedlist>').replace(/<\/ol>/g, '</orderedlist></para>')
                    .replace(/<li>/g, '<listitem><para>').replace(/<\/li>/g, '</para></listitem>');

                // Replace <p> with <para>
                html = html.replace(/<p>/g, '<para>').replace(/<\/p>/g, '</para>');

                // Replace <b> with <emphasis>
                html = html.replace(/<b>/g, '<emphasis>').replace(/<\/b>/g, '</emphasis>');

                // Replace <sub> with <subscript> and <sup> with <superscript>
                html = html.replace(/<sub>/g, '<subscript>').replace(/<\/sub>/g, '</subscript>');
                html = html.replace(/<sup>/g, '<superscript>').replace(/<\/sup>/g, '</superscript>');

                // Replace <pre> with <literal>
                html = html.replace(/<pre>/g, '<literal>').replace(/<\/pre>/g, '</literal>');

                // Remove <br>
                html = html.replace(/<br>/g, '').replace(/<\/br>/g, '');

                // Replace <a href="...">...</a> with <ulink url="..."><citetitle>...</citetitle></ulink>
                html = html.replace(/<a href="([^"]+)">([^<]+)<\/a>/g, '<ulink url="$1"><citetitle>$2</citetitle></ulink>');

                return html;
            }

            function convertToHtml(docBook) {
                // Decode HTML entities
                docBook = $('<textarea />').html(docBook).text();

                // Replace <section> with <div>
                docBook = docBook.replace(/<section>/g, '<div>').replace(/<\/section>/g, '</div>');

                // Replace <title> with <h1>
                docBook = docBook.replace(/<title>/g, '<h1>').replace(/<\/title>/g, '</h1>');

                // Replace <itemizedlist> with <ul>
                // Replace <orderedlist> with <ol>
                // Replace <listitem> with <li>
                // Also, unwrap <para> where needed
                docBook = docBook
                    .replace(/<para><itemizedlist>/g, '<ul>').replace(/<\/itemizedlist><\/para>/g, '</ul>')
                    .replace(/<para><orderedlist>/g, '<ol>').replace(/<\/orderedlist><\/para>/g, '</ol>')
                    .replace(/<listitem><para>/g, '<li>').replace(/<\/para><\/listitem>/g, '</li>');

                // Replace <para> with <p>
                docBook = docBook.replace(/<para>/g, '<p>').replace(/<\/para>/g, '</p>');

                // Replace <emphasis> with <b>
                docBook = docBook.replace(/<emphasis>/g, '<b>').replace(/<\/emphasis>/g, '</b>');

                // Replace <subscript> with <sub> and <superscript> with <sup>
                docBook = docBook.replace(/<subscript>/g, '<sub>').replace(/<\/subscript>/g, '</sub>');
                docBook = docBook.replace(/<superscript>/g, '<sup>').replace(/<\/superscript>/g, '</sup>');

                // Replace <literal> with <pre>
                docBook = docBook.replace(/<literal>/g, '<pre>').replace(/<\/literal>/g, '</pre>');

                // Replace <ulink url="..."><citetitle>...</citetitle></ulink> with <a href="...">...</a>
                docBook = docBook.replace(/<ulink url="([^"]+)"><citetitle>([^<]+)<\/citetitle><\/ulink>/g, '<a href="$1">$2</a>');

                return docBook;
            }

            function validateHTML(html) {
                // Define allowed tags
                const allowedTags = [
                    'h1', 'h2', 'h3', 'h4', 'h5',
                    'ul', 'ol', 'li',
                    'p', 'b', 'sub', 'sup', 'pre', 'a'
                ];

                // Match all HTML tags in the string
                const regex = /<\/?([a-z][a-z0-9]*)\b[^>]*>/gi;
                let match;

                // Loop through all found tags
                while ((match = regex.exec(html)) !== null) {
                    // Extract the tag name from the match
                    const tagName = match[1].toLowerCase();

                    // Check if the tag is not in the allowed list
                    if (!allowedTags.includes(tagName)) {
                        return { isValid: false, tag: match[0] }; // Forbidden tag found
                    }
                }

                return { isValid: true }; // No forbidden tags found
            }


            // Form submission events
            $('#additional-description-form').submit(function(event) {
                // Prevent the default form submission
                event.preventDefault();

                // Extract HTML content from Summernote editor
                var htmlContentPurpose = $('#purpose-editor').summernote('code');
                var htmlContentGettingStarted = $('#gettingStarted-editor').summernote('code');
                var htmlContentIntroduction = $('#introduction-editor').summernote('code');

                // const introductionValidation = validateHTML(htmlContentIntroduction);
                // if (!introductionValidation.isValid) {
                //     $("#html-validation-error-block").show();
                //     $("#html-validation-error-message").text("Invalid introduction. Unsupported tag: " + descriptionValidation.tag);
                //     return;
                // }
                //
                // const gettingStartedValidation = validateHTML(htmlContentGettingStarted);
                // if (!gettingStartedValidation.isValid) {
                //     $("#html-validation-error-block").show();
                //     $("#html-validation-error-message").text("Invalid getting started. Unsupported tag: " + descriptionValidation.tag);
                //     return;
                // }

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

                            <div class="mt-3">
                                <textarea id="purpose-editor" name="purpose"></textarea>
                                <input id="purpose" type="hidden" name="eml.purpose">
                            </div>
                        </div>

                        <!-- Introduction -->
                        <div class="my-md-3 p-3">
                            <@textinline name="manage.metadata.introduction" help="i18n"/>

                            <div>
                                <p class="mb-3 mt-3">
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
                                <p class="mb-3 mt-3">
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
