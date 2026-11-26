<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header_setup.ftl">
<#include "/WEB-INF/pages/inc/setup_page.ftl">

<@setupPage step=2 action="setupMode.do"; part>
    <#if part == "alerts">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    <#else>
        <section class="ov-card">
            <p class="su-intro"><@s.text name="admin.config.setup2.mode.help"/></p>
            <div class="su-choices<#if setupHasError('modeSelected')> is-invalid</#if>" role="radiogroup" aria-label="<@s.text name='admin.config.setup2.mode'/>">
                <#list modes as mode>
                    <label class="su-choice" for="mode${mode}">
                        <input class="form-check-input" type="radio" name="modeSelected" id="mode${mode}" value="${mode}"<#if modeSelected?? && mode == modeSelected> checked</#if>>
                        <span>
                            <span class="su-choice-title"><@s.text name="admin.config.setup2.mode.${mode?lower_case}.label"/></span>
                            <@s.text name="admin.config.setup2.mode.${mode?lower_case}"/>
                        </span>
                    </label>
                </#list>
            </div>
            <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="modeSelected"/>
        </section>
    </#if>
</@setupPage>

<#include "/WEB-INF/pages/inc/footer.ftl">
