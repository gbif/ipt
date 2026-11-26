<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header_setup.ftl">
<#include "/WEB-INF/pages/inc/setup_page.ftl">

<@setupPage step=1 action="setupDefaultAdministrator.do"; part>
    <#if part == "alerts">
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    <#else>
        <input type="hidden" name="setupDefaultAdministrator" value="true">
        <section class="ov-card">
            <p class="su-intro"><@s.text name="admin.config.setup2.administrator.help"/></p>
            <div class="su-grid">
                <@setupField name="user.firstname" key="user.firstname" value=(user.firstname)! extra='autocomplete="given-name" autofocus'/>
                <@setupField name="user.lastname" key="user.lastname" value=(user.lastname)! extra='autocomplete="family-name"'/>
                <div class="su-row">
                    <@setupField name="user.email" key="user.email" value=(user.email)! type="email" extra='autocomplete="email"'/>
                </div>
                <@setupField name="user.password" key="user.password" type="password" extra='autocomplete="new-password"'/>
                <@setupField name="password2" key="user.password2" type="password" extra='autocomplete="new-password"'/>
            </div>
        </section>
    </#if>
</@setupPage>

<#include "/WEB-INF/pages/inc/footer.ftl">
