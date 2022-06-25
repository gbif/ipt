<#escape x as x?html>
    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <script src="${baseURL}/js/ajaxfileupload.js"></script>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <title><@s.text name='manage.metadata.additional.title'/></title>
    <script>
        $(document).ready(function () {
            $("#buttonUpload").click(function (event) {
                event.preventDefault()
                return ajaxFileUpload();
            });

            $("#buttonRemove").click(function () {
                $("#resourcelogo").remove();
                $("#eml\\.logoUrl").val('');
                $("#file").val('');
            });

            function ajaxFileUpload() {
                var logourl = $("#resourcelogo img").attr("src");
                $.ajaxFileUpload
                (
                    {
                        url: 'uploadlogo.do',
                        secureuri: false,
                        fileElementId: 'file',
                        dataType: 'json',
                        done: function (data, status) {
                            if (typeof (data.error) != 'undefined') {
                                if (data.error !== '') {
                                    alert(data.error);
                                } else {
                                    alert(data.msg);
                                }
                            }
                        },
                        fail: function (data, status, e) {
                            alert(e);
                        }
                    }
                )
                if (logourl === undefined) {
                    var baseimg = $('#baseimg').clone();
                    baseimg.appendTo('#resourcelogo');
                    logourl = $("#resourcelogo img").attr("src");
                    $("#resourcelogo img").hide('slow').removeAttr("src");
                    $("#resourcelogo img").show('slow', function () {
                        $("[id$='eml.logoUrl']").attr("value", logourl);
                        $("#resourcelogo img").attr("src", logourl + "&t=" + (new Date()).getTime());
                    });
                } else {
                    $("#resourcelogo img").hide('slow').removeAttr("src");
                    logourl = $("#baseimg").attr("src");
                    $("#resourcelogo img").show('slow', function () {
                        $("#resourcelogo img").attr("src", logourl + "&t=" + (new Date()).getTime());
                        $("#logofields input[name$='logoUrl']").val(logourl);
                    });
                }
                return false;
            }

            $('#metadata-section').change(function () {
                var metadataSection = $('#metadata-section').find(':selected').val()
                $(location).attr('href', 'metadata-' + metadataSection + '.do?r=${resource.shortname!r!}');
            });

        });
    </script>
    <#assign currentMetadataPage = "additional"/>
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
                    <@s.text name="manage.overview.metadata"/>
                </div>

                <div class="text-center">
                    <h1 class="py-2 mb-0 text-gbif-header fs-2 fw-normal">
                        <@s.text name='manage.metadata.additional.title'/>
                    </h1>
                </div>

                <div class="text-center fs-smaller">
                    <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
                </div>

                <div class="text-center mt-2">
                    <@s.submit cssClass="button btn btn-sm btn-outline-gbif-primary top-button" name="save" key="button.save"/>
                    <@s.submit cssClass="button btn btn-sm btn-outline-secondary top-button" name="cancel" key="button.back"/>
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
                                <@s.text name='manage.metadata.additional.intro'/>
                            </p>

                            <!-- Resource Logo -->
                            <div id="logofields" class="row g-3 pb-1 mt-1">
                                <div class="col-lg-6">
                                    <#if eml.dateStamp??>
                                        <@input name="dateStamp" value='${eml.dateStamp?date?string("yyyy-MM-dd")}' i18nkey="eml.dateStamp" help="i18n" disabled=true />
                                    <#else>
                                        <@input name="dateStamp" value="" i18nkey="eml.dateStamp" help="i18n" disabled=true />
                                    </#if>
                                </div>

                                <div class="col-lg-6">
                                    <#if eml.pubDate??>
                                        <@input name="eml.pubDate" value='${eml.pubDate?date?string("yyyy-MM-dd")}' i18nkey="eml.pubDate" help="i18n" disabled=true />
                                    <#else>
                                        <@input name="eml.pubDate" value="" i18nkey="eml.pubDate" help="i18n" disabled=true />
                                    </#if>
                                </div>

                                <div class="col-lg-6">
                                    <@input name="eml.logoUrl" i18nkey="eml.logoUrl" help="i18n" type="url" />
                                    <@s.file cssClass="form-control my-1" name="file"/>
                                    <a href="#" class="button btn btn-outline-gbif-primary" id="buttonUpload">
                                        <@s.text name="button.upload"/>
                                    </a>
                                    <a href="#" class="button btn btn-outline-gbif-danger" id="buttonRemove">
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

                            <div class="row g-3 mt-1">
                                <!-- Purpose -->
                                <div>
                                    <@text name="eml.purpose" i18nkey="eml.purpose" help="i18n"/>
                                </div>

                                <!-- Maintenance Update Frequency -->
                                <div>
                                    <@text name="eml.updateFrequencyDescription" i18nkey="eml.updateFrequencyDescription" help="i18n" />
                                </div>

                                <!-- Additional info -->
                                <div>
                                    <@text name="eml.additionalInfo" i18nkey="eml.additionalInfo" help="i18n"/>
                                </div>
                            </div>

                        </div>

                        <div class="my-md-3 p-3">
                            <!-- Alternative identifiers -->
                            <div class="listBlock">
                                <@textinline name="manage.metadata.alternateIdentifiers.title" help="i18n"/>
                                <div id="items">
                                    <#list eml.alternateIdentifiers as item>
                                        <div id="item-${item_index}" class="item row g-3 border-bottom pb-3 mt-1">
                                            <div class="mt-2 d-flex justify-content-end">
                                                <a id="removeLink-${item_index}" class="removeLink text-smaller" href="">
                                                    <span>
                                                        <svg viewBox="0 0 24 24" class="link-icon">
                                                            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                                        </svg>
                                                    </span>
                                                    <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.alternateIdentifiers.item'/></span>
                                                </a>
                                            </div>
                                            <@input name="eml.alternateIdentifiers[${item_index}]" i18nkey="eml.alternateIdentifier" help="i18n"/>
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
                                        <span><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.alternateIdentifiers.item'/></span>
                                    </a>
                                </div>
                            </div>

                            <!-- internal parameters needed by ajaxFileUpload.js - do not remove -->
                            <input id="r" name="r" type="hidden" value="${resource.shortname}" />
                            <input id="validate" name="validate" type="hidden" value="false" />

                            <div id="baseItem" class="item clearfix row g-3 border-bottom pb-3 mt-1" style="display:none;">
                                <div class="mt-2 d-flex justify-content-end">
                                    <a id="removeLink" class="removeLink text-smaller" href="">
                                        <span>
                                            <svg viewBox="0 0 24 24" class="link-icon">
                                                <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM8 9h8v10H8V9zm7.5-5-1-1h-5l-1 1H5v2h14V4h-3.5z"></path>
                                            </svg>
                                        </span>
                                        <span><@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.alternateIdentifiers.item'/></span>
                                    </a>
                                </div>
                                <@input name="alternateIdentifiers" i18nkey="eml.alternateIdentifier" help="i18n"/>
                            </div>

                            <img id="baseimg" src="${baseURL}/logo.do?r=${resource.shortname}" style="display:none;"/>
                        </div>
                    </div>
                </main>
            </div>
        </div>
    </form>
    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
