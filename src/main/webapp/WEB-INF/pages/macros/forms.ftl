<#ftl output_format="HTML">

    <#macro input name value="-99999" i18nkey="" errorfield="" type="text" size=-1 disabled=false help="" helpOptions=[] date=false requiredField=false maxlength=-1>
        <#if date>
            <#include "/WEB-INF/pages/macros/form_field_label.ftl">
            <#include "/WEB-INF/pages/macros/help_icon.ftl">
            <div class="calendarInfo">
                <input
                        class="form-control"
                        type="${type}"
                        id="${name}"
                        name="${name}"
                        aria-describedby="calendar-${name}"
                        value="<#if value=="-99999"><@s.property value="${name}"/><#else>${value}</#if>"
                        <#if (size>0)>size="${size}"</#if>
                        <#if (maxlength>0)>maxlength="${maxlength}"</#if>
                        <#if disabled>readonly="readonly"</#if>
                        <#if requiredField>required</#if>
                />
                <#include "/WEB-INF/pages/macros/form_field_error.ftl">
            </div>
        <#else>
            <div>
                <div class="d-flex">
                    <#include "/WEB-INF/pages/macros/form_field_label.ftl">
                    <#include "/WEB-INF/pages/macros/help_icon.ftl">
                </div>
                <input
                        class="form-control"
                        type="${type}"
                        id="${name}"
                        name="${name}"
                        value="<#if value=="-99999"><@s.property value="${name}"/><#else>${value}</#if>"
                        <#if (size>0)>size="${size}"</#if>
                        <#if (maxlength>0)>maxlength="${maxlength}"</#if>
                        <#if disabled>readonly="readonly"</#if>
                        <#if requiredField>required</#if>
                />
                <#include "/WEB-INF/pages/macros/form_field_error.ftl">
            </div>
        </#if>
    </#macro>

    <#macro text name value="-99999" i18nkey="" errorfield="" size=40 rows=5 disabled=false help="" requiredField=false minlength=-1 maxlength=-1>
        <div>
            <div class="d-flex">
                <#include "/WEB-INF/pages/macros/form_field_label.ftl">
                <#include "/WEB-INF/pages/macros/help_icon.ftl">
            </div>
            <textarea id="${name}" class="form-control" name="${name}" cols="${size}" rows="${rows}"<#rt>
                      <#if (minlength>0)> minlength="${minlength}"</#if><#t>
                      <#if (maxlength>0)> maxlength="${maxlength}"</#if><#t>
                      <#if requiredField> required</#if><#t>
                      <#if disabled> readonly="readonly"</#if>><#t>
                      <#if value=="-99999"><#t>
                          <@s.property value="${name}"/><#t>
                      <#else><#t>
                          ${value}<#t>
                      </#if><#t>
            </textarea><#lt>
            <#include "/WEB-INF/pages/macros/form_field_error.ftl">
        </div>
    </#macro>

    <#-- has no label or help icon, and is used exclusively on basic metadata page description textareas -->
    <#macro simpleText name value="-99999" errorfield="" size=40 rows=5 disabled=false requiredField=false minlength=-1 maxlength=-1>
        <div>
            <textarea id="${name}" class="form-control" name="${name}" cols="${size}" rows="${rows}" class="basic"<#rt>
                      <#if (minlength>0)> minlength="${minlength}"</#if><#t>
                      <#if (maxlength>0)> maxlength="${maxlength}"</#if><#t>
                      <#if requiredField> required</#if><#t>
                      <#if disabled> readonly="readonly"</#if>><#t>
                      <#if value=="-99999"><@s.property value="${name}"/><#else>${value}</#if><#t>
            </textarea><#lt>
            <#include "/WEB-INF/pages/macros/form_field_error.ftl">
        </div>
    </#macro>

    <#macro textinline name value="-99999" i18nkey="" errorfield="" help="" requiredField=false>
        <div class="textinline">
            <h5 class="border-bottom pb-2 mb-0 mx-md-4 mx-2 pt-2 text-gbif-header fw-400">
                <#include "/WEB-INF/pages/macros/help_icon.ftl">
                <span><@s.text name="${name}"/><#if requiredField>&#42;</#if></span>
            </h5>
        </div>
    </#macro>

    <#macro link name value="" href="" class="" i18nkey="" help="" errorfield="">
        <div>
            <#include "/WEB-INF/pages/macros/help_icon.ftl">
            <a id="${name}" name="${name}" class="${class}" href="${href}"><@s.text name="${value}"/></a>
        </div>
    </#macro>

    <#macro select name options value="" i18nkey="" errorfield="" size=1 disabled=false help="" includeEmpty=false javaGetter=true requiredField=false>
        <div>
            <div class="d-flex">
                <#include "/WEB-INF/pages/macros/form_field_label.ftl">
                <#include "/WEB-INF/pages/macros/help_icon.ftl">
            </div>
            <select name="${name}" id="${name}" size="${size}" class="form-select" <#if disabled>readonly="readonly"</#if> <#if requiredField>required</#if>>
                <#if includeEmpty>
                    <option value="" <#if (value!"")==""> selected="selected"</#if>></option>
                </#if>
                <#list options?keys as val>
                    <option value="${val}" <#if (value!"")==""+val> selected="selected"</#if>>
                        <#if javaGetter><@s.text name="${options.get(val)}"/><#else><@s.text name="${options[val]}"/></#if>
                    </option>
                </#list>
            </select>
            <#include "/WEB-INF/pages/macros/form_field_error.ftl">
        </div>
    </#macro>

    <#macro selectList name options objValue objTitle value="" i18nkey="" errorfield="" size=1 disabled=false help="" includeEmpty=false requiredField=false>
        <div>
            <div class="d-flex">
                <#include "/WEB-INF/pages/macros/form_field_label.ftl">
                <#include "/WEB-INF/pages/macros/help_icon.ftl">
            </div>
            <@s.select id=name class="form-select" name=name list=options listKey=objValue listValue=objTitle value=value size=size disabled=disabled emptyOption=includeEmpty/>
            <#include "/WEB-INF/pages/macros/form_field_error.ftl">
        </div>
    </#macro>

    <#macro checkbox name i18nkey="" errorfield="" disabled=false value="-99999" help="" requiredField=false>
        <div class="form-check">
            <#if value=="-99999">
                <#assign val><@s.property value="${name}"/></#assign>
                <@s.checkbox cssClass="form-check-input" key="${name}" id="${name}" disabled=disabled value=val />
            <#else>
                <@s.checkbox cssClass="form-check-input" key="${name}" id="${name}" disabled=disabled value=value />
            </#if>
            <div class="d-flex">
                <#include "/WEB-INF/pages/macros/form_checkbox_label.ftl">
                <#include "/WEB-INF/pages/macros/help_icon.ftl">
            </div>
            <#include "/WEB-INF/pages/macros/form_field_error.ftl">
        </div>
    </#macro>

    <#macro readonly name i18nkey value size=-1 help="" errorfield="" requiredField=false>
        <div class="d-flex">
            <#include "/WEB-INF/pages/macros/form_field_label.ftl">
            <#include "/WEB-INF/pages/macros/help_icon.ftl">
        </div>
        <input type="text" class="form-control" value="${value}" <#if (size>0)>size="${size}"</#if> readonly="readonly"/>
        <#include "/WEB-INF/pages/macros/form_field_error.ftl">
    </#macro>

    <#macro label i18nkey help="" requiredField=false>
        <div>
            <label>
                <@s.text name="${i18nkey}"/><#if requiredField>&#42;</#if>
            </label>
            <#nested>
        </div>
    </#macro>

    <#--
      In the textWithFormattedLink macro, each word in view source will appear on a separate line. This fix this issue
      http://code.google.com/p/gbif-providertoolkit/issues/detail?id=856 the <#t> trim directive is used to ensure that
      the word list gets displayed as a complete line instead.
    -->
    <#macro textWithFormattedLink text>
    <#-- replace less than and greater than characters -->
        <#assign sanitized = text?replace("<", "&lt;")?replace(">", "&gt;")>
        <#assign words = sanitized?word_list>
        <#list words as x>
            <#assign res = x?matches("(http(s)?|ftp)://(([\\w-]+\\.)?)+[\\w-]+(:\\d+)?+(/[\\w- ./-?%&=]*)?")>
            <#assign flag=false>
            <#list res as m>
                <#if x?contains(m)><a href="${m}">${x}</a> <#t><#assign flag = true><#break></#if>
            </#list>
            <#if flag==false>${x}</#if> <#t>
        </#list>
    </#macro>

    <#macro showMore text maxLength>
        <#if (text?length>maxLength)>
            <div id="visibleContent">
                <@textWithFormattedLink (text)?substring(0,maxLength)/>... <a id="showMore" href=""><@s.text name='basic.showMore'/></a>
            </div>
            <div id="hiddenContent" style="display: none">
                <@textWithFormattedLink text/>
                <a id="showLess" href=""><@s.text name='basic.showLess'/></a>
            </div>
        <#else>
            <@textWithFormattedLink text/>
        </#if>
    </#macro>

    <#-- Adds the CC icon. Only CC-BY-NC, CC-BY, and CC0 are supported -->
    <#macro licenseLogoClass rights>
        <#if rights?contains("CC-BY-NC")>
            <a rel="license" id="cc_by_nc" class="cc_logo cc_by_nc"
               href="http://creativecommons.org/licenses/by-nc/4.0/legalcode"
               title="Creative Commons Attribution Non Commercial (CC-BY-NC) 4.0 License">&nbsp;</a>
        <#elseif rights?contains("CC-BY")>
            <a rel="license" id="cc_by" class="cc_logo cc_by"
               href="http://creativecommons.org/licenses/by/4.0/legalcode"
               title="Creative Commons Attribution (CC-BY) 4.0 License">&nbsp;</a>
        <#elseif rights?contains("CC0")>
            <a rel="license" id="cc_zero" class="cc_logo cc_zero"
               href="http://creativecommons.org/publicdomain/zero/1.0/legalcode"
               title="Creative Commons CCZero (CC0) 1.0 License">&nbsp;</a>
        </#if>
    </#macro>
