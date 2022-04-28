<#-- @ftlvariable name="" type="org.gbif.ipt.action.manage.TranslationAction" -->
<#escape x as x?html>
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name="manage.translation.title"/></title>
    <script src="${baseURL}/js/jconfirmation.jquery.js"></script>

    <script>
        $(document).ready(function(){
            $('.confirm').jConfirmAction({titleQuestion : "<@s.text name="basic.confirm"/>", yesAnswer : "<@s.text name="basic.yes"/>", cancelAnswer : "<@s.text name="basic.no"/>", buttonType: "danger"});
            $("table input").focus(function() {
                $(this).parent().parent().addClass("highlight");
            });
            $("table input").blur(function() {
                $(this).parent().parent().removeClass("highlight")
            });
            //Hack needed for Internet Explorer X.*x
            $('.reload').each(function() {
                $(this).click(function() {
                    window.location = $(this).parent('a').attr('href');
                });
            });
            $('.automap').each(function() {
                $(this).click(function() {
                    window.location = $(this).parent('a').attr('href');
                });
            });
            $('.cancel').each(function() {
                $(this).click(function() {
                    window.location = $(this).parent('a').attr('href');
                });
            });
            // end hack
            <#-- use vocabulary -->
            <#if (vocabTermsSize>0)>
            var vocab = [<#list vocabTermsKeys as code>{"value":"${code?replace('"','\"')}","label":"${vocabTerms[code]}"},</#list>];
            $("#translation input").autocomplete({
                source: vocab
            })
            </#if>
        });
    </script>
    <#assign currentMenu = "manage"/>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>
    <#include "/WEB-INF/pages/macros/manage/translation_buttons.ftl"/>

<div class="container-fluid bg-body border-bottom">
    <div class="container my-3">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    </div>

    <div class="container my-3 p-3">
        <div class="text-center text-uppercase fw-bold fs-smaller-2">
            <@s.text name="basic.resource"/>
        </div>

        <div class="text-center">
            <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-2 fw-normal">
                <@s.text name="manage.translation.title"/>
            </h1>
        </div>

        <div class="mt-2 text-center">
            <@buttons/>
        </div>
    </div>
</div>

<main class="container">
    <form class="topForm" action="translation.do" method="post">
        <div class="my-3 p-3">
            <p><@s.text name="manage.translation.intro"/></p>
        </div>

        <div class="my-3 p-3">
            <h5 class="pb-2 mb-2 pt-2 text-gbif-header-2 fw-400">
                <@s.text name="manage.translation.property"/> <em>${property.name}</em>
            </h5>

            <p>&quot;${property.description!}&quot;</p>

            <#if property.vocabulary?has_content>
                <p>
                    <strong><@s.text name="manage.translation.vocabulary.required"/></strong>:
                    <@s.text name="manage.translation.vocabulary.required.intro"/>
                </p>
                <p>
                    <em>${property.vocabulary.title!property.vocabulary.uriString}</em>:
                    <a href="vocabulary.do?id=${property.vocabulary.uriString}" class="no-text-decoration" target="_blank">
                        <i class="bi bi-book"></i>
                    </a>
                    &quot;${property.vocabulary.description!}&quot;
                </p>
            </#if>
        </div>

        <div class="my-3 p-3">
            <input type="hidden" name="r" value="${resource.shortname}"/>
            <input type="hidden" name="rowtype" value="${property.extension.rowType}"/>
            <input type="hidden" name="mid" value="${mid}"/>
            <input type="hidden" name="term" value="${property.qualname}"/>

            <div class="table-responsive text-smaller">
                <table id="translation" class="simple table">
                <colgroup>
                    <col width="400">
                    <!-- do not show column if term does not relate to vocabulary -->
                    <#if (vocabTermsSize>0)>
                        <col width="16">
                    </#if>
                    <col width="400">
                </colgroup>
                <tr>
                    <th><@s.text name="manage.translation.source.value"/></th>
                    <!-- do not show column if term does not relate to vocabulary -->
                    <#if (vocabTermsSize>0)>
                        <th></th>
                    </#if>
                    <th><@s.text name="manage.translation.translated.value"/></th>
                </tr>
                <#list sourceValuesMap?keys as k>
                    <tr>
                        <td class="align-middle py-1">${sourceValuesMap.get(k)!}</td>
                        <!-- do not show column if term does not relate to vocabulary -->
                        <#if (vocabTermsSize>0)>
                            <td class="align-middle py-1">
                                <#if vocabTerms[tmap.get(k)!k]??>
                                    <i class="bi bi-check-circle text-gbif-primary"></i>
                                <#else>
                                    <i class="bi bi-exclamation-circle text-gbif-danger"></i>
                                </#if>
                            </td>
                        </#if>
                        <td class="py-1">
                            <input type="text" class="form-control form-control-sm" name="tmap['${k}']" value="${tmap.get(k)!}"/>
                        </td>
                    </tr>
                </#list>
            </table>
            </div>
        </div>
    </form>

</main>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
