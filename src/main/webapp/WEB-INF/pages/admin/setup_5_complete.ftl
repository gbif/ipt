<#ftl output_format="HTML">
<#include "/WEB-INF/pages/inc/header_setup.ftl">
<#include "/WEB-INF/pages/inc/setup_page.ftl">

<#assign setupOk = warnings?size == 0 && actionErrors?size == 0/>

<#macro nextStep href iconName title description>
    <li>
        <@icon name=iconName/>
        <div><a href="${baseURL}${href}"><@s.text name=title/></a><small><@s.text name=description/></small></div>
    </li>
</#macro>

<@setupPage step=4 action="setupComplete.do" submitName="continue" complete=setupOk; part>
    <#if part == "alerts">
        <#-- step 4's "setup complete" and "review the Administration section" messages are left out: the result card
             and Next steps say the same. Others stay, e.g. step 1's "existing data directory" when setup skips ahead -->
        <#assign repeated = [action.getText("admin.config.setup2.success"), action.getText("admin.config.setup2.next")]/>
        <#assign actionMessages = actionMessages?filter(m -> !repeated?seq_contains(m))/>
        <#include "/WEB-INF/pages/inc/action_alerts.ftl">
    <#else>
        <section class="ov-card">
            <#if setupOk>
                <div class="su-done ok"><@icon name="check_circle"/><div><@s.text name="admin.config.setup3.welcome"/></div></div>
            <#else>
                <div class="su-done warn"><@icon name="warning"/><div><@s.text name="admin.config.setup3.welcomeWithIssues"/></div></div>
            </#if>
        </section>

        <section class="ov-card" aria-labelledby="su-next-title">
            <div class="ov-ch"><h2 id="su-next-title"><@s.text name="admin.config.setup.nextSteps"/></h2></div>
            <ul class="su-next">
                <@nextStep href="/admin/registration.do" iconName="cloud_upload" title="admin.home.editRegistration" description="admin.home.editRegistration.description"/>
                <@nextStep href="/admin/users.do" iconName="people" title="admin.home.manageUsers" description="admin.home.manageUsers.description"/>
                <@nextStep href="/admin/extensions.do" iconName="extension" title="admin.home.manageExtensions" description="admin.home.manageExtensions.description"/>
                <@nextStep href="/admin/config.do" iconName="settings" title="admin.home.editConfig" description="admin.home.editConfig.description"/>
            </ul>
        </section>
    </#if>
</@setupPage>

<#include "/WEB-INF/pages/inc/footer.ftl">
