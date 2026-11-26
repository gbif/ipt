<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header_setup.ftl">
<#include "/WEB-INF/pages/inc/setup_page.ftl">

<@setupPage step=3 action="setupPublicUrl.do"; part>
    <#if part == "alerts">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    <#else>
        <input type="hidden" name="setupPublicUrl" value="true">
        <input type="hidden" name="setupProxyUrl" value="true">
        <section class="ov-card" aria-labelledby="su-address-title">
            <div class="ov-ch"><h2 id="su-address-title"><@s.text name="admin.config.section.address"/></h2></div>
            <div class="ov-sub"><@s.text name="admin.config.section.address.help"/></div>
            <@setupField name="baseURL" key="admin.config.baseUrl" value=baseUrlInput! hint="admin.config.baseUrl.hint" extra='inputmode="url" autocomplete="off" autofocus'/>
            <@setupField name="proxy" key="admin.config.proxy" value=proxy! optional=true hint="admin.config.setup.proxy.hint" extra='inputmode="url" autocomplete="off" placeholder="http://proxy.example.org:8080"'/>
        </section>
    </#if>
</@setupPage>

<#include "/WEB-INF/pages/inc/footer.ftl">
