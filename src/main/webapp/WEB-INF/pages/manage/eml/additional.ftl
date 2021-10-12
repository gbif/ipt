<#escape x as x?html>
    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <script src="${baseURL}/js/ajaxfileupload.js"></script>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>
    <title><@s.text name='manage.metadata.additional.title'/></title>
    <script>
        $(document).ready(function () {
            $("#buttonUpload").click(function () {
                return ajaxFileUpload();
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

        });
    </script>
    <#assign auxTopNavbar=true />
    <#assign auxTopNavbarPage = "metadata" />
    <#assign currentMenu="manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

    <main class="container">
        <div class="my-3 p-3 bg-body rounded shadow-sm">

            <#include "/WEB-INF/pages/inc/action_alerts.ftl">

            <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
                <@s.text name='manage.metadata.additional.title'/>:
                <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
            </h5>

            <p class="mx-md-4 mx-2 mb-0">
                <@s.text name='manage.metadata.additional.intro'/>
            </p>

            <!-- Resource Logo -->
            <div id="logofields" class="row g-3 mx-md-3 mx-1 pb-1 mt-1">
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

                <div class="col-lg-9">
                    <@input name="eml.logoUrl" i18nkey="eml.logoUrl" help="i18n" type="url" />
                    <@s.file cssClass="form-control my-1" name="file"/>
                    <button class="button btn btn-outline-gbif-primary" id="buttonUpload">
                        <@s.text name="button.upload"/>
                    </button>
                </div>

                <div class="col-lg-3 d-flex justify-content-start align-items-center">
                    <div id="resourcelogo">
                        <#if resource.eml.logoUrl?has_content>
                            <img src="${resource.eml.logoUrl}" />
                        </#if>
                    </div>
                </div>
            </div>

            <div class="row g-3 mx-md-3 mx-1 pb-3 mt-1">
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

        <div class="my-3 p-3 bg-body rounded shadow-sm">
            <!-- Alternative identifiers -->
            <div class="listBlock">
                <@textinline name="manage.metadata.alternateIdentifiers.title" help="i18n"/>
                <div id="items">
                    <#list eml.alternateIdentifiers as item>
                        <div id="item-${item_index}" class="item row g-3 mx-md-3 mx-1 border-bottom pb-3 mt-1">
                            <div class="mt-1 d-flex justify-content-end">
                                <a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.alternateIdentifiers.item'/> ]</a>
                            </div>
                            <@input name="eml.alternateIdentifiers[${item_index}]" i18nkey="eml.alternateIdentifier" help="i18n"/>
                        </div>
                    </#list>
                </div>

                <div class="addNew col-12 mx-md-4 mx-2 mt-1">
                    <a id="plus" href="">[ <@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.alternateIdentifiers.item'/> ]</a>
                </div>

                <div id='buttons' class="buttons col-12 mx-md-4 mx-2 mt-3">
                    <@s.submit cssClass="button btn btn-outline-gbif-primary" name="save" key="button.save"/>
                    <@s.submit cssClass="button btn btn-outline-secondary" name="cancel" key="button.cancel"/>
                </div>

            </div>

            <!-- internal parameters needed by ajaxFileUpload.js - do not remove -->
            <input id="r" name="r" type="hidden" value="${resource.shortname}" />
            <input id="validate" name="validate" type="hidden" value="false" />

            <div id="baseItem" class="item clearfix row g-3 mx-md-3 mx-1 border-bottom pb-3 mt-1" style="display:none;">
                <div class="mt-1 d-flex justify-content-end">
                    <a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.alternateIdentifiers.item'/> ]</a>
                </div>
                <@input name="alternateIdentifiers" i18nkey="eml.alternateIdentifier" help="i18n"/>
            </div>

            <img id="baseimg" src="${baseURL}/logo.do?r=${resource.shortname}" style="display:none;"/>
        </div>
    </main>

    </form>
    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
