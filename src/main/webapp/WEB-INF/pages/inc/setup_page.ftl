<#ftl output_format="HTML">
<#-- Setup wizard layout (admin/setup_*.ftl): heading with the IPT name and tagline, the steps strip, the step's cards,
     Continue. Include after header_setup.ftl. -->
<#include "/WEB-INF/pages/macros/icon.ftl"/>

<#assign setupSteps = ["admin.config.server.data.dir", "admin.config.setup2.administrator.title", "admin.config.setup2.mode.title", "admin.config.setup2.publicURL.title", "admin.config.setup3.installationComplete"]/>

<#function setupHasError name><#return (fieldErrors[name])?? && fieldErrors[name]?has_content></#function>

<#-- the Continue button, under the step's cards (the heading carries the IPT name and tagline, not the step) -->
<#macro setupContinue submitName>
    <button type="submit" name="${submitName}" value="true" class="btn btn-sm btn-gbif-solid top-button"><@s.text name="button.continue"/> <@icon name="arrow_forward"/></button>
</#macro>

<#-- a whole step page. step: 0-based index into setupSteps; action: the form's action; complete: tick the current step
     too (the last step, when setup went fine). The page's nested content is called twice: with "alerts" for the
     messages above the heading, then with "content" for the step's cards -->
<#macro setupPage step action submitName="save" complete=false>
    <div class="container px-0">
        <#nested "alerts">
    </div>

    <form action="${action}" method="post" novalidate>
        <div class="container-fluid border-bottom">
            <div class="container bg-body border rounded-2 mb-4">
                <div class="container my-3 p-3 ov-head ov-head-left">
                    <div class="ov-grow">
                        <div class="fs-smaller su-kicker"><@s.text name="admin.config.setup.common.setup"/></div>
                        <h1 class="pb-2 mb-0 pt-2 text-gbif-header fs-4 fw-normal"><@s.text name="admin.config.setup.common.ipt"/></h1>
                        <div class="ov-meta su-tagline"><@s.text name="admin.config.setup.common.tagline"/></div>
                    </div>
                </div>
            </div>
        </div>

        <div class="container-fluid bg-body">
            <div class="container main-content-container">
                <main class="pt-4 pb-5 st-main su-main">
                    <@setupStepper step=step complete=complete/>
                    <h2 class="visually-hidden"><@s.text name=setupSteps[step]/></h2>
                    <#nested "content">
                    <div class="su-foot">
                        <@setupContinue submitName=submitName/>
                    </div>
                </main>
            </div>
        </div>
    </form>
</#macro>

<#-- the steps as a strip above the cards, as on the publishing page: done ones ticked, the current one tinted and
     underlined with its number in the primary colour (ticked too once setup is complete), later ones numbered in grey -->
<#macro setupStepper step complete=false>
    <ol class="su-steps" aria-label="<@s.text name='admin.config.setup.title'/>">
        <#list setupSteps as key>
            <#assign ticked = key_index < step || (complete && key_index == step)/>
            <#assign state = (key_index == step)?then("active", ticked?then("done", "todo"))/>
            <li class="su-s ${state}<#if ticked && key_index == step> done</#if>"<#if state == "active"> aria-current="step"</#if>>
                <span class="su-s-ic"><#if ticked><@icon name="check_circle"/><#else><span class="su-s-num">${key_index + 1}</span></#if></span>
                <span class="su-s-label"><@s.text name=key/></span>
            </li>
        </#list>
    </ol>
</#macro>

<#-- a text field as on the settings page: label ("optional" when it can stay empty), input, its server error, a hint -->
<#macro setupField name key value="" optional=false hint="" type="text" extra="">
    <div class="st-field">
        <label for="${name}" class="st-label"><@s.text name=key/><#if optional> <span class="st-opt"><@s.text name="admin.config.optional"/></span></#if></label>
        <input type="${type}" class="form-control form-control-sm<#if setupHasError(name)> is-invalid</#if>" id="${name}" name="${name}" value="${value}" spellcheck="false" ${extra?no_esc}>
        <@s.fielderror cssClass="invalid-feedback d-block list-unstyled my-1" fieldName="${name}"/>
        <#if hint?has_content><div class="st-hint"><@s.text name=hint/></div></#if>
    </div>
</#macro>
