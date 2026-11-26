<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header_setup.ftl">
<#include "/WEB-INF/pages/inc/setup_page.ftl">

<@setupPage step=0 action="setupDataDirectory.do"; part>
    <#if part == "alerts">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    <#else>
        <section class="ov-card">
            <p class="su-intro"><@s.text name="admin.config.setup.welcome"/></p>
            <div class="ov-banner info compact">
                <span class="ov-ic"><@icon name="info_outlined"/></span>
                <div class="ov-grow"><@s.text name="admin.config.setup.instructions"/></div>
            </div>
            <@setupField name="dataDirPath" key="admin.config.setup.datadir" value=dataDirPath! hint="admin.config.setup.examples" extra='autocomplete="off" autofocus'/>
        </section>
    </#if>
</@setupPage>

<#include "/WEB-INF/pages/inc/footer.ftl">
