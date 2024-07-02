<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.MetadataAction" -->
<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/summernote/0.8.20/summernote-bs5.min.css" rel="stylesheet">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/summernote/0.8.20/summernote-bs5.min.js"></script>
    <title><@s.text name='manage.metadata.acknowledgements.title'/></title>
    <script>
        $(document).ready(function() {
            var docBookAcknowledgements = `${eml.acknowledgements!}`;
            var htmlAcknowledgements = convertToHtml(docBookAcknowledgements);

            $('#acknowledgements-editor').summernote({
                height: 200,
                minHeight: null,
                maxHeight: null,
                focus: true,
                toolbar: [
                    ['insert', ['codeview']]
                ]
            });

            $('#acknowledgements-editor').summernote('code', htmlAcknowledgements);

            // Form submission event
            $('#acknowledgements-form').submit(function(event) {
                // Prevent the default form submission
                event.preventDefault();

                // Extract HTML content from Summernote editor
                var htmlContent = $('#acknowledgements-editor').summernote('code');

                // Convert HTML to DocBook
                var docbookContent = convertToDocBook(htmlContent);

                // Assign DocBook content to a hidden input field
                $('#acknowledgements').val(docbookContent);

                // Submit the form
                this.submit();
            });

            // Function to convert HTML to DocBook
            function convertToDocBook(html) {
                // Replace <div> with <section>
                html = html.replace(/<div>/g, '<section>').replace(/<\/div>/g, '</section>');

                // Replace <h1> with <title>
                html = html.replace(/<h1>/g, '<title>').replace(/<\/h1>/g, '</title>');

                // Replace <p> with <para>
                html = html.replace(/<p>/g, '<para>').replace(/<\/p>/g, '</para>');

                // Replace <ul> with <itemizedlist> and <li> with <listitem>
                html = html.replace(/<ul>/g, '<itemizedlist>').replace(/<\/ul>/g, '</itemizedlist>');
                html = html.replace(/<li>/g, '<listitem>').replace(/<\/li>/g, '</listitem>');

                // Replace <ol> with <orderedlist> and <li> with <listitem>
                html = html.replace(/<ol>/g, '<orderedlist>').replace(/<\/ol>/g, '</orderedlist>');
                html = html.replace(/<li>/g, '<listitem>').replace(/<\/li>/g, '</listitem>');

                // Replace <b> with <emphasis>
                html = html.replace(/<b>/g, '<emphasis>').replace(/<\/b>/g, '</emphasis>');

                // Replace <sub> with <subscript> and <sup> with <superscript>
                html = html.replace(/<sub>/g, '<subscript>').replace(/<\/sub>/g, '</subscript>');
                html = html.replace(/<sup>/g, '<superscript>').replace(/<\/sup>/g, '</superscript>');

                // Replace <pre> with <literal>
                html = html.replace(/<pre>/g, '<literal>').replace(/<\/pre>/g, '</literal>');

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

                // Replace <para> with <p>
                docBook = docBook.replace(/<para>/g, '<p>').replace(/<\/para>/g, '</p>');

                // Replace <itemizedlist> with <ul> and <listitem> with <li>
                docBook = docBook.replace(/<itemizedlist>/g, '<ul>').replace(/<\/itemizedlist>/g, '</ul>');
                docBook = docBook.replace(/<listitem>/g, '<li>').replace(/<\/listitem>/g, '</li>');

                // Replace <orderedlist> with <ol> and <listitem> with <li>
                docBook = docBook.replace(/<orderedlist>/g, '<ol>').replace(/<\/orderedlist>/g, '</ol>');
                docBook = docBook.replace(/<listitem>/g, '<li>').replace(/<\/listitem>/g, '</li>');

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
        });
    </script>
    <#assign currentMenu="manage"/>
    <#assign currentMetadataPage = "acknowledgements"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <div class="container px-0">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <form id="acknowledgements-form" class="needs-validation" action="metadata-${section}.do" method="post" novalidate>
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
                        <h1 class="py-2 text-gbif-header fs-2 fw-normal">
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
                        <div class="my-md-3 p-3">
                            <p class="mb-3">
                                <@s.text name='manage.metadata.acknowledgements.intro'/>
                            </p>

                            <textarea id="acknowledgements-editor" name="acknowledgements"></textarea>
                            <input id="acknowledgements" type="hidden" name="eml.acknowledgements">
                        </div>
                    </div>
                </main>
            </div>
        </div>
    </form>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
