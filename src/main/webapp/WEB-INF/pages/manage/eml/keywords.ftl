<#escape x as x?html>
    <#setting number_format="#####.##">
    <#include "/WEB-INF/pages/inc/header.ftl">
    <title><@s.text name='manage.metadata.keywords.title'/></title>
    <#include "/WEB-INF/pages/macros/metadata.ftl"/>
    <#assign auxTopNavbar=true />
    <#assign auxTopNavbarPage = "metadata" />
    <#assign currentMenu="manage"/>
    <script>
        $(document).ready(function(){
            initHelp();
        });
    </script>
    <#include "/WEB-INF/pages/inc/menu.ftl">
    <#include "/WEB-INF/pages/macros/forms.ftl"/>

<main class="container">
    <div class="my-3 p-3 bg-body rounded shadow-sm">

        <#include "/WEB-INF/pages/inc/action_alerts.ftl">

        <h5 class="border-bottom pb-2 mb-2 mx-md-4 mx-2 pt-2 text-gbif-header text-center">
            <@s.text name='manage.metadata.keywords.title'/>:
            <a href="resource.do?r=${resource.shortname}" title="${resource.title!resource.shortname}">${resource.title!resource.shortname}</a>
        </h5>

        <p class="mx-md-4 mx-2 mb-0">
            <@s.text name='manage.metadata.keywords.intro'/>
        </p>


        <div id="items">
            <#list eml.keywords as item>
                <div id="item-${item_index}" class="item row g-3 mx-md-3 mx-1 border-bottom pb-3 mt-1">
                    <div class="newline"></div>
                    <div class="d-flex justify-content-end">
                        <a id="removeLink-${item_index}" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.keywords.item'/> ]</a>
                    </div>
                    <@input name="eml.keywords[${item_index}].keywordThesaurus" i18nkey="eml.keywords.keywordThesaurus" help="i18n" requiredField=true />
                    <#-- work around for a bug that converts empty keywordsList into string "null". In this case, nothing should appear in text box -->
                    <#-- TODO: remove check for "null" after fixing problem in gbif-metadata-profile -->
                    <#assign keywordList = item.keywordsString />
                    <#if keywordList?has_content && keywordList?lower_case == "null">
                        <@text value="" name="eml.keywords[${item_index}].keywordsString" i18nkey="eml.keywords.keywordsString" help="i18n" requiredField=true/>
                    <#else>
                        <@text name="eml.keywords[${item_index}].keywordsString" i18nkey="eml.keywords.keywordsString" help="i18n" requiredField=true/>
                    </#if>
                </div>
            </#list>
        </div>

        <div class="addNew col-12 mx-md-4 mx-2 mt-1">
            <a id="plus" href=""><@s.text name='manage.metadata.addnew'/> <@s.text name='manage.metadata.keywords.item'/></a>
        </div>

        <div class="buttons col-12 mx-md-4 mx-2 mt-3">
            <@s.submit cssClass="button btn btn-outline-gbif-primary" name="save" key="button.save" />
            <@s.submit cssClass="button btn btn-outline-secondary" name="cancel" key="button.cancel" />
        </div>

        <!-- internal parameter -->
        <input name="r" type="hidden" value="${resource.shortname}" />


        <div id="baseItem" class="item row g-3 mx-md-3 mx-1 border-bottom pb-3 mt-1" style="display:none;">
            <div class="d-flex justify-content-end mt-0">
                <a id="removeLink" class="removeLink" href="">[ <@s.text name='manage.metadata.removethis'/> <@s.text name='manage.metadata.keywords.item'/> ]</a>
            </div>
            <@input name="keywordThesaurus" i18nkey="eml.keywords.keywordThesaurus" help="i18n" requiredField=true/>
            <@text name="keywordsString" i18nkey="eml.keywords.keywordsString" help="i18n" requiredField=true/>
        </div>
    </div>
</main>
    </form>

    <#include "/WEB-INF/pages/inc/footer.ftl">
</#escape>
